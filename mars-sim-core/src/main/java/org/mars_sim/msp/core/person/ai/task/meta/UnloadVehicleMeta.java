/*
 * Mars Simulation Project
 * UnloadVehicleGarageMeta.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.util.AbstractTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleMeta extends MetaTask {
    private static class GarageUnloadJob extends AbstractTaskJob {

        private Vehicle target;

        public GarageUnloadJob(Vehicle target, double score) {
            super("Unload " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new UnloadVehicleGarage(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            return new UnloadVehicleGarage(robot, target);
        }
    }

	private static class EVAUnloadJob extends AbstractTaskJob {

        private Vehicle target;

        public EVAUnloadJob(Vehicle target, double score) {
            super("EVA Unload " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new UnloadVehicleEVA(person, target);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicle"); //$NON-NLS-1$

    private static final double BASE_SCORE = 300D;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleMeta.class.getName());

    private static MissionManager missionManager;
	
    public UnloadVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
        addPreferredRobot(RobotType.DELIVERYBOT);
	}

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        List<TaskJob> tasks = null;
		if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();
			double insideModifier = getPersonModifier(person);
            insideModifier *= settlement.getGoodsManager().getTransportationFactor();

			double evaModifier = insideModifier * getRadiationModifier(settlement)
									* getEVAModifier(person);

			tasks = getSettlementTasks(settlement, insideModifier, evaModifier);
		}
		return tasks;
	}

	@Override
    public List<TaskJob> getTaskJobs(Robot robot) {

        List<TaskJob> tasks = null;
		if (robot.isInSettlement()) {
			double modifier = robot.getPerformanceRating();

			tasks = getSettlementTasks(robot.getSettlement(), modifier, 0D);
		}
		return tasks;
	}

	/**
	 * Get a collection of Tasks for any vehicle that needs unloading
	 * @param settlement Settlement to scan for vehicles
	 * @param insideFactor Score modifier for inside jobs
	 * @param evaFactor Score modifier for EVA tasks
	 */
	private List<TaskJob> getSettlementTasks(Settlement settlement, double insideModifier, double evaModifier) {
		List<TaskJob> tasks = new ArrayList<>();

		boolean insideTasks = MaintainVehicleMeta.getGarageSpaces(settlement) > 0;
		if (!insideTasks && (evaModifier <= 0)) {
			// EVA tasks and on EVA allowed; then abort search
			return tasks;
		}

        Set<Vehicle> assessed = new HashSet<>();
        
        // Check Vehicle Missions first
		for (Mission mission : missionManager.getMissions()) {
			if ((mission instanceof VehicleMission) && !mission.isDone()) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				if (vehicleMission.isVehicleUnloadableHere(settlement)) {
                    Vehicle v = vehicleMission.getVehicle();
                    if (v != null) {
                        // Not sure why vehicle could be null but it does happen. Race condition of vehicle
                        // being released before the mission is completed?
                        assessed.add(v);

                        TaskJob job = scoreVehicle(settlement, v, insideTasks, insideModifier, evaModifier);
                        if (job != null) {
                            tasks.add(job);
                        }
                    }
                }
            }
        }

        // Check non-mission vehicles
        for(Vehicle vehicle : settlement.getParkedVehicles()) {
			if (!vehicle.isReserved() && !assessed.contains(vehicle)) {
                TaskJob job = scoreVehicle(settlement, vehicle, insideTasks, insideModifier, evaModifier);
                if (job != null) {
                    tasks.add(job);
                }
			}
        }
        return tasks;
    }

    /**
     * Score a vehicle for it's suitability to be unloaded.
     * @param settlement Location of Vehcile
     * @param vehicle Vehicle to unload
     * @param insideOnlyTasks Only do Garage inside Tasks
     * @param insideModifier Modifier for inside task score
     * @param evaModifier Modifier for EVA task score
     * @return
     */
    private static TaskJob scoreVehicle(Settlement settlement, Vehicle vehicle, boolean insideOnlyTasks,
                                double insideModifier, double evaModifier) {
        TaskJob result = null;
        double remaining = vehicle.getStoredMass();
        if (remaining > 0D) {
            double score = BASE_SCORE + (100D * remaining)/vehicle.getCargoCapacity();
        
            boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
            if (insideOnlyTasks || inGarageAlready) {
                score *= insideModifier;
                if (inGarageAlready) {
                    // If in Garage already then boost score
                    score *= 2;
                }
                return new GarageUnloadJob(vehicle, score);
            }
            return new EVAUnloadJob(vehicle, score * evaModifier);    
        }

        return result;
    }

    /**
     * Create an appropriate Unload job for a vehicle.
     */
    public static TaskJob createUnloadJob(Settlement settlement, Vehicle vehicle) {
        return scoreVehicle(settlement, vehicle, false, 1D, 1D);
    }

    /**
	 * Attached to the common controllign classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}

}
