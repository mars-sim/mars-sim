/**
 * Mars Simulation Project
 * MechanicalFailure.java
 * @version 2.72 2001-04-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The MechanicalFailure class represents a
 *  mechanical failure in a vehicle.
 */
public class MechanicalFailure {

    // Data members
    private String name; // The name of the failure.
    private double totalWorkHours; // The total number of work hours required to fix the failure. (1 - 50).
    private double remainingWorkHours; // The remaining work hours required to fix the failure. (1 - 50).
    private boolean fixed; // True when mechanical failure is fixed.

    /** Constructs a MechanicalFailure object
     *  @param name name of the mechanical failure
     */
    MechanicalFailure(String name) {

        // Initialize data members
        this.name = name;
        fixed = false;

        // workHours random from 1 to 50 hours.
        totalWorkHours = (Math.random() * (50D - 1D)) + 1D;
        remainingWorkHours = totalWorkHours;
    }

    /** Returns the name of the failure. 
     *  @return name of the mechanical failure
     */
    public String getName() {
        return name;
    }

    /** Returns true if mechanical failure is fixed. 
     *  @return true if failure is fixed
     */
    public boolean isFixed() {
        return fixed;
    }

    /** Returns the total work hours required to fix the failure. 
     *  @return the total work hours required to fix the failure
     */
    public double getTotalWorkHours() {
        return totalWorkHours;
    }

    /** Returns the remaining work hours required to fix the failure. 
     *  @return the remaining work hours required to fix the failure 
     */
    public double getRemainingWorkHours() {
        return remainingWorkHours;
    }

    /** Adds some work time (in seconds) to the failure. 
     *  @param seconds work time (in seconds)
     */
    void addWorkTime(double seconds) {

        // Convert seconds to work hours.
        double hours = (seconds / 60) / 60;
        remainingWorkHours -= hours;
        if (remainingWorkHours <= 0F)
            fixed = true;
    }
}
