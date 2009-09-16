/**
 * Mars Simulation Project
 * Science.java
 * @version 2.87 2009-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.science;

import java.io.Serializable;
import java.util.Arrays;

import org.mars_sim.msp.simulation.person.ai.job.Job;

/**
 * A class representing a field of science.
 */
public class Science implements Serializable, Comparable {

    // Static science field names.
    public static final String AREOLOGY = "Areology";
    public static final String ASTRONOMY = "Astronomy";
    public static final String BIOLOGY = "Biology";
    public static final String BOTANY = "Botany";
    public static final String CHEMISTRY = "Chemistry";
    public static final String MATHEMATICS = "Mathematics";
    public static final String MEDICINE = "Medicine";
    public static final String METEOROLOGY = "Meteorology";
    public static final String PHYSICS = "Physics";
    
    
    // Data members.
    private String name;
    private Job[] jobs;
    private Science[] collaborativeSciences;
    
    /**
     * Constructor
     * @param name the name of the field of science.
     * @param jobs jobs associated with the field.
     */
    Science(String name, Job[] jobs) {
        this.name = name;
        this.jobs = jobs;
    }
    
    /**
     * Set the sciences that can collaborate on research with this field of science.
     * @param collaborativeSciences sciences that can collaborate.
     */
    void setCollaborativeSciences(Science[] collaborativeSciences) {
        this.collaborativeSciences = collaborativeSciences;
    }
    
    /**
     * Gets the sciences that can collaborate on research with this field of science.
     * @return sciences.
     */
    Science[] getCollaborativeSciences() {
        return collaborativeSciences.clone();
    }
    
    /**
     * Gets the name of the field of science.
     * @return name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the jobs associated with this field of science.
     * @return jobs.
     */
    Job[] getJobs() {
        return jobs.clone();
    }
    
    /**
     * Compares this object with the specified object for order.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        if (o instanceof Science) return name.compareTo(((Science) o).getName());
        else return 0;
    }
    
    /**
     * Checks if an object is equal to this object.
     * @return true if equal
     */
    public boolean equals(Object object) {
        if (object instanceof Science) {
            Science otherObject = (Science) object;
            if (name.equals(otherObject.getName())) return true;
        }
        return false;
    }
    
    /**
     * Gets the hash code value.
     */
    public int hashCode() {
        return (name.hashCode());
    }
    
    @Override
    public String toString() {
        return name;
    }
}