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

        // Appel interne à la fonction de création du JSON
        JSONObject bodyHike = construireJsonRandonnee(libelle, dureeJours,
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

    // ================================================================
    // ================= CONVERSION JSON ET OBJETS ====================
    // ================================================================

    private static JSONObject construireJsonRandonnee(String libelle, int dureeJours,
                                                      String nomDepart, String descDepart, double latitudeD, double longitudeD,
                                                      String nomArrivee, String descArrivee, double latitudeA, double longitudeA) {
        JSONObject bodyHike = new JSONObject();
        try {
            bodyHike.put("libelle", libelle);
            bodyHike.put("dureeJours", dureeJours);

            JSONObject depart = new JSONObject();
            depart.put("nom", nomDepart);
            depart.put("description", descDepart);
            depart.put("latitude", latitudeD);
            depart.put("longitude", longitudeD);
            bodyHike.put("depart", depart);

            JSONObject arrivee = new JSONObject();
            arrivee.put("nom", nomArrivee);
            arrivee.put("description", descArrivee);
            arrivee.put("latitude", latitudeA);
            arrivee.put("longitude", longitudeA);
            bodyHike.put("arrivee", arrivee);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyHike;
    }
}