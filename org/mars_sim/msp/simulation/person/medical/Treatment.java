package org.mars_sim.msp.simulation.person.medical;

import org.mars_sim.msp.simulation.MarsClock;

/**
 * This class represents a Medical treatment that can be applied to
 * a Person to cure a complaint.
 */
public class Treatment implements java.io.Serializable {

    private String  name;
    private int     requiredSkill;
    private double  duration;

    /**
     * Create a Treatment.
     *
     * @param name The unique name.
     * @param skill Required Medical skill.
     * @param earthDuration The duration of trwatment in earth hours.
     */
    public Treatment(String name, int skill, double earthDuration) {
        this.name = name;
        this.requiredSkill = skill;
        if (earthDuration < 0) {
            // Negative duration means, the treatment takes as long as recovery
            duration = -1;
        }
        else {
            duration = MarsClock.convertSecondsToMillisols(earthDuration * 360D);
        }
    }

    /**
     * Check this object with another object.
     * @param other Object to compare.
     * @return DO they match.
     */
    public boolean equals(Object other) {
        boolean match = false;
        if (other instanceof Treatment) {
            match = name.equals(((Treatment)other).name);
        }
        return match;
    }

    /**
     * Get the time required to perform this treatment
     * @return treatment time.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Return the name of the treatment
     */
    public String getName() {
        return name;
    }

    /**
     * Return the Medical skill requried for this treatment
     */
    public int getSkill() {
        return requiredSkill;
    }

    /**
     * Hash code vlaue for this object.
     * @return hash code.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Return string representation.
     * @return The treatment name.
     */
    public String toString() {
        return name;
    }
}
