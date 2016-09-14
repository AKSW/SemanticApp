
package com.example.polipo.semanticapp.model;

import android.util.Log;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by robocop on 1/4/16.
 */


public class GeoTool {
    /**
     * This Funktion Converts Strings to LatLon Data.
     * If the contains more than one point, it calculates the average
     * @param myString the String you want to convert
     * @return Coordinates
     */

    public final GeoCoordinates stringToLatLon(final String myString) {
        int beginIndex;
        int endIndex;

        String string = myString;
        if (string.contains("POINT")) {

            beginIndex = string.indexOf("(") + 1;
            endIndex = string.indexOf(")");

            string = string.substring(beginIndex, endIndex);

            StringTokenizer st = new StringTokenizer(string);
            if (st.countTokens() == 2) {
                return new GeoCoordinates(Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
            }
        } else
        if (string.contains("LINESTRING")) {
            double lat = 0;
            double lon = 0;


            beginIndex = string.indexOf("(") + 1;
            endIndex = string.indexOf(")");

            string = string.substring(beginIndex, endIndex);
            StringTokenizer st = new StringTokenizer(string, ",");
            int anz = st.countTokens();
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                StringTokenizer st1 = new StringTokenizer(pair);
                if (st1.countTokens() == 2) {
                    lon += Double.parseDouble(st1.nextToken());
                    lat += Double.parseDouble(st1.nextToken());
                }
            }

            lat = (lat / anz);
            lon = (lon / anz);

            //Log.e("Linestring" , "lat" + lat + " lon" + lon);
            return new GeoCoordinates(lat, lon);

        } else {
            String[] part = string.split(",");
            String[] latpart = part[0].split("^^");
            String[] lonpart = part[1].split("^^");

            return new GeoCoordinates(Double.parseDouble(latpart[0]), Double.parseDouble(lonpart[0]));
        }

        return null;

    }

    /**
     * Cur Strings to extract Numbers.
     * @param string the String
     * @return the Number
     * todo
     */
    public final Double cutString(final String string) {
        try {
            if (string.contains("http://www.w3.org/2001/XMLSchema#double")) {
                String[] part = string.split(Pattern.quote("^^"));
                return Double.parseDouble(part[0]);

            } else if (string.contains("http://www.w3.org/2001/XMLSchema#decimal")) {
                String[] part = string.split(Pattern.quote("^^"));
                return Double.parseDouble(part[0]);
            } else if (string.contains("http://www.w3.org/2001/XMLSchema#float")) {
                String[] part = string.split(Pattern.quote("^^"));
                return Double.parseDouble(part[0]);
            }


        } catch (Exception e) {
            Log.e("TAG", "cutString: " + string + "", e);
        }
        return Double.parseDouble(string);


    }
}
