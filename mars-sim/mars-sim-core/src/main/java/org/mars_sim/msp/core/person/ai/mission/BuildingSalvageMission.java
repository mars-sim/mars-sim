/**
 * Mars Simulation Project
 * BuildingSalvageMission.java
 * @version 3.03 2012-06-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.SalvageBuilding;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.*;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mission for salvaging a construction stage at a building construction site.
 */
public class BuildingSalvageMission extends Mission implements Serializable {

    private static Logger logger = Logger.getLogger(BuildingSalvageMission.class.getName());

    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Salvage Building";

    // Mission phases
    final public static String PREPARE_SITE_PHASE = "Prepare Site";
    final public static String SALVAGE_PHASE = "Salvage";

    // Number of mission members.
    private static final int MIN_PEOPLE = 3;
    private static final int MAX_PEOPLE = 10;

    // Light utility vehicle attachment parts for salvage.
    public static final String SOIL_COMPACTOR = "soil compactor";
    public static final String BACKHOE = "backhoe";
    public static final String BULLDOZER_BLADE = "bulldozer blade";
    public static final String CRANE_BOOM = "crane boom";

    // Time (millisols) required to prepare construction site for salvaging stage.
    private static final double SITE_PREPARE_TIME = 500D;

    // Data members
    private Settlement settlement;
    private ConstructionSite constructionSite;
    private ConstructionStage constructionStage;
    private List<GroundVehicle> constructionVehicles;
    private MarsClock sitePreparationStartTime;
    private boolean finishingExistingStage;
    private List<Part> luvAttachmentParts;
    private double wearCondition;

    /**
     * Constructor
     * @param startingPerson the person starting the mission.
     * @throws MissionException if error creating mission.
     */
    public BuildingSalvageMission(Person startingPerson) {
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson, MIN_PEOPLE);

        // Set wear condition to 100% by default.
        wearCondition = 100D;

