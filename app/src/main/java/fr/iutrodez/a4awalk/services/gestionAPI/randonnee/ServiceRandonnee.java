package fr.iutrodez.a4awalk.services.gestionAPI.randonnee;

import android.content.Context;
import android.util.Log;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.entites.FoodProduct;
import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;

public class ServiceRandonnee {

    private static final String URL_RANDOS = "http://98.94.8.220:8080/hikes/my";
    private static final String URL_RANDO_DETAIL = "http://98.94.8.220:8080/hikes/";

    public interface RandoCallback {
        void onSuccess(ArrayList<Hike> randonnees);
        void onError(VolleyError error);
    }

    public interface RandoDetailCallback {
        void onSuccess(Hike randonnee);
        void onError(VolleyError error);
    }

    public static void recupererRandonneesUtilisateur(Context context, String token, User currentUser, RandoCallback callback) {
        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                if (result == null || result.length() == 0) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                ArrayList<Hike> listeRandos = parseHikesFromJSON(result, currentUser);
                callback.onSuccess(listeRandos);
            }

            @Override
            public void onError(VolleyError erreur) {
                Log.e("ServiceRandonnee", "Erreur réseau liste : " + erreur.toString());
                callback.onError(erreur);
            }
        });
    }

    public static void recupererDetailsRandonnee(Context context, String token, int hikeId, User currentUser, RandoDetailCallback callback) {
        String url = URL_RANDO_DETAIL + hikeId;
        AppelAPI.get(url, token, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (result == null) {
                    callback.onError(new VolleyError("Résultat vide"));
                    return;
                }
                Hike hikeDetail = parseHikeDetail(result, currentUser);
                if (hikeDetail != null) {
                    callback.onSuccess(hikeDetail);
                } else {
                    callback.onError(new VolleyError("Erreur de parsing des détails"));
                }
            }

            @Override
            public void onError(VolleyError erreur) {
                Log.e("ServiceRandonnee", "Erreur réseau détail : " + erreur.toString());
                callback.onError(erreur);
            }
        });
    }

    private static ArrayList<Hike> parseHikesFromJSON(JSONArray jsonArray, User currentUser) {
        ArrayList<Hike> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject randoJson = jsonArray.getJSONObject(i);
                Hike hike = parseHikeDetail(randoJson, currentUser);
                if (hike != null) {
                    liste.add(hike);
                }
            } catch (JSONException e) {
                Log.e("ServiceRandonnee", "Erreur parsing JSON sur l'index " + i + " : " + e.getMessage());
            }
        }
        return liste;
    }

    public static Hike parseHikeDetail(JSONObject response, User currentUser) {
        try {
            int id = response.getInt("id");
            String libelle = response.getString("libelle");
            int dureeJours = response.getInt("dureeJours");
            boolean optimize = response.optBoolean("optimize", false);

            JSONObject departObj = response.optJSONObject("depart");
            PointOfInterest depart = (departObj != null) ? parsePOI(departObj) : null;

            JSONObject arriveeObj = response.optJSONObject("arrivee");
            PointOfInterest arrivee = (arriveeObj != null) ? parsePOI(arriveeObj) : null;

            Hike hike = new Hike(id, libelle, depart, arrivee, dureeJours, currentUser, optimize);

            // --- Parsing Participants (Gère le cas Liste et le cas Détail) ---
            ArrayList<Participant> participants = new ArrayList<>();
            JSONArray partsJson = response.optJSONArray("participants");

            // On cherche le nombre de participants (clé nbParticipants ou participants si c'est un nombre)
            int nbParticipantsCompteur = response.optInt("nbParticipants", response.optInt("participants", 0));

            if (partsJson != null && partsJson.length() > 0) {
                // CAS DÉTAIL : On a les objets complets
                for (int i = 0; i < partsJson.length(); i++) {
                    participants.add(parseParticipant(partsJson.getJSONObject(i)));
                }
            } else if (nbParticipantsCompteur > 0) {
                // CAS LISTE : On crée des objets vides pour que le .size() soit correct
                for (int i = 0; i < nbParticipantsCompteur; i++) {
                    participants.add(new Participant());
                }
            }
            hike.setParticipants(participants);

            // --- Parsing points d'intérêt (Gère le cas Liste et le cas Détail) ---
            ArrayList<PointOfInterest> pois = new ArrayList<>();
            JSONArray poisJson = response.optJSONArray("points");

            if (poisJson != null && poisJson.length() > 0) {
                for (int i = 0; i < poisJson.length(); i++) {
                    pois.add(parsePOI(poisJson.getJSONObject(i)));
                }
            } else if (nbParticipantsCompteur > 0) {
                for (int i = 0; i < nbParticipantsCompteur; i++) {
                    pois.add(new PointOfInterest());
                }
            }
            hike.setOptionalPoints(pois);


            // --- Food Catalogue ---
            JSONArray foodJson = response.optJSONArray("foodCatalogue");
            List<FoodProduct> foodList = new ArrayList<>();
            if (foodJson != null) {
                for (int i = 0; i < foodJson.length(); i++) {
                    JSONObject f = foodJson.getJSONObject(i);
                    FoodProduct fp = ServiceFoodProduct.constructFPFromJson(f);
                    foodList.add(fp);
                }
            }
            hike.setFoodCatalogue(foodList);

            // --- Equipment Groups ---
            JSONObject groupsJson = response.optJSONObject("equipmentGroups");
            List<EquipmentItem> equipList = new ArrayList<>();
            if (groupsJson != null) {
                java.util.Iterator<String> keys = groupsJson.keys();
                while (keys.hasNext()) {
                    String categoryKey = keys.next();
                    JSONObject categoryObj = groupsJson.optJSONObject(categoryKey);
                    if (categoryObj != null) {
                        JSONArray itemsArray = categoryObj.optJSONArray("items");
                        if (itemsArray != null) {
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject e = itemsArray.getJSONObject(i);
                                EquipmentItem item = new EquipmentItem();
                                item.setId(e.getInt("id"));
                                item.setNom(e.getString("nom"));
                                item.setType(TypeEquipment.valueOf(e.optString("type", "AUTRE")));
                                item.setNbItem(e.optInt("nbItem", 0));
                                equipList.add(item);
                            }
                        }
                    }
                }
            }
            hike.setEquipmentGroups(equipList);

            return hike;

        } catch (Exception e) {
            Log.e("ServiceRandonnee", "Erreur critique parsing JSON : " + e.getMessage());
            return null;
        }
    }

    private static PointOfInterest parsePOI(JSONObject obj) throws JSONException {
        return new PointOfInterest(
                obj.getInt("id"),
                obj.getString("nom"),
                obj.getDouble("latitude"),
                obj.getDouble("longitude"),
                obj.optString("description", ""),
                obj.optInt("sequence", 0)
        );
    }

    private static Participant parseParticipant(JSONObject obj) throws JSONException {
        Participant p = new Participant();
        p.setId(obj.getInt("id"));
        p.setPrenom(obj.optString("prenom", ""));
        p.setNom(obj.optString("nom", ""));
        p.setAge(obj.optInt("age", 0));
        p.setBesoinKcal(obj.optInt("besoinKcal", 0));
        p.setBesoinEauLitre(obj.optDouble("besoinEauLitre", 0.0));
        p.setCapaciteEmportMaxKg(obj.optDouble("capaciteEmportMaxKg", 0.0));

        try {
            p.setNiveau(Level.valueOf(obj.optString("niveau", "DEBUTANT")));
            p.setMorphologie(Morphology.valueOf(obj.optString("morphologie", "MOYENNE")));
        } catch (Exception e) {
            p.setNiveau(Level.DEBUTANT);
            p.setMorphologie(Morphology.MOYENNE);
        }
        return p;
    }

    public static void supprimerRandonnee(Context context, String token, int hikeId, AppelAPI.VolleyObjectCallback callback) {
        String url = URL_RANDO_DETAIL + hikeId;
        AppelAPI.delete(url, token, context, callback);
    }
}