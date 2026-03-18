package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;

/**
 * Classe de tests unitaires pour {@link ServiceParticipant}.
 *
 * <p>Cette classe couvre les scénarios suivants :</p>
 * <ul>
 *   <li>Construction du JSON d'un participant — {@code buildParticipantJSON} —
 *       cas nominaux, limites et erreurs</li>
 *   <li>Parsing d'un objet JSON en {@link Participant} — {@code parseParticipant} —
 *       cas nominaux, champs optionnels avec valeurs par défaut, enums inconnus</li>
 *   <li>Extraction d'une liste — {@code extractParticipants} — tableau valide,
 *       tableau vide, fallback {@code nbParticipants}, fallback entier</li>
 * </ul>
 *
 * <p>La convention <b>Given / When / Then</b> est appliquée sur chaque test.</p>
 *
 * <p>Dépendances requises dans {@code build.gradle (app)} :</p>
 * <pre>
 * testImplementation 'junit:junit:4.13.2'
 * testImplementation 'org.mockito:mockito-core:5.x.x'
 * </pre>
 *
 * @author Équipe A4AWalk
 * @version 1.0
 * @see ServiceParticipant
 * @see Participant
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceParticipantTest {

    // =========================================================================
    // Constantes de test
    // =========================================================================

    /** Identifiant d'un participant existant. */
    private static final int        ID_PARTICIPANT  = 7;

    /** Prénom standard. */
    private static final String     PRENOM          = "Alice";

    /** Nom standard. */
    private static final String     NOM             = "Dupont";

    /** Âge standard. */
    private static final int        AGE             = 30;

    /** Niveau standard. */
    private static final Level      NIVEAU          = Level.ENTRAINE;

    /** Morphologie standard. */
    private static final Morphology MORPHOLOGIE     = Morphology.MOYENNE;

    /** Besoin calorique journalier standard. */
    private static final int        BESOIN_KCAL     = 2500;

    /** Besoin en eau journalier (litres). */
    private static final double     BESOIN_EAU      = 2.5;

    /** Capacité de portage maximale (kg). */
    private static final double     CAPACITE_EMPORT = 12.5;

    // =========================================================================
    // Initialisation
    // =========================================================================

    /** Initialise les mocks Mockito avant chaque test. */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================

    /**
     * Crée un {@link Participant} valide avec toutes les propriétés renseignées.
     *
     * @return participant de test.
     */
    private Participant creerParticipantValide() {
        Participant p = new Participant();
        p.setId(ID_PARTICIPANT);
        p.setPrenom(PRENOM);
        p.setNom(NOM);
        p.setAge(AGE);
        p.setNiveau(NIVEAU);
        p.setMorphologie(MORPHOLOGIE);
        p.setBesoinKcal(BESOIN_KCAL);
        p.setBesoinEauLitre(BESOIN_EAU);
        p.setCapaciteEmportMaxKg(CAPACITE_EMPORT);
        return p;
    }

    /**
     * Construit un {@link JSONObject} représentant un participant complet valide
     * destiné à {@code parseParticipant}.
     *
     * @return JSONObject de participant valide.
     * @throws JSONException si la construction échoue.
     */
    private JSONObject creerJsonParticipantValide() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id",                  ID_PARTICIPANT);
        json.put("prenom",              PRENOM);
        json.put("nom",                 NOM);
        json.put("age",                 AGE);
        json.put("niveau",              NIVEAU.toString());
        json.put("morphologie",         MORPHOLOGIE.toString());
        json.put("besoinKcal",          BESOIN_KCAL);
        json.put("besoinEauLitre",      BESOIN_EAU);
        json.put("capaciteEmportMaxKg", CAPACITE_EMPORT);
        return json;
    }

    // =========================================================================
    // Tests — buildParticipantJSON — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que {@code buildParticipantJSON} retourne un JSON non nul pour un
     * participant valide.
     *
     * <p><b>Given</b> : un participant avec toutes les propriétés renseignées.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON(participant)}.</p>
     * <p><b>Then</b> : le JSON retourné est non nul.</p>
     */
    @Test
    public void buildParticipantJSON_participantValide_retourneJsonNonNull() {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull("Le JSON ne doit pas être nul pour un participant valide", result);
    }

    /**
     * Vérifie que le champ {@code "age"} est correctement renseigné.
     *
     * <p><b>Given</b> : un participant avec {@code age = 30}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getInt("age")} vaut 30.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_age_estCorrectementRenseigne() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("L'âge doit correspondre à la valeur du participant", AGE, result.getInt("age"));
    }

    /**
     * Vérifie que le champ {@code "niveau"} est sérialisé en chaîne via {@code toString()}.
     *
     * <p><b>Given</b> : un participant avec {@code niveau = INTERMEDIAIRE}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getString("niveau")} vaut {@code "INTERMEDIAIRE"}.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_niveau_estSerialiseEnChaine() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Le niveau doit être sérialisé via toString()", NIVEAU.toString(), result.getString("niveau"));
    }

    /**
     * Vérifie que le champ {@code "morphologie"} est sérialisé en chaîne.
     *
     * <p><b>Given</b> : un participant avec {@code morphologie = MOYENNE}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getString("morphologie")} vaut {@code "MOYENNE"}.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_morphologie_estSerialiseEnChaine() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("La morphologie doit être sérialisée via toString()", MORPHOLOGIE.toString(), result.getString("morphologie"));
    }

    /**
     * Vérifie que le champ {@code "besoinKcal"} est correctement renseigné.
     *
     * <p><b>Given</b> : un participant avec {@code besoinKcal = 2500}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getInt("besoinKcal")} vaut 2500.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_besoinKcal_estCorrectementRenseigne() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("besoinKcal doit correspondre à la valeur du participant", BESOIN_KCAL, result.getInt("besoinKcal"));
    }

    /**
     * Vérifie que le champ {@code "besoinEauLitre"} est correctement renseigné.
     *
     * <p><b>Given</b> : un participant avec {@code besoinEauLitre = 2.5}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getDouble("besoinEauLitre")} est proche de 2.5.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_besoinEauLitre_estCorrectementRenseigne() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("besoinEauLitre doit correspondre à la valeur du participant", BESOIN_EAU, result.getDouble("besoinEauLitre"), 0.001);
    }

    /**
     * Vérifie que {@code "capaciteEmportMaxKg"} contient la valeur réelle quand > 0.
     *
     * <p><b>Given</b> : un participant avec {@code capaciteEmportMaxKg = 12.5}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getDouble("capaciteEmportMaxKg")} est proche de 12.5.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_capaciteEmportNonNulle_estRenseignee() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("capaciteEmportMaxKg doit correspondre à la valeur du participant", CAPACITE_EMPORT, result.getDouble("capaciteEmportMaxKg"), 0.001);
    }

    /**
     * Vérifie que {@code "nom"} est présent dans le JSON quand le nom n'est pas null.
     *
     * <p><b>Given</b> : un participant avec un nom non null.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getString("nom")} correspond au nom fourni.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_nomNonNull_estPresent() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Le nom doit être présent dans le JSON", NOM, result.getString("nom"));
    }

    /**
     * Vérifie que {@code "prenom"} est présent dans le JSON quand le prénom n'est pas null.
     *
     * <p><b>Given</b> : un participant avec un prénom non null.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getString("prenom")} correspond au prénom fourni.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_prenomNonNull_estPresent() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Le prénom doit être présent dans le JSON", PRENOM, result.getString("prenom"));
    }

    // =========================================================================
    // Tests — buildParticipantJSON — Cas limites et erreurs
    // =========================================================================

    /**
     * Vérifie que le champ {@code "nom"} est absent du JSON quand le nom est null
     * (le service ne l'insère pas si null).
     *
     * <p><b>Given</b> : un participant avec {@code nom = null}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : le JSON ne contient pas la clé {@code "nom"}.</p>
     */
    @Test
    public void buildParticipantJSON_nomNull_champNomAbsent() {
        // Given
        Participant p = creerParticipantValide();
        p.setNom(null);
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Le champ 'nom' doit être absent si le nom est null", false, result.has("nom"));
    }

    /**
     * Vérifie que le champ {@code "prenom"} est absent du JSON quand le prénom est null.
     *
     * <p><b>Given</b> : un participant avec {@code prenom = null}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : le JSON ne contient pas la clé {@code "prenom"}.</p>
     */
    @Test
    public void buildParticipantJSON_prenomNull_champPrenomAbsent() {
        // Given
        Participant p = creerParticipantValide();
        p.setPrenom(null);
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Le champ 'prenom' doit être absent si le prénom est null", false, result.has("prenom"));
    }

    /**
     * Vérifie que {@code "capaciteEmportMaxKg"} est stocké comme entier 0 quand la
     * capacité est 0.0 (guard {@code != 0.0 ? valeur : 0}).
     *
     * <p><b>Given</b> : un participant avec {@code capaciteEmportMaxKg = 0.0}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : {@code json.getInt("capaciteEmportMaxKg")} vaut 0.</p>
     *
     * @throws JSONException si la lecture du JSON échoue.
     */
    @Test
    public void buildParticipantJSON_capaciteEmportZero_stockeZeroEntier() throws JSONException {
        // Given
        Participant p = creerParticipantValide();
        p.setCapaciteEmportMaxKg(0.0);
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNotNull(result);
        assertEquals("Une capacité de 0.0 doit être stockée comme entier 0", 0, result.getInt("capaciteEmportMaxKg"));
    }

    /**
     * Vérifie que {@code buildParticipantJSON} retourne {@code null} pour un participant null
     * (exception capturée par le bloc {@code catch}).
     *
     * <p><b>Given</b> : un participant {@code null}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON(null)}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     */
    @Test
    public void buildParticipantJSON_participantNull_retourneNull() {
        // Given / When
        JSONObject result = ServiceParticipant.buildParticipantJSON(null);
        // Then
        assertNull("Un participant null doit produire un JSON null", result);
    }

    /**
     * Vérifie que {@code buildParticipantJSON} retourne {@code null} quand le niveau est
     * {@code null} (NullPointerException sur {@code toString()} capturée par le catch).
     *
     * <p><b>Given</b> : un participant avec {@code niveau = null}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     */
    @Test
    public void buildParticipantJSON_niveauNull_retourneNull() {
        // Given
        Participant p = creerParticipantValide();
        p.setNiveau(null);
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNull("Un niveau null doit produire un JSON null (NullPointerException capturée)", result);
    }

    /**
     * Vérifie que {@code buildParticipantJSON} retourne {@code null} quand la morphologie
     * est {@code null}.
     *
     * <p><b>Given</b> : un participant avec {@code morphologie = null}.</p>
     * <p><b>When</b> : on appelle {@code buildParticipantJSON}.</p>
     * <p><b>Then</b> : le résultat est {@code null}.</p>
     */
    @Test
    public void buildParticipantJSON_morphologieNull_retourneNull() {
        // Given
        Participant p = creerParticipantValide();
        p.setMorphologie(null);
        // When
        JSONObject result = ServiceParticipant.buildParticipantJSON(p);
        // Then
        assertNull("Une morphologie null doit produire un JSON null", result);
    }

    // =========================================================================
    // Tests — parseParticipant — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que {@code parseParticipant} retourne un participant non nul pour un JSON valide.
     *
     * <p><b>Given</b> : un JSON de participant complet et valide.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : le participant retourné est non nul.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_jsonValide_retourneParticipantNonNull() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull("Le Participant parsé ne doit pas être nul", result);
    }

    /**
     * Vérifie que l'identifiant est correctement extrait via {@code getInt} (strict).
     *
     * <p><b>Given</b> : un JSON avec {@code id = 7}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getId()} vaut 7.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_id_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("L'id doit correspondre au champ 'id' du JSON", ID_PARTICIPANT, result.getId());
    }

    /**
     * Vérifie que le prénom est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code prenom = "Alice"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getPrenom()} vaut {@code "Alice"}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_prenom_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Le prénom doit correspondre au champ 'prenom' du JSON", PRENOM, result.getPrenom());
    }

    /**
     * Vérifie que le nom est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code nom = "Dupont"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getNom()} vaut {@code "Dupont"}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_nom_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Le nom doit correspondre au champ 'nom' du JSON", NOM, result.getNom());
    }

    /**
     * Vérifie que l'âge est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code age = 30}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getAge()} vaut 30.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_age_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("L'âge doit correspondre au champ 'age' du JSON", AGE, result.getAge());
    }

    /**
     * Vérifie que le niveau est correctement parsé depuis sa représentation en chaîne.
     *
     * <p><b>Given</b> : un JSON avec {@code niveau = "INTERMEDIAIRE"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getNiveau()} vaut {@link Level#ENTRAINE}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_niveau_estCorrectementParse() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Le niveau doit être parsé via Level.valueOf()", NIVEAU, result.getNiveau());
    }

    /**
     * Vérifie que la morphologie est correctement parsée.
     *
     * <p><b>Given</b> : un JSON avec {@code morphologie = "MOYENNE"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getMorphologie()} vaut {@link Morphology#MOYENNE}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_morphologie_estCorrectementParsee() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("La morphologie doit être parsée via Morphology.valueOf()", MORPHOLOGIE, result.getMorphologie());
    }

    /**
     * Vérifie que le besoin calorique est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code besoinKcal = 2500}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getBesoinKcal()} vaut 2500.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_besoinKcal_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("besoinKcal doit correspondre au champ du JSON", BESOIN_KCAL, result.getBesoinKcal());
    }

    /**
     * Vérifie que le besoin en eau est correctement extrait.
     *
     * <p><b>Given</b> : un JSON avec {@code besoinEauLitre = 2.5}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getBesoinEauLitre()} est proche de 2.5.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_besoinEauLitre_estCorrectementExtrait() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("besoinEauLitre doit correspondre au champ du JSON", BESOIN_EAU, result.getBesoinEauLitre(), 0.001);
    }

    /**
     * Vérifie que la capacité de portage est correctement extraite.
     *
     * <p><b>Given</b> : un JSON avec {@code capaciteEmportMaxKg = 12.5}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getCapaciteEmportMaxKg()} est proche de 12.5.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_capaciteEmportMaxKg_estCorrectementExtraite() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("capaciteEmportMaxKg doit correspondre au champ du JSON", CAPACITE_EMPORT, result.getCapaciteEmportMaxKg(), 0.001);
    }

    // =========================================================================
    // Tests — parseParticipant — Champs optionnels et valeurs par défaut
    // =========================================================================

    /**
     * Vérifie que le prénom prend la valeur {@code ""} quand le champ est absent
     * (comportement de {@code optString}).
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "prenom"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getPrenom()} vaut {@code ""}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_prenomAbsent_retourneValeurParDefaut() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.remove("prenom");
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Le prénom absent doit retourner la chaîne vide", "", result.getPrenom());
    }

    /**
     * Vérifie que le nom prend la valeur {@code ""} quand le champ est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "nom"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getNom()} vaut {@code ""}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_nomAbsent_retourneValeurParDefaut() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.remove("nom");
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Le nom absent doit retourner la chaîne vide", "", result.getNom());
    }

    /**
     * Vérifie que l'âge prend la valeur 0 quand le champ est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "age"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getAge()} vaut 0.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_ageAbsent_retourneZeroParDefaut() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.remove("age");
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("L'âge absent doit retourner 0 par défaut", 0, result.getAge());
    }

    /**
     * Vérifie que le niveau prend la valeur {@link Level#DEBUTANT} quand le champ
     * contient une valeur inconnue de l'enum.
     *
     * <p><b>Given</b> : un JSON avec {@code niveau = "INCONNU"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getNiveau()} vaut {@link Level#DEBUTANT}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_niveauInconnu_retourneDebutantParDefaut() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.put("niveau", "INCONNU");
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Un niveau inconnu doit tomber sur DEBUTANT par défaut", Level.DEBUTANT, result.getNiveau());
    }

    /**
     * Vérifie que la morphologie prend {@link Morphology#MOYENNE} pour une valeur inconnue.
     *
     * <p><b>Given</b> : un JSON avec {@code morphologie = "INEXISTANTE"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getMorphologie()} vaut {@link Morphology#MOYENNE}.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_morphologieInconnue_retourneMoyenneParDefaut() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.put("morphologie", "INEXISTANTE");
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertEquals("Une morphologie inconnue doit tomber sur MOYENNE par défaut", Morphology.MOYENNE, result.getMorphologie());
    }

    /**
     * Vérifie que le backpack est nul quand le champ {@code "backpack"} est absent.
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "backpack"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : {@code participant.getBackpack()} est nul.</p>
     *
     * @throws JSONException si le parsing échoue.
     */
    @Test
    public void parseParticipant_backpackAbsent_backpackEstNull() throws JSONException {
        // Given — creerJsonParticipantValide() ne contient pas de champ backpack
        JSONObject json = creerJsonParticipantValide();
        // When
        Participant result = ServiceParticipant.parseParticipant(json);
        // Then
        assertNotNull(result);
        assertNull("Le backpack doit être null si absent du JSON", result.getBackpack());
    }

    /**
     * Vérifie que {@code parseParticipant} lève une {@link JSONException} quand le champ
     * obligatoire {@code "id"} est absent (utilise {@code getInt} strict).
     *
     * <p><b>Given</b> : un JSON sans le champ {@code "id"}.</p>
     * <p><b>When</b> : on appelle {@code parseParticipant(json)}.</p>
     * <p><b>Then</b> : une {@link JSONException} est levée.</p>
     *
     * @throws JSONException attendue — le test réussit si elle est levée.
     */
    @Test(expected = JSONException.class)
    public void parseParticipant_idAbsent_leveJSONException() throws JSONException {
        // Given
        JSONObject json = creerJsonParticipantValide();
        json.remove("id");
        // When — doit lever JSONException
        ServiceParticipant.parseParticipant(json);
        // Then — vérifié par @Test(expected = JSONException.class)
    }

    // =========================================================================
    // Tests — extractParticipants — Cas nominaux
    // =========================================================================

    /**
     * Vérifie que {@code extractParticipants} retourne une liste vide pour un JSON
     * ne contenant aucun champ relatif aux participants.
     *
     * <p><b>Given</b> : un JSON vide {@code {}}.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste est vide et non nulle.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void extractParticipants_jsonSansParticipants_retourneListeVide() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull("La liste ne doit pas être null", result);
        assertTrue("La liste doit être vide si aucun champ participants n'est présent", result.isEmpty());
    }

    /**
     * Vérifie que {@code extractParticipants} retourne une liste vide pour un tableau
     * {@code "participants"} vide.
     *
     * <p><b>Given</b> : un JSON avec {@code "participants": []}.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste est vide.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void extractParticipants_tableauParticipantsVide_retourneListeVide() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("participants", new JSONArray());
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertTrue("Un tableau participants vide doit produire une liste vide", result.isEmpty());
    }

    /**
     * Vérifie que {@code extractParticipants} parse un tableau d'un seul participant valide.
     *
     * <p><b>Given</b> : un JSON avec un tableau {@code "participants"} d'un élément.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste contient 1 {@link Participant} avec l'id correct.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void extractParticipants_unParticipant_retourneListeAvecUnElement() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        JSONArray tableau = new JSONArray();
        tableau.put(creerJsonParticipantValide());
        json.put("participants", tableau);
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertEquals("La liste doit contenir 1 participant", 1, result.size());
        assertEquals("L'id du participant doit être correct", ID_PARTICIPANT, result.get(0).getId());
    }

    /**
     * Vérifie que {@code extractParticipants} parse un tableau de trois participants.
     *
     * <p><b>Given</b> : un JSON avec un tableau {@code "participants"} de 3 éléments.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste contient 3 {@link Participant}.</p>
     *
     * @throws JSONException si la construction ou le parsing du JSON échouent.
     */
    @Test
    public void extractParticipants_troisParticipants_retourneListeDeTroisElements() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        JSONArray tableau = new JSONArray();
        for (int i = 1; i <= 3; i++) {
            JSONObject p = creerJsonParticipantValide();
            p.put("id", i);
            tableau.put(p);
        }
        json.put("participants", tableau);
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertEquals("La liste doit contenir 3 participants", 3, result.size());
    }

    // =========================================================================
    // Tests — extractParticipants — Fallback nbParticipants
    // =========================================================================

    /**
     * Vérifie que le fallback sur {@code "nbParticipants"} crée des participants vides
     * quand le tableau {@code "participants"} est absent.
     *
     * <p><b>Given</b> : un JSON avec {@code "nbParticipants": 3} sans tableau.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste contient 3 participants vides.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void extractParticipants_fallbackNbParticipants_retourneParticipantsVides() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("nbParticipants", 3);
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertEquals("Le fallback nbParticipants doit créer 3 participants vides", 3, result.size());
    }

    /**
     * Vérifie que le fallback sur le champ {@code "participants"} entier (non tableau)
     * crée des participants vides.
     *
     * <p><b>Given</b> : un JSON avec {@code "participants": 2} (entier).</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste contient 2 participants vides.</p>
     *
     * <p><i>Note :</i> {@code optJSONArray("participants")} retourne null si le champ
     * est un entier — le fallback {@code optInt("participants", 0)} prend alors la valeur.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void extractParticipants_fallbackParticipantsEntier_retourneParticipantsVides() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("participants", 2);
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertEquals("Un champ 'participants' entier doit créer 2 participants vides", 2, result.size());
    }

    /**
     * Vérifie que le fallback retourne une liste vide quand {@code "nbParticipants"}
     * vaut 0 et qu'il n'y a pas de tableau.
     *
     * <p><b>Given</b> : un JSON avec {@code "nbParticipants": 0}.</p>
     * <p><b>When</b> : on appelle {@code extractParticipants(json)}.</p>
     * <p><b>Then</b> : la liste est vide.</p>
     *
     * @throws JSONException si la construction du JSON échoue.
     */
    @Test
    public void extractParticipants_nbParticipantsZero_retourneListeVide() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("nbParticipants", 0);
        // When
        ArrayList<Participant> result = ServiceParticipant.extractParticipants(json);
        // Then
        assertNotNull(result);
        assertTrue("nbParticipants=0 doit produire une liste vide", result.isEmpty());
    }
}