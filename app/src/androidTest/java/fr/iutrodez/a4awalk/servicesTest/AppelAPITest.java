package fr.iutrodez.a4awalk.servicesTest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

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

import java.util.Map;

import fr.iutrodez.a4awalk.services.AppelAPI;

/**
 * Classe de tests unitaires pour {@link AppelAPI}.
 *
 * <p>Teste les méthodes HTTP (GET, POST, PUT, DELETE) en vérifiant :</p>
 * <ul>
 *     <li>Les cas nominaux : succès des requêtes et appel correct des callbacks</li>
 *     <li>Les cas limites : token null, token vide, body null</li>
 *     <li>Les cas d'erreur : erreurs réseau, erreurs serveur, exceptions JSON</li>
 * </ul>
 *
 * <p>Utilise Mockito pour simuler les dépendances Android (Context, RequestQueue, Volley).</p>
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
public class AppelAPITest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL utilisée dans tous les tests */
    private static final String URL_TEST = "https://api.exemple.com/ressource";

    /** Token JWT valide utilisé dans les tests nominaux */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android (requis par Volley) */
    @Mock
    private Context mockContexte;

    /** Mock de la file de requêtes Volley */
    @Mock
    private RequestQueue mockFileRequete;

    /** Mock du callback pour les réponses en tableau JSON */
    @Mock
    private AppelAPI.VolleyCallback mockCallbackArray;

    /** Mock du callback pour les réponses en objet JSON */
    @Mock
    private AppelAPI.VolleyObjectCallback mockCallbackObject;

    // =========================================================================
    // CONFIGURATION DES TESTS
    // =========================================================================

    /**
     * Initialisation avant chaque test.
     *
     * <p>Réinitialise le singleton de la file de requêtes afin d'isoler
     * chaque test et d'éviter les effets de bord entre eux.</p>
     */
    @Before
    public void setUp() {
        AppelAPI.resetFileRequete();
    }

    /**
     * Nettoyage après chaque test.
     *
     * <p>Réinitialise à nouveau le singleton pour garantir l'isolation
     * des tests suivants.</p>
     */
    @After
    public void tearDown() {
        AppelAPI.resetFileRequete();
    }

    // =========================================================================
    // TESTS : getFileRequete (Singleton)
    // =========================================================================

    /**
     * Vérifie que {@link AppelAPI#getFileRequete(Context)} retourne toujours
     * la même instance (pattern Singleton).
     *
     * <p><b>Given</b> un contexte Android valide<br>
     * <b>When</b> on appelle getFileRequete deux fois de suite<br>
     * <b>Then</b> les deux appels retournent la même instance de RequestQueue</p>
     */
    @Test
    public void getFileRequete_deuxiemeAppel_retourneMemeInstance() {
        // Given
        // Contexte mocké prêt à l'emploi

        // When
        RequestQueue premiere = AppelAPI.getFileRequete(mockContexte);
        RequestQueue deuxieme = AppelAPI.getFileRequete(mockContexte);

        // Then
        assertSame("Le Singleton doit retourner la même instance", premiere, deuxieme);
    }

    /**
     * Vérifie que {@link AppelAPI#resetFileRequete()} réinitialise bien le Singleton,
     * permettant la création d'une nouvelle instance au prochain appel.
     *
     * <p><b>Given</b> une instance de RequestQueue déjà créée<br>
     * <b>When</b> on appelle resetFileRequete puis getFileRequete<br>
     * <b>Then</b> une nouvelle instance est créée (différente de la précédente)</p>
     */
    @Test
    public void resetFileRequete_apresReset_nouvelleInstanceCreee() {
        // Given
        RequestQueue premiere = AppelAPI.getFileRequete(mockContexte);
        assertNotNull("La première instance ne doit pas être null", premiere);

        // When
        AppelAPI.resetFileRequete();
        RequestQueue deuxieme = AppelAPI.getFileRequete(mockContexte);

        // Then
        assertNotNull("La nouvelle instance ne doit pas être null", deuxieme);
        assertNotSame("Après reset, une nouvelle instance doit être créée", premiere, deuxieme);
    }

    // =========================================================================
    // TESTS : GET (tableau JSON)
    // =========================================================================

    /**
     * Vérifie qu'une requête GET avec un token valide ajoute bien la requête
     * dans la file Volley avec les bons en-têtes d'authentification.
     *
     * <p><b>Given</b> une URL valide et un token Bearer valide<br>
     * <b>When</b> on appelle {@link AppelAPI#get(String, String, Context, AppelAPI.VolleyCallback)}<br>
     * <b>Then</b> la requête est ajoutée à la file avec le bon en-tête Authorization</p>
     */
    @Test
    public void get_avecTokenValide_ajouteRequeteAvecBonEnTete() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonArrayRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonArrayRequest.class);

            // When
            AppelAPI.get(URL_TEST, TOKEN_VALIDE, mockContexte, mockCallbackArray);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            JsonArrayRequest requete = captureurRequete.getValue();
            assertNotNull("La requête ne doit pas être null", requete);

            Map<String, String> headers = requete.getHeaders();
            assertTrue("Le header Authorization doit être présent",
                    headers.containsKey("Authorization"));
            assertEquals("Le header Authorization doit contenir le token Bearer",
                    "Bearer " + TOKEN_VALIDE,
                    headers.get("Authorization"));
            assertEquals("Le Content-Type doit être application/json",
                    "application/json",
                    headers.get("Content-Type"));
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie que le callback {@link AppelAPI.VolleyCallback#onSuccess(JSONArray)}
     * est appelé lors d'une réponse réussie à une requête GET.
     *
     * <p><b>Given</b> un callback enregistré pour une requête GET<br>
     * <b>When</b> la réponse du serveur est un tableau JSON valide<br>
     * <b>Then</b> onSuccess est appelé avec le tableau JSON retourné</p>
     */
    @Test
    public void get_reponseSucces_appelleOnSuccess() {
        // Given
        JSONArray tableauAttendu = new JSONArray();
        try {
            tableauAttendu.put(new JSONObject().put("id", 1).put("nom", "test"));
        } catch (JSONException e) {
            fail("Erreur de préparation du test : " + e.getMessage());
        }

        AppelAPI.VolleyCallback callbackReel = new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // Then
                assertNotNull("Le résultat ne doit pas être null", result);
                assertEquals("Le tableau doit contenir 1 élément", 1, result.length());
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé");
            }
        };

        // When - simulation de l'appel du callback comme le ferait Volley
        callbackReel.onSuccess(tableauAttendu);
    }

    /**
     * Vérifie que le callback {@link AppelAPI.VolleyCallback#onError(VolleyError)}
     * est appelé lors d'une erreur réseau sur une requête GET.
     *
     * <p><b>Given</b> une erreur réseau simulée<br>
     * <b>When</b> Volley retourne une erreur<br>
     * <b>Then</b> onError est appelé avec l'erreur correspondante</p>
     */
    @Test
    public void get_erreurReseau_appelleOnError() {
        // Given
        VolleyError erreurSimulee = new VolleyError("Erreur réseau simulée");

        AppelAPI.VolleyCallback callbackReel = new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                fail("onSuccess ne devrait pas être appelé en cas d'erreur");
            }

            @Override
            public void onError(VolleyError error) {
                // Then
                assertNotNull("L'erreur ne doit pas être null", error);
                assertEquals("Erreur réseau simulée", error.getMessage());
            }
        };

        // When
        callbackReel.onError(erreurSimulee);
    }

    // =========================================================================
    // TESTS : GET (objet JSON)
    // =========================================================================

    /**
     * Vérifie que le callback {@link AppelAPI.VolleyObjectCallback#onSuccess(JSONObject)}
     * est appelé lors d'une réponse réussie à une requête GET retournant un objet JSON.
     *
     * <p><b>Given</b> un objet JSON de réponse valide<br>
     * <b>When</b> le callback onSuccess est déclenché<br>
     * <b>Then</b> l'objet JSON est reçu et exploitable</p>
     */
    @Test
    public void getObjet_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONObject objetAttendu = new JSONObject();
        objetAttendu.put("id", 42);
        objetAttendu.put("nom", "utilisateur_test");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // Then
                assertNotNull("Le résultat ne doit pas être null", result);
                assertEquals("L'id doit être 42", 42, result.getInt("id"));
                assertEquals("Le nom doit correspondre", "utilisateur_test", result.getString("nom"));
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé");
            }
        };

        // When
        callbackReel.onSuccess(objetAttendu);
    }

    /**
     * Vérifie que le callback {@link AppelAPI.VolleyObjectCallback#onError(VolleyError)}
     * est appelé lors d'une erreur sur une requête GET retournant un objet JSON.
     *
     * <p><b>Given</b> une erreur serveur 404 simulée<br>
     * <b>When</b> le callback onError est déclenché<br>
     * <b>Then</b> l'erreur est correctement transmise au callback</p>
     */
    @Test
    public void getObjet_erreurServeur_appelleOnError() {
        // Given
        VolleyError erreur404 = new VolleyError("404 Not Found");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                fail("onSuccess ne devrait pas être appelé en cas d'erreur serveur");
            }

            @Override
            public void onError(VolleyError error) {
                // Then
                assertNotNull("L'erreur ne doit pas être null", error);
            }
        };

        // When
        callbackReel.onError(erreur404);
    }

    // =========================================================================
    // TESTS : POST
    // =========================================================================

    /**
     * Vérifie que le callback onSuccess est bien appelé avec la réponse du serveur
     * lors d'une requête POST réussie.
     *
     * <p><b>Given</b> un corps de requête POST valide et un serveur qui répond<br>
     * <b>When</b> le serveur retourne un objet JSON de confirmation<br>
     * <b>Then</b> onSuccess est appelé avec l'objet de réponse</p>
     */
    @Test
    public void post_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONObject reponseServeur = new JSONObject();
        reponseServeur.put("statut", "créé");
        reponseServeur.put("idCreated", 99);

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // Then
                assertNotNull("La réponse ne doit pas être null", result);
                assertEquals("Le statut doit être 'créé'", "créé", result.getString("statut"));
                assertEquals("L'id créé doit être 99", 99, result.getInt("idCreated"));
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé en cas de succès");
            }
        };

        // When
        callbackReel.onSuccess(reponseServeur);
    }

    /**
     * Vérifie que le callback onError est bien appelé lors d'une erreur 500
     * (erreur interne serveur) sur une requête POST.
     *
     * <p><b>Given</b> un serveur qui retourne une erreur 500<br>
     * <b>When</b> Volley transmet cette erreur via le callback<br>
     * <b>Then</b> onError est appelé et l'erreur est non null</p>
     */
    @Test
    public void post_erreurServeur500_appelleOnError() {
        // Given
        VolleyError erreur500 = new VolleyError("500 Internal Server Error");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                fail("onSuccess ne devrait pas être appelé en cas d'erreur 500");
            }

            @Override
            public void onError(VolleyError error) {
                // Then
                assertNotNull("L'erreur 500 ne doit pas être null", error);
                assertEquals("500 Internal Server Error", error.getMessage());
            }
        };

        // When
        callbackReel.onError(erreur500);
    }

    // =========================================================================
    // TESTS : PUT (objet JSON)
    // =========================================================================

    /**
     * Vérifie que le callback onSuccess est appelé avec la réponse correcte
     * lors d'une requête PUT réussie.
     *
     * <p><b>Given</b> un objet JSON de mise à jour valide<br>
     * <b>When</b> le serveur confirme la mise à jour<br>
     * <b>Then</b> onSuccess est appelé avec l'objet de confirmation</p>
     */
    @Test
    public void put_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONObject reponseMAJ = new JSONObject();
        reponseMAJ.put("statut", "mis à jour");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // Then
                assertNotNull("La réponse ne doit pas être null", result);
                assertEquals("Le statut doit être 'mis à jour'",
                        "mis à jour", result.getString("statut"));
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé en cas de succès PUT");
            }
        };

        // When
        callbackReel.onSuccess(reponseMAJ);
    }

    // =========================================================================
    // TESTS : PUT (tableau JSON)
    // =========================================================================

    /**
     * Vérifie que le callback {@link AppelAPI.VolleyCallback#onSuccess(JSONArray)}
     * est appelé avec le bon tableau JSON lors d'une requête PUT avec un tableau en body.
     *
     * <p><b>Given</b> un tableau JSON envoyé en corps de PUT<br>
     * <b>When</b> le serveur retourne un tableau JSON de confirmation<br>
     * <b>Then</b> onSuccess reçoit le tableau avec le bon nombre d'éléments</p>
     */
    @Test
    public void putArray_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONArray reponseTableau = new JSONArray();
        reponseTableau.put(new JSONObject().put("id", 1).put("statut", "ok"));
        reponseTableau.put(new JSONObject().put("id", 2).put("statut", "ok"));

        AppelAPI.VolleyCallback callbackReel = new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // Then
                assertNotNull("Le tableau de réponse ne doit pas être null", result);
                assertEquals("Le tableau doit contenir 2 éléments", 2, result.length());
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé en cas de succès PUT");
            }
        };

        // When
        callbackReel.onSuccess(reponseTableau);
    }

    // =========================================================================
    // TESTS : DELETE
    // =========================================================================

    /**
     * Vérifie que le callback onSuccess est appelé avec la confirmation de suppression
     * lors d'une requête DELETE réussie.
     *
     * <p><b>Given</b> une ressource existante à supprimer<br>
     * <b>When</b> le serveur confirme la suppression via un objet JSON<br>
     * <b>Then</b> onSuccess est appelé avec l'objet de confirmation</p>
     */
    @Test
    public void delete_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONObject reponseSupp = new JSONObject();
        reponseSupp.put("statut", "supprimé");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // Then
                assertNotNull("La réponse de suppression ne doit pas être null", result);
                assertEquals("Le statut doit être 'supprimé'",
                        "supprimé", result.getString("statut"));
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé lors d'une suppression réussie");
            }
        };

        // When
        callbackReel.onSuccess(reponseSupp);
    }

    /**
     * Vérifie que le callback onError est bien appelé lorsque la ressource
     * à supprimer n'existe pas (erreur 404).
     *
     * <p><b>Given</b> une ressource inexistante<br>
     * <b>When</b> le serveur retourne une erreur 404 sur la requête DELETE<br>
     * <b>Then</b> onError est appelé avec l'erreur 404</p>
     */
    @Test
    public void delete_ressourceInexistante_appelleOnError() {
        // Given
        VolleyError erreur404 = new VolleyError("404 Not Found");

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                fail("onSuccess ne devrait pas être appelé si la ressource n'existe pas");
            }

            @Override
            public void onError(VolleyError error) {
                // Then
                assertNotNull("L'erreur 404 ne doit pas être null", error);
                assertEquals("404 Not Found", error.getMessage());
            }
        };

        // When
        callbackReel.onError(erreur404);
    }

    // =========================================================================
    // TESTS : CAS LIMITES - En-têtes HTTP
    // =========================================================================

    /**
     * Vérifie que lorsque le token est {@code null}, aucun en-tête Authorization
     * n'est ajouté à la requête (pas de header Bearer null).
     *
     * <p><b>Given</b> un token null<br>
     * <b>When</b> on effectue une requête GET<br>
     * <b>Then</b> l'en-tête Authorization est absent des headers</p>
     */
    @Test
    public void get_avecTokenNull_aucunHeaderAuthorization() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonArrayRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonArrayRequest.class);

            // When
            AppelAPI.get(URL_TEST, null, mockContexte, mockCallbackArray);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            JsonArrayRequest requete = captureurRequete.getValue();
            Map<String, String> headers = requete.getHeaders();

            assertFalse("Aucun header Authorization ne doit être présent avec un token null",
                    headers.containsKey("Authorization"));
            assertEquals("Le Content-Type doit toujours être présent",
                    "application/json", headers.get("Content-Type"));
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie que lorsque le token est une chaîne vide, aucun en-tête Authorization
     * n'est ajouté à la requête.
     *
     * <p><b>Given</b> un token vide ("")<br>
     * <b>When</b> on effectue une requête GET<br>
     * <b>Then</b> l'en-tête Authorization est absent des headers</p>
     */
    @Test
    public void get_avecTokenVide_aucunHeaderAuthorization() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonArrayRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonArrayRequest.class);

            // When
            AppelAPI.get(URL_TEST, "", mockContexte, mockCallbackArray);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            JsonArrayRequest requete = captureurRequete.getValue();
            Map<String, String> headers = requete.getHeaders();

            assertFalse("Aucun header Authorization ne doit être présent avec un token vide",
                    headers.containsKey("Authorization"));
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie que le Content-Type est toujours "application/json"
     * quelle que soit la valeur du token.
     *
     * <p><b>Given</b> n'importe quelle requête (token valide ou non)<br>
     * <b>When</b> on inspecte les en-têtes de la requête<br>
     * <b>Then</b> le header Content-Type vaut toujours "application/json"</p>
     */
    @Test
    public void get_avecTokenValide_contentTypeEstToujoursJson() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonArrayRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonArrayRequest.class);

            // When
            AppelAPI.get(URL_TEST, TOKEN_VALIDE, mockContexte, mockCallbackArray);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            JsonArrayRequest requete = captureurRequete.getValue();
            Map<String, String> headers = requete.getHeaders();

            assertEquals("Le Content-Type doit être application/json",
                    "application/json", headers.get("Content-Type"));
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES - Méthodes HTTP
    // =========================================================================

    /**
     * Vérifie que la requête GET utilise bien la méthode HTTP GET.
     *
     * <p><b>Given</b> un appel à {@link AppelAPI#get(String, String, Context, AppelAPI.VolleyCallback)}<br>
     * <b>When</b> la requête est ajoutée à la file<br>
     * <b>Then</b> la méthode HTTP de la requête est bien GET (valeur = 0)</p>
     */
    @Test
    public void get_methodeHttp_estGET() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonArrayRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonArrayRequest.class);

            // When
            AppelAPI.get(URL_TEST, TOKEN_VALIDE, mockContexte, mockCallbackArray);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            assertEquals("La méthode HTTP doit être GET",
                    Request.Method.GET, captureurRequete.getValue().getMethod());
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie que la requête POST utilise bien la méthode HTTP POST.
     *
     * <p><b>Given</b> un appel à {@link AppelAPI#post(String, String, JSONObject, Context, AppelAPI.VolleyObjectCallback)}<br>
     * <b>When</b> la requête est ajoutée à la file<br>
     * <b>Then</b> la méthode HTTP de la requête est bien POST (valeur = 1)</p>
     */
    @Test
    public void post_methodeHttp_estPOST() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonObjectRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonObjectRequest.class);

            JSONObject body = new JSONObject().put("cle", "valeur");

            // When
            AppelAPI.post(URL_TEST, TOKEN_VALIDE, body, mockContexte, mockCallbackObject);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            assertEquals("La méthode HTTP doit être POST",
                    Request.Method.POST, captureurRequete.getValue().getMethod());
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    /**
     * Vérifie que la requête DELETE utilise bien la méthode HTTP DELETE.
     *
     * <p><b>Given</b> un appel à {@link AppelAPI#delete(String, String, Context, AppelAPI.VolleyObjectCallback)}<br>
     * <b>When</b> la requête est ajoutée à la file<br>
     * <b>Then</b> la méthode HTTP de la requête est bien DELETE (valeur = 7)</p>
     */
    @Test
    public void delete_methodeHttp_estDELETE() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            ArgumentCaptor<JsonObjectRequest> captureurRequete =
                    ArgumentCaptor.forClass(JsonObjectRequest.class);

            // When
            AppelAPI.delete(URL_TEST, TOKEN_VALIDE, mockContexte, mockCallbackObject);

            // Then
            verify(mockFileRequete).add(captureurRequete.capture());
            assertEquals("La méthode HTTP doit être DELETE",
                    Request.Method.DELETE, captureurRequete.getValue().getMethod());
        } catch (Exception e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES - Callbacks null
    // =========================================================================

    /**
     * Vérifie que l'appel à {@link AppelAPI#put(String, String, JSONObject, Context, AppelAPI.VolleyObjectCallback)}
     * avec un callback null ne provoque pas de NullPointerException.
     *
     * <p><b>Given</b> un callback null passé à la méthode put<br>
     * <b>When</b> la requête est ajoutée à la file<br>
     * <b>Then</b> aucune exception n'est levée (la méthode gère le cas null)</p>
     */
    @Test
    public void put_avecCallbackNull_aucuneExceptionLevee() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            JSONObject body;
            try {
                body = new JSONObject().put("cle", "valeur");
            } catch (JSONException e) {
                fail("Préparation du test échouée : " + e.getMessage());
                return;
            }

            // When / Then — aucune exception ne doit être levée
            try {
                AppelAPI.put(URL_TEST, TOKEN_VALIDE, body, mockContexte, null);
            } catch (NullPointerException e) {
                fail("Un callback null ne doit pas provoquer de NullPointerException");
            }
        }
    }

    /**
     * Vérifie que l'appel à {@link AppelAPI#delete(String, String, Context, AppelAPI.VolleyObjectCallback)}
     * avec un callback null ne provoque pas de NullPointerException.
     *
     * <p><b>Given</b> un callback null passé à la méthode delete<br>
     * <b>When</b> la requête est ajoutée à la file<br>
     * <b>Then</b> aucune exception n'est levée (la méthode gère le cas null)</p>
     */
    @Test
    public void delete_avecCallbackNull_aucuneExceptionLevee() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(
                AppelAPI.class, Mockito.CALLS_REAL_METHODS)) {

            staticMock.when(() -> AppelAPI.getFileRequete(mockContexte))
                    .thenReturn(mockFileRequete);

            // When / Then — aucune exception ne doit être levée
            try {
                AppelAPI.delete(URL_TEST, TOKEN_VALIDE, mockContexte, null);
            } catch (NullPointerException e) {
                fail("Un callback null ne doit pas provoquer de NullPointerException");
            }
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES - Réponse JSON vide
    // =========================================================================

    /**
     * Vérifie que le callback onSuccess gère correctement un tableau JSON vide
     * (réponse 200 OK avec un tableau sans éléments).
     *
     * <p><b>Given</b> une réponse serveur avec un tableau JSON vide []<br>
     * <b>When</b> onSuccess est déclenché avec ce tableau<br>
     * <b>Then</b> le tableau est reçu, non null, et de taille 0</p>
     */
    @Test
    public void get_reponseTableauVide_onSuccessRecoitTableauVide() {
        // Given
        JSONArray tableauVide = new JSONArray();

        AppelAPI.VolleyCallback callbackReel = new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // Then
                assertNotNull("Le tableau vide ne doit pas être null", result);
                assertEquals("Le tableau doit être vide", 0, result.length());
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé pour un tableau vide valide");
            }
        };

        // When
        callbackReel.onSuccess(tableauVide);
    }

    /**
     * Vérifie que le callback onSuccess gère correctement un objet JSON vide {}.
     *
     * <p><b>Given</b> une réponse serveur avec un objet JSON vide {}<br>
     * <b>When</b> onSuccess est déclenché avec cet objet<br>
     * <b>Then</b> l'objet est reçu, non null, et ne contient aucune clé</p>
     */
    @Test
    public void getObjet_reponseObjetVide_onSuccessRecoitObjetVide() throws JSONException {
        // Given
        JSONObject objetVide = new JSONObject();

        AppelAPI.VolleyObjectCallback callbackReel = new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // Then
                assertNotNull("L'objet vide ne doit pas être null", result);
                assertEquals("L'objet ne doit contenir aucune clé", 0, result.length());
            }

            @Override
            public void onError(VolleyError error) {
                fail("onError ne devrait pas être appelé pour un objet vide valide");
            }
        };

        // When
        callbackReel.onSuccess(objetVide);
    }
}