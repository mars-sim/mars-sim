/**
 * Mars Simulation Project
 * PlayHoloGameMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.PlayHoloGame;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PlayHoloGame task.
 */
public class PlayHoloGameMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.playHoloGame"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .1D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(PlayHoloGameMeta.class.getName());

    public PlayHoloGameMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		addFavorite(FavoriteType.GAMING);
		addTrait(TaskTrait.AGILITY);
		addTrait(TaskTrait.RELAXATION);

	}
    
    @Override
    public Task constructInstance(Person person) {
        return new PlayHoloGame(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double hunger = condition.getHunger();
            double stress = condition.getStress();
            
            if (fatigue > 1000)
            	return 0;
            
            if (hunger > 1000)
            	return 0;
            
        	double pref = person.getPreference().getPreferenceScore(this);
            
        	result = pref * 3D;
            
            if (pref > 0) {
             	if (stress > 45D)
             		result*=1.5;
             	else if (stress > 65D)
             		result*=2D;
             	else if (stress > 85D)
             		result*=3D;
             	else
             		result*=4D;
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
                    	
	            	Building recBuilding = PlayHoloGame.getAvailableRecreationBuilding(person);
	           		
	            	if (recBuilding != null) {
	                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, recBuilding);
	                    result *= TaskProbabilityUtil.getRelationshipModifier(person, recBuilding);
	                    result *= RandomUtil.getRandomDouble(3);
	            	}
	            	else {
		            	// Check if a person has a designated bed
		                Building quarters = person.getQuarters();    
		                if (quarters == null) {
		                	quarters = Sleep.getBestAvailableQuarters(person, true);
		
			            	if (quarters == null) {
			            		result *= RandomUtil.getRandomDouble(2);
			            	}
			            	else
			            		result *= RandomUtil.getRandomDouble(1.2);
			            }
	           		}
            	
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }
            	
            }
            
            // Modify probability if during person's work shift.
            int millisols = marsClock.getMillisolInt();
            boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
            if (isShiftHour) {
                result*= WORK_SHIFT_MODIFIER;
            }

            if (result <= 0) return 0;
            
            result *= person.getAssociatedSettlement().getGoodsManager().getTourismFactor();
            		
            if (result < 0) result = 0;

        }


        return result;
    }
}
