/**
 * Mars Simulation Project
 * MissionStep.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package com.mars_sim.core.mission;


import com.mars_sim.core.UnitManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.ProjectStep;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Represent a step that a Mission has to undertake. 
 * Must implement the execute method call when a Worker is active.
 * May also override the start & complete methods to implement any startup or cleardown logic.
 */
public abstract class MissionStep extends ProjectStep {
	private static final long serialVersionUID = 1L;

    private static UnitManager unitManager;

    private static MasterClock clock;

    private MissionProject mission;

    protected MissionStep(MissionProject project, Stage stage, String description) {
        super(stage, description);
        this.mission = project;
    }
    
    protected MissionProject getMission() {
        return mission;
    }

    /**
     * How on has the current step been running?
     * 
     * @return mSol
     */
    protected int getStepDuration() {
        return (int) clock.getMarsTime().getTimeDiff(mission.getPhaseStartTime());
    }

    /**
     * Gets the leader for this Mission.
     * 
     * @return
     */
    protected Person getLeader() {
		return mission.getStartingPerson();
	}

    /**
     * Calculates what resources are needed for this step.
     * 
     * Method should be empty
     * The return value may change once the step is active.
     * 
     * @param includeOptionals
     * @param resources
     */
    protected void getRequiredResources(SuppliesManifest resources, boolean includeOptionals) {}

    /**
     * Calculates and add life support resources to the manifest for the crew.
     * 
     * @param crew Number of crew members
     * @param durationMSol Duration to cover for supplies
     * @param ideal Calculate the ideal amount which will be more than the minimum
     * @param manifest Place to hold the order
     */
    protected void addLifeSupportResource(int crew, double durationMSol, boolean ideal, SuppliesManifest manifest) {
        double personSols = (crew * durationMSol) / 1000D; // Consumption rates are in Sols
        personSols *= (ideal ? Vehicle.getLifeSupportRangeErrorMargin() : 1D);
        manifest.addAmount(ResourceUtil.OXYGEN_ID,
                        PhysicalCondition.getOxygenConsumptionRate() * personSols, true);

		manifest.addAmount(ResourceUtil.WATER_ID,
                        PhysicalCondition.getWaterConsumptionRate() * personSols, true);

        manifest.addAmount(ResourceUtil.FOOD_ID,
		                PhysicalCondition.getFoodConsumptionRate() * personSols, true);
    }

    /**
     * Assigns a Task to a Worker as part of this mission step.
     * 
     * @param worker Worker looking to work
     * @param task Task allocated
     */
    protected boolean assignTask(Worker worker, Task task) {
        return worker.getTaskManager().directlyAssignTask(task, false);
    }

    protected static UnitManager getUnitManager() {
        return unitManager;
    }

    public static void initializeInstances(MasterClock mc, UnitManager um) {
        clock = mc;
        unitManager = um;
    }

    /**
     * Gets the objective for this step. By default this is null.
     */
    public MissionObjective getObjective() {
        return null;
    }
}
