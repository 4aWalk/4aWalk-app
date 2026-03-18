package fr.iutrodez.a4awalk.servicesTest.gestionAPITest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment;

/**
 * Classe de tests unitaires pour {@link ServiceEquipment}.
 *
 * <p>Teste l'ensemble des méthodes du service d'équipements en simulant
 * les dépendances réseau et Android :</p>
 * <ul>
 *     <li>Cas nominaux : extraction JSON correcte, appels API bien formés</li>
 *     <li>Cas limites : listes vides, champs optionnels absents, type inconnu</li>
 *     <li>Cas d'erreur : JSON malformé, ownerId null, réponse réseau absente</li>
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
public class ServiceEquipmentTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** Token d'authentification utilisé dans les tests */
    private static final String TOKEN_VALIDE = "Bearer.token.test";

    /** Identifiant de randonnée utilisé dans les tests */
    private static final int HIKE_ID = 42;

    /** Identifiant d'équipement utilisé dans les tests */
    private static final int EQUIP_ID = 7;

    /** URL de base des équipements */
    private static final String URL_EQUIPMENTS = "http://98.94.8.220:8080/equipments";

    // =========================================================================
    // MOCKS
    // =========================================================================

    /** Mock du contexte Android */
    @Mock
    private Context mockContexte;

    /** Mock du callback pour les réponses en tableau JSON */
    @Mock
    private AppelAPI.VolleyCallback mockCallbackArray;

    /** Mock du callback pour les réponses en objet JSON */
    @Mock
    private AppelAPI.VolleyObjectCallback mockCallbackObject;

    // =========================================================================
    // CLASSE INTERNE — VolleyError avec code HTTP
    // =========================================================================

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
         * @param statusCode Code HTTP à simuler (ex : 400, 404, 500)
         */
        VolleyErrorAvecCode(int statusCode) {
            super(new NetworkResponse(statusCode, new byte[0], false, 0, null));
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
     * Construit un {@link JSONObject} représentant un équipement complet avec tous ses champs.
     *
     * @param id          Identifiant de l'équipement
     * @param nom         Nom de l'équipement
     * @param type        Type de l'équipement (valeur de l'enum {@link TypeEquipment})
     * @param masse       Masse en grammes
     * @param masseAVide  Masse à vide en grammes
     * @param nbItem      Nombre d'items
     * @param ownerId     Identifiant du propriétaire (peut être null)
     * @return Un {@link JSONObject} représentant l'équipement
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildEquipmentJson(int id, String nom, String type,
                                          double masse, double masseAVide,
                                          int nbItem, Integer ownerId) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("nom", nom);
        obj.put("description", "Description de " + nom);
        obj.put("masseGrammes", masse);
        obj.put("masseAVide", masseAVide);
        obj.put("nbItem", nbItem);
        obj.put("type", type);
        if (ownerId != null) {
            obj.put("ownerId", ownerId);
        } else {
            obj.put("ownerId", JSONObject.NULL);
        }
        return obj;
    }

    /**
     * Construit un {@link JSONObject} simulant une réponse API contenant des groupes
     * d'équipements dans une catégorie donnée.
     *
     * @param categorieKey Clé de la catégorie (ex : "SOIN", "NOURRITURE")
     * @param items        Tableau JSON des équipements de la catégorie
     * @return Un {@link JSONObject} avec la structure {@code equipmentGroups > catégorie > items}
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildReponseAvecGroupes(String categorieKey, JSONArray items) throws JSONException {
        JSONObject categorie = new JSONObject();
        categorie.put("items", items);

        JSONObject groupes = new JSONObject();
        groupes.put(categorieKey, categorie);

        JSONObject reponse = new JSONObject();
        reponse.put("equipmentGroups", groupes);
        return reponse;
    }

    /**
     * Construit un {@link EquipmentItem} de test avec les propriétés minimales.
     *
     * @param id      Identifiant de l'équipement
     * @param ownerId Identifiant du propriétaire (peut être null)
     * @return Un {@link EquipmentItem} configuré
     */
    private EquipmentItem buildEquipmentItem(int id, Integer ownerId) {
        EquipmentItem item = new EquipmentItem();
        item.setId(id);
        item.setNom("Équipement " + id);
        item.setOwnerId(ownerId);
        return item;
    }

    // =========================================================================
    // TESTS : constructEqFromJson
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#constructEqFromJson(JSONObject)} construit
     * correctement un {@link EquipmentItem} depuis un JSON complet avec tous les champs.
     *
     * <p><b>Given</b> un JSONObject complet avec tous les champs d'un équipement<br>
     * <b>When</b> on appelle {@code constructEqFromJson}<br>
     * <b>Then</b> l'objet retourné contient toutes les valeurs correctes</p>
     */
    @Test
    public void constructEqFromJson_jsonComplet_retourneEquipmentCorrect() throws JSONException {
        // Given
        JSONObject json = buildEquipmentJson(1, "Gourde", TypeEquipment.AUTRE.name(),
                500.0, 150.0, 2, 10);

        // When
        EquipmentItem item = ServiceEquipment.constructEqFromJson(json);

        // Then
        assertNotNull("L'équipement ne doit pas être null", item);
        assertEquals("L'id doit être 1", 1, item.getId());
        assertEquals("Le nom doit être 'Gourde'", "Gourde", item.getNom());
        assertEquals("La description doit correspondre", "Description de Gourde", item.getDescription());
        assertEquals("La masse doit être 500.0", 500.0, item.getMasseGrammes(), 0.001);
        assertEquals("La masse à vide doit être 150.0", 150.0, item.getMasseAVide(), 0.001);
        assertEquals("Le nombre d'items doit être 2", 2, item.getNbItem());
        assertEquals("Le ownerId doit être 10", Integer.valueOf(10), item.getOwnerId());
        assertEquals("Le type doit être AUTRE", TypeEquipment.AUTRE, item.getType());
    }

    /**
     * Vérifie que {@code constructEqFromJson} affecte {@code null} à {@code ownerId}
     * lorsque le champ JSON est explicitement {@code null}.
     *
     * <p><b>Given</b> un JSON avec le champ {@code ownerId} à JSON null<br>
     * <b>When</b> on appelle {@code constructEqFromJson}<br>
     * <b>Then</b> le {@code ownerId} de l'item est null (pas de propriétaire)</p>
     */
    @Test
    public void constructEqFromJson_ownerIdNull_setOwnerIdNull() throws JSONException {
        // Given
        JSONObject json = buildEquipmentJson(2, "Tente", TypeEquipment.AUTRE.name(),
                1200.0, 800.0, 1, null);

        // When
        EquipmentItem item = ServiceEquipment.constructEqFromJson(json);

        // Then
        assertNull("Le ownerId doit être null quand le JSON contient null", item.getOwnerId());
    }

    /**
     * Vérifie que {@code constructEqFromJson} utilise la valeur par défaut {@link TypeEquipment#AUTRE}
     * lorsque le type JSON ne correspond à aucune valeur de l'enum.
     *
     * <p><b>Given</b> un JSON avec un type inconnu ("TYPE_INEXISTANT")<br>
     * <b>When</b> on appelle {@code constructEqFromJson}<br>
     * <b>Then</b> le type de l'item est {@link TypeEquipment#AUTRE} (sécurité par défaut)</p>
     */
    @Test
    public void constructEqFromJson_typeInconnu_setTypeAUTRE() throws JSONException {
        // Given
        JSONObject json = new JSONObject();
        json.put("id", 3);
        json.put("nom", "Objet inconnu");
        json.put("type", "TYPE_QUI_NEXISTE_PAS");

        // When
        EquipmentItem item = ServiceEquipment.constructEqFromJson(json);

        // Then
        assertEquals("Un type inconnu doit être remplacé par AUTRE",
                TypeEquipment.AUTRE, item.getType());
    }

    /**
     * Vérifie que {@code constructEqFromJson} utilise les valeurs par défaut
     * pour tous les champs optionnels absents du JSON.
     *
     * <p><b>Given</b> un JSON vide {}<br>
     * <b>When</b> on appelle {@code constructEqFromJson}<br>
     * <b>Then</b> les champs ont leurs valeurs par défaut (0, "", AUTRE)</p>
     */
    @Test
    public void constructEqFromJson_jsonVide_retourneValeursParDefaut() throws JSONException {
        // Given
        JSONObject jsonVide = new JSONObject();

        // When
        EquipmentItem item = ServiceEquipment.constructEqFromJson(jsonVide);

        // Then
        assertNotNull("L'item ne doit pas être null avec un JSON vide", item);
        assertEquals("L'id par défaut doit être 0", 0, item.getId());
        assertEquals("Le nom par défaut doit être vide", "", item.getNom());
        assertEquals("La description par défaut doit être vide", "", item.getDescription());
        assertEquals("La masse par défaut doit être 0.0", 0.0, item.getMasseGrammes(), 0.001);
        assertEquals("Le nbItem par défaut doit être 1", 1, item.getNbItem());
        assertEquals("Le type par défaut doit être AUTRE", TypeEquipment.AUTRE, item.getType());
    }

    // =========================================================================
    // TESTS : extractEquipmentGroups
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#extractEquipmentGroups(JSONObject)} retourne
     * une liste correctement peuplée depuis une réponse JSON avec une catégorie et plusieurs items.
     *
     * <p><b>Given</b> une réponse JSON avec un groupe contenant 2 équipements<br>
     * <b>When</b> on appelle {@code extractEquipmentGroups}<br>
     * <b>Then</b> la liste retournée contient 2 éléments avec les bonnes valeurs</p>
     */
    @Test
    public void extractEquipmentGroups_deuxItems_retourneListeDeuxElements() throws JSONException {
        // Given
        JSONArray items = new JSONArray();
        items.put(buildEquipmentJson(1, "Lampe", TypeEquipment.AUTRE.name(), 200.0, 50.0, 1, null));
        items.put(buildEquipmentJson(2, "Couteau", TypeEquipment.AUTRE.name(), 150.0, 30.0, 1, 5));
        JSONObject reponse = buildReponseAvecGroupes("OUTIL", items);

        // When
        List<EquipmentItem> liste = ServiceEquipment.extractEquipmentGroups(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertEquals("La liste doit contenir 2 éléments", 2, liste.size());
    }

    /**
     * Vérifie que {@code extractEquipmentGroups} retourne une liste vide
     * lorsque la réponse ne contient pas de champ {@code equipmentGroups}.
     *
     * <p><b>Given</b> une réponse JSON sans champ "equipmentGroups"<br>
     * <b>When</b> on appelle {@code extractEquipmentGroups}<br>
     * <b>Then</b> une liste vide est retournée sans exception</p>
     */
    @Test
    public void extractEquipmentGroups_sansEquipmentGroups_retourneListeVide() {
        // Given
        JSONObject reponseSansGroupes = new JSONObject();

        // When
        List<EquipmentItem> liste = ServiceEquipment.extractEquipmentGroups(reponseSansGroupes);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide en l'absence de equipmentGroups", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractEquipmentGroups} retourne une liste vide
     * lorsque tous les items d'une catégorie sont absents (tableau null).
     *
     * <p><b>Given</b> une réponse JSON avec une catégorie sans tableau "items"<br>
     * <b>When</b> on appelle {@code extractEquipmentGroups}<br>
     * <b>Then</b> aucune exception n'est levée et la liste est vide</p>
     */
    @Test
    public void extractEquipmentGroups_categoriesSansItems_retourneListeVide() throws JSONException {
        // Given
        JSONObject categorieVide = new JSONObject();
        // Pas de champ "items"
        JSONObject groupes = new JSONObject();
        groupes.put("OUTIL", categorieVide);
        JSONObject reponse = new JSONObject();
        reponse.put("equipmentGroups", groupes);

        // When
        List<EquipmentItem> liste = ServiceEquipment.extractEquipmentGroups(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide si la catégorie n'a pas d'items", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractEquipmentGroups} agrège correctement les équipements
     * issus de plusieurs catégories différentes.
     *
     * <p><b>Given</b> une réponse JSON avec deux catégories contenant 1 item chacune<br>
     * <b>When</b> on appelle {@code extractEquipmentGroups}<br>
     * <b>Then</b> la liste contient 2 éléments au total (1 par catégorie)</p>
     */
    @Test
    public void extractEquipmentGroups_deuxCategories_aggrelesElements() throws JSONException {
        // Given
        JSONArray items1 = new JSONArray();
        items1.put(buildEquipmentJson(1, "Lampe", TypeEquipment.AUTRE.name(), 200.0, 50.0, 1, null));

        JSONArray items2 = new JSONArray();
        items2.put(buildEquipmentJson(2, "Gourde", TypeEquipment.AUTRE.name(), 500.0, 150.0, 1, null));

        JSONObject categorie1 = new JSONObject();
        categorie1.put("items", items1);
        JSONObject categorie2 = new JSONObject();
        categorie2.put("items", items2);

        JSONObject groupes = new JSONObject();
        groupes.put("ECLAIRAGE", categorie1);
        groupes.put("HYDRATATION", categorie2);

        JSONObject reponse = new JSONObject();
        reponse.put("equipmentGroups", groupes);

        // When
        List<EquipmentItem> liste = ServiceEquipment.extractEquipmentGroups(reponse);

        // Then
        assertEquals("La liste doit contenir 2 éléments (1 par catégorie)", 2, liste.size());
    }

    // =========================================================================
    // TESTS : extractEquipmentCatalogue
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#extractEquipmentCatalogue(JSONObject)} est bien
     * un alias de {@code extractEquipmentGroups} et retourne les mêmes résultats.
     *
     * <p><b>Given</b> une réponse JSON avec un groupe d'équipements<br>
     * <b>When</b> on appelle {@code extractEquipmentCatalogue}<br>
     * <b>Then</b> la liste retournée est identique à celle de {@code extractEquipmentGroups}</p>
     */
    @Test
    public void extractEquipmentCatalogue_memeResultatQueExtractGroups() throws JSONException {
        // Given
        JSONArray items = new JSONArray();
        items.put(buildEquipmentJson(1, "Sac", TypeEquipment.AUTRE.name(), 800.0, 300.0, 1, null));
        JSONObject reponse = buildReponseAvecGroupes("SAC", items);

        // When
        List<EquipmentItem> listeGroups = ServiceEquipment.extractEquipmentGroups(reponse);
        List<EquipmentItem> listeCatalogue = ServiceEquipment.extractEquipmentCatalogue(reponse);

        // Then
        assertEquals("Les deux méthodes doivent retourner le même nombre d'éléments",
                listeGroups.size(), listeCatalogue.size());
        assertEquals("Le premier élément doit avoir le même id",
                listeGroups.get(0).getId(), listeCatalogue.get(0).getId());
    }

    // =========================================================================
    // TESTS : extractEquipmentsForBackpack
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#extractEquipmentsForBackpack(JSONArray)} retourne
     * un ensemble non null et peuplé depuis un tableau JSON valide.
     *
     * <p><b>Given</b> un tableau JSON contenant 2 équipements<br>
     * <b>When</b> on appelle {@code extractEquipmentsForBackpack}<br>
     * <b>Then</b> le Set retourné contient 2 éléments distincts</p>
     */
    @Test
    public void extractEquipmentsForBackpack_deuxEquipements_retourneSetDeuxElements() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        JSONObject eq1 = new JSONObject();
        eq1.put("id", 10);
        eq1.put("nom", "Boussole");
        eq1.put("masseGrammes", 80.0);
        eq1.put("nbItem", 1);
        tableau.put(eq1);

        JSONObject eq2 = new JSONObject();
        eq2.put("id", 11);
        eq2.put("nom", "Carte");
        eq2.put("masseGrammes", 50.0);
        eq2.put("nbItem", 2);
        tableau.put(eq2);

        // When
        Set<EquipmentItem> set = ServiceEquipment.extractEquipmentsForBackpack(tableau);

        // Then
        assertNotNull("Le Set ne doit pas être null", set);
        assertEquals("Le Set doit contenir 2 éléments", 2, set.size());
    }

    /**
     * Vérifie que {@code extractEquipmentsForBackpack} retourne un Set vide
     * lorsque le tableau JSON passé est null.
     *
     * <p><b>Given</b> un tableau JSON null<br>
     * <b>When</b> on appelle {@code extractEquipmentsForBackpack}<br>
     * <b>Then</b> un Set vide est retourné sans exception</p>
     */
    @Test
    public void extractEquipmentsForBackpack_tableauNull_retourneSetVide() {
        // Given — tableau null

        // When
        Set<EquipmentItem> set = ServiceEquipment.extractEquipmentsForBackpack(null);

        // Then
        assertNotNull("Le Set ne doit pas être null même avec un tableau null", set);
        assertTrue("Le Set doit être vide avec un tableau null", set.isEmpty());
    }

    /**
     * Vérifie que {@code extractEquipmentsForBackpack} retourne un Set vide
     * lorsque le tableau JSON est vide [].
     *
     * <p><b>Given</b> un tableau JSON vide<br>
     * <b>When</b> on appelle {@code extractEquipmentsForBackpack}<br>
     * <b>Then</b> le Set retourné est vide</p>
     */
    @Test
    public void extractEquipmentsForBackpack_tableauVide_retourneSetVide() {
        // Given
        JSONArray tableauVide = new JSONArray();

        // When
        Set<EquipmentItem> set = ServiceEquipment.extractEquipmentsForBackpack(tableauVide);

        // Then
        assertNotNull("Le Set ne doit pas être null", set);
        assertTrue("Le Set doit être vide avec un tableau vide", set.isEmpty());
    }

    /**
     * Vérifie que {@code extractEquipmentsForBackpack} ignore silencieusement
     * les entrées null dans le tableau JSON (éléments corrompus).
     *
     * <p><b>Given</b> un tableau JSON contenant une entrée null<br>
     * <b>When</b> on appelle {@code extractEquipmentsForBackpack}<br>
     * <b>Then</b> aucune exception n'est levée et le Set ne contient que les entrées valides</p>
     */
    @Test
    public void extractEquipmentsForBackpack_entreeNullDansTableau_ignoree() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        tableau.put(JSONObject.NULL); // Entrée nulle dans le tableau

        JSONObject eq = new JSONObject();
        eq.put("id", 5);
        eq.put("nom", "Allumettes");
        tableau.put(eq);

        // When
        Set<EquipmentItem> set = ServiceEquipment.extractEquipmentsForBackpack(tableau);

        // Then — seul l'équipement valide est dans le set
        assertEquals("Seul l'équipement valide doit être dans le Set", 1, set.size());
    }

    // =========================================================================
    // TESTS : getAllEquipments
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#getAllEquipments(Context, String, AppelAPI.VolleyCallback)}
     * appelle bien {@link AppelAPI#get(String, String, Context, AppelAPI.VolleyCallback)}
     * avec l'URL correcte des équipements.
     *
     * <p><b>Given</b> un contexte et un token valides<br>
     * <b>When</b> on appelle {@code getAllEquipments}<br>
     * <b>Then</b> AppelAPI.get est invoqué avec l'URL {@code /equipments}</p>
     */
    @Test
    public void getAllEquipments_appelleAppelAPIGet_avecBonneUrl() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.get(captureurUrl.capture(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.getAllEquipments(mockContexte, TOKEN_VALIDE, mockCallbackArray);

            // Then
            assertEquals("L'URL doit pointer vers l'endpoint /equipments",
                    URL_EQUIPMENTS, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que {@code getAllEquipments} transmet correctement le token
     * à {@link AppelAPI#get}.
     *
     * <p><b>Given</b> un token valide<br>
     * <b>When</b> on appelle {@code getAllEquipments}<br>
     * <b>Then</b> le token passé à AppelAPI.get est identique au token fourni</p>
     */
    @Test
    public void getAllEquipments_transmettreTokenCorrect() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.get(anyString(), captureurToken.capture(),
                            any(Context.class), any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.getAllEquipments(mockContexte, TOKEN_VALIDE, mockCallbackArray);

            // Then
            assertEquals("Le token transmis à AppelAPI.get doit être identique",
                    TOKEN_VALIDE, captureurToken.getValue());
        }
    }

    // =========================================================================
    // TESTS : creerNouveauEquipement
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#creerNouveauEquipement} construit le bon corps JSON
     * avec toutes les propriétés de l'équipement à créer.
     *
     * <p><b>Given</b> les caractéristiques complètes d'un nouvel équipement<br>
     * <b>When</b> on appelle {@code creerNouveauEquipement}<br>
     * <b>Then</b> le body JSON passé à AppelAPI.post contient tous les champs attendus</p>
     */
    @Test
    public void creerNouveauEquipement_corpsRequete_contientTousLesChamps() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.creerNouveauEquipement(
                    mockContexte, TOKEN_VALIDE,
                    "Bâton de marche", 350.0, "Bâton télescopique",
                    TypeEquipment.AUTRE, 200.0, 2, mockCallbackObject
            );

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps de la requête ne doit pas être null", body);
            assertEquals("Le nom doit être correct", "Bâton de marche", body.getString("nom"));
            assertEquals("La masse doit être correcte", 350.0, body.getDouble("masseGrammes"), 0.001);
            assertEquals("La description doit être correcte", "Bâton télescopique", body.getString("description"));
            assertEquals("Le type doit être correct", TypeEquipment.AUTRE.name(), body.getString("type"));
            assertEquals("La masse à vide doit être correcte", 200.0, body.getDouble("masseAVide"), 0.001);
            assertEquals("Le nombre d'items doit être correct", 2, body.getInt("nbItem"));
        }
    }

    /**
     * Vérifie que {@code creerNouveauEquipement} appelle {@link AppelAPI#post}
     * avec l'URL correcte de l'endpoint des équipements.
     *
     * <p><b>Given</b> des paramètres valides pour un nouvel équipement<br>
     * <b>When</b> on appelle {@code creerNouveauEquipement}<br>
     * <b>Then</b> l'URL passée à AppelAPI.post est {@code /equipments}</p>
     */
    @Test
    public void creerNouveauEquipement_urlUtilisee_estEquipmentsUrl() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(), any(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.creerNouveauEquipement(mockContexte, TOKEN_VALIDE,
                    "Gourde", 300.0, "Desc", TypeEquipment.AUTRE, 100.0, 1, mockCallbackObject);

            // Then
            assertEquals("L'URL de création doit pointer vers /equipments",
                    URL_EQUIPMENTS, captureurUrl.getValue());
        }
    }

    // =========================================================================
    // TESTS : lierEquipmentARandonnee
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#lierEquipmentARandonnee} construit l'URL
     * correcte sans query parameter lorsque {@code ownerId} est null.
     *
     * <p><b>Given</b> un hikeId, un equipId et un ownerId null<br>
     * <b>When</b> on appelle {@code lierEquipmentARandonnee}<br>
     * <b>Then</b> l'URL ne contient pas de paramètre {@code ?owner=...}</p>
     */
    @Test
    public void lierEquipmentARandonnee_ownerIdNull_urlSansQueryParam() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.lierEquipmentARandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, EQUIP_ID, null, mockCallbackObject);

            // Then
            String url = captureurUrl.getValue();
            assertFalse("L'URL ne doit pas contenir '?owner=' quand ownerId est null",
                    url.contains("?owner="));
            assertTrue("L'URL doit contenir le hikeId",
                    url.contains(String.valueOf(HIKE_ID)));
            assertTrue("L'URL doit contenir l'equipId",
                    url.contains(String.valueOf(EQUIP_ID)));
        }
    }

    /**
     * Vérifie que {@code lierEquipmentARandonnee} ajoute le query parameter {@code ?owner=}
     * lorsque {@code ownerId} est non null.
     *
     * <p><b>Given</b> un hikeId, un equipId et un ownerId non null<br>
     * <b>When</b> on appelle {@code lierEquipmentARandonnee}<br>
     * <b>Then</b> l'URL contient {@code ?owner=<ownerId>}</p>
     */
    @Test
    public void lierEquipmentARandonnee_ownerIdNonNull_urlAvecQueryParam() {
        // Given
        int ownerId = 99;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.lierEquipmentARandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, EQUIP_ID, ownerId, mockCallbackObject);

            // Then
            String url = captureurUrl.getValue();
            assertTrue("L'URL doit contenir '?owner=99'", url.contains("?owner=" + ownerId));
        }
    }

    /**
     * Vérifie que {@code lierEquipmentARandonnee} appelle bien {@link AppelAPI#post}
     * (et non GET, PUT ou DELETE).
     *
     * <p><b>Given</b> des paramètres valides<br>
     * <b>When</b> on appelle {@code lierEquipmentARandonnee}<br>
     * <b>Then</b> c'est AppelAPI.post qui est invoqué</p>
     */
    @Test
    public void lierEquipmentARandonnee_utilisePost() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.lierEquipmentARandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, EQUIP_ID, null, mockCallbackObject);

            // Then
            staticMock.verify(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                    any(Context.class), any(AppelAPI.VolleyObjectCallback.class)));
        }
    }

    // =========================================================================
    // TESTS : retirerEquipmentDeRandonnee
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#retirerEquipmentDeRandonnee} appelle
     * {@link AppelAPI#delete} avec l'URL correcte.
     *
     * <p><b>Given</b> un hikeId et un equipId valides<br>
     * <b>When</b> on appelle {@code retirerEquipmentDeRandonnee}<br>
     * <b>Then</b> AppelAPI.delete est invoqué avec l'URL {@code /hikes/{hikeId}/equipment/{equipId}}</p>
     */
    @Test
    public void retirerEquipmentDeRandonnee_appelleDelete_avecBonneUrl() {
        // Given
        String urlAttendue = "http://98.94.8.220:8080/hikes/" + HIKE_ID + "/equipment/" + EQUIP_ID;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.delete(captureurUrl.capture(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.retirerEquipmentDeRandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, EQUIP_ID, mockCallbackObject);

            // Then
            assertEquals("L'URL de suppression doit correspondre à /hikes/{id}/equipment/{id}",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    // =========================================================================
    // TESTS : synchroniserEquipments
    // =========================================================================

    /**
     * Vérifie que {@link ServiceEquipment#synchroniserEquipments} n'effectue aucun appel API
     * lorsque les listes initiale et modifiée sont identiques (aucun changement).
     *
     * <p><b>Given</b> une liste initiale et modifiée identiques (même équipement)<br>
     * <b>When</b> on appelle {@code synchroniserEquipments}<br>
     * <b>Then</b> aucun appel à AppelAPI.post ou AppelAPI.delete n'est effectué</p>
     */
    @Test
    public void synchroniserEquipments_listesIdentiques_aucunAppelAPI() {
        // Given
        List<EquipmentItem> listeInitiale = new ArrayList<>();
        listeInitiale.add(buildEquipmentItem(1, null));

        List<EquipmentItem> listeModifiee = new ArrayList<>();
        listeModifiee.add(buildEquipmentItem(1, null));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServiceEquipment.synchroniserEquipments(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, listeInitiale, listeModifiee);

            // Then — aucun appel POST ou DELETE
            staticMock.verify(() -> AppelAPI.post(anyString(), anyString(), any(), any(), any()),
                    never());
            staticMock.verify(() -> AppelAPI.delete(anyString(), anyString(), any(), any()),
                    never());
        }
    }

    /**
     * Vérifie que {@code synchroniserEquipments} effectue un appel POST
     * pour chaque équipement présent dans la liste modifiée mais absent de la liste initiale.
     *
     * <p><b>Given</b> une liste initiale vide et une liste modifiée avec 1 équipement<br>
     * <b>When</b> on appelle {@code synchroniserEquipments}<br>
     * <b>Then</b> un appel POST est effectué pour lier le nouvel équipement</p>
     */
    @Test
    public void synchroniserEquipments_unAjout_unAppelPost() {
        // Given
        List<EquipmentItem> listeInitiale = new ArrayList<>();
        List<EquipmentItem> listeModifiee = new ArrayList<>();
        listeModifiee.add(buildEquipmentItem(10, null));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceEquipment.synchroniserEquipments(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, listeInitiale, listeModifiee);

            // Then
            staticMock.verify(() -> AppelAPI.post(anyString(), anyString(), isNull(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)),
                    times(1));
        }
    }

    /**
     * Vérifie que {@code synchroniserEquipments} n'effectue aucun appel API
     * lorsque les deux listes sont vides.
     *
     * <p><b>Given</b> deux listes vides<br>
     * <b>When</b> on appelle {@code synchroniserEquipments}<br>
     * <b>Then</b> aucun appel réseau n'est effectué</p>
     */
    @Test
    public void synchroniserEquipments_deuxListesVides_aucunAppelAPI() {
        // Given
        List<EquipmentItem> listeVide1 = new ArrayList<>();
        List<EquipmentItem> listeVide2 = new ArrayList<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServiceEquipment.synchroniserEquipments(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, listeVide1, listeVide2);

            // Then
            staticMock.verify(() -> AppelAPI.post(any(), any(), any(), any(), any()), never());
            staticMock.verify(() -> AppelAPI.delete(any(), any(), any(), any()), never());
        }
    }
}