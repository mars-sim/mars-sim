/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 2.74 2002-01-30
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation;

/**
 * This interface represents a Life Support system that provide Oxygen, Water
 * Temperature and Air Pressure to a Person. Implementations of this interface 
 * are used by the PhysicalCondition entity.
 *
 * @see org.mars_sim.msp.simulation.person.PhysicalCondition
 */
public interface LifeSupport {

    /** Returns true if life support is working properly and is not out
     *  of oxygen or water.
     *  @return true if life support is OK
     */
    public boolean lifeSupportCheck();
	
    /** Gets the number of people the life support can provide for.
     *  @return the capacity of the life support system
     */
    public int getLifeSupportCapacity();
	
    /** Gets oxygen from system.
     *  @param amountRequested the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     */
    public double provideOxygen(double amountRequested);

    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     */
    public double provideWater(double amountRequested);

    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure();

    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature();
}
