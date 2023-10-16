/*
 * Mars Simulation Project
 * UnloadVehicleMeta.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.data.UnitSet;
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
import org.mars_sim.msp.core.person.ai.task.util.TaskProbabilityUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.tools.Msg;

/**
 * Meta task for the UnloadVehicleGarage or UnloadVehicleEVA task.
 */
public class UnloadVehicleMeta extends MetaTask implements SettlementMetaTask {

    private static class UnloadJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public UnloadJob(SettlementMetaTask owner, Vehicle target, boolean eva, RatingScore score) {
            super(owner, "Unload " + (eva ? "via EVA " : "") + "@ " + target.getName(), target, score);
            setEVA(eva);
        }

        /**
         * The vehicle being unloaded is the focus.
         */
        private Vehicle getVehicle() {
            return (Vehicle) getFocus();
        }

        @Override
        public Task createTask(Person person) {
            if (isEVA()) {
                return new UnloadVehicleEVA(person, getVehicle());
            }
            return new UnloadVehicleGarage(person, getVehicle());
        }

        @Override
        public Task createTask(Robot robot) {
            if (isEVA()) {
				// Should not happen
				throw new IllegalStateException("Robots can not do EVA unload vehicle");
			}
            return new UnloadVehicleGarage(robot, getVehicle());
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
     * For a robot can not do EVA tasks so will return a zero factor in this case.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskProbabilityUtil.assessRobot(t, r);
    }

	/**
	 * Gets a collection of Tasks for any vehicle that needs unloading.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		boolean insideTasks = MaintainVehicleMeta.getGarageSpaces(settlement) > 0;

        Set<Vehicle> assessed = new UnitSet<>();
        
        // Check Vehicle Missions first
		for (Mission mission : missionManager.getMissions()) {
			if ((mission instanceof VehicleMission vehicleMission) && !mission.isDone()) {
				if (vehicleMission.isVehicleUnloadableHere(settlement)) {
                    Vehicle v = vehicleMission.getVehicle();
                    if (v != null) {
                        // Not sure why vehicle could be null but it does happen. Race condition of vehicle
                        // being released before the mission is completed?
                        assessed.add(v);

                        SettlementTask job = scoreVehicle(settlement, v, insideTasks, this);
                        if (job != null) {
                            tasks.add(job);
                        }
                    }
                }
            }
        }

        // Check non-mission vehicles
        for (Vehicle vehicle : settlement.getParkedVehicles()) {
			if (!vehicle.isReserved() && !assessed.contains(vehicle)) {
                SettlementTask job = scoreVehicle(settlement, vehicle, insideTasks, this);
                if (job != null) {
                    tasks.add(job);
                }
			}
        }
        return tasks;
    }

	
	
    /**
     * Scores a vehicle for it's suitability to be unloaded.
     * 
     * @param settlement Location of Vehicle
     * @param vehicle Vehicle to unload
     * @param insideOnlyTasks Only do Garage inside Tasks
     * @param owner The owning metaTask
     * @return
     */
    private static SettlementTask scoreVehicle(Settlement settlement, Vehicle vehicle,
                                                boolean insideOnlyTasks,
                                                SettlementMetaTask owner) {
        double remaining = vehicle.getStoredMass();
        if (remaining > 0D) {
            RatingScore score = new RatingScore(BASE_SCORE);
            score.addBase("vehicle", (100D * remaining)/vehicle.getCargoCapacity());
            score.addModifier(GOODS_MODIFIER, settlement.getGoodsManager().getTransportationFactor());

            boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
            if (insideOnlyTasks || inGarageAlready) {
                if (inGarageAlready) {
                    // If in Garage already then boost score
                    score.addModifier(GARAGED_MODIFIER, 2);
                }
                return new UnloadJob(owner, vehicle, false, score);
            }
            return new UnloadJob(owner, vehicle, true, score);    
        }

        return null;
    }

    /**
     * Creates an appropriate Unload job for a vehicle.
     */
    public static TaskJob createUnloadJob(Settlement settlement, Vehicle vehicle) {
        return scoreVehicle(settlement, vehicle, false, null);
    }

    /**
	 * Attached to the common controlling classes.
	 */
	public static void initialiseInstances(Simulation sim) {
		missionManager = sim.getMissionManager();
	}

}
