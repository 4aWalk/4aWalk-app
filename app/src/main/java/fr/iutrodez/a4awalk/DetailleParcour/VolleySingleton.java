package fr.iutrodez.a4awalk.DetailleParcour;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton pour gérer une unique instance de RequestQueue Volley dans l'application.
 * Permet d'ajouter facilement des requêtes réseau depuis n'importe quelle activité ou service.
 */
public class VolleySingleton {

    /** Instance unique du singleton */
    private static VolleySingleton instance;

    /** Queue de requêtes Volley */
    private RequestQueue requestQueue;

    /** Contexte application utilisé pour initialiser Volley */
    private static Context ctx;

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     *
     * @param context Contexte de l'application
     */
    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    /**
     * Retourne l'instance unique de VolleySingleton.
     * Crée l'instance si elle n'existe pas encore.
     *
     * @param context Contexte de l'application
     * @return Instance unique de VolleySingleton
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    /**
     * Retourne la RequestQueue unique de l'application.
     * Crée la queue si elle n'existe pas encore.
     *
     * @return RequestQueue de Volley
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Ajoute une requête à la RequestQueue pour exécution.
     *
     * @param req Requête Volley à exécuter
     * @param <T> Type de la réponse attendue
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
