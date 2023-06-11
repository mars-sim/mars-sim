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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
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
public class LoadVehicleMeta extends MetaTask 
    implements SettlementMetaTask {
        
    private static class LoadJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private VehicleMission target;
        private boolean eva;

        private LoadJob(SettlementMetaTask owner, VehicleMission target, boolean eva, double score) {
            super(owner, "Load " + (eva ? "via EVA " : "") + "@ " + target.getName(), score);
            this.target = target;
            this.eva = eva;
        }

        @Override
        public Task createTask(Person person) {
            if (eva) {
                return new LoadVehicleEVA(person, target);
            }
            return new LoadVehicleGarage(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            if (eva) {
				// SHould not happen
				throw new IllegalStateException("Robots can not do EVA load vehicle");
			}
            return new LoadVehicleGarage(robot, target);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicle"); //$NON-NLS-1$

    private static final double GARAGE_DEFAULT_SCORE = 500D;

    private static final double EVA_DEFAULT_SCORE = 300D;

    /** The static instance of the MissionManager */
	private static MissionManager missionManager;

    public LoadVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
        addPreferredRobot(RobotType.DELIVERYBOT);
	}
    
    /**
     * Get the score for a Settlement task for a person. This considers and EVA factor for eva maintenance.
	 * @param t Task being scored
	 * @parma p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
			LoadJob mtj = (LoadJob) t;

			factor = getPersonModifier(p);
			if (mtj.eva) {
				// EVA factor is the radition and the EVA modifiers applied extra
				factor *= getRadiationModifier(p.getSettlement());
				factor *= getEVAModifier(p);
			}
		}
		return factor;
	}

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
	 * @param t Task being scored
	 * @parma r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        LoadJob mtj = (LoadJob) t;
        if (mtj.eva) {
            return 0D;
        }
        return r.getPerformanceRating();
    }

	/**
	 * Get a collection of Tasks for any mission that needs loading
	 * @param settlement Settlement to scan for vehicles
	 */
    @Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		boolean insideTasks = MaintainVehicleMeta.getGarageSpaces(settlement) > 0;
        double factor = settlement.getGoodsManager().getTransportationFactor();

        // Find all Vehcile missions with an active loading plan
		for(Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				LoadingController plan = vehicleMission.getLoadingPlan();

				// Must have a local Loading Plan that is not complete
				if ((plan != null) && plan.getSettlement().equals(settlement) && !plan.isCompleted()) {
                    SettlementTask job = createLoadJob(vehicleMission, settlement, insideTasks, factor, this);
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
     * @param modifier Score emodifier for  Task
     * @param owner 
     */
    private static SettlementTask createLoadJob(VehicleMission vehicleMission, Settlement settlement,
                                        boolean insideOnlyTasks,
                                        double modifier, SettlementMetaTask owner) {

        Vehicle vehicle = vehicleMission.getVehicle();
        if (vehicle == null)
            return null; // Should not happen

        boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
        if (insideOnlyTasks || inGarageAlready) {
            double score = GARAGE_DEFAULT_SCORE * modifier;
            if (inGarageAlready) {
                // If in Garage already then boost score
                score *= 2;
            }
            return new LoadJob(owner, vehicleMission, false, score);
        }
        return new LoadJob(owner, vehicleMission, true, EVA_DEFAULT_SCORE * modifier);
    }

    /**
     * Create the appriopriate TaskJob to load a mission. This considers whether the Vehcile is alreayd in a Garage
     * and whether there is Garage space.
     * @param vehicleMission Mission needing a load
     * @param settlement Locaiton the load is occuring
     */
    public static TaskJob createLoadJob(VehicleMission vehicleMission, Settlement settlement) {
        return createLoadJob(vehicleMission, settlement, false, 1D, null);
    } 

    /**
	 * Attached to the common controllign classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}
}
