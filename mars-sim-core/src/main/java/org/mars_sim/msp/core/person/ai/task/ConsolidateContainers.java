/*
 * Mars Simulation Project
 * ConsolidateContainers.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

/** 
 * A task for consolidating the resources stored in local containers.
 */
public class ConsolidateContainers 
extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(ConsolidateContainers.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$
    
    /** Task phases. */
    private static final TaskPhase CONSOLIDATING = new TaskPhase(Msg.getString(
            "Task.phase.consolidating")); //$NON-NLS-1$
    
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;
    
    /** The amount of resources (kg) one person of average strength can load per millisol. */
    private static final double LOAD_RATE = 20D;
    
    /** Time (millisols) duration. */
    private static final double DURATION = 30D;
    
    /**
     * Constructor.
     * @param person the person performing the task.
     * @throws Exception if error constructing task.
     */
    public ConsolidateContainers(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, DURATION);
                
        if (person.isOutside()) {
        	endTask();
        	return;
        }
        
        else if (person.isInVehicle()) {
            // If person is in rover, walk to passenger activity spot.
            if (person.getVehicle() instanceof Rover) {
                walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
            }
        }
        
        else if (person.isInSettlement()) {
        	Building storage = person.getSettlement().getBuildingManager().getABuilding(FunctionType.STORAGE);
        	walkToActivitySpotInBuilding(storage, FunctionType.STORAGE, true);
        }
        
        else {
            logger.severe(person, "A top inventory could not be determined for consolidating containers");
            endTask();
        }
        
        // Add task phase
        addPhase(CONSOLIDATING);
        setPhase(CONSOLIDATING);
    }
    
    public ConsolidateContainers(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, false, STRESS_MODIFIER, DURATION);
        
        if (robot.isInVehicle()) {
            // If robot is in rover, walk to passenger activity spot.
            if (robot.getVehicle() instanceof Rover) {
                walkToPassengerActivitySpotInRover((Rover) robot.getVehicle(), true);
            }
        }
        else if (robot.isInSettlement()) {
        	Building storage = robot.getSettlement().getBuildingManager().getABuilding(FunctionType.STORAGE);
        	walkToActivitySpotInBuilding(storage, FunctionType.STORAGE, true);
        }
        
        else {
            logger.severe(robot, "A top inventory could not be determined for consolidating containers");
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
    public static boolean needResourceConsolidation(Worker person) {
    	Unit container = person.getTopContainerUnit();
        return needsConsolidation(container);
    }
    
    /**
     * Consolidate the container's resources
     * 
     * @param inv
     * @return
     */
    private static boolean needsConsolidation(Unit container) {   	        
        int partialContainers = 0;
        
        boolean useTopInventory = container.getUnitType() == UnitType.SETTLEMENT;
        
        // In Vehciles do not use main store; keep in Containers
        for (Container e: ((EquipmentOwner)container).findAllContainers()) {
            if (e.getStoredMass() > 0D) {
                // Only check one type of amount resource for container.
                int resource = e.getResource();
                // Check if this resource from this container could be loaded into the settlement/vehicle's inventory.
                if (useTopInventory && (resource > 0) && ((EquipmentOwner)container).getAmountResourceRemainingCapacity(resource) > 0D) {
                	return true;
                }

                // Check if container is only partially full of resource.
                if (e.getAmountResourceRemainingCapacity(resource) > 0D) {
                    // If another container is also partially full of resource, they can be consolidated.
                	partialContainers++;
                    if (partialContainers > 2) {
                    	// Need at least 3 containers
                        return true;
                    }
                }
            }
        }
    	
    	return false;
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
    	EquipmentOwner parent = (EquipmentOwner)(worker.getContainerUnit());
    	boolean useTopInventory = worker.isInSettlement();
    	
        // Determine consolidation load rate.
    	int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);	
        
        double strengthModifier = .1D + (strength * .018D);
        double totalAmountLoading = LOAD_RATE * strengthModifier * time;
        double remainingAmountLoading = totalAmountLoading;
        
        // Go through each container in top inventory.   
        for (Container source: parent.findAllContainers()) {
        	int resourceID = source.getResource();
            if (resourceID != -1) {
            	// resourceID = -1 means the container has not been initialized
                double sourceAmount = source.getAmountResourceStored(resourceID);
                if (sourceAmount > 0D) {
	                // Move resource in container to top inventory if possible.
	                double topRemainingCapacity = parent.getAmountResourceRemainingCapacity(resourceID);
	                if (useTopInventory && (topRemainingCapacity > 0D)) {
                        double loadAmount = transferResource(source, sourceAmount, resourceID,
                                                             topRemainingCapacity,
                                                             parent, topRemainingCapacity);
	                   
	                    remainingAmountLoading -= loadAmount;
	                    sourceAmount -= loadAmount;
	                    if (remainingAmountLoading <= 0D) {
	                    	return 0D;
	                    }
	                }
	                
	                // Check if container is empty.
	                if (sourceAmount > 0D) {
	                    // Go through each other container in top inventory and try to consolidate resource.
	                    Iterator<Container> k = parent.findAllContainers().iterator();
	                    while (k.hasNext() && (remainingAmountLoading > 0D) && (sourceAmount > 0D)) {
	                    	Container otherUnit = k.next();
	                        if (otherUnit != source && otherUnit instanceof Container) {
	                            double otherAmount = otherUnit.getAmountResourceStored(resourceID);
	                            if (otherAmount > 0D) {
	                                double otherRemainingCapacity = otherUnit.getAmountResourceRemainingCapacity(resourceID);
	                                if (otherRemainingCapacity > 0D) {
                                        double loadAmount = transferResource(source, sourceAmount, resourceID,
                                                                             remainingAmountLoading,
                                                                             otherUnit, otherRemainingCapacity);

	                                    remainingAmountLoading -= loadAmount;
	                                    sourceAmount -= loadAmount;
	            	                    if (remainingAmountLoading <= 0D) {
                                            return 0D;
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
        
        // If nothing has been loaded, end task.
        if (remainingAmountLoading == totalAmountLoading) {
            endTask();
        }
        
        return remainingTime;
    }

    /**
     * Transfer resource from a Container into a parent holder. 
     * @param source Source container
     * @param sourceAmount Amount availble in the source
     * @param sourceResource Resource being transferred
     * @param transferAmount Maximum amount to be transferred
     * @param target Target resource holder
     * @param targetCapacity Capacity in the target
     * @return
     */
    private static double transferResource(Container source, double sourceAmount, int sourceResource,
                                           double transferAmount,
                                           ResourceHolder target, double targetCapacity) {
       double loadAmount = targetCapacity;
        if (loadAmount > sourceAmount) {
            loadAmount = sourceAmount;
        }
        
        if (loadAmount > transferAmount) {
            loadAmount = transferAmount;
        }
        
        source.retrieveAmountResource(sourceResource, loadAmount);
        if (source.getStoredMass() == 0) {
            source.clean();
        }
        target.storeAmountResource(sourceResource, loadAmount);
        return loadAmount;
    }
}
