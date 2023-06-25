/*
 * Mars Simulation Project
 * AbstractMetaMission.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Default implementation of the MetaMission interface. Provides 
 * default implementations.
 */
public class AbstractMetaMission implements MetaMission {
	private static MasterClock masterClock;
	private static MissionManager missionMgr;

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
	 * Checks the suitability for this Person to be the leader. It currently checks their Job.
	 * 
	 * @param person
	 * @return
	 */
	@Override
	public double getLeaderSuitability(Person person) {
		double result = 0.25D;
		
		JobType jt = person.getMind().getJob();
		
		// If the person has a job and it is a preferred job
		// OR there are no preferred Jobs then give it a boost
		if ((jt != null) &&
				((preferredLeaderJob  == null) || preferredLeaderJob.contains(jt))) {
			result = 1D;
		}
		
		return result;
	}

	protected Set<JobType> getPreferredLeaderJob() {
		return preferredLeaderJob;
	}

	/**
	 * Get the current time on Mars
	 * @return
	 */
	protected MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}

	/**
	 * Get the score modifier for a Settlement based on it's population for this type of mission
	 * @param target The settlemetn to check
	 * @param capacity The number of person needed for capacity calcs
	 */
	protected double getSettlementPopModifier(Settlement target, int capacity) {
		int numEmbarking = MissionUtil.numEmbarkingMissions(target);
	    int numThisMission = missionMgr.numParticularMissions(type, target);
		int pop = target.getNumCitizens();
				
		// Check for # of embarking missions.
	    if (Math.max(1, pop / capacity) < numEmbarking) {
	    	return 0;
	    }

		// 
	    if (numThisMission > Math.max(1, pop / capacity)) {
	    	return 0;
	    }
		
	    int f1 = numEmbarking + 1;
	    int f2 = numThisMission + 1;
		
	    return (double)Math.max(1, pop / capacity) / f1 / f2;
	}
	
    public static void initializeInstances(MasterClock mc, MissionManager m) {
		masterClock = mc;
		missionMgr = m;
    }
}
