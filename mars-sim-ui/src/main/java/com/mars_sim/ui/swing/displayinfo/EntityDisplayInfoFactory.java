/*
 * Mars Simulation Project
 * EntityDisplayInfoFactory.java
 * @date 2022-06-27
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.displayinfo;

import com.mars_sim.core.Entity;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Factory for Entity display info beans.
 */
public final class EntityDisplayInfoFactory {

	public static final String AUTHORITY_TYPE = "authority";
	public static final String MISSION_TYPE = "mission";
	public static final String STUDY_TYPE = "scientificstudy";
	public static final String EQUIPMENT_TYPE = "equipment";
	public static final String CONSTRUCTION_TYPE = "constructionsite";

	// Static bean instances.
	private static EntityDisplayInfo settlementBean = new SettlementDisplayInfoBean();
	private static EntityDisplayInfo authorityBean = new EntityDisplayInfo(AUTHORITY_TYPE);
	private static EntityDisplayInfo studyBean = new EntityDisplayInfo(STUDY_TYPE);
	private static EntityDisplayInfo buildingBean = new EntityDisplayInfo(UnitType.BUILDING.name().toLowerCase());
	private static EntityDisplayInfo personBean = new PersonDisplayInfoBean();
	private static EntityDisplayInfo robotBean = new RobotDisplayInfoBean();
	private static EntityDisplayInfo explorerRoverBean = new VehicleDisplayInfoBean("unit/rover_explorer");
	private static EntityDisplayInfo transportRoverBean = new VehicleDisplayInfoBean("unit/rover_transport");
	private static EntityDisplayInfo cargoRoverBean = new VehicleDisplayInfoBean("unit/rover_cargo");
	private static EntityDisplayInfo luvBean = new VehicleDisplayInfoBean("unit/luv");
	private static EntityDisplayInfo deliveryDroneBean = new VehicleDisplayInfoBean("unit/drone");
	private static EntityDisplayInfo equipmentBean = new EntityDisplayInfo(EQUIPMENT_TYPE, SoundConstants.SND_EQUIPMENT);
	private static EntityDisplayInfo constructionBean = new EntityDisplayInfo(CONSTRUCTION_TYPE);
	private static EntityDisplayInfo missionBean = new EntityDisplayInfo(MISSION_TYPE);

	/**
	 * Private constructor
	 */
	private EntityDisplayInfoFactory() {
		// empty for now
	}

	/**
	 * Gets a display information about a given entity.
	 * 
	 * @param focus the entity to display.
	 * @return display info instance.
	 */
	public static EntityDisplayInfo getDisplayInfo(Entity focus) {
		return switch (focus) {
			case Authority authority -> authorityBean;
			case ScientificStudy study -> studyBean;
			case Settlement settlement -> settlementBean;
			case Mission mission -> missionBean;
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
