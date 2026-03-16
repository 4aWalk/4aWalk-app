package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.EquipmentItem;
import fr.iutrodez.a4awalk.modeles.enums.TypeEquipment;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceEquipment {

    private static final String URL_EQUIPMENTS = "http://98.94.8.220:8080/equipments"; // À adapter si besoin

    /**
     * Parse les équipements depuis les groupes de la réponse JSON.
     */
    public static List<EquipmentItem> extractEquipmentGroups(JSONObject response) {
        List<EquipmentItem> equipList = new ArrayList<>();
        JSONObject groupsJson = response.optJSONObject("equipmentGroups");

        if (groupsJson != null) {
            try {
                Iterator<String> keys = groupsJson.keys();
                while (keys.hasNext()) {
                    String categoryKey = keys.next();
                    JSONObject categoryObj = groupsJson.optJSONObject(categoryKey);
                    if (categoryObj != null) {
                        JSONArray itemsArray = categoryObj.optJSONArray("items");
                        if (itemsArray != null) {
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject e = itemsArray.getJSONObject(i);
                                equipList.add(constructEqFromJson(e));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("ServiceEquipment", "Erreur parsing des équipements : " + e.getMessage());
            }
        }
        return equipList;
    }

    /**
     * Alias utilisé dans ServiceRandonnee pour maintenir la compatibilité.
     */
    public static List<EquipmentItem> extractEquipmentCatalogue(JSONObject response) {
        return extractEquipmentGroups(response);
    }

    /**
     * Construit un objet EquipmentItem depuis un JSONObject (mutualisation du code).
     */
    public static EquipmentItem constructEqFromJson(JSONObject obj) throws JSONException {
        EquipmentItem item = new EquipmentItem();
        item.setId(obj.optInt("id", 0));
        item.setNom(obj.optString("nom", ""));
        item.setDescription(obj.optString("description", ""));
        item.setMasseGrammes(obj.optDouble("masseGrammes", 0.0));
        item.setNbItem(obj.optInt("nbItem", 1));
        item.setMasseAVide(obj.optDouble("masseAVide", 0.0));

        try {
            item.setType(TypeEquipment.valueOf(obj.optString("type", "AUTRE")));
        } catch (IllegalArgumentException ex) {
            item.setType(TypeEquipment.AUTRE); // Sécurité si le type renvoyé par l'API est inconnu
        }
        return item;
    }

    /**
     * Récupère tout le catalogue d'équipements depuis l'API.
     */
    public static void getAllEquipments(Context context, String token, AppelAPI.VolleyCallback callback) {
        AppelAPI.get(URL_EQUIPMENTS, token, context, callback);
    }

    /**
     * Création d'un nouvel équipement via l'API.
     */
    public static void creerNouveauEquipement(Context context, String token, String nom, double masse, String description, TypeEquipment type, double masseAVide, int nbItem, AppelAPI.VolleyObjectCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("nom", nom);
            body.put("masseGrammes", masse);
            body.put("description", description);
            body.put("type", type.name());
            body.put("masseAVide", masseAVide);
            body.put("nbItem", nbItem);
        } catch (JSONException e) {
            Log.e("ServiceEquipment", "Erreur JSON création équipement", e);
        }
        AppelAPI.post(URL_EQUIPMENTS, token, body, context, callback);
    }

    /**
     * Synchronise la liste des équipements d'une randonnée avec l'API.
     */
    public static void synchroniserEquipments(Context context, String token, int hikeId,
                                              List<EquipmentItem> listeInitiale,
                                              List<EquipmentItem> listeModifiee) { // <-- Le changement est ici (List au lieu de ArrayList)

        // Exemple d'URL (à adapter selon ta route API exacte)
        String urlSync = "http://98.94.8.220:8080/hikes/" + hikeId + "/equipments";

        // Création du tableau JSON contenant les IDs ou les objets modifiés
        JSONArray bodyArray = new JSONArray();
        try {
            for (EquipmentItem item : listeModifiee) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                // Ajoute d'autres champs si ton API l'exige pour la synchronisation
                bodyArray.put(obj);
            }
        } catch (JSONException e) {
            Log.e("ServiceEquipment", "Erreur JSON lors de la synchro", e);
        }

        // Appel PUT (ou POST selon ton API) pour mettre à jour la liste dans la rando
        AppelAPI.putA(urlSync, token, bodyArray, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("ServiceEquipment", "Synchronisation des équipements réussie.");
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("ServiceEquipment", "Erreur lors de la synchronisation", error);
            }
        });
    }
}