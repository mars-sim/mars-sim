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
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.RoboticStation;
import com.mars_sim.core.time.MarsTime;
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

	static final int LEVEL_UPPER_LIMIT = 95;
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = Charge.class.getSimpleName();
	
	/** Task name for robot */
	public static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$
	public static final String CHARGING_AT = "Charging at ";
	public static final String WIRELESS_CHARGING_AT = "Wireless Charging at ";
	public static final String END_CHARGING = "Charging Ended";
	public static final String NO_STATION = "No Station Available";
	 
	/** Task phases for robot. */
	private static final TaskPhase CHARGING = new TaskPhase(Msg.getString("Task.phase.charging")); //$NON-NLS-1$
	
	private boolean isWirelessCharge = false;
	
	public Charge(Robot robot, Building buildingStation) {
		super(NAME, robot, false, false, 0, 50D);
	
		// NOTE: May offer directional charging in future
		
		boolean canWalk = false;
		
		// Future: robot should first "reserve" a spot before going there
		
		if (buildingStation == null) {
			buildingStation = findStation(robot);
		}
		
		if (buildingStation == null) {
			setDescriptionDone(NO_STATION);
		}
		
		if (buildingStation != null) {
			
		RoboticStation station = buildingStation.getRoboticStation();
		
			if (station != null) {
				
				setDescription(NAME);
	//			logger.info(robot, 30_000L, "Walking to " + buildingStation + ".");
				
				canWalk = walkToRoboticStation(robot, false);
			}
		}
		
		if (!canWalk) {
			// Future: at this point. May switch to wireless charging that would be slower
			
			isWirelessCharge = true;
			
			logger.info(robot, 30_000L, "Switching to wireless charging. "
					+ "Unable to find a robotic station. Current building: " 
					+ robot.getBuildingLocation() + ".");
		}

		// Initialize phase
		addPhase(CHARGING);
		setPhase(CHARGING);
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
			setDescriptionDone(END_CHARGING);
			
			setDuration(0);
        	// this task has ended
//			endTask();
			return time;
		}
	
		boolean toCharge = false;
		SystemCondition sc = robot.getSystemCondition();
		double batteryLevel = sc.getBatteryState();
		double threshold = sc.getRecommendedThreshold();
		double lowPower = sc.getLowPowerPercent();
		double timeLeft = getTimeLeft();
		
		if (batteryLevel >= LEVEL_UPPER_LIMIT) {
			toCharge = false;
		}

		else if (batteryLevel > threshold) {
			// Note that the charging process will continue until it's at least 80%
			// before ending
			double rand = RandomUtil.getRandomDouble(batteryLevel);
			if (rand < lowPower + timeLeft * 2)
				// At max, 20% chance it will need to charge 
				toCharge = true;
			else
				toCharge = false;
		}
		
		else if (batteryLevel > lowPower) {

			double rand = RandomUtil.getRandomDouble(batteryLevel);
			if (rand < lowPower + timeLeft * 2)
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
			
			if (isWirelessCharge) {
				
				setDescription(WIRELESS_CHARGING_AT + Math.round(batteryLevel * 10.0)/10.0 + "%");

				// Look for a station that offers wireless charging
				RoboticStation station = null;
				
				Building building = robot.getBuildingLocation();
				
				if (building != null) {
					
					station = building.getRoboticStation();
					
					if (station == null) {
						Set<Building> functionBuildings = robot.getAssociatedSettlement()
								.getBuildingManager().getBuildingSet(FunctionType.ROBOTIC_STATION);
					
						building = RandomUtil.getARandSet(functionBuildings);
						
						station = building.getRoboticStation();
						
						if (station == null) {
							logger.severe(robot, 30_000L, "Unable to find a robotic station with wireless charging. "
									+ "Current building: " + robot.getBuildingLocation() + ".");
							endTask();
							return 0;
						}
					}
				
					double hrs = time * MarsTime.HOURS_PER_MILLISOL;
		
					double energy = sc.deliverEnergy(RoboticStation.WIRELESS_CHARGE_RATE * hrs);
					
					// Record the power spent at the robotic station
					station.setPowerLoad(energy/hrs);
		
					// Reset the duration
					setDuration(LEVEL_UPPER_LIMIT - batteryLevel);
				}
			}
			
			else {
				RoboticStation station = robot.getStation();
			
				if (station != null) {
					
					setDescription(CHARGING_AT + Math.round(batteryLevel * 10.0)/10.0 + "%");
					
					double hrs = time * MarsTime.HOURS_PER_MILLISOL;
		
					double energy = sc.deliverEnergy(RoboticStation.CHARGE_RATE * hrs);
					
					// Record the power spent at the robotic station
					station.setPowerLoad(energy/hrs);
		
					// Reset the duration
					setDuration(LEVEL_UPPER_LIMIT - batteryLevel);
				}
				else {
					setDescriptionDone(END_CHARGING);
					
					setDuration(0);
					
					// End charging
	//				endTask();
					
					return time;
				}
			}
		}
		else {
			setDescriptionDone(END_CHARGING);
			
			setDuration(0);
			
			// End charging
//			endTask();
			
			return time;
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

//		if (robot.getStation() != null) {
//			robot.getStation().removeRobot(robot);
//		}
		
		walkToAssignedDutyLocation(robot, true);
		
		super.clearDown();
	}


	/**
	 * Looks for a robotic station.
	 * 
	 * @return
	 */
	static Building findStation(Robot robot) {
		// Case 1: Check if robot currently occupies a robotic station spot already
		RoboticStation roboticStation = robot.getStation();
		if (roboticStation != null) {			
			return roboticStation.getBuilding();
		}
		
		// Case 2: assume robot is serving a different function (other than robotic station) in a building.
		// Check if robot's current building may offer a spot
		Building currentBldg = robot.getBuildingLocation();
		
		if (currentBldg != null && currentBldg.hasFunction(FunctionType.ROBOTIC_STATION)) {
			roboticStation = currentBldg.getRoboticStation();
			if (roboticStation != null) {
					
				// Future: reserve an activity spot in robotic station for this bot
				
				if (roboticStation.isSpaceAvailable()) {
					return currentBldg;
				}
			}
		}
		
		// Case 3: Check other buildigns
		Set<Building> functionBuildings = robot.getAssociatedSettlement()
				.getBuildingManager().getBuildingSet(FunctionType.ROBOTIC_STATION);
	
		for (Building bldg : functionBuildings) {
			
			// Future: reserve an activity spot in robotic station for this bot
			roboticStation = bldg.getRoboticStation();
			
			if (roboticStation.isSpaceAvailable()) {
				return bldg;
			}
		}
		
		return null;
	}
	
	/**
	 * Walks to a robotic station.
	 * 
	 * @param robot
	 * @param allowFail
	 * @return
	 */
	protected boolean walkToRoboticStation(Robot robot, RoboticStation station, boolean allowFail) {
		return walkToActivitySpotInBuilding(BuildingManager.getBuilding(robot), 
				FunctionType.ROBOTIC_STATION, allowFail);
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

}
