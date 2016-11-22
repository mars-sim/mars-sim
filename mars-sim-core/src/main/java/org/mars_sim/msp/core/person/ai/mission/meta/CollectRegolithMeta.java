/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

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
    	
        // Check if min number of EVA suits at settlement.
        if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getParkedSettlement()) < 
                CollectRegolith.MIN_PEOPLE) {
            result = 0D;
        }
        
        else 
        	result = CollectResourcesMission.getNewMissionProbability(person, Bag.class, 
                CollectRegolith.REQUIRED_BAGS, CollectRegolith.MIN_PEOPLE, CollectRegolith.class);
        
        if (result > 0D) {
            
            // Don't start mission until after first Sol of the simulation.
            MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            double totalTimeSols = MarsClock.getTimeDiff(currentTime, startTime) / 1000D;
            if (totalTimeSols < 1D) {
                result = 0;
            }
            
            else {
	            // Factor the value of regolith at the settlement.
	            GoodsManager manager = person.getParkedSettlement().getGoodsManager();
	            AmountResource regolithResource = AmountResource.findAmountResource("regolith");
	            double value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(regolithResource));
	            result *= value;
	            if (result > 1D) {
	                result = 1D;
	                // TODO : why setting result to 1D ? 
	            }
            }
            
        }
        
        //if (result > 0)
        //	System.out.println("CollectRegolithMeta : " + result);
        
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