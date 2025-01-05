package com.mars_sim.core.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

            return 1000; // this needs to adjust to the every morning hours of the owner Settlement
        }

    }
	private static SimLogger logger = SimLogger.getLogger(ManufacturingManager.class.getName());

    private List<QueuedProcess> queue;
    private Settlement owner;
    private int maxTechLevel = -2;

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
     * Claim the next process on the queue that matches a tech level.
     * @param techLevel Maximum tech level of process
     * @param skillLevel Maximum skill level of worker
     * @param manuFilter Filter to just Manufacturing or Salvage
     * @return
     */
    public QueuedProcess claimNextProcess(int techLevel, int skillLevel, boolean manuFilter) {     
        // Update the available resource status of everything queued
        updateQueueItems();

        // Find startable process grouped by priority and filtered by
        // 1. Tech level
        // 2. Worker skill
        // 3. Type of process
        // 4. Resoruce are available
        var startableByPri = queue.stream()
                        .filter(q -> (q.info.getTechLevelRequired() <= techLevel)
                                        && (q.info.getSkillLevelRequired() <= skillLevel)
                                        && ((manuFilter && q.info instanceof ManufactureProcessInfo)
                                            || (!manuFilter && q.info instanceof SalvageProcessInfo))
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
     * Add a process to the queue for later processing.
     * @param newProcess Process definition to add
     */
    public void addProcessToQueue(ProcessInfo newProcess) {
        addToQueue(newProcess, null);
    }

    /**
     * Add a salvage process to the queue for later processing. This is targeted at a specific
     * entity.
     * @param newProcess Process definition to add
     * @param target Item to salvage in this process
     */
    public void addSalvage(SalvageProcessInfo newProcess, Salvagable target) {
        addToQueue(newProcess, target);
    }

    private void addToQueue(ProcessInfo newProcess, Salvagable target) {
        var available = newProcess.isResourcesAvailable(owner);
        var newItem = new QueuedProcess(newProcess, target, 1, available);
        synchronized(queue) {
            queue.add(newItem);
        }   
        logger.info(owner, "Added new Process to queue " + newProcess.getName());
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
     * Calculate the maximum tech level process this Settlement can run.
     * Scans the connected Workshops.
     */
    public void updateTechLevel() {
        maxTechLevel = owner.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)
                    .stream()
                    .mapToInt(w -> w.getManufacture().getTechLevel())
                    .max().orElse(-1);
    }

    /**
     * Find any manufacturing proesses that can be added to the queue
     */
    void updateQueue() {
        // Check no workshop have been added/removed/upgraded
        updateTechLevel();

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
     * Get the highest skill level of all the Workers at this Settlement
     * @return
     */
    private int getHighestSkill() {
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
        return Math.max(highestSkillLevel, highestRobotSkillLevel);
    }

    /**
     * Get the mau processed that can be supported by this Settlement. It considers
     * - Maxtechlevel of any workshops
     * - MaterialScience skill of settlement workers; this is recalculated each time
     * @return Processes that can be processed by this Settlment
     */
    private List<ManufactureProcessInfo> getSupportedManuProcesses() {
        return ManufactureUtil.getManufactureProcessesForTechSkillLevel(getMaxTechLevel(),
                                                                        getHighestSkill());
    }

    /**
     * Get a list of Manufacturing processes that can be queued.
     * There is no process of the type already queue and workers with the skill present.
     * Optionally the queuable processes can be filtered by a manatory output of the process.
     * 
     * @param outputName Optional name of an output that must be produced
     * @return
     */
    public List<ManufactureProcessInfo> getQueuableManuProcesses(String outputName) {

        // Get set of what is already queued
        Set<ManufactureProcessInfo> alreadyQueued = queue.stream()
                    .map(QueuedProcess::getInfo)
                    .filter(ManufactureProcessInfo.class::isInstance)
                    .map(ManufactureProcessInfo.class::cast)
                    .collect(Collectors.toSet());
                    
        // Determine all manufacturing processes that are possible and profitable.
        var stream = getSupportedManuProcesses().stream()
                .filter(q -> !alreadyQueued.contains(q));
        
        // Add filter by output if required
        if (outputName != null) {
            stream = stream.filter(p -> p.isOutput(outputName));
        }
        return stream
                .sorted()
                .toList();
    }

    /**
     * Get the Salvage processes that can be processed by this Settlement
     * @return List of salvage processes.
     */
    public List<SalvageProcessInfo> getQueuableSalvageProcesses() {
        return ManufactureUtil.getSalvageProcessesForTechSkillLevel(getMaxTechLevel(), getHighestSkill());
    }

    /**
     * Get the maximum tech level that can be supported by this Settlement.
     * The value is updated if not initialised.
     * @return
     */
    public int getMaxTechLevel() {
        if (maxTechLevel == -2) {
            updateTechLevel();
        }
        return maxTechLevel;
    }

    /**
     * Get the list of resources that could be manufactured bu this Settlement based on it's workshops.
     * This does not consder avaialble resources
     */
    public List<String> getPossibleOutputs() {
        var supported = getSupportedManuProcesses();

        return supported.stream()
                    .map(ProcessInfo::getOutputList)
                    .flatMap(Collection::stream)
                    .map(m -> m.getName())
                    .distinct()
                    .sorted()
                    .toList();
    }
}
