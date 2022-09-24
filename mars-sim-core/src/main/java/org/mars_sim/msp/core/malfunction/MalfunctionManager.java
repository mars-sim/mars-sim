/*
 * Mars Simulation Project
 * MalfunctionManager.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.logging.LocationFormat;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.PersonalityTraitType;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.MaintenanceScope;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The MalfunctionManager class manages malfunctions for units such as 
 * Building, BuildingKit, EVASuit, Robot, MockBuilding, or Vehicle). 
 * Each building has its own MalfunctionManager
 */
public class MalfunctionManager implements Serializable, Temporal {

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
	private static final double MAINTENANCE_FACTOR = .01;
	/** Factor for chance of malfunction by time since last maintenance. */
	private static final double MALFUNCTION_FACTOR = .01;
	/** Factor for chance of malfunction due to wear condition. */
	private static final double WEAR_MALFUNCTION_FACTOR = .01;
	/** Factor for chance of accident due to wear condition. */
	private static final double WEAR_ACCIDENT_FACTOR = 1D;

	private static final String OXYGEN = "Oxygen";
	private static final String CAUSE = ". Probable Cause: ";
	private static final String CAUSED_BY = " caused by ";

	private static final int SCORE_DEFAULT = 50;

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
	/** The percentage of the malfunctionable's condition from wear and tear. 0% = worn out -> 100% = new condition. */
	private double currentWearCondition;
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
	private Collection<Malfunction> malfunctions;
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
		baseWearLifeTime = wearLifeTime;
		
		double preUseTime = RandomUtil.getRandomDouble(100);
		currentWearLifeTime = wearLifeTime - preUseTime;
		cumulativeTime = preUseTime;
		effectiveTimeSinceLastMaintenance = preUseTime;
		timeSinceLastMaintenance = preUseTime;
		
