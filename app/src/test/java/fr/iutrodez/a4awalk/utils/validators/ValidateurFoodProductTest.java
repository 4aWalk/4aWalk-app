package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests unitaires pour {@link ValidateurFoodProduct}.
 *
 * Aucune dépendance Android : tests JVM purs.
 *
 * Règles métier :
 *   - nom & appellation : obligatoires
 *   - masse             : 50 <= masse <= 5000
 *   - kcal              : 50 <= kcal <= 3000
 *   - prix              : prix >= 0
 */
public class ValidateurFoodProductTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String valider(String nom, String masse, String appellation, String kcal, String prix) {
        return ValidateurFoodProduct.valider(nom, masse, appellation, kcal, prix);
    }

    // -----------------------------------------------------------------------
    // Tests sur nom / appellation
    // -----------------------------------------------------------------------

    @Test
    public void valider_nomNull_retourneErreur() {
        String err = valider(null, "200", "Barre", "200", "1.5");
        assertNotNull(err);
        assertEquals("Le nom et l'appellation sont obligatoires.", err);
    }

    @Test
    public void valider_nomVide_retourneErreur() {
        String err = valider("   ", "200", "Barre", "200", "1.5");
        assertNotNull(err);
        assertEquals("Le nom et l'appellation sont obligatoires.", err);
    }

    @Test
    public void valider_appellationNull_retourneErreur() {
        String err = valider("Snickers", "200", null, "200", "1.5");
        assertNotNull(err);
        assertEquals("Le nom et l'appellation sont obligatoires.", err);
    }

    @Test
    public void valider_appellationVide_retourneErreur() {
        String err = valider("Snickers", "200", "  ", "200", "1.5");
        assertNotNull(err);
        assertEquals("Le nom et l'appellation sont obligatoires.", err);
    }

    // -----------------------------------------------------------------------
    // Tests sur les champs numériques (format)
    // -----------------------------------------------------------------------

    @Test
    public void valider_masseNonNumerique_retourneErreur() {
        String err = valider("Barre", "abc", "Snickers", "200", "1.5");
        assertNotNull(err);
        assertEquals("Veuillez remplir correctement les champs numériques.", err);
    }

    @Test
    public void valider_kcalNonNumerique_retourneErreur() {
        String err = valider("Barre", "200", "Snickers", "abc", "1.5");
        assertNotNull(err);
        assertEquals("Veuillez remplir correctement les champs numériques.", err);
    }

    @Test
    public void valider_prixNonNumerique_retourneErreur() {
        String err = valider("Barre", "200", "Snickers", "200", "abc");
        assertNotNull(err);
        assertEquals("Veuillez remplir correctement les champs numériques.", err);
    }

    @Test
    public void valider_masseAvecVirgule_acceptee() {
        String err = valider("Barre", "200,5", "Snickers", "250", "1,5");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la masse (bornes : 50 <= masse <= 5000)
    // -----------------------------------------------------------------------

    @Test
    public void valider_masseTropPetite_retourneErreur() {
        String err = valider("Barre", "49", "Snickers", "200", "1.5");
        assertNotNull(err);
        assertEquals("La masse doit être entre 50g et 5000g.", err);
    }

    @Test
    public void valider_masseTropGrande_retourneErreur() {
        String err = valider("Barre", "5001", "Snickers", "200", "1.5");
        assertNotNull(err);
        assertEquals("La masse doit être entre 50g et 5000g.", err);
    }

    @Test
    public void valider_masseLimiteBasseValide() {
        String err = valider("Barre", "50", "Snickers", "200", "1.5");
        assertNull(err);
    }

    @Test
    public void valider_masseLimiteHauteValide() {
        String err = valider("Barre", "5000", "Snickers", "200", "1.5");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur les kcal (bornes : 50 <= kcal <= 3000)
    // -----------------------------------------------------------------------

    @Test
    public void valider_kcalTropPetit_retourneErreur() {
        String err = valider("Barre", "200", "Snickers", "49", "1.5");
        assertNotNull(err);
        assertEquals("L'apport nutritionnel doit être entre 50 et 3000 Kcal.", err);
    }

    @Test
    public void valider_kcalTropGrand_retourneErreur() {
        String err = valider("Barre", "200", "Snickers", "3001", "1.5");
        assertNotNull(err);
        assertEquals("L'apport nutritionnel doit être entre 50 et 3000 Kcal.", err);
    }

    @Test
    public void valider_kcalLimiteBasseValide() {
        String err = valider("Barre", "200", "Snickers", "50", "1.5");
        assertNull(err);
    }

    @Test
    public void valider_kcalLimiteHauteValide() {
        String err = valider("Barre", "200", "Snickers", "3000", "1.5");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur le prix (prix >= 0)
    // -----------------------------------------------------------------------

    @Test
    public void valider_prixNegatif_retourneErreur() {
        String err = valider("Barre", "200", "Snickers", "200", "-0.01");
        assertNotNull(err);
        assertEquals("Le prix ne peut pas être négatif.", err);
    }

    @Test
    public void valider_prixZero_valide() {
        // Prix à 0 est accepté (pas négatif)
        String err = valider("Barre", "200", "Snickers", "200", "0");
        assertNull(err);
    }

    @Test
    public void valider_prixPositif_valide() {
        String err = valider("Barre", "200", "Snickers", "200", "2.99");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Test du cas entièrement valide
    // -----------------------------------------------------------------------

    @Test
    public void valider_donneesCompletes_retourneNull() {
        String err = valider("Snickers", "50", "Barre chocolatée", "250", "1.5");
        assertNull(err);
    }
}
