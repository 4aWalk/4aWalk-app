package fr.iutrodez.a4awalk.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppelAPI {

    private static RequestQueue fileRequete;

    public interface VolleyCallback {
        void onSuccess(JSONArray result);
        void onError(VolleyError error);
    }

    public interface VolleyObjectCallback {
        void onSuccess(JSONObject result) throws JSONException;
        void onError(VolleyError error);
    }

    /**
     * Méthode GET
     */
    public static void appelAPI(String url, String token, Context contexte, final VolleyCallback callback) {
        JsonArrayRequest requeteVolley = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                // On passe l'erreur brute directement au callback
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    /**
     * Méthodes POST
     */
    public static void postAPI(String url, String token, JSONObject body, Context contexte, final VolleyObjectCallback callback) {
        JsonObjectRequest requeteVolley = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    // Méthode utilitaire pour éviter la duplication de code des headers
    private static Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public static RequestQueue getFileRequete(Context contexte) {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(contexte);
        }
        return fileRequete;
    }
}