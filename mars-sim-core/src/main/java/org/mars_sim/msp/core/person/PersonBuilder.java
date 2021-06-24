/**
 * Mars Simulation Project
 * PersonBuilder.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.Map;

import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

public interface PersonBuilder<T> {

	public PersonBuilder<Person> setGender(GenderType g);

	public PersonBuilder<Person> setAge(int age);
	
	public PersonBuilder<Person> setName(String name);

	public PersonBuilder<Person> setCountry(String c);

	public PersonBuilder<Person> setAssociatedSettlement(int s);

	public PersonBuilder<Person> setSponsor(ReportingAuthorityType sponsor);
	
	/**
	 * Sets the skills of a person
	 * 
	 * @param skillMap
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setSkill(Map<String, Integer> skillMap);
	
	/**
	 * Sets the personality of a person
	 * 
	 * @param map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setPersonality(Map<String, Integer> map, String mbti);

	/**
	 * Sets the attributes of a person
	 * 
	 * @param attribute map
	 * @return {@link PersonBuilder<>}
	 */
	public PersonBuilder<Person> setAttribute(Map<String, Integer> attributeMap);
	
	public T build();
}
