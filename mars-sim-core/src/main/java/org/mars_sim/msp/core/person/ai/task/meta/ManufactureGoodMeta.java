/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ManufactureGood;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Makerbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    private static final double CAP = 3000D; 
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureGood"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureGood(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement() && !person.getSettlement().getManufactureOverride()) {
            // the person has to be inside the settlement to check for manufacture override

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
            // See if there is an available manufacturing building.
            Building manufacturingBuilding = ManufactureGood.getAvailableManufacturingBuilding(person);
            if (manufacturingBuilding != null) {

                // If manufacturing building has process requiring work, add
                // modifier.
                SkillManager skillManager = person.getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                if (ManufactureGood.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                    result += 10D;
                }

                // Stress modifier
                result = result - stress * 3.5D;
                // fatigue modifier
                result = result - (fatigue - 100) / 2.5D;

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, manufacturingBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, manufacturingBuilding);

                // Manufacturing good value modifier.
                result *= ManufactureGood.getHighestManufacturingProcessValue(person, manufacturingBuilding);

                // Effort-driven task modifier.
                result *= person.getPerformanceRating();

                // Job modifier.
                Job job = person.getMind().getJob();
                if (job != null) {
                    result *= job.getStartTaskProbabilityModifier(ManufactureGood.class)
                    		* person.getSettlement().getGoodsManager().getManufacturingFactor();
                }

                // Modify if tinkering is the person's favorite activity.
                if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
                    result *= RandomUtil.getRandomDouble(2D);
                }

                // Add Preference modifier
                if (result > 0D) {
                    result = result + result * person.getPreference().getPreferenceScore(this)/6D;
                }
                
                // Capping the probability at 100 as manufacturing process values can be very large numbers.
                if (result > CAP) {
                    result = CAP;
                }
                
                if (result < 0) result = 0;
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ManufactureGood(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Makerbot) {

	        if (robot.isInSettlement()) {
	            // If settlement has manufacturing override, no new
	            // manufacturing processes can be created.
	            if (!robot.getSettlement().getManufactureOverride()) {
	        	// the person has to be inside the settlement to check for manufacture override

		            // See if there is an available manufacturing building.
		            Building manufacturingBuilding = ManufactureGood.getAvailableManufacturingBuilding(robot);
		            if (manufacturingBuilding != null) {
		                result = 100D;

		                // Crowding modifier.
		                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, manufacturingBuilding);
		                //result *= TaskProbabilityUtil.getRelationshipModifier(robot, manufacturingBuilding);

		                // Manufacturing good value modifier.
		                result *= ManufactureGood.getHighestManufacturingProcessValue(robot, manufacturingBuilding);

		                if (result > 100D) {
		                    result = 100D;
		                }

		                // If manufacturing building has process requiring work, add
		                // modifier.
		                SkillManager skillManager = robot.getSkillManager();
		                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
		                if (ManufactureGood.hasProcessRequiringWork(manufacturingBuilding, skill)) {
		                    result += 10D;
		                }

			            // Effort-driven task modifier.
			            result *= robot.getPerformanceRating();
		            }

		        }
	            
		        // Cancel any manufacturing processes that's beyond the skill of any people
		        // associated with the settlement.
		        if (result > 0)
		        	ManufactureGood.cancelDifficultManufacturingProcesses(robot);

	        }      

        }

        return result;
    }
}