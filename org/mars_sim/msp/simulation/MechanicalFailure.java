/**
 * Mars Simulation Project
 * MechanicalFailure.java
 * @version 2.72 2001-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The MechanicalFailure class represents a
 *  mechanical failure in a vehicle.
 */
public class MechanicalFailure {

    // Data members
    private String name; // The name of the failure.
    private double totalWorkTime; // The total amount of work time required to fix the failure. (40 - 2000 millisols).
    private double remainingWorkTime; // The remaining work time left to fix the failure. (40 - 2000 millisols).
    private boolean fixed; // True when mechanical failure is fixed.

    /** Constructs a MechanicalFailure object
     *  @param name name of the mechanical failure
     */
    MechanicalFailure(String name) {

        // Initialize data members
        this.name = name;
        fixed = false;

        // Work time random from 40 to 2000 millisols.
        totalWorkTime = RandomUtil.getRandomInt(40, 2000);
        remainingWorkTime = totalWorkTime;
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

    /** Returns the total work time required to fix the failure. 
     *  @return the total work time (in millisols)
     */
    public double getTotalWorkTime() {
        return totalWorkTime;
    }

    /** Returns the remaining work time required to fix the failure. 
     *  @return the remaining work time (in millisols)
     */
    public double getRemainingWorkTime() {
        return remainingWorkTime;
    }

    /** Adds some work time to the failure. 
     *  @param time work time (in millisols)
     */
    public void addWorkTime(double time) {
        remainingWorkTime -= time;
        if (remainingWorkTime <= 0F)
            fixed = true;
    }
}
