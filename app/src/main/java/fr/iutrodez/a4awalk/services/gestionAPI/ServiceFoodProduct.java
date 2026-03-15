package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

public class ServiceFoodProduct {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    public static void getAllFoodProducts(Context context, String token, AppelAPI.VolleyCallback callback) {
        String url = BASE_URL + "/foods";
        AppelAPI.get(url, token, context, callback);
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
}