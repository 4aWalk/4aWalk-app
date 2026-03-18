package fr.iutrodez.a4awalk.modeles.entites;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private String id;

    private int hikeId;

    private LocalDateTime dateRealisation;

    private PointOfInterest depart;

    private PointOfInterest arrivee;

    private boolean isFinished;

    private boolean isPaused;

    private List<GeoCoordinate> trajetsRealises;

    public Course() {
        this.trajetsRealises = new ArrayList<>();
        this.dateRealisation = LocalDateTime.now();
        this.isFinished = false;
        this.isPaused = false;
    }

    public Course(int hikeId, PointOfInterest depart) {
        this();
        this.hikeId = hikeId;
        this.depart = depart;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getHikeId() { return hikeId; }
    public void setHikeId(int hikeId) { this.hikeId = hikeId; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public PointOfInterest getDepart() { return depart; }
    public void setDepart(PointOfInterest depart) { this.depart = depart; }

    public PointOfInterest getArrivee() { return arrivee; }
    public void setArrivee(PointOfInterest arrivee) { this.arrivee = arrivee; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { this.isFinished = finished; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { this.isPaused = paused; }

    public List<GeoCoordinate> getTrajetsRealises() { return trajetsRealises; }
    public void setTrajetsRealises(List<GeoCoordinate> trajetsRealises) { this.trajetsRealises = trajetsRealises; }
}