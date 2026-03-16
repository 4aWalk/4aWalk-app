package fr.iutrodez.a4awalk.services.gestionAPI;

import static fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI.parsePOI;

import android.content.Context;
import android.util.Log;

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

    private static final String URL_RANDOS = "http://98.94.8.220:8080/courses/my";

    /**
     * Interface pour renvoyer le résultat au Fragment
     */
    public interface ParcoursCallback {
        void onSuccess(ArrayList<Course> parcours);
        void onError(VolleyError error);
    }

    /**
     * Récupère les randonnées depuis l'API, les transforme en objets et notifie le callback.
     */
    public static void recupererParcoursUtilisateur(Context context, String token, ParcoursCallback callback) {

        AppelAPI.get(URL_RANDOS, token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                // On transforme le JSON en objets Java
                ArrayList<Course> listeParcours = parseCoursesFromJSON(result);
                callback.onSuccess(listeParcours);
            }

            @Override
            public void onError(VolleyError erreur) {
                // On transmet l'erreur au Fragment
                callback.onError(erreur);
            }
        });
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
        course.setDateRealisation(LocalDateTime.parse(dateStr));

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