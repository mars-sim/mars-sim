/*
 * Mars Simulation Project
 * MissionUtil.java
 * @date 2022-09-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Iterator;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Helper class that provides numerous assessment method for the state of various
 * Units that support the Mission logic
 */
public class MissionUtil {
    
	private static final String PHASE_1 = "phase 1";
	private static final String MINING = "mining";
	private static final String TRADING = "trading";
    private static MissionManager missionManager;
    private static UnitManager unitManager;

    private MissionUtil() {
    }

    	
	/**
	 * Gets the number of available EVA suits for a mission at a settlement.
	 *
	 * @param settlement the settlement to check.
	 * @return number of available suits.
	 */
	public static int getNumberAvailableEVASuitsAtSettlement(Settlement settlement) {

		if (settlement == null)
			throw new IllegalArgumentException("Settlement is null");

		int result = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);

		// Leave one suit for settlement use.
		if (result > 0) {
			result--;
		}
		return result;
	}
    
    /**
	 * Finds the closest settlement to a Coordinate
	 * @param location Center of search
	 * @return settlement
	 */
	public static Settlement findClosestSettlement(Coordinates location) {
		Settlement result = null;
		double closestDistance = Double.MAX_VALUE;

		for (Settlement settlement : unitManager.getSettlements()) {
			double distance = settlement.getCoordinates().getDistance(location);
			if (distance < closestDistance) {
				result = settlement;
				closestDistance = distance;
			}
		}

		return result;
	}


    /**
	 * Checks to see how many currently embarking missions at the settlement.
	 *
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	public static int numEmbarkingMissions(Settlement settlement) {
		int result = 0;
		for(Mission i : missionManager.getMissionsForSettlement(settlement)) {
			if (i.getStage() == Stage.PREPARATION) {
				result++;
			}
		}

		return result;
	}

    	/**
	 * Checks to see if at least a minimum number of people are available for a
	 * mission at a settlement.
	 *
	 * @param settlement the settlement to check.
	 * @param minNum     minimum number of people required.
	 * @return true if minimum people available.
	 */
	public static boolean minAvailablePeopleAtSettlement(Settlement settlement, int minNum) {
		boolean result = false;
		int min = minNum;
		if (settlement != null) {

			String template = settlement.getTemplate();
			// Override the minimum num req if the settlement is too small
			if (template.toLowerCase().contains(PHASE_1)
					|| template.toLowerCase().contains(MINING)
					|| template.toLowerCase().contains(TRADING))
				min = 0;

			int numAvailable = 0;
			Iterator<Person> i = settlement.getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission())
					numAvailable++;
			}
			if (numAvailable >= min)
				result = true;
		}

		return result;
	}

    public static void initializeInstances(UnitManager u, MissionManager m) {
        unitManager = u;
        missionManager = m;
    }
}
