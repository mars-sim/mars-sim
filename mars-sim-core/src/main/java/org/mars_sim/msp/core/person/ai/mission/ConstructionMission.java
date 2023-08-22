/*
 * Mars Simulation Project
 * ConstructionMission.java
 * @date 2023-06-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars.sim.mapdata.location.BoundedObject;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.mission.Construction;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.BuildingSpec;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Mission for construction a stage for a settlement building. TODO externalize
 * strings
 */
public class ConstructionMission extends AbstractMission
	implements Construction {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ConstructionMission.class.getName());

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.CONSTRUCTION;

	/** Mission phases. */
	private static final MissionPhase SELECT_SITE_PHASE = new MissionPhase("Mission.phase.selectConstructionSite");
	private static final MissionPhase PREPARE_SITE_PHASE = new MissionPhase("Mission.phase.prepareConstructionSite");
	private static final MissionPhase CONSTRUCTION_PHASE = new MissionPhase("Mission.phase.construction");

	private static final MissionStatus LUV_NOT_AVAILABLE = new MissionStatus("Mission.status.noLUV");
	private static final MissionStatus CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED =new MissionStatus("Mission.status.noConstructionSite");
	private static final MissionStatus CONSTRUCTION_ENDED = new MissionStatus("Mission.status.constructionEnded");
	private static final MissionStatus NEW_CONSTRUCTION_STAGE_NOT_DETERMINED = new MissionStatus("Mission.status.noConstructionStage");

	// Number of mission members.
	public static final int MIN_PEOPLE = 2;
	
	private static final int MAX_PEOPLE = 10;

	public final static int FIRST_AVAILABLE_SOL = 2;

	private static final int DIG_REGOLITH_PERCENT_PROBABILITY = 10;
	
	private static final int CONSTRUCT_PERCENT_PROBABILITY = 50;
	
	private static final double SMALL_AMOUNT = 0.001D;
	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = 250D;
	// Default distance between buildings for construction.
	private static final double DEFAULT_HABITABLE_BUILDING_DISTANCE = 5D;

	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = 2D;

	private static final double DEFAULT_FARMING_DISTANCE = 5D;

	// Default width and length for variable size buildings if not otherwise
	// determined.
	private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 7D;

	private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 9D;

	/** Minimum length of a building connector (meters). */
	private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

	// Data members	
	private Settlement settlement;
	private ConstructionSite site;
	private ConstructionStage stage;

	private ConstructionManager manager;
	
	private List<GroundVehicle> constructionVehicles;
	private List<Integer> luvAttachmentParts;

	private static BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	
	/**
	 * Constructor 1 for Case 1: Determined by the need of the settlement.
	 *
	 * @param startingMember the mission member starting the mission.
	 */
	public ConstructionMission(Worker startingMember) {
		// Use Mission constructor.
		super(missionType, startingMember);

		if (!isDone()) {
			// Sets the settlement.
			settlement = startingMember.getSettlement();

			// Sets the mission capacity.
			setMissionCapacity(MAX_PEOPLE);

			// Recruit additional members to mission.
			recruitMembersForMission(startingMember, true, MIN_PEOPLE);

			// Determine construction site and stage.
			// TODO Refactor.
			int constructionSkill = 1;
			if (startingMember.getUnitType() == UnitType.PERSON) {
				Person person = (Person) startingMember;
				// logger.info("The starting member is " + person);
				// person.setMission(this);
				constructionSkill += person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
				person.getMind().setMission(this);
			}

			determineSiteByProfit(constructionSkill);
		}

		if (!isDone()) {
			// Set initial mission phase.
			if (site == null) {
				endMission(CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
				return;
			}

			else {
				// Reserve construction vehicles.
				reserveConstructionVehicles();
				// Retrieve construction LUV attachment parts.
				retrieveConstructionLUVParts();
				
				setPhase(PREPARE_SITE_PHASE, settlement.getName());
			}
		}
		
		// Need to set the description of this mission correctly
		// e.g. Pouring the foundation, Building the frame, or Constructing the building

		// Create mission designation
		createFullDesignation();
		// Call missionManager to add this mission
	    missionManager.addMission(this);
	}

	/**
	 * Determines the construction site based upon profit.
	 * 
	 * @param skill
	 */
	public void determineSiteByProfit(int skill) {
		// Note: a settler starts this mission
		logger.info(settlement, "Determining sites by profits.");

		if (manager == null)
			manager = settlement.getConstructionManager();
				
		ConstructionValues values = manager.getConstructionValues();
		values.clearCache();
		double existingSitesProfit = values.getAllConstructionSitesProfit(skill);
		double newSiteProfit = values.getNewConstructionSiteProfit(skill);
		ConstructionStageInfo info = null;

		logger.info(settlement, "existingSitesProfit: " + existingSitesProfit + "   newSiteProfit: " + newSiteProfit);
		
		if (existingSitesProfit > newSiteProfit) {
			// If there are existing construction sites
			logger.info(settlement, "Developing an existing construction site.");
			
			// Determine which existing construction site to work on.
			double topSiteProfit = 0D;
			Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingConstructionMission().iterator();
			while (i.hasNext()) {
				ConstructionSite _site = i.next();
				double siteProfit = values.getConstructionSiteProfit(_site, skill);
				if (siteProfit > topSiteProfit) {
					this.site = _site;
					info = _site.getStageInfo();
					topSiteProfit = siteProfit;
				}
			}
			
			site.setStageInfo(info);
			
			determineNewStage(site, info, skill, values);
		}

		else if (newSiteProfit >= 0D) {
			// If there aren't any existing construction sites
			logger.info(settlement, "Creating a new construction site.");
			
			// Case 1a: if using GUI			
			// Case 1b: if not using GUI

			// Create new site.
			site = manager.createNewConstructionSite();

			if (site == null)
				logger.info(settlement, "site is null.");
			
			// Determine construction site new stage info via profits probability.
			info = determineNewStageInfoByProfits(site, skill);

			site.setStageInfo(info);
			
			// Determine construction site location and facing.
			if (info != null) {
				// Set construction site size.
				if (info.getWidth() > 0D)
					site.setWidth(info.getWidth());
				else
					// Set initial width value that may be modified later.
					site.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);

				if (info.getLength() > 0D)
					site.setLength(info.getLength());
				else
					// Set initial length value that may be modified later.
					site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);

				determineNewStage(site, info, skill, values);

				positionNewSite(site);

				logger.info(settlement, "New construction site '" + site + "' added.");
			}
			
			else {
				logger.warning(settlement, "New construction stage could not be determined.");
				endMission(NEW_CONSTRUCTION_STAGE_NOT_DETERMINED);
				return;
			}
		}
		else {
			logger.info(settlement, "Case 3");
		}
	}

	/**
	 * Determines a new stage to work on.
	 * 
	 * @param cSite
	 * @param stageInfo
	 * @param constructionSkill
	 * @param values
	 */
	public void determineNewStage(ConstructionSite cSite, ConstructionStageInfo stageInfo, int constructionSkill,
			ConstructionValues values) {
		this.site = cSite;
		logger.info(settlement, "Determining a new stage to work on for " + cSite + ".");

		if (cSite != null) {

			// Determine new stage to work on.
			if (cSite.hasUnfinishedStage()) {
				stage = site.getCurrentConstructionStage();
				logger.info(settlement, "Continuing work on existing site at " + settlement.getName());
			}
			
			else {
				logger.info(settlement, "Found no unfinished stages at " + cSite + ".");
				
				if (stageInfo == null) {
					stageInfo = determineNewStageInfoByProfits(site, constructionSkill);
				}

				if (stageInfo != null) {
					stage = new ConstructionStage(stageInfo, cSite);
					cSite.addNewStage(stage);
					values.clearCache();
					logger.info(settlement, "Starting a new construction stage '" + stage + "' for " + cSite + ".");
				} 
				
				else {
					endMission(NEW_CONSTRUCTION_STAGE_NOT_DETERMINED);
				}
			}

			// Mark site as undergoing construction.
			if (stage != null) {
				cSite.setUndergoingConstruction(true);
			}
		}
		else {
			endMission(CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
		}
	}

	/**
	 * Constructor 2: Player manually creates this mission.
	 *
	 * @param members
	 * @param settlement
	 * @param no_site
	 * @param stageInfo
	 * @param xLoc
	 * @param yLoc
	 * @param facing
	 * @param vehicles
	 */
	public ConstructionMission(Collection<Worker> members, Settlement settlement,
			ConstructionSite no_site, ConstructionStageInfo stageInfo, 
			double xLoc, double yLoc, double facing,
			List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		// TODO: Need to pick the best one, not the first one
		super(missionType, (Worker) members.toArray()[0]);

		this.settlement = settlement;
		this.constructionVehicles = vehicles;

		// Add mission members.
		addMembers(members, false);
		
		setMissionCapacity(MAX_PEOPLE);

		if (site != null) {
			// site already selected
			
			// Case 2a: if using GUI	
			// Case 2b: if not using GUI
				
			site.setStageInfo(stageInfo);

			if (stageInfo != null) {
				logger.info(settlement, "Case 2. stageInfo is " + stageInfo.getName());
			} else {
				logger.info(settlement, "Case 2. new construction stageInfo could not be determined.");
			}
	
			setupConstructionStage(site, stageInfo);

			if (!isDone()) {
				// Reserve construction vehicles.
				reserveConstructionVehicles();
				// Retrieve construction LUV attachment parts.
				retrieveConstructionLUVParts();
				// Set initial mission phase.
				setPhase(PREPARE_SITE_PHASE, settlement.getName());
			}
		}

		else {
			// site has NOT been selected
			logger.info(settlement, "Case 3 : site has NOT been picked yet and the construction is manually started by users");

			// Case 3a: if using GUI to pick a site
			// Case 3b: if GUI is NOT in use

			if (manager == null)
				manager = settlement.getConstructionManager();

			site = manager.createNewConstructionSite();

			if (site != null) {

				site.setStageInfo(stageInfo);
				// Set construction site size.

				if (stageInfo.getWidth() > 0D)
					site.setWidth(stageInfo.getWidth());
				else
					// Set initial width value that may be modified later.
					site.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);

				if (stageInfo.getLength() > 0D)
					site.setLength(stageInfo.getLength());
				else
					// Set initial length value that may be modified later.
					site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);

				positionNewSite(site);
			}

			else {
				endMission(CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
				return;
			}

			setupConstructionStage(site, stageInfo);

			if (!isDone()) {
				// Reserve construction vehicles.
				reserveConstructionVehicles();
				// Retrieve construction LUV attachment parts.
				retrieveConstructionLUVParts();
				// Set initial mission phase.
				setPhase(PREPARE_SITE_PHASE, settlement.getName());
			}
		}
	}
	
	/**
	 * Sets up the construction stage.
	 *
	 * @param modSite
	 * @param info
	 */
	public void setupConstructionStage(ConstructionSite modSite, ConstructionStageInfo info) {
		this.site = modSite;
		logger.info(settlement, modSite, 5_000, "Stage Info: " + info.toString());

		if (site.hasUnfinishedStage()) {
			stage = site.getCurrentConstructionStage();
			logger.info(settlement, modSite, 5_000, "Still in the stage '" + stage + "'.");
		}
		else {
			stage = new ConstructionStage(info, site);
			logger.info(settlement, modSite, 5_000, "Starting a new construction stage for '" + stage + "'.");
			site.addNewStage(stage);
		}

		// Mark site as undergoing construction.
		if (stage != null) {
			site.setUndergoingConstruction(true);
		}
	}

	/**
	 * Reserves construction vehicles for the mission.
	 */
	public void reserveConstructionVehicles() {
		if (stage != null) {
			// Construct a new list of construction vehicles
			constructionVehicles = new ArrayList<>();
			Iterator<ConstructionVehicleType> j = stage.getInfo().getVehicles().iterator();
			while (j.hasNext()) {
				ConstructionVehicleType vehicleType = j.next();
				// Only handle light utility vehicles for now.
				if (vehicleType.getVehicleType().equalsIgnoreCase(VehicleType.LUV.getName())) {
					LightUtilityVehicle luv = reserveLightUtilityVehicle();
					if (luv != null) {
						constructionVehicles.add(luv);
//						luv.setMission(this);
						claimVehicle(luv);
					} else {
						logger.warning(settlement, "BuildingConstructionMission : LUV not available");
						endMission(LUV_NOT_AVAILABLE);
					}
				}
			}
		}
	}
	
	/**
	 * Claims the mission's vehicle and reserve it.
	 * 
	 * @param v Vehicle to be claimed
	 */
	protected final void claimVehicle(Vehicle v) {
		if (v.getMission() != null) {
			logger.warning(v, "Aready assigned to a Mission when assigning " + getName());
		}

		v.setReservedForMission(true);
//		v.addUnitListener(this);
		v.setMission(this);
		
		fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
	}
	
	/**
	 * Retrieves LUV attachment parts from the settlement.
	 */
	public void retrieveConstructionLUVParts() {
		if (stage != null) {
			luvAttachmentParts = new ArrayList<>();
			int vehicleIndex = 0;
			Iterator<ConstructionVehicleType> k = stage.getInfo().getVehicles().iterator();
			while (k.hasNext()) {
				Vehicle vehicle = null;
				if (constructionVehicles.size() > vehicleIndex) {
					vehicle = constructionVehicles.get(vehicleIndex);
				}

				Iterator<Integer> l = k.next().getAttachmentParts().iterator();
				while (l.hasNext()) {
					Integer part = l.next();
					try {
						settlement.retrieveItemResource(part, 1);
						if (vehicle != null) {
							vehicle.storeItemResource(part, 1);
						}
						luvAttachmentParts.add(part);
					} catch (Exception e) {
						Part p = ItemResourceUtil.findItemResource(part);
						endMissionProblem(settlement, "Cannot retreive Part " + p.getName());
					}
				}
				vehicleIndex++;
			}
		}
	}

	/**
	 * Determines a new construction stage info for a site.
	 *
	 * @param site  the construction site.
	 * @param skill the architect's construction skill.
	 * @return construction stage info.
	 * @throws Exception if error determining construction stage info.
	 */
	public ConstructionStageInfo determineNewStageInfoByProfits(ConstructionSite site, int skill) {
		ConstructionStageInfo result = null;
		if (manager == null)
			manager = settlement.getConstructionManager();
		ConstructionValues values = manager.getConstructionValues();
		Map<ConstructionStageInfo, Double> stageProfits = values.getNewConstructionStageProfits(site, skill);
		if (!stageProfits.isEmpty()) {
			result = RandomUtil.getWeightedRandomObject(stageProfits);
		}

		return result;
	}

	/**
	 * Checks if a light utility vehicle (LUV) is available for the mission.
	 *
	 * @param settlement the settlement to check.
	 * @return true if LUV available.
	 */
	public static boolean isLUVAvailable(Settlement settlement) {
		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			if (vehicle.getVehicleType() == VehicleType.LUV) {
				boolean usable = !vehicle.isReserved();

                usable = usable && vehicle.isVehicleReady() && !vehicle.isBeingTowed();

				if (((Crewable) vehicle).getCrewNum() > 0)
					usable = false;

				if (usable)
					result = true;

			}
		}

		return result;
	}

	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (SELECT_SITE_PHASE.equals(getPhase())) {
			setPhase(PREPARE_SITE_PHASE, stage.getInfo().getName());
		}
		else if (PREPARE_SITE_PHASE.equals(getPhase())) {
			setPhase(CONSTRUCTION_PHASE, stage.getInfo().getName());
		}
		else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			endMission(CONSTRUCTION_ENDED);
		}
		else {
			handled = false;
		}
		return handled;
	}

	@Override
	protected void performPhase(Worker member) {
		super.performPhase(member);
		if (SELECT_SITE_PHASE.equals(getPhase())) {
			selectSitePhase(member);
		} else if (PREPARE_SITE_PHASE.equals(getPhase())) {
			prepareSitePhase(member);
		} else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			constructionPhase(member);
		}
	}

	/**
	 * Performs the tasks in 'Select site' phase.
	 * 
	 * @param member
	 */
	private void selectSitePhase(Worker member) {
		// Need player to acknowledge the site location before proceeding
		if (site.isSiteLocConfirmed()) {
			setPhaseEnded(true);
			logger.info(settlement, site, "Ending the 'Select Site' phase.");
		}
	}
	
	/**
	 * Checks if the construction materials are ready.
	 * 
	 * @return
	 */
	private boolean isMaterialReady(Worker member) {
		boolean available = loadAvailableConstructionMaterials();
		
		if (!available) {
			// If the materials are not ready
			retrieveMaterials(member);
		}
				
		return loadAvailableConstructionMaterials();
	}

	/**
	 * Obtains materials by performing the DigLocalRegolith task.
	 */
	private void retrieveMaterials(Worker member) {
		// If material not available, prompt settlers to dig local regolith
		Person p = (Person) member;
		if (RandomUtil.lessThanRandPercent(DIG_REGOLITH_PERCENT_PROBABILITY)
			&& member.getUnitType() == UnitType.PERSON) {
			boolean accepted = assignTask(p, new DigLocalRegolith(p));
			if (accepted)
				logger.info(p, 60_000, "Confirmed receiving the assigned task of DigLocalRegolith.");
		}		
	}
	
	/**
	 * Performs the task in 'Prepares site' phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void prepareSitePhase(Worker member) {
		logger.info(settlement, member, 60_000, "Preparing '" + site + "'.");

		if (!isMaterialReady(member)) {
			logger.info(settlement, member, 60_000, "Materials not ready at " + site + ".");
			return;
		}
	
		if (!loadAvailableConstructionParts()) {
			logger.info(settlement, member, 60_000, "Parts not ready at " + site + ".");
			return;
		}
		
		// Check if site preparation time has expired
		// TODO: generate a task to truly model what settlers need to do to prep a site
		if (getPhaseDuration() >= SITE_PREPARE_TIME) {
			// Automatically confirm the site location after a certain period of time
			site.setSiteLocConfirmed(true);
			
			setPhaseEnded(true);
		}
	}

	/**
	 * Loads remaining required construction materials into site that are available
	 * at settlement inventory.
	 * 
	 * @return true if all resources are available
	 */
	private boolean loadAvailableConstructionMaterials() {
		boolean enough = true;
		// Load amount resources.
		Iterator<Integer> i = stage.getMissingResources().keySet().iterator();
		while (i.hasNext()) {
			Integer resource = i.next();
			double amountNeeded = stage.getMissingResources().get(resource);
			double amountAvailable = settlement.getAmountResourceStored(resource);
			// Load as much of the remaining resource as possible into the construction site
			// stage.
			double amountLoading = Math.min(amountAvailable, amountNeeded);

			if (amountLoading > SMALL_AMOUNT) {
				// Retrieve this materials now
				settlement.retrieveAmountResource(resource, amountLoading);
				// Store the materials at this site
				stage.addResource(resource, amountLoading);
			}
			else
				enough = false;
			
			// Use a 10% buffer just in case other tasks will consume this materials at the same time
			if (amountAvailable < amountNeeded * 1.1) {
				enough = false;
			}
		}
		
		return enough;
	}
		
		
	/**
	 * Loads remaining required construction materials into site that are available
	 * at settlement inventory.
	 * 
	 * @return true if all parts are available
	 */
	private boolean loadAvailableConstructionParts() {
		boolean enough = true;
		// Load parts.
		Iterator<Integer> j = stage.getMissingParts().keySet().iterator();
		while (j.hasNext()) {
			Integer part = j.next();
			int numberNeeded = stage.getMissingParts().get(part);
			int numberAvailable = settlement.getItemResourceStored(part);
			// Load as many remaining parts as possible into the construction site stage.
			int numberLoading = Math.min(numberAvailable, numberNeeded);

			if (numberLoading > 0) {
				// Retrieve this item now
				settlement.retrieveItemResource(part, numberLoading);
				// Store this item at this site
				stage.addParts(part, numberLoading);
			}
			else
				enough = false;
		}
		
		return enough;
	}

	/**
	 * Performs the construction phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void constructionPhase(Worker member) {
		
		if (!isMaterialReady(member)) {
			setPhase(PREPARE_SITE_PHASE, settlement.getName());
			return;
		}
	
		if (!loadAvailableConstructionParts()) {
			setPhase(PREPARE_SITE_PHASE, settlement.getName());
			return;
		}

		// Display the LUV(s)
		showLightUtilityVehicle();
		
		// Anyone in the crew or a single person at the home settlement has a
		// dangerous illness, end phase.
		if (hasEmergency()) {
			setPhaseEnded(true);
		}

		// Check if further work can be done on construction stage.
		if (stage.getCompletableWorkTime() <= stage.getCompletedWorkTime()) {
			setPhaseEnded(true);
		}

		boolean canAssign = false;
		if (!getPhaseEnded()) {
			// Assign construction task to member.
			Person p = (Person) member;
			if (RandomUtil.lessThanRandPercent(CONSTRUCT_PERCENT_PROBABILITY)
				&& member.getUnitType() == UnitType.PERSON
				&& ConstructBuilding.canConstruct(p, site)) {
				canAssign = assignTask(p, new ConstructBuilding(p, stage, site, constructionVehicles));
			}
		}
		
		if (canAssign)
			logger.info(member.getAssociatedSettlement(), member, 30_000L, "Assigned to construct " + site + ".");
		else
			logger.info(member.getAssociatedSettlement(), member, 30_000L, "Not ready to be assigned to construct " + site + ".");
		
		checkConstructionStageComplete();
	}

	/**
	 * Checks if this construction stage is complete.
	 */
	public void checkConstructionStageComplete() {
		if (stage.isComplete()) {
			setPhaseEnded(true);
			if (manager == null)
				manager = settlement.getConstructionManager();
			manager.getConstructionValues().clearCache();

			if (site.isAllConstructionComplete()) {
				// Construct building if all 3 stages of the site construction have been complete.
				Building building = site.createBuilding(((Unit)settlement).getIdentifier());
				manager.removeConstructionSite(site);
				settlement.fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_BUILDING_EVENT, building);
				logger.info(settlement, "New building '" + site.getBuildingName() + "' constructed.");
			}
			else {
				// Inform that this stage is finish
				logger.info(settlement, "'" + site.getStageInfo().getName() + "' was finished.");
			}
		}
	}

	@Override
	public void endMission(MissionStatus endStatus) {
		// Mark site as not undergoing construction.
		if (site != null)
			site.setUndergoingConstruction(false);

		// Unreserve all LUV attachment parts for this mission.
		unreserveLUVparts();

		for (GroundVehicle v : getConstructionVehicles()) {
			if (v.getMission().equals(this)) {
				v.setMission(null);
			}
		}
		super.endMission(endStatus);
	}

	/**
	 * Gets the settlement associated with the vehicle.
	 *
	 * @return settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return settlement;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {

		Map<Integer, Number> resources = new HashMap<>();

		// Add construction LUV attachment parts.
		if (luvAttachmentParts != null) {
			Iterator<Integer> i = luvAttachmentParts.iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				if (resources.containsKey(part)) {
					resources.put(part, (resources.get(part).intValue() + 1));
				} else {
					resources.put(part, 1);
				}
			}
		}

		return resources;
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> equipment = new HashMap<>(1);
		equipment.put(EquipmentType.convertName2ID(EVASuit.TYPE), getMembers().size());
		return equipment;
	}

	/**
	 * Display the light utility vehicles on the settlement map.
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private void showLightUtilityVehicle() {

		Iterator<GroundVehicle> i = constructionVehicles.iterator();
		while (i.hasNext()) {
			GroundVehicle vehicle = i.next();
			LightUtilityVehicle luv = (LightUtilityVehicle) vehicle;
			// Place light utility vehicles at random location in construction site.
			LocalPosition settlementLocSite = LocalAreaUtil.getRandomLocalRelativePosition(site);
			luv.setParkedLocation(settlementLocSite, RandomUtil.getRandomDouble(360D));
		}
	}
	
	/**
	 * Reserves a light utility vehicle for the mission.
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle() {
		LightUtilityVehicle result = null;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext() && (result == null)) {
			Vehicle vehicle = i.next();

			if (vehicle.getVehicleType() == VehicleType.LUV) {
				LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
				if (((luvTemp.getPrimaryStatus() == StatusType.PARKED) || (luvTemp.getPrimaryStatus() == StatusType.GARAGED))
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
					result = luvTemp;
					luvTemp.setReservedForMission(true);

					if (!settlement.removeParkedVehicle(luvTemp)) {
						endMissionProblem(luvTemp, "Can not remove parked vehicle");
					}
				}
			}
		}

		return result;
	}


	/*
	 * Unreserves and store back all LUV attachment parts in settlement.
	 */
	public void unreserveLUVparts() {

		if (luvAttachmentParts != null) {
			Iterator<Integer> i = luvAttachmentParts.iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				try {
					settlement.storeItemResource(part, 1);
				} catch (Exception e) {
					logger.severe(settlement, 5_000, 
							"Error storing attachment part " + ItemResourceUtil.findItemResource(part).getName());
				}
			}
		}
	}

	/**
	 * Gets a list of all construction vehicles used by the mission.
	 *
	 * @return list of construction vehicles.
	 */
	@Override
	public List<GroundVehicle> getConstructionVehicles() {
		if (constructionVehicles != null && !constructionVehicles.isEmpty()) {
			return new ArrayList<>(constructionVehicles);
		} 
		
		return new ArrayList<>();
	}

	@Override
	protected boolean hasEmergency() {
		boolean result = super.hasEmergency();

		try {
			// Cancel construction mission if there are any beacon vehicles within range
			// that need help.
			Vehicle vehicleTarget = null;
			Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
			if (vehicle != null) {
				vehicleTarget = RescueSalvageVehicle.findBeaconVehicle(settlement, vehicle.getRange());
				if (vehicleTarget != null) {
					if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget))
						result = true;
				}
			}
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * Gets the mission's construction site.
	 *
	 * @return construction site.
	 */
	public ConstructionSite getConstructionSite() {
		return site;
	}

	/**
	 * Gets the mission's construction stage.
	 *
	 * @return construction stage.
	 */
	public ConstructionStage getConstructionStage() {
		return stage;
	}

	public static boolean positionSameBuildingType(String buildingType, double dist, ConstructionSite site) {
		boolean goodPosition = false;
		// Try to put building next to the same building type.
		List<Building> sameBuildings = site.getSettlement().getBuildingManager()
				.getBuildingsOfSameType(buildingType);
		
		Collections.shuffle(sameBuildings);
		Iterator<Building> j = sameBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			goodPosition = positionNextToBuilding(site, building, Math.round(dist), false);
			if (goodPosition) {
//				logger.info("Positioning '" + site + "' next to " + b.getNickName());
				logger.info(site.getSettlement(), "Case 1. The building type '" 
						+ buildingType + "' has life support.");
				break;
			}
		}
		return goodPosition;
	}

	/**
	 * Determines and sets the position of a new construction site.
	 *
	 * @param site the new construction site.
	 */
	public static void positionNewSite(ConstructionSite site) {

		boolean goodPosition = false;

		Settlement s = site.getSettlement();
		// Use settlement's objective to determine the desired building type
		String buildingType = s.getObjectiveBuildingType();
		
		logger.info(s, "Applying building type '" + buildingType + "' as reference for '" + site + "'.");
		
		if (buildingType != null) {
			
			if (buildingConfig == null) {
				buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
			}
			
			BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);
			site.setWidth(spec.getWidth());
			site.setLength(spec.getLength());
			boolean isBuildingConnector = spec.getFunctionSupported().contains(FunctionType.BUILDING_CONNECTION);
			boolean hasLifeSupport = spec.getFunctionSupported().contains(FunctionType.LIFE_SUPPORT);

			if (isBuildingConnector) {
				// Try to find best location to connect two buildings.
				goodPosition = positionNewBuildingConnectorSite(site, buildingType);
			} 
			
			else if (hasLifeSupport) {

				goodPosition = positionSameBuildingType(buildingType, DEFAULT_FARMING_DISTANCE, site);
		
				if (!goodPosition) {
					// Try to put building next to another habitable building.
					List<Building> habitableBuildings = site.getSettlement().getBuildingManager()
							.getBuildings(FunctionType.LIFE_SUPPORT);
					Collections.shuffle(habitableBuildings);
					for (Building b : habitableBuildings) {
						// Match the floor area (e.g look more organize to put all 7m x 9m next to one
						// another)
						if (b.getFloorArea() == site.getWidth() * site.getLength()) {
							goodPosition = positionNextToBuilding(site, b, DEFAULT_HABITABLE_BUILDING_DISTANCE,
									false);
							if (goodPosition) {
								logger.info(s, "Case 2. Habitable.");
								break;
							}
						}
					}
				}
			}
			else {
				// Try to put building next to the same building type.
				logger.info(s, "Case 3. Inhabitable.");
				goodPosition = positionSameBuildingType(buildingType, DEFAULT_INHABITABLE_BUILDING_DISTANCE, site);
			}
		}

		else {
			// Case 4: building type is null
			
			// Determine preferred building type from foundation stage info.
			// buildingType = determinePreferredConstructedBuildingType(foundationStageInfo,
			// constructionSkill);

			// Try to put building next to another habitable building.
			List<Building> habitableBuildings = s.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
			Collections.shuffle(habitableBuildings);
			for (Building b : habitableBuildings) {
				// Match the floor area (e.g look more organize to put all 7m x 9m next to one
				// another)
				if (b.getFloorArea() == site.getWidth() * site.getLength()) {
					goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
					if (goodPosition) {
						logger.info(s, "Case 4. Habitable. Building type not given.");
						break;
					}
				}
			}
		}

		if (!goodPosition) {
			// Try to put building next to another building.
			// If not successful, try again 10m from each building and continue out at 10m
			// increments
			// until a location is found.
			BuildingManager buildingManager = site.getSettlement().getBuildingManager();
			if (buildingManager.getNumBuildings() > 0) {
				for (int x = 10; !goodPosition; x += 10) {
					for (Building b : buildingManager.getBuildingSet()) {
						goodPosition = positionNextToBuilding(site, b, (double) x, false);
						if (goodPosition) {
							logger.info(s, "Case 5. Any one of the buildings.");
							break;
						}
					}
				}
			} 
			
			else {
				logger.info(s, "Case 6. No buildings found.");
				// If no buildings at settlement, position new construction site at (0, 0) with
				// random facing.
				int angle = RandomUtil.getRandomInt(4) * 90;
				site.setFacing(angle);
				site.setPosition(LocalPosition.DEFAULT_POSITION);
			}
		}
	}

	/**
	 * Determine the position and length (for variable length sites) for a new
	 * building connector construction site.
	 *
	 * @param site         the construction site.
	 * @param buildingType the new building type.
	 * @return true if position/length of construction site could be found, false if
	 *         not.
	 */
	private static boolean positionNewBuildingConnectorSite(ConstructionSite site, String buildingType) {

		boolean result = false;

		BuildingManager manager = site.getSettlement().getBuildingManager();
		List<Building> inhabitableBuildings = manager.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(inhabitableBuildings);

		if (buildingConfig == null) {
			buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		}

		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);

		int baseLevel = spec.getBaseLevel();

		// Try to find a connection between an inhabitable building without access to
		// airlock and
		// another inhabitable building with access to an airlock.
		if (site.getSettlement().getAirlockNum() > 0) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> i = inhabitableBuildings.iterator();
			while (i.hasNext()) {
				Building startingBuilding = i.next();
				if (!site.getSettlement().hasWalkableAvailableAirlock(startingBuilding)) {

					// Find a different inhabitable building that has walkable access to an airlock.
					Iterator<Building> k = inhabitableBuildings.iterator();
					while (k.hasNext()) {
						Building building = k.next();
						if (!building.equals(startingBuilding)) {

							// Check if connector base level matches either building.
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

							if (site.getSettlement().hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
								double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
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

		// Try to find valid connection location between two inhabitable buildings with
		// no joining walking path.
		if (!result) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean hasWalkingPath = site.getSettlement().getBuildingConnectorManager().hasValidPath(startingBuilding,
							building);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

						double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding, building)) {
								leastDistance = distance;
								result = true;
							}
						}
					}
				}
			}
		}

		// Try to find valid connection location between two inhabitable buildings that
		// are not directly connected.
		if (!result) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean directlyConnected = (site.getSettlement().getBuildingConnectorManager()
							.getBuildingConnections(startingBuilding, building).size() > 0);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
						double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding, building)) {
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
			if (spec.getLength() == -1D) {
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
	 * Determine the position and length (for variable length) for a connector
	 * building between two existing buildings.
	 *
	 * @param buildingType   the new connector building type.
	 * @param site           the construction site.
	 * @param firstBuilding  the first of the two existing buildings.
	 * @param secondBuilding the second of the two existing buildings.
	 * @return true if position/length of construction site could be found, false if
	 *         not.
	 */
	private static boolean positionConnectorBetweenTwoBuildings(String buildingType, ConstructionSite site,
			Building firstBuilding, Building secondBuilding) {

		boolean result = false;

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
		if (buildingConfig == null) {
			buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		}
		
		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);
		double width = spec.getWidth();
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
					boolean clearPath = LocalAreaUtil.isLinePathCollisionFree(line, site.getSettlement().getCoordinates(), false);
					if (clearPath) {
						validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
					}
				}
			}
		}

		if (!validLines.isEmpty()) {
			result = isLineValid(validLines, site, firstBuilding, secondBuilding, width);
		}

		return result;
	}
	
	private static boolean isLineValid(List<Line2D> validLines, ConstructionSite site,
			Building firstBuilding, Building secondBuilding, double width) {
		
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

		if (shortestLine == null)
			shortestLine = validLines.get(0);
	
		// Create building template with position, facing, width and length for the
		// connector building.
		double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
		Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
		Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding,
				width);
		double centerX = (p1.getX() + p2.getX()) / 2D;
		double centerY = (p1.getY() + p2.getY()) / 2D;
		double newLength = p1.distance(p2);
		double facingDegrees = LocalAreaUtil.getDirection(p1, p2);

		// Provide the site the position, facing and length
		site.setPosition(new LocalPosition(centerX, centerY));
		site.setFacing(facingDegrees);
		site.setLength(newLength);
		
		// TODO: is there any situation it returns false
		return true;
	}


	/**
	 * Adjust the connector end point based on relative angle of the connection.
	 *
	 * @param point          the initial connector location.
	 * @param lineFacing     the facing of the connector line (degrees).
	 * @param building       the existing building being connected to.
	 * @param connectorWidth the width of the new connector.
	 * @return point adjusted location for connector end point.
	 */
	private static Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building,
			double connectorWidth) {

		double lineFacingRad = Math.toRadians(lineFacing);
		double angleFromBuildingCenterDegrees = LocalAreaUtil
				.getDirection(new Point2D.Double(building.getXLocation(), building.getYLocation()), point);
		double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
		double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
		double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

		double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
		double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

		return new Point2D.Double(newXLoc, newYLoc);
	}

	/**
	 * Gets four positions surrounding a building with a given distance from its
	 * edge.
	 *
	 * @param building         the building.
	 * @param distanceFromSide distance (distance) for positions from the edge of
	 *                         the building.
	 * @return list of four positions.
	 */
	private static List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

		List<Point2D> result = new ArrayList<>(4);

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		for (int x = 0; x < 4; x++) {
			double xPos = 0D;
			double yPos = 0D;

			switch (x) {
			case front:
				xPos = 0D;
				yPos = (building.getLength() / 2D) + distanceFromSide;
				break;
			case back:
				xPos = 0D;
				yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
				break;
			case right:
				xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
				yPos = 0D;
				break;
			case left:
				xPos = (building.getWidth() / 2D) + distanceFromSide;
				yPos = 0D;
				break;
			}

			Point2D position = LocalAreaUtil.getLocalRelativeLocation(xPos, yPos, building);
			result.add(position);
		}

		return result;
	}

	/**
	 * Positions a new construction site near an existing building.
	 *
	 * @param site               the new construction site.
	 * @param building           the existing building.
	 * @param separationDistance the separation distance (meters) from the building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return true if construction site could be positioned, false if not.
	 */
	private static boolean positionNextToBuilding(ConstructionSite site, Building building, double separationDistance,
			boolean faceAway) {

		boolean goodPosition = false;

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		List<Integer> directions = new ArrayList<>(4);
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
			case front:
				direction = building.getFacing();
				structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
				break;
			case back:
				direction = building.getFacing() + 180D;
				structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
				if (faceAway) {
					rectRotation = building.getFacing() + 180D;
				}
				break;
			case right:
				direction = building.getFacing() + 90D;
				structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
					rectRotation = building.getFacing() + 90D;
				}
				break;
			case left:
				direction = building.getFacing() + 270D;
				structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
					rectRotation = building.getFacing() + 270D;
				}
			}

			if (rectRotation > 360D) {
				rectRotation -= 360D;
			}

			// Cause each the site to face a random direction each time this method is run
//			int rand = RandomUtil.getRandomInt(4);
//			rectRotation += 90 * rand;
//			if (rectRotation > 360D) {
//				rectRotation -= 360D;
//			}

			double distance = structureDistance + separationDistance;
			double radianDirection = Math.PI * direction / 180D;
			LocalPosition rectCenter = building.getPosition().getPosition(distance, radianDirection);

			// Check to see if proposed new site position intersects with any existing
			// buildings
			// or construction sites.
			BoundedObject sitePosition = new BoundedObject(rectCenter, site.getWidth(), site.getLength(), rectRotation);
			if (site.getSettlement().getBuildingManager().isBuildingLocationOpen(sitePosition, site)) {
				// Set the new site here.
				site.setPosition(rectCenter);
				site.setFacing(rectRotation);
				goodPosition = true;
				break;
			}
		}

		return goodPosition;
	}
	
	protected void setPhase(MissionPhase phase, String s) {
		site.setPhase(phase);
		super.setPhase(phase, s);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		site = null;
		stage = null;
		constructionVehicles = null;
		luvAttachmentParts = null;
	}
}
