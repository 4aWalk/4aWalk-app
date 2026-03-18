package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParcours;

/**
 * Classe de tests unitaires pour {@link ServiceParcours}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Création d'une {@link Course} depuis un JSON complet — cas nominaux</li>
 *   <li>Parsing d'un tableau de courses — cas nominaux et limites</li>
 *   <li>Gestion des champs optionnels ({@code depart}, {@code arrivee},
 *       {@code dateRealisation}, {@code path})</li>
 *   <li>Cas d'erreur : JSON null, champs obligatoires manquants</li>
 *   <li>Contrat des interfaces {@link ServiceParcours.ParcoursCallback}
 *       et {@link ServiceParcours.CourseCreationCallback}</li>
 * </ul>
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
 * @see ServiceParcours
 * @see Course
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceParcoursTest {

    // =========================================================================
    // Constantes de test
    // =========================================================================

    /** Identifiant MongoDB fictif d'une course. */
    private static final String ID_COURSE        = "507f1f77bcf86cd799439011";

    /** Identifiant de la randonnée associée. */
    private static final int    HIKE_ID          = 42;

    /** Date de réalisation au format ISO-8601. */
    private static final String DATE_REALISATION = "2026-02-05T08:19:12.027";

    /** Latitude du premier point du trajet. */
    private static final double LAT_POINT_1      = 44.3508;

    /** Longitude du premier point du trajet. */
    private static final double LON_POINT_1      = 2.5731;

    /** Latitude du deuxième point du trajet. */
    private static final double LAT_POINT_2      = 44.3600;

    /** Longitude du deuxième point du trajet. */
    private static final double LON_POINT_2      = 2.5800;

    // =========================================================================
    // Mocks
    // =========================================================================

    /** Mock du callback de liste de parcours. */
    @Mock
    private ServiceParcours.ParcoursCallback mockParcoursCallback;

    /** Mock du callback de création de course. */
    @Mock
    private ServiceParcours.CourseCreationCallback mockCreationCallback;

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
     * Construit un {@link JSONObject} représentant une course complète et valide,
     * avec un trajet de deux points et les champs départ/arrivée à {@code null}.
     *
     * @return un JSONObject de course valide.
     * @throws JSONException si la construction échoue.
     */
    private JSONObject creerJsonCourseComplet() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id",              ID_COURSE);
        json.put("hikeId",          HIKE_ID);
        json.put("isFinished",      false);
        json.put("isPaused",        false);
        json.put("dateRealisation", DATE_REALISATION);
        json.put("depart",          JSONObject.NULL);
        json.put("arrivee",         JSONObject.NULL);

        JSONArray path = new JSONArray();
        JSONObject p1 = new JSONObject();
        p1.put("latitude",  LAT_POINT_1);
        p1.put("longitude", LON_POINT_1);
        JSONObject p2 = new JSONObject();
        p2.put("latitude",  LAT_POINT_2);
        p2.put("longitude", LON_POINT_2);
        path.put(p1);
        path.put(p2);
        json.put("path", path);

        return json;
    }

    /**
     * Construit un {@link JSONObject} de course avec les blocs départ et arrivée
     * renseignés (non null).
     *
     * @return un JSONObject de course avec départ et arrivée.
     * @throws JSONException si la construction échoue.
     */
    private JSONObject creerJsonCourseAvecDepartArrivee() throws JSONException {
        JSONObject json = creerJsonCourseComplet();

        JSONObject depart = new JSONObject();
        depart.put("nom",         "Rodez Centre");
        depart.put("description", "Point de départ");
        depart.put("latitude",    LAT_POINT_1);
        depart.put("longitude",   LON_POINT_1);
        json.put("depart", depart);

        JSONObject arrivee = new JSONObject();
        arrivee.put("nom",         "Millau");
        arrivee.put("description", "Point d'arrivée");
        arrivee.put("latitude",    43.9500);
        arrivee.put("longitude",   3.0780);
        json.put("arrivee", arrivee);

        return json;
    }

    // =========================================================================
    // Tests — createCourse — Cas nominaux — champs simples
    // =========================================================================

    /**
     * Vérifie que {@code createCourse} retourne un objet {@link Course} non nul
     * pour un JSON valide et complet.
     *
     * <p><b>Given</b> : un JSON de course valide avec tous les champs requis.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : la {@link Course} retournée est non nulle.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_jsonValide_retourneCourseNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull("La Course parsée ne doit pas être nulle pour un JSON valide", result);
    }

    /**
     * Vérifie que l'identifiant MongoDB de la course est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code id = "507f1f77bcf86cd799439011"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getId()} correspond à la valeur du JSON.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_id_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertEquals("L'identifiant doit correspondre au champ 'id' du JSON",
                ID_COURSE, result.getId());
    }

    /**
     * Vérifie que l'identifiant de la randonnée associée est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code hikeId = 42}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getHikeId()} vaut 42.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_hikeId_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertEquals("Le hikeId doit correspondre au champ 'hikeId' du JSON",
                HIKE_ID, result.getHikeId());
    }

    /**
     * Vérifie que {@code isFinished} vaut {@code false} quand le JSON l'indique.
     *
     * <p><b>Given</b> : un JSON avec {@code isFinished = false}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.isFinished()} vaut {@code false}.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_isFinishedFalse_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertFalse("isFinished doit valoir false comme dans le JSON", result.isFinished());
    }

    /**
     * Vérifie que {@code isFinished} vaut {@code true} quand le JSON l'indique.
     *
     * <p><b>Given</b> : un JSON avec {@code isFinished = true}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.isFinished()} vaut {@code true}.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_isFinishedTrue_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.put("isFinished", true);

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertTrue("isFinished doit valoir true quand le JSON le précise", result.isFinished());
    }

    /**
     * Vérifie que {@code isPaused} vaut {@code false} quand le JSON l'indique.
     *
     * <p><b>Given</b> : un JSON avec {@code isPaused = false}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.isPaused()} vaut {@code false}.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_isPausedFalse_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertFalse("isPaused doit valoir false comme dans le JSON", result.isPaused());
    }

    /**
     * Vérifie que {@code isPaused} vaut {@code true} quand le JSON l'indique.
     *
     * <p><b>Given</b> : un JSON avec {@code isPaused = true}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.isPaused()} vaut {@code true}.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_isPausedTrue_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.put("isPaused", true);

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertTrue("isPaused doit valoir true quand le JSON le précise", result.isPaused());
    }

    // =========================================================================
    // Tests — createCourse — Champ dateRealisation
    // =========================================================================

    /**
     * Vérifie que la date de réalisation est correctement parsée depuis le format
     * ISO-8601 avec millisecondes.
     *
     * <p><b>Given</b> : un JSON avec {@code dateRealisation = "2026-02-05T08:19:12.027"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getDateRealisation()} est non nulle et les
     * composantes année, mois, jour, heure et minute sont correctes.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_dateRealisation_estCorrectementParsee() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNotNull("La date de réalisation ne doit pas être nulle", result.getDateRealisation());
        assertEquals("L'année doit être 2026",  2026, result.getDateRealisation().getYear());
        assertEquals("Le mois doit être 2",        2, result.getDateRealisation().getMonthValue());
        assertEquals("Le jour doit être 5",         5, result.getDateRealisation().getDayOfMonth());
        assertEquals("L'heure doit être 8",         8, result.getDateRealisation().getHour());
        assertEquals("Les minutes doivent être 19", 19, result.getDateRealisation().getMinute());
    }

    /**
     * Vérifie que la date de réalisation est nulle quand le champ est absent du JSON.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code dateRealisation}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getDateRealisation()} est nulle.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_dateRealisationAbsente_dateEstNull() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("dateRealisation");

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNull("La date doit être null si absente du JSON", result.getDateRealisation());
    }

    /**
     * Vérifie que la date de réalisation est nulle quand le champ est une chaîne vide.
     *
     * <p><b>Given</b> : un JSON avec {@code dateRealisation = ""}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getDateRealisation()} est nulle.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_dateRealisationVide_dateEstNull() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.put("dateRealisation", "");

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNull("Une date vide doit produire une dateRealisation null",
                result.getDateRealisation());
    }

    // =========================================================================
    // Tests — createCourse — Champs depart et arrivee (optionnels)
    // =========================================================================

    /**
     * Vérifie que départ et arrivée sont nuls lorsqu'ils valent
     * {@code JSONObject.NULL} dans le JSON.
     *
     * <p><b>Given</b> : un JSON avec {@code "depart": null} et {@code "arrivee": null}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getDepart()} et {@code course.getArrivee()} sont nuls.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_departEtArriveeNull_sontNulsDansCourse() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNull("Le départ null dans le JSON doit produire getDepart() == null",
                result.getDepart());
        assertNull("L'arrivée null dans le JSON doit produire getArrivee() == null",
                result.getArrivee());
    }

    /**
     * Vérifie que le départ est non nul quand le JSON contient un bloc départ valide.
     *
     * <p><b>Given</b> : un JSON avec un objet {@code "depart"} valide.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getDepart()} est non nul.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_departValide_retourneDepartNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseAvecDepartArrivee();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNotNull("Le départ doit être non nul quand le JSON contient un objet départ valide",
                result.getDepart());
    }

    /**
     * Vérifie que l'arrivée est non nulle quand le JSON contient un bloc arrivée valide.
     *
     * <p><b>Given</b> : un JSON avec un objet {@code "arrivee"} valide.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getArrivee()} est non nulle.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_arriveeValide_retourneArriveeNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseAvecDepartArrivee();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNotNull("L'arrivée doit être non nulle quand le JSON contient un objet arrivée valide",
                result.getArrivee());
    }

    // =========================================================================
    // Tests — createCourse — Champ path (trajet GPS)
    // =========================================================================

    /**
     * Vérifie que le trajet contient le bon nombre de points GPS.
     *
     * <p><b>Given</b> : un JSON avec un tableau {@code path} de deux points GPS.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getTrajetsRealises()} contient exactement 2 éléments.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_path_contientLeNombreCorrectDePoints() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNotNull("Le trajet ne doit pas être null", result.getTrajetsRealises());
        assertEquals("Le trajet doit contenir 2 points GPS",
                2, result.getTrajetsRealises().size());
    }

    /**
     * Vérifie que les coordonnées du premier point du trajet sont correctes.
     *
     * <p><b>Given</b> : un JSON avec le premier point en
     * ({@value #LAT_POINT_1}, {@value #LON_POINT_1}).</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : le premier {@link GeoCoordinate} correspond aux coordonnées attendues.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_path_premierPointEstCorrect() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        GeoCoordinate p1 = (GeoCoordinate) result.getTrajetsRealises().get(0);
        assertEquals("Latitude du 1er point incorrecte",  LAT_POINT_1, p1.getLatitude(),  0.0001);
        assertEquals("Longitude du 1er point incorrecte", LON_POINT_1, p1.getLongitude(), 0.0001);
    }

    /**
     * Vérifie que les coordonnées du deuxième point du trajet sont correctes.
     *
     * <p><b>Given</b> : un JSON avec le deuxième point en
     * ({@value #LAT_POINT_2}, {@value #LON_POINT_2}).</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : le deuxième {@link GeoCoordinate} correspond aux coordonnées attendues.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_path_deuxiemePointEstCorrect() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        GeoCoordinate p2 = (GeoCoordinate) result.getTrajetsRealises().get(1);
        assertEquals("Latitude du 2e point incorrecte",  LAT_POINT_2, p2.getLatitude(),  0.0001);
        assertEquals("Longitude du 2e point incorrecte", LON_POINT_2, p2.getLongitude(), 0.0001);
    }

    /**
     * Vérifie que le trajet est une liste vide quand le champ {@code path} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code path}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getTrajetsRealises()} est une liste vide non nulle.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_pathAbsent_retourneListeVide() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("path");

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertNotNull("getTrajetsRealises() ne doit pas être null quand path est absent",
                result.getTrajetsRealises());
        assertTrue("Le trajet doit être vide si path est absent du JSON",
                result.getTrajetsRealises().isEmpty());
    }

    /**
     * Vérifie que le trajet est une liste vide quand le tableau {@code path} est vide.
     *
     * <p><b>Given</b> : un JSON avec {@code "path": []}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getTrajetsRealises()} est vide.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_pathTableauVide_retourneListeVide() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.put("path", new JSONArray());

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertTrue("Un tableau path vide doit produire un trajet vide",
                result.getTrajetsRealises().isEmpty());
    }

    /**
     * Vérifie que le trajet peut contenir un seul point GPS (valeur limite).
     *
     * <p><b>Given</b> : un JSON avec un tableau {@code path} d'un seul point.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : {@code course.getTrajetsRealises()} contient exactement 1 élément.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void createCourse_pathUnSeulPoint_retourneListeAvecUnElement() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        JSONArray pathUnPoint = new JSONArray();
        JSONObject point = new JSONObject();
        point.put("latitude",  LAT_POINT_1);
        point.put("longitude", LON_POINT_1);
        pathUnPoint.put(point);
        json.put("path", pathUnPoint);

        // When
        Course result = ServiceParcours.createCourse(json);

        // Then
        assertNotNull(result);
        assertEquals("Un path à 1 point doit produire une liste de taille 1",
                1, result.getTrajetsRealises().size());
    }

    // =========================================================================
    // Tests — createCourse — Cas d'erreur (champs obligatoires manquants)
    // =========================================================================

    /**
     * Vérifie que {@code createCourse} lève une {@link JSONException} quand le
     * champ obligatoire {@code "id"} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "id"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : une {@link JSONException} est levée.</p>
     *
     * @throws JSONException attendue — le test réussit si elle est levée.
     */
    @Test(expected = JSONException.class)
    public void createCourse_champIdAbsent_leveJSONException() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("id");

        // When — doit lever JSONException
        ServiceParcours.createCourse(json);

        // Then — vérifié par @Test(expected = JSONException.class)
    }

    /**
     * Vérifie que {@code createCourse} lève une {@link JSONException} quand le
     * champ obligatoire {@code "isFinished"} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "isFinished"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : une {@link JSONException} est levée.</p>
     *
     * @throws JSONException attendue — le test réussit si elle est levée.
     */
    @Test(expected = JSONException.class)
    public void createCourse_champIsFinishedAbsent_leveJSONException() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("isFinished");

        // When — doit lever JSONException
        ServiceParcours.createCourse(json);

        // Then — vérifié par @Test(expected = JSONException.class)
    }

    /**
     * Vérifie que {@code createCourse} lève une {@link JSONException} quand le
     * champ obligatoire {@code "hikeId"} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "hikeId"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : une {@link JSONException} est levée.</p>
     *
     * @throws JSONException attendue — le test réussit si elle est levée.
     */
    @Test(expected = JSONException.class)
    public void createCourse_champHikeIdAbsent_leveJSONException() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("hikeId");

        // When — doit lever JSONException
        ServiceParcours.createCourse(json);

        // Then — vérifié par @Test(expected = JSONException.class)
    }

    /**
     * Vérifie que {@code createCourse} lève une {@link JSONException} quand le
     * champ obligatoire {@code "isPaused"} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "isPaused"}.</p>
     * <p><b>When</b> : on appelle {@code createCourse(json)}.</p>
     * <p><b>Then</b> : une {@link JSONException} est levée.</p>
     *
     * @throws JSONException attendue — le test réussit si elle est levée.
     */
    @Test(expected = JSONException.class)
    public void createCourse_champIsPausedAbsent_leveJSONException() throws JSONException {
        // Given
        JSONObject json = creerJsonCourseComplet();
        json.remove("isPaused");

        // When — doit lever JSONException
        ServiceParcours.createCourse(json);

        // Then — vérifié par @Test(expected = JSONException.class)
    }

    // =========================================================================
    // Tests — parseCoursesFromJSON — Cas nominaux et limites
    // =========================================================================

    /**
     * Vérifie que le parsing d'un tableau JSON {@code null} retourne une liste
     * vide non nulle.
     *
     * <p><b>Given</b> : un tableau JSON {@code null}.</p>
     * <p><b>When</b> : on appelle {@code parseCoursesFromJSON(null)}.</p>
     * <p><b>Then</b> : le résultat est une liste vide non nulle.</p>
     */
    @Test
    public void parseCoursesFromJSON_jsonNull_retourneListeVide() {
        // Given
        JSONArray tableauNull = null;

        // When
        ArrayList<Course> result = ServiceParcours.parseCoursesFromJSON(tableauNull);

        // Then
        assertNotNull("La liste ne doit pas être null pour un tableau null", result);
        assertTrue("La liste doit être vide pour un tableau null", result.isEmpty());
    }

    /**
     * Vérifie que le parsing d'un tableau JSON vide retourne une liste vide.
     *
     * <p><b>Given</b> : un tableau JSON vide {@code []}.</p>
     * <p><b>When</b> : on appelle {@code parseCoursesFromJSON(jsonArray)}.</p>
     * <p><b>Then</b> : le résultat est une liste vide.</p>
     */
    @Test
    public void parseCoursesFromJSON_tableauVide_retourneListeVide() {
        // Given
        JSONArray tableauVide = new JSONArray();

        // When
        ArrayList<Course> result = ServiceParcours.parseCoursesFromJSON(tableauVide);

        // Then
        assertNotNull(result);
        assertTrue("La liste doit être vide pour un tableau JSON vide", result.isEmpty());
    }

    /**
     * Vérifie que le parsing d'un tableau JSON avec un seul élément retourne
     * une liste de taille 1.
     *
     * <p><b>Given</b> : un tableau JSON contenant une course valide.</p>
     * <p><b>When</b> : on appelle {@code parseCoursesFromJSON(jsonArray)}.</p>
     * <p><b>Then</b> : la liste contient exactement 1 {@link Course}.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void parseCoursesFromJSON_unElement_retourneListeAvecUnElement() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        tableau.put(creerJsonCourseComplet());

        // When
        ArrayList<Course> result = ServiceParcours.parseCoursesFromJSON(tableau);

        // Then
        assertNotNull(result);
        assertEquals("La liste doit contenir 1 course", 1, result.size());
    }

    /**
     * Vérifie que le parsing d'un tableau JSON avec 3 éléments retourne une liste
     * de taille 3.
     *
     * <p><b>Given</b> : un tableau JSON contenant 3 courses valides avec des ids distincts.</p>
     * <p><b>When</b> : on appelle {@code parseCoursesFromJSON(jsonArray)}.</p>
     * <p><b>Then</b> : la liste contient exactement 3 {@link Course}.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void parseCoursesFromJSON_troisElements_retourneListeDeTroisCourses() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        for (int i = 0; i < 3; i++) {
            JSONObject json = creerJsonCourseComplet();
            json.put("id", "course_id_" + i);
            tableau.put(json);
        }

        // When
        ArrayList<Course> result = ServiceParcours.parseCoursesFromJSON(tableau);

        // Then
        assertNotNull(result);
        assertEquals("La liste doit contenir 3 courses", 3, result.size());
    }

    /**
     * Vérifie que les identifiants des courses parsées correspondent aux valeurs
     * présentes dans le tableau JSON, dans l'ordre.
     *
     * <p><b>Given</b> : un tableau JSON avec deux courses ayant des identifiants distincts.</p>
     * <p><b>When</b> : on appelle {@code parseCoursesFromJSON(jsonArray)}.</p>
     * <p><b>Then</b> : les identifiants sont corrects et dans le bon ordre.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void parseCoursesFromJSON_identifiantsCorrects_dansChaqueElement() throws JSONException {
        // Given
        String id1 = "aaa111";
        String id2 = "bbb222";
        JSONObject json1 = creerJsonCourseComplet();
        json1.put("id", id1);
        JSONObject json2 = creerJsonCourseComplet();
        json2.put("id", id2);
        JSONArray tableau = new JSONArray();
        tableau.put(json1);
        tableau.put(json2);

        // When
        ArrayList<Course> result = ServiceParcours.parseCoursesFromJSON(tableau);

        // Then
        assertNotNull(result);
        assertEquals("Le premier identifiant doit correspondre",  id1, result.get(0).getId());
        assertEquals("Le deuxième identifiant doit correspondre", id2, result.get(1).getId());
    }

    // =========================================================================
    // Tests — Contrat des interfaces callbacks
    // =========================================================================

    /**
     * Vérifie que {@link ServiceParcours.ParcoursCallback#onSuccess} est bien
     * invocable avec une liste de parcours non vide.
     *
     * <p><b>Given</b> : une liste contenant une course.</p>
     * <p><b>When</b> : on appelle {@code onSuccess} sur le mock.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte.</p>
     */
    @Test
    public void parcoursCallback_onSuccess_estAppeleAvecListe() {
        // Given
        ArrayList<Course> liste = new ArrayList<>();
        Course c = new Course();
        c.setId(ID_COURSE);
        liste.add(c);

        // When
        mockParcoursCallback.onSuccess(liste);

        // Then
        verify(mockParcoursCallback, times(1)).onSuccess(liste);
    }

    /**
     * Vérifie que {@link ServiceParcours.ParcoursCallback#onError} est bien
     * invocable avec une {@link VolleyError}.
     *
     * <p><b>Given</b> : une {@link VolleyError} simulant un timeout.</p>
     * <p><b>When</b> : on appelle {@code onError} sur le mock.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte.</p>
     */
    @Test
    public void parcoursCallback_onError_estAppeleAvecVolleyError() {
        // Given
        VolleyError erreur = new VolleyError("Timeout réseau");

        // When
        mockParcoursCallback.onError(erreur);

        // Then
        verify(mockParcoursCallback, times(1)).onError(erreur);
    }

    /**
     * Vérifie que {@link ServiceParcours.CourseCreationCallback#onSuccess} est bien
     * invocable avec un objet {@link Course}.
     *
     * <p><b>Given</b> : un objet {@link Course} avec un identifiant.</p>
     * <p><b>When</b> : on appelle {@code onSuccess} sur le mock.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation avec l'objet exact.</p>
     */
    @Test
    public void creationCallback_onSuccess_estAppeleAvecCourse() {
        // Given
        Course course = new Course();
        course.setId(ID_COURSE);

        // When
        mockCreationCallback.onSuccess(course);

        // Then
        verify(mockCreationCallback, times(1)).onSuccess(course);
    }

    /**
     * Vérifie que {@link ServiceParcours.CourseCreationCallback#onError} est bien
     * invocable avec une {@link VolleyError}.
     *
     * <p><b>Given</b> : une {@link VolleyError} simulant une erreur serveur 500.</p>
     * <p><b>When</b> : on appelle {@code onError} sur le mock.</p>
     * <p><b>Then</b> : Mockito vérifie l'invocation exacte.</p>
     */
    @Test
    public void creationCallback_onError_estAppeleAvecVolleyError() {
        // Given
        VolleyError erreur = new VolleyError("500 Internal Server Error");

        // When
        mockCreationCallback.onError(erreur);

        // Then
        verify(mockCreationCallback, times(1)).onError(erreur);
    }
}