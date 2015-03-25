/**
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @version 3.07 2014-12-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.Repairbot;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;

public class RepairEVAMalfunctionMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairEVAMalfunction"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new RepairEVAMalfunction(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        
        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
        } 
        
        if (result != 0 )  {
	        
	        // Add probability for all malfunctionable entities in person's local.
	        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
	        while (i.hasNext()) {
	            Malfunctionable entity = i.next();
	            MalfunctionManager manager = entity.getMalfunctionManager();
	            
	            // Check if entity has any EVA malfunctions.
	            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
	            while (j.hasNext()) {
	                Malfunction malfunction = j.next();
	                try {
	                    if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), 
	                            malfunction)) {
	                        result += 100D;
	                        
	                        if (person.getFavorite().getFavoriteActivity().equals("Repair"))
	                        	result += 25D;
	                  
	                    }
	                }
	                catch (Exception e) {
	                    e.printStackTrace(System.err);
	                }
	            }
	            
	            // Check if entity requires an EVA and has any normal malfunctions.
	            if (RepairEVAMalfunction.requiresEVA(person, entity)) {
	                Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
	                while (k.hasNext()) {
	                    Malfunction malfunction = k.next();
	                    try {
	                        if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
	                            result += 100D;
	                            
		                        if (person.getFavorite().getFavoriteActivity().equals("Tinkering"))
		                        	result += 25D;
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
	
	        // Job modifier if not in vehicle.
	        Job job = person.getMind().getJob();
	        if ((job != null)) {
	            result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);        
	        }

        }
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new RepairEVAMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
	      
        double result = 0D;
        
        if (robot.getBotMind().getRobotJob() instanceof Repairbot) {
	        
	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(robot) == null) 
	            result = 0D;
	           	
	        // Check if it is night time.
	        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	        if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0) {
	            if (!surface.inDarkPolarRegion(robot.getCoordinates())) {
	                result = 0D;
	            }
	        } 
	        
	        if (result != 0 )  {// if task penalty is not zero
	        	
		        // Add probability for all malfunctionable entities in person's local.
		        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
		        while (i.hasNext()) {
		            Malfunctionable entity = i.next();
		            MalfunctionManager manager = entity.getMalfunctionManager();
		            
		            // Check if entity has any EVA malfunctions.
		            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
		            while (j.hasNext()) {
		                Malfunction malfunction = j.next();
		                try {
		                    if (RepairEVAMalfunction.hasRepairPartsForMalfunction(robot, robot.getTopContainerUnit(), 
		                            malfunction)) {
		                        result += 100D;
		                    }
		                }
		                catch (Exception e) {
		                    e.printStackTrace(System.err);
		                }
		            }
		            
		            // Check if entity requires an EVA and has any normal malfunctions.
		            if (RepairEVAMalfunction.requiresEVA(robot, entity)) {
		                Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
		                while (k.hasNext()) {
		                    Malfunction malfunction = k.next();
		                    try {
		                        if (RepairMalfunction.hasRepairPartsForMalfunction(robot, malfunction)) {
		                            result += 100D;
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
	        
        }
        return result;
	}
}