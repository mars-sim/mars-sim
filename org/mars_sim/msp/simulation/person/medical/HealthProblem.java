/**
 * Mars Simulation Project
 * HealthProblem.java
 * @version 2.75 2004-01-06
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PhysicalCondition;

/**
 * This class represents a Health problem being suffered by a Person.
 * The class references a fixed Complaint that defines the
 * characteristics of this problem.
 */
public class HealthProblem implements Serializable {

    private static final int DEGRADING = 0;
    private static final int TREATMENT = 1;
    private static final int RECOVERING = 2;
    private static final int CURED = 3;
    private static final int DEAD = 4;

    private Complaint       illness;        // Illness
    private Person          sufferer;       // Person
    private int             state;          // State of problem
    private double          timePassed;     // Current time of state
    private double          duration;       // Length of the current state
    private MedicalAid      usedAid;        // Any aid being used

    /**
     * Create a new Health Problem that relates to a single Physical
     * Condition object. It also references a complaint that defines
     * the behaviour. If the Complaint has no degrade period then self-recovery
     * starts immediately.
     *
     * @param complaint Medical complaint being suffered.
     * @param person The Physical condition being effected.
     * @param aid The local Medical Aid facility.
     */
    public HealthProblem(Complaint complaint, Person person, MedicalAid aid) {
        illness = complaint;
        sufferer = person;
        timePassed = 0;
        state = DEGRADING;
        duration = illness.getDegradePeriod();
        usedAid = null;
        Treatment treatment = illness.getRecoveryTreatment();
        
        // If no degrade period & no treatment, then can do self heel
        if ((duration == 0D) && (treatment == null)) {
            startRecovery();
        }
        else {
            // Start treatment if the medical aid can help.
            if ((state == DEGRADING) && (aid != null) && aid.canTreatProblem(this)) {
                usedAid = aid;
                try {
                    usedAid.requestTreatment(this);
                }
                catch (Exception e) {
                    // System.out.println("HeathProblem: " + e.getMessage());
                }
            }
        }
        
        // System.out.println(person.getName() + " has new health problem: " + complaint.getName());
    }

    /**
     * Has the problem been cured.
     */
    public boolean getCured() {
        return (state == CURED);
    }

    /**
     * Get a rating of the current health situation. This is a percentage value
     * and may either represent the recovering or degradation of the current
     * illness.
     * @return Percentage value.
     */
    private int getHealthRating() {
        return (int)((timePassed * 100D) / duration);
    }

    /**
     * Return the illness that this problem has.
     *
     * @return Complaint defining problem.
     */
    public Complaint getIllness() {
        return illness;
    }

    /**
     * Sufferer of problem
     */
    public Person getSufferer() {
        return sufferer;
    }

    /**
     * The performance rating for this Problem. If there is an aid in used, then
     * the factor is zero otherwise it is the illness rating.
     */
    public double getPerformanceFactor() {
        if (usedAid != null) return 0D;
        return illness.getPerformanceFactor();
    }

    /**
     * Has the problem been cured.
     */
    public boolean getRecovering() {
        return (state == RECOVERING);
    }

    /**
     * Awaiting treatment
     */
    public boolean getAwaitingTreatment() {
        return ((state == DEGRADING) && (usedAid != null));
    }

    /**
     * Generates a situation string that represents the current status of this
     * problem.
     * @return Name of the complaint prefixed by the status.
     */
    public String getSituation() {
        if (state == RECOVERING) {
            return "Recovering " + illness.getName();
        }
        else if (state == TREATMENT) {
            return "Treatment " + illness.getName();
        }
        else {
            return illness.getName();
        }
    }
    
    /**
     * Gets a string representing this illness's current state.
     *
     * @return illness state as string
     */
    public String getStateString() {
        switch(state) {
            case DEGRADING: 
                return "degrading";
            case TREATMENT:
                return "treatment";
            case RECOVERING:
                return "recovering";
            case CURED:
                return "cured";
            case DEAD:
                return "dead";
            default:
                return "";
        }
    }

    /**
     * Start the required treatment. It will take the specified
     * duration.
     *
     * @param treatmentLength Length of treatment.
     */
    public void startTreatment(double treatmentLength) {
        duration = treatmentLength;
        timePassed = 0;
        state = TREATMENT;
    }
    
    /**
     * Stops the treatment for now.
     */
    public void stopTreatment() {
        if (state == TREATMENT) {
            if (duration > timePassed) state = DEGRADING;
            else startRecovery();
        }
    }

    /**
     * This is now moving to a recovery state.
     */
    public void startRecovery() {
        
        if ((state == DEGRADING) || (state == TREATMENT)) {
            // If no recovery period, then it's done.
            duration = illness.getRecoveryPeriod();
            timePassed = 0;
            if (duration != 0D) state = RECOVERING;
            else state = CURED;
        }
    }

    /**
     * A time period has expired for this problem.
     *
     * @param time The time period this problem has passed.
     * @param condition Physical condition being effected.
     * @return Return a replacement Medical complaint.
     */
    public Complaint timePassing(double time, PhysicalCondition condition) {
        Complaint result = null;

        timePassed += time;

        if (timePassed > duration) {

            // Recoving so has the recovery period expired
            if (state == RECOVERING) {
                state = CURED;

                // If person is cured or treatment person has expired, then
                // release the aid.
                if (usedAid != null) {
                    try {
                        usedAid.stopTreatment(this);
                    }
                    catch (Exception e) {
                        // System.out.println("HealthProblem.timePassing(): " + e.getMessage());
                    }
                    usedAid = null;
                }
            }
            else if (state == DEGRADING) {
                if (duration != 0D) {
                    // Illness has moved to next phase, if null then dead
                    Complaint nextPhase = illness.getNextPhase();
                    if (usedAid != null) {
                        try {
                            usedAid.stopTreatment(this);
                        }
                        catch (Exception e) {
                            // System.out.println("HealthProblem.timePassing(): " + e.getMessage());
                        }
                    }

                    if (nextPhase == null) {
                        state = DEAD;
                        condition.setDead(illness);
                    }
                    else result = nextPhase;
                }
            }
            else if (state == TREATMENT) {
                if ((usedAid != null) && !illness.getRecoveryTreatment().getRetainAid()) {
                    try {
                        usedAid.stopTreatment(this);
                    }
                    catch (Exception e) {
                        // System.out.println("HealthProblem.timePassing(): " + e.getMessage());
                    }
                    usedAid = null;
                }
                startRecovery();
            }
        }

        return result;
    }

    /**
     * This method generates a string representation of this problem.
     * It contains the illness and the health rating.
     * @return String description.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (state == RECOVERING) {
            buffer.append("Recovering ");
            buffer.append(illness.getName());
        }
        else if (state == TREATMENT) {
            buffer.append("Treatment (");
            buffer.append(illness.getRecoveryTreatment().getName());
            buffer.append(") ");
            buffer.append(illness.getName());
        }
        else buffer.append(illness.getName());

        buffer.append(' ');
        buffer.append(getHealthRating());
        buffer.append('%');

        return buffer.toString();
    }
    
    /**
     * Checks if this problem is an environmental problem.
     * 
     * @return true if environmental problem.
     */
    public boolean isEnvironmentalProblem() {
        return sufferer.getMars().getMedicalManager().isEnvironmentalComplaint(illness);
    }
}