package com.example.polipo.semanticapp.model;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by polipo on 26.04.16.
 *
 *
 * This Modul is needed to Configure the App
 *
 * all Configurations are done here
 *
 *
 *
 */
public class SemanticWebModul {


    /**
     * name of the configuration.
     */
    private String name;

    public String getService() {
        return service;
    }

    /**
     *
     * the sparql endpoint.
     */
    private String service;

    /**
     * the query.
     */
    private String query;

    /**
     * edited query after formating it.
     */
    private String editedQuery;

    /**
     * QueryBuilderId.
     */
    private int queryBuilderId = 0;

    /**
     * SparQlModul.
     */
    private SparqlModul spaql;


    /**
     * DatabaseModul.
     */
    private DatabaseModul database;

    public DatabaseModul getDatabase() {
        return database;
    }
    /**
     * the application context.
     */
    private Context context;

    //information about dataset
    //String information;

    //File Content
    //String file;

    /**
     * Id for Routine.
     */
    private int routineId = 0;

    /**
     * LinkedData URI.
     */
    private String linkedDataUri;

    /**
     * LinkedData Service.
     */
    private String linkedDataService;

    /**
     * LinkedDataName.
     */
    private String linkedDataDomainName;

    /**
     * LinkedDataFilter.
     */
    private String linkedDataFilter;



    /*
    public SemanticWebModul(String service, String query, Context context) {
        this.service = service;
        this.query = query;
        this.context = context;
        spaql = new SparqlModul(context);
        database = new DatabaseModul(context);
    }
    */


    /**
     * Constructor.
     * generates a new SemanticWebModul
     * transfers geocoordinates and the searchradius
     * @param context Applikations Context
     * @param lon Longitude
     * @param lat Latituds
     * @param radius Radius
     * @param name Name of the Configuration
     */

    public SemanticWebModul(final Context context, final double lon, final double lat, final int radius, final String name) {
        this.context = context;

        jsonParser(readFile(name));

        this.name = name;

        queryFormat(lat, lon, radius);


        initDatabase();
    }

    /**
     * generates a new Database and fills it with.
     * saved Resources
     */

    public final void initDatabase() {
        database = new DatabaseModul(context);

        database.readFromFile(name);
    }

    /**
     * Clears the Database.
     *
     */

    public final void clearDatabase() {
        database.clearDatabase(name);
    }


    /**
     * switches the Database Request routines.
     * using the id
     *
     */

    public final void databaseRequest() {
        switch (routineId) {
            case 0:
                simpleDatabaseRequest();
                break;

            case 1:
                complexDatabaseRequest();
                break;

            default:
                simpleDatabaseRequest();
        }
    }

    /**
     * request simple object from a single database.
     */
    public final void simpleDatabaseRequest() {
        spaql = new SparqlModul(context);

        //request resources
        ArrayList<String> listOfResources =  spaql.requestResources(service, editedQuery);
        //information = information + "#ofRes:" + listOfResources.size() + "\n";

        //check which resources are not in the database
        ArrayList<String> listOfResourcesNotInDb = database.whichResourcesExistsNotInDb(listOfResources);
        //information = information + "notInDB:" + listOfResourcesNotInDb.size() + "\n \n";

        //get all the Attributes of not existant resources
        HashMap<String, Resource> resourceHashMap =  spaql.requestResourcesAttributes(listOfResourcesNotInDb, service);

        //add the new Resources to the database
        database.addResources(resourceHashMap);

        database.writeToFile(name);
    }

