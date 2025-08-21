/*
 * Mars Simulation Project
 * LoadVehicleMeta.java
 * @date 2023-09-02
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
 * Meta task for both LoadVehicleEVA and LoadVehicleGarage tasks.
 */
public class LoadVehicleMeta extends MetaTask 
    implements SettlementMetaTask {
        
    private static class LoadJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

		private Vehicle vehicle;
		
        private LoadJob(SettlementMetaTask owner, Vehicle target, boolean eva, RatingScore score) {
            super(owner, "Load " + (eva ? "via EVA " : ""), target, score);
            vehicle = target;
            setEVA(eva);
        }

        @Override
        public Task createTask(Person person) {
            if (!person.isInSettlement())
            	return null;
            if (isEVA()) {
                return new LoadVehicleEVA(person, vehicle);
            }
	
    		boolean hasGarage = vehicle.isInGarage(); 
    		if (hasGarage)
    			return new LoadVehicleGarage(person, vehicle);
    			
    		boolean garageTask = MaintainVehicleMeta.hasGarageSpaces(
    				vehicle.getAssociatedSettlement(), vehicle instanceof Rover);
            
    		if (garageTask)
    			return new LoadVehicleGarage(person, vehicle);
			
    		return new LoadVehicleEVA(person, vehicle);
        }

        @Override
        public Task createTask(Robot robot) {
            if (isEVA()) {
				// Should not happen
				throw new IllegalStateException("Robots can not do EVA load vehicle");
			}
            return new LoadVehicleGarage(robot, vehicle);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicle"); //$NON-NLS-1$

    private static final double GARAGE_DEFAULT_SCORE = 500D;

    public LoadVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
        addPreferredRobot(RobotType.DELIVERYBOT);
		addAllCrewRoles();
	}

    /**
     * Gets the score for a Settlement task for a robot. Note that robots can not do EVA.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

	/**
	 * Gets a collection of Tasks for any mission that needs loading.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
    @Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

        // Find all parked Vehicles with an active loading plan
		for (var vehicle : settlement.getParkedGaragedVehicles()) {
			if (vehicle.haveStatusType(StatusType.LOADING)) {
				LoadingController plan = vehicle.getLoadingPlan();

				// Must have a local Loading Plan that is not complete
				if ((plan != null) && plan.getSettlement().equals(settlement) && !plan.isCompleted()) {
					
					boolean garageTask = MaintainVehicleMeta.hasGarageSpaces(
							settlement, vehicle instanceof Rover);
							
                    SettlementTask job = createLoadJob(vehicle, settlement, garageTask, this);
                    if (job != null) {
                        tasks.add(job);
                    }
				}
			}
        }
        return tasks;
    }

    /**
     * Creates the appropriate TaskJob to load a vehicle. This considers whether the vehicle is already in a garage.
     * and whether there is Garage space.
     * 
     * @param vehicle Vehicle to load
     * @param settlement Location the load is occurring
     * @param insideOnlyTasks Only inside tasks
     * @param owner 
     */
    private static SettlementTask createLoadJob(Vehicle vehicle, Settlement settlement,
                                        boolean insideOnlyTasks,
                                        SettlementMetaTask owner) {

        RatingScore score = new RatingScore(GARAGE_DEFAULT_SCORE);
        score = applyCommerceFactor(score, settlement, CommerceType.TRANSPORT);
        boolean inGarageAlready = settlement.getBuildingManager().isInGarage(vehicle);
        if (insideOnlyTasks || inGarageAlready) {
            if (inGarageAlready) {
                // If in Garage already then boost score
                score.addModifier(GARAGED_MODIFIER, 2);
            }
            // Note: owner can be null
            return new LoadJob(owner, vehicle, false, score);
        }
        return new LoadJob(owner, vehicle, true, score);
    }

    /**
     * Creates the appropriate TaskJob to load a Vehicle. This considers whether the vehicle is 
     * already in a garage and whether there is garage space.
     * 
     * @param vehicle Vehicle needing a load
     * @param settlement Location the load is occurring
     */
    public static TaskJob createLoadJob(Vehicle vehicle, Settlement settlement) {
    	// Question: will a null SettlementTask be causing any issues ?
        return createLoadJob(vehicle, settlement, false, null);
    } 
}
