package fr.iutrodez.a4awalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.*;
import android.location.Location;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.*;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;

import java.util.*;

public class SuiviParcours extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userMarker;

    private List<GeoPoint> pointsParcours;
    private int indexProchainPoint = 0;

    private TextView tvProchainPoint;
    private TextView tvArrivee;

    private Location lastLocation = null;

    // On stocke la position où on a mis à jour le texte pour la dernière fois
    private Location locationDerniereMajTexte = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.main_activity);

        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        tvProchainPoint = findViewById(R.id.tvProchainPoint);
        tvArrivee = findViewById(R.id.tvArrivee);

        pointsParcours = new ArrayList<>();
        pointsParcours.add(new GeoPoint(44.360369301617794, 2.5758112393065384));
        pointsParcours.add(new GeoPoint(44.351077610605785, 2.5740525086171298));
        pointsParcours.add(new GeoPoint(44.34951392435509, 2.576044023961549));
        pointsParcours.add(new GeoPoint(44.352487429572584, 2.5677165393061494));
        pointsParcours.add(new GeoPoint(44.360369301617794, 2.5758112393065384));

        map.getController().setZoom(14.0);
        map.getController().setCenter(pointsParcours.get(0));

        ajouterMarkers(pointsParcours);
        calculerItineraire(pointsParcours);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        verifierPermissionsEtDemarrer();
    }

    /* ===================== PERMISSIONS ===================== */

    private void verifierPermissionsEtDemarrer() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            demarrerLocalisation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            demarrerLocalisation();
        }
    }

    /* ===================== LOCALISATION ===================== */

    private void demarrerLocalisation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Nouvelle API LocationRequest.Builder
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .setMaxUpdateDelayMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;

                for (Location location : result.getLocations()) {
                    // Filtrage précision
                    if (location.getAccuracy() > 25) continue;

                    // Micro-déplacements
                    if (lastLocation != null &&
                            location.distanceTo(lastLocation) < 3) continue;

                    lastLocation = location;

                    GeoPoint userPos = new GeoPoint(
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    mettreAJourMarker(userPos);
                    calculerDistances(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }


    /* ===================== MAP ===================== */

    private void mettreAJourMarker(GeoPoint userPos) {

        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setTitle("Vous êtes ici");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            Drawable d = ContextCompat.getDrawable(this, R.drawable.ic_pin_user);
            if (d != null) {
                Bitmap b = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                d.setBounds(0, 0, c.getWidth(), c.getHeight());
                d.draw(c);
                userMarker.setIcon(new BitmapDrawable(getResources(), b));
            }

            map.getOverlays().add(userMarker);
            map.getController().animateTo(userPos);
        }

        userMarker.setPosition(userPos);

        // 🔹 Recentrage intelligent
        IGeoPoint center = map.getMapCenter();
        if (center instanceof GeoPoint) {
            GeoPoint centerGeo = (GeoPoint) center;
            if (centerGeo.distanceToAsDouble(userPos) > 30) {
                map.getController().animateTo(userPos);
            }
        }


        map.invalidate();
    }

    /* ===================== DISTANCES ===================== */

    private void calculerDistances(Location location) {
        if (indexProchainPoint >= pointsParcours.size()) return; // Fin du parcours

        GeoPoint prochain = pointsParcours.get(indexProchainPoint);
        float[] d = new float[1];

        // Calcul de la distance vers le prochain point immédiat
        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                prochain.getLatitude(),
                prochain.getLongitude(),
                d
        );

        boolean pointChange = false;

        // Vérification : est-on arrivé au point ? (Rayon de 20m)
        if (d[0] < 20 && indexProchainPoint < pointsParcours.size() - 1) {
            indexProchainPoint++;
            pointChange = true; // On retient qu'on a changé de cible
            Toast.makeText(this, "Point validé !", Toast.LENGTH_SHORT).show();
        }

        // --- LOGIQUE DE MISE A JOUR DE L'AFFICHAGE ---

        // On calcule la distance parcourue depuis la dernière mise à jour du TEXTE
        float distanceDepuisDernierAffichage = 0;
        if (locationDerniereMajTexte != null) {
            distanceDepuisDernierAffichage = location.distanceTo(locationDerniereMajTexte);
        }

        // ON MET À JOUR L'AFFICHAGE SEULEMENT SI :
        // 1. C'est la toute première fois (locationDerniereMajTexte est null)
        // 2. OU on a parcouru plus de 100 mètres
        // 3. OU on vient de valider un point (pointChange est vrai) -> il faut forcer la mise à jour car la cible a changé !
        if (locationDerniereMajTexte == null || distanceDepuisDernierAffichage >= 100 || pointChange) {

            // On sauvegarde cette position comme référence pour les prochains 100m
            locationDerniereMajTexte = location;

            // On lance le calcul complet (avec la correction de logique vue précédemment)
            float distanceRestanteTotale = calculerDistanceTotaleRestante(location);

            // Si on a changé de point, on recalcule la distance vers le NOUVEAU point
            if (pointChange) {
                GeoPoint nouveauProchain = pointsParcours.get(indexProchainPoint);
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        nouveauProchain.getLatitude(), nouveauProchain.getLongitude(),
                        d // d[0] contient maintenant la distance vers le nouveau point
                );
            }

            mettreAJourAffichageDistances(d[0], distanceRestanteTotale);
        }
    }

    private float calculerDistanceTotaleRestante(Location location) {
        float total = 0;

        // 1. D'abord, on ajoute la distance entre MOI et le PROCHAIN point
        if (indexProchainPoint < pointsParcours.size()) {
            GeoPoint prochain = pointsParcours.get(indexProchainPoint);
            float[] distToNext = new float[1];
            Location.distanceBetween(
                    location.getLatitude(),
                    location.getLongitude(),
                    prochain.getLatitude(),
                    prochain.getLongitude(),
                    distToNext
            );
            total += distToNext[0];
        }

        // 2. Ensuite, on ajoute la somme des distances entre les points restants de la liste
        for (int i = indexProchainPoint; i < pointsParcours.size() - 1; i++) {
            float[] r = new float[1];
            Location.distanceBetween(
                    pointsParcours.get(i).getLatitude(),
                    pointsParcours.get(i).getLongitude(),
                    pointsParcours.get(i + 1).getLatitude(),
                    pointsParcours.get(i + 1).getLongitude(),
                    r
            );
            total += r[0];
        }

        return total;
    }

    private void mettreAJourAffichageDistances(float prochain, float arrivee) {

        tvProchainPoint.setText(
                prochain >= 1000
                        ? String.format("Prochain point dans %.2f km", prochain / 1000)
                        : String.format("Prochain point dans %.0f m", prochain)
        );

        tvArrivee.setText(
                arrivee >= 1000
                        ? String.format("Arrivée dans %.2f km", arrivee / 1000)
                        : String.format("Arrivée dans %.0f m", arrivee)
        );
    }

    /* ===================== ITINERAIRE ===================== */

    private void ajouterMarkers(List<GeoPoint> points) {
        for (GeoPoint p : points) {
            Marker m = new Marker(map);
            m.setPosition(p);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(m);
        }
    }

    private void calculerItineraire(List<GeoPoint> points) {
        new Thread(() -> {
            try {
                OSRMRoadManager rm = new OSRMRoadManager(this, getPackageName());
                rm.setMean(OSRMRoadManager.MEAN_BY_FOOT);
                Road road = rm.getRoad(new ArrayList<>(points));
                Polyline line = RoadManager.buildRoadOverlay(road);
                line.getOutlinePaint().setColor(Color.argb(120, 120, 120, 120));
                line.getOutlinePaint().setStrokeWidth(15f);

                runOnUiThread(() -> map.getOverlays().add(0, line));
            } catch (Exception e) {
                Log.e("OSRM", e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }
}
