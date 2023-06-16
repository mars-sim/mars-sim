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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
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
 * Meta task for the UnloadVehicleGarage task.
 */
public class UnloadVehicleMeta extends MetaTask implements SettlementMetaTask {
    private static class UnloadJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private Vehicle target;
        private boolean eva;

        public UnloadJob(SettlementMetaTask owner, Vehicle target, boolean eva, double score) {
            super(owner, "Unload " + (eva ? "via EVA " : "") + "@ " + target.getName(), score);
            this.target = target;
            this.eva = eva;
        }

        @Override
        public Task createTask(Person person) {
            if (eva) {
                return new UnloadVehicleEVA(person, target);
            }
            return new UnloadVehicleGarage(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            if (eva) {
				// SHould not happen
				throw new IllegalStateException("Robots can not do EVA unload vehicle");
			}
            return new UnloadVehicleGarage(robot, target);
        }
    }
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.unloadVehicle"); //$NON-NLS-1$

    private static final double BASE_SCORE = 300D;

    private static MissionManager missionManager;
	
    public UnloadVehicleMeta() {
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
			UnloadJob mtj = (UnloadJob) t;

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
        UnloadJob mtj = (UnloadJob) t;
        if (mtj.eva) {
            return 0D;
        }
        return r.getPerformanceRating();
    }

	/**
	 * Get a collection of Tasks for any vehicle that needs unloading
	 * @param settlement Settlement to scan for vehicles
	 */
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		boolean insideTasks = MaintainVehicleMeta.getGarageSpaces(settlement) > 0;
        double modifier = settlement.getGoodsManager().getTransportationFactor();

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

                        SettlementTask job = scoreVehicle(settlement, v, insideTasks, modifier, this);
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
                SettlementTask job = scoreVehicle(settlement, vehicle, insideTasks, modifier, this);
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
     * @param modifier Modifier for inside task score
     * @return
     */
    private static SettlementTask scoreVehicle(Settlement settlement, Vehicle vehicle, boolean insideOnlyTasks,
                                double modifier, SettlementMetaTask owner) {
        double remaining = vehicle.getStoredMass();
        if (remaining > 0D) {
            double score = BASE_SCORE + (100D * remaining)/vehicle.getCargoCapacity();
        
            boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
            if (insideOnlyTasks || inGarageAlready) {
                score *= modifier;
                if (inGarageAlready) {
                    // If in Garage already then boost score
                    score *= 2;
                }
                return new UnloadJob(owner, vehicle, false, score);
            }
            return new UnloadJob(owner, vehicle, true, score * modifier);    
        }

        return null;
    }

    /**
     * Create an appropriate Unload job for a vehicle.
     */
    public static TaskJob createUnloadJob(Settlement settlement, Vehicle vehicle) {
        return scoreVehicle(settlement, vehicle, false, 1D, null);
    }

    /**
	 * Attached to the common controllign classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}

}
