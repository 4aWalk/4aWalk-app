package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

/**
 * Service gérant les opérations CRUD sur les produits alimentaires.
 * <p>
 * Fournit des méthodes pour récupérer le catalogue, créer un produit,
 * lier ou retirer un produit d'une randonnée et synchroniser deux listes
 * de produits via des appels API séquentiels.
 */
public class ServiceFoodProduct {

    /** URL de base de l'API. */
    private static final String BASE_URL = "http://98.94.8.220:8080";

    /**
     * Récupère l'intégralité du catalogue de produits alimentaires depuis l'API.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param callback Callback Volley pour traiter la réponse brute.
     */
    public static void getAllFoodProducts(Context context, String token,
                                          AppelAPI.VolleyCallback callback) {
        String url = BASE_URL + "/foods";
        AppelAPI.get(url, token, context, callback);
    }

    /**
     * Lie un produit alimentaire existant à une randonnée via un appel POST.
     * Un corps JSON vide est envoyé conformément au contrat de l'API.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param hikeId   Identifiant de la randonnée cible.
     * @param foodId   Identifiant du produit alimentaire à lier.
     * @param callback Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void lierFoodProductARandonnee(Context context, String token, int hikeId,
                                                 int foodId,
                                                 AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/food/" + foodId;
        // Corps JSON vide requis par le contrat de l'API
        AppelAPI.post(url, token, new JSONObject(), context, callback);
    }

    /**
     * Retire un produit alimentaire d'une randonnée via un appel DELETE.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param hikeId   Identifiant de la randonnée cible.
     * @param foodId   Identifiant du produit alimentaire à retirer.
     * @param callback Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void retirerFoodProductDeRandonnee(Context context, String token, int hikeId,
                                                     int foodId,
                                                     AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/food/" + foodId;
        AppelAPI.delete(url, token, context, callback);
    }

    /**
     * Méthode de haut niveau pour créer un nouveau produit alimentaire.
     * <p>
     * Instancie un {@link FoodProduct}, renseigne ses champs puis délègue
     * l'appel API à {@link #creerFoodProduct(Context, String, FoodProduct, AppelAPI.VolleyObjectCallback)}.
     *
     * @param context        Contexte Android.
     * @param token          Token JWT d'authentification.
     * @param nom            Nom du produit.
     * @param masse          Masse en grammes.
     * @param appellation    Appellation courante (ex. : "barre de céréales").
     * @param conditionnement Conditionnement du produit (peut être vide).
     * @param kcal           Apport nutritionnel en kilocalories.
     * @param prix           Prix en euros.
     * @param nbItem         Nombre d'unités.
     * @param callback       Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void creerNouveauProduit(Context context, String token, String nom, double masse,
                                           String appellation, String conditionnement, double kcal,
                                           double prix, int nbItem,
                                           AppelAPI.VolleyObjectCallback callback) {

        FoodProduct nouveauProduit = new FoodProduct();
        nouveauProduit.setNom(nom);
        nouveauProduit.setMasseGrammes(masse);
        nouveauProduit.setAppellationCourante(appellation);
        // Un conditionnement vide est converti en null pour être cohérent avec l'API
        nouveauProduit.setConditionnement(conditionnement.isEmpty() ? null : conditionnement);
        nouveauProduit.setApportNutritionnelKcal(kcal);
        nouveauProduit.setPrixEuro(prix);
        nouveauProduit.setNbItem(nbItem);

        creerFoodProduct(context, token, nouveauProduit, callback);
    }

    /**
     * Extrait la liste des produits alimentaires depuis la réponse globale de l'API.
     * <p>
     * Attend un tableau JSON sous la clé {@code foodCatalogue}.
     *
     * @param response L'objet JSON contenant le catalogue alimentaire.
     * @return Liste des {@link FoodProduct} extraits, vide si aucun trouvé.
     */
    public static List<FoodProduct> extractFoodCatalogue(JSONObject response) {
        List<FoodProduct> foodList = new ArrayList<>();
        JSONArray foodJson = response.optJSONArray("foodCatalogue");

        if (foodJson != null) {
            try {
                for (int i = 0; i < foodJson.length(); i++) {
                    foodList.add(constructFPFromJson(foodJson.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e("ServiceFoodProduct", "Erreur parsing catalogue alimentaire");
            }
        }
        return foodList;
    }

    /**
     * Envoie un appel POST à l'API pour créer le produit alimentaire fourni.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param fp       Objet {@link FoodProduct} dont les données sont sérialisées en JSON.
     * @param callback Callback Volley pour traiter la réponse ou l'erreur.
     */
    public static void creerFoodProduct(Context context, String token, FoodProduct fp,
                                        AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/foods";
        try {
            // Sérialisation manuelle du FoodProduct en corps JSON
            JSONObject body = new JSONObject();
            body.put("nom", fp.getNom());
            body.put("description", fp.getDescription());
            body.put("masseGrammes", fp.getMasseGrammes());
            body.put("appellationCourante", fp.getAppellationCourante());
            body.put("conditionnement", fp.getConditionnement());
            body.put("apportNutritionnelKcal", fp.getApportNutritionnelKcal());
            body.put("prixEuro", fp.getPrixEuro());
            body.put("nbItem", fp.getNbItem());

            AppelAPI.post(url, token, body, context, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construit un objet {@link FoodProduct} depuis un objet JSON.
     * <p>
     * Note : l'API retourne la clé {@code "appelationCourante"} (un seul 'l'),
     * contrairement au setter Java {@code setAppellationCourante} (deux 'l').
     *
     * @param obj L'objet JSON représentant un produit alimentaire.
     * @return Un {@link FoodProduct} renseigné.
     * @throws JSONException En cas de clé manquante ou de type incompatible.
     */
    public static FoodProduct constructFPFromJson(JSONObject obj) throws JSONException {
        FoodProduct fp = new FoodProduct();
        fp.setId(obj.getInt("id"));
        fp.setNom(obj.getString("nom"));
        fp.setMasseGrammes(obj.getDouble("masseGrammes"));
        // Attention : l'API retourne "appelationCourante" avec un seul 'l'
        fp.setAppellationCourante(obj.getString("appelationCourante"));
        fp.setConditionnement(obj.getString("conditionnement"));
        fp.setApportNutritionnelKcal(obj.getDouble("apportNutritionnelKcal"));
        fp.setPrixEuro(obj.getDouble("prixEuro"));
        fp.setNbItem(obj.getInt("nbItem"));
        return fp;
    }

    /**
     * Extrait les produits alimentaires du sac à dos d'un participant depuis un tableau JSON.
     * <p>
     * Retourne un {@link java.util.Set} pour éviter les doublons.
     *
     * @param nourritureJson Tableau JSON contenant les produits du participant.
     * @return Ensemble des {@link FoodProduct} extraits, vide si le tableau est null.
     */
    public static java.util.Set<FoodProduct> extractFoodForBackpack(JSONArray nourritureJson) {
        java.util.Set<FoodProduct> nourritures = new java.util.HashSet<>();
        if (nourritureJson != null) {
            for (int k = 0; k < nourritureJson.length(); k++) {
                JSONObject foodJson = nourritureJson.optJSONObject(k);
                if (foodJson != null) {
                    FoodProduct food = new FoodProduct();
                    food.setId(foodJson.optInt("id"));
                    food.setNom(foodJson.optString("nom"));
                    food.setMasseGrammes(foodJson.optDouble("masseGrammes", 0.0));
                    food.setNbItem(foodJson.optInt("nbItem", 1));
                    nourritures.add(food);
                }
            }
        }
        return nourritures;
    }

    /**
     * Synchronise les produits alimentaires d'une randonnée avec l'API.
     * <p>
     * Compare {@code originaux} et {@code temporaires} pour déterminer
     * les produits à ajouter (présents dans temporaires mais pas dans originaux)
     * et ceux à supprimer (présents dans originaux mais pas dans temporaires).
     * Les ajouts sont effectués en premier, séquentiellement, puis les suppressions.
     *
     * @param context     Contexte Android.
     * @param token       Token JWT d'authentification.
     * @param hikeId      Identifiant de la randonnée concernée.
     * @param originaux   État d'origine des produits liés à la randonnée.
     * @param temporaires Nouvel état souhaité des produits.
     */
    public static void synchroniserFoodProducts(Context context, String token, int hikeId,
                                                List<FoodProduct> originaux,
                                                List<FoodProduct> temporaires) {

        // 1. Détermination des produits à AJOUTER (nouveaux dans temporaires)
        List<FoodProduct> aAjouter = new ArrayList<>();
        for (FoodProduct temp : temporaires) {
            boolean existeDeja = false;
            for (FoodProduct orig : originaux) {
                if (temp.getId() == orig.getId()) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) aAjouter.add(temp);
        }

        // 2. Détermination des produits à SUPPRIMER (absents de temporaires)
        List<FoodProduct> aSupprimer = new ArrayList<>();
        for (FoodProduct orig : originaux) {
            boolean estConserve = false;
            for (FoodProduct temp : temporaires) {
                if (orig.getId() == temp.getId()) {
                    estConserve = true;
                    break;
                }
            }
            if (!estConserve) aSupprimer.add(orig);
        }

        // 3. Exécution séquentielle : ajouts d'abord, suppressions ensuite
        ajouterSequentiellement(context, token, hikeId, aAjouter, 0, () ->
                supprimerSequentiellement(context, token, hikeId, aSupprimer, 0)
        );
    }

    /**
     * Lie les produits de {@code liste} à la randonnée un par un, de manière
     * séquentielle (chaque appel API attend la réponse du précédent).
     * <p>
     * En cas d'erreur sur un produit, la liaison continue avec le suivant
     * pour ne pas bloquer toute la file.
     *
     * @param context   Contexte Android.
     * @param token     Token JWT d'authentification.
     * @param hikeId    Identifiant de la randonnée cible.
     * @param liste     Liste des produits à lier.
     * @param index     Indice courant dans la liste (utiliser 0 pour démarrer).
     * @param onTermine Runnable exécuté une fois tous les ajouts traités.
     */
    private static void ajouterSequentiellement(Context context, String token, int hikeId,
                                                List<FoodProduct> liste, int index,
                                                Runnable onTermine) {
        // Condition d'arrêt : tous les produits ont été traités
        if (index >= liste.size()) {
            if (onTermine != null) onTermine.run();
            return;
        }

        FoodProduct fp = liste.get(index);
        lierFoodProductARandonnee(context, token, hikeId, fp.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceFoodProduct", "Produit " + fp.getId() + " lié avec succès.");
                        // Passage au produit suivant après succès
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("ServiceFoodProduct", "Erreur lors de la liaison du produit " + fp.getId());
                        // On continue malgré l'erreur pour ne pas bloquer les suivants
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }
                }
        );
    }

    /**
     * Retire les produits de {@code liste} de la randonnée un par un, de manière
     * séquentielle (chaque appel API attend la réponse du précédent).
     * <p>
     * Un code HTTP 204 est traité comme un succès (suppression sans contenu retourné).
     * En cas d'autre erreur, la suppression continue avec le produit suivant.
     *
     * @param context  Contexte Android.
     * @param token    Token JWT d'authentification.
     * @param hikeId   Identifiant de la randonnée cible.
     * @param liste    Liste des produits à retirer.
     * @param index    Indice courant dans la liste (utiliser 0 pour démarrer).
     */
    private static void supprimerSequentiellement(Context context, String token, int hikeId,
                                                  List<FoodProduct> liste, int index) {
        // Condition d'arrêt : tous les produits ont été traités
        if (index >= liste.size()) return;

        FoodProduct fp = liste.get(index);
        retirerFoodProductDeRandonnee(context, token, hikeId, fp.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceFoodProduct", "Produit " + fp.getId() + " retiré avec succès.");
                        // Passage au produit suivant après succès
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Le code 204 signifie que la suppression s'est bien effectuée
                        if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                            Log.i("ServiceFoodProduct", "Produit " + fp.getId() + " retiré avec succès (204).");
                        } else {
                            Log.e("ServiceFoodProduct", "Erreur lors du retrait du produit " + fp.getId());
                        }
                        // On continue avec le produit suivant dans tous les cas
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }
                }
        );
    }
}