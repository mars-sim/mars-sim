/*
 * Mars Simulation Project
 * ConstructionMission.java
 * @date 2026-08-15
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
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
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * This class is responsible for the construction of a stage for a settlement building.
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
	
	private static final int CONSTRUCT_PERCENT_PROBABILITY = 25;
	
	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = 250D;

	private double sitePrepTime;
	
	private ConstructionObjective objective;

	
	/**
	 * Constructor 1 for Case 1: Determined by the need of the settlement.
	 *
	 * @param  crew the roster of crew members for the mission.
	 */
	public ConstructionMission(MetaMission.Roster crew) {
		// Use Mission constructor.
		super(MissionType.CONSTRUCTION, crew.leader());

		if (isDone()) {
			return;
		}
		addMembers(crew.members(), false);

		var startingMember = crew.leader();

		// Determine construction site and stage.
		int constructionSkill = startingMember.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);

		var home = startingMember.getAssociatedSettlement();
		var site = home.getConstructionManager().getNextConstructionSite(constructionSkill);
		if (site == null) {
			endMission(NEW_CONSTRUCTION_STAGE_NOT_DETERMINED);
			return;
		}
		createObjectives(site, null);
		
		// Need to set the description of this mission correctly
		// e.g. Pouring the foundation, Building the frame, or Constructing the building
	}

	/**
	 * Constructor 2: Player manually creates this mission.
	 * 
	 * @param members
	 * @param settlement
	 * @param choosenSite
	 * @param vehicles
	 */
	public ConstructionMission(Collection<Worker> members, Settlement settlement,
			ConstructionSite choosenSite, List<LightUtilityVehicle> vehicles) {
		
		if (choosenSite == null) {
			throw new IllegalArgumentException("Choosen site is missing");
		}

		// Use Mission constructor.
		super(MissionType.CONSTRUCTION, (Worker) members.toArray()[0]);

		// Add mission members.
		addMembers(members, false);

		// site already selected
		logger.info(settlement, "Case 2. new construction stageInfo could not be determined.");

		if (isDone()) {
			return;
		}

		createObjectives(choosenSite, vehicles);
	}
	
	/**
	 * Creates the objective instance.
	 * 
	 * @param site
	 * @param constructionVehicles
	 */
	private void createObjectives(ConstructionSite site, List<LightUtilityVehicle> constructionVehicles) {
		var settlement = site.getAssociatedSettlement();
		
		site.setWorkOnSite(this);

		// Site prepare time
		sitePrepTime = SITE_PREPARE_TIME;
		if (settlement.getPreferences().getBooleanValue(SettlementParameters.QUICK_CONST,
                                            false)) {
			sitePrepTime *= 0.1D;
		}
		
		var stage = site.getCurrentConstructionStage();
		// Reserve construction vehicles.
		if (constructionVehicles == null) {
			constructionVehicles = reserveConstructionVehicles(settlement, stage);
		}

		// Retrieve construction LUV attachment parts.
		var luvAttachmentParts = retrieveConstructionLUVParts(settlement, stage, constructionVehicles);

		objective = new ConstructionObjective(site, stage, constructionVehicles, luvAttachmentParts);
		addObjective(objective);

		setPhase(PREPARE_SITE_PHASE, site.getAssociatedSettlement().getName());
	}

	/**
	 * Reserves construction vehicles for the mission.
	 * 
	 * @param settlement
	 * @param stage
	 * @return
	 */
	private List<LightUtilityVehicle> reserveConstructionVehicles(Settlement settlement, ConstructionStage stage) {
		// Construct a new list of construction vehicles
		List<LightUtilityVehicle> constructionVehicles = new ArrayList<>();
		for (ConstructionVehicleType vehicleType : stage.getInfo().getVehicles()) {
			// Only handle light utility vehicles for now.
			if (vehicleType.getVehicleType() == VehicleType.LUV) {
				LightUtilityVehicle luv = reserveLightUtilityVehicle(settlement);
				if (luv != null) {
					constructionVehicles.add(luv);
					claimVehicle(luv);
				} 
//				else {
//					logger.warning(settlement, "BuildingConstructionMission : LUV not available");
//					endMission(LUV_NOT_AVAILABLE);
//					return Collections.emptyList();
//				}
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
		
		fireMissionUpdate(VehicleMission.VEHICLE_EVENT);
	}
	
	/**
	 * Retrieves LUV attachment parts from the settlement.
	 * 
	 * @param settlement
	 * @param stage
	 * @param reserved
	 * @return
	 */
	public List<Integer> retrieveConstructionLUVParts(Settlement settlement, ConstructionStage stage, List<LightUtilityVehicle> reserved) {
		List<Integer> luvAttachmentParts = new ArrayList<>();
		int vehicleIndex = 0;
		var ih = settlement.getEquipmentInventory();
		for(var k : stage.getInfo().getVehicles()) {
			Vehicle vehicle = null;
			if (reserved.size() > vehicleIndex) {
				vehicle = reserved.get(vehicleIndex);
			}

			for(Integer part : k.getAttachmentParts()) {
				try {
					ih.retrieveItemResource(part, 1);
					if (vehicle != null) {
						vehicle.storeItemResource(part, 1);
					}
					luvAttachmentParts.add(part);
				} catch (Exception _) {
					Part p = ItemResourceUtil.findItemResource(part);
					endMissionProblem(settlement, "Cannot retrieve " + p.getName());
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

		Iterator<LightUtilityVehicle> i = settlement.getLUVs().iterator();
		while (i.hasNext()) {
			LightUtilityVehicle luv = i.next();
			boolean usable = !luv.isReserved();				
            usable = usable && luv.isVehicleReady() && !luv.isBeingTowed();

			if (luv.isFull())
				usable = false;

			if (usable)
				result = true;
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
			prepareSitePhase(member, objective.getSite());
		} else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			constructionPhase(member);
		}
	}
	
	/**
	 * Are all the prerequisites meet to start construction ?
	 * 
	 * @param site
	 * @return
	 */
	private boolean isPreReqsAvailable(Worker member, ConstructionSite site) {
		var settlement = site.getAssociatedSettlement();
		var stage = objective.getStage();

		if (!stage.loadAvailableConstructionMaterials(settlement)) {
			logger.info(site, 30_000, member + " found construction materials not ready at " + site.getName() + ".");
			return false;
		}
		return true;
	}

	/**
	 * Performs the task in 'Prepares site' phase.
	 *
	 * @param site the ConstructionSite of interest.
	 */
	private void prepareSitePhase(Worker member, ConstructionSite site) {
		if (!isPreReqsAvailable(member, site)) {
			return;
		}
		
		// Future: add the work of bringing it construction material next to the site
		
		// Check if site preparation time has expired
		if (getPhaseTimeElapsed() >= sitePrepTime) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the construction phase.
	 *
	 * @param worker the mission member performing the phase.
	 */
	private void constructionPhase(Worker worker) {
		var site = objective.getSite();

		if (!isPreReqsAvailable(worker, site)) {
			setPhase(PREPARE_SITE_PHASE, site.getAssociatedSettlement().getName());
			return;
		}
		
		// Check if further work can be done on construction stage.
		var stage = objective.getStage();
		if (stage.getRequiredWorkTime() <= stage.getCompletedWorkTime()) {
			setPhaseEnded(true);
		}

		// Display the LUV(s)
		if (worker.isInVehicle())
			showLightUtilityVehicle(worker);
		
		checkConstructionStageComplete(site, stage);
	}

	/**
	 * Display the light utility vehicles on the settlement map.
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private void showLightUtilityVehicle(Worker worker) {
		var site = objective.getSite();
		for (LightUtilityVehicle luv : objective.getConstructionVehicles()) {
			if (luv.isCrewmember(worker)) {
				// Place light utility vehicles at random location in construction site.
				LocalPosition settlementLocSite = LocalAreaUtil.getRandomLocalPos(site);
				luv.setParkedLocation(settlementLocSite, RandomUtil.getRandomDouble(360D));
			}
		}
	}
	
	/**
	 * Checks if this construction stage is complete.
	 * 
	 * @param site 
	 * @param stage 
	 */
	private void checkConstructionStageComplete(ConstructionSite site, ConstructionStage stage) {
		if (stage.isComplete()) {
			setPhaseEnded(true);

			if (!stage.isConstruction()) {
				// Is salvage so collect parts
				// Get average construction skill of mission members.
				double averageSkill = getMembers().stream()
						.mapToDouble(w -> w.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION))
						.average().orElse(0D);
				site.reclaimParts(averageSkill);
			}
			else if (site.isComplete()) {
				// For construction await the whole site
				var manager = site.getAssociatedSettlement().getConstructionManager();
				manager.removeConstructionSite(site);

				// Construct building if all 3 stages of the site construction have been complete.
				site.createBuilding();
				logger.info(site, "New building '" + site.getBuildingName() + "' constructed.");
			}
			else {
				// Move on to the next one fromm the current one
				site.advanceToNextPhase();
			}
		}
	}

	@Override
	public void endMission(MissionStatus endStatus) {
		if (objective != null) {
			var site = objective.getSite();
			// Mark site as not undergoing construction.
			site.setWorkOnSite(null);
	
			// Unreserve all LUV attachment parts for this mission.
			unreserveLUVparts(objective.getLuvAttachmentParts(), site.getAssociatedSettlement());
	
			objective.getConstructionVehicles().stream()
				.filter(v -> this.equals(v.getMission()))
				.forEach(v1 -> v1.setMission(null));
		}
		
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
	 * Reserves a light utility vehicle for the mission.
	 * 
	 * @param settlement 
	 *
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle(Settlement settlement) {
		for (LightUtilityVehicle luvTemp : settlement.getLUVs()) {
			if (((luvTemp.getPrimaryStatus() == StatusType.PARKED) || (luvTemp.getPrimaryStatus() == StatusType.GARAGED))
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0)) {
				luvTemp.setReservedForMission(true);
				return luvTemp;
			}	
		}

		return null;
	}

	/*
	 * Unreserves and store back all LUV attachment parts in settlement.
	 * 
	 * @param parts
	 * @param settlement
	 */
	private void unreserveLUVparts(List<Integer> parts, Settlement settlement) {
		var ih = settlement.getEquipmentInventory();
		parts.forEach(p -> ih.storeItemResource(p, 1));
	}

	/**
	 * Gets a list of all construction vehicles used by the mission.
	 *
	 * @return list of construction vehicles.
	 */
	public List<LightUtilityVehicle> getConstructionVehicles() {
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
	 * Gets the construction objective.
	 * 
	 * @return
	 */
	public ConstructionObjective getObjective() {
		return objective;
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
		return getStartingPerson().getAssociatedSettlement();
	}
}
