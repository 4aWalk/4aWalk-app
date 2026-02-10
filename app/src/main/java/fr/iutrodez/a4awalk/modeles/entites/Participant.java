package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

import fr.iutrodez.a4awalk.modeles.Person;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Représente un participant à une randonnée.
 * Implémente Parcelable pour permettre le transfert via Intent.
 */
public class Participant implements Person, Parcelable {

    private Long id;
    private Long idRando; // <--- NOUVEAU CHAMP

    private String nom;
    private String prenom;

    private Integer age;
    private Level niveau;
    private Morphology morphologie;
    private boolean creator = false;
    private Integer besoinKcal = 0;
    private Integer besoinEauLitre = 0;
    private Double capaciteEmportMaxKg = 0.0;
    private Backpack backpack;

    // --- Constructeurs ---
    public Participant() {
    }

    // Mise à jour du constructeur pour prendre en compte idRando
    public Participant(String nom, String prenom, int age, Level niveau, Morphology morphologie, boolean creator,
                       int besoinKcal, int besoinEauLitre, double capaciteEmportMaxKg, Long idRando) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
        this.creator = creator;
        this.besoinKcal = besoinKcal;
        this.besoinEauLitre = besoinEauLitre;
        this.capaciteEmportMaxKg = capaciteEmportMaxKg;
        this.idRando = idRando; // <--- ASSIGNATION
    }

    // --- Implémentation Parcelable ---

    protected Participant(Parcel in) {
        if (in.readByte() == 0) id = null;
        else id = in.readLong();
        if (in.readByte() == 0) idRando = null;
        else idRando = in.readLong(); // <--- LECTURE PARCEL

        nom = in.readString();
        prenom = in.readString();

        if (in.readByte() == 0) age = null;
        else age = in.readInt();

        String niveauStr = in.readString();
        niveau = (niveauStr != null) ? Level.valueOf(niveauStr) : null;

        String morphoStr = in.readString();
        morphologie = (morphoStr != null) ? Morphology.valueOf(morphoStr) : null;

        creator = in.readByte() != 0;

        if (in.readByte() == 0) besoinKcal = null;
        else besoinKcal = in.readInt();
        if (in.readByte() == 0) besoinEauLitre = null;
        else besoinEauLitre = in.readInt();
        if (in.readByte() == 0) capaciteEmportMaxKg = null;
        else capaciteEmportMaxKg = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        if (idRando == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeLong(idRando);
        } // <--- ECRITURE PARCEL

        dest.writeString(nom);
        dest.writeString(prenom);

        if (age == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(age);
        }
        dest.writeString(niveau != null ? niveau.name() : null);
        dest.writeString(morphologie != null ? morphologie.name() : null);
        dest.writeByte((byte) (creator ? 1 : 0));
        if (besoinKcal == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(besoinKcal);
        }
        if (besoinEauLitre == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(besoinEauLitre);
        }
        if (capaciteEmportMaxKg == null) dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeDouble(capaciteEmportMaxKg);
        }
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

    // --- Logique métier ---

    public boolean isOverloaded() {
        if (this.backpack == null) return false;
        return this.backpack.getTotalMassKg() > this.capaciteEmportMaxKg;
    }

    // --- Overrides Equals & HashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNull
    @Override
    public String toString() {
        String fullName = "";
        if (prenom != null && !prenom.isEmpty()) fullName += prenom + " ";
        if (nom != null && !nom.isEmpty()) fullName += nom;

        if (fullName.trim().isEmpty()) {
            fullName = "Nouveau participant";
        } else {
            fullName = fullName.trim();
        }

        String details = "";
        if (age != null) details += age + " ans";
        if (niveau != null) details += " - " + niveau;

        return fullName + " (" + details + ")";
    }

    // --- Implémentation de l'interface Person ---

    @Override
    public String getNom() {
        return this.nom;
    }

    @Override
    public int getAge() {
        return (age != null) ? age : 0;
    }

    @Override
    public Level getNiveau() {
        return this.niveau;
    }

    @Override
    public Morphology getMorphologie() {
        return this.morphologie;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdRando() {
        return idRando;
    } // <--- GETTER

    public void setIdRando(Long idRando) {
        this.idRando = idRando;
    } // <--- SETTER

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setNiveau(Level niveau) {
        this.niveau = niveau;
    }

    public void setMorphologie(Morphology morphologie) {
        this.morphologie = morphologie;
    }

    public boolean getCreator() {
        return creator;
    }

    public void setCreator(boolean isCreator) {
        this.creator = isCreator;
    }

    public int getBesoinKcal() {
        return (besoinKcal != null) ? besoinKcal : 0;
    }

    public void setBesoinKcal(int besoinKcal) {
        this.besoinKcal = besoinKcal;
    }

    public int getBesoinEauLitre() {
        return (besoinEauLitre != null) ? besoinEauLitre : 0;
    }

    public void setBesoinEauLitre(int besoin) {
        this.besoinEauLitre = besoinEauLitre;
    }

    public double getCapaciteEmportMaxKg() {
        return this.capaciteEmportMaxKg;
    }

    public void setCapaciteEmportMaxKg(double capacite) {
        this.capaciteEmportMaxKg = capacite;
    }
}