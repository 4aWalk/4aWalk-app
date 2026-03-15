package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;

public class ServiceEquipment {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    // Récupérer tous les équipements (GET)
    public static void getAllEquipments(Context context, String token, AppelAPI.VolleyCallback callback) {
        String url = BASE_URL + "/equipments";
        AppelAPI.get(url, token, context, callback);
    }

    // Créer un équipement (POST)
    public static void creerEquipment(Context context, String token, EquipmentItem eq, AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/equipments";

        try {
            JSONObject body = new JSONObject();
            body.put("nom", eq.getNom());
            body.put("description", eq.getDescription());
            body.put("masseGrammes", eq.getMasseGrammes());
            body.put("nbItem", eq.getNbItem());
            body.put("type", eq.getType().name());
            body.put("masseAVide", eq.getMasseAVide());

            AppelAPI.post(url, token, body, context, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}