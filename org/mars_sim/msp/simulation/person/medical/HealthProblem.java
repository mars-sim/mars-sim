/**
 * Mars Simulation Project
 * HealthProblem.java
 * @version 2.74 2002-02-26
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
    public HealthProblem(Complaint complaint,
                          Person person, MedicalAid aid) {
        illness = complaint;
        sufferer = person;
        timePassed = 0;
        state = DEGRADING;
        duration = illness.getDegradePeriod();
        usedAid = null;

        // If no degrade period & no treatment, then can do self heel
        if ((duration == 0D) &&
                (illness.getRecoveryTreatment() == null)) {
            startRecovery();
        }
        else {
            // Check if a medical aid is available to help
            canStartTreatment(aid);
        }
	System.out.println(person.getName() + " has new health problem: " + complaint.getName());
    }

    /**
     * Can this problem be cured by using this MedicalAid. It has to check that
     * the aid has the required treatment. If this illness has no recovery
     * treatment the recovery can only start by a change in external situation,
     * e.g. Starvation.
     * @param newAid The Medical aid to try.
     */
    public void canStartTreatment(MedicalAid newAid) {
        if ((state == DEGRADING) && (usedAid == null)) {

            Treatment treatment = illness.getRecoveryTreatment();
            if ((treatment != null) && (newAid != null) &&
                 newAid.requestTreatment(this))
            {
                usedAid = newAid;
            }
        }
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
        if (usedAid != null) {
            return 0D;
        }
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
     * Start the treatment required treatment. It will take the specified
     * duration.
     *
     * @param treatmentLength Lenght of treatment.
     */
    public void startTreatment(double treatmentLength) {
        duration = treatmentLength;
        timePassed = 0;
        state = TREATMENT;
    }

    /**
     * This is now moving to a recovery state.
     */
    public void startRecovery() {
        if ((state == DEGRADING) || (state == TREATMENT)) {
            // If no recovery period, then it's done.
            duration = illness.getRecoveryPeriod();
            if (duration != 0D) {
                state = RECOVERING;
            }
            else {
                state = CURED;
            }

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

                // If person is cured or treatment persion has expired, then
                // release the aid.
                if (usedAid != null) {
                    usedAid.stopTreatment(this);
                    usedAid = null;
                }
            }
            else if (state == DEGRADING) {
                // Illness has moved to next phase, if null then dead
                Complaint nextPhase = illness.getNextPhase();
                if (usedAid != null) {
                    usedAid.stopTreatment(this);
                }

                if (nextPhase == null) {
                    state = DEAD;
                    condition.setDead();
                }
                else {
                    result = nextPhase;
                }
            }
            else if (state == TREATMENT) {
                if ((usedAid != null) && 
                     !illness.getRecoveryTreatment().getRetainAid()) {
                    usedAid.stopTreatment(this);
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
        else {
            buffer.append(illness.getName());
        }
        buffer.append(' ');
        buffer.append(getHealthRating());
        buffer.append('%');

        return buffer.toString();
    }
}
