/*
 * Mars Simulation Project
 * Charge.java
 * @date 2025-07-25
 * @author Barry Evans
 */
package com.mars_sim.core.robot.ai.task;

import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Charge task will replenish a robot's battery.
 */
public class Charge extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Charge.class.getName());

	static final int LEVEL_UPPER_LIMIT = 100;
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = Charge.class.getSimpleName();
	
	/** Task name for robot */
	public static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$
	public static final String FINDING = "Finding a robotic station";
	public static final String WALKING = "Walking to a robotic station";
	public static final String REGULAR_CHARGING = "Regular Charging";
	public static final String WIRELESS_CHARGING = "Wireless Charging";
	public static final String END_CHARGING = "Charging Ended";
	public static final String NO_STATION = "No Station Available";
	 
	/** Task phases for robot. */
	private static final TaskPhase CHARGING = new TaskPhase(Msg.getString("Task.phase.charging")); //$NON-NLS-1$
	
	public static final double DURATION = 50D;
	
	private boolean isWirelessCharge = false;
	
	public Charge(Robot robot, Building currentBuilding) {
		super(NAME, robot, false, false, 0, DURATION);
		
		boolean canWalk = false;
		
		// Future: robot should first "reserve" a spot before going there
		
		if (currentBuilding == null) {
			
			setDescription(FINDING);
			
			currentBuilding = findStation(robot);
		}
		
		if (currentBuilding == null) {
			
			setDescriptionDone(NO_STATION);
		}
		
		else {
			
			RoboticStation station = currentBuilding.getRoboticStation();
		
			if (station != null) {
				
				canWalk = walkToRoboticStation(station, true);
			}
			
			// Future : walk to a nearby building with robotic station even if the station is full
			
		}
		
		if (currentBuilding == null || !canWalk) {
			// Note: at this point, switch to wireless charging 
			// Note: Wireless charging is a slow
			
			isWirelessCharge = true;
			
			setDescription(WIRELESS_CHARGING);
		}
		
		// Initialize phase
		setPhase(CHARGING);
	}

	/**
	 * Walks to a robotic station.
	 * 
	 * @param robot
	 * @param station
	 * @param allowFail
	 * @return
	 */
	protected boolean walkToRoboticStation(RoboticStation station, boolean allowFail) {
		// Set the description
		setDescription(WALKING);
		
		return walkToActivitySpotInBuilding(station.getBuilding(), 
				FunctionType.ROBOTIC_STATION, allowFail);
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
			
			endCharging();
			
			return 0;
		}
	
		boolean toCharge = false;
		
		SystemCondition sc = robot.getSystemCondition();
		double batteryLevel = sc.getBattery().getBatteryPercent();
		
		if (getDuration() == DURATION) {
			// When this phase is being called for the first time
			toCharge = true;
		}
		
		else {
			
			double threshold = sc.getRecommendedThreshold();
			double lowPower = sc.getBattery().getLowPowerPercent();
		
			if (batteryLevel >= LEVEL_UPPER_LIMIT) {
				endCharging();
				
				return 0;
			}

			if (batteryLevel > threshold) {

				double rand = RandomUtil.getRandomDouble(batteryLevel);
				if (rand < lowPower * 3)
					// Will charge 
					toCharge = true;
			}
			
			else if (batteryLevel > lowPower) {

				double rand = RandomUtil.getRandomDouble(batteryLevel);
				if (rand < lowPower * 3)
					// Will need to charge 
					toCharge = true;
			}

			// if power is below the low power level, always recharge
			else {
				toCharge = true;
			}
		}

		if (toCharge) {
			
			// Switch to charging
			sc.setCharging(true);
			
			RoboticStation occupiedStation = robot.getOccupiedStation();
			
			// If robot cannot occupy a station, go with wireless charging
			if (occupiedStation == null) {
				isWirelessCharge = true;
			}
			
			if (isWirelessCharge) {

				// FUTURE: will need to consider if a robot is in a remote station, 
				// outside or in a vehicle

				Building building = robot.getBuildingLocation();
					
				if (building != null) {		
					
					// FUTURE: Will consider how many wireless charging ports are available 
					// Look for a station that offers wireless charging
					
					RoboticStation station = building.getRoboticStation();
					
					if (station != null) {
						chargeUp(sc, station, batteryLevel, time, 
								WIRELESS_CHARGING, RoboticStation.WIRELESS_CHARGE_RATE);	
					}
					
					else {
						logger.warning(robot, "No station found for wireless charging.");
						
						endCharging();
						
						return 0;
					}
				}
			}
			
			else {
				chargeUp(sc, occupiedStation, batteryLevel, time, 
							REGULAR_CHARGING, RoboticStation.CHARGE_RATE);	
			}
		}
		
		else {
			// Reset the duration
			setDuration(LEVEL_UPPER_LIMIT - batteryLevel);
			
			return 0;
		}

		return 0;
	}

	/**
	 * Charges the battery, namely, delivering power to the robot battery.
     * 
     * Note: For calculating charging time: To estimate charging time, divide 
     * the battery capacity (in Ah) by the charging current (in A), and add 
     * 0.5-1 hour to account for the slower charging rate at the end of the cycle.
	 * 
	 * Constant-Current Charging: For lithium batteries, the typical charging current 
	 * is usually set between 0.2C and 1C, with 0.5C being a commonly recommended 
	 * balance between charging time and safety.
	 * 
	 * Pre-Charge Stage: A pre-charge stage, also known as trickle charging, can be
	 * helpful to extend battery life. For single lithium-ion batteries, this stage 
	 * typically occurs at 3.0V with a current of around 100mA (10% of the constant 
	 * current charging current).
	 * 
	 * @param sc
	 * @param station
	 * @param batteryLevel
	 * @param time
	 * @param mode
	 * @param rate the power [in kW] available for charging 
	 * @return
	 */
	private double chargeUp(SystemCondition sc, RoboticStation station, double batteryLevel, 
			double time, String mode, double rate) {
		
		setDescription(mode + " at " 
				+ Math.round(batteryLevel * 10.0)/10.0 + "%");

		double hrs = time * MarsTime.HOURS_PER_MILLISOL;
		// Note: input energy kWh = rate [1kW] * hours
		double kWhAccepted = sc.getBattery().chargeBattery(rate * hrs, hrs);
		
		if (kWhAccepted < 0.001) {
			
			endCharging();
			
			return 0;
		}
		
		// Record the power delivered to robot's battery at the robotic station
		station.setPowerLoad(kWhAccepted/hrs);

		// Reset the duration
		setDuration(LEVEL_UPPER_LIMIT - batteryLevel);
	
		return 0;
	}
	
	/**
	 * Ends the charging.
	 */
	private void endCharging() {
		setDescriptionDone(END_CHARGING);
		
		setDuration(0);
		// End charging
//			endTask();
	}
	
	/**
	 * If worker is a Robot then send them to report to duty
	 */
	@Override
	protected void clearDown() {

		// Disable charging so that it can potentially 
		// be doing other tasks while consuming energy
		robot.getSystemCondition().setCharging(false);

		walkToAssignedDutyLocation(robot, true);
		
		super.clearDown();
	}

	/**
	 * Looks for a robotic station.
	 * 
	 * @param robot
	 * @return
	 */
	public static Building findStation(Robot robot) {
		
		// Case 1: Check if robot currently occupies a robotic station spot already
		RoboticStation roboticStation = robot.getOccupiedStation();
		
		if (roboticStation != null) {
			return roboticStation.getBuilding();
		}
		
		// Case 2: assume robot is serving a different function (other than robotic station) in a building.
		// Check if robot's current building may offer a spot
		Building currentBldg = robot.getBuildingLocation();
		
		if (currentBldg != null 
				&& currentBldg.getCategory() != BuildingCategory.EVA
				&& currentBldg.hasFunction(FunctionType.ROBOTIC_STATION)) {
			
			roboticStation = currentBldg.getFunction(FunctionType.ROBOTIC_STATION);
			
			if (roboticStation != null) {
					
				// Future: reserve an activity spot in robotic station for this bot
				if (roboticStation.isSpaceAvailable()) {
					return currentBldg;
				}
			}
		}
		
		// Case 3: Check other buildings
		Set<Building> functionBuildings = robot.getSettlement().getBuildingManager()
					.getBuildings(FunctionType.ROBOTIC_STATION)
					.stream()
					.filter(b -> b.getZone() == robot.getBuildingLocation().getZone()
							&& b.getCategory() != BuildingCategory.EVA
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		
		for (Building bldg : functionBuildings) {
			
			// Future: reserve an activity spot in robotic station for this bot
			roboticStation = bldg.getRoboticStation();
			
			if (roboticStation.isSpaceAvailable()) {
				return bldg;
			}
		}
		
		return null;
	}
}
