/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Relax;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Relax task.
 */
public class RelaxMeta extends FactoryMetaTask{

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .25D;

	private static final double WEIGHT = 2D;
	
    public RelaxMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.RELAXATION);
	}
   
    @Override
    public Task constructInstance(Person person) {
    	return new Relax(person);
    }

    /**
     * Assesses whether a person can relax. Many depends upon whether they are on Duty or not.
     * 
     * @param person Being assessed.
     * @return Potential TaskJobs.
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        if (!person.isInside()) {
            return EMPTY_TASKLIST;
        }

        RatingScore result = new RatingScore(WEIGHT);
            
        Building recBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
        result = assessBuildingSuitability(result, recBuilding, person);
        result = assessPersonSuitability(result, person);

        // Modify probability if during person's work shift.
        boolean isShiftHour = person.isOnDuty();
        if (isShiftHour) {
            result.addModifier("shift", WORK_SHIFT_MODIFIER);
        }
            
        return createTaskJobs(result);
    }
}
