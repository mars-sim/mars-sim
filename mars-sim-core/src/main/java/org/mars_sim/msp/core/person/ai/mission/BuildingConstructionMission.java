/**
 * Mars Simulation Project
 * BuildingConstructionMission.java
 * @version 3.1.0 2017-10-14
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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Mission for construction a stage for a settlement building. TODO externalize
 * strings
 */
public class BuildingConstructionMission extends Mission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingConstructionMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.buildingConstructionMission"); //$NON-NLS-1$

	/** Mission phases. */
	final public static MissionPhase SELECT_SITE_PHASE = new MissionPhase(
			Msg.getString("Mission.phase.selectConstructionSite")); //$NON-NLS-1$
	final public static MissionPhase PREPARE_SITE_PHASE = new MissionPhase(
			Msg.getString("Mission.phase.prepareConstructionSite")); //$NON-NLS-1$
	final public static MissionPhase CONSTRUCTION_PHASE = new MissionPhase(Msg.getString("Mission.phase.construction")); //$NON-NLS-1$

	// Number of mission members.
	public static final int MIN_PEOPLE = 3;
	public static final int MAX_PEOPLE = 10;

	public static int FIRST_AVAILABLE_SOL = 1000;

	/** Time (millisols) required to prepare construction site for stage. */
	public static final double SITE_PREPARE_TIME = 100D;

	// Default distance between buildings for construction.
	public static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = 5D;

	public static final double DEFAULT_NONINHABITABLE_BUILDING_DISTANCE = 2D;

	public static final double DEFAULT_HAB_BUILDING_DISTANCE = 5D;

	public static final double DEFAULT_SMALL_GREENHOUSE_DISTANCE = 5D;

	public static final double DEFAULT_LARGE_GREENHOUSE_DISTANCE = 5D;

	public static final double DEFAULT_RECT_DISTANCE = 5D;

	// Default width and length for variable size buildings if not otherwise
	// determined.
	public static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 10D;

	public static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 10D;

	/** Minimum length of a building connector (meters). */
	public static final double MINIMUM_CONNECTOR_LENGTH = 1D;

	// Data members
	private Settlement settlement;
	private ConstructionSite site;
	private ConstructionStage stage;

	private List<GroundVehicle> constructionVehicles;
	private Collection<MissionMember> members;// = constructionSite.getMembers();
	private List<Integer> luvAttachmentParts;

	private static MarsClock sitePreparationStartTime;

	/**
	 * Constructor 1 for Case 1
	 *
	 * @param startingMember the mission member starting the mission.
	 */
	public BuildingConstructionMission(MissionMember startingMember) {
		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, startingMember, MIN_PEOPLE);
		// logger.info("BuildingConstructionMission's constructor is in " +
		// Thread.currentThread().getName() + " Thread");

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
				// logger.info("The starting member is " + person);
				// person.setMission(this);
				constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
				person.getMind().setMission(this);
			}
