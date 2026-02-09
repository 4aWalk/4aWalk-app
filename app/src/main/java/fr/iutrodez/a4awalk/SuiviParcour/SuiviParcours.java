package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.R;

public class SuiviParcours extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private MapView mapView;
    private MapManager mapManager;
    private DistanceManager distanceManager;
    private LocationManager locationManager;
    private AudioManager audioManager;

    private TextView tvProchainPoint, tvArrivee;
    private MaterialButton btnPause;

    private List<GeoPoint> parcoursPoints = new ArrayList<>();

    private boolean isPaused = false;

    // 👉 On stocke le callback pour éviter d'en recréer
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.main_activity);

        initViews();
        initManagers();
        initParcours();
        initDistanceManager();
        initPauseButton();

        verifierPermissionsEtDemarrer();
    }

    // =====================================================
    // INITIALISATIONS
    // =====================================================

    private void initViews() {

        mapView = findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        tvProchainPoint = findViewById(R.id.tvProchainPoint);
        tvArrivee = findViewById(R.id.tvArrivee);
        btnPause = findViewById(R.id.btnPause);
    }

    private void initManagers() {
        mapManager = new MapManager(mapView, this);
        audioManager = new AudioManager(this, R.raw.notif);
        locationManager = new LocationManager(this);
    }

    private void initParcours() {

        parcoursPoints.add(new GeoPoint(44.360369301617794, 2.5758112393065384));
        parcoursPoints.add(new GeoPoint(44.351077610605785, 2.5740525086171298));
        parcoursPoints.add(new GeoPoint(44.34951392435509, 2.576044023961549));
        parcoursPoints.add(new GeoPoint(44.352487429572584, 2.5677165393061494));
        parcoursPoints.add(new GeoPoint(44.360369301617794, 2.5758112393065384));

        mapManager.addMarkers(parcoursPoints);
        mapManager.calculerItineraire(parcoursPoints);

        if (!parcoursPoints.isEmpty()) {
            mapView.getController().setZoom(14.0);
            mapView.getController().setCenter(parcoursPoints.get(0));
        }
    }

    private void initDistanceManager() {

        distanceManager = new DistanceManager(parcoursPoints);

        distanceManager.setListener(new DistanceListener() {

            @Override
            public void onDistanceUpdated(float distanceToNext, float distanceToEnd) {
                runOnUiThread(() -> {
                    tvProchainPoint.setText(formatDistanceProchain(distanceToNext));
                    tvArrivee.setText(formatDistanceArrivee(distanceToEnd));
                });
            }

            @Override
            public void onPointValidated() {
                runOnUiThread(() ->
                        Toast.makeText(SuiviParcours.this,
                                "Point validé !",
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onApproachAlert(float distance) {
                runOnUiThread(() -> {
                    Toast.makeText(SuiviParcours.this,
                            "Vous approchez du point (" + (int) distance + " m)",
                            Toast.LENGTH_LONG).show();
                    audioManager.play();
                });
            }
        });
    }

    private void initPauseButton() {

        btnPause.setOnClickListener(v -> {

            if (!isPaused) {
                pauseRandonnee();
            } else {
                reprendreRandonnee();
            }
        });
    }

    // =====================================================
    // GESTION RANDONNÉE
    // =====================================================

    private void pauseRandonnee() {
        locationManager.stopLocationUpdates();
        btnPause.setText("Reprendre la randonnée");
        isPaused = true;
    }

    private void reprendreRandonnee() {
        startLocation();
        btnPause.setText("Mettre en pause");
        isPaused = false;
    }

    // =====================================================
    // GESTION LOCALISATION
    // =====================================================

    private void verifierPermissionsEtDemarrer() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );

        } else {
            startLocation();
        }
    }

    private void startLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) return;

        // 👉 Créé une seule fois
        if (locationCallback == null) {

            locationCallback = new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult result) {

                    if (result == null) return;

                    for (Location loc : result.getLocations()) {

                        GeoPoint userPos =
                                new GeoPoint(loc.getLatitude(), loc.getLongitude());

                        Drawable icon = ContextCompat.getDrawable(
                                SuiviParcours.this,
                                R.drawable.ic_pin_user
                        );

                        mapManager.updateUserPosition(userPos, icon);
                        distanceManager.updateLocation(loc);
                    }
                }
            };
        }

        locationManager.startLocationUpdates(locationCallback);
    }

    // =====================================================
    // FORMAT DISTANCE
    // =====================================================

    private String formatDistanceProchain(float meters) {
        return meters >= 1000
                ? String.format("Prochain point dans %.2f km", meters / 1000)
                : String.format("Prochain point dans %.0f m", meters);
    }

    private String formatDistanceArrivee(float meters) {
        return meters >= 1000
                ? String.format("Arrivée dans %.2f km", meters / 1000)
                : String.format("Arrivée dans %.0f m", meters);
    }

    // =====================================================
    // PERMISSIONS
    // =====================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startLocation();
        }
    }

    // =====================================================
    // CYCLE DE VIE ANDROID
    // =====================================================

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        if (!isPaused) {
            startLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.release();
    }
}
