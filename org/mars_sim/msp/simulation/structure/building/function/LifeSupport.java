/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The LifeSupport class is a building function for life support and managing inhabitants.
 */
public class LifeSupport extends Function implements Serializable {

	public static final String NAME = "Life Support";
	
	// Data members
	private int occupantCapacity;
	private double powerRequired;
	private PersonCollection occupants;

	/**
	 * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
	 */
	public LifeSupport(Building building) throws BuildingException {
		// Call Function constructor.
		super(NAME, building);
		
		occupants = new PersonCollection();
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
		
		try {
			// Set occupant capacity.
			occupantCapacity = config.getLifeSupportCapacity(building.getName());
		
			// Set life support power required.
			powerRequired = config.getLifeSupportPowerRequirement(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("LifeSupport.constructor: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the building's capacity for supporting occupants.
	 * @return number of inhabitants.
	 */
	public int getOccupantCapacity() {
		return occupantCapacity;
	}
	
	/**
	 * Gets the current number of occupants in the building.
	 * @return occupant number
	 */
	public int getOccupantNumber() {
		return occupants.size();
	}

	/**
	 * Gets the available occupancy room.
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = getOccupantCapacity() - getOccupantNumber();
		if (available > 0) return available;
		else return 0;
	}
	
	/**
	 * Checks if the building contains a particular person.
	 * @return true if person is in building.
	 */
	public boolean containsPerson(Person person) {
		if (occupants.contains(person)) return true;
		else return false;
	}
	
	/**
	 * Gets a collection of occupants in the building.
	 * @return collection of occupants
	 */
	public PersonCollection getOccupants() {
		return new PersonCollection(occupants);
	}
	
	/**
	 * Adds a person to the building.
	 * Note: building occupant capacity can be exceeded but stress levels
	 * in the building will increase. 
	 * (todo: add stress later)
	 * @param person new person to add to building.
	 * @throws BuildingException if person is already building occupant.
	 */
	public void addPerson(Person person) throws BuildingException {
		if (!occupants.contains(person)) {
			// Remove person from any other inhabitable building in the settlement.
			Iterator i = getBuilding().getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				if (building.hasFunction(NAME)) {
					LifeSupport lifeSupport = (LifeSupport) building.getFunction(NAME);
					if (lifeSupport.containsPerson(person)) lifeSupport.removePerson(person);
				}
			}

			// Add person to this building.            
			occupants.add(person);
		}
		else {throw new BuildingException("Person already occupying building.");} 
	}
	
	/**
	 * Removes a person from the building.
	 * @param person occupant to remove from building.
	 * @throws BuildingException if person is not building occupant.
	 */
	public void removePerson(Person occupant) throws BuildingException {
		if (occupants.contains(occupant)) occupants.remove(occupant);
		else throw new BuildingException("Person does not occupy building.");
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {

		// Make sure all occupants are actually in settlement inventory.
		// If not, remove them as occupants.
		Inventory inv = getBuilding().getInventory();
		PersonIterator i = occupants.iterator();
		while (i.hasNext()) {
			if (!inv.containsUnit(i.next())) i.remove();
		}
		
		// Add stress if building is overcrowded.
		int overcrowding = getOccupantNumber() - getOccupantCapacity();
		if (overcrowding > 0) {
			// System.out.println("Overcrowding at " + getBuilding());
			double stressModifier = .1D * overcrowding * time;
			PersonIterator j = getOccupants().iterator();
			while (j.hasNext()) {
				PhysicalCondition condition = j.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + stressModifier);
			}
		}
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0;
	}
}