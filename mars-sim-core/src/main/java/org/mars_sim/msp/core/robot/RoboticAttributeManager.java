/**
 * Mars Simulation Project
 * RoboticAttributeManager.java
 * @version 3.08 2016-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Hashtable;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The RoboticAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class RoboticAttributeManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** List of the person's natural attributes keyed by unique name. */
	private Hashtable<RoboticAttributeType, Integer> attributeList;

	/**
	 * Constructor.
	 * @param robot the robot with the attributes.
	 */
	public RoboticAttributeManager(Robot robot) {

		attributeList = new Hashtable<RoboticAttributeType, Integer>();

		// Create natural attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (RoboticAttributeType attributeKey : RoboticAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeList.put(attributeKey, attributeValue);
		}

		// TODO: need to overhaul and define the use of attributes for robots.
		addAttributeModifier(RoboticAttributeType.STRENGTH, 50);
		addAttributeModifier(RoboticAttributeType.ENDURANCE, 50);
		addAttributeModifier(RoboticAttributeType.AGILITY, 50);
		addAttributeModifier(RoboticAttributeType.TEACHING, 5);
		addAttributeModifier(RoboticAttributeType.EXPERIENCE_APTITUDE, 50);
		addAttributeModifier(RoboticAttributeType.CONVERSATION, 5);

	}

	/**
	 * Adds a random modifier to an attribute.
	 * @param attributeName the name of the attribute
	 * @param modifier the random ceiling of the modifier
	 */
	private void addAttributeModifier(RoboticAttributeType attributeName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0) random *= -1;
		setAttribute(attributeName, getAttribute(attributeName) + random);
	}

	/**
	 * Returns the number of natural attributes.
	 * @return the number of natural attributes
	 */
	public int getAttributeNum() {
		return attributeList.size();
	}

	/**
	 * Gets the integer value of a named natural attribute if it exists.
	 * Returns 0 otherwise.
	 * @param attribute {@link RoboticAttributeType} the attribute
	 * @return the value of the attribute
	 */
	public int getAttribute(RoboticAttributeType attribute) {
		int result = 0;
		if (attributeList.containsKey(attribute)) result = attributeList.get(attribute);
		return result;
	}

	/**
	 * Sets an attribute's level.
	 * @param attrib {@link RoboticAttributeType} the attribute
	 * @param level the level the attribute is to be set
	 */
	public void setAttribute(RoboticAttributeType attrib, int level) {
		if (level > 100) level = 100;
		if (level < 0) level = 0;
		attributeList.put(attrib, level);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		attributeList.clear();
		attributeList = null;
	}
}