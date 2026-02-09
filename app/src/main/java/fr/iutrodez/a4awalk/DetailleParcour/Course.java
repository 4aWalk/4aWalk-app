package fr.iutrodez.a4awalk.DetailleParcour;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    // Getters
    public String getId() { return id; }
    public int getHikeId() { return hikeId; }
    public String getDateRealisation() { return dateRealisation; }
    public Location getDepart() { return depart; }
    public Location getArrivee() { return arrivee; }
    public List<Point> getPath() { return path; }
    public boolean isFinished() { return finished; }
    public boolean isPaused() { return paused; }
}
