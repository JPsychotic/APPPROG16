package com.example.mi.parkenamberg;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jeremy on 05.12.2016.
 */

/**
 * Class for the garage
 */
public class Garage {
    /**
     * Id of the garage in the xml document
     */
    private int id;
    /**
     * Location of the garage
     */
    private LatLng location;
    /**
     * Variable to check if this garage is opened or closed
     */
    public boolean closed = false;
    /**
     * Check if Garage should be shown on the map
     */
    private boolean show;
    /**
     * Check if the geofence of this garage is entered
     */
    private boolean entered;
    /**
     * Trend of this garage
     */
    private int trend;

    /**
     * Name of the garage
     */
    private String name;
    /**
     * max. count of parking lots
     */
    private int maxPlaetze;
    /**
     * current count of used parking lots
     */
    private int curPlaetze;

    /**
     * Getter for curPlaetze
     * @return current count of used parking lots
     */
    int getCurPlaetze() {
        return curPlaetze;
    }

    /**
     * Setter for curPlaetze
     * @param curPlaetze new count
     */
    void setCurPlaetze(int curPlaetze) {
        this.curPlaetze = curPlaetze;
    }

    /**
     * Getter for maxPlaetze
     * @return count of max parking lots
     */
    int getMaxPlaetze() {
        return maxPlaetze;
    }

    /**
     * Setter for maxPlaetze
     * @param maxPlaetze the new count
     */
    void setMaxPlaetze(int maxPlaetze) {
        this.maxPlaetze = maxPlaetze;
    }

    /**
     * Constructor
     * @param l location
     * @param n name
     * @param id id
     */
    Garage(LatLng l, String n, int id)
    {
        if(l != null) location = l;
        name = n;
        this.id = id;
        show = true;
        entered = false;
        trend = 2;
    }

    /**
     * Getter for the location
     * @return the location
     */
    LatLng getLocation() {
        return location;
    }

    /**
     * Getter for the name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the id
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for the show-variable
     * @param s new show-Status
     */
    public void setShow(boolean s) { show = s; }

    /**
     * Getter for the show-Variable
     * @return the current show-Status
     */
    public boolean getShow() { return show; }

    /**
     * Getter for the entered-Status
     * @return the entered-Status
     */
    public boolean getEntered() { return entered; }

    /**
     * Setter for the entered-Status
     * @param e the entered-Status
     */
    public void setEntered(boolean e) { entered = e; }

    /**
     * Getter for the trend
     * @return the trend
     */
    public int getTrend() { return trend; }

    /**
     * Setter for the trend
     * @param t the trend
     */
    public void setTrend(int t) { trend = t; }
}
