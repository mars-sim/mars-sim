/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.1.0 2017-05-02
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
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalIceMeta;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.collectIce"); //$NON-NLS-1$

    private static final int MIN_ICE_RESERVE = 100; // per person
	public static final double MIN_WATER_RESERVE = 300D; // per person

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

            Settlement settlement = person.getSettlement();

            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, false)) {
                return 0;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
                return 0;
            }

    	    // Check if minimum number of people are available at the settlement.
            else if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
    	        return 0;
    	    }

    	    // Check if min number of EVA suits at settlement.
            else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
    	        return 0;
    	    }

            // Check if settlement has enough basic resources for a rover mission.
            else if (!RoverMission.hasEnoughBasicResources(settlement)) {
                return 0;
            }

            // Check for embarking missions.
            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
                return 0;
            }

            // Check if starting settlement has minimum amount of methane fuel.
            else if (settlement.getInventory().getAmountResourceStored(Rover.methaneAR, false) <
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
                return 0;
            }

            else {
                // Factor the value of ice at the settlement.
                GoodsManager manager = settlement.getGoodsManager();
                //AmountResource iceResource = AmountResource.findAmountResource("ice");
                //AmountResource waterResource =AmountResource.findAmountResource(LifeSupportType.WATER);
                double ice_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.iceAR));
                ice_value = ice_value * GoodsManager.ICE_VALUE_MODIFIER;
            	if (ice_value > 1000)
            		ice_value = 1000;

                double water_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.waterAR));
                water_value = water_value * GoodsManager.ICE_VALUE_MODIFIER;
                if (water_value > 1000)
            		water_value = 1000;

                int pop = settlement.getCurrentPopulationNum();

                double ice_available = settlement.getInventory().getAmountResourceStored(ResourceUtil.iceAR, false);
                double water_available = settlement.getInventory().getAmountResourceStored(ResourceUtil.waterAR, false);

                if (ice_available < MIN_ICE_RESERVE * pop + ice_value/10D && water_available < MIN_WATER_RESERVE * pop + water_value/10D) {
                	missionProbability = (water_value + ice_value + MIN_ICE_RESERVE * pop - ice_available) / 100D;
                }
                else
                	return 0;

                // Prompt the collect ice mission to proceed more easily if water resource is dangerously low,
                if (water_available > MIN_WATER_RESERVE * pop ) {
                	;// no change to missionProbability
                }
                else if (water_available > MIN_WATER_RESERVE * pop / 1.5 ) {
                	missionProbability = missionProbability + (MIN_WATER_RESERVE * pop - water_available) /20;
                }
                else if (water_available > MIN_WATER_RESERVE * pop / 2D) {
                	missionProbability = missionProbability + (MIN_WATER_RESERVE * pop - water_available) /10;
                }
                else
                	missionProbability = missionProbability + (MIN_WATER_RESERVE * pop - water_available) /5;

            }

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
                        CollectIce.class);
            }

            if (missionProbability > 3D) {
            	missionProbability = 3D;
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