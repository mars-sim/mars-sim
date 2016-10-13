/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 3.07 2015-02-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LifeSupport class is a building function for life support and managing inhabitants.
 */
public class LifeSupport
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(LifeSupport.class.getName());

	DecimalFormat fmt = new DecimalFormat("#.#######");

	private static final BuildingFunction FUNCTION = BuildingFunction.LIFE_SUPPORT;

	// Data members
	private int occupantCapacity;

	private double powerRequired;

  	private double length;
  	private double width ;

  	protected double floorArea;

  	private String buildingType;

 	//protected ThermalGeneration furnace;
	private Building building;
	private Heating heating;

	private Inventory inv;
	private Collection<Person> occupants;
	//private Collection<Robot> robotOccupants;

	/**
	 * Constructor.
	 * @param building the building this function is for.
	 */
	public LifeSupport(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);
		// Each building has its own instance of LifeSupport
        // System.out.println("Calling LifeSupport's constructor");

		this.building = building;

		occupants = new ConcurrentLinkedQueue<Person>();
		//robotOccupants = new ConcurrentLinkedQueue<Robot>();

		inv = building.getSettlementInventory();

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		// Set occupant capacity.
		occupantCapacity = config.getLifeSupportCapacity(building.getBuildingType());

		powerRequired = config.getLifeSupportPowerRequirement(building.getBuildingType());

		length = building.getLength();
		width = building.getWidth() ;
		buildingType =  building.getBuildingType();
		floorArea = length * width ;

		this.buildingType = building.getBuildingType();
	}

	/**
	 * Alternate constructor (for use by Mock Building in Unit testing) with given occupant capacity and power required
	 * @param building the building this function is for.
	 * @param occupantCapacity the number of occupants this building can hold.
	 * @param powerRequired the power required (kW)
	 */
	public LifeSupport(Building building, int occupantCapacity, double powerRequired) {
		// Use Function constructor
		super(FUNCTION, building);

		occupants = new ConcurrentLinkedQueue<Person>();
		//robotOccupants = new ConcurrentLinkedQueue<Robot>();

		this.occupantCapacity = occupantCapacity;
		this.powerRequired = powerRequired;

		this.building = building;

		length = building.getLength();
		width = building.getWidth() ;
		buildingType =  building.getBuildingType();
		floorArea = length * width ;

		this.buildingType = building.getBuildingType();
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		// Demand is 2 occupant capacity for every inhabitant.
		double demand = settlement.getAllAssociatedPeople().size() * 2D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				LifeSupport lsFunction = (LifeSupport) building.getFunction(FUNCTION);
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
		double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double powerPerSol = power * hoursInSol;
		double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue() / 1000D;
		result -= powerValue;

		if (result < 0D) result = 0D;

		return result;
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

	//public int getRobotOccupantNumber() {
	//	return robotOccupants.size();
	//}

	/**
	 * Gets the available occupancy room.
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = occupantCapacity - getOccupantNumber();
		if (available > 0) return available;
		else return 0;
	}

	/**
	 * Checks if the building contains a particular unit.
	 * @return true if unit is in building.
	 */
	public boolean containsOccupant(Person person) {
        return occupants.contains(person);
	}

	//public boolean containsRobotOccupant(Robot robot) {
	//	return robotOccupants.contains(robot);
	//}

	/**
	 * Gets a collection of occupants in the building.
	 * @return collection of occupants
	 */
	public Collection<Person> getOccupants() {
		return new ConcurrentLinkedQueue<Person>(occupants);
	}

	/**
	 * Gets a collection of robotOccupants in the building.
	 * @return collection of robotOccupants
	 */
	//public Collection<Robot> getRobotOccupants() {
	//	return new ConcurrentLinkedQueue<Robot>(robotOccupants);
	//}


	/**
	 * Adds a person to the building.
	 * Note: building occupant capacity can be exceeded but stress levels
	 * in the building will increase.
	 * (todo: add stress later)
	 * @param person new person to add to building.
	 */
	public void addPerson(Person person) {
		if (!occupants.contains(person)) {
			// Remove person from any other inhabitable building in the settlement.
			Iterator<Building> i = building.getBuildingManager().getACopyOfBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(FUNCTION)) {
					BuildingManager.removePersonOrRobotFromBuilding(person, building);
				}
			}

			// Add person to this building.
			logger.finest("Adding " + person + " to " + building + " life support.");
			occupants.add(person);
		}
		else {
			throw new IllegalStateException("Person already occupying building.");
		}
	}

	/**
	 * Removes a person from the building.
	 * @param occupant the person to remove from building.
	 */
	public void removePerson(Person occupant) {
		if (occupants.contains(occupant)) {
		    occupants.remove(occupant);
		    logger.finest("Removing " + occupant + " from " + building + " life support.");
		}
		else {
			throw new IllegalStateException("Person does not occupy building.");
		}
	}


	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 */
	// 2014-10-25 Currently skip calling for thermal control for Hallway
	public void timePassing(double time) {
		//logger.info("timePassing() : building is " + building.getName());
		// Make sure all occupants are actually in settlement inventory.
		// If not, remove them as occupants.
		if (inv == null)
			inv = building.getSettlementInventory();

		if (occupants != null)
			if (occupants.size() > 0) {
				Iterator<Person> i = occupants.iterator();
				while (i.hasNext()) {
					if (!inv.containsUnit(i.next()))
						i.remove();
				}
			}
/*
		if (robotOccupants != null)
			if (robotOccupants.size() > 0) {
				Iterator<Robot> ii = robotOccupants.iterator();
				while (ii.hasNext()) {
					if (!inv.containsUnit(ii.next()))
						ii.remove();
				}
			}
*/
		// Add stress if building is overcrowded.
		int overcrowding = getOccupantNumber() - occupantCapacity;
		if (overcrowding > 0) {

			if(logger.isLoggable(Level.FINEST)){
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
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired; // + heating.getFullPowerRequired());
	}

	/**
	 * Gets the amount of power required when function is at power down level.
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
		heating = null;
		inv = null;
		occupants.clear();
		occupants = null;
	}
}