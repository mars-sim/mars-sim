/*
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @date 2022-06-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
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
		switch (unit.getUnitType()) {
			case SETTLEMENT:
				return settlementBean;
			case PERSON:
				return personBean;
			case BUILDING:
				return buildingBean;
			case ROBOT:
				return robotBean;
			case VEHICLE: {
				Vehicle vehicle = (Vehicle) unit;
				VehicleType type = vehicle.getVehicleType();
				switch (type) {
					case EXPLORER_ROVER:	
						return explorerRoverBean;
					case TRANSPORT_ROVER:
						return transportRoverBean;
					case CARGO_ROVER:
						return cargoRoverBean;
					case LUV:
						return luvBean;
					case DELIVERY_DRONE:
						return deliveryDroneBean;
					default:
						// Should never happen
						return explorerRoverBean;
				}
			}
			case EVA_SUIT:
			case CONTAINER:
				return equipmentBean;

			default:
				return null;
		}
	}
}
