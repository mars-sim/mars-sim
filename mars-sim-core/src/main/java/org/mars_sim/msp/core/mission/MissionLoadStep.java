/*
 * Mars Simulation Project
 * MissionLoadStep.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This mission step is responsible for loading a vehicle ready for a new Mission.
 * Workers may be set the local task.
 */
public class MissionLoadStep extends MissionStep {

    private static final MissionStatus CANNOT_LOAD_RESOURCES = new MissionStatus("Mission.status.loadResources");
    private LoadingController loadingPlan;

    /**
     * @param parent Parent mission
     */
    public MissionLoadStep(MissionVehicleProject parent) {
        super(parent, Stage.PREPARATION, "Load Vehicle");
    }

    /**
     * Execute the vehicle loading step. This will create a Loading controller on the first
     * call ready for later use.
     * @param worker The worker attemting to execute the loading
     */
    @Override
    protected boolean execute(Worker worker) {
        MissionVehicleProject vp = (MissionVehicleProject) getProject();
        Vehicle v = vp.getVehicle();
        Settlement settlement = v.getSettlement();
		if (loadingPlan == null) {
            MissionManifest manifest = vp.getResources(true);
			loadingPlan = new LoadingController(v.getSettlement(), v,
												manifest.getResources(true),
												manifest.getResources(false),
                                                manifest.getItems(true),
												manifest.getItems(false));	
            
                                                
            // Try and move the vehicle to garage
		    settlement.getBuildingManager().addToGarage(v);
		}

        // Loading still active
        if (loadingPlan.isFailure()) {
            vp.abortMission(CANNOT_LOAD_RESOURCES);
            return false;
        }

        boolean workOn = false;
        if (!loadingPlan.isCompleted()) {
			// Load vehicle if not fully loaded.

            // Note: randomly select this member to load resources for the rover
			// This allows person to do other important things such as eating
			if (worker.isInSettlement()
				&& RandomUtil.lessThanRandPercent(75)) {

				Task job = createLoadTask(worker, v);
				if (job != null) {
                    workOn = assignTask(worker, job);
				}
			}
		}
		else {
			complete();
		}

        return workOn;
    }

    /**
     * Get the loading plan associetd with the step.
     * @return
     */
    public LoadingController getLoadingPlan() {
        return loadingPlan;
    }

    /**
     * Create the most suitable Load Vehicle task if possible
     * @param worker
     * @param vehicle
     * @return
     */
    private Task createLoadTask(Worker worker, Vehicle vehicle) {
        boolean inGarage = vehicle.isInAGarage();
        VehicleMission target = (VehicleMission) getProject();
        if (worker instanceof Person p) {
            if (inGarage) {
                return new LoadVehicleEVA(p, target);
            }
            return new LoadVehicleGarage(p, target);
        }
        else if ((worker instanceof Robot r) && inGarage) {
            return new LoadVehicleGarage(r, target);
        }

        return null;
    }
}
