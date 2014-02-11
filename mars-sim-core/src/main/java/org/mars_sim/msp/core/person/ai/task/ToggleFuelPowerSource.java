/**
 * Mars Simulation Project
 * ToggleFuelPowerSource.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The ToggleFuelPowerSource class is an EVA task for toggling a particular
 * fuel power source building on or off.
 * This is an effort driven task.
 */
public class ToggleFuelPowerSource extends EVAOperation implements Serializable {

    private static Logger logger = Logger.getLogger(ToggleFuelPowerSource.class.getName());
    
    // Task phase
    private static final String WALK_OUTSIDE_TO_BUILDING = "Walk Outside to Building";
    private static final String TOGGLE_POWER_SOURCE = "Toggle Power Source";
    private static final String WALK_TO_AIRLOCK = "Walk to Airlock";
    
    // Data members
    private boolean isEVA; // True if toggling process is EVA operation.
    private Airlock airlock; // Airlock to be used for EVA.
    private FuelPowerSource powerSource; // The fuel power source to toggle.
    private Building building; // The building the resource process is in.
    private boolean toggleOn; // True if power source is to be turned on, false if turned off.
    private double toggleXLoc;
    private double toggleYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public ToggleFuelPowerSource(Person person) {
        super("Turning on fuel power source", person);
        
        building = getFuelPowerSourceBuilding(person);
        if (building != null) {
            powerSource = getFuelPowerSource(building);
            toggleOn = !powerSource.isToggleON();
            if (!toggleOn) {
                setName("Turning off fuel power source");
                setDescription("Turning off fuel power source");
            }
            isEVA = !building.hasFunction(LifeSupport.NAME);
            
            // If habitable building, add person to building.
            if (!isEVA) {
                // Walk to power source building.
                walkToPowerSourceBuilding(building);
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
        addPhase(TOGGLE_POWER_SOURCE);
        addPhase(WALK_TO_AIRLOCK);
        
        if (isEVA) {
            setPhase(EVAOperation.EXIT_AIRLOCK);
        }
        else {
            setPhase(TOGGLE_POWER_SOURCE);
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
            
            try {
                Building building = getFuelPowerSourceBuilding(person);
                if (building != null) {
                    FuelPowerSource powerSource = getFuelPowerSource(building);
                    isEVA = !building.hasFunction(LifeSupport.NAME);
                    double diff = getValueDiff(settlement, powerSource);
                    double baseProb = diff * 10000D;
                    if (baseProb > 100D) baseProb = 100D;
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
            
            if (isEVA) {
                // Check if an airlock is available
                if (getWalkableAvailableAirlock(person) == null) {
                    result = 0D;
                }
                
                // Check if it is night time.
                SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
                    if (!surface.inDarkPolarRegion(person.getCoordinates()))
                        result = 0D;
                } 
                
                // Crowded settlement modifier
                if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                    if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
                }
            }
            
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(ToggleFuelPowerSource.class);    
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
     * Walk to power source building.
     * @param powerBuilding the power source building.
     */
    private void walkToPowerSourceBuilding(Building powerBuilding) {
        
        // Determine location within power source building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(powerBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), powerBuilding);
        
        // Check if there is a valid interior walking path between buildings.
        BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();
        Building currentBuilding = BuildingManager.getBuilding(person);
        
        if (connectorManager.hasValidPath(currentBuilding, powerBuilding)) {
            Task walkingTask = new WalkSettlementInterior(person, powerBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
            addSubTask(walkingTask);
        }
        else {
            // TODO: Add task for EVA walking to get to power source building.
            BuildingManager.addPersonToBuilding(person, powerBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
        }
    }
    
    /**
     * Gets the building at a person's settlement with the fuel power source that needs toggling.
     * @param person the person.
     * @return building with fuel power source to toggle, or null if none.
     * @throws Exception if error getting building.
     */
    private static Building getFuelPowerSourceBuilding(Person person) {
        Building result = null;
        
        Settlement settlement = person.getSettlement();
        if (settlement != null) {
            BuildingManager manager = settlement.getBuildingManager();
            double bestDiff = 0D;
            Iterator<Building> i = manager.getBuildings(PowerGeneration.NAME).iterator();
            while (i.hasNext()) {
                Building building = i.next();
                FuelPowerSource powerSource = getFuelPowerSource(building);
                if (powerSource != null) {
                    double diff = getValueDiff(settlement, powerSource);
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
     * Gets the fuel power source to toggle at a building.
     * @param building the building
     * @return the fuel power source to toggle or null if none.
     * @throws Exception if error getting fuel power source.
     */
    private static FuelPowerSource getFuelPowerSource(Building building) {
        FuelPowerSource result = null;
        
        Settlement settlement = building.getBuildingManager().getSettlement();
        if (building.hasFunction(PowerGeneration.NAME)) {
            double bestDiff = 0D;
            PowerGeneration powerGeneration = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
            Iterator<PowerSource> i = powerGeneration.getPowerSources().iterator();
            while (i.hasNext()) {
                PowerSource powerSource = i.next();
                if (powerSource instanceof FuelPowerSource) {
                    FuelPowerSource fuelSource = (FuelPowerSource) powerSource;
                    double diff = getValueDiff(settlement, fuelSource);
                    if (diff > bestDiff) {
                        bestDiff = diff;
                        result = fuelSource;
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets the value diff between inputs and outputs for a fuel power source.
     * @param settlement the settlement the resource process is at.
     * @param fuelSource the fuel power source.
     * @return the value diff (value points)
     * @throws Exception if error getting value diff.
     */
    private static double getValueDiff(Settlement settlement, FuelPowerSource fuelSource) {
        
        double inputValue = getInputResourcesValue(settlement, fuelSource);
        double outputValue = getPowerOutputValue(settlement, fuelSource);
        double diff = outputValue - inputValue;
        if (fuelSource.isToggleON()) diff *= -1D;
        
        // Check if settlement doesn't have one or more of the input resources.
        if (isEmptyInputResource(settlement, fuelSource)) {
            if (fuelSource.isToggleON()) diff = 1D;
            else diff = 0D;
        }
        return diff;
    }
    
    /**
     * Gets the total value of a fuel power sources input resources.
     * @param settlement the settlement.
     * @param fuel source the fuel power source.
     * @return the total value for the input resources per Sol.
     * @throws Exception if problem determining resources value.
     */
    private static double getInputResourcesValue(Settlement settlement, FuelPowerSource fuelSource) {
        
        AmountResource resource = fuelSource.getFuelResource();
        double massPerSol = fuelSource.getFuelConsumptionRate();
        double value = settlement.getGoodsManager().getGoodValuePerItem(GoodsUtil.getResourceGood(resource));
        
        return value * massPerSol;
    }
    
    /**
     * Gets the total value of the power produced by the power source.
     * @param settlement the settlement.
     * @param fuelSource the fuel power source.
     * @return the value of the power generated per Sol.
     */
    private static double getPowerOutputValue(Settlement settlement, FuelPowerSource fuelSource) {
        
        // Get settlement value for kW hr produced.
        double power = fuelSource.getMaxPower();
        double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
        double powerPerSol = power * hoursInSol;
        double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
        
        return powerValue;
    }
    
    /**
     * Checks if a fuel power source has no input resources.
     * @param settlement the settlement the resource is at.
     * @param fuelSource the fuel power source.
     * @return true if any input resources are empty.
     * @throws Exception if error checking input resources.
     */
    private static boolean isEmptyInputResource(Settlement settlement, 
            FuelPowerSource fuelSource) {
        boolean result = false;
        
        AmountResource resource = fuelSource.getFuelResource();
        double stored = settlement.getInventory().getAmountResourceStored(resource, false);
        if (stored == 0D) result = true;
        
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
            person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
        }
        
        // If phase is toggle power source, add experience to mechanics skill.
        if (TOGGLE_POWER_SOURCE.equals(getPhase())) {
            // 1 base experience point per 100 millisols of time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double mechanicsExperience = time / 100D;
            mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
        }
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#getAssociatedSkills()
     */
    @Override
    public List<String> getAssociatedSkills() {
        List<String> result = new ArrayList<String>(2);
        result.add(Skill.MECHANICS);
        if (isEVA) result.add(Skill.EVA_OPERATIONS);
        return result;
    }

    /* (non-Javadoc)
     * @see org.mars_sim.msp.simulation.person.ai.task.Task#getEffectiveSkillLevel()
     */
    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
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
        else if (TOGGLE_POWER_SOURCE.equals(getPhase())) {
            return togglePowerSourcePhase(time);
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
            setPhase(TOGGLE_POWER_SOURCE);
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
     * Performs the toggle power source phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double togglePowerSourcePhase(double time) {
        
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
        }

        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
        }
        
        if (isDone()) return time;
        
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the toggle power source.
        powerSource.addToggleWorkTime(workTime);
            
        // Add experience points
        addExperience(time);
            
        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
            if (isEVA) {
                setPhase(WALK_TO_AIRLOCK);
            }
            else {
                endTask();
            }
            
            Settlement settlement = building.getBuildingManager().getSettlement();
            String toggle = "off";
            if (toggleOn) toggle = "on";
            logger.info(person.getName() + " turning " + toggle + " " + powerSource.getType() + 
                    " at " + settlement.getName() + ": " + building.getName());
        }
            
        // Check if an accident happens during toggle power source.
        checkForAccident(time);
        
        return 0D;
    }
    
    /**
     * Check for accident with entity during toggle resource phase.
     * @param time the amount of time (in millisols)
     */
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        if (isEVA) {
            super.checkForAccident(time);
        }

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }
        
        // Modify based on the building's wear condition.
        chance *= building.getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            building.getMalfunctionManager().accident();
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        airlock = null;
        powerSource = null;
        building = null;
    }
}