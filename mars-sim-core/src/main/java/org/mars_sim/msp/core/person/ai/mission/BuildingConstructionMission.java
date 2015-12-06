/**
 * Mars Simulation Project
 * BuildingConstructionMission.java
 * @version 3.08 2015-07-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Line2D;
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
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
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
 * TODO externalize strings
 */
public class BuildingConstructionMission
extends Mission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingConstructionMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.buildingConstructionMission"); //$NON-NLS-1$

	/** Mission phases. */
	final public static MissionPhase PREPARE_SITE_PHASE = new MissionPhase(Msg.getString(
            "Mission.phase.prepareConstructionSite")); //$NON-NLS-1$
	final public static MissionPhase CONSTRUCTION_PHASE = new MissionPhase(Msg.getString(
            "Mission.phase.construction")); //$NON-NLS-1$

	// Number of mission members.
	public static final int MIN_PEOPLE = 3;
	private static final int MAX_PEOPLE = 10;

	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = 500D;

	// Default distance between buildings for construction.
	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = 5D;
	private static final double DEFAULT_NONINHABITABLE_BUILDING_DISTANCE = 2D;

	// Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 10D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 10D;

    /** Minimum length of a building connector (meters). */
    private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

	// Data members
	private Settlement settlement;
	private ConstructionSite constructionSite;
	private ConstructionStage constructionStage;
	private List<GroundVehicle> constructionVehicles;
	private MarsClock sitePreparationStartTime;
	private List<Part> luvAttachmentParts;

	/**
	 * Constructor.
	 * @param startingMember the mission member starting the mission.
	 */
	public BuildingConstructionMission(MissionMember startingMember) {
        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, startingMember, MIN_PEOPLE);

        if (!isDone()) {
            // Sets the settlement.
            settlement = startingMember.getSettlement();

            // Sets the mission capacity.
            setMissionCapacity(MAX_PEOPLE);
            int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(settlement);
            if (availableSuitNum < getMissionCapacity()) {
                setMissionCapacity(availableSuitNum);
            }

            // Recruit additional members to mission.
            recruitMembersForMission(startingMember);

            // Determine construction site and stage.
            // TODO Refactor.
            int constructionSkill = 0;
            if (startingMember instanceof Person) {
                Person person = (Person) startingMember;
                constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
            }
            else if (startingMember instanceof Robot) {
                Robot robot = (Robot) startingMember;
                constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
            }

            init(constructionSkill);
        }

        // Add phases.
        addPhase(PREPARE_SITE_PHASE);
        addPhase(CONSTRUCTION_PHASE);
        // Set initial mission phase.
        setPhase(PREPARE_SITE_PHASE);

        setPhaseDescription(Msg.getString(
        		"Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
                , settlement.getName()));
    }

