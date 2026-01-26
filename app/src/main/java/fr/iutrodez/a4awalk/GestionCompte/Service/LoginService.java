package fr.iutrodez.a4awalk.GestionCompte.Service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.GestionCompte.LoginRequest;

public class LoginService {

    public static void loginUser(
            Context context,
            LoginRequest loginRequest,
            java.util.function.Consumer<String> onSuccess,
            java.util.function.Consumer<String> onError
    ) {

        String url = "http://98.94.8.220:8080/users/login";

        JSONObject body = new JSONObject();
        try {
            body.put("mail", loginRequest.getEmail());
            body.put("password", loginRequest.getPassword());
        } catch (JSONException e) {
            onError.accept("Erreur création requête");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        // 🔑 Récupération du token
                        String token = response.getString("token");
                        onSuccess.accept(token);
                    } catch (JSONException e) {
                        onError.accept("Réponse invalide du serveur");
                    }
                },
                error -> {
                    String message;

                    if (error.networkResponse == null) {
                        message = "Impossible de se connecter au serveur";
                    } else {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                message = "Email ou mot de passe incorrect";
                                break;
                            case 400:
                                message = "Données invalides";
                                break;
                            case 500:
                                message = "Erreur serveur";
                                break;
                            default:
                                message = "Erreur inconnue";
                        }
                    }
                    onError.accept(message);
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}

