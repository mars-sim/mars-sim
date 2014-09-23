/**
 * Mars Simulation Project
 * ManufactureConstructionMaterialsMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ManufactureConstructionMaterials;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ManufactureConstructionMaterials task.
 */
public class ManufactureConstructionMaterialsMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureConstructionMaterials"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(ManufactureConstructionMaterialsMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ManufactureConstructionMaterials(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            try {
                // See if there is an available manufacturing building.
                Building manufacturingBuilding = ManufactureConstructionMaterials.getAvailableManufacturingBuilding(person);
                if (manufacturingBuilding != null) {
                    result = 1D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person,
                            manufacturingBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person,
                            manufacturingBuilding);

                    // Manufacturing good value modifier.
                    result *= ManufactureConstructionMaterials.getHighestManufacturingProcessValue(person,
                            manufacturingBuilding);

                    if (result > 100D) {
                        result = 100D;
                    }

                    // If manufacturing building has process requiring work, add
                    // modifier.
                    SkillManager skillManager = person.getMind().getSkillManager();
                    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                    if (ManufactureConstructionMaterials.hasProcessRequiringWork(manufacturingBuilding, skill)) {
                        result += 10D;
                    }
                    
                    // If settlement has manufacturing override, no new
                    // manufacturing processes can be created.
                    else if (person.getSettlement().getManufactureOverride()) {
                        result = 0;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "ManufactureConstructionMaterials.getProbability()", e);
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(
                    ManufactureConstructionMaterials.class);
        }

        return result;
    }
}