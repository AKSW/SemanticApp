package com.example.polipo.semanticapp.model;

/**
 * Created by robocop on 3/2/16.
 */
import android.content.Context;
import android.util.Log;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This Class manages SPARQL Queries.
 */
class SparqlModul {

    /**
     * the application context.
     */
    private Context context;
	/**
	 * the constructor.
	 * inits the local variables
     * @param context Application Context
	 *
	 */

    public SparqlModul(final Context context) {
        this.context = context;
    }


/*
 * ===============================================================================================
 * 										Queries
 * ===============================================================================================
*/
    /**
     * request all subjects of resources.
     *   around the gps
     *   with radius
     * @param service SPARQL Endpoint
     * @param query Query
     * @return ArrayList of ResourceURIs
     */
    public ArrayList<String> requestResources(final String service, final String query) {

        ResultSet rs = executeHttpQuery(service, query);

        ArrayList<String> results = new ArrayList<String>();

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            //results.add(qs.getResource("?s").getLocalName()); //returns wayXYZ123456
            //results.add(qs.getResource("?s").getNameSpace()); //returns Prefix http://linkeddata.org/triplify/
            results.add(qs.get("?s").toString()); //returns http://linkeddata.org/triplify/wayXYZ123456
       }

        return results;

    }
    /**
     * request resource Attributes.
     * iterates all resources and
     * returns a hashmap<String ,Resource> with the attributes
     *
     * @param subjects ResourceURIs
     * @param service SPARQL Endpoint
     * @return Resources
     */
    public HashMap<String, Resource> requestResourcesAttributes(final ArrayList<String> subjects, final String service) {


        //Resources myResources = new Resources(); //create new resources
        HashMap<String, Resource> myResources = new HashMap<>();

        boolean latExists = false;
        boolean lonExists = false;

        ResultSet rs = null;
        if ((subjects != null) && (!subjects.isEmpty())) {
            Iterator<String> it = subjects.iterator();
            while (it.hasNext()) {

                String subject = it.next();

                Resource myResource = new Resource(subject);

                String myQuery = "Select  ?p ?o "
                            + "Where {  "
                            + "<" + subject + "> ?p ?o. " //important to make < uri >
                            +  "} ";

                rs = executeHttpQuery(service, myQuery);

                if (rs.hasNext()) {
                    /*
                 * iterate all results
                 * and add the tuples
                 */
                    while (rs.hasNext()) {

                        QuerySolution qs = rs.next();

                        //get predicate and object
                        String predicate = qs.get("?p").toString();
                        String object = qs.get("?o").toString();

                        if (predicate.contains("http://www.w3.org/2003/01/geo/wgs84_pos#lat")) {
                            latExists = true;
                        }
                        if (predicate.contains("http://www.w3.org/2003/01/geo/wgs84_pos#lon")) {
                            lonExists = true;
                        }

                        //generate new tuple with predicate and object
                        Tuple tuple =  new Tuple(predicate, object);

                        myResource.addAttribute(tuple);
                    }
                    if ((!latExists) && (!lonExists)) {
                    /*
                     * add Geo
                     */

                        GeoCoordinates geo = requestGeoCoordinatesForSubject(subject, service);


                        Tuple wgs84Lat = new Tuple("http://www.w3.org/2003/01/geo/wgs84_pos#lat", String.valueOf(geo.getLat()) + "");
                        Tuple wgs84Lon = new Tuple("http://www.w3.org/2003/01/geo/wgs84_pos#lon", String.valueOf(geo.getLon()) + "");
                        myResource.addAttribute(wgs84Lat);
                        myResource.addAttribute(wgs84Lon);
                        Log.e("Resource:", myResource.getSubject() + " lat:"  + myResource.getCoordinates().getLat() + " lon:" + myResource.getCoordinates().getLon() + "");
                    }


                    latExists = false;
                    lonExists = false;

                    myResources.put(myResource.getSubject(), myResource);
                }
            }
        }
        return myResources;

    }
    /**
     * this function filters resource URIs.
     * the filtercriteria is a given String
     *
     * this funktion uses a simple sparql request over all subject strings
     * with the filterstring as object
     * @param subjects ResourceURIs
     * @param service SPARQL Endpoint
     * @param filter Filtercriteria
     * @return filtered ResourceURIs
     */
    public ArrayList<String> filterResourcesWithObjectFilter(
            final ArrayList<String> subjects, final String service, final String filter) {

        ArrayList<String> myResult = new ArrayList<>();

        Set<String> resources = new HashSet<>();

        ResultSet rs = null;

        String resourcesString = ""; //important to make < uri >
        Iterator<String> subjectIterator = subjects.iterator();
        while (subjectIterator.hasNext()) {
            resourcesString = resourcesString + " <" + subjectIterator.next() + "> ";
        }


        String myQuery = "Select  distinct ?s "
                        + "Where {  "
                        + "Values ?s "
                        + " { "
                        + " " + resourcesString  + " "
                        + " } "
                        + " ?s ?p <" +  filter  + "> . "
                        + "} ";


        rs = executeHttpQuery(service, myQuery);

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            resources.add(qs.get("?s").toString()); //returns http://linkeddata.org/triplify/wayXYZ123456
        }

        Iterator<String> resourcesIterator = resources.iterator();
        while (resourcesIterator.hasNext()) {
            myResult.add(resourcesIterator.next());
        }

        return myResult;

    }

    /**
     * requestGeoCoordinates for Resource.
     * @param subject Resource URI
     * @param service SPARQL Endpoint
     * @return Geocoordinates
     */
    public GeoCoordinates requestGeoCoordinatesForSubject(final String subject, final String service) {
        String myQuery = "Prefix ogc: <http://www.opengis.net/ont/geosparql#> "
                + "Prefix geom: <http://geovocab.org/geometry#> "
                + "Select ?geo "
                + "Where {  "
                + "<" + subject + "> geom:geometry [ ogc:asWKT ?geo ] . "  //important to make < uri >
                + "} ";
        ResultSet rs = executeHttpQuery(service, myQuery);
        while (rs.hasNext()) {
            return new GeoTool().stringToLatLon(rs.next().get("?geo").toString());
        }
        return null;
    }
    /**
     * execute a http query and return the resultSet.
     * @param service SPARQL Endpoint
     * @param query Query
     * @return Result as a ResultSet
     */
    private ResultSet executeHttpQuery(final String service, final String query) {
        try {
            QueryExecution vqe = new QueryEngineHTTP(service, query);
            ResultSet results = vqe.execSelect();
            return results;
        } catch (Exception e) {
            e.getMessage();
        }
        return null;

    }



