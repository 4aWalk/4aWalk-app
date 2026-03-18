package fr.iutrodez.a4awalk.utils.validators;

public class ValidateurEquipement {

    /**
     * Valide les saisies pour un EquipmentItem.
     * @return null si tout est valide, sinon retourne le message d'erreur à afficher.
     */
    public static String valider(String nom, String masseStr, String nbItemStr) {
        if (nom == null || nom.trim().isEmpty()) {
            return "Le nom de l'équipement est obligatoire.";
        }

        double masse;
        int nbItem;

        try {
            masse = Double.parseDouble(masseStr.replace(",", "."));
            nbItem = Integer.parseInt(nbItemStr);
        } catch (NumberFormatException e) {
            return "Veuillez remplir correctement les champs numériques (masse et quantité).";
        }

        // On vérifie que la masse est cohérente (ex: pas de sac à dos de 100 kilos)
        if (masse <= 50 || masse > 5000) {
            return "La masse doit être comprise entre 1g et 5 000g.";
        }

        // On évite les quantités farfelues
        if (nbItem <= 0 || nbItem > 3) {
            return "La quantité doit être comprise entre 1 et 3.";
        }

        return null; // Aucune erreur, les données sont valides
    }
}