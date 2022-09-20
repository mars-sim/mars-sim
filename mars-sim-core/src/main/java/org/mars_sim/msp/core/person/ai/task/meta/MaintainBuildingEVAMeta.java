/*
 * Mars Simulation Project
 * MaintainBuildingEVAMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.MaintainBuildingEVA;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for the MaintainBuildingEVA task.
 */
public class MaintainBuildingEVAMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainBuildingEVA"); //$NON-NLS-1$

	private static final int CAP = 3_000;
	
	private static final double FACTOR = 400;
	
    public MaintainBuildingEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new MaintainBuildingEVA(person);
    }

    @Override
    public double getProbability(Person person) {

    	Settlement settlement = person.getSettlement();
        
        if (settlement == null)
        	return 0;
        	  	
        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
    		return 0;

        // Check if it is night time.
        if (EVAOperation.isGettingDark(person))
        	return 0;

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	return 0;
        
        // Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;
        	
    	// Check for radiation events
    	boolean[] exposed = settlement.getExposed();

		if (exposed[2]) {// SEP can give lethal dose of radiation
			return 0;
		}
		
		double result = getSettlementProbability(settlement);

        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
            result *= 2D;
        
        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
        
        // Encourage to get this task done early in a work shift
        result *= shiftBonus / 10;
        
        result = applyPersonModifier(result, person);

    	if (exposed[0]) {
			result = result/2D;// Baseline can give a fair amount dose of radiation
		}

    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
			result = result/4D;
		}

        if (result < 0) result = 0;

        if (result > CAP)
        	result = CAP;
        
        return result;
    }

	public double getSettlementProbability(Settlement settlement) {
		double result = 0D;

		for (Building building: settlement.getBuildingManager().getBuildings()) {
			
			MalfunctionManager manager = building.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			boolean hasParts = MaintainBuilding.hasMaintenanceParts(settlement, building);
			boolean uninhabitableBuilding = !building.hasFunction(FunctionType.LIFE_SUPPORT);
			
			double condition = manager.getAdjustedCondition();
			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
			boolean minTime = (effectiveTime >= 100D);
			
			if (!hasMalfunction && uninhabitableBuilding && hasParts && minTime) {
				result += (100 - condition);
			}
		}
		
		result *= FACTOR;
	
		return result;
	}
}
