package fr.iutrodez.a4awalk.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import fr.iutrodez.a4awalk.model.Person;
import fr.iutrodez.a4awalk.model.enums.Level;
import fr.iutrodez.a4awalk.model.enums.Morphology;

/**
 * Utilisateur principal du système.
 * Gère le compte, l'authentification et les randonnées créées (UC001, UC002).
 */
public class User implements Person {

    private Long id;

    private String nom;

    private String prenom;

    private String mail;

    private String password; // Stocké sous forme de hash (BCrypt par exemple)

    private String adresse;

    private int age;

    private Level niveau;

    private Morphology morphologie;

    /** Liste des randonnées dont cet utilisateur est l'organisateur */
    private Set<Hike> createdHikes = new HashSet<>();

    // --- Constructeurs ---

    public User() {}

    /** Constructeur complet (sans ID car géré par la BDD) */
    public User(String nom, String prenom, String mail, String password, String adresse,
                int age, Level niveau, Morphology morphologie) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.adresse = adresse;
        this.age = age;
        this.niveau = niveau;
        this.morphologie = morphologie;
    }

    // --- Logique métier de bas niveau ---

    /** Retourne le nom complet formaté */
    public String getFullName() {
        return prenom + " " + nom.toUpperCase();
    }

    /** Ajoute une randonnée créée et assure la cohérence du lien */
    public void addCreatedHike(Hike hike) {
        this.createdHikes.add(hike);
        hike.setCreator(this);
    }

    // --- Implémentation de l'interface Person ---

    @Override
    public String getNom() {
        return this.nom;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public Level getNiveau() {
        return this.niveau;
    }

    @Override
    public Morphology getMorphologie() {
        return this.morphologie;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // L'email est unique en base, c'est notre identifiant métier le plus fiable
        return Objects.equals(mail, user.mail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mail);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, mail='%s', nom='%s']", id, mail, nom);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public void setNom(String nom) { this.nom = nom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public void setAge(int age) { this.age = age; }

    public void setNiveau(Level niveau) { this.niveau = niveau; }

    public void setMorphologie(Morphology morphologie) { this.morphologie = morphologie; }

    public Set<Hike> getCreatedHikes() { return createdHikes; }
    public void setCreatedHikes(Set<Hike> createdHikes) { this.createdHikes = createdHikes; }
}