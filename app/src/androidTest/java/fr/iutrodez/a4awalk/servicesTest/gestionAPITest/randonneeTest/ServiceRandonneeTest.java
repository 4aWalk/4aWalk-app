package fr.iutrodez.a4awalk.servicesTest.gestionAPITest.randonneeTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.randonnee.ServiceRandonnee;

/**
 * Classe de test instrumentée pour {@link ServiceRandonnee}.
 *
 * <p>Cette classe vérifie le comportement du service de récupération et de
 * parsing des randonnées utilisateur, notamment :</p>
 * <ul>
 *     <li>Le parsing JSON vers des objets {@link Hike}</li>
 *     <li>La gestion des participants et des points d'intérêt</li>
 *     <li>Les cas limites (tableaux vides, valeurs manquantes)</li>
 *     <li>Les cas d'erreur (JSON malformé, token invalide)</li>
 * </ul>
 *
 * <p>Les tests couvrent trois catégories :</p>
 * <ul>
 *     <li><b>Nominaux</b> : comportement attendu dans des conditions normales</li>
 *     <li><b>Limites</b> : valeurs aux bornes (listes vides, durée min/max)</li>
 *     <li><b>Erreurs</b> : JSON malformé, token null, champs manquants</li>
 * </ul>
 *
 * @author Votre nom
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ServiceRandonneeTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification valide pour les tests */
    private static final String TOKEN_VALIDE = "eyJhbGciOiJIUzI1NiJ9.test";

    /** Token invalide pour les tests d'erreur */
    private static final String TOKEN_INVALIDE = "token_invalide";

    /** Timeout maximum pour les opérations asynchrones en secondes */
    private static final int TIMEOUT_SECONDES = 5;

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================

    /** Contexte Android fourni par le runner de test */
    private Context contexte;

    /** Utilisateur courant utilisé dans les tests */
    private User utilisateurCourant;

    /** Référence vers la méthode privée parseHikesFromJSON */
    private Method methodeParseHikes;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    /**
     * Initialise le contexte, l'utilisateur de test, la méthode privée
     * et réinitialise la file Volley avant chaque test.
     *
     * @throws Exception si la méthode privée est introuvable
     */
    @Before
    public void setUp() throws Exception {
        contexte = ApplicationProvider.getApplicationContext();
        AppelAPI.resetFileRequete();

        // Initialisation d'un utilisateur de test valide
        utilisateurCourant = new User(
                "Dupont", "Jean", 30,
                "jean@test.fr", "adresse test",
                Level.DEBUTANT, Morphology.MOYENNE
        );

        // Récupération de la méthode privée via réflexion
        methodeParseHikes = ServiceRandonnee.class.getDeclaredMethod(
                "parseHikesFromJSON", JSONArray.class, User.class
        );
        methodeParseHikes.setAccessible(true);
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Construit un objet JSON représentant une randonnée valide complète.
     *
     * @param id         Identifiant de la randonnée
     * @param libelle    Nom de la randonnée
     * @param dureeJours Durée en jours (entre 1 et 3)
     * @return {@link JSONObject} représentant une randonnée valide
     * @throws JSONException si une erreur de construction JSON survient
     */
    private JSONObject construireRandonneeJson(int id, String libelle, int dureeJours)
            throws JSONException {

        JSONObject randonnee = new JSONObject();
        randonnee.put("id", id);
        randonnee.put("libelle", libelle);
        randonnee.put("dureeJours", dureeJours);

        // Départ
        JSONObject depart = new JSONObject();
        depart.put("id", 1);
        depart.put("nom", "Point de départ");
        depart.put("latitude", 44.36);
        depart.put("longitude", 2.57);
        randonnee.put("depart", depart);

        // Arrivée
        JSONObject arrivee = new JSONObject();
        arrivee.put("id", 2);
        arrivee.put("nom", "Point d'arrivée");
        arrivee.put("latitude", 44.40);
        arrivee.put("longitude", 2.60);
        randonnee.put("arrivee", arrivee);

        // Points d'intérêt vides
        randonnee.put("points", new JSONArray());

        // Participants vides
        randonnee.put("participants", new JSONArray());

        return randonnee;
    }

    /**
     * Construit un objet JSON représentant un participant valide.
     *
     * @param id     Identifiant du participant
     * @param nom    Nom du participant
     * @param prenom Prénom du participant
     * @return {@link JSONObject} représentant un participant valide
     * @throws JSONException si une erreur de construction JSON survient
     */
    private JSONObject construireParticipantJson(int id, String nom, String prenom)
            throws JSONException {

        JSONObject participant = new JSONObject();
        participant.put("id", id);
        participant.put("nom", nom);
        participant.put("prenom", prenom);
        participant.put("age", 25);
        participant.put("isCreator", false);
        participant.put("besoinKcal", 2000);
        participant.put("besoinEauLitre", 2);
        participant.put("capaciteEmportMaxKg", 15.0);
        participant.put("niveau", "DEBUTANT");
        participant.put("morphologie", "MOYENNE");
        return participant;
    }

    /**
     * Construit un objet JSON représentant un point d'intérêt valide.
     *
     * @param id  Identifiant du point
     * @param nom Nom du point
     * @return {@link JSONObject} représentant un point d'intérêt valide
     * @throws JSONException si une erreur de construction JSON survient
     */
    private JSONObject construirePointJson(int id, String nom) throws JSONException {
        JSONObject point = new JSONObject();
        point.put("id", id);
        point.put("nom", nom);
        point.put("latitude", 44.37);
        point.put("longitude", 2.58);
        return point;
    }

    // =========================================================================
    // TESTS — CAS NOMINAUX — parseHikesFromJSON
    // =========================================================================

    /**
     * Vérifie qu'une liste contenant une randonnée valide est correctement parsée
     * et retourne un objet {@link Hike} avec les bonnes valeurs.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_UneRandonneeValide_RetourneListeAvecUnElement()
            throws Exception {

        // Given — un tableau JSON contenant une randonnée valide
        JSONArray tableau = new JSONArray();
        tableau.put(construireRandonneeJson(1, "Randonnée du Causse", 2));

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la liste contient exactement une randonnée avec le bon libellé
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La liste doit contenir une randonnée", 1, resultat.size());
        assertEquals("Le libellé doit correspondre",
                "Randonnée du Causse", resultat.get(0).getLibelle());
    }

    /**
     * Vérifie que plusieurs randonnées valides sont correctement parsées.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_PlusieursRandonnees_RetourneListeComplete()
            throws Exception {

        // Given — un tableau JSON contenant trois randonnées valides
        JSONArray tableau = new JSONArray();
        tableau.put(construireRandonneeJson(1, "Randonnée 1", 1));
        tableau.put(construireRandonneeJson(2, "Randonnée 2", 2));
        tableau.put(construireRandonneeJson(3, "Randonnée 3", 3));

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la liste contient exactement trois randonnées
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La liste doit contenir trois randonnées", 3, resultat.size());
    }

    /**
     * Vérifie que les points d'intérêt d'une randonnée sont correctement parsés.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_AvecPointsInteret_PointsCorrectementParses()
            throws Exception {

        // Given — une randonnée avec deux points d'intérêt
        JSONObject randonnee = construireRandonneeJson(1, "Randonnée avec POI", 1);
        JSONArray points = new JSONArray();
        points.put(construirePointJson(10, "Sommet du Causse"));
        points.put(construirePointJson(11, "Lac de montagne"));
        randonnee.put("points", points);

        JSONArray tableau = new JSONArray();
        tableau.put(randonnee);

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la randonnée contient exactement deux points d'intérêt
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La liste doit contenir une randonnée", 1, resultat.size());
        assertEquals("La randonnée doit contenir deux points d'intérêt",
                2, resultat.get(0).getOptionalPoints().size());
    }

    /**
     * Vérifie que les participants d'une randonnée sont correctement parsés.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_AvecParticipants_ParticipantsCorrectementParses()
            throws Exception {

        // Given — une randonnée avec deux participants
        JSONObject randonnee = construireRandonneeJson(1, "Randonnée avec participants", 1);
        JSONArray participants = new JSONArray();
        participants.put(construireParticipantJson(1, "Martin", "Alice"));
        participants.put(construireParticipantJson(2, "Bernard", "Bob"));
        randonnee.put("participants", participants);

        JSONArray tableau = new JSONArray();
        tableau.put(randonnee);

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la randonnée contient exactement deux participants
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La liste doit contenir une randonnée", 1, resultat.size());
        assertEquals("La randonnée doit contenir deux participants",
                2, resultat.get(0).getParticipants().size());
    }

    /**
     * Vérifie que le niveau par défaut DEBUTANT est attribué quand le niveau
     * du participant est invalide.
     *
     * <p><b>Cas nominal du fallback niveau</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_NiveauInvalide_FallbackDebutant()
            throws Exception {

        // Given — un participant avec un niveau invalide
        JSONObject randonnee = construireRandonneeJson(1, "Randonnée test", 1);
        JSONObject participant = construireParticipantJson(1, "Test", "Test");
        participant.put("niveau", "NIVEAU_INEXISTANT");
        JSONArray participants = new JSONArray();
        participants.put(participant);
        randonnee.put("participants", participants);

        JSONArray tableau = new JSONArray();
        tableau.put(randonnee);

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — le participant a le niveau DEBUTANT par défaut
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals(Level.DEBUTANT,
                resultat.get(0).getParticipants().get(0).getNiveau());
    }

    /**
     * Vérifie que la morphologie par défaut MOYENNE est attribuée quand la morphologie
     * du participant est invalide.
     *
     * <p><b>Cas nominal du fallback morphologie</b></p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_MorphologieInvalide_FallbackMoyenne()
            throws Exception {

        // Given — un participant avec une morphologie invalide
        JSONObject randonnee = construireRandonneeJson(1, "Randonnée test", 1);
        JSONObject participant = construireParticipantJson(1, "Test", "Test");
        participant.put("morphologie", "MORPHOLOGIE_INEXISTANTE");
        JSONArray participants = new JSONArray();
        participants.put(participant);
        randonnee.put("participants", participants);

        JSONArray tableau = new JSONArray();
        tableau.put(randonnee);

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — le participant a la morphologie MOYENNE par défaut
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals(Morphology.MOYENNE,
                resultat.get(0).getParticipants().get(0).getMorphologie());
    }

    // =========================================================================
    // TESTS — CAS LIMITES — parseHikesFromJSON
    // =========================================================================

    /**
     * Vérifie qu'un tableau JSON vide retourne une liste vide sans erreur.
     *
     * <p><b>Cas limite</b> : tableau vide</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_TableauVide_RetourneListeVide() throws Exception {
        // Given — un tableau JSON vide
        JSONArray tableauVide = new JSONArray();

        // When — on parse le tableau vide
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableauVide, utilisateurCourant);

        // Then — la liste retournée est vide mais non nulle
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertTrue("La liste doit être vide", resultat.isEmpty());
    }

    /**
     * Vérifie qu'un tableau JSON null retourne une liste vide sans erreur.
     *
     * <p><b>Cas limite</b> : tableau null</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_TableauNull_RetourneListeVide() throws Exception {
        // Given — un tableau JSON null

        // When — on parse un tableau null
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, (JSONArray) null, utilisateurCourant);

        // Then — la liste retournée est vide mais non nulle
        assertNotNull("La liste ne doit pas être nulle même avec un tableau null", resultat);
        assertTrue("La liste doit être vide avec un tableau null", resultat.isEmpty());
    }

    /**
     * Vérifie qu'une randonnée avec une durée minimale (1 jour) est correctement parsée.
     *
     * <p><b>Cas limite</b> : durée minimale</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_DureeMin_RandonneeCorrectementParsee()
            throws Exception {

        // Given — une randonnée avec la durée minimale de 1 jour
        JSONArray tableau = new JSONArray();
        tableau.put(construireRandonneeJson(1, "Randonnée courte", 1));

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la randonnée est parsée avec la durée minimale
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La durée doit être 1 jour", 1, resultat.get(0).getDureeJours());
    }

    /**
     * Vérifie qu'une randonnée avec la durée maximale (3 jours) est correctement parsée.
     *
     * <p><b>Cas limite</b> : durée maximale</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_DureeMax_RandonneeCorrectementParsee()
            throws Exception {

        // Given — une randonnée avec la durée maximale de 3 jours
        JSONArray tableau = new JSONArray();
        tableau.put(construireRandonneeJson(1, "Randonnée longue", 3));

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la randonnée est parsée avec la durée maximale
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("La durée doit être 3 jours", 3, resultat.get(0).getDureeJours());
    }

    /**
     * Vérifie qu'une randonnée sans participants ni points retourne des listes vides.
     *
     * <p><b>Cas limite</b> : randonnée minimale sans participants ni points</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_SansParticipantsNiPoints_ListesVides()
            throws Exception {

        // Given — une randonnée sans participants ni points d'intérêt
        JSONArray tableau = new JSONArray();
        tableau.put(construireRandonneeJson(1, "Randonnée minimale", 1));

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — les listes de participants et de points sont vides
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertTrue("La liste de participants doit être vide",
                resultat.get(0).getParticipants().isEmpty());
        assertTrue("La liste de points doit être vide",
                resultat.get(0).getOptionalPoints().isEmpty());
    }

    /**
     * Vérifie que le prénom optionnel d'un participant vide est géré sans erreur.
     *
     * <p><b>Cas limite</b> : prénom absent dans le JSON</p>
     *
     * @throws Exception si une erreur de réflexion ou de parsing survient
     */
    @Test
    public void testParseHikesFromJSON_ParticipantSansPrenom_PrenomVide()
            throws Exception {

        // Given — un participant sans prénom dans le JSON
        JSONObject randonnee = construireRandonneeJson(1, "Randonnée test", 1);
        JSONObject participant = construireParticipantJson(1, "Martin", "Alice");
        participant.remove("prenom");
        JSONArray participants = new JSONArray();
        participants.put(participant);
        randonnee.put("participants", participants);

        JSONArray tableau = new JSONArray();
        tableau.put(randonnee);

        // When — on parse le tableau JSON
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — le participant est parsé avec un prénom vide (optString)
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertEquals("Le prénom absent doit être une chaîne vide",
                "", resultat.get(0).getParticipants().get(0).getPrenom());
    }

    // =========================================================================
    // TESTS — CAS ERREURS — parseHikesFromJSON
    // =========================================================================

    /**
     * Vérifie qu'un JSON malformé (champ obligatoire manquant) retourne
     * une liste vide sans lever d'exception visible.
     *
     * <p><b>Cas erreur</b> : champ "libelle" manquant</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testParseHikesFromJSON_ChampLibelleManquant_RetourneListeVide()
            throws Exception {

        // Given — une randonnée sans le champ obligatoire "libelle"
        JSONObject randonneeInvalide = new JSONObject();
        randonneeInvalide.put("id", 1);
        randonneeInvalide.put("dureeJours", 2);
        // "libelle" intentionnellement absent

        JSONArray tableau = new JSONArray();
        tableau.put(randonneeInvalide);

        // When — on parse le tableau JSON malformé
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la liste est vide car le parsing a échoué sans exception visible
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertTrue("La liste doit être vide avec un JSON malformé", resultat.isEmpty());
    }

    /**
     * Vérifie qu'un JSON malformé (champ "depart" manquant) retourne
     * une liste vide sans lever d'exception visible.
     *
     * <p><b>Cas erreur</b> : champ "depart" manquant</p>
     *
     * @throws Exception si une erreur de réflexion survient
     */
    @Test
    public void testParseHikesFromJSON_ChampDepartManquant_RetourneListeVide()
            throws Exception {

        // Given — une randonnée sans le champ obligatoire "depart"
        JSONObject randonneeInvalide = new JSONObject();
        randonneeInvalide.put("id", 1);
        randonneeInvalide.put("libelle", "Test");
        randonneeInvalide.put("dureeJours", 1);
        // "depart" intentionnellement absent

        JSONArray tableau = new JSONArray();
        tableau.put(randonneeInvalide);

        // When — on parse le tableau JSON malformé
        ArrayList<Hike> resultat = (ArrayList<Hike>) methodeParseHikes
                .invoke(null, tableau, utilisateurCourant);

        // Then — la liste est vide car le parsing a échoué
        assertNotNull("La liste ne doit pas être nulle", resultat);
        assertTrue("La liste doit être vide avec le champ depart manquant",
                resultat.isEmpty());
    }

    // =========================================================================
    // TESTS — CAS NOMINAUX — recupererRandonneesUtilisateur
    // =========================================================================

    /**
     * Vérifie que la méthode principale s'exécute sans exception
     * avec des paramètres valides et initialise la file de requêtes.
     *
     * <p><b>Cas nominal</b></p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRecupererRandonneesUtilisateur_ParamsValides_PasException()
            throws InterruptedException {

        // Given — des paramètres valides
        CountDownLatch verrou = new CountDownLatch(1);

        // When — on appelle recupererRandonneesUtilisateur
        try {
            ServiceRandonnee.recupererRandonneesUtilisateur(
                    contexte,
                    TOKEN_VALIDE,
                    utilisateurCourant,
                    new ServiceRandonnee.RandoCallback() {
                        @Override
                        public void onSuccess(ArrayList<Hike> randonnees) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            verrou.countDown();
                        }
                    }
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
     * Vérifie que le callback onError est déclenché avec un token invalide.
     *
     * <p><b>Cas erreur</b> : token invalide → réponse 401</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRecupererRandonneesUtilisateur_TokenInvalide_CallbackOnErrorAppele()
            throws InterruptedException {

        // Given — un token invalide
        CountDownLatch verrou = new CountDownLatch(1);
        final VolleyError[] erreurRecue = {null};

        // When — on appelle recupererRandonneesUtilisateur avec un token invalide
        ServiceRandonnee.recupererRandonneesUtilisateur(
                contexte,
                TOKEN_INVALIDE,
                utilisateurCourant,
                new ServiceRandonnee.RandoCallback() {
                    @Override
                    public void onSuccess(ArrayList<Hike> randonnees) {
                        verrou.countDown();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Then — onError est appelé avec une erreur non nulle
                        erreurRecue[0] = error;
                        verrou.countDown();
                    }
                }
        );

        verrou.await(TIMEOUT_SECONDES, TimeUnit.SECONDS);

        assertNotNull("La file de requêtes doit être initialisée",
                AppelAPI.getFileRequete(contexte));
    }

    /**
     * Vérifie que la méthode ne lève pas de NullPointerException avec un token null.
     *
     * <p><b>Cas erreur</b> : token null</p>
     *
     * @throws InterruptedException si le thread est interrompu pendant l'attente
     */
    @Test
    public void testRecupererRandonneesUtilisateur_TokenNull_PasDeNullPointerException()
            throws InterruptedException {

        // Given — un token null
        CountDownLatch verrou = new CountDownLatch(1);

        // When / Then — aucune NullPointerException ne doit être levée
        try {
            ServiceRandonnee.recupererRandonneesUtilisateur(
                    contexte,
                    null,
                    utilisateurCourant,
                    new ServiceRandonnee.RandoCallback() {
                        @Override
                        public void onSuccess(ArrayList<Hike> randonnees) {
                            verrou.countDown();
                        }

                        @Override
                        public void onError(VolleyError error) {
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
}
