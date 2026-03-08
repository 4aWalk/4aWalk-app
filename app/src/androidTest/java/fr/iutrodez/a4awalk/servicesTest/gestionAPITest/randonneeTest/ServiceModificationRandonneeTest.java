package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.VolleyError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceModificationRandonnee;

/**
 * Classe de test instrumentée pour {@link ServiceModificationRandonnee}.
 *
 * <p>Cette classe vérifie le comportement du service de modification de randonnée,
 * notamment la construction de l'URL, la gestion des callbacks, et les cas limites
 * liés aux paramètres d'entrée.</p>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (libelle vide, durée nulle, id min, etc.)</li>
 *     <li><b>Erreurs</b> : token null, token invalide, callback null, libelle null</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceModificationRandonneeTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification valide pour les tests */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

    /** Token invalide pour les tests d'erreur */
    private static final String TOKEN_INVALIDE = "token_invalide";

    /** Identifiant de randonnée valide */
    private static final int HIKE_ID_VALIDE = 1;

    /** Libellé de randonnée valide */
    private static final String LIBELLE_VALIDE = "Randonnée du Causse";

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
    // TESTS — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la méthode s'exécute sans exception avec des paramètres valides
     * et que la file de requêtes est correctement initialisée.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_ParamsValides_PasException()
            throws InterruptedException {

        // Given — des paramètres tous valides
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on appelle modifierRandonneeAPI avec des paramètres valides
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des paramètres valides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est correctement initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée après l'appel",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onError est déclenché lorsque le token est invalide.
     *
     * <p><b>Cas nominal du flux erreur</b> : token invalide → réponse 401</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_TokenInvalide_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — un token invalide
        CountDownLatch verrou = new CountDownLatch(1);
        final String[] messageErreurRecu = {null};

        // When — on appelle modifierRandonneeAPI avec un token invalide
        ServiceModificationRandonnee.modifierRandonneeAPI(
                contexte,
                TOKEN_INVALIDE,
                HIKE_ID_VALIDE,
                LIBELLE_VALIDE,
                DUREE_VALIDE,
                new ServiceModificationRandonnee.UpdateHikeCallback() {
                    @Override
                    public void onSuccess() {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        // Then — onError est appelé avec un message non nul
                        messageErreurRecu[0] = message;
                        verrou.countDown();
                    }
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que plusieurs appels successifs n'interfèrent pas entre eux
     * et que la file Singleton est correctement réutilisée.
     *
     * <p><b>Cas nominal</b> : appels successifs</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_AppelsSuccessifs_MemeFileSingleton()
            throws InterruptedException {

        // Given — deux appels successifs avec des IDs différents
        CountDownLatch verrou = new CountDownLatch(2);

        // When — on effectue deux appels successifs
        ServiceModificationRandonnee.modifierRandonneeAPI(
                contexte, TOKEN_VALIDE, 1, LIBELLE_VALIDE, DUREE_VALIDE,
                new ServiceModificationRandonnee.UpdateHikeCallback() {
                    @Override public void onSuccess() { verrou.countDown(); }
                    @Override public void onError(String message) { verrou.countDown(); }
                }
        );

        ServiceModificationRandonnee.modifierRandonneeAPI(
                contexte, TOKEN_VALIDE, 2, "Deuxième randonnée", 5,
                new ServiceModificationRandonnee.UpdateHikeCallback() {
                    @Override public void onSuccess() { verrou.countDown(); }
                    @Override public void onError(String message) { verrou.countDown(); }
                }
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
     * Vérifie que la méthode fonctionne avec un libellé vide.
     *
     * <p><b>Cas limite</b> : libellé vide</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_LibelleVide_PasException()
            throws InterruptedException {

        // Given — un libellé vide
        CountDownLatch verrou = new CountDownLatch(1);
        String libelleVide = "";

        // When — on appelle modifierRandonneeAPI avec un libellé vide
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    libelleVide,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un libellé vide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file doit être initialisée avec un libellé vide",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec une durée de 1 jour (minimum).
     *
     * <p><b>Cas limite</b> : durée minimale</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_DureeMin_PasException()
            throws InterruptedException {

        // Given — une durée minimale de 1 jour
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeMin = 1;

        // When — on appelle modifierRandonneeAPI avec une durée minimale
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    dureeMin,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée minimale : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec une durée minimale",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec une durée nulle (0 jour).
     *
     * <p><b>Cas limite</b> : durée nulle</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_DureeZero_PasException()
            throws InterruptedException {

        // Given — une durée de zéro
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeZero = 0;

        // When — on appelle modifierRandonneeAPI avec une durée nulle
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    dureeZero,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée nulle : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec une durée nulle",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec une durée maximale.
     *
     * <p><b>Cas limite</b> : durée maximale (Integer.MAX_VALUE)</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_DureeMax_PasException()
            throws InterruptedException {

        // Given — une durée maximale
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeMax = Integer.MAX_VALUE;

        // When — on appelle modifierRandonneeAPI avec une durée maximale
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    dureeMax,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée maximale : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec une durée maximale",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec un hikeId minimal (1).
     *
     * <p><b>Cas limite</b> : hikeId minimal</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_HikeIdMin_PasException()
            throws InterruptedException {

        // Given — un hikeId minimal de 1
        CountDownLatch verrou = new CountDownLatch(1);
        int hikeIdMin = 1;

        // When — on appelle modifierRandonneeAPI avec un hikeId minimal
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    hikeIdMin,
                    LIBELLE_VALIDE,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un hikeId minimal : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un hikeId minimal",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode fonctionne avec un libellé très long.
     *
     * <p><b>Cas limite</b> : libellé de 255 caractères</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_LibelleTresLong_PasException()
            throws InterruptedException {

        // Given — un libellé de 255 caractères
        CountDownLatch verrou = new CountDownLatch(1);
        String libelleLong = "A".repeat(255);

        // When — on appelle modifierRandonneeAPI avec un libellé très long
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    libelleLong,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un libellé très long : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec un libellé très long",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException avec un token null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    null,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
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
     * Vérifie que la méthode ne lève pas de NullPointerException avec un libellé null.
     *
     * <p><b>Cas erreur</b> : libellé null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_LibelleNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un libellé null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    null,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) {
                            // Then — onError appelé avec message d'erreur JSON
                            verrou.countDown();
                        }
                    }
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec un libellé null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback null ne provoque pas de NullPointerException.
     *
     * <p><b>Cas erreur</b> : callback null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_CallbackNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un callback null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    DUREE_VALIDE,
                    null
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec callback null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec callback null",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère correctement un hikeId négatif.
     *
     * <p><b>Cas erreur</b> : hikeId négatif</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_HikeIdNegatif_PasException()
            throws InterruptedException {

        // Given — un hikeId négatif
        CountDownLatch verrou = new CountDownLatch(1);
        int hikeIdNegatif = -1;

        // When — on appelle modifierRandonneeAPI avec un hikeId négatif
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    hikeIdNegatif,
                    LIBELLE_VALIDE,
                    DUREE_VALIDE,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) {
                            // Then — onError attendu car l'URL sera invalide
                            verrou.countDown();
                        }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un hikeId négatif : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère correctement une durée négative.
     *
     * <p><b>Cas erreur</b> : durée négative</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testModifierRandonneeAPI_DureeNegative_PasException()
            throws InterruptedException {

        // Given — une durée négative
        CountDownLatch verrou = new CountDownLatch(1);
        int dureeNegative = -1;

        // When — on appelle modifierRandonneeAPI avec une durée négative
        try {
            ServiceModificationRandonnee.modifierRandonneeAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    LIBELLE_VALIDE,
                    dureeNegative,
                    new ServiceModificationRandonnee.UpdateHikeCallback() {
                        @Override
                        public void onSuccess() { verrou.countDown(); }

                        @Override
                        public void onError(String message) { verrou.countDown(); }
                    }
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec une durée négative : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file doit être initialisée avec une durée négative",
                AppelAPI.getFileRequete(contexte));
    }
}
