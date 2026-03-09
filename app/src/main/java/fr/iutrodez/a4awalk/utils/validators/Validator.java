package fr.iutrodez.a4awalk.utils.validators;

import android.util.Patterns;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

public class Validator {

    public static ValidationResult validate(
            String nom,
            String prenom,
            String ageStr,
            String adresse,
            String email,
            String password,
            String confirmPassword,  // <-- ajouté
            String niveau,
            String morphologie
    ) {

        if (isEmpty(nom))
            return new ValidationResult(false, "nom", "Veuillez entrer votre nom", 0);

        if (isEmpty(prenom))
            return new ValidationResult(false, "prenom", "Veuillez entrer votre prénom", 0);

        if (isEmpty(ageStr))
            return new ValidationResult(false, "age", "Veuillez entrer votre âge", 0);

        if (isEmpty(adresse))
            return new ValidationResult(false, "adresse", "Veuillez entrer votre adresse", 0);

        if (isEmpty(email))
            return new ValidationResult(false, "email", "Veuillez entrer votre email", 0);

        if (isEmpty(password))
            return new ValidationResult(false, "password", "Veuillez entrer un mot de passe", 0);

        if (isEmpty(confirmPassword))
            return new ValidationResult(false, "confirmPassword", "Veuillez confirmer le mot de passe", 0);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return new ValidationResult(false, "email", "Email invalide", 0);

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "age", "L'âge doit être un nombre", 0);
        }

        if (age < 1 || age > 120)
            return new ValidationResult(false, "age", "Âge invalide", 0);

        if (password.length() < 6)
            return new ValidationResult(false, "password", "Mot de passe trop court", 0);

        // 🔥 VERIFICATION MOT DE PASSE
        if (!password.equals(confirmPassword))
            return new ValidationResult(false, "confirmPassword", "Les mots de passe ne correspondent pas", 0);

        if (isSpinnerInvalidN(niveau))
            return new ValidationResult(false, "niveau", "Veuillez choisir un niveau", 0);

        if (isSpinnerInvalidM(morphologie))
            return new ValidationResult(false, "morphologie", "Veuillez choisir une morphologie", 0);

        return new ValidationResult(true, null, null, age);
    }

    private static boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }

    private static boolean isSpinnerInvalidN(String v) {
        return v == null || v.equalsIgnoreCase("Choisir votre niveau");
    }

    private static boolean isSpinnerInvalidM(String v) {
        return v == null || v.equalsIgnoreCase("Choisir votre morphologie");
    }
}
