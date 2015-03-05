/**
 * Mars Simulation Project
 * SleepMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta implements MetaTask {
    
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

        // Fatigue modifier.
        double fatigue = person.getPhysicalCondition().getFatigue();
        if (fatigue > 500D) {
            result += (fatigue - 500D) / 4D;
        }
        
        if (result < 0) result = 0;
        
        // Dark outside modifier.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            result *= 2D;
        }
        
        // Crowding modifier.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            Building building = Sleep.getAvailableLivingQuartersBuilding(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            }
        }
        
        // No sleeping outside.
        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Sleep(robot);
	}

	@Override
	public double getProbability(Robot robot) {
	      
        double result = 0D;
        
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

            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, building);
                //result *= TaskProbabilityUtil.getRelationshipModifier(robot, building);
            }
        }
        
        // No sleeping outside.
        if (robot.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }

        return result;
	}
}