/**
 * Mars Simulation Project
 * RobotBuilder.java
 * @version 3.1.0 2017-04-16
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import org.mars_sim.msp.core.structure.Settlement;

public interface RobotBuilder<T> {

	public RobotBuilder<Robot> setRobotType(RobotType t);

	public RobotBuilder<Robot> setName(String name);

	public RobotBuilder<Robot> setCountry(String c);

	public RobotBuilder<Robot> setAssociatedSettlement(int s);

	public RobotBuilder<Robot> setSponsor(String sponsor);

	public T build();
}
