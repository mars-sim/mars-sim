/*
 * Mars Simulation Project
 * PlayHoloGameMeta.java
 * @date 2022-07-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.PlayHoloGame;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

    @Override
    public RatingScore getRating(Person person) {

        if (!person.isInside() || person.isOnDuty()) {
            return RatingScore.ZERO_RATING;
        }
        	
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double stress = condition.getStress();
            
        if ((fatigue > 500) || (hunger > 500))
            return RatingScore.ZERO_RATING;
            
        double pref = person.getPreference().getPreferenceScore(this);
        var result = new RatingScore(1.2D);
        result.addModifier(FAV_MODIFIER, pref);
        
        if (pref > 0) {
            result.addModifier(STRESS_MODIFIER, 1 + stress/30.0);
        }
  
        if (person.isInSettlement()) {
          	Building recBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
	           		
            if (recBuilding != null) {
                assessBuildingSuitability(result, recBuilding, person);
            }
            else {
                // Check if a person has a designated bed
                if ((person.getQuarters() == null) 
                        && (LivingAccommodations.getBestAvailableQuarters(person, true, true) == null)) {

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
    
        result.addModifier(GOODS_MODIFIER,
                        person.getAssociatedSettlement().getGoodsManager().getTourismFactor());

        return result;
    }
}
