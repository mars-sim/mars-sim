/*
 * Mars Simulation Project
 * RoboticStation.java
 * @date 2022-07-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The RoboticStation class is a building function for a Robotic Station.
 */
public class RoboticStation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RoboticStation.class.getName());
	
	/** The charge rate of the bot in kW. */
	public final static double CHARGE_RATE = 15D;

	private int slots;
	private int sleepers;
	private int occupantCapacity;
	
	private double powerToDraw;

	private Collection<Robot> robotOccupants;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Spec of the Robot station
	 * @throws BuildingException if error in constructing function.
	 */
	public RoboticStation(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.ROBOTIC_STATION, spec, building);

		robotOccupants = new HashSet<>();
		// Set occupant capacity.
		occupantCapacity = buildingConfig.getFunctionSpec(building.getBuildingType(), FunctionType.ROBOTIC_STATION).getCapacity();

		slots = spec.getCapacity();
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		// Demand is one stations for every robot
		double demand = settlement.getNumBots() * 1D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.ROBOTIC_STATION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				RoboticStation station = building.getRoboticStation();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += station.slots * wearModifier;
			}
		}

		double stationCapacityValue = demand / (supply + 1D);
		int stationCapacity = buildingConfig.getFunctionSpec(buildingName, FunctionType.ROBOTIC_STATION).getCapacity();
		return stationCapacity * stationCapacityValue;
	}

	/**
	 * Gets the number of slots in the living accommodations.
	 * 
	 * @return number of slots.
	 */
	public int getSlots() {
		return slots;
	}

	/**
	 * Gets the number of robots sleeping in the stations.
	 * 
	 * @return number of robots
	 */
	public int getSleepers() {
		return sleepers;
	}

	/**
	 * Adds a sleeper to a station.
	 * 
	 * @throws BuildingException if stations are already in use.
	 */
	public void addSleeper() {
		sleepers++;
		if (sleepers > slots) {
			sleepers = slots;
			throw new IllegalStateException("All slots are full.");
		}
	}

	/**
	 * Removes a sleeper from a station.
	 * 
	 * @throws BuildingException if no sleepers to remove.
	 */
	public void removeSleeper() {
		sleepers--;
		if (sleepers < 0) {
			sleepers = 0;
			throw new IllegalStateException("Slots are empty.");
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			double load = 0;
			double hrs = pulse.getElapsed() * MarsClock.HOURS_PER_MILLISOL;
			for (Robot robot: robotOccupants) {
				if (!robot.getSystemCondition().isBatteryAbove80()
						|| robot.getSystemCondition().isCharging()) {
					double energy = robot.getSystemCondition().deliverEnergy(CHARGE_RATE * hrs);
					load += energy;
		    		robot.fireUnitUpdate(UnitEventType.ROBOT_POWER_EVENT);
				}
			}
			if (hrs > 0)
				powerToDraw = load/hrs;
		}
		return valid;
	}

	@Override
	public double getMaintenanceTime() {
		return slots * 7D;
	}

	/**
	 * Gets the building's capacity for supporting occupants.
	 * 
	 * @return number of inhabitants.
	 */
	public int getOccupantCapacity() {
		return occupantCapacity;
	}

	/**
	 * Gets a collection of robotOccupants in the building.
	 * 
	 * @return collection of robotOccupants
	 */
	public Collection<Robot> getRobotOccupants() {
		return robotOccupants;
	}

	public int getRobotOccupantNumber() {
		return robotOccupants.size();
	}

	/**
	 * Gets the available occupancy room.
	 * 
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = occupantCapacity - getRobotOccupantNumber();
        return Math.max(available, 0);
	}

	/**
	 * Checks if the building contains a particular unit.
	 * 
	 * @return true if unit is in building.
	 */
	public boolean containsRobotOccupant(Robot robot) {
		return robotOccupants.contains(robot);

	}

	/**
	 * Adds a robot to the building. Note: robot capacity can be exceeded
	 * 
	 * @param robot new robot to add to building.
	 * @throws BuildingException if robot is already building occupant.
	 */
	public void addRobot(Robot robot) {
		if (!robotOccupants.contains(robot)) {
			// Remove robot from any other inhabitable building in the settlement.
			Iterator<Building> i = getBuilding().getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(FunctionType.ROBOTIC_STATION)) {
					BuildingManager.removeRobotFromBuilding(robot, building);
				}
			}

			// Add robot to this building.
			logger.fine(robot, "Added to " + getBuilding() + "'s robotic station.");
			robotOccupants.add(robot);
		} else {
			throw new IllegalStateException("This robot is already in this building.");
		}
	}

	/**
	 * Removes a robot from the building.
	 * 
	 * @param occupant the robot to remove from building.
	 * @throws BuildingException if robot is not building occupant.
	 */
	public void removeRobot(Robot robot) {
		if (robotOccupants.contains(robot)) {
			robotOccupants.remove(robot);
			logger.fine(robot, "Removed from " + getBuilding() + "'s robotic station.");
		} else {
			throw new IllegalStateException("The robot is not in this building.");
		}
	}

	/**
	 * Gets the amount of power required, based on the current load.
	 *
	 * @return power (kW) default zero
	 */
	@Override
	public double getFullPowerRequired() {
		double power = 0;
		if (powerToDraw > 0) {
			// Set the power load this time to the power load to draw
			power = powerToDraw;
			// Reset it back to zero
			powerToDraw = 0;
		}
		return power;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		super.destroy();
		robotOccupants.clear();
		robotOccupants = null;
	}
}
