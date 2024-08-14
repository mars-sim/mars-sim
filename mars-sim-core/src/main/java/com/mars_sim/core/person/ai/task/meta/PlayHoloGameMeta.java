/*
 * Mars Simulation Project
 * PlayHoloGameMeta.java
 * @date 2022-07-20
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.PlayHoloGame;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Meta task for the PlayHoloGame task.
 */
public class PlayHoloGameMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.playHoloGame"); //$NON-NLS-1$

    public PlayHoloGameMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		setFavorite(FavoriteType.GAMING);
		setTrait(TaskTrait.AGILITY, TaskTrait.RELAXATION);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new PlayHoloGame(person);
    }

    /**
     * Assesses this person playing a Holo game.
     * 
     * @param person Being assessed
     * @return Potential suitable tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (!person.isInside() || person.isOnDuty()) {
            return EMPTY_TASKLIST;
        }
        	
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
            
        if ((fatigue > 500) || (hunger > 500))
            return EMPTY_TASKLIST;
            
        var result = new RatingScore(1.2D);
        result = assessPersonSuitability(result, person);
  
        if (person.isInSettlement()) {
          	Building recBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
	           		
            if (recBuilding != null) {
                result = assessBuildingSuitability(result, recBuilding, person);
            }
            else {
                // Check if a person has a designated bed
				if (person.hasBed()) {
                    result.addModifier("quarters", RandomUtil.getRandomDouble(0.8));
                }
            }
        }
        // Check if person is in a moving rover.
        else if (Vehicle.inMovingRover(person)) {
            // the bonus inside a vehicle, 
            // rather than having nothing to do if a person is not driving
            result.addModifier("vehicle", 0.5D);
        }
    
        result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.TOURISM);

        return createTaskJobs(result);
    }
}
