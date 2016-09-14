package com.example.polipo.semanticapp.model;

import java.io.Serializable;

/**
 * Created by polipo on 20.04.16.
 */
public class GeoCoordinates implements Serializable {
    /**
     * latitude.
     */
    private double lat; //51.123

    /**
     * longitude.
     */
    private double lon; //12.123

    /**
     * Constructor.
     * @param lat Latitude
     * @param lon Longitude
     */
    public GeoCoordinates(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * get Latitude.
     * @return Latitude
     */
    public final double getLat() {
        return lat;
    }

    /**
     * get Longitude.
     * @return Longitude
     */
    public final double getLon() {
        return lon;
    }

    /**
     * print out.
     * @return values
     */
    public final String toString() {
        return "" + lat + "," + lon + "";
    }
}
