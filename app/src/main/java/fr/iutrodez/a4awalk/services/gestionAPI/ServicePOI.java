package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServicePOI {

    private static final String URL_CREATION_POI = "http://IP:Port/hikes/%d/poi";

    public static void ajoutPOI(Context context, String token, PointOfInterest poi, Long idRandonnee) {

        JSONObject jsonBody = createPOIJson(poi);

        String url = String.format(URL_CREATION_POI, idRandonnee);
        if (jsonBody == null) {
            Log.e("ServicePOI", "Échec de la création du JSON pour le POI : " + poi.getName());
            return;
        }

        // 3. Appel via la classe utilitaire AppelAPI
        AppelAPI.post(URL_CREATION_POI, token, jsonBody, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                // Succès
                Log.i("API_POI", "POI ajouté avec succès : " + poi.getName());
            }

            @Override
            public void onError(VolleyError error) {
                // Erreur
                String message = "Erreur inconnue";
                if (error.networkResponse != null) {
                    message = "Code " + error.networkResponse.statusCode;
                }
                Log.e("API_POI", "Erreur ajout POI : " + message);
                Toast.makeText(context, "Erreur ajout POI : " + poi.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Méthode isolée pour transformer un objet PointOfInterest en JSONObject.
     * @return le JSONObject ou null en cas d'erreur de parsing.
     */
    private static JSONObject createPOIJson(PointOfInterest poi) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", poi.getName());
            json.put("latitude", poi.getLatitude());
            json.put("longitude", poi.getLongitude());
            json.put("description", null);

            // On peut ajouter d'autres champs ici si nécessaire (ex: description)
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}