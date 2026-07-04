/*
 * Mars Simulation Project
 * AbstractMetaMission.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

/**
 * Default implementation of the MetaMission interface. Provides 
 * default implementations.
 */
public class AbstractMetaMission implements MetaMission {

	protected static final String GOODS = "goods";
	protected static final String LEADER = "leader";
	protected static final String PERSON_EXTROVERT = "extrovert";
	protected static final String OVER_CROWDING = "crowding";
	protected static final String DEMAND_PROBABILITY = "demand";

	private static MasterClock masterClock;

	private String name;
	private MissionType type;
	private int minimum;
	private int capacity;
	private Set<JobType> preferredLeaderJob;
	private Set<JobType> preferredWorkerJob;
	private Set<RobotType> preferredRobots = Collections.emptySet();  // Optional so empty by default
	
	/**
	 * Creates a new Mission meta instance.
	 * 
	 * @param type 
	 * @param preferredLeaderJob Jobs that a leader should have; null means no preference
	 */
	protected AbstractMetaMission(MissionType type, int minimum,int capacity, Set<JobType> preferredLeaderJob, Set<JobType> preferredWorkerJob) {
		super();
		this.type = type;
		this.preferredLeaderJob = preferredLeaderJob;
		this.preferredWorkerJob = preferredWorkerJob;
		this.name = type.getName();
		this.capacity = capacity;
		this.minimum = minimum;
	}

	protected void setPreferredRobots(Set<RobotType> preferredRobots) {
		this.preferredRobots = preferredRobots;
	}

	@Override
	public MissionType getType() {
		return type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	protected Set<JobType> getPreferredLeaderJob() {
		return preferredLeaderJob;
	}

	protected Set<JobType> getPreferredWorkerJobs() {
		return preferredWorkerJob;
	}
	
	/** 
	 * Gets the default capacity for a mission of this type.
	 */
	@Override
	public int getDefaultCapacity() {
		return capacity;
	}

	@Override
	public int getMinimumMembers() {
		return minimum;
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		throw new UnsupportedOperationException("Mission Meta "+ name + " does not support mission for Person.");
	}

	@Override
	public RatingScore getProbability(Person person) {
		return RatingScore.ZERO_RATING;
	}

	/**
	 * Apply a modifier that is an average of two Commerce factors of a Settlement
	 * @param score Source score
	 * @param s Settlement in question
	 * @param type1 Type 1
	 * @param type2 Type 2
	 * @return
	 */
	protected static RatingScore applyCommerceAverage(RatingScore score, Settlement s,
										CommerceType type1, CommerceType type2) {
		var gMgr = s.getGoodsManager();
		score.addModifier(GOODS, (gMgr.getCommerceFactor(type1)
							+ gMgr.getCommerceFactor(type2))/1.5);
		return score;
	}

	/**
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param person
	 * @return
	 */
	@Override
	public double getLeaderSuitability(Person person) {
		JobType jt = person.getMind().getJobType();
		
		double lead = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
		double result = lead/100;
		
		// If the person has a job and it is a preferred job
		// OR there are no preferred Jobs then give it a boost
		if ((jt != null) &&
				(preferredLeaderJob.isEmpty() || preferredLeaderJob.contains(jt))) {
			result *= 1.25;
		}
		else
			result *= 0.5;
		
		return result;
	}

	/**
	 * Gets the mission qualification value for the member. Member is qualified in
	 * joining the mission if the value is larger than 0. The larger the
	 * qualification value, the more likely the member will be picked for the
	 * mission.
	 *
	 * @param member the member to check.
	 * @return mission qualification value.
	 */
	@Override
	public double getWorkerSuitability(Worker member) {

		double result;

		if (member instanceof Person person) {
			result = Math.max(5,  person.getMissionExperience(type));

			// Get base result for job modifier.
			JobType job = person.getMind().getJobType();
			double jobModifier = 2D;
			if (preferredWorkerJob.isEmpty() || preferredWorkerJob.contains(job)) {
				jobModifier = 3D;
			}

			result = result * jobModifier;
		}
		else {
			Robot robot = (Robot) member;

			// Get base result for job modifier.
			result = (preferredRobots.contains(robot.getRobotType())) ? 30.0 : 0;
		}

		return result;
	}

	/**
	 * Gets the current time on Mars.
	 * 
	 * @return
	 */
	protected MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}
	
    public static void initializeInstances(MasterClock mc) {
		masterClock = mc;
    }
}
