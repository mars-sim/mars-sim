/*
 * Mars Simulation Project
 * DeliveryMeta.java
 * @date 2021-08-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.Rating;
import org.mars_sim.msp.core.goods.Deal;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.DroneMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Drone;

/**
 * A meta mission for the delivery mission.
 */
public class DeliveryMeta extends AbstractMetaMission {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DeliveryMeta.class.getName());
    
    private static final int VALUE = 1;
    
    private static final double DIVISOR = 10;


	DeliveryMeta() {
		// Everyone can start Delivery ??
		super(MissionType.DELIVERY, null);
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new Delivery(person, needsReview);
	}

	@Override
	public Rating getProbability(Person person) {

		Rating missionProbability = Rating.ZERO_RATING;

		// Check if mission is possible for person based on their circumstance.
		Settlement settlement = person.getAssociatedSettlement();

		if (settlement.isFirstSol())
			return missionProbability;
		
		RoleType roleType = person.getRole().getType();
		if (RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
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
		missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));
		
		missionProbability.applyRange(0, LIMIT);

		return missionProbability;
	}

	
	/**
	 * Gets the settlement contribution of the probability of the delivery mission.
	 * 
	 * @param settlement
	 * @return
	 */
	private Rating getSettlementProbability(Settlement settlement) {

		Rating missionProbability = Rating.ZERO_RATING;

		// Check for the best delivery settlement within range.
		Drone drone = (Drone) DroneMission.getDroneWithGreatestRange(settlement, false);
		if (drone == null) {
			return Rating.ZERO_RATING;
		}
		
		logger.info(drone, 10_000L, "Available for delivery mission.");
		GoodsManager gManager = settlement.getGoodsManager();

		Deal deal = gManager.getBestDeal(MissionType.DELIVERY, drone);
		if (deal == null) {
			return Rating.ZERO_RATING;
		}
		
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(MissionType.DELIVERY, settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 2.0) < numThisMission) {
			return Rating.ZERO_RATING;
		}			
		
		else if (numThisMission > 1)
			return Rating.ZERO_RATING;	

		double deliveryProfit = deal.getProfit() * VALUE;

		// Delivery value modifier.
		missionProbability = new Rating(deliveryProfit / DIVISOR * gManager.getTradeFactor());
		missionProbability.applyRange(0, Delivery.MAX_STARTING_PROBABILITY);
		
		int f2 = 2 * numThisMission + 1;
		
		missionProbability.addModifier("citizen", settlement.getNumCitizens() / f2 / 2D);
		
		// Crowding modifier.
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0) {
			missionProbability.addModifier(OVER_CROWDING, crowding + 1);
		}

		return missionProbability;
	}
}
