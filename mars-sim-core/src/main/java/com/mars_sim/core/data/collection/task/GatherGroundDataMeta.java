/*
 * Mars Simulation Project
 * GatherGroundDataMeta.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection.task;

import java.util.List;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the GatherGroundData task.
 */
public class GatherGroundDataMeta extends GatherDataMeta {
    		
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$
    
    public GatherGroundDataMeta() {
		super(NAME, GatherGroundData.CONTAINER_TYPE);
	}

    @Override
    protected Task createTask(Person person) {
        return new GatherGroundData(person);
    }

    /**
     * Assesses what digging tasks can be done at a Settlement.
     * 
     * @param settlement The focus of the search
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
    	
    	return getSettlementTaskJobs(settlement, 0.0);
    }
}
