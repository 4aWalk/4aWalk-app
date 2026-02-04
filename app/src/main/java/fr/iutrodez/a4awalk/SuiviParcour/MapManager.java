package fr.iutrodez.a4awalk.SuiviParcour;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class MapManager {
    private final MapView map;
    private final Context context;
    private final Polyline userTrace;
    private Marker userMarker;

    public MapManager(MapView map, Context context) {
        this.map = map;
        this.context = context;
        userTrace = new Polyline();
        userTrace.getOutlinePaint().setColor(0xFF00FF00); // vert
        userTrace.getOutlinePaint().setStrokeWidth(12f);
        map.getOverlays().add(userTrace);
    }

    public void addMarkers(List<GeoPoint> points) {
        for (GeoPoint p : points) {
            Marker m = new Marker(map);
            m.setPosition(p);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(m);
        }
    }

    public void updateUserPosition(GeoPoint pos, Drawable iconDrawable) {
        userTrace.addPoint(pos);

        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setTitle("Vous êtes ici");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            if (iconDrawable != null) {
                Bitmap b = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                iconDrawable.setBounds(0, 0, c.getWidth(), c.getHeight());
                iconDrawable.draw(c);
                userMarker.setIcon(new BitmapDrawable((Resources) map.getResources(), b));
            }
            map.getOverlays().add(userMarker);
        }

        userMarker.setPosition(pos);

        GeoPoint center = (GeoPoint) map.getMapCenter();
        if (center.distanceToAsDouble(pos) > 30) {
            map.getController().animateTo(pos);
        }

        map.invalidate();
    }

    // --- Nouvelle méthode : calculer l'itinéraire ---
    public void calculerItineraire(List<GeoPoint> points) {
        new Thread(() -> {
            try {
                OSRMRoadManager rm = new OSRMRoadManager(context, context.getPackageName());
                rm.setMean(OSRMRoadManager.MEAN_BY_FOOT);

                Road road = rm.getRoad(new ArrayList<>(points));
                Polyline line = RoadManager.buildRoadOverlay(road);
                line.getOutlinePaint().setColor(Color.argb(120, 120, 120, 120));
                line.getOutlinePaint().setStrokeWidth(15f);

                map.post(() -> map.getOverlays().add(0, line));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}