    /**
     * request linked data from multiple databases.
     */
    public final void complexDatabaseRequest() {
        spaql = new SparqlModul(context);

        //request resources
        ArrayList<String> listOfResources =  spaql.requestResources(service, editedQuery);
        //information = information + "#ofRes:" + listOfResources.size() + "\n";

        //check which resources are not in the database
        ArrayList<String> listOfResourcesNotInDb = database.whichResourcesExistsNotInDb(listOfResources);
        //information = information + "notInDB:" + listOfResourcesNotInDb.size() + "\n \n";

        //get all the Attributes of not existant resources
        HashMap<String, Resource> resourceHashMap =  spaql.requestResourcesAttributes(listOfResourcesNotInDb, service);

        //add the new Resources to the database
        database.addResources(resourceHashMap);


        //get the linked data Resouces
        ArrayList<String> linkedResources = database.getObjectsWithPredicate(linkedDataUri, linkedDataDomainName);

        //filter the resources with the given linkedDataFilter
        linkedResources = spaql.filterResourcesWithObjectFilter(linkedResources, linkedDataService, linkedDataFilter);

        //check which linked resources are not in the db
        ArrayList<String> linkedResourcesNotInDb = database.whichResourcesExistsNotInDb(linkedResources);

        //get all Attributes of linkedData Resources
        HashMap<String, Resource> linkedResourceHashMap = spaql.requestResourcesAttributes(linkedResourcesNotInDb, linkedDataService);

        //get all Attributes of linkedData Resources
        //HashMap<String, Resource> linkedResourceHashMap = spaql.requestResourcesAttributesWithObjectFilter(linkedResources, linkedDataService, linkedDataFilter);

        //add the new Resources to the database
        database.addResources(linkedResourceHashMap);

        database.writeToFile(name);
    }

    /**
     * Parse .json to local variables.
     * @param json Sring with json data
     */
    public final void jsonParser(final String json) {
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);

                service = jsonObject.getString("service");
                query = jsonObject.getString("query");
                queryBuilderId = jsonObject.getInt("queryBuilderId");
                routineId = jsonObject.getInt("routineId");
                linkedDataUri = jsonObject.getString("linkedDataUri");
                linkedDataService = jsonObject.getString("linkedDataService");
                linkedDataDomainName = jsonObject.getString("linkedDataDomainName");
                linkedDataFilter = jsonObject.getString("linkedDataFilter");


            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    /**
     * Put lat, lon and radius into the query and save it as editedQuery.
     * @param lat Latitude
     * @param lon Longitude
     * @param radius Radius
     */
    public final void queryFormat(final double lat, final double lon, final int radius) {


        String resultQuery = "";

        switch (queryBuilderId) {

            /*
             * geom:geometry [ ogc:asWKT ?geo ] .  Filter (  bif:st_intersects (?geo, bif:st_point ( %s , %s ), %s ) ) .
             */
            case 0:

               resultQuery = String.format(query, lon, lat, radius);
                break;

            /*
             * radius needs to be calculated
             * radius in degree
             */

            case 1:
                resultQuery = query.replaceAll(" myLat ", lat + " ");
                resultQuery = resultQuery.replaceAll(" myLon ", lon + " ");
                //https://www.scribd.com/doc/2569355/Geo-Distance-Search-with-MySQL
                // abgeleitet von der Haversine Formula
                double myRadius = radius;
                double myLatRadius = Math.abs(myRadius / 111);
                double myLonRadius =   Math.abs(myRadius / (Math.cos(lat) * 111));

                Toast.makeText(context, myLatRadius + " " + myLonRadius, Toast.LENGTH_LONG).show();


                resultQuery = resultQuery.replaceAll(" myLatRadius ", myLatRadius + "");
                //resultQuery = resultQuery.replaceAll("myLonRadius", (radius / ( Math.cos(lat) * 111 ) ) + "");
                resultQuery = resultQuery.replaceAll(" myLonRadius ", myLonRadius + "");
                break;
            default:
                resultQuery = String.format(query, lon, lat, radius);
        }

        editedQuery = resultQuery;
        Toast.makeText(context, "Query ready", Toast.LENGTH_SHORT).show();
    }


    /**
     * Read from a File.
     * @param filename Filename
     * @return the Content of the file as String
     */
    public final String readFile(final String filename) {
        StringBuilder returnString =  new StringBuilder();

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = context.getResources().getAssets().open(filename, Context.MODE_WORLD_READABLE);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                returnString.append(line);
            }

        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception ee) {
                ee.getMessage();
            }
        }
        return returnString.toString();

    }







}
