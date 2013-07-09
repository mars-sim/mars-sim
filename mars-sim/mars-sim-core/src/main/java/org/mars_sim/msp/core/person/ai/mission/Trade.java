/**
 * Mars Simulation Project
 * Trade.java
 * @version 3.05 2013-05-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.NegotiateTrade;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mission for trading between two settlements.
 */
public class Trade extends RoverMission implements Serializable {

    private static Logger logger = Logger.getLogger(Trade.class.getName());
    // Mission event types
    public static final String BUY_LOAD_EVENT = "buy load";
    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Trade with Settlement";
    // Mission phases.
    public static final String TRADE_DISEMBARKING = "Trade Disembarking";
    public static final String TRADE_NEGOTIATING = "Trade Negotiating";
    public static final String UNLOAD_GOODS = "Unload Goods";
    public static final String LOAD_GOODS = "Load Goods";
    public static final String TRADE_EMBARKING = "Trade Embarking";
    // Static members
    static final int MAX_MEMBERS = 2;
    private static final double MAX_STARTING_PROBABILITY = 10D;

    // Static cache for holding trade profit info.
    private static final Map<Settlement, TradeProfitInfo> TRADE_PROFIT_CACHE = new HashMap<Settlement, TradeProfitInfo>();
    private static final Map<Settlement, Settlement> TRADE_SETTLEMENT_CACHE = new HashMap<Settlement, Settlement>();

    // Data members.
    private Settlement tradingSettlement;
    private Map<Good, Integer> sellLoad;
    private Map<Good, Integer> buyLoad;
    private double profit;
    private Map<Good, Integer> desiredBuyLoad;
    private double desiredProfit;
    private boolean outbound;
    private MarsClock startNegotiationTime;
    private NegotiateTrade negotiationTask;
    private boolean doNegotiation;

    /**
     * Constructor.
     * @param startingPerson the person starting the settlement.
     */
    public Trade(Person startingPerson) {
        // Use RoverMission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson);

        // Set the mission capacity.
        setMissionCapacity(MAX_MEMBERS);
        int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
        if (availableSuitNum < getMissionCapacity()) {
            setMissionCapacity(availableSuitNum);
        }

        outbound = true;
        doNegotiation = true;

        if (!isDone()) {

            // Initialize data members
            setStartingSettlement(startingPerson.getSettlement());

            // Get trading settlement
            tradingSettlement = TRADE_SETTLEMENT_CACHE.get(getStartingSettlement());
            if ((tradingSettlement != null) && (tradingSettlement != getStartingSettlement())) {
                addNavpoint(new NavPoint(tradingSettlement.getCoordinates(), tradingSettlement,
                        tradingSettlement.getName()));
                setDescription("Trade with " + tradingSettlement.getName());
                TRADE_PROFIT_CACHE.remove(getStartingSettlement());
                TRADE_PROFIT_CACHE.remove(tradingSettlement);
                TRADE_SETTLEMENT_CACHE.remove(getStartingSettlement());
                TRADE_SETTLEMENT_CACHE.remove(tradingSettlement);
            } else {
                endMission("Could not determine trading settlement.");
            }

            if (!isDone()) {
                // Get the credit that the starting settlement has with the destination settlement.
                CreditManager creditManager = Simulation.instance().getCreditManager();
                double credit = creditManager.getCredit(getStartingSettlement(), tradingSettlement);

                if (credit > (TradeUtil.SELL_CREDIT_LIMIT * -1D)) {
                    // Determine desired buy load,
                    desiredBuyLoad = TradeUtil.getDesiredBuyLoad(getStartingSettlement(), getRover(), tradingSettlement);
                }
                else {
                    // Cannot buy from settlement due to credit limit.
                    desiredBuyLoad = new HashMap<Good, Integer>(0);
                }

                if (credit < TradeUtil.SELL_CREDIT_LIMIT) {
                    // Determine sell load.
                    sellLoad = TradeUtil.determineBestSellLoad(getStartingSettlement(), getRover(), tradingSettlement);
                }
                else {
                    // Will not sell to settlement due to credit limit.
                    sellLoad = new HashMap<Good, Integer>(0);
                }

                // Determine desired trade profit.
                desiredProfit = estimateTradeProfit(desiredBuyLoad);
            }

            // Recruit additional people to mission.
            if (!isDone()) {
                recruitPeopleForMission(startingPerson);
            }
        }

        // Add trade mission phases.
        addPhase(TRADE_DISEMBARKING);
        addPhase(TRADE_NEGOTIATING);
        addPhase(UNLOAD_GOODS);
        addPhase(LOAD_GOODS);
        addPhase(TRADE_EMBARKING);

        // Set initial phase
        setPhase(VehicleMission.EMBARKING);
        setPhaseDescription("Embarking from " + getStartingSettlement().getName());
        if (logger.isLoggable(Level.INFO)) {
            if (startingPerson != null && getRover() != null) {
                logger.info(startingPerson.getName() + " starting Trade mission on " + getRover().getName());
            }
        }
    }

