/**
 * Mars Simulation Project
 * PersonBuilderImpl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.training.TrainingType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.util.RandomUtil;

public class PersonBuilder {

	private int age;
	private String name;
	private Settlement settlement;
	private GenderType gender;
	private Map<String, Integer> personality;
	private String mbti;
	private NationSpec country;
	private Authority sponsor;
	private Map<NaturalAttributeType, Integer> attributeMap;
	private Map<SkillType, Integer> skillMap;

	public PersonBuilder(String name, Settlement settlement, GenderType gender) {
		this.name = name;
		this.settlement = settlement;
		this.gender = gender;
	}

	public PersonBuilder setSponsor(Authority sponsor) {
		this.sponsor = sponsor;
		return this;
	}
	
	public PersonBuilder setAge(int age) {
		this.age = age;
		return this;
	}

	public PersonBuilder setCountry(NationSpec spec) {
		this.country = spec;
		return this;
	}

	/**
	 * Sets the skills of a person
	 * 
	 * @param skillMap2
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder setSkill(Map<SkillType, Integer> skillMap2) {
		this.skillMap = skillMap2;
		return this;
	}

	private void applySkillMap(Person p) {

		if (skillMap == null || skillMap.isEmpty()) {
			buildDefaultSkills(p);
		}
		else {
			var skillMgr = p.getSkillManager();
			for(var e : skillMap.entrySet()) {
				skillMgr.addNewSkill(e.getKey(), e.getValue());
			}
		}
	}

		
	/**
	 * Sets some random skills for a person.
	 */
	private void buildDefaultSkills(Person person) {
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
	public static int getInitialSkillLevel(int level, int chance) {
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
	public PersonBuilder setPersonality(Map<String, Integer> personality, String mbti) {
		this.personality = personality;
		this.mbti = mbti;

		return this;
	}

	private void applyPersonality(Person person) {
		int scoreFromMBTI = 0;
		int scoreFromBigFive = 0;
		
		var mind = person.getMind();
		var traitMgr = mind.getTraitManager();

		if (personality == null || personality.isEmpty()) {
			traitMgr.setRandomBigFive();
		}
		else {
			for (Entry<String, Integer> e : personality.entrySet()) {
				traitMgr.setPersonalityTrait(PersonalityTraitType.fromString(e.getKey()),
						e.getValue());
			}
		}
		
		if (mbti == null) {
			mind.getMBTI().setRandomMBTI();
		}
		else {
			mind.getMBTI().setTypeString(mbti);
		}
		
		scoreFromMBTI = mind.getMBTI().getIntrovertExtrovertScore();
		scoreFromBigFive = traitMgr.getIntrovertExtrovertScore();
		
		// Call syncUpExtraversion() to sync up the extraversion score between the two
		// personality models
		if (personality != null && !personality.isEmpty() && mbti == null)
			// Use Big Five's extraversion score to derive the introvert/extrovert score in MBTI 
			mind.getMBTI().syncUpIntrovertExtravertScore(scoreFromBigFive);
		else
			// Use MBTI's introvert/extrovert score to derive the extraversion score in Big Five
			traitMgr.syncUpExtraversionScore(scoreFromMBTI);
	}
	
	/**
	 * Sets the attributes of a person
	 * 
	 * @param attribute map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder setAttribute(Map<NaturalAttributeType, Integer> attributeMap) {
		this.attributeMap = attributeMap;
		return this;
	}
	
	
	/**
	 * Modify an attribute.
	 * 
	 * @param origValue The attribute value
	 * @param modifier  a positive or negative random number ceiling 
	 */
	private static int applyAttributeModifier(int origValue, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0)
			random *= -1;
		return origValue + random;
	}
	
	/**
	 * Randomizes attributes with modifiers.
	 */
	private Map<NaturalAttributeType,Integer> buildDefaultAttributes() {
		Map<NaturalAttributeType,Integer> result = new EnumMap<>(NaturalAttributeType.class);

		// Create natural attributes using random values (averaged for bell curve around
		// 50%).
		// Note: this may change later.
		for (NaturalAttributeType attributeKey : NaturalAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 5;
			for (int y = 0; y < numberOfIterations; y++)
				attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;

			// Randomize the attributes reflective of the earth-borne Martian settlers.
			int modifier = switch(attributeKey) {
				case ACADEMIC_APTITUDE -> 20;
				case AGILITY -> 20;
				case ARTISTRY -> -10;
				case ATTRACTIVENESS -> 20;
				case CONVERSATION -> -10;
				case COURAGE -> 30;
				case EMOTIONAL_STABILITY -> 20;
				case ENDURANCE -> 5;
				case EXPERIENCE_APTITUDE -> 10;
				case LEADERSHIP -> 15;
				case SPIRITUALITY -> -15;
				case STRESS_RESILIENCE -> 25;
				case TEACHING -> 10;
				default -> 0;
			};
			if (modifier != 0) {
				attributeValue = applyAttributeModifier(attributeValue, modifier);
			}
			
			// Adjust certain attributes reflective of differences between the genders.
			if (attributeKey == NaturalAttributeType.STRENGTH) {
				if (gender == GenderType.MALE) {
					attributeValue = applyAttributeModifier(attributeValue, RandomUtil.getRandomInt(20));
				}
				else {
					attributeValue = applyAttributeModifier(attributeValue, -RandomUtil.getRandomInt(15));
				}
			}

			result.put(attributeKey, attributeValue);
		}
		return result;
	}
	
	public Person build() {
		if (age <= 0) {
			age = RandomUtil.getRandomInt(21, 65);
		}
		PopulationCharacteristics ethnicity = null;
		if (country != null) {
			ethnicity = country.getPopulation();
		}
		if (attributeMap == null) {
			attributeMap = buildDefaultAttributes();
		}
		
		Person p = new Person(name, settlement, gender, age, ethnicity, attributeMap);

		applyPersonality(p);
		applySkillMap(p);
		if (sponsor != null) {
			p.setSponsor(sponsor);
		}
		if (country != null) {
			p.setCountry(country.getName());
		}
		return p;
	}
}
