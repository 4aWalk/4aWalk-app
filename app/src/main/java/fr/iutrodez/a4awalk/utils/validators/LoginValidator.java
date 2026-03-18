package fr.iutrodez.a4awalk.utils.validators;

import java.util.regex.Pattern;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

public class LoginValidator {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static ValidationResult validate(String email, String password) {

        if (email == null || email.trim().isEmpty())
            return new ValidationResult(false, "email", "Veuillez entrer votre email", 0);

        if (!EMAIL_PATTERN.matcher(email).matches())
            return new ValidationResult(false, "email", "Email invalide", 0);

        if (password == null || password.trim().isEmpty())
            return new ValidationResult(false, "password", "Veuillez entrer votre mot de passe", 0);

        return new ValidationResult(true, null, null, 0);
    }
}