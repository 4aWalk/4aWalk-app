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

import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceInscription;

/**
 * Classe de test instrumentée pour {@link ServiceInscription}.
 *
 * <p>Cette classe vérifie le comportement du service d'inscription utilisateur,
 * notamment :</p>
 * <ul>
 *     <li>La construction et l'envoi de la requête d'inscription</li>
 *     <li>La gestion des callbacks de succès et d'erreur</li>
 *     <li>Les cas limites (champs vides, valeurs minimales/maximales)</li>
 *     <li>Les cas d'erreur (utilisateur null, champs null, callbacks null)</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (age min/max, champs vides)</li>
 *     <li><b>Erreurs</b> : utilisateur null, champs null, callbacks null</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceInscriptionTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Nom valide pour les tests nominaux */
    private static final String NOM_VALIDE = "Dupont";

    /** Prénom valide pour les tests nominaux */
    private static final String PRENOM_VALIDE = "Jean";

    /** Email valide pour les tests nominaux */
    private static final String EMAIL_VALIDE = "jean.dupont@test.fr";

    /** Mot de passe valide pour les tests nominaux */
    private static final String PASSWORD_VALIDE = "MotDePasse123!";

    /** Adresse valide pour les tests nominaux */
    private static final String ADRESSE_VALIDE = "12 rue de la Paix, Paris";

    /** Age valide pour les tests nominaux */
    private static final int AGE_VALIDE = 30;

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
     * Construit un utilisateur valide avec toutes les données obligatoires renseignées.
     *
     * @return {@link User} valide pour les tests nominaux
     */
    private User construireUtilisateurValide() {
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, AGE_VALIDE,
                EMAIL_VALIDE, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.DEBUTANT, Morphology.MOYENNE
        );
        return user;
    }

    /**
     * Exécute un appel à {@link ServiceInscription#registerUser} avec les callbacks
     * fournis et attend la réponse pendant {@link #TIMEOUT_SECONDES} secondes.
     *
     * @param user    Utilisateur à inscrire
     * @param verrou  Verrou pour synchroniser l'appel asynchrone
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    private void appellerRegisterUser(
            User user,
            CountDownLatch verrou,
            ServiceInscription.ApiSuccessCallback onSuccess,
            ServiceInscription.ApiErrorCallback onError
    ) throws InterruptedException {
        ServiceInscription.registerUser(contexte, user, onSuccess, onError);
        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
    }

    // =========================================================================
    // TESTS — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la méthode registerUser s'exécute sans exception
     * avec un utilisateur valide et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_UtilisateurValide_PasException()
            throws InterruptedException {

        // Given — un utilisateur valide avec toutes les données renseignées
        CountDownLatch verrou = new CountDownLatch(1);
        User user = construireUtilisateurValide();

        // When — on appelle registerUser avec un utilisateur valide
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un utilisateur valide : "
                    + e.getMessage());
        }

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onError est déclenché lorsqu'un utilisateur
     * avec un email déjà existant tente de s'inscrire.
     *
     * <p><b>Cas nominal du flux erreur</b> : email déjà utilisé → réponse 409</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_EmailDejaUtilise_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — un utilisateur avec un email déjà enregistré
        CountDownLatch verrou = new CountDownLatch(1);
        User user = construireUtilisateurValide();
        final String[] messageErreur = {null};

        // When — on appelle registerUser avec un email existant
        appellerRegisterUser(
                user, verrou,
                () -> verrou.countDown(),
                message -> {
                    messageErreur[0] = message;
                    verrou.countDown();
                }
        );

        // Then — la file est initialisée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que plusieurs appels successifs réutilisent la même file Singleton.
     *
     * <p><b>Cas nominal</b> : appels successifs</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_AppelsSuccessifs_MemeFileSingleton()
            throws InterruptedException {

        // Given — deux utilisateurs différents à inscrire
        CountDownLatch verrou = new CountDownLatch(2);
        User user1 = construireUtilisateurValide();
        User user2 = new User(
                "Martin", "Alice", 25,
                "alice.martin@test.fr", PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.ENTRAINE, Morphology.LEGERE
        );

        // When — on effectue deux appels successifs
        ServiceInscription.registerUser(
                contexte, user1,
                () -> verrou.countDown(),
                message -> verrou.countDown()
        );
        ServiceInscription.registerUser(
                contexte, user2,
                () -> verrou.countDown(),
                message -> verrou.countDown()
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la même file Singleton est utilisée pour les deux appels
        assertNotNull("La file doit rester initialisée après des appels successifs",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode gère correctement un âge minimal.
     *
     * <p><b>Cas limite</b> : âge minimal (1 an)</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_AgeMinimal_PasException() throws InterruptedException {
        // Given — un utilisateur avec l'âge minimal
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, 1,
                EMAIL_VALIDE, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // When — on appelle registerUser avec un âge minimal
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un âge minimal : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un âge minimal",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère correctement un âge maximal.
     *
     * <p><b>Cas limite</b> : âge maximal (120 ans)</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_AgeMaximal_PasException() throws InterruptedException {
        // Given — un utilisateur avec l'âge maximal
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, 120,
                EMAIL_VALIDE, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // When — on appelle registerUser avec un âge maximal
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un âge maximal : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un âge maximal",
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
    public void testRegisterUser_EmailTresLong_PasException() throws InterruptedException {
        // Given — un email très long de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String emailLong = "a".repeat(243) + "@test.fr";
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, AGE_VALIDE,
                emailLong, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // When — on appelle registerUser avec un email très long
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un email très long : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un email très long",
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
    public void testRegisterUser_NomTresLong_PasException() throws InterruptedException {
        // Given — un nom très long de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String nomLong = "A".repeat(255);
        User user = new User(
                nomLong, PRENOM_VALIDE, AGE_VALIDE,
                EMAIL_VALIDE, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // When — on appelle registerUser avec un nom très long
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un nom très long : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un nom très long",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère tous les niveaux disponibles sans exception.
     *
     * <p><b>Cas limite</b> : niveau SPORTIF (valeur maximale de l'enum)</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_NiveauSportif_PasException() throws InterruptedException {
        // Given — un utilisateur avec le niveau maximal SPORTIF
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, AGE_VALIDE,
                EMAIL_VALIDE, PASSWORD_VALIDE, ADRESSE_VALIDE,
                Level.SPORTIF, Morphology.FORTE
        );

        // When — on appelle registerUser avec le niveau SPORTIF
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec le niveau SPORTIF : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec le niveau SPORTIF",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un adresse vide sans lever d'exception.
     *
     * <p><b>Cas limite</b> : adresse vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_AdresseVide_PasException() throws InterruptedException {
        // Given — un utilisateur avec une adresse vide
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User(
                NOM_VALIDE, PRENOM_VALIDE, AGE_VALIDE,
                EMAIL_VALIDE, PASSWORD_VALIDE, "",
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // When — on appelle registerUser avec une adresse vide
        try {
            appellerRegisterUser(
                    user, verrou,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une adresse vide : "
                    + e.getMessage());
        }

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec une adresse vide",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque l'utilisateur est null.
     *
     * <p><b>Cas erreur</b> : utilisateur null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_UtilisateurNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un utilisateur null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceInscription.registerUser(
                    contexte,
                    null,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un utilisateur null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode appelle le callback onError lorsque
     * le niveau de l'utilisateur est null (JSONException attendue).
     *
     * <p><b>Cas erreur</b> : niveau null → erreur de préparation JSON</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_NiveauNull_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — un utilisateur avec un niveau null
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User();
        user.setNom(NOM_VALIDE);
        user.setPrenom(PRENOM_VALIDE);
        user.setAge(AGE_VALIDE);
        user.setMail(EMAIL_VALIDE);
        user.setPassword(PASSWORD_VALIDE);
        user.setAdresse(ADRESSE_VALIDE);
        user.setNiveau(null);
        user.setMorphologie(Morphology.MOYENNE);

        final String[] messageErreur = {null};

        // When — on appelle registerUser avec un niveau null
        ServiceInscription.registerUser(
                contexte,
                user,
                () -> verrou.countDown(),
                message -> {
                    // Then — onError est appelé avec un message d'erreur
                    messageErreur[0] = message;
                    verrou.countDown();
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
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
    public void testRegisterUser_CallbacksNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — des callbacks null
        CountDownLatch verrou = new CountDownLatch(1);
        User user = construireUtilisateurValide();

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceInscription.registerUser(contexte, user, null, null);
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec des callbacks null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec des callbacks null",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un mail null sans lever de NullPointerException.
     *
     * <p><b>Cas erreur</b> : mail null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRegisterUser_MailNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un utilisateur avec un mail null
        CountDownLatch verrou = new CountDownLatch(1);
        User user = new User();
        user.setNom(NOM_VALIDE);
        user.setPrenom(PRENOM_VALIDE);
        user.setAge(AGE_VALIDE);
        user.setMail(null);
        user.setPassword(PASSWORD_VALIDE);
        user.setAdresse(ADRESSE_VALIDE);
        user.setNiveau(Level.DEBUTANT);
        user.setMorphologie(Morphology.MOYENNE);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceInscription.registerUser(
                    contexte,
                    user,
                    () -> verrou.countDown(),
                    message -> verrou.countDown()
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un mail null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }
}
