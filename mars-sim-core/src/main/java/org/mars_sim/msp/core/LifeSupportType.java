/**
 * Mars Simulation Project
 * LifeSupportType.java
 * @version 3.1.0 2017-03-08
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

import org.mars_sim.msp.core.resource.AmountResource;

/**
 * This interface represents a Life Support system that provides Oxygen, Water
 * Temperature and Air Pressure to a Person. Implementations of this interface
 * are used by the PhysicalCondition entity.
 * @see org.mars_sim.msp.core.person.PhysicalCondition
 */
public interface LifeSupportType {

	public static final String OXYGEN = "oxygen";
	public static final String WATER = "water";
	public static final String FOOD = "food";
	public static final String CO2 = "carbon dioxide";

	public static AmountResource foodAR = AmountResource.foodAR;//findAmountResource(LifeSupportType.FOOD); //foodAR;//
	public static AmountResource oxygenAR = AmountResource.oxygenAR;//findAmountResource(LifeSupportType.OXYGEN); //oxygenAR;//
	public static AmountResource waterAR = AmountResource.waterAR;//findAmountResource(LifeSupportType.WATER); //waterAR;//
	public static AmountResource carbonDioxideAR = AmountResource.carbonDioxideAR;//findAmountResource(LifeSupportType.CO2); //carbonDioxideAR;//

	/**
	 * Returns true if life support is working properly and is not out
	 * of oxygen or water.
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck();

	/** Gets the number of people the life support system can provide for.
	 *  @return the capacity of the life support system
	 */
	public int getLifeSupportCapacity();

	/** Gets oxygen from system.
	 *  @param amountRequested the amount of oxygen requested from system (kg)
	 *  @return the amount of oxygen actually received from system (kg)
	 *  @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested);

	/** Gets water from system.
	 *  @param amountRequested the amount of water requested from system (kg)
	 *  @return the amount of water actually received from system (kg)
	 *  @throws Exception if error providing water.
	 */
	public double provideWater(double amountRequested);

	/** Gets the air pressure of the life support system.
	 *  @return air pressure (Pa)
	 */
	public double getAirPressure();

	/** Gets the temperature of the life support system.
	 *  @return temperature (degrees C)
	 */
	public double getTemperature();
}
