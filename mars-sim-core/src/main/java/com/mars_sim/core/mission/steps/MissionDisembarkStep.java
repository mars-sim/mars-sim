/**
 * Mars Simulation Project
 * MissionDisembarkStep.java
 * @date 2023-06-10
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import java.util.Set;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.WalkingSteps;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.task.RequestMedicalTreatment;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.UnloadVehicleMeta;

/**
 * this class controls the Mission step of disembarking a Vehicle at a Settlement.
 */
public class MissionDisembarkStep extends MissionStep {

    private static final long serialVersionUID = 1L;
	private static final SimLogger logger = SimLogger.getLogger(MissionDisembarkStep.class.getName());

    /**
     * @param parent Parent mission
     */
    public MissionDisembarkStep(MissionVehicleProject parent) {
        super(parent, Stage.CLOSEDOWN, "Disembark");
    }

    /**
     * This step has just become the active step so mark Vehicle as unloading
     */
    @Override
    protected void start() {
        getVehicle().addSecondaryStatus(StatusType.UNLOADING);
    }

    @Override
    protected boolean execute(Worker worker) {
        Vehicle v = getVehicle();

        boolean workOn = false;
         boolean vehicleEmpty = !v.haveStatusType(StatusType.UNLOADING);
        // Check end state as vehicle must be unloaded
        if (!vehicleEmpty && RandomUtil.lessThanRandPercent(50)) {
			workOn = unloadCargo(worker, v);
        }
        
        // If not unloading; then leave Vehicle
        if (worker.isInVehicle() && !workOn) {
            // leave Vehicle
            boolean inGarage = v.isInGarage();
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
  
        BuildingManager.transferToBldg(w, w.getBuildingLocation(), destBuilding);
        return false;
    }
    
    /**
     * Leaves a vehicle and walk to a building airlock.
     * 
     * @param w Worker wanting to leave
     * @param disembarkSettlement Settlement to reach
     * @return Assign a task
     */
    private boolean walkToAirLock(Worker w, Settlement disembarkSettlement) {
    	Person person = (Person)w;
    	
		boolean hasStrength = person.getPhysicalCondition().isFitByLevel(1500, 90, 1500);

		if (!hasStrength) {
			// Note 1: Help this person put on an EVA suit
			// Note 2: consider inflatable medical tent for emergency transport of incapacitated personnel
			logger.info(person, 10_000, 
					 Msg.getString("mission.status.emergencyEnterSettlement", person.getName(),
							disembarkSettlement.getName())); //$NON-NLS-1$

			logger.info(person, 10_000, ""
					+ "Currently at "
					+ person.getLocationTag().getExtendedLocation()); 

			// Initiate an rescue operation
			// Note: Gets a lead person to perform it and give him a rescue badge
			// rescueOperation((Rover)getVehicle(), person, disembarkSettlement);

//			logger.info(person, 10_000, "" + "Transported to "
//					+ person.getLocationTag().getExtendedLocation()); 
			
			// Note: how to force the person to receive some form of medical treatment ?
	
			Task currentTask = person.getMind().getTaskManager().getTask();
			if (currentTask != null && !currentTask.getName().equalsIgnoreCase(RequestMedicalTreatment.NAME)) {
				person.getMind().getTaskManager().addPendingTask(RequestMedicalTreatment.SIMPLE_NAME);
			}
			
			return false;
		}
	
		Set<Building> airlocks = disembarkSettlement.getBuildingManager().getAirlocks();
			
		if (airlocks != null && airlocks.isEmpty()) {
			logger.severe(person, 10_000, "No airlock found at " + disembarkSettlement);
		}
		
		boolean canDo = false;
				
		for (Building destinationBuilding: airlocks) {
			
			// Get random airlock building at settlement.
	//		Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();
		
			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);
	
				WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, destinationBuilding);
				boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
				
				if (canWalk) {
					canDo = assignTask(person, new Walk(person, walkingSteps));
					if (!canDo) {
						logger.warning(person, 20_000, "Unable to walk back to " + disembarkSettlement + " via " + destinationBuilding + ".");
					}
					else
						return true;
				}
			}
		}
		
		if (!canDo) {
			logger.warning(person, 20_000, "Currently no airlock was found available to walk back to " + disembarkSettlement + ".");
		}
		
		return canDo;
//		
//    	
//        Building destinationBuilding = target.getBuildingManager().getRandomAirlockBuilding();
//        if (destinationBuilding == null) {
//            logger.warning(w, "Cannot find an airlock in " + target.getName());
//            return false;
//        }
//
//        if (w instanceof Person p) {
//			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);
//            Walk walk = Walk.createWalkingTask(p, adjustedLoc, destinationBuilding, true);
//            if (walk != null) {
//                // walk back home
//                return assignTask(p, walk);
//            }
//            else {
//                logger.warning(w, "No path to EVA from vehicle.");
//            }
//        }
//        
//        return false;
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
