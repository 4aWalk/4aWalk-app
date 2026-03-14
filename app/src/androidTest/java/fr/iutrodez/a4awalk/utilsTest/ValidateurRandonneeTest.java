package fr.iutrodez.a4awalk.utilsTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import fr.iutrodez.a4awalk.utils.validators.ValidateurRandonnee;

/**
 * Classe de tests unitaires pour {@link ValidateurRandonnee}.
 *
 * <p>Couvre l'ensemble des règles de validation de la méthode
 * {@link ValidateurRandonnee#verifierDonnees(String, String, String, String, String, int)} :</p>
 * <ul>
 *   <li><b>Cas nominaux</b> : données complètement valides, retour {@code null} attendu.</li>
 *   <li><b>Cas limites</b>  : valeurs aux bornes exactes des coordonnées, durée minimale,
 *       séparateur décimal virgule ou point.</li>
 *   <li><b>Cas d'erreur</b> : champs obligatoires absents, coordonnées hors bornes,
 *       valeurs non numériques, durée nulle ou négative.</li>
 * </ul>
 *
 * <p>L'annotation {@link RunWith} avec {@code AndroidJUnit4} est nécessaire car
 * {@link android.text.TextUtils} est une classe du SDK Android, disponible uniquement
 * via le runner d'instrumentation ou Robolectric.</p>
 *
 * <p>Dépendances requises dans {@code build.gradle} (module app) :</p>
 * <pre>
 *   androidTestImplementation 'androidx.test.ext:junit:1.2.+'
 *   androidTestImplementation 'androidx.test:runner:1.6.+'
 * </pre>
 *
 * @author Votre équipe
 * @version 1.0
 * @see ValidateurRandonnee
 */
@RunWith(AndroidJUnit4.class)
public class ValidateurRandonneeTest {

    // -------------------------------------------------------------------------
    // Constantes de test — données valides de référence
    // -------------------------------------------------------------------------

    /** Nom de randonnée valide utilisé dans les cas nominaux. */
    private static final String NOM_VALIDE          = "Tour du Mont-Blanc";

    /** Latitude de départ valide (Paris). */
    private static final String DEP_LAT_VALIDE      = "48.8566";

    /** Longitude de départ valide (Paris). */
    private static final String DEP_LON_VALIDE      = "2.3522";

    /** Latitude d'arrivée valide (Lyon). */
    private static final String ARR_LAT_VALIDE      = "45.7640";

    /** Longitude d'arrivée valide (Lyon). */
    private static final String ARR_LON_VALIDE      = "4.8357";

    /** Durée valide minimale en jours. */
    private static final int    DUREE_VALIDE        = 3;

