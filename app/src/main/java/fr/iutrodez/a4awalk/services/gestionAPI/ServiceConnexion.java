package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import fr.iutrodez.a4awalk.R;
import fr.iutrodez.a4awalk.modeles.entites.LoginRequest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceConnexion {

    private final static String LOGIN_URL = "http://98.94.8.220:8080/users/login";

    public static void loginUser(
            Context context,
            LoginRequest loginRequest,
            java.util.function.BiConsumer<String, User> onSuccess,
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
        AppelAPI.post(LOGIN_URL, null, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                if (result.has("token")) {
                    String token = result.getString("token");

                    // On récupère l'utilisateur créé par votre méthode d'extraction
                    User utilisateur = extractionUtilisateur(result);

                    // On transmet les DEUX informations au callback
                    onSuccess.accept(token, utilisateur);
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
                        case 400: message = context.getString(R.string.erreur_donnees_util_connexion); break;
                        case 403: message = context.getString(R.string.erreur_acces); break;
                        case 404: message = context.getString(R.string.erreur_ressource); break;
                        case 500: message = context.getString(R.string.erreur_serveur); break;
                        case 401:message="Email ou mot de passe incorrecte";break;
                        default:  message = "Erreur inconnue (Code: " + erreur.networkResponse.statusCode + ")";
                    }
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

        });
    }

    private static User extractionUtilisateur(JSONObject result) {
        try {

            // 2. Accéder à l'objet imbriqué "user"
            if (result.has("user")) {
                JSONObject userObject = result.getJSONObject("user");

                int age = userObject.optInt("age");
                String nom = userObject.optString("nom");
                String prenom = userObject.optString("prenom");
                String mail = userObject.optString("mail");
                String adresse = userObject.optString("adresse");
                String niveau = userObject.optString("niveau");
                String morphologie = userObject.optString("morphologie");
                String fullName = userObject.optString("fullName");

                // Exemple d'affichage
                Log.d("JSON_DATA", "Utilisateur : " + prenom + " - Niveau : " + niveau);

                return new User(nom, prenom, age, mail, adresse, Level.valueOf(niveau), Morphology.valueOf(morphologie));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON_ERROR", "Erreur lors du parsing du JSON");
        }
        return null;
    }
}

