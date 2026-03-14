package fr.iutrodez.a4awalk.modeles;


import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;

public interface Person {
    String getPrenom();
    void setPrenom(String prenom);
    String getNom();
    void setNom(String nom);
    int getAge();
    void setAge(int age);
    Level getNiveau();
    void setNiveau(Level niveau);
    Morphology getMorphologie();
    void setMorphologie(Morphology morphologie);
    int getId();
    void setId(int id);
}