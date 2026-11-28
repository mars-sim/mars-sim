/*
 * Mars Simulation Project
 * MissionLoadVehicleStep.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadVehicleEVA;
import com.mars_sim.core.vehicle.task.LoadVehicleGarage;
import com.mars_sim.core.vehicle.task.LoadingController;

/**
 * This mission step is responsible for loading a vehicle ready for a new Mission.
 * Workers may be set the local task.
 */
public class MissionLoadVehicleStep extends MissionStep {

    private static final String DESCRIPTION = "Load Vehicle";
    private static final long serialVersionUID = 1L;
    private LoadingController loadingPlan;

    /**
     * Constructor.
     * 
     * @param parent Parent mission
     */
    public MissionLoadVehicleStep(MissionVehicleProject parent) {
        super(parent, Stage.PREPARATION, DESCRIPTION);
    }

    /**
     * Gets the description of the loading. This is dynamic and contains the loading progress
     */
    @Override 
    public String getDescription() {
        int progress = loadingPlan == null ? 0 : loadingPlan.getProgress();

        // A fudge so the first call does not have 0% displayed
        if (progress > 0) {
            return DESCRIPTION + ": " + progress + "%";
        }
        return DESCRIPTION;
    }

    /**
     * Starts a step and makes it active. Marks the vehicle for loading.
     */
    @Override
    protected void start() {
        MissionVehicleProject vp = (MissionVehicleProject) getMission();
        Vehicle v = vp.getVehicle();
        Settlement settlement = v.getSettlement();
		if (loadingPlan == null) {
			loadingPlan = v.setLoading(vp.getResources(true));	
                                                            
            // Try and move the vehicle to garage
		    settlement.getBuildingManager().addToGarage(v);
		}
    }


    /**
     * Executes the vehicle loading step. This will create a Loading controller on the first
     * call ready for later use.
     * 
     * @param worker The worker attempting to execute the loading
     */
    @Override
    protected boolean execute(Worker worker) {
        // Loading still active
        boolean workOn = false;
        int missingRes = loadingPlan.getFailedResource();
        if (missingRes >= 0) {
            Vehicle vehicle = loadingPlan.getVehicle();
            vehicle.removeSecondaryStatus(StatusType.LOADING);
            getMission().abortMission(MissionStatus.createResourceStatus(missingRes), getLeader());
        }
        else if (!loadingPlan.isCompleted()) {
			// Load vehicle if not fully loaded.

            // Note: randomly select this member to load resources for the rover
			// This allows person to do other important things such as eating
			if (worker.isInSettlement()
				&& RandomUtil.lessThanRandPercent(75)) {

				Task job = createLoadTask(worker, loadingPlan.getVehicle());
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
     * Creates the most suitable Load Vehicle task if possible.
     * 
     * @param worker
     * @param vehicle
     * @return
     */
    private Task createLoadTask(Worker worker, Vehicle vehicle) {
        boolean inGarage = vehicle.isInGarage();
        if (worker.isInSettlement())
        	return null;
        if (worker instanceof Person p) {
            if (inGarage) {
                return new LoadVehicleGarage(p, vehicle);
            }
            return new LoadVehicleEVA(p, vehicle);
        }
        else if ((worker instanceof Robot r) && inGarage) {
            return new LoadVehicleGarage(r, vehicle);
        }

        return null;
    }

    @Override
	public String toString() {
		return "Mission " + getMission().getName() + " load vehicle";
	}
}
