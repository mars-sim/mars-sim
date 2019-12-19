/**
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @version 3.1.0 2017-03-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairMalfunctionMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 300D;
	
    @Override
    public String getName() {
        return NAME;
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
    						
    				if (malfunction != null) {
    					if (malfunction.areAllRepairerSlotsFilled()) {
    						return 0;
    					}
    					else if (malfunction.needEVARepair()) {
    						result += WEIGHT * malfunction.numRepairerSlotsEmpty(1)
    								+ WEIGHT * malfunction.numRepairerSlotsEmpty(0);
    					}
    				}
    				else {
    					return 0;
    				}
    				
    			}
    			else {
    				return 0;
    			}
            }
            
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);
	        }
	
	        // Modify if tinkering is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
	            result *= 1.5D;
	        }
	
	        if (result > 0D) {
	            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	        }
	
	        if (result < 0) result = 0;
        }
        
        return result;
    }


    public double getSettlementProbability(Settlement settlement) {
        double result = 0D;
        // Add probability for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
  
            MalfunctionManager manager = entity.getMalfunctionManager();
            Iterator<Malfunction> j = manager.getGeneralMalfunctions().iterator();
            while (j.hasNext()) {
                Malfunction malfunction = j.next();
                if (!malfunction.isGeneralRepairDone())
                	result += WEIGHT/4D;
                try {
                    if (RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
                        result += WEIGHT/2D;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            
            Iterator<Malfunction> k = manager.getEmergencyMalfunctions().iterator();
            while (k.hasNext()) {
                Malfunction malfunction = k.next();
                if (!malfunction.isEmergencyRepairDone())
                	result += WEIGHT/2D;
                try {
                    if (RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
                        result += WEIGHT;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return result;
    }
    
    public double computeProbability(Settlement settlement, Unit unit) {

        double result = 0D;
        // Add probability for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            
            if (unit instanceof Robot && entity.getUnit() instanceof Vehicle) {
            	// Note that currently robot cannot go outside and board a vehicle
            	continue;
            }
            
//          if (!RepairMalfunction.requiresEVA(entity)) { // do NOT use requiresEVA() since it will filter out Emergency work repair
            
            MalfunctionManager manager = entity.getMalfunctionManager();
            Iterator<Malfunction> j = manager.getGeneralMalfunctions().iterator();
            while (j.hasNext()) {
                Malfunction malfunction = j.next();
                if (!malfunction.isGeneralRepairDone())
                	result += WEIGHT/4D;
                try {
                    if (RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
                        result += WEIGHT/2D;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            
            Iterator<Malfunction> k = manager.getEmergencyMalfunctions().iterator();
            while (k.hasNext()) {
                Malfunction malfunction = k.next();
                if (!malfunction.isEmergencyRepairDone())
                	result += WEIGHT/2D;
                try {
                    if (RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
                        result += WEIGHT;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return result;
    }
    
	@Override
	public Task constructInstance(Robot robot) {
        return new RepairMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        double result = 0D;

      	result = computeProbability(robot.getSettlement(), robot);
      	
	    // Effort-driven task modifier.
	    result *= robot.getPerformanceRating();

        return result;
	}
}