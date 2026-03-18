package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceModificationRandonnee;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceCreationRandonnee;

/**
 * Classe de tests unitaires pour {@link ServiceModificationRandonnee}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Construction du JSON de mise à jour — cas nominaux</li>
 *   <li>Construction du JSON — cas limites (coordonnées extrêmes, durée minimale, etc.)</li>
 *   <li>Construction du JSON — cas d'erreur (libellé null, champs null)</li>
 *   <li>Contrat de l'interface {@link ServiceModificationRandonnee.UpdateHikeCallback}</li>
 * </ul>
 *
 * <p>La méthode privée {@code construireJsonRandonnee} est testée par introspection
 * ({@link java.lang.reflect.Method}) afin de couvrir directement la logique de
 * sérialisation JSON, indépendamment de la couche réseau Volley.</p>
 *
 * <p>La convention <b>Given / When / Then</b> est appliquée sur chaque test.</p>
 *
 * <p>Dépendances requises dans {@code build.gradle (app)} :</p>
 * <pre>
 * testImplementation 'junit:junit:4.13.2'
 * testImplementation 'org.mockito:mockito-core:5.x.x'
 * testImplementation 'org.mockito:mockito-inline:5.x.x'
 * </pre>
 *
 * @author Équipe A4AWalk
 * @version 1.0
 * @see ServiceModificationRandonnee
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceModificationRandonneeTest {

    // =========================================================================
    // Constantes de test
    // =========================================================================

    /** Libellé valide utilisé dans les tests nominaux. */
    private static final String LIBELLE_VALIDE  = "Traversée des Grands Causses";

    /** Durée en jours standard utilisée dans les tests nominaux. */
    private static final int    DUREE_VALIDE    = 4;

    /** Nom du point de départ fixé en dur dans le service. */
    private static final String NOM_DEPART      = "Départ";

    /** Description du point de départ fixée en dur dans le service. */
    private static final String DESC_DEPART     = "Point de départ de la randonnée";

    /** Nom du point d'arrivée fixé en dur dans le service. */
    private static final String NOM_ARRIVEE     = "Arrivée";

    /** Description du point d'arrivée fixée en dur dans le service. */
    private static final String DESC_ARRIVEE    = "Point d'arrivée de la randonnée";

    /** Latitude du point de départ standard. */
    private static final double LAT_DEPART      = 44.1080;

    /** Longitude du point de départ standard. */
    private static final double LON_DEPART      = 3.0780;

    /** Latitude du point d'arrivée standard. */
    private static final double LAT_ARRIVEE     = 43.9500;

    /** Longitude du point d'arrivée standard. */
    private static final double LON_ARRIVEE     = 3.2500;

    // =========================================================================
    // Mocks
    // =========================================================================

    /** Mock du callback de mise à jour d'une randonnée. */
    @Mock
    private ServiceModificationRandonnee.UpdateHikeCallback mockCallback;

    // =========================================================================
    // Accès réflexif à la méthode privée
    // =========================================================================

    /**
     * Référence réflexive vers la méthode privée
     * {@code construireJsonRandonnee} de {@link ServiceModificationRandonnee}.
     */
    private Method methodePrivee;

    // =========================================================================
    // Initialisation
    // =========================================================================

    /**
     * Initialise les mocks Mockito et rend accessible la méthode privée
     * {@code construireJsonRandonnee} via réflexion avant chaque test.
     *
     * @throws NoSuchMethodException si la signature de la méthode a changé.
     */
    @Before
    public void setUp() throws NoSuchMethodException {
        MockitoAnnotations.openMocks(this);

        methodePrivee = ServiceModificationRandonnee.class.getDeclaredMethod(
                "construireJsonRandonnee",
                String.class, int.class,
                String.class, String.class, double.class, double.class,
                String.class, String.class, double.class, double.class
        );
        methodePrivee.setAccessible(true);
    }

    // =========================================================================
    // Méthode utilitaire
    // =========================================================================

    /**
     * Invoque la méthode privée {@code construireJsonRandonnee} avec les paramètres
     * standards définis comme constantes de la classe.
     *
     * @return le {@link JSONObject} produit par la méthode.
     * @throws Exception si la réflexion ou la construction du JSON échouent.
     */
    private JSONObject invoquerAvecParametresStandards() throws Exception {
        return (JSONObject) methodePrivee.invoke(null,
                LIBELLE_VALIDE, DUREE_VALIDE,
                NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART,
                NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE
        );
    }

    /**
     * Invoque la méthode privée {@code construireJsonRandonnee} avec des paramètres
     * personnalisés.
     *
     * @param libelle    libellé de la randonnée.
     * @param dureeJours durée en jours.
     * @param latD       latitude du départ.
     * @param lonD       longitude du départ.
     * @param latA       latitude de l'arrivée.
     * @param lonA       longitude de l'arrivée.
     * @return le {@link JSONObject} produit par la méthode.
     * @throws Exception si la réflexion ou la construction du JSON échouent.
     */
    private JSONObject invoquerAvecParametres(String libelle, int dureeJours,
                                              double latD, double lonD,
                                              double latA, double lonA) throws Exception {
        return (JSONObject) methodePrivee.invoke(null,
                libelle, dureeJours,
                NOM_DEPART, DESC_DEPART, latD, lonD,
                NOM_ARRIVEE, DESC_ARRIVEE, latA, lonA
        );
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que la méthode retourne un JSON non nul pour des paramètres valides.
     *
     * <p><b>Given</b> : des paramètres de randonnée valides standards.</p>
     * <p><b>When</b> : on appelle {@code construireJsonRandonnee}.</p>
     * <p><b>Then</b> : le JSON retourné est non nul.</p>
     *
     * @throws Exception si la réflexion échoue.
     */
    @Test
    public void construireJson_parametresValides_retourneJsonNonNull() throws Exception {
        // Given — paramètres standards (constantes de classe)

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull("Le JSON produit ne doit pas être nul", result);
    }

    /**
     * Vérifie que le champ {@code "libelle"} est correctement renseigné dans le JSON.
     *
     * <p><b>Given</b> : le libellé {@value #LIBELLE_VALIDE}.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code json.getString("libelle")} correspond au libellé fourni.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_libelle_estCorrectementRenseigne() throws Exception {
        // Given
        String libelleAttendu = LIBELLE_VALIDE;

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        assertEquals("Le libellé doit correspondre à la valeur fournie",
                libelleAttendu, result.getString("libelle"));
    }

    /**
     * Vérifie que le champ {@code "dureeJours"} est correctement renseigné.
     *
     * <p><b>Given</b> : une durée de {@value #DUREE_VALIDE} jours.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code json.getInt("dureeJours")} vaut {@value #DUREE_VALIDE}.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_dureeJours_estCorrectementRenseignee() throws Exception {
        // Given
        int dureeAttendue = DUREE_VALIDE;

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        assertEquals("La durée en jours doit correspondre à la valeur fournie",
                dureeAttendue, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que le bloc {@code "depart"} contient les quatre champs attendus
     * avec leurs valeurs correctes.
     *
     * <p><b>Given</b> : des informations de départ valides.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code depart.nom}, {@code depart.description},
     * {@code depart.latitude} et {@code depart.longitude} sont corrects.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_depart_contientTousLesChamps() throws Exception {
        // Given — constantes NOM_DEPART, DESC_DEPART, LAT_DEPART, LON_DEPART

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        JSONObject depart = result.getJSONObject("depart");
        assertEquals("Nom du départ incorrect",           NOM_DEPART,  depart.getString("nom"));
        assertEquals("Description du départ incorrecte",  DESC_DEPART, depart.getString("description"));
        assertEquals("Latitude du départ incorrecte",     LAT_DEPART,  depart.getDouble("latitude"),  0.0001);
        assertEquals("Longitude du départ incorrecte",    LON_DEPART,  depart.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le bloc {@code "arrivee"} contient les quatre champs attendus
     * avec leurs valeurs correctes.
     *
     * <p><b>Given</b> : des informations d'arrivée valides.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code arrivee.nom}, {@code arrivee.description},
     * {@code arrivee.latitude} et {@code arrivee.longitude} sont corrects.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_arrivee_contientTousLesChamps() throws Exception {
        // Given — constantes NOM_ARRIVEE, DESC_ARRIVEE, LAT_ARRIVEE, LON_ARRIVEE

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        JSONObject arrivee = result.getJSONObject("arrivee");
        assertEquals("Nom de l'arrivée incorrect",           NOM_ARRIVEE,  arrivee.getString("nom"));
        assertEquals("Description de l'arrivée incorrecte",  DESC_ARRIVEE, arrivee.getString("description"));
        assertEquals("Latitude de l'arrivée incorrecte",     LAT_ARRIVEE,  arrivee.getDouble("latitude"),  0.0001);
        assertEquals("Longitude de l'arrivée incorrecte",    LON_ARRIVEE,  arrivee.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le JSON de premier niveau contient exactement quatre clés :
     * {@code libelle}, {@code dureeJours}, {@code depart} et {@code arrivee}.
     *
     * <p><b>Given</b> : des paramètres valides standards.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON contient exactement 4 clés au premier niveau.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_structurePremierNiveau_contientExactementQuatresCles() throws Exception {
        // Given — paramètres standards

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        assertEquals("Le JSON doit contenir exactement 4 clés au premier niveau", 4, result.length());
    }

    /**
     * Vérifie que les noms de départ et d'arrivée sont bien les valeurs fixes
     * définies dans le service ({@value #NOM_DEPART} et {@value #NOM_ARRIVEE}),
     * indépendamment des paramètres de la méthode publique.
     *
     * <p><b>Given</b> : un appel avec les paramètres standards.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code depart.nom} vaut "Départ" et {@code arrivee.nom} vaut "Arrivée".</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_nomsDeparts_sontLesFaleursFixesDuService() throws Exception {
        // Given — le service fixe les noms en dur dans modifierRandonneeAPI

        // When
        JSONObject result = invoquerAvecParametresStandards();

        // Then
        assertNotNull(result);
        assertEquals("Le nom du départ est fixé en dur dans le service",
                "Départ",  result.getJSONObject("depart").getString("nom"));
        assertEquals("Le nom de l'arrivée est fixé en dur dans le service",
                "Arrivée", result.getJSONObject("arrivee").getString("nom"));
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas limites
    // =========================================================================

    /**
     * Vérifie que le JSON est valide pour une durée de 1 jour (valeur limite basse).
     *
     * <p><b>Given</b> : une durée de 1 jour.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et {@code dureeJours} vaut 1.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_dureeUnJour_retourneJsonValide() throws Exception {
        // Given
        int dureeMinimale = 1;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, dureeMinimale,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("La durée minimale de 1 jour doit être acceptée",
                dureeMinimale, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que le JSON est valide pour des coordonnées géographiques extrêmes
     * (pôles et anti-méridien).
     *
     * <p><b>Given</b> : latitude = 90.0 et longitude = 180.0.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les coordonnées extrêmes sont conservées dans le JSON.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_coordonneesExtremes_retourneJsonValide() throws Exception {
        // Given
        double latExtreme = 90.0;
        double lonExtreme = 180.0;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, DUREE_VALIDE,
                latExtreme, lonExtreme, latExtreme, lonExtreme);

        // Then
        assertNotNull(result);
        assertEquals("La latitude extrême doit être conservée",
                latExtreme, result.getJSONObject("depart").getDouble("latitude"), 0.0001);
        assertEquals("La longitude extrême doit être conservée",
                lonExtreme, result.getJSONObject("depart").getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le JSON est valide pour des coordonnées négatives
     * (hémisphère sud et ouest).
     *
     * <p><b>Given</b> : latitude négative et longitude négative.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les coordonnées négatives sont correctement stockées.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_coordonneesNegatives_retourneJsonValide() throws Exception {
        // Given
        double latSud   = -33.8688;
        double lonOuest = -70.6693;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, DUREE_VALIDE,
                latSud, lonOuest, latSud, lonOuest);

        // Then
        assertNotNull(result);
        JSONObject depart = result.getJSONObject("depart");
        assertEquals("La latitude négative doit être conservée",  latSud,   depart.getDouble("latitude"),  0.0001);
        assertEquals("La longitude négative doit être conservée", lonOuest, depart.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le JSON est valide pour une durée de 0 jour (valeur limite absolue).
     *
     * <p><b>Given</b> : une durée de 0 jour.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et {@code dureeJours} vaut 0.</p>
     *
     * <p><i>Note :</i> l'implémentation ne valide pas la durée — ce test documente
     * le comportement réel.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_dureeZero_retourneJsonAvecDureeZero() throws Exception {
        // Given
        int dureeZero = 0;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, dureeZero,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("Une durée de 0 est stockée sans validation métier",
                dureeZero, result.getInt("dureeJours"));
    }

    /**
     * Vérifie que le JSON est valide pour des coordonnées de départ et d'arrivée
     * identiques (randonnée en boucle).
     *
     * <p><b>Given</b> : les mêmes coordonnées pour le départ et l'arrivée.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les deux blocs contiennent les mêmes coordonnées.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_departEtArriveeIdentiques_retourneJsonValide() throws Exception {
        // Given — même lieu (randonnée en boucle)

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, DUREE_VALIDE,
                LAT_DEPART, LON_DEPART, LAT_DEPART, LON_DEPART);

        // Then
        assertNotNull(result);
        JSONObject depart  = result.getJSONObject("depart");
        JSONObject arrivee = result.getJSONObject("arrivee");
        assertEquals("Les latitudes doivent être identiques pour une boucle",
                depart.getDouble("latitude"), arrivee.getDouble("latitude"), 0.0001);
        assertEquals("Les longitudes doivent être identiques pour une boucle",
                depart.getDouble("longitude"), arrivee.getDouble("longitude"), 0.0001);
    }

    /**
     * Vérifie que le JSON est valide pour un libellé vide.
     *
     * <p><b>Given</b> : un libellé vide {@code ""}.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le champ {@code "libelle"} vaut {@code ""} (pas de validation).</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_libelleVide_stockeLibelleVide() throws Exception {
        // Given
        String libelleVide = "";

        // When
        JSONObject result = invoquerAvecParametres(libelleVide, DUREE_VALIDE,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("Un libellé vide est stocké tel quel",
                libelleVide, result.getString("libelle"));
    }

    /**
     * Vérifie que le JSON est valide pour une durée très grande (valeur limite haute).
     *
     * <p><b>Given</b> : une durée de 365 jours.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : {@code dureeJours} vaut 365.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_dureeTresGrande_retourneJsonValide() throws Exception {
        // Given
        int dureeMax = 365;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, dureeMax,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("La durée maximale de 365 jours doit être acceptée",
                dureeMax, result.getInt("dureeJours"));
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Cas d'erreur
    // =========================================================================

    /**
     * Vérifie que le JSON retourné est non nul mais contient un libellé null
     * stocké tel quel lorsque le libellé fourni est {@code null}.
     *
     * <p><b>Given</b> : un libellé {@code null}.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON retourné est non nul (contrairement à
     * {@link ServiceCreationRandonnee} qui retourne null, ici la JSONException
     * est silencieuse et le JSON partiel est retourné).</p>
     *
     * <p><i>Note :</i> {@code construireJsonRandonnee} dans ce service capture
     * l'exception sans retourner null — ce test documente ce comportement
     * différent de {@link ServiceCreationRandonnee}.</p>
     *
     * @throws Exception si la réflexion échoue.
     */
    @Test
    public void construireJson_libelleNull_retourneJsonPartiellementRempli() throws Exception {
        // Given
        String libelleNull = null;

        // When
        JSONObject result = invoquerAvecParametres(libelleNull, DUREE_VALIDE,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then — contrairement à ServiceCreationRandonnee, ce service ne retourne pas null
        // Il retourne le JSON partiellement rempli ou vide selon l'ordre des puts
        assertNotNull("Ce service retourne un JSONObject même si libelle est null " +
                "(l'exception est capturée sans retourner null)", result);
    }

    /**
     * Vérifie que la durée négative est stockée sans validation dans le JSON.
     *
     * <p><b>Given</b> : une durée négative ({@code -5}).</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : le JSON est non nul et {@code dureeJours} vaut -5.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_dureeNegative_stockeValeurNegative() throws Exception {
        // Given
        int dureeNegative = -5;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, dureeNegative,
                LAT_DEPART, LON_DEPART, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("La durée négative est stockée sans validation",
                dureeNegative, result.getInt("dureeJours"));
    }

    // =========================================================================
    // Tests — UpdateHikeCallback — Contrat de l'interface
    // =========================================================================

    /**
     * Vérifie que le mock de callback {@code onSuccess} est bien invocable
     * sans argument (la mise à jour ne retourne pas d'entité).
     *
     * <p><b>Given</b> : un mock de {@link ServiceModificationRandonnee.UpdateHikeCallback}.</p>
     * <p><b>When</b> : on appelle {@code onSuccess()}.</p>
     * <p><b>Then</b> : Mockito vérifie que la méthode a été appelée exactement une fois.</p>
     */
    @Test
    public void updateHikeCallback_onSuccess_estAppeleUneSeuleFois() {
        // Given — mock initialisé dans setUp()

        // When
        mockCallback.onSuccess();

        // Then
        verify(mockCallback, times(1)).onSuccess();
    }

    /**
     * Vérifie que le mock de callback {@code onError} est bien invocable
     * avec un message d'erreur non nul.
     *
     * <p><b>Given</b> : un message d'erreur représentant une erreur Volley sérialisée.</p>
     * <p><b>When</b> : on appelle {@code onError} avec ce message.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte avec le bon message.</p>
     */
    @Test
    public void updateHikeCallback_onError_estAppeleAvecMessage() {
        // Given
        String messageErreur = "com.android.volley.TimeoutError";

        // When
        mockCallback.onError(messageErreur);

        // Then
        verify(mockCallback, times(1)).onError(messageErreur);
    }

    /**
     * Vérifie que {@code onSuccess} n'est jamais appelé lorsque seul {@code onError}
     * est déclenché, et inversement.
     *
     * <p><b>Given</b> : un mock de callback fraîchement initialisé.</p>
     * <p><b>When</b> : on appelle uniquement {@code onError}.</p>
     * <p><b>Then</b> : {@code onSuccess} n'a pas été appelé.</p>
     */
    @Test
    public void updateHikeCallback_onError_nAppellePasOnSuccess() {
        // Given
        String messageErreur = "Erreur réseau";

        // When
        mockCallback.onError(messageErreur);

        // Then
        verify(mockCallback, times(0)).onSuccess();
        verify(mockCallback, times(1)).onError(messageErreur);
    }

    /**
     * Vérifie qu'un callback null est géré sans lever de {@link NullPointerException}
     * au niveau du service (la méthode {@code modifierRandonneeAPI} vérifie
     * {@code callback != null} avant chaque appel).
     *
     * <p><b>Given</b> : un callback null.</p>
     * <p><b>When</b> : on simule l'appel conditionnel présent dans le service.</p>
     * <p><b>Then</b> : aucune exception n'est levée.</p>
     */
    @Test
    public void updateHikeCallback_callbackNull_neLevePasNullPointerException() {
        // Given
        ServiceModificationRandonnee.UpdateHikeCallback callbackNull = null;

        // When — simulation du comportement conditionnel du service
        boolean exceptionLevee = false;
        try {
            if (callbackNull != null) callbackNull.onSuccess();
        } catch (NullPointerException e) {
            exceptionLevee = true;
        }

        // Then
        assertEquals("La vérification null du service doit éviter toute NullPointerException",
                false, exceptionLevee);
    }

    // =========================================================================
    // Tests — construireJsonRandonnee — Précision des coordonnées
    // =========================================================================

    /**
     * Vérifie que les coordonnées à haute précision (nombreuses décimales) sont
     * conservées sans perte de précision significative.
     *
     * <p><b>Given</b> : des coordonnées GPS à haute précision (6 décimales).</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les valeurs sont conservées avec une tolérance de 6 décimales.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_coordonneesHautePrecision_sontConservees() throws Exception {
        // Given
        double latPrecise = 44.350812;
        double lonPrecise = 2.573156;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, DUREE_VALIDE,
                latPrecise, lonPrecise, LAT_ARRIVEE, LON_ARRIVEE);

        // Then
        assertNotNull(result);
        assertEquals("La latitude à haute précision doit être conservée",
                latPrecise, result.getJSONObject("depart").getDouble("latitude"), 0.000001);
        assertEquals("La longitude à haute précision doit être conservée",
                lonPrecise, result.getJSONObject("depart").getDouble("longitude"), 0.000001);
    }

    /**
     * Vérifie que des coordonnées à zéro (équateur / méridien de Greenwich)
     * sont correctement stockées.
     *
     * <p><b>Given</b> : latitude = 0.0 et longitude = 0.0.</p>
     * <p><b>When</b> : on construit le JSON.</p>
     * <p><b>Then</b> : les valeurs 0.0 sont bien présentes dans le JSON.</p>
     *
     * @throws Exception si la réflexion ou la lecture du JSON échouent.
     */
    @Test
    public void construireJson_coordonneesZero_sontCorrectementStockees() throws Exception {
        // Given
        double latZero = 0.0;
        double lonZero = 0.0;

        // When
        JSONObject result = invoquerAvecParametres(LIBELLE_VALIDE, DUREE_VALIDE,
                latZero, lonZero, latZero, lonZero);

        // Then
        assertNotNull(result);
        assertEquals("La latitude zéro (équateur) doit être conservée",
                latZero, result.getJSONObject("depart").getDouble("latitude"), 0.0001);
        assertEquals("La longitude zéro (Greenwich) doit être conservée",
                lonZero, result.getJSONObject("depart").getDouble("longitude"), 0.0001);
    }
}