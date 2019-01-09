/**
 * Mars Simulation Project
 * RobotBuilderImpl.java
 * @version 3.1.0 2017-04-16
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import org.mars_sim.msp.core.structure.Settlement;

public class RobotBuilderImpl implements RobotBuilder<Robot>{

	private Robot robot;

	//public RobotBuilderImpl() {
	//	robot = new Robot("tester", null);
	//}

	public RobotBuilderImpl(String name, Settlement settlement, RobotType robotType) {
		robot = new Robot(name, settlement, robotType);
	}

	public RobotBuilder<Robot> setRobotType(RobotType t) {
		robot.setRobotType(t);
		return this;
	}

	public RobotBuilder<Robot> setName(String n) {
		robot.setName(n);
		return this;
	}

	public RobotBuilder<Robot> setCountry(String c) {
		robot.setCountry(c);
		return this;
	}

	public RobotBuilder<Robot> setAssociatedSettlement(int s) {
		robot.setAssociatedSettlement(s);
		return this;
	}

	public RobotBuilder<Robot> setSponsor(String sponsor) {
		robot.setSponsor(sponsor);
		return this;
	}

	public Robot build() {
		return robot;
	}

}
