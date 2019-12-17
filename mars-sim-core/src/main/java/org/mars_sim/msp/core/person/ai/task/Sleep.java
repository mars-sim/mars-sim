/**
 * Mars Simulation Project
 * Sleep.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Sleep class is a task for sleeping. The duration of the task is by
 * default chosen randomly, between 250 - 330 millisols. Note: Sleeping reduces
 * fatigue and stress.
 */
public class Sleep extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Sleep.class.getName());
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
	private static final int MAX_FATIGUE = 1500;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase SLEEPING = new TaskPhase(Msg.getString("Task.phase.sleeping")); //$NON-NLS-1$

	/** Task name for robot */
	private static final String SLEEP_MODE = Msg.getString("Task.description.sleepMode"); //$NON-NLS-1$

	/** Task phases for robot. */
	private static final TaskPhase SLEEPING_MODE = new TaskPhase(Msg.getString("Task.phase.sleepMode")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.2D;
	/** The base alarm time (millisols) at 0 degrees longitude. */
	private static final double BASE_ALARM_TIME = 300D;

	// Data members
	/** The previous time (millisols). */
	private double previousTime;
	private double timeFactor = 4; // TODO: should vary this factor by person
	private double totalSleepTime;
	
	private LocalBoundedObject interiorObject;
	private Point2D returnInsideLoc;
	
	/** The living accommodations if any. */
	private LivingAccommodations accommodations;
	private RoboticStation station;
	private CircadianClock circadian;
	private PhysicalCondition pc;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	//
	public Sleep(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 
				(50 + RandomUtil.getRandomDouble(2.5) - RandomUtil.getRandomDouble(2.5)));

		pc = person.getPhysicalCondition();
		circadian = person.getCircadianClock();
		
		if (person.isOutside()) {
			logger.warning(person + " was not supposed to be outside when calling Sleep");
			walkBackInside();
			endTask();
		}
		// If person is in rover, walk to passenger activity spot.
		else if (person.isInVehicle() && person.getVehicle() instanceof Rover) {
			
			if (person.getLocationTag().isInSettlementVicinity()) {
				if (!walkBackInside())
					walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
			}
			else
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
//				logger.info(person + " is in " + person.getVehicle());
			
			previousTime = marsClock.getMillisol();

			// Initialize phase
			addPhase(SLEEPING);
			setPhase(SLEEPING);
			
//			logger.info(person + " will sleep at " + person.getVehicle());
		}

		// If person is in a settlement, try to find a living accommodations building.
		else if (person.isInSettlement()) {
			// Double the sleep duration
			setDuration(getDuration() * 2);
			
			// Organized into 9 branching decisions
			// A bed can be either empty(E) or occupied(O), either unmarked(U) or
			// designated(D).
			// thus a 2x2 matrix with 4 possibilities: EU, ED, OU, OD

			Settlement s1 = person.getSettlement();
			Settlement s0 = person.getAssociatedSettlement();
			// check to see if a person is a trader or on a trading mission
			if (s1 != null && !s1.equals(s0)) {
//				logger.info(person + " (from " + person.getAssociatedSettlement() + ") is in " + person.getSettlement());
				// yes he is a trader/guest (Case 1-3)
				// logger.info(person + " is a guest of a trade mission and will use an
				// unoccupied bed randomly.");
				// find a best empty (EU, ED) bed
				Building q2 = getBestAvailableQuarters(person, false);

				if (q2 != null) {
					// find a best empty, unmarked (EU) bed
					Building q1 = getBestAvailableQuarters(person, true);
					if (q1 != null) {
						// Case 1 : (the BEST case for a guest) the settlement does have one or more
						// empty, unmarked (EU) bed(s)
						accommodations = q1.getLivingAccommodations();
						walkToActivitySpotInBuilding(q1, getLivingFunction(), true);
						// Building startBuilding = BuildingManager.getBuilding(person);
						// logger.fine("Case 1: " + person + " is walking from " + startBuilding + " to
						// use his/her temporary quarters at " + q1);

					} else {
						// Case 2 : the settlement has only empty, designated (ED) bed(s) available
						// Question : will the owner of this bed be coming back soon from duty ?
						// TODO : will split into Case 2a and Case 2b.
						accommodations = q2.getLivingAccommodations();
						walkToActivitySpotInBuilding(q2, getLivingFunction(), true);
						// Building startBuilding = BuildingManager.getBuilding(person);
						// logger.fine("Case 2: " + person + " is walking from " + startBuilding + " to
						// use his/her temporary quarters at " + q2);

					}

					accommodations.registerSleeper(person, true);
					// walkSite = true;

				} else {
					// Case 3 : the settlement has NO empty bed(s). OU and/or OD only
					// logger.info("Case 3: " + person + " couldn't find an empty bed at all. Will
					// find a spot to fall asleep wherever he/she likes.");
					// TODO: should allow him/her to sleep in gym or anywhere based on his/her usual
					// preferences
					// endTask();
					// Just walk to a random location.
					walkToRandomLocation(true);
				}

			} else {
				// He/she is an inhabitant in this settlement
//				logger.info(person + " (from " + person.getAssociatedSettlement() + ") is also in " + person.getSettlement());
				
				// Check if a person has a designated bed
				Building pq = person.getQuarters();

				if (pq != null) {
					// This person has his quarters assigned with a designated bed
					// logger.fine(person + " does have a designated bed at " + pq.getNickName());

					// check if this bed is currently empty or occupied (either ED or OD)
					Point2D bed = person.getBed();
					accommodations = pq.getLivingAccommodations();
					boolean empty = accommodations.isActivitySpotEmpty(bed);

					if (empty) {
						// Case 4: this designated bed is currently empty (ED)
						// Building startBuilding = BuildingManager.getBuilding(person);

						// logger.info("Case 4: " + person + " is walking from " + startBuilding + " to
						// his private quarters at " + pq);
						// addSubTask(new WalkSettlementInterior(person, quarters, bed.getX(),
						// bed.getY()));
						accommodations.registerSleeper(person, false);
						walkToBed(accommodations, person, true); // can cause StackOverflowError from excessive log or
																	// calling ExitAirlock
					} else {
						// Case 5: this designated bed is currently occupied (OD)
						// logger.info("Case 5: " + person + " has a designated bed but is currently
						// occupied. Will find a spot to fall asleep.");
						// TODO: should allow him/her to sleep in gym or anywhere based on his/her usual
						// preferences
						// Just walk to a random location.
						walkToRandomLocation(true);
					}

				} else {
					// this inhabitant has never been assigned a quarter and does not have a
					// designated bed so far
					// logger.fine(person + " has never been designated a bed so far");

					// find an empty (either marked or unmarked) bed
					Building q7 = getBestAvailableQuarters(person, false);

					if (q7 != null) {
						// yes it has empty (either marked or unmarked) bed

						// find an empty unmarked bed
						Building q6 = getBestAvailableQuarters(person, true);

						if (q6 != null) {
							// Case 6: an empty unmarked bed is available for assigning to the person

							// logger.info(q6.getNickName() + " has empty, unmarked bed (ED) that can be
							// assigned to " + person);
							// addSubTask(new WalkSettlementInterior(person, quarters, bed.getX(),
							// bed.getY()));
							// person.setQuarters(q6);
							// Point2D bed = person.getBed();
							accommodations = q6.getLivingAccommodations();
							accommodations.registerSleeper(person, false);
							walkToBed(accommodations, person, true);
							// walkToActivitySpotInBuilding(q7, BuildingFunction.LIVING_ACCOMODATIONS,
							// false);
							// Building startBuilding = BuildingManager.getBuilding(person);
							// logger.info("Case 6: " + person + " is walking from " + startBuilding + " to
							// use his/her new quarters at " + q6);

						} else {
							logger.fine(q7.getNickName() + " has an empty, already designated (ED) bed available for "
									+ person);
							// Case 7: the settlement has only empty, designated (ED) bed(s) available
							// Question : will the owner of this bed be coming back soon from duty ?
							// TODO : will split into Case 2a and Case 2b.
							
//							accommodations = q7.getLivingAccommodations();
//							walkToActivitySpotInBuilding(q7, BuildingFunction.LIVING_ACCOMODATIONS, false); 
//							Building startBuilding = BuildingManager.getBuilding(person);
//							logger.info("Case 7a: " + person + " is walking from " + startBuilding + " to use someone else's quarters at " + q7);
//							accommodations.addSleeper(person, false);
							
							// logger.info("Case 7b: " + person + " will look for a spot to fall asleep.");

							// Walk to random location.
							walkToRandomLocation(true);
						}

					} else {

						// Case 8 : no empty bed at all
						logger.info("Case 8: " + person
								+ " couldn't find an empty bed at all. will look for a spot to fall asleep.");
						// TODO: should allow him/her to sleep in gym or anywhere.
						// Walk to random location.
						walkToRandomLocation(true);
					}
				}
			}

			previousTime = marsClock.getMillisol();

			// Initialize phase
			addPhase(SLEEPING);
			setPhase(SLEEPING);
			
//			logger.info(person + " will sleep at " + person.getSettlement());
		}
	}

	public Sleep(Robot robot) {
		super(SLEEP_MODE, robot, false, false, STRESS_MODIFIER, true, 10D);

		// If robot is in a settlement, try to find a living accommodations building.
		if (robot.isInSettlement()) {

			// TODO: if power is below a certain threshold, go to robotic station for
			// recharge, else stay at the same place

			// If currently in a building with a robotic station, go to a station activity
			// spot.
			// boolean atStation = false;
			Building currentBuilding = BuildingManager.getBuilding(robot);
			if (currentBuilding != null) {
				if (currentBuilding.hasFunction(getRoboticFunction())) {
					RoboticStation currentStation = currentBuilding.getRoboticStation();
					if (currentStation.getSleepers() < currentStation.getSlots()) {
						// atStation = true;
						station = currentStation;
						station.addSleeper();

						// Check if robot is currently at an activity spot for the robotic station.
						if (currentStation.hasActivitySpots() && !currentStation.isAtActivitySpot(robot)) {
							// Walk to an available activity spot.
							walkToActivitySpotInBuilding(currentBuilding, true);
						}
					}
				}
			} else {
				// if (!atStation) {
				Building building = getAvailableRoboticStationBuilding(robot);
				if (building != null) {
					// System.out.println("building.toString() is " + building.toString() );
					station = building.getRoboticStation();
					if (station != null) {
						// TODO: see https://github.com/mars-sim/mars-sim/issues/22
						// Question: why would the method below cause RepairBot to walk outside the
						// settlement to a vehicle ?
						walkToActivitySpotInBuilding(building, true);
						// TODO: need to add activity spots in every building or
						// walkToActivitySpotInBuilding(building, false) will fail
						// and create java.lang.NullPointerException
						station.addSleeper();
					}
				}
			}
		}

		previousTime = marsClock.getMillisol();

		// Initialize phase
		addPhase(SLEEPING_MODE);
		setPhase(SLEEPING_MODE);
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.LIVING_ACCOMODATIONS;
	}

	@Override
	public FunctionType getRoboticFunction() {
		return FunctionType.ROBOTIC_STATION;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (person != null) {
			if (getPhase() == null)
				throw new IllegalArgumentException("Task phase is null");
			else if (SLEEPING.equals(getPhase())) {
				return sleepingPhase(time);
			} else
				return time;
		}

		else if (robot != null) {
			if (getPhase() == null)
				throw new IllegalArgumentException("Task phase is null");
			else if (SLEEPING_MODE.equals(getPhase()))
				return sleepingPhase(time);
			else
				return time;
		}
		return time;
	}

	/**
	 * Performs the sleeping phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double sleepingPhase(double time) {
//		logger.info(person + " at sleepingPhase()");
		
		if (person != null) {
			if (person.isOutside()) {
				walkBackInside();
				return time;
			}
			
			pc.recoverFromSoreness(.05);

			// Obtain the fractionOfRest to restore fatigue faster in high fatigue case
			double fractionOfRest = time * timeFactor;
			double newFatigue = 0;
			double residualFatigue = 0;
			double f = pc.getFatigue();
			if (f > MAX_FATIGUE) {
				f = MAX_FATIGUE;

				if (f > 1000)
					residualFatigue = (f - 1000) / 15; //75;

				// Reduce person's fatigue
				newFatigue = f - fractionOfRest - residualFatigue;
//				logger.info(person + " f : " + Math.round(f*10.0)/10.0 
//						+ "   time : " + Math.round(time*1000.0)/1000.0
//						+ "   residualFatigue : " + Math.round(residualFatigue*10.0)/10.0  
//						+ "   fractionOfRest : " + Math.round(fractionOfRest*10.0)/10.0  
//								+ "   newFatigue : " + Math.round(newFatigue*10.0)/10.0);
			}
			else {
				
				if (f > 1000)
					residualFatigue = (f - 1000) / 25;//125;
				else if (f > 500)
					residualFatigue = (f - 500) / 35;//175;
				
				newFatigue = f - fractionOfRest - residualFatigue;	
//				logger.info(person + " f : " + Math.round(f*10.0)/10.0
//						+ "   time : " + Math.round(time*1000.0)/1000.0
//						+ "   residualFatigue : " + Math.round(residualFatigue*10.0)/10.0  
//						+ "   fractionOfRest : " + Math.round(fractionOfRest*10.0)/10.0  
//								+ "   newFatigue : " + Math.round(newFatigue*10.0)/10.0);
			}
				
			pc.setFatigue(newFatigue);
				
			circadian.setAwake(false);
			
			totalSleepTime += time;
//			logger.info(person + "  time: " + Math.round(time*1000.0)/1000.0
//					+ "  totalSleepTime: " + Math.round(totalSleepTime*1000.0)/1000.0);
			
			// Adjust the leptin and ghrelin level
			circadian.getRested(time);

			// Record the sleep time [in millisols]
			circadian.recordSleep(time);
			
			double newTime = marsClock.getMillisol();
			double alarmTime = getAlarmTime();

			// Check if alarm went off
			if ((previousTime <= alarmTime) && (newTime >= alarmTime)) {
				circadian.setNumSleep(circadian.getNumSleep() + 1);
				circadian.updateSleepCycle((int) marsClock.getMillisol(), true);
				logger.info(person.getName() + " woke up from the alarm at " + (int)alarmTime);
				endTask();
			} else {
				previousTime = newTime;
			}
			
			if (newFatigue <= 0) {
				logger.finest(person.getName() + " woke up from sleep.");
				endTask();
			}
		}

		else if (robot != null) {
			double newTime = marsClock.getMillisol();
			double alarmTime = getAlarmTime();
			
			// Check if alarm went off
			if ((previousTime <= alarmTime) && (newTime >= alarmTime)) {
				logger.finest(robot.getName() + " woke up from alarm.");
				endTask();
			} else {
				previousTime = newTime;
			}
		}

		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	@Override
	public void endTask() {
		if (person != null) {
//	    	logger.info(person.getNickName() + " called endTask() in " + this);
			// Remove person from living accommodations bed so others can use it.
			if (accommodations != null && accommodations.getSleepers() > 0) {
				accommodations.removeSleeper(person);
			}

			circadian.setAwake(true);

		} else if (robot != null) {
			// Remove robot from stations so other robots can use it.
			if (station != null && station.getSleepers() > 0) {
				station.removeSleeper();
				// TODO: assess how well this work
//	        	logger.info(robot.getNickName() + " was done sleeping and waking up.");
				walkToAssignedDutyLocation(robot, false);
			}
		}
		
		super.endTask();
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

		Building result = null;

		if (person.isInSettlement()) {
			// BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> quartersBuildings = person.getSettlement().getBuildingManager()
					.getBuildings(FunctionType.LIVING_ACCOMODATIONS);
			quartersBuildings = BuildingManager.getNonMalfunctioningBuildings(quartersBuildings);
			quartersBuildings = getQuartersWithEmptyBeds(quartersBuildings, unmarked);
			if (quartersBuildings.size() > 0) {
				quartersBuildings = BuildingManager.getLeastCrowdedBuildings(quartersBuildings);
			}
			if (quartersBuildings.size() > 0) {
				Map<Building, Double> quartersBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						quartersBuildings);
				result = RandomUtil.getWeightedRandomObject(quartersBuildingProbs);
			}
		}

		return result;
	}

	public static Building getAvailableRoboticStationBuilding(Robot robot) {

		Building result = null;

		if (robot.isInSettlement()) {
			BuildingManager manager = robot.getSettlement().getBuildingManager();
			List<Building> buildings = manager.getBuildings(FunctionType.ROBOTIC_STATION);
			buildings = BuildingManager.getNonMalfunctioningBuildings(buildings);
			buildings = getRoboticStationsWithEmptySlots(buildings);
			if (RandomUtil.getRandomInt(2) == 0) // robot is not as inclined to move around
				buildings = BuildingManager.getLeastCrowded4BotBuildings(buildings);
			int size = buildings.size();
			// System.out.println("size is "+size);
			int selected = 0;
			if (size == 0)
				result = null;
			else if (size >= 1) {
				selected = RandomUtil.getRandomInt(size - 1);
				result = buildings.get(selected);
			}
		}

		return result;
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
			boolean notFull = quarters.getSleepers() < quarters.getBeds();
			// Check if an unmarked bed is wanted
			if (unmarked) {
				if (quarters.hasAnUnmarkedBed() && notFull ) {
					result.add(building);
				}
			}
			else if (notFull) {
				result.add(building);
			}
		}

		return result;
	}

	/***
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
	 * Gets the wakeup alarm time for the person's longitude.
	 * 
	 * @return alarm time in millisols.
	 */
	private double getAlarmTime() {
		double timeDiff = 0;
		double modifiedAlarmTime = 0;

		if (person != null) {
			ShiftType shiftType = person.getTaskSchedule().getShiftType();
			// Set to 33 millisols prior to the beginning of the duty shift hour
			if (shiftType == ShiftType.A)
				modifiedAlarmTime = 967;
			else if (shiftType == ShiftType.B)
				modifiedAlarmTime = 467;
			else if (shiftType == ShiftType.X)
				modifiedAlarmTime = 967;
			else if (shiftType == ShiftType.Y)
				modifiedAlarmTime = 300;
			else if (shiftType == ShiftType.Z)
				modifiedAlarmTime = 634;
			else if (shiftType == ShiftType.ON_CALL) { 
				// if only one person is at the settlement, go with this schedule
				timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
				modifiedAlarmTime = BASE_ALARM_TIME - timeDiff;
			}

		} else if (robot != null) {
			timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));
			modifiedAlarmTime = BASE_ALARM_TIME - timeDiff;
		}

		if (modifiedAlarmTime < 0D) {
			modifiedAlarmTime += 1000D;
		}
		return modifiedAlarmTime;
	}

	public boolean walkBackInside() {
		boolean canWalkInside = true;
		// Get closest airlock building at settlement.
		Settlement s = person.getLocationTag().findSettlementVicinity();
		if (s != null) {
			interiorObject = (Building)(s.getClosestAvailableAirlock(person).getEntity()); 
//			System.out.println("interiorObject is " + interiorObject);
			if (interiorObject == null)
				interiorObject = (LocalBoundedObject)(s.getClosestAvailableAirlock(person).getEntity());
//			System.out.println("interiorObject is " + interiorObject);
			LogConsolidated.log(Level.FINE, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
					+ " at "
					+ person.getLocationTag().getImmediateLocation()
					+ " found " + ((Building)interiorObject).getNickName()
					+ " as the closet building with an airlock to enter.");
		}
		else {
			// near a vehicle
			Rover r = (Rover)person.getVehicle();
			interiorObject = (LocalBoundedObject) (r.getAirlock()).getEntity();
			LogConsolidated.log(Level.INFO, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
					+ " was near " + r.getName()
					+ " and had to walk back inside the vehicle.");
		}
		if (interiorObject == null) {
			LogConsolidated.log(Level.WARNING, 0, sourceName,
				"[" + person.getLocationTag().getLocale() + "] " + person.getName()
				+ " was near " + person.getLocationTag().getImmediateLocation()
				+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
				+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
				+ " but interiorObject is null.");
			canWalkInside = false;
		}
		else {
			// Set return location.
			Point2D rawReturnInsideLoc = LocalAreaUtil.getRandomInteriorLocation(interiorObject);
			returnInsideLoc = LocalAreaUtil.getLocalRelativeLocation(rawReturnInsideLoc.getX(),
					rawReturnInsideLoc.getY(), interiorObject);
			
			if (returnInsideLoc != null && !LocalAreaUtil.checkLocationWithinLocalBoundedObject(returnInsideLoc.getX(),
						returnInsideLoc.getY(), interiorObject)) {
				LogConsolidated.log(Level.WARNING, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
						+ " was near " + ((Building)interiorObject).getNickName() //person.getLocationTag().getImmediateLocation()
						+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
						+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
						+ " but could not be found inside " + interiorObject);
				canWalkInside = false;
			}
		}

		// If not at return inside location, create walk inside subtask.
        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
        boolean closeToLocation = LocalAreaUtil.areLocationsClose(personLocation, returnInsideLoc);
        
		// If not inside, create walk inside subtask.
		if (interiorObject != null && !closeToLocation) {
			String name = "";
			if (interiorObject instanceof Building) {
				name = ((Building)interiorObject).getNickName();
			}
			else if (interiorObject instanceof Vehicle) {
				name = ((Vehicle)interiorObject).getNickName();
			}
					
			LogConsolidated.log(Level.FINEST, 10_000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
						+ " was near " +  name 
						+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
						+ Math.round(returnInsideLoc.getY()*10.0)/10.0 
						+ ") and was attempting to enter its airlock.");
			
			if (Walk.canWalkAllSteps(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject)) {
				Task walkingTask = new Walk(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject);
				addSubTask(walkingTask);
			} 
			
			else {
				LogConsolidated.log(Level.SEVERE, 0, sourceName,
						person.getName() + " was " + person.getTaskDescription().toLowerCase() 
						+ " and cannot find a valid path to enter an airlock. Will see what to do.");
				canWalkInside = false;
			}
		} else {
			LogConsolidated.log(Level.SEVERE, 0, sourceName,
					person.getName() + " was " + person.getTaskDescription().toLowerCase() 
					+ " and cannot find the building airlock to  walk back inside. Will see what to do.");
			canWalkInside = false;
		}
		
		return canWalkInside;
	}
	
	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();
		
		SLEEPING_MODE.destroy();
		SLEEPING.destroy();
		
		station = null;
		accommodations = null;
		circadian = null;
		pc = null;
		interiorObject = null;
		returnInsideLoc = null;
	}
}