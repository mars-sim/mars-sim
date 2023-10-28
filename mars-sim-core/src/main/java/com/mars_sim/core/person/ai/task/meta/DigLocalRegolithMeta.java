/*
 * Mars Simulation Project
 * DigLocalRegolithMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.DigLocalRegolith;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the DigLocalRegolith task.
 */
public class DigLocalRegolithMeta extends DigLocalMeta {
    
	// May add back private static SimLogger logger = SimLogger.getLogger(DigLocalRegolithMeta.class.getName())
	
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

    /**
     * Assess a Persons suitability to dig Regolith locally. Depends on many conditions.
     * @param person Being assessed
     * @return TaskJobs that could be done
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
    	
    	Settlement settlement = person.getSettlement();
    	
        // Check if settlement has DIG_LOCAL_REGOLITH override flag set.
        if ((settlement == null) 
            || !person.isInSettlement()
            || settlement.getProcessOverride(OverrideType.DIG_LOCAL_REGOLITH)
            || (settlement.getAmountResourceRemainingCapacity(ResourceUtil.regolithID) < THRESHOLD_AMOUNT)) {
        	return EMPTY_TASKLIST;
        }
    	
    	return getTaskJobs(ResourceUtil.regolithID, settlement, 
    			person, settlement.getRegolithCollectionRate() * settlement.getRegolithProbabilityValue());
    }
}
