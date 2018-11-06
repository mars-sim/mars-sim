/**
 * Mars Simulation Project
 * Administration.java
 * @version 3.1.0 2017-10-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * An administration building function. The building facilitates report writing
 * and other administrative paperwork.
 */
public class Administration extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Administration.class.getName());

	private static final FunctionType FUNCTION = FunctionType.ADMINISTRATION;
	private static BuildingConfig buildingConfig;

	// Data members
	private int populationSupport;
	private int staff;
	private int staffCapacity;

	private String buildingType;
	private Building building;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public Administration(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);
		this.building = building;

		buildingType = building.getBuildingType();
		// Populate data members.
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();

		if (buildingType.equalsIgnoreCase("Command and Control"))
			populationSupport = 16;
		else if (buildingType.equalsIgnoreCase("Lander Hab"))
			populationSupport = 8;
		else if (buildingType.equalsIgnoreCase("Outpost Hub"))
			populationSupport = 6;

		staffCapacity = buildingConfig.getAdministrationPopulationSupport(buildingType);

		// Load activity spots
		loadActivitySpots(buildingConfig.getAdministrationActivitySpots(buildingType));
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		// Settlements need enough administration buildings to support population.
		double demand = settlement.getNumCitizens();

		// Supply based on wear condition of buildings.
		double supply = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building adminBuilding = i.next();
			Administration admin = adminBuilding.getAdministration();// adminBuilding.getFunction(FUNCTION);
			double populationSupport = admin.getPopulationSupport();
			double wearFactor = ((adminBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
			supply += populationSupport * wearFactor;
		}

		if (!newBuilding) {
			// BuildingConfig config =
			// SimulationConfig.instance().getBuildingConfiguration();
			supply -= buildingConfig.getAdministrationPopulationSupport(buildingName);
			if (supply < 0D)
				supply = 0D;
		}

		return demand / (supply + 1D);
	}

	/**
	 * Gets an available building with the administration function.
	 * 
	 * @param person the person looking for the office.
	 * @return an available office space or null if none found.
	 */
	public static Building getAvailableOffice(Person person) {
		Building result = null;

		// If person is in a settlement, try to find a building with )an office.
		if (person.isInSettlement()) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List<Building> offices = buildingManager.getBuildings(FunctionType.ADMINISTRATION);
			offices = BuildingManager.getNonMalfunctioningBuildings(offices);
			offices = BuildingManager.getLeastCrowdedBuildings(offices);

			if (offices.size() > 0) {
				Map<Building, Double> selectedOffices = BuildingManager.getBestRelationshipBuildings(person, offices);
				result = RandomUtil.getWeightedRandomObject(selectedOffices);
			}
		}

		return result;
	}

	/**
	 * Gets the number of people this administration facility can support.
	 * 
	 * @return population that can be supported.
	 */
	public int getPopulationSupport() {
		return populationSupport;
	}

	/**
	 * Gets the number of people this administration facility can be used all at a
	 * time.
	 * 
	 * @return population that can be supported.
	 */
	public int getStaffCapacity() {
		return staffCapacity;
	}

	/**
	 * Gets the current number of people using the office space.
	 * 
	 * @return number of people.
	 */
	public int getNumStaff() {
		return staff;
	}

	public boolean isFull() {
		if (staff == staffCapacity)
			return true;
		else
			return false;
	}

	/**
	 * Adds a person to the office space.
	 * 
	 * @throws BuildingException if person would exceed office space capacity.
	 */
	public void addstaff() {
		staff++;
		if (staff > staffCapacity) {
			staff = staffCapacity;
			logger.info("[" + building.getSettlement() + "] The office space in " + building.getNickName() + " is full.");
		}
	}

	/**
	 * Removes a person from the office space.
	 * 
	 * @throws BuildingException if nobody is using the office space.
	 */
	public void removeStaff() {
		staff--;
		if (staff < 0) {
			staff = 0;
			logger.severe("[" + building.getSettlement() + "] Miscalculating the office space occupancy in " + building.getNickName() + ".");
		}
	}

	@Override
	public double getMaintenanceTime() {
		return populationSupport * 1D;
	}

	@Override
	public void timePassing(double time) {
		// Do nothing
	}

	@Override
	public double getFullPowerRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		return 0;
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
}