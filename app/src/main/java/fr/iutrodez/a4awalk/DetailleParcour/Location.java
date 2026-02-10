package fr.iutrodez.a4awalk.DetailleParcour;

/**
 * Représente un point de départ ou d'arrivée avec coordonnées et description.
 */
public class Location {

    private double latitude;
    private double longitude;
    private String description;

    /**
     * Retourne la latitude.
     *
     * @return Latitude en degrés
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Retourne la longitude.
     *
     * @return Longitude en degrés
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Retourne la description du lieu.
     *
     * @return Description, ou null si non défini
     */
    public String getDescription() {
        return description;
    }

    /**
     * Constructeur par défaut pour Gson.
     */
    public Location() {
    }

    /**
     * Constructeur avec tous les champs.
     *
     * @param latitude    Latitude en degrés
     * @param longitude   Longitude en degrés
     * @param description Description du lieu
     */
    public Location(double latitude, double longitude, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }
}
