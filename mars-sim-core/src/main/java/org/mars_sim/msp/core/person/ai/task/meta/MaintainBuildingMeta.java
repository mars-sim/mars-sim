/*
 * Mars Simulation Project
 * MaintainBuildingMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for maintaining buildings.
 */
public class MaintainBuildingMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainBuilding"); //$NON-NLS-1$

	private static final int CAP = 3_000;
	
	private static final double FACTOR = 250;
	
    public MaintainBuildingMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new MaintainBuilding(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0D;
        
		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {
            
			result = getSettlementProbability(settlement);
			
			result = applyPersonModifier(result, person);
		}

        if (result > CAP)
        	result = CAP;
        
		return result;
	}


	public double getSettlementProbability(Settlement settlement) {
		double result = 0;
	
		for (Building building: settlement.getBuildingManager().getBuildings()) {
			
			MalfunctionManager manager = building.getMalfunctionManager();
			boolean hasNoMalfunction = !manager.hasMalfunction();
			boolean hasParts = MaintainBuilding.hasMaintenanceParts(settlement, building);
			boolean inhabitableBuilding = building.hasFunction(FunctionType.LIFE_SUPPORT);

			double condition = manager.getAdjustedCondition();
			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
			boolean minTime = (effectiveTime >= 100D);
			// Note: look for buildings that are NOT malfunction since
			// malfunctioned building are being taken care of by the two Repair*Malfunction tasks
			if (hasNoMalfunction && inhabitableBuilding && hasParts && minTime) {
				result += (100 - condition);
			}
		}
		
		result *= FACTOR;

		return result;
	}
	
	@Override
	public Task constructInstance(Robot robot) {
		return new MaintainBuilding(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		double result = 0D;

		Settlement settlement = robot.getSettlement();
		
		if (settlement != null && robot.getRobotType() == RobotType.REPAIRBOT) {
			
			result = getSettlementProbability(settlement);
			
			// Effort-driven task modifier.
			result *= FACTOR;
		}

        if (result > CAP)
        	result = CAP;
        
		return result;
	}
}
