/*
 * Mars Simulation Project
 * LifeSupportInterface.java
 * @date 2022-06-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

/**
 * This interface represents a Life Support system that provides oxygen and water
 * and provides temperature and air pressure regulation to a Person. 
 * Implementations of this interface are used by the PhysicalCondition entity.
 * 
 * @see org.mars_sim.msp.core.person.PhysicalCondition
 */
public interface LifeSupportInterface {


	/**
	 * Is life support working properly ?
	 * 
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck();

	/**
	 * Gets the number of people the life support system can provide for.
	 * 
	 * @return the capacity of the life support system
	 */
	public int getLifeSupportCapacity();

	/**
	 * Gets oxygen from system.
	 * 
	 * @param amountRequested the amount of oxygen requested from system (kg)
	 * @return the amount of oxygen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested);

	/**
	 * Gets water from system.
	 * 
	 * @param amountRequested the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double amountRequested);

	/**
	 * Gets the air pressure of the life support system.
	 * 
	 * @return air pressure (Pa)
	 */
	public double getAirPressure();

	/**
	 * Gets the temperature of the life support system.
	 * 
	 * @return temperature (degrees C)
	 */
	public double getTemperature();
}