//            else if (startingMember instanceof Robot) {
//                Robot robot = (Robot) startingMember;
//                //robot.setMission(this);
//                constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
//                robot.getBotMind().setMission(this);
//            }

			initCase1Step1(constructionSkill);
		}

		if (!isDone()) {
			// Set initial mission phase.
			if (Simulation.getUseGUI()) {
				// Add phases.
				addPhase(SELECT_SITE_PHASE);
				addPhase(PREPARE_SITE_PHASE);
				addPhase(CONSTRUCTION_PHASE);

				setPhase(SELECT_SITE_PHASE);
				setPhaseDescription(Msg.getString("Mission.phase.selectConstructionSite.description" //$NON-NLS-1$
						, settlement.getName()));
			} else {

				// Reserve construction vehicles.
				reserveConstructionVehicles();
				// Retrieve construction LUV attachment parts.
				retrieveConstructionLUVParts();

				addPhase(PREPARE_SITE_PHASE);
				addPhase(CONSTRUCTION_PHASE);

				setPhase(PREPARE_SITE_PHASE);
				setPhaseDescription(Msg.getString("Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
						, settlement.getName()));
			}
		}

	}

	public void initCase1Step1(int skill) {
		// a settler initiates this mission
		logger.info("Calling initCase1Step1()");
		ConstructionManager manager = settlement.getConstructionManager();
		ConstructionValues values = manager.getConstructionValues();
		values.clearCache();
		double existingSitesProfit = values.getAllConstructionSitesProfit(skill);
		double newSiteProfit = values.getNewConstructionSiteProfit(skill);
		ConstructionStageInfo info = null;

		if (existingSitesProfit > newSiteProfit) {

			// Determine which existing construction site to work on.
			double topSiteProfit = 0D;
			Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingConstructionMission().iterator();
			while (i.hasNext()) {
				ConstructionSite _site = i.next();
				double siteProfit = values.getConstructionSiteProfit(_site, skill);
				if (siteProfit > topSiteProfit) {
					this.site = _site;
					topSiteProfit = siteProfit;
				}
			}
		}

		else if (newSiteProfit > 0D) {

			if (Simulation.getUseGUI()) {
				logger.info(
						"Case 1 : Construction initiated by a starting member. Building picked by settlement. Site to be 'automatically' picked.");
				// if GUI is in use
				site = new ConstructionSite(settlement);
				site.setSkill(skill);
				site.setSitePicked(false);
				site.setManual(false);
				// constructionSite.setStageInfo(stageInfo);
				manager.getSites().add(site);
				settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_WIZARD_EVENT, this);

			} else {
				// if GUI is NOT in use
				// Create new site.
				site = manager.createNewConstructionSite();

				// Determine construction site location and facing.
				info = determineNewStageInfo(site, skill);

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

					positionNewSite(site, info, skill);

					logger.log(Level.INFO, "New construction site added at " + settlement.getName());
				} else
					System.out.println("New construction stage could not be determined.");
				// endMission("New construction stage could not be determined.");

				initCase1Step2(site, info, skill, values);
				// init_case_1_step_3();

			}

		}

	}

	public void initCase1Step2(ConstructionSite m_site, ConstructionStageInfo stageInfo, int constructionSkill,
			ConstructionValues values) {
		this.site = m_site;
		logger.info("Calling initCase1Step2()");

		// System.out.println("constructionSite is " + constructionSite.getDescription()
		//// + " x is " + constructionSite.getXLocation()
		// + " y is " + constructionSite.getYLocation());
		// System.out.println("stageInfo is " + stageInfo.toString());

		if (site != null) {

			// Determine new stage to work on.
			if (site.hasUnfinishedStage()) {
				stage = site.getCurrentConstructionStage();
				logger.log(Level.INFO, "Continuing work on existing site at " + settlement.getName());
			} else {

				if (stageInfo == null) {
					stageInfo = determineNewStageInfo(site, constructionSkill);
				}

				if (stageInfo != null) {
					stage = new ConstructionStage(stageInfo, site);
					site.addNewStage(stage);
					values.clearCache();
					logger.log(Level.INFO, "Starting new construction stage: " + stage);
				} else {
					endMission("New construction stage could not be determined.");
				}
			}

			// Mark site as undergoing construction.
			if (stage != null) {
				site.setUndergoingConstruction(true);
			}
		} else {
			System.out.println("Construction site could not be found or created.");
			endMission("Construction site could not be found or created.");
		}

	}

	public void initCase1Step3() {
		// Reserve construction vehicles.
		reserveConstructionVehicles();
		// Retrieve construction LUV attachment parts.
		retrieveConstructionLUVParts();

		addPhase(PREPARE_SITE_PHASE);
		addPhase(CONSTRUCTION_PHASE);

	}

	/**
	 * Constructor 2 for Case 2 and Case 3
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
	public BuildingConstructionMission(Collection<MissionMember> members, Settlement settlement,
			ConstructionSite no_site, ConstructionStageInfo stageInfo, double xLoc, double yLoc, double facing,
			List<GroundVehicle> vehicles) {

		// Use Mission constructor.
		super(DEFAULT_DESCRIPTION, (MissionMember) members.toArray()[0], 1);

		// this.site = no_site;
		this.members = members;
		this.settlement = settlement;
		this.constructionVehicles = vehicles;

		int bestConstructionSkill = 0;

		setMissionCapacity(MAX_PEOPLE);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(settlement);
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		Iterator<MissionMember> i = members.iterator();

		while (i.hasNext()) {

			int constructionSkill = 0;

			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;
				// person.getMind().setMission(this);
				constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			}
//	        else if (member instanceof Robot) {
//	        	Robot robot = (Robot) member;
//	        	//robot.getBotMind().setMission(this);
//	        	constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(
//        				SkillType.CONSTRUCTION);
//	        }

			if (constructionSkill > bestConstructionSkill) {
				bestConstructionSkill = constructionSkill;
			}
		}

		ConstructionManager manager = settlement.getConstructionManager();

		if (site != null) {
			// site already selected
			if (Simulation.getUseGUI()) {
				// if GUI is in use
				logger.info("Case 2 : the site has been picked and the construction is started by users");
				logger.log(Level.INFO, "New construction site added at " + settlement.getName());
				site.setSkill(bestConstructionSkill);
				site.setSitePicked(true);
				site.setStageInfo(stageInfo);
				site.setManual(true);
				manager.getSites().add(no_site);

				// Note : Should NOT invoke construction wizard to allow user to pick the site
				// again.
				// settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_WIZARD_EVENT,
				// this);

				if (stageInfo != null) {
					logger.info("Case 2. stageInfo is " + stageInfo.getName());
				} else {
					logger.info("Case 2. new construction stageInfo could not be determined.");
				}

				initialize(site, stageInfo);
				if (!isDone()) {
					// Reserve construction vehicles.
					// reserveConstructionVehicles();
					// Retrieve construction LUV attachment parts.
					retrieveConstructionLUVParts();
					startPhase();
				}

			}
		}

		else {
			// site has NOT been selected
			logger.info("Case 3 : site has NOT been picked yet and the construction is manually started by users");

			// boolean check = false;
			// if (check) {
			if (Simulation.getUseGUI()) {
				// if GUI is in use
				site = new ConstructionSite(settlement);// , manager);
				site.setSkill(bestConstructionSkill);
				site.setSitePicked(false);
				site.setManual(true);
				// constructionSite.setMembers(members);
				// constructionSite.setVehicles(vehicles);
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

				manager.getSites().add(site);

				settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_WIZARD_EVENT, this);

			} else { // if GUI is NOT in use

				site = manager.createNewConstructionSite();

				if (site != null) {
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

					positionNewSite(site, stageInfo, bestConstructionSkill);
				}

				else
					endMission("Construction site could not be created.");

				initialize(site, stageInfo);// , vehicles, members);

				if (!isDone()) {
					// Reserve construction vehicles.
					// reserveConstructionVehicles();
					// Retrieve construction LUV attachment parts.
					retrieveConstructionLUVParts();
					startPhase();
				}
			}

		}

	}

	public void initialize(ConstructionSite modSite, ConstructionStageInfo info) {
		this.site = modSite;
		logger.info("stageInfo is " + info.toString());
		// ConstructionStageInfo stageInfo = info;
		// System.out.println("x is " + constructionSite.getXLocation()
		// + " y is " + constructionSite.getYLocation()
		// + " w is " + constructionSite.getWidth()
		// + " l is " + constructionSite.getLength()
		// + " f is " + constructionSite.getFacing());

		if (site.hasUnfinishedStage()) {
			stage = site.getCurrentConstructionStage();
			logger.log(Level.INFO, "Using existing construction stage: " + stage);
		} else {
			stage = new ConstructionStage(info, site);
			logger.log(Level.INFO, "Starting new construction stage: " + stage);
			try {
				site.addNewStage(stage);
			} catch (Exception e) {
				endMission("Construction stage could not be created.");
			}
		}

		// Mark site as undergoing construction.
		if (stage != null) {
			site.setUndergoingConstruction(true);
		}

	}

	public void retrieveVehicles() {
		// Retrieve construction vehicles.
		Iterator<GroundVehicle> j = constructionVehicles.iterator();
		while (j.hasNext()) {
			GroundVehicle vehicle = j.next();
			vehicle.setReservedForMission(true);
			if (settlement.getInventory().containsUnit(vehicle)) {
				settlement.getInventory().retrieveUnit(vehicle);
			} else {
				logger.severe("Unable to retrieve " + vehicle.getName() + " cannot be retrieved from "
						+ settlement.getName() + " inventory.");
				endMission("Construction vehicle " + vehicle.getName()
						+ " could not be retrieved from settlement inventory.");
			}
		}
	}

	public void setMembers() {
		// Add mission members.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;
				person.getMind().setMission(this);
				person.setShiftType(ShiftType.ON_CALL);
			}
			// else if (member instanceof Robot) {
			// Robot robot = (Robot) member;
			// robot.getBotMind().setMission(this);
			// }
		}
	}

	public void startPhase() {
		// Add phases.
		addPhase(PREPARE_SITE_PHASE);
		addPhase(CONSTRUCTION_PHASE);

		// Set initial mission phase.
		setPhase(PREPARE_SITE_PHASE);
		setPhaseDescription(Msg.getString("Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
				, settlement.getName()));

	}

	/**
	 * Reserve construction vehicles for the mission.
	 */
	public void reserveConstructionVehicles() {
		logger.info("calling reserveConstructionVehicles()");
		if (stage != null) {
			constructionVehicles = new ArrayList<GroundVehicle>();
			Iterator<ConstructionVehicleType> j = stage.getInfo().getVehicles().iterator();
			while (j.hasNext()) {
				ConstructionVehicleType vehicleType = j.next();
				// Only handle light utility vehicles for now.
				if (vehicleType.getVehicleClass() == LightUtilityVehicle.class) {
					LightUtilityVehicle luv = reserveLightUtilityVehicle();
					if (luv != null) {
						constructionVehicles.add(luv);
					} else {
						endMission("Light utility vehicle not available.");
						System.out
								.println("calling reserveConstructionVehicles() : Light utility vehicle not available");
					}
				}
			}
		}
	}

	/**
	 * Retrieve LUV attachment parts from the settlement.
	 */
	public void retrieveConstructionLUVParts() {
		logger.info("calling retrieveConstructionLUVParts()");
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
						settlement.getInventory().retrieveItemResources(part, 1);
						if (vehicle != null) {
							vehicle.getInventory().storeItemResources(part, 1);
						}
						luvAttachmentParts.add(part);
					} catch (Exception e) {
						Part p = ItemResourceUtil.findItemResource(part);
						logger.log(Level.SEVERE, "Error retrieving attachment part " + p.getName());
						endMission("Construction attachment part " + p.getName() + " could not be retrieved.");
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
	public ConstructionStageInfo determineNewStageInfo(ConstructionSite site, int skill) {
		ConstructionStageInfo result = null;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		Map<ConstructionStageInfo, Double> stageProfits = values.getNewConstructionStageProfits(site, skill);
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

				if (vehicle.getStatus() != StatusType.PARKED && vehicle.getStatus() != StatusType.GARAGED)
					usable = false;

				if (((Crewable) vehicle).getCrewNum() > 0)// || ((Crewable) vehicle).getRobotCrewNum() > 0)
					usable = false;

				if (usable)
					result = true;

			}
		}

		return result;
	}

	@Override
	protected void determineNewPhase() {
		// System.out.println("starting determineNewPhase()");
		if (SELECT_SITE_PHASE.equals(getPhase())) {
			setPhase(PREPARE_SITE_PHASE);
			setPhaseDescription(Msg.getString("Mission.phase.prepareConstructionSite.description" //$NON-NLS-1$
					, stage.getInfo().getName()));
		} else if (PREPARE_SITE_PHASE.equals(getPhase())) {
			setPhase(CONSTRUCTION_PHASE);
			setPhaseDescription(Msg.getString("Mission.phase.construction.description" //$NON-NLS-1$
					, stage.getInfo().getName()));
		} else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			endMission(CONSTRUCTION_ENDED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (SELECT_SITE_PHASE.equals(getPhase())) {
			// System.out.println("performPhase() : in SELECT_SITE_PHASE");
			selectSitePhase(member);
		} else if (PREPARE_SITE_PHASE.equals(getPhase())) {
			// System.out.println("performPhase() : in PREPARE_SITE_PHASE");
			prepareSitePhase(member);
		} else if (CONSTRUCTION_PHASE.equals(getPhase())) {
			// System.out.println("performPhase() : in CONSTRUCTION_PHASE");
			constructionPhase(member);
		}
	}

	private void selectSitePhase(MissionMember member) {
		// System.out.println("at selectSitePhase(MissionMember member)");
		selectSitePhase();
		// waiting for the site to be selected by mars-simmers
	}

	public void selectSitePhase() {
		// Reserve construction vehicles.
		// reserveConstructionVehicles();
		// Retrieve construction LUV attachment parts.
		retrieveConstructionLUVParts();
		// System.out.println("starting selectSitePhase() and calling
		// setPhaseEnded(true);");
		setPhaseEnded(true);
	}

	/**
	 * Performs the prepare site phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void prepareSitePhase(MissionMember member) {
		prepareSitePhase();
	}

//    private void prepareSitePhase(Robot robot) {
//    	prepareSitePhase();
//    }

	private void prepareSitePhase() {
		// System.out.println("starting prepareSitePhase()");
		// Load all available materials needed for construction.
		if (!site.getStageInfo().getType().equals(ConstructionStageInfo.FOUNDATION))
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
	 * Load remaining required construction materials into site that are available
	 * at settlement inventory.
	 */
	private void loadAvailableConstructionMaterials() {
		// System.out.println("starting loadAvailableConstructionMaterials()");
		Inventory inv = settlement.getInventory();

		// Load amount resources.
		Iterator<Integer> i = stage.getRemainingResources().keySet().iterator();
		while (i.hasNext()) {
			Integer resource = i.next();
			double amountNeeded = stage.getRemainingResources().get(resource);

			inv.addAmountDemandTotalRequest(resource);

			double amountAvailable = inv.getAmountResourceStored(resource, false);

			// Load as much of the remaining resource as possible into the construction site
			// stage.
			double amountLoading = amountNeeded;
			if (amountAvailable < amountNeeded) {
				amountLoading = amountAvailable;
			}

			if (amountLoading > 0D) {
				inv.retrieveAmountResource(resource, amountLoading);
				stage.addResource(resource, amountLoading);

				inv.addAmountDemand(resource, amountLoading);
			}
		}

		// Load parts.
		Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
		while (j.hasNext()) {
			Integer part = j.next();
			int numberNeeded = stage.getRemainingParts().get(part);
			int numberAvailable = inv.getItemResourceNum(part);

			// Load as many remaining parts as possible into the construction site stage.
			int numberLoading = numberNeeded;
			if (numberAvailable < numberNeeded) {
				numberLoading = numberAvailable;
			}

			if (numberLoading > 0) {
				inv.retrieveItemResources(part, numberLoading);
				stage.addParts(part, numberLoading);
			}
		}
	}

	/**
	 * Performs the construction phase.
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void constructionPhase(MissionMember member) {

		// Anyone in the crew or a single person at the home settlement has a
		// dangerous illness, end phase.
		if (hasEmergency()) {
			setPhaseEnded(true);
		}

		// Load available construction materials into construction site.
		// if (site.getNextStageType().equals(ConstructionStageInfo.FRAME))
		if (!site.getStageInfo().getType().equals(ConstructionStageInfo.FOUNDATION))
			loadAvailableConstructionMaterials();

		// Check if further work can be done on construction stage.
		if (stage.getCompletableWorkTime() <= stage.getCompletedWorkTime()) {
			setPhaseEnded(true);
		}

		if (!getPhaseEnded()) {

			// 75% chance of assigning task, otherwise allow break.
			if (RandomUtil.lessThanRandPercent(75D)) {

				// Assign construction task to member.
				// TODO Refactor.
				if (member instanceof Person) {
					Person person = (Person) member;
					if (ConstructBuilding.canConstruct(person, site)) {
						assignTask(person, new ConstructBuilding(person, stage, site, constructionVehicles));
					}
				}
//                else if (member instanceof Robot) {
//                    Robot robot = (Robot) member;
//                    if (ConstructBuilding.canConstruct(robot, site)) {
//                        assignTask(robot, new ConstructBuilding(robot, stage,
//                                site, constructionVehicles));
//                    }
//                }
			}
		}

		constructionStageComplete();
	}

	public void constructionStageComplete() {
		if (stage.isComplete()) {
			setPhaseEnded(true);
			settlement.getConstructionManager().getConstructionValues().clearCache();

			// Construct building if all site construction complete.
			if (site.isAllConstructionComplete()) {

				Building building = site.createBuilding(settlement.getBuildingManager());
				settlement.getConstructionManager().removeConstructionSite(site);
				settlement.fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_BUILDING_EVENT, building);
				logger.log(Level.INFO,
						"New " + site.getBuildingName() + " building constructed at " + settlement.getName());

				// setDone(true);
			}

		}

	}

	@Override
	public void endMission(String reason) {
		// logger.info("BuildingConstructionMission's endMission() is in " +
		// Thread.currentThread().getName() + " Thread");
		// logger.info("reason : " + reason);

		// Mark site as not undergoing construction.
		if (site != null)
			site.setUndergoingConstruction(false);

		// Unreserve all LUV attachment parts for this mission.
		unreserveLUVparts();

		super.endMission(reason);
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return settlement;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {

		Map<Integer, Number> resources = new HashMap<Integer, Number>();

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
		equipment.put(EquipmentType.str2int(EVASuit.TYPE), getPeopleNumber());
		return equipment;
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
				if ((luvTemp.getStatus() == StatusType.PARKED || luvTemp.getStatus() == StatusType.GARAGED)
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
					result = luvTemp;
					luvTemp.setReservedForMission(true);

					// Place light utility vehicles at random location in construction site.
					Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site);
					Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(),
							relativeLocSite.getY(), site);
					luvTemp.setParkedLocation(settlementLocSite.getX(), settlementLocSite.getY(),
							RandomUtil.getRandomDouble(360D));

					if (settlement.getInventory().containsUnit(luvTemp)) {
						settlement.getInventory().retrieveUnit(luvTemp);
					} else {
						logger.severe("Unable to retrieve " + luvTemp.getName() + " cannot be retrieved from "
								+ settlement.getName() + " inventory.");
						endMission("Construction vehicle " + luvTemp.getName()
								+ " could not be retrieved from settlement inventory.");
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

	/*
	 * Unreserve and store back all LUV attachment parts in settlement.
	 */
	public void unreserveLUVparts() {

		if (luvAttachmentParts != null) {
			Iterator<Integer> i = luvAttachmentParts.iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				try {
					settlement.getInventory().storeItemResources(part, 1);
				} catch (Exception e) {
					logger.log(Level.SEVERE,
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
	public List<GroundVehicle> getConstructionVehicles() {
		// System.out.println("starting BuildingConstructionMission's
		// getConstructionVehicles()");
		// 2015-12-28 Added checking for null
		if (constructionVehicles != null) {
			if (!constructionVehicles.isEmpty())
				return new ArrayList<GroundVehicle>(constructionVehicles);
			else
				return null;
		} else
			return null;
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

	public boolean determineSite(String buildingType, double dist, ConstructionSite site) {
		boolean goodPosition = false;
		// Try to put building next to the same building type.
		List<Building> sameBuildings = site.getSettlement().getBuildingManager().getBuildingsOfSameType(buildingType);
		Collections.shuffle(sameBuildings);
		for (Building b : sameBuildings) {
			logger.info("Positioning next to " + b.getNickName());
			goodPosition = positionNextToBuilding(site, b, dist, false);
			if (goodPosition) {
				break;
			}
		}
		return goodPosition;
	}

	/**
	 * Determines and sets the position of a new construction site.
	 * 
	 * @param site                the new construction site.
	 * @param foundationStageInfo the site's foundation stage info.
	 * @param constructionSkill   the mission starter's construction skill.
	 */
	public void positionNewSite(ConstructionSite site, ConstructionStageInfo foundationStageInfo,
			int constructionSkill) {

		boolean goodPosition = false;

		Settlement s = site.getSettlement();
		// Use settlement's objective to determine the desired building type
		String buildingType = s.getObjectiveBuildingType();

		if (buildingType != null) {
			BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
			site.setWidth(buildingConfig.getWidth(buildingType));
			site.setLength(buildingConfig.getLength(buildingType));
			boolean isBuildingConnector = buildingConfig.hasBuildingConnection(buildingType);
			boolean hasLifeSupport = buildingConfig.hasLifeSupport(buildingType);

			if (isBuildingConnector) {
				// Try to find best location to connect two buildings.
				goodPosition = positionNewBuildingConnectorSite(site, buildingType);
			} else if (hasLifeSupport) {

				if (buildingType.toLowerCase().contains("inflatable greenhouse")) {
					goodPosition = determineSite(buildingType, DEFAULT_SMALL_GREENHOUSE_DISTANCE, site);
				}

				else if (buildingType.toLowerCase().contains("inground greenhouse")) {
					goodPosition = determineSite(buildingType, DEFAULT_SMALL_GREENHOUSE_DISTANCE, site);
				}

				else if (buildingType.toLowerCase().contains("large greenhouse")) {
					goodPosition = determineSite(buildingType, DEFAULT_LARGE_GREENHOUSE_DISTANCE, site);
				} else {
					// Try to put building next to another inhabitable building.
					List<Building> inhabitableBuildings = settlement.getBuildingManager()
							.getBuildings(FunctionType.LIFE_SUPPORT);
					Collections.shuffle(inhabitableBuildings);
					for (Building b : inhabitableBuildings) {
						// Match the floor area (e.g look more organize to put all 7m x 9m next to one
						// another)
						if (b.getFloorArea() == site.getWidth() * site.getLength()) {
							goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE,
									false);
							if (goodPosition) {
								break;
							}
						}
					}
				}
			} else {
				// Try to put building next to the same building type.
				goodPosition = determineSite(buildingType, DEFAULT_NONINHABITABLE_BUILDING_DISTANCE, site);
			}
		}

		else {
			// Determine preferred building type from foundation stage info.
			// buildingType = determinePreferredConstructedBuildingType(foundationStageInfo,
			// constructionSkill);

			logger.info("buildingType : " + buildingType);
			// Try to put building next to another inhabitable building.
			List<Building> inhabitableBuildings = s.getBuildingManager().getBuildings();// FunctionType.LIFE_SUPPORT);
			Collections.shuffle(inhabitableBuildings);
			for (Building b : inhabitableBuildings) {
				// Match the floor area (e.g look more organize to put all 7m x 9m next to one
				// another)
				if (b.getFloorArea() == site.getWidth() * site.getLength()) {
					goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
					if (goodPosition) {
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
			BuildingManager buildingManager = settlement.getBuildingManager();
			if (buildingManager.getNumBuilding() > 0) {
				for (int x = 10; !goodPosition; x += 10) {
					List<Building> allBuildings = buildingManager.getACopyOfBuildings();
					Collections.shuffle(allBuildings);
					for (Building b : allBuildings) {
						goodPosition = positionNextToBuilding(site, b, (double) x, false);
						if (goodPosition) {
							break;
						}
					}
				}
			} else {
				// If no buildings at settlement, position new construction site at 0,0 with
				// random facing.
				site.setXLocation(0D);
				site.setYLocation(0D);
				site.setFacing(RandomUtil.getRandomDouble(360D));
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
	private boolean positionNewBuildingConnectorSite(ConstructionSite site, String buildingType) {

		boolean result = false;

		BuildingManager manager = settlement.getBuildingManager();
		List<Building> inhabitableBuildings = manager.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(inhabitableBuildings);

		BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		int baseLevel = buildingConfig.getBaseLevel(buildingType);

		// Try to find a connection between an inhabitable building without access to
		// airlock and
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
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

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
					boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(startingBuilding,
							building);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
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
					boolean directlyConnected = (settlement.getBuildingConnectorManager()
							.getBuildingConnections(startingBuilding, building).size() > 0);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
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
	private boolean positionConnectorBetweenTwoBuildings(String buildingType, ConstructionSite site,
			Building firstBuilding, Building secondBuilding) {

		boolean result = false;

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<Line2D>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
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
	 * 
	 * @param point          the initial connector location.
	 * @param lineFacing     the facing of the connector line (degrees).
	 * @param building       the existing building being connected to.
	 * @param connectorWidth the width of the new connector.
	 * @return point adjusted location for connector end point.
	 */
	private Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building,
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
	private List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

		List<Point2D> result = new ArrayList<Point2D>(4);

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
	 * Determines the preferred construction building type for a given foundation.
	 * 
	 * @param foundationStageInfo the foundation stage info.
	 * @param constructionSkill   the mission starter's construction skill.
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
	 * 
	 * @param site               the new construction site.
	 * @param building           the existing building.
	 * @param separationDistance the separation distance (meters) from the building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return true if construction site could be positioned, false if not.
	 */
	private boolean positionNextToBuilding(ConstructionSite site, Building building, double separationDistance,
			boolean faceAway) {

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

			double distance = structureDistance + separationDistance;
			double radianDirection = Math.PI * direction / 180D;
			double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
			double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));

			// Check to see if proposed new site position intersects with any existing
			// buildings
			// or construction sites.
			if (settlement.getBuildingManager().isBuildingLocationOpen(rectCenterX, rectCenterY, site.getWidth(),
					site.getLength(), rectRotation, site)) {
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
		site = null;
		stage = null;
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