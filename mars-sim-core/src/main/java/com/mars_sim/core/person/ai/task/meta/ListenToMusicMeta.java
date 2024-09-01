/*
 * Mars Simulation Project
 * ListenToMusicMeta.java
 * @date 2022-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.ListenToMusic;
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
 * Meta task for the ListenToMusic task.
 */
public class ListenToMusicMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.listenToMusic"); //$NON-NLS-1$

    public ListenToMusicMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.RELAXATION);

	}

    @Override
    public Task constructInstance(Person person) {
        return new ListenToMusic(person);
    }

	/**
	 * Assesses the suitability of a Person to listen to music.
	 * 
	 * @param person Being assessed
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        if (!person.isInside() || person.isOnDuty()) {
			return EMPTY_TASKLIST;
		}

		double pref = person.getPreference().getPreferenceScore(this);

		var result = new RatingScore((RandomUtil.getRandomDouble(10) + pref) * .25);

		// Probability affected by the person's stress and fatigue.
		PhysicalCondition condition = person.getPhysicalCondition();
		double stress = condition.getStress();
		if (pref > 0) {
			result.addModifier(STRESS_MODIFIER, stress); Math.max(1, stress/20);
		}
		
		if (person.isInSettlement()) {
			// Check if a person has a designated bed
			Building recBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
			result = assessBuildingSuitability(result, recBuilding, person);
		}
	    else {
			// In a moving vehicle?
			result = assessMoving(result, person);
		}

		return createTaskJobs(result);
    }
}
