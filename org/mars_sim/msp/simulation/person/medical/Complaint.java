/**
 * Mars Simulation Project
 * @author Barry Evans
 * @version 2.75 2003-11-27
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import org.mars_sim.msp.simulation.MarsClock;

/**
 * This class represents the definition of a specific Medical Complaint that can
 * effect a Person. The Complaint once effecting a Person can either
 * result in the Person entering a recovery period or developing a more serious
 * complaint or possibly death.
 *
 * I
 */
public class Complaint implements Serializable {

    /**
     * The maximum probability rating. This allows the complaint to be specifed
     * to 1/100 th of a percentage.
     */
    public final static int MAXPROBABILITY = 100000;


    private String name;                    // Identifying name
    private int seriousness;                // Seriousness of this illness
    private double degradePeriod;           // Time before complaint degrades
    private double recoveryPeriod;          // Time before Person recovers
    private double probability;             // Probability of occuring
    private double performanceFactor;       // Factor effecting Person performance
    private Treatment recoveryTreatment;     // Treatment needed for recovery
    Complaint nextPhase;                      // Next phase of this illness

    /**
     * Create a Medical Complaint instance.
     *
     * @param name Name of complaint.
     * @param seriousness How serious is this complaint.
     * @param degrade The time it takes before this complaint advances, if this
     * value is zero, then the Person can shelf heel themselves. This value is
     * in Earth minutes.
     * @param recovery The time is takes for a Person to recover. If this value
     * is zero it means the complaint results in death unless treated. This
     * value is in Earth minutes.
     * @param probability The probability of this illness occuring, this can be
     * between 0 and MAXPROBABILITY.
     * @param performance The percentage that a Persons performance is decreased.
     * @param recoveryTreatment Any treatment that is needed for recovery.
     * @param next The complaint that this degrades into unless checked.
     */
    Complaint(String name, int seriousness,
                             double degrade, double recovery,
                             double probability,
                             int performance,
                             Treatment recoveryTreatment, Complaint next) {
        this.name = name;
        this.seriousness = seriousness;
        // Convert from minutes into Millisols
        this.degradePeriod = MarsClock.convertSecondsToMillisols(degrade * 60D);
        this.recoveryPeriod = MarsClock.convertSecondsToMillisols(recovery * 60D);
        this.performanceFactor = (performance / 100D);
        this.nextPhase = next;
        this.probability = probability;
        this.recoveryTreatment = recoveryTreatment;
    }

    /**
     * Get the degrade period.
     * @return Double value representing a duration.
     */
    public double getDegradePeriod() {
        return degradePeriod;
    }

    /**
     * Get the name of complaint.
     * @return Complaint name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the next complaint that this complaint developers into.
     * @return The next complaint, if null then death results.
     */
    public Complaint getNextPhase() {
        return nextPhase;
    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performanceFactor;
    }

    /**
     * Get the probabity of this complaint.
     * @return Probabity from 0 to 100.
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Get the treatment required for recovery to start.
     * @return recovery treatment.
     */
    public Treatment getRecoveryTreatment() {
        return recoveryTreatment;
    }

    /**
     * Get the recover period.
     * @return Double value representing a duration.
     */
    public double getRecoveryPeriod() {
        return recoveryPeriod;
    }

    /**
     * Get the seriousness of this complaint.
     * @return Seriousness rating.
     */
    public int getSeriousness() {
        return seriousness;
    }

    /**
     * Get a string respresentation.
     * @return The name of the complaint.
     */
    public String toString() {
        return name;
    }
}
