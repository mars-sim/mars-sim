/**
 * Mars Simulation Project
 * MetaMissionUtil.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.predefined.TestDriveMetaMission;
import com.mars_sim.core.person.ai.mission.MissionType;

/**
 * A utility mission for getting the list of meta missions.
 */
public class MetaMissionUtil {
	
	// Static values.
	private static List<MetaMission> metaMissions = null;
	private static List<MetaMission> automaticMissions;

	/**
	 * Private constructor for utility class.
	 */
	private MetaMissionUtil() {
		// Stop creating instance of helper
	}

	/**
	 * Lazy initialization of metaMissions list.
	 */
	private static synchronized void initializeMetaMissions() {

		// Check for concurrrent creation
		if (metaMissions != null) {
			// Already created during the lock wait of this Thread
			return;
		}

		List<MetaMission> newMissions = new ArrayList<>();

		// Populate metaMissions list with all meta missions.
		newMissions.add(new AreologyFieldStudyMeta());
		newMissions.add(new BiologyFieldStudyMeta());
		newMissions.add(new ConstructionMissionMeta());
		newMissions.add(new CollectIceMeta());
		newMissions.add(new CollectRegolithMeta());
		newMissions.add(new DeliveryMeta());
		newMissions.add(new ExplorationMeta());
		newMissions.add(new EmergencySupplyMeta());
		newMissions.add(new MeteorologyFieldStudyMeta());
		newMissions.add(new MiningMeta());
		newMissions.add(new RescueSalvageVehicleMeta());
		newMissions.add(new TradeMeta());
		newMissions.add(new TravelToSettlementMeta());
		newMissions.add(new TestDriveMetaMission());	

		metaMissions = newMissions;

		automaticMissions = metaMissions.stream().filter(MetaMission::isAutomatic).toList();
	}

	/**
	 * Gets a meta mission by its type.
	 * 
	 * @param type the type of the meta mission.
	 * @return the meta mission with the specified type, or null if not found.
	 */
	public static MetaMission getMetaMission(MissionType type) {
		for (MetaMission meta : getMetaMissions()) {
			if (meta.getType() == type) {
				return meta;
			}
		}
		return null;
	}
	
	/**
	 * Gets a list of all meta missions.
	 * 
	 * @return list of meta missions.
	 */
	public static List<MetaMission> getMetaMissions() {

		// Lazy initialize meta missions list if necessary.
		if (metaMissions == null) {
			initializeMetaMissions();
		}

		// Return copy of meta mission list.
		return metaMissions;
	}

	/**
	 * Gets a list of all meta missions that can be automatically created.
	 * 
	 * @return list of meta missions.
	 */
	public static List<MetaMission> getAutomaticMetaMissions() {

		// Lazy initialize meta missions list if necessary.
		if (metaMissions == null) {
			initializeMetaMissions();
		}

		return automaticMissions;
	}
}