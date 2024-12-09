/*
 * Mars Simulation Project
 * ToggleFuelPowerSource.java
 * @date 2022-07-17
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.utility.power.FuelPowerSource;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The ToggleFuelPowerSource class is an EVA task for toggling a particular
 * fuel power source building on or off.
 */
public class ToggleFuelPowerSource extends Task {

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
    
    private static final ExperienceImpact IMPACT = new ExperienceImpact(100D,
                                                        NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                        true, 0.05,
                                                        SkillType.MECHANICS);

    // Data members
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
        super(NAME_ON, person, true, IMPACT, 0D);

        this.building = building;
        this.powerSource = powerSource;

        boolean isOn = powerSource.isToggleON();

        if (orbitInfo == null)
            orbitInfo = Simulation.instance().getOrbitInfo();

        boolean isSunSetting = orbitInfo.isSunSetting(
                person.getSettlement().getCoordinates(), true);
        
        if (isSunSetting && isOn) {
            // if it's sunsetting and sky is dark  
            // should let the fuel power source STAY ON 
            // throughout the night since solar panels are not supplying power.
            endTask();
            return;
        }
            
        toggleOn = !isOn;

        if (!toggleOn) {
            setName(NAME_OFF);
            setDescription(NAME_OFF);
        }

        var isInhabitable = !building.hasFunction(FunctionType.LIFE_SUPPORT);
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
        LocalPosition settlementLoc = LocalAreaUtil.getRandomLocalPos(powerBuilding);

        Walk walk = Walk.createWalkingTask(person, settlementLoc, powerBuilding);
        if (walk != null) {
            // Add subtask for walking to power building.
			boolean canAdd = addSubTask(walk);
			if (!canAdd) {
				logger.log(person, Level.WARNING, 4_000,
						". Unable to add subtask Walk.createWalkingTask.");
				// Note: may call below many times
				endTask();
			}
        }
        else {
            logger.log(person, Level.WARNING, 3_000,
            		"Unable to walk to power building " +
                    powerBuilding.getName() + ".");
            endTask();
        }
    }

    @Override
    protected double performMappedPhase(double time) {
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
  
		if (!person.isNominallyFit() || (person.getPerformanceRating() == 0D)) {
            endTask();
        	return time;
        }

        // Check if toggle has already been completed.
        if (powerSource.isToggleON() == toggleOn) {
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
                + " in " + building.getName() + ".");

        return 0;
    }

    /**
     * Gets the building.
     * 
     * @return
     */
    public Building getBuilding() {
    	return building;
    }
    
}
