package fr.iutrodez.a4awalk.services.gestionAPI;

import static fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI.parsePOI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.modeles.entites.PointOfInterest;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParcours {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    public static void terminerParcours(Context context, String id, String token) {
        String url = BASE_URL + "/courses/" + id + "/finish";
        AppelAPI.put(url, token, null, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {

            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Interface pour renvoyer le résultat au Fragment concernant la récupération des parcours
     */
    public interface ParcoursCallback {
        void onSuccess(ArrayList<Course> parcours);
        void onError(VolleyError error);
    }

    /**
     * Interface pour renvoyer le résultat de la création de la course
     */
    public interface CourseCreationCallback {
        void onSuccess(Course course); // CHANGEMENT : On renvoie l'objet Course complet
        void onError(VolleyError error);
    }

    /**
     * Lance une nouvelle course en envoyant la position actuelle et une position proche.
     * Utilise la classe utilitaire AppelAPI.
     */
    public static void demarrerCourse(Context context, String token, int hikeId, double latitude, double longitude, CourseCreationCallback callback) {
        String url = BASE_URL + "/courses";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("hikeId", hikeId);

            JSONArray pathArray = new JSONArray();

            // Point 1 : Position actuelle
            JSONObject point1 = new JSONObject();
            point1.put("latitude", latitude);
            point1.put("longitude", longitude);

            // Point 2 : Position quasiment à côté (+ 0.0001 degré)
            JSONObject point2 = new JSONObject();
            point2.put("latitude", latitude + 0.0001);
            point2.put("longitude", longitude + 0.0001);

            pathArray.put(point1);
            pathArray.put(point2);

            requestBody.put("path", pathArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Appel API centralisé via la méthode POST de votre utilitaire
        AppelAPI.post(url, token, requestBody, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // On crée l'objet Course à partir du JSON complet renvoyé par l'API
                Course course = createCourse(result);

                // On renvoie l'objet entier au callback
                callback.onSuccess(course);
            }

            @Override
            public void onError(VolleyError error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Récupère les randonnées depuis l'API, les transforme en objets et notifie le callback.
     */
    public static void recupererParcoursUtilisateur(Context context, String token, ParcoursCallback callback) {

        AppelAPI.get(BASE_URL + "/courses/my", token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                ArrayList<Course> listeParcours = parseCoursesFromJSON(result);
                callback.onSuccess(listeParcours);
            }

            @Override
            public void onError(VolleyError erreur) {
                callback.onError(erreur);
            }
        });
    }

    /**
     * Met à jour le statut du parcours (pause ou reprise).
     */
    public static void changerStatutPause(Context context, String token, String courseId, boolean isPaused, AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/courses/" + courseId + "/state";

        // On utilise la méthode PUT existante. Si l'API nécessite le booléen isPaused dans le body,
        // il faudra remplacer 'null' par un JSONObject contenant l'état.
        AppelAPI.put(url, token, null, context, callback);
    }

    /**
     * Méthode interne pour parser le JSON (Private car utilisée uniquement ici)
     */
    public static ArrayList<Course> parseCoursesFromJSON(JSONArray jsonArray) {
        ArrayList<Course> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Course course = createCourse(jsonArray.getJSONObject(i));
                liste.add(course);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public static Course createCourse(JSONObject courseJson) throws JSONException {
        Course course = new Course();

        // --- CHAMPS SIMPLES ---
        // "id" est un String dans le JSON de Course (Mongo ID)
        course.setId(courseJson.getString("id"));

        // "hikeId" est un entier/long
        course.setHikeId(courseJson.getInt("hikeId"));

        // "finished" et "paused"
        course.setFinished(courseJson.getBoolean("isFinished"));
        course.setPaused(courseJson.getBoolean("isPaused"));

        // --- DATE ---
        // Parsing de la date ISO-8601 ("2026-02-05T08:19:12.027")
        String dateStr = courseJson.optString("dateRealisation", null);
        if (dateStr != null && !dateStr.isEmpty()) {
            course.setDateRealisation(LocalDateTime.parse(dateStr));
        }

        // --- DEPART & ARRIVEE (Gestion du null) ---
        // Dans ton JSON, ils sont null, donc on vérifie avec isNull()
        if (!courseJson.isNull("depart")) {
            course.setDepart(parsePOI(courseJson.getJSONObject("depart")));
        }

        if (!courseJson.isNull("arrivee")) {
            course.setArrivee(parsePOI(courseJson.getJSONObject("arrivee")));
        }

        // --- PATH (Liste de GeoCoordinate) ---
        List<GeoCoordinate> pathList = new ArrayList<>();
        JSONArray pathArray = courseJson.optJSONArray("path");

        if (pathArray != null) {
            for (int j = 0; j < pathArray.length(); j++) {
                JSONObject pointJson = pathArray.getJSONObject(j);

                double lat = pointJson.getDouble("latitude");
                double lon = pointJson.getDouble("longitude");

                pathList.add(new GeoCoordinate(lat, lon));
            }
        }
        course.setTrajetsRealises(pathList);
        return course;
    }
}