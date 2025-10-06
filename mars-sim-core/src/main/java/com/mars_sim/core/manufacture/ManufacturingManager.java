/*
 * Mars Simulation Project
 * ManufacturingManager.java
 * @date 2025-07-23
 * @author Barry Evans
 */

package com.mars_sim.core.manufacture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * This manages the manufacturing and salvage processes of a Settlement.
 * It maintains a queue of processes that are awaiting processes.
 */
public class ManufacturingManager implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class QueuedProcess implements Serializable {
        private static final long serialVersionUID = 1L;

        private WorkshopProcessInfo info;
        private Salvagable target;
        private RatingScore value;
        private boolean resourcesAvailable;

        private QueuedProcess(WorkshopProcessInfo info, Salvagable target, RatingScore value,
                            boolean resourcesAvailable) {
            this.info = info;
            this.target = target;
            this.value = value;
            this.value.addModifier(USER_BONUS, 1D); // Add the default bonus
            this.resourcesAvailable = resourcesAvailable;
        }

        public WorkshopProcessInfo getInfo() {
            return info;
        }

        public Salvagable getTarget() {
            return target;
        }

        /**
         * Changes the priority of this queued process.
         * 
         * @param newPri
         */
        public void setValue(RatingScore newScore) {
            value = newScore;
        }

        public RatingScore getValue() {
            return value;
        }

        private void setResourcesAvailable(boolean avail) {
            resourcesAvailable = avail;
        }

        public boolean isResourcesAvailable() {
            return resourcesAvailable;
        }

