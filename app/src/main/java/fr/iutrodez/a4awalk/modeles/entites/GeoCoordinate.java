package fr.iutrodez.a4awalk.modeles.entites;

public class GeoCoordinate {

    private double latitude;
    private double longitude;


    // --- Constructeurs ---

    public GeoCoordinate() {
    }

    /**
     * Constructeur qui prend lat/lon mais crée l'objet standard GeoJsonPoint.
     *
     * @param latitude  (Y)
     * @param longitude (X)
     */
    public GeoCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Getters et Setters ---

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
