/**
 * Mars Simulation Project
 * PersonBuilder.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import java.util.Map;

import com.mars_sim.core.authority.Authority;

public interface PersonBuilder {

	public PersonBuilder setAge(int age);

	public PersonBuilder setCountry(NationSpec spec);

	public PersonBuilder setSponsor(Authority sponsor);
	
	/**
	 * Sets the skills of a person
	 * 
	 * @param skillMap
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder setSkill(Map<String, Integer> skillMap);
	
	/**
	 * Sets the personality of a person
	 * 
	 * @param map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder setPersonality(Map<String, Integer> map, String mbti);

	/**
	 * Sets the attributes of a person
	 * 
	 * @param attribute map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder setAttribute(Map<String, Integer> attributeMap);
	
	public Person build();
}
