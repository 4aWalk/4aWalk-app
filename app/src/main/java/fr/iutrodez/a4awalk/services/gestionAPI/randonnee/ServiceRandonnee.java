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

    private static final String URL_RANDOS = "http://98.94.8.220:8080/hikes/my";

    public interface RandoCallback {
        void onSuccess(ArrayList<Hike> randonnees);
        void onError(VolleyError error);
    }

    public static void recupererRandonneesUtilisateur(Context context, String token, User currentUser, RandoCallback callback) {
        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                ArrayList<Hike> listeRandos = parseHikesFromJSON(result, currentUser);
                callback.onSuccess(listeRandos);
            }

            @Override
            public void onError(VolleyError erreur) {
                callback.onError(erreur);
            }
        });
    }

    private static ArrayList<Hike> parseHikesFromJSON(JSONArray jsonArray, User currentUser) {
        ArrayList<Hike> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject randoJson = jsonArray.getJSONObject(i);

                int id = randoJson.getInt("id");
                String name = randoJson.getString("libelle");
                int dureeJours = randoJson.getInt("dureeJours");

                JSONObject departObj = randoJson.getJSONObject("depart");
                PointOfInterest POIDepart = new PointOfInterest(
                        departObj.getInt("id"), departObj.getString("nom"),
                        departObj.getDouble("latitude"), departObj.getDouble("longitude")
                );

                JSONObject arriveeObj = randoJson.getJSONObject("arrivee");
                PointOfInterest POIArrivee = new PointOfInterest(
                        arriveeObj.getInt("id"), arriveeObj.getString("nom"),
                        arriveeObj.getDouble("latitude"), arriveeObj.getDouble("longitude")
                );

                ArrayList<PointOfInterest> listePoi = new ArrayList<>();
                JSONArray points = randoJson.getJSONArray("points");
                for (int j = 0; j < points.length(); j++) {
                    JSONObject point = points.getJSONObject(j);
                    listePoi.add(new PointOfInterest(
                            point.getInt("id"), point.getString("nom"),
                            point.getDouble("latitude"), point.getDouble("longitude")
                    ));
                }

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

                    try {
                        p.setNiveau(Level.valueOf(partJson.getString("niveau")));
                    } catch (Exception e) {
                        p.setNiveau(Level.DEBUTANT);
                    }

                    try {
                        p.setMorphologie(Morphology.valueOf(partJson.getString("morphologie")));
                    } catch (Exception e) {
                        p.setMorphologie(Morphology.MOYENNE);
                    }

                    listeParticipants.add(p);
                }

                Hike hike = new Hike(id, name, POIDepart, POIArrivee, dureeJours, currentUser);
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