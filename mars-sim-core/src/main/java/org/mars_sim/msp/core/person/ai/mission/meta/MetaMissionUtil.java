/**
 * Mars Simulation Project
 * MetaMissionUtil.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility mission for getting the list of meta missions.
 */
public class MetaMissionUtil {
	
	// Static values.
	private static List<MetaMission> metaMissions = null;

	/**
	 * Private constructor for utility class.
	 */
	private MetaMissionUtil() {
	};

	/**
	 * Lazy initialization of metaMissions list.
	 */
	private synchronized static void initializeMetaMissions() {

		// Check for concurrrent creation
		if (metaMissions != null) {
			// Already created during the lock wait of this Thread
			return;
		}

		List<MetaMission> newMissions = new ArrayList<>();

		// Populate metaMissions list with all meta missions.
		newMissions.add(new AreologyFieldStudyMeta());
		newMissions.add(new BiologyFieldStudyMeta());
		newMissions.add(new BuildingConstructionMissionMeta());
		newMissions.add(new BuildingSalvageMissionMeta());
		newMissions.add(new CollectIceMeta());
		newMissions.add(new CollectRegolithMeta());
		newMissions.add(new DeliveryMeta());
		newMissions.add(new EmergencySupplyMeta());
		newMissions.add(new ExplorationMeta());
		newMissions.add(new MeteorologyFieldStudyMeta());
		newMissions.add(new MiningMeta());
		newMissions.add(new RescueSalvageVehicleMeta());
		newMissions.add(new TradeMeta());
		newMissions.add(new TravelToSettlementMeta());		

		metaMissions = newMissions;
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
}