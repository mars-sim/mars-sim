/**
 * Mars Simulation Project
 * SleepMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.sleep"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Sleep(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // TODO: check if the person has already slept for 300 millisols (give or take)
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
        		|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

            // Fatigue modifier.
            double fatigue = person.getPhysicalCondition().getFatigue();
        	result = fatigue;

            if (fatigue > 500D) {
                result += (fatigue - 500D) / 4D;
            }

            // Check if person is an astronomer.
            boolean isAstronomer = (person.getMind().getJob() instanceof Astronomer);

            // Dark outside modifier.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            boolean isDark = (surface.getSolarIrradiance(person.getCoordinates()) == 0);
            if (isDark && !isAstronomer) {
                // Non-astronomers more likely to sleep when it's dark out.
                result *= 4D;
            }
            else if (!isDark && isAstronomer) {
                // Astronomers more likely to sleep when it's not dark out.
                result *= 4D;
            }

            Building building = Sleep.getAvailableLivingQuartersBuilding(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            }

            // 2015-06-07 Added Preference modifier
	        if (result > 0)
	        	result += person.getPreference().getPreferenceScore(this);
            
		    boolean isOnCall = person.getTaskSchedule().getShiftType().equals(ShiftType.ON_CALL);

		    if (!isOnCall) {
	            // Check if person's work shift will begin in the next 50 millisols.
	            int millisols = (int) Simulation.instance().getMasterClock().getMarsClock().getMillisol();
	            millisols += 50;
	            if (millisols > 1000) {
	                millisols -= 1000;
	            }
	            
	            boolean willBeShiftHour = person.getTaskSchedule().isShiftHour(millisols);
	            if (willBeShiftHour) {
	            	//if work shift is slated to begin in the next 50 millisols, probability of sleep reduces to one quarter of its value
	                result = result/4D; 
	            }
		    }
		    else {
		    	; // allows sleep during "on-call" work shift type
		    }

		    if (result < 0) result = 0;
        }

        // No sleeping outside.
        //else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
        //   result = 0D;
        //}

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Sleep(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        // No sleeping outside.
        if (robot.getLocationSituation() == LocationSituation.OUTSIDE)
            result = 0D;


        // TODO: in what case should a bot "relax" or slow down its pace?
        // result += robot.getPhysicalCondition().getStress();

        /*
        // Fatigue modifier.
        double fatigue = robot.getPhysicalCondition().getFatigue();
        if (fatigue > 500D) {
            result = (fatigue - 500D) / 4D;
        }
        */
        // Dark outside modifier.
        //SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        //if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0) {
        //    result *= 2D;
        //}

        // Crowding modifier.
        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	result += 2;


        	// TODO: stay at the work location
        	// TODO: go to that building to recharge if battery is low

            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
            	result += 2;

                //result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, building);
                //result *= TaskProbabilityUtil.getRelationshipModifier(robot, building);
            }
        }


        return result;
	}
}