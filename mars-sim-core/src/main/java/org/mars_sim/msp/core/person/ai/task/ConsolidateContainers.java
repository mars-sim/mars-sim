/**
 * Mars Simulation Project
 * ConsolidateContainers.java
  * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.vehicle.Rover;

/** 
 * A task for consolidating the resources stored in local containers.
 */
public class ConsolidateContainers 
extends Task 
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(ConsolidateContainers.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$
    
    /** Task phases. */
    private static final TaskPhase CONSOLIDATING = new TaskPhase(Msg.getString(
            "Task.phase.consolidating")); //$NON-NLS-1$
    
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;
    
    /** The amount of resources (kg) one person of average strength can load per millisol. */
    private static double LOAD_RATE = 20D;
    
    /** Time (millisols) duration. */
    private static final double DURATION = 30D;
    
    // Data members.
    private Inventory topInventory = null;
    
    /**
     * Constructor.
     * @param person the person performing the task.
     * @throws Exception if error constructing task.
     */
    public ConsolidateContainers(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, true, DURATION);
                
        if (person.isOutside()) {//.getTopContainerUnit() == null) {
        	endTask();
        	return;
        }
        
        else if (person.isInVehicle()) {
        	topInventory = person.getTopContainerUnit().getInventory();
            // If person is in rover, walk to passenger activity spot.
            if (person.getVehicle() instanceof Rover) {
                walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
            }
        }
        
        else if (person.isInSettlement()) {
        	topInventory = person.getTopContainerUnit().getInventory();
            // Walk to location to consolidate containers.
            walkToRandomLocation(true);
        }
        
        else {
            logger.severe("A top inventory could not be determined for consolidating containers for " + 
                    person.getName());
            endTask();
        }
        
        // Add task phase
        addPhase(CONSOLIDATING);
        setPhase(CONSOLIDATING);
    }
    
    public ConsolidateContainers(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, false, STRESS_MODIFIER, true, DURATION);
        
        if (robot.isInVehicle()) {
           	topInventory = robot.getContainerUnit().getInventory();
            // If robot is in rover, walk to passenger activity spot.
            if (robot.getVehicle() instanceof Rover) {
                walkToPassengerActivitySpotInRover((Rover) robot.getVehicle(), true);
            }
        }
        else if (robot.isInSettlement()) {
        	topInventory = robot.getContainerUnit().getInventory();
            // Walk to location to consolidate containers.
            walkToRandomLocation(false);
        }
        
        else {
            logger.severe("A top inventory could not be determined for consolidating containers for " + 
                    robot.getName());
            endTask();
        }
        
        // Add task phase
        addPhase(CONSOLIDATING);
        setPhase(CONSOLIDATING);
    }    
    
    /**
     * Checks if containers need resource consolidation at the person's location.
     * @param person the person.
     * @return true if containers need resource consolidation.
     */
    public static boolean needResourceConsolidation(Person person) {
        Inventory inv = person.getTopContainerUnit().getInventory();
        return consolidate(inv);
    }
    
    public static boolean needResourceConsolidation(Robot robot) {
        Inventory inv = robot.getTopContainerUnit().getInventory(); 
        return consolidate(inv);
    }
    
    public static boolean consolidate(Inventory inv) {   	
        boolean result = false;
        
        Set<Integer> partialResources = new HashSet<Integer>();
        
        Iterator<Equipment> i = inv.findAllContainers().iterator();
        while (i.hasNext() && !result) {
        	Equipment e = i.next();
//            if (e instanceof Container) {
                Inventory contInv = e.getInventory();
                if (!contInv.isEmpty(false)) {
                    // Only check one type of amount resource for container.
                    Integer resource = null;
                    Iterator<Integer> j = contInv.getAllARStored(false).iterator();
                    while (j.hasNext()) {
                        resource = j.next();
                    }
                    
                    if (resource != null) {
                    
                        // Check if container could be unloaded into main inventory.
                        if (inv.getAmountResourceRemainingCapacity(resource, false, false) > 0D) {
                            result = true;
                            break;
                        }

                        // Check if container is only partially full of resource.
                        if (contInv.getAmountResourceRemainingCapacity(resource, false, false) > 0D) {
                            // If another container is also partially full of resource, they can be consolidated.
                            if (partialResources.contains(resource)) {
                                result = true;
                            }
                            else {
                                partialResources.add(resource);
                            }
                        }
                    }
                }
//            }
        }
    	
    	return result;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (CONSOLIDATING.equals(getPhase())) {
            return consolidatingPhase(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Perform the consolidating phase.
     * @param time the amount of time (millisol) to perform the consolidating phase.
     * @return the amount of time (millisol) left after performing the consolidating phase.
     */
    private double consolidatingPhase(double time) {
        
        // Determine consolidation load rate.
    	int strength = 0;
    	if (person != null) {
    	   	strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);	
    	}
		else if (robot != null) {
			strength = robot.getRoboticAttributeManager().getAttribute(RoboticAttributeType.STRENGTH);
		}
        
        double strengthModifier = .1D + (strength * .018D);
        double totalAmountLoading = LOAD_RATE * strengthModifier * time;
        double remainingAmountLoading = totalAmountLoading;
        
        // Go through each container in top inventory.     
        Iterator<Equipment> i = topInventory.findAllContainers().iterator();
        while (i.hasNext() && (remainingAmountLoading > 0D)) {
        	Equipment e = i.next();
//            if (e instanceof Container) {  
                Inventory contInv = e.getInventory();
                if (!contInv.isEmpty(false)) {
                    // Only check one type of amount resource for container.
                	Integer resource = null;
                    Iterator<Integer> j = contInv.getAllARStored(false).iterator();
                    while (j.hasNext()) {
                        resource = j.next();
                    }
                    
                    if (resource != null) {
                        
                        double amount = contInv.getAmountResourceStored(resource, false);
                        
                        // Move resource in container to top inventory if possible.
                        double topRemainingCapacity = topInventory.getAmountResourceRemainingCapacity(
                                resource, false, false);
                        if (topRemainingCapacity > 0D) {
                            double loadAmount = topRemainingCapacity;
                            if (loadAmount > amount) {
                                loadAmount = amount;
                            }
                            
                            if (loadAmount > remainingAmountLoading) {
                                loadAmount = remainingAmountLoading;
                            }
                            
                            contInv.retrieveAmountResource(resource, loadAmount);
                            topInventory.storeAmountResource(resource, loadAmount, false);
                            remainingAmountLoading -= loadAmount;
                            amount -= loadAmount;
                        }
                        
                        // Check if container is full.
                        boolean isFull = (contInv.getAmountResourceRemainingCapacity(resource, false, false) == 0D);
                        if (!isFull) {
                            
                            // Go through each other container in top inventory and try to consolidate resource.
                            Iterator<Equipment> k = topInventory.findAllContainers().iterator();
                            while (k.hasNext() && (remainingAmountLoading > 0D) && (amount > 0D)) {
                            	Equipment otherUnit = k.next();
                                if (otherUnit != e) {// && (otherUnit instanceof Container)) {
                                    Inventory otherContInv = otherUnit.getInventory();
                                    double otherAmount = otherContInv.getAmountResourceStored(resource, false);
                                    if (otherAmount > 0D) {
                                        double otherRemainingCapacity = otherContInv.getAmountResourceRemainingCapacity(
                                                resource, false, false);
                                        if (otherRemainingCapacity > 0D) {
                                            double loadAmount = otherRemainingCapacity;
                                            amount = contInv.getAmountResourceStored(resource, false);
                                            
                                            if (loadAmount > amount) {
                                                loadAmount = amount;
                                            }

                                            if (loadAmount > remainingAmountLoading) {
                                                loadAmount = remainingAmountLoading;
                                            }
                                            
                                            contInv.retrieveAmountResource(resource, loadAmount);
                                            otherContInv.storeAmountResource(resource, loadAmount, false);
                                            remainingAmountLoading -= loadAmount;
                                            amount -= loadAmount;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
//            }
        }
        
        double remainingTime = (remainingAmountLoading / totalAmountLoading) * time;
        
        // If nothing has been loaded, end task.
        if (remainingAmountLoading == totalAmountLoading) {
            endTask();
        }
        
        return remainingTime;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        return new ArrayList<SkillType>(0);
    }

    @Override
    protected void addExperience(double time) {
        // Do nothing
    }
    
    @Override
    public void destroy() {
        super.destroy();
    }
}