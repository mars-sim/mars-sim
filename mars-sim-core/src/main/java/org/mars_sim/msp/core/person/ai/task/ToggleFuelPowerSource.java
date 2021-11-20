/*
 * Mars Simulation Project
 * ToggleFuelPowerSource.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ToggleFuelPowerSource class is an EVA task for toggling a particular
 * fuel power source building on or off.
 */
public class ToggleFuelPowerSource
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ToggleFuelPowerSource.class.getName());
	
    /** Task name */
    private static final String NAME_ON = Msg.getString(
            "Task.description.toggleFuelPowerSource.on"); //$NON-NLS-1$
    private static final String NAME_OFF = Msg.getString(
            "Task.description.toggleFuelPowerSource.off"); //$NON-NLS-1$
    /** Task phases. */
    private static final TaskPhase TOGGLE_POWER_SOURCE = new TaskPhase(Msg.getString(
            "Task.phase.togglePowerSource")); //$NON-NLS-1$

    // Data members
//    /** True if toggling process is EVA operation. */
    private boolean isEVA;
    /** The fuel power source to toggle. */
    private FuelPowerSource powerSource;
    /** The building the resource process is in. */
    private Building building;
	/** The building the person can go to remotely control the resource process. */
	private Building destination;
    /** True if power source is to be turned on, false if turned off. */
    private boolean toggleOn;

    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public ToggleFuelPowerSource(Person person) {
        super(NAME_ON, person, false, 0D, SkillType.MECHANICS);

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
        building = getFuelPowerSourceBuilding(person);
        if (building != null) {
            powerSource = getFuelPowerSource(building);
            
            //MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
            //double millisols = clock.getMillisol();
            boolean isOn = powerSource.isToggleON();
            
            if (orbitInfo == null)
            	orbitInfo = Simulation.instance().getMars().getOrbitInfo();
            
            boolean isSunRising = orbitInfo.isSunRising(person.getSettlement().getCoordinates());
            
            if (!isSunRising && isOn)
                // if the sky is getting dark soon, should let it STAY ON since solar panel will no longer be supplying power soon.
            	endTask();
            	
            toggleOn = !isOn;
            
            if (!toggleOn) {
                setName(NAME_OFF);
                setDescription(NAME_OFF);
            }
            
            isEVA = !building.hasFunction(FunctionType.LIFE_SUPPORT);

            // If habitable building, add person to building.
            if (!isEVA) {
                // Walk to power source building.
                walkToPowerSourceBuilding(building);
            }
            
            else {
                // Determine location for toggling power source.
//                Point2D toggleLoc = determineToggleLocation();
//                setOutsideSiteLocation(toggleLoc.getX(), toggleLoc.getY());
            	
            	Management m = building.getManagement();
    			if (m != null) {
    				destination = building;
    				walkToTaskSpecificActivitySpotInBuilding(destination, FunctionType.MANAGEMENT, false);
    			}
            }
           
        }
        else {
            endTask();
        }

        addPhase(TOGGLE_POWER_SOURCE);

//        if (!isEVA) {
            setPhase(TOGGLE_POWER_SOURCE);
//        }
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

        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 0,
                powerBuilding)) {

            // Add subtask for walking to power building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 0,
                    powerBuilding));
        }
        else {
            logger.log(person, Level.WARNING, 3_000, 
            		"Unable to walk to power building " +
                    powerBuilding.getNickName() + ".");
            endTask();
        }
    }

    /**
     * Gets the building at a person's settlement with the fuel power source that needs toggling.
     * @param person the person.
     * @return building with fuel power source to toggle, or null if none.
     */
    public static Building getFuelPowerSourceBuilding(Person person) {
        Building result = null;

        Settlement settlement = person.getSettlement();
        if (settlement != null) {
            BuildingManager manager = settlement.getBuildingManager();
            double bestDiff = 0D;
            Iterator<Building> i = manager.getBuildings(FunctionType.POWER_GENERATION).iterator();
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
     */
    public static FuelPowerSource getFuelPowerSource(Building building) {
        FuelPowerSource result = null;

        Settlement settlement = building.getSettlement();
        if (building.hasFunction(FunctionType.POWER_GENERATION)) {
            double bestDiff = 0D;
            PowerGeneration powerGeneration = building.getPowerGeneration();
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
     */
    public static double getValueDiff(Settlement settlement, FuelPowerSource fuelSource) {

        double inputValue = getInputResourcesValue(settlement, fuelSource);
        double outputValue = getPowerOutputValue(settlement, fuelSource);
        double diff = outputValue - inputValue;
        if (fuelSource.isToggleON()) {
            diff *= -1D;
        }

        // Check if settlement doesn't have one or more of the input resources.
        if (isEmptyInputResource(settlement, fuelSource)) {
            if (fuelSource.isToggleON()) {
                diff = 1D;
            }
            else {
                diff = 0D;
            }
        }
        return diff;
    }

    /**
     * Gets the total value of a fuel power sources input resources.
     * @param settlement the settlement.
     * @param fuel source the fuel power source.
     * @return the total value for the input resources per Sol.
     */
    private static double getInputResourcesValue(Settlement settlement, FuelPowerSource fuelSource) {
    	int resource = fuelSource.getFuelResourceID();
    	// Gets the rate [kg/Sol]
        double massPerSol = fuelSource.getFuelConsumptionRate();
        // Gets the demand for this fuel
        double value = settlement.getGoodsManager().getAmountDemandValue(resource);
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
        double powerPerSol = power * MarsClock.HOURS_PER_MILLISOL * 1000D;
        double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
        return powerValue;
    }

    /**
     * Checks if a fuel power source has no input resources.
     * @param settlement the settlement the resource is at.
     * @param fuelSource the fuel power source.
     * @return true if any input resources are empty.
     */
    private static boolean isEmptyInputResource(Settlement settlement,
            FuelPowerSource fuelSource) {
        boolean result = false;

        int resource = fuelSource.getFuelResourceID();
        double stored = settlement.getAmountResourceStored(resource);
        if (stored == 0D) {
            result = true;
        }

        return result;
    }

    @Override
    protected void addExperience(double time) {

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;

        if (isEVA) {
            // Add experience to "EVA Operations" skill.
            // (1 base experience point per 100 millisols of time spent)
            double evaExperience = time / 100D;
            evaExperience += evaExperience * experienceAptitudeModifier;
            evaExperience *= getTeachingExperienceModifier();
            person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);
        }

        // If phase is toggle power source, add experience to mechanics skill.
        if (TOGGLE_POWER_SOURCE.equals(getPhase())) {
            // 1 base experience point per 100 millisols of time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double mechanicsExperience = time / 100D;
            mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
            person.getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience, time);
        }
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> result = new ArrayList<>(2);
        result.add(SkillType.MECHANICS);
        if (isEVA) {
            result.add(SkillType.EVA_OPERATIONS);
        }
        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
        if (isEVA) {
            return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D);
        }
        else {
            return (mechanicsSkill);
        }
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return TOGGLE_POWER_SOURCE;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
        if (!isDone()) {
	        if (getPhase() == null) {
	            throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (TOGGLE_POWER_SOURCE.equals(getPhase())) {
	            time = togglePowerSourcePhase(time);
	        }
        }
        return time;
    }

    /**
     * Performs the toggle power source phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double togglePowerSourcePhase(double time) {

        // Check for radiation exposure during the EVA operation.
        if (isDone() || isRadiationDetected(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
	
        // Check if there is a reason to cut short and return.
        if (shouldEndEVAOperation() || addTimeOnSite(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            if (isEVA) {
                setPhase(WALK_BACK_INSIDE);
            }
            else {
                endTask();
            }
        }

        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
            if (isEVA) {
                setPhase(WALK_BACK_INSIDE);
            }
            else {
                endTask();
            }
        }

        if (isDone()) {
			endTask();
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) {
            workTime /= 2;
        }
        else if (mechanicSkill > 1) {
            workTime += workTime * (.2D * mechanicSkill);
        }

        // Add work to the toggle power source.
        powerSource.addToggleWorkTime(workTime);

        // Add experience points
        addExperience(time);

        String toggle = "off";
        if (toggleOn) toggle = "on";
        
        logger.log(person, Level.INFO, 3_000,
				"Turning " + toggle + " " + powerSource.getType()
                + " in " + building.getNickName() + ".");

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

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        checkForAccident(building, time, .005D, skill, null);
    }
}
