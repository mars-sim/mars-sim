/**
 * Mars Simulation Project
 * NaturalAttributeManager.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Hashtable;

import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The NaturalAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class NaturalAttributeManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** List of the person's natural attributes keyed by unique name. */
	private Hashtable<NaturalAttributeType, Integer> attributeList;

	/**
	 * Constructor.
	 * 
	 * @param person the person with the attributes.
	 */
	NaturalAttributeManager(Person person) {

		attributeList = new Hashtable<NaturalAttributeType, Integer>();

		// Create natural attributes using random values (averaged for bell curve around
		// 50%).
		// Note: this may change later.
		for (NaturalAttributeType attributeKey : NaturalAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++)
				attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeList.put(attributeKey, attributeValue);
		}

		// Modify the attributes reflective of Martian settlers.
		addAttributeModifier(NaturalAttributeType.ACADEMIC_APTITUDE, 30);
		addAttributeModifier(NaturalAttributeType.AGILITY, 30);
		//addAttributeModifier(NaturalAttributeType.ARTISTRY, 20);
		addAttributeModifier(NaturalAttributeType.COURAGE, 40);
		addAttributeModifier(NaturalAttributeType.ATTRACTIVENESS, 20);
		addAttributeModifier(NaturalAttributeType.EMOTIONAL_STABILITY, 30);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 5);
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 10);
		addAttributeModifier(NaturalAttributeType.LEADERSHIP, 20);
		//addAttributeModifier(NaturalAttributeType.SPIRITUALITY, 60);
		addAttributeModifier(NaturalAttributeType.STRENGTH, 20);
		addAttributeModifier(NaturalAttributeType.STRESS_RESILIENCE, 40);
		//addAttributeModifier(NaturalAttributeType.TEACHING, 20);

		// Adjust certain attributes reflective of differences between the genders.
		// TODO: Do more research on this and cite references if possible.
		if (person.getGender() == GenderType.MALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, 20);
		} else if (person.getGender() == GenderType.FEMALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, -20);
			addAttributeModifier(NaturalAttributeType.ENDURANCE, 20);
		}
	}


	/**
	 * Adds a random modifier to an attribute.
	 * 
	 * @param attributeName the name of the attribute
	 * @param modifier      the random ceiling of the modifier
	 */
	private void addAttributeModifier(NaturalAttributeType attributeName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0)
			random *= -1;
		setAttribute(attributeName, getAttribute(attributeName) + random);
	}

	/**
	 * Returns the number of natural attributes.
	 * 
	 * @return the number of natural attributes
	 */
	public int getAttributeNum() {
		return attributeList.size();
	}

	/**
	 * Gets the integer value of a named natural attribute if it exists. Returns 0
	 * otherwise.
	 * 
	 * @param attribute {@link NaturalAttributeType} the attribute
	 * @return the value of the attribute
	 */
	public int getAttribute(NaturalAttributeType attribute) {
		int result = 0;
		if (attributeList.containsKey(attribute))
			result = attributeList.get(attribute);
		return result;
	}

	/**
	 * Sets an attribute's level.
	 * 
	 * @param attrib {@link NaturalAttributeType} the attribute
	 * @param level  the level the attribute is to be set
	 */
	public void setAttribute(NaturalAttributeType attrib, int level) {
		if (level > 100)
			level = 100;
		if (level < 0)
			level = 0;
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