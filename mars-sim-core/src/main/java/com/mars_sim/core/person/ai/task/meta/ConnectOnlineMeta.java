/*
 * Mars Simulation Project
 * ConnectOnlineMeta.java
 * @date 2023-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.ConnectOnline;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the ConnectOnline task.
 */
public class ConnectOnlineMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.connectOnline"); //$NON-NLS-1$

    public ConnectOnlineMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.PEOPLE);
		setPreferredJob(JobType.POLITICIAN, JobType.REPORTER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConnectOnline(person);
    }

    /**
     * Assess a person's suitabilty connecting online to friends
     * @param person Being assessed
     * @return lis of tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
 
        if (!person.isInside() || person.isOnDuty()) {
            return EMPTY_TASKLIST;
        }
        		
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1500 || hunger > 1500)
            return EMPTY_TASKLIST;
            
        double pref = person.getPreference().getPreferenceScore(this);
        
        // Use preference modifier
        double base = (RandomUtil.getRandomDouble(10) + pref) * .5;
        base -= fatigue/100 + hunger/100;

        if (pref > 0) {
            base *= Math.max(1, stress/20);
        }
        var result = new RatingScore(base);
	        
        if (person.isInSettlement()) {	
            // Get an available office space.
            Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.COMMUNICATION);

            if (building != null) {
                // A comm facility has terminal equipment that provides communication access with Earth
                // It is necessary
                result = assessBuildingSuitability(result, building, person);
            }   
        }
            
        return createTaskJobs(result);
    }
}
