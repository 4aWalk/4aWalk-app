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

    public static void modifierRandonneeAPI(Context context, String token, int hikeId, String libelle, int dureeJours,
                                            double latitudeD, double longitudeD, double latitudeA, double longitudeA,
                                            UpdateHikeCallback callback) {
        String nomDepart = "Départ";
        String descDepart = "Point de départ de la randonnée";
        String nomArrivee = "Arrivée";
        String descArrivee = "Point d'arrivée de la randonnée";

        JSONObject bodyHike = ServiceCreationRandonnee.construireJsonRandonnee(libelle, dureeJours,
                nomDepart, descDepart, latitudeD, longitudeD,
                nomArrivee, descArrivee, latitudeA, longitudeA);

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
    }
}