//	public BuildingConstructionMission(Robot startingRobot) {
//        // Use Mission constructor.
//        super(DEFAULT_DESCRIPTION, startingRobot, MIN_PEOPLE);
//
//        if (!isDone()) {
//            // Sets the settlement.
//            settlement = startingRobot.getSettlement();
//
//            // Sets the mission capacity.
//            setMissionCapacity(MAX_PEOPLE);
//            int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(settlement);
//            if (availableSuitNum < getMissionCapacity()) {
//                setMissionCapacity(availableSuitNum);
//            }
//
//            // Recruit additional people to mission.
//            recruitRobotsForMission(startingRobot);
//
//            // Determine construction site and stage.
//            int constructionSkill = startingRobot.getBotMind().getSkillManager().
//                    getEffectiveSkillLevel(SkillType.CONSTRUCTION);
//
//            init(constructionSkill);
//        }
//
//        // Add phases.
//        addPhase(PREPARE_SITE_PHASE);
//        addPhase(CONSTRUCTION_PHASE);
//        // Set initial mission phase.
//        setPhase(PREPARE_SITE_PHASE);
//
//        setPhaseDescription(Msg.getString(
//        		"Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
//                , settlement.getName()));
//    }


	public void init(int constructionSkill) {

        ConstructionManager manager = settlement.getConstructionManager();
        ConstructionValues values = manager.getConstructionValues();
        values.clearCache();
        double existingSitesProfit = values.getAllConstructionSitesProfit(constructionSkill);
        double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
        ConstructionStageInfo stageInfo = null;

		if (existingSitesProfit > newSiteProfit) {

		    // Determine which existing construction site to work on.
		    double topSiteProfit = 0D;
		    Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingConstructionMission().iterator();
		    while (i.hasNext()) {
		        ConstructionSite site = i.next();
		        double siteProfit = values.getConstructionSiteProfit(site, constructionSkill);
		        if (siteProfit > topSiteProfit) {
		            constructionSite = site;
		            topSiteProfit = siteProfit;
		        }
		    }
		}
		else if (newSiteProfit > 0D) {

		    // Create new site.
		    constructionSite = manager.createNewConstructionSite();

		    // Determine construction site location and facing.
		    stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
		    if (stageInfo != null) {


		        // Set construction site size.
		        if (stageInfo.getWidth() > 0D) {
		            constructionSite.setWidth(stageInfo.getWidth());
		        }
		        else {
		            // Set initial width value that may be modified later.
		            constructionSite.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);
		        }

		        if (stageInfo.getLength() > 0D) {
		            constructionSite.setLength(stageInfo.getLength());
		        }
		        else {
		            // Set initial length value that may be modified later.
		            constructionSite.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
		        }

		        //if (Simulation.getUseGUI())  {
		        	// if GUI is in use
				//	settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_SITE_EVENT);

		        //} else {
		        	// if not, use the classic way of automatically selecting a constructino location
			        positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
		        //}

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
		        logger.log(Level.INFO, "Continuing work on existing site at " + settlement.getName());
		    }
		    else {
		        if (stageInfo == null) {
		            stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
		        }

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
		    if (constructionStage != null) {
		        constructionSite.setUndergoingConstruction(true);
		    }
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
     */
/* public BuildingConstructionMission(Collection<Person> members, Settlement settlement,
            ConstructionSite site, ConstructionStageInfo stageInfo, double xLoc, double yLoc,
            double facing, List<GroundVehicle> vehicles) {

        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, (Person) members.toArray()[0], 1);

        this.settlement = settlement;

        ConstructionManager manager = settlement.getConstructionManager();
        if (site != null) {
            constructionSite = site;
        }
        else {
            logger.log(Level.INFO, "New construction site added at " + settlement.getName());
            constructionSite = manager.createNewConstructionSite();

            if (constructionSite != null) {

                // Set construction site size.
                if (stageInfo.getWidth() > 0D) {
                    constructionSite.setWidth(stageInfo.getWidth());
                }
                else {
                    // Set initial width value that may be modified later.
                    constructionSite.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);
                }

                if (stageInfo.getLength() > 0D) {
                    constructionSite.setLength(stageInfo.getLength());
                }
                else {
                    // Set initial length value that may be modified later.
                    constructionSite.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
                }

                int bestConstructionSkill = 0;
                Iterator<Person> i = members.iterator();
                while (i.hasNext()) {
                    Person person = i.next();
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(
                            SkillType.CONSTRUCTION);
                    if (constructionSkill > bestConstructionSkill) {
                        bestConstructionSkill = constructionSkill;
                    }
                }

                // Set new construction site location, length and facing.
                // TODO: Replace auto-positioning with set position/facing from parameters
                // when mission creation wizard supports this.
                positionNewConstructionSite(constructionSite, stageInfo, bestConstructionSkill);
            }
            else {
                endMission("Construction site could not be created.");
            }
        }

        if (constructionSite.hasUnfinishedStage()) {
            constructionStage = constructionSite.getCurrentConstructionStage();
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
        if (constructionStage != null) {
            constructionSite.setUndergoingConstruction(true);
        }

        // Add mission members.
        Iterator<Person> i = members.iterator();
        while (i.hasNext()) {
            i.next().getMind().setMission(this);
        }

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
        setPhaseDescription(Msg.getString(
        		"Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
                , settlement.getName()));
    }
    */
    public BuildingConstructionMission(Collection<MissionMember> members, Settlement settlement,
            ConstructionSite site, ConstructionStageInfo stageInfo, double xLoc, double yLoc,
            double facing, List<GroundVehicle> vehicles) {

        // Use Mission constructor.
        super(DEFAULT_DESCRIPTION, (MissionMember) members.toArray()[0], 1);

        this.settlement = settlement;

        ConstructionManager manager = settlement.getConstructionManager();
        if (site != null) {
            constructionSite = site;
        }
        else {
            logger.log(Level.INFO, "New construction site added at " + settlement.getName());
            constructionSite = manager.createNewConstructionSite();

            if (constructionSite != null) {

                // Set construction site size.
                if (stageInfo.getWidth() > 0D) {
                    constructionSite.setWidth(stageInfo.getWidth());
                }
                else {
                    // Set initial width value that may be modified later.
                    constructionSite.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);
                }

                if (stageInfo.getLength() > 0D) {
                    constructionSite.setLength(stageInfo.getLength());
                }
                else {
                    // Set initial length value that may be modified later.
                    constructionSite.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
                }

                int bestConstructionSkill = 0;

                Iterator<MissionMember> i = members.iterator();

                while (i.hasNext()) {
                 	// TODO Refactor
        	        MissionMember member = i.next();
        	        if (member instanceof Person) {
        	        	Person person = (Person) member;
        	        	person.getMind().setMission(this);
        	        }
        	        else if (member instanceof Robot) {
        	        	Robot robot = (Robot) member;
        	        	robot.getBotMind().setMission(this);
        	        }

                    int constructionSkill = 0;
                    // TODO Refactor
                    if (member instanceof Person) {
                        Person person = (Person) member;
                    	constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(
                            SkillType.CONSTRUCTION);
                    }
                    else if (member instanceof Robot) {
                        Robot robot = (Robot) member;
                		constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(
                				SkillType.CONSTRUCTION);
                    }

                    if (constructionSkill > bestConstructionSkill) {
                        bestConstructionSkill = constructionSkill;
                    }
                }

                // Set new construction site location, length and facing.
                // TODO: Replace auto-positioning with set position/facing from parameters
                // when mission creation wizard supports this.
                positionNewConstructionSite(constructionSite, stageInfo, bestConstructionSkill);
            }
            else {
                endMission("Construction site could not be created.");
            }
        }

        if (constructionSite.hasUnfinishedStage()) {
            constructionStage = constructionSite.getCurrentConstructionStage();
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
        if (constructionStage != null) {
            constructionSite.setUndergoingConstruction(true);
        }

        // Add mission members.
        Iterator<MissionMember> i = members.iterator();

        while (i.hasNext()) {

	        MissionMember member = i.next();
	        if (member instanceof Person) {
	        	Person person = (Person) member;
	        	person.getMind().setMission(this);
	        }
	        else if (member instanceof Robot) {
	        	Robot robot = (Robot) member;
	        	robot.getBotMind().setMission(this);
	        }
        }

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
        setPhaseDescription(Msg.getString(
        		"Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
                , settlement.getName()));
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
                    if (luv != null) {
                        constructionVehicles.add(luv);
                    }
                    else {
                        endMission("Light utility vehicle not available.");
                    }
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
    protected boolean isCapableOfMission(MissionMember member) {
        boolean result = super.isCapableOfMission(member);

        if (result) {
            boolean atSettlement = false;
            if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                if (member.getSettlement() == settlement) {
                    atSettlement = true;
                }
            }
            result = atSettlement;
        }

        return result;
    }

    /**
     * Checks if a light utility vehicle (LUV) is available for the mission.
     * @param settlement the settlement to check.
     * @return true if LUV available.
     */
    public static boolean isLUVAvailable(Settlement settlement) {
        boolean result = false;

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();

            if (vehicle instanceof LightUtilityVehicle) {
                boolean usable = true;
                if (vehicle.isReserved()) {
                    usable = false;
                }
                if (!vehicle.getStatus().equals(Vehicle.PARKED)) {
                    usable = false;
                }
                if (((Crewable) vehicle).getCrewNum() > 0 || ((Crewable) vehicle).getRobotCrewNum() > 0) {
                    usable = false;
                }
                if (usable) {
                    result = true;
                }
            }
        }

        return result;
    }

    @Override
    protected void determineNewPhase() {
        if (PREPARE_SITE_PHASE.equals(getPhase())) {
            setPhase(CONSTRUCTION_PHASE);
            setPhaseDescription(Msg.getString(
            		"Mission.phase.construction.description" //$NON-NLS-1$
                    ,constructionStage.getInfo().getName()));
        }
        else if (CONSTRUCTION_PHASE.equals(getPhase())) {
            endMission("Successfully ended construction");
        }
    }

    @Override
    protected void performPhase(MissionMember member) {
        super.performPhase(member);
        if (PREPARE_SITE_PHASE.equals(getPhase())) {
            prepareSitePhase(member);
        }
        else if (CONSTRUCTION_PHASE.equals(getPhase())) {
            constructionPhase(member);
        }
    }
//    @Override
//    protected void performPhase(Robot robot) {
//        super.performPhase(robot);
//        if (PREPARE_SITE_PHASE.equals(getPhase())) {
//            prepareSitePhase(robot);
//        }
//        else if (CONSTRUCTION_PHASE.equals(getPhase())) {
//            constructionPhase(robot);
//        }
//    }
    /**
     * Performs the prepare site phase.
     * @param member the mission member performing the phase.
     */
    private void prepareSitePhase(MissionMember member) {
    	prepareSitePhase();
    }

//    private void prepareSitePhase(Robot robot) {
//    	prepareSitePhase();
//    }

    private void prepareSitePhase() {
        // Load all available materials needed for construction.
        loadAvailableConstructionMaterials();

        // Check if site preparation time has expired.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if (sitePreparationStartTime == null) {
            sitePreparationStartTime = (MarsClock) currentTime.clone();
        }

        if (MarsClock.getTimeDiff(currentTime, sitePreparationStartTime) >= SITE_PREPARE_TIME) {
            setPhaseEnded(true);
        }
    }

    /**
     * Load remaining required construction materials into site that are available at settlement inventory.
     */
    private void loadAvailableConstructionMaterials() {

        Inventory inv = settlement.getInventory();

        // Load amount resources.
        Iterator<AmountResource> i = constructionStage.getRemainingResources().keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            double amountNeeded = constructionStage.getRemainingResources().get(resource);

            inv.addAmountDemandTotalRequest(resource);

            double amountAvailable = inv.getAmountResourceStored(resource,  false);

            // Load as much of the remaining resource as possible into the construction site stage.
            double amountLoading = amountNeeded;
            if (amountAvailable < amountNeeded) {
                amountLoading = amountAvailable;
            }

            if (amountLoading > 0D) {
                inv.retrieveAmountResource(resource, amountLoading);
                constructionStage.addResource(resource, amountLoading);

                inv.addAmountDemand(resource, amountLoading);
            }
        }

        // Load parts.
        Iterator<Part> j = constructionStage.getRemainingParts().keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            int numberNeeded = constructionStage.getRemainingParts().get(part);
            int numberAvailable = inv.getItemResourceNum(part);

            // Load as many remaining parts as possible into the construction site stage.
            int numberLoading = numberNeeded;
            if (numberAvailable < numberNeeded) {
                numberLoading = numberAvailable;
            }

            if (numberLoading > 0) {
                inv.retrieveItemResources(part, numberLoading);
                constructionStage.addParts(part, numberLoading);
            }
        }
    }

    /**
     * Performs the construction phase.
     * @param member the mission member performing the phase.
     */
    private void constructionPhase(MissionMember member) {

        // Anyone in the crew or a single person at the home settlement has a
        // dangerous illness, end phase.
        if (hasEmergency()) {
            setPhaseEnded(true);
        }

        // Load available construction materials into construction site.
        loadAvailableConstructionMaterials();

        // Check if further work can be done on construction stage.
        if (constructionStage.getCompletableWorkTime() <= constructionStage.getCompletedWorkTime()) {
            setPhaseEnded(true);
        }

        if (!getPhaseEnded()) {

            // 75% chance of assigning task, otherwise allow break.
            if (RandomUtil.lessThanRandPercent(75D)) {

                // Assign construction task to member.
                // TODO Refactor.
                if (member instanceof Person) {
                    Person person = (Person) member;
                    if (ConstructBuilding.canConstruct(person, constructionSite)) {
                        assignTask(person, new ConstructBuilding(person, constructionStage,
                                constructionSite, constructionVehicles));
                    }
                }
                else if (member instanceof Robot) {
                    Robot robot = (Robot) member;
                    if (ConstructBuilding.canConstruct(robot, constructionSite)) {
                        assignTask(robot, new ConstructBuilding(robot, constructionStage,
                                constructionSite, constructionVehicles));
                    }
                }
            }
        }

        constructionStageComplete();
    }

