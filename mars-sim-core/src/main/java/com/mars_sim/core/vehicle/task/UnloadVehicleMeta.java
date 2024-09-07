/*
 * Mars Simulation Project
 * UnloadVehicleMeta.java
 * @date 2024-06-17
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

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
	
    public UnloadVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
        addPreferredRobot(RobotType.DELIVERYBOT);
	}

    /**
     * Assesses the suitability of the Robot for this task. 
     * Notes that it can not do EVA tasks and will return a zero factor.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

	/**
	 * Gets a collection of Tasks for any vehicle that needs unloading.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();
  
        // Check non-mission vehicles
        for (Vehicle vehicle : settlement.getParkedGaragedVehicles()) {
            if (vehicle.haveStatusType(StatusType.UNLOADING)) {
				
   				boolean garageTask = MaintainVehicleMeta.hasGarageSpaces(
   						vehicle.getAssociatedSettlement(), vehicle instanceof Rover);
						
                SettlementTask job = createUnloadJob(settlement, vehicle, garageTask, this);

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
    private static SettlementTask createUnloadJob(Settlement settlement, Vehicle vehicle,
                                                boolean insideOnlyTasks,
                                                SettlementMetaTask owner) {
        double remaining = vehicle.getStoredMass();
        if (remaining > 0D) {
            RatingScore score = new RatingScore(BASE_SCORE);
            score.addBase("vehicle", (100D * remaining)/vehicle.getCargoCapacity());

            // TODO Do not use CommerceFactor; just use size of load
            score = applyCommerceFactor(score, settlement, CommerceType.TRANSPORT);

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
        return createUnloadJob(settlement, vehicle, false, null);
    }
}
