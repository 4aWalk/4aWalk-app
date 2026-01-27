package fr.iutrodez.a4awalk.model;


import fr.iutrodez.a4awalk.model.enums.Level;
import fr.iutrodez.a4awalk.model.enums.Morphology;

public interface Person {
    String getNom();

    int getAge();
    Level getNiveau(); // Sportif, Entrainé, Débutant
    Morphology getMorphologie(); // Légère, Moyenne, Forte

    Long getId();
}