package fr.iutrodez.a4awalk;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
        GeoPoint pointArrivee = new GeoPoint(45.645, 6.375); // Exemple de point d'arrivée

        // Zoom + position (centrer entre les deux points)
        map.getController().setZoom(13.5);
        GeoPoint centre = new GeoPoint(
                (pointDepart.getLatitude() + pointArrivee.getLatitude()) / 2,
                (pointDepart.getLongitude() + pointArrivee.getLongitude()) / 2
        );
        map.getController().setCenter(centre);

        // Marker départ
        Marker markerDepart = new Marker(map);
        markerDepart.setPosition(pointDepart);
        markerDepart.setTitle("Point de départ");
        markerDepart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerDepart);

        // Marker arrivée
        Marker markerArrivee = new Marker(map);
        markerArrivee.setPosition(pointArrivee);
        markerArrivee.setTitle("Point d'arrivée");
        markerArrivee.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerArrivee);

        // Créer le tracé (ligne) entre les deux points
        Polyline line = new Polyline(map);
        List<GeoPoint> points = new ArrayList<>();
        points.add(pointDepart);
        points.add(pointArrivee);
        line.setPoints(points);

        // Style de la ligne
        line.setColor(Color.BLUE);
        line.setWidth(5f);

        map.getOverlays().add(line);

        // Rafraîchir la carte
        map.invalidate();
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