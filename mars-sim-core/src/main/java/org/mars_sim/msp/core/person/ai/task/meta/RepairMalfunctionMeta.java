/**
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
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
public class RepairMalfunctionMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 300D;
	
    public RepairMalfunctionMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		addFavorite(FavoriteType.OPERATION);
		addFavorite(FavoriteType.TINKERING);
		addTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANIICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new RepairMalfunction(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement() || person.isInVehicle() || person.isInVehicleInGarage()) { 
        	    
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
            if (person.isInSettlement()) 
            	result = computeProbability(person.getSettlement(), person);
            else {
            	// Get the malfunctioning entity.
            	Malfunctionable entity = RepairMalfunction.getMalfunctionEntity(person);
    			
    			if (entity != null) {
    				Malfunction malfunction = RepairMalfunction.getMalfunction(person, entity);
    						
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
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            
            if (unit instanceof Robot && entity instanceof Vehicle) {
            	// Note that currently robot cannot go outside and board a vehicle
            	continue;
            }
            
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasMalfunction()) {
	            Iterator<Malfunction> j = manager.getGeneralMalfunctions().iterator();
	            while (j.hasNext()) {
	                Malfunction malfunction = j.next();
	                double initialResult = scoreMalfunction(malfunction);
	                if ((initialResult > 0) &&
	                		RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
	                	initialResult += WEIGHT;
	                }
	                
	                result += initialResult;
	            }
	            
	            Iterator<Malfunction> k = manager.getEmergencyMalfunctions().iterator();
	            while (k.hasNext()) {
	                Malfunction malfunction = k.next();
	                double initialResult = scoreMalfunction(malfunction);
	                if ((initialResult > 0) &&
	                		RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
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
    	boolean available = false;
    
    	// Emergency work takes precedence; must be complete first
        if (!malfunction.isWorkDone(MalfunctionRepairWork.EMERGENCY)) {
        	available = (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.EMERGENCY) > 0);
        }
        else if (!malfunction.isWorkDone(MalfunctionRepairWork.GENERAL)
        	&& (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.GENERAL) > 0)) {
        	available = true;
        }
	
        return (available ? WEIGHT + ((WEIGHT * malfunction.getSeverity()) / 100D)
        				: 0D);
    }
    
	@Override
	public Task constructInstance(Robot robot) {
        return new RepairMalfunction(robot);
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
