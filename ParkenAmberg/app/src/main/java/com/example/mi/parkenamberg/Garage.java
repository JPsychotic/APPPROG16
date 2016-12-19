package com.example.mi.parkenamberg;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jeremy on 05.12.2016.
 */

public class Garage {
    //ID im XML-Dokument
    private int id;
    private LatLng location;
    //Montag - Freitag Öffnungszeiten
    private int MoFrOeffnung;
    private int MoFrSchliess;
    //Samstag Öffnungszeiten
    private int SaOeffnung;
    private int SaSchliess;
    //Sonntag Öffnungszeiten
    private int SoSchliess;
    private int SoOeffnung;

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

    Garage(LatLng l, String n, int id) {
        if(l != null) location = l;
        name = n;
        this.id = id;
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


}
