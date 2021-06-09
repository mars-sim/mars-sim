/**
 * Mars Simulation Project
 * ReviewJobReassignment.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The ReviewJobReassignment class is a task for reviewing job reassignment
 * submission in an office space
 */
public class ReviewJobReassignment extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(ReviewJobReassignment.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.reviewJobReassignment"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REVIEWING = new TaskPhase(
			Msg.getString("Task.phase.reviewJobReassignment")); //$NON-NLS-1$

	private static final TaskPhase FINISHED = new TaskPhase(
			Msg.getString("Task.phase.reviewJobReassignment.finished")); //$NON-NLS-1$

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
		
		if (person.isInside()) {

			if (roleType != null && roleType == RoleType.PRESIDENT || roleType == RoleType.MAYOR
					|| roleType == RoleType.COMMANDER || roleType == RoleType.SUB_COMMANDER) {

				// If person is in a settlement, try to find an office building.
				Building officeBuilding = Administration.getAvailableOffice(person);

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
					Building dining = EatDrink.getAvailableDiningBuilding(person, false);
					// Note: dining building is optional
					if (dining != null) {
						// Walk to the dining building.
						walkToTaskSpecificActivitySpotInBuilding(dining, FunctionType.DINING, true);
					}
//					else {
//						// work anywhere
//					}				
				}
				
				// TODO: if administration building is not available, go to 

			} // end of roleType
			else {
				endTask();
			}
		}
		else {
			endTask();
		}

		// Initialize phase
		addPhase(REVIEWING);
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
	 * Performs the reviewingPhasephase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reviewingPhase(double time) {
		// Iterates through each person
		Iterator<Person> i = person.getAssociatedSettlement().getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person tempPerson = i.next();
			List<JobAssignment> list = tempPerson.getJobHistory().getJobAssignmentList();
			int last = list.size() - 1;
			JobAssignmentType status = list.get(last).getStatus();

			if (status != null && status == JobAssignmentType.PENDING) {
				JobType pendingJob = list.get(last).getJobType();
				JobType lastJob = null;
				if (last == 0)
					lastJob = pendingJob;
				else
					lastJob = list.get(last - 1).getJobType();
				String approvedBy = person.getRole().getType() + " " + person.getName();

				// 1. Reviews requester's cumulative job rating
				double rating = list.get(last).getJobRating();
				double cumulative_rating = 0;
				int size = list.size();
				for (int j = 0; j < size; j++) {
					cumulative_rating += list.get(j).getJobRating();
				}
				cumulative_rating = cumulative_rating / size;

				// TODO: Add more depth to this process
				// 2. Reviews this person's preference 
				// 3. Go to him/her to have a chat
				// 4. Modified by the affinity between them
				// 5. Approve/disapprove the job change
								
				if (rating < 2.5 || cumulative_rating < 2.5) {
					tempPerson.getMind().reassignJob(lastJob, true, JobUtil.USER,
							JobAssignmentType.NOT_APPROVED, approvedBy);

					logger.log(worker, Level.INFO, 3000, "Did NOT approve " + tempPerson
							+ "'s job reassignment as " + pendingJob);

				} else {

					// Updates the job
					tempPerson.getMind().reassignJob(pendingJob, true, JobUtil.USER,
							JobAssignmentType.APPROVED, approvedBy);
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
	 * Release Office space
	 */
	@Override
	protected void clearDown() {
		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}
}
