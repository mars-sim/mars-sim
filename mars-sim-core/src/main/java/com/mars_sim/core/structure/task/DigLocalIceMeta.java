/*
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.structure.task;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta extends DigLocalMeta {
		
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    
    public DigLocalIceMeta() {
		super(NAME, DigLocalIce.CONTAINER_TYPE);
	}

    @Override
    protected Task createTask(Person person) {
        return new DigLocalIce(person);
    }

    /**
     * Assesses what digging tasks can be done at a settlement.
     * 
     * @param settlement The focus of the search
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
    	
        // Check if settlement has DIG_LOCAL_REGOLITH override flag set.
        if (settlement.getProcessOverride(OverrideType.DIG_LOCAL_ICE)) {
        	return Collections.emptyList();
        }
    	
    	return getSettlementTaskJobs(ResourceUtil.ICE_ID, settlement, 
            settlement.getIceProbabilityValue());
    }
}