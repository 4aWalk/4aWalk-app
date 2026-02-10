package fr.iutrodez.a4awalk.SuiviParcour;

import android.location.Location;
import org.osmdroid.util.GeoPoint;
import java.util.List;

/**
 * Gestionnaire de distance pour un parcours.
 * <p>
 * Permet de suivre la position de l'utilisateur, calculer les distances vers
 * le prochain point et la fin du parcours, et déclencher des alertes ou
 * validations de points.
 */
public class DistanceManager {

    /** Liste des points du parcours */
    private final List<GeoPoint> parcoursPoints;

    /** Index du prochain point à atteindre */
    private int nextPointIndex = 0;

    /** Dernière position GPS reçue */
    private Location lastLocation = null;

    /** Indique si l'alerte d'approche a déjà été déclenchée */
    private boolean alertApproachDone = false;

    /** Listener pour notifier les événements de distance */
    private DistanceListener listener;

    /**
     * Crée un DistanceManager avec la liste des points du parcours.
     *
     * @param points Liste des points GeoPoint du parcours
     */
    public DistanceManager(List<GeoPoint> points) {
        this.parcoursPoints = points;
    }

    /**
     * Définit le listener pour recevoir les événements de distance.
     *
     * @param listener Listener implémentant DistanceListener
     */
    public void setListener(DistanceListener listener) {
        this.listener = listener;
    }

    /**
     * Met à jour la position de l'utilisateur.
     * <p>
     * Déclenche :
     * - Alerte d'approche si l'utilisateur est à moins de 50m du point suivant
     * - Validation du point si l'utilisateur est à moins de 20m
     * - Notification des distances vers le prochain point et la fin du parcours
     *
     * @param location Position GPS actuelle
     */
    public void updateLocation(Location location) {
        if (lastLocation != null && location.distanceTo(lastLocation) < 3) return;
        lastLocation = location;

        if (nextPointIndex >= parcoursPoints.size()) return;

        GeoPoint nextPoint = parcoursPoints.get(nextPointIndex);
        float distanceToNext = distanceToPoint(location, nextPoint);

        // Alerte approche 50m
        if (distanceToNext <= 50 && !alertApproachDone) {
            alertApproachDone = true;
            if (listener != null) listener.onApproachAlert(distanceToNext);
        }

        // Validation point 20m
        if (distanceToNext < 20 && nextPointIndex < parcoursPoints.size() - 1) {
            nextPointIndex++;
            alertApproachDone = false;
            if (listener != null) listener.onPointValidated();
        }

        float totalRemaining = calculateTotalRemainingDistance(location);
        if (listener != null) listener.onDistanceUpdated(distanceToNext, totalRemaining);
    }

    /**
     * Calcule la distance entre la position GPS et un GeoPoint.
     *
     * @param loc   Position GPS
     * @param point Point du parcours
     * @return Distance en mètres
     */
    private float distanceToPoint(Location loc, GeoPoint point) {
        float[] result = new float[1];
        Location.distanceBetween(
                loc.getLatitude(), loc.getLongitude(),
                point.getLatitude(), point.getLongitude(),
                result
        );
        return result[0];
    }

    /**
     * Calcule la distance totale restante jusqu'à la fin du parcours.
     *
     * @param location Position GPS actuelle
     * @return Distance restante en mètres
     */
    private float calculateTotalRemainingDistance(Location location) {
        float total = 0;

        if (nextPointIndex < parcoursPoints.size()) {
            total += distanceToPoint(location, parcoursPoints.get(nextPointIndex));
        }

        for (int i = nextPointIndex; i < parcoursPoints.size() - 1; i++) {
            float[] segment = new float[1];
            GeoPoint start = parcoursPoints.get(i);
            GeoPoint end = parcoursPoints.get(i + 1);

            Location.distanceBetween(
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude(),
                    segment
            );
            total += segment[0];
        }

        return total;
    }
}
