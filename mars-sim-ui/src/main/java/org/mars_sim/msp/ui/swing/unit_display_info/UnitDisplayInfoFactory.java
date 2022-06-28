/*
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @date 2022-06-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Factory for unit display info beans.
 */
public final class UnitDisplayInfoFactory {

	// Static bean instances.
	private static UnitDisplayInfo settlementBean = new SettlementDisplayInfoBean();
	private static UnitDisplayInfo buildingBean = new BuildingDisplayInfoBean();
	private static UnitDisplayInfo personBean = new PersonDisplayInfoBean();
	private static UnitDisplayInfo robotBean = new RobotDisplayInfoBean();
	private static UnitDisplayInfo roverBean = new RoverDisplayInfoBean();
	private static UnitDisplayInfo explorerRoverBean = new ExplorerRoverDisplayInfoBean();
	private static UnitDisplayInfo transportRoverBean = new TransportRoverDisplayInfoBean();
	private static UnitDisplayInfo cargoRoverBean = new CargoRoverDisplayInfoBean();
	private static UnitDisplayInfo luvBean = new LUVDisplayInfoBean();
	private static UnitDisplayInfo deliveryDroneBean = new DroneDisplayInfoBean();
	private static UnitDisplayInfo equipmentBean = new EquipmentDisplayInfoBean();

	/**
	 * Private constructor
	 */
	private UnitDisplayInfoFactory() {
		// empty for now
	}

	/**
	 * Gets a display information about a given unit.
	 * 
	 * @param unit the unit to display.
	 * @return unit display info instance.
	 */
	public static UnitDisplayInfo getUnitDisplayInfo(Unit unit) {
		if (unit.getUnitType() == UnitType.SETTLEMENT)
			return settlementBean;
		else if (unit.getUnitType() == UnitType.PERSON)
			return personBean;
		else if (unit.getUnitType() == UnitType.BUILDING)
			return buildingBean;
		else if (unit.getUnitType() == UnitType.ROBOT)
			return robotBean;
		else if (unit.getUnitType() == UnitType.VEHICLE) {
			Vehicle vehicle = (Vehicle) unit;
			VehicleType type = vehicle.getVehicleType();
			if (vehicle instanceof Rover) {
				if (type == VehicleType.EXPLORER_ROVER)
					return explorerRoverBean;
				else if (type == VehicleType.TRANSPORT_ROVER)
					return transportRoverBean;
				else if (type == VehicleType.CARGO_ROVER)
					return cargoRoverBean;
				else
					return roverBean;
			} else if (type == VehicleType.LUV) {
				return luvBean;
			} else if (type == VehicleType.DELIVERY_DRONE) {
				return deliveryDroneBean;
			}
			else
				return null;
		} else if (unit.getUnitType() == UnitType.EQUIPMENT)
			return equipmentBean;
		else
			return null;
	}
}
