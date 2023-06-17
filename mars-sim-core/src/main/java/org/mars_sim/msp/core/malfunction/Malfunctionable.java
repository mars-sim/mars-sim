/*
 * Mars Simulation Project
 * Malfunctionable.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Malfunctionable interface represents a Unit that can have malfunctions.
 */
public interface Malfunctionable extends Loggable, Serializable {

	/**
	 * Gets the entity's malfunction manager.
	 * 
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager();

	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople();

	/**
	 * Gets the Settlement associated with the malfunctioning entity.
	 * 
	 * @return
	 */
	public Settlement getAssociatedSettlement();
	
	
	/**
	 * Gets the unit type.
	 * 
	 * @return
	 */
	public abstract UnitType getUnitType();
}
