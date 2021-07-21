/**
 * Mars Simulation Project
 * ManufactureConstructionMaterialsMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.ManufactureConstructionMaterials;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ManufactureConstructionMaterials task.
 */
public class ManufactureConstructionMaterialsMeta extends MetaTask {
    
    private static final double CAP = 3000D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureConstructionMaterials"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(ManufactureConstructionMaterialsMeta.class.getName());

    public ManufactureConstructionMaterialsMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISITC);
		setPreferredJob(JobType.ARCHITECT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureConstructionMaterials(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
    	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            // If settlement has manufacturing override, no new
            // manufacturing processes can be created.
            if (person.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
                return 0;
            }
            
            try {
                // See if there is an available manufacturing building.
                Building manufacturingBuilding = ManufactureConstructionMaterials.getAvailableManufacturingBuilding(person);
                if (manufacturingBuilding != null) {
                	
                	if (manufacturingBuilding.getManufacture().getBuilding().getMalfunctionManager().hasMalfunction())
                		return 0;
                	
                    result = 1D;

                    // If manufacturing building has process requiring work, add
                    // modifier.
                    SkillManager skillManager = person.getSkillManager();
                    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                    
                    if (ManufactureConstructionMaterials.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                        result += 10D;
                    }
                    
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person,
                            manufacturingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person,
                            manufacturingBuilding);

                    // Manufacturing good value modifier.
                    result *= ManufactureConstructionMaterials.getHighestManufacturingProcessValue(person,
                            manufacturingBuilding);
                    
            		result *= person.getSettlement().getGoodsManager().getManufacturingFactor();

            		result = applyPersonModifier(result, person);
            		
                    // Capping the probability as manufacturing process values can be very large numbers.
                    if (result > CAP) {
                        result = CAP;
                    }
                    else if (result < 0) result = 0;
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "ManufactureConstructionMaterials.getProbability()", e);
            }

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ManufactureConstructionMaterials(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.isInSettlement()) {
    	
            try {
                // See if there is an available manufacturing building.
                Building manufacturingBuilding = ManufactureConstructionMaterials.getAvailableManufacturingBuilding(robot);
                if (manufacturingBuilding != null) {
                    result = 1D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot,
                            manufacturingBuilding);

                    // Manufacturing good value modifier.
                    result *= ManufactureConstructionMaterials.getHighestManufacturingProcessValue(robot,
                            manufacturingBuilding);

                    // Cap the result to a max value of 100.
                    if (result > 100D) {
                        result = 100D;
                    }

                    // If manufacturing building has process requiring work, add
                    // modifier.
                    SkillManager skillManager = robot.getSkillManager();
                    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                    
                    if (ManufactureConstructionMaterials.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                        result += 10D;
                    }

                    // If settlement has manufacturing override, no new
                    // manufacturing processes can be created.
                    else if (robot.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {
                        result = 0;
                        return 0;
                    }
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "ManufactureConstructionMaterials.getProbability()", e);
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();
            
            if (result < 0) result = 0;

        }

        return result;
	}
}
