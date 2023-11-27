/*
 * Mars Simulation Project
 * Sleep.java
 * @date 2023-08-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LivingAccommodations;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.mapdata.location.LocalPosition;
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

	// Data members
	/**
	 * 0 = none
	 * 1 = regular bed
	 * 2 = guest bed
	 * 3 = vehicle seat
	 */
	private int typeOfBed = 0;
	
	/**
	 * Constructor 1.
	 *
	 * @param person the person to perform the task
	 */
	public Sleep(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER,
				(50 + RandomUtil.getRandomDouble(5) - RandomUtil.getRandomDouble(5)));

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
		}
	}
	
	/**
	 * Is this Task interruptable? This Task can not be interrupted.
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
//			return walkToEVABed(currentBuilding, person, true);
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
			if (typeOfBed == 2) {
				releaseGuestBed();
			}
        	// this task has ended
			endTask();
			return time;
		}

		if (person.isInSettlement()) {
			if (typeOfBed == 0) {
				// Walk to a location
				walkToDestination();
			}
		}

//			// Check if a person's subtask is not the Sleep task itself
//			if (isNotSubTask())
//				// Clear the sub task to avoid getting stuck before walking to a bed or a destination
//				endSubTask();

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
			if (typeOfBed == 2) {
				releaseGuestBed();
			}
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
	 * Looks for a guest bed.
	 */
	private void lookForGuestBed() {
		logger.info(person + " in need of getting a guest bed.");	
		
		// Case 0 : A guest bed - The BEST case for a guest
		boolean hasGuestBed = walkToGuestBed(person, true);
		
		if (hasGuestBed) {
			
			typeOfBed = 2;
			
			return;
		}
			
		//////////////////// Case 1 - 3 /////////////////////////

		Building q0 = BuildingManager.getBestAvailableQuarters(person, true, false);

		if (q0 != null) {
			// Case 1 : have unmarked, empty (UE) bed

			walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);

			typeOfBed = 1;
		}

		else { 
			// Since there is no unmarked/guest bed,
			// look for a marked bed

			q0 = BuildingManager.getBestAvailableQuarters(person, false, false);

			if (q0 != null) {
				// Case 2 : marked, empty (ME) bed(s)

				// FUTURE: may disable the use of a marked bed 
				// Find a bed whose owner is on a mission
				
				walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
				
				typeOfBed = 1;
			}
			else {
				// Case 3 : No empty bed(s)

				walkToRandomLocation(true);
				
				// NOTE: should allow him/her to sleep in gym or a medical bed 
				// or anywhere based on his/her usual preferences
				
				typeOfBed = 0;
			}
		}
	}
	
	/**
	 * Looks for assigned bed.
	 */
	private void lookForAssignedBed(Building building) {
		
		// Case 4: marked and empty (ME)
		if (person.getQuarters() != null) {
			walkToBed(building, person, true);

			typeOfBed = 2;
		}
		else {
			// Since his/her marked bed is being occupied,
			// go look for a guest bed
			lookForGuestBed();
		}
	}
	
	/**
	 * Looks to be assigned a bed.
	 */
	private void lookTobeAssignedABed() {
		
		// Case 7: unmarked, empty (UE) bed
		Building q7 = BuildingManager.getBestAvailableQuarters(person, true, false);
		
		if (q7 != null) {
			// Register this sleeper
			LocalPosition bed = q7.getLivingAccommodations().registerSleeper(person, false);

			if (bed != null) {
				// Case 8: unmarked, empty (UE) bed
				walkToBed(q7, person, true);

				typeOfBed = 2;
			}
		}
		
		else { // no unmarked bed
			
			// Case 9: Get a guest bed
			lookForGuestBed();
		}
	}
	
	/**
	 * Looks for a bed.
	 */
	private void lookForBed() {
		
		//////////////////// Case 4 - 10 /////////////////////////
		// He/she is an inhabitant in this settlement

		// Check if a person has a designated quarters and a marked bed
		Building q4 = person.getQuarters();

		if (q4 != null) {
			// This is the BEST case for an inhabitant
			
			// This person has his quarters and have a designated bed
			lookForAssignedBed(q4);
		}

		else {
			// this inhabitant has never registered a bed and have no designated quarters
			lookTobeAssignedABed();
		}
	}
	
	/**
	 * Walks to a destination.
	 */
	private void walkToDestination() {
		
		if (typeOfBed == 0) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover rover) {
				
				walkToPassengerActivitySpotInRover(rover, true);
				
				typeOfBed = 3;
			}

			// If person is in a settlement, try to find a living accommodations building.
			else if (person.isInSettlement()) {
				// Double the sleep duration
				setDuration(getDuration() * 2);

				//////////////////// Case 0 /////////////////////////
				
				// Case A : if a person is in the astronomy observatory
				Building q0 = person.getBuildingLocation();
				if (q0 != null && q0.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)) {
//					|| BuildingManager.isAdjacentBuilding(FunctionType.ASTRONOMICAL_OBSERVATION, person, building)) {
					// Rest in one of the 2 beds there
					walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
					
					typeOfBed = 2;
					
					return;
				}
				
				// Note: A bed can be either unmarked(U) or marked(M); and either empty(E) or occupied(O).
				// 4 possibilities : ME, MO, UE, or UO

				Settlement s1 = person.getSettlement();
				Settlement s0 = person.getAssociatedSettlement();

				if (!s1.equals(s0)) {
					// If this person is a trader, a tourist, or a guest to this settlement
					lookForGuestBed();
				}

				else {
					// This person is a resident/citizen of this settlement
					lookForBed();
				}
			}
		}
	}

	/**
	 * Releases the guest bed.
	 */
	public void releaseGuestBed() {
		// Deregister this person if using a guest bed
		LivingAccommodations q = person.getQuarters().getLivingAccommodations();
		// Register this person to use this guest bed
		q.deRegisterGuestBed(person.getIdentifier());
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
