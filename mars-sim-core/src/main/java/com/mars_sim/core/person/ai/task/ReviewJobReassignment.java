/*
 * Mars Simulation Project
 * ReviewJobReassignment.java
 * @date 2023-06-17
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Administration;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.Assignment;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is a task for reviewing job reassignment
 * submission in an office space.
 */
public class ReviewJobReassignment extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(ReviewJobReassignment.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.reviewJobReassignment"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REVIEWING = new TaskPhase(
			Msg.getString("Task.phase.reviewJobReassignment")); //$NON-NLS-1$


	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The administration building the person is using. */
	private Administration office;

	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ReviewJobReassignment(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, 20D + RandomUtil.getRandomDouble(5D) - RandomUtil.getRandomDouble(5D));

		RoleType roleType = person.getRole().getType();
		
		if (person.isInSettlement()) {

			if (roleType != null && (roleType == RoleType.PRESIDENT 
					|| roleType == RoleType.MAYOR
					|| roleType == RoleType.ADMINISTRATOR
					|| roleType == RoleType.DEPUTY_ADMINISTRATOR
					|| roleType == RoleType.COMMANDER 
					|| roleType == RoleType.SUB_COMMANDER)) {

				// If person is in a settlement, try to find an office building.
				Building officeBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);

				// Note: office building is optional
				if (officeBuilding != null) {
					// Walk to the office building.
					office = officeBuilding.getAdministration();
					if (!office.isFull()) {
						office.addStaff();
						// Walk to the office building.
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
//					else {
//						// work anywhere
//					}				
				}
				
				// if administration building is not available, go to 

			} // end of roleType
			else {
				endTask();
			}
		}
		else {
			endTask();
		}

		// Initialize phase
		setPhase(REVIEWING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REVIEWING.equals(getPhase())) {
			return reviewingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the reviewing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reviewingPhase(double time) {
		// Iterates through each person
		Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person tempPerson = i.next();
			List<HistoryItem<Assignment>> list = tempPerson.getJobHistory().getJobAssignmentList();
			int last = list.size() - 1;
			AssignmentType status = list.get(last).getWhat().getStatus();

			if (status != null && status == AssignmentType.PENDING) {
				JobType pendingJob = list.get(last).getWhat().getType();
				JobType lastJob = null;
				if (last == 0)
					lastJob = pendingJob;
				else
					lastJob = list.get(last - 1).getWhat().getType();
				String approvedBy = person.getRole().getType() + " " + person.getName();

				// 1. Reviews requester's cumulative job rating
				double rating = list.get(last).getWhat().getJobRating();
				double cumulative_rating = 0;
				int size = list.size();
				for (int j = 0; j < size; j++) {
					cumulative_rating += list.get(j).getWhat().getJobRating();
				}
				cumulative_rating = cumulative_rating / size;

				// TODO: Add more depth to this process
				// 2. Reviews this person's preference 
				// 3. Go to him/her to have a chat
				// 4. Modified by the affinity between them
				// 5. Approve/disapprove the job change
								
				if (rating < 2.5 || cumulative_rating < 2.5) {
					tempPerson.getMind().reassignJob(lastJob, true, JobUtil.USER,
							AssignmentType.NOT_APPROVED, approvedBy);

					logger.log(worker, Level.INFO, 3000, "Did NOT approve " + tempPerson
							+ "'s job reassignment as " + pendingJob);

				} else {

					// Updates the job
					tempPerson.getMind().reassignJob(pendingJob, true, JobUtil.USER,
							AssignmentType.APPROVED, approvedBy);
					logger.log(worker, Level.INFO, 3000, "Approved " + tempPerson
							+ "'s job reassignment as " + pendingJob);
				}
				
				addExperience(time);
				
				// Do only one review each time
				break;
			}
		} // end of while
		
		return 0;
	}

	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = worker.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        worker.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);
	}

	/**
	 * Releases Office space.
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}
