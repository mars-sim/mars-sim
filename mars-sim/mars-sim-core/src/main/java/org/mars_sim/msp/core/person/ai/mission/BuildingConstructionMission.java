/**
 * Mars Simulation Project
 * BuildingConstructionMission.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.InventoryException;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Mission for construction a stage for a settlement building.
 */
public class BuildingConstructionMission extends Mission implements Serializable {

    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // Default description.
    public static final String DEFAULT_DESCRIPTION = "Construct Building";
    
    // Mission phases
    final public static String PREPARE_SITE_PHASE = "Prepare Site";
    final public static String CONSTRUCTION_PHASE = "Construction";
    
    // Number of mission members.
    private static final int MIN_PEOPLE = 3;
    private static final int MAX_PEOPLE = 10;
    
    // Light utility vehicle attachment parts for construction.
    public static final String SOIL_COMPACTOR = "soil compactor";
    public static final String BACKHOE = "backhoe";
    public static final String BULLDOZER_BLADE = "bulldozer blade";
    public static final String CRANE_BOOM = "crane boom";
    
    // Time (millisols) required to prepare construction site for stage.
    private static final double SITE_PREPARE_TIME = 500D;
    
    // Data members
    private Settlement settlement;
    private ConstructionSite constructionSite;
    private ConstructionStage constructionStage;
    private List<GroundVehicle> constructionVehicles;
    private boolean constructionSuppliesAdded;
    private MarsClock sitePreparationStartTime;
    private boolean finishingExistingStage;
    private boolean constructionSuppliesLoaded;
    private List<Part> luvAttachmentParts;
    
    /**
     * Constructor
     * @param startingPerson the person starting the mission.
     * @throws MissionException if error creating mission.
     */
    public BuildingConstructionMission(Person startingPerson) throws MissionException {
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, startingPerson, MIN_PEOPLE);
        
