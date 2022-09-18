/*
 * Mars Simulation Project
 * Charge.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Charge task will replenish a Robots battery
 */
public class Charge extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Charge.class.getName());


	/** Task name for robot */
	private static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$

	/** Task phases for robot. */
	private static final TaskPhase CHARGING = new TaskPhase(Msg.getString("Task.phase.charging")); //$NON-NLS-1$

	
	public Charge(Robot robot) {
		super(NAME, robot, false, false, 0, 10D);
		setDescription(NAME);
		
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
        	// this task has ended
			endTask();
			return time;
		}
			
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

			}
		}
		else {
			// Disable charging
			robot.getSystemCondition().setCharging(false);
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

}
