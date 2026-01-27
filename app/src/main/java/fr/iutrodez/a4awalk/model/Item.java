package fr.iutrodez.a4awalk.model;

/**
 * Interface définissant le contrat pour tout objet pouvant être transporté
 * dans un sac à dos (Backpack).
 * Utilisée pour l'unification du calcul des charges (UC 2.1.4).
 */
public interface Item {

    /**
     * @return Le nom ou la désignation de l'objet.
     */
    String getNom();

    /**
     * @return Une description détaillée de l'objet.
     */
    String getDescription();

    /**
     * @return La masse de l'objet exprimée en grammes.
     */
    double getMasseGrammes();

    /**
     * Méthode par défaut pour obtenir le poids en kilogrammes.
     * Utile pour les calculs de charge totale du sac.
     */
    default double getMasseKg() {
        return getMasseGrammes() / 1000.0;
    }
}