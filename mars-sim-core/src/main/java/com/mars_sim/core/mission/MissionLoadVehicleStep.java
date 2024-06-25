/*
 * Mars Simulation Project
 * MissionLoadVehicleStep.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadVehicleEVA;
import com.mars_sim.core.vehicle.task.LoadVehicleGarage;
import com.mars_sim.core.vehicle.task.LoadingController;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This mission step is responsible for loading a vehicle ready for a new Mission.
 * Workers may be set the local task.
 */
public class MissionLoadVehicleStep extends MissionStep {

    private static final long serialVersionUID = 1L;
	private static final MissionStatus CANNOT_LOAD_RESOURCES = new MissionStatus("Mission.status.loadResources");
    private LoadingController loadingPlan;

    /**
     * @param parent Parent mission
     */
    public MissionLoadVehicleStep(MissionVehicleProject parent) {
        super(parent, Stage.PREPARATION, "Load Vehicle");
    }

    /**
     * Executes the vehicle loading step. This will create a Loading controller on the first
     * call ready for later use.
     * 
     * @param worker The worker attempting to execute the loading
     */
    @Override
    protected boolean execute(Worker worker) {
        MissionVehicleProject vp = (MissionVehicleProject) getMission();
        Vehicle v = vp.getVehicle();
        Settlement settlement = v.getSettlement();
		if (loadingPlan == null) {
            MissionManifest manifest = vp.getResources(true);
			loadingPlan = new LoadingController(v.getSettlement(), v,
												manifest.getResources(true),
												manifest.getResources(false),
                                                manifest.getEquipment(true),
												manifest.getEquipment(false));	
            
                                                
            // Try and move the vehicle to garage
		    settlement.getBuildingManager().addToGarage(v);
		}

        // Loading still active
        boolean workOn = false;
        if (loadingPlan.isFailure()) {
            vp.abortMission(CANNOT_LOAD_RESOURCES);
        }
        else if (!loadingPlan.isCompleted()) {
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
     * Gets the loading plan associated with the step.
     * 
     * @return
     */
    public LoadingController getLoadingPlan() {
        return loadingPlan;
    }

    /**
     * Creates the most suitable Load Vehicle task if possible.
     * 
     * @param worker
     * @param vehicle
     * @return
     */
    private Task createLoadTask(Worker worker, Vehicle vehicle) {
        boolean inGarage = vehicle.isInGarage();
        VehicleMission target = (VehicleMission) getMission();
        if (worker.isInSettlement())
        	return null;
        if (worker instanceof Person p) {
            if (inGarage) {
                return new LoadVehicleGarage(p, target.getLoadingPlan());
            }
            return new LoadVehicleEVA(p, target.getLoadingPlan());
        }
        else if ((worker instanceof Robot r) && inGarage) {
            return new LoadVehicleGarage(r, target.getLoadingPlan());
        }

        return null;
    }

    @Override
	public String toString() {
		return "Mission " + getMission().getName() + " load  vehicle";
	}
}
