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
import org.mars_sim.msp.core.resource.ResourceUtil;
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

    private static final int MIN_REGOLITH_RESERVE = 10; // per person
	public static final int MIN_SAND_RESERVE = 5; // per person


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

	        if (result == 0)
	        	return 0;

            // Factor the value of ice at the settlement.
            GoodsManager manager = settlement.getGoodsManager();
            //AmountResource iceResource = AmountResource.findAmountResource("ice");
            //AmountResource waterResource =AmountResource.findAmountResource(LifeSupportType.WATER);
            double regolith_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.regolithAR));
            regolith_value = regolith_value * GoodsManager.REGOLITH_VALUE_MODIFIER;
        	if (regolith_value > 1000)
        		regolith_value = 1000;
        	else if (regolith_value <= 5)
        		return 0;

            double sand_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.sandAR));
            sand_value = sand_value * GoodsManager.SAND_VALUE_MODIFIER;
            if (sand_value > 1000)
        		sand_value = 1000;
            else if (sand_value <= 3)
            	return 0;

            int pop = settlement.getCurrentPopulationNum();

            double regolith_available = settlement.getInventory().getAmountResourceStored(ResourceUtil.regolithAR, false);
            double sand_available = settlement.getInventory().getAmountResourceStored(ResourceUtil.sandAR, false);

            if (regolith_available < MIN_REGOLITH_RESERVE * pop + regolith_value/10D && sand_available < MIN_SAND_RESERVE * pop + sand_value/10D) {
            	result = (sand_value + regolith_value + MIN_REGOLITH_RESERVE * pop - regolith_available) / 100D;
            }
            else
            	return 0;

            // Factor the value of regolith at the settlement.
            double value = settlement.getGoodsManager().getGoodValuePerItem(GoodsUtil.getResourceGood(Rover.regolithAR));
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