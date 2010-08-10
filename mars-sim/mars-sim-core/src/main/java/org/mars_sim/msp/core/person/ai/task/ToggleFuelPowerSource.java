/**
 * Mars Simulation Project
 * ToggleFuelPowerSource.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
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
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The ToggleFuelPowerSource class is an EVA task for toggling a particular
 * fuel power source building on or off.
 * This is an effort driven task.
 */
public class ToggleFuelPowerSource extends EVAOperation implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ToggleFuelPowerSource";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // Task phase
    private static final String TOGGLE_POWER_SOURCE = "toggle power source";
    
    // Data members
    private boolean isEVA; // True if toggling process is EVA operation.
    private Airlock airlock; // Airlock to be used for EVA.
    private FuelPowerSource powerSource; // The fuel power source to toggle.
    private Building building; // The building the resource process is in.
    private boolean toggleOn; // True if power source is to be turned on, false if turned off.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public ToggleFuelPowerSource(Person person) throws Exception {
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
            if (!isEVA) BuildingManager.addPersonToBuilding(person, building);
        }
        else endTask();
        
        if (isEVA) {
            // Get an available airlock.
            airlock = getAvailableAirlock(person);
            if (airlock == null) endTask();
        }
        
        addPhase(TOGGLE_POWER_SOURCE);
        if (!isEVA) setPhase(TOGGLE_POWER_SOURCE);
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
                if (getAvailableAirlock(person) == null) result = 0D;
                
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
     * Gets the building at a person's settlement with the fuel power source that needs toggling.
     * @param person the person.
     * @return building with fuel power source to toggle, or null if none.
     * @throws Exception if error getting building.
     */
    private static Building getFuelPowerSourceBuilding(Person person) throws Exception {
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
    private static FuelPowerSource getFuelPowerSource(Building building) throws Exception {
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
    private static double getValueDiff(Settlement settlement, FuelPowerSource fuelSource) 
            throws Exception {
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
    private static double getInputResourcesValue(Settlement settlement, FuelPowerSource fuelSource) 
            throws Exception {
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
            FuelPowerSource fuelSource) throws Exception {
        boolean result = false;
        
        AmountResource resource = fuelSource.getFuelResource();
        double stored = settlement.getInventory().getAmountResourceStored(resource);
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
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (isEVA) {
            if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
            if (TOGGLE_POWER_SOURCE.equals(getPhase())) return togglePowerSourcePhase(time);
            if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
            else return time;
        }
        else {
            if (TOGGLE_POWER_SOURCE.equals(getPhase())) return togglePowerSourcePhase(time);
            else return time;
        }
    }
    
    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) throws Exception {
        
        try {
            time = exitAirlock(time, airlock);
        
            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }
        
        if (exitedAirlock) setPhase(TOGGLE_POWER_SOURCE);
        return time;
    }
    
    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) throws Exception {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) endTask();
        return time;
    }   
    
    /**
     * Performs the toggle power source phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double togglePowerSourcePhase(double time) throws Exception {
        
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            if (isEVA) setPhase(ENTER_AIRLOCK);
            else endTask();
        }

        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
            if (isEVA) setPhase(ENTER_AIRLOCK);
            else endTask();
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
            if (isEVA) setPhase(ENTER_AIRLOCK);
            else endTask();
            
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
        if (isEVA) super.checkForAccident(time);

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);
        
        // Modify based on the LUV's wear condition.
        chance *= building.getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) building.getMalfunctionManager().accident();
    }
}