/**
 * Mars Simulation Project
 * MetaMissionUtil.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility mission for getting the list of meta missions.
 */
public class MetaMissionUtil {

	private static int numMetaMissions;
	
	// Static values.
	private static List<MetaMission> metaMissions = null;
	private static List<MetaMission> robotMetaMissions = null;

	/**
	 * Private constructor for utility class.
	 */
	private MetaMissionUtil() {
	};

	/**
	 * Lazy initialization of metaMissions list.
	 */
	private static void initializeMetaMissions() {

		metaMissions = new ArrayList<MetaMission>(13);

		// Populate metaMissions list with all meta missions.
		metaMissions.add(new AreologyFieldStudyMeta());
		metaMissions.add(new BiologyFieldStudyMeta());
		metaMissions.add(new BuildingConstructionMissionMeta());
		metaMissions.add(new BuildingSalvageMissionMeta());
		metaMissions.add(new CollectIceMeta());
		metaMissions.add(new CollectRegolithMeta());
		metaMissions.add(new EmergencySupplyMissionMeta());
		metaMissions.add(new ExplorationMeta());
		metaMissions.add(new MeteorologyFieldStudyMeta());
		metaMissions.add(new MiningMeta());
		metaMissions.add(new RescueSalvageVehicleMeta());
		metaMissions.add(new TradeMeta());
		metaMissions.add(new TravelToSettlementMeta());
		
		computeNumMetaMissions();
	}

	private static void initializeRobotMetaMissions() {

		robotMetaMissions = new ArrayList<MetaMission>();

		// Populate robotMetaMissions list with all meta missions.
		// robotMetaMissions.add(new TradeMeta());
		// robotMetaMissions.add(new TravelToSettlementMeta());
		// robotMetaMissions.add(new BuildingConstructionMissionMeta());
		// robotMetaMissions.add(new BuildingSalvageMissionMeta());
	}

	public static int getNumMetaMissions() {
		return numMetaMissions;
	}

	public static void computeNumMetaMissions() {
		numMetaMissions = getMetaMissions().size();
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
		// return new ArrayList<MetaMission>(metaMissions);
		return metaMissions;
	}

	public static List<MetaMission> getRobotMetaMissions() {

		// Lazy initialize meta missions list if necessary.
		if (robotMetaMissions == null) {
			initializeRobotMetaMissions();
		}

		// Return copy of meta mission list.
		// return new ArrayList<MetaMission>(robotMetaMissions);
		return robotMetaMissions;
	}
}