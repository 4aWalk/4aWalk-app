package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import fr.iutrodez.a4awalk.modeles.Person;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Représente un participant à une randonnée.
 * Implémente Parcelable pour permettre le transfert via Intent.
 */
public class Participant implements Person, Parcelable {

    private int id;
    private int idRando;
    private String nom;
    private String prenom;

    private int age;
    private Level niveau;
    private Morphology morphologie;
    private boolean creator;
    private int besoinKcal;
    private double besoinEauLitre;
    private double capaciteEmportMaxKg;
    private Backpack backpack;

    // --- Constructeurs ---
    public Participant() {
    }

    public Participant(String nom, String prenom, int age, Level niveau, Morphology morphologie, boolean creator,
                       int besoinKcal, double besoinEauLitre, double capaciteEmportMaxKg, int idRando) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.creator = creator;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
        this.idRando = idRando;
    }

    // --- Implémentation Parcelable ---

    protected Participant(Parcel in) {
        // MODIFICATION : Lecture simplifiée pour les types primitifs
        id = in.readInt();
        idRando = in.readInt();
        nom = in.readString();
        prenom = in.readString();
        age = in.readInt();

        String niveauStr = in.readString();
        niveau = (niveauStr != null) ? Level.valueOf(niveauStr) : null;

        String morphoStr = in.readString();
        morphologie = (morphoStr != null) ? Morphology.valueOf(morphoStr) : null;

        creator = in.readByte() != 0;
        besoinKcal = in.readInt();
        besoinEauLitre = in.readInt();
        capaciteEmportMaxKg = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // MODIFICATION : Écriture simplifiée (plus de gestion de null pour les numériques)
        dest.writeInt(id);
        dest.writeInt(idRando);
        dest.writeString(nom);
        dest.writeString(prenom);
        dest.writeInt(age);
        dest.writeString(niveau != null ? niveau.name() : null);
        dest.writeString(morphologie != null ? morphologie.name() : null);
        dest.writeByte((byte) (creator ? 1 : 0));
        dest.writeInt(besoinKcal);
        dest.writeDouble(besoinEauLitre);
        dest.writeDouble(capaciteEmportMaxKg);
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

    // --- Override interface ---

    @Override
    public String getPrenom() { return prenom; }
    @Override
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String getNom() {return this.nom;}
    @Override
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public void setAge(int age) { this.age = age; }

    @Override
    public Level getNiveau() {
        return this.niveau;
    }

    @Override
    public void setNiveau(Level niveau) { this.niveau = niveau; }

    @Override
    public Morphology getMorphologie() {
        return this.morphologie;
    }

    @Override
    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }

    // --- Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRando() {
        return idRando;
    }
    public void setIdRando(int idRando) {
        this.idRando = idRando;
    }

    public boolean getCreator() { return creator; }
    public void setCreator(boolean isCreator) { this.creator = isCreator; }
    public int getBesoinKcal() { return besoinKcal; }
    public void setBesoinKcal(int besoinKcal) { this.besoinKcal = besoinKcal; }

    public double getBesoinEauLitre() { return besoinEauLitre; }
    public void setBesoinEauLitre(int besoinEauLitre) { this.besoinEauLitre = besoinEauLitre; }

    public double getCapaciteEmportMaxKg() { return capaciteEmportMaxKg; }
    public void setCapaciteEmportMaxKg(double capaciteEmportMaxKg) { this.capaciteEmportMaxKg = capaciteEmportMaxKg; }

    public Backpack getBackpack() { return backpack; }
    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
        if (backpack != null && backpack.getOwner() != this) {
            backpack.setOwner(this);
        }
    }
}