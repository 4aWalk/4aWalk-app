package fr.iutrodez.a4awalk;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

public class ParcoursDetailleActivity extends AppCompatActivity {

    private MapView map;
    private static final String TAG = "ParcoursDetailleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.detaille_parcour);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Points de départ et d'arrivée
        GeoPoint pointDepart = new GeoPoint(45.633, 6.360);
        GeoPoint pointArrivee = new GeoPoint(45.645, 6.375);

        // Zoom adapté pour la randonnée (plus proche)
        map.getController().setZoom(14.5);
        GeoPoint centre = new GeoPoint(
                (pointDepart.getLatitude() + pointArrivee.getLatitude()) / 2,
                (pointDepart.getLongitude() + pointArrivee.getLongitude()) / 2
        );
        map.getController().setCenter(centre);

        // Marker départ
        Marker markerDepart = new Marker(map);
        markerDepart.setPosition(pointDepart);
        markerDepart.setTitle("Départ de la randonnée");
        markerDepart.setSnippet("Point de départ");
        markerDepart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerDepart);

        // Marker arrivée
        Marker markerArrivee = new Marker(map);
        markerArrivee.setPosition(pointArrivee);
        markerArrivee.setTitle("Arrivée de la randonnée");
        markerArrivee.setSnippet("Point d'arrivée");
        markerArrivee.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerArrivee);

        // Calculer l'itinéraire pédestre
        calculerItineraireRandonnee(pointDepart, pointArrivee);
    }

    private void calculerItineraireRandonnee(GeoPoint depart, GeoPoint arrivee) {
        new Thread(() -> {
            try {
                // Configuration du gestionnaire de routes en MODE PIÉTON
                OSRMRoadManager roadManager = new OSRMRoadManager(this, getPackageName());

                // IMPORTANT : Définir le mode piéton pour les chemins de randonnée
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                // Liste des waypoints
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(depart);
                waypoints.add(arrivee);

                // Calcul de l'itinéraire
                Road road = roadManager.getRoad(waypoints);

                // Mise à jour de l'interface sur le thread principal
                runOnUiThread(() -> {
                    if (road.mStatus == Road.STATUS_OK) {
                        // Créer la polyline avec l'itinéraire calculé
                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

                        // Style de la ligne adapté à la randonnée
                        roadOverlay.setColor(Color.rgb(255, 102, 0)); // Orange
                        roadOverlay.setWidth(10f);

                        map.getOverlays().add(roadOverlay);
                        map.invalidate();

                        // Afficher les informations de l'itinéraire
                        double distance = road.mLength; // en km
                        double duree = road.mDuration / 60; // en minutes

                        // Calcul d'informations pour la randonnée
                        int dureeHeures = (int) (duree / 60);
                        int dureeMinutes = (int) (duree % 60);

                        String info;
                        if (dureeHeures > 0) {
                            info = String.format(
                                    "Distance: %.2f km\nDurée: %dh%02dmin à pied\nRandonnée pédestre",
                                    distance, dureeHeures, dureeMinutes
                            );
                        } else {
                            info = String.format(
                                    "Distance: %.2f km\nDurée: %d min à pied\nRandonnée pédestre",
                                    distance, (int)duree
                            );
                        }

                        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Itinéraire de randonnée calculé: " + info);

                    } else {
                        String errorMsg = "Erreur lors du calcul de l'itinéraire";
                        if (road.mStatus == Road.STATUS_INVALID) {
                            errorMsg = "Impossible de trouver un chemin piéton";
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erreur route: " + road.mStatus);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Erreur: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Impossible de calculer l'itinéraire",
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}