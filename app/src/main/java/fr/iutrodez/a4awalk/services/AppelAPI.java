package fr.iutrodez.a4awalk.services;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

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

/**
 * Classe utilitaire centralisant tous les appels HTTP de l'application.
 *
 * <p>Fournit des méthodes statiques pour effectuer des requêtes
 * GET, POST, PUT et DELETE via la librairie Volley.</p>
 *
 * <p>Utilise le pattern Singleton pour la file de requêtes.</p>
 */
public class AppelAPI {

    /** File de requêtes Volley (Singleton) */
    private static RequestQueue fileRequete;

    // =========================================================================
    // INTERFACES CALLBACKS
    // =========================================================================

    /**
     * Callback pour les réponses retournant un tableau JSON.
     */
    public interface VolleyCallback {
        /**
         * Appelé en cas de succès.
         *
         * @param result Tableau JSON retourné par le serveur
         */
        void onSuccess(JSONArray result);

        /**
         * Appelé en cas d'erreur réseau ou serveur.
         *
         * @param error Erreur Volley contenant le détail de l'échec
         */
        void onError(VolleyError error);
    }

    /**
     * Callback pour les réponses retournant un objet JSON.
     */
    public interface VolleyObjectCallback {
        /**
         * Appelé en cas de succès.
         *
         * @param result Objet JSON retourné par le serveur
         * @throws JSONException si une erreur de parsing survient
         */
        void onSuccess(JSONObject result) throws JSONException;

        /**
         * Appelé en cas d'erreur réseau ou serveur.
         *
         * @param error Erreur Volley contenant le détail de l'échec
         */
        void onError(VolleyError error);
    }

    // =========================================================================
    // MÉTHODES HTTP
    // =========================================================================

    /**
     * Effectue une requête GET et attend un tableau JSON en réponse.
     *
     * @param url      URL de l'endpoint
     * @param token    Token d'authentification Bearer
     * @param contexte Contexte Android
     * @param callback Callback appelé en cas de succès ou d'erreur
     */
    public static void get(String url, String token, Context contexte,
                           final VolleyCallback callback) {
        JsonArrayRequest requeteVolley = new JsonArrayRequest(
                Request.Method.GET, url, null,
                callback::onSuccess,
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
     * Effectue une requête POST avec un objet JSON et attend un objet JSON en réponse.
     *
     * @param url      URL de l'endpoint
     * @param token    Token d'authentification Bearer
     * @param body     Corps de la requête en JSON
     * @param contexte Contexte Android
     * @param callback Callback appelé en cas de succès ou d'erreur
     */
    public static void post(String url, String token, JSONObject body,
                            Context contexte, final VolleyObjectCallback callback) {
        JsonObjectRequest requeteVolley = new JsonObjectRequest(
                Request.Method.POST, url, body,
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

    /**
     * Effectue une requête PUT avec un tableau JSON et attend un tableau JSON en réponse.
     *
     * @param url      URL de l'endpoint
     * @param token    Token d'authentification Bearer
     * @param body     Corps de la requête en tableau JSON
     * @param contexte Contexte Android
     * @param callback Callback appelé en cas de succès ou d'erreur
     */
    public static void putA(String url, String token, JSONArray body,
                            Context contexte, final VolleyCallback callback) {
        JsonArrayRequest requeteVolley = new JsonArrayRequest(
                Request.Method.PUT, url, body,
                callback::onSuccess,
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
     * Effectue une requête PUT avec un objet JSON et attend un objet JSON en réponse.
     *
     * @param url      URL de l'endpoint
     * @param token    Token d'authentification Bearer
     * @param body     Corps de la requête en JSON
     * @param contexte Contexte Android
     * @param callback Callback appelé en cas de succès ou d'erreur
     */
    public static void put(String url, String token, JSONObject body,
                           Context contexte, final VolleyObjectCallback callback) {
        JsonObjectRequest requeteVolley = new JsonObjectRequest(
                Request.Method.PUT, url, body,
                response -> {
                    try {
                        if (callback != null) callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (callback != null) callback.onError(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    /**
     * Effectue une requête DELETE et attend un objet JSON en réponse.
     *
     * @param url      URL de l'endpoint
     * @param token    Token d'authentification Bearer
     * @param contexte Contexte Android
     * @param callback Callback appelé en cas de succès ou d'erreur
     */
    public static void delete(String url, String token, Context contexte,
                              final VolleyObjectCallback callback) {
        JsonObjectRequest requeteVolley = new JsonObjectRequest(
                Request.Method.DELETE, url, null,
                response -> {
                    try {
                        if (callback != null) callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (callback != null) callback.onError(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        getFileRequete(contexte).add(requeteVolley);
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    /**
     * Construit les en-têtes HTTP avec le token Bearer et le Content-Type.
     *
     * @param token Token d'authentification, peut être null ou vide
     * @return Map des en-têtes HTTP
     */
    private static Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    /**
     * Retourne la file de requêtes Volley en la créant si nécessaire (Singleton).
     *
     * @param contexte Contexte Android
     * @return Instance unique de {@link RequestQueue}
     */
    public static RequestQueue getFileRequete(Context contexte) {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(contexte);
        }
        return fileRequete;
    }

    /**
     * Réinitialise la file de requêtes Volley.
     *
     * <p><b>Usage réservé aux tests unitaires</b> afin d'isoler
     * chaque test et éviter les effets de bord entre eux.</p>
     */
    @VisibleForTesting
    public static void resetFileRequete() {
        fileRequete = null;
    }
}