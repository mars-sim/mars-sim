/*
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.utility.power.FuelPowerSource;
import com.mars_sim.core.structure.building.utility.power.PowerGeneration;
import com.mars_sim.core.structure.building.utility.power.PowerSource;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the ToggleFuelPowerSource task.
 */
public class ToggleFuelPowerSourceMeta extends MetaTask implements SettlementMetaTask {

	/**
     * Represents a Job needed in a Fishery
     */
    private static class PowerTaskJob extends SettlementTask {
    	
		private static final long serialVersionUID = 1L;

		private FuelPowerSource powerSource;

        public PowerTaskJob(SettlementMetaTask owner, Building building, FuelPowerSource powerSource,
                                    RatingScore score) {
            super(owner, "Toggle " + powerSource.getType().getName(), building, score);
			this.powerSource = powerSource;
		}

         /**
         * The Building holding the power source is the focus.
         */
        private Building getBuilding() {
            return (Building) getFocus();
        }

        @Override
        public Task createTask(Person person) {
            return new ToggleFuelPowerSource(person, getBuilding(), powerSource);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                PowerTaskJob other = (PowerTaskJob) obj;
                if (powerSource == null) {
                    if (other.powerSource != null)
                        return false;
                } else if (!powerSource.equals(other.powerSource))
                    return false;
                else
                    return true;
            }
            return false;
        }
    }
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleFuelPowerSource"); //$NON-NLS-1$

	private static final double FACTOR = 20D;

    public ToggleFuelPowerSourceMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);
	}

    /**
     * Gets the score for a Settlement task for a person. 
     * Considers EVA fitness and overcrowding for inside activities.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
            Building building = ((PowerTaskJob)t).getBuilding();
      
            // Checks if this is a standalone power building that requires EVA to reach
            if ((BuildingCategory.POWER == building.getCategory()) 
                    &&     // Checks if the person is physically fit for heavy EVA tasks
                    !EVAOperation.isEVAFit(p)) {
                // Probability affected by the person's stress, hunger, thirst and fatigue.
                return factor;
            }
	          
			factor = super.assessPersonSuitability(t, p);
            if ((factor.getScore() > 0) && building.hasFunction(FunctionType.LIFE_SUPPORT)) {
                // Factor in building crowding and relationship factors.
                assessBuildingSuitability(factor, building, p);
            }
		}
		return factor;
	}
    
    /**
     * Finds any tasks related to toggle power supplies. 
     * Scans for POWER_GENERATION functions.
     * 
     * @param settlement Source Settlement to scan
     * @return List of potential Tasks.
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();
        for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.POWER_GENERATION)) {
            // Select the best Fuel power source
            double bestDiff = 0D;
            FuelPowerSource bestSource = null;
            PowerGeneration powerGeneration = building.getPowerGeneration();
            for(PowerSource powerSource : powerGeneration.getPowerSources()) {
                if (powerSource instanceof FuelPowerSource fuelSource) {
                    double diff = scorePowerSource(settlement, fuelSource);
                    if (diff > bestDiff) {
                        bestDiff = diff;
                        bestSource = fuelSource;
                    }
                }
            }

            if (bestDiff > 0) {
                RatingScore score = new RatingScore(FACTOR);
                score.addModifier("best", bestDiff);
                tasks.add(new PowerTaskJob(this, building, bestSource, score));
            }
        }
        
        return tasks;
    }

	/**
     * Gets the score of a Power source based on the difference in value 
     * between inputs and output and whether it is running but exhausted inputs.
     * 
     * @param settlement the settlement the resource process is at.
     * @param fuelSource the fuel power source.
     * @return the value diff (value points)
     */
    private static double scorePowerSource(Settlement settlement, FuelPowerSource fuelSource) {

        double diff = 0D;

        // If source is not on; then score is the difference between outputs and inputs
        if (!fuelSource.isToggleON()) {
            double inputValue = getInputResourcesValue(settlement, fuelSource);
            double outputValue = getPowerOutputValue(settlement, fuelSource);
            diff = outputValue - inputValue;
        }

        // Check if settlement doesn't have one or more of the input resources.
        if (fuelSource.isToggleON() && isEmptyInputResource(settlement, fuelSource)) {
            diff = 10D;
        }
        return diff;
    }

    /**
     * Gets the total value of a fuel power sources input resources.
     * 
     * @param settlement the settlement.
     * @param fuel source the fuel power source.
     * @return the total value for the input resources per Sol.
     */
    private static double getInputResourcesValue(Settlement settlement, FuelPowerSource fuelSource) {
    	int resource = fuelSource.getFuelResourceID();
    	// Gets the rate [kg/sol]
        double massPerSol = fuelSource.getFuelConsumptionRate();
        // Gets the demand for this fuel
        double value = settlement.getGoodsManager().getDemandValueWithID(resource);
        return value * massPerSol;
    }

    /**
     * Gets the total value of the power produced by the power source.
     * 
     * @param settlement the settlement.
     * @param fuelSource the fuel power source.
     * @return the value of the power generated per Sol.
     */
    private static double getPowerOutputValue(Settlement settlement, FuelPowerSource fuelSource) {
        // Get settlement value for kW hr produced.
        double power = fuelSource.getMaxPower();
        double powerPerSol = power * MarsTime.HOURS_PER_MILLISOL * 1000D;
        return powerPerSol * settlement.getPowerGrid().getPowerValue();
    }

    /**
     * Checks if a fuel power source has no input resources.
     * 
     * @param settlement the settlement the resource is at.
     * @param fuelSource the fuel power source.
     * @return true if any input resources are empty.
     */
    private static boolean isEmptyInputResource(Settlement settlement,
            FuelPowerSource fuelSource) {
        boolean result = false;

        int resource = fuelSource.getFuelResourceID();
        double stored = settlement.getAmountResourceStored(resource);
        if (stored == 0D) {
            result = true;
        }

        return result;
    }
}