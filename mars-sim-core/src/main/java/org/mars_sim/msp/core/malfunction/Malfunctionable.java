/**
 * Mars Simulation Project
 * Malfunctionable.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;

import java.util.Collection;

/**
 * The Malfunctionable interface represents a Unit that can have malfunctions.
 */
public interface Malfunctionable {

	/**
	 * Gets the entity's object.
	 * 
	 * @return object
	 */
	public Unit getUnit();

	/**
	 * Gets the entity's malfunction manager.
	 * 
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager();

	/**
	 * Gets the name of the malfunctionable entity.
	 * 
	 * @return name the entity name
	 * 
	 *         public String getName();
	 */

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

}