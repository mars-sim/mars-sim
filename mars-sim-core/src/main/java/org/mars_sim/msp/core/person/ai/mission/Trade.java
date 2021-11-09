/*
 * Mars Simulation Project
 * Trade.java
 * @date 2021-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.NegotiateTrade;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodCategory;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for trading between two settlements. TODO externalize strings
 */
public class Trade extends RoverMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Trade.class.getName());

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.trade"); //$NON-NLS-1$

	/** Mission Type enum. */
	private static final MissionType MISSION_TYPE = MissionType.TRADE;
	
	/** Mission phases. */
	private static final MissionPhase TRADE_DISEMBARKING = new MissionPhase("Mission.phase.tradeDisembarking"); 
	private static final MissionPhase TRADE_NEGOTIATING = new MissionPhase("Mission.phase.tradeNegotiating"); 
	public static final MissionPhase UNLOAD_GOODS = new MissionPhase("Mission.phase.unloadGoods"); 
	public static final MissionPhase LOAD_GOODS = new MissionPhase("Mission.phase.loadGoods"); 
	private static final MissionPhase TRADE_EMBARKING = new MissionPhase("Mission.phase.tradeEmbarking");

	// Static members
	public static final double MAX_STARTING_PROBABILITY = 100D;

	// Static cache for holding trade profit info.
	public static final Map<Settlement, TradeProfitInfo> TRADE_PROFIT_CACHE = new HashMap<>();
	public static final Map<Settlement, Settlement> TRADE_SETTLEMENT_CACHE = new HashMap<>();

	static final int MAX_MEMBERS = 2;

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
		super(DEFAULT_DESCRIPTION, MISSION_TYPE, startingMember);

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
			tradingSettlement = TRADE_SETTLEMENT_CACHE.get(s);
			if (tradingSettlement != null && !tradingSettlement.equals(s)) {
				addNavpoint(tradingSettlement);
				TRADE_PROFIT_CACHE.remove(getStartingSettlement());
				TRADE_PROFIT_CACHE.remove(tradingSettlement);
				TRADE_SETTLEMENT_CACHE.remove(getStartingSettlement());
				TRADE_SETTLEMENT_CACHE.remove(tradingSettlement);
			} else {
				addMissionStatus(MissionStatus.NO_TRADING_SETTLEMENT);
				endMission();
			}

			if (!isDone()) {
				// Get the credit that the starting settlement has with the destination
				// settlement.
				double credit = creditManager.getCredit(s, tradingSettlement);

				if (credit > (TradeUtil.SELL_CREDIT_LIMIT * -1D)) {
					// Determine desired buy load,
					desiredBuyLoad = TradeUtil.getDesiredBuyLoad(s, getRover(), tradingSettlement);
				} else {
					// Cannot buy from settlement due to credit limit.
					desiredBuyLoad = new HashMap<Good, Integer>(0);
				}

				if (credit < TradeUtil.SELL_CREDIT_LIMIT) {
					// Determine sell load.
					sellLoad = TradeUtil.determineBestSellLoad(s, getRover(), tradingSettlement);
				} else {
					// Will not sell to settlement due to credit limit.
					sellLoad = new HashMap<Good, Integer>(0);
				}

				// Determine desired trade profit.
				desiredProfit = estimateTradeProfit(desiredBuyLoad);
			}

			// Recruit additional members to mission.
			if (!isDone()) {
				if (!recruitMembersForMission(startingMember, MAX_MEMBERS))
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
		super(description, MISSION_TYPE, (MissionMember) members.toArray()[0], rover);

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
		profit = estimateTradeProfit(buyLoad);
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
				if (getCurrentNavpoint().isSettlementAtNavpoint()) {
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
					addMissionStatus(MissionStatus.CANNOT_LOAD_RESOURCES);
					endMission();
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
				v.findNewParkingLoc();
			}
		}

		// Have person exit rover if necessary.
		if (!member.isInSettlement()) {

			// Get random inhabitable building at trading settlement.
			Building destinationBuilding = tradingSettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(),
						destinationLoc.getY(), destinationBuilding);
				if (member instanceof Person) {
					Person person = (Person) member;
					if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding)) {
						assignTask(person,
								new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding));
					} else {
							logger.severe(person, "Is unable to walk to building " + destinationBuilding);
					}
				} else if (member instanceof Robot) {
					Robot robot = (Robot) member;
					if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding)) {
						assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding));
					} else {
						logger.severe(robot, "Is unable to walk to building " + destinationBuilding);
					}
				}
			} else {
				logger.severe(tradingSettlement, "No inhabitable buildings");
				addMissionStatus(MissionStatus.NO_INHABITABLE_BUILDING);
				endMission();
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
						profit = estimateTradeProfit(buyLoad);
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
					new NavPoint(tradingSettlement.getCoordinates(), 
							tradingSettlement,
							tradingSettlement.getName()),

					new NavPoint(getStartingSettlement().getCoordinates(), 
								getStartingSettlement(),
								getStartingSettlement().getName()));
			
			TRADE_PROFIT_CACHE.remove(getStartingSettlement());
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
					addMissionStatus(MissionStatus.SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE);
					endMission();
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
			Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
			Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), vehicleLoc.getY(),
					getVehicle());

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
						EVASuit suit0 = getVehicle().findEVASuit(person);
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
						if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, getVehicle())) {
							assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, getVehicle()));
						} else {
							logger.severe(person, "Unable to enter rover " + getVehicle().getName());
							addMissionStatus(MissionStatus.CANNOT_ENTER_ROVER);
							endMission();
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
					addMissionStatus(MissionStatus.SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE);
					endMission();
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
	public void endMission() {
		super.endMission();

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
			double firstCapacity = firstVehicle.getTotalCapacity();
			double secondCapacity = secondVehicle.getTotalCapacity();
			if (firstCapacity > secondCapacity) {
				result = 1;
			} else if (secondCapacity > firstCapacity) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange(MISSION_TYPE) > secondVehicle.getRange(MISSION_TYPE)) {
					result = 1;
				} else if (firstVehicle.getRange(MISSION_TYPE) < secondVehicle.getRange(MISSION_TYPE)) {
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
			return new HashMap<Good, Integer>(sellLoad);
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
			return new HashMap<Good, Integer>(buyLoad);
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
			return new HashMap<Good, Integer>(desiredBuyLoad);
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

	/**
	 * Estimates the profit for the starting settlement for a given buy load.
	 * 
	 * @param buyingLoad the load to buy.
	 * @return profit (VP).
	 */
	private double estimateTradeProfit(Map<Good, Integer> buyingLoad) {
		double result = 0D;

		try {
			double sellingValueHome = TradeUtil.determineLoadCredit(sellLoad, getStartingSettlement(), false);
			double sellingValueRemote = TradeUtil.determineLoadCredit(sellLoad, tradingSettlement, true);
			double sellingProfit = sellingValueRemote - sellingValueHome;

			double buyingValueHome = TradeUtil.determineLoadCredit(buyingLoad, getStartingSettlement(), true);
			double buyingValueRemote = TradeUtil.determineLoadCredit(buyingLoad, tradingSettlement, false);
			double buyingProfit = buyingValueHome - buyingValueRemote;

			double totalProfit = sellingProfit + buyingProfit;

			double estimatedDistance = Coordinates.computeDistance(getStartingSettlement().getCoordinates(), 
					tradingSettlement.getCoordinates()) * 2D;
			double missionCost = TradeUtil.getEstimatedMissionCost(getStartingSettlement(), getRover(),
					estimatedDistance);

			result = totalProfit - missionCost;
		} catch (Exception e) {
			logger.severe(getVehicle(), "Cannot estimate trade profit: ", e);
			addMissionStatus(MissionStatus.COULD_NOT_ESTIMATE_TRADE_PROFIT);
			endMission();
		}

		return result;
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
	 * Inner class for storing trade profit info.
	 */
	public static class TradeProfitInfo {
		
		public double profit;
		public MarsClock time;

		public TradeProfitInfo(double profit, MarsClock time) {
			this.profit = profit;
			this.time = time;
		}
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
