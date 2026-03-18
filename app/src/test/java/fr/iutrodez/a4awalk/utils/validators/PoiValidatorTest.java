package fr.iutrodez.a4awalk.utils.validators;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests unitaires pour {@link PoiValidator}.
 *
 * Aucune dépendance Android : tests JVM purs.
 */
public class PoiValidatorTest {

    // -----------------------------------------------------------------------
    // Tests sur le nom
    // -----------------------------------------------------------------------

    @Test
    public void valider_nomNull_retourneErreur() {
        PoiValidator.ValidationResult result = PoiValidator.valider(null, "48.0", "2.0");
        assertFalse(result.isValid());
        assertEquals("Le nom du POI ne peut pas être vide.", result.getErrorMessage());
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
    }

    @Test
    public void valider_nomVide_retourneErreur() {
        PoiValidator.ValidationResult result = PoiValidator.valider("   ", "48.0", "2.0");
        assertFalse(result.isValid());
        assertEquals("Le nom du POI ne peut pas être vide.", result.getErrorMessage());
    }

    @Test
    public void valider_nomValide_pasDerreurNom() {
        PoiValidator.ValidationResult result = PoiValidator.valider("Mont Blanc", "48.0", "2.0");
        assertTrue(result.isValid());
    }

    // -----------------------------------------------------------------------
    // Tests sur la latitude
    // -----------------------------------------------------------------------

    @Test
    public void valider_latitudeNonNumerique_retourneErreurFormat() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "abc", "2.0");
        assertFalse(result.isValid());
        assertEquals("Le format de la latitude est incorrect.", result.getErrorMessage());
    }

    @Test
    public void valider_latitudeTropPetite_retourneErreurBornes() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "-90.1", "2.0");
        assertFalse(result.isValid());
        assertEquals("La latitude doit être comprise entre -90 et 90.", result.getErrorMessage());
    }

    @Test
    public void valider_latitudeTropGrande_retourneErreurBornes() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "90.1", "2.0");
        assertFalse(result.isValid());
        assertEquals("La latitude doit être comprise entre -90 et 90.", result.getErrorMessage());
    }

    @Test
    public void valider_latitudeBorneMin_retourneSucces() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "-90.0", "0.0");
        assertTrue(result.isValid());
        assertEquals(-90.0, result.getLatitude(), 0.0001);
    }

    @Test
    public void valider_latitudeBorneMax_retourneSucces() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "90.0", "0.0");
        assertTrue(result.isValid());
        assertEquals(90.0, result.getLatitude(), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Tests sur la longitude
    // -----------------------------------------------------------------------

    @Test
    public void valider_longitudeNonNumerique_retourneErreurFormat() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "48.0", "xyz");
        assertFalse(result.isValid());
        assertEquals("Le format de la longitude est incorrect.", result.getErrorMessage());
    }

    @Test
    public void valider_longitudeTropPetite_retourneErreurBornes() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "48.0", "-180.1");
        assertFalse(result.isValid());
        assertEquals("La longitude doit être comprise entre -180 et 180.", result.getErrorMessage());
    }

    @Test
    public void valider_longitudeTropGrande_retourneErreurBornes() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "48.0", "180.1");
        assertFalse(result.isValid());
        assertEquals("La longitude doit être comprise entre -180 et 180.", result.getErrorMessage());
    }

    @Test
    public void valider_longitudeBorneMin_retourneSucces() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "0.0", "-180.0");
        assertTrue(result.isValid());
        assertEquals(-180.0, result.getLongitude(), 0.0001);
    }

    @Test
    public void valider_longitudeBorneMax_retourneSucces() {
        PoiValidator.ValidationResult result = PoiValidator.valider("POI", "0.0", "180.0");
        assertTrue(result.isValid());
        assertEquals(180.0, result.getLongitude(), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Test cas valide complet
    // -----------------------------------------------------------------------

    @Test
    public void valider_donneesCompletes_retourneSucces() {
        PoiValidator.ValidationResult result = PoiValidator.valider("Sommet", "45.833", "6.865");
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertEquals(45.833, result.getLatitude(),  0.0001);
        assertEquals(6.865,  result.getLongitude(), 0.0001);
    }

    // -----------------------------------------------------------------------
    // Tests sur ValidationResult (factory methods)
    // -----------------------------------------------------------------------

    @Test
    public void validationResult_success_contientCoordonnees() {
        PoiValidator.ValidationResult r = PoiValidator.ValidationResult.success(10.0, 20.0);
        assertTrue(r.isValid());
        assertNull(r.getErrorMessage());
        assertEquals(10.0, r.getLatitude(),  0.0001);
        assertEquals(20.0, r.getLongitude(), 0.0001);
    }

    @Test
    public void validationResult_error_contientMessage() {
        PoiValidator.ValidationResult r = PoiValidator.ValidationResult.error("Erreur test");
        assertFalse(r.isValid());
        assertEquals("Erreur test", r.getErrorMessage());
        assertNull(r.getLatitude());
        assertNull(r.getLongitude());
    }
}
