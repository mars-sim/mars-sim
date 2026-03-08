/*
 * Mars Simulation Project
 * MaintainVehicleMeta.java
 * @date 2025-09-21
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.maintenance.MaintenanceUtil;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGarageVehicle task.
 */
public class MaintainVehicleMeta extends MetaTask implements SettlementMetaTask {
	// Default logger
	// May add back private static final SimLogger logger = SimLogger.getLogger(MaintainVehicleMeta.class.getName());

	/**
     * Represents a Job needed for internal maintenance on a vehicle.
     */
	private static class VehicleMaintenanceJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public VehicleMaintenanceJob(SettlementMetaTask owner, Vehicle target, boolean eva, RatingScore score) {
            super(owner, "Vehicle Maintenance " + (eva ? "via EVA " : ""), target, score);
			setEVA(eva);
        }

		/**
         * The vehicle needing maintenance is the focus.
         */
        private Vehicle getVehicle() {
            return (Vehicle) getFocus();
        }

        @Override
        public Task createTask(Person person) {
			if (isEVA()) {
				return new MaintainEVAVehicle(person, getVehicle());
			}
            return new MaintainGarageVehicle(person, getVehicle());
        }

        @Override
        public Task createTask(Robot robot) {
			if (isEVA()) {
				throw new IllegalStateException("Robots can not do EVA Vehicel maintenance");
			}
            return new MaintainGarageVehicle(robot, getVehicle());
        }
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGarageVehicle"); //$NON-NLS-1$
	
	private static final double ROBOT_FACTOR = 2D;
	
    public MaintainVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
		addPreferredRobot(RobotType.REPAIRBOT, RobotType.DELIVERYBOT, RobotType.CONSTRUCTIONBOT);
		addAllCrewRoles();
	}

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
		var factor = TaskUtil.assessRobot(t, r);
		if (factor.getScore() >= 100)
			factor.addModifier("robot.expert", ROBOT_FACTOR);
		return factor;
    }

	/**
	 * Gets a collection of Tasks for any Vehicle maintenance that is required.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		Vehicle worstVehicle = null;
		double highestScore = 0;
		RatingScore score = new RatingScore(0);
				
		for (Vehicle vehicle : getAllDownVehicleCandidates(settlement, false)) {
				
			MalfunctionManager manager = vehicle.getMalfunctionManager();
			
			boolean hasMalfunction = manager.hasMalfunction();
			
			// Note: Look for entities that are NOT malfunction since
			//       malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
			if (!hasMalfunction) {
			
				boolean partsPosted = vehicle.getMalfunctionManager()
						.hasMaintenancePartsInStorage(settlement);
				
				score = MaintenanceUtil.scoreMaintenance(manager, vehicle, partsPosted);
	
				if (score.getScore() > highestScore) {
					worstVehicle = vehicle;
					highestScore = score.getScore();
				}
			}
		}
		
		// Vehicle in need of maintenance
		if (highestScore > 0) {
			
			boolean garageTask = MaintainVehicleMeta.hasGarageSpaces(
					worstVehicle.getAssociatedSettlement(), worstVehicle);
			
			tasks.add(new VehicleMaintenanceJob(this, worstVehicle, !garageTask, score));
		}

		// Reset them
		worstVehicle = null;
		highestScore = 0;
			
		for (Vehicle vehicle : getAllGoodVehicleCandidates(settlement, false)) {
				
			MalfunctionManager manager = vehicle.getMalfunctionManager();
			
			boolean hasMalfunction = manager.hasMalfunction();
			
			// Note: Look for entities that are NOT malfunction since
			//       malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
			if (!hasMalfunction) {
			
				boolean partsPosted = vehicle.getMalfunctionManager()
						.hasMaintenancePartsInStorage(settlement);
				
				score = MaintenanceUtil.scoreMaintenance(manager, vehicle, partsPosted);
	
				if (score.getScore() > highestScore) {
					worstVehicle = vehicle;
					highestScore = score.getScore();
				}
			}
		}
		
		// Vehicle in need of maintenance
		if (highestScore > 0) {
			
			boolean garageTask = MaintainVehicleMeta.hasGarageSpaces(
					worstVehicle.getAssociatedSettlement(), worstVehicle);
			
			tasks.add(new VehicleMaintenanceJob(this, worstVehicle, !garageTask, score));
		}
		
		return tasks;
	}
	
	/**
	 * Gets all good vehicles not reserved for maintenance yet. Candidate list be filtered
	 * for just outside Vehicles.
	 * 
	 * @param home Settlement checking.
	 * @param mustBeOutside
	 * @return collection of ground vehicles available for maintenance.
	 */
	private static List<Vehicle> getAllGoodVehicleCandidates(Settlement home, boolean mustBeOutside) {
		// Vehicle must not be reserved for Mission nor maintenance
		return home.getParkedGaragedVehicles().stream()
			.filter(v -> (!v.isReserved() && !v.isReservedForMaintenance()
						&& (!mustBeOutside || !v.isInGarage())))
			.collect(Collectors.toList());
	}

	/**
	 * Gets all down vehicles under maintenance. Candidate list be filtered
	 * for just outside Vehicles.
	 * 
	 * @param home Settlement checking.
	 * @param mustBeOutside
	 * @return collection of ground vehicles available for maintenance.
	 */
	private static List<Vehicle> getAllDownVehicleCandidates(Settlement home, boolean mustBeOutside) {
		// Vehicle must not be reserved for Mission nor maintenance
		return home.getParkedGaragedVehicles().stream()
			.filter(v -> (!v.isReserved() && v.isReservedForMaintenance()
						&& (!mustBeOutside || !v.isInGarage())))
			.collect(Collectors.toList());
	}
	
	/**
	 * Checks if a garages space is available in a Settlement.
	 * 
	 * @param settlement Location to check.
	 * @param vehicle
	 */
	public static boolean hasGarageSpaces(Settlement settlement, Vehicle vehicle) {

		for (Building j : settlement.getBuildingManager().getBuildingSet(
				FunctionType.VEHICLE_MAINTENANCE)) {
			VehicleMaintenance garage = j.getVehicleParking();
			
			boolean hasSpace = false;
			if (vehicle instanceof Rover)
				hasSpace = garage.getAvailableRoverCapacity() > 0;
			else if (vehicle instanceof Flyer)
				hasSpace = garage.getAvailableFlyerCapacity() > 0;
			else if (vehicle instanceof LightUtilityVehicle)
				hasSpace = garage.getAvailableUtilityVehicleCapacity() > 0;
				
			if (hasSpace)
				return true;
		}
		return false;
	}
	
	/**
	 * Counts the number of available garages spaces in a Settlement.
	 * 
	 * @param settlement Location to check.
	 * @param vehicle
	 */
	public static int getGarageSpaces(Settlement settlement, Vehicle vehicle) {
		int garageSpaces = 0;
		for(Building j : settlement.getBuildingManager().getBuildingSet(
				FunctionType.VEHICLE_MAINTENANCE)) {
			VehicleMaintenance garage = j.getVehicleParking();
			
			if (vehicle instanceof Rover)
				garageSpaces += garage.getAvailableRoverCapacity();
			else if (vehicle instanceof Flyer)
				garageSpaces += garage.getAvailableFlyerCapacity();
			else if (vehicle instanceof LightUtilityVehicle)
				garageSpaces += garage.getAvailableUtilityVehicleCapacity();
		}

		return garageSpaces;
	}
}
