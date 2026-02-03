package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import fr.iutrodez.a4awalk.modeles.Person;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Représente un participant à une randonnée.
 * Implémente Parcelable pour permettre le transfert via Intent.
 */
public class Participant implements Person, Parcelable {

    private Long id;
    private String noParticipant; // Ajout du champ
    private Integer age;
    private Level niveau;
    private Morphology morphologie;
    private boolean creator = false;
    private Integer besoinKcal = 0;
    private Integer besoinEauLitre = 0;
    private Double capaciteEmportMaxKg = 0.0;
    private Backpack backpack;

    // --- Constructeurs ---
    public Participant() {}

    public Participant(int age, Level niveau, Morphology morphologie, boolean creator,
                       int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg) {
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.creator = creator;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
    }

    public Participant(String noParticipant, int age, Level niveau, Morphology morphologie, boolean creator,
                       int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg) {
        this.noParticipant = noParticipant;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.creator = creator;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
    }

    // --- Implémentation Parcelable ---

    protected Participant(Parcel in) {
        if (in.readByte() == 0) id = null; else id = in.readLong();

        // Lecture de noParticipant
        noParticipant = in.readString();

        if (in.readByte() == 0) age = null; else age = in.readInt();

        // Lecture des Enums via leur nom String
        String niveauStr = in.readString();
        niveau = (niveauStr != null) ? Level.valueOf(niveauStr) : null;

        String morphoStr = in.readString();
        morphologie = (morphoStr != null) ? Morphology.valueOf(morphoStr) : null;

        creator = in.readByte() != 0; // byte to boolean

        if (in.readByte() == 0) besoinKcal = null; else besoinKcal = in.readInt();
        if (in.readByte() == 0) besoinEauLitre = null; else besoinEauLitre = in.readInt();
        if (in.readByte() == 0) capaciteEmportMaxKg = null; else capaciteEmportMaxKg = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeLong(id); }

        // Écriture de noParticipant
        dest.writeString(noParticipant);

        if (age == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeInt(age); }

        // Écriture des Enums
        dest.writeString(niveau != null ? niveau.name() : null);
        dest.writeString(morphologie != null ? morphologie.name() : null);

        dest.writeByte((byte) (creator ? 1 : 0)); // boolean to byte

        if (besoinKcal == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeInt(besoinKcal); }
        if (besoinEauLitre == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeInt(besoinEauLitre); }
        if (capaciteEmportMaxKg == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(capaciteEmportMaxKg); }
    }

    public static final Creator<Participant> CREATOR = new Creator<Participant>() {
        @Override
        public Participant createFromParcel(Parcel in) {
            return new Participant(in);
        }

        @Override
        public Participant[] newArray(int size) {
            return new Participant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // --- Logique métier (Entity Logic) ---

    public boolean isOverloaded() {
        if (this.backpack == null) return false;
        return this.backpack.getTotalMassKg() > this.capaciteEmportMaxKg;
    }

    // --- Méthode toString pour l'affichage dans la ListView ---
    @NonNull
    @Override
    public String toString() {
        String label = (noParticipant != null && !noParticipant.isEmpty()) ? "Participant " + noParticipant : "Nouveau participant";
        String details = "";
        if (age != null) details += age + " ans";
        if (niveau != null) details += " - " + niveau;

        return label + " (" + details + ")";
    }

    // --- Implémentation de l'interface Person ---
    @Override public String getNom() { return noParticipant; } // On utilise noParticipant comme "nom" par défaut
    @Override public int getAge() { return (age != null) ? age : 0; }
    @Override public Level getNiveau() { return this.niveau; }
    @Override public Morphology getMorphologie() { return this.morphologie; }

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNoParticipant() { return noParticipant; }
    public void setNoParticipant(String noParticipant) { this.noParticipant = noParticipant; }

    public void setAge(int age) { this.age = age; }
    public void setNiveau(Level niveau) { this.niveau = niveau; }
    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }
    public boolean getCreator() { return creator; }
    public void setCreator(boolean isCreator) { this.creator = isCreator; }
    public int getBesoinKcal() { return (besoinKcal != null) ? besoinKcal : 0; }
    public void setBesoinKcal(int besoinKcal) { this.besoinKcal = besoinKcal; }
    public int getBesoinEauLitre() { return (besoinEauLitre != null) ? besoinEauLitre : 0; }
    public void setBesoinEauLitre(int besoinEauLitre) { this.besoinEauLitre = besoinEauLitre; }
    public double getCapaciteEmportMaxKg() { return (capaciteEmportMaxKg != null) ? capaciteEmportMaxKg : 0.0; }
    public void setCapaciteEmportMaxKg(double capaciteEmportMaxKg) { this.capaciteEmportMaxKg = capaciteEmportMaxKg; }
    public Backpack getBackpack() { return backpack; }
    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
        if (backpack != null && backpack.getOwner() != this) {
            backpack.setOwner(this);
        }
    }
}