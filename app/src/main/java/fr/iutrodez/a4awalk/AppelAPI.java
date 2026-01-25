package fr.iutrodez.a4awalk;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppelAPI {

    public final static String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QDRhd2Fsay5mciIsInVzZXJJZCI6MywiaWF0IjoxNzY5Mzc2NDI3LCJleHAiOjE3Njk0NjI4Mjd9.4Myn9_3J3ppCcgeGsEOmINj-o1OUmSykiEKSme7L-Zg"; // Ton token

    private static RequestQueue fileRequete;

    // Callback existant pour les listes (GET)
    public interface VolleyCallback {
        void onSuccess(JSONArray result);
        void onError(String message);
    }

    // NOUVEAU : Callback pour un objet unique (POST / Création)
    public interface VolleyObjectCallback {
        void onSuccess(JSONObject result);
        void onError(String message);
    }

    /**
     * Méthode existante pour récupérer la liste (GET)
     */
    public static void appelAPI(String url, Context contexte, final VolleyCallback callback) {
        JsonArrayRequest requeteVolley = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                erreur -> {
                    Toast.makeText(contexte, R.string.data_error_message, Toast.LENGTH_LONG).show();
                    callback.onError(erreur.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    /**
     * NOUVELLE MÉTHODE : Pour envoyer des données (POST)
     */
    public static void postAPI(String url, JSONObject body, Context contexte, final VolleyObjectCallback callback) {
        JsonObjectRequest requeteVolley = new JsonObjectRequest(Request.Method.POST, url, body,
                callback::onSuccess,
                erreur -> {
                    Toast.makeText(contexte, "Erreur lors de la création", Toast.LENGTH_LONG).show();
                    callback.onError(erreur.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    public static RequestQueue getFileRequete(Context contexte) {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(contexte);
        }
        return fileRequete;
    }
}