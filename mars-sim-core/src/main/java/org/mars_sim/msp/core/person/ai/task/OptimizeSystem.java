/*
 * Mars Simulation Project
 * OptimizeSystem.java
 * @date 2022-08-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a task for investigating what system to optimize.
 */
public class OptimizeSystem extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.optimizeSystem"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase OPTIMIZING_SYSTEM = new TaskPhase(Msg.getString("Task.phase.optimizingSystem")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	/** The administration building the person is using. */
	private Administration office;

	public RoleType roleType;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public OptimizeSystem(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomInt(20));

		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = Administration.getAvailableOffice(person);
			if (officeBuilding != null) {
				// Walk to the office building.
				walkToTaskSpecificActivitySpotInBuilding(officeBuilding, FunctionType.ADMINISTRATION, false);
				office = officeBuilding.getAdministration();
				if (!office.isFull()) {
					office.addStaff();
					// Walk to the dining building.
					walkToTaskSpecificActivitySpotInBuilding(officeBuilding, FunctionType.ADMINISTRATION, true);
				}
			}
			else {
				Building building = getAvailableBuildingSpot(person, FunctionType.POWER_GENERATION);
				// Walk to that building.
				walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.POWER_GENERATION, true);
	
			}
			// Initialize phase
			addPhase(OPTIMIZING_SYSTEM);
			setPhase(OPTIMIZING_SYSTEM);
			
		} else if (person.isInVehicle()) {

			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
			
				// Initialize phase
				addPhase(OPTIMIZING_SYSTEM);
				setPhase(OPTIMIZING_SYSTEM);
			}
		}

		else {
			endTask();
		}

	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (OPTIMIZING_SYSTEM.equals(getPhase())) {
			return optimizingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the optimizing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double optimizingPhase(double time) {
		return 0;
	}

	/**
	 * Releases office space.
	 */
	@Override
	protected void clearDown() {
		// nothing.
	}

	/**
	 * Gets an available building with a function type.
	 * 
	 * @param person
	 * @return
	 */
	public static Building getAvailableBuildingSpot(Person person, FunctionType functionType) {
		Building result = null;

		// If person is in a settlement, try to find a building with a function type.
		if (person.isInSettlement()) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List<Building> bldgs = buildingManager.getBuildings(functionType); 
			bldgs = BuildingManager.getNonMalfunctioningBuildings(bldgs);
			bldgs = BuildingManager.getLeastCrowdedBuildings(bldgs);

			if (bldgs.size() > 0) {
				Map<Building, Double> selectedBldgs = BuildingManager.getBestRelationshipBuildings(person, bldgs);
				result = RandomUtil.getWeightedRandomObject(selectedBldgs);
			}
		}

		return result;
	}
}
