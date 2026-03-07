package fr.iutrodez.a4awalk.services.gestionAPI.randonnee;

import android.content.Context;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceModificationRandonnee {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    public interface UpdateHikeCallback {
        void onSuccess();
        void onError(String message);
    }

    public static void modifierRandonneeAPI(Context context, String token, int hikeId, String libelle, int dureeJours, UpdateHikeCallback callback) {
        try {
            JSONObject bodyHike = new JSONObject();
            bodyHike.put("libelle", libelle);
            bodyHike.put("dureeJours", dureeJours);

            // IDs techniques requis par l'API pour la structure initiale
            JSONObject departObj = new JSONObject();
            departObj.put("id", 1);
            bodyHike.put("depart", departObj);

            JSONObject arriveeObj = new JSONObject();
            arriveeObj.put("id", 2);
            bodyHike.put("arrivee", arriveeObj);

            String urlUpdateHike = BASE_URL + "/hikes/" + hikeId;

            AppelAPI.put(urlUpdateHike, token, bodyHike, context, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    if (callback != null) callback.onSuccess();
                }

                @Override
                public void onError(VolleyError error) {
                    if (callback != null) callback.onError(error.toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            if (callback != null) callback.onError("Erreur de formatage JSON");
        }
    }
}