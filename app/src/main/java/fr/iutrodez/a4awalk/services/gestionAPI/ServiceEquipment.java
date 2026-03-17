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
     * Lie un équipement existant à une Randonnée.
     */
    public static void lierEquipmentARandonnee(Context context, String token, int hikeId, int equipId, Integer ownerId, AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/equipment/" + equipId;

        // Ajout du query parameter si ownerId n'est pas null
        if (ownerId != null) {
            url += "?owner=" + ownerId;
        }

        AppelAPI.post(url, token, null, context, callback);
    }

    /**
     * Retire un équipement d'une Randonnée.
     */
    public static void retirerEquipmentDeRandonnee(Context context, String token, int hikeId, int equipId, AppelAPI.VolleyObjectCallback callback) {
        String url = "http://98.94.8.220:8080/hikes/" + hikeId + "/equipment/" + equipId;
        // On effectue la requête DELETE
        // Attention : Vérifie le nom exact de ta méthode delete dans ta classe AppelAPI (ça peut être delete, deleteObject, etc.)
        AppelAPI.delete(url, token, context, callback);
    }

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

            item.setOwnerId(obj.getInt("ownerId"));
        } else {
            item.setOwnerId(null);
        }

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
     * Compare la liste initiale avec la liste modifiée et effectue les appels API (POST/DELETE) nécessaires.
     */
    public static void synchroniserEquipments(Context context, String token, int hikeId,
                                              List<EquipmentItem> listeInitiale,
                                              List<EquipmentItem> listeModifiee) {

        // 1. Trouver les AJOUTS (présents dans listeModifiee, absents dans listeInitiale)
        for (EquipmentItem temp : listeModifiee) {
            boolean existeDeja = false;
            for (EquipmentItem orig : listeInitiale) {
                if (temp.getId() == orig.getId()) {
                    existeDeja = true;
                    break;
                }
            }
            if (!existeDeja) {
                // L'équipement a été ajouté, on fait le POST
                lierEquipmentARandonnee(context, token, hikeId, temp.getId(), temp.getOwnerId(), new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceEquipment", "Équipement " + temp.getId() + " lié avec succès.");
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("ServiceEquipment", "Erreur lors de la liaison de l'équipement " + temp.getId());
                    }
                });
            }
        }

        // 2. Trouver les SUPPRESSIONS (présents dans listeInitiale, absents dans listeModifiee)
        for (EquipmentItem orig : listeInitiale) {
            boolean estConserve = false;
            for (EquipmentItem temp : listeModifiee) {
                if (orig.getId() == temp.getId()) {
                    estConserve = true;
                    break;
                }
            }
            if (!estConserve) {
                // L'équipement a été retiré, on fait le DELETE
                retirerEquipmentDeRandonnee(context, token, hikeId, orig.getId(), new AppelAPI.VolleyObjectCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("ServiceEquipment", "Équipement " + orig.getId() + " retiré avec succès.");
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Gestion du cas où le DELETE renvoie un 204 No Content
                        if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                            Log.i("ServiceEquipment", "Équipement " + orig.getId() + " retiré avec succès (204).");
                        } else {
                            Log.e("ServiceEquipment", "Erreur lors du retrait de l'équipement " + orig.getId());
                        }
                    }
                });
            }
        }
    }
}