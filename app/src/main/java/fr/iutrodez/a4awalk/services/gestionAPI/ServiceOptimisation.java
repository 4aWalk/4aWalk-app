package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceOptimisation {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    /**
     * Interface pour gérer la réponse de l'API d'optimisation.
     */
    public interface OptimisationCallback {
        void onSuccess(JSONObject result);
        void onError(String message);
    }

    /**
     * Envoie une requête POST pour optimiser la randonnée.
     *
     * @param context   Contexte Android
     * @param token     Token d'authentification Bearer
     * @param idRando   ID de la randonnée à optimiser
     * @param callback  Gestionnaire de succès ou d'erreur
     */
    public static void optimiserRandonnee(Context context, String token, int idRando, final OptimisationCallback callback) {
        String url = BASE_URL + "/hikes/" + idRando + "/optimize";

        AppelAPI.post(url, token, null, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(VolleyError error) {
                String serverMessage = "Erreur réseau inconnue";

                // Si le serveur a renvoyé des données avec l'erreur (ex: 400 Bad Request)
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        // 1. On convertit les données brutes en texte (String)
                        String jsonString = new String(error.networkResponse.data, "UTF-8");

                        // 2. On transforme ce texte en objet JSON
                        JSONObject jsonObject = new JSONObject(jsonString);

                        // 3. On vérifie si la clé "message" existe et on récupère sa valeur
                        if (jsonObject.has("message")) {
                            serverMessage = jsonObject.getString("message");
                        } else {
                            // Si la clé "message" n'existe pas, on garde tout le JSON pour le debug
                            serverMessage = jsonString;
                        }

                    } catch (Exception e) {
                        // En cas de problème de conversion (ex: ce n'était pas du JSON valide)
                        serverMessage = "Erreur " + error.networkResponse.statusCode + " (Impossible de parser le JSON)";
                        Log.e("ServiceOptimisation", "Erreur de parsing JSON", e);
                    }
                } else if (error.getMessage() != null) {
                    serverMessage = error.getMessage();
                }

                Log.e("ServiceOptimisation", "Échec de la requête: " + serverMessage);

                // On renvoie le message extrait à l'activité
                if (callback != null) {
                    callback.onError(serverMessage);
                }
            }
        });
    }
}