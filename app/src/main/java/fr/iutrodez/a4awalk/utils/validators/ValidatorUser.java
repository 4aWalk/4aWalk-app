package fr.iutrodez.a4awalk.utils.validators;

import java.util.regex.Pattern;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

public class ValidatorUser {

    // Regex pour l'email
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Regex pour le mot de passe : min 8 caractères, 1 majuscule, 1 caractère spécial
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static ValidationResult validate(
            String nom,
            String prenom,
            String ageStr,
            String adresse,
            String email,
            String password,
            String confirmPassword,
            String niveau,
            String morphologie
    ) {

        // ---- Champs obligatoires ----
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

        // ---- Vérification email ----
        if (!EMAIL_PATTERN.matcher(email).matches())
            return new ValidationResult(false, "email", "Email invalide", 0);

        // ---- Vérification âge ----
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "age", "L'âge doit être un nombre", 0);
        }
        if (age < 1 || age > 120)
            return new ValidationResult(false, "age", "Âge invalide", 0);

        // ---- Vérification mot de passe (regex) ----
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return new ValidationResult(false, "password",
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un caractère spécial", 0);
        }

        // ---- Vérification que mot de passe et confirmation sont identiques ----
        if (!password.equals(confirmPassword))
            return new ValidationResult(false, "confirmPassword",
                    "Les mots de passe ne correspondent pas", 0);

        // ---- Vérification Spinners ----
        if (isSpinnerInvalidN(niveau))
            return new ValidationResult(false, "niveau", "Veuillez choisir un niveau", 0);

        if (isSpinnerInvalidM(morphologie))
            return new ValidationResult(false, "morphologie", "Veuillez choisir une morphologie", 0);

        // ---- Tout est valide ----
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