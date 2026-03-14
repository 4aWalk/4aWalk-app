package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

public class PointOfInterest implements Parcelable {

    private int id;
    private String nom;
    private double latitude;
    private double longitude;
    private String description;
    private int sequence;

    /** La randonnée associée à ce point d'intérêt */
    private Hike hike;

    // --- Constructeurs ---

    public PointOfInterest() {}

    /** Constructeur rapide pour les points de départ/arrivée */
    public PointOfInterest(int id, String nom, double latitude, double longitude, String description, int sequence) {
        this.id = id;
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.sequence = sequence;
    }

    // --- Implémentation Parcelable ---

    protected PointOfInterest(Parcel in) {
        id = in.readInt();
        nom = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nom);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Creator<PointOfInterest> CREATOR = new Creator<PointOfInterest>() {
        @Override
        public PointOfInterest createFromParcel(Parcel in) {
            return new PointOfInterest(in);
        }

        @Override
        public PointOfInterest[] newArray(int size) {
            return new PointOfInterest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // --- Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSequence() { return sequence; }
    public void setSequence(int order) { this.sequence = order; }
}