/**
 * Mars Simulation Project
 * ListenToMusicMeta.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.ListenToMusic;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ListenToMusic task.
 */
public class ListenToMusicMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.listenToMusic"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .2D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(ListenToMusicMeta.class.getName());
 
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ListenToMusic(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
        
        if (person.isInSettlement()) {
            
            try {
            	// Check if a person has a designated bed
                Building quarters = person.getQuarters();    
                if (quarters == null) {
                	quarters = Sleep.getBestAvailableQuarters(person, true);

	            	if (quarters == null) {
		                Building recBuilding = ListenToMusic.getAvailableRecreationBuilding(person);
			                if (recBuilding != null) {
			                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, recBuilding);
			                    result *= TaskProbabilityUtil.getRelationshipModifier(person, recBuilding);
//			                    result *= RandomUtil.getRandomDouble(3);
			                }
	            	}
//	            	else
//	            		result *= RandomUtil.getRandomDouble(1.5);
	            }
	
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            
            // Modify probability if during person's work shift.
            int millisols = marsClock.getMillisolInt();
            boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
            if (isShiftHour && person.getShiftType() != ShiftType.ON_CALL) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
            if (result < 0) result = 0;
            
        }
            
        else if (person.isInVehicle()) {	
	        // Check if person is in a moving rover.
	        if (Vehicle.inMovingRover(person)) {
	        	result += 20D;
	        }
        }
        
        if (person.isInside() && result > 0) {
	        double pref = person.getPreference().getPreferenceScore(this);
	        
	     	result = pref * 2.5D;
	     	
	        // Probability affected by the person's stress and fatigue.
	        PhysicalCondition condition = person.getPhysicalCondition();
	        double stress = condition.getStress();
	        
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
        }
        
        if (result < 0) result = 0;
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return null;
	}

	@Override
	public double getProbability(Robot robot) {
        return 0;
	}
}
