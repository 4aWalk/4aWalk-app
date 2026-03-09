package fr.iutrodez.a4awalk.utils.validators; // Adapte le package selon ton arborescence

import android.text.TextUtils;

public class ValidateurRandonnee {

    /**
     * Vérifie la validité des données de base d'une randonnée.
     *
     * @param nom       Le libellé de la randonnée
     * @param depLatStr Latitude de départ (String brut)
     * @param depLonStr Longitude de départ (String brut)
     * @param arrLatStr Latitude d'arrivée (String brut)
     * @param arrLonStr Longitude d'arrivée (String brut)
     * @param duree     La durée en jours
     * @return null si tout est valide, sinon un message d'erreur (String).
     */
    public static String verifierDonnees(String nom, String depLatStr, String depLonStr,
                                         String arrLatStr, String arrLonStr, int duree) {

        // 1. Vérification du Nom
        if (TextUtils.isEmpty(nom)) {
            return "Le nom de la randonnée est obligatoire.";
        }

        // 2. Vérification de la Durée
        if (duree <= 0) {
            return "La durée doit être d'au moins 1 jour.";
        }

        // 3. Vérification des Coordonnées de DÉPART
        if (!estCoordonneeValide(depLatStr, -90, 90)) {
            return "La latitude de départ est invalide (doit être entre -90 et 90).";
        }
        if (!estCoordonneeValide(depLonStr, -180, 180)) {
            return "La longitude de départ est invalide (doit être entre -180 et 180).";
        }

        // 4. Vérification des Coordonnées d'ARRIVÉE
        if (!estCoordonneeValide(arrLatStr, -90, 90)) {
            return "La latitude d'arrivée est invalide (doit être entre -90 et 90).";
        }
        if (!estCoordonneeValide(arrLonStr, -180, 180)) {
            return "La longitude d'arrivée est invalide (doit être entre -180 et 180).";
        }

        // Si tout est bon, on retourne null
        return null;
    }

    /**
     * Méthode interne pour vérifier si une string est un double valide dans les bornes.
     */
    private static boolean estCoordonneeValide(String coord, double min, double max) {
        if (TextUtils.isEmpty(coord)) return false;
        try {
            double val = Double.parseDouble(coord.replace(",", ".")); // Gère virgule ou point
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}