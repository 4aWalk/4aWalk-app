package fr.iutrodez.a4awalk.Request;

import org.json.JSONException;
import org.json.JSONObject;

public class ParticipantRequest {

    public static JSONObject build(
            int age,
            String niveau,
            String morphologie,
            Integer kcal,
            Integer eau,
            Double capacite
    ) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("age", age);
        json.put("niveau", niveau);
        json.put("morphologie", morphologie);

        if (kcal != null) json.put("besoinKcal", kcal);
        if (eau != null) json.put("besoinEauLitre", eau);
        if (capacite != null) json.put("capaciteEmportMaxKg", capacite);

        return json;
    }
}
