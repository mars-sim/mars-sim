/**
 * Mars Simulation Project
 * Management.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A management building function.  The building facilitates management
 * of a settlement population.
 */
public class Management extends Function {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(Management.class.getName());

    // Data members
	private int staff;
	private int staffCapacity;

    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Management(Building building, FunctionSpec spec) {
        // Use Function constructor.
        super(FunctionType.MANAGEMENT, spec, building);

		staffCapacity = spec.getCapacity();
    }

    /**
     * Gets the value of the function for a named building.
     *
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Settlements need enough management buildings to support population.
        double demand = settlement.getNumCitizens();

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.MANAGEMENT).iterator();
        while (i.hasNext()) {
            Building managementBuilding = i.next();
            Management management = managementBuilding.getManagement();
            double capacity = management.getStaffCapacity();
            double wearFactor = ((managementBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
            supply += capacity * wearFactor;
        }

        if (!newBuilding) {
            supply -= buildingConfig.getFunctionSpec(buildingName, FunctionType.MANAGEMENT).getCapacity();
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }

	/**
	 * Gets an available building with the management function.
	 *
	 * @param person the person looking for the command and control station.
	 * @return an available office space or null if none found.
	 */
	public static Building getAvailableStation(Person person) {
		Building result = null;

		// If person is in a settlement, try to find a building with )an office.
		if (person.isInSettlement()) {
			Set<Building> stations = person.getSettlement().getBuildingManager().getBuildingSet(FunctionType.MANAGEMENT);
			stations = BuildingManager.getNonMalfunctioningBuildings(stations);

			Set<Building> comfortOffices = BuildingManager.getLeastCrowdedBuildings(stations);

			if (!comfortOffices.isEmpty()) {
				stations = comfortOffices;
			}

			// skip filtering the crowded stations
			Map<Building, Double> selected = BuildingManager.getBestRelationshipBuildings(person, stations);
			result = RandomUtil.getWeightedRandomObject(selected);
		}

		return result;
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
        return staff >= staffCapacity;
	}

	/**
	 * Adds a person to the office space.
	 *
	 * @throws BuildingException if person would exceed office space capacity.
	 */
	public void addStaff() {
		if (staff >= staffCapacity) {
			logger.log(building, Level.INFO, 10_000, "The office space is full.");
		}
		else
			staff++;
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
			logger.log(building, Level.SEVERE, 10_000, "Miscalculating the office space occupancy");
		}
	}

    @Override
    public double getMaintenanceTime() {
        return staffCapacity * 1D;
    }
}
