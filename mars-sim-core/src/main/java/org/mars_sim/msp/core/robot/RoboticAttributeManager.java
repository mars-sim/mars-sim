/**
 * Mars Simulation Project
 * RoboticAttributeManager.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The RoboticAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class RoboticAttributeManager extends NaturalAttributeManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Person specific Natural Attributes.
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
	 * Sets some random attributes
	 */
	public void	setRandomAttributes() {		
		// Create attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (NaturalAttributeType type : ROBOT_ATTRIBUTES) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			setAttribute(type, attributeValue);
		}

		// TODO: need to overhaul and define the use of attributes for robots.
		addAttributeModifier(NaturalAttributeType.STRENGTH, 50);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 50);
		addAttributeModifier(NaturalAttributeType.AGILITY, 50);
		addAttributeModifier(NaturalAttributeType.TEACHING, 5);
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 50);
		addAttributeModifier(NaturalAttributeType.CONVERSATION, 5);
	}
}
