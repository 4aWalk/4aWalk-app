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

    private Long id;
    private String libelle;
    private PointOfInterest depart;
    private PointOfInterest arrivee;
    private int dureeJours;
    private User creator;

    // CHANGEMENT MAJEUR : ArrayList au lieu de Set pour Parcelable
    private ArrayList<Participant> participants = new ArrayList<>();
    private List<PointOfInterest> optionalPoints = new ArrayList<>();

    // --- Constructeurs ---
    public Hike() {}

    public Hike(Long id, String libelle, PointOfInterest depart, PointOfInterest arrivee, int dureeJours, User creator) {
        this.id = id;
        this.libelle = libelle;
        this.depart = depart;
        this.arrivee = arrivee;
        setDureeJours(dureeJours);
        this.creator = creator;
    }

    // --- Implémentation Parcelable ---

    protected Hike(Parcel in) {
        if (in.readByte() == 0) id = null; else id = in.readLong();
        libelle = in.readString();
        depart = in.readParcelable(PointOfInterest.class.getClassLoader());
        arrivee = in.readParcelable(PointOfInterest.class.getClassLoader());
        dureeJours = in.readInt();
        creator = in.readParcelable(User.class.getClassLoader());

        // Lecture optimisée de la liste typée Participant
        participants = in.createTypedArrayList(Participant.CREATOR);

        // Lecture de la liste de POI
        // (Si POI implémente Parcelable correctement, on pourrait aussi utiliser createTypedArrayList)
        List<PointOfInterest> poiList = new ArrayList<>();
        in.readList(poiList, PointOfInterest.class.getClassLoader());
        optionalPoints = new ArrayList<>(poiList);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0);
        else { dest.writeByte((byte) 1); dest.writeLong(id); }
        dest.writeString(libelle);
        dest.writeParcelable(depart, flags);
        dest.writeParcelable(arrivee, flags);
        dest.writeInt(dureeJours);
        dest.writeParcelable(creator, flags);

        // Écriture optimisée de la liste typée
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

    /**
     * Ajoute un participant à la randonnée.
     * Vérifie manuellement l'existence car on utilise une List.
     */
    public void addParticipant(Participant participant) throws HikeException {
        if (participant == null) {
            throw new HikeException("Le participant ne peut pas être nul.");
        }
        // Utilisation de contains (nécessite equals() dans Participant)
        if (this.participants.contains(participant)) {
            throw new HikeException("Ce participant est déjà inscrit à cette randonnée.");
        }
        this.participants.add(participant);
    }

    /**
     * Retire un participant de la randonnée.
     */
    public void removeParticipant(Participant participant) throws HikeException {
        if (!this.participants.remove(participant)) {
            throw new HikeException("Le participant n'a pas été trouvé dans cette randonnée.");
        }
    }

    /**
     * Ajoute un point d'intérêt.
     */
    public void addPointOfInterest(PointOfInterest poi) {
        this.optionalPoints.add(poi);
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

    // On retourne et on accepte désormais une ArrayList/List
    public ArrayList<Participant> getParticipants() { return participants; }

    // Surcharge pour accepter Set si besoin de compatibilité legacy, mais convertit en List
    public void setParticipants(ArrayList<Participant> participants) { this.participants = participants; }

    // Méthode de compatibilité si votre Service utilise encore Set au début
    public void setParticipants(java.util.Set<Participant> participantsSet) {
        this.participants = new ArrayList<>(participantsSet);
    }

    public int participantSize() { return this.participants.size(); }

    public List<PointOfInterest> getOptionalPoints() { return optionalPoints; }
    public void setOptionalPoints(List<PointOfInterest> optionalPoints) { this.optionalPoints = optionalPoints; }
}