/*
 * Mars Simulation Project
 * Charge.java
 * @date 2023-12-04
 * @author Barry Evans
 */
package com.mars_sim.core.robot.ai.task;

import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.RoboticStation;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Charge task will replenish a robot's battery.
 */
public class Charge extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Charge.class.getName());

	/** Simple Task name */
	public static final String SIMPLE_NAME = Charge.class.getSimpleName();
	
	/** Task name for robot */
	public static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$

	/** Task phases for robot. */
	private static final TaskPhase CHARGING = new TaskPhase(Msg.getString("Task.phase.charging")); //$NON-NLS-1$
	
	public Charge(Robot robot, Building buildingStation) {
		super(NAME, robot, false, false, 0, 50D);
	
		boolean canWalk = false;
		
		if (buildingStation == null) {
			buildingStation = findChargingStation(robot);
		}
		
		if (buildingStation == null) {
			endTask();
			return;
		}
		
		RoboticStation station = buildingStation.getRoboticStation();
		if (station == null) {
			canWalk = walkToRoboticStation(robot, false);
//			logger.info(robot, 10_000L, "canWalk: " + canWalk + ".");
		}
		
		if (canWalk) {	
			logger.info(robot, 10_000L, "Walking to " + station + ".");
			station = robot.getStation();
		}

		if (station != null && station.getSleepers() < station.getSlots()) {
			station.addSleeper();
		}
		else {
			// End charging
			endTask();
		}
			
		// Initialize phase
		addPhase(CHARGING);
		setPhase(CHARGING);
	}

	/**
	 * Looks for a robotic charging station.
	 * 
	 * @return
	 */
	static Building findChargingStation(Robot robot) {
		Building currentBldg = robot.getBuildingLocation();
    	
		if (currentBldg != null && currentBldg.hasFunction(FunctionType.ROBOTIC_STATION)) {
			RoboticStation roboticStation = currentBldg.getRoboticStation();
			if (roboticStation != null) {
				// Find an empty spot in robotic station
				LocalPosition loc = roboticStation.getAvailableActivitySpot();
				if (loc != null) {
					return currentBldg;
				}
			}
		}
		
		Set<Building> functionBuildings = robot.getAssociatedSettlement()
				.getBuildingManager().getBuildingSet(FunctionType.ROBOTIC_STATION);

		for (Building bldg : functionBuildings) {
			if (bldg != null && bldg.hasFunction(FunctionType.ROBOTIC_STATION)) {
				RoboticStation roboticStation = bldg.getRoboticStation();
				if (roboticStation != null) {
					// Find an empty spot in robotic station
					LocalPosition loc = roboticStation.getAvailableActivitySpot();
					if (loc != null) {
						return bldg;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null)
			throw new IllegalArgumentException("Task phase is null");
		else if (CHARGING.equals(getPhase()))
			return chargingPhase(time);
		else
			return time;
	}


	/**
	 * Performs the charging phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double chargingPhase(double time) {
		
		if (isDone() || getTimeLeft() <= 0) {
        	// this task has ended
			endTask();
			return time;
		}
	
		boolean toCharge = false;
		SystemCondition sc = robot.getSystemCondition();
		double batteryLevel = sc.getBatteryState();
		double threshold = sc.getRecommendedThreshold();
		double lowPower = sc.getLowPowerPercent();
		double timeLeft = getTimeLeft();
		
		if (batteryLevel >= 95) {
			toCharge = false;
		}

		else if (batteryLevel > threshold) {
			// Note that the charging process will continue until it's at least 80%
			// before ending
			double rand = RandomUtil.getRandomDouble(batteryLevel);
			if (rand < lowPower + timeLeft/2)
				// At max, 20% chance it will need to charge 
				toCharge = true;
			else
				toCharge = false;
		}
		
		else if (batteryLevel > lowPower) {

			double rand = RandomUtil.getRandomDouble(batteryLevel);
			if (rand < lowPower + timeLeft)
				// At max, 20% chance it will need to charge 
				toCharge = true;
			else
				toCharge = false;
		}

		// if power is below a certain threshold ~70%, stay to get a recharge
		else {
			toCharge = true;
		}

		if (toCharge) {		
			// Switch to charging
			sc.setCharging(true);
			
			RoboticStation station = robot.getStation();
		
			if (station != null) {
				
				setDescription(NAME);
				
				double hrs = time * MarsTime.HOURS_PER_MILLISOL;
	
				double energy = sc.deliverEnergy(RoboticStation.CHARGE_RATE * hrs);
				
				// Record the power spent at the robotic station
				station.setPowerLoad(energy/hrs);
	
				// Lengthen the duration for charging the battery
				setDuration(timeLeft + (100 - batteryLevel));
			}
			else {
				// End charging
				endTask();
			}
		}
		else {
			// End charging
			endTask();
		}

		return 0;
	}

	/**
	 * If worker is a Robot then send them to report to duty
	 */
	@Override
	protected void clearDown() {

		// Disable charging so that it can potentially 
		// be doing other tasks while consuming energy
		robot.getSystemCondition().setCharging(false);

		// Remove robot from stations so other robots can use it.
		RoboticStation station = robot.getStation();
		if (station != null && station.getSleepers() > 0) {
			station.removeSleeper();
			// NOTE: assess how well this work
		}
		
		walkToAssignedDutyLocation(robot, true);

		super.clearDown();
	}

//	public static Building getAvailableRoboticStationBuilding(Robot robot) {
//		if (robot.isInSettlement()) {
//			BuildingManager manager = robot.getSettlement().getBuildingManager();
//			Set<Building> buildings0 = manager.getBuildingSet(FunctionType.ROBOTIC_STATION);
//			Set<Building> buildings1 = BuildingManager.getNonMalfunctioningBuildings(buildings0);
//			Set<Building> buildings2 = getRoboticStationsWithEmptySlots(buildings1);
//			Set<Building> buildings3 = null;
//			if (!buildings2.isEmpty()) {
//				// robot is not as inclined to move around
//				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings2);
//				if (buildings3 == null) {
//					buildings3 = buildings2;
//				}
//			}
//			else if (!buildings1.isEmpty()) {
//				// robot is not as inclined to move around
//				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings1);
//				if (buildings3 == null) {
//					buildings3 = buildings1;
//				}
//			}
//			else if (!buildings0.isEmpty()) {
//				// robot is not as inclined to move around
//				buildings3 = BuildingManager.getLeastCrowded4BotBuildings(buildings0);
//				if (buildings3 == null) {
//					buildings3 = buildings0;
//				}
//			}
//			
//			if (buildings3 == null)
//				return null;
//			
//			int size = buildings3.size();
//
//			if (size >= 1) {
//				return RandomUtil.getARandSet(buildings3);
//			}
//		}
//
//		return null;
//	}
//
//	/**
//	 * Gets a set of robotic stations with empty spots
//	 *
//	 * @param buildingList
//	 * @return
//	 */
//	private static Set<Building> getRoboticStationsWithEmptySlots(Set<Building> buildings) {
//		Set<Building> result = new UnitSet<>();
//
//		Iterator<Building> i = buildings.iterator();
//		while (i.hasNext()) {
//			Building building = i.next();
//			RoboticStation station = building.getRoboticStation();
//			if (station.getSleepers() < station.getSlots()) {
//				result.add(building);
//			}
//		}
//
//		return result;
//	}

}
