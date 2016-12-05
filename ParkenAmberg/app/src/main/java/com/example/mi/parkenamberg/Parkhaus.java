package com.example.mi.parkenamberg;

/**
 * Created by Jeremy on 05.12.2016.
 */

public class Parkhaus {
    private double longitude;
    private double latitude;
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

    public Parkhaus(double lat, double lon, String n, int maxPl) {
        longitude = lon;
        latitude = lat;
        String name = n;
        maxPlaetze = maxPl;
    }
}
