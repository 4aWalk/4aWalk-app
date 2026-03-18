package fr.iutrodez.a4awalk.services.gestionAPI.randonnee;

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

    // Interface pour communiquer le résultat final à l'Activité
    public interface FullCreationCallback {
        void onSuccess(long hikeId);
        void onError(String message);
    }

    /**
     * Point d'entrée principal pour créer une randonnée complète
     */
    public static void creerRandonnee(Context context, String token, String nom, int duree,
                                                String nomD, String descriptionD, double latitudeD, double longitudeD,
                                                String nomA, String descriptionA, double latitudeA, double longitudeA,
                                                FullCreationCallback callback){

            JSONObject body = construireJsonRandonnee(nom, duree,
                    nomD, descriptionD, latitudeD, longitudeD,
                    nomA, descriptionA, latitudeA, longitudeA);

            AppelAPI.post(URL_CREATION, token, body, context, new AppelAPI.VolleyObjectCallback() {
                @Override
                public void onSuccess(JSONObject result) throws JSONException {
                    // On récupère l'ID de la rando créée (clé "id" selon ton API)
                    long hikeId = result.getLong("id");
                    Log.i("1", "Création randonnée n°" + hikeId);

                    // On informe l'activité que le processus est lancé avec succès
                    callback.onSuccess(hikeId);
                }

                @Override
                public void onError(com.android.volley.VolleyError error) {
                    callback.onError("Erreur lors de la création de la randonnée : " + error.getMessage());
                }
            });
        };

    public static JSONObject construireJsonRandonnee(String libelle, int duree,
                                                      String nomD, String descriptionD, double latitudeD, double longitudeD,
                                                      String nomA, String descriptionA, double latitudeA, double longitudeA) {
        JSONObject json = new JSONObject();
        try {
            json.put("libelle", libelle);
            json.put("dureeJours", duree);

            // IDs techniques requis par l'API pour la structure initiale
            JSONObject depart = new JSONObject();
            depart.put("nom", nomD);
            depart.put("description", descriptionD);
            depart.put("latitude", latitudeD);
            depart.put("longitude", longitudeD);
            json.put("depart", depart);

            JSONObject arrivee = new JSONObject();
            arrivee.put("nom", nomA);
            arrivee.put("description", descriptionA);
            arrivee.put("latitude", latitudeA);
            arrivee.put("longitude", longitudeA);
            json.put("arrivee", arrivee);

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}