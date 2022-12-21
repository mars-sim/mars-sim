/*
 * Mars Simulation Project
 * RobotBuilderImpl.java
 * @date 2022-09-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;

public class RobotBuilderImpl implements RobotBuilder<Robot>{

	private Robot robot;

	public RobotBuilderImpl(String name, Settlement settlement, RobotSpec spec) {
		robot = new Robot(name, settlement, spec);
	}

	public RobotBuilder<Robot> setCountry(String c) {
		robot.setCountry(c);
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
