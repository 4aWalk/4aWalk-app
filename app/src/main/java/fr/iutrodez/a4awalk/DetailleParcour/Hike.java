package fr.iutrodez.a4awalk.DetailleParcour;

/**
 * Représente une randonnée (Hike) avec son identifiant, son nom et sa durée en jours.
 */
public class Hike {

    private int id;
    private String libelle;
    private int dureeJours;

    /**
     * Constructeur par défaut pour Gson ou autres frameworks de sérialisation.
     */
    public Hike() {
    }

    /**
     * Constructeur avec tous les champs.
     *
     * @param id        Identifiant unique de la randonnée
     * @param libelle   Nom de la randonnée
     * @param dureeJours Durée en jours
     */
    public Hike(int id, String libelle, int dureeJours) {
        this.id = id;
        this.libelle = libelle;
        this.dureeJours = dureeJours;
    }

    /**
     * Retourne l'identifiant unique de la randonnée.
     *
     * @return Identifiant de la randonnée
     */
    public int getId() {
        return id;
    }

    /**
     * Retourne le nom de la randonnée.
     *
     * @return Nom de la randonnée
     */
    public String getLibelle() {
        return libelle;
    }

    /**
     * Retourne la durée de la randonnée en jours.
     *
     * @return Durée en jours
     */
    public int getDureeJours() {
        return dureeJours;
    }

    /**
     * Retourne une représentation textuelle de la randonnée.
     *
     * @return Chaîne décrivant la randonnée
     */
    @Override
    public String toString() {
        return "Hike{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", dureeJours=" + dureeJours +
                '}';
    }
}
