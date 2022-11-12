/*
 * Mars Simulation Project
 * MaintainGarageVehicleMeta.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainEVAVehicle;
import org.mars_sim.msp.core.person.ai.task.MaintainGarageVehicle;
import org.mars_sim.msp.core.person.ai.task.util.AbstractTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGarageVehicle task.
 */
public class MaintainVehicleMeta extends MetaTask {
	private static class GarageMaintenanceJob extends AbstractTaskJob {

        private Vehicle target;

        public GarageMaintenanceJob(Vehicle target, double score) {
            super("Maintain " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainGarageVehicle(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            return new MaintainGarageVehicle(robot, target);
        }
    }

	private static class EVAMaintenanceJob extends AbstractTaskJob {

        private Vehicle target;

        public EVAMaintenanceJob(Vehicle target, double score) {
            super("EVA Maintain " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainEVAVehicle(person, target);
        }
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGarageVehicle"); //$NON-NLS-1$
	
    public MaintainVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setPreferredJob(JobType.MECHANICS);
	}

	@Override
    public List<TaskJob> getTaskJobs(Person person) {

        List<TaskJob> tasks = null;
		if (person.isInSettlement()) {
			double insideModifier = getPersonModifier(person);
			double evaModifier = insideModifier * getRadiationModifier(person.getSettlement())
									* getEVAModifier(person);

			tasks = getSettlementTasks(person.getSettlement(), insideModifier, evaModifier);
		}
		return tasks;
	}


	@Override
    public List<TaskJob> getTaskJobs(Robot robot) {

        List<TaskJob> tasks = null;
		if (robot.isInSettlement() && robot.getRobotType() == RobotType.REPAIRBOT) {
			double modifier = robot.getPerformanceRating();

			tasks = getSettlementTasks(robot.getSettlement(), modifier, 0D);
		}
		return tasks;
	}

	/**
	 * Get a collection of Tasks for any Vehcile maintenance that is required.
	 * @param settlement Settlement to scan for vehicles
	 * @param insideFactor Score modifier for inside jobs
	 * @param evaFactor Score modifier for EVA tasks
	 */
	private List<TaskJob> getSettlementTasks(Settlement settlement, double insideModifier, double evaModifier) {
		List<TaskJob> tasks = new ArrayList<>();

		boolean insideTasks = getGarageSpaces(settlement) > 0;
		if (!insideTasks && (evaModifier <= 0)) {
			// EVA tasks and on EVA allowed; then abort search
			return tasks;
		}

		for (Vehicle vehicle : getAllVehicleCandidates(settlement, false)) {
			double score = MaintainBuildingMeta.scoreMaintenance(vehicle);

			// Vehcile in need of maintenance
			if (score > 0) {
				if (insideTasks) {
					score *= insideModifier;
					tasks.add(new GarageMaintenanceJob(vehicle, score));
				}
				else {
					score *= evaModifier;
					tasks.add(new EVAMaintenanceJob(vehicle, score));	
				}
			}
		}

		return tasks;
	}
	
		/**
	 * Gets all ground vehicles requiring maintenance. Candidate list be filtered
	 * for just outside Vehicles.
	 * 
	 * @param home Settlement checking.
	 * @param mustBeOutside
	 * @return collection of ground vehicles available for maintenance.
	 */
	private static List<Vehicle> getAllVehicleCandidates(Settlement home, boolean mustBeOutside) {
		// Vehicle must not be reserved for Mission nor maintenance
		return home.getParkedVehicles().stream()
			.filter(v -> (!v.isReserved()
						&& (!mustBeOutside || !v.isInAGarage())))
			.collect(Collectors.toList());
	}

	/**
	 * Count the number of available garages spaces in a Settlement.
	 * @param settlement Location to check.
	 */
	private static int getGarageSpaces(Settlement settlement) {
		int garageSpaces = 0;
		for(Building j : settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE)) {
			VehicleMaintenance garage = j.getVehicleParking();
			
			garageSpaces += (garage.getAvailableCapacity() + garage.getAvailableFlyerCapacity());
		}
		return garageSpaces;
	}
}
