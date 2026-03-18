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

public class ServiceFoodProduct {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    public static void getAllFoodProducts(Context context, String token, AppelAPI.VolleyCallback callback) {
        String url = BASE_URL + "/foods";
        AppelAPI.get(url, token, context, callback);
    }

    /**
     * Lie un FoodProduct existant à une Randonnée.
     */
    public static void lierFoodProductARandonnee(Context context, String token, int hikeId, int foodId, AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/food/" + foodId;
        // On envoie un corps JSON vide comme demandé par l'API
        AppelAPI.post(url, token, new JSONObject(), context, callback);
    }

    /**
     * Retire un FoodProduct d'une Randonnée.
     */
    public static void retirerFoodProductDeRandonnee(Context context, String token, int hikeId, int foodId, AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/food/" + foodId;
        // Utilisez votre méthode d'appel DELETE personnalisée
        AppelAPI.delete(url, token, context, callback);
    }

    /**
     * Méthode de haut niveau : instancie le FoodProduct et gère l'appel API.
     */
    public static void creerNouveauProduit(Context context, String token, String nom, double masse,
                                           String appellation, String conditionnement, double kcal,
                                           double prix, int nbItem, AppelAPI.VolleyObjectCallback callback) {

        FoodProduct nouveauProduit = new FoodProduct();
        nouveauProduit.setNom(nom);
        nouveauProduit.setMasseGrammes(masse);
        nouveauProduit.setAppellationCourante(appellation);
        nouveauProduit.setConditionnement(conditionnement.isEmpty() ? null : conditionnement);
        nouveauProduit.setApportNutritionnelKcal(kcal);
        nouveauProduit.setPrixEuro(prix);
        nouveauProduit.setNbItem(nbItem);

        creerFoodProduct(context, token, nouveauProduit, callback);
    }

    /**
     * Extrait le catalogue de FoodProducts depuis la réponse globale.
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

    // Méthode existante conservée pour l'appel brut à l'API
    public static void creerFoodProduct(Context context, String token, FoodProduct fp, AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/foods";
        try {
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

    public static FoodProduct constructFPFromJson(JSONObject obj) throws JSONException {
        FoodProduct fp = new FoodProduct();
        fp.setId(obj.getInt("id"));
        fp.setNom(obj.getString("nom"));
        fp.setMasseGrammes(obj.getDouble("masseGrammes"));
        fp.setAppellationCourante(obj.getString("appelationCourante"));
        fp.setConditionnement(obj.getString("conditionnement"));
        fp.setApportNutritionnelKcal(obj.getDouble("apportNutritionnelKcal"));
        fp.setPrixEuro(obj.getDouble("prixEuro"));
        fp.setNbItem(obj.getInt("nbItem"));
        return fp;
    }

    /**
     * Extrait la liste de la nourriture contenue dans le sac à dos d'un participant.
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
     * Compare la liste originale avec la liste temporaire et effectue les appels API (POST/DELETE) nécessaires SÉQUENTIELLEMENT.
     */
    public static void synchroniserFoodProducts(Context context, String token, int hikeId,
                                                List<FoodProduct> originaux, List<FoodProduct> temporaires) {

        // 1. Construire la liste des produits à AJOUTER
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

        // 2. Construire la liste des produits à SUPPRIMER
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

        // 3. Lancer les ajouts séquentiellement, puis les suppressions séquentiellement
        ajouterSequentiellement(context, token, hikeId, aAjouter, 0, () ->
                supprimerSequentiellement(context, token, hikeId, aSupprimer, 0)
        );
    }

    private static void ajouterSequentiellement(Context context, String token, int hikeId,
                                                List<FoodProduct> liste, int index,
                                                Runnable onTermine) {
        // Condition d'arrêt : on a parcouru toute la liste
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
                        // On lance le produit suivant
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("ServiceFoodProduct", "Erreur lors de la liaison du produit " + fp.getId());
                        // On lance quand même le suivant pour ne pas bloquer toute la file
                        ajouterSequentiellement(context, token, hikeId, liste, index + 1, onTermine);
                    }
                }
        );
    }

    private static void supprimerSequentiellement(Context context, String token, int hikeId,
                                                  List<FoodProduct> liste, int index) {
        // Condition d'arrêt : on a parcouru toute la liste
        if (index >= liste.size()) return;

        FoodProduct fp = liste.get(index);
        retirerFoodProductDeRandonnee(context, token, hikeId, fp.getId(),
                new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceFoodProduct", "Produit " + fp.getId() + " retiré avec succès.");
                        // On lance la suppression suivante
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Gestion spécifique du code 204 No Content
                        if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                            Log.i("ServiceFoodProduct", "Produit " + fp.getId() + " retiré avec succès (204).");
                        } else {
                            Log.e("ServiceFoodProduct", "Erreur lors du retrait du produit " + fp.getId());
                        }
                        // On continue avec le suivant
                        supprimerSequentiellement(context, token, hikeId, liste, index + 1);
                    }
                }
        );
    }
}