package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

/**
 * Tests unitaires pour {@link LoginValidator}.
 */
public class LoginValidatorTest {

    // -----------------------------------------------------------------------
    // Tests sur l'email
    // -----------------------------------------------------------------------

    @Test
    public void validate_emailNull_retourneErreurEmail() {
        ValidationResult result = LoginValidator.validate(null, "Password1!");
        assertFalse(result.isValid());
        assertEquals("email", result.getField());
        assertEquals("Veuillez entrer votre email", result.getMessage());
    }

    @Test
    public void validate_emailVide_retourneErreurEmail() {
        ValidationResult result = LoginValidator.validate("   ", "Password1!");
        assertFalse(result.isValid());
        assertEquals("email", result.getField());
        assertEquals("Veuillez entrer votre email", result.getMessage());
    }

    @Test
    public void validate_emailInvalide_retourneErreurEmailInvalide() {
        ValidationResult result = LoginValidator.validate("not-an-email", "Password1!");
        assertFalse(result.isValid());
        assertEquals("email", result.getField());
        assertEquals("Email invalide", result.getMessage());
    }

    // -----------------------------------------------------------------------
    // Tests sur le mot de passe
    // -----------------------------------------------------------------------

    @Test
    public void validate_motDePasseNull_retourneErreurPassword() {
        ValidationResult result = LoginValidator.validate("user@test.com", null);
        assertFalse(result.isValid());
        assertEquals("password", result.getField());
        assertEquals("Veuillez entrer votre mot de passe", result.getMessage());
    }

    @Test
    public void validate_motDePasseVide_retourneErreurPassword() {
        ValidationResult result = LoginValidator.validate("user@test.com", "   ");
        assertFalse(result.isValid());
        assertEquals("password", result.getField());
        assertEquals("Veuillez entrer votre mot de passe", result.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test du cas valide
    // -----------------------------------------------------------------------

    @Test
    public void validate_donneesValides_retourneSucces() {
        ValidationResult result = LoginValidator.validate("user@test.com", "Password1!");
        assertTrue(result.isValid());
        assertNull(result.getField());
        assertNull(result.getMessage());
    }
}