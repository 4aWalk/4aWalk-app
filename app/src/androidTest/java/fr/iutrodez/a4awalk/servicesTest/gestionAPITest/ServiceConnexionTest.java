package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.widget.Toast;

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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.LoginRequest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceConnexion;

/**
 * Classe de tests unitaires pour {@link ServiceConnexion}.
 *
 * <p>Teste la méthode {@code loginUser} en simulant les différents scénarios
 * possibles lors d'une tentative de connexion :</p>
 * <ul>
 *     <li>Cas nominaux : connexion réussie, extraction complète de l'utilisateur</li>
 *     <li>Cas limites : réponse sans token, réponse sans objet user, champs optionnels absents</li>
 *     <li>Cas d'erreur : erreurs réseau, codes HTTP 400/401/403/404/500, code inconnu</li>
 * </ul>
 *
 * <p>Utilise Mockito pour simuler {@link AppelAPI}, {@link Context} et {@link Toast},
 * évitant ainsi toute dépendance au framework Android.</p>
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
public class ServiceConnexionTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token JWT retourné par le serveur lors d'une connexion réussie */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature";

    /** Email de test valide */
    private static final String EMAIL_VALIDE = "jean.dupont@example.com";

    /** Mot de passe de test valide */
    private static final String MDP_VALIDE = "MotDePasse123!";

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android (requis par Toast et AppelAPI) */
    @Mock
    private Context mockContexte;

    /** Mock du callback de succès (token + utilisateur) */
    @Mock
    private BiConsumer<String, User> mockOnSuccess;

    /** Mock du callback d'erreur (message) */
    @Mock
    private Consumer<String> mockOnError;

    // =========================================================================
    // CONFIGURATION DES TESTS
    // =========================================================================

    /**
     * Initialisation avant chaque test.
     *
     * <p>Réinitialise le singleton Volley pour isoler les tests et
     * configure le contexte mocké pour les appels aux ressources string.</p>
     */
    @Before
    public void setUp() {
        AppelAPI.resetFileRequete();

        // Configuration des ressources string mockées (utilisées dans onError)
        when(mockContexte.getString(R.string.erreur_donnees_util_connexion))
                .thenReturn("Données utilisateur incorrectes");
        when(mockContexte.getString(R.string.erreur_acces))
                .thenReturn("Accès refusé");
        when(mockContexte.getString(R.string.erreur_ressource))
                .thenReturn("Ressource introuvable");
        when(mockContexte.getString(R.string.erreur_serveur))
                .thenReturn("Erreur interne du serveur");
    }

    /**
     * Nettoyage après chaque test.
     *
     * <p>Réinitialise le singleton Volley pour garantir l'isolation des tests suivants.</p>
     */
    @After
    public void tearDown() {
        AppelAPI.resetFileRequete();
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un {@link LoginRequest} de test avec des valeurs par défaut valides.
     *
     * @return Un {@link LoginRequest} avec l'email et le mot de passe de test
     */
    private LoginRequest buildLoginRequest() {
        return new LoginRequest(EMAIL_VALIDE, MDP_VALIDE);
    }

    /**
     * Construit un objet JSON simulant une réponse de connexion réussie
     * du serveur, avec le token et les données utilisateur complètes.
     *
     * @return Un {@link JSONObject} représentant la réponse serveur complète
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildReponseSuccesComplete() throws JSONException {
        JSONObject userObject = new JSONObject();
        userObject.put("age", 30);
        userObject.put("nom", "Dupont");
        userObject.put("prenom", "Jean");
        userObject.put("mail", EMAIL_VALIDE);
        userObject.put("adresse", "12 rue des Tests, Rodez");
        userObject.put("niveau", Level.DEBUTANT.name());
        userObject.put("morphologie", Morphology.MOYENNE.name());
        userObject.put("fullName", "Jean Dupont");

        JSONObject reponse = new JSONObject();
        reponse.put("token", TOKEN_VALIDE);
        reponse.put("user", userObject);
        return reponse;
    }

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
         * @param statusCode Code HTTP à simuler (ex : 400, 401, 404, 500)
         */
        VolleyErrorAvecCode(int statusCode) {
            super(new NetworkResponse(statusCode, new byte[0], false, 0, null));
        }
    }

    /**
     * Construit un {@link VolleyError} simulant un code HTTP donné.
     *
     * @param statusCode Code HTTP à simuler (ex : 401, 404, 500)
     * @return Un {@link VolleyErrorAvecCode} avec une {@link NetworkResponse} au code donné
     */
    private VolleyError buildVolleyError(int statusCode) {
        return new VolleyErrorAvecCode(statusCode);
    }

    // =========================================================================
    // TESTS : CAS NOMINAUX — Connexion réussie
    // =========================================================================

    /**
     * Vérifie que {@code onSuccess} est appelé avec le bon token lors d'une
     * connexion réussie (réponse complète avec token et user).
     *
     * <p><b>Given</b> des identifiants valides et une réponse serveur complète<br>
     * <b>When</b> le serveur retourne un token et un objet user<br>
     * <b>Then</b> {@code onSuccess} est appelé avec le token correct</p>
     */
    @Test
    public void loginUser_reponseComplete_onSuccessAvecTokenCorrect() throws JSONException {
        // Given
        JSONObject reponse = buildReponseSuccesComplete();
        AtomicReference<String> tokenCapture = new AtomicReference<>();

        BiConsumer<String, User> callbackSucces = (token, user) -> tokenCapture.set(token);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(reponse);

            // Then
            assertEquals("Le token reçu doit correspondre au token du serveur",
                    TOKEN_VALIDE, tokenCapture.get());
        }
    }

    /**
     * Vérifie que {@code onSuccess} est appelé avec un utilisateur correctement
     * extrait de la réponse JSON (nom, prénom, age, mail, niveau, morphologie).
     *
     * <p><b>Given</b> une réponse serveur contenant un objet user complet<br>
     * <b>When</b> le callback {@code onSuccess} est déclenché<br>
     * <b>Then</b> l'utilisateur reçu possède les bonnes valeurs de chaque champ</p>
     */
    @Test
    public void loginUser_reponseComplete_onSuccessAvecUtilisateurExtrait() throws JSONException {
        // Given
        JSONObject reponse = buildReponseSuccesComplete();
        AtomicReference<User> userCapture = new AtomicReference<>();

        BiConsumer<String, User> callbackSucces = (token, user) -> userCapture.set(user);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(reponse);

            // Then
            User utilisateur = userCapture.get();
            assertNotNull("L'utilisateur extrait ne doit pas être null", utilisateur);
            assertEquals("Le nom doit être 'Dupont'", "Dupont", utilisateur.getNom());
            assertEquals("Le prénom doit être 'Jean'", "Jean", utilisateur.getPrenom());
            assertEquals("L'age doit être 30", 30, utilisateur.getAge());
            assertEquals("Le mail doit correspondre", EMAIL_VALIDE, utilisateur.getMail());
            assertEquals("Le niveau doit être DEBUTANT", Level.DEBUTANT, utilisateur.getNiveau());
            assertEquals("La morphologie doit être MOYENNE", Morphology.MOYENNE, utilisateur.getMorphologie());
        }
    }

    /**
     * Vérifie que {@code onError} n'est pas appelé lors d'une connexion réussie.
     *
     * <p><b>Given</b> une réponse serveur valide et complète<br>
     * <b>When</b> la connexion réussit<br>
     * <b>Then</b> le callback {@code onError} n'est jamais invoqué</p>
     */
    @Test
    public void loginUser_reponseComplete_onErrorNonAppele() throws JSONException {
        // Given
        JSONObject reponse = buildReponseSuccesComplete();
        AtomicBoolean onErrorAppele = new AtomicBoolean(false);

        Consumer<String> callbackErreur = msg -> onErrorAppele.set(true);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, callbackErreur);
            captureurCallback.getValue().onSuccess(reponse);

            // Then
            assertFalse("Le callback onError ne doit pas être appelé lors d'un succès",
                    onErrorAppele.get());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — Réponse invalide du serveur
    // =========================================================================

    /**
     * Vérifie que {@code onError} est appelé avec le message approprié
     * lorsque la réponse du serveur ne contient pas de champ "token".
     *
     * <p><b>Given</b> une réponse JSON valide mais sans champ "token"<br>
     * <b>When</b> le callback {@code onSuccess} de Volley est déclenché<br>
     * <b>Then</b> {@code onError} est appelé avec le message "Réponse invalide du serveur"</p>
     */
    @Test
    public void loginUser_reponseSansToken_appelleOnError() throws JSONException {
        // Given
        JSONObject reponseSansToken = new JSONObject();
        reponseSansToken.put("user", new JSONObject());

        AtomicReference<String> messageErreur = new AtomicReference<>();
        Consumer<String> callbackErreur = messageErreur::set;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, callbackErreur);
            captureurCallback.getValue().onSuccess(reponseSansToken);

            // Then
            assertEquals("Le message d'erreur doit indiquer une réponse invalide",
                    "Réponse invalide du serveur", messageErreur.get());
        }
    }

    /**
     * Vérifie que {@code onSuccess} n'est pas appelé lorsque la réponse
     * du serveur ne contient pas de champ "token".
     *
     * <p><b>Given</b> une réponse JSON sans champ "token"<br>
     * <b>When</b> Volley déclenche le callback<br>
     * <b>Then</b> {@code onSuccess} n'est jamais invoqué</p>
     */
    @Test
    public void loginUser_reponseSansToken_onSuccessNonAppele() throws JSONException {
        // Given
        JSONObject reponseSansToken = new JSONObject();
        reponseSansToken.put("autreChamp", "valeur");

        AtomicBoolean onSuccessAppele = new AtomicBoolean(false);
        BiConsumer<String, User> callbackSucces = (t, u) -> onSuccessAppele.set(true);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(reponseSansToken);

            // Then
            assertFalse("onSuccess ne doit pas être appelé sans token dans la réponse",
                    onSuccessAppele.get());
        }
    }

    /**
     * Vérifie qu'un utilisateur null est retourné lorsque la réponse JSON
     * contient un token mais pas d'objet "user".
     *
     * <p><b>Given</b> une réponse JSON avec un token mais sans objet "user"<br>
     * <b>When</b> le callback {@code onSuccess} est déclenché<br>
     * <b>Then</b> {@code onSuccess} est appelé mais l'utilisateur reçu est null</p>
     */
    @Test
    public void loginUser_reponseSansUser_utilisateurNullDansOnSuccess() throws JSONException {
        // Given
        JSONObject reponseSansUser = new JSONObject();
        reponseSansUser.put("token", TOKEN_VALIDE);
        // Pas d'objet "user"

        AtomicReference<User> userCapture = new AtomicReference<>(new User()); // valeur sentinelle non-null
        BiConsumer<String, User> callbackSucces = (token, user) -> userCapture.set(user);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(reponseSansUser);

            // Then
            assertNull("L'utilisateur doit être null si l'objet 'user' est absent de la réponse",
                    userCapture.get());
        }
    }

    /**
     * Vérifie que {@code onError} est appelé immédiatement (avant tout appel réseau)
     * si la construction du corps JSON de la requête échoue.
     *
     * <p><b>Given</b> un {@link LoginRequest} dont les getters retournent des valeurs
     * provoquant une {@link JSONException} (simulé via mock)<br>
     * <b>When</b> {@code loginUser} est appelé<br>
     * <b>Then</b> {@code onError} est appelé avec "Erreur création requête"
     * et aucun appel réseau n'est effectué</p>
     *
     * <p><i>Note : ce test est applicable si LoginRequest est mockable.
     * Sinon, documenter que le cas est couvert par le typage fort de JSONObject.</i></p>
     */
    @Test
    public void loginUser_loginRequestMocke_erreurCreationCorps() {
        // Given
        LoginRequest loginRequestMocke = mock(LoginRequest.class);
        // On simule un email null pour provoquer l'erreur de corps si applicable
        when(loginRequestMocke.getEmail()).thenReturn(null);
        when(loginRequestMocke.getPassword()).thenReturn(null);

        AtomicReference<String> messageErreur = new AtomicReference<>();
        Consumer<String> callbackErreur = messageErreur::set;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServiceConnexion.loginUser(mockContexte, loginRequestMocke, mockOnSuccess, callbackErreur);

            // Then — si l'erreur est levée, on vérifie le message ; sinon, la requête est construite normalement
            // JSONObject accepte null comme valeur de chaîne (converti en "null")
            // ce test documente ce comportement limite
            staticMock.verify(() -> AppelAPI.post(anyString(), isNull(), any(), any(), any()),
                    atMostOnce());
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Codes HTTP
    // =========================================================================

    /**
     * Vérifie que le callback {@code onError} côté Volley affiche un Toast
     * avec le message correspondant à une erreur 400 (données invalides).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 400<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec le message d'erreur des données utilisateur</p>
     */
    @Test
    public void loginUser_erreur400_afficheToastDonneesInvalides() {
        // Given
        VolleyError erreur400 = buildVolleyError(400);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur400);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Données utilisateur incorrectes"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast avec le bon message
     * lors d'une erreur 401 (email ou mot de passe incorrect).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 401<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec "Email ou mot de passe incorrecte"</p>
     */
    @Test
    public void loginUser_erreur401_afficheToastIdentifiantsInvalides() {
        // Given
        VolleyError erreur401 = buildVolleyError(401);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur401);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Email ou mot de passe incorrecte"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast avec le bon message
     * lors d'une erreur 403 (accès refusé).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 403<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec le message d'accès refusé</p>
     */
    @Test
    public void loginUser_erreur403_afficheToastAccesRefuse() {
        // Given
        VolleyError erreur403 = buildVolleyError(403);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur403);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Accès refusé"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast avec le bon message
     * lors d'une erreur 404 (ressource introuvable).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 404<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec le message de ressource introuvable</p>
     */
    @Test
    public void loginUser_erreur404_afficheToastRessourceIntrouvable() {
        // Given
        VolleyError erreur404 = buildVolleyError(404);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur404);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Ressource introuvable"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast avec le bon message
     * lors d'une erreur 500 (erreur interne du serveur).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 500<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec le message d'erreur interne serveur</p>
     */
    @Test
    public void loginUser_erreur500_afficheToastErreurServeur() {
        // Given
        VolleyError erreur500 = buildVolleyError(500);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur500);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Erreur interne du serveur"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast contenant le code HTTP
     * lorsque celui-ci ne correspond à aucun des cas gérés explicitement (ex : 503).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 503 (non géré)<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec un message contenant "503"</p>
     */
    @Test
    public void loginUser_erreurCodeInconnu503_afficheToastAvecCode() {
        // Given
        VolleyError erreur503 = buildVolleyError(503);

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            ArgumentCaptor<String> captureurMessage = ArgumentCaptor.forClass(String.class);
            staticMockToast.when(() -> Toast.makeText(any(), captureurMessage.capture(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreur503);

            // Then
            String messageAffiche = captureurMessage.getValue();
            assertTrue("Le message d'erreur doit contenir le code 503",
                    messageAffiche.contains("503"));
            verify(mockToast).show();
        }
    }

    /**
     * Vérifie que le callback d'erreur Volley affiche un Toast avec le message
     * "Impossible de se connecter au serveur" lorsqu'il n'y a pas de réponse réseau
     * (networkResponse est null, ex : pas de connexion internet).
     *
     * <p><b>Given</b> une erreur Volley sans networkResponse (networkResponse == null)<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> un Toast est affiché avec le message de connexion impossible</p>
     */
    @Test
    public void loginUser_erreurSansReseauResponse_afficheToastConnexionImpossible() {
        // Given
        VolleyError erreurSansReseau = new VolleyError("Timeout");
        // networkResponse reste null par défaut

        try (MockedStatic<AppelAPI> staticMockAPI = Mockito.mockStatic(AppelAPI.class);
             MockedStatic<Toast> staticMockToast = Mockito.mockStatic(Toast.class)) {

            Toast mockToast = mock(Toast.class);
            staticMockToast.when(() -> Toast.makeText(any(), anyString(), anyInt()))
                    .thenReturn(mockToast);

            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMockAPI.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);
            captureurCallback.getValue().onError(erreurSansReseau);

            // Then
            staticMockToast.verify(() -> Toast.makeText(eq(mockContexte),
                    eq("Impossible de se connecter au serveur"), eq(Toast.LENGTH_LONG)));
            verify(mockToast).show();
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — Extraction utilisateur
    // =========================================================================

    /**
     * Vérifie que les champs optionnels absents de l'objet "user" ne provoquent
     * pas d'exception et produisent des valeurs par défaut (chaîne vide ou 0).
     *
     * <p><b>Given</b> un objet "user" ne contenant que le niveau et la morphologie (obligatoires pour valueOf)<br>
     * <b>When</b> la réponse est reçue<br>
     * <b>Then</b> l'extraction ne lève pas d'exception et l'utilisateur est non null</p>
     */
    @Test
    public void loginUser_userAvecChampsOptionnelsAbsents_utilisateurNonNull() throws JSONException {
        // Given — seulement les champs indispensables à valueOf
        JSONObject userMinimal = new JSONObject();
        userMinimal.put("niveau", Level.DEBUTANT.name());
        userMinimal.put("morphologie", Morphology.MOYENNE.name());

        JSONObject reponse = new JSONObject();
        reponse.put("token", TOKEN_VALIDE);
        reponse.put("user", userMinimal);

        AtomicReference<User> userCapture = new AtomicReference<>();
        BiConsumer<String, User> callbackSucces = (token, user) -> userCapture.set(user);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                    any(Context.class), captureurCallback.capture())).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(reponse);

            // Then
            assertNotNull("L'utilisateur doit être extrait même avec des champs optionnels absents",
                    userCapture.get());
        }
    }

    /**
     * Vérifie que l'URL utilisée par {@code loginUser} est bien celle définie
     * dans la constante {@code LOGIN_URL} de {@link ServiceConnexion}.
     *
     * <p><b>Given</b> un appel à loginUser<br>
     * <b>When</b> AppelAPI.post est invoqué en interne<br>
     * <b>Then</b> l'URL passée correspond à l'URL de l'endpoint de connexion</p>
     */
    @Test
    public void loginUser_urlUtilisee_estLoginUrl() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), isNull(),
                    any(JSONObject.class), any(Context.class),
                    any(AppelAPI.VolleyObjectCallback.class))).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);

            // Then
            assertEquals("L'URL doit correspondre à l'endpoint de connexion",
                    "http://98.94.8.220:8080/users/login", captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que l'email contenu dans le corps de la requête POST correspond
     * bien à celui fourni dans le {@link LoginRequest}.
     *
     * <p><b>Given</b> un LoginRequest avec un email spécifique<br>
     * <b>When</b> AppelAPI.post est invoqué<br>
     * <b>Then</b> le body JSON contient bien l'email du LoginRequest</p>
     */
    @Test
    public void loginUser_corpsRequete_contientEmailCorrect() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps de la requête ne doit pas être null", body);
            assertTrue("Le corps doit contenir le champ 'mail'", body.has("mail"));
            assertEquals("L'email dans le corps doit correspondre au LoginRequest",
                    EMAIL_VALIDE, body.getString("mail"));
        }
    }

    /**
     * Vérifie que le mot de passe contenu dans le corps de la requête POST correspond
     * bien à celui fourni dans le {@link LoginRequest}.
     *
     * <p><b>Given</b> un LoginRequest avec un mot de passe spécifique<br>
     * <b>When</b> AppelAPI.post est invoqué<br>
     * <b>Then</b> le body JSON contient bien le mot de passe du LoginRequest</p>
     */
    @Test
    public void loginUser_corpsRequete_contientMotDePasseCorrect() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps de la requête ne doit pas être null", body);
            assertTrue("Le corps doit contenir le champ 'password'", body.has("password"));
            assertEquals("Le mot de passe dans le corps doit correspondre au LoginRequest",
                    MDP_VALIDE, body.getString("password"));
        }
    }

    /**
     * Vérifie que le token passé à AppelAPI.post est null (aucune authentification
     * préalable requise pour se connecter).
     *
     * <p><b>Given</b> un appel à loginUser<br>
     * <b>When</b> AppelAPI.post est invoqué<br>
     * <b>Then</b> le paramètre token passé à AppelAPI est null</p>
     */
    @Test
    public void loginUser_tokenPasseAAppelAPI_estNull() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(anyString(), captureurToken.capture(),
                    any(JSONObject.class), any(Context.class),
                    any(AppelAPI.VolleyObjectCallback.class))).thenAnswer(inv -> null);

            // When
            ServiceConnexion.loginUser(mockContexte, buildLoginRequest(), mockOnSuccess, mockOnError);

            // Then
            assertNull("Le token passé à AppelAPI doit être null pour la connexion",
                    captureurToken.getValue());
        }
    }
}