    /**
     * Constructor with explicit data.
     * @param members collection of mission members.
     * @param startingSettlement the starting settlement.
     * @param tradingSettlement the trading settlement.
     * @param rover the rover to use.
     * @param description the mission's description.
     * @param sellGoods map of mission sell goods and integer amounts.
     * @param buyGoods map of mission buy goods and integer amounts
     */
    public Trade(Collection<Person> members, Settlement startingSettlement, Settlement tradingSettlement,
            Rover rover, String description, Map<Good, Integer> sellGoods, Map<Good, Integer> buyGoods) {
        // Use RoverMission constructor.
        super(description, (Person) members.toArray()[0], 1, rover);

        outbound = true;
        doNegotiation = false;

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
        addNavpoint(new NavPoint(tradingSettlement.getCoordinates(), tradingSettlement,
                tradingSettlement.getName()));

        // Add mission members.
        Iterator<Person> i = members.iterator();
        while (i.hasNext()) {
            i.next().getMind().setMission(this);
        }

        // Set trade goods.
        sellLoad = sellGoods;
        buyLoad = buyGoods;
        desiredBuyLoad = new HashMap<Good, Integer>(buyGoods);
        profit = estimateTradeProfit(buyLoad);
        desiredProfit = profit;

        // Add trade mission phases.
        addPhase(TRADE_DISEMBARKING);
        addPhase(TRADE_NEGOTIATING);
        addPhase(UNLOAD_GOODS);
        addPhase(LOAD_GOODS);
        addPhase(TRADE_EMBARKING);

        // Set initial phase
        setPhase(VehicleMission.EMBARKING);
        setPhaseDescription("Embarking from " + getStartingSettlement().getName());
        if (logger.isLoggable(Level.INFO)) {
            Person startingPerson = (Person) members.toArray()[0];
            if (startingPerson != null && getRover() != null) {
                logger.info(startingPerson.getName() + " starting Trade mission on " + getRover().getName());
            }
        }
    }

