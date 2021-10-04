/*
 * Mars Simulation Project
 * Delivery.java
 * @date 2021-08-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.NegotiateDelivery;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodCategory;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for delivery between two settlements. TODO externalize strings
 */
public class Delivery extends DroneMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Delivery.class.getName());

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.delivery"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.TRADE;
	
	/** Mission phases. */
	public static final MissionPhase TRADE_DISEMBARKING = new MissionPhase(
			Msg.getString("Mission.phase.deliveryDisembarking")); //$NON-NLS-1$
	public static final MissionPhase TRADE_NEGOTIATING = new MissionPhase(
			Msg.getString("Mission.phase.deliveryNegotiating")); //$NON-NLS-1$
	public static final MissionPhase UNLOAD_GOODS = new MissionPhase(Msg.getString("Mission.phase.unloadGoods")); //$NON-NLS-1$
	public static final MissionPhase LOAD_GOODS = new MissionPhase(Msg.getString("Mission.phase.loadGoods")); //$NON-NLS-1$
	public static final MissionPhase TRADE_EMBARKING = new MissionPhase(Msg.getString("Mission.phase.deliveryEmbarking")); //$NON-NLS-1$

	// Static members
	public static final double MAX_STARTING_PROBABILITY = 100D;

	// Static cache for holding trade profit info.
	public static final Map<Settlement, DeliveryProfitInfo> TRADE_PROFIT_CACHE = new HashMap<>();
	public static final Map<Settlement, Settlement> TRADE_SETTLEMENT_CACHE = new HashMap<>();

	static final int MAX_MEMBERS = 1;

	// Data members.
	private double profit;
	private double desiredProfit;

	private boolean outbound;
	private boolean doNegotiation;

	private Settlement tradingSettlement;
	private MarsClock startNegotiationTime;
	private NegotiateDelivery negotiationTask;

	private Map<Good, Integer> sellLoad;
	private Map<Good, Integer> buyLoad;
	private Map<Good, Integer> desiredBuyLoad;

	/**
	 * Constructor. Started by DeliveryMeta
	 * 
	 * @param startingMember the mission member starting the settlement.
	 */
	public Delivery(MissionMember startingMember) {
		// Use DroneMission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingMember);
		
		// Problem starting Mission
		if (isDone()) {
			return;
		}
		
		Settlement s = startingMember.getSettlement();
		// Set the mission capacity.
		setMissionCapacity(MAX_MEMBERS);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(s);
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		outbound = true;
		doNegotiation = true;

		if (!isDone() && s != null) {

			// Initialize data members
			setStartingSettlement(s);

			// Get trading settlement
			tradingSettlement = TRADE_SETTLEMENT_CACHE.get(s);
			if (tradingSettlement != null && !tradingSettlement.equals(s)) {
				addNavpoint(new NavPoint(tradingSettlement.getCoordinates(), tradingSettlement,
						tradingSettlement.getName()));
//				setDescription(Msg.getString("Mission.description.delivery.detail", tradingSettlement.getName())); // $NON-NLS-1$
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

				if (credit > (DeliveryUtil.SELL_CREDIT_LIMIT * -1D)) {
					// Determine desired buy load,
					desiredBuyLoad = DeliveryUtil.getDesiredBuyLoad(s, getDrone(), tradingSettlement);
				} else {
					// Cannot buy from settlement due to credit limit.
					desiredBuyLoad = new HashMap<>();
				}

				if (credit < DeliveryUtil.SELL_CREDIT_LIMIT) {
					// Determine sell load.
					sellLoad = DeliveryUtil.determineBestSellLoad(s, getDrone(), tradingSettlement);
				} else {
					// Will not sell to settlement due to credit limit.
					sellLoad = new HashMap<>();
				}

				// Determine desired trade profit.
				desiredProfit = estimateDeliveryProfit(desiredBuyLoad);
			}

			// Recruit additional members to mission.
			if (!isDone() && !recruitMembersForMission(startingMember, true)) {
				return;
			}
		}

		// Add trade mission phases.
		addPhase(TRADE_DISEMBARKING);
		addPhase(TRADE_NEGOTIATING);
		addPhase(UNLOAD_GOODS);
		addPhase(LOAD_GOODS);
		addPhase(TRADE_EMBARKING);

		// Set initial phase
		setPhase(VehicleMission.REVIEWING);
		setPhaseDescription(Msg.getString("Mission.phase.reviewing.description")); //$NON-NLS-1$
		if (logger.isLoggable(Level.INFO)) {
			if (startingMember != null && getDrone() != null) {
				logger.info(startingMember, "Starting Delivery mission on " + getDrone().getName() + ".");
			}
		}
	}

	/**
	 * Constructor 2. Started by MissionDataBean 
	 * 
	 * @param members
	 * @param startingSettlement
	 * @param tradingSettlement
	 * @param drone
	 * @param description
	 * @param sellGoods
	 * @param buyGoods
	 */
	public Delivery(MissionMember startingMember, Collection<MissionMember> members, Settlement startingSettlement, Settlement tradingSettlement,
			Drone drone, String description, Map<Good, Integer> sellGoods, Map<Good, Integer> buyGoods) {
		// Use DroneMission constructor.
		super(description, missionType, startingMember, 2, drone);

		outbound = true;
		doNegotiation = false;

		// Add mission members.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember mm = i.next();
			if (mm instanceof Person)
				((Person)mm).getMind().setMission(this);
			else if (mm instanceof Robot)
				((Robot)mm).getBotMind().setMission(this);
		}
		
		// Initialize data members
		setStartingSettlement(startingSettlement);

		// Sets the mission capacity.
		setMissionCapacity(MAX_MEMBERS);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		// Set mission destination.
		this.tradingSettlement = tradingSettlement;
		addNavpoint(new NavPoint(tradingSettlement.getCoordinates(), tradingSettlement, tradingSettlement.getName()));

		// Set trade goods.
		sellLoad = sellGoods;
		buyLoad = buyGoods;
		desiredBuyLoad = new HashMap<>(buyGoods);
		profit = estimateDeliveryProfit(buyLoad);
		desiredProfit = profit;

		// Add trade mission phases.
		addPhase(TRADE_DISEMBARKING);
		addPhase(TRADE_NEGOTIATING);
		addPhase(UNLOAD_GOODS);
		addPhase(LOAD_GOODS);
		addPhase(TRADE_EMBARKING);

		// Set initial phase
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.embarking.description"));//, getStartingSettlement().getName())); // $NON-NLS-1$
		if (logger.isLoggable(Level.INFO)) {
			if (startingMember != null && getDrone() != null) {
				logger.info(startingMember, "Starting Delivery mission on " + getDrone().getName() + ".");
			}
		}
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 */
	@Override
	protected void determineNewPhase() {
		if (REVIEWING.equals(getPhase())) {
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getCurrentNavpoint().getDescription()));//startingMember.getSettlement().toString())); // $NON-NLS-1$
		}
		
		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				if (outbound) {
					setPhase(TRADE_DISEMBARKING);
					setPhaseDescription(
							Msg.getString("Mission.phase.disembarking.description", tradingSettlement.getName())); // $NON-NLS-1$
				} else {
					setPhase(VehicleMission.DISEMBARKING);
					setPhaseDescription(Msg.getString("Mission.phase.disembarking.description",
							getCurrentNavpoint().getDescription())); // $NON-NLS-1$
				}
			}
		} 
		
		else if (TRADE_DISEMBARKING.equals(getPhase())) {
			setPhase(TRADE_NEGOTIATING);
			setPhaseDescription(
					Msg.getString("Mission.phase.deliveryNegotiating.description", tradingSettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (TRADE_NEGOTIATING.equals(getPhase())) {
			setPhase(UNLOAD_GOODS);
			setPhaseDescription(Msg.getString("Mission.phase.unloadGoods.description", tradingSettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (UNLOAD_GOODS.equals(getPhase())) {
			clearLoadingPlan(); // Clear the original loading plan
			setPhase(LOAD_GOODS);
			setPhaseDescription(Msg.getString("Mission.phase.loadGoods.description", tradingSettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (LOAD_GOODS.equals(getPhase())) {
			setPhase(TRADE_EMBARKING);
			setPhaseDescription(Msg.getString("Mission.phase.embarking.description", tradingSettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (TRADE_EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (DISEMBARKING.equals(getPhase())) {
			setPhase(VehicleMission.COMPLETED);
			setPhaseDescription(
					Msg.getString("Mission.phase.completed.description")); // $NON-NLS-1$
		}
		
		else if (COMPLETED.equals(getPhase())) {
			addMissionStatus(MissionStatus.MISSION_ACCOMPLISHED);
			endMission();
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
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
			computeEstimatedTotalDistance();
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

			tradingSettlement.getInventory().storeUnit(v);
	
			// Add vehicle to a garage if available.
			if (!tradingSettlement.getBuildingManager().addToGarage(v)) {
				// or else re-orient it
				v.findNewParkingLoc();
			}
		}

		setPhaseEnded(true);
	}

	/**
	 * Perform the delivery negotiating phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void performDeliveryNegotiatingPhase(MissionMember member) {
		if (doNegotiation) {
				if (negotiationTask != null) {
					if (negotiationTask.isDone()) {
						buyLoad = negotiationTask.getBuyLoad();
						profit = estimateDeliveryProfit(buyLoad);
						fireMissionUpdate(MissionEventType.BUY_LOAD_EVENT);
						setPhaseEnded(true);
					}
					else {
						// Check if the caller should be doing negotiation
						Worker dealer = negotiationTask.getWorker();
						if (dealer == null) {
							// Task has not be reinit after a restore
							logger.warning(member, "Reinit the Negotiation Task");
							negotiationTask.reinit();
							dealer = negotiationTask.getWorker();
						}
						if (dealer.equals(member)) {
							// It's the caller so restart and it will be a Person
							logger.info(member, "Resuming negotiation for " + getTypeID());
							assignTask((Person)member, negotiationTask);
						}
					}
				} 
				
				else {
					MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
					
					if (startNegotiationTime == null) {
						startNegotiationTime = currentTime;
					}
					
					Person settlementTrader = getSettlementTrader();
					
					if (settlementTrader != null) {
						boolean assigned = false;
						
						for (MissionMember mm: getMembers()) {
							
							if (mm instanceof Person) {
								Person person = (Person) mm;
								negotiationTask = new NegotiateDelivery(tradingSettlement, getStartingSettlement(), getDrone(),
										sellLoad, person, settlementTrader);
								assigned = assignTask(person, negotiationTask);
							}
							
							if (assigned)
								break;	
						}
						
					} else {

						double timeDiff = MarsClock.getTimeDiff(currentTime, startNegotiationTime);
						
						if (timeDiff > 1000D) {
							buyLoad = new HashMap<Good, Integer>(0);
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
			equipmentNeededCache = null;
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
	private void performDestinationUnloadGoodsPhase() {

		// Unload drone if necessary.
		if (getDrone().getInventory().getTotalInventoryMass(false) == 0D) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the load goods phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void performDestinationLoadGoodsPhase() {

		if (!isDone() && !isVehicleLoaded()) {

			// Check if vehicle can hold enough supplies for mission.
			if (!isVehicleLoadable()) {
				addMissionStatus(MissionStatus.CANNOT_LOAD_RESOURCES);
				endMission();
			}
		} else {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the delivery embarking phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void performDeliveryEmbarkingPhase(MissionMember member) {

		// If person is not aboard the drone, board drone.
		if (!isDone()) {

			if (member instanceof Person) {
				Person pilot = (Person) member;
				if (pilot.isDeclaredDead()) {
					logger.info(pilot, "No longer alive. Switching to another pilot.");
					int bestSkillLevel = 0;
					// Pick another member to head the delivery
					for (MissionMember mm: getMembers()) {
						if (member instanceof Person) {
							Person p = (Person) mm;
							if (!p.isDeclaredDead()) {
								int level = p.getSkillManager().getSkillExp(SkillType.PILOTING);
								if (level > bestSkillLevel) {
									bestSkillLevel = level;
									pilot = p;
									setStartingMember(p);
									break;
								}
							}
						}
						else if (member instanceof Robot) {
							setStartingMember(mm);
							break;
						}
					}
				}
			}

			// Remove from garage if in garage.
			Building garageBuilding = BuildingManager.getBuilding(getVehicle());
			if (garageBuilding != null) {
				VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
				garage.removeVehicle(getVehicle());
			}

			// Embark from settlement
			tradingSettlement.getInventory().retrieveUnit(getVehicle());
			setPhaseEnded(true);
		}
	}

	@Override
	public Map<Integer, Integer> getOptionalEquipmentToLoad() {

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
				&& good.getName().equalsIgnoreCase(EVASuit.TYPE)) {
				// For EVA suits
				int num = load.get(good);
				int id = good.getID();
				if (result.containsKey(id)) {
					num += result.get(id);
				}
				result.put(id, num);
			}
			
			else if (good.getCategory() == GoodCategory.CONTAINER) {
				int num = load.get(good);
				int id = good.getID();
				if (result.containsKey(id)) {
					num += result.get(id);
				}
				result.put(id, num);
			}
		}

		return result;
	}

	@Override
	public Map<Integer, Number> getOptionalResourcesToLoad() {

		Map<Integer, Number> result = new HashMap<>();

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
			double firstCapacity = firstVehicle.getInventory().getGeneralCapacity();
			double secondCapacity = secondVehicle.getInventory().getGeneralCapacity();
			if (firstCapacity > secondCapacity) {
				result = 1;
			} else if (secondCapacity > firstCapacity) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange(missionType) > secondVehicle.getRange(missionType)) {
					result = 1;
				} else if (firstVehicle.getRange(missionType) < secondVehicle.getRange(missionType)) {
					result = -1;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the trader and the destination settlement for the mission.
	 * 
	 * @return the trader.
	 */
	private Person getSettlementTrader() {
		Person bestDeliveryr = null;
		int bestDeliverySkill = -1;

		Iterator<Person> i = tradingSettlement.getIndoorPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!getMembers().contains(person)) {
				int tradeSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
				if (tradeSkill > bestDeliverySkill) {
					bestDeliverySkill = tradeSkill;
					bestDeliveryr = person;
				}
			}
		}

		return bestDeliveryr;
	}

	/**
	 * Gets the load that is being sold in the trade.
	 * 
	 * @return sell load.
	 */
	public Map<Good, Integer> getSellLoad() {
		if (sellLoad != null) {
			return new HashMap<>(sellLoad);
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * Gets the load that is being bought in the trade.
	 * 
	 * @return buy load.
	 */
	public Map<Good, Integer> getBuyLoad() {
		if (buyLoad != null) {
			return new HashMap<>(buyLoad);
		} else {
			return Collections.emptyMap();
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
			return new HashMap<>(desiredBuyLoad);
		} else {
			return Collections.emptyMap();
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
	private double estimateDeliveryProfit(Map<Good, Integer> buyingLoad) {
		double result = 0D;

		try {
			double sellingCreditHome = DeliveryUtil.determineLoadCredit(sellLoad, getStartingSettlement(), false);
			double sellingCreditRemote = DeliveryUtil.determineLoadCredit(sellLoad, tradingSettlement, true);
			double sellingProfit = sellingCreditRemote - sellingCreditHome;

			double buyingCreditHome = DeliveryUtil.determineLoadCredit(buyingLoad, getStartingSettlement(), true);
			double buyingCreditRemote = DeliveryUtil.determineLoadCredit(buyingLoad, tradingSettlement, false);
			double buyingProfit = buyingCreditHome - buyingCreditRemote;

			double totalProfit = sellingProfit + buyingProfit;

			double estimatedDistance = Coordinates.computeDistance(getStartingSettlement().getCoordinates(), 
					tradingSettlement.getCoordinates()) * 2D;
			double missionCost = DeliveryUtil.getEstimatedMissionCost(getStartingSettlement(), getDrone(),
					estimatedDistance);

			result = totalProfit - missionCost;
		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot estimate delivery profit: "+ e.getMessage());
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
		startNegotiationTime = null;
		negotiationTask = null;
	}

	/**
	 * Inner class for storing trade profit info.
	 */
	public static class DeliveryProfitInfo {
		
		public double profit;
		public MarsClock time;

		public DeliveryProfitInfo(double profit, MarsClock time) {
			this.profit = profit;
			this.time = time;
		}
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		if (equipmentNeededCache != null)
			return equipmentNeededCache;
		else {
			Map<Integer, Integer> result = new HashMap<>(0);
			equipmentNeededCache = result;
			return result;
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
