package fr.iutrodez.a4awalk.entity;

import java.util.Objects;

import fr.iutrodez.a4awalk.model.Item;

/**
 * Représente un équipement (matériel indispensable comme une tente, un duvet, etc.).
 * Implémente l'interface Item pour la logique de calcul de charge.
 */
public class EquipmentItem implements Item {

    private Long id;

    private String nom;

    private String description;

    private Double masseGrammes;

    /** * Critère spécifique (UC 2.1.4.1) :
     * Définit si cet équipement permet au participant de se reposer (ex: tente, sac de couchage).
     */
    private boolean permetRepos;

    // --- Constructeurs ---

    public EquipmentItem() {}

    public EquipmentItem(String nom, String description, double masseGrammes, boolean permetRepos) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.permetRepos = permetRepos;
    }

    // --- Méthodes de l'interface Item & Logique métier ---

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getMasseGrammes() {
        return masseGrammes;
    }

    /** * Retourne la masse convertie en Kilogrammes pour le calcul du sac à dos.
     */
    public double getWeightKg() {
        return this.masseGrammes / 1000.0;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentItem that = (EquipmentItem) o;
        // Si l'ID est présent, on l'utilise, sinon on compare par nom (clé naturelle)
        return Objects.equals(id, that.id) || Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }

    @Override
    public String toString() {
        return String.format("%s (%.1fg) [%s]", nom, masseGrammes, permetRepos ? "Repos" : "Utilitaire");
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }

    public void setDescription(String description) { this.description = description; }

    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    public boolean isPermetRepos() { return permetRepos; }
    public void setPermetRepos(boolean permetRepos) { this.permetRepos = permetRepos; }
}