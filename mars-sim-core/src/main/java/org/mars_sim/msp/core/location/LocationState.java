/**
 * Mars Simulation Project
 * LocationState.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

public interface LocationState {

	String getName();

	public void leaveBuilding();

	public void enterBuilding();

	public void departFromVicinity();

	public void returnToVicinity();

	public void embarkVehicleInVicinity();

	public void disembarkVehicleInVicinity();

	public void embarkVehicleInGarage();

	public void disembarkVehicleInGarage();

	public void transferFromSettlementToPerson();

	public void transferFromPersonToSettlement();

	public void transferFromPersonToVehicle();

	public void transferFromVehicleToPerson();

}