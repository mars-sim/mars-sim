/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LifeSupport class is a building function for life support and managing
 * inhabitants.
 */
public class LifeSupport extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(LifeSupport.class.getName());

//	private DecimalFormat fmt = new DecimalFormat("#.#######");

	private static final FunctionType THE_FUNCTION = FunctionType.LIFE_SUPPORT;

	// Data members
	private int occupantCapacity;

	private double powerRequired;
	private double length;
	private double width;
	protected double floorArea;

	private Building building;

	private Collection<Person> occupants;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public LifeSupport(Building building) {
		// Call Function constructor.
		super(THE_FUNCTION, building);
		// Each building has its own instance of LifeSupport
		this.building = building;

		occupants = new ConcurrentLinkedQueue<Person>();

		// Set occupant capacity.
		occupantCapacity = buildingConfig.getLifeSupportCapacity(building.getBuildingType());

		powerRequired = buildingConfig.getLifeSupportPowerRequirement(building.getBuildingType());

		length = building.getLength();
		width = building.getWidth();
		floorArea = length * width;

	}

	/**
	 * Alternate constructor (for use by Mock Building in Unit testing) with given
	 * occupant capacity and power required
	 * 
	 * @param building         the building this function is for.
	 * @param occupantCapacity the number of occupants this building can hold.
	 * @param powerRequired    the power required (kW)
	 */
	public LifeSupport(Building building, int occupantCapacity, double powerRequired) {
		// Use Function constructor
		super(THE_FUNCTION, building);

		occupants = new ConcurrentLinkedQueue<Person>();

		this.occupantCapacity = occupantCapacity;
		this.powerRequired = powerRequired;
		this.building = building;

		length = building.getLength();
		width = building.getWidth();
		floorArea = length * width;
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		// Demand is 2 occupant capacity for every inhabitant.
		double demand = settlement.getNumCitizens() * 2D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(THE_FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				LifeSupport lsFunction = building.getLifeSupport();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += lsFunction.occupantCapacity * wearModifier;
			}
		}

		double occupantCapacityValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double occupantCapacity = config.getLifeSupportCapacity(buildingName);

		double result = occupantCapacity * occupantCapacityValue;

		// Subtract power usage cost per sol.
		double power = config.getLifeSupportPowerRequirement(buildingName);
//		double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double powerPerSol = power * MarsClock.HOURS_PER_MILLISOL * 1000D;
		double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue() / 1000D;
		result -= powerValue;

		if (result < 0D)
			result = 0D;

		return result;
	}

	/**
	 * Gets the building's capacity for supporting occupants.
	 * 
	 * @return number of inhabitants.
	 */
	public int getOccupantCapacity() {
		return occupantCapacity;
	}

	/**
	 * Gets the current number of occupants in the building.
	 * 
	 * @return occupant number
	 */
	public int getOccupantNumber() {
		return occupants.size();
	}

	/**
	 * Gets the available occupancy room.
	 * 
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = occupantCapacity - getOccupantNumber();
		if (available > 0)
			return available;
		else
			return 0;
	}

	/**
	 * Checks if the building contains a particular unit.
	 * 
	 * @return true if unit is in building.
	 */
	public boolean containsOccupant(Person person) {
		return occupants.contains(person);
	}

	/**
	 * Gets a collection of occupants in the building.
	 * 
	 * @return collection of occupants
	 */
	public Collection<Person> getOccupants() {
		return new ConcurrentLinkedQueue<Person>(occupants);
	}

	/**
	 * Adds a person to the building. Note: building occupant capacity can be
	 * exceeded but stress levels in the building will increase. (todo: add stress
	 * later)
	 * 
	 * @param person new person to add to building.
	 */
	public void addPerson(Person person) {
		if (!occupants.contains(person)) {
			// Remove person from any other inhabitable building in the settlement.
			Iterator<Building> i = building.getBuildingManager().getBuildings().iterator(); // getACopyOfBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(THE_FUNCTION)) {
					// remove this person from this building first
					BuildingManager.removePersonOrRobotFromBuilding(person, building);
				}
			}

			// Add person to this building.
			occupants.add(person);
			logger.finest("Adding " + person + " to " + building + " life support.");
		} else {
			throw new IllegalStateException("Person already occupying building.");
		}
	}

	/**
	 * Removes a person from the building.
	 * 
	 * @param occupant the person to remove from building.
	 */
	public void removePerson(Person occupant) {
		if (occupants.contains(occupant)) {
			occupants.remove(occupant);
			logger.finest("Removing " + occupant + " from " + building + " life support.");
		} else {
			throw new IllegalStateException("Person does not occupy building.");
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		// logger.info("timePassing() : building is " + building.getName());

		// TODO: Skip calling for thermal control for Hallway ?

		if (occupants != null && occupants.size() > 0) {
			// Make sure all occupants are actually in settlement inventory.
			// If not, remove them as occupants.
			Iterator<Person> i = occupants.iterator();
			while (i.hasNext()) {
				if (!building.getInventory().containsUnit(i.next()))
					i.remove();
			}
		}

		// Add stress if building is overcrowded.
		int overcrowding = getOccupantNumber() - occupantCapacity;
		if (overcrowding > 0) {

			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Overcrowding at " + building);
			}

			double stressModifier = .1D * overcrowding * time;

			if (occupants != null) {
				Iterator<Person> j = getOccupants().iterator();
				while (j.hasNext()) {
					PhysicalCondition condition = j.next().getPhysicalCondition();
					condition.setStress(condition.getStress() + stressModifier);
				}
			}
		}
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired; // + heating.getFullPowerRequired());
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	@Override
	public double getMaintenanceTime() {
		return occupantCapacity * 10D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void destroy() {
		super.destroy();

		building = null;
		occupants.clear();
		occupants = null;
	}
}