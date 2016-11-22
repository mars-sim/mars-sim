/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.collectIce"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new CollectIce(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double missionProbability = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if mission is possible for person based on their
            // circumstance.
            boolean missionPossible = true;
            Settlement settlement = person.getParkedSettlement();

            // Check if there are any desirable settlements within range.
            double topSettlementDesirability = 0D;
            Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, false);
            
            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, false)) {
                missionPossible = false;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
                missionPossible = false;
            }

            // Check if minimum number of people are available at the
            // settlement.
            // Plus one to hold down the fort.
            else if (!RoverMission.minAvailablePeopleAtSettlement(settlement, (RoverMission.MIN_PEOPLE + 1))) {
                missionPossible = false;
            }

            // Check if min number of EVA suits at settlement.
            else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < 
                    RoverMission.MIN_PEOPLE) {
                missionPossible = false;
            }

            // Check if settlement has enough basic resources for a rover mission.
            else if (!RoverMission.hasEnoughBasicResources(settlement)) {
                missionPossible = false;
            }

            // Check if there are any desirable settlements within range.
            //double topSettlementDesirability = 0D;
            //Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, false);
            else if (vehicle != null) {          
            	
                Map<Settlement, Double> desirableSettlements = TravelToSettlement.getDestinationSettlements(
                        person, settlement, vehicle.getRange());
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

            // TODO: if water resource is dangerously low, we should allow collect ice mission to proceed no matter what !?
            // Check for embarking missions.
            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
                missionPossible = false;
            }
            
            // Check if starting settlement has minimum amount of methane fuel.
            //AmountResource methane = AmountResource.findAmountResource("methane");
            else if (settlement.getInventory().getAmountResourceStored(AmountResource.findAmountResource("methane"), false) < 
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
                    
                // Job modifier.
                Job job = person.getMind().getJob();
                if (job != null) {
                    missionProbability *= job.getStartMissionProbabilityModifier(
                            TravelToSettlement.class);
                }
            }
        }

        return missionProbability;
    }

	@Override
	public Mission constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}