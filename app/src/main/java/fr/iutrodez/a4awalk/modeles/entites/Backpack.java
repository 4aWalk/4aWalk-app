package fr.iutrodez.a4awalk.modeles.entites;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente le chargement du sac à dos d'un participant.
 * C'est l'entité centrale pour le résultat de l'optimisation du chargement.
 */
public class Backpack {

    private int id;

    /** Poids total réel porté en Kg (Somme des équipements et de la nourriture) */
    private double totalMassKg;

    /** Le propriétaire du sac (Lien One-to-One avec Participant) */
    private Participant owner;

    /** Liste des produits alimentaires présents dans le sac */
    private Set<FoodProduct> foodItems = new HashSet<>();

    /** Liste des équipements présents dans le sac */
    private Set<EquipmentItem> equipmentItems = new HashSet<>();

    // --- Constructeurs ---

    public Backpack() {
        this.totalMassKg = 0.0;
    }

    public Backpack(Participant owner) {
        this();
        this.owner = owner;
    }

    // --- Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotalMassKg() { return totalMassKg; }
    public void setTotalMassKg(double totalMassKg) { this.totalMassKg = totalMassKg; }

    public Participant getOwner() { return owner; }
    public void setOwner(Participant owner) { this.owner = owner; }

    public Set<FoodProduct> getFoodItems() { return foodItems; }
    public void setFoodItems(Set<FoodProduct> foodItems) { this.foodItems = foodItems; }

    public Set<EquipmentItem> getEquipmentItems() { return equipmentItems; }
    public void setEquipmentItems(Set<EquipmentItem> equipmentItems) { this.equipmentItems = equipmentItems; }
}