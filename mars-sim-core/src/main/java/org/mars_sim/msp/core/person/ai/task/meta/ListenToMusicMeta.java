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

//        // TODO: listening to music is driven by boredom, not so much fatigue
//        // Fatigue modifier.
//        double fatigue = person.getPhysicalCondition().getFatigue();
//    	result = fatigue;
//
//        if (fatigue > 800D) {
//            result += (fatigue - 800D) / 4D;
//        }

        // Crowding modifier
        if (person.isInSettlement()) {
        	
           if (person.isInVehicle()) {
        	   result *= RandomUtil.getRandomDouble(2); // more likely to listen to music than not if on a vehicle
            }
            
        	// Stress modifier
            double stress = person.getPhysicalCondition().getStress();
            result += stress * 3D; // 0 to 100%
            
            try {
            	// 2016-01-10 Added checking if a person has a designated bed
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

            // 2015-06-07 Added Preference modifier
            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this)/2D;
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
