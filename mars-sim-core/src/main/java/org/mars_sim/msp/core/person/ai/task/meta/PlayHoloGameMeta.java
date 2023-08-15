/*
 * Mars Simulation Project
 * PlayHoloGameMeta.java
 * @date 2022-07-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.PlayHoloGame;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PlayHoloGame task.
 */
public class PlayHoloGameMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.playHoloGame"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(PlayHoloGameMeta.class.getName());

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
    public double getProbability(Person person) {
        double result = 0D;

        if (person.isInside()) {

            // Modify probability if during person's work shift.
            if (person.isOnDuty()) {
                return 0;
            }
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double hunger = condition.getHunger();
            double stress = condition.getStress();
            
            if (fatigue > 500)
            	return 0;
            
            if (hunger > 500)
            	return 0;
            
        	double pref = person.getPreference().getPreferenceScore(this);
        	result += pref * 1.2D;
            
            if (pref > 0) {
            	result *= (1 + stress/30.0);
            }
  
            if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    		        // the bonus inside a vehicle, 
    	        	// rather than having nothing to do if a person is not driving
    	        	result += -20;
    	        } 	       
    	        else
    		        // the bonus inside a vehicle, 
    	        	// rather than having nothing to do if a person is not driving
    	        	result += 20;
            }
            
            else { // the person is in the settlement
            	
            	try {
                    	
	            	Building recBuilding = BuildingManager.getAvailableRecBuilding(person);
	           		
	            	if (recBuilding != null) {
                        result *= getBuildingModifier(recBuilding, person);
	            	}
	            	else {
		            	// Check if a person has a designated bed
		                Building quarters = person.getQuarters();    
		                if (quarters == null) {
		                	quarters = LivingAccommodations.getBestAvailableQuarters(person, true);
		
			            	if (quarters == null) {
			            		result *= RandomUtil.getRandomDouble(0.8);
			            	}
			            	else
			            		result *= RandomUtil.getRandomDouble(1.2);
			            }
	           		}
            	
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }

            if (result <= 0) return 0;
            
            result *= person.getAssociatedSettlement().getGoodsManager().getTourismFactor();
            		
            if (result < 0) result = 0;

        }

        return result;
    }
}
