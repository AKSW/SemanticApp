package com.example.polipo.semanticapp.model;

import java.io.Serializable;

/**
 * Created by polipo on 21.04.16.
 * Tuple is an inner Class of Resources
 * it's being uses to store the Values Resources
 */
public class Tuple implements Serializable {

    /**
     * Predicate.
     */
    private String predicate;

    /**
     * Object.
     */
    private String object;

    /**
     * Constructor.
     * @param predicate Predicate
     * @param object Object
     */
    public Tuple(final String predicate, final String object) {
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * get Predicate.
     * @return  Predicate
     */

    public final String getPredicate() {
        return this.predicate;
    }

    /**
     * Get Object.
     * @return Object
     */
    public final String getObject() {
        return this.object;
    }

    /**
     * to String.
     * @return String
     */
    public final String toString() {
        return "Predicate:" + this.predicate + "\nObject:" + this.object + "\n\n";
    }
}
