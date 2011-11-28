/**
 * Mars Simulation Project
 * Mining.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.CollectMinedMinerals;
import org.mars_sim.msp.core.person.ai.task.MineSite;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mission for mining mineral concentrations at an explored site.
 */
public class Mining extends RoverMission {

    private static Logger logger = Logger.getLogger(Mining.class.getName());

    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Mining";

    // Mission phases
    final public static String MINING_SITE = "Mining Site";

    // Mission event types
    public static final String EXCAVATE_MINERALS_EVENT = "excavate minerals";
    public static final String COLLECT_MINERALS_EVENT = "collect minerals";

    // Number of bags needed for mission.
    private static final int NUMBER_OF_BAGS = 20;

    // Base amount (kg) of a type of mineral at a site.
    private static final double MINERAL_BASE_AMOUNT = 1000D;

    // Amount of time(millisols) to spend at the mining site.
    private static final double MINING_SITE_TIME = 3000D;

    // Minimum amount (kg) of an excavated mineral that can be collected.
    private static final double MINIMUM_COLLECT_AMOUNT = 10D;

    // Light utility vehicle attachment parts for mining.
    public static final String PNEUMATIC_DRILL = "pneumatic drill";
    public static final String BACKHOE = "backhoe";
    public static final String BULLDOZER_BLADE = "bulldozer blade";

    // Data members
    private ExploredLocation miningSite;
    private MarsClock miningSiteStartTime;
    private boolean endMiningSite;
    private Map<AmountResource, Double> excavatedMinerals;
    private Map<AmountResource, Double> totalExcavatedMinerals;
    private LightUtilityVehicle luv;

    /**
     * Constructor
     * @param startingPerson the person starting the mission.
     * @throws MissionException if error creating mission.
     */
    public Mining(Person startingPerson) {

        // Use RoverMission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson, RoverMission.MIN_PEOPLE);

        if (!isDone()) {
            // Set mission capacity.
            if (hasVehicle())
                setMissionCapacity(getRover().getCrewCapacity());
            int availableSuitNum = Mission
                    .getNumberAvailableEVASuitsAtSettlement(startingPerson
                            .getSettlement());
            if (availableSuitNum < getMissionCapacity())
                setMissionCapacity(availableSuitNum);

            // Initialize data members.
            setStartingSettlement(startingPerson.getSettlement());
            excavatedMinerals = new HashMap<AmountResource, Double>(1);
            totalExcavatedMinerals = new HashMap<AmountResource, Double>(1);

            // Recruit additional people to mission.
            recruitPeopleForMission(startingPerson);

            // Determine mining site.
            try {
                if (hasVehicle()) {
                    miningSite = determineBestMiningSite(getRover(),
                            getStartingSettlement());
                    miningSite.setReserved(true);
                    addNavpoint(new NavPoint(miningSite.getLocation(),
                            "mining site"));
                }
            } catch (Exception e) {
                endMission("Mining site could not be determined.");
            }

            // Add home settlement
            addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(),
                    getStartingSettlement(), getStartingSettlement().getName()));

            // Check if vehicle can carry enough supplies for the mission.
            if (hasVehicle() && !isVehicleLoadable())
                endMission("Vehicle is not loadable. (Mining)");

