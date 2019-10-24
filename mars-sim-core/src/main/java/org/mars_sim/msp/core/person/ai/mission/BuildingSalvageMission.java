/**
 * Mars Simulation Project
 * BuildingSalvageMission.java
 * @version 3.1.0 2017-10-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
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
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.SalvageBuilding;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.structure.construction.SalvageValues;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Mission for salvaging a construction stage at a building construction site.
 */
public class BuildingSalvageMission extends Mission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingSalvageMission.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.buildingSalvageMission"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.BUILDING_SALVAGE;
	
	/** Mission phases. */
	final public static MissionPhase PREPARE_SITE_PHASE = new MissionPhase(
			Msg.getString("Mission.phase.prepareSalvageSite")); //$NON-NLS-1$
	final public static MissionPhase SALVAGE_PHASE = new MissionPhase(Msg.getString("Mission.phase.salvage")); //$NON-NLS-1$

	// Number of mission members.
	public static final int MIN_PEOPLE = 3;
	private static final int MAX_PEOPLE = 10;

	public static int FIRST_AVAILABLE_SOL = 2000;
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
	private MarsClock sitePreparationStartTime;
	
	private List<GroundVehicle> constructionVehicles;
	private List<Integer> luvAttachmentParts;
	

	/**
	 * Constructor.
	 * 
	 * @param startingMember the mission member starting the mission.
	 */
	public BuildingSalvageMission(MissionMember startingMember) {
		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingMember, MIN_PEOPLE);

		// Set wear condition to 100% by default.
		wearCondition = 100D;

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
				constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			} else if (startingMember instanceof Robot) {
				Robot robot = (Robot) startingMember;
				constructionSkill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			}
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
						constructionStage.setSalvaging(true);
						logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
								, constructionStage.toString()));
					} else {
						logger.warning(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$
						addMissionStatus(MissionStatus.SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND);
						endMission();
					}
				}

				// Mark construction site as undergoing salvage.
				if (constructionStage != null) {
					constructionSite.setUndergoingSalvage(true);
				}
			} else {
				logger.warning(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
				addMissionStatus(MissionStatus.SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
				endMission();
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
		setPhaseDescription(Msg.getString("Mission.phase.prepareSalvageSite.description" //$NON-NLS-1$
				, settlement.getName()));
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
	public BuildingSalvageMission(Collection<MissionMember> members, Settlement settlement, Building building,
			ConstructionSite site, List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, missionType, (MissionMember) members.toArray()[0], 1);

		this.settlement = settlement;

		ConstructionManager manager = settlement.getConstructionManager();

		if (building != null) {
			// Create new salvage construction site.
			constructionSite = manager.createNewSalvageConstructionSite(building);
		} else if (site != null) {
			constructionSite = site;
		} else {
			logger.log(Level.WARNING, Msg.getString("BuildingSalvageMission.log.noSite")); //$NON-NLS-1$
//			throw new IllegalStateException(PREPARE_SITE_PHASE + Msg.getString("BuildingSalvageMission.log.noSite")); //$NON-NLS-1$
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
					constructionStage.setSalvaging(true);
					logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
							, constructionStage.toString()));
				} else {
					logger.warning(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$					
					addMissionStatus(MissionStatus.SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND);
					endMission();
				}
			}

			// Mark construction site as undergoing salvage.
			if (constructionStage != null)
				constructionSite.setUndergoingSalvage(true);
		} else {
			logger.warning(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
			addMissionStatus(MissionStatus.SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED);
			endMission();
		}

		// Mark site as undergoing salvage.
		if (constructionStage != null)
			constructionSite.setUndergoingSalvage(true);

		// Add mission members.
		// Iterator<Person> i = members.iterator();
		// while (i.hasNext())
		// i.next().getMind().setMission(this);

		Iterator<MissionMember> i = members.iterator();

		while (i.hasNext()) {
			// TODO Refactor.
			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;
				person.getMind().setMission(this);
			} else if (member instanceof Robot) {
//				Robot robot = (Robot) member;
//				robot.getBotMind().setMission(this);
			}
		}

		// Reserve construction vehicles and retrieve from inventory.
		constructionVehicles = vehicles;
		Iterator<GroundVehicle> j = vehicles.iterator();
		while (j.hasNext()) {
			GroundVehicle vehicle = j.next();
			vehicle.setReservedForMission(true);
			// Record the name of this vehicle in Mission
			setReservedVehicle(vehicle.getName());
			if (settlement.getInventory().containsUnit(vehicle)) {
				settlement.getInventory().retrieveUnit(vehicle);
			} else {
				logger.severe(Msg.getString("BuildingSalvageMission.log.cantRetrieve" //$NON-NLS-1$
						, vehicle.getName(), settlement.getName()));
				addMissionStatus(MissionStatus.CONSTRUCTION_VEHICLE_NOT_RETRIEVED);
				endMission();
//				endMission(Msg.getString("BuildingSalvageMission.log.constructionVehicle" //$NON-NLS-1$
//						, vehicle.getName()));
			}
		}

		// Retrieve construction LUV attachment parts.
		retrieveConstructionLUVParts();

		// Add phases.
		addPhase(PREPARE_SITE_PHASE);
		addPhase(SALVAGE_PHASE);

		// Set initial mission phase.
		setPhase(PREPARE_SITE_PHASE);
		setPhaseDescription(Msg.getString("Mission.phase.prepareSalvageSite.description", //$NON-NLS-1$
				settlement.getName()));
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
		Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingSalvageMission().iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
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
		Map<Building, Double> salvageBuildings = new HashMap<Building, Double>();
		Iterator<Building> i = settlement.getBuildingManager().getACopyOfBuildings().iterator();
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
	protected void determineNewPhase() {
		if (PREPARE_SITE_PHASE.equals(getPhase())) {
			setPhase(SALVAGE_PHASE);
			setPhaseDescription(Msg.getString("Mission.phase.salvage.description", //$NON-NLS-1$
					constructionStage.getInfo().getName()));
		} else if (SALVAGE_PHASE.equals(getPhase()))
			addMissionStatus(MissionStatus.BUILDING_SALVAGE_SUCCESSFULLY_ENDED);
			endMission();
//			endMission(Msg.getString("BuildingSalvageMission.log.success")); //$NON-NLS-1$
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (PREPARE_SITE_PHASE.equals(getPhase())) {
			prepareSitePhase(member);
		} else if (SALVAGE_PHASE.equals(getPhase())) {
			salvagePhase(member);
		}
	}
