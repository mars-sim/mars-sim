/*
 * Mars Simulation Project
 * Delivery.java
 * @date 2021-10-20
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.goods.CommerceMission;
import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Deal;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.objectives.TradeObjective;
import com.mars_sim.core.mission.task.NegotiateTrade;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.comparators.CargoRangeComparator;

/**
 * A mission for delivery between two settlements
 */
public class Delivery extends DroneMission implements CommerceMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Delivery.class.getName());

	/** Mission phases. */
	private static final MissionPhase TRADE_DISEMBARKING = new MissionPhase("Mission.phase.deliveryDisembarking");
	private static final MissionPhase TRADE_NEGOTIATING = new MissionPhase("Mission.phase.deliveryNegotiating");
	public static final MissionPhase UNLOAD_GOODS = new MissionPhase("Mission.phase.unloadGoods");
	public static final MissionPhase LOAD_GOODS = new MissionPhase("Mission.phase.loadGoods");
	private static final MissionPhase TRADE_EMBARKING = new MissionPhase("Mission.phase.deliveryEmbarking");

	// Static members
	public static final double MAX_STARTING_PROBABILITY = 100D;
	public static final int MAX_MEMBERS = 3;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TRADE_CENTER);

	// Data members.
	private boolean outbound;

	private NegotiateTrade negotiationTask;
	private Person missionTrader;
	private TradeObjective objective;

	/**
	 * Constructor. Started by DeliveryMeta.
	 *
	 * @param startingMember the mission member starting the settlement.
	 */
	public Delivery(Worker startingMember, boolean needsReview) {
		// Use DroneMission constructor.
		super(MissionType.DELIVERY, startingMember, null);

		// Problem starting Mission
		if (isDone()) {
			return;
		}

		Settlement s = startingMember.getSettlement();
		// Set the mission capacity.
		setMissionCapacity(MAX_MEMBERS);

		outbound = true;

		if (!isDone() && s != null) {
			// Get trading settlement
			Deal deal = s.getGoodsManager().getBestDeal(MissionType.DELIVERY, getVehicle());
			if (deal == null) {
				endMission(NO_TRADING_SETTLEMENT);
				return;
			}

			var tradingSettlement = deal.getBuyer();
			addNavpoint(tradingSettlement);

			objective = new TradeObjective(tradingSettlement, deal.getBuyingLoad(), deal.getSellingLoad(), deal.getProfit());
			addObjective(objective);
			
			// Recruit additional members to mission.
			recruitMembersForMission(startingMember, true, 2);

			if (!isDone()) {
				// Set initial phase
				setInitialPhase(needsReview);
			}
		}
	}

	/**
	 * Constructor 2. Started by MissionDataBean
	 *
	 * @param members
	 * @param tradingSettlement
	 * @param drone
	 * @param description
	 * @param sellGoods
	 * @param buyGoods
	 */
	public Delivery(Worker startingMember, Collection<Worker> members, Settlement tradingSettlement,
			Drone drone, Map<Good, Integer> sellGoods, Map<Good, Integer> buyGoods) {
		// Use DroneMission constructor.
		super(MissionType.DELIVERY, startingMember, drone);

		outbound = true;

		// Add mission members.
		addMembers(members, true);

		// Sets the mission capacity.
		setMissionCapacity(MAX_MEMBERS);

		// Set mission destination.
		addNavpoint(tradingSettlement);

		// Set trade goods.
		var profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getVehicle(), tradingSettlement, buyGoods, sellGoods);
		
		objective = new TradeObjective(tradingSettlement, new HashMap<>(buyGoods), sellGoods, profit);
		addObjective(objective);

		// Set initial phase
		setInitialPhase(false);
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 */
	@Override
	protected boolean determineNewPhase() {
		if (super.determineNewPhase()) {
			return true;
		}

		// for me to handle
		var tradingSettlement = objective.getTradingVenue();
		boolean handled = true;
		var ph = getPhase();

		if (TRAVELLING.equals(ph)) {
			if (isCurrentNavpointSettlement()) {
				if (outbound) {
					// Outbound so at the trading Settlement
					getVehicle().transfer(tradingSettlement); 
				}
				startDisembarkingPhase(outbound ? TRADE_DISEMBARKING : DISEMBARKING);
			}
		}

		else if (TRADE_DISEMBARKING.equals(ph)) {
			setPhase(TRADE_NEGOTIATING, tradingSettlement.getName());
		}

		else if (TRADE_NEGOTIATING.equals(ph)) {
			setPhase(UNLOAD_GOODS, tradingSettlement.getName());
		}

		else if (UNLOAD_GOODS.equals(ph)) {
			if (isVehicleLoadable()) {
				// Start the loading
				prepareLoadingPlan(tradingSettlement);
				setPhase(LOAD_GOODS, tradingSettlement.getName());
			}
			else {
				endMission(CANNOT_LOAD_RESOURCES);
			}
		}

		else if (LOAD_GOODS.equals(ph)) {
			setPhase(TRADE_EMBARKING, tradingSettlement.getName());
		}
		else if (TRADE_EMBARKING.equals(ph)) {
			startTravellingPhase();
		}
		else {
			handled = false;
		}

		return handled;
	}

	@Override
	protected void performPhase(Worker member) {
		super.performPhase(member);
		if (TRADE_DISEMBARKING.equals(getPhase())) {
			performDeliveryDisembarkingPhase();
		} else if (TRADE_NEGOTIATING.equals(getPhase())) {
			performDeliveryNegotiatingPhase(member);
		} else if (UNLOAD_GOODS.equals(getPhase())) {
			performDestinationUnloadGoodsPhase();
		} else if (LOAD_GOODS.equals(getPhase())) {
			performDestinationLoadGoodsPhase();
		} else if (TRADE_EMBARKING.equals(getPhase())) {
			computeTotalDistanceProposed();
			performDeliveryEmbarkingPhase(member);
		}
	}

	/**
	 * Performs the trade disembarking phase.
	 *
	 * @param member the mission member performing the mission.
	 */
	private void performDeliveryDisembarkingPhase() {
		Vehicle v = getVehicle();
		// If drone is not parked at settlement, park it.
		if ((v != null) && (v.getSettlement() == null)) {
			var tradingSettlement = objective.getTradingVenue();

			tradingSettlement.addVicinityVehicle(v);

			// Add vehicle to a garage if available.
			tradingSettlement.getBuildingManager().addToGarage(v);
		}

		setPhaseEnded(true);
	}

	/**
	 * Perform the delivery negotiating phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performDeliveryNegotiatingPhase(Worker member) {
		if (member == getMissionTrader()) {
			if (negotiationTask != null) {
				if (negotiationTask.isDone()) {
					var buyLoad = negotiationTask.getBuyLoad();
					double profit = 0D;
					if (buyLoad != null) {
						profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getDrone(),
										objective.getTradingVenue(), buyLoad, objective.getSell());
					}
					else {
						buyLoad = Collections.emptyMap();
					}
					objective.updateBought(buyLoad, profit);

					fireMissionUpdate(Trade.BUY_LOAD_EVENT);
					setPhaseEnded(true);
				}
			}

			else {
				Person settlementTrader = getSettlementBuyer();

				if (settlementTrader != null) {
					if (member instanceof Person person) {
						negotiationTask = new NegotiateTrade(settlementTrader, person, false,
															getVehicle(), objective.getSell());
						assignTask(person, negotiationTask);
					}
				}
				else if (getPhaseTimeElapse() > 1000D) {
					objective.updateBought(new HashMap<>(), 0D);
					fireMissionUpdate(Trade.BUY_LOAD_EVENT);
					setPhaseEnded(true);
				}
			}
		}

		if (getPhaseEnded()) {
			outbound = false;
			var tradingSettlement = objective.getTradingVenue();
			resetToReturnTrip(
					new NavPoint(tradingSettlement, null),
					new NavPoint(getStartingSettlement(), tradingSettlement.getCoordinates()));

			getStartingSettlement().getGoodsManager().clearDeal(MissionType.TRADE);
		}
	}

	/**
	 * Perform the unload goods phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performDestinationUnloadGoodsPhase() {

		// Unload drone if necessary.
		if (!getDrone().haveStatusType(StatusType.UNLOADING)) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the load goods phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performDestinationLoadGoodsPhase() {

		if (isDone() || isVehicleLoaded()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the delivery embarking phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performDeliveryEmbarkingPhase(Worker member) {

		// If person is not aboard the drone, board drone.
		if (!isDone()) {

			if (member instanceof Person pilot && pilot.isDeclaredDead()) {
				logger.info(pilot, "No longer alive. Switching to another pilot.");
				// Pick another member to head the delivery
				var newP = Trade.getMostSkilled(getMembers(), Collections.emptyList(), SkillType.PILOTING, false);
				setStartingMember(newP);
			}		

			Vehicle v = getVehicle();

			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not transfer to Surface");
			}
		}
	}

		/**
	 * Add trading equipment to the optional load list.
	 */
	@Override
	protected Map<Integer, Integer> getOptionalEquipmentToLoad() {
		Map<Integer, Integer> result = super.getOptionalEquipmentToLoad();
		return objective.addEquipmentToLoad(result, outbound);
	}

	/**
	 * Add the trading resources to the optional load list
	 */
	@Override
	protected Map<Integer, Number> getOptionalResourcesToLoad() {
		Map<Integer, Number> result = super.getOptionalResourcesToLoad();
		return objective.addResourcesToLoad(result, outbound);
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	/**
	 * Gets the trader for the mission.
	 *
	 * @return the trader.
	 */
	private Person getMissionTrader() {
		if (missionTrader == null) {
			missionTrader = (Person) Trade.getMostSkilled(getMembers(), Collections.emptyList(),
									SkillType.TRADING, true);
		}
		return missionTrader;
	}

	/**
	 * Gets the trader and the destination settlement for the mission.
	 *
	 * @return the trader.
	 */
	private Person getSettlementBuyer() {
		return (Person) Trade.getMostSkilled(objective.getTradingVenue().getIndoorPeople(), getMembers(),
									SkillType.TRADING, true);

	}

	/**
	 * Get the Vehicle comparator that is based on largest cargo
	 */
	@Override
	protected  Comparator<Vehicle> getVehicleComparator() {
		return new CargoRangeComparator();
	}
	
	/**
	 * Gets the settlement that the starting settlement is trading with.
	 *
	 * @return trading settlement.
	 */
	public Settlement getTradingSettlement() {
		return objective.getTradingVenue();
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
