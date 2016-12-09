/**
 * Mars Simulation Project
 * Administration.java
 * @version 3.07 2014-07-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

/**
 * An administration building function.  The building facilitates report writing and other
 * administrative paperwork.
 */
public class Administration extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static final BuildingFunction FUNCTION = BuildingFunction.ADMINISTRATION;
    private static BuildingConfig buildingConfig;
    
    // Data members
    private int populationSupport;
    private int staff, staffCapacity;
    
    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Administration(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        // Populate data members.
        buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        populationSupport = buildingConfig.getAdministrationPopulationSupport(building.getName());

        // Load activity spots
        loadActivitySpots(buildingConfig.getAdministrationActivitySpots(building.getName()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Settlements need enough administration buildings to support population.
        double demand = settlement.getAllAssociatedPeople().size();

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building adminBuilding = i.next();
            Administration admin = (Administration) adminBuilding.getFunction(FUNCTION);
            double populationSupport = admin.getPopulationSupport();
            double wearFactor = ((adminBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
            supply += populationSupport * wearFactor;
        }

        if (!newBuilding) {
            //BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
            supply -= buildingConfig.getAdministrationPopulationSupport(buildingName);
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }

    /**
     * Gets the number of people this administration facility can support.
     * @return population that can be supported.
     */
    public int getPopulationSupport() {
        return populationSupport;
    }


    /**
     * Gets the number of people this administration facility can be used all at a time.
     * @return population that can be supported.
     */
    public int getStaffCapacity() {
        return staffCapacity;
    }


    /**
     * Gets the current number of people using the office space.
     * @return number of people.
     */
    public int getNumStaff() {
        return staff;
    }

    /**
     * Adds a person to the office space.
     * @throws BuildingException if person would exceed office space capacity.
     */
    public void addstaff() {
        staff++;
        if (staff > staffCapacity) {
            staff = staffCapacity;
            throw new IllegalStateException("The office space is full.");
        }
    }

    /**
     * Removes a person from the office space.
     * @throws BuildingException if nobody is using the office space.
     */
    public void removeStaff() {
        staff--;
        if (staff < 0) {
            staff = 0;
            throw new IllegalStateException("The office space is empty.");
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