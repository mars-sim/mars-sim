/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.07 2015-03-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
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

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            getMission(settlement, person);
            
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null)
	            missionProbability *= job.getStartMissionProbabilityModifier(
	                    TravelToSettlement.class);
	        }
        
        return missionProbability;
    }
    
    @Override
    public double getProbability(Robot robot) {
        
        double missionProbability = 0D;
/*
        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if mission is possible for robot based on their
            // circumstance.
            Settlement settlement = robot.getSettlement();

            getMission(settlement, robot);
        }
*/        
        return missionProbability;
    }
    
    public double getMission(Settlement settlement, Unit unit) {
    	Person person = null;
    	Robot robot = null;

    	
        boolean missionPossible = true;
        double missionProbability = 0;
        
        // Check if available rover.
        if (!RoverMission.areVehiclesAvailable(settlement, false)) {
            missionPossible = false;
        }

        // Check if available backup rover.
        if (!RoverMission.hasBackupRover(settlement)) {
            missionPossible = false;
        }

        // Check if minimum number of people are available at the
        // settlement.
        // Plus one to hold down the fort.
        if (!RoverMission.minAvailablePeopleAtSettlement(settlement, (RoverMission.MIN_PEOPLE + 1))) {
            missionPossible = false;
        }

        // Check if min number of EVA suits at settlement.
        if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < 
                RoverMission.MIN_PEOPLE) {
            missionPossible = false;
        }

        // Check if settlement has enough basic resources for a rover mission.
        if (!RoverMission.hasEnoughBasicResources(settlement)) {
            missionPossible = false;
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
                missionPossible = false;
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

        // Check for embarking missions.
        if (VehicleMission.hasEmbarkingMissions(settlement)) {
            missionPossible = false;
        }
        
        // Check if starting settlement has minimum amount of methane fuel.
        AmountResource methane = AmountResource.findAmountResource("methane");
        if (settlement.getInventory().getAmountResourceStored(methane, false) < 
                RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
            missionPossible = false;
        }

        // Determine mission probability.
        if (missionPossible) {
            missionProbability = TravelToSettlement.BASE_MISSION_WEIGHT
                    + (topSettlementDesirability / 100D);

            // Crowding modifier.
            int crowding = settlement.getCurrentPopulationNum()
                    - settlement.getPopulationCapacity();
            if (crowding > 0) {
                missionProbability *= (crowding + 1);
            }
                
        }
        return missionProbability;
    }
    
}