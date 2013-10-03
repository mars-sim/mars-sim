/**
 * Mars Simulation Project
 * ConsolidateContainers.java
 * @version 3.06 2013-10-02
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
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;

/** 
 * A task for consolidating the resources stored in local containers.
 */
public class ConsolidateContainers extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(ConsolidateContainers.class.getName());
    
    // Task phase
    private static final String CONSOLIDATING = "Consolidating Containers";
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = -.1D;
    
    // The amount of resources (kg) one person of average strength can load per millisol.
    private static double LOAD_RATE = 20D;
    
    // Time (millisols) duration.
    private static final double DURATION = 50D;
    
    // Data members.
    private Inventory topInventory = null;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing task.
     */
    public ConsolidateContainers(Person person) {
        // Use Task constructor
        super("Consolidate Containers", person, true, false, STRESS_MODIFIER, true, DURATION);
        
        // Initialize data members
        setDescription("Consolidating containers");
        
        topInventory = person.getTopContainerUnit().getInventory();
        if (topInventory == null) {
            logger.severe("A top inventory could not be determined for consolidating containers for " + 
                    person.getName());
            endTask();
        }
        
        // Add task phase
        addPhase(CONSOLIDATING);
        setPhase(CONSOLIDATING);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        if (Person.INSETTLEMENT.equals(person.getLocationSituation()) || 
                Person.INVEHICLE.equals(person.getLocationSituation())) {
        
            // Check if there are local containers that need resource consolidation.
            if (needResourceConsolidation(person)) {
                result = 10D;
            }
        
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
        }

        return result;
    }
    
    /**
     * Checks if containers need resource consolidation at the person's location.
     * @param person the person.
     * @return true if containers need resource consolidation.
     */
    private static boolean needResourceConsolidation(Person person) {
        boolean result = false;
        
        Set<AmountResource> partialResources = new HashSet<AmountResource>();
        Inventory inv = person.getTopContainerUnit().getInventory();
        Iterator<Unit> i = inv.getContainedUnits().iterator();
        while (i.hasNext() && !result) {
            Unit unit = i.next();
            if (unit instanceof Container) {
                Inventory contInv = unit.getInventory();
                if (!contInv.isEmpty(false)) {
                    // Only check one type of amount resource for container.
                    AmountResource resource = null;
                    Iterator<AmountResource> j = contInv.getAllAmountResourcesStored(false).iterator();
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
            }
        }
        
        return result;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        if (CONSOLIDATING.equals(getPhase())) {
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
        int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
        double strengthModifier = .1D + (strength * .018D);
        double totalAmountLoading = LOAD_RATE * strengthModifier * time;
        double remainingAmountLoading = totalAmountLoading;
        
        // Go through each container in top inventory.
        Iterator<Unit> i = topInventory.getContainedUnits().iterator();
        while (i.hasNext() && (remainingAmountLoading > 0D)) {
            Unit unit = i.next();
            if (unit instanceof Container) {
                Inventory contInv = unit.getInventory();
                if (!contInv.isEmpty(false)) {
                    // Only check one type of amount resource for container.
                    AmountResource resource = null;
                    Iterator<AmountResource> j = contInv.getAllAmountResourcesStored(false).iterator();
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
                            Iterator<Unit> k = topInventory.getContainedUnits().iterator();
                            while (k.hasNext() && (remainingAmountLoading > 0D) && (amount > 0D)) {
                                Unit otherUnit = k.next();
                                if ((otherUnit != unit) && (otherUnit instanceof Container)) {
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
            }
        }
        
        double remainingTime = (remainingAmountLoading / totalAmountLoading) * time;
        
        return remainingTime;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<String> getAssociatedSkills() {
        return new ArrayList<String>(0);
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