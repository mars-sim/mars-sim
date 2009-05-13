/**
 * Mars Simulation Project
 * Medication.java
 * @version 2.86 2009-05-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;

import org.mars_sim.msp.simulation.person.Person;

/**
 * An abstract class representing a medication a person
 * has taken.
 */
public abstract class Medication implements Serializable, 
        Comparable<Medication> {

    // Data members.
    private String name;
    private double duration;
    private double timeElapsed;
    private Person person;
    
    /**
     * Constructor.
     * @param name the name of the medication.
     * @param duration the time duration (millisols).
     * @param person the person to be medicated.
     */
    public Medication(String name, double duration, Person person) {
        this.name = name;
        this.duration = duration;
        this.person = person;
        timeElapsed = 0D;
    }
    
    /**
     * Gets the name of the medication.
     * @return name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the time duration of the medication.
     * @return duration (millisols).
     */
    public double getDuration() {
        return duration;
    }
    
    /**
     * Gets the person taking the medication.
     * @return person.
     */
    public Person getPerson() {
        return person;
    }
    
    /**
     * Gets the time elapsed since medication was taken.
     * @return time (millisols).
     */
    public double getTimeElapsed() {
        return timeElapsed;
    }
    
    /**
     * Update the medication based on passing time.
     * Child classes should override for other medical effects.
     * @param time amount of time (millisols).
     */
    public void timePassing(double time) {
        
        // Add to time elapsed.
        timeElapsed += time;
    }
    
    /**
     * Is the person under the influence of this medication?
     * @return true if medicated.
     */
    public boolean isMedicated() {
        return (timeElapsed < duration);
    }
    
    @Override
    public boolean equals(Object object) {
        boolean result = true;
        if (object instanceof Medication) {
            Medication med = (Medication) object;
            if (!getName().equals(med.getName())) result = false;
            if (getDuration() != med.getDuration()) result = false;
            if (getTimeElapsed() != med.getTimeElapsed()) result = false;
            if (!getPerson().equals(med.getPerson())) result = false;
        }
        else result = false;
        
        return result;
    }
    
    @Override
    public int hashCode() {
        int hashCode = getName().hashCode();
        hashCode *= new Double(getDuration()).hashCode();
        hashCode *= new Double(getTimeElapsed()).hashCode();
        hashCode *= getPerson().hashCode();
        return hashCode;
    }
    
    /**
     * Compares this object with the specified object for order.
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object.
     */
    public int compareTo(Medication o) {
        return getName().compareTo(o.getName());
    }
}