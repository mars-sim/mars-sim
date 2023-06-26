/**
 * Mars Simulation Project
 * MissionStep.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;


import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.ProjectStep;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Represent a step that a Mission has to undertake. 
 * Must implement the execute method call when a Worker is active.
 * May also override the start & complete methods to implement any startup or cleardown logic.
 */
public abstract class MissionStep extends ProjectStep {
	private static final SimLogger logger = SimLogger.getLogger(MissionStep.class.getName());

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
     * @return mSol
     */
    protected int getStepDuration() {
        return (int) clock.getMarsTime().getTimeDiff(mission.getPhaseStartTime());
    }

    /**
     * Get the leader for this Mission
     * @return
     */
    protected Person getLeader() {
		return mission.getStartingPerson();
	}

    /**
     * Calculate what resources are needed for this step.
     * The return value may change once the step is active.
     * @param includeOptionals
     * @param resources
     */
    void getRequiredResources(MissionManifest resources, boolean includeOptionals) {
       // Do nonthing; nothing to add
    }

    /**
     * Calculate and add life support resources to the manifest for the crew
     * @param crew Number of crew members
     * @param durationMSol Duration to cover for supplies
     * @param ideal Calculate the ideal amount which will be more thn the minimum
     * @param manifest Place to hold the order
     */
    protected void addLifeSupportResource(int crew, double durationMSol, boolean ideal, MissionManifest manifest) {
        double personSols = (crew * durationMSol)/1000D; // Consumption rates are in Sols
        personSols *= (ideal ? Vehicle.getLifeSupportRangeErrorMargin() : 1D);
        manifest.addResource(ResourceUtil.oxygenID,
                        PhysicalCondition.getOxygenConsumptionRate() * personSols, true);

		manifest.addResource(ResourceUtil.waterID,
                        PhysicalCondition.getWaterConsumptionRate() * personSols, true);

        manifest.addResource(ResourceUtil.foodID,
		                PhysicalCondition.getFoodConsumptionRate() * personSols, true);
    }

    /**
     * Assign a Task to a Worker as part of this mission step
     * @param worker Worker looking to work
     * @param task Task allocated
     */
    protected boolean assignTask(Worker worker, Task task) {
        boolean assignTask = false;

        // Bit messy
        if (worker instanceof Robot r) {
            assignTask = (!r.getMalfunctionManager().hasMalfunction() 
                                && r.getSystemCondition().isBatteryAbove(5));
        }
        else if (worker instanceof Person p) {
            assignTask = (!task.isEffortDriven() || (p.getPerformanceRating() != 0D));
        }

        if (assignTask) {
            assignTask = worker.getTaskManager().addTask(task);
        }
        if (!assignTask) {
            logger.warning(worker, "Unable to start " + task.getName());
        }

        return assignTask;
    }

    protected static UnitManager getUnitManager() {
        return unitManager;
    }

    public static void initializeInstances(MasterClock mc, UnitManager um) {
        clock = mc;
        unitManager = um;
    }
}
