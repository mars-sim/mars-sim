/*
 * Mars Simulation Project
 * ToggleFuelPowerSource.java
 * @date 2022-07-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The ToggleFuelPowerSource class is an EVA task for toggling a particular
 * fuel power source building on or off.
 */
public class ToggleFuelPowerSource extends EVAOperation {

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
	/** True if the building with the fuel power source is inhabitable. */
    private boolean isInhabitable;
    /** True if power source is to be turned on, false if turned off. */
    private boolean toggleOn;

    /** The fuel power source to toggle. */
    private FuelPowerSource powerSource;
    /** The building the resource process is in. */
    private Building building;

    /**
     * Constructor.
     * 
     * @param person the person performing the task.
     * @param powerSource
     * @param building
     * @throws Exception if error constructing the task.
     */
    public ToggleFuelPowerSource(Person person, Building building, FuelPowerSource powerSource) {
        super(NAME_ON, person, false, 0D, SkillType.MECHANICS);

        this.building = building;
        this.powerSource = powerSource;

        boolean isOn = powerSource.isToggleON();

        if (orbitInfo == null)
            orbitInfo = Simulation.instance().getOrbitInfo();

        boolean isSunSetting = orbitInfo.isSunSetting(
                person.getSettlement().getCoordinates(), true);

        double sunlight = getSolarIrradiance(person.getSettlement().getCoordinates());
        
        if ((isSunSetting && isOn)
            // if it's sunsetting and sky is dark or getting dark, 
            // should let the fuel power source STAY ON 
            // throughout the night since solar panels are not supplying power.
            || (sunlight <= 0 && isOn)) {
                endTask();
        }
            
        toggleOn = !isOn;

        if (!toggleOn) {
            setName(NAME_OFF);
            setDescription(NAME_OFF);
        }

        isInhabitable = !building.hasFunction(FunctionType.LIFE_SUPPORT);

        // If habitable building, send person there.
        if (!isInhabitable) {
            
            int rand = RandomUtil.getRandomInt(5);
            
            if (rand == 0) {
                // Yes it requires EVA
                
                // Walk to power source building.
                walkToPowerSourceBuilding(building);
                
            }
            else { // 5 out of 6 cases requires no EVA
                // Looks for management function for toggling power source.
                checkManagement();
            }
        }

        else {
            // Looks for management function for toggling power source.
            checkManagement();
        }

        addPhase(TOGGLE_POWER_SOURCE);
        setPhase(TOGGLE_POWER_SOURCE);
    }

	/**
	 * Checks if any management function is available.
	 */
	private void checkManagement() {

		boolean done = false;
		// Pick an administrative building for remote access to the resource building
		Set<Building> mgtBuildings = person.getSettlement().getBuildingManager()
				.getBuildingSet(FunctionType.MANAGEMENT);

		if (!mgtBuildings.isEmpty()) {

			Set<Building> notFull = new UnitSet<>();

			for (Building b : mgtBuildings) {
				if (b.hasFunction(FunctionType.ADMINISTRATION)) {
					walkToMgtBldg(b);
					done = true;
					break;
				}
				else if (b.getManagement() != null && !b.getManagement().isFull()) {
					notFull.add(b);
				}
			}

			if (!done) {
				if (!notFull.isEmpty()) {
					walkToMgtBldg(RandomUtil.getARandSet(mgtBuildings));
				}
				else {
					end(powerSource.getType().getName() + ": Management space unavailable.");
				}
			}
		}
		else {
			end("Management space unavailable.");
		}
	}

    /**
     * Walks to the building with management function.
     *
     * @param b the building
     */
	private void walkToMgtBldg(Building b) {
		walkToTaskSpecificActivitySpotInBuilding(b,
				FunctionType.MANAGEMENT,
				false);
	}

	/**
	 * Ends the task.
	 *
	 * @param s
	 */
	private void end(String s) {
		logger.log(person, Level.WARNING, 20_000, s);
		endTask();
	}

    /**
     * Walks to power source building.
     * 
     * @param powerBuilding the power source building.
     */
    private void walkToPowerSourceBuilding(Building powerBuilding) {

        // Determine location within power source building.
        // Note: Use action point rather than random internal location.
        LocalPosition settlementLoc = LocalAreaUtil.getRandomLocalRelativePosition(powerBuilding);

        Walk walk = Walk.createWalkingTask(person, settlementLoc, 0, powerBuilding);
        if (walk != null) {
            // Add subtask for walking to power building.
            addSubTask(walk);
        }
        else {
            logger.log(person, Level.WARNING, 3_000,
            		"Unable to walk to power building " +
                    powerBuilding.getNickName() + ".");
            endTask();
        }
    }

    @Override
    protected void addExperience(double time) {

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = ((experienceAptitude) - 50D) / 100D;

        if (isInhabitable) {
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
        if (isInhabitable) {
            result.add(SkillType.EVA_OPERATIONS);
        }
        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getSkillManager();
        int evaOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
        if (isInhabitable) {
            return (int) Math.round((evaOperationsSkill + mechanicsSkill) / 2D);
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
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double togglePowerSourcePhase(double time) {
  
		if (person.isUnFit()) {
			checkLocation();
			return time;
		}
		
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
        	checkLocation();
        	return time;
        }

        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
            checkLocation();
            return time;
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
        addExperience(workTime);

        String toggle = "off";
        if (toggleOn) toggle = "on";

        logger.log(person, Level.INFO, 30_000L,
				"Turning " + toggle + " " + powerSource.getType()
                + " in " + building.getNickName() + ".");

        // Check if an accident happens during toggle power source.
        checkForAccident(time);

        return 0;
    }


    /**
     * Checks for accident with entity during toggle resource phase.
     *
     * @param time the amount of time (in millisols)
     */
    @Override
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        if (isInhabitable) {
            super.checkForAccident(time);
        }

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        checkForAccident(building, time, .005D, skill, powerSource.toString());
    }
}
