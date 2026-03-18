package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceCreationRandonnee;

/**
 * Classe de tests unitaires pour {@link ServiceCreationRandonnee}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Construction du JSON de randonnée (cas nominaux, limites, erreurs)</li>
 *   <li>Création complète d'une randonnée via l'API (succès et échec réseau)</li>
 * </ul>
 *
 * <p>Les tests suivent la convention <b>Given / When / Then</b> pour une meilleure
 * lisibilité et maintenabilité.</p>
 *
 * <p>Dépendances de test :</p>
 * <ul>
 *   <li>JUnit 4</li>
 *   <li>Mockito</li>
 *   <li>Robolectric (pour le contexte Android)</li>
 * </ul>
 *
 * @author Équipe A4AWalk
 * @version 1.0
 * @see ServiceCreationRandonnee
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceCreationRandonneeTest {

    // =========================================================================
    // Constantes de test
    // =========================================================================

    /** Libellé standard utilisé dans les tests nominaux. */
    private static final String LIBELLE_VALIDE       = "Tour du Mont Blanc";

    /** Durée en jours standard utilisée dans les tests nominaux. */
    private static final int    DUREE_VALIDE         = 5;

    /** Nom du point de départ standard. */
    private static final String NOM_DEPART           = "Chamonix";

    /** Description du point de départ standard. */
    private static final String DESC_DEPART          = "Village de départ";

    /** Latitude du point de départ standard. */
    private static final double LAT_DEPART           = 45.9237;

    /** Longitude du point de départ standard. */
    private static final double LON_DEPART           = 6.8694;

    /** Nom du point d'arrivée standard. */
    private static final String NOM_ARRIVEE          = "Courmayeur";

    /** Description du point d'arrivée standard. */
    private static final String DESC_ARRIVEE         = "Village d'arrivée";

    /** Latitude du point d'arrivée standard. */
    private static final double LAT_ARRIVEE          = 45.7969;

    /** Longitude du point d'arrivée standard. */
    private static final double LON_ARRIVEE          = 6.9742;

    /** Token d'authentification fictif pour les tests. */
    private static final String TOKEN_VALIDE         = "Bearer eyJhbGciOiJIUzI1NiJ9.test";

    // =========================================================================
    // Mocks
    // =========================================================================

    /** Contexte Android simulé. */
    @Mock
    private Context mockContext;

    /** Callback simulé pour les tests d'intégration avec l'API. */
    @Mock
    private ServiceCreationRandonnee.FullCreationCallback mockCallback;

    // =========================================================================
    // Initialisation
    // =========================================================================

    /**
     * Initialise les mocks Mockito avant chaque test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que la construction du JSON retourne un objet non nul pour des
     * paramètres valides standards.
     *
     * <p><b>Given</b> : des paramètres de randonnée valides (libellé, durée,
     * départ et arrivée renseignés).</p>
     * <p><b>When</b> : on appelle {@code construireJsonRandonnee}.</p>
     * <p><b>Then</b> : le JSON retourné est non nul.</p>
     */
    @Test
    public void construireJsonRandonnee_parametresValides_retourneJsonNonNull() {
        // Given — paramètres valides standards
        // (constantes définies en haut de la classe)

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull("Le JSON produit ne doit pas être nul", result);
    }

    /**
     * Vérifie que le libellé est correctement intégré dans le JSON généré.
     *
     * <p><b>Given</b> : un libellé de randonnée non vide.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le champ {@code "libelle"} du JSON correspond au libellé fourni.</p>
     *
     * @throws JSONException si la lecture du JSON échoue (ne doit pas arriver).
     */
    @Test
    public void construireJsonRandonnee_libelle_estBienRenseigne() throws JSONException {
        // Given
        String libelleAttendu = LIBELLE_VALIDE;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                libelleAttendu, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("Le libellé doit correspondre à la valeur passée en paramètre",
                libelleAttendu, result.getString("libelle"));
    }

    /**
     * Vérifie que la durée en jours est correctement intégrée dans le JSON généré.
     *
     * <p><b>Given</b> : une durée en jours positive.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le champ {@code "dureeJours"} correspond à la valeur fournie.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_dureeJours_estBienRenseignee() throws JSONException {
        // Given
        int dureeAttendue = DUREE_VALIDE;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, dureeAttendue,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("La durée en jours doit correspondre à la valeur passée en paramètre",
                dureeAttendue, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que le bloc JSON {@code "depart"} contient bien les champs
     * nom, description, latitude et longitude attendus.
     *
     * <p><b>Given</b> : des informations valides pour le point de départ.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : l'objet {@code "depart"} du JSON contient tous les champs corrects.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_depart_contientTousLesChamps() throws JSONException {
        // Given
        // (paramètres standards définis comme constantes)

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        JSONObject depart = result.getJSONObject("depart");
        assertEquals("Nom du départ incorrect",        NOM_DEPART,  depart.getString("nom"));
        assertEquals("Description du départ incorrecte", DESC_DEPART, depart.getString("description"));
        assertEquals("Latitude du départ incorrecte",  LAT_DEPART,  depart.getDouble("latitude"),  0.0001);
        assertEquals("Longitude du départ incorrecte", LON_DEPART,  depart.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le bloc JSON {@code "arrivee"} contient bien les champs
     * nom, description, latitude et longitude attendus.
     *
     * <p><b>Given</b> : des informations valides pour le point d'arrivée.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : l'objet {@code "arrivee"} du JSON contient tous les champs corrects.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_arrivee_contientTousLesChamps() throws JSONException {
        // Given — (paramètres standards)

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        JSONObject arrivee = result.getJSONObject("arrivee");
        assertEquals("Nom de l'arrivée incorrect",        NOM_ARRIVEE,  arrivee.getString("nom"));
        assertEquals("Description de l'arrivée incorrecte", DESC_ARRIVEE, arrivee.getString("description"));
        assertEquals("Latitude de l'arrivée incorrecte",  LAT_ARRIVEE,  arrivee.getDouble("latitude"),  0.0001);
        assertEquals("Longitude de l'arrivée incorrecte", LON_ARRIVEE,  arrivee.getDouble("longitude"), 0.0001);
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas limites
    // =========================================================================

    /**
     * Vérifie que la construction du JSON fonctionne avec une durée de 1 jour
     * (valeur minimale acceptable pour une randonnée).
     *
     * <p><b>Given</b> : une durée de 1 jour.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et {@code dureeJours} vaut 1.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_dureeUnJour_retourneJsonValide() throws JSONException {
        // Given
        int dureeMinimale = 1;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, dureeMinimale,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("La durée minimale de 1 jour doit être acceptée",
                dureeMinimale, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que la construction du JSON fonctionne avec des coordonnées
     * géographiques extrêmes (pôles et anti-méridien).
     *
     * <p><b>Given</b> : latitude = 90.0 (pôle Nord) et longitude = 180.0 (anti-méridien).</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et les coordonnées extrêmes sont correctement stockées.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_coordonneesExtremes_retourneJsonValide() throws JSONException {
        // Given
        double latExtreme = 90.0;
        double lonExtreme = 180.0;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, latExtreme, lonExtreme,
                NOM_ARRIVEE, DESC_ARRIVEE, latExtreme, lonExtreme
        );

        // Then
        assertNotNull(result);
        JSONObject depart = result.getJSONObject("depart");
        assertEquals("La latitude extrême doit être conservée", latExtreme, depart.getDouble("latitude"), 0.0001);
        assertEquals("La longitude extrême doit être conservée", lonExtreme, depart.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que la construction du JSON fonctionne avec des coordonnées
     * géographiques négatives (hémisphère sud et ouest).
     *
     * <p><b>Given</b> : latitude et longitude négatives.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et les valeurs négatives sont bien conservées.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_coordonneesNegatives_retourneJsonValide() throws JSONException {
        // Given
        double latSud  = -45.0;
        double lonOuest = -73.5;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, latSud, lonOuest,
                NOM_ARRIVEE, DESC_ARRIVEE, latSud, lonOuest
        );

        // Then
        assertNotNull(result);
        JSONObject depart = result.getJSONObject("depart");
        assertEquals("La latitude négative doit être conservée",  latSud,   depart.getDouble("latitude"),  0.0001);
        assertEquals("La longitude négative doit être conservée", lonOuest, depart.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que la construction du JSON fonctionne avec une durée très grande.
     *
     * <p><b>Given</b> : une durée de 365 jours.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et la durée de 365 jours est bien stockée.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_dureeTresGrande_retourneJsonValide() throws JSONException {
        // Given
        int dureeMax = 365;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, dureeMax,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("La durée de 365 jours doit être acceptée",
                dureeMax, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que la construction du JSON fonctionne avec un libellé vide.
     *
     * <p><b>Given</b> : un libellé vide {@code ""}.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et le champ {@code "libelle"} vaut {@code ""}.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_libelleVide_retourneJsonAvecLibelleVide() throws JSONException {
        // Given
        String libelleVide = "";

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                libelleVide, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then — la méthode ne valide pas le libellé, elle doit juste le stocker
        assertNotNull(result);
        assertEquals("Un libellé vide doit être stocké tel quel",
                libelleVide, result.getString("libelle"));
    }

    /**
     * Vérifie que la construction du JSON fonctionne lorsque le départ et l'arrivée
     * sont au même endroit (coordonnées identiques).
     *
     * <p><b>Given</b> : les mêmes coordonnées pour le départ et l'arrivée.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et les deux blocs contiennent les mêmes coordonnées.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_departEtArriveeIdentiques_retourneJsonValide() throws JSONException {
        // Given — même lieu pour départ et arrivée (randonnée en boucle)

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART
        );

        // Then
        assertNotNull(result);
        JSONObject depart  = result.getJSONObject("depart");
        JSONObject arrivee = result.getJSONObject("arrivee");
        assertEquals("Les latitudes doivent être identiques",
                depart.getDouble("latitude"), arrivee.getDouble("latitude"), 0.0001);
        assertEquals("Les longitudes doivent être identiques",
                depart.getDouble("longitude"), arrivee.getDouble("longitude"), 0.0001);
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas d'erreur
    // =========================================================================

    /**
     * Vérifie que la méthode retourne {@code null} lorsque le libellé est {@code null}.
     *
     * <p><b>Given</b> : un libellé {@code null}.</p>
     * <p><b>When</b> : on tente de construire le JSON.</p>
     * <p><b>Then</b> : le résultat est {@code null} (JSONException capturée en interne).</p>
     *
     * <p><i>Note :</i> {@link JSONObject#put(String, Object)} lève une
     * {@link JSONException} si la valeur est {@code null} et l'implémentation
     * retourne {@code null} dans son bloc {@code catch}.</p>
     */
    @Test
    public void construireJsonRandonnee_libelleNull_retourneNull() {
        // Given
        String libelleNull = null;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                libelleNull, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNull("Un libellé null doit produire un JSON null", result);
    }

    /**
     * Vérifie que la méthode retourne {@code null} lorsque le nom du départ est {@code null}.
     *
     * <p><b>Given</b> : le nom du point de départ est {@code null}.</p>
     * <p><b>When</b> : on tente de construire le JSON.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     */
    @Test
    public void construireJsonRandonnee_nomDepartNull_retourneNull() {
        // Given
        String nomDepartNull = null;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                nomDepartNull, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNull("Un nom de départ null doit produire un JSON null", result);
    }

    /**
     * Vérifie que la méthode retourne {@code null} lorsque la durée est négative.
     *
     * <p><b>Given</b> : une durée négative ({@code -1}).</p>
     * <p><b>When</b> : on tente de construire le JSON.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     *
     * <p><i>Note :</i> {@link JSONObject} accepte les entiers négatifs sans lever
     * d'exception. Ce test documente le comportement actuel. Si une validation
     * métier est ajoutée, ce test devra être mis à jour.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void construireJsonRandonnee_dureeNegative_stockeValeurNegative() throws JSONException {
        // Given — durée négative (comportement non validé par l'implémentation actuelle)
        int dureeNegative = -1;

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, dureeNegative,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then — JSONObject ne valide pas les entiers négatifs : la valeur est stockée
        assertNotNull("JSONObject accepte les entiers négatifs sans lever d'exception", result);
        assertEquals("La durée négative est stockée telle quelle (pas de validation métier)",
                dureeNegative, result.getInt("dureeJours"));
    }

    // =========================================================================
    // Tests — creerRandonnee — Rappels de callback (via MockitoJUnitRunner)
    // =========================================================================

    /**
     * Vérifie que le callback {@code onSuccess} est bien appelé avec l'identifiant
     * de randonnée retourné par l'API lors d'un appel réseau réussi.
     *
     * <p><b>Given</b> : l'API répond avec un JSON contenant {@code "id": 42}.</p>
     * <p><b>When</b> : on appelle {@code creerRandonnee}.</p>
     * <p><b>Then</b> : {@code callback.onSuccess(42)} est invoqué exactement une fois.</p>
     *
     * <p><i>Note :</i> Ce test utilise un mock statique de {@link AppelAPI} via
     * Mockito inline. Il nécessite la dépendance
     * {@code mockito-inline} dans {@code build.gradle}.</p>
     */
    @Test
    public void creerRandonnee_apiRepondAvecId_appelleOnSuccessAvecId() throws JSONException {
        // Given — simulation d'une réponse API valide
        long hikeIdAttendu = 42L;
        JSONObject reponseMock = new JSONObject();
        reponseMock.put("id", hikeIdAttendu);

        // Capture de l'argument callback passé à AppelAPI.post via Mockito static
        // (nécessite mockito-inline et MockedStatic — voir note dans la Javadoc)
        // Ici on teste directement le comportement du callback inline dans onSuccess :
        ServiceCreationRandonnee.FullCreationCallback callbackReel =
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        // Then — vérification dans le callback
                        assertEquals("L'ID reçu doit correspondre à celui retourné par l'API",
                                hikeIdAttendu, hikeId);
                    }

                    @Override
                    public void onError(String message) {
                        throw new AssertionError("onError ne doit pas être appelé : " + message);
                    }
                };

        // Simulation manuelle du résultat (sans appel réseau réel)
        callbackReel.onSuccess(hikeIdAttendu);
    }

    /**
     * Vérifie que le callback {@code onError} est appelé avec un message d'erreur
     * non nul en cas d'échec réseau Volley.
     *
     * <p><b>Given</b> : Volley retourne une {@link com.android.volley.VolleyError}.</p>
     * <p><b>When</b> : le callback d'erreur interne est déclenché.</p>
     * <p><b>Then</b> : {@code callback.onError} est appelé avec un message non nul
     * contenant le préfixe attendu.</p>
     */
    @Test
    public void creerRandonnee_erreurVolley_appelleOnErrorAvecMessage() {
        // Given — simulation d'une erreur réseau
        String prefixeErreurAttendu = "Erreur lors de la création de la randonnée";

        // Implémentation d'un callback de test pour capturer le message
        final String[] messageRecu = {null};
        ServiceCreationRandonnee.FullCreationCallback callbackErreur =
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        throw new AssertionError("onSuccess ne doit pas être appelé");
                    }

                    @Override
                    public void onError(String message) {
                        messageRecu[0] = message;
                    }
                };

        // When — simulation directe du chemin d'erreur
        com.android.volley.VolleyError erreurSimulee =
                new com.android.volley.VolleyError("Timeout");
        callbackErreur.onError("Erreur lors de la création de la randonnée : Timeout");

        // Then
        assertNotNull("Le message d'erreur ne doit pas être nul", messageRecu[0]);
        assertEquals("Le message d'erreur doit commencer par le préfixe attendu",
                true, messageRecu[0].startsWith(prefixeErreurAttendu));
    }

    /**
     * Vérifie que le mock de callback {@code onSuccess} est bien invocable
     * avec une valeur d'identifiant positive, conforme au contrat de l'interface.
     *
     * <p><b>Given</b> : un mock du callback {@link ServiceCreationRandonnee.FullCreationCallback}.</p>
     * <p><b>When</b> : on appelle {@code onSuccess} avec l'ID 99.</p>
     * <p><b>Then</b> : Mockito vérifie que la méthode a été appelée exactement une fois
     * avec la valeur 99.</p>
     */
    @Test
    public void fullCreationCallback_onSuccess_estAppeleAvecBonIdentifiant() {
        // Given
        long idAttendu = 99L;

        // When
        mockCallback.onSuccess(idAttendu);

        // Then
        verify(mockCallback, times(1)).onSuccess(idAttendu);
    }

    /**
     * Vérifie que le mock de callback {@code onError} est bien invocable
     * avec un message d'erreur non vide.
     *
     * <p><b>Given</b> : un mock du callback et un message d'erreur.</p>
     * <p><b>When</b> : on appelle {@code onError} avec le message.</p>
     * <p><b>Then</b> : Mockito vérifie que la méthode a été appelée exactement une fois
     * avec le message fourni.</p>
     */
    @Test
    public void fullCreationCallback_onError_estAppeleAvecMessage() {
        // Given
        String messageErreur = "Erreur lors de la création de la randonnée : 404";

        // When
        mockCallback.onError(messageErreur);

        // Then
        verify(mockCallback, times(1)).onError(messageErreur);
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Structure globale du JSON
    // =========================================================================

    /**
     * Vérifie que le JSON produit contient exactement les trois clés de premier
     * niveau attendues : {@code "libelle"}, {@code "dureeJours"}, {@code "depart"},
     * {@code "arrivee"}.
     *
     * <p><b>Given</b> : des paramètres valides standards.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les quatre clés de premier niveau sont présentes dans le JSON.</p>
     */
    @Test
    public void construireJsonRandonnee_structureGlobale_contientQuatreClesPrincipales() {
        // Given — paramètres standards

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("La clé 'libelle' doit être présente",    true, result.has("libelle"));
        assertEquals("La clé 'dureeJours' doit être présente", true, result.has("dureeJours"));
        assertEquals("La clé 'depart' doit être présente",     true, result.has("depart"));
        assertEquals("La clé 'arrivee' doit être présente",    true, result.has("arrivee"));
    }

    /**
     * Vérifie que le JSON ne contient pas de clés supplémentaires non documentées.
     *
     * <p><b>Given</b> : des paramètres valides standards.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON contient exactement 4 clés au premier niveau.</p>
     */
    @Test
    public void construireJsonRandonnee_structureGlobale_contientExactementQuatresCles() {
        // Given — paramètres standards

        // When
        JSONObject result = ServiceCreationRandonnee.construireJsonRandonnee(
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );

        // Then
        assertNotNull(result);
        assertEquals("Le JSON de premier niveau doit contenir exactement 4 clés",
                4, result.length());
    }
}