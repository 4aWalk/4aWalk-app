package fr.iutrodez.a4awalk.DetailParcours;

/**
 * Représente un point géographique d'un parcours avec latitude et longitude.
 */
public class Point {

    private double latitude;
    private double longitude;

    /**
     * Retourne la latitude du point.
     *
     * @return Latitude en degrés
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Retourne la longitude du point.
     *
     * @return Longitude en degrés
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Constructeur par défaut pour Gson.
     */
    public Point() {
    }

    /**
     * Constructeur avec latitude et longitude.
     *
     * @param latitude  Latitude en degrés
     * @param longitude Longitude en degrés
     */
    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
