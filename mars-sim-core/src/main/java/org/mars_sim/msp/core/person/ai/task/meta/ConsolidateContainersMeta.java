/*
 * Mars Simulation Project
 * ConsolidateContainersMeta.java
 * @date 2023-05-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the ConsolidateContainers task. This can created shared SettlementTask
 * and invividual Person tasks when they are in a Vehicle
 */
public class ConsolidateContainersMeta extends FactoryMetaTask implements SettlementMetaTask {
    private static class ConsolidateTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

		public ConsolidateTaskJob(SettlementMetaTask owner, double score) {
			super(owner, "Consolidate containers", score);
		}

		@Override
		public Task createTask(Person person) {
			return new ConsolidateContainers(person);
		}

		@Override
		public Task createTask(Robot robot) {
			return new ConsolidateContainers(robot);
		}
	}

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$

    private static final double DEFAULT_SCORE = 100;
    
    public ConsolidateContainersMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ORGANIZATION, TaskTrait.DISCIPLINE);

        addPreferredRobot(RobotType.DELIVERYBOT);
	}

		
	/**
	 * Person do not get any individual consolidate assigned as they never go in a Vehicle
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        List<TaskJob> result = Collections.emptyList();
        if (person.isInVehicle() && person.isInside() &&
                        needsConsolidation(person.getVehicle(), false)) {
            // Create a real list
            result = new ArrayList<>();
            result.add(new ConsolidateTaskJob(this, DEFAULT_SCORE * getPersonModifier(person)));
        }
        return result;
	}
	

	/**
	 * Robots do not get any individual consolidate container assigned as they never go in a Vehicle
	 */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {
		return null;
	}
	
    /**
     * Create a task if any containers can be consolidted in the Settlement
     * @param settlement Source of Containers
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> result = Collections.emptyList();
        if (needsConsolidation(settlement, true)) {
            // Create a real list
            result = new ArrayList<>();
            result.add(new ConsolidateTaskJob(this, DEFAULT_SCORE));
        }
        return result;
    }

    @Override
    public double getPersonSettlementModifier(SettlementTask t, Person p) {
        return getPersonModifier(p);
    }

    /**
     * Score modifier for a Robot is based on it's performance rating
     */
    @Override
    public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        return r.getPerformanceRating();
    }

    /**
     * Consolidates the container's resources.
     * 
     * @param inv
     * @return
     */
    private static boolean needsConsolidation(EquipmentOwner topContainer, boolean useTopInventory) {   	        
        int partialContainers = 0;
                
        // Note: if in a vehicle, do not use main store. keep resources in containers
        for (Container e: topContainer.findAllContainers()) {
        	
            if (e.getStoredMass() > 0D) {
                // Only check one type of amount resource for container.
                int resource = e.getResource();
                // Check if this resource from this container could be loaded into the settlement/vehicle's inventory.
                if (useTopInventory && (resource > 0) 
                		&& topContainer.hasAmountResourceRemainingCapacity(resource)) {
                	return true;
                }

                // Check if container is only partially full of resource.
                if (e.hasAmountResourceRemainingCapacity(resource)) {
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
}