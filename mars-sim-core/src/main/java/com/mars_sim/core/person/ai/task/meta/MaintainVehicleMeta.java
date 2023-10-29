/*
 * Mars Simulation Project
 * MaintainVehicleMeta.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.MaintainEVAVehicle;
import com.mars_sim.core.person.ai.task.MaintainGarageVehicle;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.VehicleMaintenance;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the MaintainGarageVehicle task.
 */
public class MaintainVehicleMeta extends MetaTask implements SettlementMetaTask {
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
	
    public MaintainVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setPreferredJob(JobType.MECHANICS);

		addPreferredRobot(RobotType.REPAIRBOT);
	}

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
     * 
	 * @param t Task being scored
	 * @parma r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
		return TaskUtil.assessRobot(t, r);
    }

	/**
	 * Gets a collection of Tasks for any Vehicle maintenance that is required.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		boolean insideTasks = getGarageSpaces(settlement) > 0;

		for (Vehicle vehicle : getAllVehicleCandidates(settlement, false)) {
			RatingScore score = MaintainBuildingMeta.scoreMaintenance(vehicle);

			// Vehicle in need of maintenance
			if (score.getScore() > 0) {
				tasks.add(new VehicleMaintenanceJob(this, vehicle, !insideTasks, score));
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
	 * Counts the number of available garages spaces in a Settlement.
	 * 
	 * @param settlement Location to check.
	 */
	public static int getGarageSpaces(Settlement settlement) {
		int garageSpaces = 0;
		for(Building j : settlement.getBuildingManager().getBuildingSet(FunctionType.VEHICLE_MAINTENANCE)) {
			VehicleMaintenance garage = j.getVehicleParking();
			
			garageSpaces += (garage.getAvailableCapacity() + garage.getAvailableFlyerCapacity());
		}
		return garageSpaces;
	}
}
