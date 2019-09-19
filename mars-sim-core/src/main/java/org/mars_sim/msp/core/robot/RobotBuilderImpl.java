/**
 * Mars Simulation Project
 * RobotBuilderImpl.java
 * @version 3.1.0 2017-04-16
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillType;
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

	public RobotBuilder<Robot> setSkill(Map<String, Integer> skillMap, RobotType t) {
		if (skillMap == null || skillMap.isEmpty()) {
			robot.getSkillManager().setRandomBotSkills(t);
		}
		else {
			Iterator<String> i = skillMap.keySet().iterator();
			while (i.hasNext()) {
				String skillName = i.next();
				int level = skillMap.get(skillName);
				robot.getSkillManager()
						.addNewSkill(new Skill(SkillType.valueOfIgnoreCase(skillName), level));
			}
		}
		return this;
	}
	
	/**
	 * Sets the attributes of a robot
	 * 
	 * @param attribute map
	 * @return {@link RobotBuilder<>}
	 */
	public RobotBuilder<Robot> setAttribute(Map<String, Integer> attributeMap) {	
		if (attributeMap == null || attributeMap.isEmpty()) {
			robot.getRoboticAttributeManager().setRandomAttributes();	
		}
		else {
			Iterator<String> i = attributeMap.keySet().iterator();
			while (i.hasNext()) {
				String attributeName = i.next();
				int value = (Integer) attributeMap.get(attributeName);
				robot.getRoboticAttributeManager()
						.setAttribute(RoboticAttributeType.valueOfIgnoreCase(attributeName), value);
			}
		}
		return this;
	}
	
	public Robot build() {
		return robot;
	}

}
