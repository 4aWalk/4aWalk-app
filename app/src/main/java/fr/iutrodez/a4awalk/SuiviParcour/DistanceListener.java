package fr.iutrodez.a4awalk.SuiviParcour;

public interface DistanceListener {
    void onDistanceUpdated(float distanceToNext, float distanceToEnd);
    void onPointValidated();
    void onApproachAlert(float distance);
}


