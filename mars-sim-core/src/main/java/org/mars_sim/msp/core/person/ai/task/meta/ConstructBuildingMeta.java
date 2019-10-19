/**
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(ConstructBuildingMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

	private static final double WEIGHT = 100D;
	
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

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 50 || hunger > 500)
        	return 0;
        
        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            return 0;
        }

        // Check if it is night time.
        if (EVAOperation.isGettingDark(person)) {
        	return 0;
        }

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();
            
            result = getProbability(settlement);
        }


        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(ConstructBuilding.class);
        }

        // Modify if construction is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING)
            result += RandomUtil.getRandomInt(1, 20);

        // 2015-06-07 Added Preference modifier
        if (result > 0D) {
            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        }

        if (result < 0D) {
            result = 0D;
        }


        return result;
    }

    
    public double getProbability(Settlement settlement) {

        double result = 0D;

        try {
            // Crowded settlement modifier
            int associated = settlement.getNumCitizens();
            int cap = settlement.getPopulationCapacity();
            if (associated >= cap) {
                result = WEIGHT * associated/cap * associated;
            }
            
            // Check all building construction missions occurring at the settlement.
            List<BuildingConstructionMission> missions = ConstructBuilding.
                    getAllMissionsNeedingAssistance(settlement);
            
            int size = missions.size();
            
//            double factor = 0;
//            if (size == 0)
//            	factor = 1;
//            else if (size == 1)
//            	factor = Math.pow(1.5, 2);
//            else 
//            	factor = Math.pow(size, 2);
//            
//            result /= factor;
            
            result *= size;
            
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding building construction missions.", e);
        }
        
        return result;
    }
        
	public Task constructInstance(Robot robot) {
        return null;
	}

	public double getProbability(Robot robot) {
        return 0;
    }
}