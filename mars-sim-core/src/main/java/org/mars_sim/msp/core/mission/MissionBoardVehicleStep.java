/**
 * Mars Simulation Project
 * MissionDepartStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Get all Members ready to depart on the mission
 */
public class MissionBoardVehicleStep extends MissionStep {
	private static final SimLogger logger = SimLogger.getLogger(MissionBoardVehicleStep.class.getName());

	/* Howlong do Worker have to complete departure */
	private static final int DEPARTURE_DURATION = 150;
	private static final int DEPARTURE_PREPARATION = 15;

    private boolean notifiedMembers = false;

    /**
     * @param parent Parent mission
     */
    public MissionBoardVehicleStep(MissionVehicleProject parent) {
        super(parent, Stage.PREPARATION, "Board Vehicle");
    }

    @Override
    protected boolean execute(Worker worker) {
        boolean canWork = false;
        if (!notifiedMembers) {
            // Tell everyone to get onboard
            callMembersToMission((int)(DEPARTURE_DURATION - DEPARTURE_PREPARATION));
            notifiedMembers = true;
        }

        boolean canLeave = false;
        if (!worker.isInVehicle()) {
            // Get in vehicle
        }
        else if (everyoneOnBoard(worker.getVehicle())) {
            canLeave = true;
        }
        else if (getStepDuration() > DEPARTURE_DURATION) {
            // Evict stragglers
            evictStragglers(worker.getVehicle());
            canLeave = true;
        }
        
        if (canLeave) {
            depart(worker.getVehicle());
            complete();
        }
        return canWork;
    }

    /**
     * Prepare the vehcile to depart the Settlement
     * @param v
     */
    private void depart(Vehicle v) {
        // If the rover is in a garage, put the rover outside.
        if (v.isInAGarage()) {
            BuildingManager.removeFromGarage(v);
        }

        // Record the start mass right before departing the settlement
        //recordStartMass();

        // Embark from settlement
        // if (!v.transfer(unitManager.getMarsSurface())) {
        //     getProject().endMissionProblem(v, "Could not exit Settlement");
        // }

        // // Marks everyone departed
        // for(Worker m : getProject().getMembers()) {
        //     Person p = (Person) m;
        //     p.getTaskManager().recordActivity(getName(), "Departed", getName(), this);
        // }
    }

    /**
     * Evict any members not on board the vehicle.
     * @param v Vehicle to check
     */
    private void evictStragglers(Vehicle v) {
    }

    /**
     * How on has the current step been running?
     * @return mSol
     */
    private int getStepDuration() {
        return 0;
    }

    /**
     * Call all members to boad; alarms the members
     * @param boardingTime Time to board vehicle
     */
    private void callMembersToMission(int boardingTime) {
    }

    /**
     * Is everyone on  board the vehicle
     * @param vehicle
     * @return
     */
    private boolean everyoneOnBoard(Vehicle vehicle) {
        return false;
    }
		// // Can depart if every is on the vehicle or time has run out
		// boolean canDepart = isEveryoneInRover();
		// if (!canDepart && (getPhaseDuration() > DEPARTURE_DURATION)) {
		// 	// Find who has not boarded
		// 	List<Person> ejectedMembers = new ArrayList<>();
		// 	Rover r = getRover();
		// 	for(Worker m : getMembers()) {
		// 		Person p = (Person) m;
		// 		if (!r.isCrewmember(p)) {
		// 			ejectedMembers.add(p);
		// 		}
		// 	}



		// 	// Must have the leader
		// 	if (!ejectedMembers.contains(getStartingPerson())) {
		// 		// Still enough members ? If so eject late arrivals
		// 		if ((getMembers().size() - ejectedMembers.size()) >= 2) {
		// 			for(Person ej : ejectedMembers) {
		// 				logger.info(ej, "Ejected from mission " + getName() + " missed Departure");
		// 				removeMember(ej);
		// 				addMissionLog(ej.getName() + " evicted");
		// 			}
		// 			canDepart = true;
		// 		}
		// 	}
		// 	else {
		// 		// Too many generated
		// 		//logger.info(member, "Leader " + getStartingPerson().getName() + " still not boarded for mission " + getName());
		// 	}
		// }

		// // Check if everyone is boarded
		// if (canDepart) {
		// 	// If the rover is in a garage, put the rover outside.
		// 	if (v.isInAGarage()) {
		// 		BuildingManager.removeFromGarage(v);
		// 	}

		// 	// Record the start mass right before departing the settlement
		// 	recordStartMass();

		// 	// Embark from settlement
		// 	if (v.transfer(unitManager.getMarsSurface())) {
		// 		setPhaseEnded(true);
		// 	}
		// 	else {
		// 		endMissionProblem(v, "Could not exit Settlement");
		// 	}

		// 	// Marks everyone departed
		// 	for(Worker m : getMembers()) {
		// 		Person p = (Person) m;
		// 		p.getTaskManager().recordActivity(getName(), "Departed", getName(), this);
		// 	}
		// }
		// else {
		// 	// Add the rover to a garage if possible.
		// 	boolean	isRoverInAGarage = settlement.getBuildingManager().addToGarage(v);

		// 	// Gets a random location within rover.
		// 	LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(v);
		// 	callMembersToMission((int)(DEPARTURE_DURATION - DEPARTURE_PREPARATION));
			
		// 	if (member instanceof Person) {
		// 		Person person = (Person) member;

		// 		// If person is not aboard the rover, board the rover and be ready to depart.
		// 		if (!getRover().isCrewmember(person)) {

		// 			Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, v);
		// 			if (walk != null) {
		// 				boolean canDo = assignTask(person, walk);
		// 				if (!canDo) {
		// 					logger.warning(person, "Unable to start walk to " + v + ".");
		// 				}

		// 				if (!isDone() && isRoverInAGarage
		// 					&& settlement.findNumContainersOfType(EquipmentType.EVA_SUIT) > 1
		// 					&& !hasBaselineNumEVASuit(v)) {

		// 					EVASuit suit = InventoryUtil.getGoodEVASuitNResource(settlement, person);
		// 					if (suit != null && !suit.transfer(v)) {
		// 						logger.warning(person, "Unable to transfer a spare " + suit.getName() + " from "
		// 							+ settlement + " to " + v + ".");
		// 					}
		// 				}
		// 			}

		// 			else { // this crew member cannot find the walking steps to enter the rover
		// 				logger.warning(member, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
		// 						v.getName()));
		// 			}
		// 		}
		// 	}

		// 	else if (member instanceof Robot) {
		// 		Robot robot = (Robot) member;
		// 		Walk walkingTask = Walk.createWalkingTask(robot, adjustedLoc, v);
		// 		if (walkingTask != null) {
		// 			boolean canDo = assignTask(robot, walkingTask);
		// 			if (!canDo) {
		// 				logger.warning(robot, "Unable to walk to " + v + ".");
		// 			}
		// 		}
		// 		else {
		// 			logger.severe(member, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
		// 					v.getName()));
		// 		}
		// 	}
		// }
}
