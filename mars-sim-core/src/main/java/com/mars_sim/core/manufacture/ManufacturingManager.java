package com.mars_sim.core.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This manages the manufacturing and salvage processes of a Settlement.
 * It maintains a queue of processes that are awaiting processes.
 */
public class ManufacturingManager implements Serializable {

    private static final long serialVersionUID = 1L;

    public class QueuedProcess implements Serializable {
        private static final long serialVersionUID = 1L;

        private ProcessInfo info;
        private Salvagable target;
        private int priority;
        private boolean resourcesAvailable;

        private QueuedProcess(ProcessInfo info, Salvagable target, int priority,
                            boolean resourcesAvailable) {
            this.info = info;
            this.target = target;
            this.priority = priority;
            this.resourcesAvailable = resourcesAvailable;
        }

        public ProcessInfo getInfo() {
            return info;
        }

        public Salvagable getTarget() {
            return target;
        }

        /**
         * Change the priority of this queued process
         * @param newPri
         */
        public void setPriority(int newPri) {
            priority = newPri;
        }

        public int getPriority() {
            return priority;
        }

        private void setResourcesAvailable(boolean avail) {
            resourcesAvailable = avail;
        }

        public boolean isResourcesAvailable() {
            return resourcesAvailable;
        }
    }

    private class UpdateEvent implements ScheduledEventHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public String getEventDescription() {
            return "Update the Manufacturing queue";
        }

        @Override
        public int execute(MarsTime currentTime) {
            updateQueue();

            return 1000; // TODO; this needs to adjust to the every morning hours of the owner Settlement
        }

    }
	private static SimLogger logger = SimLogger.getLogger(ManufacturingManager.class.getName());

    private List<QueuedProcess> queue;
    private Settlement owner;

    public ManufacturingManager(Settlement owner) {
        this.owner = owner;
        this.queue = new ArrayList<>();

        // Set up the future event to build queue every day.
        // First event is slightly in the future
        var futures = owner.getFutureManager();

        futures.addEvent(10, new UpdateEvent());
    }

    /**
     * The queued processes waiting for resources/capacity
     * @return
     */
    public List<QueuedProcess> getQueue() {
        return queue;
    }

    /**
     * Claim the nxt process on the queue that matches a tech level.
     * @param techLevel Maximum tech level of process
     * @param skillLevel Maximum skill level of worker
     * @return
     */
    public QueuedProcess claimNextProcess(int techLevel, int skillLevel) {     
        // Update the available resource status of everything queued
        updateQueueItems();

        // Find startable process grouped by prioirity
        var startableByPri = queue.stream()
                        .filter(q -> (q.getInfo().getTechLevelRequired() <= techLevel)
                                        && (q.getInfo().getSkillLevelRequired() <= skillLevel)
                                        && q.isResourcesAvailable())
                        .collect(Collectors.groupingBy(QueuedProcess::getPriority));

        if (startableByPri.isEmpty()) {
            return null;
        }

        // Select random task from top priority
        int highest = startableByPri.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        var selected = RandomUtil.getRandomElement(startableByPri.get(highest));

        // Remove as it's been claimed
        if (selected != null) {
            queue.remove(selected);
            if (queue.isEmpty()) {
                // Repopulate the queue
                updateQueue();
            }
        }

        return selected;
    }

    /**
     * Add a manufacturing process to the queue for later processing.
     * @param newProcess Process definition to add
     */
    public void addManufacturing(ManufactureProcessInfo newProcess) {
        var available = newProcess.isResourcesAvailable(owner);
        var newItem = new QueuedProcess(newProcess, null, 1, available);
        synchronized(queue) {
            queue.add(newItem);
        }
        logger.info(owner, "Added new ManuProcess to queue " + newProcess.getName());

    }

    /**
     * Add a salvage  process to the queue for later processing.
     * @param newProcess Process definition to add
     * @param target Item to salvage in this process
     */
    public void addSalvage(SalvageProcessInfo newProcess, Salvagable target) {
        var available = newProcess.isResourcesAvailable(owner);
        var newItem = new QueuedProcess(newProcess, target, 1, available);
        synchronized(queue) {
            queue.add(newItem);
        }   
    }

    /**
     * Check which queued processes have available resources
     */
    private void updateQueueItems() {
        // Check resoruces on queue
        for(var q : queue) {
            var ready = q.getInfo().isResourcesAvailable(owner);
            q.setResourcesAvailable(ready);
        }   
    }

    /**
     * Find any manufacturing proesses that can be added to the queue
     */
    void updateQueue() {
        // Update the resources available flag on existing queue
        updateQueueItems();

        // Add new queue items if automatic enabled
        if (!owner.getProcessOverride(OverrideType.MANUFACTURE)) {
            int added = 0;
            // Auto select processes to add based on value
            if (added > 0)
                logger.info(owner, "Automatically added ManuProcesses: added " + added);
        }
    }

    /**
     * Get the lowest tech level of queued items
     * @return
     */
    public int getLowestOnQueue() {
        return queue.stream()
            .mapToInt(q -> q.getInfo().getTechLevelRequired())
            .min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Get a list of Manufacturing processes that can be queued.
     * There is no process of the type already queue and workers with the skill present.
     * @return
     */
    public List<ManufactureProcessInfo> getQueuableManuProcesses() {

		int highestSkillLevel = owner.getAllAssociatedPeople().stream()
			.map(Person::getSkillManager)
			.map(sm -> sm.getSkillLevel(SkillType.MATERIALS_SCIENCE))
			.mapToInt(v -> v)
			.max().orElse(-1);

		// Get skill for robots
		int highestRobotSkillLevel = owner.getAllAssociatedRobots().stream()
			.map(Robot::getSkillManager)
			.map(sm -> sm.getSkillLevel(SkillType.MATERIALS_SCIENCE))
			.mapToInt(v -> v)
			.max().orElse(-1);
		highestSkillLevel = Math.max(highestSkillLevel, highestRobotSkillLevel);
	
        // Look for highest workshop tech level
        int highestTechLevel = owner.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)
                                .stream()
                                .mapToInt(w -> w.getManufacture().getTechLevel())
                                .max().orElse(-1);      

        // Get set of what is already queued
        Set<ManufactureProcessInfo> alreadyQueued = queue.stream()
                    .map(QueuedProcess::getInfo)
                    .filter(ManufactureProcessInfo.class::isInstance)
                    .map(ManufactureProcessInfo.class::cast)
                    .collect(Collectors.toSet());
                    
        // Determine all manufacturing processes that are possible and profitable.
        return ManufactureUtil.getManufactureProcessesForTechSkillLevel(highestTechLevel, highestSkillLevel)
                .stream()
                .filter(q -> !alreadyQueued.contains(q))
                .sorted()
                .toList();
    }
}
