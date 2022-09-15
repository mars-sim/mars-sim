/*
 * Mars Simulation Project
 * Sleep.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Sleep class is a task for sleeping. The duration of the task is by
 * default chosen randomly, between 250 - 330 millisols. Note: Sleeping reduces
 * fatigue and stress.
 */
public class Sleep extends Task implements Serializable {

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
	/** Task name for robot */
	private static final String SLEEP_MODE = Msg.getString("Task.description.sleepMode"); //$NON-NLS-1$

	/** Task phases for person. */
	private static final TaskPhase SLEEPING = new TaskPhase(Msg.getString("Task.phase.sleeping")); //$NON-NLS-1$

	/** Task phases for robot. */
	private static final TaskPhase SLEEPING_MODE = new TaskPhase(Msg.getString("Task.phase.sleepMode")); //$NON-NLS-1$

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
	 * Constructor.
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
			double maxSleep = alarmTime - marsClock.getMillisol();
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

	public Sleep(Robot robot) {
		super(SLEEP_MODE, robot, false, false, STRESS_MODIFIER, 10D);
		setDescription(SLEEP_MODE);
		
		// Initialize phase
		addPhase(SLEEPING_MODE);
		setPhase(SLEEPING_MODE);
	}

	/**
	 * Refers the person to sleep in a bed inside the EVA airlock
	 *
	 * @return
	 */
	public boolean sleepInEVABed() {
		Building currentBuilding = BuildingManager.getBuilding(person);
		if (currentBuilding != null && currentBuilding.hasFunction(FunctionType.EVA)) {
//			return walkToEVABed(currentBuilding, person, true);
			// Future: need to rework this method to find the two emergency beds in EVA Airlock
		}
		return false;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null)
			throw new IllegalArgumentException("Task phase is null");
		else if (SLEEPING.equals(getPhase())
				|| SLEEPING_MODE.equals(getPhase()))
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
			clearTask();
			return time;
		}
		
		if (person != null) {

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

			if (person.getTaskSchedule().isShiftHour(marsClock.getMillisolInt())) {
				// Reduce the probability if it's not the right time to sleep
				refreshSleepHabit(person, circadian);
			}

			// Check if fatigue is zero
			if (pc.getFatigue() <= 0) {
				logger.log(person, Level.FINE, 0, "Totally refreshed from a good sleep.");
				endTask();
			}
		}

		else {
			RoboticStation station = null;

			boolean toCharge = false;
			double level = robot.getSystemCondition().getBatteryState();
			
			// if power is below a certain threshold, stay to get a recharge
			if (robot.getSystemCondition().isLowPower()) {
				toCharge = true;
			}
			
			else if (!robot.getSystemCondition().isBatteryAbove(70)) {
				double rand = RandomUtil.getRandomDouble(level);
				if (rand < robot.getSystemCondition().getLowPowerPercent()/100.0)
					toCharge = true;
			}
			
			boolean canWalk = false;
			if (toCharge) {
				// Switch to charging
    			robot.getSystemCondition().setCharging(true);
    			
				station = robot.getStation();
				if (station == null) {
//					BuildingManager.addRobotToRoboticStation(robot);
					canWalk = walkToRoboticStation(robot, false);
					logger.info(robot, 10_000L, "canWalk: " + canWalk + ".");
				}
				
				if (canWalk) {	
					logger.info(robot, 10_000L, "Walking to " + station + ".");
					station = robot.getStation();
				}

				if (station != null) {
					if (station.getSleepers() < station.getSlots()) {
						station.addSleeper();
					}
					
	    			double hrs = time * MarsClock.HOURS_PER_MILLISOL;
	
	    			double energy = robot.getSystemCondition().deliverEnergy(RoboticStation.CHARGE_RATE * hrs);
	    			
	    			// Record the power spent at the robotic station
	    			station.setPowerLoad(energy/hrs);
	
					// Lengthen the duration for charging the battery
					setDuration(getDuration() + 1.5 * (1 - level));
					
//					logger.info(robot, 10_000L, "Dumping " + Math.round(energy * 1000.0)/1000.0 + " kWh to the battery. "
//							+ "New Duration: " + getDuration());
				}
			}
			else {
				// Disable charging
    			robot.getSystemCondition().setCharging(false);
    			endTask();
			}
		}

		return 0;
	}

	/**
	 * Clears the current task. Disables charging. 
	 */
	public void clearTask() {
		
		// Disable charging so that it can potentially 
		// be doing other tasks while consuming energy
		robot.getSystemCondition().setCharging(false);
		
		super.endTask();
	}
	
	
	/**
	 * Walk to a destination
	 */
	private void walkToDestination() {
		
		if (!arrived) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover) {
//				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
	//			logger.info(person + " will sleep at " + person.getVehicle());
			}

			// If person is in a settlement, try to find a living accommodations building.
			if (person.isInSettlement()) {
				// Double the sleep duration
				setDuration(getDuration() * 2);

				// Note: A bed can be either unmarked(U) or marked(M); and either empty(E) or occupied(O).
				// 4 possibilities : ME, MO, UE, or UO

				Settlement s1 = person.getSettlement();
				Settlement s0 = person.getAssociatedSettlement();

				if (s1 != null && !s1.equals(s0)) {
					// This person is a trader, a tourist, or a guest to this settlement

	//				logger.info(person + " (from " + person.getAssociatedSettlement() + ") is in " + person.getSettlement());

					//////////////////// Case 0 /////////////////////////
					// Case 0 : if a person is in the astronomy observatory, no need of looking for a bed
					if (person.getBuildingLocation().hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
						|| person.isAdjacentBuilding(FunctionType.ASTRONOMICAL_OBSERVATION)) {
						return;
					}
					
					//////////////////// Case 1 - 3 /////////////////////////

					Building q0 = getBestAvailableQuarters(person, true);

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

						q0 = getBestAvailableQuarters(person, false);

						if (q0 != null) {
							// Case 2 : marked, empty (ME) bed(s)

	//						accommodations = q0.getLivingAccommodations();
							// NOTE: need to figure out first how to deregister a trader/tourist once he departs
	//						accommodations.registerSleeper(person, true);

							walkToActivitySpotInBuilding(q0, FunctionType.LIVING_ACCOMMODATIONS, true);
	//						walkToAnUnoccupiedBed(q0, true);
						}
						else {
							// Case 3 :  NO empty bed(s)

							walkToRandomLocation(true);
							// NOTE: should allow him/her to sleep in gym or anywhere based on his/her usual
							// preferences
						}
					}
				}

				else {
					//////////////////// Case 4 - ? /////////////////////////

					// He/she is an inhabitant in this settlement
	//				logger.info(person + " (from " + person.getAssociatedSettlement() + ") is also in " + person.getSettlement());

					// Check if a person has a designated quarters and a marked bed
					Building q1 = person.getQuarters();

					if (q1 != null) {
						// This person has his quarters and have a designated bed
						// logger.fine(person + " does have a designated bed at " + pq.getNickName());

						// check if this bed is currently empty or occupied (either ED or OD)
						LocalPosition bed = person.getBed();
						double x = bed.getX();
						double y = bed.getY();

						// Concert the coordinate back
						LocalPosition spot = new LocalPosition(x - q1.getPosition().getX(), y - q1.getPosition().getY());

						boolean empty = q1.getLivingAccommodations().isActivitySpotEmpty(spot);

						if (empty) {
							// the BEST case for an inhabitant
							// Case 4: marked and empty (ME)

							// addSubTask(new WalkSettlementInterior(person, quarters, bed.getX(),
							// bed.getY()));

							walkToBed(q1, person, true);
						}

						else { // unfortunately his/her marked bed is not empty

							q1 = getBestAvailableQuarters(person, true);

							if (q1 != null)
								// Case 5: unmarked empty (UE)

								walkToActivitySpotInBuilding(q1, FunctionType.LIVING_ACCOMMODATIONS, true);
	//							walkToAnUnoccupiedBed(q1, true);

							else { // no unmarked bed

								q1 = getBestAvailableQuarters(person, false);

								if (q1 != null) {
									// Case 6: this marked bed is currently empty (ME)

									walkToActivitySpotInBuilding(q1, FunctionType.LIVING_ACCOMMODATIONS, true);
	//								walkToAnUnoccupiedBed(q1, true);
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

					else {
						// this inhabitant has never registered a bed and have no designated quarter
						// logger.fine(person + " has never been designated a bed so far");

						q1 = getBestAvailableQuarters(person, true);

						if (q1 == null) {
							walkToRandomLocation(true);
							arrived = true;
							return;
						}

						LivingAccommodations la = q1.getLivingAccommodations();
						if (la == null) {
							logger.severe(person, "la is null.");
							return;
						}

						LocalPosition bed = la.registerSleeper(person, false);
						if (bed == null)
							logger.severe(person, "bed is null.");

						if (q1 != null && bed != null) {
//						if (q1 != null && q1.getLivingAccommodations().registerSleeper(person, false) != null) {
							// Case 8: unmarked, empty (UE) bed

							walkToBed(q1, person, true);

						}

						else { // no unmarked bed

							q1 = getBestAvailableQuarters(person, false);

							if (q1 != null)
								// Case 9: marked, empty (ME)

								walkToActivitySpotInBuilding(q1, FunctionType.LIVING_ACCOMMODATIONS, true);
	//							walkToAnUnoccupiedBed(q1, true);
							else
								// Case 10: No beds available, go to any activity spots

								walkToRandomLocation(true);

							// NOTE: should allow him/her to sleep in gym or anywhere based on his/her usual
							// preferences
						}
					}
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
			circadian.updateSleepCycle((int) marsClock.getMillisol(), true);
			circadian.setAwake(true);

		} else if (robot != null) {
			// Remove robot from stations so other robots can use it.
			RoboticStation station = robot.getStation();
			if (station != null && station.getSleepers() > 0) {
				station.removeSleeper();
				// NOTE: assess how well this work
			}
//    		logger.info(robot.getNickName() + " was done sleeping and waking up.");
			walkToAssignedDutyLocation(robot, true);
		}
	}

	/**
	 * Gets the best available living accommodations building that the person can
	 * use. Returns null if no living accommodations building is currently
	 * available.
	 *
	 * @param person   the person
	 * @param unmarked does the person wants an unmarked(aka undesignated) bed or
	 *                 not.
	 * @return a building with available bed(s)
	 */
	public static Building getBestAvailableQuarters(Person person, boolean unmarked) {
		Building b = null; //person.getBuildingLocation();

		if (person.isInSettlement()) {
//			// If this person is located in the observatory
//			if (b.getBuildingType().equalsIgnoreCase(Building.ASTRONOMY_OBSERVATORY))
//				return b;

			List<Building> quartersBuildings = person.getSettlement().getBuildingManager()
					.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
			quartersBuildings = BuildingManager.getNonMalfunctioningBuildings(quartersBuildings);
			quartersBuildings = getQuartersWithEmptyBeds(quartersBuildings, unmarked);
			if (quartersBuildings.size() > 0) {
				quartersBuildings = BuildingManager.getLeastCrowdedBuildings(quartersBuildings);
			}
			if (quartersBuildings.size() == 1) {
				return quartersBuildings.get(0);
			}
			else if (quartersBuildings.size() > 1) {
				Map<Building, Double> quartersBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						quartersBuildings);
				b = RandomUtil.getWeightedRandomObject(quartersBuildingProbs);
			}
		}

		return b;
	}

	public static Building getAvailableRoboticStationBuilding(Robot robot) {
		if (robot.isInSettlement()) {
			BuildingManager manager = robot.getSettlement().getBuildingManager();
			List<Building> buildings0 = manager.getBuildings(FunctionType.ROBOTIC_STATION);
			List<Building> buildings1 = BuildingManager.getNonMalfunctioningBuildings(buildings0);
			List<Building> buildings2 = getRoboticStationsWithEmptySlots(buildings1);
			List<Building> buildings3 = null;
			if (!buildings2.isEmpty()) {
				// robot is not as inclined to move around
				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings2);
				if (buildings3 == null) {
					buildings3 = buildings2;
				}
			}
			else if (!buildings1.isEmpty()) {
				// robot is not as inclined to move around
				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings1);
				if (buildings3 == null) {
					buildings3 = buildings1;
				}
			}
			else if (!buildings0.isEmpty()) {
				// robot is not as inclined to move around
				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings0);
				if (buildings3 == null) {
					buildings3 = buildings0;
				}
			}
			
			if (buildings3 == null)
				return null;
			
			int size = buildings3.size();

			int selected = 0;
			if (size == 1) {
				 return buildings3.get(0);
			}
			else if (size > 1) {
				selected = RandomUtil.getRandomInt(size - 1);
				return buildings3.get(selected);
			}
		}

		return null;
	}

	/**
	 * Gets living accommodations with empty beds from a list of buildings with the
	 * living accommodations function.
	 *
	 * @param buildingList list of buildings with the living accommodations
	 *                     function.
	 * @param unmarked     does the person wants an unmarked(aka undesignated) bed
	 *                     or not.
	 * @return list of buildings with empty beds.
	 */
	private static List<Building> getQuartersWithEmptyBeds(List<Building> buildingList, boolean unmarked) {
		List<Building> result = new ArrayList<Building>();

		for (Building building : buildingList) {
			LivingAccommodations quarters = building.getLivingAccommodations();
			boolean notFull = quarters.getNumEmptyActivitySpots() > 0;//quarters.getRegisteredSleepers() < quarters.getBedCap();
			// Check if an unmarked bed is wanted
			if (unmarked) {
				if (quarters.hasAnUnmarkedBed() && notFull) {
					result.add(building);
				}
			}
			else if (notFull) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a list of robotic stations with empty spots
	 *
	 * @param buildingList
	 * @return
	 */
	private static List<Building> getRoboticStationsWithEmptySlots(List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			RoboticStation station = building.getRoboticStation();
			if (station.getSleepers() < station.getSlots()) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the wake-up alarm time, based on a person's shift
	 *
	 * @return alarm time in millisols.
	 */
	private double getAlarmTime() {
		double timeDiff = 0;
		double time = 0;

		if (person != null) {
			ShiftType shiftType = person.getTaskSchedule().getShiftType();
			// Set to 30 millisols prior to the beginning of the duty shift hour
			if (shiftType == ShiftType.A)
				time = TaskSchedule.A_START - 30.0; // 220
			else if (shiftType == ShiftType.B)
				time = TaskSchedule.B_START - 30.0; // 721;
			else if (shiftType == ShiftType.X)
				time = TaskSchedule.X_START - 30.0; // 970;
			else if (shiftType == ShiftType.Y)
				time = TaskSchedule.Y_START - 30.0; // 304;
			else if (shiftType == ShiftType.Z)
				time = TaskSchedule.Z_START - 30.0; // 637;
			else if (shiftType == ShiftType.ON_CALL) {
				// if a person is on a mission outside, assume the day begins with
				// the sun rises at ~250 milisols at 0 longitude
				timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
				time = BASE_ALARM_TIME - timeDiff;

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
    	int now = marsClock.getMillisolInt();

        // if a person is NOT on-call
        if (person.getTaskSchedule().getShiftType() != ShiftType.ON_CALL) {
	        // if a person is on shift right now
           	if (person.getTaskSchedule().isShiftHour(now)) {

           		int habit = circadian.getSuppressHabit();
           		int spaceOut = circadian.getSpaceOut();
	           	// limit adjustment to 10 times and space it out to at least 50 millisols apart
           		if (spaceOut < now && habit < MAX_SUPPRESSION) {
	           		// Discourage the person from forming the sleep habit at this time
		  	  		person.updateSleepCycle(now, false);
			    	//System.out.println("spaceOut : " + spaceOut + "   now : " + now + "  suppressHabit : " + habit);

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
}
