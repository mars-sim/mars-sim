/*
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta extends DigLocalMeta {

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
    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    	if ((settlement == null) || (settlement.getIceCollectionRate() <= 0D)) {
    		return 0D;
    	}
    	
        double settlementCap = settlement.getAmountResourceRemainingCapacity(ResourceUtil.iceID);
        if (settlementCap < THRESHOLD_AMOUNT) {
        	return 0;
        }
    	
    	return getProbability(ResourceUtil.iceID, settlement, 
    			person, settlement.getIceProbabilityValue());
    }
}
