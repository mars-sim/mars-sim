/*
 * Mars Simulation Project
 * Sleep.java
 * @date 2023-08-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.ActivitySpot;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.core.structure.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Sleep class is a task for sleeping. The duration of the task is by
 * default chosen randomly, between 250 - 330 millisols. Note: Sleeping reduces
 * fatigue and stress.
 */
public class Sleep extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Sleep.class.getName());

	/** Simple Task name */
	public static final String SIMPLE_NAME = Sleep.class.getSimpleName();
		
    private static final int MAX_SUPPRESSION = 100;

	/** Task name */
	public static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

	/** Task phases for person. */
	private static final TaskPhase SLEEPING = new TaskPhase(Msg.getString("Task.phase.sleeping")); //$NON-NLS-1$


	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -2.2D;
	/** The base alarm time (millisols) at 0 degrees longitude. */
	private static final double BASE_ALARM_TIME = 300D;
	private static final double TIME_FACTOR = 1.7; // NOTE: should vary this factor by person
	private static final double RESIDUAL_MODIFIER = .005;

	
	/**
	 * Constructor 1.
	 *
	 * @param person the person to perform the task
	 */
	public Sleep(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER,
				(50 + RandomUtil.getRandomDouble(-5, 5)));

		if (person.isOutside()) {
			logger.log(person, Level.WARNING, 1000, "Not supposed to be falling asleep outside.");

			endTask();
		}

		else {
			// Initialize phase
			addPhase(SLEEPING);
			setPhase(SLEEPING);

			// Adjust the duration so the person does not oversleep
			double alarmTime = getAlarmTime();
			double maxSleep = alarmTime - getMarsTime().getMillisol();
			if (maxSleep < 0D) {
				// Roll over midnight
				maxSleep += 1000D;
			}

			// Is schedule sleep longer than allowed ?
			if (maxSleep < getDuration()) {
				logger.info(person, 20_000, "Sleep adjusted for shift starts at " + (int)alarmTime + ". Duration: " + (int)maxSleep);
				setDuration(maxSleep);
			}

			walkToDestination();

			
			// Finally assign essentials for sleeping
			person.wearGarment(((EquipmentOwner)person.getContainerUnit()));
			person.assignThermalBottle();
		}
	}

	/**
	 * Constructor 2.
	 *
	 * @param person the person to perform the task
	 * @param alarmTime
	 */
	public Sleep(Person person, int duration) {
		super(NAME, person, false, false, STRESS_MODIFIER, duration);

		if (person.isOutside()) {
			logger.log(person, Level.WARNING, 1000, "Not supposed to be falling asleep outside.");

			endTask();
		}

		else {
			// Initialize phase
			addPhase(SLEEPING);
			setPhase(SLEEPING);

			walkToDestination();
		}
	}
	
	/**
	 * Is this Task interruptable? This Task can not be interrupted.
	 * 
	 * @return Returns false by default
	 */
	@Override
	public boolean isInterruptable() {
        return false;
    }

	/**
	 * Refers the person to sleep in a medical bed inside the EVA airlock.
	 *
	 * @return
	 */
	public boolean sleepInEVAMedicalBed() {
		Building currentBuilding = BuildingManager.getBuilding(person);
		if (currentBuilding != null && currentBuilding.hasFunction(FunctionType.EVA)) {
			// Future: need to rework this method to find the two emergency beds in EVA Airlock
			walkToActivitySpotInBuilding(currentBuilding, FunctionType.MEDICAL_CARE, true);
		}
		return false;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null)
			throw new IllegalArgumentException("Task phase is null");
		else if (SLEEPING.equals(getPhase()))
			return sleepingPhase(time);
		else
			return time;
	}


	/**
	 * Performs the sleeping phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double sleepingPhase(double time) {
		
		if (isDone() || getTimeLeft() <= 0) {
        	// this task has ended
			endTask();
			return time;
		}

		PhysicalCondition pc = person.getPhysicalCondition();
		
		CircadianClock circadian = person.getCircadianClock();

		pc.recoverFromSoreness(time);
		
		pc.relaxMuscle(time);

		double fractionOfRest = time * TIME_FACTOR;

		double f = pc.getFatigue();

		double residualFatigue = f * RESIDUAL_MODIFIER;
		// (1) Use the residualFatigue to speed up the recuperation for higher fatigue cases
		// (2) Realistically speaking, the first hour of sleep restore more strength than the
		//     the last hour.
		// (3) For someone who is deprived of sleep for 3 sols or 3000 msols, it should still
		//     take 8 hours of sleep to regain most of the strength, not 24 hours.
		// (4) The lost hours of sleep is already lost and there's no need to rest on a per
		//     msol basis, namely, exchanging 1 msol of fatigue per msol of sleep.

		pc.reduceFatigue(fractionOfRest + residualFatigue);

		circadian.setAwake(false);
		// Change hormones
		circadian.setRested(time);
		// Record the sleep time [in millisols]
		circadian.recordSleep(time);

		if (person.isOnDuty()) {
			// Reduce the probability if it's not the right time to sleep
			refreshSleepHabit(person, circadian);
		}

		// Check if fatigue is zero
		if (pc.getFatigue() <= 0) {
			logger.log(person, Level.FINE, 0, "Totally refreshed from a good sleep.");
			endTask();
		}

		return 0;
	}

	/**
	 * Sets an alarm for a duration in the future.
	 * 
	 * @param untilAlarm Time until the alarm will go off. 
	 */
	public void setAlarm(int untilAlarm) {
		if (getTimeLeft() > untilAlarm) {
			double newDuration = getTimeCompleted() + untilAlarm;

			logger.info(person,  "Alarm clock set for " + untilAlarm);
			setDuration(newDuration);
		}
	}
	
	/**
	 * Walks to a destination.
	 */
	private void walkToDestination() {
		
		// If person is in rover, walk to passenger activity spot.
		if (person.getVehicle() instanceof Rover rover) {
			
			walkToPassengerActivitySpotInRover(rover, true);
		}

		// If person is in a settlement, try to find a living accommodation building.
		else if (person.isInSettlement()) {
			// Double the sleep duration
			setDuration(getDuration() * 2);

			Settlement s = person.getSettlement();
			// Home settlement and bed assigned
			if (person.getAssociatedSettlement().equals(s) && person.hasBed()) {
				
				if (person.getBuildingLocation().getZone() == person.getBed().getOwner().getZone()) {
					// if the person is in the same zone as the building he's in
					walkToBed(person, effortDriven);
					return;
				}
			}

			AllocatedSpot tempBed = findTempBed(s);

			if (tempBed != null) {
				createWalkingSubtask(tempBed.getOwner(), tempBed.getAllocated().getPos(), effortDriven);
			}
		}
	}

	/**
	 * Finds a temporary bed.
	 * 
	 * @param s
	 * @return
	 */
	private AllocatedSpot findTempBed(Settlement s) {
		// Find a bed, if at home settlement attempt to make it permanent
		AllocatedSpot tempBed = LivingAccommodation.allocateBed(s, person,
						s.equals(person.getAssociatedSettlement()));
		if (tempBed == null) {
			tempBed = findSleepRoughLocation(s, person);
			if (tempBed == null) {
				logger.severe(person, "Nowhere to sleep, staying awake.");
				endTask();
				return null;
			}
			logger.warning(person, "No bed can be found; sleeping rough at "
									+ tempBed.getSpotDescription());
		}
		return tempBed;
	}
	
	/**
	 * Finds an empty activity spot in a building that supports life.
	 * 
	 * @param s
	 * @param p
	 * @return
	 */
	private AllocatedSpot findSleepRoughLocation(Settlement s, Person p) {
		var buildMgr = s.getBuildingManager();
		// Find a building in the same zone as the person
		// Avoid sleeping inside EVA Airlock
		for (Building b: buildMgr.getSameZoneBuildingsF1NoF2(p, 
				FunctionType.LIFE_SUPPORT, FunctionType.EVA)) {
			for (Function f : b.getFunctions()) {
				for (ActivitySpot as : f.getActivitySpots()) {
					if (as.isEmpty()) {
						// Claim this activity spot
						boolean canClaim = f.claimActivitySpot(as.getPos(), worker);					
						if (canClaim) {
							return worker.getActivitySpot();
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Clears down the task. 
	 * Note: if worker is a robot then send them to report to duty.
	 */
	@Override
	protected void clearDown() {
		if (person != null) {
			if (person.getPhysicalCondition().getFatigue() > 0) {
				logger.fine(person, "Still fatigued after Sleep " + (int)person.getPhysicalCondition().getFatigue());
			}

			person.setActivitySpot(null);

	    	// Update sleep times once ending
	    	CircadianClock circadian = person.getCircadianClock();
			circadian.setNumSleep(circadian.getNumSleep() + 1);
			circadian.updateSleepCycle((int) getMarsTime().getMillisol(), true);
			circadian.setAwake(true);
		} 
	}

	/**
	 * Gets the wake-up alarm time, based on a person's shift.
	 *
	 * @return alarm time in millisols.
	 */
	private double getAlarmTime() {
		double time = 0;

		if (person != null) {
			ShiftSlot ss = person.getShiftSlot();
			if (ss.getStatus() == WorkStatus.ON_CALL) {
				// if a person is on a mission outside, assume the day begins with
				// the sun rises at ~250 milisols at 0 longitude
				double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
				time = BASE_ALARM_TIME - timeDiff;
			}
			else {
				// Set to 30 millisols prior to the beginning of the duty shift hour
				time = ss.getShift().getStart() - 30.0;
			}

			if (time < 0D) {
				time += 1000D;
			}
		}
		return time;
	}

	/**
     * Refreshes a person's sleep habit based on his/her latest work shift
     *
     * @param person
     */
    public void refreshSleepHabit(Person person, CircadianClock circadian) {
    	int now = getMarsTime().getMillisolInt();

		// if a person is on shift right now
		if (person.isOnDuty()) {

			int habit = circadian.getSuppressHabit();
			int spaceOut = circadian.getSpaceOut();
			// limit adjustment to 10 times and space it out to at least 50 millisols apart
			if (spaceOut < now && habit < MAX_SUPPRESSION) {
				// Discourage the person from forming the sleep habit at this time
				person.updateSleepCycle(now, false);

				int rand = RandomUtil.getRandomInt(2);
				if (rand == 2) {
					circadian.setSuppressHabit(habit+1);
					spaceOut = now + 20;
					if (spaceOut > 1000) {
						spaceOut = spaceOut - 1000;
					}
					circadian.setSpaceOut(spaceOut);
				}
			}
		}
    }
}
