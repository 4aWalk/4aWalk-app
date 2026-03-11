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
            Runnable onSuccess,
            java.util.function.Consumer<String> onError
    ) {

        String url = "http://98.94.8.220:8080/api/v1/users/login";

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
                    // Ici plus tard : token, user, etc.
                    onSuccess.run();
                },
                error -> {

                    String message;

                    if (error.networkResponse == null) {
                        message = "Impossible de se connecter au serveur";
                    } else {
                        int code = error.networkResponse.statusCode;

                        switch (code) {
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
                                message = "Erreur inconnue (" + code + ")";
                        }
                    }

                    onError.accept(message);
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}
