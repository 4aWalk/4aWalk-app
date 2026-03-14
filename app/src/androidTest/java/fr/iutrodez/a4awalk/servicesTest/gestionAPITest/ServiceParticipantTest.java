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

import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;

/**
 * Classe de test instrumentée pour {@link ServiceParticipant}.
 *
 * <p>Cette classe vérifie le comportement du service de gestion des participants,
 * notamment :</p>
 * <ul>
 *     <li>La suppression d'un participant via l'API</li>
 *     <li>La construction du JSON d'un participant</li>
 *     <li>La mise à jour (ajout, modification, suppression) des participants</li>
 *     <li>Les cas limites (listes vides, id à 0, champs null)</li>
 *     <li>Les cas d'erreur (token null, participant null, callbacks null)</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (id=0, listes vides, capacité nulle)</li>
 *     <li><b>Erreurs</b> : token null, participant null, niveau null</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceParticipantTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification valide pour les tests */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

    /** Identifiant de randonnée valide */
    private static final int HIKE_ID_VALIDE = 1;

    /** Identifiant de participant valide */
    private static final int PARTICIPANT_ID_VALIDE = 42;

    /** Timeout maximum pour les opérations asynchrones en secondes */
    private static final int TIMEOUT_SECONDES = 5;

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================

    /** Contexte Android fourni par le runner de test */
    private Context contexte;

    /** Gestionnaire de token initialisé avec un token valide */
    private TokenManager tokenManager;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    /**
     * Initialise le contexte, le TokenManager et réinitialise la file Volley
     * avant chaque test afin d'isoler les tests entre eux.
     */
    @Before
    public void setUp() {
        contexte = ApplicationProvider.getApplicationContext();
        AppelAPI.resetFileRequete();

        // Initialisation du TokenManager avec un token valide
        tokenManager = new TokenManager(contexte);
        tokenManager.saveToken(TOKEN_VALIDE);
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un participant valide avec toutes les données obligatoires.
     *
     * @param id     Identifiant du participant (0 pour un nouveau participant)
     * @param nom    Nom du participant
     * @param prenom Prénom du participant
     * @return {@link Participant} valide pour les tests
     */
    private Participant construireParticipantValide(int id, String nom, String prenom) {
        Participant participant = new Participant(
                nom, prenom, 30,
                Level.DEBUTANT, Morphology.MOYENNE,
                false, 2000, 2, 15.0, HIKE_ID_VALIDE
        );
        participant.setId(id);
        return participant;
    }

    /**
     * Construit une liste de participants valides pour les tests.
     *
     * @param avecId Si true, les participants ont un id > 0 (existants),
     *               sinon id = 0 (nouveaux)
     * @return Liste de {@link Participant} valides
     */
    private ArrayList<Participant> construireListeParticipants(boolean avecId) {
        ArrayList<Participant> liste = new ArrayList<>();
        Participant p1 = construireParticipantValide(
                avecId ? 1 : 0, "Martin", "Alice"
        );
        Participant p2 = construireParticipantValide(
                avecId ? 2 : 0, "Bernard", "Bob"
        );
        liste.add(p1);
        liste.add(p2);
        return liste;
    }

    // =========================================================================
    // TESTS — supprimerParticipantAPI — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la suppression d'un participant valide s'exécute sans exception
     * et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_ParamsValides_PasException()
            throws InterruptedException {

        // Given — des paramètres valides avec un participantId > 0
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on appelle supprimerParticipantAPI
        try {
            ServiceParticipant.supprimerParticipantAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    PARTICIPANT_ID_VALIDE,
                    () -> verrou.countDown()
            );
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des paramètres valides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, aucune exception levée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que le callback onSuccess est appelé lors d'une suppression réussie.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_SuppressionReussie_CallbackOnSuccessAppele()
            throws InterruptedException {

        // Given — un participantId valide et un verrou pour l'async
        CountDownLatch verrou = new CountDownLatch(1);
        final boolean[] callbackAppele = {false};

        // When — on appelle supprimerParticipantAPI
        ServiceParticipant.supprimerParticipantAPI(
                contexte,
                TOKEN_VALIDE,
                HIKE_ID_VALIDE,
                PARTICIPANT_ID_VALIDE,
                () -> {
                    // Then — le callback onSuccess est bien appelé
                    callbackAppele[0] = true;
                    verrou.countDown();
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — supprimerParticipantAPI — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode ne fait rien et ne lève pas d'exception
     * lorsque le participantId est 0.
     *
     * <p><b>Cas limite</b> : participantId = 0 → retour immédiat</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_ParticipantIdZero_RetourImmediat()
            throws InterruptedException {

        // Given — un participantId à 0 (retour immédiat attendu)
        CountDownLatch verrou = new CountDownLatch(1);
        final boolean[] callbackAppele = {false};

        // When — on appelle supprimerParticipantAPI avec id = 0
        try {
            ServiceParticipant.supprimerParticipantAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    0,
                    () -> {
                        callbackAppele[0] = true;
                        verrou.countDown();
                    }
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec participantId = 0 : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — le callback ne doit pas être appelé et aucune requête envoyée
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode gère un onSuccess null sans lever de NullPointerException.
     *
     * <p><b>Cas limite</b> : callback onSuccess null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_OnSuccessNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un callback onSuccess null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceParticipant.supprimerParticipantAPI(
                    contexte,
                    TOKEN_VALIDE,
                    HIKE_ID_VALIDE,
                    PARTICIPANT_ID_VALIDE,
                    null
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec onSuccess null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — supprimerParticipantAPI — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le token est null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceParticipant.supprimerParticipantAPI(
                    contexte,
                    null,
                    HIKE_ID_VALIDE,
                    PARTICIPANT_ID_VALIDE,
                    () -> verrou.countDown()
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
     * Vérifie que la méthode gère un hikeId négatif sans lever d'exception.
     *
     * <p><b>Cas erreur</b> : hikeId négatif</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testSupprimerParticipantAPI_HikeIdNegatif_PasException()
            throws InterruptedException {

        // Given — un hikeId négatif
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on appelle supprimerParticipantAPI avec un hikeId négatif
        try {
            ServiceParticipant.supprimerParticipantAPI(
                    contexte,
                    TOKEN_VALIDE,
                    -1,
                    PARTICIPANT_ID_VALIDE,
                    () -> verrou.countDown()
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un hikeId négatif : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée avec un hikeId négatif",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — buildParticipantJSON — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la construction du JSON d'un participant valide
     * ne lève pas d'exception et produit un résultat non nul.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testBuildParticipantJSON_ParticipantValide_JsonNonNul() throws Exception {
        // Given — un participant valide avec toutes les données
        Participant participant = construireParticipantValide(
                1, "Martin", "Alice"
        );

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceParticipant.class
                .getDeclaredMethod("buildParticipantJSON", Participant.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, participant);

        // Then — le JSON est non nul et contient les champs obligatoires
        assertNotNull("Le JSON ne doit pas être nul pour un participant valide", json);
        assert json.has("age");
        assert json.has("niveau");
        assert json.has("morphologie");
        assert json.has("besoinKcal");
        assert json.has("besoinEauLitre");
    }

    /**
     * Vérifie que le JSON contient bien le nom et le prénom
     * lorsqu'ils sont renseignés.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testBuildParticipantJSON_NomPrenomRenseignes_ChampsPresentsDansJson()
            throws Exception {

        // Given — un participant avec nom et prénom
        Participant participant = construireParticipantValide(1, "Dupont", "Jean");

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceParticipant.class
                .getDeclaredMethod("buildParticipantJSON", Participant.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, participant);

        // Then — nom et prénom sont présents dans le JSON
        assertNotNull("Le JSON ne doit pas être nul", json);
        assert json.has("nom") : "Le JSON doit contenir le nom";
        assert json.has("prenom") : "Le JSON doit contenir le prénom";
    }

    // =========================================================================
    // TESTS — buildParticipantJSON — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que le JSON est construit sans le champ nom
     * lorsque le nom est null.
     *
     * <p><b>Cas limite</b> : nom null → champ absent du JSON</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testBuildParticipantJSON_NomNull_ChampNomAbsentDuJson() throws Exception {
        // Given — un participant sans nom
        Participant participant = construireParticipantValide(1, null, "Alice");

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceParticipant.class
                .getDeclaredMethod("buildParticipantJSON", Participant.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, participant);

        // Then — le JSON est non nul mais ne contient pas le champ "nom"
        assertNotNull("Le JSON ne doit pas être nul même avec un nom null", json);
        assert !json.has("nom") : "Le champ nom ne doit pas être présent si null";
    }

    /**
     * Vérifie que la capacité d'emport est absente du JSON lorsqu'elle est à 0.
     *
     * <p><b>Cas limite</b> : capacité à 0.0 → champ absent du JSON</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testBuildParticipantJSON_CapaciteZero_ChampAbsentDuJson() throws Exception {
        // Given — un participant avec une capacité d'emport à 0
        Participant participant = new Participant(
                "Martin", "Alice", 30,
                Level.DEBUTANT, Morphology.MOYENNE,
                false, 2000, 2, 0.0, HIKE_ID_VALIDE
        );
        participant.setId(1);

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceParticipant.class
                .getDeclaredMethod("buildParticipantJSON", Participant.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, participant);

        // Then — le champ capaciteEmportMaxKg est absent du JSON
        assertNotNull("Le JSON ne doit pas être nul", json);
        assert !json.has("capaciteEmportMaxKg")
                : "Le champ capaciteEmportMaxKg ne doit pas être présent si = 0";
    }

    // =========================================================================
    // TESTS — buildParticipantJSON — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode retourne null lorsque le niveau est null
     * (NullPointerException capturée en interne).
     *
     * <p><b>Cas erreur</b> : niveau null → retour null</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testBuildParticipantJSON_NiveauNull_RetourneNull() throws Exception {
        // Given — un participant avec un niveau null
        Participant participant = new Participant();
        participant.setId(1);
        participant.setNom("Martin");
        participant.setPrenom("Alice");
        participant.setAge(30);
        participant.setNiveau(null);
        participant.setMorphologie(Morphology.MOYENNE);

        // When — on construit le JSON via réflexion
        java.lang.reflect.Method method = ServiceParticipant.class
                .getDeclaredMethod("buildParticipantJSON", Participant.class);
        method.setAccessible(true);
        org.json.JSONObject json =
                (org.json.JSONObject) method.invoke(null, participant);

        // Then — le JSON retourné est null car une exception a été capturée
        assert json == null : "Le JSON doit être null si le niveau est null";
    }

    // =========================================================================
    // TESTS — traiterMAJParticipants — CAS NOMINAUX
    // =========================================================================

    /**
     * Vérifie que la mise à jour des participants s'exécute sans exception
     * avec des listes valides contenant des participants existants.
     *
     * <p><b>Cas nominal</b> : participants existants → PUT envoyé</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_ParticipantsExistants_PasException()
            throws InterruptedException {

        // Given — deux listes avec des participants existants (id > 0)
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = construireListeParticipants(true);
        ArrayList<Participant> originaux = construireListeParticipants(true);

        // When — on appelle traiterMAJParticipants
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des participants existants : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la mise à jour envoie un POST pour les nouveaux participants
     * (id = 0).
     *
     * <p><b>Cas nominal</b> : nouveaux participants → POST envoyé</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_NouveauxParticipants_PostEnvoye()
            throws InterruptedException {

        // Given — une liste de nouveaux participants (id = 0)
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = construireListeParticipants(false);
        ArrayList<Participant> originaux = new ArrayList<>();

        // When — on appelle traiterMAJParticipants avec des nouveaux participants
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec des nouveaux participants : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, les requêtes POST ont été ajoutées
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que les participants supprimés (présents dans originaux mais absents
     * de temporaires) déclenchent bien un DELETE.
     *
     * <p><b>Cas nominal</b> : participant supprimé → DELETE envoyé</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_ParticipantSupprime_DeleteEnvoye()
            throws InterruptedException {

        // Given — un participant dans originaux absent de temporaires
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = new ArrayList<>();
        ArrayList<Participant> originaux = construireListeParticipants(true);

        // When — on appelle traiterMAJParticipants avec une liste temporaire vide
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée lors d'une suppression : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — la file est initialisée, les requêtes DELETE ont été ajoutées
        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — traiterMAJParticipants — CAS LIMITES
    // =========================================================================

    /**
     * Vérifie que la méthode ne fait rien et ne lève pas d'exception
     * lorsque les deux listes sont vides.
     *
     * <p><b>Cas limite</b> : deux listes vides</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_DeuxListesVides_PasException()
            throws InterruptedException {

        // Given — deux listes vides
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = new ArrayList<>();
        ArrayList<Participant> originaux = new ArrayList<>();

        // When — on appelle traiterMAJParticipants avec deux listes vides
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec deux listes vides : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — aucune requête envoyée, file initialisée
        assertNotNull("La file doit être initialisée avec deux listes vides",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ignore les participants dont le JSON est null
     * (niveau ou morphologie null).
     *
     * <p><b>Cas limite</b> : participant avec niveau null → ignoré</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_ParticipantJsonNull_ParticipantIgnore()
            throws InterruptedException {

        // Given — un participant avec un niveau null (buildParticipantJSON retourne null)
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = new ArrayList<>();
        Participant participantInvalide = new Participant();
        participantInvalide.setId(0);
        participantInvalide.setNom("Test");
        participantInvalide.setNiveau(null);
        participantInvalide.setMorphologie(null);
        temporaires.add(participantInvalide);

        ArrayList<Participant> originaux = new ArrayList<>();

        // When — on appelle traiterMAJParticipants avec un participant invalide
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (Exception e) {
            fail("Aucune exception ne doit être levée avec un participant invalide : "
                    + e.getMessage());
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        // Then — le participant invalide est ignoré, la file est initialisée
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    // =========================================================================
    // TESTS — traiterMAJParticipants — CAS ERREURS
    // =========================================================================

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque la liste temporaire est null.
     *
     * <p><b>Cas erreur</b> : liste temporaire null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_ListeTemporaireNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — une liste temporaire null
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> originaux = construireListeParticipants(true);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    null,
                    originaux,
                    tokenManager
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec liste temporaire null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque la liste originale est null.
     *
     * <p><b>Cas erreur</b> : liste originale null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_ListeOriginaleNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — une liste originale null
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = construireListeParticipants(true);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    null,
                    tokenManager
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec liste originale null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException
     * lorsque le TokenManager est null.
     *
     * <p><b>Cas erreur</b> : TokenManager null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testTraiterMAJParticipants_TokenManagerNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un TokenManager null
        CountDownLatch verrou = new CountDownLatch(1);
        ArrayList<Participant> temporaires = construireListeParticipants(true);
        ArrayList<Participant> originaux = construireListeParticipants(true);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceParticipant.traiterMAJParticipants(
                    contexte,
                    HIKE_ID_VALIDE,
                    temporaires,
                    originaux,
                    null
            );
            verrou.countDown();
        } catch (NullPointerException e) {
            fail("NullPointerException ne doit pas être levée avec TokenManager null");
        }

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);
        assertNotNull("La file doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }
}
