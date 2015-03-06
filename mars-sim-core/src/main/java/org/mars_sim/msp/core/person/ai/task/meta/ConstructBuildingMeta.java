/**
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @version 3.07 2015-03-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Constructionbot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(ConstructBuildingMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ConstructBuilding(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates()))
                result = 0D;
        } 
        
        if (result != 0 )  {// if task penalty is not zero
	            
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	            
	            // Check all building construction missions occurring at the settlement.
	            try {
	                List<BuildingConstructionMission> missions = ConstructBuilding.
	                        getAllMissionsNeedingAssistance(person.getSettlement());
	                result = 50D * missions.size();
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error finding building construction missions.", e);
	            }
	        }
	        
	        // Crowded settlement modifier
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	            Settlement settlement = person.getSettlement();
	            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
	                result *= 2D;
	            }
	        }
	        
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	        
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(ConstructBuilding.class);        
	        }
	        
        }
    
        return result;
    }


	public Task constructInstance(Robot robot) {
        return new ConstructBuilding(robot);
	}

	public double getProbability(Robot robot) {
	       
        double result = 0D;

        
        if (robot.getBotMind().getRobotJob() instanceof Constructionbot) {

	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(robot) == null) {
	            result = 0D;
	        }
	
	        // Check if it is night time.
	        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	        if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0) {
	            if (!surface.inDarkPolarRegion(robot.getCoordinates()))
	                result = 0D;
	        } 
	        
	        if (result != 0 )  {// if task penalty is not zero
	        
	            
	            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	                
	                // Check all building construction missions occurring at the settlement.
	                try {
	                    List<BuildingConstructionMission> missions = ConstructBuilding.
	                            getAllMissionsNeedingAssistance(robot.getSettlement());
	                    result = 50D * missions.size();
	                }
	                catch (Exception e) {
	                    logger.log(Level.SEVERE, "Error finding building construction missions.", e);
	                }
	            }
	
	        	
		        // Crowded settlement modifier
		        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
		            Settlement settlement = robot.getSettlement();
		            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
		                result *= 2D;
		            }
		        }
		        
		        // Effort-driven task modifier.
		        result *= robot.getPerformanceRating();
		        
	        }
        
        }
        return result;
    }
}