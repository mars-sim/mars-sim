/**
 * Mars Simulation Project
 * MechanicalFailure.java
 * @version 2.71 2000-09-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The MechanicalFailure class represents a
 *  mechanical failure in a vehicle.
 */
public class MechanicalFailure {

    private String name; // The name of the failure.
    private float totalWorkHours; // The total number of work hours required to fix the failure. (1 - 50).
    private float remainingWorkHours; // The remaining work hours required to fix the failure. (1 - 50).
    private boolean fixed; // True when mechanical failure is fixed.

    MechanicalFailure(String name) {

        // Initialize data members
        this.name = name;
        fixed = false;

        // workHours random from 1 to 50 hours.
        totalWorkHours = ((float) Math.random() * (50F - 1F)) + 1F;
        remainingWorkHours = totalWorkHours;
    }

    /** Returns the name of the failure. */
    public String getName() {
        return name;
    }

    /** Returns true if mechanical failure is fixed. */
    public boolean isFixed() {
        return fixed;
    }

    /** Returns the total work hours required to fix the failure. */
    public float getTotalWorkHours() {
        return totalWorkHours;
    }

    /** Returns the remaining work hours required to fix the failure. */
    public float getRemainingWorkHours() {
        return remainingWorkHours;
    }

    /** Adds some work time (in seconds) to the failure. */
    void addWorkTime(int seconds) {

        // Convert seconds to work hours.
        float hours = ((float) seconds / 60) / 60;
        remainingWorkHours -= hours;
        if (remainingWorkHours <= 0F)
            fixed = true;
    }
}

