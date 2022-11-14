/*
 * Mars Simulation Project
 * LoadVehicleGarageMeta.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
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
 * Meta task for the LoadVehicleGarage task.
 */
public class LoadVehicleMeta extends MetaTask {
    private static class GarageLoadJob extends AbstractTaskJob {

        private VehicleMission target;

        public GarageLoadJob(VehicleMission target, double score) {
            super("Load " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new LoadVehicleGarage(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            return new LoadVehicleGarage(robot, target);
        }
    }

	private static class EVALoadJob extends AbstractTaskJob {

        private VehicleMission target;

        public EVALoadJob(VehicleMission target, double score) {
            super("EVA Load " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new LoadVehicleEVA(person, target);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleGarage"); //$NON-NLS-1$

    private static final double GARAGE_DEFAULT_SCORE = 500D;

    private static final double EVA_DEFAULT_SCORE = 300D;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(LoadVehicleMeta.class.getName());

    /** The static instance of the MissionManager */
	private static MissionManager missionManager;

    public LoadVehicleMeta() {
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
	 * Get a collection of Tasks for any mission that needs loading
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

        // Find all Vehcile missions with an active loading plan
		for(Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				LoadingController plan = vehicleMission.getLoadingPlan();

				// Must have a local Loading Plan that is not complete
				if ((plan != null) && plan.getSettlement().equals(settlement) && !plan.isCompleted()) {
                    TaskJob job = createLoadJob(vehicleMission, settlement, insideTasks, insideModifier, evaModifier);
                    if (job != null) {
                        tasks.add(job);
                    }
				}
			}
        }
        return tasks;
    }

    /**
     * Create the appriopriate TaskJob to load a mission. This considers whether the Vehcile is alreayd in a Garage
     * and whether there is Garage space.
     * @param vehicleMission Mission needing a load
     * @param settlement Locaiton the load is occuring
     * @param insideOnlyTasks Only inside tasks
     * @param insideModifier Scor emodifier for inside Task
     * @parma evaModifier Scor emodifier for EVA Tasks
     */
    private static TaskJob createLoadJob(VehicleMission vehicleMission, Settlement settlement, boolean insideOnlyTasks,
                                        double insideModifier, double evaModifier) {

        Vehicle vehicle = vehicleMission.getVehicle();
        if (vehicle == null)
            return null; // Should not happen

        boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
        if (insideOnlyTasks || inGarageAlready) {
            double score = GARAGE_DEFAULT_SCORE * insideModifier;
            if (inGarageAlready) {
                // If in Garage already then boost score
                score *= 2;
            }
            return new GarageLoadJob(vehicleMission, score);
        }
        return new EVALoadJob(vehicleMission, EVA_DEFAULT_SCORE * evaModifier);
    }

    /**
     * Create the appriopriate TaskJob to load a mission. This considers whether the Vehcile is alreayd in a Garage
     * and whether there is Garage space.
     * @param vehicleMission Mission needing a load
     * @param settlement Locaiton the load is occuring
     */
    public static TaskJob createLoadJob(VehicleMission vehicleMission, Settlement settlement) {
        return createLoadJob(vehicleMission, settlement, false, 1D, 1D);
    } 

    /**
	 * Attached to the common controllign classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}
}
