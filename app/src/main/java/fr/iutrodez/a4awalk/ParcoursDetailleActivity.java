package fr.iutrodez.a4awalk;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

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

    // TextViews de la vue
    private TextView tvNomParcours, tvDepart, tvArrivee, tvDate, tvRandonnee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.detaille_parcour);

        // ======================================
        // RÉFÉRENCES VUES
        // ======================================
        map = findViewById(R.id.map);

        tvNomParcours = findViewById(R.id.tvNomParcours);
        tvDepart = findViewById(R.id.tvDepart);
        tvArrivee = findViewById(R.id.tvArrivee);
        tvDate = findViewById(R.id.tvDate);
        tvRandonnee = findViewById(R.id.tvRandonnee);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // ======================================
        // INITIALISATION DES DONNÉES
        // ======================================
        String nomParcours = "Parcours 1";
        String departText = "Les granges des forêts";
        String arriveeText = "Pointe feuillette";
        String dateText = "08/10/2025";
        String randonneeText = "Randonnée 1";

        // Affectation aux TextViews
        tvNomParcours.setText(nomParcours);
        tvDepart.setText(departText);
        tvArrivee.setText(arriveeText);
        tvDate.setText(dateText);
        tvRandonnee.setText(randonneeText);

        // ======================================
        // LISTE DES POINTS
        // ======================================
        pointsParcours = new ArrayList<>();
        GeoPoint depart = new GeoPoint(44.43676503582271, 2.5109473622927245);
        GeoPoint poi1 = new GeoPoint(44.45000426562201, 2.5016136393106736);
        GeoPoint poi2 = new GeoPoint(44.44917238006365, 2.489074453725384);
        GeoPoint arrivee = new GeoPoint(44.43676503582271, 2.5109473622927245);

        pointsParcours.add(depart);
        pointsParcours.add(poi1);
        pointsParcours.add(poi2);
        pointsParcours.add(arrivee);

        // ======================================
        // CENTRAGE CARTE
        // ======================================
        map.getController().setZoom(14.5);
        map.getController().setCenter(depart);
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(12.0);

        // ======================================
        // AJOUT DES MARKERS Départ / Arrivée
        // ======================================
        ajouterMarkersDepartArrivee(depart, arrivee);

        // ======================================
        // TRACÉ DE L’ITINÉRAIRE
        // ======================================
        tracerItineraire(pointsParcours);
    }

    /**
     * Ajoute les markers uniquement pour le départ et l’arrivée
     */
    private void ajouterMarkersDepartArrivee(GeoPoint depart, GeoPoint arrivee) {
        Marker markerDepart = new Marker(map);
        markerDepart.setPosition(depart);
        markerDepart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerDepart.setTitle("Départ");
        map.getOverlays().add(markerDepart);

        Marker markerArrivee = new Marker(map);
        markerArrivee.setPosition(arrivee);
        markerArrivee.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerArrivee.setTitle("Arrivée");
        map.getOverlays().add(markerArrivee);
    }

    /**
     * Tracer un itinéraire entre tous les points
     */
    private void tracerItineraire(List<GeoPoint> points) {
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, getPackageName());
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                ArrayList<GeoPoint> roadPoints = new ArrayList<>(points);
                Road road = roadManager.getRoad(roadPoints);

                Polyline overlay = RoadManager.buildRoadOverlay(road);
                overlay.setColor(Color.BLUE);
                overlay.setWidth(12f);

                runOnUiThread(() -> {
                    map.getOverlays().add(overlay);
                    map.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
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
