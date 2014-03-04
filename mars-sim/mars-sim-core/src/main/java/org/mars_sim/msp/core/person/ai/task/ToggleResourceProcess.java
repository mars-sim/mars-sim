/**
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The ToggleResourceProcess class is an EVA task for toggling a particular
 * automated resource process on or off.
 * This is an effort driven task.
 */
public class ToggleResourceProcess
extends EVAOperation
implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	// TODO Task phase should be an enum.
    private static final String WALK_OUTSIDE_TO_BUILDING = "Walk Outside to Building";
    private static final String TOGGLE_PROCESS = "toggle process";
    private static final String WALK_TO_AIRLOCK = "Walk to Airlock";

    // Data members
    /** True if toggling process is EVA operation. */
    private boolean isEVA;
    /** Airlock to be used for EVA. */
    private Airlock airlock;
    /** The resource process to toggle. */
    private ResourceProcess process;
    /** The building the resource process is in. */
    private Building building;
    /** True if process is to be turned on, false if turned off. */
    private boolean toggleOn;
    private double toggleXLoc;
    private double toggleYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public ToggleResourceProcess(Person person) {
        super("Turning on resource process", person);

        building = getResourceProcessingBuilding(person);
        if (building != null) {
            process = getResourceProcess(building);
            toggleOn = !process.isProcessRunning();
            if (!toggleOn) {
                setName("Turning off resource process");
                setDescription("Turning off resource process");
            }
            isEVA = !building.hasFunction(LifeSupport.NAME);

            // If habitable building, add person to building.
            if (!isEVA) {
                // Walk to building.
                walkToProcessBuilding(building);
            }
            else {
                // Determine location for toggling power source.
                Point2D toggleLoc = determineToggleLocation();
                toggleXLoc = toggleLoc.getX();
                toggleYLoc = toggleLoc.getY();
                
                // Get an available airlock.
                airlock = getClosestWalkableAvailableAirlock(person, building.getXLocation(), 
                        building.getYLocation());
                if (airlock == null) {
                    endTask();
                }
                else {
                    // Determine location for reentering building airlock.
                    Point2D enterAirlockLoc = determineAirlockEnteringLocation();
                    enterAirlockXLoc = enterAirlockLoc.getX();
                    enterAirlockYLoc = enterAirlockLoc.getY();
                }
            }
        }
        else {
            endTask();
        }

        addPhase(WALK_OUTSIDE_TO_BUILDING);
        addPhase(TOGGLE_PROCESS);
        addPhase(WALK_TO_AIRLOCK);
        
        if (isEVA) {
            setPhase(EVAOperation.EXIT_AIRLOCK);
        }
        else {
            setPhase(TOGGLE_PROCESS);
        }
    }

    /** 
     * Gets the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            boolean isEVA = false;

            Settlement settlement = person.getSettlement();

            // Check if settlement has resource process override set.
            if (!settlement.getResourceProcessOverride()) {
                try {
                    Building building = getResourceProcessingBuilding(person);
                    if (building != null) {
                        ResourceProcess process = getResourceProcess(building);
                        isEVA = !building.hasFunction(LifeSupport.NAME);
                        double diff = getResourcesValueDiff(settlement, process);
                        double baseProb = diff * 10000D;
                        if (baseProb > 100D) {
                            baseProb = 100D;
                        }
                        result += baseProb;

                        if (!isEVA) {
                            // Factor in building crowding and relationship factors.
                            result *= Task.getCrowdingProbabilityModifier(person, building);
                            result *= Task.getRelationshipModifier(person, building);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }

            if (isEVA) {
                // Check if an airlock is available
                if (getWalkableAvailableAirlock(person) == null) {
                    result = 0D;
                }

                // Check if it is night time.
                SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
                    if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                        result = 0D;
                    }
                } 

                // Crowded settlement modifier
                if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                    if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                        result *= 2D;
                    }
                }
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(ToggleResourceProcess.class);    
            }
        }

        return result;
    }
    
    /**
     * Determine location to toggle power source.
     * @return location.
     */
    private Point2D determineToggleLocation() {
        
        Point2D.Double newLocation = new Point2D.Double(0D, 0D);
        
        boolean goodLocation = false;
        for (int x = 0; (x < 50) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(building, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                    boundedLocalPoint.getY(), building);
            goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                    person.getCoordinates());
        }
        
        return newLocation;
    }
    
    /**
     * Determine location outside building airlock.
     * @return location.
     */
    private Point2D determineAirlockEnteringLocation() {
        
        Point2D result = null;
        
        // Move the person to a random location outside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(entityBounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            result = newLocation;
        }
        
        return result;
    }

    /**
     * Walk to process toggle building.
     * @param processBuilding the process toggle building.
     */
    private void walkToProcessBuilding(Building processBuilding) {

        // Determine location within process toggle building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(processBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), processBuilding);

        // Check if there is a valid interior walking path between buildings.
        BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();
        Building currentBuilding = BuildingManager.getBuilding(person);

        if (connectorManager.hasValidPath(currentBuilding, processBuilding)) {
            Task walkingTask = new WalkSettlementInterior(person, processBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
            addSubTask(walkingTask);
        }
        else {
            // TODO: Add task for EVA walking to get to process toggle building.
            BuildingManager.addPersonToBuilding(person, processBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
        }
    }

    /**
     * Gets the building at a person's settlement with the resource process that needs toggling.
     * @param person the person.
     * @return building with resource process to toggle, or null if none.
     * @throws Exception if error getting building.
     */
    private static Building getResourceProcessingBuilding(Person person) {
        Building result = null;

        Settlement settlement = person.getSettlement();
        if (settlement != null) {
            BuildingManager manager = settlement.getBuildingManager();
            double bestDiff = 0D;
            Iterator<Building> i = manager.getBuildings(ResourceProcessing.NAME).iterator();
            while (i.hasNext()) {
                Building building = i.next();
                ResourceProcess process = getResourceProcess(building);
                if (process != null) {
                    double diff = getResourcesValueDiff(settlement, process);
                    if (diff > bestDiff) {
                        bestDiff = diff;
                        result = building;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the resource process to toggle at a building.
     * @param building the building
     * @return the resource process to toggle or null if none.
     * @throws Exception if error getting resource process.
     */
    private static ResourceProcess getResourceProcess(Building building) {
        ResourceProcess result = null;

        Settlement settlement = building.getBuildingManager().getSettlement();
        if (building.hasFunction(ResourceProcessing.NAME)) {
            double bestDiff = 0D;
            ResourceProcessing processing = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
            Iterator<ResourceProcess> i = processing.getProcesses().iterator();
            while (i.hasNext()) {
                ResourceProcess process = i.next();
                double diff = getResourcesValueDiff(settlement, process);
                if (diff > bestDiff) {
                    bestDiff = diff;
                    result = process;
                }
            }
        }

        return result;
    }

    /**
     * Gets the resources value diff between inputs and outputs for a resource process.
     * @param settlement the settlement the resource process is at.
     * @param process the resource process.
     * @return the resource value diff (value points)
     * @throws Exception if error getting value diff.
     */
    private static double getResourcesValueDiff(Settlement settlement, ResourceProcess process) {
        double inputValue = getResourcesValue(settlement, process, true);
        double outputValue = getResourcesValue(settlement, process, false);
        double diff = outputValue - inputValue;

        // Subtract power required per millisol.
        double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
        double powerHrsRequiredPerMillisol = process.getPowerRequired() * hoursInMillisol;
        double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();
        diff -= powerValue;

        if (process.isProcessRunning()) diff *= -1D;

        // Check if settlement doesn't have one or more of the input resources.
        if (isEmptyInputResourceInProcess(settlement, process)) {
            if (process.isProcessRunning()) diff = 1D;
            else diff = 0D;
        }
        return diff;
    }

    /**
     * Gets the total value of a resource process's input or output.
     * @param settlement the settlement for the resource process.
     * @param process the resource process.
     * @param input is the resource value for the input?
     * @return the total value for the input or output.
     * @throws Exception if problem determining resource value.
     */
    private static double getResourcesValue(Settlement settlement, ResourceProcess process, boolean input) {

        double result = 0D;

        Iterator<AmountResource> i = null;
        if (input) i = process.getInputResources().iterator();
        else i = process.getOutputResources().iterator();

        while (i.hasNext()) {
            AmountResource resource = i.next();
            boolean useResource = true;
            if (input && process.isAmbientInputResource(resource)) useResource = false;
            if (!input && process.isWasteOutputResource(resource)) useResource = false;
            if (useResource) {
                double value = settlement.getGoodsManager().getGoodValuePerItem(
                        GoodsUtil.getResourceGood(resource));
                double rate = 0D;
                if (input) rate = process.getMaxInputResourceRate(resource);
                else rate = process.getMaxOutputResourceRate(resource);
                result += (value * rate);
            }
        }

        return result;
    }

    /**
     * Checks if a resource process has no input resources.
     * @param settlement the settlement the resource is at.
     * @param process the resource process.
     * @return true if any input resources are empty.
     * @throws Exception if error checking input resources.
     */
    private static boolean isEmptyInputResourceInProcess(Settlement settlement, 
            ResourceProcess process) {
        boolean result = false;

        Iterator<AmountResource> i = process.getInputResources().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            if (!process.isAmbientInputResource(resource)) {
                double stored = settlement.getInventory().getAmountResourceStored(resource, false);
                if (stored == 0D) result = true;
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#addExperience(double)
     */
    @Override
    protected void addExperience(double time) {

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;

        if (isEVA) {
            // Add experience to "EVA Operations" skill.
            // (1 base experience point per 100 millisols of time spent)
            double evaExperience = time / 100D;
            evaExperience += evaExperience * experienceAptitudeModifier;
            evaExperience *= getTeachingExperienceModifier();
            person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
        }

        // If phase is toggle process, add experience to mechanics skill.
        if (TOGGLE_PROCESS.equals(getPhase())) {
            // 1 base experience point per 100 millisols of time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double mechanicsExperience = time / 100D;
            mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience);
        }
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#getAssociatedSkills()
     */
    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> result = new ArrayList<SkillType>(2);
        result.add(SkillType.MECHANICS);
        if (isEVA) result.add(SkillType.EVA_OPERATIONS);
        return result;
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#getEffectiveSkillLevel()
     */
    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
        if (isEVA) return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D);
        else return (mechanicsSkill);
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#performMappedPhase(double)
     */
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitEVA(time);
        }
        else if (WALK_OUTSIDE_TO_BUILDING.equals(getPhase())) {
            return walkOutsideToBuildingPhase(time);
        }
        else if (TOGGLE_PROCESS.equals(getPhase())) {
            return toggleProcessPhase(time);
        }
        else if (WALK_TO_AIRLOCK.equals(getPhase())) {
            return walkToAirlockPhase(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterEVA(time);
        }
        else {
            return time;
        }
    }

    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) {

        try {
            time = exitAirlock(time, airlock);

            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }

        if (exitedAirlock) {
            setPhase(WALK_OUTSIDE_TO_BUILDING);
        }
        return time;
    }
    
    /**
     * Perform the walk outside to building phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkOutsideToBuildingPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // If not at power source toggle location, create walk outside subtask.
        if ((person.getXLocation() != toggleXLoc) || (person.getYLocation() != toggleYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    toggleXLoc, toggleYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(TOGGLE_PROCESS);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToAirlockPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside airlock location, create walk outside subtask.
        if ((person.getXLocation() != enterAirlockXLoc) || (person.getYLocation() != enterAirlockYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    enterAirlockXLoc, enterAirlockYLoc, true);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EVAOperation.ENTER_AIRLOCK);
        }
        
        return time;
    }

    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) {
        time = enterAirlock(time, airlock);

        // Add experience points
        addExperience(time);

        if (enteredAirlock) {
            endTask();
        }
        
        return time;
    }	

    /**
     * Performs the toggle process phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double toggleProcessPhase(double time) {

        // If person is incapacitated, enter airlock.
        if (person.getPerformanceRating() == 0D) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
        }

        // Check if process has already been completed.
        if (process.isProcessRunning() == toggleOn) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
        }

        if (isDone()) {
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) {
            workTime /= 2;
        }
        if (mechanicSkill > 1) {
            workTime += workTime * (.2D * mechanicSkill);
        }

        // Add work to the toggle process.
        process.addToggleWorkTime(workTime);

        // Add experience points
        addExperience(time);

        // Check if process has already been completed.
        if (process.isProcessRunning() == toggleOn) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
            // Settlement settlement = building.getBuildingManager().getSettlement();
            // String toggle = "off";
            // if (toggleOn) toggle = "on";
            // logger.info(person.getName() + " turning " + toggle + " " + process.getProcessName() + " at " + settlement.getName() + ": " + building.getName());
        }

        // Check if an accident happens during toggle process.
        checkForAccident(time);

        return 0D;
    }

    /**
     * Check for accident with entity during toggle resource phase.
     * @param time the amount of time (in millisols)
     */
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        if (isEVA) super.checkForAccident(time);

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the building's wear condition.
        chance *= building.getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) building.getMalfunctionManager().accident();
    }

    @Override
    public void destroy() {
        super.destroy();

        airlock = null;
        process = null;
        building = null;
    }
}