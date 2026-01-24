package fr.iutrodez.a4awalk.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import fr.iutrodez.a4awalk.error.HikeException;

/**
 * Représente une randonnée planifiée.
 * Gère l'itinéraire, les participants et les points d'intérêt (UC004).
 */
public class Hike implements Parcelable {

    private Long id;

    private String libelle;

    private String depart;
    private String arrivee;

    /** Durée en jours (Contrainte : entre 1 et 3 jours) */
    private int dureeJours;

    private User creator;

    private Set<Participant> participants = new HashSet<>();

    private Set<PointOfInterest> optionalPoints = new HashSet<>();

    // --- Constructeurs ---

    public Hike() {}

    public Hike(Long id, String libelle, String depart, String arrivee, int dureeJours, User creator) {
        this.id = id;
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours); // Utilise le setter pour valider la règle
        this.creator = creator;
    }

    protected Hike(Parcel in) {
        id = in.readLong();
        libelle = in.readString();
        depart = in.readString();
        arrivee = in.readString();
        dureeJours = in.readInt();
        //TODO
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

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(depart);
        dest.writeString(arrivee);
        dest.writeInt(dureeJours);
        //TODO
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // --- Logique métier de bas niveau (Entity Logic) ---

    /**
     * Ajoute un participant à la randonnée.
     * @throws HikeException si le participant est nul ou déjà présent.
     */
    public void addParticipant(Participant participant) throws HikeException {
        if (participant == null) {
            throw new HikeException("Le participant ne peut pas être nul.");
        }
        if (!this.participants.add(participant)) {
            throw new HikeException("Ce participant est déjà inscrit à cette randonnée.");
        }
    }

    /**
     * Retire un participant de la randonnée.
     * Utilise equals() basé sur l'ID du participant.
     */
    public void removeParticipant(Participant participant) throws HikeException {
        if (!this.participants.remove(participant)) {
            throw new HikeException("Le participant n'a pas été trouvé dans cette randonnée.");
        }
    }

    /**
     * Ajoute un point d'intérêt et maintient la cohérence bidirectionnelle.
     */
    public void addPointOfInterest(PointOfInterest poi) {
        this.optionalPoints.add(poi);
        poi.setHike(this);
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hike hike = (Hike) o;
        return Objects.equals(id, hike.id) || Objects.equals(libelle, hike.libelle);
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDepart() { return depart; }
    public void setDepart(String depart) { this.depart = depart; }

    public String getArrivee() { return arrivee; }
    public void setArrivee(String arrivee) { this.arrivee = arrivee; }

    public int getDureeJours() { return dureeJours; }

    /** Valide que la durée est comprise entre 1 et 3 jours */
    public void setDureeJours(int dureeJours) {
        if (dureeJours < 1 || dureeJours > 3) {
            throw new IllegalArgumentException("La durée doit être comprise entre 1 et 3 jours.");
        }
        this.dureeJours = dureeJours;
    }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public Set<Participant> getParticipants() { return participants; }
    public void setParticipants(Set<Participant> participants) { this.participants = participants; }

    public int participantSize() {return this.participants.size();}

    public Set<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(Set<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }}