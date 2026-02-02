package fr.iutrodez.a4awalk.GestionP.Activity.Service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class ParticipantService {

    private static final String BASE_URL = "http://98.94.8.220:8080/";

    private Context context;

    public ParticipantService(Context context) {
        this.context = context;
    }

    public void addParticipant(
            int hikeId,
            JSONObject body,
            String token,
            Response.Listener<JSONObject> success,
            Response.ErrorListener error
    ) {

        String url = BASE_URL + "hikes/" + hikeId + "/participants";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                success,
                error
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
