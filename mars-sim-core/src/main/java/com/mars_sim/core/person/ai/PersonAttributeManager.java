/*
 * Mars Simulation Project
 * PersonAttributeManager.java
 * @date 2023-11-30
 * @author Barry Evans
 */

package com.mars_sim.core.person.ai;

import java.util.Map;

import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.tools.util.RandomUtil;

public class PersonAttributeManager extends NaturalAttributeManager {

	public PersonAttributeManager(Map<NaturalAttributeType, Integer> initialAttr) {
		super(initialAttr);
		// TODO Auto-generated constructor stub
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(PersonAttributeManager.class.getName());

	/**
	 * Randomizes attributes with modifiers.
	 */
	public void setRandomAttributes(Person person) {
		// Create natural attributes using random values (averaged for bell curve around
		// 50%).
		// Note: this may change later.
		for (NaturalAttributeType attributeKey : NaturalAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 5;
			for (int y = 0; y < numberOfIterations; y++)
				attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			setAttribute(attributeKey, attributeValue);
		}

		// Randomize the attributes reflective of the earth-borne Martian settlers.
		addAttributeModifier(NaturalAttributeType.ACADEMIC_APTITUDE, 20);
		addAttributeModifier(NaturalAttributeType.AGILITY, 20);
		addAttributeModifier(NaturalAttributeType.ARTISTRY, -10);
		addAttributeModifier(NaturalAttributeType.ATTRACTIVENESS, 20);
		
		addAttributeModifier(NaturalAttributeType.CONVERSATION, -10);
		addAttributeModifier(NaturalAttributeType.COURAGE, 30);
		addAttributeModifier(NaturalAttributeType.EMOTIONAL_STABILITY, 20);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 5);
		
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 10);
		addAttributeModifier(NaturalAttributeType.LEADERSHIP, 15);
		addAttributeModifier(NaturalAttributeType.SPIRITUALITY, -15);
		addAttributeModifier(NaturalAttributeType.STRESS_RESILIENCE, 25);
		
		addAttributeModifier(NaturalAttributeType.TEACHING, 10);

		// Adjust certain attributes reflective of differences between the genders.
		if (person.getGender() == GenderType.MALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, RandomUtil.getRandomInt(20));
		} else if (person.getGender() == GenderType.FEMALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, -RandomUtil.getRandomInt(15));
		}
	}
}
