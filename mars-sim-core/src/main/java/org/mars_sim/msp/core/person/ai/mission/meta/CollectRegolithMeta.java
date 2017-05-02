/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.1.0 2017-03-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.collectRegolith"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new CollectRegolith(person);
    }

    @Override
    public double getProbability(Person person) {

    	double result = 0;

        //MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        //int today = currentTime.getSolElapsedFromStart();
        if (Simulation.instance().getMasterClock().getMarsClock().getSolElapsedFromStart() < CollectRegolith.MIN_NUM_SOL)
        	return 0;

        //MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
        //double totalTimeSols = MarsClock.getTimeDiff(currentTime, startTime) / 1000D;
        //if (totalTimeSols < CollectRegolith.MIN_NUM_SOL) {
        //    return 0;
        //}

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	        Settlement settlement = person.getSettlement();

		    // Check if minimum number of people are available at the settlement.
	        if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
		        return 0;
		    }

		    // Check if min number of EVA suits at settlement.
	        else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
		        return 0;
		    }

	        else
	        	result = CollectResourcesMission.getNewMissionProbability(person, Bag.class,
	                CollectRegolith.REQUIRED_BAGS, CollectRegolith.MIN_PEOPLE, CollectRegolith.class);

            // Factor the value of regolith at the settlement.
            double value = person.getSettlement().getGoodsManager().getGoodValuePerItem(GoodsUtil.getResourceGood(Rover.regolithAR));
            result *= value;

            if (result > 1D) {
                result = 1D;
            }

        }
        return result;
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