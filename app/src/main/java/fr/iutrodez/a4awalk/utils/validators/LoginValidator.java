package fr.iutrodez.a4awalk.utils.validators;

import android.util.Patterns;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

public class LoginValidator {

    public static ValidationResult validate(String email, String password) {

        if (email == null || email.trim().isEmpty())
            return new ValidationResult(false, "email", "Veuillez entrer votre email", 0);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return new ValidationResult(false, "email", "Email invalide", 0);

        if (password == null || password.trim().isEmpty())
            return new ValidationResult(false, "password", "Veuillez entrer votre mot de passe", 0);

        return new ValidationResult(true, null, null, 0);
    }
}
