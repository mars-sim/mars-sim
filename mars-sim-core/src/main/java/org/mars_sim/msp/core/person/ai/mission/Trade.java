/*
 * Mars Simulation Project
 * Trade.java
 * @date 2021-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.CommerceMission;
import org.mars_sim.msp.core.goods.CommerceUtil;
import org.mars_sim.msp.core.goods.Deal;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodCategory;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.NegotiateTrade;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for trading between two settlements. TODO externalize strings
 */
public class Trade extends RoverMission implements CommerceMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Trade.class.getName());

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.trade"); //$NON-NLS-1$

	/** Mission phases. */
	private static final MissionPhase TRADE_DISEMBARKING = new MissionPhase("Mission.phase.tradeDisembarking");
	private static final MissionPhase TRADE_NEGOTIATING = new MissionPhase("Mission.phase.tradeNegotiating");
	public static final MissionPhase UNLOAD_GOODS = new MissionPhase("Mission.phase.unloadGoods");
	public static final MissionPhase LOAD_GOODS = new MissionPhase("Mission.phase.loadGoods");
	private static final MissionPhase TRADE_EMBARKING = new MissionPhase("Mission.phase.tradeEmbarking");

	// Static members
	public static final double MAX_STARTING_PROBABILITY = 100D;

	public static final int MAX_MEMBERS = 2;

	// Data members.
	private double profit;
	private double desiredProfit;

	private boolean outbound;
	private boolean doNegotiation;

	private Settlement tradingSettlement;
	private NegotiateTrade negotiationTask;

	private Map<Good, Integer> sellLoad;
	private Map<Good, Integer> buyLoad;
	private Map<Good, Integer> desiredBuyLoad;

	/**
	 * Constructor. Started by TradeMeta
	 *
	 * @param startingMember the mission member starting the settlement.
	 */
	public Trade(MissionMember startingMember) {
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, MissionType.TRADE, startingMember, null);

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
		doNegotiation = true;

		if (!isDone() && s != null) {
			// Get trading settlement
			Deal deal = s.getGoodsManager().getBestDeal(MissionType.TRADE, getVehicle());
			if (deal == null) {
				endMission(MissionStatus.NO_TRADING_SETTLEMENT);
				return;
			}
			tradingSettlement = deal.getBuyer();
			addNavpoint(tradingSettlement);

			desiredBuyLoad = deal.getBuyingLoad();
			sellLoad = deal.getSellingLoad();
			desiredProfit = deal.getProfit();

			// Recruit additional members to mission.
			if (!isDone() && !recruitMembersForMission(startingMember, MAX_MEMBERS)) {
				return;
			}
		}

		// Set initial phase
		setPhase(VehicleMission.REVIEWING, null);
		if (logger.isLoggable(Level.INFO)) {
			if (startingMember != null && getRover() != null) {
				logger.info(startingMember, "Starting Trade mission on " + getRover().getName() + ".");
			}
		}
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
	public Trade(Collection<MissionMember> members, Settlement tradingSettlement,
			Rover rover, String description, Map<Good, Integer> sellGoods, Map<Good, Integer> buyGoods) {
		// Use RoverMission constructor.
		super(description, MissionType.TRADE, (MissionMember) members.toArray()[0], rover);

		outbound = true;
		doNegotiation = false;

		// Sets the mission capacity.
		if (getMissionCapacity() > MAX_MEMBERS) {
			setMissionCapacity(MAX_MEMBERS);
		}

		// Set mission destination.
		this.tradingSettlement = tradingSettlement;
		addNavpoint(tradingSettlement);

		addMembers(members, false);

		// Set trade goods.
		sellLoad = sellGoods;
		buyLoad = buyGoods;
		desiredBuyLoad = new HashMap<>(buyGoods);
		profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getRover(), tradingSettlement, buyLoad, sellLoad);

		desiredProfit = profit;

		// Set initial phase
		setPhase(EMBARKING, getStartingSettlement().getName());
		if (logger.isLoggable(Level.INFO)) {
			MissionMember startingMember = getStartingPerson();
			if (startingMember != null && getRover() != null) {
				logger.info(startingMember, "Starting Trade mission on " + getRover().getName() + ".");
			}
		}
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 */
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					if (outbound) {
						setPhase(TRADE_DISEMBARKING, tradingSettlement.getName());
					}
					else {
						startDisembarkingPhase();
					}
				}
			}

			else if (TRADE_DISEMBARKING.equals(getPhase())) {
				setPhase(TRADE_NEGOTIATING, tradingSettlement.getName());
			}

			else if (TRADE_NEGOTIATING.equals(getPhase())) {
				setPhase(UNLOAD_GOODS, tradingSettlement.getName());
			}

			else if (UNLOAD_GOODS.equals(getPhase())) {
				// Check if vehicle can hold enough supplies for mission.
				if (!isVehicleLoadable()) {
					endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
				}
				else {
					setPhase(LOAD_GOODS, tradingSettlement.getName());
				}
			}

			else if (LOAD_GOODS.equals(getPhase())) {
				setPhase(TRADE_EMBARKING, tradingSettlement.getName());
			}

			else if (TRADE_EMBARKING.equals(getPhase())) {
				startTravellingPhase();
			}
			else {
				handled = false;
			}
		}

		return handled;
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (TRADE_DISEMBARKING.equals(getPhase())) {
			performTradeDisembarkingPhase(member);
		} else if (TRADE_NEGOTIATING.equals(getPhase())) {
			performTradeNegotiatingPhase(member);
		} else if (UNLOAD_GOODS.equals(getPhase())) {
			performUnloadGoodsPhase(member);
		} else if (LOAD_GOODS.equals(getPhase())) {
			performLoadGoodsPhase(member);
		} else if (TRADE_EMBARKING.equals(getPhase())) {
			computeEstimatedTotalDistance();
			performTradeEmbarkingPhase(member);
		}
	}

	/**
	 * Performs the trade disembarking phase.
	 *
	 * @param member the mission member performing the mission.
	 */
	private void performTradeDisembarkingPhase(MissionMember member) {
		Vehicle v = getVehicle();
		// If rover is not parked at settlement, park it.
		if ((v != null) && (v.getSettlement() == null)) {

			tradingSettlement.addParkedVehicle(v);

			// Add vehicle to a garage if available.
			if (!tradingSettlement.getBuildingManager().addToGarage(v)) {
				// or else re-orient it
//				v.findNewParkingLoc();
			}
		}

		// Have person exit rover if necessary.
		if (!member.isInSettlement()) {

			// Get random inhabitable building at trading settlement.
			Building destinationBuilding = tradingSettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(destinationBuilding);
				if (member instanceof Person) {
					Person person = (Person) member;
					Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, destinationBuilding);
					if (walk != null) {
						assignTask(person, walk);
					}
					else {
						logger.severe(person, "Is unable to walk to building " + destinationBuilding);
					}
				}
				else if (member instanceof Robot) {
					Robot robot = (Robot) member;
					Walk walkingTask = Walk.createWalkingTask(robot, adjustedLoc, destinationBuilding);
					if (walkingTask != null) {
						assignTask(robot, walkingTask);
					}
					else {
						logger.severe(robot, "Is unable to walk to building " + destinationBuilding);
					}
				}
			}
			else {
				logger.severe(tradingSettlement, "No inhabitable buildings");
				endMission(MissionStatus.NO_INHABITABLE_BUILDING);
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
	private void performTradeNegotiatingPhase(MissionMember member) {
		if (doNegotiation) {
			if (member == getMissionTrader()) {
				if (negotiationTask != null) {
					if (negotiationTask.isDone()) {
						buyLoad = negotiationTask.getBuyLoad();
						profit = CommerceUtil.getEstimatedProfit(getStartingSettlement(), getRover(), tradingSettlement, buyLoad, sellLoad);
						fireMissionUpdate(MissionEventType.BUY_LOAD_EVENT);
						setPhaseEnded(true);
					}
				}

				else {
					Person settlementTrader = getSettlementTrader();

					if (settlementTrader != null) {
						if (member instanceof Person) {
							Person person = (Person) member;
							negotiationTask = new NegotiateTrade(tradingSettlement, getStartingSettlement(), getRover(),
									sellLoad, person, settlementTrader);
							assignTask(person, negotiationTask);
						}
					}
					else if (getPhaseDuration() > 1000D) {
						buyLoad = new HashMap<>();
						profit = 0D;
						fireMissionUpdate(MissionEventType.BUY_LOAD_EVENT);
						setPhaseEnded(true);
					}
				}
			}
		} else {
			setPhaseEnded(true);
		}

		if (getPhaseEnded()) {
			outbound = false;
			resetToReturnTrip(
					new NavPoint(tradingSettlement),
					new NavPoint(getStartingSettlement()));

			getStartingSettlement().getGoodsManager().clearDeal(MissionType.TRADE);

			// Start the loading
			prepareLoadingPlan(tradingSettlement);
		}
	}

	/**
	 * Perform the unload goods phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performUnloadGoodsPhase(MissionMember member) {

		// Unload towed vehicle (if necessary).
		unloadTowedVehicle();

		// Unload rover if necessary.
		boolean roverUnloaded = getRover().getStoredMass() == 0D;
		if (roverUnloaded) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the load goods phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performLoadGoodsPhase(MissionMember member) {

		if (!isDone()) {
			// Load towed vehicle (if necessary).
			loadTowedVehicle();
		}

		if (isDone() || isVehicleLoaded()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Unload any towed vehicles.
	 */
	private void unloadTowedVehicle() {
		Vehicle towed = getRover().getTowedVehicle();
		if (towed != null) {
			towed.setReservedForMission(false);
			getRover().setTowedVehicle(null);
			towed.setTowingVehicle(null);
			tradingSettlement.addParkedVehicle(towed);
			towed.findNewParkingLoc();
		}
	}

	/**
	 * Load the towed vehicle is not already loaded.
	 */
	private void loadTowedVehicle() {
		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			String vehicleType = getLoadVehicleType(true);
			if (vehicleType != null) {
				Vehicle buyVehicle = getInitialLoadVehicle(vehicleType, true);
				if (buyVehicle != null) {
					buyVehicle.setReservedForMission(true);
					getRover().setTowedVehicle(buyVehicle);
					buyVehicle.setTowingVehicle(getRover());
					tradingSettlement.removeParkedVehicle(buyVehicle);
				} else {
					logger.warning(getRover(), "Selling vehicle (" + vehicleType + ") is not available (Trade).");
					endMission(MissionStatus.SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE);
				}
			}
		}
	}

	/**
	 * Performs the trade embarking phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performTradeEmbarkingPhase(MissionMember member) {

		// If person is not aboard the rover, board rover.
		if (!isDone() && !member.isInVehicle()) {

			// Move person to random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(getVehicle());

			// Elect a new mission lead if the previous one was dead
			if (member instanceof Person) {
				Person lead = (Person) member;
				if (lead.isDeclaredDead()) {
					logger.info(lead, "No longer alive.");
					int bestSkillLevel = 0;
					for (MissionMember mm: getMembers()) {
						if (mm instanceof Person) {
							Person p = (Person) mm;
							int level = lead.getSkillManager().getSkillExp(SkillType.TRADING);
							if (level > bestSkillLevel) {
								bestSkillLevel = level;
								lead = p;
								setStartingMember(p);
								break;
							}
						}
					}
				}
			}

			// Question: is the trading settlement responsible
			// for providing an EVA suit for each person
			for (MissionMember mm: getMembers()) {
				if (mm instanceof Person) {
					Person person = (Person) mm;
					if (!person.isDeclaredDead()) {
						EVASuit suit0 = getEVASuit(person);
						if (suit0 == null) {
							if (tradingSettlement.findNumContainersOfType(EquipmentType.EVA_SUIT) > 0) {
								EVASuit suit1 = InventoryUtil.getGoodEVASuitNResource(tradingSettlement, person);
								if (suit1 != null) {
									boolean done = suit1.transfer(getVehicle());
									if (!done)
										logger.warning(person, "Not able to transfer an EVA suit from " + tradingSettlement);
								} else {
									logger.warning(person, "EVA suit not provided for by " + tradingSettlement);
								}
							}
						}

						// Walk back to the vehicle and be ready to embark and go home
						Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, getVehicle());
						if (walk != null) {
							assignTask(person, walk);
						}
						else {
							logger.severe(person, "Unable to enter rover " + getVehicle().getName());
							endMission(MissionStatus.CANNOT_ENTER_ROVER);
						}
					}
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (!isDone() && isEveryoneInRover()) {

			// If the rover is in a garage, put the rover outside.
			BuildingManager.removeFromGarage(getVehicle());

			// Embark from settlement
			tradingSettlement.removeParkedVehicle(getVehicle());
			setPhaseEnded(true);
		}
	}

	@Override
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		super.performEmbarkFromSettlementPhase(member);

		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			String vehicleType = getLoadVehicleType(false);
			if (vehicleType != null) {
				Vehicle sellVehicle = getInitialLoadVehicle(vehicleType, false);
				if (sellVehicle != null) {
					sellVehicle.setReservedForMission(true);
					getRover().setTowedVehicle(sellVehicle);
					sellVehicle.setTowingVehicle(getRover());
					getStartingSettlement().removeParkedVehicle(sellVehicle);
				} else {
					logger.warning(getRover(), "Selling vehicle (" + vehicleType + ") is not available (Trade).");
					endMission(MissionStatus.SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE);
				}
			}
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {

		// Unload towed vehicle if any.
		if (!isDone() && (getRover().getTowedVehicle() != null)) {
			Vehicle towed = getRover().getTowedVehicle();
			towed.setReservedForMission(false);
			getRover().setTowedVehicle(null);
			towed.setTowingVehicle(null);
			disembarkSettlement.addParkedVehicle(towed);
			towed.findNewParkingLoc();
		}

		super.performDisembarkToSettlementPhase(member, disembarkSettlement);
	}

	@Override
	protected void endMission(MissionStatus endStatus) {
		super.endMission(endStatus);

		// Unreserve any towed vehicles.
		if (getRover() != null) {
			if (getRover().getTowedVehicle() != null) {
				Vehicle towed = getRover().getTowedVehicle();
				towed.setReservedForMission(false);
			}
		}
	}

	/**
	 * Gets the type of vehicle in a load.
	 *
	 * @param buy true if buy load, false if sell load.
	 * @return vehicle type or null if none.
	 */
	private String getLoadVehicleType(boolean buy) {
		String result = null;

		Map<Good, Integer> load = null;
		if (buy) {
			load = buyLoad;
		} else {
			load = sellLoad;
		}

		Iterator<Good> i = load.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getCategory().equals(GoodCategory.VEHICLE)) {
				result = good.getName();
			}
		}

		return result;
	}

	/**
	 * Gets the initial load vehicle.
	 *
	 * @param vehicleType the vehicle type string.
	 * @param buy         true if buying load, false if selling load.
	 * @return load vehicle.
	 */
	private Vehicle getInitialLoadVehicle(String vehicleType, boolean buy) {
		Vehicle result = null;

		if (vehicleType != null) {
			Settlement settlement = null;
			if (buy) {
				settlement = tradingSettlement;
			} else {
				settlement = getStartingSettlement();
			}

			Iterator<Vehicle> j = settlement.getParkedVehicles().iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				boolean isEmpty = vehicle.isEmpty();
				if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) {
					if ((vehicle != getVehicle()) && !vehicle.isReserved() && isEmpty) {
						result = vehicle;
					}
				}
			}
		}

		return result;
	}

	@Override
	protected Map<Integer, Integer> getOptionalEquipmentToLoad() {

		Map<Integer, Integer> result = super.getOptionalEquipmentToLoad();

		// Add buy/sell load.
		Map<Good, Integer> load = null;
		if (outbound) {
			load = sellLoad;
		} else {
			load = buyLoad;
		}

		Iterator<Good> i = load.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getCategory().equals(GoodCategory.EQUIPMENT)
					|| good.getCategory() == GoodCategory.CONTAINER) {
				int num = load.get(good);
				int id = good.getID();
				if (result.containsKey(id)) {
					num += (Integer) result.get(id);
				}
				result.put(id, num);
			}
		}

		return result;
	}

	@Override
	protected Map<Integer, Number> getOptionalResourcesToLoad() {

		Map<Integer, Number> result = super.getOptionalResourcesToLoad();

		// Add buy/sell load.
		Map<Good, Integer> load = null;
		if (outbound) {
			load = sellLoad;
		} else {
			load = buyLoad;
		}

		Iterator<Good> i = load.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getCategory().equals(GoodCategory.AMOUNT_RESOURCE)) {
				int id = good.getID();
				double amount = load.get(good).doubleValue();
				if (result.containsKey(id)) {
					amount += (Double) result.get(id);
				}
				result.put(id, amount);
			} else if (good.getCategory().equals(GoodCategory.ITEM_RESOURCE)) {
				int id = good.getID();
				int num = load.get(good);
				if (result.containsKey(id)) {
					num += (Integer) result.get(id);
				}
				result.put(id, num);
			}
		}

		return result;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		if ((result == 0) && isUsableVehicle(firstVehicle) && isUsableVehicle(secondVehicle)) {
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
				if (firstVehicle.getRange(MissionType.TRADE) > secondVehicle.getRange(MissionType.TRADE)) {
					result = 1;
				} else if (firstVehicle.getRange(MissionType.TRADE) < secondVehicle.getRange(MissionType.TRADE)) {
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
		Person bestTrader = null;
		int bestTradeSkill = -1;

		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;
				int tradeSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
				if (tradeSkill > bestTradeSkill) {
					bestTradeSkill = tradeSkill;
					bestTrader = person;
				}
			}
		}

		return bestTrader;
	}

	/**
	 * Gets the trader and the destination settlement for the mission.
	 *
	 * @return the trader.
	 */
	private Person getSettlementTrader() {
		Person bestTrader = null;
		int bestTradeSkill = -1;

		Iterator<Person> i = tradingSettlement.getIndoorPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!getMembers().contains(person)) {
				int tradeSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
				if (tradeSkill > bestTradeSkill) {
					bestTradeSkill = tradeSkill;
					bestTrader = person;
				}
			}
		}

		return bestTrader;
	}

	/**
	 * Gets the load that is being sold in the trade.
	 *
	 * @return sell load.
	 */
	public Map<Good, Integer> getSellLoad() {
		if (sellLoad != null) {
			return Collections.unmodifiableMap(sellLoad);
		} else {
			return null;
		}
	}

	/**
	 * Gets the load that is being bought in the trade.
	 *
	 * @return buy load.
	 */
	public Map<Good, Integer> getBuyLoad() {
		if (buyLoad != null) {
			return Collections.unmodifiableMap(buyLoad);
		} else {
			return null;
		}
	}

	/**
	 * Gets the profit for the settlement initiating the trade.
	 *
	 * @return profit (VP).
	 */
	public double getProfit() {
		return profit;
	}

	/**
	 * Gets the load that the starting settlement initially desires to buy.
	 *
	 * @return desired buy load.
	 */
	public Map<Good, Integer> getDesiredBuyLoad() {
		if (desiredBuyLoad != null) {
			return Collections.unmodifiableMap(desiredBuyLoad);
		} else {
			return null;
		}
	}

	/**
	 * Gets the profit initially expected by the starting settlement.
	 *
	 * @return desired profit (VP).
	 */
	public double getDesiredProfit() {
		return desiredProfit;
	}

	/**
	 * Gets the settlement that the starting settlement is trading with.
	 *
	 * @return trading settlement.
	 */
	public Settlement getTradingSettlement() {
		return tradingSettlement;
	}

	@Override
	public void destroy() {
		super.destroy();

		tradingSettlement = null;
		if (sellLoad != null)
			sellLoad.clear();
		sellLoad = null;
		if (buyLoad != null)
			buyLoad.clear();
		buyLoad = null;
		if (desiredBuyLoad != null)
			desiredBuyLoad.clear();
		desiredBuyLoad = null;
		negotiationTask = null;
	}

	/**
	 * If the mission is in the UNLOAD_GOODS phase at the trading settlement
	 * then it can be unloaded.
	 */
	@Override
	public boolean isVehicleUnloadableHere(Settlement settlement) {
		if (UNLOAD_GOODS.equals(getPhase())
					&& settlement.equals(tradingSettlement)) {
			return true;
		}
		return super.isVehicleUnloadableHere(settlement);
	}

	/**
	 * Can the mission vehicle be loaded at a Settlement. Must be in
	 * the LOAD_GOODS phase at the mission trading settlement.
	 * @param settlement
	 * @return
	 */
	@Override
	public boolean isVehicleLoadableHere(Settlement settlement) {
		if (LOAD_GOODS.equals(getPhase())
				&& settlement.equals(tradingSettlement)) {
			return true;
		}
		return super.isVehicleLoadableHere(settlement);
	}
}
