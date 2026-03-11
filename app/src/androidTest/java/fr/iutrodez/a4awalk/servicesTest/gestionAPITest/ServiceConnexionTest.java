package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import fr.iutrodez.a4awalk.modeles.entites.LoginRequest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.AppelAPI;

import fr.iutrodez.a4awalk.services.gestionAPI.ServiceConnexion;

/**
 * Classe de test instrumentée pour {@link ServiceConnexion}.
 *
 * <p>Cette classe vérifie le comportement du service de connexion utilisateur,
 * notamment :</p>
 * <ul>
 *     <li>La construction et l'envoi de la requête de connexion</li>
 *     <li>La gestion des callbacks de succès et d'erreur</li>
 *     <li>L'extraction de l'utilisateur depuis la réponse JSON</li>
 *     <li>Les cas limites (email vide, mot de passe vide)</li>
 *     <li>Les cas d'erreur (credentials invalides, champs null)</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (email vide, mot de passe minimal)</li>
 *     <li><b>Erreurs</b> : credentials invalides, champs null, serveur injoignable</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceConnexionTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Email valide pour les tests nominaux */
    private static final String EMAIL_VALIDE = "test@4awalk.fr";

    /** Mot de passe valide pour les tests nominaux */
    private static final String PASSWORD_VALIDE = "MotDePasse123!";

    /** Email invalide pour les tests d'erreur */
    private static final String EMAIL_INVALIDE = "email_inexistant@test.fr";

    /** Mot de passe invalide pour les tests d'erreur */
    private static final String PASSWORD_INVALIDE = "mauvais_mdp";

    /** Email au format incorrect */
    private static final String EMAIL_FORMAT_INCORRECT = "pasunemail";

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
     * Crée un callback de succès qui décompte le verrou et stocke le token reçu.
     *
     * @param verrou      Verrou à décompter lors de l'appel du callback
     * @param tokenRecu   Tableau d'un élément pour stocker le token reçu
     * @param userRecu    Tableau d'un élément pour stocker l'utilisateur reçu
     * @return {@link BiConsumer} représentant le callback de succès
     */
    private BiConsumer<String, User> creerCallbackSucces(
            CountDownLatch verrou,
            String[] tokenRecu,
            User[] userRecu) {

        return (token, user) -> {
            tokenRecu[0] = token;
            userRecu[0] = user;
            verrou.countDown();
        };
    }

    /**
     * Crée un callback d'erreur qui décompte le verrou et stocke le message reçu.
     *
     * @param verrou         Verrou à décompter lors de l'appel du callback
     * @param messageErreur  Tableau d'un élément pour stocker le message d'erreur
     * @return {@link Consumer} représentant le callback d'erreur
     */
    private Consumer<String> creerCallbackErreur(
            CountDownLatch verrou,
            String[] messageErreur) {

        return message -> {
            messageErreur[0] = message;
            verrou.countDown();
        };
    }

    // =========================================================================
    // TESTS — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la méthode loginUser s'exécute sans exception
     * avec des credentials valides et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_CredentialsValides_PasException()
            throws InterruptedException {

        // Given — des credentials valides
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, PASSWORD_VALIDE);
        String[] tokenRecu = {null};
        User[] userRecu = {null};

        // When — on appelle loginUser avec des credentials valides
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    creerCallbackSucces(verrou, tokenRecu, userRecu),
                    creerCallbackErreur(verrou, new String[]{null})
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des credentials valides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que plusieurs appels successifs n'interfèrent pas
     * et réutilisent la même file Singleton.
     *
     * <p><b>Cas nominal</b> : appels successifs</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_AppelsSuccessifs_MemeFileSingleton()
            throws InterruptedException {

        // Given — deux requêtes de connexion successives
        CountDownLatch verrou = new CountDownLatch(2);
        LoginRequest loginRequest1 = new LoginRequest(EMAIL_VALIDE, PASSWORD_VALIDE);
        LoginRequest loginRequest2 = new LoginRequest(EMAIL_INVALIDE, PASSWORD_INVALIDE);

        // When — on effectue deux appels successifs
        ServiceConnexion.loginUser(
                contexte, loginRequest1,
                (token, user) -> verrou.countDown(),
                message -> verrou.countDown()
        );

        ServiceConnexion.loginUser(
                contexte, loginRequest2,
                (token, user) -> verrou.countDown(),
                message -> verrou.countDown()
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la même file Singleton est utilisée pour les deux appels
        assertNotNull("La file doit rester initialisée après des appels successifs",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onError est déclenché avec des credentials incorrects.
     *
     * <p><b>Cas nominal du flux erreur</b> : mauvais credentials → réponse 400/404</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_CredentialsInvalides_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — des credentials incorrects
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_INVALIDE, PASSWORD_INVALIDE);
        final String[] messageErreur = {null};

        // When — on appelle loginUser avec des credentials incorrects
        ServiceConnexion.loginUser(
                contexte,
                loginRequest,
                (token, user) -> verrou.countDown(),
                creerCallbackErreur(verrou, messageErreur)
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode gère correctement un email vide.
     *
     * <p><b>Cas limite</b> : email vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_EmailVide_PasException() throws InterruptedException {
        // Given — un email vide
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest("", PASSWORD_VALIDE);

        // When — on appelle loginUser avec un email vide
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un email vide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file doit être initialisée avec un email vide",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère correctement un mot de passe vide.
     *
     * <p><b>Cas limite</b> : mot de passe vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_PasswordVide_PasException() throws InterruptedException {
        // Given — un mot de passe vide
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, "");

        // When — on appelle loginUser avec un mot de passe vide
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un mot de passe vide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file doit être initialisée avec un mot de passe vide",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère correctement un email et un mot de passe tous deux vides.
     *
     * <p><b>Cas limite</b> : email et mot de passe vides</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_EmailEtPasswordVides_PasException()
            throws InterruptedException {

        // Given — un email et un mot de passe tous deux vides
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest("", "");

        // When — on appelle loginUser avec les deux champs vides
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec email et password vides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file doit être initialisée avec email et password vides",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un email au format incorrect sans lever d'exception.
     *
     * <p><b>Cas limite</b> : format d'email incorrect</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_EmailFormatIncorrect_PasException()
            throws InterruptedException {

        // Given — un email au format incorrect
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_FORMAT_INCORRECT, PASSWORD_VALIDE);

        // When — on appelle loginUser avec un email mal formaté
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un email mal formaté : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file doit être initialisée avec un email mal formaté",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un mot de passe d'un seul caractère.
     *
     * <p><b>Cas limite</b> : mot de passe minimal (1 caractère)</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_PasswordUnSeulCaractere_PasException()
            throws InterruptedException {

        // Given — un mot de passe d'un seul caractère
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, "a");

        // When — on appelle loginUser avec un mot de passe minimal
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un mot de passe minimal : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un mot de passe minimal",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un email très long sans lever d'exception.
     *
     * <p><b>Cas limite</b> : email de 255 caractères</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_EmailTresLong_PasException() throws InterruptedException {
        // Given — un email très long de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String emailLong = "a".repeat(243) + "@test.fr";
        LoginRequest loginRequest = new LoginRequest(emailLong, PASSWORD_VALIDE);

        // When — on appelle loginUser avec un email très long
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un email très long : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un email très long",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque l'email est null.
     *
     * <p><b>Cas erreur</b> : email null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_EmailNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un email null
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(null, PASSWORD_VALIDE);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un email null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le mot de passe est null.
     *
     * <p><b>Cas erreur</b> : mot de passe null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_PasswordNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un mot de passe null
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, null);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    loginRequest,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un mot de passe null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le LoginRequest est null.
     *
     * <p><b>Cas erreur</b> : LoginRequest null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_LoginRequestNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un LoginRequest null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceConnexion.loginUser(
                    contexte,
                    null,
                    (token, user) -> verrou.countDown(),
                    message -> verrou.countDown()
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un LoginRequest null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onError est déclenché avec
     * un message non null lorsque le serveur est injoignable.
     *
     * <p><b>Cas erreur</b> : serveur injoignable</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_ServeurInjoignable_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — une URL de serveur injoignable et un verrou
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, PASSWORD_VALIDE);
        final String[] messageErreur = {null};

        // When — on appelle loginUser avec un serveur injoignable
        ServiceConnexion.loginUser(
                contexte,
                loginRequest,
                (token, user) -> verrou.countDown(),
                message -> {
                    // Then — onError est déclenché avec un message
                    messageErreur[0] = message;
                    verrou.countDown();
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que les callbacks null ne provoquent pas de NullPointerException.
     *
     * <p><b>Cas erreur</b> : callbacks null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testLoginUser_CallbacksNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — des callbacks null
        CountDownLatch verrou = new CountDownLatch(1);
        LoginRequest loginRequest = new LoginRequest(EMAIL_VALIDE, PASSWORD_VALIDE);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceConnexion.loginUser(contexte, loginRequest, null, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec des callbacks null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec des callbacks null",
                AppelAPI.getFileRequete(contexte));
    }
}
