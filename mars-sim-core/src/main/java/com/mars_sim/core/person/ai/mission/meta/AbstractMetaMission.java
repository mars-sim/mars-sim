/*
 * Mars Simulation Project
 * AbstractMetaMission.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.robot.Robot;
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
	protected static final String SETTLEMENT_POPULATION = "population";
	protected static final String PERSON_EXTROVERT = "extrovert";
	protected static final String OVER_CROWDING = "crowding";
	protected static final String MINERALS = "minerals";

	private static MasterClock masterClock;
	private static MissionManager missionMgr;

	private String name;
	private MissionType type;
	private Set<JobType> preferredLeaderJob = null;
	
	/**
	 * Creates a new Mission meta instance.
	 * 
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
	public RatingScore getProbability(Person person) {
		return RatingScore.ZERO_RATING;
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}

	/**
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param person
	 * @return
	 */
	@Override
	public double getLeaderSuitability(Person person) {
		JobType jt = person.getMind().getJob();
		
		double lead = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
		double result = lead/100;
		
		// If the person has a job and it is a preferred job
		// OR there are no preferred Jobs then give it a boost
		if ((jt != null) &&
				((preferredLeaderJob == null) || preferredLeaderJob.contains(jt))) {
			result *= 1.25;
		}
		else
			result *= 0.5;
		
		return result;
	}

	protected Set<JobType> getPreferredLeaderJob() {
		return preferredLeaderJob;
	}

	/**
	 * Gets the current time on Mars.
	 * 
	 * @return
	 */
	protected MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}

	/**
	 * Gets the score modifier for a Settlement based on its population for this type of mission.
	 * 
	 * @param target The settlement to check
	 * @param modifier
	 */
	protected double getSettlementPopModifier(Settlement target, int modifier) {
		int numEmbarking = MissionUtil.numEmbarkingMissions(target);
	    int numThisMission = missionMgr.numParticularMissions(type, target);
		int pop = target.getNumCitizens();
		double value = Math.max(1.0, 1.0 * pop / modifier);	
		
	    if (numEmbarking > value) {
	    	return 0;
	    }

	    if (numThisMission > value) {
	    	return 0;
	    }
		
	    int f1 = 2 * numEmbarking;
	    int f2 = 3 * numThisMission;
		
	    return value / f1 / f2;
	}
	
    public static void initializeInstances(MasterClock mc, MissionManager m) {
		masterClock = mc;
		missionMgr = m;
    }
}
