/*
 * Mars Simulation Project
 * MalfunctionManager.java
 * @date 2023-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.PersonalityTraitType;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.MaintenanceScope;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;

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

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MalfunctionManager.class.getName());

	
	/** The upper limit factor for both malfunction and maintenance that will result in 100% certainty. */
	private static final double UPPER_LIMIT = 1.000_335_221_5;
	/** The lower limit factor for malfunction that will result in 1 % certainty. */	
	private static final double MALFUNCTION_LOWER_LIMIT = 1.000_003_351_695;
	/** The lower limit factor for maintenance that will result in 10 % certainty. */	
	private static final double MAINTENANCE_LOWER_LIMIT = 1.000_033_516_95;

	
	/** Wear-and-tear points earned from a low quality inspection. */
	private static final double LOW_QUALITY_INSPECTION = 200;
	/** Wear-and-tear points earned from a high quality inspection. */
	private static final double HIGH_QUALITY_INSPECTION = 2000;
	
	/** Stress jump resulting from being in an accident. */
	private static final double ACCIDENT_STRESS = 10D;
	/** Modifier for number of parts needed for a trip. */
	public static final double PARTS_NUMBER_MODIFIER = 7.5;
	/** Estimate number of broken parts per malfunctions */
	public static final double AVERAGE_NUM_MALFUNCTION = 2.5;
	/** Estimate number of broken parts per malfunctions for EVA suits. */
	public static final double AVERAGE_EVA_MALFUNCTION = 2.0;

	/** Initial estimate for malfunctions per orbit for an entity. */
	private static final double ESTIMATED_MALFUNCTIONS_PER_ORBIT = 100;
	/** Initial estimate for maintenances per orbit for an entity. */
	private static final double ESTIMATED_MAINTENANCES_PER_ORBIT = 2000;
	/** Factor for chance of malfunction by time since last maintenance. */
	private static final double MAINTENANCE_FACTOR = 10;
	/** Factor for chance of malfunction due to wear condition. */
	private static final double WEAR_MALFUNCTION_FACTOR = .0015;
	/** Factor for chance of accident due to wear condition. */
	private static final double WEAR_ACCIDENT_FACTOR = 1D;

	private static final String OXYGEN = "Oxygen";
	private static final String PROBABLE_CAUSE = ". Probable Cause: ";
	private static final String CAUSED_BY = " Caused by '";

	private static final int SCORE_DEFAULT = 50;
	private static final int MAX_DELAY = 200;
	
	// Data members
	private int delay = 0;
	/** The number of malfunctions the entity has had so far. */
	private int numberMalfunctions;
	/** The number of times the entity has been maintained so far. */
	private int numberMaintenances;
	/** The number of orbits. */
	private int orbitCache = MarsTime.FIRST_ORBIT;
	
	/** The overall probability that a maintenance event is triggered by active use on this entity. */
	private double maintenanceProbability;	
	/** The overall probability that a malfunction is triggered by active use on this entity. */
	private double malfunctionProbability;
	/** Time passing (in millisols) since last maintenance on entity. */
	private double timeSinceLastMaintenance;
	/**
	 * Time (millisols) that entity has been actively used since last maintenance.
	 */
	private double effTimeSinceLastMaint;
	/** The required work time for maintenance on entity. */
	private double maintWorkTime;
	/** The completed. */
	private double maintenanceTimeCompleted;
	/** The periodic time window between each inspection/maintenance.  */
	private double maintPeriod;
	/** The percentage of the malfunctionable's condition from wear and tear. 0% = worn out -> 100% = new condition. */
	private double currentWearCond;
	/** The cumulative time [in millisols] since active use. */
	private double cumulativeTime;
	
	/**
	 * The expected life time [in millisols] of active use before the malfunctionable
	 * is worn out.
	 */
	private final double baseWearLifeTime;
	/**
	 * The current life time [in millisols] of active use
	 */
	private double currentWearLifeTime;

	// Life support modifiers.
	private double oxygenFlowModifier = 100D;

	/** The owning entity. */
	private Malfunctionable entity;

	/** The collection of affected scopes. */
	private Set<String> scopes;
	/** The current malfunctions in the unit. */
	private List<Malfunction> malfunctions;
	/** The parts currently identified to be retrofitted. */
	private Map<Integer, Integer> partsNeededForMaintenance;

	private boolean supportInsideRepair = true;
	
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
		malfunctions = new CopyOnWriteArrayList<>();
		
		this.maintWorkTime = maintWorkTime;
		this.baseWearLifeTime = wearLifeTime;

		// Assume the maintenance period [in millisols] is the recommended period of time between the last and the next inspection/maintenance 
		this.maintPeriod = wearLifeTime * 0.03;
		
		// deploymentime accounts for the pre-use time spent during the base deployment phase prior to the start of the sim
		// FUTURE: vary this time according to deployment scenario.
		// Usually power building gets deployed first.
		// Next is in-situ resource building such as ERV 
		int deploymentTime = 0;
		if (UnitType.EVA_SUIT != entity.getUnitType()
			|| UnitType.VEHICLE != entity.getUnitType()) {
			deploymentTime = (int) Math.round(maintPeriod/20 + RandomUtil.getRandomDouble(maintPeriod/40));
		}
		
		currentWearLifeTime = wearLifeTime - deploymentTime;
		cumulativeTime = deploymentTime;
		effTimeSinceLastMaint = deploymentTime;
		timeSinceLastMaintenance = deploymentTime;
		
		currentWearCond = currentWearLifeTime/baseWearLifeTime * 100D;
