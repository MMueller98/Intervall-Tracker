package com.trier.intervalltracker;

public class LonLatLocation {
    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    private double lon, lat;

    public LonLatLocation(double lon, double lat){
        this.lat = lat;
        this.lon = lon;
    }
}
