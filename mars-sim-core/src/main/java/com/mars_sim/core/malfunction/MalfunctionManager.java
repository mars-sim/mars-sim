/*
 * Mars Simulation Project
 * MalfunctionManager.java
 * @date 2025-08-24
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.goods.PartGood;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.PersonalityTraitType;
import com.mars_sim.core.person.health.Complaint;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.MedicalManager;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The MalfunctionManager class manages malfunctions and part maintenance needs for 
 * units such as Building, EVASuit, Robot, or Vehicle. 
 * 
 * Maintenance is different from malfunction.
 * 
 * When something breaks, it is a malfunction and repair must be done ASAP. 
 * 
 * On the other hand, maintenance are generally predictive in nature. 
 * 
 * The inspection-portion of a maintenance event by definition does not generate 
 * any needed parts. 
 * 
 * If a maintenance does generate needed parts, it should be taken as a 
 * preventive measure to swap out possibly worn parts in order to prevent a 
 * malfunction from happening. 
 * 
 * In future, we may identify and allow some of needed parts being taken offline 
 * to be retrofit, especially those those costly, limited quantity parts.
 * 
 * Both maintenance and malfunction are reliability-centered, meaning these two
 * disciplines are closely related with the concept of part and system reliability.
 *  
 * Note: almost all units (except Container) have their own MalfunctionManager.
 */
public class MalfunctionManager implements Serializable, Temporal {