//		logger.info(entity, 10_000L, "wearLifeTime: " + wearLifeTime 
//				+ "  maintPeriod: " + maintPeriod
//				+ "  maintWorkTime: " + maintWorkTime
//				+ "  preUseTime: " + deploymentTime
//				);
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
			triggerMalfunction(malfunction, true, actor);
			result = true;
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
//		else if (m.getName().equalsIgnoreCase(MalfunctionFactory.METEORITE_IMPACT_DAMAGE)) { 
//			logger.info(actor, "'" + malfunction.getName() + "' needs no repair parts.");	
//		}
		
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
		for (Entry<Integer, Integer> p : malfunction.getRepairParts().entrySet()) {	
	
			// Compute the new reliability and failure rate for this malfunction
			Part part = ItemResourceUtil.findItemResource(p.getKey());
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
	
	
//	/**
//	 * Sets the probability of a repair part for a malfunction.
//	 *
//	 * @param malfunctionName the name of the malfunction.
//	 * @param partName        the name of the part.
//	 * @return probability of failure of a particular repair part.
//	 */
//	public void setRepairPartProbability(Malfunction malfunction, String partName, double repairProbability) {
//		List<RepairPart> partList = malfunction.getMalfunctionMeta().getParts();
//		if (partList != null) {
//			Iterator<RepairPart> i = partList.iterator();
//			while (i.hasNext()) {
//				RepairPart part = i.next();
//				if (part.getName().equalsIgnoreCase(partName)) {
//					part.setRepairProbability(repairProbability);
//					break;
//				}
//			}
//		}
//	}
	
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
					whoAffected = "";
				}
				else {
					eventType = EventType.MALFUNCTION_PARTS_FAILURE;
					whileDoing = "";
					whoAffected = "";
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

		if (whoAffected.equalsIgnoreCase(""))
			whoAffected = "N/A";
		
		HistoricalEvent newEvent = new MalfunctionEvent(
								eventType, 
								malfunction, 
								whileDoing, 
								whoAffected, 
								(Unit) entity);
		
		eventManager.registerNewEvent(newEvent);

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
		timeSinceLastMaintenance += time;
		currentWearLifeTime -= time * RandomUtil.getRandomDouble(.5, 1.5);
		if (currentWearCond < 0D)
			currentWearCond = 0D;
		currentWearCond = currentWearLifeTime/baseWearLifeTime * 100;

		if (pulse.isNewMSol()
				&& pulse.getMarsTime().getMillisolInt() % 3 == 0) {
			
			delay--;
			
			if (delay > 0) {
				// This is to prevent from a series of malfunction occurring back to back.
				// Spacing out malfunction will give settlers enough time to respond 
				// to a malfunction or maintenance event 
				return;
			}
			
			double maintFactor = (effTimeSinceLastMaint/maintPeriod) + 1D;
			double wearFactor = (100 - currentWearCond) * WEAR_MALFUNCTION_FACTOR;		
			double malfunctionChance = time * maintFactor * wearFactor;
//			logger.info(entity, "MalfunctionChance min: " + Math.round(malfunctionChance * 100_000.0)/100_000.0 + " %");
			
			// For one orbit, log10 (1.000001) * 1000 * 687 is 0.2984. 
			// This results in ~0.3%, a reasonable lower limit. 
			// Or use 1.000003351695 to result in 1 %
			
			// If log10 (1.0003352215) * 1000 * 687 is 100.0000
			// This results in 100 % certainty (the upper limit) that it will have a malfunction.
			malfunctionProbability = Math.log10(Math.min(UPPER_LIMIT, Math.max(MALFUNCTION_LOWER_LIMIT, malfunctionChance)));
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
				// If it already has a malfunction in this tick,
				// do not trigger maintenance even again so that 
				// settlers can handle the stress.
				return;
			}
			
			// Note: the need for maintenance should definitely have a higher chance than the onset of malfunction
			// numberMaintenances increases the chance of having maintenance again
			// because indicates how many times it has been "patched" up
			double maintenanceChance = malfunctionChance * Math.log10(10 + numberMaintenances) * MAINTENANCE_FACTOR;
//			logger.info(entity, "maintenanceChance: " + Math.round(maintenanceChance * 100_000.0)/100_000.0 + " %");
			// For one orbit, log10 (1.00003351695) * 1000 * 687 is 10.0000. 10 %
			// Use 1.00003351695 to get 10% as a reasonable lower limit

			// If log10 (1.0003352215) * 1000 * 687 is 100.0000. 100 %
			// This results in 100 % certainty (the upper limit) that it will have a malfunction.
			maintenanceProbability = Math.log10(Math.min(UPPER_LIMIT, Math.max(MAINTENANCE_LOWER_LIMIT, maintenanceChance)));
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
					// Generates the repair parts 
					generateNewMaintenanceParts();
				}
			}
		}
	}

	/**
	 * Gets the malfunction probability per orbit.
	 * Note: Assumes checking this probability once per millisol. 
	 * Each sol has 1000 millisols and each orbit has 668.6 sols.
	 * 
	 * @return
	 */
	public double getMalfunctionProbabilityPerOrbit() {
		return malfunctionProbability * 1000 * MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
	}
	
	/**
	 * Gets the maintenance probability per orbit. 
	 * Note: Assumes checking this probability once per millisol. 
	 * Each sol has 1000 millisols and each orbit has 668.6 sols.
	 * 
	 * @return
	 */
	public double getMaintenanceProbabilityPerOrbit() {
		return maintenanceProbability * 1000 * MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
	}
	
	/**
	 * Time passing for unit.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		double time = pulse.getElapsed();

		// Check if life support modifiers are still in effect.
		setLifeSupportModifiers(time);

		// Check if resources is still draining
		depleteResources(time);

		// Add time passing.
		timeSinceLastMaintenance += time;

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
			logger.warning(entity, 20_000L, "Fixed malfunction is unknown " + fixed.getName() + ".");
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
				
					if (entity instanceof Building building) {
						if (building.isInhabitable()) {
							// If this entity is a building and it has no life support,
							// there is no need to look at life support leaking
							return;
						}
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
				logger.log(entity, Level.WARNING, 20_000, "Oxygen flow restricted to "
								+ Math.round(oxygenFlowModifier * 10.0)/10.0 + " % capacity.");
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
						double amountStored = rh.getAmountResourceStored(resource);

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
			nervousness = p.getMind().getTraitManager().getPersonalityTrait(PersonalityTraitType.NEUROTICISM);
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

			// More generic simplifed log message
			logger.log(entity, Level.WARNING, 20_000L, "Accident " + aType + " occurred. " + CAUSED_BY
						 + actor.getName() + "'.");

			// Add stress to people affected by the accident.
			Collection<Person> people = entity.getAffectedPeople();
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				i.next().getPhysicalCondition().addStress(ACCIDENT_STRESS);
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
		return effTimeSinceLastMaint;
	}

	/**
	 * The regular maintenance period for this component.
	 */
	public double getMaintenancePeriod() {
		return maintPeriod;
	}

	/**
	 * Gets the required work time for maintenance for the entity.
	 *
	 * @return time (in millisols)
	 */
	public double getMaintenanceWorkTime() {
		return maintWorkTime;
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
	 * Adds work time to maintenance.
	 *
	 * @param time (in millisols)
	 */
	public void addMaintenanceWorkTime(double time) {
		maintenanceTimeCompleted += time;
		// Check if work if done
		if (maintenanceTimeCompleted >= maintWorkTime) {
			// Reset the maint time to zero
			maintenanceTimeCompleted = 0D;
			// Reset time last inspection to zero
			timeSinceLastMaintenance = 0D;
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
		}
	}

	public double getAdjustedCondition() { 
		return currentWearLifeTime / (baseWearLifeTime - cumulativeTime) * 100;
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
	 * What Unit is used to fire events
	 */
	private Unit getUnit() {
		if (entity instanceof Unit)
			return (Unit) entity;
		else if (entity instanceof Building)
			return ((Building) entity).getSettlement();
		else
			throw new IllegalStateException("Could not find unit associated with malfunctionable.");
	}

	/**
	 * Generates a new set of required repair parts for maintenance.
	 * Note: may or may not result in parts needed.
	 */
	public void generateNewMaintenanceParts() {
		if (partsNeededForMaintenance == null) {
			partsNeededForMaintenance = new ConcurrentHashMap<>();
		}
		
		for (MaintenanceScope maintenance : partConfig.getMaintenance(scopes)) {
			if (RandomUtil.lessThanRandPercent(maintenance.getProbability())) {
				int number = RandomUtil.getRandomRegressionInteger(maintenance.getMaxNumber());
				int id = maintenance.getPart().getID();
				if (partsNeededForMaintenance.containsKey(id)) {
					number += partsNeededForMaintenance.get(id);
				}
				partsNeededForMaintenance.put(id, number);
			}
		}
		
		if (!partsNeededForMaintenance.isEmpty())
			logger.info(entity, 20_000L, "Maintenance parts due: " 
						+ getPartsString(partsNeededForMaintenance)); 
	}

	/**
	 * Looks at the parts needed for maintenance on this entity.
	 * Note: if parts don't exist, it simply means that one can still do the 
	 * inspection portion of the maintenance with no need of replacing any parts
	 *
	 * @return map of parts and their number.
	 */
	public Map<Integer, Integer> getMaintenanceParts() {
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
	public Map<Integer, Integer> retrieveMaintenancePartsFromManager() {
		if (partsNeededForMaintenance == null)
			partsNeededForMaintenance = new HashMap<>();
		if (!partsNeededForMaintenance.isEmpty()) {
			logger.info(entity, 20_000L, "Maintenance parts posted: " 
						+ getPartsString(partsNeededForMaintenance));
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
		Map<Integer, Integer> parts = getMaintenanceParts();
		
		// Need to FIRST CHECK if any parts are due for maintenance in order to use this
		// method the right way. 
		
		// One should have checked that parts have been posted.
		boolean posted = areMaintenancePartsNeeded();
		
		if (!posted)
			return false;
		
		for (Entry<Integer, Integer> entry: parts.entrySet()) {
			Integer part = entry.getKey();
			int number = entry.getValue();
			if (partStore.getItemResourceStored(part) >= number) {
				logger.info(entity, 20_000L, "Maintenance parts available: " 
						+ getPartsString(parts));
				return true;
			}
		}
		return false;
	}

	/**
	 * Transfers the required parts for the maintenance from a part store.
	 * 
	 * @param partStore Store to retrieve parts from
	 * @return 1 if not parts map is not available;
	 *         0 if all are available;
	 *         or # of shortfall
	 */
	public int transferMaintenanceParts(EquipmentOwner partStore) {
		Map<Integer, Integer> parts = getMaintenanceParts();
		
		// Call building manager to check if the maintenance parts have been submitted	
		if (parts == null || parts.isEmpty())
			return -1;
		
		Map<Integer,Integer> shortfallParts = new HashMap<>();
		
		int shortfall = 0;
		
		for (Entry<Integer, Integer> entry: parts.entrySet()) {
			Integer part = entry.getKey();
			int number = entry.getValue();
			int numMissing = partStore.retrieveItemResource(part, number);
			if (numMissing == 0) {
				logger.info(entity, 20_000L, "Retrieved " + number + " " + ItemResourceUtil.findItemResourceName(part));
			}
			// Any part still outstanding record for later
			if (numMissing > 0) {
				if (numMissing == number) {
					logger.info(entity, 20_000L, "None available 0/" + number + " " + ItemResourceUtil.findItemResourceName(part));

				}
				else {
					logger.info(entity, 20_000L, "Missing " + numMissing + "/" + number + " " + ItemResourceUtil.findItemResourceName(part));
				}
				shortfallParts.put(part, numMissing);
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
	public static String getPartsString(Map<Integer, Integer> parts) {

		StringBuilder buf = new StringBuilder();
		if (!parts.isEmpty()) {
			boolean first = true;
			for(Entry<Integer, Integer> entry : parts.entrySet()) {
				if (!first) {
					buf.append(", ");
				}
				first = false;
				Integer part = entry.getKey();
				int number = entry.getValue();
				buf.append(number).append(" ")
						.append(ItemResourceUtil.findItemResource(part).getName());
			}
			buf.append(".");
		} else
			buf.append(" None.");

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
	 * Gets the repair part probabilities for maintenance.
	 *
	 * @return maps of parts and probable number of parts in case of maintenance.
	 * @throws Exception if error finding probabilities.
	 */
	public Map<Integer, Double> getMaintenancePartProbabilities() {
		return MalfunctionFactory.getMaintenancePartProbabilities(scopes);
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
	 * Gets the estimated number of periodic maintenances this entity will have in
	 * one Martian orbit.
	 *
	 * @return number of maintenances.
	 */
	public double getEstimatedNumberOfMaintenancesPerOrbit() {
		double avgMaintenancesPerOrbit = 0D;
		double totalTimeOrbits = getElapsedOrbits();
		if (totalTimeOrbits < 1D) {
			avgMaintenancesPerOrbit = (numberMaintenances + ESTIMATED_MAINTENANCES_PER_ORBIT) / 2D;
		} else {
			avgMaintenancesPerOrbit = (1 + numberMaintenances) / totalTimeOrbits;
		}
		logger.info(entity, 20_000L, "avgMaintenancesPerOrbit: " + Math.round(avgMaintenancesPerOrbit * 100.0)/100.0);
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
	 * Gets the percentage representing the malfunctionable's condition from wear &
	 * tear. 100% = new condition 0% = worn out condition.
	 *
	 * @return wear condition.
	 */
	public double getWearCondition() {
		return currentWearCond;
	}

	/**
	 * Gets the multiplying modifier for the chance of an accident due to the
	 * malfunctionable entity's wear condition. From 0 to 1
	 *
	 * @return accident modifier.
	 */
	public double getWearConditionAccidentModifier() {
		return (100D - currentWearCond) / 100D * WEAR_ACCIDENT_FACTOR;
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
