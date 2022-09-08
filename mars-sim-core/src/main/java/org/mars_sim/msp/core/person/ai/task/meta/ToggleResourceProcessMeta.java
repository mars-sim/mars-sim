/*
 * Mars Simulation Project
 * ToggleResourceProcessMeta.java
 * @date 2022-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.AbstractMap.SimpleEntry;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.ToggleResourceProcess;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ToggleResourceProcessMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$

	private static final int THRESHOLD = 300;
	
	private static final int CAP = 3_000;
	
    public ToggleResourceProcessMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setPreferredJob(JobType.TECHNICIAN, JobType.ENGINEER);
	}

    @Override
	public Task constructInstance(Robot robot) {
		return new ToggleResourceProcess(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		return calculateProbability(robot);
	}
	
	@Override
	public double getProbability(Person person) {
		double result = calculateProbability(person);
		return applyJobModifier(result, person);
	}	
		
	@Override
	public Task constructInstance(Person person) {
		return new ToggleResourceProcess(person);
	}

	public double calculateProbability(Worker worker) {

		double result = 0D;

		// Note: A person can now remotely toggle the resource process
		// instead of having to do an EVA outside.
		
		// Question: are there circumstances when a person still
		// has to go outside ?

		Settlement settlement = worker.getSettlement();
		
		if (settlement != null) {

			// Check if settlement has resource process override set.
			if (settlement.getProcessOverride(OverrideType.RESOURCE_PROCESS))
				return 0;
			
	        if (settlement.canRetrieveFirstResourceProcess()) {
	        	return CAP;
	        } 
	        
	        SimpleEntry<Building, SimpleEntry<ResourceProcess, Double>> entry = 
	        		ToggleResourceProcess.getResourceProcessingBuilding(worker);
	        if (entry == null)
		    	return 0;
	        
//			Building resourceProcessBuilding = entry.getKey();
			ResourceProcess process = entry.getValue().getKey();
			double score = entry.getValue().getValue();
			result = score;
						
			boolean toggleOn = true;
	
			if (result < 0) {
				// Remove the negative sign	
				result = - result;
				toggleOn = false;
			}
				
	        if (result > THRESHOLD) {
	        	settlement.addResourceProcess(entry);
	        	process.setFlag(true);
	        	process.setToggleOn(toggleOn);
	        	result = CAP;
	        }
		}
		
        if (result > CAP) {
        	result = CAP;
        }
        
		return result;
	}
}
