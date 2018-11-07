/**
 * Mars Simulation Project
 * Job.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.ListenToMusic;
import org.mars_sim.msp.core.person.ai.task.PlayHoloGame;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.ReviewMissionPlan;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Job class represents a person's job.
 */
public abstract class Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Probability penalty for starting a non-job-related task. */
	private static final double NON_JOB_TASK_PENALTY = .025D;
	/** Probability penalty for starting a non-job-related mission. */
	private static final double NON_JOB_MISSION_START_PENALTY = .25D;
	/** Probability penalty for joining a non-job-related mission. */
	private static final double NON_JOB_MISSION_JOIN_PENALTY = .5D;

	private static final String JOB_STR = "job.";
	private static final String MALE_STR = "male.";
	private static final String FEMALE_STR = "female.";
	private static final String UNKNOWN = "unknown.";

	// Domain members
	protected Class<? extends Job> jobClass;
	/** List of tasks related to the job. */
	protected List<Class<?>> jobTasks;
	/** List of missions to be started by a person with this job. */
	protected List<Class<?>> jobMissionStarts;
	/** List of missions to be joined by a person with this job. */
	protected List<Class<?>> jobMissionJoins;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the job.
	 */
	public Job(Class<? extends Job> jobClass) {
		this.jobClass = jobClass;
		jobTasks = new ArrayList<Class<?>>();
		jobMissionStarts = new ArrayList<Class<?>>();
		jobMissionJoins = new ArrayList<Class<?>>();
		
		// Every settler will need to tasks
		jobTasks.add(DigLocalIce.class);
		jobTasks.add(DigLocalRegolith.class);
		jobTasks.add(ListenToMusic.class);
		jobTasks.add(PlayHoloGame.class);
		jobTasks.add(Read.class);
		jobTasks.add(ReviewJobReassignment.class);
		jobTasks.add(ReviewMissionPlan.class);
		jobTasks.add(WriteReport.class);
	}

	/**
	 * Gets the job's internationalized name for display in user interface. This
	 * uses directly the name of the class that extends {@link Job}, so take care
	 * not to rename those, or if you do then remember to change the keys in
	 * <code>messages.properties</code> accordingly.
	 * 
	 * @param gender {@link GenderType}
	 * @return name
	 */
	public String getName(GenderType gender) {
		StringBuffer key = new StringBuffer().append(JOB_STR); // $NON-NLS-1$
		switch (gender) {
		case MALE:
			key.append(MALE_STR);
			break; // $NON-NLS-1$
		case FEMALE:
			key.append(FEMALE_STR);
			break; // $NON-NLS-1$
		default:
			key.append(UNKNOWN);
			break; // $NON-NLS-1$
		}
		key.append(jobClass.getSimpleName());
		return Msg.getString(key.toString()); // $NON-NLS-1$
	};

	public Class<? extends Job> getJobClass() {
		return this.jobClass;
	}

	/**
	 * Gets a person/robot's capability to perform this job.
	 * 
	 * @param person/robot the person/robot to check.
	 * @return capability (min 0.0).
	 */
	public abstract double getCapability(Person person);

	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	// public abstract double getCapability(Robot robot);

	/**
	 * Gets the probability modifier for starting a non-job-related task.
	 * 
	 * @param taskClass the task class
	 * @return modifier >= 0.0
	 */
	public double getStartTaskProbabilityModifier(Class<?> taskClass) {
		double result = 1D;
		if (!jobTasks.contains(taskClass))
			result = NON_JOB_TASK_PENALTY;
		return result;
	}

	/**
	 * Gets the probability modifier for starting a non-job-related mission.
	 * 
	 * @param missionClass the mission class
	 * @return modifier >= 0.0
	 */
	public double getStartMissionProbabilityModifier(Class<?> missionClass) {
		double result = 1D;
		if (!jobMissionStarts.contains(missionClass))
			result = NON_JOB_MISSION_START_PENALTY;
		return result;
	}

	/**
	 * Gets the probability modifier for joining a non-job-related mission.
	 * 
	 * @param missionClass the mission class
	 * @return modifier >= 0.0
	 */
	public double getJoinMissionProbabilityModifier(Class<?> missionClass) {
		double result = 1D;
		if (!jobMissionJoins.contains(missionClass))
			result = NON_JOB_MISSION_JOIN_PENALTY;
		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public abstract double getSettlementNeed(Settlement settlement);

	/**
	 * Checks if a task is related to this job.
	 * 
	 * @param taskClass the task class
	 * @return true if job related task.
	 */
	public boolean isJobRelatedTask(Class<?> taskClass) {
		return jobTasks.contains(taskClass);
	}
}