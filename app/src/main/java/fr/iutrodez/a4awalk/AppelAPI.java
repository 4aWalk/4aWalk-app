package fr.iutrodez.a4awalk;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AppelAPI {

    private static RequestQueue fileRequete;

    // Interface pour gérer le retour asynchrone
    public interface VolleyCallback {
        void onSuccess(JSONObject result);
        void onError(String message);
    }

    /**
     * La méthode est maintenant void car on ne peut pas retourner la valeur directement.
     * On passe un "callback" qui sera appelé quand la réponse arrivera.
     */
    public static void appelAPI(String url, Context contexte, final VolleyCallback callback) {

        JsonObjectRequest requeteVolley = new JsonObjectRequest(Request.Method.GET, url, null,
                // Écouteur de succès : renvoie un JSONArray
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject reponse) {
                        // On transmet la réponse via le callback
                        callback.onSuccess(reponse);
                    }
                },
                // Écouteur d'erreur
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError erreur) {
                        Toast.makeText(contexte, R.string.data_error_message, Toast.LENGTH_LONG).show();
                        // On signale l'erreur via le callback (optionnel)
                        callback.onError(erreur.toString());
                    }
                });

        // Ajout à la file d'attente
        getFileRequete(contexte).add(requeteVolley);
    }

    public static RequestQueue getFileRequete(Context contexte) {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(contexte);
        }
        return fileRequete;
    }
}