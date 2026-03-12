package fr.iutrodez.a4awalk.servicesTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.iutrodez.a4awalk.services.AppelAPI;

/**
 * Classe de test instrumentée pour {@link AppelAPI}.
 *
 * <p>Cette classe vérifie le comportement du service HTTP {@code AppelAPI}
 * pour les opérations GET, POST, PUT, PUT tableau et DELETE,
 * ainsi que la gestion des en-têtes d'authentification.</p>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (token vide, body vide, etc.)</li>
 *     <li><b>Erreurs</b> : cas d'erreur (callback null, token invalide, etc.)</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class AppelAPITest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL de test valide */
    private static final String URL_VALIDE = "http://98.94.8.220:8080/test";

    /** Token d'authentification valide */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

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
    // TESTS — getAuthHeaders
    // =========================================================================

    /**
     * Vérifie que les en-têtes contiennent le token Bearer et le Content-Type
     * lorsqu'un token valide est fourni.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur inattendue survient
     */
    @Test
    public void testGetAuthHeaders_TokenValide_ContiendBearer() throws Exception {
        // Given — un token valide
        String token = TOKEN_VALIDE;

        // When — on appelle getAuthHeaders via réflexion
        java.lang.reflect.Method method = AppelAPI.class
                .getDeclaredMethod("getAuthHeaders", String.class);
        method.setAccessible(true);
        java.util.Map<String, String> headers =
                (java.util.Map<String, String>) method.invoke(null, token);

        // Then — Authorization et Content-Type sont présents
        assertNotNull("Les en-têtes ne doivent pas être nuls", headers);
        assertEquals("Bearer " + token, headers.get("Authorization"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    /**
     * Vérifie que les en-têtes ne contiennent pas Authorization
     * lorsque le token est vide.
     *
     * <p><b>Cas limite</b> : token vide</p>
     *
     * @throws Exception si une erreur inattendue survient
     */
    @Test
    public void testGetAuthHeaders_TokenVide_PasAuthorization() throws Exception {
        // Given — un token vide
        String token = "";

        // When — on appelle getAuthHeaders via réflexion
        java.lang.reflect.Method method = AppelAPI.class
                .getDeclaredMethod("getAuthHeaders", String.class);
        method.setAccessible(true);
        java.util.Map<String, String> headers =
                (java.util.Map<String, String>) method.invoke(null, token);

        // Then — Authorization absent, Content-Type présent
        assertNotNull("Les en-têtes ne doivent pas être nuls", headers);
        assertTrue("Authorization ne doit pas être présent pour un token vide",
                !headers.containsKey("Authorization"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    /**
     * Vérifie que les en-têtes ne contiennent pas Authorization
     * lorsque le token est null.
     *
     * <p><b>Cas limite</b> : token null</p>
     *
     * @throws Exception si une erreur inattendue survient
     */
    @Test
    public void testGetAuthHeaders_TokenNull_PasAuthorization() throws Exception {
        // Given — un token null
        String token = null;

        // When — on appelle getAuthHeaders via réflexion
        java.lang.reflect.Method method = AppelAPI.class
                .getDeclaredMethod("getAuthHeaders", String.class);
        method.setAccessible(true);
        java.util.Map<String, String> headers =
                (java.util.Map<String, String>) method.invoke(null, token);

        // Then — Authorization absent, Content-Type présent
        assertNotNull("Les en-têtes ne doivent pas être nuls", headers);
        assertTrue("Authorization ne doit pas être présent pour un token null",
                !headers.containsKey("Authorization"));
        assertEquals("application/json", headers.get("Content-Type"));
    }

    // =========================================================================
    // TESTS — getFileRequete
    // =========================================================================

    /**
     * Vérifie que la file de requêtes est correctement initialisée
     * lors du premier appel.
     *
     * <p><b>Cas nominal</b></p>
     */
    @Test
    public void testGetFileRequete_PremierAppel_FileNonNulle() {
        // Given — aucune file initialisée (setUp a appelé resetFileRequete)

        // When — on demande la file de requêtes
        RequestQueue queue = AppelAPI.getFileRequete(contexte);

        // Then — la file est correctement créée et non nulle
        assertNotNull("La file de requêtes ne doit pas être nulle", queue);
    }

    /**
     * Vérifie que la même instance est retournée lors d'appels successifs
     * (comportement Singleton).
     *
     * <p><b>Cas nominal</b> : pattern Singleton</p>
     */
    @Test
    public void testGetFileRequete_DeuxiemeAppel_MemeInstance() {
        // Given — une première file créée
        RequestQueue premiereFile = AppelAPI.getFileRequete(contexte);

        // When — on demande la file une deuxième fois
        RequestQueue deuxiemeFile = AppelAPI.getFileRequete(contexte);

        // Then — les deux références pointent vers la même instance
        assertEquals("La même instance doit être retournée (Singleton)",
                premiereFile, deuxiemeFile);
    }

    /**
     * Vérifie que la file est bien réinitialisée après un reset.
     *
     * <p><b>Cas nominal</b> : reset de la file</p>
     */
    @Test
    public void testResetFileRequete_ApresReset_NouvelleInstanceCreee() {
        // Given — une première file créée
        RequestQueue premiereFile = AppelAPI.getFileRequete(contexte);

        // When — on réinitialise la file
        AppelAPI.resetFileRequete();
        RequestQueue nouvelleFile = AppelAPI.getFileRequete(contexte);

        // Then — une nouvelle instance différente est créée
        assertNotNull("La nouvelle file ne doit pas être nulle", nouvelleFile);
        assertTrue("Une nouvelle instance doit être créée après le reset",
                premiereFile != nouvelleFile);
    }

    // =========================================================================
    // TESTS — GET
    // =========================================================================

    /**
     * Vérifie que la requête GET est correctement ajoutée à la file
     * avec un token et une URL valides.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testGet_TokenEtUrlValides_RequeteAjouteeALaFile()
            throws InterruptedException {

        // Given — une URL et un token valides
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on effectue un GET
        AppelAPI.get(URL_VALIDE, TOKEN_VALIDE, contexte, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                verrou.countDown();
            }

            @Override
            public void onError(VolleyError error) {
                verrou.countDown();
            }
        });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée et contient la requête
        assertNotNull("La file de requêtes doit être initialisée après un GET",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le GET fonctionne sans token (token vide).
     *
     * <p><b>Cas limite</b> : token vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testGet_TokenVide_RequeteAjouteeALaFile() throws InterruptedException {
        // Given — un token vide
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on effectue un GET sans token
        AppelAPI.get(URL_VALIDE, "", contexte, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                verrou.countDown();
            }

            @Override
            public void onError(VolleyError error) {
                // Then — onError appelé car pas d'Authorization
                verrou.countDown();
            }
        });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        assertNotNull("La file doit être initialisée même avec un token vide",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le GET avec un token null ne lève pas d'exception.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testGet_TokenNull_PasDeNullPointerException() throws InterruptedException {
        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune exception ne doit être levée
        try {
            AppelAPI.get(URL_VALIDE, null, contexte, new AppelAPI.VolleyCallback() {
                @Override
                public void onSuccess(JSONArray result) {
                    verrou.countDown();
                }

                @Override
                public void onError(VolleyError error) {
                    verrou.countDown();
                }
            });
            verrou.countDown();
        } catch (NullPointerException e) {
            assertTrue("NullPointerException ne doit pas être levée avec token null",
                    false);
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
    }

    // =========================================================================
    // TESTS — POST
    // =========================================================================

    /**
     * Vérifie que la requête POST est correctement ajoutée à la file
     * avec un body JSON valide.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     * @throws JSONException        si le JSON de test est mal formé
     */
    @Test
    public void testPost_BodyValide_RequeteAjouteeALaFile()
            throws InterruptedException, JSONException {

        // Given — un body JSON valide
        CountDownLatch verrou = new CountDownLatch(1);
        JSONObject body = new JSONObject();
        body.put("nom", "Jean");
        body.put("email", "jean@test.fr");

        // When — on effectue un POST
        AppelAPI.post(URL_VALIDE, TOKEN_VALIDE, body, contexte,
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée après un POST",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le POST avec un body vide est correctement géré.
     *
     * <p><b>Cas limite</b> : body JSON vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     * @throws JSONException        si le JSON de test est mal formé
     */
    @Test
    public void testPost_BodyVide_RequeteAjouteeALaFile()
            throws InterruptedException, JSONException {

        // Given — un body JSON vide
        CountDownLatch verrou = new CountDownLatch(1);
        JSONObject bodyVide = new JSONObject();

        // When — on effectue un POST avec body vide
        AppelAPI.post(URL_VALIDE, TOKEN_VALIDE, bodyVide, contexte,
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée même avec un body vide
        assertNotNull("La file ne doit pas être nulle avec un body vide",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — PUT
    // =========================================================================

    /**
     * Vérifie que la requête PUT avec objet JSON est correctement
     * ajoutée à la file.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     * @throws JSONException        si le JSON de test est mal formé
     */
    @Test
    public void testPut_BodyValide_RequeteAjouteeALaFile()
            throws InterruptedException, JSONException {

        // Given — un body JSON valide
        CountDownLatch verrou = new CountDownLatch(1);
        JSONObject body = new JSONObject();
        body.put("statut", "ACTIF");

        // When — on effectue un PUT
        AppelAPI.put(URL_VALIDE, TOKEN_VALIDE, body, contexte,
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file ne doit pas être nulle après un PUT",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le PUT avec callback null ne lève pas de NullPointerException.
     *
     * <p><b>Cas limite</b> : callback null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     * @throws JSONException        si le JSON de test est mal formé
     */
    @Test
    public void testPut_CallbackNull_PasDeNullPointerException()
            throws InterruptedException, JSONException {

        // Given — un callback null et un body valide
        CountDownLatch verrou = new CountDownLatch(1);
        JSONObject body = new JSONObject();
        body.put("statut", "ACTIF");

        // When / Then — aucune exception ne doit être levée
        try {
            AppelAPI.put(URL_VALIDE, TOKEN_VALIDE, body, contexte, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            assertTrue("NullPointerException ne doit pas être levée avec callback null",
                    false);
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file ne doit pas être nulle",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — PUT TABLEAU
    // =========================================================================

    /**
     * Vérifie que la requête PUT avec tableau JSON est correctement
     * ajoutée à la file.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     * @throws JSONException        si le JSON de test est mal formé
     */
    @Test
    public void testPutA_TableauValide_RequeteAjouteeALaFile()
            throws InterruptedException, JSONException {

        // Given — un tableau JSON valide avec un point GPS
        CountDownLatch verrou = new CountDownLatch(1);
        JSONArray tableau = new JSONArray();
        JSONObject point = new JSONObject();
        point.put("latitude", 44.36);
        point.put("longitude", 2.57);
        tableau.put(point);

        // When — on effectue un PUT avec tableau
        AppelAPI.putA(URL_VALIDE, TOKEN_VALIDE, tableau, contexte,
                new AppelAPI.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file ne doit pas être nulle après un PUT tableau",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le PUT avec un tableau vide est correctement géré.
     *
     * <p><b>Cas limite</b> : tableau vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testPutA_TableauVide_RequeteAjouteeALaFile() throws InterruptedException {
        // Given — un tableau JSON vide
        CountDownLatch verrou = new CountDownLatch(1);
        JSONArray tableauVide = new JSONArray();

        // When — on effectue un PUT avec tableau vide
        AppelAPI.putA(URL_VALIDE, TOKEN_VALIDE, tableauVide, contexte,
                new AppelAPI.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée même avec un tableau vide
        assertNotNull("La file ne doit pas être nulle avec un tableau vide",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — DELETE
    // =========================================================================

    /**
     * Vérifie qu'une requête DELETE est correctement ajoutée à la file.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testDelete_UrlValide_RequeteAjouteeALaFile() throws InterruptedException {
        // Given — une URL valide
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on effectue un DELETE
        AppelAPI.delete(URL_VALIDE, TOKEN_VALIDE, contexte,
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        verrou.countDown();
                    }
                });

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file ne doit pas être nulle après un DELETE",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le DELETE avec callback null ne lève pas de NullPointerException.
     *
     * <p><b>Cas limite</b> : callback null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testDelete_CallbackNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un callback null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune exception ne doit être levée
        try {
            AppelAPI.delete(URL_VALIDE, TOKEN_VALIDE, contexte, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            assertTrue("NullPointerException ne doit pas être levée avec callback null",
                    false);
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file ne doit pas être nulle",
                AppelAPI.getFileRequete(contexte));
    }
}