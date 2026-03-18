package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import android.text.TextUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link ValidateurRandonnee}.
 *
 * {@code android.text.TextUtils} est mocké statiquement car il ne peut pas
 * être instancié dans un contexte JVM pur.
 *
 * Dépendances build.gradle :
 *   testImplementation 'org.mockito:mockito-inline:5.x'
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateurRandonneeTest {

    // -----------------------------------------------------------------------
    // Helper : configure TextUtils.isEmpty() de façon transparente
    // -----------------------------------------------------------------------

    /**
     * Lance le test en mockant TextUtils.isEmpty() :
     * retourne true si la chaîne est null ou vide (comportement réel).
     */
    private String verifier(String nom, String depLat, String depLon,
                            String arrLat, String arrLon, int duree) {
        try (MockedStatic<TextUtils> tu = mockStatic(TextUtils.class)) {
            // Simule le comportement réel de TextUtils.isEmpty()
            tu.when(() -> TextUtils.isEmpty(any()))
              .thenAnswer(inv -> {
                  CharSequence cs = inv.getArgument(0);
                  return cs == null || cs.length() == 0;
              });

            return ValidateurRandonnee.verifierDonnees(nom, depLat, depLon, arrLat, arrLon, duree);
        }
    }

    // -----------------------------------------------------------------------
    // Tests sur le nom
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_nomNull_retourneErreur() {
        String err = verifier(null, "48.0", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("Le nom de la randonnée est obligatoire.", err);
    }

    @Test
    public void verifierDonnees_nomVide_retourneErreur() {
        String err = verifier("", "48.0", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("Le nom de la randonnée est obligatoire.", err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la durée
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_dureeZero_retourneErreur() {
        String err = verifier("Tour du Mont Blanc", "48.0", "2.0", "45.0", "6.0", 0);
        assertNotNull(err);
        assertEquals("La durée doit être d'au moins 1 jour.", err);
    }

    @Test
    public void verifierDonnees_dureeNegative_retourneErreur() {
        String err = verifier("Tour du Mont Blanc", "48.0", "2.0", "45.0", "6.0", -3);
        assertNotNull(err);
        assertEquals("La durée doit être d'au moins 1 jour.", err);
    }

    @Test
    public void verifierDonnees_duree1_valide() {
        String err = verifier("Tour du Mont Blanc", "48.0", "2.0", "45.0", "6.0", 1);
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la latitude de départ
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_depLatVide_retourneErreur() {
        String err = verifier("Rando", "", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude de départ est invalide (doit être entre -90 et 90).", err);
    }

    @Test
    public void verifierDonnees_depLatNonNumerique_retourneErreur() {
        String err = verifier("Rando", "abc", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude de départ est invalide (doit être entre -90 et 90).", err);
    }

    @Test
    public void verifierDonnees_depLatTropPetite_retourneErreur() {
        String err = verifier("Rando", "-90.1", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude de départ est invalide (doit être entre -90 et 90).", err);
    }

    @Test
    public void verifierDonnees_depLatTropGrande_retourneErreur() {
        String err = verifier("Rando", "90.1", "2.0", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude de départ est invalide (doit être entre -90 et 90).", err);
    }

    @Test
    public void verifierDonnees_depLatBorneMin_valide() {
        String err = verifier("Rando", "-90", "2.0", "45.0", "6.0", 2);
        assertNull(err);
    }

    @Test
    public void verifierDonnees_depLatBorneMax_valide() {
        String err = verifier("Rando", "90", "2.0", "45.0", "6.0", 2);
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la longitude de départ
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_depLonNonNumerique_retourneErreur() {
        String err = verifier("Rando", "48.0", "xyz", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La longitude de départ est invalide (doit être entre -180 et 180).", err);
    }

    @Test
    public void verifierDonnees_depLonTropPetite_retourneErreur() {
        String err = verifier("Rando", "48.0", "-180.1", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La longitude de départ est invalide (doit être entre -180 et 180).", err);
    }

    @Test
    public void verifierDonnees_depLonTropGrande_retourneErreur() {
        String err = verifier("Rando", "48.0", "180.1", "45.0", "6.0", 2);
        assertNotNull(err);
        assertEquals("La longitude de départ est invalide (doit être entre -180 et 180).", err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la latitude d'arrivée
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_arrLatNonNumerique_retourneErreur() {
        String err = verifier("Rando", "48.0", "2.0", "abc", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude d'arrivée est invalide (doit être entre -90 et 90).", err);
    }

    @Test
    public void verifierDonnees_arrLatHorsLimites_retourneErreur() {
        String err = verifier("Rando", "48.0", "2.0", "91", "6.0", 2);
        assertNotNull(err);
        assertEquals("La latitude d'arrivée est invalide (doit être entre -90 et 90).", err);
    }

    // -----------------------------------------------------------------------
    // Tests sur la longitude d'arrivée
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_arrLonNonNumerique_retourneErreur() {
        String err = verifier("Rando", "48.0", "2.0", "45.0", "xyz", 2);
        assertNotNull(err);
        assertEquals("La longitude d'arrivée est invalide (doit être entre -180 et 180).", err);
    }

    @Test
    public void verifierDonnees_arrLonHorsLimites_retourneErreur() {
        String err = verifier("Rando", "48.0", "2.0", "45.0", "200", 2);
        assertNotNull(err);
        assertEquals("La longitude d'arrivée est invalide (doit être entre -180 et 180).", err);
    }

    // -----------------------------------------------------------------------
    // Test avec virgule décimale (acceptée grâce au replace)
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_coordonneesAvecVirgule_valide() {
        String err = verifier("Rando", "48,5", "2,3", "45,8", "6,9", 3);
        assertNull(err);
    }

    // -----------------------------------------------------------------------
    // Test du cas entièrement valide
    // -----------------------------------------------------------------------

    @Test
    public void verifierDonnees_donneesCompletes_retourneNull() {
        String err = verifier("GR20", "42.387", "8.537", "41.544", "9.058", 15);
        assertNull(err);
    }
}
