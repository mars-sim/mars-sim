/*
 * Mars Simulation Project
 * DelegateWork.java
 * @date 2023-06-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

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
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The task for delegating work.
 */
public class DelegateWork extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.delegateWork"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase COMPILING = new TaskPhase(Msg.getString("Task.phase.compilingWork")); //$NON-NLS-1$

	private static final TaskPhase SELECTING = new TaskPhase(Msg.getString("Task.phase.selectingWork")); //$NON-NLS-1$

	private static final TaskPhase ASSIGNING = new TaskPhase(Msg.getString("Task.phase.assigningWork")); //$NON-NLS-1$

	
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
	public DelegateWork(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomInt(20));

		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = BuildingManager.getAvailableAdminBuilding(person);
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
				Building dining = BuildingManager.getAvailableDiningBuilding(person, false);
				// Note: dining building is optional
				if (dining != null) {
					// Walk to the dining building.
					walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
				}
				// or else work anywhere			
			}
			 
			// Initialize phase
			addPhase(COMPILING);
			addPhase(SELECTING);
			addPhase(ASSIGNING);
			setPhase(COMPILING);
			
			// Set the boolean to true so that it won't be done again today
			// call person.getPreference().setTaskDue(this, true);

		} else if (person.isInVehicle()) {

			if (VehicleType.isRover(person.getVehicle().getVehicleType())) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
	
				// Initialize phase
				addPhase(COMPILING);
				addPhase(SELECTING);
				addPhase(ASSIGNING);
				setPhase(COMPILING);
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
		} else if (COMPILING.equals(getPhase())) {
			return compilingPhase(time);
		} else if (SELECTING.equals(getPhase())) {
			return selectingPhase(time);
		} else if (ASSIGNING.equals(getPhase())) {
			return assigningPhase(time);			
		} else {
			return time;
		}
	}

	/**
	 * Performs the compiling phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double compilingPhase(double time) {
		// Do nothing
		return 0D;
	}

	/**
	 * Performs the selecting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double selectingPhase(double time) {
		// Do nothing
		return 0D;
	}
	
	/**
	 * Performs the assigning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double assigningPhase(double time) {
		// Do nothing
		return 0D;
	}
	
	/**
	 * Releases the office space.
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}
