/**
 * Mars Simulation Project
 * BuildingConstructionMission.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
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
public class BuildingConstructionMission
extends Mission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingConstructionMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = "Construct Building";

	// TODO Mission phases should be enums
	final public static String PREPARE_SITE_PHASE = "Prepare Site";
	final public static String CONSTRUCTION_PHASE = "Construction";

	// Number of mission members.
	private static final int MIN_PEOPLE = 3;
	private static final int MAX_PEOPLE = 10;

	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = 500D;

	// Data members
	private Settlement settlement;
	private ConstructionSite constructionSite;
	private ConstructionStage constructionStage;
	private List<GroundVehicle> constructionVehicles;
	private MarsClock sitePreparationStartTime;
	private boolean finishingExistingStage;
	private boolean constructionSuppliesLoaded;
	private List<Part> luvAttachmentParts;

	/**
	 * Constructor.
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public BuildingConstructionMission(Person startingPerson) {
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
            
            // Determine construction site and stage.
            int constructionSkill = startingPerson.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
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
                    
                // Determine construction site location and facing.
                ConstructionStageInfo stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
                if (stageInfo != null) {
                    constructionSite.setWidth(stageInfo.getWidth());
                    constructionSite.setLength(stageInfo.getLength());
                    positionNewConstructionSite(constructionSite, stageInfo);
                       
                    logger.log(Level.INFO, "New construction site added at " + settlement.getName());
                }
                else {
                    endMission("New construction stage could not be determined.");
                }
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
            
            if (!isDone()) {
                // Reserve construction vehicles.
                reserveConstructionVehicles();
            }
             
            if (!isDone()) {
                // Retrieve construction LUV attachment parts.
                retrieveConstructionLUVParts();
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
     * @param xLoc the X location of the new construction site (ignored if existing site).
     * @param yLoc the Y location of the new construction site (ignored if existing site).
     * @param facing the facing of the new construction site (ignored if existing site).
     * @param vehicles the construction vehicles.
     * @throws MissionException if error creating mission.
     */
    public BuildingConstructionMission(Collection<Person> members, Settlement settlement, 
            ConstructionSite site, ConstructionStageInfo stageInfo, double xLoc, double yLoc,
            double facing, List<GroundVehicle> vehicles) {
        
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, (Person) members.toArray()[0], 1);
        
        this.settlement = settlement;
        
        ConstructionManager manager = settlement.getConstructionManager();
        if (site != null) constructionSite = site;
        else {
            logger.log(Level.INFO, "New construction site added at " + settlement.getName());
            constructionSite = manager.createNewConstructionSite();
            
            if (constructionSite != null) {
                
                // Set construction site size.
                if (stageInfo.getWidth() > 0D) {
                    constructionSite.setWidth(stageInfo.getWidth());
                }
                else {
                    // TODO: Change this to properly determine variable width.
                    constructionSite.setWidth(10D);
                }
                
                if (stageInfo.getLength() > 0D) {
                    constructionSite.setLength(stageInfo.getLength());
                }
                else {
                    // TODO: Change this to properly determine variable length.
                    constructionSite.setLength(10D);
                }
                
                // Set new construction site location and facing.
                // TODO: Replace auto-positioning with set position/facing from parameters
                // when mission creation wizard supports this.
                positionNewConstructionSite(constructionSite, stageInfo);
                /*
                constructionSite.setXLocation(xLoc);
                constructionSite.setYLocation(yLoc);
                constructionSite.setFacing(facing);
                */
            }
            else {
                endMission("Construction site could not be created.");
            }
        }
        
        if (constructionSite.hasUnfinishedStage()) {
            constructionStage = constructionSite.getCurrentConstructionStage();
            finishingExistingStage = true;
            logger.log(Level.INFO, "Using existing construction stage: " + constructionStage);
        }
        else {
            constructionStage = new ConstructionStage(stageInfo, constructionSite);
            logger.log(Level.INFO, "Starting new construction stage: " + constructionStage);
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
        while (j.hasNext()) {
            GroundVehicle vehicle = j.next();
            vehicle.setReservedForMission(true);
            if (settlement.getInventory().containsUnit(vehicle)) {
                settlement.getInventory().retrieveUnit(vehicle);
            }
            else {
                logger.severe("Unable to retrieve " + vehicle.getName() + 
                        " cannot be retrieved from " + settlement.getName() + 
                        " inventory.");
                endMission("Construction vehicle " + vehicle.getName() + 
                        " could not be retrieved from settlement inventory.");
            }
        }
        
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
        
        // Check if person is in a settlement.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
        
            // Check if available light utility vehicles.
            boolean reservableLUV = isLUVAvailable(settlement);
            
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
            
            // Check if settlement has construction override flag set.
            boolean constructionOverride = settlement.getConstructionOverride();
            
            if (reservableLUV && enoughPeople && !constructionOverride) {
                
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
                    if (hasAnyNewSiteConstructionMaterials(constructionSkill, settlement)) {
                        ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
                        double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
                        result = constructionProfit;
                        if (result > 100D) {
                            result = 100D;
                        }
                    }
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting construction site.", e);
                }
            }       
            
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) {
                result = 0D;
            }
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(BuildingConstructionMission.class);
            }
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
            int vehicleIndex = 0;
            Iterator<ConstructionVehicleType> k = constructionStage.getInfo().getVehicles().iterator();
            while (k.hasNext()) {
                Vehicle vehicle = null;
                if (constructionVehicles.size() > vehicleIndex) {
                    vehicle = constructionVehicles.get(vehicleIndex);
                }
                
                Iterator<Part> l = k.next().getAttachmentParts().iterator();
                while (l.hasNext()) {
                    Part part = l.next();
                    try {
                        settlement.getInventory().retrieveItemResources(part, 1);
                        if (vehicle != null) {
                            vehicle.getInventory().storeItemResources(part, 1);
                        }
                        luvAttachmentParts.add(part);
                    }
                    catch (Exception e) {
                        logger.log(Level.SEVERE, "Error retrieving attachment part " + part.getName());
                        endMission("Construction attachment part " + part.getName() + " could not be retrieved.");
                    }
                }
                vehicleIndex++;
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
    private ConstructionStageInfo determineNewStageInfo(ConstructionSite site, int skill) {
        ConstructionStageInfo result = null;
        
        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        Map<ConstructionStageInfo, Double> stageProfits = 
            values.getNewConstructionStageProfits(site, skill);
        if (!stageProfits.isEmpty()) {
            result = RandomUtil.getWeightedRandomObject(stageProfits);
        }
        
        return result;
    }
    
    @Override
    protected boolean isCapableOfMission(Person person) {
        if (super.isCapableOfMission(person)) {
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
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
    
    @Override
    protected void determineNewPhase() {
        if (PREPARE_SITE_PHASE.equals(getPhase())) {
            setPhase(CONSTRUCTION_PHASE);
            setPhaseDescription("Constructing Site Stage: " + constructionStage.getInfo().getName());
        }
        else if (CONSTRUCTION_PHASE.equals(getPhase())) endMission("Successfully ended construction");
    }

    @Override
    protected void performPhase(Person person) {
        super.performPhase(person);
        if (PREPARE_SITE_PHASE.equals(getPhase())) prepareSitePhase(person);
        else if (CONSTRUCTION_PHASE.equals(getPhase())) constructionPhase(person);
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
        else if (!constructionSuppliesLoaded){
            // Load all resources needed for construction.
            Inventory inv = settlement.getInventory();
            
            // Load amount resources.
            Iterator<AmountResource> i = constructionStage.getInfo().getResources().keySet().iterator();
            while (i.hasNext()) {
                AmountResource resource = i.next();
                double amount = constructionStage.getInfo().getResources().get(resource);
                if (inv.getAmountResourceStored(resource, false) >= amount)
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
    private void constructionPhase(Person person) {

        // Anyone in the crew or a single person at the home settlement has a 
        // dangerous illness, end phase.
        if (hasEmergency()) setPhaseEnded(true);
        
        if (!getPhaseEnded()) {
            
            // 75% chance of assigning task, otherwise allow break.
            if (RandomUtil.lessThanRandPercent(75D)) {

                // Assign construction task to person.
                if (ConstructBuilding.canConstruct(person, constructionSite)) {
                    assignTask(person, new ConstructBuilding(person, constructionStage, 
                            constructionSite, constructionVehicles));
                }
            }
        }
        
        if (constructionStage.isComplete()) {
            setPhaseEnded(true);
            settlement.getConstructionManager().getConstructionValues().clearCache();
            
            // Construct building if all site construction complete.
            if (constructionSite.isAllConstructionComplete()) {

                Building building = constructionSite.createBuilding(settlement.getBuildingManager());
                settlement.getConstructionManager().removeConstructionSite(constructionSite);
                settlement.fireUnitUpdate(UnitEventType.FINISH_BUILDING_EVENT, building);
                logger.log(Level.INFO, "New " + constructionSite.getBuildingName() + 
                        " building constructed at " + settlement.getName());
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
            boolean useBuffer) {
        
        Map<Resource, Number> resources = new HashMap<Resource, Number>();
        
        if (!constructionSuppliesLoaded) {
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
		boolean useBuffer
	) {
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
        
        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext() && (result == null)) {
            Vehicle vehicle = i.next();
            
            if (vehicle instanceof LightUtilityVehicle) {
                LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
                if (luvTemp.getStatus().equals(Vehicle.PARKED) && !luvTemp.isReserved() 
                        && (luvTemp.getCrewNum() == 0)) {
                    result = luvTemp;
                    luvTemp.setReservedForMission(true);
                    
                    // Place light utility vehicles at random location in construction site.
                    Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(constructionSite);
                    Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(), 
                            relativeLocSite.getY(), constructionSite);
                    luvTemp.setParkedLocation(settlementLocSite.getX(), settlementLocSite.getY(), 
                            RandomUtil.getRandomDouble(360D));
                    
                    if (settlement.getInventory().containsUnit(luvTemp)) {
                        settlement.getInventory().retrieveUnit(luvTemp);
                    }
                    else {
                        logger.severe("Unable to retrieve " + luvTemp.getName() + 
                                " cannot be retrieved from " + settlement.getName() + 
                                " inventory.");
                        endMission("Construction vehicle " + luvTemp.getName() + 
                                " could not be retrieved from settlement inventory.");
                    }
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
            while (i.hasNext()) {
                GroundVehicle vehicle = i.next();
                vehicle.setReservedForMission(false);

                Inventory vInv = vehicle.getInventory();
                Inventory sInv = settlement.getInventory();
                
                // Store construction vehicle in settlement.
                sInv.storeUnit(vehicle);
                vehicle.determinedSettlementParkedLocationAndFacing();
                
                // Store all construction vehicle attachments in settlement.
                Iterator<ItemResource> j = vInv.getAllItemResourcesStored().iterator();
                while (j.hasNext()) {
                    ItemResource attachmentPart = j.next();
                    int num = vInv.getItemResourceNum(attachmentPart);
                    vInv.retrieveItemResources(attachmentPart, num);
                    sInv.storeItemResources(attachmentPart, num);
                }
            }
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
    private static boolean hasStageConstructionMaterials(ConstructionStageInfo stage, 
            Settlement settlement) {
        boolean result = true;
        
        Iterator<AmountResource> i = stage.getResources().keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            double amount = stage.getResources().get(resource);
            if (settlement.getInventory().getAmountResourceStored(resource, false) < amount) {
                result = false;
            }
        }
        
        Iterator<Part> j = stage.getParts().keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            int number = stage.getParts().get(part);
            if (settlement.getInventory().getItemResourceNum(part) < number) {
                result = false;
            }
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
    private static boolean hasAnyNewSiteConstructionMaterials(int skill, Settlement settlement) {
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
    private boolean hasExistingSiteConstructionMaterials(ConstructionSite site, int skill) {
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
    
    /**
     * Determines and sets the position of a new construction site.
     * @param site the new construction site.
     * @param foundationStageInfo the site's foundation stage info.
     */
    private void positionNewConstructionSite(ConstructionSite site, ConstructionStageInfo foundationStageInfo) {
        
        boolean goodPosition = false;
        
        // Determine preferred building type from foundation stage info.
        String buildingType = determinePreferredConstructedBuildingType(foundationStageInfo);
        if (buildingType != null) {
            
            boolean hasLifeSupport = SimulationConfig.instance().getBuildingConfiguration().hasLifeSupport(buildingType);
            if (hasLifeSupport) {
                // Try to put building next to another inhabitable building.
                List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(LifeSupport.NAME);
                Collections.shuffle(inhabitableBuildings);
                Iterator<Building> i = inhabitableBuildings.iterator();
                while (i.hasNext()) {
                    goodPosition = positionNextToBuilding(site, i.next(), 0D);
                    if (goodPosition) break;
                }
            }
            else {
                // Try to put building next to the same building type.
                List<Building> sameBuildings = settlement.getBuildingManager().getBuildingsOfName(buildingType);
                Collections.shuffle(sameBuildings);
                Iterator<Building> j = sameBuildings.iterator();
                while (j.hasNext()) {
                    goodPosition = positionNextToBuilding(site, j.next(), 0D);
                    if (goodPosition) break;
                }
            }
        }
        
        if (!goodPosition) {
            // Try to put building next to another building.
            // If not successful, try again 10m from each building and continue out at 10m increments 
            // until a location is found.
            BuildingManager buildingManager = settlement.getBuildingManager();
            if (buildingManager.getBuildingNum() > 0) {
                for (int x = 10; !goodPosition; x+= 10) {
                    List<Building> allBuildings = buildingManager.getBuildings();
                    Collections.shuffle(allBuildings);
                    Iterator<Building> i = allBuildings.iterator();
                    while (i.hasNext()) {
                        goodPosition = positionNextToBuilding(site, i.next(), (double) x);
                        if (goodPosition) break;
                    }
                }
            }
            else {
                // If no buildings at settlement, position new construction site at 0,0 with random facing.
                site.setXLocation(0D);
                site.setYLocation(0D);
                site.setFacing(RandomUtil.getRandomDouble(360D));
            }
        }
    }
    
    /**
     * Determines the preferred construction building type for a given foundation.
     * @param foundationStageInfo the foundation stage info.
     * @return preferred building type or null if none found.
     */
    private String determinePreferredConstructedBuildingType(ConstructionStageInfo foundationStageInfo) {
        
        String result = null;
        
        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        List<String> constructableBuildings = ConstructionUtil.getConstructableBuildingNames(foundationStageInfo);
        Iterator<String> i = constructableBuildings.iterator();
        double maxBuildingValue = Double.NEGATIVE_INFINITY;
        while (i.hasNext()) {
            String buildingType = i.next();
            double buildingValue = values.getConstructionStageValue(foundationStageInfo);
            if (buildingValue > maxBuildingValue) {
                maxBuildingValue = buildingValue;
                result = buildingType;
            }
        }
        
        return result;
    }
    
    /**
     * Positions a new construction site near an existing building.
     * @param site the new construction site.
     * @param building the existing building.
     * @param separationDistance the separation distance (meters) from the building.
     * @return true if construction site could be positioned, false if not.
     */
    private boolean positionNextToBuilding(ConstructionSite site, Building building, double separationDistance) {
        boolean goodPosition = false;
        
        final int front = 0;
        final int back = 1;
        final int right = 2;
        final int left = 3;
        
        List<Integer> directions = new ArrayList<Integer>(4);
        directions.add(front);
        directions.add(back);
        directions.add(right);
        directions.add(left);
        Collections.shuffle(directions);
        
        double direction = 0D;
        double structureDistance = 0D;
        
        for (int x = 0; x < directions.size(); x++) {
            switch (directions.get(x)) {
                case front: direction = building.getFacing();
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            break;
                case back: direction = building.getFacing() + 180D;
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            break;
                case right:  direction = building.getFacing() + 90D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
                            break;
                case left:  direction = building.getFacing() + 270D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
            }
            
            double distance = structureDistance + separationDistance;
            double radianDirection = Math.PI * direction / 180D;
            double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
            double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));
            double rectRotation = building.getFacing();
            
            // Check to see if proposed new site position intersects with any existing buildings 
            // or construction sites.
            if (settlement.getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX, 
                    rectCenterY, site.getWidth(), site.getLength(), rectRotation, site)) {
                // Set the new site here.
                site.setXLocation(rectCenterX);
                site.setYLocation(rectCenterY);
                site.setFacing(building.getFacing());
                goodPosition = true;
                break;
            }
        }
        
        return goodPosition;
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