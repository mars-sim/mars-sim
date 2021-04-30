package org.mars_sim.msp.core.person.ai.task.utils;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

public interface Worker extends Loggable {

	/**
	 * Returns a reference to the Worker natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	public NaturalAttributeManager getNaturalAttributeManager();
	
	/**
	 * Returns a reference to the Person's skill manager
	 * 
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager();

	/**
	 * Get the workers name
	 * @return
	 */
	public String getName();

	/**
	 * What is the Worker doing
	 * @return
	 */
	public String getTaskDescription();

	/**
	 * Where the the Worker positioned?
	 * @return
	 */
	public Coordinates getCoordinates();

	/**
	 * How efficient is this Worker
	 * @return
	 */
	public double getPerformanceRating();

	/**
	 * Get the current Settlement of the worker; may be different from the assoicated Settlement.
	 * @return
	 */
	public Settlement getSettlement();

	/**
	 * What is the top level container of this worker; this will be a Unit that is on the MarsSurface,
	 * e.g. Vehicle or Settlement.
	 * @return
	 */
	public Unit getTopContainerUnit();

	/**
	 * Get vehicle member is in, null if member is not in vehicle
	 * 
	 * @return the member's vehicle
	 */
	public Vehicle getVehicle();

	/**
	 * Is the member in a settlement
	 * 
	 * @return true if the member in a settlement
	 */
	public boolean isInSettlement();

	/**
	 * Is the member in a vehicle
	 * 
	 * @return true if the member in a vehicle
	 */
	public boolean isInVehicle();
	
	/**
	 * Adds a unit listener
	 * 
	 * @param newListener the listener to add.
	 */
	public void addUnitListener(UnitListener newListener);

	/**
	 * Removes a unit listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public void removeUnitListener(UnitListener oldListener);

	/**
	 * Sets the person's current mission.
	 * 
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission);
}
