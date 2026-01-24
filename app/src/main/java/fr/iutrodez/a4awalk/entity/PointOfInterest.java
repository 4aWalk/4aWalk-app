package fr.iutrodez.a4awalk.entity;

public class PointOfInterest {

    private Long id;

    private String name;

    private double latitude;

    private double longitude;

    private String description;

    /** La randonnée associée à ce point d'intérêt */
    private Hike hike;

    // --- Constructeurs ---

    public PointOfInterest() {}

    public PointOfInterest(String name, double latitude, double longitude, String description, Hike hike) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.hike = hike;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Hike getHike() { return hike; }
    public void setHike(Hike hike) { this.hike = hike; }
}
