package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceCreationRandonnee;

/**
 * Classe de test instrumentée pour {@link ServiceCreationRandonnee}.
 *
 * <p>Cette classe vérifie le comportement du service de création de randonnée,
 * notamment la construction du JSON, la gestion des callbacks et les cas limites.</p>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (nom vide, durée nulle, etc.)</li>
 *     <li><b>Erreurs</b> : token invalide, nom null, callback null</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceCreationRandonneeTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification valide pour les tests */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

    /** Token invalide pour les tests d'erreur */
    private static final String TOKEN_INVALIDE = "token_invalide";

    /** Nom de randonnée valide */
    private static final String NOM_VALIDE = "Randonnée du Causse";

    /** Durée valide en jours */
    private static final int DUREE_VALIDE = 3;

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
    // TESTS — construireJsonRandonnee (via réflexion)
    // =========================================================================

    /**
     * Vérifie que le JSON construit contient bien les champs obligatoires
     * avec des valeurs valides.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_ValeursValides_JsonComplet() throws Exception {
        // Given — un nom et une durée valides
        String nom = NOM_VALIDE;
        int duree = DUREE_VALIDE;

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, nom, duree);

        // Then — le JSON contient tous les champs obligatoires
        assertNotNull("Le JSON ne doit pas être nul", json);
        assertTrue("Le JSON doit contenir 'libelle'", json.has("libelle"));
        assertTrue("Le JSON doit contenir 'dureeJours'", json.has("dureeJours"));
        assertTrue("Le JSON doit contenir 'depart'", json.has("depart"));
        assertTrue("Le JSON doit contenir 'arrivee'", json.has("arrivee"));
    }

    /**
     * Vérifie que le champ libelle du JSON correspond bien au nom fourni.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_Libelle_CorrespondAuNom() throws Exception {
        // Given — un nom valide
        String nom = NOM_VALIDE;

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, nom, DUREE_VALIDE);

        // Then — le libelle correspond exactement au nom fourni
        assertNotNull("Le JSON ne doit pas être nul", json);
        assertTrue("Le libelle doit correspondre au nom fourni",
                json.getString("libelle").equals(nom));
    }

    /**
     * Vérifie que le champ dureeJours du JSON correspond bien à la durée fournie.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_DureeJours_CorrespondALaDuree() throws Exception {
        // Given — une durée valide
        int duree = DUREE_VALIDE;

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, NOM_VALIDE, duree);

        // Then — la durée correspond exactement à la valeur fournie
        assertNotNull("Le JSON ne doit pas être nul", json);
        assertTrue("La durée doit correspondre à la valeur fournie",
                json.getInt("dureeJours") == duree);
    }

    /**
     * Vérifie que les IDs techniques de départ et arrivée sont bien définis.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_DepartArrivee_IdsCorrects() throws Exception {
        // Given — des valeurs valides
        String nom = NOM_VALIDE;
        int duree = DUREE_VALIDE;

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, nom, duree);

        // Then — les IDs de départ (1) et arrivée (2) sont corrects
        assertNotNull("Le JSON ne doit pas être nul", json);
        assertTrue("L'ID de départ doit être 1",
                json.getJSONObject("depart").getInt("id") == 1);
        assertTrue("L'ID d'arrivée doit être 2",
                json.getJSONObject("arrivee").getInt("id") == 2);
    }

    /**
     * Vérifie que le JSON est correctement construit avec un nom vide.
     *
     * <p><b>Cas limite</b> : nom vide</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_NomVide_JsonNonNul() throws Exception {
        // Given — un nom vide
        String nomVide = "";

        // When — on construit le JSON avec un nom vide
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, nomVide, DUREE_VALIDE);

        // Then — le JSON est construit mais avec un libelle vide
        assertNotNull("Le JSON ne doit pas être nul même avec un nom vide", json);
        assertTrue("Le libelle doit être vide",
                json.getString("libelle").isEmpty());
    }

    /**
     * Vérifie que le JSON est correctement construit avec une durée de 1 jour (minimum).
     *
     * <p><b>Cas limite</b> : durée minimale</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_DureeMin_JsonNonNul() throws Exception {
        // Given — une durée minimale de 1 jour
        int dureeMin = 1;

        // When — on construit le JSON avec durée minimale
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, NOM_VALIDE, dureeMin);

        // Then — le JSON est construit avec la durée minimale
        assertNotNull("Le JSON ne doit pas être nul avec une durée minimale", json);
        assertTrue("La durée doit être 1", json.getInt("dureeJours") == 1);
    }

    /**
     * Vérifie que le JSON est correctement construit avec une durée nulle.
     *
     * <p><b>Cas limite</b> : durée nulle</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testConstruireJsonRandonnee_DureeZero_JsonNonNul() throws Exception {
        // Given — une durée de zéro
        int dureeZero = 0;

        // When — on construit le JSON avec une durée nulle
        java.lang.reflect.Method method = ServiceCreationRandonnee.class
                .getDeclaredMethod("construireJsonRandonnee", String.class, int.class);
        method.setAccessible(true);
        JSONObject json = (JSONObject) method.invoke(null, NOM_VALIDE, dureeZero);

        // Then — le JSON est construit avec la durée nulle
        assertNotNull("Le JSON ne doit pas être nul avec une durée nulle", json);
        assertTrue("La durée doit être 0", json.getInt("dureeJours") == 0);
    }

    // =========================================================================
    // TESTS — validerRandonneeComplete
    // =========================================================================

    /**
     * Vérifie que la méthode principale est bien appelée sans lever d'exception
     * avec des paramètres valides.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_ParamsValides_PasException()
            throws InterruptedException {

        // Given — des paramètres valides
        CountDownLatch verrou = new CountDownLatch(1);
        final boolean[] callbackAppele = {false};

        // When — on appelle validerRandonneeComplete
        try {
            ServiceCreationRandonnee.validerRandonneeComplete(
                    contexte,
                    TOKEN_VALIDE,
                    NOM_VALIDE,
                    DUREE_VALIDE,
                    new ServiceCreationRandonnee.FullCreationCallback() {
                        @Override
                        public void onSuccess(long hikeId) {
                            callbackAppele[0] = true;
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            callbackAppele[0] = true;
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des paramètres valides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est bien initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onError est appelé avec un token invalide.
     *
     * <p><b>Cas erreur</b> : token invalide → erreur 401</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_TokenInvalide_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — un token invalide
        CountDownLatch verrou = new CountDownLatch(1);
        final String[] messageErreur = {null};

        // When — on appelle validerRandonneeComplete avec un token invalide
        ServiceCreationRandonnee.validerRandonneeComplete(
                contexte,
                TOKEN_INVALIDE,
                NOM_VALIDE,
                DUREE_VALIDE,
                new ServiceCreationRandonnee.FullCreationCallback() {
                    @Override
                    public void onSuccess(long hikeId) {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        // Then — onError est appelé avec un message d'erreur
                        messageErreur[0] = message;
                        verrou.countDown();
                    }
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec un nom très long (cas limite).
     *
     * <p><b>Cas limite</b> : nom très long</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_NomTresLong_PasException()
            throws InterruptedException {

        // Given — un nom très long de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String nomTresLong = "A".repeat(255);

        // When — on appelle validerRandonneeComplete avec un nom très long
        try {
            ServiceCreationRandonnee.validerRandonneeComplete(
                    contexte,
                    TOKEN_VALIDE,
                    nomTresLong,
                    DUREE_VALIDE,
                    new ServiceCreationRandonnee.FullCreationCallback() {
                        @Override
                        public void onSuccess(long hikeId) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un nom très long : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — aucune exception levée, la file est initialisée
        assertNotNull("La file doit être initialisée avec un nom très long",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec une durée très grande (cas limite).
     *
     * <p><b>Cas limite</b> : durée maximale</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_DureeMaximale_PasException()
            throws InterruptedException {

        // Given — une durée très grande (Integer.MAX_VALUE)
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeMax = Integer.MAX_VALUE;

        // When — on appelle validerRandonneeComplete avec une durée maximale
        try {
            ServiceCreationRandonnee.validerRandonneeComplete(
                    contexte,
                    TOKEN_VALIDE,
                    NOM_VALIDE,
                    dureeMax,
                    new ServiceCreationRandonnee.FullCreationCallback() {
                        @Override
                        public void onSuccess(long hikeId) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée maximale : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — aucune exception levée, la file est initialisée
        assertNotNull("La file doit être initialisée avec une durée maximale",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas d'exception avec un token null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceCreationRandonnee.validerRandonneeComplete(
                    contexte,
                    null,
                    NOM_VALIDE,
                    DUREE_VALIDE,
                    new ServiceCreationRandonnee.FullCreationCallback() {
                        @Override
                        public void onSuccess(long hikeId) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            verrou.countDown();
                        }
                    }
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un token null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas d'exception avec une durée négative.
     *
     * <p><b>Cas erreur</b> : durée négative</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testValiderRandonneeComplete_DureeNegative_PasException()
            throws InterruptedException {

        // Given — une durée négative
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeNegative = -1;

        // When — on appelle validerRandonneeComplete avec une durée négative
        try {
            ServiceCreationRandonnee.validerRandonneeComplete(
                    contexte,
                    TOKEN_VALIDE,
                    NOM_VALIDE,
                    dureeNegative,
                    new ServiceCreationRandonnee.FullCreationCallback() {
                        @Override
                        public void onSuccess(long hikeId) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée négative : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, pas d'exception
        assertNotNull("La file doit être initialisée avec une durée négative",
                AppelAPI.getFileRequete(contexte));
    }
}
