/**
 * Mars Simulation Project
 * SettlementVicinity.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class SettlementVicinity implements LocationState, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String name = "Within a settlement's vicinity";
	private Unit unit;

	public SettlementVicinity(Unit unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void leaveBuilding() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void enterBuilding() {
		unit.setLocationState(unit.getInsideBuilding());
	}

	public void departFromVicinity() {
		unit.setLocationState(unit.getOutsideOnMars());
	}

	public void returnToVicinity() {
		unit.setLocationState(unit.getSettlementVicinity());
	}


	public void embarkVehicleInVicinity() {
		unit.setLocationState(unit.getInsideVehicle());
	}

	public void disembarkVehicleInVicinity() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void embarkVehicleInGarage() {
		unit.setLocationState(unit.getInsideVehicle());
	}

	public void disembarkVehicleInGarage() {
		unit.setLocationState(unit.getInsideBuilding());
	}



	public void transferFromSettlementToPerson() {
		unit.setLocationState(unit.getOnAPerson());
	}

	public void transferFromPersonToSettlement() {
		unit.setLocationState(unit.getInsideBuilding());
	}

	public void transferFromPersonToVehicle() {
		unit.setLocationState(unit.getInsideVehicle());
	}

	public void transferFromVehicleToPerson() {
		unit.setLocationState(unit.getOnAPerson());
	}

}