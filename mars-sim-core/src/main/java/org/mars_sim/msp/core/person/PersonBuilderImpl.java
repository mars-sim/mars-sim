/**
 * Mars Simulation Project
 * PersonBuilderImpl.java
 * @version 3.1.0 2017-04-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.PersonalityTraitType;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;

public class PersonBuilderImpl implements PersonBuilder<Person> {

	private Person person;

	public PersonBuilderImpl() {
		person = new Person("tester", null);
	}

	public PersonBuilderImpl(String name, Settlement settlement) {
		person = new Person(name, settlement);
	}

	public PersonBuilder<Person> setGender(GenderType gender) {
		person.setGender(gender);
		return this;
	}

	public PersonBuilder<Person> setName(String n) {
		person.setName(n);
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

	public PersonBuilder<Person> setSponsor(String sponsor) {
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
			person.getSkillManager().setRandomSkills();
		} else {
			Iterator<String> i = skillMap.keySet().iterator();
			while (i.hasNext()) {
				String skillName = i.next();
				int level = skillMap.get(skillName);
				person.getSkillManager().addNewSkill(new Skill(SkillType.valueOfIgnoreCase(skillName), level));
			}
		}
		return this;
	}

	/**
	 * Sets the personality of a person
	 * 
	 * @param map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setPersonality(Map<String, Integer> map, String mbti) {
		int introvertFromMBTIscore = 0;
		int introvertFromBigFivescore = 0;
		
		if (map == null || map.isEmpty()) {
			person.getMind().getTraitManager().setRandomBigFive();
		} else {
			for (String type : map.keySet()) {
				int value = map.get(type);
				person.getMind().getTraitManager().setPersonalityTrait(PersonalityTraitType.fromString(type),
						value);
			}
		}
		
		if (mbti == null) {
			person.getMind().getMBTI().setRandomMBTI();
		}
		else {
			person.getMind().getMBTI().setTypeString(mbti);
		}
		
		introvertFromMBTIscore = person.getMind().getMBTI().getIntrovertExtrovertScore();
		introvertFromBigFivescore = person.getMind().getTraitManager().getIntrovertExtrovertScore();
		
		// Call syncUpExtraversion() to sync up the extraversion score between the two
		// personality models
		if (map != null && !map.isEmpty() && mbti == null)
			// Use Big Five's extraversion score in MBTI 
			person.getMind().getMBTI().syncUpExtraversion(introvertFromBigFivescore);
		else
			// Use MBTI's extraversion score in Big Five
			person.getMind().getTraitManager().syncUpExtraversion(introvertFromMBTIscore);
		
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
			person.getNaturalAttributeManager().setRandomAttributes(person);	
		}
		else {
			Iterator<String> i = attributeMap.keySet().iterator();
			while (i.hasNext()) {
				String attributeName = i.next();
				int value = (Integer) attributeMap.get(attributeName);
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
