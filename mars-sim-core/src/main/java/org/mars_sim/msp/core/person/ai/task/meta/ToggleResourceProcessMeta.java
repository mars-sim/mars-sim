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
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;

/**
 * Meta task for the ToggleResourceProcess task.
 */
public class ToggleResourceProcessMeta extends MetaTask {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ToggleResourceProcessMeta.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.toggleResourceProcess"); //$NON-NLS-1$

	private static final double FACTOR = 100;
	private static final int CAP = 6_000;
	
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
			SimpleEntry<Building, SimpleEntry<ResourceProcess, Double>> entry = ToggleResourceProcess.getResourceProcessingBuilding(worker);
			if (entry == null)
				return 0;
			Building resourceProcessBuilding = entry.getKey();
			ResourceProcess process = entry.getValue().getKey();
			double score = entry.getValue().getValue();
			result = score;
			
//			logger.info(worker, "1. " + process + "  result: " + result);
			
			if (score > 0 && process.isProcessRunning()) {
				// let it continue running
				return 0;
			}
			
			else if (score < 0 && process.isProcessRunning()) {
				// need to shut it down 
				result *= result;
			}

			else if (score > 0 && !process.isProcessRunning()) {
				// need to turn it on
				result *= result;
			}
			
			else if (score < 0 && !process.isProcessRunning()) {
				// let it continue not running
				return 0;
			}
			
//			logger.info(worker, "1.5 " + process + "  result: " + result);
			
			// Check if settlement is missing one or more of the output resources.
			if (ToggleResourceProcess.isEmptyOutputResourceInProcess(settlement, process)) {
				// will need to execute the task of toggling on this process to produce more output resources
				if (score > 0 || score < 0)
					score *= 10;
			}

//			logger.info(worker, "2. " + process + "  result: " + result);
			
			// NOTE: Need to detect if the output resource is dwindling 
			
			// Check if settlement is missing one or more of the input resources.
			if (ToggleResourceProcess.isEmptyInputResourceInProcess(settlement, process)) {
				if (process.isProcessRunning()) {
					// will need to execute the task of toggling off this process 
					score *= 10;
				} else {
					// no need to turn it on
					return 0;
				}
			}
			
//			logger.info(worker, "2.5 " + process + "  result: " + result);
		
			String name = process.getProcessName().toLowerCase();
			
			boolean sab = name.contains(ResourceProcessing.SABATIER);
			boolean reg = name.contains(ResourceProcessing.REGOLITH);
			boolean water = name.contains(ResourceProcessing.WATER);
			boolean ppa = name.contains(ResourceProcessing.PPA);
			boolean cfr = name.contains(ResourceProcessing.CFR);
			boolean ogs = name.contains(ResourceProcessing.OGS);
			
//			logger.info(worker, "3. " + resourceProcessBuilding + "   " + process + "  result: " + result);
			
			if (reg) {
				result *= settlement.getRegolithProbabilityValue();
//				logger.info(worker, "3.1 " + resourceProcessBuilding
//						+ "   " + process + "  result: " + result);
			}
			
			else if (water) {
				result *= settlement.getIceProbabilityValue();
//				logger.info(worker, "3.2 " + resourceProcessBuilding
//						+ "   " + process + "  result: " + result);
			}
			
			else if (ppa) {
				double hydrogenVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.hydrogenID);
				double methaneVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.methaneID);
				result *= hydrogenVP / methaneVP ;
			}
			
			else if (cfr) {
				double hydrogenVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.hydrogenID);
				double waterVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.waterID);
				result *= waterVP / hydrogenVP ;
			}
			
			else if (sab) {
				double hydrogenVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.hydrogenID);
				double methaneVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.methaneID);
				double waterVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.waterID);
				result *= waterVP * methaneVP / hydrogenVP;
			}

			else if (ogs) {
				double hydrogenVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.hydrogenID);
				double oxygenVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.oxygenID);
				double waterVP = settlement.getGoodsManager().getGoodValuePoint(ResourceUtil.waterID);
				result *= hydrogenVP * oxygenVP / waterVP;
			}
			
			result *= FACTOR;	
	
//			boolean toPrint = reg || sab || water || cfr || ppa || ogs;
//			
//			if (toPrint) {
//				logger.info(worker, "4. " + name + "  result: " + result);
//			}
//			
//    
//			if (toPrint) {
//				logger.info(worker, "5. " + name + "  result: " + result);
//			}
				
			// Remove the negative sign
			result = Math.abs(result);	
		}
		
        if (result > CAP)
        	result = CAP;
        
        
		return result;
	}
}
