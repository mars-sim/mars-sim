/**
 * Mars Simulation Project
 * InsideBuilding.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class InsideBuilding implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name = "Inside a building";
	private Unit unit;

	public String getName() {
		return name;
	}

	public LocationStateType getType() {
		return LocationStateType.INSIDE_BUILDING;
	}
	
	public InsideBuilding(Unit unit) {
		this.unit = unit;
	}
/*
	public void leaveBuilding() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void enterBuilding() {
		//not possible, already inside a building
		//unit.setLocationState(unit.getInsideBuilding());
	}

	public void departFromVicinity() {
		//not possible, need to leave building first
		//unit.setLocationState(unit.getOutsideOnMars());
	}

	public void returnToVicinity() {
		//not possible, since the unit was in a building
		//unit.setLocationState(unit.getSettlementVicinity());
	}

	public void embarkVehicleInVicinity() {
		//not possible, need to leave building first
		//unit.setLocationState(unit.getInsideVehicle());
	}

	public void disembarkVehicleInVicinity() {
		//not possible, since the unit was in a building
		//unit.setLocationState(unit.getSettlementVicinity());
	}

	public void embarkVehicleInGarage() {
		unit.setLocationState(unit.getInsideVehicleInSettlement());
	}

	public void disembarkVehicleInGarage() {
		//not possible, need to leave building first
		//unit.setLocationState(unit.getInsideBuilding());
	}

	public void transferFromSettlementToPerson() {
		unit.setLocationState(unit.getOnAPerson());
	}

	public void transferFromPersonToSettlement() {
		//not possible, not on a person
		//unit.setLocationState(unit.getInsideBuilding());
	}

	public void transferFromPersonToVehicle() {
		//not possible, not on a person
		//unit.setLocationState(unit.getInsideVehicle());
	}

	public void transferFromVehicleToPerson() {
		//not possible, not on a vehicle
		//unit.setLocationState(unit.getOnAPerson());
	}
*/	
}