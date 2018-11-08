/**
 * Mars Simulation Project
 * PlayHoloGameMeta.java
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
import org.mars_sim.msp.core.person.ai.task.PlayHoloGame;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the PlayHoloGame task.
 */
public class PlayHoloGameMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.playHoloGame"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .1D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(PlayHoloGameMeta.class.getName());

    private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;// = sim.getMasterClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();

	public PlayHoloGameMeta() {
        masterClock = sim.getMasterClock();
        if (masterClock != null) { // to avoid NullPointerException during maven test
	        marsClock = masterClock.getMarsClock();
        }
        
	}
	
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PlayHoloGame(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        if (person.isInside()) {

            // Stress modifier
        	double stress = person.getPhysicalCondition().getStress(); //0.0 to 100.0

            if (stress > 20D) {
                result += (stress - 20D) * 2;
            }

            if (person.isInVehicle()) {
            	result *= RandomUtil.getRandomDouble(1.5);
            }
            
            else {
            	
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

            result *= person.getAssociatedSettlement().getGoodsManager().getTourismFactor();

            // Add Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/2D;

            		
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
        double result = 0D;
        return result;
	}
}
