/**
 * Mars Simulation Project
 * Malfunctionable.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Inventory;
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
	 * Gets the unique/nickname of the malfunctionable entity if it's a building.
	 * 
	 * @return nickname
	 */
	public String getNickName();

	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople();

	/**
	 * Gets the inventory associated with this entity.
	 * 
	 * @return inventory
	 */
	public Inventory getInventory();

	/**
	 * Gets the short location name associated with this entity.
	 * 
	 * @return location
	 */
	public String getImmediateLocation();

	/**
	 * Gets the long location name associated with this entity.
	 * 
	 * @return location
	 */
	public String getLocale();

	/**
	 * Get the Settlement associated with the malfunctioning entity.
	 * @return
	 */
	public Settlement getAssociatedSettlement();
}
