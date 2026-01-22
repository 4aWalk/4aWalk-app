package fr.iutrodez.a4awalk.GestionCompte.Service;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.GestionCompte.User;

public class RegisterService {

    private static final String REGISTER_URL =
            "http://98.94.8.220:8080/api/v1/users/register";

    public static void registerUser(
            Context context,
            User user,
            ApiSuccessCallback onSuccess,
            ApiErrorCallback onError
    ) {

        JSONObject body = new JSONObject();
        try {
            body.put("mail", user.getEmail());
            body.put("password", user.getPassword());
            body.put("nom", user.getNom());
            body.put("prenom", user.getPrenom());
            body.put("adresse", user.getAdresse());
            body.put("age", user.getAge()); // ✅ int propre
            body.put("niveau", user.getNiveau());
            body.put("morphologie", user.getMorphologie());
        } catch (JSONException e) {
            onError.onError("Erreur création JSON");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                REGISTER_URL,
                body,
                response -> onSuccess.onSuccess(),
                error -> onError.onError("Erreur réseau")
        );

        Volley.newRequestQueue(context).add(request);
    }

    public interface ApiSuccessCallback {
        void onSuccess();
    }

    public interface ApiErrorCallback {
        void onError(String message);
    }
}
