/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 3.1.0 2017-09-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.PersonalityTraitType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Engineer;
import org.mars_sim.msp.core.person.ai.job.Technician;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairEmergencyMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The MalfunctionManager class manages the current malfunctions in each of the
 * 6 types of units (namely, Building, BuildingKit, EVASuit, Robot,
 * MockBuilding, or Vehicle). Each building has its own MalfunctionManager
 */
// TODO: have one single MalfunctionUtility class to handle static methods that are common to all 6 types of units
public class MalfunctionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(MalfunctionManager.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Modifier for number of parts needed for a trip. */
	public static final double PARTS_NUMBER_MODIFIER = 7.5;
	/** Estimate number of broken parts per malfunctions */
	public static final double AVERAGE_NUM_MALFUNCTION = 2.5;
	/** Estimate number of broken parts per malfunctions for EVA suits. */
	public static final double AVERAGE_EVA_MALFUNCTION = 2.0;
	
	/** Initial estimate for malfunctions per orbit for an entity. */
	private static final double ESTIMATED_MALFUNCTIONS_PER_ORBIT = 10D;
	/** Initial estimate for maintenances per orbit for an entity. */
	private static final double ESTIMATED_MAINTENANCES_PER_ORBIT = 10D;
	/** Factor for chance of malfunction by time since last maintenance. */
	private static final double MAINTENANCE_MALFUNCTION_FACTOR = .000_000_001D;
	/** Factor for chance of malfunction due to wear condition. */
	private static final double WEAR_MALFUNCTION_FACTOR = 9D;
	/** Factor for chance of accident due to wear condition. */
	private static final double WEAR_ACCIDENT_FACTOR = 1D;

	private static final String OXYGEN = "Oxygen";
//	private static final String WATER = "Water";
//	private static final String PRESSURE = "Air Pressure";
//	private static final String TEMPERATURE = "Temperature";
	
	private static final String PARTS_FAILURE = "Parts Failure";// due to reliability";

	// Data members

	/** The number of malfunctions the entity has had so far. */
	private int numberMalfunctions;
	/** The number of times the entity has been maintained so far. */
	private int numberMaintenances;
	/** The number of orbits. */
	private int orbitCache = 0;
	/** Time passing (in millisols) since last maintenance on entity. */
	private double timeSinceLastMaintenance;
	/**
	 * Time (millisols) that entity has been actively used since last maintenance.
	 */
	private double effectiveTimeSinceLastMaintenance;
	/** The required work time for maintenance on entity. */
	private double maintenanceWorkTime;
	/** The completed. */
	private double maintenanceTimeCompleted;
	/**
	 * The percentage representing the malfunctionable's condition from wear and
	 * tear. 0% = worn out -> 100% = new condition.
	 */
	private double wearCondition;
	/** The max percentage between 0% to 100%.  */
//	private double maxCondition;
	/**
	 * The expected life time [in millisols] of active use before the malfunctionable
	 * is worn out.
	 */
	private final double wearLifeTime;

	// Life support modifiers.
	private double oxygenFlowModifier = 100D;
//	private double waterFlowModifier = 100D;
//	private double airPressureModifier = 100D;
//	private double temperatureModifier = 100D;

	/** The owning entity. */
	private Malfunctionable entity;

	private Unit unit;
	private EVASuit suit;
//	private Settlement settlement;
	private Building building;
	private Robot robot;
	private Equipment equipment;
	private Vehicle vehicle;
	
	/** The collection of affected scopes. */
	private Collection<String> scopes;
	/** The current malfunctions in the unit. */
	private Collection<Malfunction> malfunctions;
	/** The parts currently needed to maintain this entity. */
	private Map<Integer, Integer> partsNeededForMaintenance;

	// The static instances
	private static SimulationConfig simconfig = SimulationConfig.instance();
	private static Simulation sim = Simulation.instance();
	
	private static MasterClock masterClock;
	private static MarsClock currentTime;
	private static MedicalManager medic;
	private static MalfunctionFactory factory;
//	private static PartConfig partConfig;
	private static MalfunctionConfig malfunctionConfig;
	private static HistoricalEventManager eventManager;
	
	// NOTE : each building has its own MalfunctionManager

	/**
	 * Constructor.
	 * 
	 * @param entity              the malfunctionable entity.
	 * @param wearLifeTime        the expected life time (millisols) of active use
	 *                            before the entity is worn out.
	 * @param maintenanceWorkTime the amount of work time (millisols) required for
	 *                            maintenance.
	 */
	public MalfunctionManager(Malfunctionable entity, double wearLifeTime, double maintenanceWorkTime) {

		// Initialize data members
		this.entity = entity;
		
		if (entity.getUnit() instanceof Vehicle) {
			vehicle = (Vehicle)entity.getUnit();
		}

		else if (entity.getUnit() instanceof EVASuit) {
			suit = (EVASuit)entity.getUnit();
		}

		else if (entity.getUnit() instanceof Robot) {
			robot = (Robot)entity.getUnit();
		}

		else if (entity.getUnit() instanceof Building) {
			building = (Building)entity.getUnit();
		}
		
		else if (entity.getUnit() instanceof Equipment) {
			equipment = (Equipment)entity.getUnit();
		}
		else {
			unit = (Unit)entity.getUnit();
		}
			
		timeSinceLastMaintenance = 0D;
		effectiveTimeSinceLastMaintenance = 0D;
		scopes = new ArrayList<String>();
		malfunctions = new CopyOnWriteArrayList<Malfunction>();
		this.maintenanceWorkTime = maintenanceWorkTime;
		this.wearLifeTime = wearLifeTime;
		wearCondition = 100D;

		masterClock = sim.getMasterClock();
		if (masterClock != null)
			// Note that this if above is for maven test, or else NullPointerException
			currentTime = masterClock.getMarsClock();
			
		medic = sim.getMedicalManager();
//		partConfig = simconfig.getPartConfiguration();
		malfunctionConfig = simconfig.getMalfunctionConfiguration();
		factory = sim.getMalfunctionFactory();
		eventManager = sim.getEventManager();
	}

	/**
	 * Add a scope string of a system or a function to the manager.
	 * 
	 * @param scopeString
	 */
	public void addScopeString(String scopeString) {
		if ((scopeString != null) && !scopes.contains(scopeString.toLowerCase()))
			scopes.add(scopeString.toLowerCase());
//		System.out.println("ScopeString : " + scopeString.toLowerCase());
		// Update maintenance parts.
		determineNewMaintenanceParts();
	}
	
	public Collection<String> getScopes() {
		return scopes;
	}

	/**
	 * Checks if entity has a malfunction.
	 * 
	 * @return true if malfunction
	 */
	public boolean hasMalfunction() {
		return (malfunctions.size() > 0);
	}

	/**
	 * Checks if the entity has a given malfunction.
	 * 
	 * @return true if entity has malfunction
	 */
	public boolean hasMalfunction(Malfunction malfunction) {
		return malfunctions.contains(malfunction);
	}

	/**
	 * Checks if entity has any emergency malfunctions.
	 * 
	 * @return true if emergency malfunction
	 */
	public boolean hasEmergencyMalfunction() {
		boolean result = false;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isEmergencyRepairDone())
					return true;
			}
		}

		return result;
	}

	/**
	 * Checks if entity has any general malfunctions.
	 * 
	 * @return true if general malfunction
	 */
	public boolean hasGeneralMalfunction() {
		boolean result = false;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isGeneralRepairDone())
					return true;
			}
		}

		return result;
	}

	/**
	 * Checks if entity has any EVA malfunctions.
	 * 
	 * @return true if EVA malfunction
	 */
	public boolean hasEVAMalfunction() {
		boolean result = false;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isEVARepairDone())
					return true;
			}
		}

		return result;
	}

	/**
	 * Gets a list of the unit's current malfunctions.
	 * 
	 * @return malfunction list
	 */
	public List<Malfunction> getMalfunctions() {
		return new ArrayList<Malfunction>(malfunctions);
	}

	/**
	 * Gets the most serious malfunction the entity has.
	 * 
	 * @return malfunction
	 */
	public Malfunction getMostSeriousMalfunction() {

		Malfunction result = null;
		double highestSeverity = 0;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if ((malfunction.getSeverity() > highestSeverity) && !malfunction.isFixed()) {
					highestSeverity = malfunction.getSeverity();
					result = malfunction;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the most serious emergency malfunction the entity has.
	 * 
	 * @return malfunction
	 */
	public Malfunction getMostSeriousEmergencyMalfunction() {

		Malfunction result = null;
		double highestSeverity = 0D;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isEmergencyRepairDone()
						&& malfunction.getSeverity() > highestSeverity) {
					highestSeverity = malfunction.getSeverity();
					result = malfunction;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the most serious general malfunction the entity has.
	 * 
	 * @return malfunction
	 */
	public Malfunction getMostSeriousGeneralMalfunction() {

		Malfunction result = null;
		double highestSeverity = 0D;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isGeneralRepairDone()
						&& malfunction.getSeverity() > highestSeverity) {
					highestSeverity = malfunction.getSeverity();
					result = malfunction;
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of all general malfunctions sorted by highest severity first.
	 * 
	 * @return list of malfunctions.
	 */
	public List<Malfunction> getGeneralMalfunctions() {
		List<Malfunction> result = new ArrayList<Malfunction>();
		for (Malfunction malfunction : malfunctions) {
			if (!malfunction.isGeneralRepairDone())
				result.add(malfunction);
		}
		Collections.sort(result, new MalfunctionSeverityComparator());
		return result;
	}

	/**
	 * Gets a list of all emergency malfunctions sorted by highest severity first.
	 * 
	 * @return list of malfunctions.
	 */
	public List<Malfunction> getEmergencyMalfunctions() {
		List<Malfunction> result = new ArrayList<Malfunction>();
		for (Malfunction malfunction : malfunctions) {
			if (!malfunction.isEmergencyRepairDone())
				result.add(malfunction);
		}
		Collections.sort(result, new MalfunctionSeverityComparator());
		return result;
	}
	
	/**
	 * Gets the most serious EVA malfunction the entity has.
	 * 
	 * @return malfunction
	 */
	public Malfunction getMostSeriousEVAMalfunction() {

		Malfunction result = null;
		double highestSeverity = 0D;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isEVARepairDone()
						&& malfunction.getSeverity() > highestSeverity) {
					highestSeverity = malfunction.getSeverity();
					result = malfunction;
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of all EVA malfunctions sorted by highest severity first.
	 * 
	 * @return list of malfunctions.
	 */
	public List<Malfunction> getEVAMalfunctions() {
		List<Malfunction> result = new ArrayList<Malfunction>();
		for (Malfunction malfunction : malfunctions) {
			if (!malfunction.isEVARepairDone())
				result.add(malfunction);
		}
		Collections.sort(result, new MalfunctionSeverityComparator());
		return result;
	}

	/**
	 * Select a malfunction randomly to the unit, based on the affected scope.
	 * 
	 * @param actor
	 */
	private boolean selectMalfunction(Unit actor) {
		boolean result = false;
		// Clones a malfunction and determines repair parts
		Malfunction malfunction = factory.pickAMalfunction(scopes);
		if (malfunction != null) {
			addMalfunction(malfunction, true, actor);
			numberMalfunctions++;
			result = true;
		}

		return result;
	}

	/**
	 * Triggers a particular malfunction (used by VehicleChatUtils)
	 * 
	 * @param {@link Malfunction}
	 * @param value
	 */
	public void triggerMalfunction(Malfunction m, boolean registerEvent) {
		Malfunction malfunction = factory.determineRepairParts(m);
		if (malfunction != null) {
			addMalfunction(malfunction, registerEvent, null);
			numberMalfunctions++;
		}
	}
	
	
	/**
	 * Activates the malfunction (used by Meteorite Damage)
	 * 
	 * @param {@link Malfunction}
	 * @param value
	 */
	public void activateMalfunction(Malfunction m, boolean registerEvent) {
		Malfunction malfunction = factory.determineRepairParts(m);
		if (malfunction != null) {
			addMalfunction(malfunction, registerEvent, null);
			numberMalfunctions++;
		}
	}
	
	/**
	 * Adds a malfunction to the unit.
	 * 
	 * @param malfunction   the malfunction to add.
	 * @param registerEvent
	 * @param actor
	 */
	public void addMalfunction(Malfunction malfunction, boolean registerEvent, Unit actor) {
		malfunctions.add(malfunction);
		
		String malfunctionName = malfunction.getName();

		try {
			getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, malfunction);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		if (registerEvent) {

			if (!malfunction.getName().toLowerCase().contains("meteorite")) {
				// if it has NOTHING to do with meteorite impact
				registerAMalfunction(malfunction, actor);
			}

			else {
				// due to meteorite impact
				registerMeteoriteMalfunction(malfunction);
			}
			
		} 
//		else
//			return;

		// Register the failure of the Parts involved
		Map<Integer, Integer> parts = malfunction.getRepairParts();
		Set<Integer> partSet = parts.keySet();

		for (Integer p : partSet) {
			int num = parts.get(p);
	
			Inventory inv = entity.getUnit().getAssociatedSettlement().getInventory();
			// Add tracking demand
			inv.addItemDemandTotalRequest(p, num);
			inv.addItemDemand(p, num);
			
			// Compute the new reliability and failure rate for this malfunction
			int id = p;
			Part part = ItemResourceUtil.findItemResource(p);
			String part_name = part.getName();

			if (part_name.equalsIgnoreCase("decontamination kit") || part_name.equalsIgnoreCase("airleak patch")
					|| part_name.equalsIgnoreCase("fire extinguisher")) {
				// NOTE : they do NOT contribute to the malfunctions and are tools to fix the
				// malfunction and therefore do NOT need to change their reliability.
				return;
			}

			double old_rel = factory.getReliability(id);
			double old_prob = malfunctionConfig.getRepairPartProbability(malfunctionName, part_name);
			double old_failure = (100 - old_rel) * old_prob / 100D;
			double old_mal_probl_failure = malfunction.getProbability();
			double old_MTBF = factory.getMTBFs().get(id);

			// Increment the number of failure for this Part
			factory.setFailure(p, num);
			// Recompute the reliability of this Part
			factory.computeReliability(part);

			// String name = p.getName();
			double new_rel = factory.getReliability(id);
			double new_prob = malfunctionConfig.getRepairPartProbability(malfunctionName, part_name);
			double new_failure = (100 - new_rel) * new_prob / 100D;
			double new_mal_prob_failure = (old_mal_probl_failure + new_failure) / 2.0;
			double new_MTBF = factory.getMTBFs().get(id);
			
			logger.warning("          *** Part : " + part_name + " ***");
			
			logger.warning(" (1).   Reliability : " + addWhiteSpace(Math.round(old_rel * 1000.0) / 1000.0 + " %") 
							+ "  -->  " + Math.round(new_rel * 1000.0) / 1000.0 + " %");

			logger.warning(" (2).  Failure Rate : " + addWhiteSpace(Math.round(old_failure * 1000.0) / 1000.0 + " %") 
							+ "  -->  " + Math.round(new_failure * 1000.0) / 1000.0 + " %");

			logger.warning(" (3).          MTBF : " + addWhiteSpace(Math.round(old_MTBF * 1000.0) / 1000.0 + " hr") 
							+ "  -->  " + Math.round(new_MTBF * 1000.0) / 1000.0 + " hr");

			logger.warning("          *** Malfunction : " + malfunctionName + " ***");
			
			logger.warning(" (4).   Probability : " + addWhiteSpace(Math.round(old_mal_probl_failure * 1000.0) / 1000.0 + " %") 
							+ "  -->  " + Math.round(new_mal_prob_failure * 1000.0) / 1000.0 + " %");
			
			malfunction.setProbability(new_mal_prob_failure);

		}

		issueMedicalComplaints(malfunction);

	}

	/**
	 * Sets up a malfunction event
	 * 
	 * @param malfunction
	 * @param actor
	 */
	public void registerAMalfunction(Malfunction malfunction, Unit actor) {
		String malfunctionName = malfunction.getName();

		Settlement settlement = null;
		Person person = null;
		Robot robot = null;

		String offender = PARTS_FAILURE;
		String task = "N/A";

		malfunctions.add(malfunction);

		if (actor != null) {
			if (actor instanceof Person) {
				person = (Person) actor;
				settlement = person.getAssociatedSettlement();
				task = person.getTaskDescription();
			} else if (actor instanceof Robot) {
				robot = (Robot) actor;
				settlement = robot.getAssociatedSettlement();
				task = robot.getTaskDescription();
			}
			
			offender = actor.getName();
		}
	
		String loc0 = null;
		String loc1 = null;

		// TODO: determine what happens to each entity
//		EVASuit, Building, Robot, BuildingKit

		String object = entity.getNickName();

		if (entity.getUnit() instanceof Vehicle) {
			loc0 = entity.getNickName();
			loc1 = entity.getLocale();
			settlement = entity.getUnit().getAssociatedSettlement();
		}

		else if (entity.getUnit() instanceof EVASuit) {// object.toLowerCase().contains("eva")) {
//				Unit unit = entity.getUnit();
//				EVASuit suit = (EVASuit)entity.getUnit();
			loc0 = ((EVASuit) entity).getImmediateLocation();
			loc1 = ((EVASuit) entity).getLastOwner().getLocationTag().getLocale();
			settlement = entity.getUnit().getAssociatedSettlement();
		}

		else if (entity.getUnit() instanceof Robot) {// object.toLowerCase().contains("bot")) {
			loc0 = entity.getImmediateLocation();
			loc1 = entity.getLocale();
			settlement = entity.getUnit().getAssociatedSettlement();
		}

		else {
			loc0 = entity.getImmediateLocation();
			loc1 = entity.getLocale();
			settlement = entity.getUnit().getAssociatedSettlement();
		}

		if (object.equals(loc0)) {
			if (actor == null) {
				HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_PARTS_FAILURE,
						malfunction, malfunctionName, "N/A", "None", loc0, loc1, settlement.getName());
				eventManager.registerNewEvent(newEvent);
				
				LogConsolidated.log(Level.WARNING, 0, sourceName,
						"[" + loc1 + "] " + object + " had '" 
						+ malfunction.getName() + "'. Probable Cause : Parts Fatigue.");
			} 
			
			else {
				if (person != null) {
					HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_HUMAN_FACTORS,
							malfunction, malfunctionName, task, offender, loc0, loc1, settlement.getName());
					eventManager.registerNewEvent(newEvent);
					
					LogConsolidated.log(Level.WARNING, 0, sourceName, 
							"[" + loc1 + "] " + object + " had '"
							+ malfunction.getName() + "' as reported by " 
							+ offender + ". Probable Cause : Human Factors.");
				} else if (robot != null) {
					HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_PROGRAMMING_ERROR,
							malfunction, malfunctionName, task, offender, loc0, loc1, settlement.getName());
					eventManager.registerNewEvent(newEvent);
					
					LogConsolidated.log(Level.WARNING, 0, sourceName, 
							"[" + loc1 + "] " + object + " had '"
							+ malfunction.getName() + "' as reported by " 
							+ offender + ". Probable Cause : Software Quality Control.");
				}
			}
		} 
		
		else {
			if (actor == null) {
				HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_PARTS_FAILURE,
						malfunction, malfunctionName + " on " + object, "N/A", "None", loc0, loc1, settlement.getName());
				eventManager.registerNewEvent(newEvent);
				
				LogConsolidated.log(Level.WARNING, 0, sourceName,
						"[" + loc1 + "] " + object + " had '" 
						+ malfunction.getName() + "' in " + loc0 + ". Probable Cause : Parts Fatigue.");					
			} 
			
			else {
				if (person != null) {
					HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_HUMAN_FACTORS,
							malfunction, malfunctionName + " on " + object, task, offender, loc0, loc1, settlement.getName());
					eventManager.registerNewEvent(newEvent);
					
					LogConsolidated.log(Level.WARNING, 0, sourceName, 
							"[" + loc1 + "] " + object + " had '"
							+ malfunction.getName() + "' in " + loc0 + " as reported by " 
							+ offender + ". Probable Cause : Human Factors.");
				} 
				
				else if (robot != null) {
					HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_PROGRAMMING_ERROR,
							malfunction, malfunctionName, task, offender, loc0, loc1, settlement.getName());
					eventManager.registerNewEvent(newEvent);
					
					LogConsolidated.log(Level.WARNING, 0, sourceName, 
							"[" + loc1 + "] " + object + " had '"
							+ malfunction.getName() + "' in " + loc0 + " as reported by " 
							+ offender + ". Probable Cause : Software Quality Control.");
				}
			}
		}
	}
		
	
	public void registerMeteoriteMalfunction(Malfunction malfunction) {
		String malfunctionName = malfunction.getName();
		
		String task = "N/A";
		
		// Note : Unit actor is null
		String loc0 = null;
		String loc1 = null;

		String object = entity.getNickName();

		Settlement settlement = entity.getUnit().getAssociatedSettlement();
		
		// TODO: determine what happens to each entity
//		entity instanceof EVASuit	
//		entity instanceof Building
//		entity instanceof Robot
//		entity instanceof BuildingKit)

		if (entity.getUnit() instanceof Vehicle) {
			loc0 = entity.getNickName();
			loc1 = entity.getLocale();
		}

		else if (entity.getUnit() instanceof EVASuit) {//object.toLowerCase().contains("eva")) {
//				Unit unit = entity.getUnit();
//				EVASuit suit = (EVASuit)entity.getUnit();
			// TODO: for a eva suit malfunction,
			loc0 = ((EVASuit) entity).getImmediateLocation();
			loc1 = ((EVASuit) entity).getLastOwner().getLocationTag().getLocale();
		}

		else if (entity.getUnit() instanceof Robot) {// object.toLowerCase().contains("bot")) {
			loc0 = entity.getImmediateLocation();
			loc1 = entity.getLocale();
		}

		else {
			loc0 = entity.getImmediateLocation();
			loc1 = entity.getLocale();	
		}

		String name = malfunction.getTraumatized();

		// if it is a meteorite impact
		HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_ACT_OF_GOD, malfunction,
				malfunctionName, task, name, loc0, loc1, settlement.getName());
		eventManager.registerNewEvent(newEvent);
		
		if (object.equals(loc0)) {
			LogConsolidated.log(Level.WARNING, 0, sourceName,
				"[" + loc1 + "] " + object + " was damaged by " +  malfunctionName);
		}
		else {
			LogConsolidated.log(Level.WARNING, 0, sourceName,
				"[" + loc1 + "] " + object + " was damaged by " +  malfunctionName + " in " + loc0);
		}
	}
	
	/**
	 * Adds whitespaces
	 * 
	 * @param text
	 * @return
	 */
	public String addWhiteSpace(String text) {
		StringBuffer s = new StringBuffer();
		int max = 11;
		int size = text.length();

		for (int j=0; j< (max-size); j++) {
			s.append(" ");
		}
		s.append(text);		
	
		return s.toString();	
	}
	
