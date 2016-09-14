package com.example.polipo.semanticapp.model;

import android.content.Context;
import android.util.Log;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by polipo on 21.04.16.
 */
public class DatabaseModul {
    /**
     * A HashMap of Resources.
     */
    private HashMap<String, Resource> resources;

    /**
     * The Context from the Android Activity.
     */
    private Context context;

    /**
     * Constructor.
     * @param context the Context from the Android Activity
     */
    public DatabaseModul(final Context context) {
        this.context = context;
        resources = new HashMap<String, Resource>();
        //readFromFile();
    }



    /**
     * Add a new Resource to the Resources list.
     * @param resource the resource you want to add
     */

    public final void addResource(final Resource resource) {

        resources.put(resource.getSubject(), resource);
    }

    /**
     * Remove a Resource from the resources list.
     * @param resourceUri the Resource Uri of the Resource you want to remove
     */

    public final void removeResource(final String resourceUri) {
        resources.remove(resourceUri);
    }


    /**
     * Set a Resource.
     * @param key the resource URI
     * @param resource the Rsource
     */

    public final void setResource(final String key, final Resource resource) {
        resources.get(key).setAttributes(resource.getAttributes());
    }


     /**
      * Add multiple Resources.
      * @param newResources the Resources you want to add
      */

    public final void addResources(final HashMap<String, Resource> newResources) {
        Set<String> keySet = newResources.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String newSubject = it.next();
            Resource newResource = newResources.get(newSubject);
            resources.put(newSubject, newResource);
        }
    }





    /**
     * Check if a list of resources exists in the Db.
     * Returns a list of the resources that NOT exists.
     * @param subjects a list of Resource URIs
     * @return a list of Resource URIs that not exists in the DB.
     */
    public final ArrayList<String> whichResourcesExistsNotInDb(
            final ArrayList<String> subjects) {
        ArrayList<String> subjectsNotExists = new ArrayList<>();

        Iterator<String> it = subjects.iterator();
        while (it.hasNext()) {
            String subject = it.next();
            if (!resourceExist(subject)) {
                subjectsNotExists.add(subject);
            }
        }
        return subjectsNotExists;
    }


    /**
     * Check if a Resource exists in DB.
     * @param subject The Resource URI
     * @return true if Resource exists
     */

    public final boolean resourceExist(final String subject) {
        return resources.containsKey(subject);
    }


    /**
     * Check if the database is empty.
     * @return true if database is empty
     */

    public final boolean databaseIsEmty() {
        return resources.isEmpty();
    }


    /**
     * getResources.
     * @return a HashMap of all Resources
     */
    public final HashMap<String, Resource> getResources() {
        return resources;
    }


    /**
     * Get Triple Bbjects from Resources with a specific predicate.
     * if there are links to other triple stores you can use this
     * method to get an arraylist of the objects URIs
     * @param predicate the Predicate
     * @param linkedDataName DomainName of the Object
     * @return a List of Resource URIs that are linked with the predicate
     */

    public final ArrayList<String>  getObjectsWithPredicate(
            final String predicate, final String linkedDataName) {
        Log.e("searching for ?o with p", predicate);

        Set<String> results = new HashSet<>();
        ArrayList<String> myResults = new ArrayList<>();

        HashMap<String, Resource> myResources = getResources();
        if (!myResources.isEmpty()) {
            //Log.e("in process", "!resources.isEmpty()");
            Set<String> keySet = myResources.keySet();
            Iterator<String> resourceIterator = keySet.iterator();

            //iterate all resources
            while (resourceIterator.hasNext()) {
                //Log.e("in process", "resourceIterator.hasNext()");
                String nextResource = resourceIterator.next();
                ArrayList<Tuple> attributes = myResources.get(nextResource).
                        getAttributes();
                Iterator<Tuple> attributesIterator = attributes.iterator();

                //iterate resource attributes
                while (attributesIterator.hasNext()) {
                    //Log.e("in process", "attributesIterator.hasNext()");
                    Tuple tuple = attributesIterator.next();

                    if (tuple.getPredicate().contains(predicate)) {
                        if (tuple.getObject().contains(linkedDataName)) {
                            try {
                                results.add(tuple.getObject());
                            /*
                            URI uri = new URI(tuple.getObject());
                            results.add(uri.toString());
                            */
                                //Log.e("Found Link: ",tuple.getObject());

                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }
                    }
                }
            }
        }
        Log.e("end search", predicate);
        Iterator<String> it = results.iterator();
        while (it.hasNext()) {
            myResults.add(it.next());
        }

        return myResults;

    }





    /**
     * Clear the Database.
     * @param filename the name of the file
     */

    public final void clearDatabase(final String filename) {
        resources.clear();
        writeToFile(filename);

    }



    /**
     * write resources to a file.
     * @param filename name of the file
     */
    public final void writeToFile(final String filename) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(resources);
            os.close();
            fos.close();
        } catch (IOException e) {
            Log.e("Exception", "" + e.toString());
        }
    }

    /**
     * Read data from file.
     * @param filename the name of the file
     */
    public final void readFromFile(final String filename) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            HashMap<String, Resource> oldResources = (HashMap<String, Resource>) is.readObject();
            resources = oldResources;
            is.close();
            fis.close();
        } catch (IOException e) {
            Log.e("Exception", "" + e.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print resources to String.
     * @return a String with all Resources
     */

    public final String resourcesToString() {
        String result = "";

        Set<String> keySet = resources.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String subject = it.next();
            String line = resources.get(subject).toString();

            //cut the last ","
            if (line != null && line.length() > 0 && line.charAt(line.length() - 1) == ',') {
                line = line.substring(0, line.length() - 1);
            }

            result = result + line + "\n";
        }
        return result;
    }


    /**
     * Some Information about the Object.
     * @return a String with some Information
     */

    public final String thinResourcesToString() {
        String result = "";

        Set<String> keySet = resources.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String subject = it.next();
            String line = resources.get(subject).thinToString();

            //cut the last ","
            if (line != null && line.length() > 0 && line.charAt(line.length() - 1) == ',') {
                line = line.substring(0, line.length() - 1);
            }

            result = result + line + "\n";
        }
        return result;
    }



}


