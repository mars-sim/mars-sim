/**
 * Mars Simulation Project
 * MissionDepartStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Get all Members ready to depart on the mission
 */
public class MissionBoardVehicleStep extends MissionStep {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(MissionBoardVehicleStep.class.getName());

	/* How long do Worker have to complete departure */
	private static final int DEPARTURE_DURATION = 150;
	private static final int DEPARTURE_PREPARATION = 15;

    private boolean notifiedMembers = false;

    /**
     * @param parent Parent mission
     */
    public MissionBoardVehicleStep(MissionVehicleProject parent) {
        super(parent, Stage.PREPARATION, "Board Vehicle");
    }

	private Vehicle getVehicle() {
		return ((MissionVehicleProject) getMission()).getVehicle();
	}

    @Override
    protected boolean execute(Worker worker) {
		MissionProject mp = getMission();
		Vehicle v = getVehicle();

		boolean canWork = false;
        if (!notifiedMembers) {
            // Tell everyone to get onboard
            callMembersToMission(mp, worker, v, (int)(DEPARTURE_DURATION - DEPARTURE_PREPARATION));
            notifiedMembers = true;
        }

        boolean canLeave = false;
        if (!v.equals(worker.getVehicle())) {
            // Get in vehicle, walk there
			canWork = boardVehicle(worker, v);
        }
        else if (everyoneOnBoard(mp, v)) {
            canLeave = true;
        }
        else if (getStepDuration() > DEPARTURE_DURATION) {
            // Evict stragglers
            canLeave = evictStragglers(mp, v);
        }
        
		// All good to leave
        if (canLeave) {
            depart(mp, v);
			mp.getLog().setStarted();
            complete();
        }
        return canWork;
    }

	/**
     * Prepare the vehicle to depart the Settlement
	 * @param m Mission in control
     * @param v Vehicle to prepare
     */
    private void depart(MissionProject m, Vehicle v) {
		logger.info(v, "Ready to depart for " + m.getName());

        // If the vehcile is in a garage, put the rover outside.
        if (v.isInAGarage()) {
            BuildingManager.removeFromGarage(v);
        }

        // Record the start mass right before departing the settlement
        //recordStartMass();
		
        // Embark from settlement
		if (!v.transfer(getUnitManager().getMarsSurface())) {
		    m.abortMission("Vehicle could not exit Settlement");
		}

        // Marks everyone departed
        for(Worker w : m.getMembers()) {
            w.getTaskManager().recordActivity(m.getName(), "Departed", m.getName(), m);
        }
    }

    /**
     * Evict any members not on board the vehicle.
     * @param v Vehicle to check
	 * @return Has all evictions completed?
     */
    private boolean evictStragglers(MissionProject m, Vehicle v) {
		logger.info(v, "Look to evict members for " + m.getName());

		List<Person> ejectedMembers = new ArrayList<>();
		Collection<Worker> members = m.getMembers();
		Crewable c = (Crewable) v;
		for(Worker w : members) {
			Person p = (Person) w;
			if (!c.isCrewmember(p)) {
				ejectedMembers.add(p);
			}
		}

		boolean canDepart = false;

		// Must have the leader
		if (!ejectedMembers.contains(getLeader())) {
			// Still enough members ? If so eject late arrivals
			if ((members.size() - ejectedMembers.size()) >= 2) {
				for(Person ej : ejectedMembers) {
					logger.info(ej, "Ejected from mission " + m.getName() + " missed Departure");
					m.removeMember(ej);
					m.addMissionLog(ej.getName() + " evicted");
				}
				canDepart = true;
			}
		}
		return canDepart;
    }

	/**
     * Call all members to board; alarms the members
	 * @param m Mission in control
	 * @param caller Calling members to board
	 * @param v Vehicle to board
     * @param boardingTime Time to board vehicle
     */
    private void callMembersToMission(MissionProject m, Worker caller, Vehicle v, int boardingTime) {
		logger.info(caller, "Call members to board " + v.getName() + " for " + m.getName());

		// Set the members' work shift to on-call to get ready
		for (Worker w : getMission().getMembers()) {
			if (w instanceof Person p) {
				// If first time this person has been caleld and there is a limit interrupt them
				if (!p.getShiftSlot().setOnCall(true) && (boardingTime > 0)) {
					// First call so 
					if (p.getTaskManager().getTask() instanceof Sleep s) {
						// Not create but the only way
						s.setAlarm(boardingTime);
					}
				}
			}
		}
    }

    /**
     * Is everyone on  board the vehicle ?
	 * @param m Controlling mission
     * @param vehicle Vehicle being checked
     * @return
     */
    private boolean everyoneOnBoard(MissionProject m, Vehicle vehicle) {
		Crewable c = (Crewable) vehicle;
		for(Worker w : m.getMembers()) {
			Person p = (Person) w;
			if (!c.isCrewmember(p)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Set the worker to baord the Vehicle
	 * @param worker
	 * @param v
	 */
    private boolean boardVehicle(Worker worker, Vehicle v) {
		Walk walk = null;
		LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(v);
		if (worker instanceof Person p) {
			walk = Walk.createWalkingTask(p, adjustedLoc, 0, v);
		}

		else if (worker instanceof Robot r) {
			walk = Walk.createWalkingTask(r, adjustedLoc, v);
		}

		// Walk to board
		if (walk != null) {
			return assignTask(worker, walk);
		}
		
		// No route to board
		logger.warning(worker, "Cannot walk to vehicle " + v.getName() + " for board");
		return false; 
	}

    /**
     * What resources are needed for this travel. This includes the spares needed for the Vehicle.
	 * @param manifet Capture the parts needed
	 * @param addOptionals Any optional spares
     */
    @Override
    void getRequiredResources(MissionManifest manifest, boolean addOptionals) {
		MissionVehicleProject mvp = (MissionVehicleProject) getMission();
		
		// Get the estimated duration
		double durationMSols = mvp.getEstimateTravelTime(mvp.getDistanceProposed());
		double numberAccidents = durationMSols * OperateVehicle.BASE_ACCIDENT_CHANCE;
		double numberMalfunctions = numberAccidents * MalfunctionManager.AVERAGE_NUM_MALFUNCTION;

		Map<Integer, Double> parts = mvp.getVehicle().getMalfunctionManager().getRepairPartProbabilities();
		for (Map.Entry<Integer, Double> entry : parts.entrySet()) {
			Integer id = entry.getKey();
			double value = entry.getValue();
			double freq = value * numberMalfunctions * MalfunctionManager.PARTS_NUMBER_MODIFIER;
			int number = (int) Math.round(freq);
			if (number > 0) {
				manifest.addItem(id, number, false);
			}
		}
	}

	@Override
	public String toString() {
		return "Mission " + getMission().getName() + " board  " + getVehicle().getName();
	}
}
