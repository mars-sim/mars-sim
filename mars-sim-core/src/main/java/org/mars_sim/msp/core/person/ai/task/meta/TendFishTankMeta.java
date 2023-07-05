/*
 * Mars Simulation Project
 * TendFishTankMeta.java
 * @date 2022-08-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendFishTank;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;

/**
 * Meta task for the Tend Fish Tank task.
 */
public class TendFishTankMeta extends MetaTask implements SettlementMetaTask {

    /**
     * Represents a Job needed in a Fishery
     */
    private static class FishTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private Fishery tank;

        public FishTaskJob(SettlementMetaTask owner, Fishery tank, double score) {
            super(owner, "Tend fishtank @ " + tank.getBuilding().getName(), score);
            this.tank = tank;
        }

        @Override
        public Task createTask(Person person) {
            return new TendFishTank(person, tank);
        }

        @Override
        public Task createTask(Robot robot) {
            return new TendFishTank(robot, tank);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendFishTank"); //$NON-NLS-1$
	
    public TendFishTankMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
        
        addPreferredRobot(RobotType.GARDENBOT);
	}

    
    /**
     * Get the score for a Settlement task for a person. THis considers the number of Person farmers
     * and any personal preferences.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
            factor = 1D;
            Building b = ((FishTaskJob)t).tank.getBuilding();

            // Crowding modifier.
            factor *= getBuildingModifier(b, p);

            factor *= getPersonModifier(p);
		}
		return factor;
	}

    /**
     * For a robot the over crowding probability is considered
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        
        // Crowding modifier.
        return r.getPerformanceRating();

    }

    /**
     * Scan the settlement tanks for any that need tending. CReate one task per applicable Fishery function.
     * @param settlement Source to scan
     * @return List of applicable tasks
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();

        List<Building> buildings = settlement.getBuildingManager().getBuildings(FunctionType.FISHERY);
        for(Building building : buildings) {
            Fishery fishTank = building.getFishery();
            double result = (fishTank.getUncleaned().size() + fishTank.getUninspected().size()) *3D;

            result += (fishTank.getSurplusStock() * 10D);
            result += fishTank.getWeedDemand();
            

            
            if (result > 0) {
                tasks.add(new FishTaskJob(this, fishTank, result));
            }
        }

        return tasks;
    }
}
