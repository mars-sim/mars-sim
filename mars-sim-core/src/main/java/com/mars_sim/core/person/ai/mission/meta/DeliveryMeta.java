/*
 * Mars Simulation Project
 * DeliveryMeta.java
 * @date 2021-08-28
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Comparator;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.Deal;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.CargoRangeComparator;

/**
 * A meta mission for the delivery mission.
 */
public class DeliveryMeta extends AbstractMetaMission {

	private static final Set<JobType> LEADER_JOBS = Set.of(JobType.TRADER);
	private static final Set<JobType> WORKER_JOBS = Set.of(JobType.PILOT);

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DeliveryMeta.class.getName());
    
    private static final int VALUE = 1;
	/** Starting sol for this mission to commence. */
	private static final int MIN_STARTING_SOL = 4;
	
    private static final double DIVISOR = 10;


	DeliveryMeta() {
		// Everyone can start Delivery ??
		super(MissionType.DELIVERY, 3, LEADER_JOBS, WORKER_JOBS);

		setPreferredRobots(Set.of(RobotType.DELIVERYBOT));
		setPreferredVehicle(Set.of(VehicleType.DELIVERY_DRONE));
		setPopulationRatio(6);
		setSolThreshold(MIN_STARTING_SOL);
	}

	/**
	 * Get the Vehicle comparator that is based on largest cargo
	 */
	@Override
	protected Comparator<Vehicle> getVehicleComparator() {
		return new CargoRangeComparator();
	}
	
	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new Delivery(person, needsReview);
	}

	@Override
	public RatingScore getProbability(Person person) {

		RatingScore missionProbability = RatingScore.ZERO_RATING;

		// Check if mission is possible for person based on their circumstance.
		Settlement settlement = person.getAssociatedSettlement();

		RoleType roleType = person.getRole().getType();
		if (RoleType.CHIEF_OF_SUPPLY_RESOURCE == roleType
				|| RoleType.MISSION_SPECIALIST == roleType
				|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
				|| RoleType.RESOURCE_SPECIALIST == roleType
				|| RoleType.SUB_COMMANDER == roleType
				|| RoleType.COMMANDER == roleType
				) {
			
				// Note: checkMission() gives rise to a NULLPOINTEREXCEPTION that points to
				// Inventory
				// It happens only when this sim is a loaded saved sim.
				missionProbability = getSettlementProbability(settlement); 
			
		} 
		
		if (missionProbability.getScore() <= 0)
			return missionProbability;
	
		// if introvert, score  0 to  50 --> -2 to 0
		// if extrovert, score 50 to 100 -->  0 to 2
		// Reduce probability if introvert
		int extrovert = person.getExtrovertmodifier();
		missionProbability.addModifier(PERSON_EXTROVERT, (1D + extrovert/2D));
		missionProbability.applyRange(0, LIMIT);
		return missionProbability;
	}

	
	/**
	 * Gets the settlement contribution of the probability of the delivery mission.
	 * 
	 * @param settlement
	 * @return
	 */
	private RatingScore getSettlementProbability(Settlement settlement) {
		// Future: all drones offer the same range (unless it can be retrofitted/customized

		// Check for the best delivery settlement within range.
		var drone = selectVehicle(settlement);
		if (drone == null) {
			return RatingScore.ZERO_RATING;
		}
		
		logger.info(drone, 10_000L, "Available for delivery mission.");
		GoodsManager gManager = settlement.getGoodsManager();

		Deal deal = gManager.getBestDeal(MissionType.DELIVERY, drone);
		if (deal == null) {
			return RatingScore.ZERO_RATING;
		}

		double deliveryProfit = deal.getProfit() * VALUE;

		// Delivery value modifier.
		RatingScore missionProbability = new RatingScore(deliveryProfit / DIVISOR);
		missionProbability = MetaTask.applyCommerceFactor(missionProbability, settlement, CommerceType.TRADE);
		missionProbability.applyRange(0, Delivery.MAX_STARTING_PROBABILITY);
		
		// Crowding modifier.
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0) {
			missionProbability.addModifier(OVER_CROWDING, crowding + 1D);
		}

		return missionProbability;
	}
}
