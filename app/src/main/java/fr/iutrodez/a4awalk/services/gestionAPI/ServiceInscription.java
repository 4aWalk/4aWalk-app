package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

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

                if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
                    int statusCode = erreur.networkResponse.statusCode;
                    String errorBody = new String(erreur.networkResponse.data, StandardCharsets.UTF_8);
                    Log.e("API_ERROR", "Code: " + statusCode + " | Body: " + errorBody);

                    if (statusCode == 400) {
                        // On vérifie si le message de l'API mentionne le mail
                        try {
                            JSONObject errorJson = new JSONObject(errorBody);
                            String apiMessage = errorJson.optString("message", "").toLowerCase();
                            if (apiMessage.contains("mail") || apiMessage.contains("email")) {
                                onError.onError("Cette adresse e-mail est déjà utilisée. Veuillez en choisir une autre.");
                            } else {
                                onError.onError("Données invalides. Veuillez vérifier les informations saisies.");
                            }
                        } catch (JSONException e) {
                            // Si le body n'est pas du JSON, message générique
                            onError.onError("Cette adresse e-mail est déjà utilisée. Veuillez en choisir une autre.");
                        }
                    } else {
                        onError.onError("Erreur " + statusCode + " : " + errorBody);
                    }
                } else {
                    onError.onError("Erreur réseau : vérifiez votre connexion internet.");
                }
            }
        });
    }

}