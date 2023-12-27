/*
 * Mars Simulation Project
 * RoboticAttributeManager.java
 * @date 2023-11-30
 * @author Manny Kung
 */

package com.mars_sim.core.robot;


import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The RoboticAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class RoboticAttributeManager extends NaturalAttributeManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Specific Natural Attributes for bots.
	 */
	private static final NaturalAttributeType[] ROBOT_ATTRIBUTES = {
												NaturalAttributeType.AGILITY,
												NaturalAttributeType.CONVERSATION,
												NaturalAttributeType.ENDURANCE,
												NaturalAttributeType.EXPERIENCE_APTITUDE,
												NaturalAttributeType.STRENGTH,
												NaturalAttributeType.TEACHING
		};
	

	/**
	 * Randomizes attributes with modifiers.
	 */
	public void	setRandomAttributes() {		
		// Create attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (NaturalAttributeType type : ROBOT_ATTRIBUTES) {
			int attributeValue = 0;
			int numberOfIterations = 5;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			setAttribute(type, attributeValue);
		}

		// TODO: need to overhaul and define the use of attributes for robots.
		addAttributeModifier(NaturalAttributeType.AGILITY, 50);
		addAttributeModifier(NaturalAttributeType.CONVERSATION, 5);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 50);
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 50);
		addAttributeModifier(NaturalAttributeType.STRENGTH, 50);
		addAttributeModifier(NaturalAttributeType.TEACHING, 5);
	}
}
