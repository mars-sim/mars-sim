/*
 * Mars Simulation Project
 * DigLocalRegolithMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the DigLocalRegolith task.
 */
public class DigLocalRegolithMeta extends DigLocalMeta {
    
	// can add back private static SimLogger logger = SimLogger.getLogger(DigLocalRegolithMeta.class.getName())
	
	private static final int THRESHOLD_AMOUNT = 50;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$
    
    public DigLocalRegolithMeta() {
		super(NAME, EquipmentType.BAG);
	}

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalRegolith(person);
    }

    @Override
    public double getProbability(Person person) {
    	if (!CollectionUtils.isSettlement(person.getCoordinates())) {
    		return 0;
    	}
    	
    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    	double rate = settlement.getRegolithCollectionRate();
    	if (rate <= 0D) {
    		return 0D;
    	}
    	
        // Check if settlement has DIG_LOCAL_REGOLITH override flag set.
        if (settlement.getProcessOverride(OverrideType.DIG_LOCAL_REGOLITH)) {
        	return 0;
        }
    	
        double settlementCap = settlement.getAmountResourceRemainingCapacity(ResourceUtil.regolithID);
        if (settlementCap < THRESHOLD_AMOUNT) {
        	return 0;
        }
        
    	double result = getProbability(ResourceUtil.regolithID, settlement, 
    			person, rate * settlement.getRegolithProbabilityValue());
    	
//    	logger.info(settlement, 20_000, "rate: " + Math.round(settlement.getRegolithCollectionRate() * 100.0)/100.0 
//    			+ "  Final regolith: " + Math.round(result* 100.0)/100.0);
        
        return result;
    }
}
