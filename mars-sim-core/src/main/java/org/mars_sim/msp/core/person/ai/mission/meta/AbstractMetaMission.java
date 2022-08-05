/*
 * Mars Simulation Project
 * AbstractMetaMission.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Default implementation of the MetaMission interface. Provides 
 * default implementations.
 */
public class AbstractMetaMission implements MetaMission {
	private String name;
	private MissionType type;
	private Set<JobType> preferredLeaderJob = null;
	
	/**
	 * Creates a new Mission meta instance
	 * @param type 
	 * @param preferredLeaderJob Jobs that a leader should have; null means no preference
	 */
	protected AbstractMetaMission(MissionType type, Set<JobType> preferredLeaderJob) {
		super();
		this.type = type;
		this.preferredLeaderJob = preferredLeaderJob;
		this.name = type.getName();
	}

	@Override
	public MissionType getType() {
		return type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		throw new UnsupportedOperationException("Mission Meta "+ name + " does not support mission for Person.");
	}

	@Override
	public Mission constructInstance(Robot robot) {
		throw new UnsupportedOperationException("Mission Meta "+ name + " does not support mission for Robots.");
	}

	@Override
	public double getProbability(Person person) {
		return 0;
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}

	/**
	 * Check the suitability for this Person to be the leader. It currently checks their Job
	 * @param person
	 * @return
	 */
	@Override
	public double getLeaderSuitability(Person person) {
		double result = 0.25D;
		
		JobType jt = person.getMind().getJob();
		
		// If the person has a job and it is a preferred job
		// OR there are no preferred Jobs then give it  boost
		if ((jt != null) &&
				((preferredLeaderJob  == null) || preferredLeaderJob.contains(jt))) {
			result = 1D;
		}
		
		return result;
	}

	protected Set<JobType> getPreferredLeaderJob() {
		return preferredLeaderJob;
	}
}
