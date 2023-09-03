/*
 * Mars Simulation Project
 * LoadVehicleMeta.java
 * @date 2023-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
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
 * Meta task for both LoadVehicleEVA and LoadVehicleGarage tasks.
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
            if (!person.isInSettlement())
            	return null;
            if (eva) {
                return new LoadVehicleEVA(person, target);
            }
            return new LoadVehicleGarage(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            if (eva) {
				// Should not happen
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
     * Gets the score for a Settlement task for a person. If EVA is needed, considers radiation.
     * 
	 * @param t Task being scored
	 * @param p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
			LoadJob mtj = (LoadJob) t;

			factor = getPersonModifier(p);
			if (mtj.eva) {
				// EVA factor is the radiation and the EVA modifiers applied extra
				factor *= getRadiationModifier(p.getSettlement());
				factor *= getEVAModifier(p);
			}
		}
		return factor;
	}

    /**
     * Gets the score for a Settlement task for a robot. Note that robots can not do EVA.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work
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
	 * Gets a collection of Tasks for any mission that needs loading.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
    @Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		boolean insideTasks = MaintainVehicleMeta.getGarageSpaces(settlement) > 0;
        double factor = settlement.getGoodsManager().getTransportationFactor();

        // Find all Vehicle missions with an active loading plan
		for (Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission vehicleMission) {
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
     * Creates the appropriate TaskJob to load a mission. This considers whether the vehicle is already in a garage.
     * and whether there is Garage space.
     * 
     * @param vehicleMission Mission needing a load
     * @param settlement Location the load is occurring
     * @param insideOnlyTasks Only inside tasks
     * @param modifier Score modifier for task
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
     * Creates the appropriate TaskJob to load a mission. This considers whether the vehicle is 
     * already in a garage and whether there is garage space.
     * 
     * @param vehicleMission Mission needing a load
     * @param settlement Location the load is occurring
     */
    public static TaskJob createLoadJob(VehicleMission vehicleMission, Settlement settlement) {
        return createLoadJob(vehicleMission, settlement, false, 1D, null);
    } 

    /**
	 * Attached to the common controlling classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}
}