            // Reserve light utility vehicle.
            luv = reserveLightUtilityVehicle();
            if (luv == null)
                endMission("Light utility vehicle not available.");
        }

        // Add mining site phase.
        addPhase(MINING_SITE);

        // Set initial mission phase.
        setPhase(VehicleMission.EMBARKING);
        setPhaseDescription("Embarking from "
                + getStartingSettlement().getName());
    }

    /**
     * Constructor with explicit data.
     * @param members collection of mission members.
     * @param startingSettlement the starting settlement.
     * @param miningSite the site to mine.
     * @param rover the rover to use.
     * @param description the mission's description.
     * @throws MissionException if error constructing mission.
     */
    public Mining(Collection<Person> members, Settlement startingSettlement,
            ExploredLocation miningSite, Rover rover, LightUtilityVehicle luv,
            String description) {

        // Use RoverMission constructor.
        super(description, (Person) members.toArray()[0], 1, rover);

        // Initialize data members.
        setStartingSettlement(startingSettlement);
        this.miningSite = miningSite;
        miningSite.setReserved(true);
        excavatedMinerals = new HashMap<AmountResource, Double>(1);
        totalExcavatedMinerals = new HashMap<AmountResource, Double>(1);

        // Set mission capacity.
        setMissionCapacity(getRover().getCrewCapacity());
        int availableSuitNum = Mission
                .getNumberAvailableEVASuitsAtSettlement(startingSettlement);
        if (availableSuitNum < getMissionCapacity())
            setMissionCapacity(availableSuitNum);

        // Add mission members.
        Iterator<Person> i = members.iterator();
        while (i.hasNext())
            i.next().getMind().setMission(this);

        // Add mining site nav point.
        addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));

        // Add home settlement
        addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(),
                getStartingSettlement(), getStartingSettlement().getName()));

        // Check if vehicle can carry enough supplies for the mission.
        if (hasVehicle() && !isVehicleLoadable())
            endMission("Vehicle is not loadable. (Mining)");

        // Reserve light utility vehicle.
        this.luv = luv;
        if (luv == null)
            endMission("Light utility vehicle not available.");
        else
            luv.setReservedForMission(true);

        // Add mining site phase.
        addPhase(MINING_SITE);

        // Set initial mission phase.
        setPhase(VehicleMission.EMBARKING);
        setPhaseDescription("Embarking from "
                + getStartingSettlement().getName());
    }

    /**
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();

            // Check if a mission-capable rover is available.
            boolean reservableRover = RoverMission.areVehiclesAvailable(
                    settlement, false);

            // Check if available backup rover.
            boolean backupRover = hasBackupRover(settlement);

            // Check if minimum number of people are available at the settlement.
            // Plus one to hold down the fort.
            boolean minNum = RoverMission.minAvailablePeopleAtSettlement(
                    settlement, (MIN_PEOPLE + 1));

            // Check if there are enough bags at the settlement for collecting minerals.
            boolean enoughBags = false;

            int numBags = settlement.getInventory().findNumEmptyUnitsOfClass(
                    Bag.class);
            enoughBags = (numBags >= NUMBER_OF_BAGS);

            // Check for embarking missions.
            boolean embarkingMissions = VehicleMission
                    .hasEmbarkingMissions(settlement);

            // Check if settlement has enough basic resources for a rover mission.
            boolean hasBasicResources = RoverMission
                    .hasEnoughBasicResources(settlement);

            // Check if available light utility vehicles.
            boolean reservableLUV = isLUVAvailable(settlement);

            // Check if LUV attachment parts available.
            boolean availableAttachmentParts = areAvailableAttachmentParts(settlement);

            if (reservableRover && backupRover && minNum && enoughBags
                    && !embarkingMissions && reservableLUV
                    && availableAttachmentParts && hasBasicResources) {

                try {
                    // Get available rover.
                    Rover rover = (Rover) getVehicleWithGreatestRange(
                            settlement, false);
                    if (rover != null) {

                        // Find best mining site.
                        ExploredLocation miningSite = determineBestMiningSite(
                                rover, settlement);
                        if (miningSite != null) {
                            result = getMiningSiteValue(miningSite, settlement);
                            if (result > 100D)
                                result = 100D;
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting mining site.", e);
                }
            }

            // Crowding modifier
            int crowding = settlement.getCurrentPopulationNum()
                    - settlement.getPopulationCapacity();
            if (crowding > 0)
                result *= (crowding + 1);

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null)
                result *= job.getStartMissionProbabilityModifier(Mining.class);
        }

        if (result > 0D) {
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getSettlement()) < MIN_PEOPLE)
                result = 0D;
        }

        return result;
    }

    /**
     * Checks if a light utility vehicle (LUV) is available for the mission.
     * @param settlement the settlement to check.
     * @return true if LUV available.
     */
    private static boolean isLUVAvailable(Settlement settlement) {
        boolean result = false;

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();

            if (vehicle instanceof LightUtilityVehicle) {
                boolean usable = true;
                if (vehicle.isReserved())
                    usable = false;
                if (!vehicle.getStatus().equals(Vehicle.PARKED))
                    usable = false;
                if (((Crewable) vehicle).getCrewNum() > 0)
                    usable = false;
                if (usable)
                    result = true;
            }
        }

        return result;
    }

    /**
     * Checks if the required attachment parts are available.
     * @param settlement the settlement to check.
     * @return true if available attachment parts.
     */
    private static boolean areAvailableAttachmentParts(Settlement settlement) {
        boolean result = true;

        Inventory inv = settlement.getInventory();

        try {
            Part pneumaticDrill = (Part) Part.findItemResource(PNEUMATIC_DRILL);
            if (!inv.hasItemResource(pneumaticDrill))
                result = false;
            Part backhoe = (Part) Part.findItemResource(BACKHOE);
            if (!inv.hasItemResource(backhoe))
                result = false;
            Part bulldozerBlade = (Part) Part.findItemResource(BULLDOZER_BLADE);
            if (!inv.hasItemResource(bulldozerBlade))
                result = false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting parts.");
        }

        return result;
    }

    @Override
    protected void determineNewPhase() {
        if (EMBARKING.equals(getPhase())) {
            startTravelToNextNode();
            setPhase(VehicleMission.TRAVELLING);
            setPhaseDescription("Driving to "
                    + getNextNavpoint().getDescription());
        } else if (TRAVELLING.equals(getPhase())) {
            if (getCurrentNavpoint().isSettlementAtNavpoint()) {
                setPhase(VehicleMission.DISEMBARKING);
                setPhaseDescription("Disembarking at "
                        + getCurrentNavpoint().getSettlement().getName());
            } else {
                setPhase(MINING_SITE);
                setPhaseDescription("Mining at "
                        + getCurrentNavpoint().getDescription());
            }
        } else if (MINING_SITE.equals(getPhase())) {
            startTravelToNextNode();
            setPhase(VehicleMission.TRAVELLING);
            setPhaseDescription("Driving to "
                    + getNextNavpoint().getDescription());
        } else if (DISEMBARKING.equals(getPhase()))
            endMission("Successfully disembarked.");
    }

    @Override
    protected void performPhase(Person person) {
        super.performPhase(person);
        if (MINING_SITE.equals(getPhase()))
            miningPhase(person);
    }

    @Override
    protected void performEmbarkFromSettlementPhase(Person person) {
        super.performEmbarkFromSettlementPhase(person);

        // Attach light utility vehicle for towing.
        if (!isDone() && (getRover().getTowedVehicle() == null)) {
            try {
                Inventory settlementInv = getStartingSettlement()
                        .getInventory();
                Inventory luvInv = luv.getInventory();
                getRover().setTowedVehicle(luv);
                luv.setTowingVehicle(getRover());
                settlementInv.retrieveUnit(luv);

                // Load light utility vehicle with attachment parts.
                Part pneumaticDrill = (Part) Part
                        .findItemResource(PNEUMATIC_DRILL);
                settlementInv.retrieveItemResources(pneumaticDrill, 1);
                luvInv.storeItemResources(pneumaticDrill, 1);

                Part backhoe = (Part) Part.findItemResource(BACKHOE);
                settlementInv.retrieveItemResources(backhoe, 1);
                luvInv.storeItemResources(backhoe, 1);

                Part bulldozerBlade = (Part) Part
                        .findItemResource(BULLDOZER_BLADE);
                settlementInv.retrieveItemResources(bulldozerBlade, 1);
                luvInv.storeItemResources(bulldozerBlade, 1);
            } catch (Exception e) {
                logger
                        .log(Level.SEVERE,
                                "Error loading light utility vehicle and attachment parts.");
                endMission("Light utility vehicle and attachment parts could not be loaded.");
            }
        }
    }

    @Override
    protected void performDisembarkToSettlementPhase(Person person,
            Settlement disembarkSettlement) {

        // Unload towed light utility vehicle.
        if (!isDone() && (getRover().getTowedVehicle() != null)) {
            try {
                Inventory settlementInv = getStartingSettlement()
                        .getInventory();
                Inventory luvInv = luv.getInventory();
                getRover().setTowedVehicle(null);
                luv.setTowingVehicle(null);
                settlementInv.storeUnit(luv);

                // Unload attachment parts.
                Part pneumaticDrill = (Part) Part
                        .findItemResource(PNEUMATIC_DRILL);
                luvInv.retrieveItemResources(pneumaticDrill, 1);
                settlementInv.storeItemResources(pneumaticDrill, 1);

                Part backhoe = (Part) Part.findItemResource(BACKHOE);
                luvInv.retrieveItemResources(backhoe, 1);
                settlementInv.storeItemResources(backhoe, 1);

                Part bulldozerBlade = (Part) Part
                        .findItemResource(BULLDOZER_BLADE);
                luvInv.retrieveItemResources(bulldozerBlade, 1);
                settlementInv.storeItemResources(bulldozerBlade, 1);
            } catch (Exception e) {
                logger
                        .log(Level.SEVERE,
                                "Error unloading light utility vehicle and attachment parts.");
                endMission("Light utility vehicle and attachment parts could not be unloaded.");
            }
        }

        super.performDisembarkToSettlementPhase(person, disembarkSettlement);
    }

    /**
     * Perform the mining phase.
     * @param person the person performing the mining phase.
     * @throws MissionException if error performing the mining phase.
     */
    private void miningPhase(Person person) {

        // Set the mining site start time if necessary.
        if (miningSiteStartTime == null)
            miningSiteStartTime = (MarsClock) Simulation.instance()
                    .getMasterClock().getMarsClock().clone();

        // Detach towed light utility vehicle if necessary.
        if (getRover().getTowedVehicle() != null) {
            getRover().setTowedVehicle(null);
            luv.setTowingVehicle(null);
        }

        // Check if crew has been at site for more than three sols.
        boolean timeExpired = false;
        MarsClock currentTime = (MarsClock) Simulation.instance()
                .getMasterClock().getMarsClock().clone();
        if (MarsClock.getTimeDiff(currentTime, miningSiteStartTime) >= MINING_SITE_TIME)
            timeExpired = true;

        if (isEveryoneInRover()) {

            // Check if end mining flag is set.
            if (endMiningSite) {
                endMiningSite = false;
                setPhaseEnded(true);
            }

            // Check if crew has been at site for more than three sols, then end this phase.
            if (timeExpired)
                setPhaseEnded(true);

            // Determine if no one can start the mine site or collect resources tasks.
            boolean nobodyMineOrCollect = true;
            Iterator<Person> i = getPeople().iterator();
            while (i.hasNext()) {
                Person personTemp = i.next();
                if (MineSite.canMineSite(personTemp, getRover()))
                    nobodyMineOrCollect = false;
                if (canCollectExcavatedMinerals(personTemp))
                    nobodyMineOrCollect = false;
            }

            // If no one can mine or collect minerals at the site and this is not due to it just being
            // night time, end the mining phase.
            Mars mars = Simulation.instance().getMars();
            boolean inDarkPolarRegion = mars.getSurfaceFeatures()
                    .inDarkPolarRegion(getCurrentMissionLocation());
            double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(
                    getCurrentMissionLocation());
            if (nobodyMineOrCollect && ((sunlight > 0D) || inDarkPolarRegion))
                setPhaseEnded(true);

            // Anyone in the crew or a single person at the home settlement has a dangerous illness, end phase.
            if (hasEmergency())
                setPhaseEnded(true);

            // Check if enough resources for remaining trip.
            if (!hasEnoughResourcesForRemainingMission(false)) {
                // If not, determine an emergency destination.
                determineEmergencyDestination(person);
                setPhaseEnded(true);
            }
        } else {
            // If mining time has expired for the site, have everyone end their
            // mining and collection tasks.
            if (timeExpired) {
                Iterator<Person> i = getPeople().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof MineSite)
                        ((MineSite) task).endEVA();
                    if (task instanceof CollectMinedMinerals)
                        ((CollectMinedMinerals) task).endEVA();
                }
            }
        }

        if (!getPhaseEnded()) {

            // 75% chance of assigning task, otherwise allow break.
            if (RandomUtil.lessThanRandPercent(75D)) {
                // If mining is still needed at site, assign tasks.
                if (!endMiningSite && !timeExpired) {
                    // If person can collect minerals the site, start that task.
                    if (canCollectExcavatedMinerals(person)) {
                        AmountResource mineralToCollect = getMineralToCollect(person);
                        assignTask(person, new CollectMinedMinerals(person,
                                getRover(), mineralToCollect));
                    }
                    // Otherwise start the mining task if it can be done.
                    else if (MineSite.canMineSite(person, getRover())) {
                        assignTask(person, new MineSite(person, miningSite
                                .getLocation(), (Rover) getVehicle(), luv));
                    }
                }
            }
        } else {
            // Mark site as mined.
            miningSite.setMined(true);

            // Attach light utility vehicle for towing.
            getRover().setTowedVehicle(luv);
            luv.setTowingVehicle(getRover());
        }
    }

    /**
     * Checks if a person can collect minerals from the excavation pile.
     * @param person the person collecting.
     * @return true if can collect minerals.
     */
    private boolean canCollectExcavatedMinerals(Person person) {
        boolean result = false;

        Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
                    && CollectMinedMinerals.canCollectMinerals(person,
                            getRover(), resource))
                result = true;
        }

        return result;
    }

    /**
     * Gets the mineral resource to collect from the excavation pile.
     * @param person the person collecting.
     * @return mineral
     */
    private AmountResource getMineralToCollect(Person person) {
        AmountResource result = null;
        double largestAmount = 0D;

        Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
                    && CollectMinedMinerals.canCollectMinerals(person,
                            getRover(), resource)) {
                double amount = excavatedMinerals.get(resource);
                if (amount > largestAmount) {
                    result = resource;
                    largestAmount = amount;
                }
            }
        }

        return result;
    }

    /**
     * Ends mining at a site.
     */
    public void endMiningAtSite() {
        logger.info("Mining site phase ended due to external trigger.");
        endMiningSite = true;

        // End each member's mining site task.
        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            Task task = i.next().getMind().getTaskManager().getTask();
            if (task instanceof MineSite)
                ((MineSite) task).endEVA();
            if (task instanceof CollectMinedMinerals)
                ((CollectMinedMinerals) task).endEVA();
        }
    }

    /**
     * Determines the best available mining site.
     * @param roverRange the range of the mission rover (km).
     * @param homeSettlement the mission home settlement.
     * @return best explored location for mining, or null if none found.
     * @throws MissionException if error determining mining site.
     */
    private static ExploredLocation determineBestMiningSite(Rover rover,
            Settlement homeSettlement) {

        ExploredLocation result = null;
        double bestValue = 0D;

        try {
            double roverRange = rover.getRange();
            double tripTimeLimit = getTotalTripTimeLimit(rover, rover
                    .getCrewCapacity(), true);
            double tripRange = getTripTimeRange(tripTimeLimit, rover
                    .getBaseSpeed() / 2D);
            double range = roverRange;
            if (tripRange < range)
                range = tripRange;

            Iterator<ExploredLocation> i = Simulation.instance().getMars()
                    .getSurfaceFeatures().getExploredLocations().iterator();
            while (i.hasNext()) {
                ExploredLocation site = i.next();
                if (!site.isMined() && !site.isReserved() && site.isExplored()) {
                    Coordinates siteLocation = site.getLocation();
                    Coordinates homeLocation = homeSettlement.getCoordinates();
                    if (homeLocation.getDistance(siteLocation) <= (range / 2D)) {
                        double value = getMiningSiteValue(site, homeSettlement);
                        if (value > bestValue) {
                            result = site;
                            bestValue = value;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error determining best mining site.");
        }

        return result;
    }

    /**
     * Gets the estimated mineral value of a mining site.
     * @param site the mining site.
     * @param settlement the settlement valuing the minerals.
     * @return estimated value of the minerals at the site (VP).
     * @throws MissionException if error determining the value.
     */
    private static double getMiningSiteValue(ExploredLocation site,
            Settlement settlement) {

        double result = 0D;

        Map<String, Double> concentrations = site
                .getEstimatedMineralConcentrations();
        Iterator<String> i = concentrations.keySet().iterator();
        while (i.hasNext()) {
            String mineralType = i.next();
            AmountResource mineralResource = AmountResource
                    .findAmountResource(mineralType);
            Good mineralGood = GoodsUtil.getResourceGood(mineralResource);
            double mineralValue = settlement.getGoodsManager()
                    .getGoodValuePerItem(mineralGood);
            double concentration = concentrations.get(mineralType);
            double mineralAmount = (concentration / 100D) * MINERAL_BASE_AMOUNT;
            result += mineralValue * mineralAmount;
        }

        return result;
    }

    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws MissionException if error determining time limit.
     */
    private static double getTotalTripTimeLimit(Rover rover, int memberNum,
            boolean useBuffer) {

        Inventory vInv = rover.getInventory();

        double timeLimit = Double.MAX_VALUE;

        PersonConfig config = SimulationConfig.instance()
                .getPersonConfiguration();

        // Check food capacity as time limit.
        AmountResource food = AmountResource.findAmountResource("food");
        double foodConsumptionRate = config.getFoodConsumptionRate();
        double foodCapacity = vInv.getAmountResourceCapacity(food);
        double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
        if (foodTimeLimit < timeLimit)
            timeLimit = foodTimeLimit;

        // Check water capacity as time limit.
        AmountResource water = AmountResource.findAmountResource("water");
        double waterConsumptionRate = config.getWaterConsumptionRate();
        double waterCapacity = vInv.getAmountResourceCapacity(water);
        double waterTimeLimit = waterCapacity
                / (waterConsumptionRate * memberNum);
        if (waterTimeLimit < timeLimit)
            timeLimit = waterTimeLimit;

        // Check oxygen capacity as time limit.
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        double oxygenConsumptionRate = config.getOxygenConsumptionRate();
        double oxygenCapacity = vInv.getAmountResourceCapacity(oxygen);
        double oxygenTimeLimit = oxygenCapacity
                / (oxygenConsumptionRate * memberNum);
        if (oxygenTimeLimit < timeLimit)
            timeLimit = oxygenTimeLimit;

        // Convert timeLimit into millisols and use error margin.
        timeLimit = (timeLimit * 1000D);
        if (useBuffer)
            timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;

        return timeLimit;
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) {
        if (equipmentNeededCache != null)
            return equipmentNeededCache;
        else {
            Map<Class, Integer> result = new HashMap<Class, Integer>();

            // Include one EVA suit per person on mission.
            result.put(EVASuit.class, getPeopleNumber());

            // Include required number of bags.
            result.put(Bag.class, NUMBER_OF_BAGS);

            equipmentNeededCache = result;
            return result;
        }
    }

    @Override
    public Settlement getAssociatedSettlement() {
        return getStartingSettlement();
    }

    @Override
    protected boolean isCapableOfMission(Person person) {
        if (super.isCapableOfMission(person)) {
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                if (person.getSettlement() == getStartingSettlement())
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void recruitPeopleForMission(Person startingPerson) {
        super.recruitPeopleForMission(startingPerson);

        // Make sure there is at least one person left at the starting settlement.
        if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(),
                startingPerson)) {
            // Remove last person added to the mission.
            Person lastPerson = (Person) getPeople().toArray()[getPeopleNumber() - 1];
            if (lastPerson != null) {
                lastPerson.getMind().setMission(null);
                if (getPeopleNumber() < getMinPeople())
                    endMission("Not enough members.");
            }
        }
    }

    @Override
    public double getEstimatedRemainingMissionTime(boolean useBuffer) {
        double result = super.getEstimatedRemainingMissionTime(useBuffer);
        result += getEstimatedRemainingMiningSiteTime();
        return result;
    }

    /**
     * Gets the estimated time remaining at mining site in the mission.
     * @return time (millisols)
     */
    private double getEstimatedRemainingMiningSiteTime() {
        double result = 0D;

        // Use estimated remaining mining time at site if still there.
        if (MINING_SITE.equals(getPhase())) {
            MarsClock currentTime = Simulation.instance().getMasterClock()
                    .getMarsClock();
            double timeSpentAtMiningSite = MarsClock.getTimeDiff(currentTime,
                    miningSiteStartTime);
            double remainingTime = MINING_SITE_TIME - timeSpentAtMiningSite;
            if (remainingTime > 0D)
                result = remainingTime;
        } else {
            // If mission hasn't reached mining site yet, use estimated mining site time.
            if (miningSiteStartTime == null)
                result = MINING_SITE_TIME;
        }

        return result;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(
            boolean useBuffer, boolean parts) {
        Map<Resource, Number> result = super
                .getResourcesNeededForRemainingMission(useBuffer, parts);

        double miningSiteTime = getEstimatedRemainingMiningSiteTime();
        double timeSols = miningSiteTime / 1000D;

        int crewNum = getPeopleNumber();

        // Determine life support supplies needed for trip.
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate()
                * timeSols * crewNum;
        if (result.containsKey(oxygen))
            oxygenAmount += (Double) result.get(oxygen);
        result.put(oxygen, oxygenAmount);

        AmountResource water = AmountResource.findAmountResource("water");
        double waterAmount = PhysicalCondition.getWaterConsumptionRate()
                * timeSols * crewNum;
        if (result.containsKey(water))
            waterAmount += (Double) result.get(water);
        result.put(water, waterAmount);

        AmountResource food = AmountResource.findAmountResource("food");
        double foodAmount = PhysicalCondition.getFoodConsumptionRate()
                * timeSols * crewNum;
        if (result.containsKey(food))
            foodAmount += (Double) result.get(food);
        result.put(food, foodAmount);

        return result;
    }

    /**
     * Gets the range of a trip based on its time limit and mining site.
     * @param tripTimeLimit time (millisols) limit of trip.
     * @param averageSpeed the average speed of the vehicle.
     * @return range (km) limit.
     */
    private static double getTripTimeRange(double tripTimeLimit,
            double averageSpeed) {
        double tripTimeTravellingLimit = tripTimeLimit - MINING_SITE_TIME;
        double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
        double averageSpeedMillisol = averageSpeed / millisolsInHour;
        return tripTimeTravellingLimit * averageSpeedMillisol;
    }

    /**
     * Gets the mission mining site.
     * @return mining site.
     */
    public ExploredLocation getMiningSite() {
        return miningSite;
    }

    @Override
    public void endMission(String reason) {
        super.endMission(reason);

        if (miningSite != null)
            miningSite.setReserved(false);
        if (luv != null)
            luv.setReservedForMission(false);
    }

    /**
     * Reserves a light utility vehicle for the mission.
     * @return reserved light utility vehicle or null if none.
     */
    private LightUtilityVehicle reserveLightUtilityVehicle() {
        LightUtilityVehicle result = null;

        Iterator<Vehicle> i = getStartingSettlement().getParkedVehicles()
                .iterator();
        while (i.hasNext() && (result == null)) {
            Vehicle vehicle = i.next();

            if (vehicle instanceof LightUtilityVehicle) {
                LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
                if (luvTemp.getStatus().equals(Vehicle.PARKED)
                        && !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0)) {
                    result = luvTemp;
                    luvTemp.setReservedForMission(true);
                }
            }
        }

        return result;
    }

    /**
     * Gets the mission's light utility vehicle.
     * @return light utility vehicle.
     */
    public LightUtilityVehicle getLightUtilityVehicle() {
        return luv;
    }

    /**
     * Gets the amount of a mineral currently excavated.
     * @param mineral the mineral resource.
     * @return amount (kg)
     */
    public double getMineralExcavationAmount(AmountResource mineral) {
        if (excavatedMinerals.containsKey(mineral))
            return excavatedMinerals.get(mineral);
        else
            return 0D;
    }

    /**
     * Gets the total amount of a mineral that has been excavated so far.
     * @param mineral the mineral resource.
     * @return amount (kg)
     */
    public double getTotalMineralExcavatedAmount(AmountResource mineral) {
        if (totalExcavatedMinerals.containsKey(mineral))
            return totalExcavatedMinerals.get(mineral);
        else
            return 0D;
    }

    /**
     * Excavates an amount of a mineral.
     * @param mineral the mineral resource.
     * @param amount the amount (kg)
     */
    public void excavateMineral(AmountResource mineral, double amount) {
        double currentExcavated = amount;
        if (excavatedMinerals.containsKey(mineral))
            currentExcavated += excavatedMinerals.get(mineral);
        excavatedMinerals.put(mineral, currentExcavated);

        double totalExcavated = amount;
        if (totalExcavatedMinerals.containsKey(mineral))
            totalExcavated += totalExcavatedMinerals.get(mineral);
        totalExcavatedMinerals.put(mineral, totalExcavated);

        fireMissionUpdate(EXCAVATE_MINERALS_EVENT);
    }

    /**
     * Collects an amount of a mineral.
     * @param mineral the mineral resource.
     * @param amount the amount (kg)
     * @throws Exception if error collecting mineral.
     */
    public void collectMineral(AmountResource mineral, double amount) {
        double currentExcavated = 0D;
        if (excavatedMinerals.containsKey(mineral))
            currentExcavated = excavatedMinerals.get(mineral);
        if (currentExcavated >= amount)
            excavatedMinerals.put(mineral, (currentExcavated - amount));
        else
            throw new IllegalStateException(mineral.getName() + " amount: "
                    + amount + " more than currently excavated.");
        fireMissionUpdate(COLLECT_MINERALS_EVENT);
    }

    @Override
    public void destroy() {
        super.destroy();

        miningSite = null;
        miningSiteStartTime = null;
        if (excavatedMinerals != null) excavatedMinerals.clear();
        excavatedMinerals = null;
        if (totalExcavatedMinerals != null) totalExcavatedMinerals.clear();
        totalExcavatedMinerals = null;
        luv = null;
    }
}