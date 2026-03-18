package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests unitaires pour {@link ValidateurEquipement}.
 *
 * Aucune dépendance Android : tests JVM purs.
 *
 * Règles métier :
 *   - nom       : obligatoire (non vide)
 *   - masse     : 50 < masse <= 5000  (en grammes)
 *   - nbItem    : 0 < nbItem <= 3
 */
public class ValidateurEquipementTest {

    // -----------------------------------------------------------------------
    // Tests sur le nom
    // -----------------------------------------------------------------------

    @Test
    public void valider_nomNull_retourneErreur() {
        String err = ValidateurEquipement.valider(null, "100", "1");
        assertNotNull(err);
        assertEquals("Le nom de l'équipement est obligatoire.", err);
    }

    @Test
    public void valider_nomVide_retourneErreur() {
        String err = ValidateurEquipement.valider("   ", "100", "1");
        assertNotNull(err);
        assertEquals("Le nom de l'équipement est obligatoire.", err);
    }

    // -----------------------------------------------------------------------
    // Tests sur les champs numériques (format)
    // -----------------------------------------------------------------------

    @Test
    public void valider_masseNonNumerique_retourneErreur() {
        String err = ValidateurEquipement.valider("Gourde", "abc", "1");
        assertNotNull(err);
        assertEquals("Veuillez remplir correctement les champs numériques (masse et quantité).", err);
    }

    @Test
    public void valider_nbItemNonNumerique_retourneErreur() {
        String err = ValidateurEquipement.valider("Gourde", "200", "x");
        assertNotNull(err);
        assertEquals("Veuillez remplir correctement les champs numériques (masse et quantité).", err);
    }

    @Test
    public void valider_masseAvecVirgule_acceptee() {
        // "200,5" doit être converti en 200.5
        String err = ValidateurEquipement.valider("Gourde", "200,5", "1");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la masse (bornes : 50 < masse <= 5000)
    // -----------------------------------------------------------------------

    @Test
    public void valider_masseLimiteBasse_retourneErreur() {
        // masse == 50 → invalide (doit être > 50)
        String err = ValidateurEquipement.valider("Gourde", "50", "1");
        assertNotNull(err);
        assertEquals("La masse doit être comprise entre 1g et 5 000g.", err);
    }

    @Test
    public void valider_masseNegative_retourneErreur() {
        String err = ValidateurEquipement.valider("Gourde", "-10", "1");
        assertNotNull(err);
        assertEquals("La masse doit être comprise entre 1g et 5 000g.", err);
    }

    @Test
    public void valider_masseTropElevee_retourneErreur() {
        // masse > 5000 → invalide
        String err = ValidateurEquipement.valider("Gourde", "5001", "1");
        assertNotNull(err);
        assertEquals("La masse doit être comprise entre 1g et 5 000g.", err);
    }

    @Test
    public void valider_masseLimiteHaute_valide() {
        // masse == 5000 → valide
        String err = ValidateurEquipement.valider("Gourde", "5000", "2");
        assertNull(err);
    }

    @Test
    public void valider_masseJusteSuperieureA50_valide() {
        // masse == 51 → valide
        String err = ValidateurEquipement.valider("Gourde", "51", "1");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la quantité (bornes : 0 < nbItem <= 3)
    // -----------------------------------------------------------------------

    @Test
    public void valider_nbItemZero_retourneErreur() {
        String err = ValidateurEquipement.valider("Gourde", "200", "0");
        assertNotNull(err);
        assertEquals("La quantité doit être comprise entre 1 et 3.", err);
    }

    @Test
    public void valider_nbItemNegatif_retourneErreur() {
        String err = ValidateurEquipement.valider("Gourde", "200", "-1");
        assertNotNull(err);
        assertEquals("La quantité doit être comprise entre 1 et 3.", err);
    }

    @Test
    public void valider_nbItemTropEleve_retourneErreur() {
        // nbItem > 3 → invalide
        String err = ValidateurEquipement.valider("Gourde", "200", "4");
        assertNotNull(err);
        assertEquals("La quantité doit être comprise entre 1 et 3.", err);
    }

    @Test
    public void valider_nbItemLimiteHaute_valide() {
        // nbItem == 3 → valide
        String err = ValidateurEquipement.valider("Gourde", "200", "3");
        assertNull(err);
    }

    @Test
    public void valider_nbItem1_valide() {
        String err = ValidateurEquipement.valider("Gourde", "200", "1");
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Test du cas entièrement valide
    // -----------------------------------------------------------------------

    @Test
    public void valider_donneesCompletes_retourneNull() {
        String err = ValidateurEquipement.valider("Sac à dos", "1500", "2");
        assertNull(err);
    }
}
