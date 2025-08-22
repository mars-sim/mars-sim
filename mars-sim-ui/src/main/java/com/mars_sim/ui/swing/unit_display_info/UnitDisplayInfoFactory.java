/*
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @date 2022-06-27
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Unit;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Factory for unit display info beans.
 */
public final class UnitDisplayInfoFactory {

	// Static bean instances.
	private static UnitDisplayInfo settlementBean = new SettlementDisplayInfoBean();
	private static UnitDisplayInfo buildingBean = new UnitDisplayInfo("building");
	private static UnitDisplayInfo personBean = new PersonDisplayInfoBean();
	private static UnitDisplayInfo robotBean = new RobotDisplayInfoBean();
	private static UnitDisplayInfo explorerRoverBean = new VehicleDisplayInfoBean("unit/rover_explorer");
	private static UnitDisplayInfo transportRoverBean = new VehicleDisplayInfoBean("unit/rover_transport");
	private static UnitDisplayInfo cargoRoverBean = new VehicleDisplayInfoBean("unit/rover_cargo");
	private static UnitDisplayInfo luvBean = new VehicleDisplayInfoBean("unit/luv");
	private static UnitDisplayInfo deliveryDroneBean = new VehicleDisplayInfoBean("unit/drone");
	private static UnitDisplayInfo equipmentBean = new UnitDisplayInfo("unit/equipment", SoundConstants.SND_EQUIPMENT);
	private static UnitDisplayInfo constructionBean = buildingBean;


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
					case DELIVERY_DRONE, CARGO_DRONE:
						return deliveryDroneBean;
					default:
						// Should never happen
						return explorerRoverBean;
				}
			}
			case EVA_SUIT, CONTAINER:
				return equipmentBean;
			case CONSTRUCTION:
				return constructionBean;
			default:
				return null;
		}
	}
}
