package com.example.mi.parkenamberg;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jeremy on 05.12.2016.
 */

public class Garage {
    //ID im XML-Dokument
    private int id;
    private LatLng location;
    public boolean closed = false;
    private boolean show;
    private boolean entered;
    private int trend;

    private String name;
    private int maxPlaetze;
    private int curPlaetze;

    int getCurPlaetze() {
        return curPlaetze;
    }

    void setCurPlaetze(int curPlaetze) {
        this.curPlaetze = curPlaetze;
    }

    int getMaxPlaetze() {
        return maxPlaetze;
    }

    void setMaxPlaetze(int maxPlaetze) {
        this.maxPlaetze = maxPlaetze;
    }

    Garage(LatLng l, String n, int id)
    {
        if(l != null) location = l;
        name = n;
        this.id = id;
        show = true;
        entered = false;
        trend = 2;
    }

    LatLng getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setShow(boolean s) { show = s; }

    public boolean getShow() { return show; }

    public boolean getEntered() { return entered; }

    public void setEntered(boolean e) { entered = e; }

    public int getTrend() { return trend; }

    public void setTrend(int t) { trend = t; }
}
