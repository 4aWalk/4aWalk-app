package fr.iutrodez.a4awalk;

import android.graphics.Color;
import android.graphics.Paint; // Important pour le style du trait
import android.os.Bundle;
import android.preference.PreferenceManager; // Nécessaire pour OSM
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Pour récupérer les icônes proprement

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Configuration OSM (Important de charger les préférences avant le layout)
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // 2. Chargement du nouveau Layout XML (assure-toi que le fichier s'appelle bien activity_main.xml ou modifie ici)
        setContentView(R.layout.main_activity);

        // 3. Récupération de la MapView avec le bon ID
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK); // Pour avoir le style montagne, voir note en bas
        map.setMultiTouchControls(true);

        // Désactiver les boutons de zoom (+/-) car on gère le zoom tactile, ça fait plus propre
        map.setBuiltInZoomControls(false);

        // =========================================================
        // LISTE DES POINTS
        // =========================================================
        pointsParcours = new ArrayList<>();

        // Tes coordonnées
        GeoPoint depart = new GeoPoint(44.43676503582271, 2.5109473622927245);
        GeoPoint poi1 = new GeoPoint(44.45000426562201, 2.5016136393106736);
        GeoPoint poi2 = new GeoPoint(44.44917238006365, 2.489074453725384);
        GeoPoint arrivee = new GeoPoint(44.43676503582271, 2.5109473622927245);

        pointsParcours.add(depart);
        pointsParcours.add(poi1);
        pointsParcours.add(poi2);
        pointsParcours.add(arrivee);

        // =========================================================
        // CENTRAGE INITIAL
        // =========================================================
        map.getController().setZoom(14.0);
        map.getController().setCenter(pointsParcours.get(0));
        map.setMaxZoomLevel(19.0);
        map.setMinZoomLevel(10.0);

        // =========================================================
        // EXÉCUTION
        // =========================================================
        ajouterMarkers(pointsParcours);
        calculerItineraire(pointsParcours);
    }

    /**
     * Ajoute les markers sur la carte
     */
    private void ajouterMarkers(List<GeoPoint> points) {
        for (int i = 0; i < points.size(); i++) {
            Marker marker = new Marker(map);
            marker.setPosition(points.get(i));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Configuration selon la position (Départ, Intermédiaire, Arrivée)
            if (i == 0) {
                marker.setTitle("Départ");
                // Ici tu pourrais mettre : marker.setIcon(getDrawable(R.drawable.ic_flag));
            } else if (i == points.size() - 1) {
                marker.setTitle("Arrivée");
                // Ici tu pourrais mettre : marker.setIcon(getDrawable(R.drawable.ic_home));
            } else {
                marker.setTitle("Étape " + i);
                // Icône par défaut pour les étapes
                // marker.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_map));
            }

            // Note: Si tu ne définis pas d'icône (.setIcon), OSM utilise le marqueur par défaut.
            map.getOverlays().add(marker);
        }
        map.invalidate(); // Rafraîchir pour afficher les markers immédiatement
    }

    /**
     * Calcul de l'itinéraire via OSRM
     */
    private void calculerItineraire(List<GeoPoint> points) {
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(this, getPackageName());
                // MEAN_BY_FOOT est idéal pour la rando
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                List<Polyline> overlays = new ArrayList<>();
                int customBlue = Color.parseColor("#4A69FF"); // Un bleu similaire à ta capture

                // Logique pour gérer la boucle (ton code original conservé)
                boolean boucle = points.get(0).getLatitude() == points.get(points.size() - 1).getLatitude()
                        && points.get(0).getLongitude() == points.get(points.size() - 1).getLongitude();

                if (boucle && points.size() > 2) {
                    // Segment 1
                    ArrayList<GeoPoint> segment1 = new ArrayList<>(points.subList(0, points.size() - 1));
                    if(segment1.size() >= 2) {
                        Road road1 = roadManager.getRoad(segment1);
                        overlays.add(creerPolylineStyle(road1, customBlue));
                    }
                    // Segment 2 (fermeture de la boucle)
                    ArrayList<GeoPoint> segment2 = new ArrayList<>();
                    segment2.add(points.get(points.size() - 2));
                    segment2.add(points.get(points.size() - 1));
                    Road road2 = roadManager.getRoad(segment2);
                    overlays.add(creerPolylineStyle(road2, customBlue));

                } else {
                    // Itinéraire linéaire standard
                    ArrayList<GeoPoint> full = new ArrayList<>(points);
                    Road road = roadManager.getRoad(full);
                    overlays.add(creerPolylineStyle(road, customBlue));
                }

                // Retour sur le Thread UI pour l'affichage
                runOnUiThread(() -> {
                    for (Polyline p : overlays) {
                        map.getOverlays().add(0, p); // Ajout en index 0 pour que le tracé soit SOUS les markers
                    }
                    map.invalidate();
                });

            } catch (Exception e) {
                Log.e("OSRM_ERROR", "Erreur itinéraire: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(SuiviParcours.this, "Erreur chargement itinéraire", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Méthode utilitaire pour styliser la ligne (couleur, épaisseur)
     */
    private Polyline creerPolylineStyle(Road road, int color) {
        Polyline overlay = RoadManager.buildRoadOverlay(road);

        // STYLE VISUEL DU TRACÉ
        overlay.getOutlinePaint().setColor(color);
        overlay.getOutlinePaint().setStrokeWidth(15f); // Ligne plus épaisse comme sur l'image
        overlay.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND); // Bords arrondis
        overlay.getOutlinePaint().setStrokeJoin(Paint.Join.ROUND); // Coins arrondis

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