    /** 
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

        double missionProbability = 0D;

        // Determine job modifier.
        Job job = person.getMind().getJob();
        double jobModifier = 0D;
        if (job != null) {
            jobModifier = job.getStartMissionProbabilityModifier(Trade.class);
        }

        // Check if person is in a settlement.
        boolean inSettlement = person.getLocationSituation().equals(Person.INSETTLEMENT);

        if (inSettlement && (jobModifier > 0D)) {

            // Check if mission is possible for person based on their circumstance.
            boolean missionPossible = true;
            Settlement settlement = person.getSettlement();

            // Check if available rover.
            if (!areVehiclesAvailable(settlement, false)) {
                missionPossible = false;
            }

            // Check if available backup rover.
            if (!hasBackupRover(settlement)) {
                missionPossible = false;
            }

            // Check if minimum number of people are available at the settlement.
            // Plus one to hold down the fort.
            if (!minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_PEOPLE + 1)) {
                missionPossible = false;
            }

            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_PEOPLE) {
                missionPossible = false;
            }

            // Check for the best trade settlement within range.
            double tradeProfit = 0D;
            try {
                Rover rover = (Rover) getVehicleWithGreatestRange(settlement, false);
                if (rover != null) {
                    // Only check every couple of Sols, else use cache.
                    // Note: this method is very CPU intensive.
                    boolean useCache = false;
                    MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
                    if (TRADE_PROFIT_CACHE.containsKey(settlement)) {
                        TradeProfitInfo profitInfo = TRADE_PROFIT_CACHE.get(settlement);
                        double timeDiff = MarsClock.getTimeDiff(currentTime, profitInfo.time);
                        if (timeDiff < 2000D) {
                            tradeProfit = profitInfo.profit;
                            useCache = true;
                        }
                    }
                    else {
                        TRADE_PROFIT_CACHE.put(settlement, new TradeProfitInfo(tradeProfit,
                                (MarsClock) currentTime.clone()));
                        useCache = true;
                    }

                    if (!useCache) {
                        double startTime = System.currentTimeMillis();
                        tradeProfit = TradeUtil.getBestTradeProfit(settlement, rover);
                        double endTime = System.currentTimeMillis();
                        logger.info(settlement.getName() + " getBestTradeProfit: " + (endTime - startTime)
                                + " milliseconds - TP: " + (int) tradeProfit + " VP");
                        TRADE_PROFIT_CACHE.put(settlement, new TradeProfitInfo(tradeProfit,
                                (MarsClock) currentTime.clone()));
                        TRADE_SETTLEMENT_CACHE.put(settlement, TradeUtil.bestTradeSettlementCache);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding vehicles at settlement.", e);
            }

            // Check for embarking missions.
            if (VehicleMission.hasEmbarkingMissions(settlement)) {
                missionPossible = false;
            }

            // Check if settlement has enough basic resources for a rover mission.
            if (!RoverMission.hasEnoughBasicResources(settlement)) {
                missionPossible = false;
            }

            // Determine mission probability.
            if (missionPossible) {

                // Trade value modifier.
                missionProbability = tradeProfit / 1000D;
                if (missionProbability > MAX_STARTING_PROBABILITY) {
                    missionProbability = MAX_STARTING_PROBABILITY;
                }

                // Crowding modifier.
                int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
                if (crowding > 0) {
                    missionProbability *= (crowding + 1);
                }

                // Job modifier.
                missionProbability *= jobModifier;
            }
        }

        return missionProbability;
    }

    /**
     * Determines a new phase for the mission when the current phase has ended.
     */
    protected void determineNewPhase() {
        if (EMBARKING.equals(getPhase())) {
            startTravelToNextNode();
            setPhase(VehicleMission.TRAVELLING);
            setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
        } else if (TRAVELLING.equals(getPhase())) {
            if (getCurrentNavpoint().isSettlementAtNavpoint()) {
                if (outbound) {
                    setPhase(TRADE_DISEMBARKING);
                    setPhaseDescription("Disembarking at " + tradingSettlement);
                } else {
                    setPhase(VehicleMission.DISEMBARKING);
                    setPhaseDescription("Disembarking at " + getCurrentNavpoint().getDescription());
                }
            }
        } else if (TRADE_DISEMBARKING.equals(getPhase())) {
            setPhase(TRADE_NEGOTIATING);
            setPhaseDescription("Negotiating trade at " + tradingSettlement);
        } else if (TRADE_NEGOTIATING.equals(getPhase())) {
            setPhase(UNLOAD_GOODS);
            setPhaseDescription("Unloading sell goods at " + tradingSettlement);
        } else if (UNLOAD_GOODS.equals(getPhase())) {
            setPhase(LOAD_GOODS);
            setPhaseDescription("Loading buy goods at " + tradingSettlement);
        } else if (LOAD_GOODS.equals(getPhase())) {
            setPhase(TRADE_EMBARKING);
            setPhaseDescription("Embarking at " + tradingSettlement);
        } else if (TRADE_EMBARKING.equals(getPhase())) {
            startTravelToNextNode();
            setPhase(VehicleMission.TRAVELLING);
            setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
        } else if (DISEMBARKING.equals(getPhase())) {
            endMission("Successfully disembarked.");
        }
    }

    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     */
    protected void performPhase(Person person) {
        super.performPhase(person);
        if (TRADE_DISEMBARKING.equals(getPhase())) {
            performTradeDisembarkingPhase(person);
        } else if (TRADE_NEGOTIATING.equals(getPhase())) {
            performTradeNegotiatingPhase(person);
        } else if (UNLOAD_GOODS.equals(getPhase())) {
            performUnloadGoodsPhase(person);
        } else if (LOAD_GOODS.equals(getPhase())) {
            performLoadGoodsPhase(person);
        } else if (TRADE_EMBARKING.equals(getPhase())) {
            performTradeEmbarkingPhase(person);
        }
    }

