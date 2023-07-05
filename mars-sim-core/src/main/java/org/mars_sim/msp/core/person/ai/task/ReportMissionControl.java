/*
 * Mars Simulation Project
 * ReportMissionControl.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;


import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The ReportMissionControl class is a task for writing reports in an office space
 */
public class ReportMissionControl extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.reportMissionControl"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPORTING = new TaskPhase(Msg.getString("Task.phase.reportMissionControl")); //$NON-NLS-1$

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
	public ReportMissionControl(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 10D + RandomUtil.getRandomInt(20));

		if (person.isInSettlement()) {

			// If person is in a settlement, try to find an office building.
			Building officeBuilding = Management.getAvailableStation(person);
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
			addPhase(REPORTING);
			setPhase(REPORTING);
			
			// set the boolean to true so that it won't be done again today
//			person.getPreference().setTaskDue(this, true);
			// }
		} else if (person.isInVehicle()) {

			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);

				// set the boolean to true so that it won't be done again today
//				person.getPreference().setTaskDue(this, true);
				
				// Initialize phase
				addPhase(REPORTING);
				setPhase(REPORTING);
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
		} else if (REPORTING.equals(getPhase())) {
			return reporting(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the reporting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reporting(double time) {

		if (getTimeCompleted() <= time * 2)
			// Only print this at the beginning
			person.getReportingAuthority().getMissionAgenda().reportFindings(worker);
		
		return 0D;
	}

	/**
	 * Releases office space.
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}
