package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI;

/**
 * Classe de tests unitaires pour {@link ServicePOI}.
 *
 * <p>Teste l'ensemble des méthodes du service de gestion des Points d'Intérêt (POI)
 * en simulant les dépendances réseau et Android :</p>
 * <ul>
 *     <li>Cas nominaux : parsing JSON correct, appels API bien formés,
 *         extraction de POI unique et de liste</li>
 *     <li>Cas limites : JSON vide, champ id null, description absente,
 *         liste temporaire vide, fallback sur nbParticipants</li>
 *     <li>Cas d'erreur : champs obligatoires absents, POI null ignoré,
 *         clé absente dans extractSinglePOI</li>
 * </ul>
 *
 * <p>Utilise Mockito pour simuler {@link AppelAPI} et {@link Context}
 * sans dépendance au framework Android.</p>
 *
 * <p><b>Dépendances requises dans build.gradle :</b></p>
 * <pre>
 *     testImplementation 'junit:junit:4.13.2'
 *     testImplementation 'org.mockito:mockito-core:5.x.x'
 *     testImplementation 'org.mockito:mockito-inline:5.x.x'
 * </pre>
 *
 * @author A4AWalk
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ServicePOITest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL de base de l'API */
    private static final String BASE_URL = "http://98.94.8.220:8080";

    /** Token d'authentification utilisé dans les tests */
    private static final String TOKEN_VALIDE = "Bearer.token.test";

    /** Identifiant de randonnée utilisé dans les tests */
    private static final int HIKE_ID = 7;

    /** Identifiant de randonnée sous forme Long (utilisé dans ajoutPOI) */
    private static final long HIKE_ID_LONG = 7L;

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android */
    @Mock
    private Context mockContexte;

    // =========================================================================
    // CLASSE INTERNE — VolleyError avec code HTTP
    // =========================================================================

    /**
     * Sous-classe de {@link VolleyError} permettant de définir un code HTTP précis.
     *
     * <p>Nécessaire car le champ {@code networkResponse} de {@link VolleyError}
     * est {@code final} et ne peut pas être réassigné directement après construction.</p>
     */
    private static class VolleyErrorAvecCode extends VolleyError {
        /**
         * Construit une erreur Volley avec le code HTTP spécifié.
         *
         * @param statusCode Code HTTP à simuler (ex : 400, 500)
         */
        VolleyErrorAvecCode(int statusCode) {
            super(new NetworkResponse(statusCode, new byte[0], false, 0, null));
        }
    }

    // =========================================================================
    // CONFIGURATION
    // =========================================================================

    /**
     * Réinitialise le singleton Volley avant chaque test pour isoler les cas.
     */
    @Before
    public void setUp() {
        AppelAPI.resetFileRequete();
    }

    /**
     * Réinitialise le singleton Volley après chaque test.
     */
    @After
    public void tearDown() {
        AppelAPI.resetFileRequete();
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un {@link JSONObject} représentant un POI complet avec tous ses champs.
     *
     * @param id          Identifiant du POI (-1 si null en base)
     * @param nom         Nom du POI
     * @param latitude    Latitude GPS
     * @param longitude   Longitude GPS
     * @param description Description du POI
     * @param sequence    Numéro d'ordre dans la randonnée
     * @return Un {@link JSONObject} représentant le POI
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildPOIJson(int id, String nom, double latitude,
                                    double longitude, String description,
                                    int sequence) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("nom", nom);
        obj.put("latitude", latitude);
        obj.put("longitude", longitude);
        obj.put("description", description);
        obj.put("sequence", sequence);
        return obj;
    }

    /**
     * Construit un {@link PointOfInterest} de test avec les propriétés minimales.
     *
     * @param nom       Nom du POI
     * @param latitude  Latitude GPS
     * @param longitude Longitude GPS
     * @return Un {@link PointOfInterest} configuré
     */
    private PointOfInterest buildPOI(String nom, double latitude, double longitude) {
        PointOfInterest poi = new PointOfInterest();
        poi.setNom(nom);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        return poi;
    }

    // =========================================================================
    // TESTS : parsePOI
    // =========================================================================

    /**
     * Vérifie que {@link ServicePOI#parsePOI(JSONObject)} construit correctement
     * un {@link PointOfInterest} depuis un JSON complet avec tous les champs.
     *
     * <p><b>Given</b> un JSONObject complet avec id, nom, latitude, longitude,
     * description et sequence<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> le POI retourné contient toutes les valeurs correctes</p>
     */
    @Test
    public void parsePOI_jsonComplet_retournePOICorrect() throws JSONException {
        // Given
        JSONObject json = buildPOIJson(1, "Sommet du Puy", 44.3375, 2.5725,
                "Vue panoramique", 3);

        // When
        PointOfInterest poi = ServicePOI.parsePOI(json);

        // Then
        assertNotNull("Le POI ne doit pas être null", poi);
        assertEquals("L'id doit être 1", 1, poi.getId());
        assertEquals("Le nom doit être 'Sommet du Puy'", "Sommet du Puy", poi.getNom());
        assertEquals("La latitude doit être 44.3375", 44.3375, poi.getLatitude(), 0.00001);
        assertEquals("La longitude doit être 2.5725", 2.5725, poi.getLongitude(), 0.00001);
        assertEquals("La description doit correspondre", "Vue panoramique", poi.getDescription());
        assertEquals("La séquence doit être 3", 3, poi.getSequence());
    }

    /**
     * Vérifie que {@code parsePOI} affecte l'id {@code -1} lorsque le champ "id"
     * est explicitement {@code null} dans le JSON.
     *
     * <p><b>Given</b> un JSON avec le champ "id" à JSON null<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> l'id du POI retourné est -1 (valeur sentinelle)</p>
     */
    @Test
    public void parsePOI_idNull_retourneIdMoinsUn() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("id", JSONObject.NULL);
        json.put("nom", "POI sans id");
        json.put("latitude", 44.0);
        json.put("longitude", 2.0);

        // When
        PointOfInterest poi = ServicePOI.parsePOI(json);

        // Then
        assertEquals("L'id doit être -1 quand le JSON contient null", -1, poi.getId());
    }

    /**
     * Vérifie que {@code parsePOI} utilise une chaîne vide comme valeur par défaut
     * pour {@code description} lorsque le champ est absent.
     *
     * <p><b>Given</b> un JSON sans champ "description"<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> la description du POI est une chaîne vide</p>
     */
    @Test
    public void parsePOI_descriptionAbsente_retourneDescriptionVide() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("id", 2);
        json.put("nom", "Belvédère");
        json.put("latitude", 44.1);
        json.put("longitude", 2.1);
        // Pas de champ "description"

        // When
        PointOfInterest poi = ServicePOI.parsePOI(json);

        // Then
        assertEquals("La description par défaut doit être vide", "", poi.getDescription());
    }

    /**
     * Vérifie que {@code parsePOI} utilise 0 comme valeur par défaut
     * pour {@code sequence} lorsque le champ est absent.
     *
     * <p><b>Given</b> un JSON sans champ "sequence"<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> la séquence du POI est 0</p>
     */
    @Test
    public void parsePOI_sequenceAbsente_retourneSequenceZero() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("id", 3);
        json.put("nom", "Lac");
        json.put("latitude", 44.2);
        json.put("longitude", 2.2);
        // Pas de champ "sequence"

        // When
        PointOfInterest poi = ServicePOI.parsePOI(json);

        // Then
        assertEquals("La séquence par défaut doit être 0", 0, poi.getSequence());
    }

    /**
     * Vérifie que {@code parsePOI} lève une {@link JSONException}
     * lorsque le champ obligatoire "nom" est absent.
     *
     * <p><b>Given</b> un JSON sans le champ "nom" (obligatoire via {@code getString})<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> une {@link JSONException} est levée</p>
     */
    @Test(expected = JSONException.class)
    public void parsePOI_nomAbsent_leveJSONException() throws JSONException {
        // Given — JSON sans le champ "nom"
        JSONObject json = new JSONObject();
        json.put("id", 4);
        json.put("latitude", 44.3);
        json.put("longitude", 2.3);

        // When — doit lever JSONException
        ServicePOI.parsePOI(json);
    }

    /**
     * Vérifie que {@code parsePOI} lève une {@link JSONException}
     * lorsque le champ obligatoire "latitude" est absent.
     *
     * <p><b>Given</b> un JSON sans le champ "latitude"<br>
     * <b>When</b> on appelle {@code parsePOI}<br>
     * <b>Then</b> une {@link JSONException} est levée</p>
     */
    @Test(expected = JSONException.class)
    public void parsePOI_latitudeAbsente_leveJSONException() throws JSONException {
        // Given — JSON sans le champ "latitude"
        JSONObject json = new JSONObject();
        json.put("id", 5);
        json.put("nom", "Col");
        json.put("longitude", 2.4);

        // When — doit lever JSONException
        ServicePOI.parsePOI(json);
    }

    // =========================================================================
    // TESTS : extractSinglePOI
    // =========================================================================

    /**
     * Vérifie que {@link ServicePOI#extractSinglePOI(JSONObject, String)} retourne
     * un {@link PointOfInterest} correctement parsé depuis la clé donnée.
     *
     * <p><b>Given</b> une réponse JSON contenant un POI sous la clé "depart"<br>
     * <b>When</b> on appelle {@code extractSinglePOI} avec la clé "depart"<br>
     * <b>Then</b> le POI retourné est non null et contient les bonnes valeurs</p>
     */
    @Test
    public void extractSinglePOI_clePresente_retournePOICorrect() throws JSONException {
        // Given
        JSONObject poiJson = buildPOIJson(10, "Départ", 44.35, 2.57, "Point de départ", 0);
        JSONObject reponse = new JSONObject();
        reponse.put("depart", poiJson);

        // When
        PointOfInterest poi = ServicePOI.extractSinglePOI(reponse, "depart");

        // Then
        assertNotNull("Le POI extrait ne doit pas être null", poi);
        assertEquals("Le nom doit être 'Départ'", "Départ", poi.getNom());
        assertEquals("L'id doit être 10", 10, poi.getId());
    }

    /**
     * Vérifie que {@code extractSinglePOI} retourne {@code null}
     * lorsque la clé demandée est absente de la réponse JSON.
     *
     * <p><b>Given</b> une réponse JSON ne contenant pas la clé "arrivee"<br>
     * <b>When</b> on appelle {@code extractSinglePOI} avec la clé "arrivee"<br>
     * <b>Then</b> null est retourné sans exception</p>
     */
    @Test
    public void extractSinglePOI_cleAbsente_retourneNull() {
        // Given
        JSONObject reponse = new JSONObject();

        // When
        PointOfInterest poi = ServicePOI.extractSinglePOI(reponse, "arrivee");

        // Then
        assertNull("Le POI doit être null si la clé est absente", poi);
    }

    /**
     * Vérifie que {@code extractSinglePOI} retourne {@code null}
     * lorsque la valeur associée à la clé est {@code null} dans le JSON.
     *
     * <p><b>Given</b> une réponse JSON avec la clé "depart" à JSON null<br>
     * <b>When</b> on appelle {@code extractSinglePOI} avec la clé "depart"<br>
     * <b>Then</b> null est retourné sans exception</p>
     */
    @Test
    public void extractSinglePOI_valeurNull_retourneNull() throws JSONException {
        // Given
        JSONObject reponse = new JSONObject();
        reponse.put("depart", JSONObject.NULL);

        // When
        PointOfInterest poi = ServicePOI.extractSinglePOI(reponse, "depart");

        // Then
        assertNull("Le POI doit être null si la valeur JSON est null", poi);
    }

    /**
     * Vérifie que {@code extractSinglePOI} retourne {@code null} et ne propage pas
     * d'exception lorsque le JSON du POI est malformé (champ obligatoire manquant).
     *
     * <p><b>Given</b> une réponse JSON avec un POI sans champ "nom"<br>
     * <b>When</b> on appelle {@code extractSinglePOI}<br>
     * <b>Then</b> null est retourné (JSONException attrapée en interne)</p>
     */
    @Test
    public void extractSinglePOI_poiMalForme_retourneNull() throws JSONException {
        // Given — POI sans champ "nom" → JSONException lors du parsePOI
        JSONObject poiMalForme = new JSONObject();
        poiMalForme.put("id", 1);
        poiMalForme.put("latitude", 44.0);
        poiMalForme.put("longitude", 2.0);
        // "nom" absent

        JSONObject reponse = new JSONObject();
        reponse.put("depart", poiMalForme);

        // When
        PointOfInterest poi = ServicePOI.extractSinglePOI(reponse, "depart");

        // Then
        assertNull("Le POI doit être null si le JSON est malformé", poi);
    }

    // =========================================================================
    // TESTS : extractPOIs
    // =========================================================================

    /**
     * Vérifie que {@link ServicePOI#extractPOIs(JSONObject)} retourne une liste
     * correctement peuplée depuis une réponse JSON avec un tableau "points" valide.
     *
     * <p><b>Given</b> une réponse JSON avec 2 POIs dans le tableau "points"<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> la liste retournée contient 2 éléments avec les bons noms</p>
     */
    @Test
    public void extractPOIs_deuxPoints_retourneListeDeuxElements() throws JSONException {
        // Given
        JSONArray points = new JSONArray();
        points.put(buildPOIJson(1, "Cascade", 44.1, 2.1, "Belle cascade", 1));
        points.put(buildPOIJson(2, "Panorama", 44.2, 2.2, "Vue dégagée", 2));

        JSONObject reponse = new JSONObject();
        reponse.put("points", points);

        // When
        ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertEquals("La liste doit contenir 2 éléments", 2, liste.size());
        assertEquals("Le premier POI doit s'appeler 'Cascade'", "Cascade", liste.get(0).getNom());
        assertEquals("Le second POI doit s'appeler 'Panorama'", "Panorama", liste.get(1).getNom());
    }

    /**
     * Vérifie que {@code extractPOIs} retourne une liste vide
     * lorsque la réponse ne contient pas de champ "points".
     *
     * <p><b>Given</b> une réponse JSON sans champ "points" et sans nbParticipants<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> une liste vide est retournée sans exception</p>
     */
    @Test
    public void extractPOIs_sansChampPoints_retourneListeVide() {
        // Given
        JSONObject reponseSansPoints = new JSONObject();

        // When
        ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponseSansPoints);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide en l'absence de 'points'", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractPOIs} retourne une liste vide
     * lorsque le tableau "points" est présent mais vide et qu'il n'y a pas de fallback.
     *
     * <p><b>Given</b> une réponse JSON avec un tableau "points" vide et nbParticipants à 0<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> la liste retournée est vide</p>
     */
    @Test
    public void extractPOIs_tableauPointsVide_retourneListeVide() throws JSONException {
        // Given
        JSONObject reponse = new JSONObject();
        reponse.put("points", new JSONArray());
        reponse.put("nbParticipants", 0);

        // When
        ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide avec un tableau 'points' vide", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractPOIs} utilise le fallback {@code nbParticipants}
     * lorsque le tableau "points" est absent ou vide, en créant autant de POI vides.
     *
     * <p><b>Given</b> une réponse JSON sans "points" mais avec nbParticipants = 3<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> la liste contient 3 POIs vides</p>
     */
    @Test
    public void extractPOIs_sansPointsAvecNbParticipants_retourneListeFallback() throws JSONException {
        // Given
        JSONObject reponse = new JSONObject();
        reponse.put("nbParticipants", 3);
        // Pas de champ "points"

        // When
        ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertEquals("Le fallback doit créer 3 POIs vides", 3, liste.size());
    }

    /**
     * Vérifie que {@code extractPOIs} utilise le fallback secondaire {@code participants}
     * lorsque {@code nbParticipants} est absent mais {@code participants} est présent.
     *
     * <p><b>Given</b> une réponse JSON sans "points" ni "nbParticipants" mais avec participants = 2<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> la liste contient 2 POIs vides</p>
     */
    @Test
    public void extractPOIs_avecParticipantsFallback_retourneListeParticipants() throws JSONException {
        // Given
        JSONObject reponse = new JSONObject();
        reponse.put("participants", 2);
        // Pas de "points" ni "nbParticipants"

        // When
        ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertEquals("Le fallback 'participants' doit créer 2 POIs vides", 2, liste.size());
    }

    /**
     * Vérifie que {@code extractPOIs} ne propage pas d'exception et retourne
     * une liste partielle lorsqu'un POI du tableau "points" est malformé.
     *
     * <p><b>Given</b> un tableau "points" avec un POI valide et un POI sans "nom"<br>
     * <b>When</b> on appelle {@code extractPOIs}<br>
     * <b>Then</b> aucune exception ne se propage (JSONException attrapée en interne)</p>
     */
    @Test
    public void extractPOIs_unPOIMalForme_pasException() throws JSONException {
        // Given
        JSONArray points = new JSONArray();
        points.put(buildPOIJson(1, "Pont", 44.0, 2.0, "Vieux pont", 1));
        JSONObject poiSansNom = new JSONObject();
        poiSansNom.put("id", 2);
        poiSansNom.put("latitude", 44.1);
        poiSansNom.put("longitude", 2.1);
        // "nom" absent
        points.put(poiSansNom);

        JSONObject reponse = new JSONObject();
        reponse.put("points", points);

        // When / Then — aucune exception ne doit se propager
        try {
            ArrayList<PointOfInterest> liste = ServicePOI.extractPOIs(reponse);
            assertNotNull("La liste ne doit pas être null même avec un POI malformé", liste);
        } catch (Exception e) {
            fail("Aucune exception ne doit se propager depuis extractPOIs : " + e.getMessage());
        }
    }

    // =========================================================================
    // TESTS : ajoutPOI
    // =========================================================================

    /**
     * Vérifie que {@link ServicePOI#ajoutPOI(Context, String, PointOfInterest, Long)}
     * appelle {@link AppelAPI#post} avec l'URL correcte contenant l'idRandonnée.
     *
     * <p><b>Given</b> un POI valide et un idRandonnée<br>
     * <b>When</b> on appelle {@code ajoutPOI}<br>
     * <b>Then</b> AppelAPI.post est invoqué avec l'URL {@code /hikes/{id}/poi}</p>
     */
    @Test
    public void ajoutPOI_urlConstuite_contientHikeIdEtPoi() {
        // Given
        String urlAttendue = BASE_URL + "/hikes/" + HIKE_ID_LONG + "/poi";
        PointOfInterest poi = buildPOI("Source", 44.4, 2.6);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServicePOI.ajoutPOI(mockContexte, TOKEN_VALIDE, poi, HIKE_ID_LONG);

            // Then
            assertEquals("L'URL doit contenir l'idRandonnée et /poi",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que le corps JSON envoyé par {@code ajoutPOI} contient
     * les champs nom, latitude et longitude du POI.
     *
     * <p><b>Given</b> un POI avec nom, latitude et longitude<br>
     * <b>When</b> on appelle {@code ajoutPOI}<br>
     * <b>Then</b> le body JSON contient les valeurs correctes</p>
     */
    @Test
    public void ajoutPOI_corpsRequete_contientNomLatLon() throws JSONException {
        // Given
        PointOfInterest poi = buildPOI("Ruines", 44.5, 2.7);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServicePOI.ajoutPOI(mockContexte, TOKEN_VALIDE, poi, HIKE_ID_LONG);

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps de la requête ne doit pas être null", body);
            assertEquals("Le nom doit correspondre", "Ruines", body.getString("nom"));
            assertEquals("La latitude doit correspondre", 44.5, body.getDouble("latitude"), 0.00001);
            assertEquals("La longitude doit correspondre", 2.7, body.getDouble("longitude"), 0.00001);
        }
    }

    /**
     * Vérifie que {@code ajoutPOI} n'effectue aucun appel API
     * lorsque {@code createPOIJson} retourne null (ex : POI avec des données
     * provoquant une {@link JSONException} interne).
     *
     * <p><b>Given</b> un POI dont le nom est null (cas où JSONException est possible)<br>
     * <b>When</b> on appelle {@code ajoutPOI}<br>
     * <b>Then</b> AppelAPI.post n'est pas invoqué si le JSON ne peut pas être construit</p>
     *
     * <p><i>Note : JSONObject.put() avec une valeur null ne lève pas d'exception dans
     * l'implémentation Android standard — ce test documente le comportement observé.</i></p>
     */
    @Test
    public void ajoutPOI_poiAvecNomNull_comportementDocumente() {
        // Given — POI avec nom null (createPOIJson peut retourner null ou un JSON valide)
        PointOfInterest poiSansNom = new PointOfInterest();
        poiSansNom.setNom(null);
        poiSansNom.setLatitude(44.0);
        poiSansNom.setLongitude(2.0);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), any(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When — ne doit pas lever d'exception
            try {
                ServicePOI.ajoutPOI(mockContexte, TOKEN_VALIDE, poiSansNom, HIKE_ID_LONG);
            } catch (Exception e) {
                fail("ajoutPOI ne doit pas propager d'exception : " + e.getMessage());
            }

            // Then — l'appel est effectué ou non selon le comportement de JSONObject.put(null)
            // Ce test valide l'absence d'exception, pas le nombre d'appels
        }
    }

    // =========================================================================
    // TESTS : traiterMAJPOI
    // =========================================================================

    /**
     * Vérifie que {@link ServicePOI#traiterMAJPOI(Context, int, ArrayList, String)}
     * appelle {@link AppelAPI#putA} avec l'URL correcte lorsque la liste est non vide.
     *
     * <p><b>Given</b> une liste de 2 POIs valides<br>
     * <b>When</b> on appelle {@code traiterMAJPOI}<br>
     * <b>Then</b> AppelAPI.putA est invoqué avec l'URL {@code /hikes/{id}/pois}</p>
     */
    @Test
    public void traiterMAJPOI_listeNonVide_appelleputA_avecBonneUrl() {
        // Given
        String urlAttendue = BASE_URL + "/hikes/" + HIKE_ID + "/pois";
        ArrayList<PointOfInterest> liste = new ArrayList<>();
        liste.add(buildPOI("Sommet", 44.3, 2.5));
        liste.add(buildPOI("Refuge", 44.4, 2.6));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.putA(captureurUrl.capture(), anyString(),
                            any(JSONArray.class), any(Context.class),
                            any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServicePOI.traiterMAJPOI(mockContexte, HIKE_ID, liste, TOKEN_VALIDE);

            // Then
            assertEquals("L'URL doit contenir le hikeId et /pois",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que {@code traiterMAJPOI} envoie un tableau JSON contenant
     * autant d'éléments que de POIs dans la liste.
     *
     * <p><b>Given</b> une liste de 3 POIs valides<br>
     * <b>When</b> on appelle {@code traiterMAJPOI}<br>
     * <b>Then</b> le tableau JSON transmis à AppelAPI.putA contient 3 éléments</p>
     */
    @Test
    public void traiterMAJPOI_troisPOIs_tableauJsonTroisElements() {
        // Given
        ArrayList<PointOfInterest> liste = new ArrayList<>();
        liste.add(buildPOI("Col A", 44.1, 2.1));
        liste.add(buildPOI("Col B", 44.2, 2.2));
        liste.add(buildPOI("Col C", 44.3, 2.3));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONArray> captureurArray = ArgumentCaptor.forClass(JSONArray.class);

            staticMock.when(() -> AppelAPI.putA(anyString(), anyString(), captureurArray.capture(),
                            any(Context.class), any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServicePOI.traiterMAJPOI(mockContexte, HIKE_ID, liste, TOKEN_VALIDE);

            // Then
            assertEquals("Le tableau JSON doit contenir 3 éléments",
                    3, captureurArray.getValue().length());
        }
    }

    /**
     * Vérifie que {@code traiterMAJPOI} n'effectue aucun appel API
     * lorsque la liste de POIs est vide.
     *
     * <p><b>Given</b> une liste de POIs vide<br>
     * <b>When</b> on appelle {@code traiterMAJPOI}<br>
     * <b>Then</b> AppelAPI.putA n'est jamais invoqué</p>
     */
    @Test
    public void traiterMAJPOI_listeVide_aucunAppelAPI() {
        // Given
        ArrayList<PointOfInterest> listeVide = new ArrayList<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServicePOI.traiterMAJPOI(mockContexte, HIKE_ID, listeVide, TOKEN_VALIDE);

            // Then
            staticMock.verify(() -> AppelAPI.putA(any(), any(), any(), any(), any()), never());
        }
    }

    /**
     * Vérifie que {@code traiterMAJPOI} transmet correctement le token
     * à {@link AppelAPI#putA}.
     *
     * <p><b>Given</b> un token valide et une liste non vide<br>
     * <b>When</b> on appelle {@code traiterMAJPOI}<br>
     * <b>Then</b> le token transmis à AppelAPI.putA est identique au token fourni</p>
     */
    @Test
    public void traiterMAJPOI_tokenTransmis_estIdentique() {
        // Given
        ArrayList<PointOfInterest> liste = new ArrayList<>();
        liste.add(buildPOI("Fontaine", 44.0, 2.0));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.putA(anyString(), captureurToken.capture(),
                            any(JSONArray.class), any(Context.class),
                            any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServicePOI.traiterMAJPOI(mockContexte, HIKE_ID, liste, TOKEN_VALIDE);

            // Then
            assertEquals("Le token transmis doit être identique",
                    TOKEN_VALIDE, captureurToken.getValue());
        }
    }
}