/*
 * Mars Simulation Project
 * DigLocalRegolithMeta.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the DigLocalRegolith task.
 */
public class DigLocalRegolithMeta extends DigLocalMeta {
    
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
    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    	if ((settlement == null) || (settlement.getRegolithCollectionRate() <= 0D)) {
    		return 0D;
    	}
    	
        double settlementCap = settlement.getAmountResourceRemainingCapacity(ResourceUtil.regolithID);
        if (settlementCap < THRESHOLD_AMOUNT) {
        	return 0;
        }
        
    	return getProbability(ResourceUtil.regolithID, settlement, 
    			person, settlement.getRegolithProbabilityValue());
    }
}
