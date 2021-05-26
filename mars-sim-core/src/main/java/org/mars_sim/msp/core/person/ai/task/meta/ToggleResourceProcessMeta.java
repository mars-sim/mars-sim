/**
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$

	private static final double FACTOR = 10_000D;
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		addFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN);

	}

	@Override
	public Task constructInstance(Person person) {
		return new ToggleResourceProcess(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		// A person can remotely toggle the resource process.

		if (person.isInSettlement()) {

	        // Probability affected by the person's stress and fatigue.
	        PhysicalCondition condition = person.getPhysicalCondition();
	        double fatigue = condition.getFatigue();
	        double stress = condition.getStress();
	        double hunger = condition.getHunger();
	        
	        if (fatigue > 1000 || stress > 50 || hunger > 500)
	        	return 0;
	        
			Settlement settlement = person.getSettlement();
			// TODO: need to consider if a person is out there on Mars somewhere, out of the
			// settlement
			// and if he has to do a EVA to repair a broken vehicle.

			// Check for radiation events
//	     	boolean[] exposed = settlement.getExposed();
//		
//	 		if (exposed[2])
//	 			// SEP can give lethal dose of radiation, out won't go outside
//	             return 0;
//	 
//	            // Check if an airlock is available
//	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
//	    		return 0;
//
//            // Check if it is night time.
//            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
//            if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
//                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
//                    return 0;
//                }
//            }
//
//            boolean isEVA = false;

			// Check if settlement has resource process override set.
			if (!settlement.getResourceProcessOverride()) {
				Building building = ToggleResourceProcess.getResourceProcessingBuilding(person);
				if (building != null) {
					ResourceProcess process = ToggleResourceProcess.getResourceProcess(building);					
//                        isEVA = !building.hasFunction(FunctionType.LIFE_SUPPORT);
					double diff = ToggleResourceProcess.getResourcesValueDiff(settlement, process);
					double baseProb = diff * FACTOR;
					if (baseProb > 100D) {
						baseProb = 100D;
					}
					result += baseProb;
				}
			}

			double multiple = (settlement.getIndoorPeopleCount() + 1D) / (settlement.getPopulationCapacity() + 1D);
			result *= multiple;

			result = applyPersonModifier(result, person);
		}

		return result;
	}
}
