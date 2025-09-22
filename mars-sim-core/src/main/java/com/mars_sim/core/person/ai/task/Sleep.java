/*
 * Mars Simulation Project
 * Sleep.java
 * @date 2025-08-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.Rover;

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

	/** Task name */
	public static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

	/** Task phases for person. */
	private static final TaskPhase SLEEPING = new TaskPhase(Msg.getString("Task.phase.sleeping")); //$NON-NLS-1$


	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -2D;
	/** The base alarm time (millisols) at 0 degrees longitude. */
	private static final double BASE_ALARM_TIME = 300D;
	private static final double TIME_FACTOR = 1.2; // NOTE: should vary this factor by person
	private static final double RESIDUAL_MODIFIER = .004;
	private static final double SLEEP_PERIOD = 150;

	private int numREMCycles;
	private int cycleLength;
	/**
	 * Constructor 1. This will require a person to walk to a bed first.
	 * Note: choose constructor 2 if walking is not required.
	 *
	 * @param person
	 */
	public Sleep(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, RandomUtil.getRandomInt(-20, 20) + SLEEP_PERIOD);

		if (person.isOutside()) {
			logger.log(person, Level.WARNING, 1000, "Not supposed to be falling asleep outside.");

			endTask();
		}

		else {
			// Initialize phase
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
				logger.info(person, 0, "Sleep adjusted for shift starts at " + (int)alarmTime + ". Duration: " + (int)maxSleep);
				setDuration(maxSleep);
			}

			walkToDestination();
			
			UnitHolder uh = person.getContainerUnit();
			if (uh instanceof EquipmentOwner eo) {
				// Finally assign essentials for sleeping
				person.wearGarment(eo);
				person.assignThermalBottle();
			}
			
			// Note: each REM cycle lasts about 90 to 120 mins or 60 to 80 millisols
			numREMCycles = (int) (getDuration() / RandomUtil.getRandomInt(60, 80));
			
			cycleLength = (int)Math.round(getDuration() / numREMCycles);
		}
	}

	/**
	 * Constructor 2. This it for those who were brought in and will not be required to walk to a bed.
	 *
	 * @param person
	 * @param duration pre-defined sleep time
	 */
	public Sleep(Person person, int duration) {
		super(NAME, person, false, false, STRESS_MODIFIER, duration);

		if (person.isOutside()) {
			logger.log(person, Level.WARNING, 0, "Not supposed to be falling asleep outside.");

			endTask();
		}

		else {
			// Initialize phase
			setPhase(SLEEPING);
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
	 * DO NOT DELETE. Will revisit how to safely put a makeshift bed in the EVA Airlock building.
	 * 
	 * Refers the person to sleep in a medical bed inside the EVA airlock.
	 * 
	 * @return
	 */
	public boolean sleepInEVAMedicalBed() {

		Building currentBuilding = BuildingManager.getBuilding(person);
		if (currentBuilding != null && currentBuilding.hasFunction(FunctionType.EVA)) {
			// Future: need to rework this method to find the two emergency beds in EVA Airlock
			// This is not the right way to find the medical bed walkToActivitySpotInBuilding(currentBuilding, FunctionType.EVA, true);
			// Model after MedicalStation's bedRegistry for the EVA bed
			// May add a helper method in BuildingManager such as BuildingManager::addPatientToMedicalBed.
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
		// Assume sleeping improve the muscular soreness 
		pc.reduceMuscleSoreness(time/2);
		// Assume sleeping reduce the muscular health 
		pc.reduceMuscleHealth(time/2);

        pc.reduceStress(time/2); 
        
		double fractionOfRest = time * TIME_FACTOR;

		double f = pc.getFatigue() * time;

		double residualFatigue = 0;
		// (1) Use the residualFatigue to speed up the recuperation for higher fatigue cases
		// (2) Realistically speaking, the first hour of sleep restore more strength than the
		//     the last hour.
		// (3) For someone who is deprived of sleep for 3 sols or 3000 msols, it should still
		//     take 8 hours of sleep to regain most of the strength, not 24 hours.
		// (4) The lost hours of sleep is already lost and there's no need to rest on a per
		//     msol basis, namely, exchanging 1 msol of fatigue per msol of sleep.

		
		// The first REM cycle is usually the shortest, lasting around 10 mins, or 6.85 millisols
		// Assume the rest of the cycles last between 90 to 120 minutes
		
		
		if (getTimeCompleted() <= 6.85) {
			// first REM cycle
			residualFatigue = f * RESIDUAL_MODIFIER;
		}
		else if (getTimeCompleted() <= 6.85 + cycleLength) {
			residualFatigue = f * RESIDUAL_MODIFIER * 2;
		}
		else if (getTimeCompleted() <= 6.85 + 2 * cycleLength) {
			residualFatigue = f * RESIDUAL_MODIFIER * 3;
		}
		else if (getTimeCompleted() <= 6.85 + 3 * cycleLength) {
			residualFatigue = f * RESIDUAL_MODIFIER * 2;
		}
		else if (getTimeCompleted() <= 6.85 + 4 * cycleLength) {
			residualFatigue = f * RESIDUAL_MODIFIER;
		}
		
		pc.reduceFatigue(fractionOfRest + residualFatigue);

		circadian.setAwake(false);
		// Change hormones
		circadian.setRested(time);
		// Record the sleep time [in millisols]
		circadian.recordSleep(time);

		if (person.isOnDuty()) {
	    	int now = getMarsTime().getMillisolInt();
			// Reduce the probability if it's not the right time to sleep
	    	circadian.adjustSleepHabit(now);
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
				// if the person is in the same zone as the building he's in
				walkToBed(person, effortDriven);
					
				return;
			}
			
			// Note 1: Consider those in the astronomy observatory. They should be able to create a 
			// makeshift bed
			// Note 2: findABed will internally call findSleepRoughLocation() to turn an life support activity spot
			// into a temporary bed spot
			AllocatedSpot tempBed = findABed(s, person);
			
			if (tempBed != null) {
				boolean canWalk = createWalkingSubtask(tempBed.getOwner(), tempBed.getAllocated().getPos(), effortDriven, false);
				if (!canWalk) {
					logger.severe(person, 10_000, "Unable to walk to his/her own bed.");
					endTask();
					return;
				}
			}
			else {
				logger.severe(person, 10_000, "Unable to find a bed.");
				endTask();
				return;
			}
		}
	}

	/**
	 * Finds a bed (permanent if possible).
	 * 
	 * @param s
	 * @return
	 */
	public static AllocatedSpot findABed(Settlement s, Person person) {
		// Find a bed, if at home settlement attempt to make it permanent
		AllocatedSpot tempBed = LivingAccommodation.allocateBed(s, person,
						s.equals(person.getAssociatedSettlement()));
		if (tempBed == null) {
			tempBed = findSleepRoughLocation(s, person);
			if (tempBed == null) {
				logger.info(person, "Found no spots to sleep. Staying awake.");
				return tempBed;
			}
			else {
				logger.info(person, "No permanent bed found. Temporarily sleeping at '"
									+ tempBed.getSpotDescription() + "'.");
			}
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
	public static AllocatedSpot findSleepRoughLocation(Settlement s, Person p) {
		var manager = s.getBuildingManager();
		// Find a building in the same zone as the person
		// Avoid sleeping inside EVA Airlock
		for (Building b: manager.getSameZoneBuildingsF1NoF2(p, 
				FunctionType.LIFE_SUPPORT, FunctionType.EVA)) {
			for (Function f : b.getFunctions()) {
				for (ActivitySpot as : f.getActivitySpots()) {
					if (as.isEmpty()) {
						// Claim this activity spot
						boolean canClaim = f.claimActivitySpot(as.getPos(), p);					
						if (canClaim) {
							return p.getActivitySpot();
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
}
