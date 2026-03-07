package fr.iutrodez.a4awalk.services.gestionAPI.randonnee;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceRandonnee {

    // URL de l'API pour récupérer les randonnées de l'utilisateur
    private static final String URL_RANDOS = "http://98.94.8.220:8080/hikes/my";

    /**
     * Interface de callback pour renvoyer le résultat à l'activité/fragment.
     */
    public interface RandoCallback {
        void onSuccess(ArrayList<Hike> randonnees);
        void onError(VolleyError error);
    }

    /**
     * Méthode publique appelée par le Fragment pour charger les données.
     */
    public static void recupererRandonneesUtilisateur(Context context, String token, User currentUser, RandoCallback callback) {
        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // Conversion du JSON en objets Java
                ArrayList<Hike> listeRandos = parseHikesFromJSON(result, currentUser);
                callback.onSuccess(listeRandos);
            }

            @Override
            public void onError(VolleyError erreur) {
                callback.onError(erreur);
            }
        });
    }

    /**
     * Parsing des données JSON en objets Hike et Participant.
     */
    private static ArrayList<Hike> parseHikesFromJSON(JSONArray jsonArray, User currentUser) {
        ArrayList<Hike> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject randoJson = jsonArray.getJSONObject(i);

                // --- 1. Infos de base de la randonnée ---
                int id = randoJson.getInt("id");
                String name = randoJson.getString("libelle");
                int dureeJours = randoJson.getInt("dureeJours");

                // --- 2. Point de Départ ---
                JSONObject departObj = randoJson.getJSONObject("depart");
                PointOfInterest POIDepart = new PointOfInterest(
                        departObj.getInt("id"),
                        departObj.getString("nom"),
                        departObj.getDouble("latitude"),
                        departObj.getDouble("longitude")
                );

                // --- 3. Point d'Arrivée ---
                JSONObject arriveeObj = randoJson.getJSONObject("arrivee");
                PointOfInterest POIArrivee = new PointOfInterest(
                        arriveeObj.getInt("id"),
                        arriveeObj.getString("nom"),
                        arriveeObj.getDouble("latitude"),
                        arriveeObj.getDouble("longitude")
                );

                ArrayList<PointOfInterest> listePoi = new ArrayList<>();
                JSONArray points = randoJson.getJSONArray("points");
                for (int j = 0; j < points.length(); j++) {
                    JSONObject point = points.getJSONObject(j);
                    PointOfInterest poi = new PointOfInterest(
                            point.getInt("id"),
                            point.getString("nom"),
                            point.getDouble("latitude"),
                            point.getDouble("longitude")
                    );
                    listePoi.add(poi);
                }

                // --- 4. Participants (ArrayList pour Parcelable) ---
                ArrayList<Participant> listeParticipants = new ArrayList<>();
                JSONArray participantsArray = randoJson.getJSONArray("participants");

                for (int j = 0; j < participantsArray.length(); j++) {
                    JSONObject partJson = participantsArray.getJSONObject(j);
                    Participant p = new Participant();

                    p.setPId(partJson.getInt("id"));
                    p.setPrenom(partJson.optString("prenom", ""));
                    p.setNom(partJson.optString("nom", ""));
                    p.setAge(partJson.getInt("age"));
                    p.setCreator(partJson.getBoolean("isCreator"));
                    p.setBesoinKcal(partJson.getInt("besoinKcal"));
                    p.setBesoinEauLitre(partJson.getInt("besoinEauLitre"));
                    p.setCapaciteEmportMaxKg(partJson.getDouble("capaciteEmportMaxKg"));

                    // Gestion sécurisée des Enums (Level)
                    try {
                        String nivStr = partJson.getString("niveau");
                        p.setNiveau(Level.valueOf(nivStr));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        p.setNiveau(Level.DEBUTANT); // Valeur par défaut si erreur ou null
                    }

                    // Gestion sécurisée des Enums (Morphology)
                    try {
                        String morphoStr = partJson.getString("morphologie");
                        p.setMorphologie(Morphology.valueOf(morphoStr));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        p.setMorphologie(Morphology.MOYENNE); // Valeur par défaut
                    }

                    // Ajout à la liste
                    listeParticipants.add(p);
                    Log.i("Participants", p.getId() +" : " + p.getPrenom());
                }

                // --- 5. Création de l'objet Hike ---
                Hike hike = new Hike(id, name, POIDepart, POIArrivee, dureeJours, currentUser);

                // On passe l'ArrayList complète (compatible avec le nouveau code Parcelable)
                hike.setParticipants(listeParticipants);
                hike.setOptionalPoints(listePoi);

                liste.add(hike);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ServiceRandonnee", "Erreur parsing JSON: " + e.getMessage());
        }
        return liste;
    }
}