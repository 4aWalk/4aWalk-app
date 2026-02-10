package fr.iutrodez.a4awalk.SuiviParcour;

/**
 * Interface pour recevoir des événements liés à la progression d'un parcours.
 * <p>
 * Permet de notifier la distance restante, la validation d'un point et les alertes de proximité.
 */
public interface DistanceListener {

    /**
     * Appelé lorsque la distance vers le prochain point et vers la fin du parcours est mise à jour.
     *
     * @param distanceToNext Distance en mètres vers le prochain point
     * @param distanceToEnd  Distance en mètres vers la fin du parcours
     */
    void onDistanceUpdated(float distanceToNext, float distanceToEnd);

    /**
     * Appelé lorsqu'un point du parcours a été validé.
     */
    void onPointValidated();

    /**
     * Appelé lorsqu'une alerte de proximité est déclenchée.
     *
     * @param distance Distance en mètres pour laquelle l'alerte est déclenchée
     */
    void onApproachAlert(float distance);
}
