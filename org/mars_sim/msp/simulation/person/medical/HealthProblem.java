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

    private Complaint       illness;        // Illness
    private Person          sufferer;       // Person
    private boolean         isRecovering;   // Persion is recovering
    private boolean         cured;           // Problem completed
    private double          duration;       // Duration of state
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
        isRecovering = false;
        cured = false;
        duration = 0;
        usedAid = null;

        // If no degrade period, then can do self heel
        if (illness.getDegradePeriod() == 0D) {
            startRecovery();
        }
        else {
            // Check if a medical aid is available to help
            canStartRecovery(aid);
        }
    }

    /**
     * Can this problem be cured by using this MedicalAid. It has to check that
     * the aid has the required treatment. If this illness has no recovery
     * treatment the recovery can only start by a change in external situation,
     * e.g. Starvation.
     * @param newAid The Medical aid to try.
     */
    public void canStartRecovery(MedicalAid newAid) {
        if (!isRecovering && (usedAid == null)) {

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
        return cured;
    }

    /**
     * Get a rating of the current health situation. This is a percentage value
     * and may either represent the recovering or degradation of the current
     * illness.
     * @return Percentage value.
     */
    private int getHealthRating() {
        int rating = 0;
        if (illness != null) {
            double max = (isRecovering ? illness.getRecoveryPeriod() :
                                         illness.getDegradePeriod());
            rating = (int)((duration * 100D) / max);
        }
        return rating;
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
    Person getSufferer() {
        return sufferer;
    }

    /**
     * Has the problem been cured.
     */
    public boolean getRecovering() {
        return isRecovering;
    }

    /**
     * Generates a situation string that represents the current status of this
     * problem.
     * @return Name of the complaint prefixed by the status.
     */
    public String getSituation() {
        if (isRecovering) {
            return "Recovering " + illness.getName();
        }
        else {
            return illness.getName();
        }
    }

    /**
     * This is now moving to a recovery state.
     */
    public void startRecovery() {
        if (!isRecovering) {
            // If no recovery period, then it's done.
            isRecovering = (illness.getRecoveryPeriod() > 0);
            if (!isRecovering) {
                cured = true;
            }
            else {
                duration = 0;
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

        duration += time;

        // Recoving so has the recovery period expired
        if (isRecovering) {
            cured = (duration > illness.getRecoveryPeriod());

            // If person is cured or treatment persion has expired, then
            // release the aid.
            if ((usedAid != null) && (cured ||
                    (duration > illness.getRecoveryTreatment().getDuration())))
            {
                usedAid.stopTreatment(this);
                usedAid = null;
            }
        }
        else if (duration > illness.getDegradePeriod()) {
            // Illness has moved to next phase, if null then dead
            Complaint nextPhase = illness.getNextPhase();
            if (usedAid != null) {
                usedAid.stopTreatment(this);
            }

            if (nextPhase == null) {
                condition.setDead();
            }
            else {
                result = nextPhase;
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
        if (isRecovering) {
            buffer.append("Recovering ");
            buffer.append(illness.getName());
            if (usedAid != null) {
                buffer.append("; ");
                buffer.append(illness.getRecoveryTreatment().getName());
            }
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
