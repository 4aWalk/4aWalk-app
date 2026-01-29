package fr.iutrodez.a4awalk.gestionCompte.Service;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.entity.LoginRequest;
import fr.iutrodez.a4awalk.service.AppelAPI;

public class LoginService {

    private final static String LOGIN_URL = "http://98.94.8.220:8080/users/login";

    public static void loginUser(
            Context context,
            LoginRequest loginRequest,
            java.util.function.Consumer<String> onSuccess,
            java.util.function.Consumer<String> onError
    ) {

        JSONObject body = new JSONObject();
        try {
            body.put("mail", loginRequest.getEmail());
            body.put("password", loginRequest.getPassword());
        } catch (JSONException e) {
            onError.accept("Erreur création requête");
            return;
        }


        // 2. Utilisation de AppelAPI (pas de token nécessaire pour se connecter)
        AppelAPI.postAPI(LOGIN_URL, null, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // On extrait le token du résultat JSON
                if (result.has("token")) {
                    String token = result.getString("token");
                    onSuccess.accept(token);
                } else {
                    onError.accept("Réponse invalide du serveur");
                }
            }

            @Override
            public void onError(VolleyError erreur) {
                String message;
                if (erreur.networkResponse == null) {
                    message = "Impossible de se connecter au serveur";
                } else {
                    switch (erreur.networkResponse.statusCode) {
                        case 401: message = "Email ou mot de passe incorrect"; break;
                        case 400: message = "Données invalides"; break;
                        case 403: message = "Accès refusé"; break;
                        case 404: message = "Ressource non trouvée"; break;
                        case 500: message = "Erreur serveur"; break;
                        default:  message = "Erreur inconnue (Code: " + erreur.networkResponse.statusCode + ")";
                    }
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

        });
    }
}

