package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;

public class ServiceFoodProduct {

    private static final String BASE_URL = "http://98.94.8.220:8080"; // Ton URL de base

    public static void getAllFoodProducts(Context context, String token, AppelAPI.VolleyCallback callback) {
        String url = BASE_URL + "/foods";
        AppelAPI.get(url, token, context, callback);
    }

    public static void creerFoodProduct(Context context, String token, FoodProduct fp, AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/foods";

        try {
            JSONObject body = new JSONObject();
            body.put("nom", fp.getNom());
            body.put("description", fp.getDescription()); // Attention, ton modèle a description mais l'UI a "appellationCourante". J'ai laissé tel quel par rapport à ton code.
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
}