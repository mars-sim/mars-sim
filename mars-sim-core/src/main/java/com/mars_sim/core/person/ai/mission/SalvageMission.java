/*
 * Mars Simulation Project
 * SalvageMission.java
 * @date 2021-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionVehicleType;
import com.mars_sim.core.building.construction.SalvageValues;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.Construction;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.SalvageBuilding;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**s
 * Mission for salvaging a construction stage at a building construction site.
 */
public class SalvageMission extends AbstractMission 
	implements Construction {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(SalvageMission.class.getName());

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.SALVAGE;
	
	/** Mission phases. */
	private static final MissionPhase PREPARE_SITE_PHASE = new MissionPhase("Mission.phase.prepareSalvageSite");
	private static final MissionPhase SALVAGE_PHASE = new MissionPhase("Mission.phase.salvage");
	
	private static final MissionStatus LUV_NOT_AVAILABLE = new MissionStatus("Mission.status.noLUV");
	private static final MissionStatus SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED = new MissionStatus("Mission.status.noSalvageSite");
	private static final MissionStatus SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND = new MissionStatus("Mission.status.noSalvageStage");


	// Number of mission members.
	public static final int MIN_PEOPLE = 3;
	private static final int MAX_PEOPLE = 10;

	public static final int FIRST_AVAILABLE_SOL = 2000;
	/**
	 * Time (millisols) required to prepare construction site for salvaging stage.
	 */
	private static final double SITE_PREPARE_TIME = 500D;


	// Data members
	private boolean finishingExistingStage;
	
	private double wearCondition;
	
	private Settlement settlement;
	private ConstructionSite constructionSite;
	private ConstructionStage constructionStage;
	
	private List<GroundVehicle> constructionVehicles;	

	/**
	 * Constructor.
	 * 
	 * @param startingMember the mission member starting the mission.
	 */
	public SalvageMission(Worker startingMember) {
		// Use Mission constructor.
		super(missionType, startingMember);

		// Set wear condition to 100% by default.
		wearCondition = 100D;

		if (!isDone()) {
			// Sets the settlement.
			settlement = startingMember.getSettlement();

			// Sets the mission capacity.
			setMissionCapacity(MAX_PEOPLE);

			// Recruit additional members to mission.
			recruitMembersForMission(startingMember, true, MIN_PEOPLE);

			// Determine construction site and stage.
			int constructionSkill = startingMember.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);

			ConstructionManager manager = settlement.getConstructionManager();
			SalvageValues values = manager.getSalvageValues();
			values.clearCache();
			double existingSitesProfit = values.getAllSalvageSitesProfit(constructionSkill);
			double newSiteProfit = values.getNewSalvageSiteProfit(constructionSkill);

			if (existingSitesProfit > newSiteProfit) {
				// Determine which existing construction site to work on.
				constructionSite = determineMostProfitableSalvageConstructionSite(settlement, constructionSkill);
			} else {
				// Determine existing building to salvage.
				Building salvageBuilding = determineBuildingToSalvage(settlement, constructionSkill);

				if (salvageBuilding != null) {
					// Create new salvage construction site.
					constructionSite = manager.createNewSalvageConstructionSite(salvageBuilding);

					// Set wear condition to salvaged building's wear condition.
					wearCondition = salvageBuilding.getMalfunctionManager().getWearCondition();
				} else {
					logger.log(Level.WARNING, Msg.getString("BuildingSalvageMission.log.noBuildingFound")); //$NON-NLS-1$
				}
			}

			// Prepare salvage construction site.
			if (constructionSite != null) {

				// Determine new stage to work on.
				if (constructionSite.hasUnfinishedStage()) {
					constructionStage = constructionSite.getCurrentConstructionStage();
					finishingExistingStage = true;
					logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.continueAt" //$NON-NLS-1$
							, settlement.getName()));
				} else {
					constructionStage = constructionSite.getCurrentConstructionStage();
					if (constructionStage != null) {
						constructionStage.setCompletedWorkTime(0D);
						logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
								, constructionStage.toString()));
					} else {
						logger.warning(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$
						endMission(SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND);
					}
				}

				// Mark construction site as undergoing salvage.
				if (constructionStage != null) {
					constructionSite.setWorkOnSite(this);
				}
			} else {
				logger.warning(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
				endMission(SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
			}

			// Reserve construction vehicles.
			reserveConstructionVehicles();

			// Retrieve construction LUV attachment parts.
			retrieveConstructionLUVParts();
		}

		// Set initial mission phase.
		setPhase(PREPARE_SITE_PHASE, settlement.getName());
	}
	
	/**
	 * Constructor
	 * 
	 * @param members    the mission members.
	 * @param settlement the settlement.
	 * @param building   the building to salvage. (null if none)
	 * @param site       the existing salvage construction site. (null if none)
	 * @param vehicles   the construction vehicles.
	 */
	public SalvageMission(Collection<Worker> members, Settlement settlement, Building building,
			ConstructionSite site, List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		super(missionType, (Worker) members.toArray()[0]);

		this.settlement = settlement;

		ConstructionManager manager = settlement.getConstructionManager();

		if (building != null) {
			// Create new salvage construction site.
			constructionSite = manager.createNewSalvageConstructionSite(building);
		} else if (site != null) {
			constructionSite = site;
		} else {
			logger.log(Level.WARNING, Msg.getString("BuildingSalvageMission.log.noSite")); //$NON-NLS-1$
		}

		// Prepare salvage construction site.
		if (constructionSite != null) {

			// Determine new stage to work on.
			if (constructionSite.hasUnfinishedStage()) {
				constructionStage = constructionSite.getCurrentConstructionStage();
				finishingExistingStage = true;
				logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.continueAt" //$NON-NLS-1$
						, settlement.getName()));
			} else {
				constructionStage = constructionSite.getCurrentConstructionStage();
				if (constructionStage != null) {
					constructionStage.setCompletedWorkTime(0D);
					logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
							, constructionStage.toString()));
				} else {
					logger.warning(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$					
					endMission(SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND);
				}
			}

			// Mark construction site as undergoing salvage.
			if (constructionStage != null)
				constructionSite.setWorkOnSite(this);
		} else {
			logger.warning(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
			endMission(SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
		}

		// Mark site as undergoing salvage.
		if (constructionStage != null)
			constructionSite.setWorkOnSite(this);

		addMembers(members, false);

		// Reserve construction vehicles and retrieve from inventory.
		constructionVehicles = vehicles;
		Iterator<GroundVehicle> j = vehicles.iterator();
		while (j.hasNext()) {
			GroundVehicle vehicle = j.next();
			vehicle.setReservedForMission(true);
			// Record the name of this vehicle in Mission
			if (!settlement.removeVicinityParkedVehicle(vehicle)) {
				endMissionProblem(vehicle, "Can not remove parked vehicle");
				return;
			}
		}

		// Retrieve construction LUV attachment parts.
		retrieveConstructionLUVParts();

		// Set initial mission phase.
		setPhase(PREPARE_SITE_PHASE, settlement.getName());
	}

	/**
	 * Determines the most profitable salvage construction site at the settlement.
	 * 
	 * @param settlement        the settlement
	 * @param constructionSkill the architect's construction skill.
	 * @return construction site or null if none found.
	 * @throws Exception if error determining construction site.
	 */
	private ConstructionSite determineMostProfitableSalvageConstructionSite(Settlement settlement,
			int constructionSkill) {
		ConstructionSite result = null;

		double topSiteProfit = 0D;
		ConstructionManager manager = settlement.getConstructionManager();
		for (ConstructionSite site :  manager.getConstructionSitesNeedingMission(false)) {
			double siteProfit = manager.getSalvageValues().getSalvageSiteProfit(site, constructionSkill);
			if (siteProfit > topSiteProfit) {
				result = site;
				topSiteProfit = siteProfit;
			}
		}

		return result;
	}

	/**
	 * Determines a random profitable building at the settlement to salvage.
	 * 
	 * @param settlement        the settlement.
	 * @param constructionSkill the architect's construction skill.
	 * @return building to salvage or null in none found.
	 * @throws Exception if error determining building.
	 */
	private Building determineBuildingToSalvage(Settlement settlement, int constructionSkill) {
		Building result = null;

		SalvageValues values = settlement.getConstructionManager().getSalvageValues();
		Map<Building, Double> salvageBuildings = new HashMap<>();
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			double salvageProfit = values.getNewBuildingSalvageProfit(building, constructionSkill);
			if (salvageProfit > 0D) {
				salvageBuildings.put(building, salvageProfit);
			}
		}

		if (!salvageBuildings.isEmpty()) {
			result = RandomUtil.getWeightedRandomObject(salvageBuildings);
		}

		return result;
	}

	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (PREPARE_SITE_PHASE.equals(getPhase())) {
			setPhase(SALVAGE_PHASE, constructionStage.getInfo().getName());
		}
		else if (SALVAGE_PHASE.equals(getPhase())) {
			endMission(null);
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
			prepareSitePhase();
		} else if (SALVAGE_PHASE.equals(getPhase())) {
			salvagePhase(member);
		}
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return settlement;
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> equipment = new HashMap<>(1);
		equipment.put(EquipmentType.convertName2ID(EVASuit.TYPE), getMembers().size());
		return equipment;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		return new HashMap<>(0);
	}

	private void prepareSitePhase() {
		if (finishingExistingStage) {
			// If finishing uncompleted existing construction stage, skip resource loading.
			setPhaseEnded(true);
		}
		
		// Check if site preparation time has expired.
		if (getPhaseDuration() >= SITE_PREPARE_TIME) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the salvage phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void salvagePhase(Worker member) {

		// Anyone in the crew or a single person at the home settlement has a
		// dangerous illness, end phase.
		if (hasEmergency())
			setPhaseEnded(true);

		if (!getPhaseEnded()) {
			// 75% chance of assigning task, otherwise allow break.
			if (member.isInSettlement() && RandomUtil.lessThanRandPercent(75D)) {
				// Assign salvage building task to person.
				if (member instanceof Person person && SalvageBuilding.canSalvage(person)) {
					assignTask(person,
							new SalvageBuilding(person, constructionStage, constructionSite, constructionVehicles));
				}
			}
		}

		if (constructionStage.isComplete()) {
			setPhaseEnded(true);

			// Remove salvaged construction stage from site.
			constructionSite.advanceToNextPhase();

			// Salvage construction parts from the stage.
			salvageConstructionParts();

			// Mark construction site as not undergoing salvage.
			constructionSite.setWorkOnSite(null);
		}
	}

	@Override
	public void endMission(MissionStatus endStatus) {
		super.endMission(endStatus);

		// Mark site as not undergoing salvage.
		if (constructionSite != null)
			constructionSite.setWorkOnSite(null);

		// Unreserve all mission construction vehicles.
		unreserveConstructionVehicles();
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

			if (vehicle instanceof LightUtilityVehicle) {
                boolean usable = vehicle.isVehicleReady();
					
				if (((Crewable) vehicle).getCrewNum() > 0 || ((Crewable) vehicle).getRobotCrewNum() > 0)
					usable = false;
				
				if (usable)
					result = true;
			}
		}

		return result;
	}

	/**
	 * Reserve construction vehicles for the mission.
	 */
	private void reserveConstructionVehicles() {
		if (constructionStage != null) {
			constructionVehicles = new ArrayList<>();
			for(ConstructionVehicleType vehicleType : constructionStage.getInfo().getVehicles()) {
				// Only handle light utility vehicles for now.
				if (vehicleType.getVehicleType() == VehicleType.LUV) {
					LightUtilityVehicle luv = reserveLightUtilityVehicle();
					if (luv != null)
						constructionVehicles.add(luv);
					else {
						logger.warning(Msg.getString("BuildingSalvageMission.log.noLUV")); //$NON-NLS-1$
						endMission(LUV_NOT_AVAILABLE);
					}				
				}
			}
		}
	}

	/**
	 * Retrieve LUV attachment parts from the settlement.
	 */
	private void retrieveConstructionLUVParts() {
  List<Integer> luvAttachmentParts;
		if (constructionStage != null) {
			luvAttachmentParts = new ArrayList<>();
			int vehicleIndex = 0;
			Iterator<ConstructionVehicleType> k = constructionStage.getInfo().getVehicles().iterator();
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
	 * Reserves a light utility vehicle for the mission.
	 * 
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle() {
		LightUtilityVehicle result = null;

		Iterator<Vehicle> i = settlement.getParkedGaragedVehicles().iterator();
		while (i.hasNext() && (result == null)) {
			Vehicle vehicle = i.next();

			if (vehicle instanceof LightUtilityVehicle luvTemp) {
				StatusType primStatus = luvTemp.getPrimaryStatus();
				if (((primStatus == StatusType.PARKED) || (primStatus == StatusType.GARAGED))
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
					result = luvTemp;
					luvTemp.setReservedForMission(true);

					// Place light utility vehicles at random location in construction site.
					LocalPosition settlementLocSite = LocalAreaUtil.getRandomLocalPos(constructionSite);
					luvTemp.setParkedLocation(settlementLocSite, RandomUtil.getRandomDouble(360D));
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
				if (vehicle.getMission().equals(this)) {
					vehicle.setMission(null);
				}

				// Store construction vehicle back to the settlement.
				settlement.addVicinityVehicle(vehicle);
	
				// Store all construction vehicle attachments in settlement.
				for(int id : vehicle.getItemResourceIDs()) {
					int num = vehicle.getItemResourceStored(id);
					vehicle.retrieveItemResource(id, num);
					settlement.storeItemResource(id, num);
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

	/**
	 * Salvage construction parts from the stage.
	 * 
	 * @throws Exception if error salvaging construction parts.
	 */
	private void salvageConstructionParts() {

		// Modify salvage chance based on building wear condition.
		// Note: if non-building construction stage, wear condition should be 100%.
		double salvageChance = (wearCondition * .25D) + 25D;

		// Get average construction skill of mission members.
		double totalSkill = 0D;
		Iterator<Worker> i = getMembers().iterator();
		while (i.hasNext()) {
			Worker member = i.next();
			totalSkill += member.getSkillManager()
					.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
		}
		double averageSkill = totalSkill / getMembers().size();

		// Modify salvage chance based on average construction skill.
		salvageChance += averageSkill * 5D;

		// Salvage construction parts.
		Map<Integer, Integer> salvagableParts = constructionStage.getInfo().getParts();
		Iterator<Integer> j = salvagableParts.keySet().iterator();
		while (j.hasNext()) {
			Integer part = j.next();
			int number = salvagableParts.get(part);

			int salvagedNumber = 0;
			for (int x = 0; x < number; x++) {
				if (RandomUtil.lessThanRandPercent(salvageChance))
					salvagedNumber++;
			}

			if (salvagedNumber > 0) {
				Part p = ItemResourceUtil.findItemResource(part);
				double mass = salvagedNumber * p.getMassPerItem();
				double capacity = settlement.getCargoCapacity();
				if (mass <= capacity) {
					settlement.storeItemResource(part, salvagedNumber);
				}

			}
		}
	}

	/**
	 * Gets the mission's construction site.
	 * 
	 * @return construction site.
	 */
	public ConstructionSite getConstructionSite() {
		return constructionSite;
	}

	/**
	 * Gets the mission's construction stage.
	 * 
	 * @return construction stage.
	 */
	public ConstructionStage getConstructionStage() {
		return constructionStage;
	}
}
