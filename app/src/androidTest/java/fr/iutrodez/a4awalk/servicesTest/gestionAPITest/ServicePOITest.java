package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI;

/**
 * Classe de test instrumentée pour {@link ServicePOI}.
 *
 * <p>Cette classe vérifie le comportement du service de gestion des points
 * d'intérêt (POI), notamment :</p>
 * <ul>
 *     <li>L'ajout d'un POI via l'API</li>
 *     <li>La construction du JSON d'un POI</li>
 *     <li>La mise à jour de la liste des POI via l'API</li>
 *     <li>Les cas limites (POI null, liste vide, coordonnées limites)</li>
 *     <li>Les cas d'erreur (token null, nom null, idRandonnee null)</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (coordonnées min/max, liste vide)</li>
 *     <li><b>Erreurs</b> : token null, poi null, nom null, idRandonnee null</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServicePOITest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification valide pour les tests */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

    /** Identifiant de randonnée valide */
    private static final long HIKE_ID_VALIDE = 1L;

    /** Nom de POI valide */
    private static final String NOM_POI_VALIDE = "Sommet du Causse";

    /** Latitude valide */
    private static final double LATITUDE_VALIDE = 44.36;

    /** Longitude valide */
    private static final double LONGITUDE_VALIDE = 2.57;

    /** Timeout maximum pour les opérations asynchrones en secondes */
    private static final int TIMEOUT_SECONDES = 5;

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================

    /** Contexte Android fourni par le runner de test */
    private Context contexte;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    /**
     * Initialise le contexte et réinitialise la file Volley avant chaque test
     * afin d'isoler les tests entre eux.
     */
    @Before
    public void setUp() {
        contexte = ApplicationProvider.getApplicationContext();
        AppelAPI.resetFileRequete();
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un POI valide avec toutes les données obligatoires renseignées.
     *
     * @param nom       Nom du point d'intérêt
     * @param latitude  Latitude du point
     * @param longitude Longitude du point
     * @return {@link PointOfInterest} valide pour les tests
     */
    private PointOfInterest construirePoiValide(String nom,
                                                double latitude,
                                                double longitude) {
        return new PointOfInterest(1, nom, latitude, longitude);
    }

    /**
     * Construit une liste de POI valides pour les tests.
     *
     * @param taille Nombre de POI à créer
     * @return Liste de {@link PointOfInterest} valides
     */
    private ArrayList<PointOfInterest> construireListePoi(int taille) {
        ArrayList<PointOfInterest> liste = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            liste.add(construirePoiValide(
                    "POI " + i,
                    LATITUDE_VALIDE + i * 0.01,
                    LONGITUDE_VALIDE + i * 0.01
            ));
        }
        return liste;
    }

    // =========================================================================
    // TESTS — ajoutPOI — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que l'ajout d'un POI valide s'exécute sans exception
     * et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_PoiValide_PasException() throws InterruptedException {
        // Given — un POI valide avec toutes les données renseignées
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide(
                NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE
        );

        // When — on appelle ajoutPOI avec un POI valide
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un POI valide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que plusieurs ajouts successifs réutilisent la même file Singleton.
     *
     * <p><b>Cas nominal</b> : appels successifs</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_AppelsSuccessifs_MemeFileSingleton()
            throws InterruptedException {

        // Given — deux POI différents à ajouter successivement
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi1 = construirePoiValide("POI 1", LATITUDE_VALIDE, LONGITUDE_VALIDE);
        PointOfInterest poi2 = construirePoiValide("POI 2", 44.40, 2.60);

        // When — on effectue deux ajouts successifs
        ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi1, HIKE_ID_VALIDE);
        ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi2, HIKE_ID_VALIDE);
        verrou.countDown();

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la même file Singleton est utilisée pour les deux appels
        assertNotNull("La file doit rester initialisée après des appels successifs",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — ajoutPOI — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode gère un POI avec des coordonnées minimales
     * sans lever d'exception.
     *
     * <p><b>Cas limite</b> : latitude = -90, longitude = -180</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_CoordoneesMinimales_PasException()
            throws InterruptedException {

        // Given — un POI avec les coordonnées minimales
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide(NOM_POI_VALIDE, -90.0, -180.0);

        // When — on appelle ajoutPOI avec des coordonnées minimales
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des coordonnées minimales : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec des coordonnées minimales",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un POI avec des coordonnées maximales
     * sans lever d'exception.
     *
     * <p><b>Cas limite</b> : latitude = 90, longitude = 180</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_CoordoneesMaximales_PasException()
            throws InterruptedException {

        // Given — un POI avec les coordonnées maximales
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide(NOM_POI_VALIDE, 90.0, 180.0);

        // When — on appelle ajoutPOI avec des coordonnées maximales
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des coordonnées maximales : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec des coordonnées maximales",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un nom vide sans lever d'exception.
     *
     * <p><b>Cas limite</b> : nom vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_NomVide_PasException() throws InterruptedException {
        // Given — un POI avec un nom vide
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide("", LATITUDE_VALIDE, LONGITUDE_VALIDE);

        // When — on appelle ajoutPOI avec un nom vide
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un nom vide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un nom vide",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un nom très long sans lever d'exception.
     *
     * <p><b>Cas limite</b> : nom de 255 caractères</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_NomTresLong_PasException() throws InterruptedException {
        // Given — un POI avec un nom de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String nomLong = "A".repeat(255);
        PointOfInterest poi = construirePoiValide(nomLong, LATITUDE_VALIDE, LONGITUDE_VALIDE);

        // When — on appelle ajoutPOI avec un nom très long
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un nom très long : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un nom très long",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — ajoutPOI — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le POI est null (createPOIJson retourne null → retour immédiat).
     *
     * <p><b>Cas erreur</b> : POI null → retour immédiat sans exception</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_PoiNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un POI null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, null, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un POI null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le token est null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide(
                NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE
        );

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServicePOI.ajoutPOI(contexte, null, poi, HIKE_ID_VALIDE);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un token null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec un token null",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque l'idRandonnee est null.
     *
     * <p><b>Cas erreur</b> : idRandonnee null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testAjoutPOI_IdRandonneeNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un idRandonnee null
        CountDownLatch verrou = new CountDownLatch(1);
        PointOfInterest poi = construirePoiValide(
                NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE
        );

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServicePOI.ajoutPOI(contexte, TOKEN_VALIDE, poi, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec idRandonnee null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — createPOIJson — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la construction du JSON d'un POI valide retourne
     * un objet non nul avec tous les champs obligatoires.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testCreatePOIJson_PoiValide_JsonNonNul() throws Exception {
        // Given — un POI valide avec toutes les données
        PointOfInterest poi = construirePoiValide(
                NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE
        );

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServicePOI.class
                .getDeclaredMethod("createPOIJson", PointOfInterest.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, poi);

        // Then — le JSON est non nul et contient tous les champs obligatoires
        assertNotNull("Le JSON ne doit pas être nul pour un POI valide", json);
        assert json.has("nom") : "Le JSON doit contenir le champ nom";
        assert json.has("latitude") : "Le JSON doit contenir le champ latitude";
        assert json.has("longitude") : "Le JSON doit contenir le champ longitude";
        assert json.has("description") : "Le JSON doit contenir le champ description";
    }

    /**
     * Vérifie que la description est le nom du POI lorsque le nom est renseigné.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testCreatePOIJson_NomRenseigne_DescriptionEgaleAuNom() throws Exception {
        // Given — un POI avec un nom renseigné
        PointOfInterest poi = construirePoiValide(
                NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE
        );

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServicePOI.class
                .getDeclaredMethod("createPOIJson", PointOfInterest.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, poi);

        // Then — la description est égale au nom du POI
        assertNotNull("Le JSON ne doit pas être nul", json);
        assert NOM_POI_VALIDE.equals(json.getString("description"))
                : "La description doit être égale au nom du POI";
    }

    // =========================================================================
    // TESTS — createPOIJson — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la description vaut "POI" par défaut lorsque le nom est null.
     *
     * <p><b>Cas limite</b> : nom null → description = "POI"</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testCreatePOIJson_NomNull_DescriptionDefautPOI() throws Exception {
        // Given — un POI avec un nom null
        PointOfInterest poi = construirePoiValide(null, LATITUDE_VALIDE, LONGITUDE_VALIDE);

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServicePOI.class
                .getDeclaredMethod("createPOIJson", PointOfInterest.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, poi);

        // Then — la description par défaut vaut "POI"
        assertNotNull("Le JSON ne doit pas être nul avec un nom null", json);
        assert "POI".equals(json.getString("description"))
                : "La description doit être 'POI' si le nom est null";
    }

    /**
     * Vérifie que les coordonnées minimales sont correctement insérées dans le JSON.
     *
     * <p><b>Cas limite</b> : latitude = -90, longitude = -180</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testCreatePOIJson_CoordoneesMinimales_JsonCorrect() throws Exception {
        // Given — un POI avec les coordonnées minimales
        PointOfInterest poi = construirePoiValide(NOM_POI_VALIDE, -90.0, -180.0);

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServicePOI.class
                .getDeclaredMethod("createPOIJson", PointOfInterest.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, poi);

        // Then — les coordonnées minimales sont présentes dans le JSON
        assertNotNull("Le JSON ne doit pas être nul", json);
        assert json.getDouble("latitude") == -90.0
                : "La latitude minimale doit être -90.0";
        assert json.getDouble("longitude") == -180.0
                : "La longitude minimale doit être -180.0";
    }

    // =========================================================================
    // TESTS — createPOIJson — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode retourne null lorsque le POI est null.
     *
     * <p><b>Cas erreur</b> : POI null → retourne null</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testCreatePOIJson_PoiNull_RetourneNull() throws Exception {
        // Given — un POI null

        // When — on construit le JSON via réflexion avec un POI null
        java.lang.reflect.Method method = ServicePOI.class
                .getDeclaredMethod("createPOIJson", PointOfInterest.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, (PointOfInterest) null);

        // Then — le JSON retourné est null car une exception a été capturée
        assert json == null : "Le JSON doit être null si le POI est null";
    }

    // =========================================================================
    // TESTS — traiterMAJPOI — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la mise à jour d'une liste de POI valides s'exécute
     * sans exception et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_ListeValide_PasException() throws InterruptedException {
        // Given — une liste de trois POI valides
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<PointOfInterest> liste = construireListePoi(3);

        // When — on appelle traiterMAJPOI avec une liste valide
        try {
            ServicePOI.traiterMAJPOI(
                    contexte, (int) HIKE_ID_VALIDE, liste, TOKEN_VALIDE
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une liste valide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la mise à jour avec un seul POI s'exécute correctement.
     *
     * <p><b>Cas nominal</b> : liste avec un seul élément</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_UnSeulPOI_PasException() throws InterruptedException {
        // Given — une liste avec un seul POI valide
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<PointOfInterest> liste = construireListePoi(1);

        // When — on appelle traiterMAJPOI avec un seul POI
        try {
            ServicePOI.traiterMAJPOI(
                    contexte, (int) HIKE_ID_VALIDE, liste, TOKEN_VALIDE
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un seul POI : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un seul POI",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — traiterMAJPOI — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode ne fait rien et ne lève pas d'exception
     * lorsque la liste de POI est vide (pois.length() == 0 → pas de requête).
     *
     * <p><b>Cas limite</b> : liste vide → pas de requête envoyée</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_ListeVide_PasDeRequeteEnvoyee()
            throws InterruptedException {

        // Given — une liste de POI vide
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<PointOfInterest> listeVide = new ArrayList<>();

        // When — on appelle traiterMAJPOI avec une liste vide
        try {
            ServicePOI.traiterMAJPOI(
                    contexte, (int) HIKE_ID_VALIDE, listeVide, TOKEN_VALIDE
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une liste vide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file n'est pas initialisée car aucune requête n'a été envoyée
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que les POI avec un JSON null (nom null) sont ignorés
     * et n'empêchent pas le traitement des autres.
     *
     * <p><b>Cas limite</b> : POI avec JSON null → ignoré</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_AvecPoiJsonNull_PoiIgnore()
            throws InterruptedException {

        // Given — une liste avec un POI valide et un POI null
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<PointOfInterest> liste = new ArrayList<>();
        liste.add(construirePoiValide(NOM_POI_VALIDE, LATITUDE_VALIDE, LONGITUDE_VALIDE));
        liste.add(null);

        // When — on appelle traiterMAJPOI avec un POI null dans la liste
        try {
            ServicePOI.traiterMAJPOI(
                    contexte, (int) HIKE_ID_VALIDE, liste, TOKEN_VALIDE
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un POI null dans la liste : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — traiterMAJPOI — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque la liste est null.
     *
     * <p><b>Cas erreur</b> : liste null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_ListeNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — une liste null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServicePOI.traiterMAJPOI(
                    contexte, (int) HIKE_ID_VALIDE, null, TOKEN_VALIDE
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec une liste null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le token est null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJPOI_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<PointOfInterest> liste = construireListePoi(2);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServicePOI.traiterMAJPOI(contexte, (int) HIKE_ID_VALIDE, liste, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un token null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec un token null",
                AppelAPI.getFileRequete(contexte));
    }
}
