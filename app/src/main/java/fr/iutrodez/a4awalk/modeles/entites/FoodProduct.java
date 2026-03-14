package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import fr.iutrodez.a4awalk.modeles.Item;

/**
 * Représente un produit alimentaire du catalogue.
 * Implémente l'interface Item pour s'intégrer dans le calcul de charge du sac.
 * (Correspond à l'UC 2.1.4.4 - Caractéristiques nutritionnelles)
 */
public class FoodProduct implements Item, Parcelable {

    private Long id;

    private String nom;

    private String description;

    private Double masseGrammes;

    private String appellationCourante;

    private String conditionnement;

    private Double apportNutritionnelKcal;

    private Double prixEuro;

    private int nbItem;


    // --- Constructeurs ---

    public FoodProduct() {}

    public FoodProduct(String nom, double masseGrammes, String appellationCourante,
                       String conditionnement, double apportNutritionnelKcal, double prixEuro, int nbItem) {
        this.nom = nom;
        this.masseGrammes = masseGrammes;
        this.appellationCourante = appellationCourante;
        this.conditionnement = conditionnement;
        this.apportNutritionnelKcal = apportNutritionnelKcal;
        this.prixEuro = prixEuro;
        this.nbItem = nbItem;
    }

    protected FoodProduct(Parcel in) {
        if (in.readByte() == 0) id = null; else id = in.readLong();
        nom = in.readString();
        description = in.readString();
        if (in.readByte() == 0) masseGrammes = null; else masseGrammes = in.readDouble();
        appellationCourante = in.readString();
        conditionnement = in.readString();
        if (in.readByte() == 0) apportNutritionnelKcal = null; else apportNutritionnelKcal = in.readDouble();
        if (in.readByte() == 0) prixEuro = null; else prixEuro = in.readDouble();
        nbItem = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeLong(id); }
        dest.writeString(nom);
        dest.writeString(description);
        if (masseGrammes == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(masseGrammes); }
        dest.writeString(appellationCourante);
        dest.writeString(conditionnement);
        if (apportNutritionnelKcal == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(apportNutritionnelKcal); }
        if (prixEuro == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(prixEuro); }
        dest.writeInt(nbItem);
    }

    public static final Creator<FoodProduct> CREATOR = new Creator<FoodProduct>() {
        @Override public FoodProduct createFromParcel(Parcel in) { return new FoodProduct(in); }
        @Override public FoodProduct[] newArray(int size) { return new FoodProduct[size]; }
    };

    @Override public int describeContents() { return 0; }

    // Override de l'interface

    @Override
    public Long getId() { return id; }

    @Override
    public String getNom() { return nom; }

    @Override
    public double getMasseGrammes() { return masseGrammes; }

    @Override
    public int getNbItem() { return nbItem; }

    @Override
    public void setId(Long id) { this.id = id; }

    @Override
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public void setMasseGrammes(double masseGrammes) { this.masseGrammes = masseGrammes; }

    @Override
    public void setNbItem(int nbItem) { this.nbItem = nbItem; }

    // --- Getters et Setters ---

    public String getAppellationCourante() { return appellationCourante; }
    public void setAppellationCourante(String appellationCourante) { this.appellationCourante = appellationCourante; }

    public String getConditionnement() { return conditionnement; }
    public void setConditionnement(String conditionnement) { this.conditionnement = conditionnement; }

    public double getApportNutritionnelKcal() { return apportNutritionnelKcal;}
    public void setApportNutritionnelKcal(double apportNutritionnelKcal) {
        this.apportNutritionnelKcal = apportNutritionnelKcal;
    }

    public double getPrixEuro() { return prixEuro; }
    public void setPrixEuro(double prixEuro) { this.prixEuro = prixEuro; }

    public int getTotalMasses() { return (int) (this.masseGrammes * this.nbItem); }

    public double getTotalMassesKg(){ return this.masseGrammes * this.nbItem / 1000; }

    public int getTotalKcals() { return (int) (this.apportNutritionnelKcal * this.nbItem); }
}