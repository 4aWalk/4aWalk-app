package fr.iutrodez.a4awalk.SuiviParcour;

import android.location.Location;
import org.osmdroid.util.GeoPoint;
import java.util.List;

public class DistanceManager {

    private final List<GeoPoint> parcoursPoints;
    private int nextPointIndex = 0;
    private Location lastLocation = null;
    private boolean alertApproachDone = false;
    private DistanceListener listener;

    public DistanceManager(List<GeoPoint> points) {
        this.parcoursPoints = points;
    }

    public void setListener(DistanceListener listener) {
        this.listener = listener;
    }

    public void updateLocation(Location location) {
        if (lastLocation != null && location.distanceTo(lastLocation) < 3) return;
        lastLocation = location;

        if (nextPointIndex >= parcoursPoints.size()) return;

        GeoPoint nextPoint = parcoursPoints.get(nextPointIndex);
        float distanceToNext = distance(location, nextPoint);

        // alerte approche 50m
        if (distanceToNext <= 50 && !alertApproachDone) {
            alertApproachDone = true;
            if (listener != null) listener.onApproachAlert(distanceToNext);
        }

        // validation point 20m
        if (distanceToNext < 20 && nextPointIndex < parcoursPoints.size() - 1) {
            nextPointIndex++;
            alertApproachDone = false;
            if (listener != null) listener.onPointValidated();
        }

        float totalRemaining = calculerDistanceTotaleRestante(location);
        if (listener != null) listener.onDistanceUpdated(distanceToNext, totalRemaining);
    }

    private float distance(Location loc, GeoPoint point) {
        float[] result = new float[1];
        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                point.getLatitude(), point.getLongitude(), result);
        return result[0];
    }

    private float calculerDistanceTotaleRestante(Location location) {
        float total = 0;
        if (nextPointIndex < parcoursPoints.size()) {
            total += distance(location, parcoursPoints.get(nextPointIndex));
        }
        for (int i = nextPointIndex; i < parcoursPoints.size() - 1; i++) {
            float[] result = new float[1];
            Location.distanceBetween(parcoursPoints.get(i).getLatitude(),
                    parcoursPoints.get(i).getLongitude(),
                    parcoursPoints.get(i + 1).getLatitude(),
                    parcoursPoints.get(i + 1).getLongitude(),
                    result);
            total += result[0];
        }
        return total;
    }
}


