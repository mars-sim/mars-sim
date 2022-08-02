/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.RepairInsideMalfunction;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairInsideMalfunctionMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 300D;
	
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

        if (person.isInSettlement() || person.isInVehicle() || person.isInVehicleInGarage()) { 
        	    
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            if (person.isInSettlement()) 
            	result = computeProbability(person.getSettlement(), person);
            else {
            	// Get the malfunctioning entity.
            	Malfunctionable entity = RepairInsideMalfunction.getMalfunctionEntity(person);
    			
    			if (entity != null) {
    				Malfunction malfunction = RepairInsideMalfunction.getMalfunction(person, entity);
    						
    				if (malfunction == null) {
    						return 0;
    				}
    				result = scoreMalfunction(malfunction);
    			}
    			else {
    				// No entity at fault
    				return 0;
    			}
            }
            
	        result = applyPersonModifier(result, person);
        }
        
        return result;
    }


    private double computeProbability(Settlement settlement, Unit unit) {

        double result = 0D;
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
        	
            if (unit instanceof Robot && entity instanceof Vehicle) {
            	// Note that currently robot cannot go outside and board a vehicle
            	continue;
            }
            
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasMalfunction()) {
	            for(Malfunction malfunction : manager.getAllInsideMalfunctions()) {
	                double initialResult = scoreMalfunction(malfunction);
	                if ((initialResult > 0) &&
	                		RepairHelper.hasRepairParts(settlement, malfunction)) {
	                	initialResult += WEIGHT;
	                }
	                
	                result += initialResult;
	            }
            }
        }
        return result;
    }
    
    /**
     * Get the initial score of the malfunction.
     * @param malfunction
     * @return
     */
    private static double scoreMalfunction(Malfunction malfunction) {    
    	double result = 0D;
		if (!malfunction.isWorkDone(MalfunctionRepairWork.INSIDE)
				&& (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE) > 0)) {
	        result = WEIGHT + ((WEIGHT * malfunction.getSeverity()) / 100D);
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
