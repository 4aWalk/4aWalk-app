package fr.iutrodez.a4awalk.modeles.entites;


import java.util.Objects;

import fr.iutrodez.a4awalk.modeles.Item;

/**
 * Représente un produit alimentaire du catalogue.
 * Implémente l'interface Item pour s'intégrer dans le calcul de charge du sac.
 * (Correspond à l'UC 2.1.4.4 - Caractéristiques nutritionnelles)
 */
public class FoodProduct implements Item {

    private Long id;

    private String nom;

    private String description;

    private Double masseGrammes;

    private String appellationCourante;
    private String conditionnement;

    private Double apportNutritionnelKcal;

    private Double prixEuro;

    // --- Constructeurs ---

    public FoodProduct() {}

    public FoodProduct(String nom, String description, double masseGrammes, String appellationCourante,
                       String conditionnement, double apportNutritionnelKcal, double prixEuro) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.appellationCourante = appellationCourante;
        this.conditionnement = conditionnement;
        this.apportNutritionnelKcal = apportNutritionnelKcal;
        this.prixEuro = prixEuro;
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

    /** Retourne la masse en Kg pour le calcul global du sac */
    public double getWeightKg() {
        return this.masseGrammes / 1000.0;
    }

    /** * Calcule le ratio Kcal/Gramme.
     * Plus ce chiffre est élevé, plus l'aliment est efficace pour la randonnée.
     */
    public double getEnergyDensity() {
        if (masseGrammes <= 0) return 0;
        return apportNutritionnelKcal / masseGrammes;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodProduct that = (FoodProduct) o;
        return Objects.equals(id, that.id) || Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] (%.0f Kcal, %.2f€)",
                nom, conditionnement, apportNutritionnelKcal, prixEuro);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }

    public void setDescription(String description) { this.description = description; }

    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    public String getAppellationCourante() { return appellationCourante; }
    public void setAppellationCourante(String appellationCourante) { this.appellationCourante = appellationCourante; }

    public String getConditionnement() { return conditionnement; }
    public void setConditionnement(String conditionnement) { this.conditionnement = conditionnement; }

    public double getApportNutritionnelKcal() { return apportNutritionnelKcal; }
    public void setApportNutritionnelKcal(double apportNutritionnelKcal) { this.apportNutritionnelKcal = apportNutritionnelKcal; }

    public double getPrixEuro() { return prixEuro; }
    public void setPrixEuro(double prixEuro) { this.prixEuro = prixEuro; }
}