/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 2.74 2002-01-15
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation;

/**
 * This interface represents a Life Support system that provide Oxygen, Water
 * and Food to a Person. Implementations of this interface are used by the
 * PhysicalCondition entity.
 *
 * @see org.mars_sim.msp.simulation.person.PhysicalCondition
 */
public interface LifeSupport {

    /** Removes food from system.
     *  @param amount the amount of food requested from system (kg)
     *  @return the amount of food actually received from system (kg)
     */
    public double removeFood(double amount);

    /** Removes oxygen from system.
     *  @param amount the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     */
    public double removeOxygen(double amount);

    /** Removes water from system.
     *  @param amount the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double removeWater(double amount);
}