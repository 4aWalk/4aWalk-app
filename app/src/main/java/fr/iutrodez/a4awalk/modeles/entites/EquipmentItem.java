package fr.iutrodez.a4awalk.modeles.entites;

import android.os.Parcel;
import android.os.Parcelable;

import fr.iutrodez.a4awalk.modeles.Item;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;

/**
 * Représente un équipement (matériel indispensable comme une tente, un duvet, etc.).
 * Implémente l'interface Item pour la logique de calcul de charge.
 */
public class EquipmentItem implements Item, Parcelable {

    private Long id;

    private String nom;

    private String description;

    private Double masseGrammes;

    private int nbItem;

    private TypeEquipment type;

    private double masseAVide;


    // --- Constructeurs ---

    public EquipmentItem() {}

    public EquipmentItem(String nom,
                         String description,
                         double masseGrammes,
                         int nbItem,
                         TypeEquipment type,
                         double masseAVide) {
        this.nom = nom;
        this.description = description;
        this.masseGrammes = masseGrammes;
        this.nbItem = nbItem;
        this.type = type;
        this.masseAVide = masseAVide;
    }

    protected EquipmentItem(Parcel in) {
        if (in.readByte() == 0) id = null; else id = in.readLong();
        nom = in.readString();
        description = in.readString();
        if (in.readByte() == 0) masseGrammes = null; else masseGrammes = in.readDouble();
        nbItem = in.readInt();
        String typeStr = in.readString();
        type = typeStr != null ? TypeEquipment.valueOf(typeStr) : null;
        masseAVide = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeLong(id); }
        dest.writeString(nom);
        dest.writeString(description);
        if (masseGrammes == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(masseGrammes); }
        dest.writeInt(nbItem);
        dest.writeString(type != null ? type.name() : null);
        dest.writeDouble(masseAVide);
    }

    public static final Creator<EquipmentItem> CREATOR = new Creator<EquipmentItem>() {
        @Override public EquipmentItem createFromParcel(Parcel in) { return new EquipmentItem(in); }
        @Override public EquipmentItem[] newArray(int size) { return new EquipmentItem[size]; }
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TypeEquipment getType() { return type; }
    public void setType(TypeEquipment type){ this.type = type;}

    public double getMasseAVide() { return masseAVide; }
    public void setMasseAVide(double masseAVide) {this.masseAVide = masseAVide;}

    public double getTotalMasses() { return this.masseGrammes - this.masseAVide * this.nbItem; }
    public double getTotalMassesKg() {return this.getTotalMasses() / 1000;}
}