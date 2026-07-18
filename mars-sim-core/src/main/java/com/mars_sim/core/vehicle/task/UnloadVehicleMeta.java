/*
 * Mars Simulation Project
 * UnloadVehicleMeta.java
 * @date 2025-09-21
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
import com.mars_sim.core.person.ai.task.util.AbstractTaskJob;
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
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Meta task for the UnloadVehicleGarage or UnloadVehicleEVA task.
 */
public class UnloadVehicleMeta extends MetaTask implements SettlementMetaTask {

    /**
     * Inidividual's job to unload a vehicle.
     */
    private static class IndividualJob extends AbstractTaskJob {
        private Vehicle vehicle;
        private boolean eva;

        IndividualJob(Vehicle vehicle, boolean eva, RatingScore score) {
            super("Unload " + (eva ? "via EVA " : "") + "@ " + vehicle.getName(), score);
            this.vehicle = vehicle;
            this.eva = eva;
        }

        @Override
        public Task createTask(Person person) {
            return createUnloadTask(person, vehicle, eva);
        }

        @Override
        public Task createTask(Robot robot) {
            return createUnloadTask(robot, vehicle, eva);
        }
    }

    private static class UnloadJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public UnloadJob(SettlementMetaTask ownerTask, Settlement owner, Vehicle target, boolean eva, RatingScore score) {
            super(ownerTask, owner, "Unload Vehicle", target, score);
            setEVA(eva);
        }

        @Override
        public Task createTask(Person person) {
            return createUnloadTask(person, (Vehicle) getFocus(), isEVA());
        }

        @Override
        public Task createTask(Robot robot) {
            return createUnloadTask(robot, (Vehicle) getFocus(), isEVA());
        }
    }
    
    /**
     * Factory method to create corect unload task for a person.
     * @param person Person to create task for
     * @param vehicle Vehicle to unload
     * @param eva True if task is to be done via EVA; false if in garage
     */
    private static Task createUnloadTask(Person person, Vehicle vehicle, boolean eva) {
        if (eva) {
            return new UnloadVehicleEVA(person, vehicle);
        }
        return new UnloadVehicleGarage(person, vehicle);
    }

    /**
     * Factory method to create corect unload task for a robot.
     * @param robot Robot to create task for
     * @param vehicle Vehicle to unload
     * @param eva True if task is to be done via EVA; false if in garage
     */
    private static Task createUnloadTask(Robot robot, Vehicle vehicle, boolean eva) {
        if (eva) {
			// Should not happen
			throw new IllegalStateException("Robots can not do EVA unload vehicle");
		}
        return new UnloadVehicleGarage(robot, vehicle);
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
		addAllCrewRoles();	
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
   						vehicle.getAssociatedSettlement(), vehicle);
						
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

            score = applyCommerceFactor(score, settlement, CommerceType.TRANSPORT);

            boolean inGarageAlready = LoadVehicleMeta.isInsideLoad(settlement, vehicle);
            if (insideOnlyTasks || inGarageAlready) {
                if (inGarageAlready) {
                    // If in Garage already then boost score
                    score.addModifier(GARAGED_MODIFIER, 2);
                }
                return new UnloadJob(owner, settlement, vehicle, false, score);
            }
            return new UnloadJob(owner, settlement, vehicle, true, score);    
        }

        return null;
    }

    /**
     * Creates an appropriate Unload job for a vehicle.
     */
    public static TaskJob createUnloadJob(Settlement settlement, Vehicle vehicle) {
        return new IndividualJob(vehicle, LoadVehicleMeta.isInsideLoad(settlement, vehicle), new RatingScore(BASE_SCORE)); 
    }
}