    /**
     * Performs the trade disembarking phase.
     * @param person the person performing the mission.
     */
    private void performTradeDisembarkingPhase(Person person) {

        Building garageBuilding = null;

        // If rover is not parked at settlement, park it.
        if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {

            tradingSettlement.getInventory().storeUnit(getVehicle());
            getVehicle().determinedSettlementParkedLocationAndFacing();

            // Add vehicle to a garage if available.
            BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(), tradingSettlement);
            garageBuilding = BuildingManager.getBuilding(getVehicle());
        }

        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
            // If rover is in a garage, exit to the garage.
            if (isRoverInAGarage()) {
                getVehicle().getInventory().retrieveUnit(person);
                tradingSettlement.getInventory().storeUnit(person);
                garageBuilding = BuildingManager.getBuilding(getVehicle());
                BuildingManager.addPersonToBuilding(person, garageBuilding);
            }
            else {
                // Have person exit the rover via its airlock if possible.
                if (ExitAirlock.canExitAirlock(person, getRover().getAirlock())) {
                    assignTask(person, new ExitAirlock(person, getRover().getAirlock()));
                }
                else {
                    logger.info(person + " unable to exit " + getRover() + " through airlock to settlement " + 
                            tradingSettlement + " due to health problems or being unable to obtain a functioning EVA suit.  " + 
                            "Using emergency exit procedure.");
                    getVehicle().getInventory().retrieveUnit(person);
                    tradingSettlement.getInventory().storeUnit(person);
                    BuildingManager.addToRandomBuilding(person, tradingSettlement);
                }
            }
        }
        else if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            // Have person enter the settlement via an airlock.
            assignTask(person, new EnterAirlock(person, tradingSettlement.getAvailableAirlock()));
        }

        // End the phase when everyone is out of the rover.
        if (isNoOneInRover()) {
            setPhaseEnded(true);
        }
    }

    /**
     * Perform the trade negotiating phase.
     * @param person the person performing the phase.
     */
    private void performTradeNegotiatingPhase(Person person) {
        if (doNegotiation) {
            if (person == getMissionTrader()) {
                if (negotiationTask != null) {
                    if (negotiationTask.isDone()) {
                        buyLoad = negotiationTask.getBuyLoad();
                        profit = estimateTradeProfit(buyLoad);
                        fireMissionUpdate(BUY_LOAD_EVENT);
                        setPhaseEnded(true);
                    }
                } else {
                    if (startNegotiationTime == null) {
                        startNegotiationTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
                    }
                    Person settlementTrader = getSettlementTrader();
                    if (settlementTrader != null) {
                        negotiationTask = new NegotiateTrade(tradingSettlement, getStartingSettlement(), getRover(), sellLoad, person, settlementTrader);
                        assignTask(person, negotiationTask);
                    } else {
                        MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
                        double timeDiff = MarsClock.getTimeDiff(currentTime, startNegotiationTime);
                        if (timeDiff > 1000D) {
                            buyLoad = new HashMap<Good, Integer>(0);
                            profit = 0D;
                            fireMissionUpdate(BUY_LOAD_EVENT);
                            setPhaseEnded(true);
                        }
                    }
                }
            }
        } else {
            setPhaseEnded(true);
        }

        if (getPhaseEnded()) {
            outbound = false;
            equipmentNeededCache = null;
            addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
                    getStartingSettlement().getName()));
            TRADE_PROFIT_CACHE.remove(getStartingSettlement());
        }
    }

    /**
     * Perform the unload goods phase.
     * @param person the person performing the phase.
     */
    private void performUnloadGoodsPhase(Person person) {

        // Unload towed vehicle (if necessary).
        unloadTowedVehicle();

        // Unload rover if necessary.
        boolean roverUnloaded = getRover().getInventory().getTotalInventoryMass(false) == 0D;
        if (!roverUnloaded) {
            // Random chance of having person unload (this allows person to do other things sometimes)
            if (RandomUtil.lessThanRandPercent(50)) {
                if (isRoverInAGarage()) {
                    assignTask(person, new UnloadVehicleGarage(person, getRover()));
                }
                else {
                    // Check if it is day time.
                    SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                    if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
                            surface.inDarkPolarRegion(person.getCoordinates())) {
                        assignTask(person, new UnloadVehicleEVA(person, getRover()));
                    }
                }

                return;
            }
        } 
        else {
            setPhaseEnded(true);
        }
    }

    /**
     * Performs the load goods phase.
     * @param person the person performing the phase.
     */
    private void performLoadGoodsPhase(Person person) {

        if (!isDone()) {

            // Load towed vehicle (if necessary).
            loadTowedVehicle();
        }

        if (!isDone() && !isVehicleLoaded()) {

            // Check if vehicle can hold enough supplies for mission.
            if (isVehicleLoadable()) {
                // Random chance of having person load (this allows person to do other things sometimes)
                if (RandomUtil.lessThanRandPercent(50)) {
                    if (isRoverInAGarage()) {
                        assignTask(person, new LoadVehicleGarage(person, getVehicle(), getRequiredResourcesToLoad(),
                                getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
                    }
                    else {
                        // Check if it is day time.
                        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                        if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
                                surface.inDarkPolarRegion(person.getCoordinates())) {
                            assignTask(person, new LoadVehicleEVA(person, getVehicle(), getRequiredResourcesToLoad(),
                                    getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
                        }
                    }
                }
            } else {
                endMission("Vehicle is not loadable (RoverMission).");
            }
        } else {
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
            tradingSettlement.getInventory().storeUnit(towed);
            towed.determinedSettlementParkedLocationAndFacing();
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
                    tradingSettlement.getInventory().retrieveUnit(buyVehicle);
                } else {
                    endMission("Selling vehicle (" + vehicleType + ") is not available (Trade).");
                }
            }
        }
    }

    /**
     * Performs the trade embarking phase.
     * @param person the person performing the phase.
     */
    private void performTradeEmbarkingPhase(Person person) {

        // If person is not aboard the rover, board rover.
        if (!person.getLocationSituation().equals(Person.INVEHICLE) && !person.getLocationSituation().equals(Person.BURIED)) {

            if (isRoverInAGarage()) {
                if (getVehicle().getInventory().canStoreUnit(person, false)) {
                    if (tradingSettlement.getInventory().containsUnit(person)) {
                        tradingSettlement.getInventory().retrieveUnit(person);
                    }
                    getVehicle().getInventory().storeUnit(person);
                }
                else {
                    endMission("Crew member " + person + " cannot be loaded in rover " + getVehicle());
                    return;
                }

                // Store one EVA suit for person (if possible).
                if (tradingSettlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
                    EVASuit suit = (EVASuit) tradingSettlement.getInventory().findUnitOfClass(EVASuit.class);
                    if (getVehicle().getInventory().canStoreUnit(suit, false)) {
                        tradingSettlement.getInventory().retrieveUnit(suit);
                        getVehicle().getInventory().storeUnit(suit);
                    }
                    else {
                        endMission("Equipment " + suit + " cannot be loaded in rover " + getVehicle());
                        return;
                    }
                }

                // Move person to random location within rover.
                Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
                Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                        vehicleLoc.getY(), getVehicle());
                person.setXLocation(settlementLoc.getX());
                person.setYLocation(settlementLoc.getY());
            }
            else {
                if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {

                    // Have person exit the settlement via an airlock.
                    if (ExitAirlock.canExitAirlock(person, tradingSettlement.getAvailableAirlock())) {
                        assignTask(person, new ExitAirlock(person, tradingSettlement.getAvailableAirlock()));
                    }
                    else {
                        logger.info(person + " unable to exit airlock at " + tradingSettlement + " to rover " + 
                                getRover() + " due to health problems or being unable to obtain a functioning EVA suit.");
                        endMission(person + " unable to exit airlock from " + tradingSettlement + 
                                " due to health problems or being unable to obtain a functioning EVA suit.");                  
                    }
                }
                else if (person.getLocationSituation().equals(Person.OUTSIDE)) {

                    // Have person enter the rover airlock.
                    assignTask(person, new EnterAirlock(person, getRover().getAirlock()));
                }
            }
        }

        // If rover is loaded and everyone is aboard, embark from settlement.
        if (isEveryoneInRover()) {

            // Remove from garage if in garage.
            Building garageBuilding = BuildingManager.getBuilding(getVehicle());
            if (garageBuilding != null) {
                VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
                garage.removeVehicle(getVehicle());
            }

            // Embark from settlement
            tradingSettlement.getInventory().retrieveUnit(getVehicle());
            setPhaseEnded(true);
        }
    }

    /** 
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission
     */
    protected void performEmbarkFromSettlementPhase(Person person) {
        super.performEmbarkFromSettlementPhase(person);

        if (!isDone() && (getRover().getTowedVehicle() == null)) {
            String vehicleType = getLoadVehicleType(false);
            if (vehicleType != null) {
                Vehicle sellVehicle = getInitialLoadVehicle(vehicleType, false);
                if (sellVehicle != null) {
                    sellVehicle.setReservedForMission(true);
                    getRover().setTowedVehicle(sellVehicle);
                    sellVehicle.setTowingVehicle(getRover());
                    getStartingSettlement().getInventory().retrieveUnit(sellVehicle);
                } else {
                    endMission("Selling vehicle (" + vehicleType + ") is not available (Trade).");
                }
            }
        }
    }

    /**
     * Performs the disembark to settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @param disembarkSettlement the settlement to be disembarked to.
     */
    protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) {

        // Unload towed vehicle if any.
        if (!isDone() && (getRover().getTowedVehicle() != null)) {
            Vehicle towed = getRover().getTowedVehicle();
            towed.setReservedForMission(false);
            getRover().setTowedVehicle(null);
            towed.setTowingVehicle(null);
            disembarkSettlement.getInventory().storeUnit(towed);
            towed.determinedSettlementParkedLocationAndFacing();
        }

        super.performDisembarkToSettlementPhase(person, disembarkSettlement);
    }

    @Override
    public void endMission(String reason) {
        super.endMission(reason);

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
            if (good.getCategory().equals(Good.VEHICLE)) {
                result = good.getName();
            }
        }

        return result;
    }

    /**
     * Gets the initial load vehicle.
     * @param vehicleType the vehicle type string.
     * @param buy true if buying load, false if selling load.
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
                if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) {
                    if ((vehicle != getVehicle()) && !vehicle.isReserved()) {
                        result = vehicle;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Map<Class, Integer> getOptionalEquipmentToLoad() {

        Map<Class, Integer> result = super.getOptionalEquipmentToLoad();

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
            if (good.getCategory().equals(Good.EQUIPMENT)) {
                Class equipmentClass = good.getClassType();
                int num = load.get(good);
                if (result.containsKey(equipmentClass)) {
                    num += (Integer) result.get(equipmentClass);
                }
                result.put(equipmentClass, num);
            }
        }

        return result;
    }

    @Override
    public Map<Resource, Number> getOptionalResourcesToLoad() {

        Map<Resource, Number> result = super.getOptionalResourcesToLoad();

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
            if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
                AmountResource resource = (AmountResource) good.getObject();
                double amount = load.get(good).doubleValue();
                if (result.containsKey(resource)) {
                    amount += (Double) result.get(resource);
                }
                result.put(resource, amount);
            } else if (good.getCategory().equals(Good.ITEM_RESOURCE)) {
                ItemResource resource = (ItemResource) good.getObject();
                int num = load.get(good);
                if (result.containsKey(resource)) {
                    num += (Integer) result.get(resource);
                }
                result.put(resource, num);
            }
        }

        return result;
    }

    @Override
    public Settlement getAssociatedSettlement() {
        return getStartingSettlement();
    }

    @Override
    protected boolean isCapableOfMission(Person person) {
        if (super.isCapableOfMission(person)) {
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                if (person.getSettlement() == getStartingSettlement()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void recruitPeopleForMission(Person startingPerson) {
        super.recruitPeopleForMission(startingPerson);

        // Make sure there is at least one person left at the starting settlement.
        if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingPerson)) {
            // Remove last person added to the mission.
            Person lastPerson = null;
            int amount = getPeopleNumber() - 1;
            Object[] array = getPeople().toArray();

            if (amount >= 0 && amount < array.length) {
                lastPerson = (Person) array[amount];
            }

            if (lastPerson != null) {
                lastPerson.getMind().setMission(null);
                if (getPeopleNumber() < getMinPeople()) {
                    endMission("Not enough members.");
                }
            }
        }
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
                if (firstVehicle.getRange() > secondVehicle.getRange()) {
                    result = 1;
                } else if (firstVehicle.getRange() < secondVehicle.getRange()) {
                    result = -1;
                }
            }
        }

        return result;
    }

    /**
     * Gets the trader for the mission.
     * @return the trader.
     */
    private Person getMissionTrader() {
        Person bestTrader = null;
        int bestTradeSkill = -1;

        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            int tradeSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING);
            if (tradeSkill > bestTradeSkill) {
                bestTradeSkill = tradeSkill;
                bestTrader = person;
            }
        }

        return bestTrader;
    }

    /**
     * Gets the trader and the destination settlement for the mission.
     * @return the trader.
     */
    private Person getSettlementTrader() {
        Person bestTrader = null;
        int bestTradeSkill = -1;

        Iterator<Person> i = tradingSettlement.getInhabitants().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (!getPeople().contains(person)) {
                int tradeSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING);
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
     * @return profit (VP).
     */
    public double getProfit() {
        return profit;
    }

    /**
     * Gets the load that the starting settlement initially desires to buy.
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
     * @return desired profit (VP).
     */
    public double getDesiredProfit() {
        return desiredProfit;
    }

    /**
     * Gets the settlement that the starting settlement is trading with.
     * @return trading settlement.
     */
    public Settlement getTradingSettlement() {
        return tradingSettlement;
    }

    /**
     * Estimates the profit for the starting settlement for a given buy load.
     * @param buyingLoad the load to buy.
     * @return profit (VP).
     */
    private double estimateTradeProfit(Map<Good, Integer> buyingLoad) {
        double result = 0D;

        try {
            double sellingValueHome = TradeUtil.determineLoadValue(sellLoad, getStartingSettlement(), false);
            double sellingValueRemote = TradeUtil.determineLoadValue(sellLoad, tradingSettlement, true);
            double sellingProfit = sellingValueRemote - sellingValueHome;

            double buyingValueHome = TradeUtil.determineLoadValue(buyingLoad, getStartingSettlement(), true);
            double buyingValueRemote = TradeUtil.determineLoadValue(buyingLoad, tradingSettlement, false);
            double buyingProfit = buyingValueHome - buyingValueRemote;

            double totalProfit = sellingProfit + buyingProfit;

            double estimatedDistance = getStartingSettlement().getCoordinates().getDistance(
                    tradingSettlement.getCoordinates()) * 2D;
            double missionCost = TradeUtil.getEstimatedMissionCost(getStartingSettlement(), getRover(),
                    estimatedDistance);

            result = totalProfit - missionCost;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            endMission("Could not estimate trade profit.");
        }

        return result;
    }

    @Override
    public void destroy() {
        super.destroy();

        tradingSettlement = null;
        if (sellLoad != null) sellLoad.clear();
        sellLoad = null;
        if (buyLoad != null) buyLoad.clear();
        buyLoad = null;
        if (desiredBuyLoad != null) desiredBuyLoad.clear();
        desiredBuyLoad = null;
        startNegotiationTime = null;
        negotiationTask = null;
    }

    /**
     * Inner class for storing trade profit info.
     */
    private static class TradeProfitInfo {

        private double profit;
        private MarsClock time;

        private TradeProfitInfo(double profit, MarsClock time) {
            this.profit = profit;
            this.time = time;
        }
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) {
        if (equipmentNeededCache != null) return equipmentNeededCache;
        else {
            Map<Class, Integer> result = new HashMap<Class, Integer>(0);
            equipmentNeededCache = result;
            return result;
        }
    }
}