		currentWearCondition = currentWearLifeTime/baseWearLifeTime * 100D;
	}

	/**
	 * Does this malfunctionable support inside Repairs?
	 * 
	 * @param supported New inside repairs supported
	 */
	public void setSupportsInside(boolean supported) {
		this.supportsInside = supported;
	}
	
	/**
	 * Add a scope string of a system or a function to the manager.
	 *
	 * @param scopeString
	 */
	public void addScopeString(String scopeString) {
		if ((scopeString != null) && !scopes.contains(scopeString.toLowerCase()))
			scopes.add(scopeString.toLowerCase());

		// Update maintenance parts.
		determineNewMaintenanceParts();
	}

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
		return new ArrayList<>(malfunctions);
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
	 * Select a malfunction randomly to the unit, based on the affected scope.
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
		Malfunction malfunction = new Malfunction(factory.getNewIncidentNum(), m, supportsInside);

		malfunctions.add(malfunction);
		numberMalfunctions++;

		getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, malfunction);

		if (registerEvent) {
			registerAMalfunction(malfunction, actor);
		}

		if (malfunction.getRepairParts().isEmpty())
			logger.info("'" + malfunction.getName() + "' needs no repair parts.");
			
		// Register the failure of the Parts involved
		for (Entry<Integer, Integer> p : malfunction.getRepairParts().entrySet()) {
			int num = p.getValue();

			// Compute the new reliability and failure rate for this malfunction
			Part part = ItemResourceUtil.findItemResource(p.getKey());
			String part_name = part.getName();

			double old_rel = part.getReliability();
			double old_prob = getRepairPartProbability(malfunction, part_name);
			double old_failure = (100 - old_rel) * old_prob / 100D;
			double old_mal_prob_failure = malfunction.getProbability();
			double old_MTBF = part.getMTBF();

			// Record the number of failure
			// and recompute the new reliability
			part.setFailure(num, currentTime.getMissionSol());

			// Need to calculate the new probability for the whole MalfunctionMeta object
			// String name = p.getName();
			double new_rel = part.getReliability();
			double new_prob = getRepairPartProbability(malfunction, part_name);
			double new_failure = (100 - new_rel) * new_prob / 100D;
			double new_mal_prob_failure = (old_mal_prob_failure + new_failure) / 2.0;
			double new_MTBF = part.getMTBF();

			logger.warning("           *** Part : " + part_name + " ***");

			logger.warning(" (1).   Reliability : " + addWhiteSpace(Math.round(old_rel * 1000.0) / 1000.0 + " %")
							+ "  -->  " + Math.round(new_rel * 1000.0) / 1000.0 + " %");

			logger.warning(" (2).  Failure Rate : " + addWhiteSpace(Math.round(old_failure * 1000.0) / 1000.0 + " %")
							+ "  -->  " + Math.round(new_failure * 1000.0) / 1000.0 + " %");

			logger.warning(" (3).          MTBF : " + addWhiteSpace(Math.round(old_MTBF * 1000.0) / 1000.0 + " hr")
							+ "  -->  " + Math.round(new_MTBF * 1000.0) / 1000.0 + " hr");

			logger.warning("          *** Malfunction : " + malfunction.getName() + " ***");

			logger.warning(" (4).   Probability : " + addWhiteSpace(Math.round(old_mal_prob_failure * 1000.0) / 1000.0 + " %")
							+ "  -->  " + Math.round(new_mal_prob_failure * 1000.0) / 1000.0 + " %");

			// Modify the probability of failure for this particular malfunction
			malfunction.setProbability(new_mal_prob_failure);

		}

		issueMedicalComplaints(malfunction);

		return malfunction;
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

	/**
	 * Sets up a malfunction event
	 *
	 * @param malfunction
	 * @param actor
	 */
	private void registerAMalfunction(Malfunction malfunction, Unit actor) {
		String malfunctionName = malfunction.getName();

		Settlement settlement = entity.getAssociatedSettlement();
		String loc0 = entity.getName();
		String loc1 = LocationFormat.getLocationDescription(entity);
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
					eventType = EventType.HAZARD_METEORITE_IMPACT;
					whileDoing = "Normal Ops";
					whoAffected = actor.getName();
				}
				else {
					eventType = EventType.MALFUNCTION_PARTS_FAILURE;
					whileDoing = "Normal Ops";
					whoAffected = actor.getName();
				}
			}
			else {
				eventType = EventType.MALFUNCTION_PARTS_FAILURE;
				whileDoing = "Normal Ops";
				whoAffected = actor.getName();
			}
		}

		HistoricalEvent newEvent = new MalfunctionEvent(eventType, malfunction, 
								malfunctionName, whileDoing, 
								whoAffected, loc0, 
								loc1, settlement.getName());
		eventManager.registerNewEvent(newEvent);

		logger.log(entity, Level.WARNING, 0, malfunction.getName()
									+ CAUSE + eventType.getName()
									+ (actor != null ? CAUSED_BY
									+ whoAffected + "." : "."));
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
		effectiveTimeSinceLastMaintenance += time;
		currentWearLifeTime -= time * RandomUtil.getRandomDouble(.5, 1.5);
		if (currentWearCondition < 0D)
			currentWearCondition = 0D;
		currentWearCondition = currentWearLifeTime/baseWearLifeTime * 100D;

		if (pulse.isNewMSol()
				&& pulse.getMarsTime().getMillisolInt() % 3 == 0) {
			double maintFactor = effectiveTimeSinceLastMaintenance * MALFUNCTION_FACTOR;
			double wearFactor = (100 - currentWearCondition) * WEAR_MALFUNCTION_FACTOR;
			double malfunctionChance = time * maintFactor * wearFactor;
			// Check for malfunction due to lack of maintenance and wear condition.
			if (RandomUtil.lessThanRandPercent(malfunctionChance)) {
//				logger.info(entity, "wearFactor: " + wearFactor);
//				logger.info(entity, "MalfunctionChance: " + malfunctionChance + " %");
//				double solsLastMaint = Math.round(effectiveTimeSinceLastMaintenance / 1000D * 10.0)/10.0;
//				logger.info(entity, "Checking for malfunction if it's warranted due to wear-and-tear. "
//						+ solsLastMaint + " sols since last check-up. Condition: " 
//						+ Math.round(currentWearCondition*100.0)/100.0 + " %.");
				// Note: call selectMalfunction is just checking for the possibility 
				// of having malfunction and doesn't necessarily result in one
				selectMalfunction((Unit)entity);
			}

			// FUTURE : how to connect maintenance to field reliability statistics
			double maintenanceChance = time * maintFactor * wearFactor / (1 + numberMaintenances) * MAINTENANCE_FACTOR;
			// Check for repair items needed due to lack of maintenance and wear condition.
			if (RandomUtil.lessThanRandPercent(maintenanceChance)) {
//				logger.info(entity, "wearFactor: " + wearFactor);
//				logger.info(entity, "maintenanceChance: " + maintenanceChance + " %");
				// Note: call determineNewMaintenanceParts is just checking for the possibility 
				// of having needed repair parts and doesn't necessarily result in one
				// Updates the repair parts 
				determineNewMaintenanceParts();
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

		checkFixedMalfunction();

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
//		logger.info("Reseting modifiers type " + type );
		if (type == 0) {
			oxygenFlowModifier = 100D;
			logger.log(entity, Level.WARNING, 5_000, "The oxygen flow retrictor had been fixed");
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
	 * Checks if any malfunctions have been fixed.
	 *
	 * @param time
	 */
	private void checkFixedMalfunction() {
		Collection<Malfunction> fixedMalfunctions = new ArrayList<>();

		// Check if any malfunctions are fixed.
		if (hasMalfunction()) {
			for (Malfunction m : malfunctions) {

				if (m.isFixed()) {
					fixedMalfunctions.add(m);
				}
			}
		}

		for (Malfunction m : fixedMalfunctions) {
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

			getUnit().fireUnitUpdate(UnitEventType.MALFUNCTION_EVENT, m);

			String chiefRepairer = m.getMostProductiveRepairer();

			HistoricalEvent newEvent = new MalfunctionEvent(EventType.MALFUNCTION_FIXED, m,
					m.getName(), "Repairing", chiefRepairer, entity.getName(), 
					LocationFormat.getLocationDescription(entity),
					entity.getAssociatedSettlement().getName());

			eventManager.registerNewEvent(newEvent);

			logger.log(entity, Level.INFO, 0,"The malfunction '" + m.getName() + "' had been dealt with.");

			// Remove the malfunction
			malfunctions.remove(m);
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
			logger.log(entity, Level.WARNING, 3000, "Accident " + aType + " occurred caused by "
						 + actor.getName());

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
			effectiveTimeSinceLastMaintenance = 0D;
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
	 * Determines a new set of required repair parts for maintenance. 
	 */
	private void determineNewMaintenanceParts() {
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
	}

	/**
	 * Gets the parts needed for maintenance on this entity.
	 *
	 * @return map of parts and their number.
	 */
	public Map<Integer, Integer> getMaintenanceParts() {
		if (partsNeededForMaintenance == null)
			partsNeededForMaintenance = new ConcurrentHashMap<>();
		return new ConcurrentHashMap<>(partsNeededForMaintenance);
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
					// Consume the number of parts available 
					// and reset the number needed for this part
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
		double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, masterClock.getInitialMarsTime());
		double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;

		if (totalTimeOrbits < 1D) {
			avgMaintenancesPerOrbit = (numberMaintenances + ESTIMATED_MAINTENANCES_PER_ORBIT) / 2D;
		} else {
			avgMaintenancesPerOrbit = numberMaintenances / totalTimeOrbits;
		}

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
		return currentWearCondition;
	}

	/**
	 * Gets the multiplying modifier for the chance of an accident due to the
	 * malfunctionable entity's wear condition. From 0 to 1
	 *
	 * @return accident modifier.
	 */
	public double getWearConditionAccidentModifier() {
		return (100D - currentWearCondition) / 100D * WEAR_ACCIDENT_FACTOR;
	}

	/**
	 * Initializes instances after loading from a saved sim
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
