package fr.iutrodez.a4awalk.SuiviParcour;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Color;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de carte pour l'affichage et le suivi du parcours.
 * <p>
 * Permet d'ajouter des marqueurs, de tracer l'itinéraire et de suivre la position de l'utilisateur.
 */
public class MapManager {

    private final MapView map;
    private final Context context;
    private final Polyline userTrace;
    private Marker userMarker;

    /**
     * Crée un MapManager pour gérer une MapView.
     *
     * @param map     MapView à gérer
     * @param context Contexte de l'application
     */
    public MapManager(MapView map, Context context) {
        this.map = map;
        this.context = context;

        userTrace = new Polyline();
        userTrace.getOutlinePaint().setColor(0xFF00FF00); // vert
        userTrace.getOutlinePaint().setStrokeWidth(12f);

        map.getOverlays().add(userTrace);
    }

    /**
     * Ajoute des marqueurs sur la carte pour une liste de points.
     *
     * @param points Liste de GeoPoint à afficher
     */
    public void addMarkers(List<GeoPoint> points) {
        for (GeoPoint point : points) {
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
        }
    }

    /**
     * Met à jour la position de l'utilisateur sur la carte.
     * <p>
     * Ajoute le point au tracé et recentre la carte si l'utilisateur s'éloigne du centre.
     *
     * @param pos          Position actuelle de l'utilisateur
     * @param iconDrawable Icône personnalisée pour le marker utilisateur (peut être null)
     */
    public void updateUserPosition(GeoPoint pos, Drawable iconDrawable) {
        // Ajout du point au tracé
        userTrace.addPoint(pos);

        // Création du marker utilisateur si nécessaire
        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setTitle("Vous êtes ici");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            if (iconDrawable != null) {
                Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                iconDrawable.draw(canvas);
                userMarker.setIcon(new BitmapDrawable((Resources) map.getResources(), bitmap));
            }

            map.getOverlays().add(userMarker);
        }

        // Mise à jour de la position du marker
        userMarker.setPosition(pos);

        // Recentre la carte si l'utilisateur est éloigné du centre
        GeoPoint center = (GeoPoint) map.getMapCenter();
        if (center.distanceToAsDouble(pos) > 30) {
            map.getController().animateTo(pos);
        }

        map.invalidate();
    }

    /**
     * Calcule et affiche l'itinéraire entre une liste de points.
     * <p>
     * Le tracé est réalisé sur un thread séparé pour ne pas bloquer l'UI.
     *
     * @param points Liste de GeoPoint représentant l'itinéraire
     */
    public void calculerItineraire(List<GeoPoint> points) {
        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(context, context.getPackageName());
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                Road road = roadManager.getRoad(new ArrayList<>(points));
                Polyline roadLine = RoadManager.buildRoadOverlay(road);
                roadLine.getOutlinePaint().setColor(Color.argb(120, 120, 120, 120));
                roadLine.getOutlinePaint().setStrokeWidth(15f);

                map.post(() -> map.getOverlays().add(0, roadLine));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