//	 public void consumeFireExtingusher(int type) {
//		 if (type == 0) {
//			 if (entity.getInventory().hasItemResource(ItemResourceUtil.fireExtinguisherAR)) {
//			 entity.getInventory().retrieveItemResources(ItemResourceUtil.fireExtinguisherAR, 1);
//	 
//			 int rand = RandomUtil.getRandomInt(3); 
//			 	if (rand > 0) // Say 25% of the time, a fire extinguisher is being used up.
//				 entity.getInventory().storeItemResources(ItemResourceUtil.fireExtinguisherAR, 1); 
//			 } 
//		 } else if (type == 1) {
//			 if (entity.getInventory().hasItemResource(ItemResourceUtil.fireExtinguisherAR)) {
//				entity.getInventory().retrieveItemResources(ItemResourceUtil.fireExtinguisherAR, 1);
//	 
//			 int rand = RandomUtil.getRandomInt(3); 
//			 	if (rand > 1) // Say 50% of the time,  a fire extinguisher is being used up.
//				 entity.getInventory().storeItemResources(ItemResourceUtil.fireExtinguisherAR, 1); 
//			 } 
//		 } 
//		 else if (type == 2) {
//			 if (entity.getInventory().hasItemResource(ItemResourceUtil.fireExtinguisherAR)) {
//				 entity.getInventory().retrieveItemResources(ItemResourceUtil.fireExtinguisherAR, 1); 
//
//		 int rand = RandomUtil.getRandomInt(3); 
//		 if (rand == 0) // Say 25% of the time, a fire extinguisher is being used up.
//			 entity.getInventory().storeItemResources(ItemResourceUtil.fireExtinguisherAR, 2); 
//		 else if (rand == 1) 
//			 // Say 50% of the time, a fire extinguisher is being used up.
//			 entity.getInventory().storeItemResources(ItemResourceUtil.fireExtinguisherAR, 1); 
//			 } 
//		 }
//	 }

	/**
	 * Time passing for tracking the wear and tear condition while the unit is being actively used.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	public void activeTimePassing(double time) {

		effectiveTimeSinceLastMaintenance += time;

		// Add time to wear condition.
		wearCondition = wearCondition - (time / wearLifeTime) * 100D;
		if (wearCondition < 0D)
			wearCondition = 0D;

		double maintFactor = effectiveTimeSinceLastMaintenance * MAINTENANCE_MALFUNCTION_FACTOR;
		double wearFactor = (100D - wearCondition) / 100D * WEAR_MALFUNCTION_FACTOR + 1D;
		double chance = time * maintFactor * wearFactor;

		// Check for malfunction due to lack of maintenance and wear condition.
		if (RandomUtil.lessThanRandPercent(chance)) {
			int solsLastMaint = (int) (effectiveTimeSinceLastMaintenance / 1000D);
			// Reduce the max possible health condition
//			maxCondition = (wearCondition + 400D)/500D; 
			LogConsolidated.log(Level.WARNING, 1000, sourceName,
					"[" + entity.getImmediateLocation() + "] " + entity.getNickName() 
					+ " experienced a malfunction due to wear-and-tear.  "
					+ "# of sols since last check-up: " + solsLastMaint + ".   Condition: " + Math.round(wearCondition*10.0)/10.0
					+ " %.");

			// TODO: how to connect maintenance to field reliability statistics when selecting a malfunction ?
			selectMalfunction(null);
		}
	}

	/**
	 * Time passing for unit.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	public void timePassing(double time) {

		// Check if life support modifiers are still in effect.
//		setLifeSupportModifiers(time);

		// Check if resources is still draining
		try {
			depleteResources(time);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		
		checkFixedMalfunction(time);

		// Add time passing.
		timeSinceLastMaintenance += time;
	}

	/**
	 * Resets one or more flow modifier
	 * 
	 * @param type
	 */
	public void resetModifiers(int type) {
		// compare from previous modifier
//		logger.info("Reseting modifiers type " + type );
		if (type == 0) {
			oxygenFlowModifier = 100D;
			LogConsolidated.log(Level.WARNING, 5_000, sourceName,
					"[" + entity.getLocale() + "] The oxygen flow retrictor had been fixed in "
					+ entity.getNickName());
		}
//		
//		else if (type == 1) {
//			waterFlowModifier = 100D;
//			LogConsolidated.log(Level.WARNING, 0, sourceName,
//					"[" + entity.getLocale() + "] The water flow retrictor has been fixed in "
//					+ entity.getImmediateLocation(), null);
//		}
//		
//		else if (type == 2) {
//			airPressureModifier = 100D;
//			LogConsolidated.log(Level.WARNING, 0, sourceName,
//				"[" + entity.getLocale() + "] The air pressure regulator has been fixed in "
//				+ entity.getImmediateLocation(), null);
//		}
//		
//		else if (type == 3) {
//			temperatureModifier = 100D;
//			LogConsolidated.log(Level.WARNING, 0, sourceName,
//					"[" + entity.getLocale() + "] The temperature regulator has been fixed in "
//					+ entity.getImmediateLocation(), null);
//			
//		}
	}
	
	/**
	 * Checks if any malfunctions have been fixed
	 * 
	 * @param time
	 */
	public void checkFixedMalfunction(double time) { 
		Collection<Malfunction> fixedMalfunctions = new CopyOnWriteArrayList<Malfunction>();

		// Check if any malfunctions are fixed.
		if (hasMalfunction()) {
			for (Malfunction m : malfunctions) {
//				boolean hasEmerRepair = !m.isEmergencyRepairDone();
//				boolean hasEVARepair = !m.isEVARepairDone();
//				boolean hasGenRepair = !m.isGeneralRepairDone();
//				// Check if any repairer slots are still open
//				if (!m.areAllRepairerSlotsFilled() &&
//						(hasEmerRepair || hasEVARepair || hasGenRepair)) {
//					
//					Settlement s0 = null;
//					Vehicle v0 = null;
//					
//					Collection<Person> people = null;
//					
//					if (suit != null) {
//						// Case 1: the suit has malfunction
//						people = suit.getAffectedPeople();
//						
//						// Remove the candidate if he's already a repairer
//						for (Person p : people) {
////							logger.info(p.getName());
//							if (m.isARepairer(p.getName())) {
//								people.remove(p);
//							}
//						}
//						
//						int num = people.size();
//						
//						
//						s0 = suit.getSettlement();	
//						v0 = ((EVASuit)(entity.getUnit())).getVehicle();
//						
//						if (num > 1) {
//							people.stream()
//							.filter(p -> p.getJobName().equalsIgnoreCase(Engineer.class.getSimpleName())
//									|| p.getJobName().equalsIgnoreCase(Technician.class.getSimpleName()))
//								.collect(Collectors.toList());
//						}
//						else if (num == 1) {
//							
//						}
//						else if (num == 0) {
//							if (s0 != null) {
//								people = s0.getIndoorPeople();
//							}
//							
//							if (people.isEmpty()) {
//								s0 = suit.getAssociatedSettlement();	
//								people = s0.getIndoorPeople();
//							}
//							
//							if (people.isEmpty() && v0 != null) {
//								people = v0.getAffectedPeople();		
//							}
//						}
//					}
//					
//					else if (entity.getUnit() instanceof Settlement 
//							|| entity.getUnit() instanceof Building
//							|| entity.getUnit() instanceof Equipment) {
//						// Case 2: the malfunction occurs within a settlement
//						s0 = entity.getUnit().getSettlement();
////						System.out.println(m.getName() + " : " + settlement.getName());
//					}
//					else if (vehicle != null) {
//						// Case 3: the malfunction occurs in a vehicle
//						v0 = vehicle;
////						System.out.println(m.getName() + " : " + vehicle.getName());
//					} 		
//					
//					if (s0 != null) {
//						// Could be Case 1 or Case 2
//						people = s0.getAffectedPeople();
//						
//						// Remove the candidate if he's already a repairer
//						for (Person p : people) {
////							logger.info(p.getName());
//							if (m.isARepairer(p.getName()) || p.isOutside()) {
//								people.remove(p);
//							}
//						}
//						
//						Collection<Person> elites = people;
//						
//						if (people.size() == 0) {
//							people = s0.getIndoorPeople();
//						}
//						
//						if (!people.isEmpty()) {
//							elites = people.stream()
//									.filter(p -> p.getJobName().equalsIgnoreCase(Engineer.class.getSimpleName())
//											|| p.getJobName().equalsIgnoreCase(Technician.class.getSimpleName()))
//								.collect(Collectors.toList());
//						}
//						
//						
//						if (!elites.isEmpty() && elites.size() != 0) {
//							people = elites;			
//						}
//						
//					}
//					
//					else if (v0 != null) {
//						// For Case 3 only when the malfunction occurs in a vehicle
//						people = v0.getAffectedPeople();
//						
//						// Remove the candidate if he's already a repairer
//						for (Person p : people) {
////							logger.info(p.getName());
//							if (m.isARepairer(p.getName()) || p.isOutside()) {
//								people.remove(p);
//							}
//						}
//						
//						Collection<Person> elites = people;
//						
//						if (people.size() == 0 && v0 instanceof Rover) {
//							people = ((Rover)v0).getCrew();
//
//							// Remove the candidate if he's already a repairer
//							for (Person p : people) {
////								logger.info(p.getName());
//								if (m.isARepairer(p.getName()) || p.isOutside()) {
//									people.remove(p);
//								}
//							}					
//						}
//						
//						if (!people.isEmpty()) {
//							elites = people.stream()
//								.filter(p -> p.getJobName().equalsIgnoreCase(Engineer.class.getSimpleName())
//										|| p.getJobName().equalsIgnoreCase(Technician.class.getSimpleName()))
//								.collect(Collectors.toList());
////							System.out.println(elites);
//						}
//											
//						if (!elites.isEmpty() && elites.size() != 0) {
//							people = elites;			
//						}
//						
//					}
//					
//					Person chosen = null;
//					int highestScore = -100;
//					
//					for (Person p : people) {
////						logger.info(p.getName());
//						int pref0 = p.getPreference().getPreferenceScore(new RepairMalfunctionMeta());
////						int pref1 = p.getPreference().getPreferenceScore(new RepairEmergencyMalfunctionMeta());
//						int pref1 = p.getPreference().getPreferenceScore(new RepairEVAMalfunctionMeta());
//						int skill = p.getSkillManager().getSkillLevel(SkillType.MECHANICS);
//						int exp = p.getSkillManager().getSkillExp(SkillType.MECHANICS);
//						int score = (pref0 + pref1) + skill * 3 + exp;
//						if (highestScore < score) {
//							highestScore = score;
//							chosen = p;
//							LogConsolidated.log(Level.INFO, 0, sourceName,
//									"[" + entity.getLocale() + "] "
//									+ chosen.getName() + " had the highest repair qualification score of " + highestScore);
//						}
//					}
//					
//					if (highestScore > 0 && chosen != null) {
//						// TODO : how to avoid having the multiple person or the same person to do the following task repetitively ?
//
//						List<Integer> types = new ArrayList<>();
//						if (hasEmerRepair) {
//							types.add(1);
////							logger.info("Added 1");
//						}
//						if (hasGenRepair) {
//							types.add(0);
////							logger.info("Added 0");
//						}
//						if (hasEVARepair) {
//							types.add(2);
////							logger.info("Added 2");
//						}
//						
//						int size = types.size();
//						
//						if (size == 1) {
//							addTask(chosen, types.get(0), m);
//						}
//						else if (size == 2) {
//							
//							int rand = RandomUtil.getRandomInt(1);
//							int type = types.get(rand);
//							addTask(chosen, type, m);
//						}
//					}
//				}
//				
				if (m.isFixed()) {
					fixedMalfunctions.add(m);
				}
			}
		}

		int size = fixedMalfunctions.size();
		
		if (size > 0) {
			Iterator<Malfunction> i = fixedMalfunctions.iterator();
			while (i.hasNext()) {
				Malfunction m = i.next();

				// Reset the modifiers
				Map<String, Double> effects = m.getLifeSupportEffects();
				if (!effects.isEmpty()) {
					if (effects.containsKey(OXYGEN))
						resetModifiers(0);
//					if (effects.containsKey(WATER))
//						resetModifiers(1);
//					if (effects.containsKey(PRESSURE))
//						resetModifiers(2);
//					if (effects.containsKey(TEMPERATURE))
//						resetModifiers(3);
				}
				
				try {
					getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, m);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}

				String chiefRepairer = m.getMostProductiveRepairer();
				String loc = "";
				if (vehicle != null)
					loc = vehicle.getAssociatedSettlement().getName();
				else if (suit != null)
					loc = suit.getAssociatedSettlement().getName();
				else if (building != null)
					loc = building.getSettlement().getName();			
				else if (robot != null)
					loc = robot.getAssociatedSettlement().getName();
				else if (equipment != null)
					loc = equipment.getAssociatedSettlement().getName();
				else if (unit != null)
					loc = unit.getAssociatedSettlement().getName();
				
				HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_FIXED, m,
						m.getName(), "Repairing", chiefRepairer, entity.getImmediateLocation(),
						entity.getLocale(), loc);

				eventManager.registerNewEvent(newEvent);
				
				String loc1 = "";
				if (entity.getImmediateLocation().toLowerCase().contains("outside"))
					loc1 = "outside.";
				else
					loc1 = "in " + entity.getImmediateLocation() + ".";
				
				LogConsolidated.log(Level.WARNING, 0, sourceName,
						"[" + entity.getLocale() + "] The malfunction '" + m.getName() + "' had been dealt with "
						+ loc1);
			
				// Remove the malfunction
				fixedMalfunctions.remove(m);
				malfunctions.remove(m);				
			}
		}
	}
	
	/**
	 * Adds a new repair task for a person to perform
	 * 
	 * @param person
	 * @param task
	 */
	private void addTask(Person person, int type, Malfunction malfunction) {
		
		String chief = malfunction.getChiefRepairer(type);
		String deputy = malfunction.getDeputyRepairer(type);
//		logger.info(person.getName());
		if (chief == null || chief.equals("")) {
//			logger.info("Appointing" + person.getName() + " as the chief repairer. Type: " + type);
			// Give 50% of chance for a person to do other important things so that 
			// he would not be locked up to do just this task
//			int rand = RandomUtil.getRandomInt(1);
//			if (rand == 0) {
				if (type == 0) {
					person.getMind().getTaskManager().addTask(new RepairMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the chief repairer handling the General Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					 malfunction.setChiefRepairer(type, person.getName());
				}
				else if (type == 1) {
					person.getMind().getTaskManager().addTask(new RepairEmergencyMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the chief repairer handling the Emergency Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					malfunction.setChiefRepairer(type, person.getName());
				}
				else if (type == 2) {
					person.getMind().getTaskManager().addTask(new RepairEVAMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the chief repairer handling the EVA Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					malfunction.setChiefRepairer(type, person.getName());
				}
//			}
		}
		
		else if (deputy == null || deputy.equals("")) {
//			logger.info("Appointing" + person.getName() + " as the deputy repairer. Type: " + type);
			// Give 50% of chance for a person to do other important things so that 
			// he would not be locked up to do just this task
//			int rand = RandomUtil.getRandomInt(1);
//			if (rand == 0) {
				if (type == 0) {
					person.getMind().getTaskManager().addTask(new RepairMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the deputy repairer handling the General Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					 malfunction.setDeputyRepairer(type, person.getName());
				}
				else if (type == 1) {
					person.getMind().getTaskManager().addTask(new RepairEmergencyMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the deputy repairer handling the Emergency Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					malfunction.setDeputyRepairer(type, person.getName());
				}
				else if (type == 2) {
					person.getMind().getTaskManager().addTask(new RepairEVAMalfunction(person), false);	
					LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + entity.getLocale() + "] " + person + " was appointed as the deputy repairer handling the EVA Repair for '" 
						+ malfunction.getName() + "' on "
						+ entity.getUnit());
					malfunction.setDeputyRepairer(type, person.getName());
				}
			}
//		}
	}
	
	/**
	 * Determine life support modifiers for given time.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	public void setLifeSupportModifiers(double time) {

		double tempOxygenFlowModifier = 0D;
//		double tempWaterFlowModifier = 0D;
//		double tempAirPressureModifier = 0D;
//		double tempTemperatureModifier = 0D;

		// Make any life support modifications.
		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isFixed()) {
					Map<String, Double> effects = malfunction.getLifeSupportEffects();
					if (effects.get(OXYGEN) != null)
						tempOxygenFlowModifier += effects.get(OXYGEN) * (100D - malfunction.getPercentageFixed())/100D;
//					if (effects.get(WATER) != null)
//						tempWaterFlowModifier += effects.get(WATER) * (100D - malfunction.getPercentageFixed())/100D;
//					if (effects.get(PRESSURE) != null)
//						tempAirPressureModifier += effects.get(PRESSURE) * (100D - malfunction.getPercentageFixed())/100D;
//					if (effects.get(TEMPERATURE) != null)
//						tempTemperatureModifier += effects.get(TEMPERATURE) * (100D - malfunction.getPercentageFixed())/100D;
				}
			}

			if (tempOxygenFlowModifier < 0D) {
				oxygenFlowModifier += tempOxygenFlowModifier * time ;
				if (oxygenFlowModifier < 0)
					oxygenFlowModifier = 0;
				LogConsolidated.log(Level.WARNING, 20_000, sourceName,
						"[" + getUnit().getLocationTag().getLocale() + "] Oxygen flow restricted to "
								+ Math.round(oxygenFlowModifier*10.0)/10.0 + "% capacity in " 
								+ getUnit().getLocationTag().getImmediateLocation()+ ".", null);
			} 
//
//			if (tempWaterFlowModifier < 0D) {
//				waterFlowModifier += tempWaterFlowModifier * time;
//				if (waterFlowModifier < 0)
//					waterFlowModifier = 0;
//				LogConsolidated.log(Level.WARNING, 20_000, sourceName,
//						"[" + getUnit().getLocationTag().getLocale() + "] Water flow restricted to "
//								+ Math.round(waterFlowModifier*10.0)/10.0 + "% capacity in " + getUnit().getLocationTag().getImmediateLocation() + ".", null);
//			} 
//
//			if (tempAirPressureModifier < 0D) {
//				airPressureModifier += tempAirPressureModifier * time;
//				if (airPressureModifier < 0)
//					airPressureModifier = 0;
//				LogConsolidated.log(Level.WARNING, 20_000, sourceName,
//						"[" + getUnit().getLocationTag().getLocale() + "] Air pressure regulator malfunctioned at "
//								+ Math.round(airPressureModifier*10.0)/10.0 + "% capacity in " + getUnit().getLocationTag().getImmediateLocation() + ".", null);
//			} 
//
//			// temp mod can be above 0 or below zero
//			if (tempTemperatureModifier != 0D) {
//				temperatureModifier += tempTemperatureModifier * time;
//				if (temperatureModifier < 0)
//					temperatureModifier = 0;
//				LogConsolidated.log(Level.WARNING, 20_000, sourceName,
//						"[" + getUnit().getLocationTag().getLocale() + "] Temperature regulator malfunctioned at "
//								+ Math.round(temperatureModifier*10.0)/10.0 + "% capacity in " + getUnit().getLocationTag().getImmediateLocation() + ".", null);
//			}
		}
	}

	/**
	 * Depletes resources due to malfunctions.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error depleting resources.
	 */
	public void depleteResources(double time) {

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isFixed() && !malfunction.isEmergencyRepairDone()) {
					Map<Integer, Double> effects = malfunction.getResourceEffects();
					Iterator<Integer> i2 = effects.keySet().iterator();
					while (i2.hasNext()) {
						Integer resource = i2.next();
						double amount = effects.get(resource);
						double amountDepleted = amount * time;
						Inventory inv = entity.getInventory();
						double amountStored = inv.getAmountResourceStored(resource, false);

						if (amountStored < amountDepleted) {
							amountDepleted = amountStored;
						}
						if (amountDepleted >= 0) {
							inv.retrieveAmountResource(resource, amountDepleted);
							LogConsolidated.log(Level.WARNING, 15_000, sourceName,
									"[" + getUnit().getLocationTag().getLocale() + "] Leaking "
											+ Math.round(amountDepleted*100.0)/100.0 + " of  " 
											+ ResourceUtil.findAmountResource(resource) 
											+ " in " + getUnit().getLocationTag().getImmediateLocation()+ ".");
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a series of related malfunctions
	 * 
	 * @param s the place of accident
	 * @param r the robot who triggers the malfunction
	 */
	public void createASeriesOfMalfunctions(String s, Robot r) {
		// handleStringTypeOne(s, r);
		determineNumOfMalfunctions(1, s, r);
	}

	/**
	 * Creates a series of related malfunctions
	 * 
	 * @param s the place of accident
	 * @param p the person who triggers the malfunction
	 */
	public void createASeriesOfMalfunctions(String s, Person p) {
		// handleStringTypeOne(s, p);
		int nervousness = p.getMind().getTraitManager().getPersonalityTrait(PersonalityTraitType.NEUROTICISM);
		determineNumOfMalfunctions(1, s, nervousness, p);
	}

	/**
	 * Processes type one malfunctions
	 * 
	 * @param s the place of accident
	 * @param u the person/robot who triggers the malfunction
	 */
	public void handleStringTypeOne(String s, Unit u) {
		StringBuilder sb = new StringBuilder(Conversion.capitalize(s));

		if (s.contains("EVA")) {
			sb.insert(0, "with ");
		}

		else {
			// if it's a vehicle, no need of a/an
			sb.insert(0, "in ");
		}

//		 else { // if it's a vehicle, no need of a/an sb.insert(0, "in ");
//		 
//			 if (s.startsWith("A") || s.startsWith("E") || s.startsWith("I") ||
//				 s.startsWith("O") || s.startsWith("U")) //Conversion.checkVowel(name))
//				 sb.insert(0, "in an "); else sb.insert(0, "in a "); 
//		 }

		if (u.getLocationTag().getImmediateLocation().equalsIgnoreCase("outside")) {
			Settlement ss = u.getLocationTag().findSettlementVicinity();
			if (ss != null)
				LogConsolidated.log(Level.WARNING, 3000, sourceName,
					"[" + u.getLocationTag().getLocale() + "] A Type-I accident occurred " 
					+ sb.toString() + " outside of " + ss.getName());
			else
				LogConsolidated.log(Level.WARNING, 3000, sourceName,
						"[" + u.getLocationTag().getLocale() + "] A Type-I accident occurred " 
						+ sb.toString() + " outside.");
				
		}
		else {
			LogConsolidated.log(Level.WARNING, 3000, sourceName,
				"[" + u.getLocationTag().getLocale() + "] A Type-I accident occurred " 
				+ sb.toString() + " in " + u.getLocationTag().getImmediateLocation() + ".");

		}
	}

	/**
	 * Creates a series of related malfunctions
	 *
	 * @param p the Person who triggers the malfunction
	 */
	public void createASeriesOfMalfunctions(Person p) {
		// handleStringTypeTwo();
		int nervousness = p.getMind().getTraitManager().getPersonalityTrait(PersonalityTraitType.NEUROTICISM);
		determineNumOfMalfunctions(2, null, nervousness, p);
	}

	/**
	 * Creates a series of related malfunctions
	 * 
	 * @param r the robot who triggers the malfunction
	 */
	public void createASeriesOfMalfunctions(Robot r) {
		// handleStringTypeTwo();
		determineNumOfMalfunctions(2, null, r);
	}

	public void handleStringTypeTwo() {
		String n = entity.getNickName();
		
//		 StringBuilder sb = new StringBuilder();
//		Conversion.capitalize(n)); 
//		if (n.contains("EVA")) { 
//			sb.insert(0, "with "); 
//		}
//		  
//		 else { 
//			sb.insert(0, "in "); 
//		}
		 

//		String sName = null;
//		if (entity.getImmediateLocation() != null) {
//			sName = entity.getImmediateLocation().replace(Conversion.capitalize(n), "")
//					.replace(" in ", "");
//		}

		LogConsolidated.log(Level.WARNING, 3000, sourceName,
				// "[" + locationName + "] An accident occurs " + sb.toString() + ".", null);
				"[" + entity.getLocale() + "] A Type-II accident occurred in " 
						+ Conversion.capitalize(n) + ".");
		
	}

	/**
	 * Determines the numbers of malfunctions.
	 * 
	 * @param type  the type of malfunction
	 * @param s     the place of accident
	 * @param actor the person/robot who triggers the malfunction
	 */
	public void determineNumOfMalfunctions(int type, String s, int score, Unit actor) {
		// Multiple malfunctions may have occurred.
		// 50% one malfunction, 25% two etc.
		boolean hasMal = false;
		boolean done = false;
		double chance = 100D;
		double mod = score / 50;
		while (!done) {
			if (RandomUtil.lessThanRandPercent(chance)) {
				hasMal = selectMalfunction(actor);
				chance = chance / 3D * mod;
			} else {
				done = true;
			}
		}

		if (hasMal) {
			logger.warning("[" + entity.getLocale() + "] " + actor.getName() + " had reported an incident.");
			if (type == 1)
				handleStringTypeOne(s, actor);
			else if (type == 2)
				handleStringTypeTwo();
			// Add stress to people affected by the accident.
			Collection<Person> people = entity.getAffectedPeople();
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				PhysicalCondition condition = i.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + PhysicalCondition.ACCIDENT_STRESS);
			}
		}
	}

	/**
	 * Determines the numbers of malfunctions.
	 * 
	 * @param type  the type of malfunction
	 * @param s     the place of accident
	 * @param actor the person/robot who triggers the malfunction
	 */
	public void determineNumOfMalfunctions(int type, String s, Unit actor) {
		// Multiple malfunctions may have occurred.
		// 50% one malfunction, 25% two etc.
		boolean hasMal = false;
		boolean done = false;
		double chance = 100D;
		while (!done) {
			if (RandomUtil.lessThanRandPercent(chance)) {
				hasMal = selectMalfunction(actor);
				chance /= 3D;
			} else {
				done = true;
			}
		}

		if (hasMal) {
			if (type == 1)
				handleStringTypeOne(s, actor);
			else if (type == 2)
				handleStringTypeTwo();
			// Add stress to people affected by the accident.
			Collection<Person> people = entity.getAffectedPeople();
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				PhysicalCondition condition = i.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + PhysicalCondition.ACCIDENT_STRESS);
			}
		}
	}

	/**
	 * Gets the time since last maintenance on entity.
	 * 
	 * @return time (in millisols)
	 */
	public double getTimeSinceLastMaintenance() {
		return timeSinceLastMaintenance;
	}

	/**
	 * Gets the time the entity has been actively used since its last maintenance.
	 * 
	 * @return time (in millisols)
	 */
	public double getEffectiveTimeSinceLastMaintenance() {
		return effectiveTimeSinceLastMaintenance;
	}

	/**
	 * Gets the required work time for maintenance for the entity.
	 * 
	 * @return time (in millisols)
	 */
	public double getMaintenanceWorkTime() {
		return maintenanceWorkTime;
	}

	/**
	 * Sets the required work time for maintenance for the entity.
	 * 
	 * @param maintenanceWorkTime (in millisols)
	 */
	public void setMaintenanceWorkTime(double maintenanceWorkTime) {
		this.maintenanceWorkTime = maintenanceWorkTime;
	}

	/**
	 * Gets the work time completed on maintenance.
	 * 
	 * @return time (in millisols)
	 */
	public double getMaintenanceWorkTimeCompleted() {
		return maintenanceTimeCompleted;
	}

	/**
	 * Add work time to maintenance.
	 * 
	 * @param time (in millisols)
	 */
	public void addMaintenanceWorkTime(double time) {
		maintenanceTimeCompleted += time;
		if (maintenanceTimeCompleted >= maintenanceWorkTime) {
			maintenanceTimeCompleted = 0D;
			timeSinceLastMaintenance = 0D;
			effectiveTimeSinceLastMaintenance = 0D;
			determineNewMaintenanceParts();
			numberMaintenances++;
		}
	}

	/**
	 * Issues any necessary medical complaints.
	 * 
	 * @param malfunction the new malfunction
	 */
	public void issueMedicalComplaints(Malfunction malfunction) {

		// Determine medical complaints for each malfunction.
		Iterator<ComplaintType> i1 = malfunction.getMedicalComplaints().keySet().iterator();
		while (i1.hasNext()) {
			ComplaintType type = i1.next();
			double probability = malfunction.getMedicalComplaints().get(type);
//			MedicalManager medic = Simulation.instance().getMedicalManager();
			// Replace the use of String name with ComplaintType
			Complaint complaint = medic.getComplaintByName(type);
			if (complaint != null) {
				// Get people who can be affected by this malfunction.
				Iterator<Person> i2 = entity.getAffectedPeople().iterator();
				while (i2.hasNext()) {
					Person person = i2.next();
					if (RandomUtil.lessThanRandPercent(probability)) {
						person.getPhysicalCondition().addMedicalComplaint(complaint);
						person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
					}
				}
			}
		}
	}

	/**
	 * Gets the oxygen flow modifier.
	 * 
	 * @return modifier
	 */
	public double getOxygenFlowModifier() {
		return oxygenFlowModifier;
	}
//
//	/**
//	 * Gets the water flow modifier.
//	 * 
//	 * @return modifier
//	 */
//	public double getWaterFlowModifier() {
//		return waterFlowModifier;
//	}
//
//	/**
//	 * Gets the air flow modifier.
//	 * 
//	 * @return modifier
//	 */
//	public double getAirPressureModifier() {
//		return airPressureModifier;
//	}
//
//	/**
//	 * Gets the temperature modifier.
//	 * 
//	 * @return modifier
//	 */
//	public double getTemperatureModifier() {
//		return temperatureModifier;
//	}

	/**
	 * Gets the unit associated with this malfunctionable.
	 * 
	 * @return associated unit.
	 * @throws Exception if error finding associated unit.
	 */
	public Unit getUnit() {
		if (entity instanceof Unit)
			return (Unit) entity;
		else if (entity instanceof Building)
			return ((Building) entity).getSettlement();
		else
			throw new IllegalStateException("Could not find unit associated with malfunctionable.");
	}

	/**
	 * Determines a new set of required maintenance parts.
	 */
	private void determineNewMaintenanceParts() {
		if (partsNeededForMaintenance == null)
			partsNeededForMaintenance = new HashMap<>();
		partsNeededForMaintenance.clear();

		Iterator<String> i = scopes.iterator();
		while (i.hasNext()) {
			String entity = i.next();
			Iterator<Integer> j = Part.getItemIDs().iterator();
			while (j.hasNext()) {
				Integer id = j.next();
				Part part = ItemResourceUtil.findItemResource(id);
				// Pick the part that is related to the entity
				if (part.hasMaintenanceEntity(entity)) {
					if (RandomUtil.lessThanRandPercent(part.getMaintenanceProbability(entity))) {
						int number = RandomUtil.getRandomRegressionInteger(part.getMaintenanceMaximumNumber(entity));
						if (partsNeededForMaintenance.containsKey(id))
							number += partsNeededForMaintenance.get(id);
						partsNeededForMaintenance.put(id, number);
					}
				}
			}
		}
	}

	/**
	 * Gets the parts needed for maintenance on this entity.
	 * 
	 * @return map of parts and their number.
	 */
	public Map<Integer, Integer> getMaintenanceParts() {
		if (partsNeededForMaintenance == null)
			partsNeededForMaintenance = new HashMap<>();
		return new HashMap<>(partsNeededForMaintenance);
	}

	/**
	 * Adds a number of a part to the entity for maintenance.
	 * 
	 * @param part   the part.
	 * @param number the number used.
	 */
	public void maintainWithParts(Integer part, int number) {
		if (part == null)
			throw new IllegalArgumentException("part is null");
		if (partsNeededForMaintenance.containsKey(part)) {
			int numberNeeded = partsNeededForMaintenance.get(part);
			if (number > numberNeeded)
				throw new IllegalArgumentException(
						"number " + number + " is greater that number of parts needed: " + numberNeeded);
			else {
				numberNeeded -= number;
				if (numberNeeded > 0)
					partsNeededForMaintenance.put(part, numberNeeded);
				else
					partsNeededForMaintenance.remove(part);
			}
		} else
			throw new IllegalArgumentException("Part " + part + " is not needed for maintenance.");
	}

	/**
	 * Gets the repair part probabilities for the malfunctionable.
	 * 
	 * @return maps of parts and probable number of parts needed per malfunction.
	 * @throws Exception if error finding probabilities.
	 */
	public Map<Integer, Double> getRepairPartProbabilities() {
		// MalfunctionFactory factory = Simulation.instance().getMalfunctionFactory();
		return factory.getRepairPartProbabilities(scopes);
	}

	/**
	 * Gets the repair part probabilities for maintenance.
	 * 
	 * @return maps of parts and probable number of parts in case of maintenance.
	 * @throws Exception if error finding probabilities.
	 */
	public Map<Integer, Double> getMaintenancePartProbabilities() {
		// MalfunctionFactory factory = Simulation.instance().getMalfunctionFactory();
		return factory.getMaintenancePartProbabilities(scopes);
	}
		
	/**
	 * Gets the estimated number of malfunctions this entity will have in one
	 * Martian orbit.
	 * 
	 * @return number of malfunctions.
	 */
	public double getEstimatedNumberOfMalfunctionsPerOrbit() {
		double avgMalfunctionsPerOrbit = 0D;

		double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, masterClock.getInitialMarsTime());
		double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

		if (totalTimeOrbits < 1D) {
			avgMalfunctionsPerOrbit = (numberMalfunctions + ESTIMATED_MALFUNCTIONS_PER_ORBIT) / 2D;
		} else {
			avgMalfunctionsPerOrbit = numberMalfunctions / totalTimeOrbits;
		}

		int orbit = currentTime.getOrbit();
		if (orbitCache != orbit) {
			orbitCache = orbit;
			numberMalfunctions = 0;
		}

		return avgMalfunctionsPerOrbit;
	}

	/**
	 * Gets the estimated number of periodic maintenances this entity will have in
	 * one Martian orbit.
	 * 
	 * @return number of maintenances.
	 */
	public double getEstimatedNumberOfMaintenancesPerOrbit() {
		double avgMaintenancesPerOrbit = 0D;

		// Note : the elaborate if-else conditions below is for passing the maven test
//		if (masterClock == null)
//			masterClock = sim.getMasterClock();
//		else {
//			if (startTime == null)
//				startTime = masterClock.getInitialMarsTime();
//			if (currentTime == null)
//				currentTime = masterClock.getMarsClock();

			double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, masterClock.getInitialMarsTime());
			double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

			if (totalTimeOrbits < 1D) {
				avgMaintenancesPerOrbit = (numberMaintenances + ESTIMATED_MAINTENANCES_PER_ORBIT) / 2D;
			} else {
				avgMaintenancesPerOrbit = numberMaintenances / totalTimeOrbits;
			}
//		}

		return avgMaintenancesPerOrbit;
	}

	/**
	 * Inner class comparator for sorting malfunctions my highest severity to
	 * lowest.
	 */
	private static class MalfunctionSeverityComparator implements Comparator<Malfunction>, Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Malfunction malfunction1, Malfunction malfunction2) {
			int severity1 = malfunction1.getSeverity();
			int severity2 = malfunction2.getSeverity();
			if (severity1 > severity2)
				return -1;
			else if (severity1 == severity2)
				return 0;
			else
				return 1;
		}
	}

	/**
	 * Get the percentage representing the malfunctionable's condition from wear &
	 * tear. 100% = new condition 0% = worn out condition.
	 * 
	 * @return wear condition.
	 */
	public double getWearCondition() {
		return wearCondition;
	}

	/**
	 * Gets the multiplying modifier for the chance of an accident due to the
	 * malfunctionable entity's wear condition.
	 * 
	 * @return accident modifier.
	 */
	public double getWearConditionAccidentModifier() {
		return (100D - wearCondition) / 100D * WEAR_ACCIDENT_FACTOR + 1D;
	}

	/**
	 * initializes instances after loading from a saved sim
	 * 
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param mf {@link MalfunctionFactory}
	 * @param m {@link MedicalManager}	 
	 * @param e {@link HistoricalEventManager}
	 */
	public static void initializeInstances(MasterClock c0, MarsClock c1, MalfunctionFactory mf, MedicalManager m, HistoricalEventManager e) {
		masterClock = c0;
//		startTime = masterClock.getInitialMarsTime();
		currentTime = c1;
//		partConfig = simconfig.getPartConfiguration();
		sim = Simulation.instance();
		simconfig = SimulationConfig.instance();
		malfunctionConfig = simconfig.getMalfunctionConfiguration();
		factory = mf;
		medic = m;
		eventManager = e;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		entity = null;
		scopes.clear();
		scopes = null;
		malfunctions.clear();
		malfunctions = null;
		if (partsNeededForMaintenance != null) {
			partsNeededForMaintenance.clear();
		}
		partsNeededForMaintenance = null;
	}
}