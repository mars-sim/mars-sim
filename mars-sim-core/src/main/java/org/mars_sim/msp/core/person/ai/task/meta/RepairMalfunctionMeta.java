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
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Repairbot;
import org.mars_sim.msp.core.structure.Settlement;

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
        	    
	        // Add probability for all malfunctionable entities in person's local.
	        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
	        while (i.hasNext()) {
	            Malfunctionable entity = i.next();
	            if (!RepairMalfunction.requiresEVA(person, entity)) {
	                MalfunctionManager manager = entity.getMalfunctionManager();
	                Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
	                while (j.hasNext()) {
	                    Malfunction malfunction = j.next();
	                    try {
	                        if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
	                            result += WEIGHT;
	                        }
	                    }
	                    catch (Exception e) {
	                        e.printStackTrace(System.err);
	                    }
	                }
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

	@Override
	public Task constructInstance(Robot robot) {
        return new RepairMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Repairbot) {
	        // Add probability for all malfunctionable entities in robot's local.
	        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
	        while (i.hasNext()) {
	            Malfunctionable entity = i.next();
	            if (!RepairMalfunction.requiresEVA(robot, entity)) {
	                MalfunctionManager manager = entity.getMalfunctionManager();
	                Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
	                while (j.hasNext()) {
	                    Malfunction malfunction = j.next();
	                    try {
	                        if (RepairMalfunction.hasRepairPartsForMalfunction(robot, malfunction)) {
	                            result += WEIGHT;
	                        }
	                    }
	                    catch (Exception e) {
	                        e.printStackTrace(System.err);
	                    }
	                }
	            }

	        }

	        // Effort-driven task modifier.
	        result *= robot.getPerformanceRating();

        }

        return result;
	}
	

	public double getProbability(Settlement settlement) {

        double result = 0D;

        // Add probability for all malfunctionable entities in robot's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            if (!RepairMalfunction.requiresEVA(entity)) {
                MalfunctionManager manager = entity.getMalfunctionManager();
                Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
                while (j.hasNext()) {
                    Malfunction malfunction = j.next();
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
        }

        return result;
	}
	
}