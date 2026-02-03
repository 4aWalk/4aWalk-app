package fr.iutrodez.a4awalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class SuiviParcours extends AppCompatActivity {

    private MapView map;
    private List<GeoPoint> pointsParcours;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker userMarker;

    // 🔹 Nouveaux éléments pour l'affichage des distances
    private TextView tvProchainPoint;
    private TextView tvArrivee;
    private int indexProchainPoint = 0; // Index du prochain point à atteindre
    private Location positionActuelle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration OSM
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.main_activity);

        // MapView
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // 🔹 Boutons + / - et zoom tactile
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // 🔹 Initialisation des TextViews
        tvProchainPoint = findViewById(R.id.tvProchainPoint);
        tvArrivee = findViewById(R.id.tvArrivee);

        // Points du parcours
        pointsParcours = new ArrayList<>();
        pointsParcours.add(new GeoPoint(44.360369301617794, 2.5758112393065384));
        pointsParcours.add(new GeoPoint(44.351077610605785, 2.5740525086171298));
        pointsParcours.add(new GeoPoint(44.34951392435509, 2.576044023961549));
        pointsParcours.add(new GeoPoint(44.352487429572584, 2.5677165393061494));
        pointsParcours.add(new GeoPoint(44.360369301617794, 2.5758112393065384));

        // Centrage initial
        map.getController().setZoom(14.0);
        map.getController().setCenter(pointsParcours.get(0));
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(10.0);

        // Affichage markers et parcours
        ajouterMarkers(pointsParcours); // Pins classiques
        calculerItineraire(pointsParcours);

        // Géolocalisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        demarrerLocalisation();
    }

    /** Démarre la géolocalisation en temps réel */
    private void demarrerLocalisation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    mettreAJourPositionUtilisateur(location.getLatitude(), location.getLongitude());
                    // 🔹 Calcul des distances
                    calculerDistances(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /** Met à jour le marker de la position de l'utilisateur */
    private void mettreAJourPositionUtilisateur(double latitude, double longitude) {
        GeoPoint userPos = new GeoPoint(latitude, longitude);

        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setPosition(userPos);
            userMarker.setTitle("Vous êtes ici");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Icône rouge redimensionnée pour l'utilisateur
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_pin_user);
            if (drawable != null) {
                Bitmap bitmap = drawableToBitmap(drawable, 80, 80); // 80x80 pixels
                userMarker.setIcon(new BitmapDrawable(getResources(), bitmap));
            }

            map.getOverlays().add(userMarker);
        } else {
            userMarker.setPosition(userPos);
        }

        map.getController().setCenter(userPos);
        map.invalidate();
    }

    /** 🔹 NOUVELLE MÉTHODE : Calcule et affiche les distances */
    private void calculerDistances(Location locationActuelle) {
        if (locationActuelle == null || pointsParcours.isEmpty()) return;

        positionActuelle = locationActuelle;

        // 🔹 Vérifier si on est proche du prochain point (seuil de 20 mètres)
        if (indexProchainPoint < pointsParcours.size() - 1) {
            float[] distanceProchainPoint = new float[1];
            GeoPoint prochainPoint = pointsParcours.get(indexProchainPoint);

            Location.distanceBetween(
                    locationActuelle.getLatitude(),
                    locationActuelle.getLongitude(),
                    prochainPoint.getLatitude(),
                    prochainPoint.getLongitude(),
                    distanceProchainPoint
            );

            // Si on est très proche du point (moins de 20m), passer au suivant
            if (distanceProchainPoint[0] < 20 && indexProchainPoint < pointsParcours.size() - 1) {
                indexProchainPoint++;
            }
        }

        // 🔹 Calcul de la distance vers le prochain point
        float distanceVersProchain = 0;
        if (indexProchainPoint < pointsParcours.size() - 1) {
            float[] resultat = new float[1];
            GeoPoint prochainPoint = pointsParcours.get(indexProchainPoint);

            Location.distanceBetween(
                    locationActuelle.getLatitude(),
                    locationActuelle.getLongitude(),
                    prochainPoint.getLatitude(),
                    prochainPoint.getLongitude(),
                    resultat
            );
            distanceVersProchain = resultat[0];
        }

        // 🔹 Calcul de la distance TOTALE vers l'arrivée
        // = distance vers prochain point + somme des distances entre tous les points restants
        float distanceTotaleVersArrivee = calculerDistanceTotaleRestante(locationActuelle);

        // 🔹 Mise à jour de l'interface
        mettreAJourAffichageDistances(distanceVersProchain, distanceTotaleVersArrivee);
    }

    /** 🔹 NOUVELLE MÉTHODE : Calcule la distance totale restante jusqu'à l'arrivée */
    private float calculerDistanceTotaleRestante(Location locationActuelle) {
        if (locationActuelle == null || pointsParcours.isEmpty()) return 0;

        float distanceTotale = 0;

        // 🔹 1. Distance de ma position actuelle vers le prochain point
        if (indexProchainPoint < pointsParcours.size()) {
            float[] resultat = new float[1];
            GeoPoint prochainPoint = pointsParcours.get(indexProchainPoint);

            Location.distanceBetween(
                    locationActuelle.getLatitude(),
                    locationActuelle.getLongitude(),
                    prochainPoint.getLatitude(),
                    prochainPoint.getLongitude(),
                    resultat
            );
            distanceTotale += resultat[0];
        }

        // 🔹 2. Somme des distances entre tous les points restants
        for (int i = indexProchainPoint; i < pointsParcours.size() - 1; i++) {
            float[] resultat = new float[1];
            GeoPoint pointActuel = pointsParcours.get(i);
            GeoPoint pointSuivant = pointsParcours.get(i + 1);

            Location.distanceBetween(
                    pointActuel.getLatitude(),
                    pointActuel.getLongitude(),
                    pointSuivant.getLatitude(),
                    pointSuivant.getLongitude(),
                    resultat
            );
            distanceTotale += resultat[0];
        }

        return distanceTotale;
    }

    /** 🔹 NOUVELLE MÉTHODE : Met à jour l'affichage des distances dans l'interface */
    private void mettreAJourAffichageDistances(float distanceProchain, float distanceArrivee) {
        runOnUiThread(() -> {
            // Format de la distance vers le prochain point
            String texteProchain;
            if (indexProchainPoint < pointsParcours.size() - 1) {
                if (distanceProchain >= 1000) {
                    texteProchain = String.format("Le prochain point d'intérêt est dans %.2f km",
                            distanceProchain / 1000);
                } else {
                    texteProchain = String.format("Le prochain point d'intérêt est dans %.0f m",
                            distanceProchain);
                }
            } else {
                texteProchain = "Vous avez atteint tous les points !";
            }
            tvProchainPoint.setText(texteProchain);

            // Format de la distance vers l'arrivée
            String texteArrivee;
            if (distanceArrivee >= 1000) {
                texteArrivee = String.format("L'arrivée est dans %.2f km", distanceArrivee / 1000);
            } else {
                texteArrivee = String.format("L'arrivée est dans %.0f m", distanceArrivee);
            }
            tvArrivee.setText(texteArrivee);
        });
    }

    /** Convertit un Drawable en Bitmap redimensionné */
    private Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /** Ajoute les markers du parcours (pins classiques) */
    private void ajouterMarkers(List<GeoPoint> points) {
        for (int i = 0; i < points.size(); i++) {
            Marker marker = new Marker(map);
            marker.setPosition(points.get(i));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Pins classiques
            if (i == 0) marker.setTitle("Départ");
            else if (i == points.size() - 1) marker.setTitle("Arrivée");
            else marker.setTitle("Étape " + i);

            map.getOverlays().add(marker);
        }
        map.invalidate();
    }

    /** Calcule et trace le parcours sur la carte */
    private void calculerItineraire(List<GeoPoint> points) {
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, getPackageName());
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                List<Polyline> overlays = new ArrayList<>();
                int grisTransparent = Color.argb(120, 120, 120, 120);

                boolean boucle = points.get(0).getLatitude() == points.get(points.size() - 1).getLatitude()
                        && points.get(0).getLongitude() == points.get(points.size() - 1).getLongitude();

                if (boucle && points.size() > 2) {
                    ArrayList<GeoPoint> segment1 = new ArrayList<>(points.subList(0, points.size() - 1));
                    if (segment1.size() >= 2) {
                        Road road1 = roadManager.getRoad(segment1);
                        overlays.add(creerPolylineStyle(road1, grisTransparent));
                    }
                    ArrayList<GeoPoint> segment2 = new ArrayList<>();
                    segment2.add(points.get(points.size() - 2));
                    segment2.add(points.get(points.size() - 1));
                    Road road2 = roadManager.getRoad(segment2);
                    overlays.add(creerPolylineStyle(road2, grisTransparent));
                } else {
                    ArrayList<GeoPoint> full = new ArrayList<>(points);
                    Road road = roadManager.getRoad(full);
                    overlays.add(creerPolylineStyle(road, grisTransparent));
                }

                runOnUiThread(() -> {
                    for (Polyline p : overlays) map.getOverlays().add(0, p);
                    map.invalidate();
                });

            } catch (Exception e) {
                Log.e("OSRM_ERROR", "Erreur itinéraire: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(SuiviParcours.this,
                                "Erreur chargement itinéraire", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /** Crée un Polyline stylisé (couleur + transparence) */
    private Polyline creerPolylineStyle(Road road, int color) {
        Polyline overlay = RoadManager.buildRoadOverlay(road);

        Paint paint = overlay.getOutlinePaint();
        paint.setColor(color);
        paint.setStrokeWidth(15f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);

        return overlay;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}