        if (!isDone()) {
            // Sets the settlement.
            settlement = startingPerson.getSettlement();

            // Sets the mission capacity.
            setMissionCapacity(MAX_PEOPLE);
            int availableSuitNum = Mission
                    .getNumberAvailableEVASuitsAtSettlement(settlement);
            if (availableSuitNum < getMissionCapacity())
                setMissionCapacity(availableSuitNum);

            // Recruit additional people to mission.
            recruitPeopleForMission(startingPerson);

            // Determine construction site and stage.
            int constructionSkill = startingPerson.getMind().getSkillManager()
                    .getEffectiveSkillLevel(Skill.CONSTRUCTION);
            ConstructionManager manager = settlement.getConstructionManager();
            SalvageValues values = manager.getSalvageValues();
            double existingSitesProfit = values
                    .getAllSalvageSitesProfit(constructionSkill);
            double newSiteProfit = values
                    .getNewSalvageSiteProfit(constructionSkill);

            if (existingSitesProfit > newSiteProfit) {
                // Determine which existing construction site to work on.
                constructionSite = determineMostProfitableSalvageConstructionSite(
                        settlement, constructionSkill);
            } else {
                // Determine existing building to salvage.
                Building salvageBuilding = determineMostProfitableSalvageBuilding(
                        settlement, constructionSkill);

                if (salvageBuilding != null) {
                    // Create new salvage construction site.
                    constructionSite = manager
                            .createNewSalvageConstructionSite(salvageBuilding);

                    // Set wear condition to salvaged building's wear condition.
                    wearCondition = salvageBuilding.getMalfunctionManager()
                            .getWearCondition();
                } else {
                    logger.log(Level.WARNING,
                            "No building could be found that is profitable to salvage.");
                }
            }

            // Prepare salvage construction site.
            if (constructionSite != null) {

                // Determine new stage to work on.
                if (constructionSite.hasUnfinishedStage()) {
                    constructionStage = constructionSite
                            .getCurrentConstructionStage();
                    finishingExistingStage = true;
                    logger.log(Level.INFO, "Continuing work on existing site at "
                            + settlement.getName());
                } else {
                    constructionStage = constructionSite
                            .getCurrentConstructionStage();
                    if (constructionStage != null) {
                        constructionStage.setCompletedWorkTime(0D);
                        constructionStage.setSalvaging(true);
                        logger.log(Level.INFO, "Starting salvage construction stage: "
                                + constructionStage);
                    } else
                        endMission("Salvage construction stage could not be found.");
                }

                // Mark construction site as undergoing salvage.
                if (constructionStage != null)
                    constructionSite.setUndergoingSalvage(true);
            } else {
                endMission("Salvage construction site could not be found or created.");
            }

            // Reserve construction vehicles.
            reserveConstructionVehicles();

            // Retrieve construction LUV attachment parts.
            retrieveConstructionLUVParts();
        }

        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(SALVAGE_PHASE);

        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);
        setPhaseDescription("Preparing salvage construction site at "
                + settlement.getName());
    }

    /**
     * Constructor
     * @param members the mission members.
     * @param settlement the settlement.
     * @param building the building to salvage. (null if none)
     * @param site the existing salvage construction site. (null if none)
     * @param vehicles the construction vehicles.
     * @throws MissionException if error creating mission.
     */
    public BuildingSalvageMission(Collection<Person> members,
            Settlement settlement, Building building, ConstructionSite site,
            List<GroundVehicle> vehicles) {

        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, (Person) members.toArray()[0], 1);

        this.settlement = settlement;

        ConstructionManager manager = settlement.getConstructionManager();

        if (building != null) {
            // Create new salvage construction site.
            constructionSite = manager
                    .createNewSalvageConstructionSite(building);
        } else if (site != null) {
            constructionSite = site;
        } else {
            logger.log(Level.SEVERE,
                    "Neither salvage building or salvage construction site provided.");
            throw new IllegalStateException(PREPARE_SITE_PHASE + ":"
                    + "Neither salvage building or salvage construction site provided.");
        }

        // Prepare salvage construction site.
        if (constructionSite != null) {

            // Determine new stage to work on.
            if (constructionSite.hasUnfinishedStage()) {
                constructionStage = constructionSite
                        .getCurrentConstructionStage();
                finishingExistingStage = true;
                logger.log(Level.INFO, "Continuing work on existing site at "
                        + settlement.getName());
            } else {
                constructionStage = constructionSite
                        .getCurrentConstructionStage();
                if (constructionStage != null) {
                    constructionStage.setCompletedWorkTime(0D);
                    constructionStage.setSalvaging(true);
                    logger.log(Level.INFO,
                            "Starting salvage construction stage: "
                                    + constructionStage);
                } else
                    endMission("Salvage construction stage could not be found.");
            }

            // Mark construction site as undergoing salvage.
            if (constructionStage != null)
                constructionSite.setUndergoingSalvage(true);
        } else {
            endMission("Salvage construction site could not be found or created.");
        }

        // Mark site as undergoing salvage.
        if (constructionStage != null)
            constructionSite.setUndergoingSalvage(true);

        // Add mission members.
        Iterator<Person> i = members.iterator();
        while (i.hasNext())
            i.next().getMind().setMission(this);

        // Reserve construction vehicles.
        constructionVehicles = vehicles;
        Iterator<GroundVehicle> j = vehicles.iterator();
        while (j.hasNext())
            j.next().setReservedForMission(true);

        // Retrieve construction LUV attachment parts.
        retrieveConstructionLUVParts();

        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(SALVAGE_PHASE);

        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);
        setPhaseDescription("Preparing salvage construction site at "
                + settlement.getName());
    }

    /**
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

        double result = 0D;

        // Determine job modifier.
        Job job = person.getMind().getJob();
        double jobModifier = 0D;
        if (job != null)
            jobModifier = job
                    .getStartMissionProbabilityModifier(BuildingSalvageMission.class);

        // Check if person is in a settlement.
        boolean inSettlement = person.getLocationSituation().equals(
                Person.INSETTLEMENT);

        if (inSettlement && (jobModifier > 0D)) {
            Settlement settlement = person.getSettlement();

            // Check if available light utility vehicles.
            boolean reservableLUV = isLUVAvailable(settlement);

            // Check if LUV attachment parts available.
            boolean availableAttachmentParts = areAvailableAttachmentParts(settlement);

            // Check if enough available people at settlement for mission.
            int availablePeopleNum = 0;
            Iterator<Person> i = settlement.getInhabitants().iterator();
            while (i.hasNext()) {
                Person member = i.next();
                boolean noMission = !member.getMind().hasActiveMission();
                boolean isFit = !member.getPhysicalCondition()
                        .hasSeriousMedicalProblems();
                if (noMission && isFit)
                    availablePeopleNum++;
            }
            boolean enoughPeople = (availablePeopleNum >= MIN_PEOPLE);
            
            // Check if settlement has construction override flag set.
            boolean constructionOverride = settlement.getConstructionOverride();

            if (reservableLUV && availableAttachmentParts && enoughPeople && !constructionOverride) {
                try {
                    int constructionSkill = person.getMind().getSkillManager()
                            .getEffectiveSkillLevel(Skill.CONSTRUCTION);
                    SalvageValues values = settlement.getConstructionManager()
                            .getSalvageValues();
                    double salvageProfit = values
                            .getSettlementSalvageProfit(constructionSkill);
                    if (salvageProfit > 0D) {
                        result = 10D;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                            "Error getting salvage construction site.", e);
                }
            }

            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getSettlement()) < MIN_PEOPLE)
                result = 0D;
        }

        return result;
    }

    /**
     * Determines the most profitable salvage construction site at the settlement.
     * @param settlement the settlement
     * @param constructionSkill the architect's construction skill.
     * @return construction site or null if none found.
     * @throws Exception if error determining construction site.
     */
    private ConstructionSite determineMostProfitableSalvageConstructionSite(
            Settlement settlement, int constructionSkill) {
        ConstructionSite result = null;

        double topSiteProfit = 0D;
        ConstructionManager manager = settlement.getConstructionManager();
        Iterator<ConstructionSite> i = manager
                .getConstructionSitesNeedingSalvageMission().iterator();
        while (i.hasNext()) {
            ConstructionSite site = i.next();
            double siteProfit = manager.getSalvageValues()
                    .getSalvageSiteProfit(site, constructionSkill);
            if (siteProfit > topSiteProfit) {
                result = site;
                topSiteProfit = siteProfit;
            }
        }

        return result;
    }

    /**
     * Determines which building at the settlement would be the most profitable to salvage.
     * @param settlement the settlement.
     * @param constructionSkill the architect's construction skill.
     * @return building to salvage or null in none found.
     * @throws Exception if error determining building.
     */
    private Building determineMostProfitableSalvageBuilding(
            Settlement settlement, int constructionSkill) {
        Building result = null;

        double topSalvageProfit = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings()
                .iterator();
        while (i.hasNext()) {
            Building building = i.next();
            SalvageValues values = settlement.getConstructionManager()
                    .getSalvageValues();
            double salvageProfit = values.getNewBuildingSalvageProfit(building,
                    constructionSkill);
            if (salvageProfit > topSalvageProfit) {
                result = building;
                topSalvageProfit = salvageProfit;
            }
        }

        return result;
    }

    @Override
    protected void determineNewPhase() {
        if (PREPARE_SITE_PHASE.equals(getPhase())) {
            setPhase(SALVAGE_PHASE);
            setPhaseDescription("Salvage Construction Site Stage: "
                    + constructionStage.getInfo().getName());
        } else if (SALVAGE_PHASE.equals(getPhase()))
            endMission("Successfully ended salvage");
    }

    @Override
    protected void performPhase(Person person) {
        super.performPhase(person);
        if (PREPARE_SITE_PHASE.equals(getPhase()))
            prepareSitePhase(person);
        else if (SALVAGE_PHASE.equals(getPhase()))
            salvagePhase(person);
    }

    @Override
    public Settlement getAssociatedSettlement() {
        return settlement;
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) {

        Map<Class, Integer> equipment = new HashMap<Class, Integer>(1);
        equipment.put(EVASuit.class, getPeopleNumber());

        return equipment;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(
            boolean useBuffer, boolean parts) {
        Map<Resource, Number> resources = new HashMap<Resource, Number>();
        return resources;
    }

    /**
     * Performs the prepare site phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void prepareSitePhase(Person person) {

        if (finishingExistingStage) {
            // If finishing uncompleted existing construction stage, skip resource loading.
            setPhaseEnded(true);
        }

        // Check if site preparation time has expired.
        MarsClock currentTime = Simulation.instance().getMasterClock()
                .getMarsClock();
        if (sitePreparationStartTime == null)
            sitePreparationStartTime = (MarsClock) currentTime.clone();
        if (MarsClock.getTimeDiff(currentTime, sitePreparationStartTime) >= SITE_PREPARE_TIME)
            setPhaseEnded(true);
    }

    /**
     * Performs the salvage phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void salvagePhase(Person person) {

        // Anyone in the crew or a single person at the home settlement has a
        // dangerous illness, end phase.
        if (hasEmergency())
            setPhaseEnded(true);

        if (!getPhaseEnded()) {

            // 75% chance of assigning task, otherwise allow break.
            if (RandomUtil.lessThanRandPercent(75D)) {
                // try {
                // Assign salvage building task to person.
                if (SalvageBuilding.canSalvage(person)) {
                    assignTask(person, new SalvageBuilding(person,
                            constructionStage, constructionVehicles));
                }
                // }
                // catch(Exception e) {
                // logger.log(Level.SEVERE, "Error during salvage.", e);
                // throw new MissionException(getPhase(), e);
                // }
            }
        }

        if (constructionStage.isComplete()) {
            setPhaseEnded(true);
            settlement.getConstructionManager().getConstructionValues()
                    .clearCache();

            // Remove salvaged construction stage from site.
            constructionSite.removeSalvagedStage(constructionStage);

            // Salvage construction parts from the stage.
            salvageConstructionParts();

            // Mark construction site as not undergoing salvage.
            constructionSite.setUndergoingSalvage(false);

            // Remove construction site if all salvaging complete.
            if (constructionStage.getInfo().getType().equals(
                    ConstructionStageInfo.FOUNDATION)) {
                settlement.getConstructionManager().removeConstructionSite(
                        constructionSite);
                settlement.fireUnitUpdate(
                        ConstructionManager.FINISH_SALVAGE_EVENT,
                        constructionSite);
                logger.log(Level.INFO,
                        "Construction site completely salvaged at "
                                + settlement.getName());
            }
        }
    }

    @Override
    public void endMission(String reason) {
        super.endMission(reason);

        // Mark site as not undergoing salvage.
        if (constructionSite != null)
            constructionSite.setUndergoingSalvage(false);

        // Unreserve all mission construction vehicles.
        unreserveConstructionVehicles();

        // Store all LUV attachment parts in settlement.
        Iterator<Part> i = luvAttachmentParts.iterator();
        while (i.hasNext()) {
            Part part = i.next();
            try {
                settlement.getInventory().storeItemResources(part, 1);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error storing attachment part "
                        + part.getName());
                endMission("Construction attachment part " + part.getName()
                        + " could not be stored.");
            }
        }
    }

    @Override
    protected boolean isCapableOfMission(Person person) {
        if (super.isCapableOfMission(person)) {
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                if (person.getSettlement() == settlement)
                    return true;
            }
        }
        return false;
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
            Part soilCompactor = (Part) Part.findItemResource(SOIL_COMPACTOR);
            if (!inv.hasItemResource(soilCompactor))
                result = false;
            Part backhoe = (Part) Part.findItemResource(BACKHOE);
            if (!inv.hasItemResource(backhoe))
                result = false;
            Part bulldozerBlade = (Part) Part.findItemResource(BULLDOZER_BLADE);
            if (!inv.hasItemResource(bulldozerBlade))
                result = false;
            Part craneBoom = (Part) Part.findItemResource(CRANE_BOOM);
            if (!inv.hasItemResource(craneBoom))
                result = false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting parts.");
        }

        return result;
    }

    /**
     * Reserve construction vehicles for the mission.
     */
    private void reserveConstructionVehicles() {
        if (constructionStage != null) {
            constructionVehicles = new ArrayList<GroundVehicle>();
            Iterator<ConstructionVehicleType> j = constructionStage.getInfo()
                    .getVehicles().iterator();
            while (j.hasNext()) {
                ConstructionVehicleType vehicleType = j.next();
                // Only handle light utility vehicles for now.
                if (vehicleType.getVehicleClass() == LightUtilityVehicle.class) {
                    LightUtilityVehicle luv = reserveLightUtilityVehicle();
                    if (luv != null)
                        constructionVehicles.add(luv);
                    else
                        endMission("Light utility vehicle not available.");
                }
            }
        }
    }

    /**
     * Retrieve LUV attachment parts from the settlement.
     */
    private void retrieveConstructionLUVParts() {
        if (constructionStage != null) {
            luvAttachmentParts = new ArrayList<Part>();
            Iterator<ConstructionVehicleType> k = constructionStage.getInfo()
                    .getVehicles().iterator();
            while (k.hasNext()) {
                Iterator<Part> l = k.next().getAttachmentParts().iterator();
                while (l.hasNext()) {
                    Part part = l.next();
                    try {
                        settlement.getInventory()
                                .retrieveItemResources(part, 1);
                        luvAttachmentParts.add(part);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE,
                                "Error retrieving attachment part "
                                        + part.getName());
                        endMission("Construction attachment part "
                                + part.getName() + " could not be retrieved.");
                    }
                }
            }
        }
    }

    /**
     * Reserves a light utility vehicle for the mission.
     * @return reserved light utility vehicle or null if none.
     */
    private LightUtilityVehicle reserveLightUtilityVehicle() {
        LightUtilityVehicle result = null;

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
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
     * Unreserves all construction vehicles used in mission.
     */
    private void unreserveConstructionVehicles() {
        if (constructionVehicles != null) {
            Iterator<GroundVehicle> i = constructionVehicles.iterator();
            while (i.hasNext())
                i.next().setReservedForMission(false);
        }
    }

    /**
     * Gets a list of all construction vehicles used by the mission.
     * @return list of construction vehicles.
     */
    public List<GroundVehicle> getConstructionVehicles() {
        return new ArrayList<GroundVehicle>(constructionVehicles);
    }

    /**
     * Salvage construction parts from the stage.
     * @throws Exception if error salvaging construction parts.
     */
    private void salvageConstructionParts() {

        double salvageChance = 50D;

        // Modify salvage chance based on building wear condition.
        // Note: if non-building construction stage, wear condition should be 100%.
        salvageChance = (wearCondition * .25D) + 25D;

        // Get average construction skill of mission members.
        double totalSkill = 0D;
        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            int constructionSkill = i.next().getMind().getSkillManager()
                    .getSkillLevel(Skill.CONSTRUCTION);
            totalSkill += constructionSkill;
        }
        double averageSkill = totalSkill / getPeopleNumber();

        // Modify salvage chance based on average construction skill.
        salvageChance += averageSkill * 5D;

        // Salvage construction parts.
        Map<Part, Integer> salvagableParts = constructionStage.getInfo()
                .getParts();
        Iterator<Part> j = salvagableParts.keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            int number = salvagableParts.get(part);

            int salvagedNumber = 0;
            for (int x = 0; x < number; x++) {
                if (RandomUtil.lessThanRandPercent(salvageChance))
                    salvagedNumber++;
            }

            if (salvagedNumber > 0) {

                double mass = salvagedNumber * part.getMassPerItem();
                double capacity = settlement.getInventory()
                        .getGeneralCapacity();
                if (mass <= capacity)
                    settlement.getInventory().storeItemResources(part,
                            salvagedNumber);

                // Recalculate settlement good value for salvaged part.
                settlement.getGoodsManager().updateGoodValue(
                        GoodsUtil.getResourceGood(part), false);
            }
        }
    }
    
    @Override
    protected boolean hasEmergency() {
        boolean result = super.hasEmergency();
        
        try {
            // Cancel construction mission if there are any beacon vehicles within range that need help.
            Vehicle vehicleTarget = null;
            Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
            if (vehicle != null) {
                vehicleTarget = RescueSalvageVehicle.findAvailableBeaconVehicle(settlement, vehicle.getRange());
                if (vehicleTarget != null) {
                    if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget)) 
                        result = true;
                }
            }
        }
        catch (Exception e) {}
        
        return result;
    }

    /**
     * Gets the mission's construction site.
     * @return construction site.
     */
    public ConstructionSite getConstructionSite() {
        return constructionSite;
    }

    /**
     * Gets the mission's construction stage.
     * @return construction stage.
     */
    public ConstructionStage getConstructionStage() {
        return constructionStage;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        settlement = null;
        constructionSite = null;
        constructionStage = null;
        if (constructionVehicles != null) constructionVehicles.clear();
        constructionVehicles = null;
        sitePreparationStartTime = null;
        if (luvAttachmentParts != null) luvAttachmentParts.clear();
        luvAttachmentParts = null;
    }
}