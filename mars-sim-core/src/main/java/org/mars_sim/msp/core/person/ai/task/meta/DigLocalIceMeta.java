/*
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta extends DigLocalMeta {

	private static SimLogger logger = SimLogger.getLogger(DigLocalIceMeta.class.getName());
	
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
    	if (!CollectionUtils.isSettlement(person.getCoordinates())) {
    		return 0;
    	}
    	
    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    	double rate = settlement.getIceCollectionRate();
    	if (rate <= 0D) {
    		return 0D;
    	}
    	
        double settlementCap = settlement.getAmountResourceRemainingCapacity(ResourceUtil.iceID);
        if (settlementCap < THRESHOLD_AMOUNT) {
        	return 0;
        }
    	
    	double result = getProbability(ResourceUtil.iceID, settlement, 
    			person, rate * settlement.getIceProbabilityValue());
    	
    	logger.info(settlement, 20_000, "rate: " + Math.round(settlement.getIceCollectionRate() * 100.0)/100.0  
    			+ "  Final ice: " + Math.round(result* 100.0)/100.0);
        
        return result;
    }
}
