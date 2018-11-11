/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarageMeta.java
 * @version 3.1.0 2017-10-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleGarage task.
 */
public class MaintainGroundVehicleGarageMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGroundVehicleGarage"); //$NON-NLS-1$

	/** default logger. */
	private static Logger logger = Logger.getLogger(MaintainGroundVehicleGarageMeta.class.getName());

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new MaintainGroundVehicleGarage(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		if (person.isInSettlement() || person.isRightOutsideSettlement()) {

			try {
				// Get all vehicles requiring maintenance.
				Iterator<Vehicle> i = MaintainGroundVehicleGarage.getAllVehicleCandidates(person).iterator();
				while (i.hasNext()) {
					Vehicle vehicle = i.next();
					MalfunctionManager manager = vehicle.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					boolean hasParts = Maintenance.hasMaintenanceParts(person, vehicle);
					double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
					boolean minTime = (effectiveTime >= 1000D);
					if (!hasMalfunction && hasParts && minTime) {
						double entityProb = effectiveTime / 50D;
						if (entityProb > 100D) {
							entityProb = 100D;
						}
						result += entityProb;
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "getProbability()", e);
			}

			// Determine if settlement has available space in garage.
			boolean garageSpace = false;
			boolean needyVehicleInGarage = false;

			Settlement settlement = person.getAssociatedSettlement();
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE)
					.iterator();
			while (j.hasNext() && !garageSpace) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = building.getGroundVehicleMaintenance();
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) {
						garageSpace = true;
					}

					Iterator<Vehicle> i = garage.getVehicles().iterator();
					while (i.hasNext()) {
						if (i.next().isReservedForMaintenance()) {
							needyVehicleInGarage = true;
						}
					}
				} catch (Exception e) {
				}
			}

			if (!garageSpace && !needyVehicleInGarage) {
				result = 0D;
			}

			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class)
						* settlement.getGoodsManager().getTransportationFactor();
			}

			// Modify if tinkering is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				result *= 1.5D;
			}

			// Add Preference modifier
			if (result > 0D) {
				result = result + result * person.getPreference().getPreferenceScore(this) / 5D;
			}

			if (result < 0)
				result = 0;

		}

		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new MaintainGroundVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

		double result = 0D;

		if (robot.isInSettlement()) {

			try {
				// Get all vehicles requiring maintenance.
				Iterator<Vehicle> i = MaintainGroundVehicleGarage.getAllVehicleCandidates(robot).iterator();
				while (i.hasNext()) {
					Vehicle vehicle = i.next();
					MalfunctionManager manager = vehicle.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					boolean hasParts = Maintenance.hasMaintenanceParts(robot, vehicle);
					double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
					boolean minTime = (effectiveTime >= 1000D);
					if (!hasMalfunction && hasParts && minTime) {
						double entityProb = effectiveTime / 50D;
						if (entityProb > 100D) {
							entityProb = 100D;
						}
						result += entityProb;
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "getProbability()", e);
			}

			// Determine if settlement has available space in garage.
			boolean garageSpace = false;
			boolean needyVehicleInGarage = false;

			Settlement settlement = robot.getSettlement();
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE)
					.iterator();
			while (j.hasNext() && !garageSpace) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = building.getGroundVehicleMaintenance();
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) {
						garageSpace = true;
					}

					Iterator<Vehicle> i = garage.getVehicles().iterator();
					while (i.hasNext()) {
						if (i.next().isReservedForMaintenance()) {
							needyVehicleInGarage = true;
						}
					}
				} catch (Exception e) {
				}
			}

			if (!garageSpace && !needyVehicleInGarage) {
				result = 0D;
			}

			// Effort-driven task modifier.
			result *= robot.getPerformanceRating();

			// Job modifier.
			RobotJob job = robot.getBotMind().getRobotJob();
			if (job != null) {
				result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class)
						* robot.getSettlement().getGoodsManager().getTransportationFactor();
			}

			if (result < 0)
				result = 0;

		}

		return result;
	}
}