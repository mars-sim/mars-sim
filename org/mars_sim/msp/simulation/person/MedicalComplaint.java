/**
 * Mars Simulation Project
 * @author Barry Evans
 * @version 2.74
 */

package org.mars_sim.msp.simulation.person;

import java.util.HashMap;
import java.io.Serializable;

/**
 * This class represents the definition of a specific Medical Complaint that can
 * effect a Person. The MedicalComplaint once effecting a Person can either
 * result in the Person entering a recovery period or developing a more serious
 * complaint or possibly death.
 *
 * I
 */
public class MedicalComplaint implements Serializable {


    private String name;                // Identifying name
    private int seriousness;            // Seriousness of this illness
    private double degradePeriod;       // Time before complaint degrades
    private double recoveryPeriod;      // Time before Person recovers
    private int performanceFactor;      // Factor effecting Person performance
    MedicalComplaint nextPhase;         // Next phase of this illness

    /**
     * Create a Medical Complaint instance.
     *
     * @param name Name of complaint.
     * @param seriousness How serious is this complaint.
     * @param degrade The time it takes before this complaint advances, if this
     * value is zero, then the Person can shelf heel themselves.
     * @param recovery The time is takes for a Person to recover. If this value
     * is zero it means the complaint results in death unless treated.
     * @param performance The percentage that a Persons performance is decreased.
     * @param next The complaint that this degrades into unless checked.
     */
    MedicalComplaint(String name, int seriousness,
                             double degrade, double recovery,
                             int performance, MedicalComplaint next) {
        this.name = name;
        this.seriousness = seriousness;
        this.degradePeriod = degrade;
        this.recoveryPeriod = recovery;
        this.performanceFactor = performance;
        this.nextPhase = next;
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
    public MedicalComplaint getNextPhase() {
        return nextPhase;
    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * @return Percentage value representing the effect.
     */
    public int getPerformanceFactor() {
        return performanceFactor;
    }

    /**
     * Get the seriousness of this complaint.
     * @return Seriousness rating.
     */
    public int getSeriousness() {
        return seriousness;
    }

    /**
     * Get the recover period.
     * @return Double value representing a duration.
     */
    public double getRecoveryPeriod() {
        return recoveryPeriod;
    }
}