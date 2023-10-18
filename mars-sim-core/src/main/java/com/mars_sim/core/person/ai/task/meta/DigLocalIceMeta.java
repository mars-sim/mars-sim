/*
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.DigLocalIce;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta extends DigLocalMeta {

	// can add back private static SimLogger logger = SimLogger.getLogger(DigLocalIceMeta.class.getName());
	
	private static final int THRESHOLD_AMOUNT = 50;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    
    public DigLocalIceMeta() {
		super(NAME, EquipmentType.BAG);
	}

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalIce(person);
    }

    @Override
    public double getProbability(Person person) {
    	
    	Settlement settlement = person.getSettlement();
    	double rate = 0;
    	
    	if (settlement != null) {
    		
    		rate = settlement.getIceCollectionRate();
	    	if (rate <= 0) {
	    		return 0;
	    	}
    	}
    	else
    		return 0;
    	
        // Check if settlement has DIG_LOCAL_ICE override flag set.
        if (settlement.getProcessOverride(OverrideType.DIG_LOCAL_ICE)) {
        	return 0;
        }
    	
        double settlementCap = settlement.getAmountResourceRemainingCapacity(ResourceUtil.iceID);
        if (settlementCap < THRESHOLD_AMOUNT) {
        	return 0;
        }
    	
    	double result = getProbability(ResourceUtil.iceID, settlement, 
    			person, rate * settlement.getIceProbabilityValue());
    	
//    	logger.info(settlement, 20_000, "rate: " + Math.round(settlement.getIceCollectionRate() * 100.0)/100.0  
//    			+ "  Final ice: " + Math.round(result* 100.0)/100.0);
        
        return result;
    }
}
