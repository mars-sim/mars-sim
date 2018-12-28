/**
 * Mars Simulation Project
 * ListenToMusicMeta.java
 * @version 3.08 2015-11-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.ListenToMusic;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

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

    private static MarsClock marsClock;
    
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

        double pref = person.getPreference().getPreferenceScore(this);
        
     	result = pref * 5D;
     	
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

        
        // Crowding modifier
        if (person.isInSettlement()) {
        	
           if (person.isInVehicle()) {
        	   result *= RandomUtil.getRandomDouble(2); // more likely to listen to music than not if on a vehicle
            }
            
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
			                    result *= RandomUtil.getRandomDouble(3);
			                }
	            	}
	            	else
	            		result *= RandomUtil.getRandomDouble(2);
	            }
	
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            
            if (marsClock == null)
            	marsClock = Simulation.instance().getMasterClock().getMarsClock();
            // Modify probability if during person's work shift.
            int millisols = marsClock.getMillisolInt();
            boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
            if (isShiftHour) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
            if (result < 0) result = 0;
            
        }

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
