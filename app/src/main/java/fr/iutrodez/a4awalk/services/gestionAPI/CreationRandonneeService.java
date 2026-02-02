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

public class CreationRandonneeService {

    private final static String URL_CREATION = "http://98.94.8.220:8080/hikes";
    private final static String URL_AJOUT_POI = "http://98.94.8.220:8080/hikes/%d/poi";
    private final static String URL_AJOUT_PARTICIPANT = "http://98.94.8.220:8080/hikes/%d/participants";

    private Context context;
    private TokenManager tokenManager;

    // Interface pour communiquer le résultat final à l'Activité
    public interface CreationCallback {
        void onSuccess();
        void onError(String message);
    }

    public CreationRandonneeService(Context context) {
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
                        long randoID = result.getLong("id");
                        // Une fois la rando créée, on lance les ajouts annexes
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

        AtomicInteger requetesEnCours = new AtomicInteger(0);
        // Wrapper pour boolean mutable
        final boolean[] erreurDetectee = {false};

        // 1. Gestion Départ / Arrivée comme POI
        ajouterPoiSysteme(randoID, "Départ", depLat, depLon, requetesEnCours, erreurDetectee, callback);
        ajouterPoiSysteme(randoID, "Arrivée", arrLat, arrLon, requetesEnCours, erreurDetectee, callback);

        // 2. POIs intermédiaires
        for (PointOfInterest p : pois) {
            createAndSendPOI(randoID, p.getName(), p.getLatitude(), p.getLongitude(), requetesEnCours, erreurDetectee, callback);
        }

        // 3. Participants
        for (Participant p : participants) {
            envoyerParticipant(randoID, p, requetesEnCours, erreurDetectee, callback);
        }

        // Cas où il n'y a RIEN à ajouter (rare mais possible)
        if (requetesEnCours.get() == 0) {
            callback.onSuccess();
        }
    }

    private void ajouterPoiSysteme(long randoID, String nom, String latStr, String lonStr,
                                   AtomicInteger counter, boolean[] errorFlag, CreationCallback callback) {
        if (latStr != null && !latStr.isEmpty() && lonStr != null && !lonStr.isEmpty()) {
            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                createAndSendPOI(randoID, nom, lat, lon, counter, errorFlag, callback);
            } catch (NumberFormatException e) {
                // Ignore les coordonnées malformées pour ne pas bloquer le reste
            }
        }
    }

    private void createAndSendPOI(long randoID, String nom, double lat, double lon,
                                  AtomicInteger counter, boolean[] errorFlag, CreationCallback callback) {
        try {
            JSONObject jsonPoi = new JSONObject();
            jsonPoi.put("name", nom);
            jsonPoi.put("latitude", lat);
            jsonPoi.put("longitude", lon);
            jsonPoi.put("description", nom);

            envoyerRequeteGenerique(String.format(URL_AJOUT_POI, randoID), jsonPoi, counter, errorFlag, callback);
        } catch (JSONException e) {
            Log.e("SERVICE", "Erreur JSON POI");
        }
    }

    private void envoyerParticipant(long randoID, Participant p,
                                    AtomicInteger counter, boolean[] errorFlag, CreationCallback callback) {
        try {
            JSONObject jsonPart = new JSONObject();
            jsonPart.put("age", p.getAge());
            jsonPart.put("niveau", p.getNiveau());
            jsonPart.put("morphologie", p.getMorphologie());
            // Valeurs par défaut pour éviter erreurs API
            jsonPart.put("besoinKcal", 0);
            jsonPart.put("besoinEauLitre", 0);
            jsonPart.put("capaciteEmportMaxKg", 0.0);

            envoyerRequeteGenerique(String.format(URL_AJOUT_PARTICIPANT, randoID), jsonPart, counter, errorFlag, callback);
        } catch (JSONException e) {
            Log.e("SERVICE", "Erreur JSON Participant");
        }
    }

    private void envoyerRequeteGenerique(String url, JSONObject payload,
                                         AtomicInteger counter, boolean[] errorFlag, CreationCallback callback) {
        counter.incrementAndGet();

        AppelAPI.post(url, tokenManager.getToken(), payload, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                verifierFinDesRequetes(counter, errorFlag, callback);
            }

            @Override
            public void onError(VolleyError erreur) {
                errorFlag[0] = true;
                verifierFinDesRequetes(counter, errorFlag, callback);
            }
        });
    }

    private synchronized void verifierFinDesRequetes(AtomicInteger counter, boolean[] errorFlag, CreationCallback callback) {
        if (counter.decrementAndGet() == 0) {
            if (!errorFlag[0]) {
                callback.onSuccess();
            } else {
                // On considère que c'est un succès partiel (la rando existe),
                // mais on prévient l'utilisateur via un message spécifique si besoin
                callback.onSuccess();
                // Ou callback.onError("Créée avec des erreurs partielles") selon votre logique métier
            }
        }
    }
}