//	@Override
//	protected void performPhase(Robot robot) {
//		super.performPhase(robot);
//		if (PREPARE_SITE_PHASE.equals(getPhase()))
//			prepareSitePhase(robot);
//		else if (SALVAGE_PHASE.equals(getPhase()))
//			salvagePhase(robot);
//	}

	@Override
	public Settlement getAssociatedSettlement() {
		return settlement;
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> equipment = new HashMap<>(1);
		equipment.put(EquipmentType.convertName2ID(EVASuit.TYPE), getPeopleNumber());
		return equipment;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Number> resources = new HashMap<Integer, Number>(0);
		return resources;
	}

	/**
	 * Performs the prepare site phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void prepareSitePhase(MissionMember member) {
		prepareSitePhase();
	}

//	private void prepareSitePhase(Robot robot) {
//		prepareSitePhase();
//	}

	private void prepareSitePhase() {
		if (finishingExistingStage) {
			// If finishing uncompleted existing construction stage, skip resource loading.
			setPhaseEnded(true);
		}

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
	 * Performs the salvage phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void salvagePhase(MissionMember member) {

		// Anyone in the crew or a single person at the home settlement has a
		// dangerous illness, end phase.
		if (hasEmergency())
			setPhaseEnded(true);

		if (!getPhaseEnded()) {

			// 75% chance of assigning task, otherwise allow break.
			if (RandomUtil.lessThanRandPercent(75D)) {

				// Assign salvage building task to person.
				// TODO Refactor.
				if (member instanceof Person) {
					Person person = (Person) member;
					if (SalvageBuilding.canSalvage(person)) {
						assignTask(person,
								new SalvageBuilding(person, constructionStage, constructionSite, constructionVehicles));
					}
				}
			}
		}

		if (constructionStage.isComplete()) {
			setPhaseEnded(true);
			settlement.getConstructionManager().getConstructionValues().clearCache();

			// Remove salvaged construction stage from site.
			constructionSite.removeSalvagedStage(constructionStage);

			// Salvage construction parts from the stage.
			salvageConstructionParts();

			// Mark construction site as not undergoing salvage.
			constructionSite.setUndergoingSalvage(false);

			// Remove construction site if all salvaging complete.
			if (constructionStage.getInfo().getType().equals(ConstructionStageInfo.FOUNDATION)) {
				settlement.getConstructionManager().removeConstructionSite(constructionSite);
				settlement.fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_SALVAGE_EVENT, constructionSite);
				logger.log(Level.FINE, Msg.getString("BuildingSalvageMission.salvagedAt" //$NON-NLS-1$
						, settlement.getName()));
			}
		}
	}

	@Override
	public void endMission() {
		super.endMission();

		// Mark site as not undergoing salvage.
		if (constructionSite != null)
			constructionSite.setUndergoingSalvage(false);

		// Unreserve all mission construction vehicles.
		unreserveConstructionVehicles();
	}

	@Override
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = super.isCapableOfMission(member);

		if (result) {
			result = false;
			if (member.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {
				if (member.getSettlement() == settlement) {
					result = true;
				}
			}
		}

		return result;
	}
//	@Override
//	protected boolean isCapableOfMission(Robot robot) {
//		if (super.isCapableOfMission(robot)) {
//			if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
//				if (robot.getSettlement() == settlement)
//					return true;
//			}
//		}
//		return false;
//	}

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

			if (vehicle instanceof LightUtilityVehicle) {
				boolean usable = true;
				
				if (vehicle.isReserved())
					usable = false;

				usable = vehicle.isVehicleReady();
					
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
			constructionVehicles = new ArrayList<GroundVehicle>();
			Iterator<ConstructionVehicleType> j = constructionStage.getInfo().getVehicles().iterator();
			while (j.hasNext()) {
				ConstructionVehicleType vehicleType = j.next();
				// Only handle light utility vehicles for now.
				if (vehicleType.getVehicleClass() == LightUtilityVehicle.class) {
					LightUtilityVehicle luv = reserveLightUtilityVehicle();
					if (luv != null)
						constructionVehicles.add(luv);
					else {
						logger.warning(Msg.getString("BuildingSalvageMission.log.noLUV")); //$NON-NLS-1$
						addMissionStatus(MissionStatus.LUV_NOT_AVAILABLE);
						endMission();
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
						settlement.getInventory().retrieveItemResources(part, 1);
						if (vehicle != null) {
							vehicle.getInventory().storeItemResources(part, 1);
						}
						luvAttachmentParts.add(part);
					} catch (Exception e) {
						Part p = ItemResourceUtil.findItemResource(part);
						logger.log(Level.SEVERE, Msg.getString("BuildingSalvageMission.log.attachmentPart" //$NON-NLS-1$
								, p.getName()));
						addMissionStatus(MissionStatus.CONSTRUCTION_ATTACHMENT_PART_NOT_RETRIEVED);
						endMission();
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

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext() && (result == null)) {
			Vehicle vehicle = i.next();

			if (vehicle instanceof LightUtilityVehicle) {
				LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
				if ((luvTemp.haveStatusType(StatusType.PARKED) || luvTemp.haveStatusType(StatusType.GARAGED))
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
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
					} else {
						logger.severe(Msg.getString("BuildingSalvageMission.log.cantRetrieve" //$NON-NLS-1$
								, luvTemp.getName(), settlement.getName()));
						addMissionStatus(MissionStatus.LUV_NOT_RETRIEVED);
						endMission();
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
				Iterator<Integer> j = vInv.getAllItemResourcesStored().iterator();
				while (j.hasNext()) {
					Integer attachmentPart = j.next();
					int num = vInv.getItemResourceNum(attachmentPart);
					vInv.retrieveItemResources(attachmentPart, num);
					sInv.storeItemResources(attachmentPart, num);
				}
			}
		}
	}

	/**
	 * Gets a list of all construction vehicles used by the mission.
	 * 
	 * @return list of construction vehicles.
	 */
	public List<GroundVehicle> getConstructionVehicles() {
		return new ArrayList<GroundVehicle>(constructionVehicles);
	}

	/**
	 * Salvage construction parts from the stage.
	 * 
	 * @throws Exception if error salvaging construction parts.
	 */
	private void salvageConstructionParts() {

		double salvageChance = 50D;

		// Modify salvage chance based on building wear condition.
		// Note: if non-building construction stage, wear condition should be 100%.
		salvageChance = (wearCondition * .25D) + 25D;

		// Get average construction skill of mission members.
		double totalSkill = 0D;
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			// TODO Refactor
			int constructionSkill = 0;
			if (member instanceof Person) {
				constructionSkill = ((Person) member).getSkillManager()
						.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			} else if (member instanceof Robot) {
				constructionSkill = ((Robot) member).getSkillManager()
						.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			}
			totalSkill += constructionSkill;
		}
		double averageSkill = totalSkill / getPeopleNumber();

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
				double capacity = settlement.getInventory().getGeneralCapacity();
				if (mass <= capacity)
					settlement.getInventory().storeItemResources(part, salvagedNumber);

				// Recalculate settlement good value for salvaged part.
				settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(p), false);
			}
		}
	}

	@Override
	protected boolean hasEmergency() {
		boolean result = super.hasEmergency();

		// Cancel construction mission if there are any beacon vehicles within range
		// that need help.
		Vehicle vehicleTarget = null;
		Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(missionType, settlement, true);
		if (vehicle != null) {
			vehicleTarget = RescueSalvageVehicle.findBeaconVehicle(settlement, vehicle.getRange(missionType));
			if (vehicleTarget != null) {
				if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget)) {
					result = true;
				}
			}
		}

		return result;
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

	@Override
	public void destroy() {
		super.destroy();

		settlement = null;
		constructionSite = null;
		constructionStage = null;
		if (constructionVehicles != null)
			constructionVehicles.clear();
		constructionVehicles = null;
		sitePreparationStartTime = null;
		if (luvAttachmentParts != null)
			luvAttachmentParts.clear();
		luvAttachmentParts = null;
	}
}