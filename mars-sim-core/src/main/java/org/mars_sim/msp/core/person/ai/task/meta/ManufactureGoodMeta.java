/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ManufactureGood;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta extends FactoryMetaTask {

    private static final double CAP = 3_000D; 
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureGood"); //$NON-NLS-1$
    
    public ManufactureGoodMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.CHEMIST,
						JobType.ENGINEER, JobType.PHYSICIST);
                        
        addPreferredRobot(RobotType.MAKERBOT);
        addPreferredRobot(RobotType.REPAIRBOT);
        addPreferredRobot(RobotType.CONSTRUCTIONBOT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureGood(person);
    }

    @Override
    public double getProbability(Person person) {
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        double result = 0D;

        if (person.isInSettlement()) {
            // Check for the override
            if (person.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
                return 0;
            }
        
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
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
                // hunger modifier
                result = result - hunger / 10D;

                // Crowding modifier.
                result *= getBuildingModifier(manufacturingBuilding, person);


                // Manufacturing good value modifier.
                result *= ManufactureGood.getHighestManufacturingProcessValue(person, manufacturingBuilding);
        		result *= person.getSettlement().getGoodsManager().getManufacturingFactor();

                result *= getPersonModifier(person);
                
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
        // Check for the override
        if (robot.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
            return 0;
        }
        
        double result = 0D;

	        if (robot.isInSettlement()) {

            // See if there is an available manufacturing building.
            Building manufacturingBuilding = ManufactureGood.getAvailableManufacturingBuilding(robot);
            if (manufacturingBuilding != null) {
                result = 100D;

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

            
	        // Cancel any manufacturing processes that's beyond the skill of any people
	        // associated with the settlement.
	        if (result > 0)
	        	ManufactureGood.cancelDifficultManufacturingProcesses(robot.getAssociatedSettlement());

        }      

        return result;
    }
}
