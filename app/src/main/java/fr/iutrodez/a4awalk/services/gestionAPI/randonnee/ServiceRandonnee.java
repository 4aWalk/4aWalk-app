package fr.iutrodez.a4awalk.services.gestionAPI.randonnee;

import android.content.Context;
import android.util.Log;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import fr.iutrodez.a4awalk.modeles.entites.Hike;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.modeles.entites.User;
import fr.iutrodez.a4awalk.services.AppelAPI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceEquipment; // Remplacé "Equipement" par "Equipment" pour la cohérence
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceFoodProduct;
import fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI;
import fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant;

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

    public static void recupererRandonneesUtilisateur(Context context, String token, User currentUser,boolean details, RandoCallback callback) {
        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                if (result == null || result.length() == 0) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                ArrayList<Hike> listeRandos = parseHikesFromJSON(result, currentUser, false);
                callback.onSuccess(listeRandos);
            }

            @Override
            public void onError(VolleyError erreur) {
                Log.e("ServiceRandonnee", "Erreur réseau liste : " + erreur.toString());
                callback.onError(erreur);
            }
        });
    }

    public static void recupererDetailsRandonnee(Context context, String token, int hikeId, User currentUser, boolean isDetails, RandoDetailCallback callback) {
        String url = URL_RANDO_DETAIL + hikeId;
        AppelAPI.get(url, token, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (result == null) {
                    callback.onError(new VolleyError("Résultat vide"));
                    return;
                }
                Hike hikeDetail = parseHikeDetail(result, currentUser, isDetails);
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

    private static ArrayList<Hike> parseHikesFromJSON(JSONArray jsonArray, User currentUser, boolean isDetails) {
        ArrayList<Hike> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject randoJson = jsonArray.getJSONObject(i);
                Hike hike = parseHikeDetail(randoJson, currentUser, isDetails);
                if (hike != null) {
                    liste.add(hike);
                }
            } catch (JSONException e) {
                Log.e("ServiceRandonnee", "Erreur parsing JSON sur l'index " + i + " : " + e.getMessage());
            }
        }
        return liste;
    }

    public static Hike parseHikeDetail(JSONObject response, User currentUser, boolean isDetails) {
        try {
            int id = response.getInt("id");
            String libelle = response.getString("libelle");
            int dureeJours = response.getInt("dureeJours");
            boolean optimize;

            Hike hike = new Hike();
            hike.setId(id);
            hike.setLibelle(libelle);
            hike.setDureeJours(dureeJours);
            if (isDetails) {
                optimize = response.getBoolean("optimize");
                PointOfInterest depart = ServicePOI.extractSinglePOI(response, "depart");
                hike.setDepart(depart);
                PointOfInterest arrivee = ServicePOI.extractSinglePOI(response, "arrivee");
                hike.setArrivee(arrivee);
                hike.setParticipants(ServiceParticipant.extractParticipants(response));
                hike.setOptionalPoints(ServicePOI.extractPOIs(response));
                hike.setFoodCatalogue(ServiceFoodProduct.extractFoodCatalogue(response));
                hike.setEquipmentGroups(ServiceEquipment.extractEquipmentCatalogue(response));
            } else {
                optimize = response.getBoolean("isOptimize");
            }
            hike.setOptimize(optimize);
            Log.i("OPTIMISER", "Randonnee: " + hike.getLibelle() + ", optimisation: " + hike.getOptimize());
            return hike;

        } catch (Exception e) {
            Log.e("ServiceRandonnee", "Erreur critique parsing JSON : " + e.getMessage());
            return null;
        }
    }

    public static void supprimerRandonnee(Context context, String token, int hikeId, AppelAPI.VolleyObjectCallback callback) {
        String url = URL_RANDO_DETAIL + hikeId;
        AppelAPI.delete(url, token, context, callback);
    }
}