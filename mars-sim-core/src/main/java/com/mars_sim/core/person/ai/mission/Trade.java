/*
 * Mars Simulation Project
 * Trade.java
 * @date 2021-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Deal;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.objectives.TradeObjective;
import com.mars_sim.core.mission.task.NegotiateTrade;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.WalkingSteps;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A mission for trading between two settlements
 */
public class Trade extends RoverMission  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Trade.class.getName());

	/** Mission phases. */
	private static final MissionPhase TRADE_DISEMBARKING = new MissionPhase("Mission.phase.tradeDisembarking");
	private static final MissionPhase TRADE_NEGOTIATING = new MissionPhase("Mission.phase.tradeNegotiating");
	private static final MissionStatus NO_TRADING_SETTLEMENT = new MissionStatus("Mission.status.noTradeSettlement");
	private static final MissionPhase UNLOAD_SOLD_GOODS = new MissionPhase("Mission.phase.unloadGoods");
	private static final MissionPhase LOAD_BOUGHT_GOODS = new MissionPhase("Mission.phase.loadGoods");
	private static final MissionPhase TRADE_EMBARKING = new MissionPhase("Mission.phase.tradeEmbarking");
	private static final MissionStatus SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE = new MissionStatus("Mission.status.noSellingVehicle");

	// Static members
	public static final double MAX_STARTING_PROBABILITY = 100D;

	public static final int MAX_MEMBERS = 2;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TRADE_CENTER);

	private TradeObjective objective;

	private boolean outbound;

	private NegotiateTrade negotiationTask;

	private Person missionTrader;  // Should this be in the Objective to track who does the deal?

	/**
	 * Constructor. Started by TradeMeta
	 *
	 * @param startingMember the mission member starting the settlement.
	 */
	public Trade(Worker startingMember, boolean needsReview) {
		// Use RoverMission constructor.
		super(MissionType.TRADE, startingMember, null);

		// Problem setting up the mission
		if (isDone()) {
			return;
		}

		Settlement s = startingMember.getSettlement();
		// Set the mission capacity.
		if (getMissionCapacity() > MAX_MEMBERS) {
			setMissionCapacity(MAX_MEMBERS);
		}

		outbound = true;

		// Get trading settlement
		Deal deal = s.getGoodsManager().getBestDeal(MissionType.TRADE, getVehicle());
		if (deal == null) {
			endMission(NO_TRADING_SETTLEMENT);
			return;
		}
		var tradingSettlement = deal.getBuyer();
		addNavpoint(tradingSettlement);

		objective = new TradeObjective(tradingSettlement, deal.getBuyingLoad(), deal.getSellingLoad(), deal.getProfit());
		addObjective(objective);

		// Recruit additional members to mission.
		if (!isDone() && !recruitMembersForMission(startingMember, MAX_MEMBERS)) {
			return;
		}

		// Set initial phase
		setInitialPhase(needsReview);
	}

	/**
	 * Constructor 2. Started by MissionDataBean
	 *
	 * @param members
	 * @param startingSettlement
	 * @param tradingSettlement
	 * @param rover
	 * @param description
	 * @param sellGoods
	 * @param buyGoods
	 */
	public Trade(Collection<Worker> members, Settlement tradingSettlement,
			Rover rover, Map<Good, Integer> sellGoods, Map<Good, Integer> buyGoods) {
		// Use RoverMission constructor.
		super(MissionType.TRADE, (Worker) members.toArray()[0], rover);

		outbound = true;

		// Sets the mission capacity.
		if (getMissionCapacity() > MAX_MEMBERS) {
			setMissionCapacity(MAX_MEMBERS);
		}

		// Set mission destination.
		addNavpoint(tradingSettlement);

		addMembers(members, false);

		// Set trade goods.
		var profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getRover(), tradingSettlement, buyGoods, sellGoods);
		
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
			setPhase(UNLOAD_SOLD_GOODS, tradingSettlement.getName());
		}

		else if (UNLOAD_SOLD_GOODS.equals(ph)) {
			// Check if vehicle can hold enough supplies for mission.
			if (!isVehicleLoadable()) {
				endMission(CANNOT_LOAD_RESOURCES);
			}
			else {
				// Start the loading
				prepareLoadingPlan(tradingSettlement);
				setPhase(LOAD_BOUGHT_GOODS, tradingSettlement.getName());
			}
		}

		else if (LOAD_BOUGHT_GOODS.equals(ph)) {
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

		var ph = getPhase();
		if (TRADE_DISEMBARKING.equals(ph)) {
			performTradeDisembarkingPhase(member);
		} else if (TRADE_NEGOTIATING.equals(ph)) {
			performTradeNegotiatingPhase(member);
		} else if (UNLOAD_SOLD_GOODS.equals(ph)) {
			performUnloadGoodsPhase();
		} else if (LOAD_BOUGHT_GOODS.equals(ph)) {
			performLoadGoodsPhase();
		} else if (TRADE_EMBARKING.equals(ph)) {
			computeTotalDistanceProposed();
			performTradeEmbarkingPhase(member);
		}
	}

	/**
	 * Performs the trade disembarking phase.
	 *
	 * @param member the mission member performing the mission.
	 */
	private void performTradeDisembarkingPhase(Worker member) {
		var tradingSettlement = objective.getTradingVenue();
		
		// Have member exit rover if necessary.
		if (!member.isInSettlement()) {

			// Get random inhabitable building at trading settlement.
			Building destinationBuilding = tradingSettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);
				if (member instanceof Person person) {
					
					WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, destinationBuilding);
					boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
					if (canWalk) {
						assignTask(person, new Walk(person, walkingSteps));
					}
					else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
				else if (member instanceof Robot robot) {
					
					WalkingSteps walkingSteps = new WalkingSteps(robot, adjustedLoc, destinationBuilding);
					boolean canWalk = Walk.canWalkAllSteps(robot, walkingSteps);
					
					if (canWalk) {
						assignTask(robot, new Walk(robot, walkingSteps));
					}
					else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
			}
			else {
				endMissionProblem(tradingSettlement, "No inhabitable buildings");
			}
		}

		// End the phase when everyone is out of the rover.
		if (isNoOneInRover()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the trade negotiating phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performTradeNegotiatingPhase(Worker member) {
		if (member == getMissionTrader()) {
			if (negotiationTask != null) {
				if (negotiationTask.isDone()) {
					var buyLoad = negotiationTask.getBuyLoad();
					double profit = 0D;
					if (buyLoad != null) {
						profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getRover(), 
										objective.getTradingVenue(), buyLoad, objective.getSell());
					}
					else {
						buyLoad = Collections.emptyMap();
					}
					objective.updateBought(buyLoad, profit);

					fireMissionUpdate(MissionEventType.BUY_LOAD_EVENT);
					setPhaseEnded(true);
				}
			}

			else {
				Person settlementTrader = getSettlementBuyer();

				if (settlementTrader != null) {
					if (member instanceof Person person) {
						negotiationTask = new NegotiateTrade(objective.getTradingVenue(),
															getStartingSettlement(), getRover(),
															objective.getSell(), person, settlementTrader);
						assignTask(person, negotiationTask);
					}
				}
				else if (getPhaseDuration() > 1000D) {
					objective.updateBought(new HashMap<>(), 0D);
					fireMissionUpdate(MissionEventType.BUY_LOAD_EVENT);
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
	 */
	private void performUnloadGoodsPhase() {

		// Unload towed vehicle (if necessary).
		unloadTowedVehicle();

		// Unload rover if necessary.
		if (!getRover().haveStatusType(StatusType.UNLOADING)) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the load goods phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performLoadGoodsPhase() {

		if (!isDone()) {
			// Load towed vehicle (if necessary).
			loadTradeVehicle(false);
		}

		if (isDone() || isVehicleLoaded()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Unloads any towed vehicles.
	 */
	private void unloadTowedVehicle() {
		Rover r = getRover();
		Vehicle towed = r.getTowedVehicle();
		if (towed != null) {
			towed.setReservedForMission(false);
			r.setTowedVehicle(null);
			towed.setTowingVehicle(null);
			r.getSettlement().addVicinityVehicle(towed);
			towed.findNewParkingLoc();
		}
	}

	private void loadTradeVehicle(boolean isSelling) {
		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			String vehicleType = objective.getLoadVehicleType(isSelling);
			if (vehicleType != null) {
				Vehicle buyVehicle = getInitialLoadVehicle(vehicleType, isSelling);
				if (buyVehicle != null) {
					buyVehicle.setReservedForMission(true);
					getRover().setTowedVehicle(buyVehicle);
					buyVehicle.setTowingVehicle(getRover());
					buyVehicle.getSettlement().removeVicinityParkedVehicle(buyVehicle);
				} else {
					logger.warning(getRover(), "Selling vehicle (" + vehicleType + ") is not available (Trade).");
					endMission(SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE);
				}
			}
		}
	}

	/**
	 * Performs the trade embarking phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performTradeEmbarkingPhase(Worker member) {
		Vehicle v = getVehicle();
		
		// If person is not aboard the rover, board rover.
		if (!isDone() && !member.isInVehicle()) {

			// Move person to random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(v);			

			// Question: is the trading settlement responsible
			// for providing an EVA suit for each person
			for (Worker mm: getMembers()) {
				if (mm instanceof Person person) {
					if (person.isDeclaredDead()) {
						continue;
					}

					if (v == null)
						v = person.getVehicle();
					
					// Check if an EVA suit is available
					EVASuitUtil.fetchEVASuitFromAny(person, v, objective.getTradingVenue());

					WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, v);
					boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
					// Walk back to the vehicle and be ready to embark and go home
					if (canWalk) {
						assignTask(person, new Walk(person, walkingSteps));
					}

					else {
						endMissionProblem(person, "Unable to enter rover " + v.getName());
					}
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (!isDone() && isEveryoneInRover()) {

			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not transfer to Surface");
			}			
		}
	}

	@Override
	protected void performDepartingFromSettlementPhase(Worker member) {
		super.performDepartingFromSettlementPhase(member);

		loadTradeVehicle(true);
	}

	@Override
	protected void endMission(MissionStatus endStatus) {

		// Unreserve any towed vehicles.
		if (getRover() != null && getRover().getTowedVehicle() != null) {
			Vehicle towed = getRover().getTowedVehicle();
			towed.setReservedForMission(false);
		}
		
		super.endMission(endStatus);
	}



	/**
	 * Gets the initial load vehicle.
	 *
	 * @param vehicleType the vehicle type string.
	 * @param isSelling         true if buying load, false if selling load.
	 * @return load vehicle.
	 */
	private Vehicle getInitialLoadVehicle(String vehicleType, boolean isSelling) {
		Vehicle result = null;

		if (vehicleType != null) {
			Settlement vehicleSource = null;
			if (!isSelling) {
				vehicleSource = getStartingSettlement();
			}
			else {
				vehicleSource = objective.getTradingVenue();
			}

			result = vehicleSource.getParkedGaragedVehicles().stream()
					.filter( v -> vehicleType.equalsIgnoreCase(v.getDescription())
											&& (v != getVehicle())
											&& !v.isReserved() && v.isEmpty())
					.findAny().orElse(null);
		}

		return result;
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
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		if (result == 0) {
			// Check if one has more general cargo capacity than the other.
			double firstCapacity = firstVehicle.getCargoCapacity();
			double secondCapacity = secondVehicle.getCargoCapacity();
			if (firstCapacity > secondCapacity) {
				result = 1;
			} else if (secondCapacity > firstCapacity) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				double firstRange = firstVehicle.getEstimatedRange();
				double secondRange = secondVehicle.getEstimatedRange();
				if (firstRange > secondRange) {
					result = 1;
				} else if (firstRange <secondRange) {
					result = -1;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the trader for the mission.
	 *
	 * @return the trader.
	 */
	private Person getMissionTrader() {
		if (missionTrader == null) {
			missionTrader = getBestTrader(getMembers(), Collections.emptyList());

		}
		return missionTrader;
	}

	/**
	 * Gets the trader and the destination settlement for the mission.
	 *
	 * @return the trader.
	 */
	private Person getSettlementBuyer() {
		return getBestTrader(objective.getTradingVenue().getIndoorPeople(), getMembers());
	}

	private static Person getBestTrader(Collection<? extends Worker> potentials, Collection<? extends Worker> excluded) {
		Person bestTrader = null;
		int bestTradeSkill = -1;

		for(var w : potentials) {
			if (!excluded.contains(w) && w instanceof Person p) {
				int tradeSkill = p.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
				if (tradeSkill > bestTradeSkill) {
					bestTradeSkill = tradeSkill;
					bestTrader = p;
				}
			}
		}

		return bestTrader;
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