//    private void constructionPhase(Robot robot) {
//
//        // Anyone in the crew or a single person at the home settlement has a
//        // dangerous illness, end phase.
//        if (hasEmergency()) {
//            setPhaseEnded(true);
//        }
//
//        // Load available construction materials into construction site.
//        loadAvailableConstructionMaterials();
//
//        // Check if further work can be done on construction stage.
//        if (constructionStage.getCompletableWorkTime() <= constructionStage.getCompletedWorkTime()) {
//            setPhaseEnded(true);
//        }
//
//        if (!getPhaseEnded()) {
//
//            // 75% chance of assigning task, otherwise allow break.
//            if (RandomUtil.lessThanRandPercent(75D)) {
//
//                // Assign construction task to robot.
//                if (ConstructBuilding.canConstruct(robot, constructionSite)) {
//                    assignTask(robot, new ConstructBuilding(robot, constructionStage,
//                            constructionSite, constructionVehicles));
//                }
//            }
//        }
//
//        constructionStageComplete();
//
//    }

    public void constructionStageComplete() {
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
        if (luvAttachmentParts != null) {
            Iterator<Part> i = luvAttachmentParts.iterator();
            while (i.hasNext()) {
                Part part = i.next();
                try {
                    settlement.getInventory().storeItemResources(part, 1);
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error storing attachment part " + part.getName());
                }
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

        // Add construction LUV attachment parts.
        if (luvAttachmentParts != null) {
            Iterator<Part> i = luvAttachmentParts.iterator();
            while (i.hasNext()) {
                Part part = i.next();
                if (resources.containsKey(part)) {
                    resources.put(part, (resources.get(part).intValue() + 1));
                }
                else {
                    resources.put(part, 1);
                }
            }
        }

        return resources;
    }

	@Override
	public Map<Class, Integer> getEquipmentNeededForRemainingMission(
		boolean useBuffer) {
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
                        && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0) ) {
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
                if (sInv.canStoreUnit(vehicle, false)) {
                    sInv.storeUnit(vehicle);
                }
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
     * @param constructionSkill the mission starter's construction skill.
     */
    private void positionNewConstructionSite(ConstructionSite site, ConstructionStageInfo foundationStageInfo,
            int constructionSkill) {

        boolean goodPosition = false;

        // Determine preferred building type from foundation stage info.
        String buildingType = determinePreferredConstructedBuildingType(foundationStageInfo, constructionSkill);
        if (buildingType != null) {
            BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
            site.setWidth(buildingConfig.getWidth(buildingType));
            site.setLength(buildingConfig.getLength(buildingType));
            boolean isBuildingConnector = buildingConfig.hasBuildingConnection(buildingType);
            boolean hasLifeSupport = buildingConfig.hasLifeSupport(buildingType);
            if (isBuildingConnector) {
                // Try to find best location to connect two buildings.
                goodPosition = positionNewBuildingConnectorSite(site, buildingType);
            }
            else if (hasLifeSupport) {
                // Try to put building next to another inhabitable building.
                List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
                Collections.shuffle(inhabitableBuildings);
                Iterator<Building> i = inhabitableBuildings.iterator();
                while (i.hasNext()) {
                    goodPosition = positionNextToBuilding(site, i.next(), DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
                    if (goodPosition) {
                        break;
                    }
                }
            }
            else {
                // Try to put building next to the same building type.
                List<Building> sameBuildings = settlement.getBuildingManager().getBuildingsOfSameType(buildingType);
                Collections.shuffle(sameBuildings);
                Iterator<Building> j = sameBuildings.iterator();
                while (j.hasNext()) {
                    goodPosition = positionNextToBuilding(site, j.next(), DEFAULT_NONINHABITABLE_BUILDING_DISTANCE, false);
                    if (goodPosition) {
                        break;
                    }
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
                        goodPosition = positionNextToBuilding(site, i.next(), (double) x, false);
                        if (goodPosition) {
                            break;
                        }
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
     * Determine the position and length (for variable length sites) for a new building
     * connector construction site.
     * @param site the construction site.
     * @param buildingType the new building type.
     * @return true if position/length of construction site could be found, false if not.
     */
    private boolean positionNewBuildingConnectorSite(ConstructionSite site, String buildingType) {

        boolean result = false;

        BuildingManager manager = settlement.getBuildingManager();
        List<Building> inhabitableBuildings = manager.getBuildings(BuildingFunction.LIFE_SUPPORT);
        Collections.shuffle(inhabitableBuildings);

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        int baseLevel = buildingConfig.getBaseLevel(buildingType);

        // Try to find a connection between an inhabitable building without access to airlock and
        // another inhabitable building with access to an airlock.
        if (settlement.getAirlockNum() > 0) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> i = inhabitableBuildings.iterator();
            while (i.hasNext()) {
                Building startingBuilding = i.next();
                if (!settlement.hasWalkableAvailableAirlock(startingBuilding)) {

                    // Find a different inhabitable building that has walkable access to an airlock.
                    Iterator<Building> k = inhabitableBuildings.iterator();
                    while (k.hasNext()) {
                        Building building = k.next();
                        if (!building.equals(startingBuilding)) {

                            // Check if connector base level matches either building.
                            boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                                    (baseLevel == building.getBaseLevel());

                            if (settlement.hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
                                double distance = Point2D.distance(startingBuilding.getXLocation(),
                                        startingBuilding.getYLocation(), building.getXLocation(),
                                        building.getYLocation());
                                if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                                    // Check that new building can be placed between the two buildings.
                                    if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                            building)) {
                                        leastDistance = distance;
                                        result = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Try to find valid connection location between two inhabitable buildings with no joining walking path.
        if (!result) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> j = inhabitableBuildings.iterator();
            while (j.hasNext()) {
                Building startingBuilding = j.next();

                // Find a different inhabitable building.
                Iterator<Building> k = inhabitableBuildings.iterator();
                while (k.hasNext()) {
                    Building building = k.next();
                    boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(
                            startingBuilding, building);

                    // Check if connector base level matches either building.
                    boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                            (baseLevel == building.getBaseLevel());

                    if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

                        double distance = Point2D.distance(startingBuilding.getXLocation(),
                                startingBuilding.getYLocation(), building.getXLocation(),
                                building.getYLocation());
                        if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                            // Check that new building can be placed between the two buildings.
                            if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                    building)) {
                                leastDistance = distance;
                                result = true;
                            }
                        }
                    }
                }
            }
        }

        // Try to find valid connection location between two inhabitable buildings that are not directly connected.
        if (!result) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> j = inhabitableBuildings.iterator();
            while (j.hasNext()) {
                Building startingBuilding = j.next();

                // Find a different inhabitable building.
                Iterator<Building> k = inhabitableBuildings.iterator();
                while (k.hasNext()) {
                    Building building = k.next();
                    boolean directlyConnected = (settlement.getBuildingConnectorManager().getBuildingConnections(
                            startingBuilding, building).size() > 0);

                    // Check if connector base level matches either building.
                    boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                            (baseLevel == building.getBaseLevel());

                    if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
                        double distance = Point2D.distance(startingBuilding.getXLocation(),
                                startingBuilding.getYLocation(), building.getXLocation(),
                                building.getYLocation());
                        if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                            // Check that new building can be placed between the two buildings.
                            if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                    building)) {
                                leastDistance = distance;
                                result = true;
                            }
                        }
                    }
                }
            }
        }

        // Try to find connection to existing inhabitable building.
        if (!result) {

            // If variable length, set construction site length to default.
            if (buildingConfig.getLength(buildingType) == -1D) {
                site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
            }

            Iterator<Building> l = inhabitableBuildings.iterator();
            while (l.hasNext()) {
                Building building = l.next();
                // Make connector building face away from building.
                result = positionNextToBuilding(site, building, 0D, true);

                if (result) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Determine the position and length (for variable length) for a connector building between two existing
     * buildings.
     * @param buildingType the new connector building type.
     * @param site the construction site.
     * @param firstBuilding the first of the two existing buildings.
     * @param secondBuilding the second of the two existing buildings.
     * @return true if position/length of construction site could be found, false if not.
     */
    private boolean positionConnectorBetweenTwoBuildings(String buildingType, ConstructionSite site,
            Building firstBuilding, Building secondBuilding) {

        boolean result = false;

        // Determine valid placement lines for connector building.
        List<Line2D> validLines = new ArrayList<Line2D>();

        // Check each building side for the two buildings for a valid line unblocked by obstacles.
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(buildingType);
        List<Point2D> firstBuildingPositions = getFourPositionsSurroundingBuilding(firstBuilding, .1D);
        List<Point2D> secondBuildingPositions = getFourPositionsSurroundingBuilding(secondBuilding, .1D);
        for (int x = 0; x < firstBuildingPositions.size(); x++) {
            for (int y = 0; y < secondBuildingPositions.size(); y++) {

                Point2D firstBuildingPos = firstBuildingPositions.get(x);
                Point2D secondBuildingPos = secondBuildingPositions.get(y);

                double distance = Point2D.distance(firstBuildingPos.getX(), firstBuildingPos.getY(),
                        secondBuildingPos.getX(), secondBuildingPos.getY());

                if (distance >= MINIMUM_CONNECTOR_LENGTH) {
                    // Check line rect between positions for obstacle collision.
                    Line2D line = new Line2D.Double(firstBuildingPos.getX(), firstBuildingPos.getY(),
                            secondBuildingPos.getX(), secondBuildingPos.getY());
                    boolean clearPath = LocalAreaUtil.checkLinePathCollision(line, settlement.getCoordinates(), false);
                    if (clearPath) {
                        validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
                    }
                }
            }
        }

        if (validLines.size() > 0) {

            // Find shortest valid line.
            double shortestLineLength = Double.MAX_VALUE;
            Line2D shortestLine = null;
            Iterator<Line2D> i = validLines.iterator();
            while (i.hasNext()) {
                Line2D line = i.next();
                double length = Point2D.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
                if (length < shortestLineLength) {
                    shortestLine = line;
                    shortestLineLength = length;
                }
            }

            // Create building template with position, facing, width and length for the connector building.
            double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
            Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
            Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding, width);
            double centerX = (p1.getX() + p2.getX()) / 2D;
            double centerY = (p1.getY() + p2.getY()) / 2D;
            double newLength = p1.distance(p2);
            double facingDegrees = LocalAreaUtil.getDirection(p1, p2);

            site.setXLocation(centerX);
            site.setYLocation(centerY);
            site.setFacing(facingDegrees);
            site.setLength(newLength);
            result = true;
        }

        return result;
    }

    /**
     * Adjust the connector end point based on relative angle of the connection.
     * @param point the initial connector location.
     * @param lineFacing the facing of the connector line (degrees).
     * @param building the existing building being connected to.
     * @param connectorWidth the width of the new connector.
     * @return point adjusted location for connector end point.
     */
    private Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building, double connectorWidth) {

        double lineFacingRad = Math.toRadians(lineFacing);
        double angleFromBuildingCenterDegrees = LocalAreaUtil.getDirection(new Point2D.Double(building.getXLocation(),
                building.getYLocation()), point);
        double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
        double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
        double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

        double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
        double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

        return new Point2D.Double(newXLoc, newYLoc);
    }

    /**
     * Gets four positions surrounding a building with a given distance from its edge.
     * @param building the building.
     * @param distanceFromSide distance (distance) for positions from the edge of the building.
     * @return list of four positions.
     */
    private List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

        List<Point2D> result = new ArrayList<Point2D>(4);

        final int front = 0;
        final int back = 1;
        final int right = 2;
        final int left = 3;

        for (int x = 0; x < 4; x++) {
            double xPos = 0D;
            double yPos = 0D;

            switch(x) {
                case front: xPos = 0D;
                             yPos = (building.getLength() / 2D) + distanceFromSide;
                             break;
                case back:  xPos = 0D;
                             yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
                             break;
                case right: xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
                             yPos = 0D;
                             break;
                case left:  xPos = (building.getWidth() / 2D) + distanceFromSide;
                             yPos = 0D;
                             break;
            }

            Point2D position = LocalAreaUtil.getLocalRelativeLocation(xPos, yPos, building);
            result.add(position);
        }

        return result;
    }

    /**
     * Determines the preferred construction building type for a given foundation.
     * @param foundationStageInfo the foundation stage info.
     * @param constructionSkill the mission starter's construction skill.
     * @return preferred building type or null if none found.
     */
    private String determinePreferredConstructedBuildingType(ConstructionStageInfo foundationStageInfo,
            int constructionSkill) {

        String result = null;

        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        List<String> constructableBuildings = ConstructionUtil.getConstructableBuildingNames(foundationStageInfo);
        Iterator<String> i = constructableBuildings.iterator();
        double maxBuildingValue = Double.NEGATIVE_INFINITY;
        while (i.hasNext()) {
            String buildingType = i.next();
            double buildingValue = values.getConstructionStageValue(foundationStageInfo, constructionSkill);
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
     * @param faceAway true if new building should face away from other building.
     * @return true if construction site could be positioned, false if not.
     */
    private boolean positionNextToBuilding(ConstructionSite site, Building building,
            double separationDistance, boolean faceAway) {

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
        double rectRotation = building.getFacing();

        for (int x = 0; x < directions.size(); x++) {
            switch (directions.get(x)) {
                case front: direction = building.getFacing();
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            break;
                case back: direction = building.getFacing() + 180D;
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            if (faceAway) {
                                rectRotation = building.getFacing() + 180D;
                            }
                            break;
                case right:  direction = building.getFacing() + 90D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
                            if (faceAway) {
                                structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
                                rectRotation = building.getFacing() + 90D;
                            }
                            break;
                case left:  direction = building.getFacing() + 270D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
                            if (faceAway) {
                                structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
                                rectRotation = building.getFacing() + 270D;
                            }
            }

            if (rectRotation > 360D) {
                rectRotation -= 360D;
            }

            double distance = structureDistance + separationDistance;
            double radianDirection = Math.PI * direction / 180D;
            double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
            double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));

            // Check to see if proposed new site position intersects with any existing buildings
            // or construction sites.
            if (settlement.getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX,
                    rectCenterY, site.getWidth(), site.getLength(), rectRotation, site)) {
                // Set the new site here.
                site.setXLocation(rectCenterX);
                site.setYLocation(rectCenterY);
                site.setFacing(rectRotation);
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
        if (constructionVehicles != null) {
            constructionVehicles.clear();
        }
        constructionVehicles = null;
        sitePreparationStartTime = null;
        if (luvAttachmentParts != null) {
            luvAttachmentParts.clear();
        }
        luvAttachmentParts = null;
    }
}