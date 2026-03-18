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

import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceInscription;

/**
 * Classe de tests unitaires pour {@link ServiceInscription}.
 *
 * <p>Teste la méthode {@code registerUser} en simulant les différents scénarios
 * possibles lors d'une tentative d'inscription :</p>
 * <ul>
 *     <li>Cas nominaux : inscription réussie, callbacks correctement appelés</li>
 *     <li>Cas limites : corps JSON bien formé, token null transmis, URL correcte</li>
 *     <li>Cas d'erreur : email déjà utilisé (400 avec "mail"), données invalides (400 générique),
 *         body d'erreur non JSON (400), autres codes HTTP, absence de réponse réseau</li>
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
public class ServiceInscriptionTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL de l'endpoint d'inscription */
    private static final String REGISTER_URL = "http://98.94.8.220:8080/users/register";

    /** Email valide pour les tests nominaux */
    private static final String EMAIL_VALIDE = "jean.dupont@example.com";

    /** Mot de passe valide */
    private static final String PASSWORD_VALIDE = "MotDePasse123!";

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android */
    @Mock
    private Context mockContexte;

    /** Mock du callback de succès */
    @Mock
    private ServiceInscription.ApiSuccessCallback mockOnSuccess;

    /** Mock du callback d'erreur */
    @Mock
    private ServiceInscription.ApiErrorCallback mockOnError;

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
         * @param body       Corps de la réponse d'erreur en bytes
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
     * Construit un {@link User} de test complet avec toutes les propriétés requises
     * pour l'inscription.
     *
     * @return Un {@link User} valide prêt à être inscrit
     */
    private User buildUserValide() {
        User user = new User();
        user.setMail(EMAIL_VALIDE);
        user.setPassword(PASSWORD_VALIDE);
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user.setAdresse("12 rue des Tests, Rodez");
        user.setAge(30);
        user.setNiveau(Level.DEBUTANT);
        user.setMorphologie(Morphology.MOYENNE);
        return user;
    }

    /**
     * Construit un {@link VolleyError} simulant une erreur 400 avec un corps JSON
     * contenant un message donné.
     *
     * @param messageJson Message à inclure dans le JSON {@code {"message": "..."}}
     * @return Un {@link VolleyErrorAvecCode} avec le body JSON encodé en UTF-8
     */
    private VolleyError buildErreur400AvecMessage(String messageJson) {
        String body = "{\"message\": \"" + messageJson + "\"}";
        return new VolleyErrorAvecCode(400, body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Construit un {@link VolleyError} simulant une erreur réseau sans réponse HTTP
     * (networkResponse est null, ex : timeout ou pas de connexion).
     *
     * @return Un {@link VolleyError} sans networkResponse
     */
    private VolleyError buildErreurSansReseau() {
        return new VolleyError("Timeout");
    }

    // =========================================================================
    // TESTS : CAS NOMINAUX — Inscription réussie
    // =========================================================================

    /**
     * Vérifie que {@link ServiceInscription.ApiSuccessCallback#onSuccess()} est appelé
     * lorsque le serveur retourne une réponse valide.
     *
     * <p><b>Given</b> un utilisateur valide et une réponse serveur de succès<br>
     * <b>When</b> Volley déclenche le callback onSuccess<br>
     * <b>Then</b> {@code onSuccess.onSuccess()} est invoqué une seule fois</p>
     */
    @Test
    public void registerUser_reponseSucces_appelleOnSuccess() throws JSONException {
        // Given
        AtomicBoolean onSuccessAppele = new AtomicBoolean(false);
        ServiceInscription.ApiSuccessCallback callbackSucces = () -> onSuccessAppele.set(true);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    callbackSucces, mockOnError);
            captureurCallback.getValue().onSuccess(new JSONObject());

            // Then
            assertTrue("onSuccess doit être appelé lors d'une inscription réussie",
                    onSuccessAppele.get());
        }
    }

    /**
     * Vérifie que {@link ServiceInscription.ApiErrorCallback} n'est pas appelé
     * lors d'une inscription réussie.
     *
     * <p><b>Given</b> un utilisateur valide et une réponse serveur de succès<br>
     * <b>When</b> Volley déclenche le callback onSuccess<br>
     * <b>Then</b> {@code onError} n'est jamais invoqué</p>
     */
    @Test
    public void registerUser_reponseSucces_onErrorNonAppele() throws JSONException {
        // Given
        AtomicBoolean onErrorAppele = new AtomicBoolean(false);
        ServiceInscription.ApiErrorCallback callbackErreur = msg -> onErrorAppele.set(true);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, callbackErreur);
            captureurCallback.getValue().onSuccess(new JSONObject());

            // Then
            assertFalse("onError ne doit pas être appelé lors d'une inscription réussie",
                    onErrorAppele.get());
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — Corps de la requête
    // =========================================================================

    /**
     * Vérifie que le corps JSON de la requête POST contient tous les champs
     * de l'utilisateur avec les bonnes valeurs.
     *
     * <p><b>Given</b> un utilisateur valide avec toutes ses propriétés<br>
     * <b>When</b> {@code registerUser} est appelé<br>
     * <b>Then</b> le body JSON contient mail, password, nom, prénom, adresse, age, niveau, morphologie</p>
     */
    @Test
    public void registerUser_corpsRequete_contientTousLesChampsUtilisateur() throws JSONException {
        // Given
        User user = buildUserValide();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, user, mockOnSuccess, mockOnError);

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps de la requête ne doit pas être null", body);
            assertEquals("Le mail doit correspondre", EMAIL_VALIDE, body.getString("mail"));
            assertEquals("Le password doit correspondre", PASSWORD_VALIDE, body.getString("password"));
            assertEquals("Le nom doit correspondre", "Dupont", body.getString("nom"));
            assertEquals("Le prénom doit correspondre", "Jean", body.getString("prenom"));
            assertEquals("L'adresse doit correspondre", "12 rue des Tests, Rodez", body.getString("adresse"));
            assertEquals("L'age doit correspondre", 30, body.getInt("age"));
            assertEquals("Le niveau doit être la valeur toString() de l'enum",
                    Level.DEBUTANT.toString(), body.getString("niveau"));
            assertEquals("La morphologie doit être la valeur toString() de l'enum",
                    Morphology.MOYENNE.toString(), body.getString("morphologie"));
        }
    }

    /**
     * Vérifie que le token passé à {@link AppelAPI#post} est null
     * (aucune authentification préalable requise pour s'inscrire).
     *
     * <p><b>Given</b> un appel à {@code registerUser}<br>
     * <b>When</b> AppelAPI.post est invoqué en interne<br>
     * <b>Then</b> le paramètre token est null</p>
     */
    @Test
    public void registerUser_tokenPasseAAppelAPI_estNull() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(anyString(), captureurToken.capture(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, mockOnError);

            // Then
            assertNull("Le token passé à AppelAPI doit être null pour l'inscription",
                    captureurToken.getValue());
        }
    }

    /**
     * Vérifie que l'URL utilisée par {@code registerUser} est bien l'URL
     * de l'endpoint d'inscription.
     *
     * <p><b>Given</b> un appel à {@code registerUser}<br>
     * <b>When</b> AppelAPI.post est invoqué<br>
     * <b>Then</b> l'URL correspond à {@code /users/register}</p>
     */
    @Test
    public void registerUser_urlUtilisee_estRegisterUrl() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), isNull(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, mockOnError);

            // Then
            assertEquals("L'URL doit pointer vers l'endpoint d'inscription",
                    REGISTER_URL, captureurUrl.getValue());
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Erreur 400 avec message contenant "mail"
    // =========================================================================

    /**
     * Vérifie que {@code onError} est appelé avec le message "adresse e-mail déjà utilisée"
     * lorsque le serveur retourne une erreur 400 dont le message JSON contient "mail".
     *
     * <p><b>Given</b> une erreur 400 avec un message JSON contenant "mail"<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit le message indiquant l'email déjà utilisé</p>
     */
    @Test
    public void registerUser_erreur400AvecMessageMail_messageEmailDejaUtilise() {
        // Given
        VolleyError erreur = buildErreur400AvecMessage("This mail is already taken");
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit mentionner l'adresse e-mail déjà utilisée",
                    messageCapture.get().toLowerCase().contains("e-mail")
                            || messageCapture.get().toLowerCase().contains("email"));
        }
    }

    /**
     * Vérifie que {@code onError} est appelé avec le message "adresse e-mail déjà utilisée"
     * lorsque le serveur retourne une erreur 400 dont le message JSON contient "email".
     *
     * <p><b>Given</b> une erreur 400 avec un message JSON contenant "email"<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit le message indiquant l'email déjà utilisé</p>
     */
    @Test
    public void registerUser_erreur400AvecMessageEmail_messageEmailDejaUtilise() {
        // Given
        VolleyError erreur = buildErreur400AvecMessage("This email already exists");
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit mentionner l'adresse e-mail déjà utilisée",
                    messageCapture.get().toLowerCase().contains("e-mail")
                            || messageCapture.get().toLowerCase().contains("email"));
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Erreur 400 générique
    // =========================================================================

    /**
     * Vérifie que {@code onError} est appelé avec le message "Données invalides"
     * lorsque le serveur retourne une erreur 400 dont le message JSON ne mentionne
     * ni "mail" ni "email".
     *
     * <p><b>Given</b> une erreur 400 avec un message JSON sans mention de mail<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit le message de données invalides</p>
     */
    @Test
    public void registerUser_erreur400MessageGenerique_messageDonneesInvalides() {
        // Given
        VolleyError erreur = buildErreur400AvecMessage("Invalid input data");
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit mentionner les données invalides",
                    messageCapture.get().toLowerCase().contains("invalides")
                            || messageCapture.get().toLowerCase().contains("données"));
        }
    }

    /**
     * Vérifie que {@code onError} est appelé avec le message "adresse e-mail déjà utilisée"
     * lorsque le serveur retourne une erreur 400 dont le body n'est pas du JSON valide.
     *
     * <p><b>Given</b> une erreur 400 avec un body non JSON (ex : texte brut)<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> le fallback de la {@code JSONException} interne est déclenché :
     * message "adresse e-mail déjà utilisée"</p>
     */
    @Test
    public void registerUser_erreur400BodyNonJson_messageFallbackEmailDejaUtilise() {
        // Given — body non JSON
        byte[] bodyNonJson = "Erreur non JSON brute du serveur".getBytes(StandardCharsets.UTF_8);
        VolleyError erreur = new VolleyErrorAvecCode(400, bodyNonJson);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le fallback doit mentionner l'adresse e-mail déjà utilisée",
                    messageCapture.get().toLowerCase().contains("e-mail")
                            || messageCapture.get().toLowerCase().contains("email"));
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Autres codes HTTP
    // =========================================================================

    /**
     * Vérifie que {@code onError} est appelé avec un message contenant le code HTTP
     * lorsque le serveur retourne un code différent de 400 (ex : 500).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 500 et un body<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit un message contenant "500"</p>
     */
    @Test
    public void registerUser_erreur500_messageContientCodeHttp() {
        // Given
        byte[] bodyErreur = "Internal server error".getBytes(StandardCharsets.UTF_8);
        VolleyError erreur = new VolleyErrorAvecCode(500, bodyErreur);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit contenir le code HTTP 500",
                    messageCapture.get().contains("500"));
        }
    }

    /**
     * Vérifie que {@code onError} est appelé avec un message contenant le code HTTP
     * lorsque le serveur retourne un code 409 (conflit).
     *
     * <p><b>Given</b> une erreur Volley avec le code HTTP 409<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit un message contenant "409"</p>
     */
    @Test
    public void registerUser_erreur409_messageContientCodeHttp() {
        // Given
        byte[] bodyErreur = "Conflict".getBytes(StandardCharsets.UTF_8);
        VolleyError erreur = new VolleyErrorAvecCode(409, bodyErreur);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit contenir le code HTTP 409",
                    messageCapture.get().contains("409"));
        }
    }

    // =========================================================================
    // TESTS : CAS D'ERREUR — Absence de réponse réseau
    // =========================================================================

    /**
     * Vérifie que {@code onError} est appelé avec le message "Erreur réseau"
     * lorsque {@code networkResponse} est null (pas de connexion internet ou timeout).
     *
     * <p><b>Given</b> une erreur Volley sans {@code networkResponse} (null)<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit le message indiquant un problème réseau</p>
     */
    @Test
    public void registerUser_erreurSansReseauResponse_messageErreurReseau() {
        // Given
        VolleyError erreurSansReseau = buildErreurSansReseau();
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreurSansReseau);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit mentionner une erreur réseau",
                    messageCapture.get().toLowerCase().contains("réseau")
                            || messageCapture.get().toLowerCase().contains("connexion"));
        }
    }

    /**
     * Vérifie que {@code onError} est appelé avec le message "Erreur réseau"
     * lorsque {@code networkResponse} est non null mais que {@code data} est null.
     *
     * <p><b>Given</b> une erreur Volley avec une networkResponse dont le body est null<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onError} reçoit le message d'erreur réseau</p>
     */
    @Test
    public void registerUser_erreurAvecReponseEtDataNull_messageErreurReseau() {
        // Given — networkResponse non null mais data null
        NetworkResponse reponseSansData = new NetworkResponse(200, null, false, 0, null);
        VolleyError erreurSansData = new VolleyError(reponseSansData);
        AtomicReference<String> messageCapture = new AtomicReference<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    mockOnSuccess, messageCapture::set);
            captureurCallback.getValue().onError(erreurSansData);

            // Then
            assertNotNull("Le message d'erreur ne doit pas être null", messageCapture.get());
            assertTrue("Le message doit mentionner une erreur réseau ou connexion",
                    messageCapture.get().toLowerCase().contains("réseau")
                            || messageCapture.get().toLowerCase().contains("connexion"));
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — Niveau et Morphologie dans le corps JSON
    // =========================================================================

    /**
     * Vérifie que le niveau de l'utilisateur est bien sérialisé via {@code toString()}
     * et non via {@code name()} dans le corps JSON.
     *
     * <p><b>Given</b> un utilisateur avec le niveau {@link Level#SPORTIF}<br>
     * <b>When</b> {@code registerUser} est appelé<br>
     * <b>Then</b> le champ "niveau" du body contient la valeur {@code toString()} de l'enum</p>
     */
    @Test
    public void registerUser_niveauSportif_estSerialiseAvecToString() throws JSONException {
        // Given
        User user = buildUserValide();
        user.setNiveau(Level.SPORTIF);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, user, mockOnSuccess, mockOnError);

            // Then
            JSONObject body = captureurBody.getValue();
            assertEquals("Le niveau doit être sérialisé via toString()",
                    Level.SPORTIF.toString(), body.getString("niveau"));
        }
    }

    /**
     * Vérifie que la morphologie de l'utilisateur est bien sérialisée via {@code toString()}
     * dans le corps JSON.
     *
     * <p><b>Given</b> un utilisateur avec la morphologie {@link Morphology#FORTE}<br>
     * <b>When</b> {@code registerUser} est appelé<br>
     * <b>Then</b> le champ "morphologie" du body contient la valeur {@code toString()} de l'enum</p>
     */
    @Test
    public void registerUser_morphologieForte_estSerialiseeAvecToString() throws JSONException {
        // Given
        User user = buildUserValide();
        user.setMorphologie(Morphology.FORTE);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, user, mockOnSuccess, mockOnError);

            // Then
            JSONObject body = captureurBody.getValue();
            assertEquals("La morphologie doit être sérialisée via toString()",
                    Morphology.FORTE.toString(), body.getString("morphologie"));
        }
    }

    // =========================================================================
    // TESTS : CAS LIMITES — onSuccess non appelé en cas d'erreur
    // =========================================================================

    /**
     * Vérifie que {@code onSuccess} n'est jamais appelé lorsque le serveur retourne
     * une erreur (ici 400).
     *
     * <p><b>Given</b> une erreur 400 retournée par le serveur<br>
     * <b>When</b> Volley déclenche le callback d'erreur<br>
     * <b>Then</b> {@code onSuccess} n'est jamais invoqué</p>
     */
    @Test
    public void registerUser_erreur400_onSuccessNonAppele() {
        // Given
        VolleyError erreur = buildErreur400AvecMessage("mail already used");
        AtomicBoolean onSuccessAppele = new AtomicBoolean(false);
        ServiceInscription.ApiSuccessCallback callbackSucces = () -> onSuccessAppele.set(true);

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<AppelAPI.VolleyObjectCallback> captureurCallback =
                    ArgumentCaptor.forClass(AppelAPI.VolleyObjectCallback.class);

            staticMock.when(() -> AppelAPI.post(anyString(), isNull(), any(JSONObject.class),
                            any(Context.class), captureurCallback.capture()))
                    .thenAnswer(inv -> null);

            // When
            ServiceInscription.registerUser(mockContexte, buildUserValide(),
                    callbackSucces, mockOnError);
            captureurCallback.getValue().onError(erreur);

            // Then
            assertFalse("onSuccess ne doit pas être appelé en cas d'erreur",
                    onSuccessAppele.get());
        }
    }
}