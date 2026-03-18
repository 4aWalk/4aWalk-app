package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceOptimisation;

/**
 * Classe de tests unitaires pour {@link ServiceOptimisation}.
 *
 * <p>Teste la méthode {@code optimiserRandonnee} en simulant les différents scénarios
 * possibles lors d'une tentative d'optimisation d'une randonnée :</p>
 * <ul>
 *     <li>Cas nominaux : optimisation réussie, résultat JSON transmis au callback</li>
 *     <li>Cas limites : callback null, URL correcte, token transmis, body null envoyé</li>
 *     <li>Cas d'erreur : erreur avec message JSON (clé "message"), erreur avec JSON
 *         sans clé "message", body non JSON, erreur sans networkResponse,
 *         erreur sans message</li>
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
public class ServiceOptimisationTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL de base de l'API */
    private static final String BASE_URL = "http://98.94.8.220:8080";

    /** Token d'authentification utilisé dans les tests */
    private static final String TOKEN_VALIDE = "Bearer.token.test";

    /** Identifiant de randonnée utilisé dans les tests */
    private static final int RANDO_ID = 12;

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android */
    @Mock
    private Context mockContexte;

    /** Mock du callback d'optimisation */
    @Mock
    private ServiceOptimisation.OptimisationCallback mockCallback;

    // =========================================================================
    // CLASSE INTERNE — VolleyError avec code HTTP et body
    // =========================================================================

    /**
     * Sous-classe de {@link VolleyError} permettant de définir un code HTTP
     * et un corps de réponse précis.
     *
     * <p>Nécessaire car le champ {@code networkResponse} de {@link VolleyError}
     * est {@code final} et ne peut pas être réassigné directement après construction.</p>
     */
    private static class VolleyErrorAvecCode extends VolleyError {
        /**
         * Construit une erreur Volley avec le code HTTP et le body spécifiés.
         *
         * @param statusCode Code HTTP à simuler (ex : 400, 500)
         * @param body       Corps de la réponse d'erreur en bytes (peut être null)
         */
        VolleyErrorAvecCode(int statusCode, byte[] body) {
            super(new NetworkResponse(statusCode, body, false, 0, null));
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
     * Capture et retourne le {@link AppelAPI.VolleyObjectCallback} enregistré lors
     * de l'appel à {@link AppelAPI#post} dans un contexte de mock statique.
     *
     * @param staticMock Mock statique d'AppelAPI déjà ouvert
     * @return Le captureur du callback, prêt à être utilisé après l'appel à
     *         {@code optimiserRandonnee}
     */
    private ArgumentCaptor<AppelAPI.VolleyObjectCallback> preparerCaptureurCallback(
            MockedStatic<AppelAPI> staticMock) {

        ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

        staticMock.when(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                        any(Context.class), captureur.capture()))
                .thenAnswer(inv -> null);

        return captureur;
    }

    /**
     * Construit un {@link VolleyError} avec un corps JSON contenant la clé "message".
     *
     * @param statusCode   Code HTTP à simuler
     * @param messageValue Valeur du champ "message" dans le JSON d'erreur
     * @return Un {@link VolleyErrorAvecCode} avec body JSON encodé en UTF-8
     */
    private VolleyError buildErreurAvecMessageJson(int statusCode, String messageValue) {
        String body = "{\"message\": \"" + messageValue + "\"}";
        return new VolleyErrorAvecCode(statusCode, body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Construit un {@link VolleyError} avec un corps JSON sans la clé "message".
     *
     * @param statusCode Code HTTP à simuler
     * @return Un {@link VolleyErrorAvecCode} avec body JSON sans clé "message"
     */
    private VolleyError buildErreurSansCleMessage(int statusCode) {
        String body = "{\"code\": " + statusCode + ", \"detail\": \"Optimisation impossible\"}";
        return new VolleyErrorAvecCode(statusCode, body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Construit un {@link VolleyError} sans networkResponse (ex : timeout).
     *
     * @param message Message de l'erreur Volley
     * @return Un {@link VolleyError} dont la {@code networkResponse} est null
     */
    private VolleyError buildErreurSansReseau(String message) {
        return new VolleyError(message);
    }

    // =========================================================================
    // TESTS : CAS NOMINAUX — Optimisation réussie
    // =========================================================================

    /**
     * Vérifie que {@link ServiceOptimisation.OptimisationCallback#onSuccess(JSONObject)}
     * est appelé avec le résultat JSON retourné par le serveur.
     *
     * <p><b>Given</b> un idRando valide et une réponse JSON de succès<br>
     * <b>When</b> Volley déclenche le callback onSuccess<br>
     * <b>Then</b> {@code callback.onSuccess} est invoqué avec l'objet JSON correct</p>
     */
    @Test
    public void optimiserRandonnee_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        JSONObject reponseServeur = new JSONObject();
        reponseServeur.put("statut", "optimisé");
        reponseServeur.put("poidsTotal", 4500.0);

        AtomicReference<JSONObject> resultCapture = new AtomicReference<>();
        ServiceOptimisation.OptimisationCallback callbackReel = new ServiceOptimisation.OptimisationCallback() {
            @Override
            public void onSuccess(JSONObject result) { resultCapture.set(result); }
            @Override
            public void onError(String message) { fail("onError ne doit pas être appelé"); }
        };

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE,
                    RANDO_ID, callbackReel);
            captureur.getValue().onSuccess(reponseServeur);

            // Then
            assertNotNull("Le résultat JSON ne doit pas être null", resultCapture.get());
            assertEquals("Le statut doit être 'optimisé'",
                    "optimisé", resultCapture.get().getString("statut"));
        }
    }

    /**
     * Vérifie que {@code callback.onError} n'est pas appelé lors d'une optimisation réussie.
     *
     * <p><b>Given</b> une réponse serveur valide<br>
     * <b>When</b> Volley déclenche le callback onSuccess<br>
     * <b>Then</b> {@code callback.onError} n'est jamais invoqué</p>
     */
    @Test
    public void optimiserRandonnee_reponseSucces_onErrorNonAppele() throws JSONException {
        // Given
        AtomicBoolean onErrorAppele = new AtomicBoolean(false);
        ServiceOptimisation.OptimisationCallback callbackReel = new ServiceOptimisation.OptimisationCallback() {
            @Override
            public void onSuccess(JSONObject result) { /* succès attendu */ }
            @Override
            public void onError(String message) { onErrorAppele.set(true); }
        };

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE,
                    RANDO_ID, callbackReel);
            captureur.getValue().onSuccess(new JSONObject());

            // Then
            assertFalse("onError ne doit pas être appelé lors d'un succès",
                    onErrorAppele.get());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — URL et paramètres de la requête
    // =========================================================================

    /**
     * Vérifie que l'URL construite par {@code optimiserRandonnee} contient
     * bien le {@code idRando} et le chemin {@code /optimize}.
     *
     * <p><b>Given</b> un idRando de valeur {@value #RANDO_ID}<br>
     * <b>When</b> {@code optimiserRandonnee} est appelé<br>
     * <b>Then</b> l'URL passée à AppelAPI.post est {@code /hikes/12/optimize}</p>
     */
    @Test
    public void optimiserRandonnee_urlConstuite_contientIdEtOptimize() {
        // Given
        String urlAttendue = BASE_URL + "/hikes/" + RANDO_ID + "/optimize";

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE,
                    RANDO_ID, mockCallback);

            // Then
            assertEquals("L'URL doit contenir l'idRando et le chemin /optimize",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que différents {@code idRando} produisent des URLs distinctes.
     *
     * <p><b>Given</b> deux idRando différents (1 et 999)<br>
     * <b>When</b> {@code optimiserRandonnee} est appelé avec chacun<br>
     * <b>Then</b> les URLs générées contiennent respectivement les bons ids</p>
     */
    @Test
    public void optimiserRandonnee_idsDifferents_urlsDifferentes() {
        // Given
        int idRando1 = 1;
        int idRando2 = 999;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, idRando1, mockCallback);
            String url1 = captureurUrl.getValue();

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, idRando2, mockCallback);
            String url2 = captureurUrl.getValue();

            // Then
            assertTrue("L'URL 1 doit contenir l'id 1", url1.contains("/hikes/1/"));
            assertTrue("L'URL 2 doit contenir l'id 999", url2.contains("/hikes/999/"));
            assertNotEquals("Les deux URLs doivent être différentes", url1, url2);
        }
    }

    /**
     * Vérifie que le token passé à {@link AppelAPI#post} est identique à celui fourni.
     *
     * <p><b>Given</b> un token valide<br>
     * <b>When</b> {@code optimiserRandonnee} est appelé<br>
     * <b>Then</b> le token transmis à AppelAPI est identique</p>
     */
    @Test
    public void optimiserRandonnee_tokenTransmis_estIdentique() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(anyString(), captureurToken.capture(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE,
                    RANDO_ID, mockCallback);

            // Then
            assertEquals("Le token transmis à AppelAPI doit être identique",
                    TOKEN_VALIDE, captureurToken.getValue());
        }
    }

    /**
     * Vérifie que le body passé à {@link AppelAPI#post} est null
     * (l'API d'optimisation ne nécessite pas de corps de requête).
     *
     * <p><b>Given</b> un appel à {@code optimiserRandonnee}<br>
     * <b>When</b> AppelAPI.post est invoqué<br>
     * <b>Then</b> le body est null</p>
     */
    @Test
    public void optimiserRandonnee_bodyRequete_estNull() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE,
                    RANDO_ID, mockCallback);

            // Then — isNull() vérifié via le captureur
            assertNull("Le body doit être null pour la requête d'optimisation",
                    captureurBody.getValue());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — Callback null
    // =========================================================================

    /**
     * Vérifie qu'aucune {@link NullPointerException} n'est levée lorsque le callback
     * est null et que le serveur retourne un succès.
     *
     * <p><b>Given</b> un callback null<br>
     * <b>When</b> Volley déclenche onSuccess<br>
     * <b>Then</b> aucune exception n'est levée (la méthode vérifie {@code callback != null})</p>
     */
    @Test
    public void optimiserRandonnee_callbackNullEtSucces_aucuneException() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                            any(Context.class), captureur.capture()))
                    .thenAnswer(inv -> null);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID, null);

            // When / Then — aucune NullPointerException ne doit être levée
            try {
                captureur.getValue().onSuccess(new JSONObject());
            } catch (NullPointerException e) {
                fail("Un callback null ne doit pas provoquer de NullPointerException lors de onSuccess");
            }
        }
    }

    /**
     * Vérifie qu'aucune {@link NullPointerException} n'est levée lorsque le callback
     * est null et que le serveur retourne une erreur.
     *
     * <p><b>Given</b> un callback null<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> aucune exception n'est levée (la méthode vérifie {@code callback != null})</p>
     */
    @Test
    public void optimiserRandonnee_callbackNullEtErreur_aucuneException() {
        // Given
        VolleyError erreur = buildErreurSansReseau("Timeout");

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                            any(Context.class), captureur.capture()))
                    .thenAnswer(inv -> null);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID, null);

            // When / Then — aucune NullPointerException ne doit être levée
            try {
                captureur.getValue().onError(erreur);
            } catch (NullPointerException e) {
                fail("Un callback null ne doit pas provoquer de NullPointerException lors de onError");
            }
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Body JSON avec clé "message"
    // =========================================================================

    /**
     * Vérifie que {@code callback.onError} reçoit la valeur du champ "message"
     * lorsque le body d'erreur est un JSON contenant cette clé.
     *
     * <p><b>Given</b> une erreur 400 avec un JSON {@code {"message": "Poids maximum dépassé"}}<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> {@code callback.onError} reçoit "Poids maximum dépassé"</p>
     */
    @Test
    public void optimiserRandonnee_erreurAvecCleMessage_transmetsValeurMessage() {
        // Given
        String messageServeur = "Poids maximum dépassé pour cette randonnée";
        VolleyError erreur = buildErreurAvecMessageJson(400, messageServeur);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) {}
                        @Override public void onError(String message) { messageCapture.set(message); }
                    });

            // When
            captureur.getValue().onError(erreur);

            // Then
            assertEquals("Le message transmis doit correspondre au champ 'message' du JSON",
                    messageServeur, messageCapture.get());
        }
    }

    /**
     * Vérifie que {@code callback.onError} reçoit le JSON entier (en String)
     * lorsque le body d'erreur est un JSON valide mais ne contient pas la clé "message".
     *
     * <p><b>Given</b> une erreur avec un JSON sans clé "message"<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> {@code callback.onError} reçoit le JSON brut sous forme de String</p>
     */
    @Test
    public void optimiserRandonnee_erreurJsonSansCleMessage_transmetsJsonBrut() {
        // Given
        VolleyError erreur = buildErreurSansCleMessage(400);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) {}
                        @Override public void onError(String message) { messageCapture.set(message); }
                    });

            // When
            captureur.getValue().onError(erreur);

            // Then — le message ne doit pas être null et ne doit pas être le message par défaut
            assertNotNull("Le message ne doit pas être null", messageCapture.get());
            assertNotEquals("Le message ne doit pas être le message par défaut",
                    "Erreur réseau inconnue", messageCapture.get());
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Body non JSON
    // =========================================================================

    /**
     * Vérifie que {@code callback.onError} reçoit un message contenant le code HTTP
     * lorsque le body d'erreur n'est pas du JSON valide.
     *
     * <p><b>Given</b> une erreur 500 avec un body non JSON ("Erreur interne brute")<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> le message reçu contient le code HTTP 500</p>
     */
    @Test
    public void optimiserRandonnee_erreurBodyNonJson_messageContientCodeHttp() {
        // Given
        byte[] bodyNonJson = "Erreur interne brute du serveur".getBytes(StandardCharsets.UTF_8);
        VolleyError erreur = new VolleyErrorAvecCode(500, bodyNonJson);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) {}
                        @Override public void onError(String message) { messageCapture.set(message); }
                    });

            // When
            captureur.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit contenir le code HTTP 500",
                    messageCapture.get().contains("500"));
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Absence de networkResponse
    // =========================================================================

    /**
     * Vérifie que {@code callback.onError} reçoit le message Volley brut
     * lorsque {@code networkResponse} est null et que l'erreur a un message.
     *
     * <p><b>Given</b> une erreur Volley sans networkResponse avec le message "Timeout"<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> le message reçu est "Timeout"</p>
     */
    @Test
    public void optimiserRandonnee_erreurSansReseau_transmetsMessageVolley() {
        // Given
        VolleyError erreur = buildErreurSansReseau("Timeout");
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) {}
                        @Override public void onError(String message) { messageCapture.set(message); }
                    });

            // When
            captureur.getValue().onError(erreur);

            // Then
            assertEquals("Le message doit correspondre au message de l'erreur Volley",
                    "Timeout", messageCapture.get());
        }
    }

    /**
     * Vérifie que {@code callback.onError} reçoit le message par défaut
     * "Erreur réseau inconnue" lorsque {@code networkResponse} est null
     * et que {@code error.getMessage()} est aussi null.
     *
     * <p><b>Given</b> une erreur Volley sans networkResponse et sans message<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> le message reçu est "Erreur réseau inconnue"</p>
     */
    @Test
    public void optimiserRandonnee_erreurSansReseauSansMessage_messageParDefaut() {
        // Given — VolleyError sans networkResponse ni message textuel
        VolleyError erreurSilencieuse = new VolleyError();
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) {}
                        @Override public void onError(String message) { messageCapture.set(message); }
                    });

            // When
            captureur.getValue().onError(erreurSilencieuse);

            // Then
            assertEquals("Le message par défaut doit être 'Erreur réseau inconnue'",
                    "Erreur réseau inconnue", messageCapture.get());
        }
    }

    /**
     * Vérifie que {@code callback.onError} n'est jamais appelé lors d'un succès,
     * et que {@code callback.onSuccess} n'est jamais appelé lors d'une erreur.
     *
     * <p><b>Given</b> un appel à {@code optimiserRandonnee} suivi d'une erreur<br>
     * <b>When</b> Volley déclenche onError<br>
     * <b>Then</b> onSuccess n'est pas appelé</p>
     */
    @Test
    public void optimiserRandonnee_erreurReseau_onSuccessNonAppele() {
        // Given
        VolleyError erreur = buildErreurSansReseau("Connexion refusée");
        AtomicBoolean onSuccessAppele = new AtomicBoolean(false);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureur =
                    preparerCaptureurCallback(staticMock);

            ServiceOptimisation.optimiserRandonnee(mockContexte, TOKEN_VALIDE, RANDO_ID,
                    new ServiceOptimisation.OptimisationCallback() {
                        @Override public void onSuccess(JSONObject r) { onSuccessAppele.set(true); }
                        @Override public void onError(String message) { /* erreur attendue */ }
                    });

            // When
            captureur.getValue().onError(erreur);

            // Then
            assertFalse("onSuccess ne doit pas être appelé lors d'une erreur",
                    onSuccessAppele.get());
        }
    }
}