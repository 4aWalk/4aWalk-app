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

    /**
     * Extrait un POI unique (comme le départ ou l'arrivée) depuis la réponse globale.
     */
    public static PointOfInterest extractSinglePOI(JSONObject response, String key) {
        JSONObject obj = response.optJSONObject(key);
        if (obj != null) {
            try {
                return parsePOI(obj);
            } catch (JSONException e) {
                Log.e("ServicePOI", "Erreur parsing du POI : " + key);
            }
        }
        return null;
    }

    /**
     * Extrait la liste des POIs optionnels depuis la réponse globale.
     */
    public static ArrayList<PointOfInterest> extractPOIs(JSONObject response) {
        ArrayList<PointOfInterest> pois = new ArrayList<>();
        JSONArray poisJson = response.optJSONArray("points");

        // Conservation de votre logique de fallback d'origine
        int fallbackCount = response.optInt("nbParticipants", response.optInt("participants", 0));

        try {
            if (poisJson != null && poisJson.length() > 0) {
                for (int i = 0; i < poisJson.length(); i++) {
                    pois.add(parsePOI(poisJson.getJSONObject(i)));
                }
            } else if (fallbackCount > 0) {
                for (int i = 0; i < fallbackCount; i++) {
                    pois.add(new PointOfInterest());
                }
            }
        } catch (JSONException e) {
            Log.e("ServicePOI", "Erreur parsing liste POIs");
        }
        return pois;
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

    /**
     * Parse un JSONObject pour en extraire un PointOfInterest
     */
    public static PointOfInterest parsePOI(JSONObject obj) throws JSONException {
        return new PointOfInterest(
                obj.getInt("id"),
                obj.getString("nom"),
                obj.getDouble("latitude"),
                obj.getDouble("longitude"),
                obj.optString("description", ""),
                obj.optInt("sequence", 0)
        );
    }

}