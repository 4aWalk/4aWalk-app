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

    /**
     * Synchronise les produits alimentaires d'une randonnée.
     * Compare la liste originale avec la liste temporaire et effectue les appels API nécessaires.
     */
    public static void synchroniserFoodProducts(Context context, String token, int hikeId,
                                                List<FoodProduct> originaux, List<FoodProduct> temporaires) {

        // 1. Trouver les AJOUTS (présents dans temporaires, absents dans originaux)
        for (FoodProduct temp : temporaires) {
            boolean existeDeja = false;
            for (FoodProduct orig : originaux) {
                if (temp.getId() == orig.getId()) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) {
                // Le produit a été ajouté, on fait le POST
                lierFoodProductARandonnee(context, token, hikeId, temp.getId(), new AppelAPI.VolleyObjectCallback() {
                    @Override public void onSuccess(JSONObject result) {
                        Log.i("ServiceFoodProduct", "Produit " + temp.getId() + " lié avec succès.");
                    }
                    @Override public void onError(VolleyError error) {
                        Log.e("ServiceFoodProduct", "Erreur lors de la liaison du produit " + temp.getId());
                    }
                });
            }
        }

        // 2. Trouver les SUPPRESSIONS (présents dans originaux, absents dans temporaires)
        for (FoodProduct orig : originaux) {
            boolean estConserve = false;
            for (FoodProduct temp : temporaires) {
                if (orig.getId() == temp.getId()) {
                    estConserve = true;
                    break;
                }
            }
            if (!estConserve) {
                // Le produit a été retiré, on fait le DELETE
                retirerFoodProductDeRandonnee(context, token, hikeId, orig.getId(), new AppelAPI.VolleyObjectCallback() {
                    @Override public void onSuccess(JSONObject result) {
                        Log.i("ServiceFoodProduct", "Produit " + orig.getId() + " retiré avec succès.");
                    }
                    @Override public void onError(VolleyError error) {
                        // Gestion du cas où le DELETE renvoie un 204 No Content
                        if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                            Log.i("ServiceFoodProduct", "Produit " + orig.getId() + " retiré avec succès (204).");
                        } else {
                            Log.e("ServiceFoodProduct", "Erreur lors du retrait du produit " + orig.getId());
                        }
                    }
                });
            }
        }
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
}