/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask {

	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(ToggleResourceProcessMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$

	private static final double FACTOR = 10_000D;

    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);
	}

	@Override
	public Task constructInstance(Person person) {
		return new ToggleResourceProcess(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		// Note: A person can now remotely toggle the resource process
		// instead of having to do an EVA outside.
		// Question: are there circumstances when a person still
		// has to go outside ?
		
		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {

			// Check if settlement has resource process override set.
			if (settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS)) {
				return 0;
			}
			
			Building building = ToggleResourceProcess.getResourceProcessingBuilding(person);
			
			if (building != null) {
				ResourceProcess process = ToggleResourceProcess.getResourceProcess(building);

				String name = process.getProcessName();

				if (name.toLowerCase().contains(ResourceProcessing.SABATIER)) {
					int waterRationLevel = settlement.getWaterRationLevel();
					result += waterRationLevel;
				}

				double diff = ToggleResourceProcess.getResourcesValueDiff(settlement, process);
				
//				logger.info(building, 20_000, "@Meta " + name + " diff: " + Math.round(diff * 1000.0)/1000.0);
				
				double baseProb = diff * FACTOR;
				if (baseProb > FACTOR) {
					baseProb = FACTOR;
				}
				result += baseProb;

                if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
                    // Factor in building crowding and relationship factors.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }
                
    			double multiple = (settlement.getIndoorPeopleCount() + 1D) / (settlement.getPopulationCapacity() + 1D);
    			result *= multiple;

    			result = applyPersonModifier(result, person);
			}
		}

		return result;
	}
}
