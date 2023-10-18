/*
 * Mars Simulation Project
 * TeachMeta.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.Collection;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Teach;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskProbabilityUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Teach task.
 */
public class TeachMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.teach"); //$NON-NLS-1$

	private static final int CAP = 1_000;
	
    public TeachMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.TEACHING);
	}

    @Override
    public Task constructInstance(Person person) {
        return new Teach(person);
    }

    /**
     * Assess whether a Person can help teach a task to another. This depends on whether
     * there are students and this person's suitability to teaching.
     * @param person Being assessed
     * @return Potential tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (!person.isInside() || !person.getPhysicalCondition().isFitByLevel(1000, 75, 750)) {
                return EMPTY_TASKLIST;         
        }
        
        // Find potential students.
        Collection<Person> potentialStudents = Teach.getBestStudents(person);
        if (potentialStudents.isEmpty())
            return EMPTY_TASKLIST;

        RatingScore result = new RatingScore(potentialStudents.size() * 30.0);
        
        // Add Preference modifier
        result = assessPersonSuitability(result, person);
        
        return createTaskJobs(result);
    }
    
    @Override
    public Task constructInstance(Robot robot) {
        return new Teach(robot);
    }

    @Override
    public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.isInSettlement()) {

            // Find potential students.
            Collection<Person> potentialStudents = Teach.getBestStudents(robot);
            if (potentialStudents.isEmpty())
            	return 0;

            else {

	            result = potentialStudents.size() * 15D;
	            
	            for (Person student : potentialStudents) {
	                Building building = BuildingManager.getBuilding(student);
	
	                if (building != null) {
	                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot,
	                            building);
	                }
	            }
	            
    	        if (result < 0) result = 0;
            }
        }

        if (result > CAP)
        	result = CAP;
        
        return result;
    }
}
