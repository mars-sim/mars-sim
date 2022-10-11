/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.AbstractMap.SimpleEntry;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.RepairInsideMalfunction;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairInsideMalfunctionMeta extends MetaTask {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairInsideMalfunctionMeta.class.getName());
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final int WEIGHT = 300;
	
	private static final int CAP = 6000;
	
    public RepairInsideMalfunctionMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new RepairInsideMalfunction(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInside()) { 
        	    
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            if (person.isInSettlement()) {
            	result = computeProbability(person.getSettlement(), person);
            }

	        result = applyPersonModifier(result, person);
        }
        
        if (result > CAP)
        	result = CAP;
        
        return result;
    }


    private double computeProbability(Settlement settlement, Worker worker) {

		if (worker.getSettlement().canRetrieveMalfunctionPair()) {
			return CAP;
		}
    	
        double result = 0;
        double highestScore = 0;
        
        Malfunction highestScoreMalfunction = null;
        Malfunctionable highestScoreEntity = null;
 
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
        	
            if (entity instanceof Robot && entity instanceof Vehicle) {
            	// Note: robot's malfunction is not currently modeled
            	// vehicle malfunctions are handled by other meta tasks
            	continue;
            }
	
            MalfunctionManager manager = entity.getMalfunctionManager();
            
            if (manager.hasMalfunction()) {
            	// Pick the worst malfunction
            	Malfunction mal = manager.getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.INSIDE);

            	if (mal != null) {
            		highestScoreMalfunction = mal; 
            		highestScoreEntity = entity;
	            }
            	
            	else {
    	            for (Malfunction malfunction : manager.getAllInsideMalfunctions()) {
    	            	
    	            	// Since it fails to find the most serious malfunction earlier
    	            	if (mal == null) {
    	            		mal = malfunction;
    		            }
    	            	
    	            	double score = scoreMalfunction(settlement, malfunction);
    	            	if (score >= highestScore) {
    	            		highestScoreMalfunction = malfunction; 
    	            		highestScoreEntity = entity;
    	            	}
    	                result += score;
    	                
    	            }
            	}
            }
        }
        
        if (result > 0 && highestScoreMalfunction !=  null && highestScoreEntity != null) {
	        // Create the malfunction pair for storage
			SimpleEntry<Malfunction, Malfunctionable> highestScorePair = new SimpleEntry<>(highestScoreMalfunction, highestScoreEntity);
	        // Save the malfunction pair in the settlement
	        settlement.saveMalfunctionPair(highestScorePair);
	        
	        logger.info(worker, "mal: " + highestScoreMalfunction);
        }
        
        return result;
    }
    
    /**
     * Gets the initial score of the malfunction.
     * 
     * @param malfunction
     * @return
     */
    private static double scoreMalfunction(Settlement settlement, Malfunction malfunction) {    
    	double result = 0D;
		if (!malfunction.isWorkDone(MalfunctionRepairWork.INSIDE)
				&& (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE) > 0)) {
	        result = ((WEIGHT * malfunction.getSeverity()) / 100D);
	        
	        if (RepairHelper.hasRepairParts(settlement, malfunction)) {
	    		result *= 2;
	    	}
		}
    	
		return result;
    }
    
	@Override
	public Task constructInstance(Robot robot) {
        return new RepairInsideMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        double result = 0D;

        if (robot.getRobotType() == RobotType.REPAIRBOT) {
        	// Calculate probability
	      	result = computeProbability(robot.getSettlement(), robot);
		    // Effort-driven task modifier.
		    result *= robot.getPerformanceRating();
        }

        return result;
	}
}
