/**
 * Mars Simulation Project
 * LocationState.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

public interface LocationState {

	//TODO: need to fix the methods in LocationState to truly take advantage of the use of interface,
	// compared to the use of LocationState declared as just an enum
	
	String getName();
	
	LocationStateType getType();

//	// for a person or robot
//	public void leavingBuilding();
//	// for a person or robot
//	public void enterBuilding();
//	// for a person or robot
//	public void embarkVehicleInVicinity();
//	// for a person or robot
//	public void disembarkVehicleInVicinity();
//	// for a person or robot
//	public void embarkVehicleInGarage();
//	// for a person or robot
//	public void disembarkVehicleInGarage();
//
//	
//	// for a vehicle
//	public void departFromVicinity();
//	// for a vehicle
//	public void returnToVicinity();
//
//	
//	// for a resource or item
//	public void transferFromSettlementToPerson();
//	// for a resource or item
//	public void transferFromPersonToSettlement();
//	// for a resource or item
//	public void transferFromPersonToVehicle();
//	// for a resource or item
//	public void transferFromVehicleToPerson();

}