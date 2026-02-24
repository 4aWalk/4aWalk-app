package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.iutrodez.a4awalk.modeles.erreurs.HikeException;

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

    // --- Constructeurs ---
    public Hike() {}

    public Hike(int id, String libelle, PointOfInterest depart, PointOfInterest arrivee, int dureeJours, User creator) {
        this.id = id;
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours);
        this.creator = creator;
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

    // --- Logique métier (Entity Logic) ---

    public void addParticipant(Participant participant) throws HikeException {
        if (participant == null) {
            throw new HikeException("Le participant ne peut pas être nul.");
        }
        if (this.participants.contains(participant)) {
            throw new HikeException("Ce participant est déjà inscrit à cette randonnée.");
        }
        this.participants.add(participant);
    }

    public void removeParticipant(Participant participant) throws HikeException {
        if (!this.participants.remove(participant)) {
            throw new HikeException("Le participant n'a pas été trouvé dans cette randonnée.");
        }
    }

    public void addPointOfInterest(PointOfInterest poi) {
        this.optionalPoints.add(poi);
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hike hike = (Hike) o;
        // MODIFICATION : Comparaison directe (==) car id est un type primitif
        return id == hike.id || Objects.equals(libelle, hike.libelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libelle);
    }

    @Override
    public String toString() {
        return String.format("Hike[id=%d, libelle='%s', participants=%d]",
                id, libelle, participants.size());
    }

    // --- Getters et Setters ---

    // MODIFICATION : type de retour int
    public int getId() { return id; }
    // MODIFICATION : paramètre type int
    public void setId(int id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) { this.depart = depart; }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) { this.arrivee = arrivee; }

    public int getDureeJours() { return dureeJours; }

    public void setDureeJours(int dureeJours) {
        if (dureeJours < 1 || dureeJours > 3) {
            throw new IllegalArgumentException("La durée doit être comprise entre 1 et 3 jours.");
        }
        this.dureeJours = dureeJours;
    }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public ArrayList<Participant> getParticipants() { return participants; }

    public void setParticipants(ArrayList<Participant> participants) { this.participants = participants; }

    public void setParticipants(java.util.Set<Participant> participantsSet) {
        this.participants = new ArrayList<>(participantsSet);
    }

    public int participantSize() { return this.participants.size(); }

    public List<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(List<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }
}