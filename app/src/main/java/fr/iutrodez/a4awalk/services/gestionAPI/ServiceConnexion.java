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

/**
 * Service gérant l'authentification de l'utilisateur.
 * <p>
 * Envoie les identifiants à l'API REST, traite la réponse et extrait
 * le token JWT ainsi que les informations du profil utilisateur.
 */
public class ServiceConnexion {

    /** URL de l'endpoint de connexion. */
    private final static String LOGIN_URL = "http://98.94.8.220:8080/users/login";

    /**
     * Envoie une requête de connexion à l'API avec les identifiants fournis.
     * <p>
     * En cas de succès, transmet le token JWT et l'objet {@link User} au callback.
     * En cas d'échec, affiche un message d'erreur contextuel et notifie via {@code onError}.
     *
     * @param context      Contexte Android utilisé pour les appels réseau et les messages UI.
     * @param loginRequest Objet contenant l'email et le mot de passe saisis par l'utilisateur.
     * @param onSuccess    Callback appelé avec (token, utilisateur) en cas de succès.
     * @param onError      Callback appelé avec un message d'erreur en cas d'échec.
     */
    public static void loginUser(
            Context context,
            LoginRequest loginRequest,
            java.util.function.BiConsumer<String, User> onSuccess,
            java.util.function.Consumer<String> onError
    ) {

        // Construction du corps JSON de la requête à partir des identifiants
        JSONObject body = new JSONObject();
        try {
            body.put("mail", loginRequest.getEmail());
            body.put("password", loginRequest.getPassword());
        } catch (JSONException e) {
            onError.accept("Erreur création requête");
            return;
        }

        // Appel POST sans token (la connexion ne nécessite pas d'authentification préalable)
        AppelAPI.post(LOGIN_URL, null, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                if (result.has("token")) {
                    String token = result.getString("token");

                    // Extraction du profil utilisateur depuis la réponse JSON
                    User utilisateur = extractionUtilisateur(result);

                    // Transmission du token et de l'utilisateur à l'appelant
                    onSuccess.accept(token, utilisateur);
                } else {
                    onError.accept("Réponse invalide du serveur");
                }
            }

            @Override
            public void onError(VolleyError erreur) {
                // Sélection du message d'erreur selon le code HTTP retourné
                String message;
                if (erreur.networkResponse == null) {
                    message = "Impossible de se connecter au serveur";
                } else {
                    switch (erreur.networkResponse.statusCode) {
                        case 400: message = context.getString(R.string.erreur_donnees_util_connexion); break;
                        case 401: message = "Email ou mot de passe incorrecte"; break;
                        case 403: message = context.getString(R.string.erreur_acces); break;
                        case 404: message = context.getString(R.string.erreur_ressource); break;
                        case 500: message = context.getString(R.string.erreur_serveur); break;
                        default:  message = "Erreur inconnue (Code: " + erreur.networkResponse.statusCode + ")";
                    }
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Extrait les données du profil utilisateur depuis la réponse JSON de l'API.
     * <p>
     * Attend un objet JSON imbriqué sous la clé {@code "user"} contenant
     * les champs : nom, prenom, age, mail, adresse, niveau et morphologie.
     *
     * @param result L'objet JSON complet retourné par l'API lors de la connexion.
     * @return Un objet {@link User} renseigné, ou {@code null} en cas d'erreur de parsing.
     */
    private static User extractionUtilisateur(JSONObject result) {
        try {
            // Accès à l'objet imbriqué "user" dans la réponse
            if (result.has("user")) {
                JSONObject userObject = result.getJSONObject("user");

                int age        = userObject.optInt("age");
                String nom     = userObject.optString("nom");
                String prenom  = userObject.optString("prenom");
                String mail    = userObject.optString("mail");
                String adresse = userObject.optString("adresse");
                String niveau  = userObject.optString("niveau");
                String morpho  = userObject.optString("morphologie");

                Log.d("JSON_DATA", "Utilisateur : " + prenom + " - Niveau : " + niveau);

                // Conversion des chaînes vers les enums Level et Morphology
                return new User(nom, prenom, age, mail, adresse,
                        Level.valueOf(niveau), Morphology.valueOf(morpho));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON_ERROR", "Erreur lors du parsing du JSON");
        }
        return null;
    }
}