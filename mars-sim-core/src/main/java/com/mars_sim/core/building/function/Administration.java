/**
 * Mars Simulation Project
 * Administration.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Iterator;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement;

/**
 * An administration building function. The building facilitates report writing
 * and other administrative paperwork.
 */
public class Administration extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(Administration.class.getName());

	private static final String POPULATION_SUPPORT = "population-support";

	// Data members
	
	// Note: define the use of populationSupport
	private int populationSupport;
	private int staff;
	private int staffCapacity;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Spec of the Administration Function
	 */
	public Administration(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.ADMINISTRATION, spec, building);

		populationSupport = spec.getIntegerProperty(POPULATION_SUPPORT);

		staffCapacity = spec.getCapacity();
	}

	/**
	 * Gets the full value of the function for a building type across the settlement.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {
		double value = 0;
		// Note: do use getNumCitizens() since populationSupport and staffCapacity below will be used
		double demand = settlement.getNumCitizens();

		// Supply based on wear condition of buildings.
		double supply = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.ADMINISTRATION).iterator();
		while (i.hasNext()) {
			Building adminBuilding = i.next();
			Administration admin = adminBuilding.getAdministration();
			double populationSupport = admin.getPopulationSupport();
			double staffCapacity = admin.getStaffCapacity();
			double wearFactor = ((adminBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
			supply += populationSupport - staffCapacity * wearFactor;
		}

		value = demand / (supply + 1D);

		return value;
	}

	/**
	 * Gets the value of this function.
	 * 
	 * @return value (VP) of building function.
	 */
	@Override
	public double getFunctionValue() {
		double value = 0;
		// Note: do use getNumCitizens() since populationSupport and staffCapacity below will be used
		double demand = getBuilding().getSettlement().getNumCitizens();

		double supply = 0D;

		supply = populationSupport - staffCapacity;

		value = (demand + 1) / (supply + 1D);

		return value;
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
		return populationSupport * 1.5;
	}
}
