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

    // --- Logique métier ---

    public double distanceTo(GeoCoordinate other) {
        if (other == null) return 0;
        final int R = 6371000; // Rayon Terre en mètres

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
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
