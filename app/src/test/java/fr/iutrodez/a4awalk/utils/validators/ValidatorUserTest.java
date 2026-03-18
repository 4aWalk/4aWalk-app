package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.iutrodez.a4awalk.modeles.entites.ValidationResult;

/**
 * Tests unitaires pour {@link ValidatorUser}.
 */
public class ValidatorUserTest {

    // -----------------------------------------------------------------------
    // Constantes pour les appels valides
    // -----------------------------------------------------------------------

    private static final String NOM_OK          = "Dupont";
    private static final String PRENOM_OK       = "Jean";
    private static final String AGE_OK          = "30";
    private static final String ADRESSE_OK      = "1 rue de la Paix";
    private static final String EMAIL_OK        = "jean.dupont@example.com";
    private static final String PASSWORD_OK     = "Password1!";
    private static final String CONFIRM_OK      = "Password1!";
    private static final String NIVEAU_OK       = "Intermédiaire";
    private static final String MORPHO_OK       = "Normale";

    // -----------------------------------------------------------------------
    // Helper : exécute validate() directement sans mock
    // -----------------------------------------------------------------------

    private ValidationResult validate(String nom, String prenom, String age, String adresse,
                                      String email, String password, String confirm,
                                      String niveau, String morphologie) {

        return ValidatorUser.validate(nom, prenom, age, adresse, email,
                password, confirm, niveau, morphologie);
    }

    /** Raccourci pour un appel entièrement valide */
    private ValidationResult valide() {
        return validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
    }

    // -----------------------------------------------------------------------
    // Tests sur les champs obligatoires
    // -----------------------------------------------------------------------

    @Test
    public void validate_nomNull_retourneErreur() {
        ValidationResult r = validate(null, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("nom", r.getField());
        assertEquals("Veuillez entrer votre nom", r.getMessage());
    }

    @Test
    public void validate_nomVide_retourneErreur() {
        ValidationResult r = validate("   ", PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("nom", r.getField());
    }

    @Test
    public void validate_prenomNull_retourneErreur() {
        ValidationResult r = validate(NOM_OK, null, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("prenom", r.getField());
    }

    @Test
    public void validate_ageNull_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, null, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("age", r.getField());
    }

    @Test
    public void validate_adresseVide_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, "", EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("adresse", r.getField());
    }

    @Test
    public void validate_emailVide_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, "",
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("email", r.getField());
        assertEquals("Veuillez entrer votre email", r.getMessage());
    }

    @Test
    public void validate_passwordVide_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                "", CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("password", r.getField());
    }

    @Test
    public void validate_confirmPasswordVide_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, "", NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("confirmPassword", r.getField());
    }

    // -----------------------------------------------------------------------
    // Tests sur l'email
    // -----------------------------------------------------------------------

    @Test
    public void validate_emailInvalide_retourneErreur() {
        // Ici, on passe "not-an-email". La Regex interne va le détecter toute seule !
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, "not-an-email",
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("email", r.getField());
        assertEquals("Email invalide", r.getMessage());
    }

    // -----------------------------------------------------------------------
    // Tests sur l'âge
    // -----------------------------------------------------------------------

    @Test
    public void validate_ageNonNumerique_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, "abc", ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("age", r.getField());
        assertEquals("L'âge doit être un nombre", r.getMessage());
    }

    @Test
    public void validate_ageZero_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, "0", ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("age", r.getField());
        assertEquals("Âge invalide", r.getMessage());
    }

    @Test
    public void validate_ageTropGrand_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, "121", ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("age", r.getField());
        assertEquals("Âge invalide", r.getMessage());
    }

    @Test
    public void validate_age1_valide() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, "1", ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertTrue(r.isValid());
    }

    @Test
    public void validate_age120_valide() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, "120", ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertTrue(r.isValid());
    }

    // -----------------------------------------------------------------------
    // Tests sur le mot de passe (regex)
    // -----------------------------------------------------------------------

    @Test
    public void validate_passwordSansMajuscule_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                "password1!", CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("password", r.getField());
    }

    @Test
    public void validate_passwordSansSpecial_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                "Password1", CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("password", r.getField());
    }

    @Test
    public void validate_passwordTropCourt_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                "P1!", CONFIRM_OK, NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("password", r.getField());
    }

    @Test
    public void validate_passwordValide_pasDerreur() {
        ValidationResult r = valide();
        assertTrue(r.isValid());
    }

    // -----------------------------------------------------------------------
    // Tests sur la confirmation de mot de passe
    // -----------------------------------------------------------------------

    @Test
    public void validate_confirmPasswordDifferent_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, "Autre1!", NIVEAU_OK, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("confirmPassword", r.getField());
        assertEquals("Les mots de passe ne correspondent pas", r.getMessage());
    }

    // -----------------------------------------------------------------------
    // Tests sur les Spinners (niveau / morphologie)
    // -----------------------------------------------------------------------

    @Test
    public void validate_niveauNull_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, null, MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("niveau", r.getField());
        assertEquals("Veuillez choisir un niveau", r.getMessage());
    }

    @Test
    public void validate_niveauChoixParDefaut_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, "Choisir votre niveau", MORPHO_OK);
        assertFalse(r.isValid());
        assertEquals("niveau", r.getField());
    }

    @Test
    public void validate_morphologieNull_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, null);
        assertFalse(r.isValid());
        assertEquals("morphologie", r.getField());
        assertEquals("Veuillez choisir une morphologie", r.getMessage());
    }

    @Test
    public void validate_morphologieChoixParDefaut_retourneErreur() {
        ValidationResult r = validate(NOM_OK, PRENOM_OK, AGE_OK, ADRESSE_OK, EMAIL_OK,
                PASSWORD_OK, CONFIRM_OK, NIVEAU_OK, "Choisir votre morphologie");
        assertFalse(r.isValid());
        assertEquals("morphologie", r.getField());
    }

    // -----------------------------------------------------------------------
    // Test du cas entièrement valide
    // -----------------------------------------------------------------------

    @Test
    public void validate_donneesCompletes_retourneSucces() {
        ValidationResult r = valide();
        assertTrue(r.isValid());
        assertNull(r.getField());
        assertNull(r.getMessage());
        assertEquals(30, r.getAge());
    }
}