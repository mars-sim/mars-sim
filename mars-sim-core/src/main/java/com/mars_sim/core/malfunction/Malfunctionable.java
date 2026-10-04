/*
 * Mars Simulation Project
 * Malfunctionable.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction;

import java.util.Collection;

import com.mars_sim.core.Entity;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * The Malfunctionable interface represents a Unit that can have malfunctions.
 */
public interface Malfunctionable extends Entity {
	
	/**
	 * Gets the entity's malfunction manager.
	 * 
	 * @return malfunction manager
	 */
	MalfunctionManager getMalfunctionManager();

	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	Collection<Person> getAffectedPeople();

	/**
	 * Gets the Settlement associated with the malfunctioning entity.
	 * 
	 * @return
	 */
	Settlement getAssociatedSettlement();
}
