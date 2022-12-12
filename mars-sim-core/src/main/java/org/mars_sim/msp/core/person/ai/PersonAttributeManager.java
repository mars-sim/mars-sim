/**
 * Mars Simulation Project
 * PersonAttributeManager.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.ai;

import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.tool.RandomUtil;

public class PersonAttributeManager extends NaturalAttributeManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Person specific Natural Attributes.
	 */
	private static final NaturalAttributeType[] PERSON_ATTRIBUTES = {
			NaturalAttributeType.ACADEMIC_APTITUDE, NaturalAttributeType.AGILITY,
			NaturalAttributeType.ARTISTRY, NaturalAttributeType.ATTRACTIVENESS,
			NaturalAttributeType.CONVERSATION, NaturalAttributeType.COURAGE,
			NaturalAttributeType.EMOTIONAL_STABILITY, NaturalAttributeType.ENDURANCE,
			NaturalAttributeType.EXPERIENCE_APTITUDE, NaturalAttributeType.LEADERSHIP,
			NaturalAttributeType.SPIRITUALITY, NaturalAttributeType.STRENGTH,
			NaturalAttributeType.STRESS_RESILIENCE, NaturalAttributeType.TEACHING
		};

	/**
	 * Sets some random attributes
	 */
	public void setRandomAttributes(Person person) {
		// Create natural attributes using random values (averaged for bell curve around
		// 50%).
		// Note: this may change later.
		for (NaturalAttributeType attributeKey : PERSON_ATTRIBUTES) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++)
				attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			setAttribute(attributeKey, attributeValue);
		}

		// Randomize the attributes reflective of the first generation Martian settlers.
		addAttributeModifier(NaturalAttributeType.ACADEMIC_APTITUDE, 10);
		addAttributeModifier(NaturalAttributeType.AGILITY, 30);
		addAttributeModifier(NaturalAttributeType.ARTISTRY, -10);
		addAttributeModifier(NaturalAttributeType.COURAGE, 30);
		addAttributeModifier(NaturalAttributeType.ATTRACTIVENESS, 20);
		
		addAttributeModifier(NaturalAttributeType.CONVERSATION, -10);
		addAttributeModifier(NaturalAttributeType.EMOTIONAL_STABILITY, 20);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 5);
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 10);
		addAttributeModifier(NaturalAttributeType.LEADERSHIP, 20);
		
		addAttributeModifier(NaturalAttributeType.SPIRITUALITY, 10);
		addAttributeModifier(NaturalAttributeType.STRENGTH, 5);
		addAttributeModifier(NaturalAttributeType.STRESS_RESILIENCE, 30);
		addAttributeModifier(NaturalAttributeType.TEACHING, 10);

		// Adjust certain attributes reflective of differences between the genders.
		if (person.getGender() == GenderType.MALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, RandomUtil.getRandomInt(20));
		} else if (person.getGender() == GenderType.FEMALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, -RandomUtil.getRandomInt(20));
		}
	}
}
