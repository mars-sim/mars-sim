/*
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ToggleFuelPowerSource;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.time.MarsClock;

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
		private Building building;

        public PowerTaskJob(SettlementMetaTask owner, Building building, FuelPowerSource powerSource, double score) {
            super(owner, "Toggle " + powerSource.getType().getName() + " @ " + building.getName(), score);
			this.building = building;
			this.powerSource = powerSource;
		}

        @Override
        public Task createTask(Person person) {
            return new ToggleFuelPowerSource(person, building, powerSource);
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
     * Get the score for a Settlement task for a person. Considers EVA fitness and overcrowding for inside activities.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
            Building building = ((PowerTaskJob)t).building;
      
            // Checks if this is a standalone power building that requires EVA to reach
            if ((BuildingCategory.POWER == building.getCategory()) 
                    &&     // Checks if the person is physically fit for heavy EVA tasks
                    !EVAOperation.isEVAFit(p)) {
                // Probability affected by the person's stress, hunger, thirst and fatigue.
                return 0D;
            }
	                      
            if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
                // Factor in building crowding and relationship factors.
                factor *= getBuildingModifier(building, p);
            }
            factor *= getPersonModifier(p);
		}
		return factor;
	}
    
    /**
     * Find any tasks realetd to toggle power supplies. Scans for POWER_GENERATION functions
     * @param settlemetn Source Settlement to scna
     * @return Lit of potential Tasks.
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();
        BuildingManager manager = settlement.getBuildingManager();
        for(Building building : manager.getBuildings(FunctionType.POWER_GENERATION)) {
            // Select the best Fuel power source
            double bestDiff = 0D;
            FuelPowerSource bestSource = null;
            PowerGeneration powerGeneration = building.getPowerGeneration();
            for(PowerSource powerSource : powerGeneration.getPowerSources()) {
                if (powerSource instanceof FuelPowerSource) {
                    FuelPowerSource fuelSource = (FuelPowerSource) powerSource;
                    double diff = scorePowerSource(settlement, fuelSource);
                    if (diff > bestDiff) {
                        bestDiff = diff;
                        bestSource = fuelSource;
                    }
                }
            }

            double score = bestDiff * FACTOR;
            if (score > 0) {
                tasks.add(new PowerTaskJob(this, building, bestSource, score));
            }
        }
        
        return tasks;
    }

	/**
     * Gets the score of a Power source based on the difference in vlaue between inputs and output
	 * and whether it is running but exhausted inputs.
     * 
     * @param settlement the settlement the resource process is at.
     * @param fuelSource the fuel power source.
     * @return the value diff (value points)
     */
    private static double scorePowerSource(Settlement settlement, FuelPowerSource fuelSource) {

        double diff = 0D;

        // If sourc eis not on; then score is the differenc ebetween outputs and inputs
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
    	// Gets the rate [kg/Sol]
        double massPerSol = fuelSource.getFuelConsumptionRate();
        // Gets the demand for this fuel
        double value = settlement.getGoodsManager().getDemandValueWithID(resource);
        return value * massPerSol;
    }

    /**
     * Gets the total value of the power produced by the power source.
     * @param settlement the settlement.
     * @param fuelSource the fuel power source.
     * @return the value of the power generated per Sol.
     */
    private static double getPowerOutputValue(Settlement settlement, FuelPowerSource fuelSource) {
        // Get settlement value for kW hr produced.
        double power = fuelSource.getMaxPower();
        double powerPerSol = power * MarsClock.HOURS_PER_MILLISOL * 1000D;
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

    @Override
    public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        // TODO Auto-generated method stub
        return 0;
    }
}