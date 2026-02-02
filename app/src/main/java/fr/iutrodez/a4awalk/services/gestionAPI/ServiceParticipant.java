package fr.iutrodez.a4awalk.services.gestionAPI;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import fr.iutrodez.a4awalk.modeles.entites.Participant;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParticipant {

    private final static String URL_AJOUT_PARTICIPANT = "http://98.94.8.220:8080/hikes/%d/participants";

    public static JSONObject ajoutParticipantUI(Context context, String token, int age, String choixNiveau,
                                                String choixMorpho, Integer kcal, Integer eau,
                                                double capacite) {
        try {
            JSONObject json = new JSONObject();
            json.put("age", age);
            json.put("niveau", choixNiveau);
            json.put("morphologie", choixMorpho);

            json.put("besoinKcal", kcal);
            json.put("besoinEauLitre", eau);
            if (capacite != 0.0) json.put("capaciteEmportMaxKg", capacite);

            return json;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erreur JSON", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public static void ajoutParticipantAPI(Context context, String token, Participant participant, Long idRandonnee) {
        JSONObject body = new JSONObject();
        try {
            body.put("age", participant.getAge());
            body.put("niveau", participant.getNiveau().toString());
            body.put("morphologie", participant.getMorphologie().toString());

            body.put("besoinKcal", participant.getBesoinKcal());
            body.put("besoinEauLitre", participant.getBesoinEauLitre());
            if (participant.getCapaciteEmportMaxKg() != 0.0) {
                body.put("capaciteEmportMaxKg", participant.getCapaciteEmportMaxKg());
            }
            addParticipant(
                    context,
                    idRandonnee,
                    body,
                    token,
                    response -> {
                        Toast.makeText(context, "Participant ajouté !", Toast.LENGTH_LONG).show();
                    },
                    error -> {
                        Toast.makeText(context, "Erreur : " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erreur JSON", Toast.LENGTH_SHORT).show();
        }
    }

    public static void addParticipant(
            Context context,
            Long hikeId,
            JSONObject body,
            String token,
            Response.Listener<JSONObject> success,
            Response.ErrorListener error
    ) {
        // Préparation de l'URL
        String url = String.format(URL_AJOUT_PARTICIPANT, hikeId);

        // Appel via la classe utilitaire
        AppelAPI.post(url, token, body, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                // On transmet la réponse au listener d'origine
                if (success != null) {
                    success.onResponse(result);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                // On transmet l'erreur au listener d'origine
                if (error != null) {
                    error.onErrorResponse(volleyError);
                }
            }
        });
    }
}
