package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceCreationRandonnee {

    private final static String URL_CREATION = "http://98.94.8.220:8080/hikes";
    private final static String URL_AJOUT_POI = "http://98.94.8.220:8080/hikes/%d/poi";

    private Context context;
    private TokenManager tokenManager;

    // Interface pour communiquer le résultat final à l'Activité
    public interface CreationCallback {
        // On ajoute un paramètre pour recevoir l'ID
        void onSuccess(long hikeId);
        void onError(String message);
    }

    public ServiceCreationRandonnee(Context context) {
        this.context = context;
        this.tokenManager = new TokenManager(context);
    }

    /**
     * Point d'entrée principal pour créer une randonnée complète
     */
    public void creerRandonnee(String libelle, int duree,
                               String depLat, String depLon,
                               String arrLat, String arrLon,
                               List<PointOfInterest> pois,
                               List<Participant> participants,
                               CreationCallback callback) {
        try {
            JSONObject randoAEnvoyer = construireJsonRandonnee(libelle, duree);

            AppelAPI.post(URL_CREATION, tokenManager.getToken(), randoAEnvoyer, context, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        // Extraction de l'ID depuis la réponse JSON
                        long randoID = result.getLong("id");

                        // IMPORTANT : Transmettre randoID à envoyerDonneesLiees
                        // Cette méthode devra appeler callback.onSuccess(randoID) à la toute fin
                        envoyerDonneesLiees(randoID, depLat, depLon, arrLat, arrLon, pois, participants, callback);

                    } catch (JSONException e) {
                        callback.onError("Erreur lors de la lecture de l'ID de la randonnée créée.");
                    }
                }

                @Override
                public void onError(VolleyError erreur) {
                    callback.onError("Échec de la création de la randonnée (API).");
                }
            });

        } catch (JSONException e) {
            callback.onError("Erreur interne lors de la construction des données.");
        }
    }

    private JSONObject construireJsonRandonnee(String libelle, int duree) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("libelle", libelle);
        json.put("dureeJours", duree);

        // IDs techniques requis par l'API pour la structure initiale
        JSONObject depart = new JSONObject();
        depart.put("id", 1);
        json.put("depart", depart);

        JSONObject arrivee = new JSONObject();
        arrivee.put("id", 2);
        json.put("arrivee", arrivee);

        return json;
    }

    private void envoyerDonneesLiees(long randoID,
                                     String depLat, String depLon,
                                     String arrLat, String arrLon,
                                     List<PointOfInterest> pois,
                                     List<Participant> participants,
                                     CreationCallback callback) {

        // 2. POIs intermédiaires
        //for (PointOfInterest p : pois) {
        //    createAndSendPOI(randoID, p.getName(), p.getLatitude(), p.getLongitude());
        //}
    }
}