        /**
         * Creates a workshop process to handle this item at a specific Workshop.
         * 
         * @param workshop
         * @return
         */
        public WorkshopProcess createProcess(Manufacture workshop) {
            switch (info) {
              case ManufactureProcessInfo mp:
                    return  new ManufactureProcess(mp, workshop);
              case SalvageProcessInfo sp: {
                    if (target == null) {
                        target = ManufactureUtil.findUnitForSalvage(sp, workshop.getBuilding().getSettlement());
                    }
                    return new SalvageProcess(sp, workshop, target);
              }
              default: throw new IllegalArgumentException("Unknown process type: " + info.getClass().getName());
            }
        }
    }

    private class UpdateEvent implements ScheduledEventHandler {
        private static final long serialVersionUID = 1L;
        private int nextRefresh;

        UpdateEvent(int nextRefresh) {
            this.nextRefresh = nextRefresh;
        }

        @Override
        public String getEventDescription() {
            return "Update the Manufacturing queue";
        }

        @Override
        public int execute(MarsTime currentTime) {
            updateQueue();

            return nextRefresh;
        }

    }

    private static final int REFRESH_TIME = 10;
    private static final Integer DEFAULT_VALUE = 30;
    private static final Integer DEFAULT_LIMIT = 200;
    private static final Integer DEFAULT_QUEUE_SIZE = 200;
    public static final String USER_BONUS = "user-bonus";

    private static SimLogger logger = SimLogger.getLogger(ManufacturingManager.class.getName());

    private List<QueuedProcess> queue;
    private Settlement owner;
    private int maxTechLevel = -2;
    private Set<Tooling> allTools;

    public ManufacturingManager(Settlement owner) {
        this.owner = owner;
        this.queue = new ArrayList<>();

        // Set up the future event to build queue every day.
        // First event is slightly in the future
        var futures = owner.getFutureManager();

        // Add a daily refresh event
        futures.addEvent(owner.getTimeZone().getMSolOffset() + REFRESH_TIME, new UpdateEvent(1000));

        // Add a one off event to build the queue
        futures.addEvent(1, new UpdateEvent(0));

        // Add the controlling preferences 
        var pMgr = owner.getPreferences();
        pMgr.putValue(ManufacturingParameters.INSTANCE, ManufacturingParameters.NEW_MANU_VALUE, DEFAULT_VALUE);
        pMgr.putValue(ManufacturingParameters.INSTANCE, ManufacturingParameters.NEW_MANU_LIMIT, DEFAULT_LIMIT);
        pMgr.putValue(ManufacturingParameters.INSTANCE, ManufacturingParameters.MAX_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

    }

    /**
     * Gets the queued processes waiting for resources/capacity.
     * 
     * @return
     */
    public List<QueuedProcess> getQueue() {
        return queue;
    }

    /**
     * Claims the next process on the queue that matches a tech level.
     * 
     * @param techLevel Maximum tech level of process
     * @param skillLevel Maximum skill level of worker
     * @return
     */
    public QueuedProcess claimNextProcess(int techLevel, int skillLevel, Set<Tooling> tools) {     
        // Update the available resource status of everything queued
        updateQueueItems();

        // Find startable process grouped by priority and filtered by
        // 1. Tech level
        // 2. Worker skill
        // 3. Type of process
        // 4. Resource are available
        var startableByPri = queue.stream()
                        .filter(q -> (q.info.getTechLevelRequired() <= techLevel)
                                        && (q.info.getSkillLevelRequired() <= skillLevel)
                                        && q.isResourcesAvailable()
                                        && q.info.isSupported(tools))
                        .sorted(Comparator.comparing(QueuedProcess::getValue).reversed())
                        .toList();

        if (startableByPri.isEmpty()) {
            return null;
        }

        // Select top value item
        var selected = startableByPri.get(0);
        removeProcessFromQueue(selected);
        return selected;
    }

    /**
     * Removes the process from the queue.
     * 
     * @param selected Process to remove
     */
    public void removeProcessFromQueue(QueuedProcess selected) {
        queue.remove(selected);
        owner.fireUnitUpdate(UnitEventType.MANU_QUEUE_REMOVE, selected);
    }

    /**
     * Adds a process to the queue for later processing. This is usually triggered by
     * the end user.
     * 
     * @param newProcess Process definition to add
     */
    public void addProcessToQueue(WorkshopProcessInfo newProcess) {
        var available = newProcess.isResourcesAvailable(owner);
        var value = getProcessValue(newProcess);
        var newItem = new QueuedProcess(newProcess, null, value, available);
        addToQueue(newItem);
    }

    /**
     * Adds a salvage process to the queue for later processing. This is targeted at a specific
     * entity.
     * 
     * @param newProcess Process definition to add
     * @param target Item to salvage in this process
     */
    public void addSalvageToQueue(SalvageProcessInfo newProcess, Salvagable target) {
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null for salvage process");
        }
        var available = newProcess.isResourcesAvailable(owner);
        var value = getProcessValue(newProcess);
        var newItem = new QueuedProcess(newProcess, target, value, available);

        addToQueue(newItem);
    }

    /**
     * Adds a new item to the processing queue.
     * 
     * @param newItem Queued item to add
     */
    private void addToQueue(QueuedProcess newItem) {
        synchronized(queue) {
            queue.add(newItem);
        }   
        owner.fireUnitUpdate(UnitEventType.MANU_QUEUE_ADD, newItem);
    }

    /**
     * Checks which queued processes have available resources.
     */
    private void updateQueueItems() {
        // Check resources on queue
        for(var q : queue) {
            var p = q.getInfo();
            q.setResourcesAvailable(p.isResourcesAvailable(owner));

            // Get a new value to this Settlement and reapply the user bonus
            var newValue = getProcessValue(p);
            var bonus = q.getValue().getModifiers().getOrDefault(USER_BONUS, 1D);
            newValue.addModifier(USER_BONUS, bonus);
            q.setValue(newValue);
        }  

        if (!queue.isEmpty()) {
            owner.fireUnitUpdate(UnitEventType.MANE_QUEUE_REFRESH);
        }
    }

    /**
     * Sets the percentage of boost the user applies to a QueuedProcess. This will
     * apply a modifier to the value. The bonus is a percentage of the value so 100% is zero bonus.
     * 
     * @param q
     * @param bonusPerc A positive value that represents a percentage modifier
     */
    public void setBonus(QueuedProcess q, int bonusPerc) {
        q.getValue().addModifier(USER_BONUS, bonusPerc/100D);
    }

    /**
     * Calculates the maximum tech level and available tooling this Settlement can run.
     * Scans the connected Workshops.
     */
    public void updateTechLevel() {
        maxTechLevel = -1;
        allTools = new HashSet<>();
        
        for(var b : owner.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)) {
            var m = b.getManufacture();

            allTools.addAll(m.getToolDetails().keySet());
            if (m.getTechLevel() > maxTechLevel) {
                maxTechLevel = m.getTechLevel();
            }
        }
    }

    /**
     * Gets the preferred capacity to add new items to the queue.
     * 
     * @return This could be negative
     */
    private int getQueueCapacity() {
        // Add new queue items if queue is within limit
        var maxQueue = owner.getPreferences().getIntValue(ManufacturingParameters.INSTANCE,
                                        ManufacturingParameters.MAX_QUEUE_SIZE,
                                        DEFAULT_QUEUE_SIZE);
        return maxQueue - queue.size();
    }
    /**
     * Finds any manufacturing processes that can be added to the queue.
     */
    void updateQueue() {
        // Check no workshop have been added/removed/upgraded
        updateTechLevel();

        // Update the resources available flag on existing queue
        updateQueueItems();

        // Add new queue items if queue is within limit
        if (getQueueCapacity() > 0) {
            createManuQueueItems();

            // Could add Salvage processes following the pattern of createManuQueue method
            // Would need to could Equipment present
        }
    }

    /**
     * Creates and add new items to the queue for manufacturing. Get any queuable manu process
     * that has resources available. Then score the remaining set.
     * The score of the potential processes are check for the threshold value.
     * The top N processes with the highest process values are added to the queue.
     */
    private void createManuQueueItems() {
        var pMgr = owner.getPreferences();
        int maxProcesses = Math.min(getQueueCapacity(),
                                    pMgr.getIntValue(ManufacturingParameters.INSTANCE,
                                                     ManufacturingParameters.NEW_MANU_LIMIT,
                                                     DEFAULT_LIMIT));
        if (maxProcesses > 0) {

            var scoreThreshold = pMgr.getIntValue(ManufacturingParameters.INSTANCE, ManufacturingParameters.NEW_MANU_VALUE, DEFAULT_VALUE);

            var potential = getQueuableManuProcesses()
                    .filter(i -> i.isResourcesAvailable(owner))
                    .toList();
            addTopValueProcesses(potential, scoreThreshold, maxProcesses);
        }   
    }

    /**
     * Gets the value of a process to the settlement. This is captured as a RatingScore so 
     * the individual parts can be seen.
     * 
     * @param info
     * @return
     */
    private RatingScore getProcessValue(ProcessInfo info) {
        RatingScore value = new RatingScore();
        info.getOutputList().forEach(i -> value.addBase(i.getName(),
                        ManufactureUtil.getManufactureProcessItemGoodValuePoint(i, owner, true)));  
        
        return value;
    }

    /**
     * Adds the top value processes from the potential list where the value is above the 
     * threshold.
     * 
     * @param potential Potential processes to evaluated.
     * @param scoreThreshold Value threshold of processes to add
     * @param maxProcesses Max number of processes to add
     * @return Number added
     */
    private int addTopValueProcesses(List<? extends WorkshopProcessInfo> potential,
                                     int scoreThreshold, int maxProcesses) {

        record ProcessValue(WorkshopProcessInfo info, RatingScore score) {
            double value() {return score.getScore();}
        }
            
        // Score the potential processes and take those above threshold
        List<ProcessValue> candidates = new ArrayList<>();
        for(var p : potential) {
            // Add the individual output values
            RatingScore value = getProcessValue(p);
            
            if (value.getScore() > scoreThreshold) {
                candidates.add(new ProcessValue(p, value));
            }
        }

        // Take the top N of what is left
        Collections.sort(candidates, Comparator.comparingDouble(ProcessValue::value));
        int added = Math.min(candidates.size(), maxProcesses);
        for (int i = 0; i < added; i++) {
            var choosen = candidates.get(i);

            // This info has resources otherwise would not be here
            var newItem = new QueuedProcess(choosen.info, null, choosen.score, true);
            addToQueue(newItem);
        }

        if (added == 1) {
            logger.info(owner, "Automatically added a process. max: " + candidates.size() + ".");
        }
        else if (added > 1) {
            logger.info(owner, "Automatically added " + added + " processes. max: " + candidates.size() + ".");
        }
        
        return added;
    }

    /**
     * Gets the lowest tech level of queued items.
     * 
     * @return
     */
    public int getLowestOnQueue() {
        return queue.stream()
            .mapToInt(q -> q.getInfo().getTechLevelRequired())
            .min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Gets the highest skill level of all the Workers at this settlement.
     * 
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
     * Gets the manu processed that can be supported by this settlement. It considers
     * - MaxTechlevel of any workshops
     * - MaterialScience skill of settlement workers; this is recalculated each time
     * 
     * @return Processes that can be processed by this settlement
     */
    private List<ManufactureProcessInfo> getSupportedManuProcesses() {
        return ManufactureUtil.getManufactureProcessesForTechSkillLevel(getMaxTechLevel(),
                                                                        getHighestSkill(), allTools);
    }

    
    /**
     * Gets a list of Manufacturing processes that can be queued.
     * There is no process of the type already queue and workers with the skill present.
     * 
     * @return
     */
    private Stream<ManufactureProcessInfo> getQueuableManuProcesses() {

        // Get set of what is already queued
        Set<ManufactureProcessInfo> alreadyQueued = queue.stream()
                    .map(QueuedProcess::getInfo)
                    .filter(ManufactureProcessInfo.class::isInstance)
                    .map(ManufactureProcessInfo.class::cast)
                    .collect(Collectors.toSet());
                    
        // Determine all manufacturing processes that are possible and profitable.
        return getSupportedManuProcesses().stream()
                .filter(q -> !alreadyQueued.contains(q));
    }

    /**
     * Gets a list of Manufacturing processes that can be queued.
     * There is no process of the type already queue and workers with the skill present.
     * Optionally the queuable processes can be filtered by a mandatory output of the process.
     * 
     * @param outputName Optional name of an output that must be produced
     * @return
     */
    public List<ManufactureProcessInfo> getQueuableManuProcesses(String outputName) {
        // Determine all manufacturing processes that are possible and profitable.
        var stream = getQueuableManuProcesses();
        
        // Add filter by output if required
        if (outputName != null) {
            stream = stream.filter(p -> p.isOutput(outputName));
        }
        return stream.sorted().toList();
    }

    /**
     * Gets the Salvage processes that can be processed by this settlement.
     * 
     * @return List of salvage processes.
     */
    public List<SalvageProcessInfo> getQueuableSalvageProcesses() {
        return ManufactureUtil.getSalvageProcessesForTechSkillLevel(getMaxTechLevel(), getHighestSkill(), allTools);
    }

    /**
     * Gets the maximum tech level that can be supported by this settlement.
     * The value is updated if not initialised.
     * 
     * @return
     */
    public int getMaxTechLevel() {
        if (maxTechLevel == -2) {
            updateTechLevel();
        }
        return maxTechLevel;
    }

    /**
     * Gets the list of resources that could be manufactured based on its workshops.
     * This does not consider available resources.
     */
    public List<String> getPossibleOutputs() {
        var supported = getSupportedManuProcesses();

        return supported.stream()
                    .map(ProcessInfo::getOutputList)
                    .flatMap(Collection::stream)
                    .map(m -> m.getName())
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .toList();
    }
}
