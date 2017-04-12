/**
 * Mars Simulation Project
 * PersonBuilder.java
 * @version 3.1.0 2017-04-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.structure.Settlement;

public interface PersonBuilder<T> {

	public PersonBuilder<Person> setGender(PersonGender g);

	public PersonBuilder<Person> setName(String name);

	public PersonBuilder<Person> setCountry(String c);

	public PersonBuilder<Person> setAssociatedSettlement(Settlement s);

	public PersonBuilder<Person> setSponsor(String sponsor);

	public T build();
}
