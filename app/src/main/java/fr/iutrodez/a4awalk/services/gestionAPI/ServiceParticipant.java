package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Backpack;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParticipant {

    private final static String BASE_URL = "http://98.94.8.220:8080";
    private final static String URL_BASE_PARTICIPANT = BASE_URL + "/hikes/%d/participants";
    private final static String URL_MODIF_PARTICIPANT = BASE_URL + "/hikes/%d/participants/%d";

    public static void supprimerParticipantAPI(Context context, String token, int hikeId, int participantId, AppelAPI.VolleyObjectCallback callback) {
        if (participantId == 0) return;
        String url = String.format(URL_MODIF_PARTICIPANT, hikeId, participantId);
        AppelAPI.delete(url, token, context, callback);
    }

    public static void traiterMAJParticipants(Context contexte, int hikeId, ArrayList<Participant> listeTemporaireParticipants, ArrayList<Participant> participantsOriginaux, TokenManager tokenManager) {
        // Création du callback silencieux (gère onSuccess et onError pour éviter l'erreur de compilation)
        AppelAPI.VolleyObjectCallback silentCallback = new AppelAPI.VolleyObjectCallback() {
            @Override public void onSuccess(JSONObject result) {}
            @Override public void onError(VolleyError error) {}
        };

        for (Participant p1 : listeTemporaireParticipants) {
            JSONObject body = buildParticipantJSON(p1);
            if (body == null) continue;

            int id = p1.getId();
            if (id == 0) {
                Log.i("Création", p1.toString() + p1.getId() + ", " + p1.getNiveau() + ", " + p1.getMorphologie() + ", " + p1.getBesoinEauLitre() + ", " +  p1.getBesoinKcal() + ", " + p1.getCapaciteEmportMaxKg());
                AppelAPI.post(BASE_URL + "/hikes/" + hikeId + "/participants", tokenManager.getToken(), body, contexte, silentCallback);
            } else {
                Log.i("modif", p1.toString());
                AppelAPI.put(BASE_URL + "/hikes/" + hikeId + "/participants/" + id, tokenManager.getToken(), body, contexte, silentCallback);
            }
        }

        for (Participant pOrigin : participantsOriginaux) {
            boolean present = false;
            for (Participant pTemp : listeTemporaireParticipants) {
                if (pTemp.getId() != 0 && pTemp.getId() == pOrigin.getId()) {
                    present = true; break;
                }
            }
            if (!present && !pOrigin.getCreator()) {
                supprimerParticipantAPI(contexte, tokenManager.getToken(), hikeId, pOrigin.getId(), silentCallback);
            }
        }
    }

    /**
     * Extrait la liste des participants depuis la réponse globale.
     */
    public static ArrayList<Participant> extractParticipants(JSONObject response) {
        ArrayList<Participant> participants = new ArrayList<>();
        JSONArray partsJson = response.optJSONArray("participants");
        int fallbackCount = response.optInt("nbParticipants", response.optInt("participants", 0));

        try {
            if (partsJson != null && partsJson.length() > 0) {
                for (int i = 0; i < partsJson.length(); i++) {
                    participants.add(parseParticipant(partsJson.getJSONObject(i)));
                }
            } else if (fallbackCount > 0) {
                for (int i = 0; i < fallbackCount; i++) {
                    participants.add(new Participant());
                }
            }
        } catch (JSONException e) {
            Log.e("ServiceParticipant", "Erreur parsing liste Participants");
        }
        return participants;
    }

    private static JSONObject buildParticipantJSON(Participant participant) {
        try {
            JSONObject body = new JSONObject();
            if (participant.getNom() != null) body.put("nom", participant.getNom());
            if (participant.getPrenom() != null) body.put("prenom", participant.getPrenom());
            body.put("age", participant.getAge());
            body.put("niveau", participant.getNiveau().toString());
            body.put("morphologie", participant.getMorphologie().toString());
            body.put("besoinKcal", participant.getBesoinKcal());
            body.put("besoinEauLitre", participant.getBesoinEauLitre());
            if (participant.getCapaciteEmportMaxKg() != 0.0) {
                body.put("capaciteEmportMaxKg", participant.getCapaciteEmportMaxKg());
            } else {
                body.put("capaciteEmportMaxKg", 0);
            }
            return body;
        } catch (Exception e) {
            return null;
        }
    }

    // Dans fr.iutrodez.a4awalk.services.gestionAPI.ServiceParticipant

    /**
     * Parse un JSONObject pour en extraire un Participant
     */
    public static Participant parseParticipant(JSONObject obj) throws JSONException {
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

        JSONObject backpackJson = obj.optJSONObject("backpack");
        if (backpackJson != null) {
            Backpack backpack = new Backpack(p);
            backpack.setId(backpackJson.optInt("id"));
            backpack.setTotalMassKg(backpackJson.optDouble("poidsActuelKg", 0.0));

            backpack.setEquipmentItems(new ArrayList<>(
                    ServiceEquipment.extractEquipmentsForBackpack(
                            backpackJson.optJSONArray("equipements"))));

            backpack.setFoodItems(new ArrayList<>(
                    ServiceFoodProduct.extractFoodForBackpack(
                            backpackJson.optJSONArray("nourriture"))));

            p.setBackpack(backpack);
            Log.i("backpack", "equipements=" + backpack.getEquipmentItems().size()
                    + " nourriture=" + backpack.getFoodItems().size());
        }
            Log.i("backpack", p.getBackpack().getEquipmentItems().toString());
            Log.i("backpack", p.getBackpack().getFoodItems().toString());

        return p;
    }

    public static void getMyParticipants(Context context, String token, AppelAPI.VolleyCallback callback) {
        AppelAPI.get(BASE_URL + "/participants/my", token, context, callback);
    }
}