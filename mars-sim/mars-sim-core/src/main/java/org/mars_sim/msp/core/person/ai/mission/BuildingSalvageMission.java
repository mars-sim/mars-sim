/**
 * Mars Simulation Project
 * BuildingSalvageMission.java
 * @version 3.07 2014-09-15
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
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.SalvageBuilding;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
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
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Mission for salvaging a construction stage at a building construction site.
 */
public class BuildingSalvageMission
extends Mission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingSalvageMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString(
			"Mission.description.salvageBuilding"); //$NON-NLS-1$

	/** Mission phases. */
	final public static MissionPhase PREPARE_SITE_PHASE = new MissionPhase(Msg.getString(
			"Mission.phase.prepareSalvageSite")); //$NON-NLS-1$
	final public static MissionPhase SALVAGE_PHASE = new MissionPhase(Msg.getString(
			"Mission.phase.salvage")); //$NON-NLS-1$

	// Number of mission members.
	public static final int MIN_PEOPLE = 3;
	private static final int MAX_PEOPLE = 10;

	/** Time (millisols) required to prepare construction site for salvaging stage. */
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
	 * Constructor.
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
			if (availableSuitNum < getMissionCapacity()) {
				setMissionCapacity(availableSuitNum);
			}

			// Recruit additional people to mission.
			recruitPeopleForMission(startingPerson);

			// Determine construction site and stage.
			int constructionSkill = startingPerson.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
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
				Building salvageBuilding = determineBuildingToSalvage(
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
							Msg.getString("BuildingSalvageMission.log.noBuildingFound")); //$NON-NLS-1$
				}
			}

			// Prepare salvage construction site.
			if (constructionSite != null) {

				// Determine new stage to work on.
				if (constructionSite.hasUnfinishedStage()) {
					constructionStage = constructionSite
							.getCurrentConstructionStage();
					finishingExistingStage = true;
					logger.log(Level.INFO, Msg.getString("BuildingSalvageMission.log.continueAt" //$NON-NLS-1$
							,settlement.getName()));
				} else {
					constructionStage = constructionSite
							.getCurrentConstructionStage();
					if (constructionStage != null) {
						constructionStage.setCompletedWorkTime(0D);
						constructionStage.setSalvaging(true);
						logger.log(Level.INFO, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
								,constructionStage.toString()));
					} else {
						endMission(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$
					}
				}

				// Mark construction site as undergoing salvage.
				if (constructionStage != null) {
					constructionSite.setUndergoingSalvage(true);
				}
			} else {
				endMission(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
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
		setPhaseDescription(Msg.getString(
				"Mission.phase.prepareSalvageSite.description" //$NON-NLS-1$ 
				,settlement.getName()));
	}
	public BuildingSalvageMission(Robot startingRobot) {
		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, startingRobot, MIN_PEOPLE);

		// Set wear condition to 100% by default.
		wearCondition = 100D;

		if (!isDone()) {
			// Sets the settlement.
			settlement = startingRobot.getSettlement();

			// Sets the mission capacity.
			setMissionCapacity(MAX_PEOPLE);
			int availableSuitNum = Mission
					.getNumberAvailableEVASuitsAtSettlement(settlement);
			if (availableSuitNum < getMissionCapacity()) {
				setMissionCapacity(availableSuitNum);
			}

			// Recruit additional Robots to mission.
			recruitRobotsForMission(startingRobot);

			// Determine construction site and stage.
			int constructionSkill = startingRobot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
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
				Building salvageBuilding = determineBuildingToSalvage(
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
							Msg.getString("BuildingSalvageMission.log.noBuildingFound")); //$NON-NLS-1$
				}
			}

			// Prepare salvage construction site.
			if (constructionSite != null) {

				// Determine new stage to work on.
				if (constructionSite.hasUnfinishedStage()) {
					constructionStage = constructionSite
							.getCurrentConstructionStage();
					finishingExistingStage = true;
					logger.log(Level.INFO, Msg.getString("BuildingSalvageMission.log.continueAt" //$NON-NLS-1$
							,settlement.getName()));
				} else {
					constructionStage = constructionSite
							.getCurrentConstructionStage();
					if (constructionStage != null) {
						constructionStage.setCompletedWorkTime(0D);
						constructionStage.setSalvaging(true);
						logger.log(Level.INFO, Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
								,constructionStage.toString()));
					} else {
						endMission(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$
					}
				}

				// Mark construction site as undergoing salvage.
				if (constructionStage != null) {
					constructionSite.setUndergoingSalvage(true);
				}
			} else {
				endMission(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
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
		setPhaseDescription(Msg.getString(
				"Mission.phase.prepareSalvageSite.description" //$NON-NLS-1$ 
				,settlement.getName()));
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
	public BuildingSalvageMission(Collection<Unit> members,
			Settlement settlement, Building building, ConstructionSite site,
			List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, (Unit) members.toArray()[0], 1);

		this.settlement = settlement;

     	Person person = null;
    	Robot robot = null;
    	
		ConstructionManager manager = settlement.getConstructionManager();

		if (building != null) {
			// Create new salvage construction site.
			constructionSite = manager
					.createNewSalvageConstructionSite(building);
		} else if (site != null) {
			constructionSite = site;
		} else {
			logger.log(Level.SEVERE,
					Msg.getString("BuildingSalvageMission.log.noSite")); //$NON-NLS-1$
			throw new IllegalStateException(PREPARE_SITE_PHASE
					+ Msg.getString("BuildingSalvageMission.log.noSite")); //$NON-NLS-1$
		}

		// Prepare salvage construction site.
		if (constructionSite != null) {

			// Determine new stage to work on.
			if (constructionSite.hasUnfinishedStage()) {
				constructionStage = constructionSite
						.getCurrentConstructionStage();
				finishingExistingStage = true;
				logger.log(Level.INFO, Msg.getString("BuildingSalvageMission.log.continueAt" //$NON-NLS-1$
						,settlement.getName()));
			} else {
				constructionStage = constructionSite
						.getCurrentConstructionStage();
				if (constructionStage != null) {
					constructionStage.setCompletedWorkTime(0D);
					constructionStage.setSalvaging(true);
					logger.log(Level.INFO,
							Msg.getString("BuildingSalvageMission.log.startStage" //$NON-NLS-1$
									,constructionStage.toString()));
				} else
					endMission(Msg.getString("BuildingSalvageMission.log.stageNotFound")); //$NON-NLS-1$
			}

			// Mark construction site as undergoing salvage.
			if (constructionStage != null)
				constructionSite.setUndergoingSalvage(true);
		} else {
			endMission(Msg.getString("BuildingSalvageMission.log.siteNotFound")); //$NON-NLS-1$
		}

		// Mark site as undergoing salvage.
		if (constructionStage != null)
			constructionSite.setUndergoingSalvage(true);

		// Add mission members.
		//Iterator<Person> i = members.iterator();
		//while (i.hasNext())
		//	i.next().getMind().setMission(this);

		Iterator<Unit> i = members.iterator();

        while (i.hasNext()) {
         	                    	
	        Unit unit = i.next();
	        if (unit instanceof Person) {
	        	person = (Person) unit;
	        	person.getMind().setMission(this);
	        }
	        else if (unit instanceof Robot) {
	        	robot = (Robot) unit;
	        	robot.getBotMind().setMission(this);
	        }     
        }
		
		// Reserve construction vehicles and retrieve from inventory.
		constructionVehicles = vehicles;
		Iterator<GroundVehicle> j = vehicles.iterator();
		while (j.hasNext()) {
			GroundVehicle vehicle = j.next();
			vehicle.setReservedForMission(true);
			if (settlement.getInventory().containsUnit(vehicle)) {
				settlement.getInventory().retrieveUnit(vehicle);
			}
			else {
				logger.severe(Msg.getString(
						"BuildingSalvageMission.log.cantRetrieve" //$NON-NLS-1$
						,vehicle.getName()
						,settlement.getName()));
				endMission(Msg.getString(
						"BuildingSalvageMission.log.constructionVehicle" //$NON-NLS-1$
						,vehicle.getName()));
			}
		}

		// Retrieve construction LUV attachment parts.
		retrieveConstructionLUVParts();

		// Add phases.
		addPhase(PREPARE_SITE_PHASE);
		addPhase(SALVAGE_PHASE);

		// Set initial mission phase.
		setPhase(PREPARE_SITE_PHASE);
		setPhaseDescription(Msg.getString(
				"Mission.phase.prepareSalvageSite.description", //$NON-NLS-1$ 
				settlement.getName()));
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
	 * Determines a random profitable building at the settlement to salvage.
	 * @param settlement the settlement.
	 * @param constructionSkill the architect's construction skill.
	 * @return building to salvage or null in none found.
	 * @throws Exception if error determining building.
	 */
	private Building determineBuildingToSalvage(Settlement settlement, 
			int constructionSkill) {
		Building result = null;

		SalvageValues values = settlement.getConstructionManager().getSalvageValues();
		Map<Building, Double> salvageBuildings = new HashMap<Building, Double>();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings()
				.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			double salvageProfit = values.getNewBuildingSalvageProfit(building,
					constructionSkill);
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
			setPhaseDescription(Msg.getString(
					"Mission.phase.salvage.description", //$NON-NLS-1$
					constructionStage.getInfo().getName()));
		} else if (SALVAGE_PHASE.equals(getPhase()))
			endMission(Msg.getString("BuildingSalvageMission.log.success")); //$NON-NLS-1$
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
	protected void performPhase(Robot robot) {
		super.performPhase(robot);
		if (PREPARE_SITE_PHASE.equals(getPhase()))
			prepareSitePhase(robot);
		else if (SALVAGE_PHASE.equals(getPhase()))
			salvagePhase(robot);
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
			boolean useBuffer) {
		Map<Resource, Number> resources = new HashMap<Resource, Number>(0);
		return resources;
	}

	/**
	 * Performs the prepare site phase.
	 * @param person the person performing the phase.
	 * @throws MissionException if error performing the phase.
	 */
	private void prepareSitePhase(Person person) {
		prepareSitePhase();
	}

	private void prepareSitePhase(Robot robot) {
		prepareSitePhase();
	}
	
	private void prepareSitePhase() {		
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

				// Assign salvage building task to person.
				if (SalvageBuilding.canSalvage(person)) {
					assignTask(person, new SalvageBuilding(person,
							constructionStage, constructionSite, 
							constructionVehicles));
				}
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
						UnitEventType.FINISH_SALVAGE_EVENT,
						constructionSite);
				logger.log(Level.INFO,
						Msg.getString(
								"BuildingSalvageMission.salvagedAt" //$NON-NLS-1$
								,settlement.getName()));
			}
		}
	}
	private void salvagePhase(Robot robot) {

		// Anyone in the crew or a single person at the home settlement has a
		// dangerous illness, end phase.
		if (hasEmergency())
			setPhaseEnded(true);

		if (!getPhaseEnded()) {

			// 75% chance of assigning task, otherwise allow break.
			if (RandomUtil.lessThanRandPercent(75D)) {

				// Assign salvage building task to robot.
				if (SalvageBuilding.canSalvage(robot)) {
					assignTask(robot, new SalvageBuilding(robot,
							constructionStage, constructionSite, 
							constructionVehicles));
				}
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
						UnitEventType.FINISH_SALVAGE_EVENT,
						constructionSite);
				logger.log(Level.INFO,
						Msg.getString(
								"BuildingSalvageMission.salvagedAt" //$NON-NLS-1$
								,settlement.getName()));
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
	}

	@Override
	protected boolean isCapableOfMission(Person person) {
		if (super.isCapableOfMission(person)) {
			if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
				if (person.getSettlement() == settlement)
					return true;
			}
		}
		return false;
	}
	@Override
	protected boolean isCapableOfMission(Robot robot) {
		if (super.isCapableOfMission(robot)) {
			if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
				if (robot.getSettlement() == settlement)
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
	public static boolean isLUVAvailable(Settlement settlement) {
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
						endMission(Msg.getString("BuildingSalvageMission.log.noLUV")); //$NON-NLS-1$
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
			Iterator<ConstructionVehicleType> k = constructionStage.getInfo()
					.getVehicles().iterator();
			while (k.hasNext()) {
				Vehicle vehicle = null;
				if (constructionVehicles.size() > vehicleIndex) {
					vehicle = constructionVehicles.get(vehicleIndex);
				}

				Iterator<Part> l = k.next().getAttachmentParts().iterator();
				while (l.hasNext()) {
					Part part = l.next();
					try {
						settlement.getInventory()
						.retrieveItemResources(part, 1);
						if (vehicle != null) {
							vehicle.getInventory().storeItemResources(part, 1);
						}
						luvAttachmentParts.add(part);
					} catch (Exception e) {
						logger.log(Level.SEVERE,
								Msg.getString(
										"BuildingSalvageMission.log.attachmentPart" //$NON-NLS-1$
										,part.getName()));
						endMission(Msg.getString(
								"BuildingSalvageMission.log.attachmentPart" //$NON-NLS-1$
								,part.getName()));
					}
				}
				vehicleIndex++;
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
					}
					else {
						logger.severe(Msg.getString(
								"BuildingSalvageMission.log.cantRetrieve" //$NON-NLS-1$
								,luvTemp.getName()
								,settlement.getName()));
						endMission(Msg.getString(
								"BuildingSalvageMission.log.constructionVehicle" //$NON-NLS-1$
								,luvTemp.getName()));
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
			int constructionSkill = i.next().getMind().getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
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