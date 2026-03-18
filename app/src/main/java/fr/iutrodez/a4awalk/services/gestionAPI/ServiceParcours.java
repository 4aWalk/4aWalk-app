package fr.iutrodez.a4awalk.services.gestionAPI;

import static fr.iutrodez.a4awalk.services.gestionAPI.ServicePOI.parsePOI;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.a4awalk.modeles.entites.Course;
import fr.iutrodez.a4awalk.modeles.entites.GeoCoordinate;
import fr.iutrodez.a4awalk.services.AppelAPI;

public class ServiceParcours {

    private static final String BASE_URL = "http://98.94.8.220:8080";

    // =========================================================
    // ====================== INTERFACES =======================
    // =========================================================

    public interface ParcoursCallback {
        void onSuccess(ArrayList<Course> parcours);
        void onError(VolleyError error);
    }

    public interface CourseCreationCallback {
        void onSuccess(Course course);
        void onError(VolleyError error);
    }

    public interface CoursesCallback {
        void onSuccess(List<String> courseIds);
        void onError(VolleyError error);
    }

    // =========================================================
    // ====================== APPELS API =======================
    // =========================================================

    public static void terminerParcours(Context context, String token, String id) {
        String url = BASE_URL + "/courses/" + id + "/finish";
        AppelAPI.put(url, token, null, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                // rien à faire
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void demarrerCourse(Context context, String token, int hikeId,
                                      double latitude, double longitude,
                                      CourseCreationCallback callback) {
        String url = BASE_URL + "/courses";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("hikeId", hikeId);

            JSONArray pathArray = new JSONArray();

            JSONObject point1 = new JSONObject();
            point1.put("latitude", latitude);
            point1.put("longitude", longitude);

            JSONObject point2 = new JSONObject();
            point2.put("latitude", latitude + 0.0001);
            point2.put("longitude", longitude + 0.0001);

            pathArray.put(point1);
            pathArray.put(point2);

            requestBody.put("path", pathArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AppelAPI.post(url, token, requestBody, context, new AppelAPI.VolleyObjectCallback() {
            @Override
            public void onSuccess(JSONObject result) throws JSONException {
                callback.onSuccess(createCourse(result));
            }

            @Override
            public void onError(VolleyError error) {
                callback.onError(error);
            }
        });
    }

    public static void recupererParcoursUtilisateur(Context context, String token,
                                                    ParcoursCallback callback) {
        AppelAPI.get(BASE_URL + "/courses/my", token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                callback.onSuccess(parseCoursesFromJSON(result));
            }

            @Override
            public void onError(VolleyError erreur) {
                callback.onError(erreur);
            }
        });
    }

    public static void changerStatutPause(Context context, String token, String courseId,
                                          boolean isPaused,
                                          AppelAPI.VolleyObjectCallback callback) {
        String url = BASE_URL + "/courses/" + courseId + "/state";
        AppelAPI.put(url, token, null, context, callback);
    }

    /**
     * Récupère les IDs des courses liées à une randonnée donnée.
     * Filtre côté client depuis GET /courses/my.
     */
    public static void getCoursesLieesARandonnee(Context context, String token, int hikeId,
                                                 CoursesCallback callback) {
        AppelAPI.get(BASE_URL + "/courses/my", token, context, new AppelAPI.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                List<String> courseIds = new ArrayList<>();
                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject course = result.getJSONObject(i);
                        if (course.getInt("hikeId") == hikeId) {
                            courseIds.add(course.getString("id"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onSuccess(courseIds);
            }

            @Override
            public void onError(VolleyError error) {
                callback.onError(error);
            }
        });
    }

    // =========================================================
    // ====================== PARSING ==========================
    // =========================================================

    public static ArrayList<Course> parseCoursesFromJSON(JSONArray jsonArray) {
        ArrayList<Course> liste = new ArrayList<>();
        if (jsonArray == null) return liste;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                liste.add(createCourse(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public static Course createCourse(JSONObject courseJson) throws JSONException {
        Course course = new Course();

        course.setId(courseJson.getString("id"));
        course.setHikeId(courseJson.getInt("hikeId"));
        course.setFinished(courseJson.getBoolean("isFinished"));
        course.setPaused(courseJson.getBoolean("isPaused"));

        String dateStr = courseJson.optString("dateRealisation", null);
        if (dateStr != null && !dateStr.isEmpty()) {
            course.setDateRealisation(
                    LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        } else {
            course.setDateRealisation(null);
        }

        if (!courseJson.isNull("depart")) {
            course.setDepart(parsePOI(courseJson.getJSONObject("depart")));
        }

        if (!courseJson.isNull("arrivee")) {
            course.setArrivee(parsePOI(courseJson.getJSONObject("arrivee")));
        }

        List<GeoCoordinate> pathList = new ArrayList<>();
        JSONArray pathArray = courseJson.optJSONArray("path");
        if (pathArray != null) {
            for (int j = 0; j < pathArray.length(); j++) {
                JSONObject pointJson = pathArray.getJSONObject(j);
                pathList.add(new GeoCoordinate(
                        pointJson.getDouble("latitude"),
                        pointJson.getDouble("longitude")
                ));
            }
        }
        course.setTrajetsRealises(pathList);

        return course;
    }
}