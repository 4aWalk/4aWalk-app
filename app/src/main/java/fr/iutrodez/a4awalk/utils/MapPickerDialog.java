package fr.iutrodez.a4awalk.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;
import fr.iutrodez.a4awalk.R;

public class MapPickerDialog {

    public interface OnLocationPickedListener {
        void onLocationPicked(double latitude, double longitude);
    }

    public static void afficher(Context context, double latInitiale, double lonInitiale,
                                String titre, OnLocationPickedListener listener) {

        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_map_picker, null);
        MapView mapView = view.findViewById(R.id.map_picker);
        TextView tvInstruction = view.findViewById(R.id.tv_map_instruction);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Position initiale (France par défaut si pas de coords)
        double lat = (latInitiale != 0) ? latInitiale : 46.2276;
        double lon = (lonInitiale != 0) ? lonInitiale : 2.2137;
        GeoPoint startPoint = new GeoPoint(lat, lon);
        mapView.getController().setZoom(latInitiale != 0 ? 15.0 : 6.0);
        mapView.getController().setCenter(startPoint);

        // Marqueur initial si coords existantes
        final Marker[] marker = {null};
        if (latInitiale != 0) {
            marker[0] = new Marker(mapView);
            marker[0].setPosition(startPoint);
            marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker[0]);
        }

        final double[] selectedLat = {latInitiale};
        final double[] selectedLon = {lonInitiale};

        // Listener de tap sur la carte
        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedLat[0] = p.getLatitude();
                selectedLon[0] = p.getLongitude();

                // Supprimer l'ancien marqueur
                if (marker[0] != null) mapView.getOverlays().remove(marker[0]);

                // Ajouter le nouveau
                marker[0] = new Marker(mapView);
                marker[0].setPosition(p);
                marker[0].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker[0]);
                mapView.invalidate();

                tvInstruction.setText(String.format("📍 %.5f, %.5f — Validez pour confirmer",
                        p.getLatitude(), p.getLongitude()));
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        });
        mapView.getOverlays().add(0, eventsOverlay);

        new AlertDialog.Builder(context)
                .setTitle(titre)
                .setView(view)
                .setPositiveButton("Valider", (dialog, which) -> {
                    if (selectedLat[0] != 0 || selectedLon[0] != 0) {
                        listener.onLocationPicked(selectedLat[0], selectedLon[0]);
                    }
                })
                .setNegativeButton("Annuler", null)
                .setOnDismissListener(d -> mapView.onDetach())
                .show();
    }
}