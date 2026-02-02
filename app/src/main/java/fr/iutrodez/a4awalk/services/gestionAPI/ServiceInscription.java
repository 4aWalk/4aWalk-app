package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceInscription {

    private static final String REGISTER_URL = "http://98.94.8.220:8080/users/register";

    public interface ApiSuccessCallback {
        void onSuccess();
    }

    public interface ApiErrorCallback {
        void onError(String message);
    }

    public static void registerUser(
            Context context,
            User user,
            ApiSuccessCallback onSuccess,
            ApiErrorCallback onError
    ) {

        // Préparation du corps de la requête (JSON)
        JSONObject body = new JSONObject();
        try {
            body.put("mail", user.getMail());
            body.put("password", user.getPassword());
            body.put("nom", user.getNom());
            body.put("prenom", user.getPrenom());
            body.put("adresse", user.getAdresse());
            body.put("age", user.getAge());

            // On envoie le nom de l'enum sous forme de String
            body.put("niveau", user.getNiveau().toString());
            body.put("morphologie", user.getMorphologie().toString());
            Log.i("données JSON", body.toString());
        } catch (JSONException e) {
            onError.onError("Erreur lors de la préparation des données");
            return;
        }

        // Appel via la classe utilitaire AppelAPI
        AppelAPI.post(REGISTER_URL, null, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                onSuccess.onSuccess();
            }

            @Override
            public void onError(VolleyError erreur) {
                Log.e("API_ERROR", "Détails : " + erreur.toString());
                onError.onError(erreur.networkResponse.toString());
            }
        });
    }

}