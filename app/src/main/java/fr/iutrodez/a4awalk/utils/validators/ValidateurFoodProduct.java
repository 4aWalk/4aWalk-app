package fr.iutrodez.a4awalk.utils.validators;

public class ValidateurFoodProduct {

    /**
     * Valide les saisies pour un FoodProduct.
     * @return null si tout est valide, sinon retourne le message d'erreur à afficher.
     */
    public static String valider(String nom, String masseStr, String appellation, String kcalStr, String prixStr) {
        if (nom == null || nom.trim().isEmpty() || appellation == null || appellation.trim().isEmpty()) {
            return "Le nom et l'appellation sont obligatoires.";
        }

        double masse, kcal, prix;

        try {
            masse = Double.parseDouble(masseStr.replace(",", "."));
            kcal = Double.parseDouble(kcalStr.replace(",", "."));
            prix = Double.parseDouble(prixStr.replace(",", "."));
        } catch (NumberFormatException e) {
            return "Veuillez remplir correctement les champs numériques.";
        }

        if (masse < 50 || masse > 5000) {
            return "La masse doit être entre 50g et 5000g.";
        }
        if (kcal < 50 || kcal > 3000) {
            return "L'apport nutritionnel doit être entre 50 et 3000 Kcal.";
        }
        if (prix < 0) {
            return "Le prix ne peut pas être négatif.";
        }

        return null; // Aucune erreur, les données sont valides
    }
}