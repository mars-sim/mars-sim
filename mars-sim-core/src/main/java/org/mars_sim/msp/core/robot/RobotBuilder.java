/**
 * Mars Simulation Project
 * RobotBuilder.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

public interface RobotBuilder<T> {

	public RobotBuilder<Robot> setCountry(String c);

	public RobotBuilder<Robot> setSponsor(String sponsor);
	
	public T build();
}