        if (!isDone()) {
            // Sets the settlement.
            settlement = startingPerson.getSettlement();
            
            // Sets the mission capacity.
            setMissionCapacity(MAX_PEOPLE);
            int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(settlement);
            if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
            
            // Recruit additional people to mission.
            recruitPeopleForMission(startingPerson);
            
            try {
                // Determine construction site and stage.
                int constructionSkill = startingPerson.getMind().getSkillManager().getEffectiveSkillLevel(
                        Skill.CONSTRUCTION);
                ConstructionManager manager = settlement.getConstructionManager();
                ConstructionValues values = manager.getConstructionValues();
                double existingSitesProfit = values.getAllConstructionSitesProfit(constructionSkill);
                double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
                
                if (existingSitesProfit > newSiteProfit) {
                    // Determine which existing construction site to work on.
                    double topSiteProfit = 0D;
                    Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingConstructionMission().iterator();
                    while (i.hasNext()) {
                        ConstructionSite site = i.next();
                        double siteProfit = values.getConstructionSiteProfit(site, constructionSkill);
                        if ((siteProfit > topSiteProfit) && 
                                hasExistingSiteConstructionMaterials(site, constructionSkill)) {
                            constructionSite = site;
                            topSiteProfit = siteProfit;
                        }
                    }
                }
                else if (hasAnyNewSiteConstructionMaterials(constructionSkill, settlement)) {
                    // Create new site.
                    constructionSite = manager.createNewConstructionSite();
                    // TODO Determine construction site location and facing.
                    logger.log(Level.INFO, "New construction site added at " + settlement.getName());
                }
                
                if (constructionSite != null) {
                    
                    // Determine new stage to work on.
                    if (constructionSite.hasUnfinishedStage()) {
                        constructionStage = constructionSite.getCurrentConstructionStage(); 
                        finishingExistingStage = true;
                        logger.log(Level.INFO, "Continuing work on existing site at " + settlement.getName());
                    }
                    else {
                        ConstructionStageInfo stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
                        
                        if (stageInfo != null) {
                            constructionStage = new ConstructionStage(stageInfo, constructionSite);
                            constructionSite.addNewStage(constructionStage);
                            values.clearCache();
                            logger.log(Level.INFO, "Starting new construction stage: " + constructionStage);
                        }
                        else {
                            endMission("New construction stage could not be determined.");
                        }
                    }
                    
                    // Mark site as undergoing construction.
                    if (constructionStage != null) constructionSite.setUndergoingConstruction(true);
                }
                else {
                    endMission("Construction site could not be found or created.");
                }
                
                // Reserve construction vehicles.
                reserveConstructionVehicles();
                
                // Retrieve construction LUV attachment parts.
                retrieveConstructionLUVParts();
            } 
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error determining construction sites.");
                throw new MissionException("Error determining construction sites.", e);
            }
        }
        
        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(CONSTRUCTION_PHASE);
        
        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);
        setPhaseDescription("Preparing construction site at " + settlement.getName());
    }
    
    /**
     * Constructor
     * @param members the mission members.
     * @param settlement the settlement.
     * @param site the construction site.
     * @param stageInfo the construction stage info.
     * @param vehicles the construction vehicles.
     * @throws MissionException if error creating mission.
     */
    public BuildingConstructionMission(Collection<Person> members, Settlement settlement, 
            ConstructionSite site, ConstructionStageInfo stageInfo, 
            List<GroundVehicle> vehicles) throws MissionException {
        
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, (Person) members.toArray()[0], 1);
        
        this.settlement = settlement;
        
        ConstructionManager manager = settlement.getConstructionManager();
        if (site != null) constructionSite = site;
        else {
            logger.log(Level.INFO, "New construction site added at " + settlement.getName());
            constructionSite = manager.createNewConstructionSite();
            // TODO determine location and facing of construction site.
            if (constructionSite == null) endMission("Construction site could not be created.");
        }
        
        if (constructionSite.hasUnfinishedStage()) {
            constructionStage = constructionSite.getCurrentConstructionStage();
        }
        else {
            logger.log(Level.INFO, "Starting new construction stage: " + constructionStage);
            constructionStage = new ConstructionStage(stageInfo, constructionSite);
            try {
                constructionSite.addNewStage(constructionStage);
            }
            catch (Exception e) {
                endMission("Construction stage could not be created.");
            }
        }
        
        // Mark site as undergoing construction.
        if (constructionStage != null) constructionSite.setUndergoingConstruction(true);
        
        // Add mission members.
        Iterator<Person> i = members.iterator();
        while (i.hasNext()) i.next().getMind().setMission(this);
        
        // Reserve construction vehicles.
        constructionVehicles = vehicles;
        Iterator<GroundVehicle> j = vehicles.iterator();
        while (j.hasNext()) j.next().setReservedForMission(true);
        
        // Retrieve construction LUV attachment parts.
        retrieveConstructionLUVParts();
        
        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(CONSTRUCTION_PHASE);
        
        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);
        setPhaseDescription("Preparing construction site at " + settlement.getName());
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
            jobModifier = job.getStartMissionProbabilityModifier(BuildingConstructionMission.class); 
        
        // Check if person is in a settlement.
        boolean inSettlement = person.getLocationSituation().equals(Person.INSETTLEMENT);
        
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
                boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
                if (noMission && isFit) availablePeopleNum++;
            }
            boolean enoughPeople = (availablePeopleNum >= MIN_PEOPLE);
            
            if (reservableLUV && availableAttachmentParts && enoughPeople) {
                
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(
                            Skill.CONSTRUCTION);
                    if (hasAnyNewSiteConstructionMaterials(constructionSkill, settlement)) {
                        ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
                        double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
                        if (constructionProfit > 0D) {
                            result = 10D;
                        }
                    }
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting construction site.", e);
                }
            }       
            
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) 
                result = 0D;
        }
        
        return result;
    }
    
    /**
     * Reserve construction vehicles for the mission.
     */
    private void reserveConstructionVehicles() {
        if (constructionStage != null) {
            constructionVehicles = new ArrayList<GroundVehicle>();
            Iterator<ConstructionVehicleType> j = constructionStage.getInfo().getVehicles().iterator();
            while (j.hasNext()) {
                ConstructionVehicleType vehicleType = j.next();
                // Only handle light utility vehicles for now.
                if (vehicleType.getVehicleClass() == LightUtilityVehicle.class) {
                    LightUtilityVehicle luv = reserveLightUtilityVehicle();
                    if (luv != null) constructionVehicles.add(luv); 
                    else endMission("Light utility vehicle not available.");
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
            Iterator<ConstructionVehicleType> k = constructionStage.getInfo().getVehicles().iterator();
            while (k.hasNext()) {
                Iterator<Part> l = k.next().getAttachmentParts().iterator();
                while (l.hasNext()) {
                    Part part = l.next();
                    try {
                        settlement.getInventory().retrieveItemResources(part, 1);
                        luvAttachmentParts.add(part);
                    }
                    catch (Exception e) {
                        logger.log(Level.SEVERE, "Error retrieving attachment part " + part.getName());
                        endMission("Construction attachment part " + part.getName() + " could not be retrieved.");
                    }
                }
            }
        }
    }
    
    /**
     * Determines a new construction stage info for a site.
     * @param site the construction site.
     * @param skill the architect's construction skill.
     * @return construction stage info.
     * @throws Exception if error determining construction stage info.
     */
    private ConstructionStageInfo determineNewStageInfo(ConstructionSite site, int skill) throws Exception {
        ConstructionStageInfo result = null;
        
        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        Map<ConstructionStageInfo, Double> stageProfits = 
            values.getNewConstructionStageProfits(site, skill);
        double totalProfit = 0D;
        Iterator<ConstructionStageInfo> i = stageProfits.keySet().iterator();
        while (i.hasNext()) {
            ConstructionStageInfo info = i.next();
            double infoProfit = stageProfits.get(info);
            if (infoProfit > 0D) totalProfit += infoProfit;
        }
        
        double randomValue = RandomUtil.getRandomDouble(totalProfit);
        
        double totalProfit2 = 0D;
        Iterator<ConstructionStageInfo> j = stageProfits.keySet().iterator();
        while (j.hasNext()) {
            ConstructionStageInfo info = j.next();
            double infoProfit = stageProfits.get(info);
            if (infoProfit > 0D) {
                totalProfit2 += infoProfit;
                if (totalProfit2 > randomValue) {
                    result = info;
                    break;
                }
            }
        }
        
        return result;
    }
    
    @Override
    protected boolean isCapableOfMission(Person person) {
        if (super.isCapableOfMission(person)) {
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                if (person.getSettlement() == settlement) return true;
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
                if (vehicle.isReserved()) usable = false;
                if (!vehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
                if (((Crewable) vehicle).getCrewNum() > 0) usable = false;
                if (usable) result = true;
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
            if (!inv.hasItemResource(soilCompactor)) result = false;
            Part backhoe = (Part) Part.findItemResource(BACKHOE);
            if (!inv.hasItemResource(backhoe)) result = false;
            Part bulldozerBlade = (Part) Part.findItemResource(BULLDOZER_BLADE);
            if (!inv.hasItemResource(bulldozerBlade)) result = false;
            Part craneBoom = (Part) Part.findItemResource(CRANE_BOOM);
            if (!inv.hasItemResource(craneBoom)) result = false;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting parts.");
        }
        
        return result;
    }
    
    @Override
    protected void determineNewPhase() throws MissionException {
        if (PREPARE_SITE_PHASE.equals(getPhase())) {
            setPhase(CONSTRUCTION_PHASE);
            setPhaseDescription("Constructing Site Stage: " + constructionStage.getInfo().getName());
        }
        else if (CONSTRUCTION_PHASE.equals(getPhase())) endMission("Successfully ended construction");
    }

    @Override
    protected void performPhase(Person person) throws MissionException {
        super.performPhase(person);
        if (PREPARE_SITE_PHASE.equals(getPhase())) prepareSitePhase(person);
        else if (CONSTRUCTION_PHASE.equals(getPhase())) constructionPhase(person);
    }
    
    /**
     * Performs the prepare site phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void prepareSitePhase(Person person) throws MissionException {
        
        if (finishingExistingStage) {
            // If finishing uncompleted existing construction stage, skip resource loading.
            setPhaseEnded(true);
        }
        else if (!constructionSuppliesLoaded){
            // Load all resources needed for construction.
            Inventory inv = settlement.getInventory();
            
            try {
                // Load amount resources.
                Iterator<AmountResource> i = constructionStage.getInfo().getResources().keySet().iterator();
                while (i.hasNext()) {
                    AmountResource resource = i.next();
                    double amount = constructionStage.getInfo().getResources().get(resource);
                    if (inv.getAmountResourceStored(resource) >= amount)
                        inv.retrieveAmountResource(resource, amount);
                }
                
                // Load parts.
                Iterator<Part> j = constructionStage.getInfo().getParts().keySet().iterator();
                while (j.hasNext()) {
                    Part part = j.next();
                    int number = constructionStage.getInfo().getParts().get(part);
                    if (inv.getItemResourceNum(part) >= number)
                        inv.retrieveItemResources(part, number);
                }
            }
            catch (InventoryException e) {
                logger.log(Level.SEVERE, "Error in getting construction resources.");
                throw new MissionException("Error in getting construction resources.", e);
            }
            
            constructionSuppliesLoaded = true;
        }
        
        // Check if site preparation time has expired.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if (sitePreparationStartTime == null) sitePreparationStartTime = (MarsClock) currentTime.clone();
        if (MarsClock.getTimeDiff(currentTime, sitePreparationStartTime) >= SITE_PREPARE_TIME) 
            setPhaseEnded(true);
    }
    
    /**
     * Performs the construction phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void constructionPhase(Person person) throws MissionException {

        // Anyone in the crew or a single person at the home settlement has a 
        // dangerous illness, end phase.
        if (hasEmergency()) setPhaseEnded(true);
        
        if (!getPhaseEnded()) {
            
            // 75% chance of assigning task, otherwise allow break.
            if (RandomUtil.lessThanRandPercent(75D)) {
                try {
                    // Assign construction task to person.
                    if (ConstructBuilding.canConstruct(person)) {
                        assignTask(person, new ConstructBuilding(person, constructionStage, 
                                constructionVehicles));
                    }
                }
                catch(Exception e) {
                    logger.log(Level.SEVERE, "Error during construction.", e);
                    throw new MissionException(getPhase(), e);
                }
            }
        }
        
        if (constructionStage.isComplete()) {
            setPhaseEnded(true);
            settlement.getConstructionManager().getConstructionValues().clearCache();
            
            // Construct building if all site construction complete.
            if (constructionSite.isAllConstructionComplete()) {
                try {
                    constructionSite.createBuilding(settlement.getBuildingManager());
                    settlement.getConstructionManager().removeConstructionSite(constructionSite);
                    logger.log(Level.INFO, "New " + constructionSite.getBuildingName() + 
                            " building constructed at " + settlement.getName());
                }
                catch (Exception e) {
                    throw new MissionException("Error constructing new building.", e);
                }
            }
        }
    }
    
    @Override
    public void endMission(String reason) {
        super.endMission(reason);
        
        // Mark site as not undergoing construction.
        if (constructionSite != null) constructionSite.setUndergoingConstruction(false);
        
        // Unreserve all mission construction vehicles.
        unreserveConstructionVehicles();
        
        // Store all LUV attachment parts in settlement.
        Iterator<Part> i = luvAttachmentParts.iterator();
        while (i.hasNext()) {
            Part part = i.next();
            try {
                settlement.getInventory().storeItemResources(part, 1);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error storing attachment part " + part.getName());
                endMission("Construction attachment part " + part.getName() + " could not be stored.");
            }
        }
    }

    @Override
    public Settlement getAssociatedSettlement() {
        return settlement;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(
            boolean useBuffer, boolean parts) throws MissionException {
        
        Map<Resource, Number> resources = new HashMap<Resource, Number>();
        
        if (!constructionSuppliesAdded) {
            // Add construction amount resources.
            resources.putAll(constructionStage.getInfo().getResources());
            
            // Add construction parts.
            resources.putAll(constructionStage.getInfo().getParts());
            
            // Add construction LUV attachment parts.
            Iterator<Part> i = luvAttachmentParts.iterator();
            while (i.hasNext()) {
                Part part = i.next();
                if (resources.containsKey(part)) 
                    resources.put(part, (resources.get(part).intValue() + 1));
                else resources.put(part, 1);
            }
        }

        return resources;
    }

    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(
            boolean useBuffer) throws MissionException {
        
        Map<Class, Integer> equipment = new HashMap<Class, Integer>(1);
        equipment.put(EVASuit.class, getPeopleNumber());
        
        return equipment;
    }
    
    /**
     * Reserves a light utility vehicle for the mission.
     * @return reserved light utility vehicle or null if none.
     */
    private LightUtilityVehicle reserveLightUtilityVehicle() {
        LightUtilityVehicle result = null;
        
        Iterator<Vehicle> i = getAssociatedSettlement().getParkedVehicles().iterator();
        while (i.hasNext() && (result == null)) {
            Vehicle vehicle = i.next();
            
            if (vehicle instanceof LightUtilityVehicle) {
                LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
                if (luvTemp.getStatus().equals(Vehicle.PARKED) && !luvTemp.isReserved() 
                        && (luvTemp.getCrewNum() == 0)) {
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
            while (i.hasNext()) i.next().setReservedForMission(false);
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
     * Checks if the construction materials required for a stage are available at the settlement.
     * @param stage the construction stage information.
     * @param settlement the settlement to construct at.
     * @return true if construction materials are available.
     * @throws Exception if error checking construction materials.
     */
    private static boolean hasStageConstructionMaterials(ConstructionStageInfo stage, Settlement settlement) 
            throws Exception {
        boolean result = true;
        
        Iterator<AmountResource> i = stage.getResources().keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            double amount = stage.getResources().get(resource);
            if (settlement.getInventory().getAmountResourceStored(resource) < amount) result = false;
        }
        
        Iterator<Part> j = stage.getParts().keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            int number = stage.getParts().get(part);
            if (settlement.getInventory().getItemResourceNum(part) < number) result = false;
        }
        
        return result;
    }
    
    /**
     * Checks if the settlement has enough construction materials for any new site.
     * @param skill the construction skill.
     * @param settlement the settlement to check.
     * @return true if enough construction materials.
     * @throws Exception if error checking construction materials.
     */
    private static boolean hasAnyNewSiteConstructionMaterials(int skill, Settlement settlement) 
            throws Exception {
        boolean result = false;
        
        Iterator<ConstructionStageInfo> i = ConstructionUtil.getConstructionStageInfoList(
                ConstructionStageInfo.FOUNDATION, skill).iterator();
        while (i.hasNext()) {
            if (hasStageConstructionMaterials(i.next(), settlement)) result = true;
        }
        
        return result;
    }
    
    /**
     * Checks if settlement has enough construction materials to work on an existing site.
     * @param site the construction site to check.
     * @param skill the construction skill.
     * @return true if enough construction materials.
     * @throws Exception if error checking site.
     */
    private boolean hasExistingSiteConstructionMaterials(ConstructionSite site, int skill) throws Exception {
        boolean result = true;
        
        if (!site.hasUnfinishedStage()) {
            result = false;
            String stageType = site.getNextStageType();
            Iterator<ConstructionStageInfo> i = ConstructionUtil.getConstructionStageInfoList(
                    stageType, skill).iterator();
            while (i.hasNext()) {
                if (hasStageConstructionMaterials(i.next(), settlement)) result = true;
            }
        }
        
        return result;
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
}