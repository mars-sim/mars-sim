/*
 * Mars Simulation Project
 * DigLocalRegolithMeta.java
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
 * Meta task for the DigLocalRegolith task.
 */
public class DigLocalRegolithMeta extends DigLocalMeta {
    		
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$
    
    public DigLocalRegolithMeta() {
		super(NAME, DigLocalRegolith.CONTAINER_TYPE);
	}

    @Override
    protected Task createTask(Person person) {
        return new DigLocalRegolith(person);
    }

    /**
     * Assesses what digging tasks can be done at a Settlement.
     * 
     * @param settlement The focus of the search
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
    	
        // Check if settlement has DIG_LOCAL_REGOLITH override flag set.
        if (settlement.getProcessOverride(OverrideType.DIG_LOCAL_REGOLITH)) {
        	return Collections.emptyList();
        }
    	
        double iceValue = settlement.getIceProbabilityValue();
        
        double regValue = settlement.getRegolithProbabilityValue();
       
        double relativeValue = 0;
        
        // Note: this is for prioritizing digging local ice more than regolith
        if (regValue > iceValue) {
        	relativeValue = regValue - iceValue / 2;
        }
        else {
        	relativeValue = regValue / 2;
        }
           
    	return getSettlementTaskJobs(ResourceUtil.REGOLITH_ID, settlement, relativeValue);
    }
}
