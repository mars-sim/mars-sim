/**
 * Mars Simulation Project
 * NaturalAttributeManager.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Hashtable;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The NaturalAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class NaturalAttributeManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** List of the person's natural attributes keyed by unique name. */
	private Hashtable<NaturalAttribute, Integer> attributeList;

	/**
	 * Constructor.
	 * @param person the person with the attributes.
	 */
	NaturalAttributeManager(Person person) {

		attributeList = new Hashtable<NaturalAttribute, Integer>();

		// Create natural attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (NaturalAttribute attributeKey : NaturalAttribute.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeList.put(attributeKey, attributeValue);
		}

		// Adjust certain attributes reflective of Martian settlers.
		addAttributeModifier(NaturalAttribute.STRENGTH, 40);
		addAttributeModifier(NaturalAttribute.ENDURANCE, 40);
		addAttributeModifier(NaturalAttribute.AGILITY, 40);
		addAttributeModifier(NaturalAttribute.STRESS_RESILIENCE, 80);
		addAttributeModifier(NaturalAttribute.TEACHING, 40);
		addAttributeModifier(NaturalAttribute.ACADEMIC_APTITUDE, 80);
		addAttributeModifier(NaturalAttribute.EXPERIENCE_APTITUDE, 60);
		addAttributeModifier(NaturalAttribute.ARTISTRY, 60);
		addAttributeModifier(NaturalAttribute.SPIRITUALITY, 60);
		addAttributeModifier(NaturalAttribute.COURAGE, 60);
		addAttributeModifier(NaturalAttribute.EMOTIONAL_STABILITY, 60);

		// Adjust certain attributes reflective of differences between the genders.
		// TODO: Do more research on this and cite references if possible.
		if (person.getGender() == PersonGender.MALE) {
			addAttributeModifier(NaturalAttribute.STRENGTH, 20);
		}
		else if (person.getGender() == PersonGender.FEMALE) {
			addAttributeModifier(NaturalAttribute.STRENGTH, -20);
			addAttributeModifier(NaturalAttribute.ENDURANCE, 20);
		}
	}

	/**
	 * Constructor.
	 * @param robot the robot with the attributes.
	 */
	public NaturalAttributeManager(Robot robot) {

		attributeList = new Hashtable<NaturalAttribute, Integer>();

		// Create natural attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (NaturalAttribute attributeKey : NaturalAttribute.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeList.put(attributeKey, attributeValue);
		}

		// TODO: need to overhaul and define the use of attributes for robots.
		addAttributeModifier(NaturalAttribute.STRENGTH, 95);
		addAttributeModifier(NaturalAttribute.ENDURANCE, 95);
		addAttributeModifier(NaturalAttribute.AGILITY, 50);
		addAttributeModifier(NaturalAttribute.STRESS_RESILIENCE, 95);
		addAttributeModifier(NaturalAttribute.TEACHING, 1);
		addAttributeModifier(NaturalAttribute.ACADEMIC_APTITUDE, 80);
		addAttributeModifier(NaturalAttribute.EXPERIENCE_APTITUDE, 50);
		addAttributeModifier(NaturalAttribute.ARTISTRY, 1);
		addAttributeModifier(NaturalAttribute.SPIRITUALITY, 1);
		addAttributeModifier(NaturalAttribute.COURAGE, 1);
		addAttributeModifier(NaturalAttribute.EMOTIONAL_STABILITY, 99);
	}

	/**
	 * Adds a random modifier to an attribute.
	 * @param attributeName the name of the attribute
	 * @param modifier the random ceiling of the modifier
	 */
	private void addAttributeModifier(NaturalAttribute attributeName, int modifier) {
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
	 * @param attribute {@link NaturalAttribute} the attribute
	 * @return the value of the attribute
	 */
	public int getAttribute(NaturalAttribute attribute) {
		int result = 0;
		if (attributeList.containsKey(attribute)) result = attributeList.get(attribute);
		return result;
	}

	/**
	 * Sets an attribute's level.
	 * @param attrib {@link NaturalAttribute} the attribute
	 * @param level the level the attribute is to be set
	 */
	public void setAttribute(NaturalAttribute attrib, int level) {
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