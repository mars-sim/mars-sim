/**
 * Mars Simulation Project
 * InsideSettlement.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class InsideSettlement implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name = "Inside a settlement";
	private Unit unit;

	public String getName() {
		return name;
	}
	
	public LocationStateType getType() {
		return LocationStateType.INSIDE_SETTLEMENT;
	}
	
	public InsideSettlement(Unit unit) {
		this.unit = unit;
	}
/*
	public void leaveBuilding() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void enterBuilding() {
		//not possible, already inside the settlement
		//unit.setLocationState(unit.getInsideBuilding());
	}

	public void departFromVicinity() {
		unit.setLocationState(unit.getOutsideOnMars());
	}

	public void returnToVicinity() {
		//not possible, still inside settlement
		//unit.setLocationState(unit.getSettlementVicinity());
	}


	public void embarkVehicleInVicinity() {
		//not possible, need to get out of the settlement and walk to a vehicle first
		//unit.setLocationState(unit.getInsideVehicleInSettlement());
	}

	public void disembarkVehicleInVicinity() {
		//not possible, need to get inside a vehicle first
		//unit.setLocationState(unit.getSettlementVicinity());
	}

	public void embarkVehicleInGarage() {
		unit.setLocationState(unit.getInsideVehicleInSettlement());
	}

	public void disembarkVehicleInGarage() {
		//not possible, not inside a vehicle
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
		//not possible, not inside a vehicle
		//unit.setLocationState(unit.getOnAPerson());
	}
	 */
}