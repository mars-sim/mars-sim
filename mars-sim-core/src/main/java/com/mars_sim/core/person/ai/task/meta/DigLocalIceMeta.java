/*
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @date 2023-06-08
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.DigLocalIce;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;


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

    /**
     * Assess a Person's suitability to dig ice locally. Depends on many factors.
     * @param person Being assessed.
     * @return List of potential TaskJobs
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        Settlement settlement = person.getSettlement();
    	
        // Check if settlement has DIG_LOCAL_REGOLITH override flag set.
        if ((settlement == null) 
            || !person.isInSettlement()
            || settlement.getProcessOverride(OverrideType.DIG_LOCAL_ICE)
            || (settlement.getAmountResourceRemainingCapacity(ResourceUtil.iceID) < THRESHOLD_AMOUNT)) {
        	return EMPTY_TASKLIST;
        }
    	
    	return getTaskJobs(ResourceUtil.iceID, settlement, 
    			person, settlement.getIceCollectionRate() * settlement.getIceProbabilityValue());

    }
}
