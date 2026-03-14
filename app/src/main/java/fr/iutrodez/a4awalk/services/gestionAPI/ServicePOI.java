package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServicePOI {

    private static final String BASE_URL = "http://98.94.8.220:8080";
    private static final String URL_CREATION_POI = BASE_URL + "/hikes/%d/poi";

    public static void ajoutPOI(Context context, String token, PointOfInterest poi, Long idRandonnee) {
        JSONObject jsonBody = createPOIJson(poi);
        String url = String.format(URL_CREATION_POI, idRandonnee);

        if (jsonBody == null) return;

        AppelAPI.post(url, token, jsonBody, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.i("API_POI", "POI ajouté avec succès");
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("API_POI", "Erreur ajout POI");
            }
        });
    }

    public static void traiterMAJPOI(Context context, int hikeId, ArrayList<PointOfInterest> listeTemporairePOI, String token) {
        JSONArray pois = new JSONArray();

        for (PointOfInterest poi : listeTemporairePOI) {
            JSONObject body = createPOIJson(poi);
            if (body != null) {
                pois.put(body);
            }
        }

        if (pois.length() > 0) {
            String url = BASE_URL + "/hikes/" + hikeId + "/pois"; // ou /poi selon ton backend
            AppelAPI.putA(url, token, pois, context, new AppelAPI.VolleyCallback() {
                @Override
                public void onSuccess(JSONArray result) {}
                @Override
                public void onError(VolleyError error) {}
            });
        }
    }

    private static JSONObject createPOIJson(PointOfInterest poi) {
        try {
            JSONObject json = new JSONObject();
            json.put("nom", poi.getNom());
            json.put("latitude", poi.getLatitude());
            json.put("longitude", poi.getLongitude());
            json.put("description", (poi.getNom() != null) ? poi.getNom() : "POI");
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}