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

import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;

/**
 * Classe de tests unitaires pour {@link ServiceFoodProduct}.
 *
 * <p>Teste l'ensemble des méthodes du service de produits alimentaires
 * en simulant les dépendances réseau et Android :</p>
 * <ul>
 *     <li>Cas nominaux : extraction JSON correcte, corps de requête bien formé,
 *         URLs et méthodes HTTP correctes</li>
 *     <li>Cas limites : catalogue vide, tableau null, conditionnement vide,
 *         listes identiques sans appel API</li>
 *     <li>Cas d'erreur : champs JSON absents, entrées null dans tableau,
 *         produit ajouté ou supprimé</li>
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
public class ServiceFoodProductTest {

    // =========================================================================
    // CONSTANTES DE TEST
    // =========================================================================

    /** URL de base de l'API */
    private static final String BASE_URL = "http://98.94.8.220:8080";

    /** Token d'authentification utilisé dans les tests */
    private static final String TOKEN_VALIDE = "Bearer.token.test";

    /** Identifiant de randonnée utilisé dans les tests */
    private static final int HIKE_ID = 42;

    /** Identifiant de produit alimentaire utilisé dans les tests */
    private static final int FOOD_ID = 5;

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
     * Construit un {@link JSONObject} représentant un produit alimentaire complet.
     *
     * @param id             Identifiant du produit
     * @param nom            Nom du produit
     * @param masse          Masse en grammes
     * @param appellation    Appellation courante
     * @param conditionnement Conditionnement du produit
     * @param kcal           Apport nutritionnel en kcal
     * @param prix           Prix en euros
     * @param nbItem         Nombre d'items
     * @return Un {@link JSONObject} représentant le produit
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildFoodProductJson(int id, String nom, double masse,
                                            String appellation, String conditionnement,
                                            double kcal, double prix, int nbItem) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("nom", nom);
        obj.put("masseGrammes", masse);
        obj.put("appelationCourante", appellation); // Note : faute de frappe volontaire conservée (cohérence API)
        obj.put("conditionnement", conditionnement);
        obj.put("apportNutritionnelKcal", kcal);
        obj.put("prixEuro", prix);
        obj.put("nbItem", nbItem);
        return obj;
    }

    /**
     * Construit un {@link FoodProduct} de test avec les propriétés minimales.
     *
     * @param id Identifiant du produit
     * @return Un {@link FoodProduct} configuré avec l'id donné
     */
    private FoodProduct buildFoodProduct(int id) {
        FoodProduct fp = new FoodProduct();
        fp.setId(id);
        fp.setNom("Produit " + id);
        return fp;
    }

    /**
     * Construit un {@link JSONObject} simulant une réponse API contenant
     * un tableau {@code foodCatalogue} avec les produits donnés.
     *
     * @param produits Tableau JSON des produits alimentaires
     * @return Un {@link JSONObject} avec la structure {@code {foodCatalogue: [...]}}
     * @throws JSONException si la construction du JSON échoue
     */
    private JSONObject buildReponseAvecCatalogue(JSONArray produits) throws JSONException {
        JSONObject reponse = new JSONObject();
        reponse.put("foodCatalogue", produits);
        return reponse;
    }

    // =========================================================================
    // TESTS : constructFPFromJson
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#constructFPFromJson(JSONObject)} construit
     * correctement un {@link FoodProduct} depuis un JSON complet avec tous les champs.
     *
     * <p><b>Given</b> un JSONObject complet avec tous les champs d'un produit alimentaire<br>
     * <b>When</b> on appelle {@code constructFPFromJson}<br>
     * <b>Then</b> l'objet retourné contient toutes les valeurs correctes</p>
     */
    @Test
    public void constructFPFromJson_jsonComplet_retourneFoodProductCorrect() throws JSONException {
        // Given
        JSONObject json = buildFoodProductJson(1, "Barre de céréales", 45.0,
                "Barre énergétique", "Sachet", 180.0, 1.50, 3);

        // When
        FoodProduct fp = ServiceFoodProduct.constructFPFromJson(json);

        // Then
        assertNotNull("Le FoodProduct ne doit pas être null", fp);
        assertEquals("L'id doit être 1", 1, fp.getId());
        assertEquals("Le nom doit être 'Barre de céréales'", "Barre de céréales", fp.getNom());
        assertEquals("La masse doit être 45.0", 45.0, fp.getMasseGrammes(), 0.001);
        assertEquals("L'appellation doit correspondre", "Barre énergétique", fp.getAppellationCourante());
        assertEquals("Le conditionnement doit correspondre", "Sachet", fp.getConditionnement());
        assertEquals("Les kcal doivent être 180.0", 180.0, fp.getApportNutritionnelKcal(), 0.001);
        assertEquals("Le prix doit être 1.50", 1.50, fp.getPrixEuro(), 0.001);
        assertEquals("Le nbItem doit être 3", 3, fp.getNbItem());
    }

    /**
     * Vérifie que {@code constructFPFromJson} lève une {@link JSONException}
     * lorsqu'un champ obligatoire (ex : "id") est absent du JSON.
     *
     * <p><b>Given</b> un JSONObject sans le champ "id" (obligatoire via {@code getInt})<br>
     * <b>When</b> on appelle {@code constructFPFromJson}<br>
     * <b>Then</b> une {@link JSONException} est levée car {@code getInt} est strict</p>
     */
    @Test(expected = JSONException.class)
    public void constructFPFromJson_champIdAbsent_leveJSONException() throws JSONException {
        // Given — JSON sans le champ "id"
        JSONObject jsonSansId = new JSONObject();
        jsonSansId.put("nom", "Produit sans id");
        jsonSansId.put("masseGrammes", 100.0);
        jsonSansId.put("appelationCourante", "Appellation");
        jsonSansId.put("conditionnement", "Boite");
        jsonSansId.put("apportNutritionnelKcal", 200.0);
        jsonSansId.put("prixEuro", 2.0);
        jsonSansId.put("nbItem", 1);

        // When — doit lever JSONException
        ServiceFoodProduct.constructFPFromJson(jsonSansId);
    }

    /**
     * Vérifie que {@code constructFPFromJson} conserve correctement
     * une valeur de masse nulle (0.0).
     *
     * <p><b>Given</b> un JSON avec une masse égale à 0.0<br>
     * <b>When</b> on appelle {@code constructFPFromJson}<br>
     * <b>Then</b> la masse du produit retourné est bien 0.0</p>
     */
    @Test
    public void constructFPFromJson_masseNulle_retourneMasseZero() throws JSONException {
        // Given
        JSONObject json = buildFoodProductJson(2, "Produit léger", 0.0,
                "Appellation", "Vrac", 50.0, 0.5, 1);

        // When
        FoodProduct fp = ServiceFoodProduct.constructFPFromJson(json);

        // Then
        assertEquals("La masse doit être 0.0", 0.0, fp.getMasseGrammes(), 0.001);
    }

    /**
     * Vérifie que {@code constructFPFromJson} gère correctement un prix à 0.0
     * (produit gratuit).
     *
     * <p><b>Given</b> un JSON avec un prix à 0.0<br>
     * <b>When</b> on appelle {@code constructFPFromJson}<br>
     * <b>Then</b> le prix retourné est bien 0.0</p>
     */
    @Test
    public void constructFPFromJson_prixZero_retournePrixZero() throws JSONException {
        // Given
        JSONObject json = buildFoodProductJson(3, "Produit gratuit", 100.0,
                "Appellation", "Sachet", 100.0, 0.0, 1);

        // When
        FoodProduct fp = ServiceFoodProduct.constructFPFromJson(json);

        // Then
        assertEquals("Le prix doit être 0.0", 0.0, fp.getPrixEuro(), 0.001);
    }

    // =========================================================================
    // TESTS : extractFoodCatalogue
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#extractFoodCatalogue(JSONObject)} retourne
     * une liste correctement peuplée depuis une réponse JSON avec plusieurs produits.
     *
     * <p><b>Given</b> une réponse JSON avec un catalogue de 2 produits<br>
     * <b>When</b> on appelle {@code extractFoodCatalogue}<br>
     * <b>Then</b> la liste retournée contient 2 éléments avec les bons ids</p>
     */
    @Test
    public void extractFoodCatalogue_deuxProduits_retourneListeDeuxElements() throws JSONException {
        // Given
        JSONArray catalogue = new JSONArray();
        catalogue.put(buildFoodProductJson(1, "Pomme", 182.0, "Fruit", "Vrac", 52.0, 0.3, 1));
        catalogue.put(buildFoodProductJson(2, "Pain", 50.0, "Viennoiserie", "Sachet", 130.0, 0.8, 2));
        JSONObject reponse = buildReponseAvecCatalogue(catalogue);

        // When
        List<FoodProduct> liste = ServiceFoodProduct.extractFoodCatalogue(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertEquals("La liste doit contenir 2 éléments", 2, liste.size());
        assertEquals("Le premier produit doit avoir l'id 1", 1, liste.get(0).getId());
        assertEquals("Le second produit doit avoir l'id 2", 2, liste.get(1).getId());
    }

    /**
     * Vérifie que {@code extractFoodCatalogue} retourne une liste vide
     * lorsque la réponse ne contient pas de champ {@code foodCatalogue}.
     *
     * <p><b>Given</b> une réponse JSON sans champ "foodCatalogue"<br>
     * <b>When</b> on appelle {@code extractFoodCatalogue}<br>
     * <b>Then</b> une liste vide est retournée sans exception</p>
     */
    @Test
    public void extractFoodCatalogue_sansChampCatalogue_retourneListeVide() {
        // Given
        JSONObject reponseSansCatalogue = new JSONObject();

        // When
        List<FoodProduct> liste = ServiceFoodProduct.extractFoodCatalogue(reponseSansCatalogue);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide en l'absence de foodCatalogue", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractFoodCatalogue} retourne une liste vide
     * lorsque le tableau {@code foodCatalogue} est vide [].
     *
     * <p><b>Given</b> une réponse JSON avec un catalogue vide []<br>
     * <b>When</b> on appelle {@code extractFoodCatalogue}<br>
     * <b>Then</b> la liste retournée est vide</p>
     */
    @Test
    public void extractFoodCatalogue_catalogueVide_retourneListeVide() throws JSONException {
        // Given
        JSONObject reponse = buildReponseAvecCatalogue(new JSONArray());

        // When
        List<FoodProduct> liste = ServiceFoodProduct.extractFoodCatalogue(reponse);

        // Then
        assertNotNull("La liste ne doit pas être null", liste);
        assertTrue("La liste doit être vide avec un catalogue vide", liste.isEmpty());
    }

    /**
     * Vérifie que {@code extractFoodCatalogue} gère silencieusement un JSON malformé
     * dans le catalogue (champ obligatoire manquant) en retournant une liste partielle.
     *
     * <p><b>Given</b> un catalogue avec un produit valide et un produit sans "id"<br>
     * <b>When</b> on appelle {@code extractFoodCatalogue}<br>
     * <b>Then</b> aucune exception n'est propagée (JSONException attrapée en interne)</p>
     */
    @Test
    public void extractFoodCatalogue_produitMalForme_pasException() throws JSONException {
        // Given — un produit sans champ "id" qui provoquera une JSONException interne
        JSONArray catalogue = new JSONArray();
        JSONObject produitSansId = new JSONObject();
        produitSansId.put("nom", "Produit corrompu");
        // Pas de champ "id" → JSONException interne attrapée par le service
        catalogue.put(produitSansId);
        JSONObject reponse = buildReponseAvecCatalogue(catalogue);

        // When / Then — aucune exception ne doit se propager
        try {
            List<FoodProduct> liste = ServiceFoodProduct.extractFoodCatalogue(reponse);
            assertNotNull("La liste ne doit pas être null même avec un produit corrompu", liste);
        } catch (Exception e) {
            fail("Aucune exception ne doit se propager depuis extractFoodCatalogue : "
                    + e.getMessage());
        }
    }

    // =========================================================================
    // TESTS : extractFoodForBackpack
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#extractFoodForBackpack(JSONArray)} retourne
     * un Set non null et peuplé depuis un tableau JSON valide.
     *
     * <p><b>Given</b> un tableau JSON contenant 2 produits alimentaires<br>
     * <b>When</b> on appelle {@code extractFoodForBackpack}<br>
     * <b>Then</b> le Set retourné contient 2 éléments distincts</p>
     */
    @Test
    public void extractFoodForBackpack_deuxProduits_retourneSetDeuxElements() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        JSONObject food1 = new JSONObject();
        food1.put("id", 10);
        food1.put("nom", "Biscuit");
        food1.put("masseGrammes", 30.0);
        food1.put("nbItem", 5);
        tableau.put(food1);

        JSONObject food2 = new JSONObject();
        food2.put("id", 11);
        food2.put("nom", "Compote");
        food2.put("masseGrammes", 90.0);
        food2.put("nbItem", 1);
        tableau.put(food2);

        // When
        Set<FoodProduct> set = ServiceFoodProduct.extractFoodForBackpack(tableau);

        // Then
        assertNotNull("Le Set ne doit pas être null", set);
        assertEquals("Le Set doit contenir 2 éléments", 2, set.size());
    }

    /**
     * Vérifie que {@code extractFoodForBackpack} retourne un Set vide
     * lorsque le tableau JSON passé est null.
     *
     * <p><b>Given</b> un tableau JSON null<br>
     * <b>When</b> on appelle {@code extractFoodForBackpack}<br>
     * <b>Then</b> un Set vide est retourné sans exception</p>
     */
    @Test
    public void extractFoodForBackpack_tableauNull_retourneSetVide() {
        // Given — tableau null

        // When
        Set<FoodProduct> set = ServiceFoodProduct.extractFoodForBackpack(null);

        // Then
        assertNotNull("Le Set ne doit pas être null même avec un tableau null", set);
        assertTrue("Le Set doit être vide avec un tableau null", set.isEmpty());
    }

    /**
     * Vérifie que {@code extractFoodForBackpack} retourne un Set vide
     * lorsque le tableau JSON est vide [].
     *
     * <p><b>Given</b> un tableau JSON vide<br>
     * <b>When</b> on appelle {@code extractFoodForBackpack}<br>
     * <b>Then</b> le Set retourné est vide</p>
     */
    @Test
    public void extractFoodForBackpack_tableauVide_retourneSetVide() {
        // Given
        JSONArray tableauVide = new JSONArray();

        // When
        Set<FoodProduct> set = ServiceFoodProduct.extractFoodForBackpack(tableauVide);

        // Then
        assertNotNull("Le Set ne doit pas être null", set);
        assertTrue("Le Set doit être vide avec un tableau vide", set.isEmpty());
    }

    /**
     * Vérifie que {@code extractFoodForBackpack} ignore silencieusement
     * les entrées null dans le tableau JSON (éléments corrompus).
     *
     * <p><b>Given</b> un tableau JSON contenant une entrée null et un produit valide<br>
     * <b>When</b> on appelle {@code extractFoodForBackpack}<br>
     * <b>Then</b> aucune exception n'est levée et seul le produit valide est dans le Set</p>
     */
    @Test
    public void extractFoodForBackpack_entreeNullDansTableau_ignoree() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        tableau.put(JSONObject.NULL);

        JSONObject food = new JSONObject();
        food.put("id", 3);
        food.put("nom", "Noix");
        tableau.put(food);

        // When
        Set<FoodProduct> set = ServiceFoodProduct.extractFoodForBackpack(tableau);

        // Then
        assertEquals("Seul le produit valide doit être dans le Set", 1, set.size());
    }

    /**
     * Vérifie que {@code extractFoodForBackpack} utilise bien 1 comme valeur par défaut
     * pour {@code nbItem} lorsque le champ est absent du JSON.
     *
     * <p><b>Given</b> un JSON de produit sans champ "nbItem"<br>
     * <b>When</b> on appelle {@code extractFoodForBackpack}<br>
     * <b>Then</b> le produit extrait a un nbItem de 1 (valeur par défaut)</p>
     */
    @Test
    public void extractFoodForBackpack_nbItemAbsent_vautUnParDefaut() throws JSONException {
        // Given
        JSONArray tableau = new JSONArray();
        JSONObject foodSansNbItem = new JSONObject();
        foodSansNbItem.put("id", 20);
        foodSansNbItem.put("nom", "Fruit sec");
        // Pas de champ "nbItem"
        tableau.put(foodSansNbItem);

        // When
        Set<FoodProduct> set = ServiceFoodProduct.extractFoodForBackpack(tableau);

        // Then
        FoodProduct produit = set.iterator().next();
        assertEquals("Le nbItem par défaut doit être 1", 1, produit.getNbItem());
    }

    // =========================================================================
    // TESTS : getAllFoodProducts
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#getAllFoodProducts} appelle bien
     * {@link AppelAPI#get} avec l'URL correcte de l'endpoint {@code /foods}.
     *
     * <p><b>Given</b> un contexte et un token valides<br>
     * <b>When</b> on appelle {@code getAllFoodProducts}<br>
     * <b>Then</b> AppelAPI.get est invoqué avec l'URL {@code BASE_URL + "/foods"}</p>
     */
    @Test
    public void getAllFoodProducts_appelleAppelAPIGet_avecBonneUrl() {
        // Given
        String urlAttendue = BASE_URL + "/foods";

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.get(captureurUrl.capture(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.getAllFoodProducts(mockContexte, TOKEN_VALIDE, mockCallbackArray);

            // Then
            assertEquals("L'URL doit pointer vers l'endpoint /foods",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que {@code getAllFoodProducts} transmet correctement le token
     * à {@link AppelAPI#get}.
     *
     * <p><b>Given</b> un token valide<br>
     * <b>When</b> on appelle {@code getAllFoodProducts}<br>
     * <b>Then</b> le token passé à AppelAPI.get est identique au token fourni</p>
     */
    @Test
    public void getAllFoodProducts_transmettreTokenCorrect() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurToken = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.get(anyString(), captureurToken.capture(),
                            any(Context.class), any(AppelAPI.VolleyCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.getAllFoodProducts(mockContexte, TOKEN_VALIDE, mockCallbackArray);

            // Then
            assertEquals("Le token transmis à AppelAPI.get doit être identique",
                    TOKEN_VALIDE, captureurToken.getValue());
        }
    }

    // =========================================================================
    // TESTS : lierFoodProductARandonnee
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#lierFoodProductARandonnee} appelle
     * {@link AppelAPI#post} avec l'URL correcte contenant le hikeId et le foodId.
     *
     * <p><b>Given</b> un hikeId et un foodId valides<br>
     * <b>When</b> on appelle {@code lierFoodProductARandonnee}<br>
     * <b>Then</b> AppelAPI.post est invoqué avec l'URL {@code /hikes/{hikeId}/food/{foodId}}</p>
     */
    @Test
    public void lierFoodProductARandonnee_appellePost_avecBonneUrl() {
        // Given
        String urlAttendue = BASE_URL + "/hikes/" + HIKE_ID + "/food/" + FOOD_ID;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.lierFoodProductARandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, FOOD_ID, mockCallbackObject);

            // Then
            assertEquals("L'URL doit contenir le hikeId et le foodId", urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que {@code lierFoodProductARandonnee} envoie un corps JSON non null
     * (corps vide {@code {}} requis par l'API).
     *
     * <p><b>Given</b> des paramètres valides<br>
     * <b>When</b> on appelle {@code lierFoodProductARandonnee}<br>
     * <b>Then</b> le body passé à AppelAPI.post est un JSONObject non null (peut être vide)</p>
     */
    @Test
    public void lierFoodProductARandonnee_corpsRequete_estJsonObjectNonNull() {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.lierFoodProductARandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, FOOD_ID, mockCallbackObject);

            // Then
            assertNotNull("Le corps de la requête ne doit pas être null", captureurBody.getValue());
        }
    }

    // =========================================================================
    // TESTS : retirerFoodProductDeRandonnee
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#retirerFoodProductDeRandonnee} appelle
     * {@link AppelAPI#delete} avec l'URL correcte.
     *
     * <p><b>Given</b> un hikeId et un foodId valides<br>
     * <b>When</b> on appelle {@code retirerFoodProductDeRandonnee}<br>
     * <b>Then</b> AppelAPI.delete est invoqué avec l'URL {@code /hikes/{hikeId}/food/{foodId}}</p>
     */
    @Test
    public void retirerFoodProductDeRandonnee_appelleDelete_avecBonneUrl() {
        // Given
        String urlAttendue = BASE_URL + "/hikes/" + HIKE_ID + "/food/" + FOOD_ID;

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.delete(captureurUrl.capture(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.retirerFoodProductDeRandonnee(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, FOOD_ID, mockCallbackObject);

            // Then
            assertEquals("L'URL de suppression doit correspondre à /hikes/{id}/food/{id}",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    // =========================================================================
    // TESTS : creerNouveauProduit
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#creerNouveauProduit} appelle
     * {@link AppelAPI#post} avec l'URL correcte de l'endpoint {@code /foods}.
     *
     * <p><b>Given</b> les caractéristiques complètes d'un nouveau produit<br>
     * <b>When</b> on appelle {@code creerNouveauProduit}<br>
     * <b>Then</b> AppelAPI.post est invoqué avec l'URL {@code BASE_URL + "/foods"}</p>
     */
    @Test
    public void creerNouveauProduit_urlUtilisee_estFoodsUrl() {
        // Given
        String urlAttendue = BASE_URL + "/foods";

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<String> captureurUrl = ArgumentCaptor.forClass(String.class);

            staticMock.when(() -> AppelAPI.post(captureurUrl.capture(), anyString(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.creerNouveauProduit(mockContexte, TOKEN_VALIDE,
                    "Noix", 30.0, "Fruit à coque", "Sachet",
                    180.0, 1.2, 10, mockCallbackObject);

            // Then
            assertEquals("L'URL de création doit pointer vers /foods",
                    urlAttendue, captureurUrl.getValue());
        }
    }

    /**
     * Vérifie que {@code creerNouveauProduit} affecte {@code null} au conditionnement
     * lorsque la chaîne passée est vide (règle métier du service).
     *
     * <p><b>Given</b> un conditionnement vide ""<br>
     * <b>When</b> on appelle {@code creerNouveauProduit}<br>
     * <b>Then</b> le corps JSON contient {@code "conditionnement": null}</p>
     */
    @Test
    public void creerNouveauProduit_conditionnementVide_envoyeNull() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When — conditionnement vide
            ServiceFoodProduct.creerNouveauProduit(mockContexte, TOKEN_VALIDE,
                    "Noix", 30.0, "Fruit à coque", "",
                    180.0, 1.2, 10, mockCallbackObject);

            // Then — le conditionnement doit être null dans le body
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps ne doit pas être null", body);
            assertTrue("Le corps doit contenir le champ 'conditionnement'",
                    body.has("conditionnement"));
            assertTrue("Le conditionnement doit être JSON null quand vide",
                    body.isNull("conditionnement"));
        }
    }

    /**
     * Vérifie que {@code creerNouveauProduit} conserve le conditionnement non vide
     * tel quel dans le corps JSON.
     *
     * <p><b>Given</b> un conditionnement non vide "Boite"<br>
     * <b>When</b> on appelle {@code creerNouveauProduit}<br>
     * <b>Then</b> le corps JSON contient {@code "conditionnement": "Boite"}</p>
     */
    @Test
    public void creerNouveauProduit_conditionnementNonVide_conserveValeur() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.creerNouveauProduit(mockContexte, TOKEN_VALIDE,
                    "Yaourt", 125.0, "Laitage", "Pot",
                    60.0, 0.9, 4, mockCallbackObject);

            // Then
            JSONObject body = captureurBody.getValue();
            assertFalse("Le conditionnement 'Pot' ne doit pas être null",
                    body.isNull("conditionnement"));
            assertEquals("Le conditionnement doit être 'Pot'", "Pot", body.getString("conditionnement"));
        }
    }

    /**
     * Vérifie que {@code creerNouveauProduit} construit un corps JSON contenant
     * toutes les propriétés du produit avec les bonnes valeurs.
     *
     * <p><b>Given</b> les caractéristiques complètes d'un nouveau produit<br>
     * <b>When</b> on appelle {@code creerNouveauProduit}<br>
     * <b>Then</b> le body JSON contient tous les champs attendus avec les bonnes valeurs</p>
     */
    @Test
    public void creerNouveauProduit_corpsRequete_contientTousLesChamps() throws JSONException {
        // Given
        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            ArgumentCaptor<JSONObject> captureurBody = ArgumentCaptor.forClass(JSONObject.class);

            staticMock.when(() -> AppelAPI.post(anyString(), anyString(), captureurBody.capture(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.creerNouveauProduit(mockContexte, TOKEN_VALIDE,
                    "Fromage", 200.0, "Produit laitier", "Emballage",
                    300.0, 2.5, 1, mockCallbackObject);

            // Then
            JSONObject body = captureurBody.getValue();
            assertNotNull("Le corps ne doit pas être null", body);
            assertEquals("Le nom doit être correct", "Fromage", body.getString("nom"));
            assertEquals("La masse doit être correcte", 200.0, body.getDouble("masseGrammes"), 0.001);
            assertEquals("L'appellation doit être correcte", "Produit laitier", body.getString("appellationCourante"));
            assertEquals("Les kcal doivent être correctes", 300.0, body.getDouble("apportNutritionnelKcal"), 0.001);
            assertEquals("Le prix doit être correct", 2.5, body.getDouble("prixEuro"), 0.001);
            assertEquals("Le nbItem doit être correct", 1, body.getInt("nbItem"));
        }
    }

    // =========================================================================
    // TESTS : synchroniserFoodProducts
    // =========================================================================

    /**
     * Vérifie que {@link ServiceFoodProduct#synchroniserFoodProducts} n'effectue aucun appel API
     * lorsque les listes originale et temporaire sont identiques.
     *
     * <p><b>Given</b> une liste originale et temporaire contenant le même produit<br>
     * <b>When</b> on appelle {@code synchroniserFoodProducts}<br>
     * <b>Then</b> aucun appel à AppelAPI.post ou AppelAPI.delete n'est effectué</p>
     */
    @Test
    public void synchroniserFoodProducts_listesIdentiques_aucunAppelAPI() {
        // Given
        List<FoodProduct> originaux = new ArrayList<>();
        originaux.add(buildFoodProduct(1));

        List<FoodProduct> temporaires = new ArrayList<>();
        temporaires.add(buildFoodProduct(1));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServiceFoodProduct.synchroniserFoodProducts(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, originaux, temporaires);

            // Then
            staticMock.verify(() -> AppelAPI.post(any(), any(), any(), any(), any()), never());
            staticMock.verify(() -> AppelAPI.delete(any(), any(), any(), any()), never());
        }
    }

    /**
     * Vérifie que {@code synchroniserFoodProducts} effectue un appel POST
     * pour un produit présent dans la liste temporaire mais absent de la liste originale.
     *
     * <p><b>Given</b> une liste originale vide et une liste temporaire avec 1 produit<br>
     * <b>When</b> on appelle {@code synchroniserFoodProducts}<br>
     * <b>Then</b> un appel POST est effectué pour lier le nouveau produit</p>
     */
    @Test
    public void synchroniserFoodProducts_unAjout_unAppelPost() {
        // Given
        List<FoodProduct> originaux = new ArrayList<>();
        List<FoodProduct> temporaires = new ArrayList<>();
        temporaires.add(buildFoodProduct(10));

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.post(anyString(), anyString(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.synchroniserFoodProducts(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, originaux, temporaires);

            // Then
            staticMock.verify(() -> AppelAPI.post(anyString(), anyString(),
                    any(JSONObject.class), any(Context.class),
                    any(AppelAPI.VolleyObjectCallback.class)), times(1));
        }
    }

    /**
     * Vérifie que {@code synchroniserFoodProducts} effectue un appel DELETE
     * pour un produit présent dans la liste originale mais absent de la liste temporaire.
     *
     * <p><b>Given</b> une liste originale avec 1 produit et une liste temporaire vide<br>
     * <b>When</b> on appelle {@code synchroniserFoodProducts}<br>
     * <b>Then</b> un appel DELETE est effectué pour retirer le produit supprimé</p>
     */
    @Test
    public void synchroniserFoodProducts_uneSuppression_unAppelDelete() {
        // Given
        List<FoodProduct> originaux = new ArrayList<>();
        originaux.add(buildFoodProduct(20));
        List<FoodProduct> temporaires = new ArrayList<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.delete(anyString(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.synchroniserFoodProducts(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, originaux, temporaires);

            // Then
            staticMock.verify(() -> AppelAPI.delete(anyString(), anyString(),
                    any(Context.class), any(AppelAPI.VolleyObjectCallback.class)), times(1));
        }
    }

    /**
     * Vérifie que {@code synchroniserFoodProducts} effectue simultanément un POST
     * et un DELETE lorsqu'un produit est ajouté et un autre est supprimé.
     *
     * <p><b>Given</b> une liste originale avec le produit A et une temporaire avec le produit B<br>
     * <b>When</b> on appelle {@code synchroniserFoodProducts}<br>
     * <b>Then</b> 1 appel POST (ajout B) et 1 appel DELETE (suppression A) sont effectués</p>
     */
    @Test
    public void synchroniserFoodProducts_unAjoutUneSuppression_unPostUnDelete() {
        // Given
        List<FoodProduct> originaux = new ArrayList<>();
        originaux.add(buildFoodProduct(1)); // Produit A (sera supprimé)

        List<FoodProduct> temporaires = new ArrayList<>();
        temporaires.add(buildFoodProduct(2)); // Produit B (sera ajouté)

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            staticMock.when(() -> AppelAPI.post(anyString(), anyString(),
                            any(JSONObject.class), any(Context.class),
                            any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);
            staticMock.when(() -> AppelAPI.delete(anyString(), anyString(),
                            any(Context.class), any(AppelAPI.VolleyObjectCallback.class)))
                    .thenAnswer(inv -> null);

            // When
            ServiceFoodProduct.synchroniserFoodProducts(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, originaux, temporaires);

            // Then
            staticMock.verify(() -> AppelAPI.post(anyString(), anyString(),
                    any(JSONObject.class), any(Context.class),
                    any(AppelAPI.VolleyObjectCallback.class)), times(1));
            staticMock.verify(() -> AppelAPI.delete(anyString(), anyString(),
                    any(Context.class), any(AppelAPI.VolleyObjectCallback.class)), times(1));
        }
    }

    /**
     * Vérifie que {@code synchroniserFoodProducts} n'effectue aucun appel API
     * lorsque les deux listes sont vides.
     *
     * <p><b>Given</b> deux listes vides<br>
     * <b>When</b> on appelle {@code synchroniserFoodProducts}<br>
     * <b>Then</b> aucun appel réseau n'est effectué</p>
     */
    @Test
    public void synchroniserFoodProducts_deuxListesVides_aucunAppelAPI() {
        // Given
        List<FoodProduct> originaux = new ArrayList<>();
        List<FoodProduct> temporaires = new ArrayList<>();

        try (MockedStatic<AppelAPI> staticMock = Mockito.mockStatic(AppelAPI.class)) {
            // When
            ServiceFoodProduct.synchroniserFoodProducts(mockContexte, TOKEN_VALIDE,
                    HIKE_ID, originaux, temporaires);

            // Then
            staticMock.verify(() -> AppelAPI.post(any(), any(), any(), any(), any()), never());
            staticMock.verify(() -> AppelAPI.delete(any(), any(), any(), any()), never());
        }
    }
}