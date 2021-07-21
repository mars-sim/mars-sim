/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.ManufactureGood;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Makerbot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta extends MetaTask {

    private static final double CAP = 3000D; 
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureGood"); //$NON-NLS-1$
    
    public ManufactureGoodMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISITC);
		setPreferredJob(JobType.ARCHITECT, JobType.CHEMIST,
						JobType.ENGINEER, JobType.PHYSICIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureGood(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement() && !person.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
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
        		result *= person.getSettlement().getGoodsManager().getManufacturingFactor();

                result = applyPersonModifier(result, person);
                
                // Capping the probability as manufacturing process values can be very large numbers.
                if (result > CAP) {
                    result = CAP;
                }
                else if (result < 0) result = 0;
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
	            if (!robot.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
	        	// the person has to be inside the settlement to check for manufacture override

		            // See if there is an available manufacturing building.
		            Building manufacturingBuilding = ManufactureGood.getAvailableManufacturingBuilding(robot);
		            if (manufacturingBuilding != null) {
		                result = 100D;
		                // Manufacturing good value modifier.
//		                result *= ManufactureGood.getHighestManufacturingProcessValue(robot, manufacturingBuilding); //java.util.ConcurrentModificationException

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
		        	ManufactureGood.cancelDifficultManufacturingProcesses(robot.getAssociatedSettlement());

	        }      

        }

        return result;
    }
}
