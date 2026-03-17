package fr.iutrodez.a4awalk.SuiviParcour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;

public class SuiviParcours extends AppCompatActivity {

    // ===== Constantes API =====
    private static final String COURSE_ID_FALLBACK = "6989e2f5a5b0b8078ee29a24";
    private static final String BASE_URL = "http://98.94.8.220:8080/courses/";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    public TokenManager tokenManager;

    // ===== ID du parcours courant (depuis Intent ou fallback) =====
    private String currentCourseId;

    // ===== UI =====
    private MapView mapView;
    private TextView tvDate;
    private TextView tvRandonnee;
    private TextView tvNomParcours;
    private Button btnPause;
    private Button btnTerminer;
    private ImageButton btnRetour;

    // ===== Managers =====
    private MapManager mapManager;
    private DistanceManager distanceManager;
    private LocationManager locationManager;
    private AudioManager audioManager;

    // ===== Parcours =====
    private List<GeoPoint> parcoursPoints = new ArrayList<>();
    private boolean isPaused = false;

    // ===== Tracking =====
    private RequestQueue requestQueue;
    private List<Location> locationBuffer = new ArrayList<>();
    private Handler trackingHandler = new Handler();
    private Runnable trackingRunnable;
    private Location lastKnownLocation;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.details_parcours);

        tokenManager = new TokenManager(this);
        // ===== Récupération du courseId depuis l'Intent =====
        String intentCourseId = getIntent().getStringExtra("COURSE_ID");
        currentCourseId = (intentCourseId != null && !intentCourseId.isEmpty())
                ? intentCourseId
                : COURSE_ID_FALLBACK;

        String dateRealisation = getIntent().getStringExtra("DATE_REALISATION");
        String nomRandonnee = getIntent().getStringExtra("NOM_RANDONNEE");
        String nomParcours = getIntent().getStringExtra("NOM_PARCOURS");

        initViews();

        if (dateRealisation != null) tvDate.setText(dateRealisation);
        if (nomRandonnee != null) tvRandonnee.setText(nomRandonnee);
        if (nomParcours != null) tvNomParcours.setText(nomParcours);

        initManagers();
        initParcours();
        initDistanceManager();
        initPauseButton();
        initTerminerButton();

        requestQueue = Volley.newRequestQueue(this);
        startTrackingTimer();

        verifierPermissionsEtDemarrer();
    }

    private void initViews() {
        mapView = findViewById(R.id.map);
        btnPause = findViewById(R.id.btnReprendre);
        btnTerminer = findViewById(R.id.btnTerminer);
        btnRetour = findViewById(R.id.btnRetour);

        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);
        tvNomParcours = findViewById(R.id.tvNomParcours);

        btnTerminer.setVisibility(View.GONE);

        btnRetour.setOnClickListener(v -> finish());

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
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
            public void onDistanceUpdated(float next, float end) {
                //runOnUiThread(() -> {
                //    tvProchainPoint.setText(formatDistanceProchain(next));
                //    tvArrivee.setText(formatDistanceArrivee(end));
                //});
            }

            @Override
            public void onPointValidated() {
                Toast.makeText(SuiviParcours.this, "Point validé", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApproachAlert(float distance) {
                Toast.makeText(SuiviParcours.this,
                        "Approche du point (" + (int) distance + " m)",
                        Toast.LENGTH_LONG).show();
                audioManager.play();
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

    private void initTerminerButton() {
        btnTerminer.setOnClickListener(v -> terminerRandonnee());
    }

    // =========================================================
    // ====================== GESTION PAUSE ====================
    // =========================================================

    private void pauseRandonnee() {
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);
            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        envoyerStatutPauseAPI(true);
        btnRetour.setVisibility(View.VISIBLE);
        locationManager.stopLocationUpdates();
        btnPause.setText("Reprendre la randonnée");
        isPaused = true;
    }

    private void reprendreRandonnee() {
        envoyerStatutPauseAPI(false);
        startLocation();
        btnPause.setText("Mettre en pause");
        isPaused = false;
    }

    private void terminerRandonnee() {
        if (lastKnownLocation != null) {
            ajouterPositionBuffer(lastKnownLocation);
            if (!locationBuffer.isEmpty()) {
                envoyerPositionsAPI();
                locationBuffer.clear();
            }
        }

        envoyerFinRandoneeAPI();
        locationManager.stopLocationUpdates();
        btnPause.setEnabled(false);
        btnTerminer.setEnabled(false);
        isPaused = true;
    }

    // =========================================================
    // ====================== LOCALISATION =====================
    // =========================================================

    private void verifierPermissionsEtDemarrer() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            startLocation();
        }
    }

    private void startLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    if (result == null) return;
                    for (Location loc : result.getLocations()) {
                        lastKnownLocation = loc;
                        GeoPoint userPos = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                        Drawable icon = ContextCompat.getDrawable(SuiviParcours.this, R.drawable.ic_pin_user);
                        mapManager.updateUserPosition(userPos, icon);
                        distanceManager.updateLocation(loc);
                    }
                }
            };
        }

        locationManager.startLocationUpdates(locationCallback);
    }

    // =========================================================
    // ====================== TRACKING TIMER ==================
    // =========================================================

    private void startTrackingTimer() {
        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && lastKnownLocation != null) {
                    ajouterPositionBuffer(lastKnownLocation);
                    if (locationBuffer.size() >= 10) {
                        envoyerPositionsAPI();
                        locationBuffer.clear();
                    }
                }
                trackingHandler.postDelayed(this, 60000);
            }
        };
        trackingHandler.post(trackingRunnable);
    }

    private void ajouterPositionBuffer(Location location) {
        locationBuffer.add(location);
        Toast.makeText(this,
                "Coordonnée ajoutée (" + locationBuffer.size() + "/10)",
                Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // ====================== APPELS API =======================
    // =========================================================

    private void envoyerPositionsAPI() {
        String url = BASE_URL + currentCourseId;
        JSONArray jsonArray = new JSONArray();

        try {
            for (Location loc : locationBuffer) {
                JSONObject obj = new JSONObject();
                obj.put("latitude", loc.getLatitude());
                obj.put("longitude", loc.getLongitude());
                jsonArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int nbPositions = locationBuffer.size();

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        nbPositions + " positions envoyées ✔",
                        Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur API : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public byte[] getBody() {
                return jsonArray.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + tokenManager.getToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void envoyerStatutPauseAPI(boolean paused) {
        ServiceParcours.changerStatutPause(this, tokenManager.getToken(), currentCourseId, paused, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Toast.makeText(SuiviParcours.this,
                        paused ? "Statut: En pause ⏸" : "Statut: En cours ▶",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(SuiviParcours.this,
                        "Erreur changement statut : " + error.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void envoyerFinRandoneeAPI() {
        String url = BASE_URL + currentCourseId + "/finish";

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        "Randonnée terminée ✔",
                        Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this,
                        "Erreur fin randonnée : " + error.toString(),
                        Toast.LENGTH_LONG).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + tokenManager.getToken());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // =========================================================
    // ====================== FORMATAGE ========================
    // =========================================================

    private String formatDistanceProchain(float meters) {
        return meters >= 1000
                ? String.format("Prochain point %.2f km", meters / 1000)
                : String.format("Prochain point %.0f m", meters);
    }

    private String formatDistanceArrivee(float meters) {
        return meters >= 1000
                ? String.format("Arrivée %.2f km", meters / 1000)
                : String.format("Arrivée %.0f m", meters);
    }

    // =========================================================
    // ====================== CALLBACKS ========================
    // =========================================================

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

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
        if (!isPaused) startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.release();
        trackingHandler.removeCallbacks(trackingRunnable);
    }
}