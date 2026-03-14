package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une randonnée planifiée.
 * Implémente Parcelable pour le transfert entre Activités.
 */
public class Hike implements Parcelable {

    private int id;
    private String libelle;
    private PointOfInterest depart;
    private PointOfInterest arrivee;
    private int dureeJours;
    private User creator;
    private ArrayList<Participant> participants = new ArrayList<>();
    private List<PointOfInterest> optionalPoints = new ArrayList<>();

    private List<FoodProduct> foodCatalogue = new ArrayList<>();
    private List<EquipmentItem> equipmentCatalogue = new ArrayList<>();

    private boolean optimize;

    // --- Constructeurs ---
    public Hike() {}

    public Hike(int id, String libelle, PointOfInterest depart, PointOfInterest arrivee, int dureeJours, User creator, boolean optimize) {
        this.id = id;
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours);
        this.creator = creator;
        this.optimize = optimize;
    }

    // --- Implémentation Parcelable ---

    protected Hike(Parcel in) {
        id = in.readInt();
        libelle = in.readString();
        depart = in.readParcelable(PointOfInterest.class.getClassLoader());
        arrivee = in.readParcelable(PointOfInterest.class.getClassLoader());
        dureeJours = in.readInt();
        creator = in.readParcelable(User.class.getClassLoader());

        participants = in.createTypedArrayList(Participant.CREATOR);

        List<PointOfInterest> poiList = new ArrayList<>();
        in.readList(poiList, PointOfInterest.class.getClassLoader());
        optionalPoints = new ArrayList<>(poiList);
        optimize = in.readBoolean();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // MODIFICATION : Écriture simplifiée de l'int
        dest.writeInt(id);
        dest.writeString(libelle);
        dest.writeParcelable(depart, flags);
        dest.writeParcelable(arrivee, flags);
        dest.writeInt(dureeJours);
        dest.writeParcelable(creator, flags);

        dest.writeTypedList(participants);
        dest.writeList(new ArrayList<>(optionalPoints));
        dest.writeBoolean(optimize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Hike> CREATOR = new Creator<Hike>() {
        @Override
        public Hike createFromParcel(Parcel in) {
            return new Hike(in);
        }

        @Override
        public Hike[] newArray(int size) {
            return new Hike[size];
        }
    };

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) { this.depart = depart; }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) { this.arrivee = arrivee; }

    public int getDureeJours() { return dureeJours; }
    public void setDureeJours(int dureeJours) { this.dureeJours = dureeJours; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public ArrayList<Participant> getParticipants() { return participants; }
    public void setParticipants(ArrayList<Participant> participants) { this.participants = participants; }

    public List<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(List<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }

    public List<FoodProduct> getFoodCatalogue() { return foodCatalogue; }
    public void setFoodCatalogue(List<FoodProduct> foodCatalogue) { this.foodCatalogue = foodCatalogue; }

    public List<EquipmentItem> getEquipmentGroups() { return equipmentCatalogue; }
    public void setEquipmentGroups(List<EquipmentItem> equipmentGroups) {
        this.equipmentCatalogue = equipmentGroups;
    }
    public boolean getOptimize() { return optimize; }
    public void setOptimize(boolean optimize) { this.optimize = optimize; }
}