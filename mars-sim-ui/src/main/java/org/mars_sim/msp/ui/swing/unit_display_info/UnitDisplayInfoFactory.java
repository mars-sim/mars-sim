/**
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @version 3.1.0 2017-10-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Factory for unit display info beans.
 */
public final class UnitDisplayInfoFactory {

	// Static bean instances.
	private static UnitDisplayInfo settlementBean = new SettlementDisplayInfoBean();
	private static UnitDisplayInfo personBean = new PersonDisplayInfoBean();
	private static UnitDisplayInfo robotBean = new RobotDisplayInfoBean();
	private static UnitDisplayInfo roverBean = new RoverDisplayInfoBean();
	private static UnitDisplayInfo explorerRoverBean = new ExplorerRoverDisplayInfoBean();
	private static UnitDisplayInfo transportRoverBean = new TransportRoverDisplayInfoBean();
	private static UnitDisplayInfo cargoRoverBean = new CargoRoverDisplayInfoBean();
	private static UnitDisplayInfo luvBean = new LUVDisplayInfoBean();
	private static UnitDisplayInfo equipmentBean = new EquipmentDisplayInfoBean();

	/**
	 * Private constructor
	 */
	private UnitDisplayInfoFactory() {
	}

	/**
	 * Gets a display information about a given unit.
	 * 
	 * @param unit the unit to display.
	 * @return unit display info instance.
	 */
	public static UnitDisplayInfo getUnitDisplayInfo(Unit unit) {
		if (unit instanceof Settlement)
			return settlementBean;
		else if (unit instanceof Person)
			return personBean;
		else if (unit instanceof Robot)
			return robotBean;
		else if (unit instanceof Vehicle) {
			Vehicle vehicle = (Vehicle) unit;
			if (vehicle instanceof Rover) {
				String type = vehicle.getVehicleType();
				if (type.equalsIgnoreCase(VehicleType.EXPLORER_ROVER.getName()))
					return explorerRoverBean;
				else if (type.equalsIgnoreCase(VehicleType.TRANSPORT_ROVER.getName()))
					return transportRoverBean;
				else if (type.equalsIgnoreCase(VehicleType.CARGO_ROVER.getName()))
					return cargoRoverBean;
				else
					return roverBean;
			} else if (vehicle.getVehicleType().equalsIgnoreCase(VehicleType.LUV.getName()))
				return luvBean;
			else
				return null;
		} else if (unit instanceof Equipment)
			return equipmentBean;
		else
			return null;
	}
}
