/*
 * Mars Simulation Project
 * ReadMeta.java
 * @date 2022-07-16
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.Read;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Read task.
 */
public class ReadMeta extends FactoryMetaTask {

    private static final double VALUE = 2.5D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.read"); //$NON-NLS-1$
    
    public ReadMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		setTrait(TaskTrait.TEACHING);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return Read.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || hunger > 750 || !person.isInside()) {
        	return EMPTY_TASKLIST;
        }

        RatingScore result = new RatingScore(VALUE);
        
        result = assessMoving(result, person);
        result = assessPersonSuitability(result, person);

        // The 3 favorite activities drive the person to want to read
        FavoriteType fav = person.getFavorite().getFavoriteActivity();
        double favRating = switch(fav)
                {
                    case RESEARCH -> 2D;
                    case TINKERING -> 0.8D;
                    case LAB_EXPERIMENTATION -> 1.2D;
                    default -> 0D;
                } ;
        if (favRating > 0) {
            result.addModifier(FAV_MODIFIER, favRating);
        }
          
        // If Read is liked; then helps with stress
        double pref = person.getPreference().getPreferenceScore(this);
        if (pref > 0) {
            double stressModifer = 0D;
            if (stress > 25D)
                stressModifer = 1.5;
            else if (stress > 50D)
                stressModifer = 2D;
            else if (stress > 75D)
                stressModifer = 3D;
            if (stressModifer > 0D) {
                result.addModifier(STRESS_MODIFIER, stressModifer);
            }
        }
            
        return createTaskJobs(result);
    }
}
