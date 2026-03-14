package fr.iutrodez.a4awalk.modeles;

import android.os.Parcel;

/**
 * Interface définissant le contrat pour tout objet pouvant être transporté
 * dans un sac à dos (Backpack).
 * Utilisée pour l'unification du calcul des charges (UC 2.1.4).
 */
public interface Item {

    Long getId();
    String getNom();
    double getMasseGrammes();
    int getNbItem();

    void setId(Long id);
    void setNom(String nom);
    void setMasseGrammes(double masseGrammes);
    void setNbItem(int nbItem);
}