/*
 * Mars Simulation Project
 * ConstructionMission.java
 * @date 2023-06-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionVehicleType;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.ConstructBuilding;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**getAss
 * Mission for construction a stage for a settlement building.
 * strings
 */
public class ConstructionMission extends AbstractMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ConstructionMission.class.getName());

	/** Mission phases. */
	private static final MissionPhase SELECT_SITE_PHASE = new MissionPhase("Mission.phase.selectConstructionSite");
	private static final MissionPhase PREPARE_SITE_PHASE = new MissionPhase("Mission.phase.prepareConstructionSite");
	private static final MissionPhase CONSTRUCTION_PHASE = new MissionPhase("Mission.phase.construction");

	private static final MissionStatus LUV_NOT_AVAILABLE = new MissionStatus("Mission.status.noLUV");
	private static final MissionStatus CONSTRUCTION_ENDED = new MissionStatus("Mission.status.constructionEnded");
	private static final MissionStatus NEW_CONSTRUCTION_STAGE_NOT_DETERMINED = new MissionStatus("Mission.status.noConstructionStage");

	// Number of mission members.
	public static final int MIN_PEOPLE = 2;
	
	private static final int MAX_PEOPLE = 10;
	
	private static final int CONSTRUCT_PERCENT_PROBABILITY = 50;
	
	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = 250D;

	private ConstructionObjective objective;
	
	/**
	 * Constructor 1 for Case 1: Determined by the need of the settlement.
	 *
	 * @param startingMember the mission member starting the mission.
	 */
	public ConstructionMission(Worker startingMember) {
		// Use Mission constructor.
		super(MissionType.CONSTRUCTION, startingMember);

		if (isDone()) {
			return;
		}

		// Sets the mission capacity.
		setMissionCapacity(MAX_PEOPLE);

		// Recruit additional members to mission.
		if (!recruitMembersForMission(startingMember, true, MIN_PEOPLE)) {
			return;
		}


		// Determine construction site and stage.
		int constructionSkill = 1;
		if (startingMember instanceof Person p) {
			constructionSkill += p.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			p.getMind().setMission(this); // THis has probably already been set
		}

		var site = startingMember.getAssociatedSettlement().getConstructionManager().getNextConstructionSite(constructionSkill);
		if (site == null) {
			endMission(NEW_CONSTRUCTION_STAGE_NOT_DETERMINED);
			return;
		}
		createObjectives(site, null);
		
		// Need to set the description of this mission correctly
		// e.g. Pouring the foundation, Building the frame, or Constructing the building

		// Call missionManager to add this mission
	    missionManager.addMission(this);
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
			ConstructionSite choosenSite,
			List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		super(MissionType.CONSTRUCTION, (Worker) members.toArray()[0]);

		// Add mission members.
		addMembers(members, false);
		
		setMissionCapacity(MAX_PEOPLE);

		if (choosenSite == null) {
			throw new IllegalArgumentException("Choosen site is missing");
		}

		// site already selected
		logger.info(settlement, "Case 2. new construction stageInfo could not be determined.");

		if (isDone()) {
			return;
		}

		createObjectives(choosenSite, vehicles);
	}
	
	private void createObjectives(ConstructionSite site, List<GroundVehicle> constructionVehicles) {
		var settlement = site.getAssociatedSettlement();
		site.setWorkOnSite(this);

		var stage = site.getCurrentConstructionStage();
		// Reserve construction vehicles.
		if (constructionVehicles == null) {
			constructionVehicles = reserveConstructionVehicles(settlement, stage);
		}

		// Retrieve construction LUV attachment parts.
		var luvAttachmentParts = retrieveConstructionLUVParts(settlement, stage, constructionVehicles);

		objective = new ConstructionObjective(site, stage, constructionVehicles, luvAttachmentParts);
		addObjective(objective);
		
		// Create mission designation
		createDesignationString();

		setPhase(PREPARE_SITE_PHASE, site.getAssociatedSettlement().getName());
	}

	/**
	 * Reserves construction vehicles for the mission.
	 */
	private List<GroundVehicle> reserveConstructionVehicles(Settlement settlement, ConstructionStage stage) {
		// Construct a new list of construction vehicles
		List<GroundVehicle> constructionVehicles = new ArrayList<>();
		for(ConstructionVehicleType vehicleType : stage.getInfo().getVehicles()) {
			// Only handle light utility vehicles for now.
			if (vehicleType.getVehicleType() == VehicleType.LUV) {
				LightUtilityVehicle luv = reserveLightUtilityVehicle(settlement);
				if (luv != null) {
					constructionVehicles.add(luv);
					claimVehicle(luv);
				} else {
					logger.warning(settlement, "BuildingConstructionMission : LUV not available");
					endMission(LUV_NOT_AVAILABLE);
					return Collections.emptyList();
				}
			}
		}

		return constructionVehicles;
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
		v.setMission(this);
		
		fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
	}
	
	/**
	 * Retrieves LUV attachment parts from the settlement.
	 * @return 
	 */
	public List<Integer> retrieveConstructionLUVParts(Settlement settlement, ConstructionStage stage, List<GroundVehicle> reserved) {
		List<Integer> luvAttachmentParts = new ArrayList<>();
		int vehicleIndex = 0;
		for(var k : stage.getInfo().getVehicles()) {
			Vehicle vehicle = null;
			if (reserved.size() > vehicleIndex) {
				vehicle = reserved.get(vehicleIndex);
			}

			for(Integer part : k.getAttachmentParts()) {
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
		return luvAttachmentParts;
	}

	
	/**
	 * Checks if a light utility vehicle (LUV) is available for the mission.
	 *
	 * @param settlement the settlement to check.
	 * @return true if LUV available.
	 */
	public static boolean isLUVAvailable(Settlement settlement) {
		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedGaragedVehicles().iterator();
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
		var phase = getPhase();
		var stage = objective.getStage();

		if (SELECT_SITE_PHASE.equals(phase)) {
			setPhase(PREPARE_SITE_PHASE, stage.getInfo().getName());
		}
		else if (PREPARE_SITE_PHASE.equals(phase)) {
			setPhase(CONSTRUCTION_PHASE, stage.getInfo().getName());
		}
		else if (CONSTRUCTION_PHASE.equals(phase)) {
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
		if (PREPARE_SITE_PHASE.equals(getPhase())) {
			prepareSitePhase(objective.getSite());
		} else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			constructionPhase(member);
		}
	}
	
	/**
	 * Are all the prerequistes meet to start construction
	 * @param site
	 * @return
	 */
	private boolean isPreReqsAvailable(ConstructionSite site) {
		var settlement = site.getAssociatedSettlement();
		var stage = objective.getStage();

		if (!stage.loadAvailableConstructionMaterials(settlement)) {
			logger.info(site, 60_000, "Materials not ready at " + site.getName() + ".");
			return false;
		}
	
		if (!stage.loadAvailableConstructionParts(settlement)) {
			logger.info(site, 60_000, "Parts not ready at " + site.getName() + ".");
			return false;
		}
		return true;
	}

	/**
	 * Performs the task in 'Prepares site' phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void prepareSitePhase(ConstructionSite site) {
		if (!isPreReqsAvailable(site)) {
			return;
		}
		
		// Check if site preparation time has expired
		if (getPhaseDuration() >= SITE_PREPARE_TIME) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the construction phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void constructionPhase(Worker member) {
		var site = objective.getSite();

		if (!isPreReqsAvailable(site)) {
			setPhase(PREPARE_SITE_PHASE, site.getAssociatedSettlement().getName());
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
		var stage = objective.getStage();
		if (stage.getRequiredWorkTime() <= stage.getCompletedWorkTime()) {
			setPhaseEnded(true);
		}

		boolean canAssign = false;
		if (!getPhaseEnded()) {
			// Assign construction task to member.
			Person p = (Person) member;
			if (p.isInSettlement() && RandomUtil.lessThanRandPercent(CONSTRUCT_PERCENT_PROBABILITY)
				&& ConstructBuilding.canConstruct(p, site)) {

				canAssign = assignTask(p, new ConstructBuilding(p, stage, site, objective.getConstructionVehicles()));
			}
		}
		
		if (canAssign)
			logger.info(member, 30_000L, "Assigned to construct " + site.getName() + ".");
		else
			logger.info(member, 30_000L, "Not ready to be assigned to construct " + site.getName() + ".");
		
		checkConstructionStageComplete(site, stage);
	}

	/**
	 * Checks if this construction stage is complete.
	 * @param site 
	 * @param constructionStage 
	 */
	private void checkConstructionStageComplete(ConstructionSite site, ConstructionStage stage) {
		if (stage.isComplete()) {
			setPhaseEnded(true);
			var manager = site.getAssociatedSettlement().getConstructionManager();

			if (site.isComplete()) {
				manager.removeConstructionSite(site);

				if (stage.isConstruction()) {		
					// Construct building if all 3 stages of the site construction have been complete.
					site.createBuilding();
					logger.info(site, "New building '" + site.getBuildingName() + "' constructed.");
				}
				else {
					// Get average construction skill of mission members.
					double averageSkill = getMembers().stream()
							.mapToDouble(w -> w.getSkillManager()
								.getEffectiveSkillLevel(SkillType.CONSTRUCTION))
							.average().orElse(0D);
					site.reclaimParts(averageSkill);
				}
			}
			else {
				// Move on to the next one fromm the current one
				site.advanceToNextPhase();
			}
		}
	}

	@Override
	public void endMission(MissionStatus endStatus) {
		var site = objective.getSite();
		// Mark site as not undergoing construction.
		site.setWorkOnSite(null);

		// Unreserve all LUV attachment parts for this mission.
		unreserveLUVparts(objective.getLuvAttachmentParts(), site.getAssociatedSettlement());

		objective.getConstructionVehicles().stream()
			.filter(v -> this.equals(v.getMission()))
			.forEach(v1 -> v1.setMission(null));

		super.endMission(endStatus);
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		
		Map<Integer, Number> resources = new HashMap<>();

		for(var part : objective.getLuvAttachmentParts()) {
		    resources.merge(part, 1, (a,b) -> (a.intValue() + b.intValue()));
		}

		return resources;
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> equipment = new HashMap<>();
		equipment.put(EquipmentType.convertName2ID(EVASuit.TYPE), getMembers().size());
		return equipment;
	}

	/**
	 * Display the light utility vehicles on the settlement map.
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private void showLightUtilityVehicle() {
		var site = objective.getSite();
		for(GroundVehicle vehicle : objective.getConstructionVehicles()) {
			LightUtilityVehicle luv = (LightUtilityVehicle) vehicle;
			// Place light utility vehicles at random location in construction site.
			LocalPosition settlementLocSite = LocalAreaUtil.getRandomLocalPos(site);
			luv.setParkedLocation(settlementLocSite, RandomUtil.getRandomDouble(360D));
		}
	}
	
	/**
	 * Reserves a light utility vehicle for the mission.
	 * @param settlement 
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle(Settlement settlement) {
		for(var vehicle : settlement.getParkedGaragedVehicles()) {
			if (vehicle instanceof LightUtilityVehicle luvTemp && ((luvTemp.getPrimaryStatus() == StatusType.PARKED) || (luvTemp.getPrimaryStatus() == StatusType.GARAGED))
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
				luvTemp.setReservedForMission(true);
				return luvTemp;
			}	
		}

		return null;
	}


	/*
	 * Unreserves and store back all LUV attachment parts in settlement.
	 */
	private void unreserveLUVparts(List<Integer> parts, Settlement settlement) {
		parts.forEach(p -> settlement.storeItemResource(p, 1));
	}

	/**
	 * Gets a list of all construction vehicles used by the mission.
	 *
	 * @return list of construction vehicles.
	 */
	public List<GroundVehicle> getConstructionVehicles() {
		return objective.getConstructionVehicles();
	}

	/**
	 * Gets the mission's construction site.
	 *
	 * @return construction site.
	 */
	public ConstructionSite getConstructionSite() {
		return objective.getSite();
	}

	/**
	 * Gets the mission's construction stage.
	 *
	 * @return construction stage.
	 */
	public ConstructionStage getConstructionStage() {
		return objective.getStage();
	}
	
	@Override
	public Settlement getAssociatedSettlement() {
		return objective.getSite().getAssociatedSettlement();
	}
}
