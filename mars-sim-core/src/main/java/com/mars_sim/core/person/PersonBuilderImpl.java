/**
 * Mars Simulation Project
 * PersonBuilderImpl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import java.util.Map;
import java.util.Map.Entry;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.PersonAttributeManager;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.util.RandomUtil;

public class PersonBuilderImpl implements PersonBuilder<Person> {

	private Person person;

	public PersonBuilderImpl() {
		person = new Person("tester", null);
	}

	public PersonBuilderImpl(String name, Settlement settlement) {
		person = new Person(name, settlement);
	}

	public PersonBuilder<Person> setName(String n) {
		person.setName(n);
		return this;
	}

	public PersonBuilder<Person> setGender(GenderType gender) {
		person.setGender(gender);
		return this;
	}

	public PersonBuilder<Person> setAge(int age) {
		person.setAge(age);
		return this;
	}
	
	public PersonBuilder<Person> setCountry(String c) {
		person.setCountry(c);
		return this;
	}

	public PersonBuilder<Person> setAssociatedSettlement(int s) {
		person.setAssociatedSettlement(s);
		return this;
	}

	public PersonBuilder<Person> setSponsor(Authority sponsor) {
		person.setSponsor(sponsor);
		return this;
	}

	/**
	 * Sets the skills of a person
	 * 
	 * @param skillMap
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setSkill(Map<String, Integer> skillMap) {
		if (skillMap == null || skillMap.isEmpty()) {
			buildDefaultSkills();
		}
		else {
			for(Entry<String, Integer> e : skillMap.entrySet()) {
				SkillType sType = SkillType.valueOf(ConfigHelper.convertToEnumName(e.getKey()));
				person.getSkillManager().addNewSkill(sType, e.getValue());
			}
		}
		return this;
	}

		
	/**
	 * Sets some random skills for a person.
	 */
	private void buildDefaultSkills() {
		int ageFactor = person.getAge();
		SkillManager sm = person.getSkillManager();

		// Add starting skills randomly for a person.
		for (SkillType startingSkill : SkillType.values()) {
			int skillLevel = -1;

			switch (startingSkill) {
				case PILOTING: 
					// Checks to see if a person has a pilot license/certification
					if (person.getTrainings().contains(TrainingType.AVIATION_CERTIFICATION)) {
						skillLevel = getInitialSkillLevel(1, 35);
					}
					break;
			
				// Medicine skill is highly needed for diagnosing sickness and prescribing medication 
				case MEDICINE:
					skillLevel = getInitialSkillLevel(0, 35);
					break;

				// psychology skill is sought after for living in confined environment
				case PSYCHOLOGY: 
					skillLevel = getInitialSkillLevel(0, 35);
					break;
		
			
			// Mechanics skill is sought after for repairing malfunctions
				case MATERIALS_SCIENCE:
				case MECHANICS:
					skillLevel = getInitialSkillLevel(0, 45);
					break;

				default: {
					int rand = RandomUtil.getRandomInt(0, 3);
					if (rand == 0) {
						skillLevel = getInitialSkillLevel(0, (int)(10 + ageFactor/10.0));
					}
					else if (rand == 1) {
						skillLevel = getInitialSkillLevel(1, (int)(5 + ageFactor/8.0));
					}
					else if (rand == 2) {
						skillLevel = getInitialSkillLevel(2, (int)(2.5 + ageFactor/6.0));
					}
				} break;
			}

			// If a initial skill level then add it and assign experience
			if (skillLevel >= 0) {
				int exp = RandomUtil.getRandomInt(0, 24);
				sm.addNewSkill(startingSkill, skillLevel);
				sm.addExperience(startingSkill, exp, 0D);
			}
		}
	}

		
	/**
	 * Returns an initial skill level.
	 * 
	 * @param level  lowest possible skill level
	 * @param chance the chance that the skill will be greater
	 * @return the initial skill level
	 */
	private static int getInitialSkillLevel(int level, int chance) {
		if (RandomUtil.lessThanRandPercent(chance))
			return getInitialSkillLevel(level + 1, chance / 2);
		else
			return level;
	}

	/**
	 * Sets the personality of a person
	 * 
	 * @param map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setPersonality(Map<String, Integer> map, String mbti) {
		int scoreFromMBTI = 0;
		int scoreFromBigFive = 0;
		
		if (map == null || map.isEmpty()) {
			person.getMind().getTraitManager().setRandomBigFive();
		}
		else {
			for (Entry<String, Integer> e : map.entrySet()) {
				person.getMind().getTraitManager().setPersonalityTrait(PersonalityTraitType.fromString(e.getKey()),
						e.getValue());
			}
		}
		
		if (mbti == null) {
			person.getMind().getMBTI().setRandomMBTI();
		}
		else {
			person.getMind().getMBTI().setTypeString(mbti);
		}
		
		scoreFromMBTI = person.getMind().getMBTI().getIntrovertExtrovertScore();
		scoreFromBigFive = person.getMind().getTraitManager().getIntrovertExtrovertScore();
		
		// Call syncUpExtraversion() to sync up the extraversion score between the two
		// personality models
		if (map != null && !map.isEmpty() && mbti == null)
			// Use Big Five's extraversion score to derive the introvert/extrovert score in MBTI 
			person.getMind().getMBTI().syncUpIntrovertExtravertScore(scoreFromBigFive);
		else
			// Use MBTI's introvert/extrovert score to derive the extraversion score in Big Five
			person.getMind().getTraitManager().syncUpExtraversionScore(scoreFromMBTI);
		
		return this;
	}
	
	/**
	 * Sets the attributes of a person
	 * 
	 * @param attribute map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setAttribute(Map<String, Integer> attributeMap) {
		if (attributeMap == null || attributeMap.isEmpty()) {
			((PersonAttributeManager) person.getNaturalAttributeManager()).setRandomAttributes(person);	
		}
		else {
			for(Entry<String,Integer> e : attributeMap.entrySet()) {
				String attributeName = e.getKey();
				int value = e.getValue();
				person.getNaturalAttributeManager()
						.setAttribute(NaturalAttributeType.valueOfIgnoreCase(attributeName), value);
			}
		}
		
		return this;
	}
	
	
	
	public Person build() {
		return person;
	}

}
