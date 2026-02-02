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
import java.util.List;

public class ParcoursDetailleActivity extends AppCompatActivity {

    private MapView map;
    private List<GeoPoint> pointsParcours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.detaille_parcour);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // =========================================================
        // LISTE DES POINTS DANS L'ORDRE
        // =========================================================
        pointsParcours = new ArrayList<>();

        GeoPoint depart = new GeoPoint(44.43676503582271, 2.5109473622927245);
        GeoPoint poi1 = new GeoPoint(44.45000426562201, 2.5016136393106736);
        GeoPoint poi2 = new GeoPoint(44.44917238006365, 2.489074453725384);
        // Départ et arrivée peuvent être identiques ou différents
        GeoPoint arrivee = new GeoPoint(44.43676503582271, 2.5109473622927245);

        // Ajout des points dans l’ordre
        pointsParcours.add(depart);
        pointsParcours.add(poi1);
        pointsParcours.add(poi2);
        pointsParcours.add(arrivee);

        // =========================================================
        // CENTRAGE CARTE
        // =========================================================
        map.getController().setZoom(14.5);
        map.getController().setCenter(pointsParcours.get(0));
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);

        // =========================================================
        // AJOUT DES MARKERS
        // =========================================================
        ajouterMarkers(pointsParcours);

        // =========================================================
        // CALCUL DE L’ITINÉRAIRE
        // =========================================================
        calculerItineraire(pointsParcours);
    }

    /**
     * Ajoute les markers : Départ, POI, Arrivée
     */
    private void ajouterMarkers(List<GeoPoint> points) {
        for (int i = 0; i < points.size(); i++) {
            Marker marker = new Marker(map);
            marker.setPosition(points.get(i));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            if (i == 0) {
                marker.setTitle("Départ");
            } else if (i == points.size() - 1) {
                marker.setTitle("Arrivée");
            } else {
                marker.setTitle("Point d’intérêt " + i);
                marker.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_info));
            }

            map.getOverlays().add(marker);
        }
    }

    /**
     * Calcul générique de l’itinéraire
     */
    private void calculerItineraire(List<GeoPoint> points) {

        if (points.size() < 2) {
            Toast.makeText(this, "Au moins 2 points requis", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, getPackageName());
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                List<Polyline> overlays = new ArrayList<>();

                // Cas spécial : départ = arrivée
                boolean boucle = points.get(0).getLatitude() == points.get(points.size() - 1).getLatitude()
                        && points.get(0).getLongitude() == points.get(points.size() - 1).getLongitude();

                if (boucle && points.size() > 2) {

                    // Segment 1 : Départ → POI jusqu'au dernier POI
                    ArrayList<GeoPoint> segment1 = new ArrayList<>(points.subList(0, points.size() - 1));
                    if(segment1.size() >= 2) {
                        Road road1 = roadManager.getRoad(segment1);
                        Polyline overlay1 = RoadManager.buildRoadOverlay(road1);
                        overlay1.setColor(Color.BLUE);
                        overlay1.setWidth(12f);
                        overlays.add(overlay1);
                    }

                    // Segment 2 : dernier POI → arrivée
                    ArrayList<GeoPoint> segment2 = new ArrayList<>();
                    segment2.add(points.get(points.size() - 2)); // dernier POI
                    segment2.add(points.get(points.size() - 1)); // arrivée
                    Road road2 = roadManager.getRoad(segment2);
                    Polyline overlay2 = RoadManager.buildRoadOverlay(road2);
                    overlay2.setColor(Color.BLUE);
                    overlay2.setWidth(12f);
                    overlays.add(overlay2);

                } else {
                    // Parcours normal (départ ≠ arrivée)
                    ArrayList<GeoPoint> full = new ArrayList<>(points);
                    Road road = roadManager.getRoad(full);
                    Polyline overlay = RoadManager.buildRoadOverlay(road);
                    overlay.setColor(Color.BLUE);
                    overlay.setWidth(12f);
                    overlays.add(overlay);
                }

                runOnUiThread(() -> {
                    for (Polyline p : overlays) {
                        map.getOverlays().add(0, p);
                    }
                    map.invalidate();

                    Toast.makeText(this,
                            "Itinéraire généré pour " + points.size() + " points",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                Log.e("OSRM_ERROR", "Exception: " + e.getMessage());
            }
        }).start();
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
