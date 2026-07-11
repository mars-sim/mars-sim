/*
 * Mars Simulation Project
 * AbstractMetaMission.java
 * @date 2021-09-28
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.RangeComparator;

/**
 * Default implementation of the MetaMission interface. Provides 
 * default implementations.
 */
public abstract class AbstractMetaMission implements MetaMission {

	protected static final String GOODS = "goods";
	protected static final String LEADER = "leader";
	protected static final String PERSON_EXTROVERT = "extrovert";
	protected static final String OVER_CROWDING = "crowding";
	protected static final String DEMAND_PROBABILITY = "demand";

	private String name;
	private MissionType type;
	private int minimum;
	private int capacity;
	private Set<JobType> preferredLeaderJob;
	private Set<JobType> preferredWorkerJob;
	private Set<RobotType> preferredRobots = Collections.emptySet();  // Optional so empty by default
	private Set<VehicleType> preferredVehicle = Collections.emptySet();  // Optional so empty by default
	private int popRatio = 0;
	private int popThreshold;
	private int solThreshold = 0;
	private boolean automatic = true;
	
	/**
	 * Creates a new Mission meta instance.
	 * 
	 * @param type Type of mission
	 * @param capacity Default maximum capacity for this mission type
	 * @param preferredLeaderJob Jobs that a leader should have; null means no preference
	 * @param preferredWorkerJob Jobs that a worker should have; null means no preference
	 */
	protected AbstractMetaMission(MissionType type, int capacity, Set<JobType> preferredLeaderJob, Set<JobType> preferredWorkerJob) {
		super();
		this.type = type;
		this.preferredLeaderJob = preferredLeaderJob;
		this.preferredWorkerJob = preferredWorkerJob;
		this.name = type.getName();
		this.capacity = capacity;
		this.minimum = 2;

		// These are defaults
		this.popThreshold = capacity + 2;
		this.popRatio = capacity + 4;
	}

	/**
	 * Can this meta create missions automatically without user intervention.
	 */
	@Override
	public boolean isAutomatic() {
		return automatic;
	}

	protected void setAutomatic(boolean automatic) {
		this.automatic = automatic;
	}

	/**
	 * Override the default minimum members for this mission. Default is 2.
	 * @param minimum New limit.
	 */
	protected void setMinimumMembers(int minimum) {
		this.minimum = minimum;
	}

	/**
	 * Sets the preferred robot types for this mission. This is optional and can be left empty.
	 * By default no RobotTypes are preferred. If a RobotType is preferred then it will be given a higher suitability score.
	 * @param preferredRobots Robots to prefer
	 */
	protected void setPreferredRobots(Set<RobotType> preferredRobots) {
		this.preferredRobots = preferredRobots;
	}

	/**
	 * Sets the preferred vehicle types for this mission. This is optional and can be left empty.
	 * By default no VehicleTypes are preferred. If a VehicleType is preferred then it will be given a higher suitability score.
	 * @param preferredVehicle Vehicle types to prefer
	 */
	protected void setPreferredVehicle(Set<VehicleType> preferredVehicle) {
		this.preferredVehicle = preferredVehicle;
	}

	/**
	 * Gets the preferred vehicle types for this mission. This is optional and can be left empty.
	 * By default no VehicleTypes are preferred. If a VehicleType is preferred then it will be given a higher suitability score.
	 * @return Vehicle types to prefer
	 */
	@Override
	public Set<VehicleType> getPreferredVehicle() {
		return preferredVehicle;
	}

	/**
	 * By default the vehicle comparator is a RangeComparator. This can be overridden by subclasses to provide a different comparator.
	 * @return Comparator for vehicles for this mission.
	 */
	protected Comparator<Vehicle> getVehicleComparator() {
		return new RangeComparator();
	}

	/**
	 * Select the most suitable Vehicle for this mission.
	 * This will use the {@link #getVehicleComparator()} to rate the best vehicle from the settlement.
	 * If no vehicles are available then null is returned.
	 * @param settlement the settlement to search for vehicles.
	 */
	@Override
	public Vehicle selectVehicle(Settlement settlement) {

		if (preferredVehicle.isEmpty()) {
			// No vehicle required for this mission.
			return null;
		}

		// Get all vehicles of the correct type that are not reserved and are at the starting settlement.
		Collection<Vehicle> vList = settlement.getAllAssociatedVehicles().stream()
			.filter(v -> preferredVehicle.contains(v.getVehicleType()))
			.filter(v -> !v.isReservedForMission())
			.filter(Vehicle::isUsableVehicle)
			.filter(v -> settlement.equals(v.getSettlement()))
			.toList();
		
		return vList.stream()
			.sorted(getVehicleComparator().reversed())
			.findFirst().orElse(null);
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
	public Set<JobType> getPreferredLeaderJob() {
		return preferredLeaderJob;
	}

	@Override
	public Set<JobType> getPreferredWorkerJobs() {
		return preferredWorkerJob;
	}

	@Override
	public Set<RobotType> getPreferredRobots() {
		return preferredRobots;
	}
	
	/**
	 * Sets the minimum sol threshold for this mission. This is the minimum sol that must be reached before this mission can be created.
	 * @param solThreshold Minimum sol threshold
	 */
	protected void setSolThreshold(int solThreshold) {
		this.solThreshold = solThreshold;
	}

	/**
	 * Gets the minimum sol threshold for this mission. This is the minimum sol that must be reached before this mission can be created.
	 * @return Minimum sol threshold
	 */
	@Override
	public int getSolThreshold() {
		return solThreshold;
	}

	/**
	 * Gets the population threshold for this mission. This is the minimum number of citizens that must be present in a settlement before this mission can be created.
	 * @return
	 */
	@Override
	public int getPopThreshold() {
		return popThreshold;
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

	/**
	 * Once over the threshold how many further concurrent missions are allowed per number of citizens.
	 * @param popRatio Number of citizens per additional mission.
	 */
	protected void setPopulationRatio(int popRatio) {
		this.popRatio = popRatio;
	}

	/**
	 * Define the minimum number of citizens before this mission can be created.
	 * @param popThreshold Lowest citizen size.
	 */
	protected void setPopulationThreshold(int popThreshold) {
		this.popThreshold = popThreshold;
	}

	/**
	 * This calculates the maximum number of missions. The algorithm check the citizens is above the population threshold.
	 * If it is then the number of missions is 1 plus the number of citizens above the threshold divided by the population ratio.
	 */
	@Override
	public int getMaxMissions(int numCitizens) {
		if (numCitizens >= popThreshold) {
			return 1 + (int) Math.floor((numCitizens - popThreshold) / (double)popRatio);
		}
		return 0;
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
}