	private static final String PERC_CHANGE = "%.3f %% --> %.3f %%";

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MalfunctionManager.class.getName());

	/** For a typical building, the number of inspection expected throughout its lifetime. */
	private static final int INSPECTION_FREQUENCY = 75;
	
	/** Initial estimate for malfunctions per orbit for an entity. */
	private static final double ESTIMATED_MALFUNCTIONS_PER_ORBIT = 5;
	/** Factor for chance of malfunction by time since last maintenance. */
	private static final double MAINT_TO_MAL_RATIO = 5;
	/** The lower limit factor for malfunction. 1.000_003_351_695 will result in 1 % certainty per orbit. */	
	private static final double MALFUNCTION_LOWER_LIMIT =  1.000_003_351_695; // 0.000_000_002; //  1.000_003_351_695;
	/** The lower limit factor for maintenance. 1.000_033_516_95 will result in 10 % certainty per orbit. */	
	private static final double MAINTENANCE_LOWER_LIMIT = 0; //MALFUNCTION_LOWER_LIMIT; // * MAINT_TO_MAL_RATIO; //1.000_033_516_95;
	/** The upper limit factor for both malfunction and maintenance. 1.000_335_221_5 will result in 100% certainty per orbit. */
	private static final double UPPER_LIMIT = 2;//1.000_335_221_5;
	
	/** Wear-and-tear points earned from a low quality inspection. */
	private static final double LOW_QUALITY_INSPECTION = 200;
	/** Wear-and-tear points earned from a high quality inspection. */
	private static final double HIGH_QUALITY_INSPECTION = 2000;
	
	/** Stress jump resulting from being in an accident. */
	private static final double ACCIDENT_STRESS = 5D;
	/** Modifier for number of parts needed for a trip. */
	public static final double PARTS_NUMBER_MODIFIER = 7.5;
	/** Estimate number of broken parts per malfunctions */
	public static final double AVERAGE_NUM_MALFUNCTION = 2.5;
	/** Estimate number of broken parts per malfunctions for EVA suits. */
	public static final double AVERAGE_EVA_MALFUNCTION = 2.0;

	/** Factor for chance of malfunction due to wear condition. */
	private static final double WEAR_MALFUNCTION_FACTOR = .01;
	/** Factor for chance of accident due to wear condition. */
	private static final double WEAR_ACCIDENT_FACTOR = 1D;

	private static final String OXYGEN = "Oxygen";
	private static final String PROBABLE_CAUSE = ". Probable Cause: ";
	private static final String CAUSED_BY = " Caused by '";

	private static final int FREQUENCY = 7;
	private static final int SCORE_DEFAULT = 50;
	private static final int MAX_DELAY = 100;

	private static boolean noFailures = false;
	
	// Data members
	private boolean supportInsideRepair = true;

	private int delay = 0;
	/** The number of malfunctions the entity has had so far. */
	private int numberMalfunctions;
	/** The number of times the entity has been maintained so far. */
	private int numberMaintenances;
	/** The number of orbits. */
	private int orbitCache = MarsTime.FIRST_ORBIT;
	/** The fatigue accumulated . */
	private double cumulativeFatigue;
	/** Life support modifiers. */
	private double oxygenFlowModifier = 100D;
	/** The overall % probability that a maintenance event is triggered by active use on this entity. */
	private double maintenanceProbability;	
	/** The overall % probability that a malfunction is triggered by active use on this entity. */
	private double malfunctionProbability;
	/** Time (millisols) that entity has been actively used since last maintenance. */
	private double effTimeSinceLastMaint;
	/** The required base work time for each inspection maintenance on entity. */
	private double baseMaintWorkTime;
	/** The inspection completed. */
	private double inspectionTimeCompleted;
	/** The periodic time window between each inspection/maintenance.  */
	private double standardInspectionWindow;
	/** The percentage of the malfunctionable's condition from wear and tear. 0% = worn out -> 100% = new condition. */
	private double currentWearCondPercent;
	/** The cumulative time [in millisols] since active use. */
	private double cumulativeTime;
	/** The current life time [in millisols] of active use. */
	private double currentWearLifeTime;
	/**
	 * The expected life time [in millisols] of active use before the malfunctionable
	 * is worn out.
	 */
	private final double baseWearLifeTime;
	
	/** The owning entity. */
	private Malfunctionable entity;

	/** The collection of affected scopes. */
	private Set<String> scopes;
	/** The current malfunctions in the unit. */
	private List<Malfunction> malfunctions;
	/** The map of maintenance scopes. */
	private Map<String, List<MaintenanceScope>> scopeMap = new HashMap<>();
	/** The parts currently identified to be retrofitted. */
	private Map<MaintenanceScope, Integer> partsNeededForMaintenance;
	// Note: there is no need of serializing scopeCollection since it's only being used by
	// TabPanelMaintenance for generating tables 
	private Map<Collection<String>, List<MaintenanceScope>> scopeCollection = new HashMap<>();
	
	private static MasterClock masterClock;
	private static MedicalManager medic;
	private static MalfunctionFactory factory;
	private static HistoricalEventManager eventManager;
	private static PartConfig partConfig;
	
	/**
	 * Constructor.
	 *
	 * @param entity              the malfunctionable entity.
	 * @param wearLifeTime        the expected life time (millisols) of active use
	 *                            before the entity is worn out.
	 * @param maintWorkTime the amount of work time (millisols) required for
	 *                            maintenance.
	 * Note: for buildings, see maintenance-time in buildings.xml                           
	 */
	public MalfunctionManager(Malfunctionable entity, double wearLifeTime, double maintWorkTime) {

		// Initialize data members
		this.entity = entity;

		scopes = new HashSet<>();
		malfunctions = new ArrayList<>();
		
		this.baseMaintWorkTime = maintWorkTime;
		this.baseWearLifeTime = wearLifeTime;

		boolean isInhabitable = false;
		boolean isERV = false;
		boolean isMainPowerGen = false;
		boolean isHallway = false;
		
		if (UnitType.BUILDING == entity.getUnitType()) {

			Building building = (Building)entity;
			if (building.isInhabitable()) {
				// Usually power building gets deployed first.
				isInhabitable = true;
				
				if (BuildingCategory.ERV == building.getCategory()) {
					// Next is resource processing building such as ERV 
					isERV = true;
				}
				else if (BuildingCategory.POWER == building.getCategory()) {
					// Next is the main power generator
					isMainPowerGen = true;
				}
			}
			else if (BuildingCategory.CONNECTION == building.getCategory()) {
				// Next is resource processing building such as ERV 
				isHallway = true;
			}
		}

		double deployedTime = 0;
		
		// deployedTime [in millisols] accounts for the pre-used time spent during the base deployment phase prior to the start of the sim
		
		// Note: May also vary deployedTime according to future pre-game deployment scenario.
		
		if (UnitType.EVA_SUIT == entity.getUnitType()) {
			
			this.standardInspectionWindow = .5;
			
			deployedTime = RandomUtil.getRandomDouble(250);
		}
		else if (UnitType.VEHICLE == entity.getUnitType()) {
	
			this.standardInspectionWindow = .75;
			
			deployedTime = RandomUtil.getRandomDouble(500);
		}
		else if (isInhabitable) {
			
			if (isMainPowerGen) {
				// Assume having an inspection window [in millisols] as the recommended period of time between the last and the next inspection/maintenance 
				
				// Note: vary this window by building category and by the frequency of malfunction of its parts

				// e.g. solar array and power generator need to be inspected more often
			
				this.standardInspectionWindow = .5;
	
				// Usually power building gets deployed first.
				deployedTime = RandomUtil.getRandomDouble(4000);
			}
			else if (isERV) {
				this.standardInspectionWindow = .75;
				
				// Next is resource processing building such as ERV 
				deployedTime = RandomUtil.getRandomDouble(3000);
			}
			else {
				this.standardInspectionWindow = 1.0;
				
				deployedTime = RandomUtil.getRandomDouble(2000);
			}
		}
		else if (isHallway){
			this.standardInspectionWindow = 1.5;
			
			deployedTime = RandomUtil.getRandomDouble(1000);
		}
		else {
			this.standardInspectionWindow = 1.0;
			
			deployedTime = RandomUtil.getRandomDouble(2000);
		}
	
		this.standardInspectionWindow *= wearLifeTime / INSPECTION_FREQUENCY;
				
		deployedTime += 1_000_000.0 / standardInspectionWindow;
		
		currentWearLifeTime = wearLifeTime - deployedTime;
		cumulativeTime = deployedTime;
		effTimeSinceLastMaint = deployedTime;
	
		currentWearCondPercent = currentWearLifeTime/baseWearLifeTime * 100D;
	}

	/**
	 * Initializes the scope map.
	 */
	public void initScopes() {
		for (String scope: scopes) {
			scopeMap.put(scope, partConfig.getMaintenanceScopeList(scope));
		}
	}
	
	/**
	 * Does this malfunctionable support inside repairs?
	 * 
	 * @param supported inside repairs supported
	 */
	public void setSupportInsideRepair(boolean supported) {
		this.supportInsideRepair = supported;
	}
	
	/**
	 * Adds a scope string of a system or a function to the manager.
	 *
	 * @param scope
	 */
	public void addScopeString(String scope) {
		String scopeString = scope.toLowerCase().replace("_", " ");
		if ((scopeString != null) && !scopes.contains(scopeString))
			scopes.add(scopeString);
	}
	
	/**
	 * 
	 * Adds a set of scopes to the manager.
	 *
	 * @param scope
	 */
	public void addScopeString(Set<String> newScopes) {
		for (String aScope: newScopes) {
			String scopeString = aScope.toLowerCase().replace("_", " ");
			if ((scopeString != null) && !scopes.contains(scopeString))
				scopes.add(scopeString);
		}
	}

	/**
	 * Returns a set of scopes.
	 * 
	 * @return
	 */
	public Set<String> getScopes() {
		return scopes;
	}

	/**
	 * Checks if entity has a malfunction.
	 *
	 * @return true if malfunction
	 */
	public boolean hasMalfunction() {
		return !malfunctions.isEmpty();
	}

	/**
	 * Gets a list of the unit's current malfunctions.
	 *
	 * @return malfunction list
	 */
	public List<Malfunction> getMalfunctions() {
		return Collections.unmodifiableList(malfunctions);
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
	 * Gets the most serious general malfunction the entity has.
	 * Malfunction must need work of the specified work type and
	 * have worker slots vacant.
	 *
	 * @param work
	 * @return malfunction
	 */
	public Malfunction getMostSeriousMalfunctionInNeed(MalfunctionRepairWork work) {

		Malfunction result = null;
		double highestSeverity = 0D;

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (malfunction.hasWorkType(work)
					&& !malfunction.isWorkDone(work)
					&& (malfunction.numRepairerSlotsEmpty(work) > 0)
					&& (result == null || malfunction.getSeverity() > highestSeverity)) {
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
	public List<Malfunction> getAllInsideMalfunctions() {
		List<Malfunction> result = new ArrayList<>();
		for (Malfunction malfunction : malfunctions) {
			if (malfunction.hasWorkType(MalfunctionRepairWork.INSIDE)
					&& !malfunction.isWorkDone(MalfunctionRepairWork.INSIDE))
				result.add(malfunction);
		}
		Collections.sort(result, new MalfunctionSeverityComparator());
		return result;
	}

	/**
	 * Gets a list of all EVA malfunctions sorted by highest severity first.
	 *
	 * @return list of malfunctions.
	 */
	public List<Malfunction> getAllEVAMalfunctions() {
		List<Malfunction> result = new ArrayList<>();
		for (Malfunction malfunction : malfunctions) {
			if (malfunction.hasWorkType(MalfunctionRepairWork.EVA)
					&& !malfunction.isWorkDone(MalfunctionRepairWork.EVA))
				result.add(malfunction);
		}
		Collections.sort(result, new MalfunctionSeverityComparator());
		return result;
	}

	/**
	 * Selects a malfunction randomly to the unit, based on the affected scope.
	 *
	 * @param actor
	 */
	private boolean selectMalfunction(Unit actor) {
		boolean result = false;
		// Clones a malfunction and determines repair parts
		MalfunctionMeta malfunction = factory.pickAMalfunction(scopes);
		if (malfunction != null) {
			result = (triggerMalfunction(malfunction, true, actor) != null);
		}

		return result;
	}

	/**
	 * Triggers a particular malfunction.
	 *
	 * @param {@link MalfunctionMeta}
	 * @param registerEvent
	 * @param actor
	 */
	public Malfunction triggerMalfunction(MalfunctionMeta m, boolean registerEvent, Unit actor) {
		if (noFailures) {
			return null;
		}
		
		Malfunction malfunction = new Malfunction(this, factory.getNewIncidentNum(), m, supportInsideRepair);

		malfunctions.add(malfunction);
		
		numberMalfunctions++;

		getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, malfunction);

		if (registerEvent) {
			registerAMalfunction(malfunction, actor);
		}

		if (malfunction.getRepairParts().isEmpty()) { 
			logger.info(actor, 0, malfunction.getName() + " triggered repair work but with no repair parts needed.");	
		}
		
		else {
			calculateNewReliability(malfunction);
		}

		if (!malfunction.getTraumatized().equalsIgnoreCase("None"))
			issueMedicalComplaints(malfunction);

		return malfunction;
	}

	/**
	 * Computes the new reliability statistics.
	 * 
	 * @param malfunction
	 */
	public void calculateNewReliability(Malfunction malfunction) {
		
		ItemResourceUtil.initConsumableParts();
		
		// Register the failure of the Parts involved
		for (Entry<MaintenanceScope, Integer> p : malfunction.getRepairParts().entrySet()) {	
			MaintenanceScope ms = p.getKey();
			// Compute the new reliability and failure rate for this malfunction
			Part part = ms.getPart();
			String partName = part.getName();

			if (ItemResourceUtil.consumablePartIDs.contains(part.getID())) {
				// No need to calculate the consumable parts since they are tools needed 
				// for repairing malfunctions and are not broken
				return;
			}
			
			// Record the number of failure
			int numFailed = p.getValue();
			part.recordCumFailure(numFailed);	
			
			double oldRel = part.getReliability();
			double oldRepairProb = getRepairPartProbability(malfunction, partName);
			double oldFailure = part.getFailureRate();
			double oldMalProb = malfunction.getProbability();
			
			double oldMTBF = part.getMTBF();
			
			// Retrieve the cumulative number of failure
			int cumFailure = part.getCumFailure();				
			// Gets the mission sol
			int missionSol = masterClock.getMarsTime().getMissionSol();
			
			double millisols = masterClock.getMarsTime().getMillisol();
			// Gets the fractional sols in use 
			double solsInUse = missionSol + millisols/1000 - part.getStartSol();
			
			// Recompute the MTBF for this part
			double newMTBF = part.computeMTBF(solsInUse, numFailed);
			// Recompute the reliability for this part
			double newRel = part.computeReliability(solsInUse);
			// Recompute the failure rate
			double newFailure = part.computeFailureRate(solsInUse);
			// Update all part's repair probability
			double newRepairProb = computeRepairPartProbability(malfunction, partName, oldRel, newRel);
			// Recompute the malfunction failure 
			double newMalProb = Math.max(oldMalProb * 1.1, oldMalProb * + 0.1 * newFailure);
			// Update the probability of failure for this particular malfunction
			malfunction.setProbability(newMalProb);
			
			logger.warning("                     Part Name : " + partName + "");
			logger.warning(" (0). Cumulative # of Failures : " + cumFailure);
			logger.warning(" (1).         Current # Failed : " + numFailed);			
			logger.warning(" (2).              Sols in Use : " + Math.round(solsInUse * 100.0)/100.0);
			logger.warning(" (3).                     MTBF : " + String.format("%.2f sols --> %.2f sols", oldMTBF, newMTBF));	
			logger.warning(" (4).             Failure Rate : " + String.format(PERC_CHANGE, oldFailure, newFailure));			
			logger.warning(" (5).      Percent Reliability : " + String.format(PERC_CHANGE, oldRel, newRel));
			logger.warning(" (6).       Repair Probability : " + String.format(PERC_CHANGE, oldRepairProb, newRepairProb));			
			logger.warning(" (7).  Malfunction Probability : " + String.format(PERC_CHANGE, oldMalProb, newMalProb));
		}
	}
	
	/**
	 * Gets the probability of a repair part for a malfunction.
	 *
	 * @param malfunctionName the name of the malfunction.
	 * @param partName        the name of the part.
	 * @return the probability of failure of a particular repair part.
	 */
	public double getRepairPartProbability(Malfunction malfunction, String partName) {
		double result = 0;
		List<RepairPart> partList = malfunction.getMalfunctionMeta().getParts();
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.getName().equalsIgnoreCase(partName)) {
					return part.getRepairProbability();
				}
			}
		}
		return result;
	}


	/**
	 * Sets the probability of a repair part for a malfunction.
	 *
	 * @param malfunctionName the name of the malfunction.
	 * @param partName        the name of the part.
	 * @param oldRel     the old reliability of the part.
	 * @param newRel     the new reliability of the part.
	 * @return probability of failure of a particular repair part.
	 */
	public double computeRepairPartProbability(Malfunction malfunction, String partName, double oldRel, double newRel) {
		List<RepairPart> partList = malfunction.getMalfunctionMeta().getParts();
		if ((partList != null) && !partList.isEmpty()) {
			Iterator<RepairPart> i = partList.iterator();
			double totalProb = 0;
			double oldRepairProb = 0;
			int num = 0;
			while (i.hasNext()) {
				RepairPart part = i.next();
				double prob = part.getRepairProbability();
				totalProb += prob;	
				if (part.getName().equalsIgnoreCase(partName)) {
					// Record its old repair probability
					oldRepairProb = prob;
					// Record the number of this repair part
					num = part.getNumber();
				}
			}

			// No parts found
			if (num == 0) {
				return oldRepairProb;
			}

			// Note that it should be proportional to oldRel divided by newRel
			// Calculate the new repair probability for this repair part
			double newRepairProb = Math.min(100, .9 * oldRepairProb + .1 * (oldRepairProb * oldRel / newRel * totalProb / 100));
			double diff = (newRepairProb - oldRepairProb);
			
			// Calculate the amount of spread for all other non-malfunction repair parts
			double spreadRepairProb = diff / num;
			
			Iterator<RepairPart> j = partList.iterator();
			while (j.hasNext()) {
				RepairPart part = j.next();
				double prob = part.getRepairProbability();
				totalProb += prob;	
				if (part.getName().equalsIgnoreCase(partName)) {
					// If this repair part has a malfunction
					part.setRepairProbability(newRepairProb);
				}
				else {
					// If this is a non-malfunction repair part (doesn't have a malfunction)
					double oldProb = part.getRepairProbability();
					// Still need to adjust the probability of part due to the malfunctioned repair part
					double newProb = (oldProb + spreadRepairProb) * totalProb / 100;		
					part.setRepairProbability(newProb);
				}
			}
			
			return newRepairProb;
		}
		
		return 0;
	}
	
	
	/**
	 * Sets up a malfunction event.
	 *
	 * @param malfunction
	 * @param actor
	 */
	private void registerAMalfunction(Malfunction malfunction, Unit actor) {
		EventType eventType = EventType.MALFUNCTION_PARTS_FAILURE;

		String whoAffected = "None";
		String whileDoing = "N/A";
	
		if (actor != null) {

			if (actor.getUnitType() == UnitType.PERSON) {
				eventType = EventType.MALFUNCTION_HUMAN_FACTORS;
				whileDoing = ((Person)actor).getTaskDescription();
				whoAffected = actor.getName();
			}
			else if (actor.getUnitType() == UnitType.ROBOT) {
				eventType = EventType.MALFUNCTION_PROGRAMMING_ERROR;
				whileDoing = ((Robot)actor).getTaskDescription();
				whoAffected = actor.getName();
			}
			else if (actor.getUnitType() == UnitType.BUILDING) {
				if (malfunction.getMalfunctionMeta().getName().contains(MalfunctionFactory.METEORITE_IMPACT_DAMAGE)) {
					eventType = EventType.HAZARD_ACTS_OF_GOD;
					whileDoing = "";
					whoAffected = "N/A";
				}
				else {
					eventType = EventType.MALFUNCTION_PARTS_FAILURE;
					whileDoing = "";
					whoAffected = "N/A";
				}
			}
			else if (actor.getUnitType() == UnitType.EVA_SUIT) {
				eventType = EventType.MALFUNCTION_PARTS_FAILURE;
				whileDoing = ""; 
				whoAffected = ((EVASuit)actor).getContainerUnit().getName();
			}
			else {
				eventType = EventType.MALFUNCTION_PARTS_FAILURE;
				whileDoing = "";
				whoAffected = actor.getName();
			}
		}

		HistoricalEvent newEvent = new MalfunctionEvent(
								eventType, 
								malfunction, 
								whileDoing, 
								whoAffected, 
								(Unit) entity);
		
		eventManager.registerNewEvent(newEvent);

		if (eventType.getName().equalsIgnoreCase("Acts of God"))
			logger.log(entity, Level.WARNING, 0, 
								malfunction.getName()
								+ PROBABLE_CAUSE 
								+ eventType.getName() 
								+ ".");
		else
			logger.log(entity, Level.WARNING, 0, 
					malfunction.getName()
					+ PROBABLE_CAUSE 
					+ eventType.getName() 
					+ "."
					+ (actor != null ? CAUSED_BY + whoAffected + "'." : "."));
	}

	/**
	 * Time passing for tracking the wear and tear condition while the unit is being actively used.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	public void activeTimePassing(ClockPulse pulse) {
		double time = pulse.getElapsed();
		
		// Updates params
		cumulativeTime += time;
		effTimeSinceLastMaint += time;
		currentWearLifeTime -= time * RandomUtil.getRandomDouble(.75, 1.25);
		if (currentWearCondPercent < 0D)
			currentWearCondPercent = 0D;
		currentWearCondPercent = currentWearLifeTime/baseWearLifeTime * 100;

		cumulativeFatigue += time;
		if (cumulativeFatigue > 1) {
			
			double portion = cumulativeFatigue/2;
			
			// Choose two scopes
			String firstScope = pickOneScope();
			if (firstScope != null) {
				injectFatigue(firstScope, portion);
			}
			
			String secondScope = pickOneScope();
			if (secondScope != null) {
				injectFatigue(secondScope, portion);
			}
			
			cumulativeFatigue = 0;
		}
		
		
		if (pulse.isNewIntMillisol()
				&& pulse.getMarsTime().getMillisolInt() % FREQUENCY * RandomUtil.getRandomInt(-4, 4) == 0) {
			
			delay--;
			
			if (delay > 0) {
				// This is to prevent from a series of malfunction occurring back to back.
				// Spacing out malfunction will give settlers enough time to respond 
				// to a malfunction or maintenance event 
				return;
			}
			
			double inspectFactor = (effTimeSinceLastMaint/standardInspectionWindow) + .1D;
			double wearFactor = (100 - currentWearCondPercent) * WEAR_MALFUNCTION_FACTOR;		
			double malfunctionChance = time * inspectFactor * wearFactor; // * FREQUENCY;
//			malfunctionProbability = malfunctionChance;
//			logger.info(entity, "MalfunctionChance min: " + Math.round(malfunctionChance * 100_000.0)/100_000.0 + " %");
			
			// For one orbit, log10 (1.000_001) * 1000 * 687 is 0.2984. 
			// This results in ~0.3%, a reasonable lower limit. 
			// Or use 1.000_003_351_695 to result in 1 %
			
			// If log10 (1.000_335_2215) * 1000 * 687 is 100.0000
			// This results in 100 % certainty (the upper limit) that it will have a malfunction.
			
			malfunctionProbability = 1.0 - Math.exp(-malfunctionChance) ;
			
//			malfunctionProbability = Math.log(MathUtils.between(malfunctionChance, MALFUNCTION_LOWER_LIMIT, UPPER_LIMIT));
//			logger.info(entity, "MalfunctionChance log10: " + Math.round(malfunctionChance * 100_000.0)/100_000.0 + " %");
				
			boolean hasMal = false;
			// Check for malfunction due to lack of maintenance and wear condition.
			if (time > 0 && RandomUtil.lessThanRandPercent(malfunctionProbability)) {
				// Reset delay back to MAX_DELAY. 
				delay = MAX_DELAY;
	
				// Note: call selectMalfunction is just checking for the possibility 
				// of having malfunction and doesn't necessarily mean it has to result in a malfunction
				hasMal = selectMalfunction((Unit)entity);
			}

			// FUTURE : how to connect maintenance to field reliability statistics of parts used in this units	
			
			if (hasMal) {
				// Note: If it already has a malfunction in this tick,
				// do inhibit any task of inspection over this entity for maintenance so that 
				// settlers can handle the stress.
				return;
			}
		}
	}
		
	/**
	 * Randomly picks one of the scopes.
	 * 
	 * @return
	 */
	private String pickOneScope() {
		// Pick a scope
		int rand = RandomUtil.getRandomInt(scopes.size() - 1);
		return scopes.stream()
				     .skip(rand)
				     .findFirst()
				     .orElse(null);
	}
	
	/**
	 * Randomly picks one of the scopes that has a proportionally high fatigue
	 * 
	 * @return
	 */
	private String pickOneScopeHighFatigue() {
		double highestFatigue = 0;
		String selectedScope = null;
		for (String scope: scopes) {
			double sumFatigue = 0;
			List<MaintenanceScope> list = scopeMap.get(scope);
			for (MaintenanceScope ms: list) {
				sumFatigue += ms.getFatigue();
			}
			if (sumFatigue > highestFatigue) {
				selectedScope = scope;
				highestFatigue = sumFatigue;
			}
		}
		return selectedScope;
	}
	
	/**
	 * Performs the inspection and generate repair parts. 
	 * 
	 * @param time
	 */
	public void performInspection(double time) {
	
		// Note 1: the need for maintenance should definitely have a higher chance than the onset of malfunction
		// Note 2: numberMaintenances increases the chance of having maintenance again
		//         because indicates how many times it has been "patched" up
		//         But if broken parts have been swapped out, should it lower the maintenance chance ? 
		
		// Question: when should numberMaintenances be lower ?
		
		double maintenanceChance = malfunctionProbability * (1 + numberMaintenances/5.0) * MAINT_TO_MAL_RATIO;
//			maintenanceProbability = maintenanceChance;
//			logger.info(entity, "maintenanceChance: " + Math.round(maintenanceChance * 100_000.0)/100_000.0 + " %");

		maintenanceProbability = MathUtils.between(maintenanceChance, MAINTENANCE_LOWER_LIMIT, 2 * UPPER_LIMIT);
//			logger.info(entity, "maintenanceChance log10: " + Math.round(maintenanceChance * 100_000.0)/100_000.0 + " %");
		
		// Check for repair items needed due to lack of maintenance and wear condition.
		if (time > 0 && RandomUtil.lessThanRandPercent(maintenanceProbability)) {
			// Reset delay back to MAX_DELAY. 
			delay = MAX_DELAY;

			// Note: call determineNewMaintenanceParts is just checking for the possibility 
			// of having needed repair parts and doesn't necessarily result in generating parts 
			// that need maintenance
			
			// If partsNeededForMaintenance has already been generated,
			// do NOT do it again so as to allow enough time for 
			// settlers to respond to the previous maintenance task order
			if (partsNeededForMaintenance == null || partsNeededForMaintenance.isEmpty()) {	
				
				logger.warning(entity, 0, "Maintenance flagged: " + maintenanceProbability);
				// Generates the repair parts 
				generateNewMaintenanceParts();
				// Retrieves the parts right away
				entity.getAssociatedSettlement().getBuildingManager().retrieveMaintParts(entity);
			}
		}
	}

	/**
	 * Gets the malfunction probability %.
	 * Note: Assumes malfunctionProbability already multiplied by the pulse width in millisols.  
	 * Each sol has 1000 millisols and each orbit has 668.6 sols.
	 * 
	 * @return probability
	 */
	public double getMalfunctionProbability() {
		return malfunctionProbability;
	}
	
	/**
	 * Gets the maintenance probability %.
	 * Note: Assumes maintenanceProbability already multiplied by the pulse width in millisols. 
	 * Each sol has 1000 millisols and each orbit has 668.6 sols.
	 * 
	 * @return probability 
	 */
	public double getMaintenanceProbability() {
		return maintenanceProbability;
	}
	
	/**
	 * Time passing for tracking resources of the entity.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		double time = pulse.getElapsed();

		if (entity.getUnitType() == UnitType.BUILDING
				|| entity.getUnitType() == UnitType.EVA_SUIT) {
			// Check if life support modifiers are still in effect.
			setLifeSupportModifiers(time);
			// Check if resources is still draining
			depleteResources(time);
		}

		return true;
	}

	/**
	 * Resets one or more flow modifier.
	 *
	 * @param type
	 */
	private void resetModifiers(int type) {
		// compare from previous modifier
		if (type == 0) {
			oxygenFlowModifier = 100D;
			logger.log(entity, Level.WARNING, 20_000L, "The oxygen flow retrictor had been fixed.");
		}
	}

	/**
	 * Removes a malfunction that has been fixed.
	 * 
	 * @param fixed Malfunction fixed. 
	 */
	void removeFixedMalfunction(Malfunction fixed) {
		if (!malfunctions.remove(fixed)) {
			logger.warning(entity, 20_000L, "Fixed '" + fixed.getName() + "'.");
		}
		else {
			Map<String, Double> effects = fixed.getLifeSupportEffects();
			if (!effects.isEmpty()) {
				if (effects.containsKey(OXYGEN))
					resetModifiers(0);
			}

			getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, fixed);

			String chiefRepairer = fixed.getMostProductiveRepairer();

			HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_FIXED, fixed,
					null, chiefRepairer, 
					(Unit) entity);

			eventManager.registerNewEvent(newEvent);

			logger.log(entity, Level.INFO, 20_000L, "The malfunction '" + fixed.getName() + "' had been dealt with.");
		}
	}


	/**
	 * Determines life support modifiers for given time.
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
				
					if (entity instanceof Building building
						 && building.isInhabitable()) {
							// If this entity is a building and it has no life support,
							// there is no need to look at life support leaking
							return;
					}
					
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
				if (oxygenFlowModifier < 100) {
					logger.log(entity, Level.WARNING, 20_000, "Oxygen flow restricted to "
								+ Math.round(oxygenFlowModifier * 10.0)/10.0 + " % capacity.");
				}
			}
		}
	}

	/**
	 * Depletes resources due to malfunctions.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error depleting resources.
	 */
	private void depleteResources(double time) {

		if (hasMalfunction()) {
			for (Malfunction malfunction : malfunctions) {
				if (!malfunction.isFixed() && !malfunction.getResourceEffects().isEmpty()) {
					// Resources are depleted according to how much of the repair is remaining
					double percent = (100.0 - malfunction.getPercentageFixed())/100D;
					for (Entry<Integer, Double> entry : malfunction.getResourceEffects().entrySet()) {
						Integer resource = entry.getKey();
						double amount = entry.getValue();
						double amountDepleted = amount * time * percent / 100;
						ResourceHolder rh = (ResourceHolder)entity;
						double amountStored = rh.getSpecificAmountResourceStored(resource);

						if (amountStored < amountDepleted) {
							amountDepleted = amountStored;
						}
						if (amountDepleted >= 0) {
							rh.retrieveAmountResource(resource, amountDepleted);
							logger.log(entity, Level.WARNING, 15_000L, "Leaking "
											+ Math.round(amountDepleted * 100.0)/100.0 + " kg of  "
											+ ResourceUtil.findAmountResource(resource) + ".");
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a series of related malfunctions.
	 * 
	 * @param location the place of accident
	 * @param r the Worker who triggers the malfunction
	 */
	public void createASeriesOfMalfunctions(String location, Unit actor) {
		int nervousness = SCORE_DEFAULT;
		if (actor instanceof Person) {
			Person p = (Person) actor;
			nervousness = p.getMind().getTraitManager()
					.getPersonalityTrait(PersonalityTraitType.NEUROTICISM);
		}
		determineNumOfMalfunctions(location, nervousness, actor);
	}

	/**
	 * Determines the numbers of malfunctions.
	 *
	 * @param type  the type of malfunction
	 * @param s     the place of accident
	 * @param actor the person/robot who triggers the malfunction
	 */
	private void determineNumOfMalfunctions(String location, int score, Unit actor) {
		// Multiple malfunctions may have occurred.
		// 50% one malfunction, 25% two etc.
		boolean hasMal = false;
		boolean done = false;
		double chance = 100D;
		double mod = (double)score / SCORE_DEFAULT;
		while (!done) {
			if (RandomUtil.lessThanRandPercent(chance)) {
				hasMal = selectMalfunction(actor);
				chance = chance / 3D * mod;
			} else {
				done = true;
			}
		}

		if (hasMal) {
			String aType;
			if (location != null) {
				aType = "Type-I";
			}
			else {
				aType = "Type-II";
			}

			// More generic simplified log message
			logger.log(entity, Level.WARNING, 20_000L, "Accident " + aType + " occurred. " + CAUSED_BY
						 + actor.getName() + "'.");

			// Add stress to people affected by the accident.
			Collection<Person> people = entity.getAffectedPeople();
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				Person p = i.next();
//	            logger.info(p, 10_000, "Adding " + Math.round(ACCIDENT_STRESS * 100.0)/100.0 + " to the stress.");
				p.getPhysicalCondition().addStress(ACCIDENT_STRESS);
			}
		}
	}

	/**
	 * Gets the time the entity has been actively used since its last maintenance.
	 *
	 * @return time (in millisols)
	 */
	public double getEffectiveTimeSinceLastMaintenance() {
		return effTimeSinceLastMaint;
	}

	/**
	 * The standard inspection time window for this entity.
	 */
	public double getStandardInspectionWindow() {
		return standardInspectionWindow;
	}

	/**
	 * Gets the work time for each inspection/maintenance for the entity.
	 *
	 * @return time (in millisols)
	 */
	public double getBaseMaintenanceWorkTime() {
		return baseMaintWorkTime;
	}

	/**
	 * Gets the work time completed on the current inspection/maintenance.
	 *
	 * @return time (in millisols)
	 */
	public double getInspectionWorkTimeCompleted() {
		return inspectionTimeCompleted;
	}

	/**
	 * Gets how many maintenances have been performed.
	 * 
	 * @return
	 */
	public int getNumberOfMaintenances() {
		return numberMaintenances;
	}

	
	/**
	 * Inspects the entity and keeps track of the maintenance parts.
	 * 
	 * @param time
	 */
	public void inspectEntityTrackParts(double time) {
		
		Settlement containerUnit = entity.getAssociatedSettlement();
		
		boolean partsPosted = hasMaintenancePartsInStorage(containerUnit);
		
		if (partsPosted) {
			
			int shortfall = consumeMaintenanceParts((EquipmentOwner) containerUnit);
			
			if (shortfall == -1) {
				logger.warning(entity, 30_000L, "No spare part(s) available for maintenance on " 
						+ entity + ".");
			}
			else if (shortfall == 0) {
				logger.warning(entity, 30_000L, "No spare part posted yet on " 
						+ entity + ".");
			}
			else {
				logger.info(entity, 30_000L, "Spare part(s) consumed on a maintenance task on " 
						+ entity + ".");
			}
		}
		else {
			// Performs the inspection maintenance to see if any parts need to be replaced
			performInspection(time);
		}
	}
	
	/**
	 * Adds work time to inspection and maintenance.
	 *
	 * @param time (in millisols)
	 * @return Is more maintenance needed?
	 */
	public boolean addInspectionMaintWorkTime(double time) {
		boolean needsMore = true;
		
		inspectionTimeCompleted += time;
		// Check if work if done
		if (inspectionTimeCompleted >= baseMaintWorkTime) {
			// Reset the maint time to zero
			inspectionTimeCompleted = 0D;
			// Reset eff time since last inspection to zero
			effTimeSinceLastMaint = 0D;
			// Increment num of maintenance 
			numberMaintenances++;
			// Improve the currentWearlifetime
			// Note: Use a person's skill to gauge the quality of maintenance
			currentWearLifeTime += time * RandomUtil.getRandomDouble(LOW_QUALITY_INSPECTION, HIGH_QUALITY_INSPECTION);
			// Add uncertainty to the wear lifetime
			double uncertainty = RandomUtil.getRandomDouble(.95, 1);
			// Set a upper limit for currentWearLifeTime
			// Note: it would deteriorate over time and won't get back to baseWearLifeTime but it can improve somewhat
			if (currentWearLifeTime > baseWearLifeTime - cumulativeTime * uncertainty)
				currentWearLifeTime = baseWearLifeTime - cumulativeTime * uncertainty;
			
			needsMore = false;
		}

		// Question: when should numberMaintenances be lower ?
		
		return needsMore;
	}

	/**
	 * Gets the adjusted condition.
	 * 
	 * @return
	 */
	public double getAdjustedCondition() { 
		// Compare with currentWearCondPercent = currentWearLifeTime/baseWearLifeTime * 100;
		return currentWearLifeTime / (baseWearLifeTime + cumulativeTime) * 100;
	}

	/**
	 * Reduces the current wear life time param.
	 * 
	 * @param fraction
	 */
	public void reduceWearLifeTime(double fraction) {
		currentWearLifeTime = .25 * currentWearLifeTime + .75 * (1 - fraction) * currentWearLifeTime;
	}
	
	/**
	 * Issues any necessary medical complaints.
	 *
	 * @param malfunction the new malfunction
	 */
	private void issueMedicalComplaints(Malfunction malfunction) {

		// Determine medical complaints for each malfunction.
		for (Entry<ComplaintType, Double> impact : malfunction.getMedicalComplaints().entrySet()) {
			// Replace the use of String name with ComplaintType
			Complaint complaint = medic.getComplaintByName(impact.getKey());
			if (complaint != null) {
				double probability = impact.getValue();

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


	/**
	 * Gets the unit associated with this malfunctionable.
	 *
	 * @return associated unit.
	 * @throws Exception if error finding associated unit.
	 */
	public Malfunctionable getEntity() {
		return entity;
	}

	/** 
	 * Gets the unit.
	 */
	private Unit getUnit() {
		if (entity instanceof Unit u)
			return u;
		else if (entity instanceof Building b)
			return b.getSettlement();
		else
			throw new IllegalStateException("Could not find unit associated with malfunctionable.");
	}

	/**
	 * Gets the maintenance scope list for a specific collection of scopes, e.g. type of vehicle or function.
	 * 
	 * @return
	 */
	public List<MaintenanceScope> getMaintenanceScopeList() {
		if (scopeCollection.containsKey(scopes)) {
			return scopeCollection.get(scopes);
		}
		else {
			List<MaintenanceScope> results = new ArrayList<>();
			for (String s : scopes) {
				List<MaintenanceScope> list = scopeMap.get(s.toLowerCase());
				if (list != null) {
					// It needs to allow duplicate MaintenanceScope objects
					results.addAll(list);
				}
			}
			scopeCollection.put(scopes, results); 
			return results ;
		}
	}
	
	/**
	 * Gets the maintenance scope list for a specific collection of scopes, e.g. type of vehicle or function.
	 * 
	 * @param scope a collection of possible scopes
	 * @return
	 */
	public List<MaintenanceScope> getMaintenanceScopeList(String scope) {
//		May add back for debugging: logger.info("scope: " + scope + "  scopeMap: " + scopeMap.keySet().toString());
		return scopeMap.getOrDefault(scope, Collections.emptyList());
	}
	
	/**
	 * Generates a new set of required repair parts for maintenance.
	 * Note: may or may not result in parts needed.
	 */
	public void generateNewMaintenanceParts() {
		if (partsNeededForMaintenance == null) {
			partsNeededForMaintenance = new HashMap<>();
		}
		
		String selectedScope = pickOneScopeHighFatigue();
		
		for (MaintenanceScope maintenance : scopeMap.get(selectedScope)) {
			if (RandomUtil.lessThanRandPercent(maintenance.getProbability())) {
				int number = RandomUtil.getRandomRegressionInteger(maintenance.getMaxNumber());
				
				if (partsNeededForMaintenance.containsKey(maintenance)) {
					number += partsNeededForMaintenance.get(maintenance);
				}
				partsNeededForMaintenance.put(maintenance, number);
			}
		}
		
		if (!partsNeededForMaintenance.isEmpty())
			logger.info(entity, 20_000L, "Maintenance parts needed: " 
						+ getPartsString(partsNeededForMaintenance)); 
	}

	
	/**
	 * Injects the fatigue time into the parts.
	 * 
	 * @param scope
	 * @param fatigue
	 */
	public void injectFatigue(String scope, double fatigue) {
		List<MaintenanceScope> list = scopeMap.get(scope);
		int num = list.size();
		double average = fatigue/num;
		double remaining = fatigue;
		for (MaintenanceScope ms: list) {
			Part part = ms.getPart();
			double failureRate = part.getFailureRate();
			double prob = ms.getProbability();
			double max = ms.getMaxNumber();
			for (int i=0; i<max; i++) {
				if (RandomUtil.lessThanRandPercent(prob)) {
					if (remaining > 0 && remaining >= average) {
						ms.addFatigue(failureRate * average);
						remaining -= average;
					}
					else if (remaining > 0) {
						ms.addFatigue(failureRate * remaining);
					}
				}
			}
		}
	}
	
	/**
	 * Resets the value of fatigue back to zero.
	 * 
	 * @param map
	 */
	public void resetPartFatigue(Map<MaintenanceScope, Integer> map) {
		for (MaintenanceScope ms: map.keySet()) {
			ms.resetFatigue();
		}
	}
	
	/**
	 * Looks at the parts needed for maintenance on this entity.
	 * Note: if parts don't exist, it simply means that one can still do the 
	 * inspection portion of the maintenance with no need of replacing any parts
	 *
	 * @return map of parts and their number.
	 */
	public Map<MaintenanceScope, Integer> getMaintenanceParts() {
		return partsNeededForMaintenance;
	}

	/**
	 * Checks if any parts are needed for maintenance on this entity.
	 *
	 * @return
	 */
	public boolean areMaintenancePartsNeeded() {
		if (partsNeededForMaintenance == null || partsNeededForMaintenance.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * Retrieves the parts needed for maintenance on this entity.
	 * Note: it doesn't automatically clear out partsNeededForMaintenance
	 * until closeoutMaintenanceParts() is being called.
	 *
	 * @return map of parts and their number.
	 */
	public Map<MaintenanceScope, Integer> retrieveMaintenancePartsFromManager() {
		if (partsNeededForMaintenance == null)
			partsNeededForMaintenance = new HashMap<>();
		if (!partsNeededForMaintenance.isEmpty()) {
			logger.info(entity, 30_000L, "Maintenance parts posted: " + getPartsString(partsNeededForMaintenance));
		}
		return partsNeededForMaintenance;
	}
	
	/**
	 * Call to check if any maintenance parts have been posted and also see if 
	 * they are available in a particular resource storage. 
	 * Note 1: only at least one part is required to trigger some level of maintenance. 
	 * Note 2: if parts don't exist, it simply means that one can still do the 
	 * inspection portion of the maintenance with no need of replacing any parts
	 * 
	 * @param partStore Store to provide parts
	 */
	public boolean hasMaintenancePartsInStorage(EquipmentOwner partStore) {
		boolean result = false;

		// Need to FIRST CHECK if any parts are due for maintenance in order to use this
		// method the right way. 
		
		// One should have checked that parts have been posted.
		boolean posted = areMaintenancePartsNeeded();
		
		if (!posted)
			return result;
		
		Map<MaintenanceScope, Integer> parts = getMaintenanceParts();
		
		for (Entry<MaintenanceScope, Integer> entry: parts.entrySet()) {
			MaintenanceScope ms = entry.getKey();
			int id = ms.getPart().getID();
			int number = entry.getValue();
			if (partStore.getItemResourceStored(id) >= number) {
				logger.info(entity, 30_000L, "Maintenance parts available: " 
						+ getPartsString(parts));
				result = result && true;
			}
			else {
				Good good = GoodsUtil.getGood(id);
                Part part = ItemResourceUtil.findItemResource(id);
				// Raise the demand on this item by a certain amount
				// Inject the demand onto this part
                if (entity.getAssociatedSettlement().getGoodsManager() != null)	 {
                	// Note: in MaintainGarageVehicleTest, good manager is null 
                	((PartGood)good).injectPartsDemand(part, entity.getAssociatedSettlement().getGoodsManager(), number);
                }
                return false;
			}
		}
		return result;
	}

	/**
	 * Consumes the maintenance parts.
	 * 
	 * @param partStore Store to retrieve parts from
	 * @return -1 if parts map is not available;
	 *         0 if all are available;
	 *         or # of shortfall parts
	 */
	public int consumeMaintenanceParts(EquipmentOwner partStore) {
		
		// Future: Can these spare parts be set aside first to avoid others using it ?
		
		Map<MaintenanceScope, Integer> parts = getMaintenanceParts();
		
		// Call building manager to check if the maintenance parts have been submitted	
		if (parts == null || parts.isEmpty())
			return -1;
		
		Map<MaintenanceScope, Integer> shortfallParts = new HashMap<>();
		
		int shortfall = 0;
		
		for (Entry<MaintenanceScope, Integer> entry: parts.entrySet()) {
			MaintenanceScope ms = entry.getKey();
			Part part = ms.getPart();
			int id = part.getID();
			int number = entry.getValue();
			int numMissing = partStore.retrieveItemResource(id, number);
			if (numMissing == 0) {
				logger.info(entity, 20_000L, "Retrieved " + number 
						+ " " + ItemResourceUtil.findItemResourceName(id) + ".");
			}
			// Any part still outstanding record for later
			if (numMissing > 0) {
				
				// Future: raise the demand on this part

				if (numMissing == number) {
					logger.info(entity, 20_000L, "Coudn't retrieve " + number + " " + ItemResourceUtil.findItemResourceName(id)
							+ ". None available. ");

				}
				else {
					logger.info(entity, 20_000L, "Missing " + numMissing + "/" + number 
							+ " " + ItemResourceUtil.findItemResourceName(id) + ".");
				}
				shortfallParts.put(ms, numMissing);
				shortfall = numMissing;
			}
		}

		entity.getAssociatedSettlement().getBuildingManager().updateMaintenancePartsMap(entity, shortfallParts);
		
		return shortfall;
	}
		
		
	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	public static String getPartsString(Map<MaintenanceScope, Integer> parts) {

		StringBuilder buf = new StringBuilder();
		if (!parts.isEmpty()) {
			boolean first = true;
			for(Entry<MaintenanceScope, Integer> entry : parts.entrySet()) {
				if (!first) {
					buf.append(", ");
				}
				first = false;
				MaintenanceScope ms = entry.getKey();
				Part part = ms.getPart();
				int number = entry.getValue();
				buf.append(number).append(" ")
						.append(part.getName());
			}
			buf.append(".");
		} else
			buf.append("Empty.");
		
		return buf.toString();
	}
	
	/**
	 * Gets the repair part probabilities for the malfunctionable.
	 *
	 * @return maps of parts and probable number of parts needed per malfunction.
	 * @throws Exception if error finding probabilities.
	 */
	public Map<Integer, Double> getRepairPartProbabilities() {
		return MalfunctionFactory.getRepairPartProbabilities(scopes);
	}

	/**
	 * Gets the estimated number of malfunctions this entity will have in one
	 * Martian orbit.
	 *
	 * @return number of malfunctions.
	 */
	public double getEstimatedNumberOfMalfunctionsPerOrbit() {
		double avgMalfunctionsPerOrbit = 0D;
		double totalTimeOrbits = getElapsedOrbits();

		if (totalTimeOrbits < 1D) {
			avgMalfunctionsPerOrbit = (numberMalfunctions + ESTIMATED_MALFUNCTIONS_PER_ORBIT) / 2D;
		} else {
			avgMalfunctionsPerOrbit = (1 + numberMalfunctions) / totalTimeOrbits;
		}

		int orbit = masterClock.getMarsTime().getOrbit();
		if (orbitCache != orbit) {
			orbitCache = orbit;
			numberMalfunctions = 0;
		}
		logger.info(entity, 20_000L, "avgMalfunctionsPerOrbit: " + Math.round(avgMalfunctionsPerOrbit * 100.0)/100.0);
		return avgMalfunctionsPerOrbit;
	}

	private static double getElapsedOrbits() {
		double totalTimeMillisols = masterClock.getMarsTime().getTimeDiff(masterClock.getInitialMarsTime());
		return totalTimeMillisols / 1000D / MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
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
	 * Gets the percentage representing the malfunctionable's condition from wear &
	 * tear. 100% = new condition 0% = worn out condition.
	 *
	 * @return wear condition.
	 */
	public double getWearCondition() {
		return currentWearCondPercent;
	}

	/**
	 * Gets the multiplying modifier for the chance of an accident due to the
	 * malfunctionable entity's wear condition. From 0 to 1
	 *
	 * @return accident modifier. 0 means no change whilst 1 means full chance
	 */
	public double getAccidentModifier() {
		if (noFailures) {
			return 0D;
		}
		return ((100D - currentWearCondPercent) / 100D) * WEAR_ACCIDENT_FACTOR;
	}

	/**
	 * Initializes instances after loading from a saved sim.
	 *
	 * @param c0 {@link MasterClock}
	 * @param mf {@link MalfunctionFactory}
	 * @param m {@link MedicalManager}
	 * @param e {@link HistoricalEventManager}
	 */
	public static void initializeInstances(MasterClock c0, MalfunctionFactory mf,
										   MedicalManager mm, HistoricalEventManager em, PartConfig pc) {
		masterClock = c0;
		factory = mf;
		medic = mm;
		eventManager = em;
		partConfig = pc;
	}

	/**
	 * Sets the global flag to stop any failures or accidents.
	 */
	public static void setNoFailures(boolean newFlag) {
		noFailures = newFlag;
//		logger.info("No failures flag set to " + noFailures);
	}

	/**
	 * Prepares object for garbage collection.
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
