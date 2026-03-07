package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParticipant {

    private final static String BASE_URL = "http://98.94.8.220:8080";
    private final static String URL_BASE_PARTICIPANT = BASE_URL + "/hikes/%d/participants";
    private final static String URL_MODIF_PARTICIPANT = BASE_URL + "/hikes/%d/participants/%d";

    public static void supprimerParticipantAPI(Context context, String token, int hikeId, int participantId, Runnable onSuccess) {
        if (participantId == 0) return;
        String url = String.format(URL_MODIF_PARTICIPANT, hikeId, participantId);
        AppelAPI.delete(url, token, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
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
            }
            return body;
        } catch (Exception e) {
            return null;
        }
    }

    public static void traiterMAJParticipants(Context contexte, int hikeId, ArrayList<Participant> listeTemporaireParticipants, ArrayList<Participant> participantsOriginaux, TokenManager tokenManager) {
        AppelAPI.VolleyObjectCallback silentCallback = new AppelAPI.VolleyObjectCallback() {
            @Override public void onSuccess(JSONObject result) {}
            @Override public void onError(VolleyError error) {}
        };

        for (Participant p1 : listeTemporaireParticipants) {
            JSONObject body = buildParticipantJSON(p1);
            if (body == null) continue;

            int id = p1.getId();
            if (id == 0) {
                AppelAPI.post(BASE_URL + "/hikes/" + hikeId + "/participants", tokenManager.getToken(), body, contexte, silentCallback);
            } else {
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
            if (!present) {
                supprimerParticipantAPI(contexte, tokenManager.getToken(), hikeId, pOrigin.getId(), () -> {});
            }
        }
    }
}