package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceRandonnee {

    private static final String URL_RANDOS = "http://98.94.8.220:8080/hikes/my";

    /**
     * Interface pour renvoyer le résultat au Fragment
     */
    public interface RandoCallback {
        void onSuccess(ArrayList<Hike> randonnees);
        void onError(VolleyError error);
    }

    /**
     * Récupère les randonnées depuis l'API, les transforme en objets et notifie le callback.
     */
    public static void recupererRandonneesUtilisateur(Context context, String token, User currentUser, RandoCallback callback) {

        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // On transforme le JSON en objets Java
                ArrayList<Hike> listeRandos = parseHikesFromJSON(result, currentUser);
                callback.onSuccess(listeRandos);
            }

            @Override
            public void onError(VolleyError erreur) {
                // On transmet l'erreur au Fragment
                callback.onError(erreur);
            }
        });
    }

    /**
     * Méthode interne pour parser le JSON (Private car utilisée uniquement ici)
     */
    private static ArrayList<Hike> parseHikesFromJSON(JSONArray jsonArray, User currentUser) {
        ArrayList<Hike> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject randoJson = jsonArray.getJSONObject(i);

                Long id = (long) randoJson.getInt("id");
                String name = randoJson.getString("libelle");

                // --- DEPART ---
                JSONObject departObj = randoJson.getJSONObject("depart");
                PointOfInterest POIDepart = new PointOfInterest(
                        (long) departObj.getInt("id"),
                        departObj.getString("name"),
                        departObj.getDouble("latitude"),
                        departObj.getDouble("longitude")
                );

                // --- ARRIVEE ---
                JSONObject arriveeObj = randoJson.getJSONObject("arrivee");
                PointOfInterest POIArrivee = new PointOfInterest(
                        (long) arriveeObj.getInt("id"),
                        arriveeObj.getString("name"),
                        arriveeObj.getDouble("latitude"),
                        arriveeObj.getDouble("longitude")
                );

                // --- PARTICIPANTS ---
                Set<Participant> ensembleParticipants = new HashSet<>();
                JSONArray participantsArray = randoJson.getJSONArray("participants");
                for (int j = 0; j < participantsArray.length(); j++) {
                    JSONObject partJson = participantsArray.getJSONObject(j);
                    Participant p = new Participant();
                    p.setId(partJson.getLong("id"));
                    p.setAge(partJson.getInt("age"));
                    p.setNiveau(Level.valueOf(partJson.getString("niveau")));
                    p.setMorphologie(Morphology.valueOf(partJson.getString("morphologie")));
                    p.setCreator(partJson.getBoolean("creator"));
                    p.setBesoinKcal(partJson.getInt("besoinKcal"));
                    p.setBesoinEauLitre(partJson.getInt("besoinEauLitre"));
                    p.setCapaciteEmportMaxKg(partJson.getDouble("capaciteEmportMaxKg"));
                    ensembleParticipants.add(p);
                }

                // --- HIKE ---
                int dureeJours = randoJson.getInt("dureeJours");
                Hike hike = new Hike(id, name, POIDepart, POIArrivee, dureeJours, currentUser);
                hike.setParticipants(ensembleParticipants);
                liste.add(hike);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liste;
    }
}