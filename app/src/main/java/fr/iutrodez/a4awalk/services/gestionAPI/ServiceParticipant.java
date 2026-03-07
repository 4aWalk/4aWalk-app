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

import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.modeles.entites.TokenManager;
import fr.iutrodez.a4awalk.modeles.enums.Level;
import fr.iutrodez.a4awalk.modeles.enums.Morphology;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParticipant {

    private final static String BASE_URL = "http://98.94.8.220:8080";

    private final static String URL_BASE_PARTICIPANT = "http://98.94.8.220:8080/hikes/%d/participants";
    private final static String URL_MODIF_PARTICIPANT = "http://98.94.8.220:8080/hikes/%d/participants/%d";

    public static Participant creationParticipant(int age, Level choixNiveau,
                                                  Morphology choixMorpho, Integer kcal, Integer eau,
                                                  double capacite) {
        return new Participant(null, null, age, choixNiveau, choixMorpho, false, kcal, eau, capacite, 0);
    }

    /**
     * Modifie un participant existant via API (PUT)
     */
    public static void modifierParticipantAPI(Context context, String token, Participant participant, Runnable onSuccess) {
        if (participant.getId() == 0) {
            Toast.makeText(context, "Erreur : ID manquant pour la modification", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = buildParticipantJSON(participant);
        if (body == null) return;

        String url = String.format(URL_MODIF_PARTICIPANT, participant.getIdRando(), participant.getId());

        AppelAPI.put(url, token, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Toast.makeText(context, "Participant modifié !", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(context, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
    }

    /**
     * Supprime un participant existant via API (DELETE)
     */
    public static void supprimerParticipantAPI(Context context, String token, int hikeId, int participantId, Runnable onSuccess) {
        if (participantId == 0) {
            Toast.makeText(context, "Erreur : ID manquant pour la suppression", Toast.LENGTH_SHORT).show();
            return;
        }

        // On utilise la même constante d'URL car le format est identique : /hikes/{hikeId}/participants/{participantId}
        String url = String.format(URL_MODIF_PARTICIPANT, hikeId, participantId);

        AppelAPI.delete(url, token, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (onSuccess != null) onSuccess.run();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(context, "Erreur lors de la suppression API", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
    }

    public static void ajoutParticipants(Context context, String token, List<Participant> participant, Long idRandonnee) {
        for(Participant participant1 : participant) {
            JSONObject body = buildParticipantJSON(participant1);
            addParticipantAPI(
                    context,
                    idRandonnee,
                    body,
                    token,
                    response -> Toast.makeText(context, "Participant ajouté !", Toast.LENGTH_LONG).show(),
                    error -> Toast.makeText(context, "Erreur : " + error.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private static JSONObject buildParticipantJSON(Participant participant) {
        try {
            JSONObject body = new JSONObject();
            // Ajout Nom/Prenom si présents
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

    public static void addParticipantAPI(
            Context context,
            Long hikeId,
            JSONObject body,
            String token,
            Response.Listener<JSONObject> success,
            Response.ErrorListener error
    ) {
        String url = String.format(URL_BASE_PARTICIPANT, hikeId);
        AppelAPI.post(url, token, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (success != null) success.onResponse(result);
            }

            @Override
            public void onError(VolleyError volleyError) {
                if (error != null) error.onErrorResponse(volleyError);
            }
        });


    }

    public static void traiterMAJParticipants(Context contexte, int hikeId, ArrayList<Participant> listeTemporaireParticipants, ArrayList<Participant> participantsOriginaux, TokenManager tokenManager) {
        AppelAPI.VolleyObjectCallback silentCallback = new AppelAPI.VolleyObjectCallback() {
            @Override public void onSuccess(JSONObject result) {}
            @Override public void onError(VolleyError error) {}
        };

        for (Participant p1 : listeTemporaireParticipants) {
            JSONObject body = new JSONObject();
            try {
                body.put("nom", p1.getNom());
                body.put("prenom", p1.getPrenom());
                body.put("age", p1.getAge());
                body.put("niveau", p1.getNiveau());
                body.put("morphologie", p1.getMorphologie());
                body.put("besoinKcal", p1.getBesoinKcal());
                body.put("besoinEauLitre", p1.getBesoinEauLitre());
                double cap = (p1.getCapaciteEmportMaxKg() != 0.0) ? p1.getCapaciteEmportMaxKg() : 0.0;
                body.put("capaciteEmportMaxKg", cap);
            } catch (JSONException e) { continue; }

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
                    present = true;
                    break;
                }
            }
            if (!present) {
                Log.i("Participant supprimé", pOrigin.getId() + pOrigin.getPrenom());
                ServiceParticipant.supprimerParticipantAPI(contexte, tokenManager.getToken(), hikeId, pOrigin.getId(), () -> {
                    Log.i("SUPPRESSION", "Participant " + pOrigin.getId() + " supprimé avec succès.");
                });
            }
        }
    }
}