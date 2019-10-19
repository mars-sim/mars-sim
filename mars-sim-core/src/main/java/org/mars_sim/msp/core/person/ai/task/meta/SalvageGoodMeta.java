/**
 * Mars Simulation Project
 * SalvageGoodMeta.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.SalvageGood;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Meta task for the SalvageGood task.
 */
public class SalvageGoodMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
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

        // If settlement has manufacturing override, no new
        // salvage processes can be created.
        if (person.isInSettlement() && !person.getSettlement().getManufactureOverride()) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
	        // No salvaging goods until after the first month of the simulation.
	        MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
	        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	        double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
	        double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
	        if (totalTimeOrbits < MarsClock.SOLS_PER_MONTH_LONG) {
	            result = 0D;
	            return 0;
	        }

	        if (result != 0) {
	            // See if there is an available manufacturing building.
	            Building manufacturingBuilding = SalvageGood.getAvailableManufacturingBuilding(person);
	            if (manufacturingBuilding != null) {
	                result = 1D;

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
	                SkillManager skillManager = person.getSkillManager();
	                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
	                if (SalvageGood.hasSalvageProcessRequiringWork(manufacturingBuilding, skill)) {
	                    result += 10D;
	                }

	                // Effort-driven task modifier.
			        result *= person.getPerformanceRating();

			        // Job modifier.
			        Job job = person.getMind().getJob();
			        if (job != null) {
			            result *= job.getStartTaskProbabilityModifier(SalvageGood.class);
			        }

			        // Modify if tinkering is the person's favorite activity.
                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
                        result *= 2D;
                    }

        	        // 2015-06-07 Added Preference modifier
        	        if (result > 0)
                    	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

        	        if (result < 0) result = 0;
	            }
	        }


        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}