/*
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @date 2022-06-27
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Entity;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Factory for unit display info beans.
 */
public final class UnitDisplayInfoFactory {

	// Static bean instances.
	private static UnitDisplayInfo settlementBean = new SettlementDisplayInfoBean();
	private static UnitDisplayInfo authorityBean = new UnitDisplayInfo("Authority");
	private static UnitDisplayInfo studyBean = new UnitDisplayInfo("ScientificStudy");
	private static UnitDisplayInfo buildingBean = new UnitDisplayInfo("Building");
	private static UnitDisplayInfo personBean = new PersonDisplayInfoBean();
	private static UnitDisplayInfo robotBean = new RobotDisplayInfoBean();
	private static UnitDisplayInfo explorerRoverBean = new VehicleDisplayInfoBean("unit/rover_explorer");
	private static UnitDisplayInfo transportRoverBean = new VehicleDisplayInfoBean("unit/rover_transport");
	private static UnitDisplayInfo cargoRoverBean = new VehicleDisplayInfoBean("unit/rover_cargo");
	private static UnitDisplayInfo luvBean = new VehicleDisplayInfoBean("unit/luv");
	private static UnitDisplayInfo deliveryDroneBean = new VehicleDisplayInfoBean("unit/drone");
	private static UnitDisplayInfo equipmentBean = new UnitDisplayInfo("Equipment", SoundConstants.SND_EQUIPMENT);
	private static UnitDisplayInfo constructionBean = new UnitDisplayInfo("ConsutructionSite");

	/**
	 * Private constructor
	 */
	private UnitDisplayInfoFactory() {
		// empty for now
	}

	/**
	 * Gets a display information about a given entity.
	 * 
	 * @param focus the entity to display.
	 * @return display info instance.
	 */
	public static UnitDisplayInfo getUnitDisplayInfo(Entity focus) {
		return switch (focus) {
			case Authority authority -> authorityBean;
			case ScientificStudy study -> studyBean;
			case Settlement settlement -> settlementBean;
			case Person person -> personBean;
			case Building building -> buildingBean;
			case Robot robot -> robotBean;
			case Vehicle vehicle -> switch (vehicle.getVehicleType()) {
				case EXPLORER_ROVER -> explorerRoverBean;
				case TRANSPORT_ROVER -> transportRoverBean;
				case CARGO_ROVER -> cargoRoverBean;
				case LUV -> luvBean;
				case PASSENGER_DRONE,DELIVERY_DRONE, CARGO_DRONE -> deliveryDroneBean;
			};
			case Equipment equipment -> equipmentBean;
			case ConstructionSite constructionSite -> constructionBean;
			default -> null;
		};
	}
}
