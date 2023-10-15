/*
 * Mars Simulation Project
 * ConnectOnlineMeta.java
 * @date 2023-08-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ConnectOnline;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

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
