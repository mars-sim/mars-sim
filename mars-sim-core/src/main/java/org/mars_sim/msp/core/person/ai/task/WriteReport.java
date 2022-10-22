/*
 * Mars Simulation Project
 * WriteReport.java
 * @date 2022-08-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

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
 * The WriteReport class is a task for writing reports in an office space
 */
public class WriteReport extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.writeReport"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WRITING_REPORT = new TaskPhase(Msg.getString("Task.phase.writingReport")); //$NON-NLS-1$

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
	public WriteReport(Person person) {
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
				Building dining = BuildingManager.getAvailableDiningBuilding(person, false);
				// Note: dining building is optional
				if (dining != null) {
					// Walk to the dining building.
					walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
				}
//				else {
//					// work anywhere
//				}				
			}
			// Initialize phase
			addPhase(WRITING_REPORT);
			setPhase(WRITING_REPORT);
			
			// set the boolean to true so that it won't be done again today
//			person.getPreference().setTaskDue(this, true);
			// }
		} else if (person.isInVehicle()) {

			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);

				// set the boolean to true so that it won't be done again today
//				person.getPreference().setTaskDue(this, true);
				
				// Initialize phase
				addPhase(WRITING_REPORT);
				setPhase(WRITING_REPORT);
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
		} else if (WRITING_REPORT.equals(getPhase())) {
			return writingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the writing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double writingPhase(double time) {
		// Do nothing
		return 0D;
	}

	/**
	 * Release office space
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}

//	/**
//	 * Gets an available building with the administration function.
//	 * 
//	 * @param person the person looking for the office.
//	 * @return an available office space or null if none found.
//	 */
//	public static Building getAvailableOffice(Person person) {
//		Building result = null;
//
//		// If person is in a settlement, try to find a building with an office.
//		if (person.isInSettlement()) {
//			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
//			List<Building> offices = buildingManager.getBuildings(FunctionType.ADMINISTRATION);
//			offices = BuildingManager.getNonMalfunctioningBuildings(offices);
//			offices = BuildingManager.getLeastCrowdedBuildings(offices);
//
//			if (offices.size() > 0) {
//				Map<Building, Double> selectedOffices = BuildingManager.getBestRelationshipBuildings(person, offices);
//				result = RandomUtil.getWeightedRandomObject(selectedOffices);
//			}
//		}
//
//		return result;
//	}
}
