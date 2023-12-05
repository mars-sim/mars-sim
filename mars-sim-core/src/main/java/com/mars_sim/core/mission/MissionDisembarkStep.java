/**
 * Mars Simulation Project
 * MissionDisembarkStep.java
 * @date 2023-06-10
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.meta.UnloadVehicleMeta;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.util.RandomUtil;

/**
 * this class controls the Mission step of disembarking a Vehicle at a Settlement.
 */
public class MissionDisembarkStep extends MissionStep {

    private static final SimLogger logger = SimLogger.getLogger(MissionDisembarkStep.class.getName());

    /**
     * @param parent Parent mission
     */
    public MissionDisembarkStep(MissionVehicleProject parent) {
        super(parent, Stage.CLOSEDOWN, "Disembark");
    }

    @Override
    protected boolean execute(Worker worker) {
        Vehicle v = getVehicle();

        boolean workOn = false;
        boolean vehicleEmpty = (v.getStoredMass() <= 0D);
        // Check end state as vehicle must be unloaded
        if (!vehicleEmpty) {
           	if (RandomUtil.lessThanRandPercent(50)) {
				workOn = unloadCargo(worker, v);
			} 
        }
        
        // If not unloading; then leave Vehicle
        if (worker.isInVehicle() && !workOn) {
            // leave Vehicle
            boolean inGarage = v.isInAGarage();
            if (!inGarage) {
                // Not in garage so walking to airlock
                workOn = walkToAirLock(worker, v.getSettlement());
            }

            // No on the way out of vehicle yet
            if (!workOn) {
                workOn = walkToGarage(worker, v);
            }            
        }

        // Check everyone is out
        if (vehicleEmpty && everyoneLeft(getMission(), v)) {
            complete();
        }
        return workOn;
    }

    /**
     * Has everyone left the vehicle ?
     * 
	 * @param m Controlling mission
     * @param vehicle Vehicle being checked
     * @return
     */
    private boolean everyoneLeft(MissionProject m, Vehicle vehicle) {
		Crewable c = (Crewable) vehicle;
		for(Worker w : m.getMembers()) {
			Person p = (Person) w;
			if (c.isCrewmember(p)) {
				return false;
			}
		}
		return true;
	}

    /**
     * Sets a worker walking to a Garage.
     * 
     * @param w Worker to move
     * @param v Leaving this vehicle
     * @return
     */
    private boolean walkToGarage(Worker w, Vehicle v) {
        // Just transfer and assing to a different building
        Settlement target = v.getSettlement();

        // Transfer has to be on Units
        if (w instanceof Person p) {
            p.transfer(target); 
        }
        else if (w instanceof Robot r) {
            r.transfer(target);
        }

        Building destBuilding = v.getBuildingLocation();
        if (destBuilding == null) {
            // Shouldn't happen
            destBuilding = target.getBuildingManager().getRandomAirlockBuilding();
        }
        BuildingManager.setToBuilding(w, destBuilding);
        return false;
    }
    
    /**
     * Leaves a vehicle and walk to a building airlock.
     * 
     * @param w Worker wanting to leave
     * @param target Settlement to reach
     * @return Assign a task
     */
    private boolean walkToAirLock(Worker w, Settlement target) {
        Building destinationBuilding = target.getBuildingManager().getRandomAirlockBuilding();
        if (destinationBuilding == null) {
            logger.warning(w, "Cannot find an arirlock in " + target.getName());
            return false;
        }

        if (w instanceof Person p) {
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(destinationBuilding);
            Walk walk = Walk.createWalkingTask(p, adjustedLoc, destinationBuilding);
            if (walk != null) {
                // walk back home
                return assignTask(p, walk);
            }
            else {
                logger.warning(w, "No path to EVA from vehicle.");
            }
        }
        return false;
    }

    /**
     * Attempts to get the worker to unload a vehicle.
     * 
     * @param worker Worker asking to help
     * @param v Vehicle to unload
     * @return
     */
    private boolean unloadCargo(Worker worker, Vehicle v) {
		TaskJob job = UnloadVehicleMeta.createUnloadJob(worker.getAssociatedSettlement(), v);
		boolean assigned = false;
        if (job != null) {
            Task task = null;
            // Create the Task ready for assignment
            if (worker instanceof Person p) {
                task = job.createTask(p);
            }
            else if (worker instanceof Robot r) {
                task = job.createTask(r);
            }

            // Task may be rejected because of the Worker's profile
            if (task != null) {
			    assigned = assignTask(worker, task);
            }
		}
        return assigned;
    }

    private Vehicle getVehicle() {
        return ((MissionVehicleProject)getMission()).getVehicle();
    }

        
    @Override
    public String toString() {
        return "Mission " + getMission().getName() + " disembark";
    }
}
