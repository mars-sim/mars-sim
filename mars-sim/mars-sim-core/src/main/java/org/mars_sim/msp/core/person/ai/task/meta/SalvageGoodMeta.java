/**
 * Mars Simulation Project
 * SalvageGoodMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.SalvageGood;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Meta task for the SalvageGood task.
 */
public class SalvageGoodMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageGood"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new SalvageGood(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // See if there is an available manufacturing building.
            Building manufacturingBuilding = SalvageGood.getAvailableManufacturingBuilding(person);
            if (manufacturingBuilding != null) {
                result = 1D;

                // No salvaging goods until after the first month of the simulation.
                MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
                MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
                double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
                double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
                if (totalTimeOrbits < MarsClock.SOLS_IN_MONTH_LONG) {
                    result = 0D;
                }

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, manufacturingBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, manufacturingBuilding);

                // Salvaging good value modifier.
                result *= SalvageGood.getHighestSalvagingProcessValue(person, manufacturingBuilding);

                if (result > 100D) {
                    result = 100D;
                }

                // If manufacturing building has salvage process requiring work, add
                // modifier.
                SkillManager skillManager = person.getMind().getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                if (SalvageGood.hasSalvageProcessRequiringWork(manufacturingBuilding, skill)) {
                    result += 10D;
                }

                // If settlement has manufacturing override, no new
                // salvage processes can be created.
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
            result *= job.getStartTaskProbabilityModifier(SalvageGood.class);
        }

        return result;
    }
}