    // =========================================================================
    // CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie qu'aucune erreur n'est retournée pour des données totalement valides.
     *
     * <p><b>Given</b> : toutes les données sont correctement renseignées
     *   (nom non vide, durée positive, coordonnées dans les bornes).<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null} (aucune erreur).</p>
     */
    @Test
    public void testVerifierDonnees_donneesValides_retourneNull() {
        // Given
        String nom      = NOM_VALIDE;
        String depLat   = DEP_LAT_VALIDE;
        String depLon   = DEP_LON_VALIDE;
        String arrLat   = ARR_LAT_VALIDE;
        String arrLon   = ARR_LON_VALIDE;
        int    duree    = DUREE_VALIDE;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(nom, depLat, depLon, arrLat, arrLon, duree);

        // Then
        assertNull("Aucune erreur ne doit être retournée pour des données valides", resultat);
    }

    /**
     * Vérifie que des coordonnées identiques pour départ et arrivée sont acceptées.
     *
     * <p><b>Given</b> : départ et arrivée ont les mêmes coordonnées (boucle).<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_departEtArriveeIdentiques_retourneNull() {
        // Given — randonnée en boucle : même point de départ et d'arrivée
        String nom    = "Boucle des Crêtes";
        int    duree  = 1;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                nom,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                duree);

        // Then
        assertNull("Une randonnée en boucle (coordonnées identiques) doit être acceptée", resultat);
    }

    /**
     * Vérifie que des coordonnées exprimées avec une virgule comme séparateur décimal
     * sont correctement interprétées.
     *
     * <p><b>Given</b> : les coordonnées utilisent {@code ","} au lieu de {@code "."}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null} (normalisation virgule → point).</p>
     */
    @Test
    public void testVerifierDonnees_coordonneesAvecVirgule_retourneNull() {
        // Given — séparateur décimal à la française
        String depLatVirgule = "48,8566";
        String depLonVirgule = "2,3522";
        String arrLatVirgule = "45,7640";
        String arrLonVirgule = "4,8357";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                depLatVirgule, depLonVirgule,
                arrLatVirgule, arrLonVirgule,
                DUREE_VALIDE);

        // Then
        assertNull("Les coordonnées avec virgule décimale doivent être acceptées", resultat);
    }

    // =========================================================================
    // CAS LIMITES — Durée
    // =========================================================================

    /**
     * Vérifie que la durée minimale autorisée (1 jour) est acceptée.
     *
     * <p><b>Given</b> : durée égale à {@code 1}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_dureeUnJour_retourneNull() {
        // Given
        int dureeMinimale = 1;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                dureeMinimale);

        // Then
        assertNull("La durée de 1 jour doit être acceptée", resultat);
    }

    // =========================================================================
    // CAS LIMITES — Coordonnées aux bornes exactes
    // =========================================================================

    /**
     * Vérifie que la latitude de départ à la borne inférieure exacte ({@code -90}) est acceptée.
     *
     * <p><b>Given</b> : latitude de départ = {@code "-90"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_borneInferieure_retourneNull() {
        // Given
        String latBorneMin = "-90";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latBorneMin, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNull("La latitude de départ à -90 (borne inférieure) doit être acceptée", resultat);
    }

    /**
     * Vérifie que la latitude de départ à la borne supérieure exacte ({@code 90}) est acceptée.
     *
     * <p><b>Given</b> : latitude de départ = {@code "90"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_borneSuperieure_retourneNull() {
        // Given
        String latBorneMax = "90";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latBorneMax, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNull("La latitude de départ à 90 (borne supérieure) doit être acceptée", resultat);
    }

    /**
     * Vérifie que la longitude de départ à la borne inférieure exacte ({@code -180}) est acceptée.
     *
     * <p><b>Given</b> : longitude de départ = {@code "-180"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeDepart_borneInferieure_retourneNull() {
        // Given
        String lonBorneMin = "-180";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, lonBorneMin,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNull("La longitude de départ à -180 (borne inférieure) doit être acceptée", resultat);
    }

    /**
     * Vérifie que la longitude de départ à la borne supérieure exacte ({@code 180}) est acceptée.
     *
     * <p><b>Given</b> : longitude de départ = {@code "180"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeDepart_borneSuperieure_retourneNull() {
        // Given
        String lonBorneMax = "180";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, lonBorneMax,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNull("La longitude de départ à 180 (borne supérieure) doit être acceptée", resultat);
    }

    /**
     * Vérifie que toutes les coordonnées aux bornes extrêmes simultanément sont acceptées.
     *
     * <p><b>Given</b> : latitude départ = {@code -90}, longitude départ = {@code -180},
     *   latitude arrivée = {@code 90}, longitude arrivée = {@code 180}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : la méthode retourne {@code null}.</p>
     */
    @Test
    public void testVerifierDonnees_toutesCoordonnees_auxBornesExtremes_retourneNull() {
        // Given — combinaison des bornes extrêmes de chaque coordonnée
        String depLat = "-90";
        String depLon = "-180";
        String arrLat = "90";
        String arrLon = "180";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE, depLat, depLon, arrLat, arrLon, DUREE_VALIDE);

        // Then
        assertNull("Les coordonnées aux bornes extrêmes combinées doivent être acceptées", resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Nom
    // =========================================================================

    /**
     * Vérifie qu'un nom vide déclenche le message d'erreur approprié.
     *
     * <p><b>Given</b> : nom = {@code ""}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné indique que le nom est obligatoire.</p>
     */
    @Test
    public void testVerifierDonnees_nomVide_retourneErreurNom() {
        // Given
        String nomVide = "";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                nomVide,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour un nom vide", resultat);
        assertEquals(
                "Le message d'erreur doit correspondre à l'absence de nom",
                "Le nom de la randonnée est obligatoire.",
                resultat);
    }

    /**
     * Vérifie qu'un nom {@code null} déclenche le message d'erreur approprié.
     *
     * <p><b>Given</b> : nom = {@code null}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné indique que le nom est obligatoire.</p>
     */
    @Test
    public void testVerifierDonnees_nomNull_retourneErreurNom() {
        // Given
        String nomNull = null;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                nomNull,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour un nom null", resultat);
        assertEquals(
                "Le message d'erreur doit correspondre à l'absence de nom",
                "Le nom de la randonnée est obligatoire.",
                resultat);
    }

    /**
     * Vérifie qu'un nom composé uniquement d'espaces déclenche le message d'erreur approprié.
     *
     * <p><b>Given</b> : nom = {@code "   "} (espaces).<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné indique que le nom est obligatoire
     *               ({@link android.text.TextUtils#isEmpty} traite les espaces comme non vides —
     *               ce test documente ce comportement actuel).</p>
     *
     * <p><em>Note :</em> {@code TextUtils.isEmpty("   ")} retourne {@code false} car la chaîne
     * n'est pas vide au sens strict. Ce test valide le comportement existant.</p>
     */
    @Test
    public void testVerifierDonnees_nomEspacesUniquement_comportementDocumente() {
        // Given
        String nomEspaces = "   ";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                nomEspaces,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then : TextUtils.isEmpty("   ") == false → le nom est accepté (comportement actuel)
        assertNull(
                "TextUtils.isEmpty ne rejette pas les espaces seuls — comportement documenté",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Durée
    // =========================================================================

    /**
     * Vérifie qu'une durée égale à zéro déclenche le message d'erreur approprié.
     *
     * <p><b>Given</b> : durée = {@code 0}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné indique qu'au moins 1 jour est requis.</p>
     */
    @Test
    public void testVerifierDonnees_dureeZero_retourneErreurDuree() {
        // Given
        int dureeZero = 0;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                dureeZero);

        // Then
        assertNotNull("Une erreur doit être retournée pour une durée de 0", resultat);
        assertEquals(
                "Le message d'erreur doit indiquer une durée minimale d'un jour",
                "La durée doit être d'au moins 1 jour.",
                resultat);
    }

    /**
     * Vérifie qu'une durée négative déclenche le message d'erreur approprié.
     *
     * <p><b>Given</b> : durée = {@code -5}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné indique qu'au moins 1 jour est requis.</p>
     */
    @Test
    public void testVerifierDonnees_dureeNegative_retourneErreurDuree() {
        // Given
        int dureeNegative = -5;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                dureeNegative);

        // Then
        assertNotNull("Une erreur doit être retournée pour une durée négative", resultat);
        assertEquals(
                "Le message d'erreur doit indiquer une durée minimale d'un jour",
                "La durée doit être d'au moins 1 jour.",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Latitude de départ
    // =========================================================================

    /**
     * Vérifie qu'une latitude de départ supérieure à 90 est rejetée.
     *
     * <p><b>Given</b> : latitude de départ = {@code "91"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_supA90_retourneErreur() {
        // Given
        String latHorsBornes = "91";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latHorsBornes, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude de départ > 90", resultat);
        assertEquals(
                "La latitude de départ invalide (doit être entre -90 et 90).",
                resultat);
    }

    /**
     * Vérifie qu'une latitude de départ inférieure à -90 est rejetée.
     *
     * <p><b>Given</b> : latitude de départ = {@code "-91"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_infA_moins90_retourneErreur() {
        // Given
        String latHorsBornes = "-91";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latHorsBornes, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude de départ < -90", resultat);
        assertEquals(
                "La latitude de départ invalide (doit être entre -90 et 90).",
                resultat);
    }

    /**
     * Vérifie qu'une latitude de départ non numérique est rejetée.
     *
     * <p><b>Given</b> : latitude de départ = {@code "abc"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_nonNumerique_retourneErreur() {
        // Given
        String latNonNumerique = "abc";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latNonNumerique, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude non numérique", resultat);
        assertEquals(
                "La latitude de départ invalide (doit être entre -90 et 90).",
                resultat);
    }

    /**
     * Vérifie qu'une latitude de départ vide est rejetée.
     *
     * <p><b>Given</b> : latitude de départ = {@code ""}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeDepart_vide_retourneErreur() {
        // Given
        String latVide = "";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latVide, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude de départ vide", resultat);
        assertEquals(
                "La latitude de départ invalide (doit être entre -90 et 90).",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Longitude de départ
    // =========================================================================

    /**
     * Vérifie qu'une longitude de départ supérieure à 180 est rejetée.
     *
     * <p><b>Given</b> : longitude de départ = {@code "181"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une longitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeDepart_supA180_retourneErreur() {
        // Given
        String lonHorsBornes = "181";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, lonHorsBornes,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une longitude de départ > 180", resultat);
        assertEquals(
                "La longitude de départ invalide (doit être entre -180 et 180).",
                resultat);
    }

    /**
     * Vérifie qu'une longitude de départ non numérique est rejetée.
     *
     * <p><b>Given</b> : longitude de départ = {@code "xyz"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une longitude de départ invalide.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeDepart_nonNumerique_retourneErreur() {
        // Given
        String lonNonNumerique = "xyz";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, lonNonNumerique,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une longitude non numérique", resultat);
        assertEquals(
                "La longitude de départ invalide (doit être entre -180 et 180).",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Latitude d'arrivée
    // =========================================================================

    /**
     * Vérifie qu'une latitude d'arrivée supérieure à 90 est rejetée.
     *
     * <p><b>Given</b> : latitude d'arrivée = {@code "91"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude d'arrivée invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeArrivee_supA90_retourneErreur() {
        // Given
        String latArrHorsBornes = "91";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                latArrHorsBornes, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude d'arrivée > 90", resultat);
        assertEquals(
                "La latitude d'arrivée invalide (doit être entre -90 et 90).",
                resultat);
    }

    /**
     * Vérifie qu'une latitude d'arrivée vide est rejetée.
     *
     * <p><b>Given</b> : latitude d'arrivée = {@code ""}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une latitude d'arrivée invalide.</p>
     */
    @Test
    public void testVerifierDonnees_latitudeArrivee_vide_retourneErreur() {
        // Given
        String latArrVide = "";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                latArrVide, ARR_LON_VALIDE,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une latitude d'arrivée vide", resultat);
        assertEquals(
                "La latitude d'arrivée invalide (doit être entre -90 et 90).",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Longitude d'arrivée
    // =========================================================================

    /**
     * Vérifie qu'une longitude d'arrivée inférieure à -180 est rejetée.
     *
     * <p><b>Given</b> : longitude d'arrivée = {@code "-181"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une longitude d'arrivée invalide.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeArrivee_infA_moins180_retourneErreur() {
        // Given
        String lonArrHorsBornes = "-181";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, lonArrHorsBornes,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une longitude d'arrivée < -180", resultat);
        assertEquals(
                "La longitude d'arrivée invalide (doit être entre -180 et 180).",
                resultat);
    }

    /**
     * Vérifie qu'une longitude d'arrivée non numérique est rejetée.
     *
     * <p><b>Given</b> : longitude d'arrivée = {@code "??"}.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné signale une longitude d'arrivée invalide.</p>
     */
    @Test
    public void testVerifierDonnees_longitudeArrivee_nonNumerique_retourneErreur() {
        // Given
        String lonArrNonNumerique = "??";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, lonArrNonNumerique,
                DUREE_VALIDE);

        // Then
        assertNotNull("Une erreur doit être retournée pour une longitude d'arrivée non numérique", resultat);
        assertEquals(
                "La longitude d'arrivée invalide (doit être entre -180 et 180).",
                resultat);
    }

    // =========================================================================
    // CAS D'ERREUR — Priorité des validations (ordre des contrôles)
    // =========================================================================

    /**
     * Vérifie que la validation du nom est effectuée en premier, avant celle de la durée.
     *
     * <p><b>Given</b> : nom vide ET durée invalide (0).<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné concerne le nom (premier contrôle).</p>
     */
    @Test
    public void testVerifierDonnees_nomVideEtDureeInvalide_retourneErreurNomEnPremier() {
        // Given — deux champs invalides simultanément
        String nomVide   = "";
        int    dureeZero = 0;

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                nomVide,
                DEP_LAT_VALIDE, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                dureeZero);

        // Then : l'erreur sur le nom doit être retournée en priorité
        assertNotNull(resultat);
        assertEquals(
                "La validation du nom doit précéder celle de la durée",
                "Le nom de la randonnée est obligatoire.",
                resultat);
    }

    /**
     * Vérifie que la validation de la durée est effectuée avant celle des coordonnées.
     *
     * <p><b>Given</b> : durée invalide (0) ET latitude de départ invalide.<br>
     * <b>When</b>  : {@code verifierDonnees} est appelé.<br>
     * <b>Then</b>  : le message retourné concerne la durée (deuxième contrôle).</p>
     */
    @Test
    public void testVerifierDonnees_dureeInvalideEtLatInvalide_retourneErreurDureeEnPremier() {
        // Given — durée et coordonnée toutes deux invalides
        int    dureeZero        = 0;
        String latHorsBornes    = "999";

        // When
        String resultat = ValidateurRandonnee.verifierDonnees(
                NOM_VALIDE,
                latHorsBornes, DEP_LON_VALIDE,
                ARR_LAT_VALIDE, ARR_LON_VALIDE,
                dureeZero);

        // Then : l'erreur sur la durée doit être retournée avant les coordonnées
        assertNotNull(resultat);
        assertEquals(
                "La validation de la durée doit précéder celle des coordonnées",
                "La durée doit être d'au moins 1 jour.",
                resultat);
    }
}
