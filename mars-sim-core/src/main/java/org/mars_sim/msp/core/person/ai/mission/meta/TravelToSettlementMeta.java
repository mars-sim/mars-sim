/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.travelToSettlement"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new TravelToSettlement(person);
    }

    @Override
    public Mission constructInstance(Robot robot) {
        return null;//new TravelToSettlement(robot);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {
            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            missionProbability = getMission(settlement, person);

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null)
	            missionProbability *= job.getStartMissionProbabilityModifier(
	                    TravelToSettlement.class)* settlement.getGoodsManager().getTourismFactor();
	        }

        return missionProbability;
    }

    @Override
    public double getProbability(Robot robot) {
        return 0;
    }

    public double getMission(Settlement settlement, Unit unit) {
    	Person person = null;
    	Robot robot = null;

        double missionProbability = 0;

        // Check if available rover.
        if (!RoverMission.areVehiclesAvailable(settlement, false)) {
        	return 0;
        }

        // Check if available backup rover.
        if (!RoverMission.hasBackupRover(settlement)) {
        	return 0;
        }

	    // Check if minimum number of people are available at the settlement.
	    if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
	        return 0;
	    }

	    // Check if min number of EVA suits at settlement.
	    if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
	        return 0;
	    }

        // Check if settlement has enough basic resources for a rover mission.
        if (!RoverMission.hasEnoughBasicResources(settlement, false)) {
        	return 0;
        }

        // Check for embarking missions.
//        if (VehicleMission.hasEmbarkingMissions(settlement)) {
//        	return 0;
//        }

        // Check if starting settlement has minimum amount of methane fuel.
        else if (settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneID, false) <
                RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
        	return 0;
        }

        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, false);
        if (vehicle != null) {
        	Map<Settlement, Double> desirableSettlements = null;
			if (unit instanceof Person) {
				person = (Person) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    person, settlement, vehicle.getRange());

			}
			else if (unit instanceof Robot) {
				robot = (Robot) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    robot, settlement, vehicle.getRange());
			}

            if (desirableSettlements.size() == 0) {
            	return 0;
            }

            Iterator<Settlement> i = desirableSettlements.keySet().iterator();
            while (i.hasNext()) {
                Settlement desirableSettlement = i.next();
                double desirability = desirableSettlements.get(desirableSettlement);
                if (desirability > topSettlementDesirability) {
                    topSettlementDesirability = desirability;
                }
            }
        }


        // Determine mission probability.

        missionProbability = TravelToSettlement.BASE_MISSION_WEIGHT
                + (topSettlementDesirability / 100D);

        // Crowding modifier.
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability *= (crowding + 1);
        }

        return missionProbability;
    }

}