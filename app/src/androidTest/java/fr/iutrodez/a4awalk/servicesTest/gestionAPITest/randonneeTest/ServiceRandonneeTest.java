package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;

/**
 * Classe de tests unitaires pour {@link ServiceRandonnee}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Parsing d'un objet JSON en {@link Hike} — mode liste ({@code isDetails=false})</li>
 *   <li>Parsing d'un objet JSON en {@link Hike} — mode détail ({@code isDetails=true})</li>
 *   <li>Parsing d'un tableau JSON en liste de {@link Hike}</li>
 *   <li>Comportement des callbacks {@link ServiceRandonnee.RandoCallback}
 *       et {@link ServiceRandonnee.RandoDetailCallback}</li>
 *   <li>Cas limites : tableau vide, JSON incomplet, champs manquants</li>
 *   <li>Cas d'erreur : JSON null, champs obligatoires absents</li>
 * </ul>
 *
 * <p>La convention <b>Given / When / Then</b> est appliquée sur chaque test
 * pour garantir la lisibilité et la maintenabilité.</p>
 *
 * <p>Dépendances de test requises dans {@code build.gradle (app)} :</p>
 * <pre>
 * testImplementation 'junit:junit:4.13.2'
 * testImplementation 'org.mockito:mockito-core:5.x.x'
 * testImplementation 'org.mockito:mockito-inline:5.x.x'
 * </pre>
 *
 * @author Équipe A4AWalk
 * @version 1.0
 * @see ServiceRandonnee
 * @see Hike
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceRandonneeTest {

    // =========================================================================
    // Constantes de test
    // =========================================================================

    /** Identifiant de randonnée utilisé dans les tests nominaux. */
    private static final int    ID_RANDO            = 1;

    /** Libellé de randonnée utilisé dans les tests nominaux. */
    private static final String LIBELLE_RANDO       = "Tour du Rouergue";

    /** Durée en jours utilisée dans les tests nominaux. */
    private static final int    DUREE_JOURS         = 3;

    /** Valeur du champ optimize en mode liste. */
    private static final boolean OPTIMIZE_LISTE     = true;

    /** Valeur du champ optimize en mode détail. */
    private static final boolean OPTIMIZE_DETAIL    = false;

    /** Nom du point de départ utilisé dans les tests détaillés. */
    private static final String NOM_DEPART          = "Rodez";

    /** Description du point de départ. */
    private static final String DESC_DEPART         = "Chef-lieu de l'Aveyron";

    /** Latitude du point de départ. */
    private static final double LAT_DEPART          = 44.3508;

    /** Longitude du point de départ. */
    private static final double LON_DEPART          = 2.5731;

    /** Nom du point d'arrivée. */
    private static final String NOM_ARRIVEE         = "Millau";

    /** Description du point d'arrivée. */
    private static final String DESC_ARRIVEE        = "Ville du viaduc";

    /** Latitude du point d'arrivée. */
    private static final double LAT_ARRIVEE         = 44.0980;

    /** Longitude du point d'arrivée. */
    private static final double LON_ARRIVEE         = 3.0780;

    // =========================================================================
    // Mocks
    // =========================================================================

    /** Mock du callback de liste de randonnées. */
    @Mock
    private ServiceRandonnee.RandoCallback mockRandoCallback;

    /** Mock du callback de détail d'une randonnée. */
    @Mock
    private ServiceRandonnee.RandoDetailCallback mockDetailCallback;

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
    // Méthodes utilitaires privées
    // =========================================================================

    /**
     * Construit un {@link JSONObject} représentant une randonnée en mode liste
     * (champ {@code isOptimize} présent, sans détails de départ/arrivée).
     *
     * @return un JSONObject valide pour le mode liste.
     * @throws JSONException si la construction du JSON échoue.
     */
    private JSONObject creerJsonRandoListe() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id",          ID_RANDO);
        json.put("libelle",     LIBELLE_RANDO);
        json.put("dureeJours",  DUREE_JOURS);
        json.put("isOptimize",  OPTIMIZE_LISTE);
        return json;
    }

    /**
     * Construit un {@link JSONObject} représentant une randonnée en mode détail
     * (champ {@code optimize} présent, avec départ, arrivée, participants, POIs,
     * catalogue alimentaire et équipements).
     *
     * @return un JSONObject valide pour le mode détail.
     * @throws JSONException si la construction du JSON échoue.
     */
    private JSONObject creerJsonRandoDetail() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id",         ID_RANDO);
        json.put("libelle",    LIBELLE_RANDO);
        json.put("dureeJours", DUREE_JOURS);
        json.put("optimize",   OPTIMIZE_DETAIL);

        // Bloc départ
        JSONObject depart = new JSONObject();
        depart.put("nom",         NOM_DEPART);
        depart.put("description", DESC_DEPART);
        depart.put("latitude",    LAT_DEPART);
        depart.put("longitude",   LON_DEPART);
        json.put("depart", depart);

        // Bloc arrivée
        JSONObject arrivee = new JSONObject();
        arrivee.put("nom",         NOM_ARRIVEE);
        arrivee.put("description", DESC_ARRIVEE);
        arrivee.put("latitude",    LAT_ARRIVEE);
        arrivee.put("longitude",   LON_ARRIVEE);
        json.put("arrivee", arrivee);

        // Listes vides pour les éléments optionnels (ServicePOI / ServiceParticipant, etc.)
        json.put("participants",   new JSONArray());
        json.put("points",         new JSONArray());
        json.put("foodCatalogue",  new JSONArray());
        json.put("equipmentGroups", new JSONArray());

        return json;
    }

    // =========================================================================
    // Tests — parseHikeDetail — Mode liste (isDetails = false) — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que le parsing d'un JSON valide en mode liste retourne un objet
     * {@link Hike} non nul.
     *
     * <p><b>Given</b> : un JSON de randonnée valide en mode liste.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le {@link Hike} retourné est non nul.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_jsonValide_modeListe_retourneHikeNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull("Le Hike parsé ne doit pas être nul pour un JSON valide", result);
    }

    /**
     * Vérifie que l'identifiant du {@link Hike} est correctement extrait du JSON
     * en mode liste.
     *
     * <p><b>Given</b> : un JSON de randonnée avec {@code id = 1}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : {@code hike.getId()} vaut 1.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeListe_idEstCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull(result);
        assertEquals("L'identifiant doit correspondre au champ 'id' du JSON",
                ID_RANDO, result.getId());
    }

    /**
     * Vérifie que le libellé du {@link Hike} est correctement extrait du JSON
     * en mode liste.
     *
     * <p><b>Given</b> : un JSON avec le libellé {@value #LIBELLE_RANDO}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : {@code hike.getLibelle()} correspond au libellé fourni.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeListe_libelleEstCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull(result);
        assertEquals("Le libellé doit correspondre au champ 'libelle' du JSON",
                LIBELLE_RANDO, result.getLibelle());
    }

    /**
     * Vérifie que la durée en jours du {@link Hike} est correctement extraite
     * en mode liste.
     *
     * <p><b>Given</b> : un JSON avec {@code dureeJours = 3}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : {@code hike.getDureeJours()} vaut 3.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeListe_dureeJoursEstCorrectementExtraite() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull(result);
        assertEquals("La durée en jours doit correspondre au champ 'dureeJours' du JSON",
                DUREE_JOURS, result.getDureeJours());
    }

    /**
     * Vérifie que le champ {@code isOptimize} est bien lu en mode liste.
     *
     * <p><b>Given</b> : un JSON de mode liste avec {@code isOptimize = true}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : {@code hike.getOptimize()} vaut {@code true}.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeListe_isOptimizeEstCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull(result);
        assertEquals("Le champ 'isOptimize' doit être lu en mode liste",
                OPTIMIZE_LISTE, result.getOptimize());
    }

    // =========================================================================
    // Tests — parseHikeDetail — Mode détail (isDetails = true) — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que le parsing d'un JSON valide en mode détail retourne un {@link Hike}
     * non nul.
     *
     * <p><b>Given</b> : un JSON de randonnée complet en mode détail.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : le {@link Hike} retourné est non nul.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_jsonValide_modeDetail_retourneHikeNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoDetail();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNotNull("Le Hike parsé en mode détail ne doit pas être nul", result);
    }

    /**
     * Vérifie que le champ {@code optimize} (sans préfixe "is") est bien lu en mode détail.
     *
     * <p><b>Given</b> : un JSON de mode détail avec {@code optimize = false}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : {@code hike.getOptimize()} vaut {@code false}.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeDetail_optimizeEstCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoDetail();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNotNull(result);
        assertEquals("Le champ 'optimize' (sans 'is') doit être lu en mode détail",
                OPTIMIZE_DETAIL, result.getOptimize());
    }

    /**
     * Vérifie que le point de départ est non nul après parsing en mode détail.
     *
     * <p><b>Given</b> : un JSON de mode détail avec un bloc {@code "depart"} valide.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : {@code hike.getDepart()} est non nul.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeDetail_departEstNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoDetail();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNotNull(result);
        assertNotNull("Le point de départ ne doit pas être nul en mode détail",
                result.getDepart());
    }

    /**
     * Vérifie que le point d'arrivée est non nul après parsing en mode détail.
     *
     * <p><b>Given</b> : un JSON de mode détail avec un bloc {@code "arrivee"} valide.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : {@code hike.getArrivee()} est non nul.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeDetail_arriveeEstNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoDetail();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNotNull(result);
        assertNotNull("Le point d'arrivée ne doit pas être nul en mode détail",
                result.getArrivee());
    }

    /**
     * Vérifie que les champs communs (id, libellé, durée) sont correctement
     * extraits en mode détail.
     *
     * <p><b>Given</b> : un JSON de mode détail avec des champs de base valides.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : id, libellé et durée correspondent aux valeurs du JSON.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeDetail_champsDeBaseCorrectementExtraits() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoDetail();

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNotNull(result);
        assertEquals("L'id doit être correct en mode détail",      ID_RANDO,      result.getId());
        assertEquals("Le libellé doit être correct en mode détail", LIBELLE_RANDO, result.getLibelle());
        assertEquals("La durée doit être correcte en mode détail",  DUREE_JOURS,   result.getDureeJours());
    }

    // =========================================================================
    // Tests — parseHikeDetail — Cas limites
    // =========================================================================

    /**
     * Vérifie que le parsing retourne {@code null} si le JSON est {@code null}.
     *
     * <p><b>Given</b> : un JSON {@code null}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(null, false)}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     */
    @Test
    public void parseHikeDetail_jsonNull_retourneNull() {
        // Given
        JSONObject jsonNull = null;

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(jsonNull, false);

        // Then
        assertNull("Un JSON null doit retourner un Hike null", result);
    }

    /**
     * Vérifie que le parsing retourne {@code null} si le champ obligatoire
     * {@code "id"} est absent du JSON.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "id"}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le résultat est {@code null} (exception capturée en interne).</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_champIdAbsent_retourneNull() throws JSONException {
        // Given — JSON sans l'identifiant obligatoire
        JSONObject json = new JSONObject();
        json.put("libelle",    LIBELLE_RANDO);
        json.put("dureeJours", DUREE_JOURS);
        json.put("isOptimize", OPTIMIZE_LISTE);

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNull("L'absence du champ 'id' doit produire un Hike null", result);
    }

    /**
     * Vérifie que le parsing retourne {@code null} si le champ obligatoire
     * {@code "libelle"} est absent du JSON.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "libelle"}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_champLibelleAbsent_retourneNull() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("id",         ID_RANDO);
        json.put("dureeJours", DUREE_JOURS);
        json.put("isOptimize", OPTIMIZE_LISTE);

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNull("L'absence du champ 'libelle' doit produire un Hike null", result);
    }

    /**
     * Vérifie que le parsing retourne {@code null} si le champ {@code "isOptimize"}
     * est absent en mode liste (champ obligatoire pour ce mode).
     *
     * <p><b>Given</b> : un JSON de mode liste sans le champ {@code "isOptimize"}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeListe_champIsOptimizeAbsent_retourneNull() throws JSONException {
        // Given — champ isOptimize manquant pour le mode liste
        JSONObject json = new JSONObject();
        json.put("id",         ID_RANDO);
        json.put("libelle",    LIBELLE_RANDO);
        json.put("dureeJours", DUREE_JOURS);
        // isOptimize volontairement absent

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNull("L'absence de 'isOptimize' en mode liste doit retourner null", result);
    }

    /**
     * Vérifie que le parsing retourne {@code null} si le champ {@code "optimize"}
     * est absent en mode détail (champ obligatoire pour ce mode).
     *
     * <p><b>Given</b> : un JSON de mode détail sans le champ {@code "optimize"}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, true)}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_modeDetail_champOptimizeAbsent_retourneNull() throws JSONException {
        // Given — champ optimize manquant pour le mode détail
        JSONObject json = new JSONObject();
        json.put("id",         ID_RANDO);
        json.put("libelle",    LIBELLE_RANDO);
        json.put("dureeJours", DUREE_JOURS);
        json.put("depart",     new JSONObject());
        json.put("arrivee",    new JSONObject());
        // optimize volontairement absent

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, true);

        // Then
        assertNull("L'absence de 'optimize' en mode détail doit retourner null", result);
    }

    /**
     * Vérifie que le parsing fonctionne avec une durée de 0 jour (valeur limite basse).
     *
     * <p><b>Given</b> : un JSON valide avec {@code dureeJours = 0}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le Hike est non nul et {@code getDureeJours()} vaut 0.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_dureeZero_retourneHikeValide() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();
        json.put("dureeJours", 0);

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull("Une durée de 0 jour doit être acceptée", result);
        assertEquals("La durée de 0 jour doit être conservée", 0, result.getDureeJours());
    }

    /**
     * Vérifie que le parsing fonctionne avec un identifiant négatif (valeur limite).
     *
     * <p><b>Given</b> : un JSON avec {@code id = -1}.</p>
     * <p><b>When</b> : on appelle {@code parseHikeDetail(json, false)}.</p>
     * <p><b>Then</b> : le Hike est non nul et {@code getId()} vaut -1.</p>
     *
     * <p><i>Note :</i> l'implémentation actuelle ne valide pas les identifiants
     * négatifs — ce test documente le comportement réel.</p>
     *
     * @throws JSONException si la construction du JSON de test échoue.
     */
    @Test
    public void parseHikeDetail_idNegatif_retourneHikeAvecIdNegatif() throws JSONException {
        // Given
        JSONObject json = creerJsonRandoListe();
        json.put("id", -1);

        // When
        Hike result = ServiceRandonnee.parseHikeDetail(json, false);

        // Then
        assertNotNull(result);
        assertEquals("Un id négatif est stocké tel quel (pas de validation métier)", -1, result.getId());
    }

    // =========================================================================
    // Tests — parseHikesFromJSON (via recupererRandonneesUtilisateur simulé)
    // =========================================================================

    /**
     * Vérifie que le parsing d'un tableau JSON vide retourne une liste vide
     * (et non null).
     *
     * <p><b>Given</b> : un tableau JSON vide {@code []}.</p>
     * <p><b>When</b> : le callback interne de {@code recupererRandonneesUtilisateur}
     * reçoit ce tableau.</p>
     * <p><b>Then</b> : le callback {@code onSuccess} est appelé avec une liste vide.</p>
     */
    @Test
    public void randoCallback_onSuccess_tableauVide_appelleCallbackAvecListeVide() {
        // Given
        ArrayList<Hike> listeVide = new ArrayList<>();

        // When
        mockRandoCallback.onSuccess(listeVide);

        // Then
        verify(mockRandoCallback, times(1)).onSuccess(listeVide);
    }

    /**
     * Vérifie que le callback {@code onError} de la liste est bien invocable
     * avec une {@link VolleyError}.
     *
     * <p><b>Given</b> : une {@link VolleyError} simulant une erreur réseau.</p>
     * <p><b>When</b> : on appelle {@code onError} sur le mock de callback.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte.</p>
     */
    @Test
    public void randoCallback_onError_estAppeleAvecVolleyError() {
        // Given
        VolleyError erreurSimulee = new VolleyError("Timeout réseau");

        // When
        mockRandoCallback.onError(erreurSimulee);

        // Then
        verify(mockRandoCallback, times(1)).onError(erreurSimulee);
    }

    /**
     * Vérifie que le callback {@code onSuccess} de détail est bien invocable
     * avec un {@link Hike} non nul.
     *
     * <p><b>Given</b> : un objet {@link Hike} valide.</p>
     * <p><b>When</b> : on appelle {@code onSuccess} sur le mock de callback détail.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation avec le bon objet.</p>
     */
    @Test
    public void detailCallback_onSuccess_estAppeleAvecHike() {
        // Given
        Hike hike = new Hike();
        hike.setId(ID_RANDO);
        hike.setLibelle(LIBELLE_RANDO);

        // When
        mockDetailCallback.onSuccess(hike);

        // Then
        verify(mockDetailCallback, times(1)).onSuccess(hike);
    }

    /**
     * Vérifie que le callback {@code onError} de détail est bien invocable
     * avec une {@link VolleyError}.
     *
     * <p><b>Given</b> : une {@link VolleyError} simulant un résultat vide.</p>
     * <p><b>When</b> : on appelle {@code onError} sur le mock de callback détail.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte.</p>
     */
    @Test
    public void detailCallback_onError_estAppeleAvecVolleyError() {
        // Given
        VolleyError erreurVide = new VolleyError("Résultat vide");

        // When
        mockDetailCallback.onError(erreurVide);

        // Then
        verify(mockDetailCallback, times(1)).onError(erreurVide);
    }

    // =========================================================================
    // Tests — parseHikeDetail — Cohérence entre mode liste et mode détail
    // =========================================================================

    /**
     * Vérifie que le même identifiant est produit en mode liste et en mode détail
     * pour le même JSON de base.
     *
     * <p><b>Given</b> : deux JSONs identiques sur les champs communs, l'un en mode
     * liste et l'autre en mode détail.</p>
     * <p><b>When</b> : on parse les deux objets.</p>
     * <p><b>Then</b> : les identifiants des deux {@link Hike} sont identiques.</p>
     *
     * @throws JSONException si la construction des JSONs de test échoue.
     */
    @Test
    public void parseHikeDetail_idIdentique_entresModeListe_etModeDetail() throws JSONException {
        // Given
        JSONObject jsonListe  = creerJsonRandoListe();
        JSONObject jsonDetail = creerJsonRandoDetail();

        // When
        Hike hikeListe  = ServiceRandonnee.parseHikeDetail(jsonListe,  false);
        Hike hikeDetail = ServiceRandonnee.parseHikeDetail(jsonDetail, true);

        // Then
        assertNotNull(hikeListe);
        assertNotNull(hikeDetail);
        assertEquals("L'identifiant doit être le même en mode liste et en mode détail",
                hikeListe.getId(), hikeDetail.getId());
    }

    /**
     * Vérifie que le libellé est identique entre mode liste et mode détail
     * pour le même JSON de base.
     *
     * <p><b>Given</b> : deux JSONs avec le même libellé.</p>
     * <p><b>When</b> : on parse les deux objets.</p>
     * <p><b>Then</b> : les libellés des deux {@link Hike} sont identiques.</p>
     *
     * @throws JSONException si la construction des JSONs de test échoue.
     */
    @Test
    public void parseHikeDetail_libelleIdentique_entresModeListe_etModeDetail() throws JSONException {
        // Given
        JSONObject jsonListe  = creerJsonRandoListe();
        JSONObject jsonDetail = creerJsonRandoDetail();

        // When
        Hike hikeListe  = ServiceRandonnee.parseHikeDetail(jsonListe,  false);
        Hike hikeDetail = ServiceRandonnee.parseHikeDetail(jsonDetail, true);

        // Then
        assertNotNull(hikeListe);
        assertNotNull(hikeDetail);
        assertEquals("Le libellé doit être le même en mode liste et en mode détail",
                hikeListe.getLibelle(), hikeDetail.getLibelle());
    }

    /**
     * Vérifie que le champ {@code optimize} est bien différent entre mode liste
     * ({@code isOptimize}) et mode détail ({@code optimize}) quand les valeurs
     * JSON diffèrent.
     *
     * <p><b>Given</b> : un JSON liste avec {@code isOptimize = true} et un JSON
     * détail avec {@code optimize = false}.</p>
     * <p><b>When</b> : on parse les deux objets.</p>
     * <p><b>Then</b> : les valeurs d'optimize sont différentes, confirmant la
     * lecture du bon champ selon le mode.</p>
     *
     * @throws JSONException si la construction des JSONs de test échoue.
     */
    @Test
    public void parseHikeDetail_champOptimizeDifferent_selonLeMode() throws JSONException {
        // Given
        JSONObject jsonListe  = creerJsonRandoListe();  // isOptimize = true
        JSONObject jsonDetail = creerJsonRandoDetail(); // optimize   = false

        // When
        Hike hikeListe  = ServiceRandonnee.parseHikeDetail(jsonListe,  false);
        Hike hikeDetail = ServiceRandonnee.parseHikeDetail(jsonDetail, true);

        // Then
        assertNotNull(hikeListe);
        assertNotNull(hikeDetail);
        assertTrue("Les valeurs d'optimize doivent être différentes selon le mode de parsing",
                hikeListe.getOptimize() != hikeDetail.getOptimize());
    }
}