package fr.iutrodez.a4awalk.DetailleParcour;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Représente un parcours (Course) avec départ, arrivée, date et chemin.
 */
public class Course {

    private String id;
    @SerializedName("hikeId")
    private int hikeId;
    @SerializedName("dateRealisation")
    private String dateRealisation;
    private Location depart;
    private Location arrivee;
    private List<Point> path;
    private boolean finished;
    private boolean paused;

    /**
     * Retourne l'identifiant du parcours.
     *
     * @return Identifiant du parcours
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne l'identifiant de la randonnée associée.
     *
     * @return Identifiant hike
     */
    public int getHikeId() {
        return hikeId;
    }

    /**
     * Retourne la date de réalisation du parcours au format ISO.
     *
     * @return Date de réalisation
     */
    public String getDateRealisation() {
        return dateRealisation;
    }

    /**
     * Retourne le point de départ du parcours.
     *
     * @return Location départ
     */
    public Location getDepart() {
        return depart;
    }

    /**
     * Retourne le point d'arrivée du parcours.
     *
     * @return Location arrivée
     */
    public Location getArrivee() {
        return arrivee;
    }

    /**
     * Retourne la liste des points constituant le chemin du parcours.
     *
     * @return Liste de Point
     */
    public List<Point> getPath() {
        return path;
    }

    /**
     * Indique si le parcours est terminé.
     *
     * @return true si terminé, false sinon
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Indique si le parcours est en pause.
     *
     * @return true si en pause, false sinon
     */
    public boolean isPaused() {
        return paused;
    }
}
