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
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The MalfunctionManager class manages malfunctions and part maintenance needs for units such as 
 * Building, BuildingKit, EVASuit, Robot, MockBuilding, or Vehicle. 
 * Note: each unit has its own MalfunctionManager.
 */
public class MalfunctionManager implements Serializable, Temporal {

	private static final String PERC_CHANGE = "%.1f %% --> %.1f %%";

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MalfunctionManager.class.getName());

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
	private static final double WEAR_MALFUNCTION_FACTOR = .05;
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
	private int orbitCache = MarsClock.FIRST_ORBIT;
	/** Time passing (in millisols) since last maintenance on entity. */
	private double timeSinceLastMaintenance;
	/**
	 * Time (millisols) that entity has been actively used since last maintenance.
	 */
	private double effTimeSinceLastMaint;
	/** The required work time for maintenance on entity. */
	private double maintenanceWorkTime;
	/** The completed. */
	private double maintenanceTimeCompleted;
	/** The periodic maintenance window */
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

	private boolean supportsInside = true;
	
	private static MasterClock masterClock;
	private static MarsClock currentTime;
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
	 * @param maintenanceWorkTime the amount of work time (millisols) required for
	 *                            maintenance.
	 */
	public MalfunctionManager(Malfunctionable entity, double wearLifeTime, double maintenanceWorkTime) {

		// Initialize data members
		this.entity = entity;

		scopes = new HashSet<>();
		malfunctions = new CopyOnWriteArrayList<>();
		
		this.maintenanceWorkTime = maintenanceWorkTime;
		this.baseWearLifeTime = wearLifeTime;

		// Assume the maintenace period is 1% of the component lifetime
		this.maintPeriod = wearLifeTime * 0.01D;

		// Assume that a random value since the last maintenance but biased
		// towards below the maintenance period
		double preUseTime = RandomUtil.getRandomDouble(maintPeriod * 1.1);
		currentWearLifeTime = wearLifeTime - preUseTime;
		cumulativeTime = preUseTime;
		effTimeSinceLastMaint = preUseTime;
		timeSinceLastMaintenance = preUseTime;
		
		currentWearCond = currentWearLifeTime/baseWearLifeTime * 100D;
	}

	/**
	 * Does this malfunctionable support inside repairs?
	 * 
	 * @param supported New inside repairs supported
	 */
	public void setSupportsInside(boolean supported) {
		this.supportsInside = supported;
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
		Malfunction malfunction = new Malfunction(this, factory.getNewIncidentNum(), m, supportsInside);

		malfunctions.add(malfunction);
		numberMalfunctions++;

		getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, malfunction);

		if (registerEvent) {
			registerAMalfunction(malfunction, actor);
		}

		if (malfunction.getRepairParts().isEmpty()) { 
			logger.info(actor, 20_000L, "'" + malfunction.getName() + "' needs no repair parts.");	
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
		// Register the failure of the Parts involved
		for (Entry<Integer, Integer> p : malfunction.getRepairParts().entrySet()) {
			int num = p.getValue();

			// Compute the new reliability and failure rate for this malfunction
			Part part = ItemResourceUtil.findItemResource(p.getKey());
			String partName = part.getName();

			double oldRel = part.getReliability();
			double oldProb = getRepairPartProbability(malfunction, partName);
			double oldFailure = (100 - oldRel) * oldProb / 100D;
			double oldMalProbFailure = malfunction.getProbability();
			double oldMTBF = part.getMTBF();

			// Record the number of failure
			// and recompute the new reliability
			part.setFailure(num, currentTime.getMissionSol());

			// Need to calculate the new probability for the whole MalfunctionMeta object
			double newRel = part.getReliability();
			double newProb = getRepairPartProbability(malfunction, partName);
			double newFailure = (100 - newRel) * newProb / 100D;
			double newMalProbFailure = (oldMalProbFailure + newFailure) / 2.0;
			double newMTBF = part.getMTBF();

			logger.warning("           *** Part : " + partName + " ***");
			logger.warning(" (1).   Reliability : " + String.format(PERC_CHANGE, oldRel, newRel));
			logger.warning(" (2).  Failure Rate : " + String.format(PERC_CHANGE, oldFailure, newFailure));
			logger.warning(" (3).          MTBF : " + String.format("%.1f hr --> %.1f hr", oldMTBF, newMTBF));
			logger.warning(" (4).   Probability : " + String.format(PERC_CHANGE, oldMalProbFailure, 
												newMalProbFailure));

			// Modify the probability of failure for this particular malfunction
			malfunction.setProbability(newMalProbFailure);
		}
	}
	
	/**
	 * Gets the probability of a repair part for a malfunction.
	 *
	 * @param malfunctionName the name of the malfunction.
	 * @param partName        the name of the part.
	 * @return the probability of the repair part.
	 */
	public double getRepairPartProbability(Malfunction malfunction, String partName) {
		double result = 0;
		List<RepairPart> partList = malfunction.getMalfunctionMeta().getParts();
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.getName().equalsIgnoreCase(partName)) {
					return part.getProbability();
				}
			}
		}
		return result;
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
				whoAffected = ((EVASuit)actor).getOwner().getName();
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
//			logger.info(entity, "MalfunctionChance: " + malfunctionChance + " %");
			malfunctionChance = Math.min(500, 2 + malfunctionChance);