/*
* ===============================================================================================
* 										Output
* ===============================================================================================
*/




    /*
      print resultset to android screen
     */

    /*

    public String printResultSetToString (ResultSet results) {
        String rs = "";
        String subject = "";
        String predicate = "";
        String object = "";

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            //System.out.println(qs.toString());
            boolean success = true;
            try {
                if ( !qs.get("s").toString().isEmpty() )
                    subject = qs.get("s").toString();

                if ( !qs.get("?p").toString().isEmpty() )
                    predicate = qs.get("?p").toString();

                if ( !qs.get("?o").toString().isEmpty() )
                    object = qs.get("?o").toString();
                rs = rs + "Subject: " + subject + "; Predicate: " + predicate + "; Object: " + object + "\n";

            } catch (Exception e) {
                success = false;
                // other exception handling
            }
            if (!success) {
                // equivalent of Python else goes here
                rs = rs + qs.toString();
            }
        }
        return rs;
    }

    */


    /*
     * print out resultset with resultsetformatter
     */

    /*

    public String printResultSet (ResultSet results) {
        return ResultSetFormatter.asText(results);
    }

    */

    /*
    * print ArrayList<String> to String
     */

    /*
    public String printArrayListToString(ArrayList<String> arrayList) {
        String result = "";
        if (!arrayList.isEmpty()) {
            Iterator<String> it = arrayList.iterator();
            while (it.hasNext()) {
                result = result + it.next() + "\n";
            }
        }
        return result;
    }

    */

}
