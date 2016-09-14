package com.example.polipo.semanticapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by polipo on 21.04.16.
 */
public class Resource implements Serializable {
    /**
     * Resource URI.
     */
    private String subject;
    /**
     * Attributes List.
     */
    private ArrayList<Tuple> attributes;

    /**
     * Constructor.
     * @param subject Resource URI
     */
    public Resource(final String subject) {
        attributes = new ArrayList<Tuple>();
        this.subject = subject;
    }

    /**
     * Constructor.
     * @param subject Resource URI
     * @param attributes Attributes List
     */
    public Resource(final String subject, final ArrayList<Tuple> attributes) {
        this.subject = subject;
        this.attributes = attributes;
    }


    /**
     * Add a tuple to the Attributes list.
     * @param tuple the tuple you want to add
     * @return true if it works
     */

    public final boolean addAttribute(final Tuple tuple) {
        return attributes.add(tuple);

    }

    /**
     * get Subject.
     * @return Resource URI
     */
    public final String getSubject() {
        return subject;
    }


    /**
     * method to get the coordinates from an object.
     * iterates all tuples and look for wgs84_pos#
     * @return the Coordinates
     */

    public final GeoCoordinates getCoordinates() {
        Double lat = 0.0;
        Double lon = 0.0;

        boolean latExists = false;
        boolean lonExists = false;
        boolean geomExists = false;

        Iterator<Tuple> it = attributes.iterator();
        while (it.hasNext()) {
            Tuple tuple = it.next();

            if (tuple.getPredicate().contains("http://www.w3.org/2003/01/geo/wgs84_pos#lat")) {
                lat = new GeoTool().cutString(tuple.getObject());
                latExists = true;
            } else
            if (tuple.getPredicate().contains("http://www.w3.org/2003/01/geo/wgs84_pos#lon")) {
                lon = new GeoTool().cutString(tuple.getObject());
                lonExists = true;
            }

        }
        if (!(latExists && lonExists)) {
            //// TODO: 30.05.16


        }

        GeoCoordinates coordinates = new GeoCoordinates(lat, lon);

        return coordinates;
    }

    /**
     * get the Attributes list.
     * @return List of Attributes
     */
    public final ArrayList<Tuple> getAttributes() {
        return  attributes;
    }

    /**
     * set the Attributes List.
     * @param attributes the attributes
     */

    public final void setAttributes(final ArrayList<Tuple> attributes) {
        this.attributes = attributes;
    }

    /**
     * prints all Attributes to a String.
     * @return a String
     */

    public final String toString() {
        String results = "Subject:" + subject + "\n\n";
        if (!attributes.isEmpty()) {
            Iterator<Tuple> it = attributes.iterator();
            while (it.hasNext()) {
                Tuple tuple = it.next();
                results = results + tuple.toString() + "";
            }
        }
        return results;
    }

    /**
     * prints only selected attributes.
     * @return selected Information from a Resource
     */

    public final String thinToString() {
        String results = subject + "\n\n";
        if (!attributes.isEmpty()) {
            Iterator<Tuple> it = attributes.iterator();
            while (it.hasNext()) {
                Tuple tuple = it.next();
                if (tuple.getPredicate().contains("http://www.w3.org/2000/01/rdf-schema#label")) {
                    results = results + "Label:" + tuple.getObject() + "\n\n";
                } else if (tuple.getPredicate().contains("http://www.w3.org/2003/01/geo/wgs84_pos#lat")) {
                    results = results + "Latitude:" + new GeoTool().cutString(tuple.getObject()) + "\n\n";
                } else if (tuple.getPredicate().contains("http://www.w3.org/2003/01/geo/wgs84_pos#lon")) {
                    results = results + "Longitude:" + new GeoTool().cutString(tuple.getObject()) + "\n\n";
                }
            }
        }
        return results + "\n";
    }

}
