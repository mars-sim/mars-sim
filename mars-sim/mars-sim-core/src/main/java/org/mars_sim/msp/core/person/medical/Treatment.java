/**
 * Mars Simulation Project
 * Treatment.java
 * @version 3.00 2010-08-10
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.medical;

/**
 * This class represents a Medical treatment that can be applied to
 * a Person to cure a complaint.
 */
public class Treatment implements java.io.Serializable, Comparable<Treatment> {

    private String  name;
    private int     requiredSkill;  // Optimal MEDICAL skill
    private int     facilityLevel;  // Required MedicalAid level
    private double  duration;       // Length of treatment
    private boolean retainAid;      // Continue to use Aid after Treatment
    private boolean selfAdmin;      // Can perform the treat

    /**
     * Create a Treatment.
     *
     * @param name The unique name.
     * @param skill Required Medical skill.
     * @param duration The duration of treatment in millisols.
     * @param retainAid Does the recovery after treatment require the medical aid
     */
    public Treatment(String name, int skill, double duration,
                     boolean selfAdmin, boolean retainAid, int facilityLevel) {
        this.name = name;
        this.requiredSkill = skill;
        this.selfAdmin = selfAdmin;
        this.retainAid = retainAid;
        this.facilityLevel = facilityLevel;
        if (duration < 0D) {
            // Negative duration means, the treatment takes as long as recovery
            duration = -1D;
        }
        else {
            this.duration = duration;
        }
    }

    /**
     * Compare this object with another
     */
    public int compareTo(Treatment otherTreatment) {
        return name.compareTo((otherTreatment).name);
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
     * Get the time required to perform this treatment by a Person with
     * the appropriate skill rating.
     * @param skill The skill rating that will apply the treatment.
     * @return Adjusted treatment time according to skill.
     */
    public double getAdjustedDuration(int skill) {
        double result = duration;
        if ((result > 0D) && (skill < requiredSkill)) {
            // Increase the time by the percentage skill lacking
            result = duration * (1 + ((requiredSkill - skill)/requiredSkill));
        }
        return result;
    }

    /**
     * Return the theoritical duration of this treatment.
     * @return The duration to apply this Treatment.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Get the required facility level
     */
    public int getFacilityLevel() {
        return facilityLevel;
    }

    /**
     * Return the name of the treatment
     */
    public String getName() {
        return name;
    }

    /**
     * Does this Treatment require the sufferer to continue to use
     * any MedicalAids.
     * @return boolean flag.
     */
    public boolean getRetainAid() {
        return retainAid;
    }
    
    /**
     * Return the Medical skill requried for this treatment
     */
    public int getSkill() {
        return requiredSkill;
    }

    /**
     * Can the treatment be self administered.
     */
    public boolean getSelfAdminister() {
        return selfAdmin;
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
