package fr.iutrodez.a4awalk.modeles;


import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

public interface Person {
    String getNom();

    int getAge();
    Level getNiveau(); // Sportif, Entrainé, Débutant
    Morphology getMorphologie(); // Légère, Moyenne, Forte

    int getId();
}