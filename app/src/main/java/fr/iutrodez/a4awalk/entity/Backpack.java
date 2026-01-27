package fr.iutrodez.a4awalk.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Représente le chargement du sac à dos d'un participant.
 * C'est l'entité centrale pour le résultat de l'optimisation du chargement.
 */
public class Backpack {

    private Long id;

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

    // --- Logique métier de bas niveau (Entity Logic) ---

    /**
     * Recalcule le poids total du sac à partir des masses de chaque item.
     * Cette méthode doit être appelée après chaque modification du contenu.
     */
    public void updateAndGetTotalMass() {
        double mass = 0.0;

        if (equipmentItems != null) {
            mass += equipmentItems.stream()
                    .mapToDouble(EquipmentItem::getWeightKg)
                    .sum();
        }

        if (foodItems != null) {
            mass += foodItems.stream()
                    .mapToDouble(FoodProduct::getWeightKg)
                    .sum();
        }

        this.totalMassKg = mass;
    }

    /** Vide intégralement le contenu du sac */
    public void clearContent() {
        this.foodItems.clear();
        this.equipmentItems.clear();
        this.totalMassKg = 0.0;
    }

    // --- Overrides Standards ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Backpack backpack = (Backpack) o;
        // L'égalité est basée sur l'ID technique ou sur le propriétaire unique
        return Objects.equals(id, backpack.id) || Objects.equals(owner, backpack.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner);
    }

    // --- Getters et Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getTotalMassKg() { return totalMassKg; }
    public void setTotalMassKg(double totalMassKg) { this.totalMassKg = totalMassKg; }

    public Participant getOwner() { return owner; }
    public void setOwner(Participant owner) { this.owner = owner; }

    public Set<FoodProduct> getFoodItems() { return foodItems; }
    public void setFoodItems(Set<FoodProduct> foodItems) { this.foodItems = foodItems; }

    public Set<EquipmentItem> getEquipmentItems() { return equipmentItems; }
    public void setEquipmentItems(Set<EquipmentItem> equipmentItems) { this.equipmentItems = equipmentItems; }
}