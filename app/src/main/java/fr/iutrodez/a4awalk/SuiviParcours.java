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
        // Sécurité : si on a fini le parcours, on ne fait rien
        if (indexProchainPoint >= pointsParcours.size()) return;

        GeoPoint prochain = pointsParcours.get(indexProchainPoint);
        float[] resultatsCalcul = new float[1];

        // 1. Calcul de la distance réelle vers le point actuel (CIBLE IMMÉDIATE)
        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                prochain.getLatitude(),
                prochain.getLongitude(),
                resultatsCalcul
        );
        float distanceVersCibleActuelle = resultatsCalcul[0];

        // 2. Vérification de validation du point (Rayon de 20m)
        // On garde 20m pour la validation officielle pour être précis
        boolean pointVientDetreValide = false;
        if (distanceVersCibleActuelle < 20 && indexProchainPoint < pointsParcours.size() - 1) {
            indexProchainPoint++;
            pointVientDetreValide = true;
            Toast.makeText(this, "Point validé !", Toast.LENGTH_SHORT).show();

            // Comme on a changé de point, on recalcule la distance vers le NOUVEAU point immédiat
            // pour que les calculs suivants soient justes
            GeoPoint nouveauProchain = pointsParcours.get(indexProchainPoint);
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    nouveauProchain.getLatitude(), nouveauProchain.getLongitude(),
                    resultatsCalcul
            );
            distanceVersCibleActuelle = resultatsCalcul[0];
        }

        // --- LOGIQUE D'AFFICHAGE INTELLIGENTE ---

        // Calcul du déplacement depuis la dernière mise à jour texte
        float distanceDepuisDernierAffichage = 0;
        if (locationDerniereMajTexte != null) {
            distanceDepuisDernierAffichage = location.distanceTo(locationDerniereMajTexte);
        }

        // CONDITION DE MISE A JOUR :
        // A. Si on est loin (>50m) : on ne met à jour que tous les 100m (économie batterie/lecture)
        // B. Si on est proche (<=50m) : on met à jour tout le temps (pour voir le décompte précis)
        // C. Si on vient de valider un point : on met à jour immédiatement
        boolean estProche = distanceVersCibleActuelle <= 50;

        if (locationDerniereMajTexte == null ||
                pointVientDetreValide ||
                estProche ||
                distanceDepuisDernierAffichage >= 100) {

            locationDerniereMajTexte = location;

            // Calcul de la distance totale restante (logique corrigée précédemment)
            float distanceTotale = calculerDistanceTotaleRestante(location);

            float distanceAffiche = distanceVersCibleActuelle;

            // --- C'EST ICI QUE LA MAGIE OPÈRE (ANTICIPATION) ---
            // Si on est à moins de 50m ET qu'il existe un point encore après le point actuel
            if (estProche && indexProchainPoint + 1 < pointsParcours.size()) {

                GeoPoint pointApresLeProchain = pointsParcours.get(indexProchainPoint + 1);
                float[] resultatsAnticipation = new float[1];

                // On calcule la distance directe entre MOI et le point N+1
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        pointApresLeProchain.getLatitude(), pointApresLeProchain.getLongitude(),
                        resultatsAnticipation
                );

                // On décide d'afficher cette distance là à l'utilisateur
                distanceAffiche = resultatsAnticipation[0];

                // Optionnel : Vous pouvez changer le texte pour indiquer que c'est le suivant
                // tvProchainPoint.setText("Vers le point suivant (anticipé)...");
            }

            mettreAJourAffichageDistances(distanceAffiche, distanceTotale);
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
