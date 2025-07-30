/*
 * Mars Simulation Project
 * EVA.java
 * @date 2024-11-30
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

/**
 * This class is a building function for extra vehicular activity.
 */
public class EVA extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double MAINTENANCE_FACTOR = 5D;

	// Different Positions of an airlock that must match the buildings.xml
	private static final String CENTER_POSITION = "center-position";
	private static final String INTERIOR_POSITION = "interior-position";
	private static final String EXTERIOR_POSITION = "exterior-position"; 
	
	private int airlockCapacity;
	
	private ClassicAirlock airlock;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for
	 * @param spec Specification of this Function
	 */
	public EVA(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.EVA, spec, building);

		// Add a building airlock.
		airlockCapacity = spec.getCapacity();
		LocalPosition airlockLoc = spec.getPositionProperty(CENTER_POSITION);
		LocalPosition airlockInteriorLoc = spec.getPositionProperty(INTERIOR_POSITION);
		LocalPosition airlockExteriorLoc = spec.getPositionProperty(EXTERIOR_POSITION);

		airlock = new ClassicAirlock(building, this, airlockCapacity, airlockLoc, 
											airlockInteriorLoc, airlockExteriorLoc);
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding,
			Settlement settlement) {

		// Demand is one airlock capacity for every four inhabitants.
		double demand = settlement.getNumCitizens() / 4D;

		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getAirlocks()) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += building.getEVA().airlock.getCapacity() * wearModifier;
			}
		}

		double airlockCapacityValue = demand / (supply + 1D);

		// Note: building.getEVA().airlock.getCapacity() is the same as the airlockCapacity below
		double airlockCapacity = buildingConfig.getFunctionSpec(type, FunctionType.EVA).getCapacity();

		return airlockCapacity * airlockCapacityValue;
	}

	/**
	 * Gets the building's airlock.
	 * 
	 * @return airlock
	 */
	public Airlock getAirlock() {
		return airlock;
	}

	public int getNumAwaitingInnerDoor() {
		return airlock.getNumAwaitingInnerDoor();
	}
	
	public int getNumAwaitingOuterDoor() {
		return airlock.getNumAwaitingOuterDoor();
	}
	
	public int getNumEmptied() {
		return airlockCapacity - getNumOccupiedActivitySpots();
	}
	
	public int getNumInChamber() {
		return getNumOccupiedActivitySpots();
	}
	
	public String getOperatorName() {
		return airlock.getOperatorName();
	}
	
	/**
	 * Time passing for the building.
	 * 
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			airlock.timePassing(pulse);
		}
		return valid;
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getCombinedPowerLoad() {
		return 0.75;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getPoweredDownPowerRequired() {
		return 0.075;
	}

	@Override
	public double getMaintenanceTime() {
		return airlock.getCapacity() * MAINTENANCE_FACTOR;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		airlock = null;
	}
}
