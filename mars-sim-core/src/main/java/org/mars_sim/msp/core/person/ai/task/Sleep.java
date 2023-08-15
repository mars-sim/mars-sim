/*
 * Mars Simulation Project
 * Sleep.java
 * @date 2023-08-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.logging.Level;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.vehicle.Rover;

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
	
//	private static final int MAX_FATIGUE = 3000;
	
    private static final int MAX_SUPPRESSION = 100;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

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
	private boolean arrived = false;

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
        	// this task has ended
			endTask();
			return time;
		}

		if (person.isInSettlement()) {
			// Walk to a location
			walkToDestination();
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
	 * Looks for a bed for a guest.
	 */
	private void lookForGuestBed() {
		
		//////////////////// Case 0 /////////////////////////
		// Case 0 : if a person is in the astronomy observatory
		Building q0 = person.getBuildingLocation();
		if (q0 != null && q0.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)) {
//			|| BuildingManager.isAdjacentBuilding(FunctionType.ASTRONOMICAL_OBSERVATION, person, building)) {
			// Rest in one of the 2 beds there
			walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
			
			arrived = true;
			
			return;
		}
		
		//////////////////// Case 1 - 3 /////////////////////////

		q0 = LivingAccommodations.getBestAvailableQuarters(person, true);

		if (q0 != null) {
			// Case 1 : (the BEST case for a guest) unmarked, empty (UE) bed(s)

//					accommodations = q0.getLivingAccommodations();
			// NOTE: need to figure out first how to deregister a trader/tourist once he departs
//					accommodations.registerSleeper(person, true);

			walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
//					walkToAnUnoccupiedBed(q0, true);
			// addSubTask(new WalkSettlementInterior(person, quarters, bed.getX(), bed.getY()));
		}

		else { // no unmarked bed

			q0 = LivingAccommodations.getBestAvailableQuarters(person, false);

			if (q0 != null) {
				// Case 2 : marked, empty (ME) bed(s)

				walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
			}
			else {
				// Case 3 :  NO empty bed(s)

				walkToRandomLocation(true);
				
				// NOTE: should allow him/her to sleep in gym or a medical bed 
				// or anywhere based on his/her usual preferences
			}
		}
		
		arrived = true;
	}
	
	/**
	 * Looks for assigned bed.
	 */
	private void lookForAssignedBed(Building building) {
		
		// Case 4: marked and empty (ME)

		if (!walkToBed(building, person, true)) {
			// If his/her marked bed is not empty
			
			building = LivingAccommodations.getBestAvailableQuarters(person, true);

			if (building != null)
				// Case 5: unmarked empty (UE)

				walkToActivitySpotInBuilding(building, FunctionType.LIVING_ACCOMMODATIONS, true);

			else { // no unmarked bed

				building = LivingAccommodations.getBestAvailableQuarters(person, false);

				if (building != null) {
					// Case 6: this marked bed is currently empty (ME)

					walkToActivitySpotInBuilding(building, FunctionType.LIVING_ACCOMMODATIONS, true);
				}

				else {
					// Case 7: No beds available, go to any activity spots

					walkToRandomLocation(true);

					// NOTE: should allow him/her to sleep in gym or anywhere based on his/her usual
					// preferences
				}
			}
		}
	}
	
	/**
	 * Looks to be assigned a bed.
	 */
	private void lookTobeAssignedABed() {
		Building q7 = LivingAccommodations.getBestAvailableQuarters(person, true);

		if (q7 == null) {
			walkToRandomLocation(true);
			arrived = true;
			return;
		}

		LivingAccommodations la = q7.getLivingAccommodations();
		if (la == null) {
			logger.severe(person, "LivingAccommodations is null.");
			return;
		}

		// Register this sleeper
		LocalPosition bed = la.registerSleeper(person, false);
		if (bed == null)
			logger.severe(person, "bed is null.");

		if (q7 != null && bed != null) {
//		if (q1 != null && q1.getLivingAccommodations().registerSleeper(person, false) != null) {
			// Case 8: unmarked, empty (UE) bed

			walkToBed(q7, person, true);
		}

		else { // no unmarked bed

			q7 = LivingAccommodations.getBestAvailableQuarters(person, false);

			if (q7 != null)
				// Case 9: marked, empty (ME)

				walkToActivitySpotInBuilding(q7, FunctionType.LIVING_ACCOMMODATIONS, true);
//						walkToAnUnoccupiedBed(q1, true);
			else
				// Case 10: No beds available, go to any activity spots

				walkToRandomLocation(true);

			// NOTE: should allow him/her to sleep in gym or anywhere based on his/her usual
			// preferences
		}
	}
	
	/**
	 * Looks for a bed.
	 */
	private void lookForBed() {
		
		//////////////////// Case 4 - 10 /////////////////////////

		// He/she is an inhabitant in this settlement
//				logger.info(person + " (from " + person.getAssociatedSettlement() + ") is also in " + person.getSettlement());

		// Check if a person has a designated quarters and a marked bed
		Building q4 = person.getQuarters();

		if (q4 != null) {
			// The BEST case for an inhabitant
			// This person has his quarters and have a designated bed
			lookForAssignedBed(q4);
		}

		else {
			// this inhabitant has never registered a bed and have no designated quarter
			lookTobeAssignedABed();
		}
	}
	
	/**
	 * Walks to a destination.
	 */
	private void walkToDestination() {
		
		if (!arrived) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover rover) {
				
				walkToPassengerActivitySpotInRover(rover, true);
				
				arrived = true;
				
				return;
			}

			// If person is in a settlement, try to find a living accommodations building.
			if (person.isInSettlement()) {
				// Double the sleep duration
				setDuration(getDuration() * 2);

				// Note: A bed can be either unmarked(U) or marked(M); and either empty(E) or occupied(O).
				// 4 possibilities : ME, MO, UE, or UO

				Settlement s1 = person.getSettlement();
				Settlement s0 = person.getAssociatedSettlement();

				if (!s1.equals(s0)) {
					// If this person is a trader, a tourist, or a guest to this settlement
					lookForGuestBed();
				}

				else {
					
					lookForBed();
				}
			}

			arrived = true;
		}
	}

	/**
	 * If worker is a Robot then send them to report to duty
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
	 * Gets the wake-up alarm time, based on a person's shift
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
