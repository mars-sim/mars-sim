/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @version 3.07 2014-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ManufactureGood;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Manufacturing";
    
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

        // Cancel any manufacturing processes that's beyond the skill of any people 
        // associated with the settlement.
        ManufactureGood.cancelDifficultManufacturingProcesses(person);

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // See if there is an available manufacturing building.
            Building manufacturingBuilding = ManufactureGood.getAvailableManufacturingBuilding(person);
            if (manufacturingBuilding != null) {
                result = 1D;

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, manufacturingBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, manufacturingBuilding);

                // Manufacturing good value modifier.
                result *= ManufactureGood.getHighestManufacturingProcessValue(person, manufacturingBuilding);

                if (result > 100D) {
                    result = 100D;
                }

                // If manufacturing building has process requiring work, add
                // modifier.
                SkillManager skillManager = person.getMind().getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                if (ManufactureGood.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                    result += 10D;
                }

                // If settlement has manufacturing override, no new
                // manufacturing processes can be created.
                else if (person.getSettlement().getManufactureOverride()) {
                    result = 0;
                }
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(ManufactureGood.class);
        }

        return result;
    }
}