//			logger.info(entity, "MalfunctionChance: " + malfunctionChance + " %");
			// log10 (1000) is 2.7. This effectively limits the change to no more than 2.7% per millisol on a unit 
			malfunctionChance = Math.log10(malfunctionChance);
//			logger.info(entity, "MalfunctionChance: " + malfunctionChance + " %");
						
			boolean hasMal = false;
			// Check for malfunction due to lack of maintenance and wear condition.
			if (time > 0 && RandomUtil.lessThanRandPercent(malfunctionChance)) {
				// Reset delay back to MAX_DELAY. 
				delay = MAX_DELAY;
				
//				logger.info(entity, "currentWearCondition: " + currentWearCondition);
//				logger.info(entity, "maintFactor: " + maintFactor);
//				logger.info(entity, "wearFactor: " + wearFactor);
//				double solsLastMaint = Math.round(effectiveTimeSinceLastMaintenance / 1000D * 10.0)/10.0;
//				logger.info(entity, "Checking for malfunction if it's warranted due to wear-and-tear. "
//						+ solsLastMaint + " sols since last check-up. Condition: " 
//						+ Math.round(currentWearCondition*100.0)/100.0 + " %.");
				
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
//			logger.info(entity, "maintenanceChance: " + maintenanceChance + " %");
			
			// Check for repair items needed due to lack of maintenance and wear condition.
			if (time > 0 && RandomUtil.lessThanRandPercent(maintenanceChance)) {
				// Reset delay back to MAX_DELAY. 
				delay = MAX_DELAY;

				// Note: call determineNewMaintenanceParts is just checking for the possibility 
				// of having needed repair parts and doesn't necessarily result in generating parts 
				// that need maintenance
				
				// Generates the repair parts 
				generateNewMaintenanceParts();
//				logger.info(entity, "Checking if repair parts are needed due to wear-and-tear. "
//						+ "Condition: " + Math.round(currentWearCondition*100.0)/100.0 + " %.");
			}
		}
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
//		setLifeSupportModifiers(time);

		// Check if resources is still draining
		depleteResources(time);

		// Add time passing.
		timeSinceLastMaintenance += time;

		return true;
	}

	/**
	 * Resets one or more flow modifier
	 *
	 * @param type
	 */
	private void resetModifiers(int type) {
		// compare from previous modifier
		if (type == 0) {
			oxygenFlowModifier = 100D;
			logger.log(entity, Level.WARNING, 20_000L, "The oxygen flow retrictor had been fixed");
		}
	}

	/**
	 * REmove a malfunction that has been fixed
	 * @param fixed Malfunction fixed. 
	 */
	void removeFixedMalfunction(Malfunction fixed) {
		if (!malfunctions.remove(fixed)) {
			logger.warning(entity, 20_000L, "Fixed malfunction is unknown " + fixed.getName());
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
				logger.log(entity, Level.WARNING, 20_000, "Oxygen flow restricted to "
								+ Math.round(oxygenFlowModifier*10.0)/10.0 + "% capacity");
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
					double remaining = (100.0 - malfunction.getPercentageFixed())/100D;
					for (Entry<Integer, Double> entry : malfunction.getResourceEffects().entrySet()) {
						Integer resource = entry.getKey();
						double amount = entry.getValue();
						double amountDepleted = amount * time * remaining;
						ResourceHolder rh = (ResourceHolder)entity;
						double amountStored = rh.getAmountResourceStored(resource);

						if (amountStored < amountDepleted) {
							amountDepleted = amountStored;
						}
						if (amountDepleted >= 0) {
							rh.retrieveAmountResource(resource, amountDepleted);
							logger.log(entity, Level.WARNING, 15_000, "Leaking "
											+ Math.round(amountDepleted*100.0)/100.0 + " of  "
											+ ResourceUtil.findAmountResource(resource));
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
		return maintenanceWorkTime;
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
			// Reset the following params
			maintenanceTimeCompleted = 0D;
			timeSinceLastMaintenance = 0D;
			effTimeSinceLastMaint = 0D;
			// Increment num of maintenance 
			numberMaintenances++;
			// Improve the currentWearlifetime
			currentWearLifeTime += time * RandomUtil.getRandomDouble(LOW_QUALITY_INSPECTION, HIGH_QUALITY_INSPECTION);
			double improvement = RandomUtil.getRandomDouble(.95, 1);
			// Set a upper limit for currentWearLifeTime
			// Note: it would deteriorate over time and won't get back to baseWearLifeTime but it can improve somewhat
			if (currentWearLifeTime > baseWearLifeTime - cumulativeTime * improvement)
				currentWearLifeTime = baseWearLifeTime - cumulativeTime * improvement;
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

		if (!partsNeededForMaintenance.isEmpty()) {
			// If partsNeededForMaintenance has already been generated,
			// do NOT do it again so as to allow enough time for 
			// settlers to respond to the maintenance call.
			return;
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
			logger.info(entity, 20_000L, "Maintenance event triggered with maintenance parts: " + partsNeededForMaintenance); 
	}

	/**
	 * Looks at the parts needed for maintenance on this entity.
	 *
	 * @return map of parts and their number.
	 */
	public Map<Integer, Integer> getMaintenanceParts() {
		return entity.getAssociatedSettlement().getBuildingManager().getMaintenanceParts(entity);
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
		if (!partsNeededForMaintenance.isEmpty())
			logger.info(entity, 20_000L, "Just retrieved maintenance parts: " + partsNeededForMaintenance);
		return Collections.unmodifiableMap(partsNeededForMaintenance);
	}
	
	/**
	 * Closes out the parts needed for maintenance on this entity
	 * after being submitted to the building manager.
	 */
	public void closeoutMaintenanceParts() {
		logger.info(entity, 20_000L, "Closed out submitted maintenance parts: " + partsNeededForMaintenance);
		partsNeededForMaintenance.clear();
	}
	
	/**
	 * Call to check if any maintenance parts have been posted and also see if 
	 * they are available in a particular resource storage. 
	 * Note: only at least one part is required to trigger some level of maintenance. 
	 * 
	 * @param partStore Store to provide parts
	 */
	public boolean hasMaintenanceParts(EquipmentOwner partStore) {
		Map<Integer, Integer> parts = getMaintenanceParts();
		
		// Call building manager to check if the maintenance parts have been submitted	
		if (parts == null || parts.isEmpty())
			return false;
		
		for (Entry<Integer, Integer> entry: parts.entrySet()) {
			Integer part = entry.getKey();
			int number = entry.getValue();
			if (partStore.getItemResourceStored(part) >= number) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Transfers the required parts for the maintenance from a part store.
	 * 
	 * @param partStore Store to retrieve parts from
	 */
	public void transferMaintenanceParts(EquipmentOwner partStore) {
		Map<Integer,Integer> newParts = new HashMap<>();
		for (Entry<Integer, Integer> entry: getMaintenanceParts().entrySet()) {
			Integer part = entry.getKey();
			int number = entry.getValue();
			int numMissing = partStore.retrieveItemResource(part, number);

			// Any part still outstanding record for later
			if (numMissing > 0) {
				newParts.put(part, numMissing);
			}        
		}

//		partsNeededForMaintenance = newParts;
		entity.getAssociatedSettlement().getBuildingManager().updateMaintenancePartsMap(entity, newParts);
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
		double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, masterClock.getInitialMarsTime());
		double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;

		if (totalTimeOrbits < 1D) {
			avgMalfunctionsPerOrbit = (numberMalfunctions + ESTIMATED_MALFUNCTIONS_PER_ORBIT) / 2D;
		} else {
			avgMalfunctionsPerOrbit = (1 + numberMalfunctions) / totalTimeOrbits;
		}

		int orbit = currentTime.getOrbit();
		if (orbitCache != orbit) {
			orbitCache = orbit;
			numberMalfunctions = 0;
		}
		logger.info(entity, 20_000L, "avgMalfunctionsPerOrbit: " + Math.round(avgMalfunctionsPerOrbit * 100.0)/100.0);
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
		double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, masterClock.getInitialMarsTime());
		double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;

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
	 * @param c1 {@link MarsClock}
	 * @param mf {@link MalfunctionFactory}
	 * @param m {@link MedicalManager}
	 * @param e {@link HistoricalEventManager}
	 */
	public static void initializeInstances(MasterClock c0, MarsClock c1, MalfunctionFactory mf,
										   MedicalManager mm, HistoricalEventManager em, PartConfig pc) {
		masterClock = c0;
		currentTime = c1;
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
