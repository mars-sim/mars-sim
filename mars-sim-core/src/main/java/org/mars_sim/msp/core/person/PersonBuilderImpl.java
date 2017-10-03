/**
 * Mars Simulation Project
 * PersonBuilderImpl.java
 * @version 3.1.0 2017-04-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.structure.Settlement;

public class PersonBuilderImpl implements PersonBuilder<Person>{

	private Person person;

	public PersonBuilderImpl() {
		person = new Person("tester", null);
	}

	public PersonBuilderImpl(String name, Settlement settlement) {
		person = new Person(name, settlement);
	}

	public PersonBuilder<Person> setGender(GenderType g) {
		person.setGender(g);
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

	public PersonBuilder<Person> setAssociatedSettlement(Settlement s) {
		person.setAssociatedSettlement(s);
		return this;
	}

	public PersonBuilder<Person> setSponsor(String sponsor) {
		person.setSponsor(sponsor);
		return this;
	}

	public Person build() {
		return person;
	}

}
