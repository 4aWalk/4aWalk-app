package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import fr.iutrodez.a4awalk.modeles.Person;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

/**
 * Utilisateur principal du système.
 * Gère le compte, l'authentification et les randonnées créées (UC001, UC002).
 */
public class User implements Person, Parcelable {

    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private String password;
    private String adresse;
    private int age;
    private Level niveau;
    private Morphology morphologie;

    // --- Constructeurs ---
    public User() {}

    public User(String nom, String prenom, int age, String mail, String password, String adresse,
                Level niveau, Morphology morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.adresse = adresse;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    public User(String nom, String prenom, int age, String mail, String adresse,
                Level niveau, Morphology morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.adresse = adresse;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    // --- Implémentation Parcelable ---

    protected User(Parcel in) {
        id = in.readInt();
        nom = in.readString();
        prenom = in.readString();
        mail = in.readString();
        password = in.readString();
        adresse = in.readString();
        age = in.readInt();

        String niveauStr = in.readString();
        niveau = niveauStr != null ? Level.valueOf(niveauStr) : null;

        String morphoStr = in.readString();
        morphologie = morphoStr != null ? Morphology.valueOf(morphoStr) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nom);
        dest.writeString(prenom);
        dest.writeString(mail);
        dest.writeString(password);
        dest.writeString(adresse);
        dest.writeInt(age);

        // Écriture des enums
        dest.writeString(niveau != null ? niveau.name() : null);
        dest.writeString(morphologie != null ? morphologie.name() : null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // --- Implémentation de l'interface ---

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

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}