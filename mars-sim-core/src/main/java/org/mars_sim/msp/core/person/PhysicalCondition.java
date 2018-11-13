/**
 * Mars Simulation Project
 * PhysicalCondition.java
 * @version 3.1.0 2017-10-21
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

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
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.person.ai.task.meta.EatMealMeta;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalEvent;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.person.health.Medication;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.MathUtils;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the Physical Condition of a Person. It models a
 * person's health and physical characteristics.
 */
public class PhysicalCondition implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PhysicalCondition.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	public static final String WELL = "Well";
	public static final String DEAD = "Dead : ";
	public static final String ILL = "Sick : ";

	public static final int THIRST_THRESHOLD = 100;

	/** Life support minimum value. */
	private static int MIN_VALUE = 0;
	/** Life support maximum value. */
	private static int MAX_VALUE = 1;

	public static final double MENTAL_BREAKDOWN = 100D;

	private static final double COLLAPSE_IMMINENT = 3000D;

	/** Stress jump resulting from being in an accident. */
	public static final double ACCIDENT_STRESS = 10D;

	public static final double FOOD_RESERVE_FACTOR = 1.5D;
	/** Performance modifier for thirst. */
	private static final double THIRST_PERFORMANCE_MODIFIER = .00015D;
	/** Performance modifier for hunger. */
	private static final double HUNGER_PERFORMANCE_MODIFIER = .0001D;
	/** Performance modifier for fatigue. */
	private static final double FATIGUE_PERFORMANCE_MODIFIER = .0005D;
	/** Performance modifier for stress. */
	private static final double STRESS_PERFORMANCE_MODIFIER = .005D;
	/** Performance modifier for energy. */
	private static final double ENERGY_PERFORMANCE_MODIFIER = .0001D;
	/** The average maximum daily energy intake */
	private static final double MAX_DAILY_ENERGY_INTAKE = 10100D;
	/**
	 * The average kJ of a 1kg food. Assume each meal has 0.1550 kg and has 2525 kJ.
	 */
	public static final double FOOD_COMPOSITION_ENERGY_RATIO = 16290.323;
	// public static int MAX_KJ = 16290; // 1kg of food has ~16290 kJ (see notes on
	// people.xml under <food-consumption-rate value="0.62" />)
	public static final double ENERGY_FACTOR = 0.8D;
	/**
	 * The maximum air pressure a person can live without harm in kPa. (somewhat
	 * arbitrary)
	 */
	public static final double MAXIMUM_AIR_PRESSURE = 68D; // Assume 68 kPa time dependent
	/** Period of time (millisols) over which random ailments may happen. */
	private static double RANDOM_AILMENT_PROBABILITY_TIME = 100_000D;

	private static double o2_consumption;
	private static double h2o_consumption;
	private static double minimum_air_pressure;
	private static double min_temperature;
	private static double max_temperature;
	private static double food_consumption;
	private static double dessert_consumption;
	private static double highFatigueCollapseChance;
	private static double stressBreakdownChance;

	/**
	 * The amount of water this person would consume each time (assuming drinking
	 * water 8 times a day)
	 */
	private double waterConsumedPerServing;
	/** True if person is starving. */
	private boolean isStarving = false;
	/** True if person is stressed out. */
	private boolean isStressedOut = false;
	/** True if person is collapsed under fatigue. */
	private boolean isCollapsed = false;
	/** True if person is dehydrated. */
	private boolean isDehydrated = false;
	/** True if person is alive. */
	private boolean alive;
	/** True if person is thirsty. */
	private boolean isThirsty;
	/** True if person is radiation Poisoned. */
	private boolean isRadiationPoisoned;
	/** True if person is doing a task that's considered resting. */
	private boolean restingTask;

	private int solCache = 0;
	private int endurance;
	private int strength;
	private int resilience;
	private int emotStability;
	private int agility;

	/**
	 * Person's Musculoskeletal system from 0 to 100 (muscle pain tolerance, muscle
	 * health, muscle soreness).
	 */
	private double[] musculoskeletal = new double[] { 0, 0, 0 };
	/** Person's thirst level. [in millisols]. */
	private double thirst;
	/** Person's fatigue level from 0 to infinity. */
	private double fatigue;
	/** Person's hunger level [in millisols]. */
	private double hunger;
	/** Person's stress level (0.0 % - 100.0 %). */
	private double stress;
	/** Performance factor 0.0 to 1.0. */
	private double performance;
	/** Person's hygiene factor (0.0 - 100.0) */
	// private double hygiene;
	/** Person's energy level [in kJ] */
	private double kJoules;
	/** Person's food appetite (0.0 to 1.0) */
	private double appetite;

	private double inclination_factor;

	private double starvationStartTime;

	private double dehydrationStartTime;

	private double personalMaxEnergy;

	private double foodDryMassPerServing;

	private double bodyMassDeviation;

	private String name;

	/** Person owning this physical. */
	private Person person;
	/** Details of persons death. */
	private DeathInfo deathDetails;
	/** Most serious problem. */
	private HealthProblem serious;
	/** Radiation Exposure. */
	private RadiationExposure radiation;

	private CircadianClock circadian;

	private TaskManager taskMgr;

	private HealthProblem starved;

	private HealthProblem dehydrated;

	private NaturalAttributeManager naturalAttributeManager;

	private static MarsClock marsClock;
	private static MasterClock masterClock;
	
	private static EatMealMeta eatMealMeta = new EatMealMeta();
	private static MedicalManager medicalManager;
	private static Complaint depression;
	private static Complaint panicAttack;
	private static Complaint highFatigue;
	private static Complaint radiationPoisoning;

	private static Complaint dehydration;
	private static Complaint starvation;
	private static Complaint freezing;
	private static Complaint heatStroke;
	private static Complaint decompression;
	private static Complaint suffocation;

	/** List of medications affecting the person. */
	private List<Medication> medicationList;
	/** Injury/Illness effecting person. */
	private Map<Complaint, HealthProblem> problems;
	/** Record of Illness frequency. */
	private Map<ComplaintType, Integer> healthLog;
	/** Record of illness start time. */
	private Map<ComplaintType, List<String>> healthHistory;

	/** List of all available medical complaints. */
	private static List<Complaint> allMedicalComplaints;
	
	private static PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();

	/**
	 * Constructor 1.
	 * 
	 * @param newPerson The person requiring a physical presence.
	 */
	public PhysicalCondition(Person newPerson) {
		person = newPerson;
		name = newPerson.getName();

		circadian = person.getCircadianClock();

		medicalManager = Simulation.instance().getMedicalManager();

		taskMgr = person.getMind().getTaskManager();

		alive = true;

		radiation = new RadiationExposure(this);
		radiation.initializeWithRandomDose();

		deathDetails = null;

		problems = new HashMap<Complaint, HealthProblem>();

		healthLog = new HashMap<ComplaintType, Integer>();

		healthHistory = new HashMap<ComplaintType, List<String>>();

		medicationList = new ArrayList<Medication>();

		if (naturalAttributeManager == null)
			naturalAttributeManager = person.getNaturalAttributeManager();

		endurance = naturalAttributeManager.getAttribute(NaturalAttributeType.ENDURANCE);
		strength = naturalAttributeManager.getAttribute(NaturalAttributeType.STRENGTH);
		resilience = naturalAttributeManager.getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
		emotStability = naturalAttributeManager.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY);
		agility = naturalAttributeManager.getAttribute(NaturalAttributeType.AGILITY);

		// Computes the adjustment from a person's natural attributes
		double es = (endurance + strength + agility) / 300D;

		// TODO: may incorporate real world parameters such as areal density in g cm−2,
		// T-socre and Z-score (see https://en.wikipedia.org/wiki/Bone_density)
		musculoskeletal[0] = RandomUtil.getRandomInt(-10, 10) + (int) es; // pain tolerance
		musculoskeletal[1] = 50; // muscle health index; 50 being the average
		musculoskeletal[2] = RandomUtil.getRandomRegressionInteger(100); // muscle soreness

		performance = 1.0D;
		thirst = RandomUtil.getRandomRegressionInteger(100);
		fatigue = 0; // RandomUtil.getRandomRegressionInteger(1000) * .5;
		stress = RandomUtil.getRandomRegressionInteger(100);
		hunger = RandomUtil.getRandomRegressionInteger(100);
		kJoules = 2500;// 1000D + RandomUtil.getRandomDouble(1500);
		// hygiene = RandomUtil.getRandomDouble(100D);

		personalMaxEnergy = MAX_DAILY_ENERGY_INTAKE;

		appetite = personalMaxEnergy / MAX_DAILY_ENERGY_INTAKE;

		PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();

		h2o_consumption = personConfig.getWaterConsumptionRate(); // 3 kg per sol
		o2_consumption = personConfig.getNominalO2ConsumptionRate();

		bodyMassDeviation = Math
				.sqrt(person.getBaseMass() / Person.AVERAGE_WEIGHT * person.getHeight() / Person.AVERAGE_HEIGHT);

		// assuming a person drinks 10 times a day, each time ~375 mL
		waterConsumedPerServing = h2o_consumption * bodyMassDeviation / 10D; // about .3 kg per serving

		minimum_air_pressure = personConfig.getMinAirPressure();
		min_temperature = personConfig.getMinTemperature();
		max_temperature = personConfig.getMaxTemperature();
		food_consumption = personConfig.getFoodConsumptionRate();
		dessert_consumption = personConfig.getDessertConsumptionRate();

		stressBreakdownChance = personConfig.getStressBreakdownChance();
		highFatigueCollapseChance = personConfig.getHighFatigueCollapseChance();

		foodDryMassPerServing = food_consumption / (double) Cooking.NUMBER_OF_MEAL_PER_SOL;

		starvationStartTime = 1000D * (personConfig.getStarvationStartTime() * bodyMassDeviation / 2);
		// + RandomUtil.getRandomDouble(.15) - RandomUtil.getRandomDouble(.15));

		dehydrationStartTime = 1000D * (personConfig.getDehydrationStartTime() * bodyMassDeviation);
//		System.out.println("dehydrationStartTime : " + dehydrationStartTime);
	}

	/**
	 * Initialize values and instances at the beginning of sol 1
	 */
	public void initialize() {

		// Modify personalMaxEnergy at the start of the sim
		int d1 = 2 * (35 - person.updateAge()); // Assume that after age 35, metabolism slows down
		double d2 = person.getBaseMass() - Person.AVERAGE_WEIGHT;
		double preference = person.getPreference().getPreferenceScore(eatMealMeta) * 10D;

		personalMaxEnergy = personalMaxEnergy + d1 + d2 + preference;
		appetite = personalMaxEnergy / MAX_DAILY_ENERGY_INTAKE;

		freezing = getMedicalManager().getFreezing();
		heatStroke = getMedicalManager().getHeatStroke();
		decompression = getMedicalManager().getDecompression();
		suffocation = getMedicalManager().getSuffocation();

	}

	public void recoverFromSoreness(double value) {
		// reduce the muscle soreness by 1 point at the end of the day
		double soreness = musculoskeletal[2];
		soreness = soreness - value;
		if (soreness < 0)
			soreness = 0;
		musculoskeletal[2] = soreness;
	}

	/**
	 * The Physical condition should be updated to reflect a passing of time. This
	 * method has to check the recover or degradation of any current illness. The
	 * progression of this time period may result in the illness turning fatal. It
	 * also updated the hunger and fatigue status
	 *
	 * @param time    amount of time passing (in millisols)
	 * @param support life support system.
	 * @return True still alive.
	 */
	public void timePassing(double time, LifeSupportType support) {

		if (alive) {

			boolean illnessEvent = false;

			masterClock = Simulation.instance().getMasterClock();
			marsClock = masterClock.getMarsClock();
			
			int solElapsed = marsClock.getMissionSol();

			if (solCache != solElapsed) {
				// check once a day only

				if (solCache == 0)
					initialize();

				// reduce the muscle soreness
				recoverFromSoreness(1);

				solCache = solElapsed;
			}

			String loc = person.getLocationTag().getQuickLocation();

			// Check if a person is performing low aerobic tasks 
			if (person.getTaskDescription().toLowerCase().contains("eat")
						|| person.getTaskDescription().toLowerCase().contains("drink")
//					person.getTaskDescription().toLowerCase().contains("assist")
//					|| person.getTaskDescription().toLowerCase().contains("compil")
						|| person.getTaskDescription().toLowerCase().contains("meet")
//					|| person.getTaskDescription().toLowerCase().contains("peer")
						|| person.getTaskDescription().toLowerCase().contains("relax")
						|| person.getTaskDescription().toLowerCase().contains("rest")
						|| person.getTaskDescription().toLowerCase().contains("sleep")
//					|| person.getTaskDescription().toLowerCase().contains("teach")
//					|| person.getTaskDescription().toLowerCase().contains("walk")		
//					|| person.getTaskDescription().toLowerCase().contains("yoga")	
				) {
					restingTask = true;
				} else
					restingTask = false;
			
			if (restingTask)
				o2_consumption = personConfig.getLowO2ConsumptionRate();
			else
				o2_consumption = personConfig.getNominalO2ConsumptionRate();
				
			// Check life support system
			try {
				if (consumeOxygen(support, o2_consumption * (time / 1000D)))
					LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
							"[" + loc + "] " + name + " has insufficient oxygen.", null);
				// if (consumeWater(support, h2o_consumption * (time / 1000D)))
				// LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, name + " has
				// insufficient water.", null);
				if (requireAirPressure(support, minimum_air_pressure))
					LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
							"[" + loc + "] " + name + " is under insufficient air pressure.", null);
				if (requireTemperature(support, min_temperature, max_temperature))
					LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
							"[" + loc + "] " + name + " cannot survive long at this extreme temperature.", null);

				// TODO: how to run to another building/location
			} catch (Exception e) {
				e.printStackTrace();
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
						"[" + loc + "] " + name + "'s life support system is failing !", null);
			}

			// Update radiation counter
			radiation.timePassing(time);


			// Update the existing health problems
			if (!problems.isEmpty()) {
				// Throw illness event if any problems already exist.
				illnessEvent = true;

				List<Complaint> newProblems = new ArrayList<Complaint>();
				List<HealthProblem> currentProblems = new ArrayList<HealthProblem>(problems.values());

				Iterator<HealthProblem> hp = currentProblems.iterator();
				while (hp.hasNext()) {
					HealthProblem problem = hp.next();
					// Advance each problem, they may change into a worse problem.
					// If the current is completed or a new problem exists then
					// remove this one.
					Complaint nextPhase = problem.timePassing(time, this);

					if (problem.isCured() || (nextPhase != null)) {
						Complaint c = problem.getIllness();

						if (c.getType() == ComplaintType.HIGH_FATIGUE_COLLAPSE)
							isCollapsed = false;

						else if (c.getType() == ComplaintType.PANIC_ATTACK || c.getType() == ComplaintType.DEPRESSION)
							isStressedOut = false;

						else if (c.getType() == ComplaintType.DEHYDRATION)
							isDehydrated = false;

						else if (c.getType() == ComplaintType.STARVATION)
							isStarving = false;

						else if (c.getType() == ComplaintType.RADIATION_SICKNESS)
							isRadiationPoisoned = false;

						// If nextPhase is not null, remove this problem so that it can 
						// properly be transitioned into the next.
						problems.remove(c);

					}

					// If a new problem, check it doesn't exist already
					if (nextPhase != null) {
						newProblems.add(nextPhase);
					}
				}

				// Add the new problems
				for (Complaint c : newProblems) {
					addMedicalComplaint(c);
					illnessEvent = true;
				}

			}

			// Has the person died ?
			// if (isDead()) return false;

			// See if any random illnesses happen.
			if (!restingTask) {
				List<Complaint> randomAilments = checkForRandomAilments(time);
				if (randomAilments.size() > 0) {
					illnessEvent = true;
				}
			}

			if (illnessEvent) {
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
			}

			// Add time to all medications affecting the person.
			Iterator<Medication> i = medicationList.iterator();
			while (i.hasNext()) {
				Medication med = i.next();
				med.timePassing(time);
				if (!med.isMedicated()) {
					i.remove();
				}
			}

			// Build up fatigue & hunger for given time passing.
			setThirst(thirst + time * bodyMassDeviation);
			setFatigue(fatigue + time);
			setHunger(hunger + time * bodyMassDeviation);

			// normal bodily function consume a minute amount of energy
			// even if a person does not perform any tasks
			// Note: removing this as reduce energy is already handled
			// in the TaskManager and people are always performing tasks
			// unless dead. - Scott
			// reduceEnergy(time);

			int msol = marsClock.getMillisolInt();
			int factor = (int) (Math.sqrt(masterClock.getTimeRatio())/10D);
			if (msol % 7 * factor == 0) {

				if (!restingTask) {
					checkStarvation(hunger);
					checkDehydration(thirst);
				}

				// If person is at high stress, check for mental breakdown.
				if (!isStressedOut)
					if (stress > MENTAL_BREAKDOWN)
						checkForStressBreakdown(time);

				// Check if person is at very high fatigue may collapse.
				if (!isCollapsed)
					if (fatigue > COLLAPSE_IMMINENT)
						checkForHighFatigueCollapse(time);

				if (!isRadiationPoisoned)
					checkRadiationPoisoning(time);
			}

			// Calculate performance and most serious illness.
			recalculatePerformance();

		}

	}

	/**
	 * Gets the person's fatigue level
	 * 
	 * @return person's fatigue
	 */
	public double getFatigue() {
		return fatigue;
	}

	public double getThirst() {
		return thirst;
	}

	/**
	 * Gets the person's caloric energy.
	 * 
	 * @return person's caloric energy in kilojoules Note: one large calorie is
	 *         about 4.2 kilojoules
	 */
	public double getEnergy() {
		return kJoules;
	}

	/**
	 * Reduces the person's energy.
	 * 
	 * @param time the amount of time (millisols).
	 */
	public void reduceEnergy(double time) {
		double xdelta = time * MAX_DAILY_ENERGY_INTAKE / 1000D;

		// Changing this to a more linear reduction of energy.
		// We may want to change it back to exponential. - Scott

		// double xdelta = 4 * time / FOOD_COMPOSITION_ENERGY_RATIO;
		// kJoules = kJoules / exponential(xdelta);

		if (kJoules < 200D) {
			kJoules -= xdelta * .75;
		} else if (kJoules < 400D) {
			kJoules -= xdelta * .8;
		} else if (kJoules < 600D) {
			kJoules -= xdelta * .85;
		} else if (kJoules < 800D) {
			kJoules -= xdelta * .9;
		} else if (kJoules < 1000D) {
			kJoules -= xdelta * .95;
		} else
			kJoules -= xdelta;

		if (kJoules < 100D) {
			// 100 kJ is the lowest possible energy level
			kJoules = 100D;
		}
	}

	// public double exponential(double x) {
	// x = 1d + x / 256d;
	// x *= x; x *= x; x *= x; x *= x;
	// x *= x; x *= x; x *= x; x *= x;
	// return x;
	// }

	/**
	 * Sets the person's energy level
	 * 
	 * @param kilojoules
	 * 
	 *                   public void setEnergy(double kJ) { kJoules = kJ;
	 *                   //System.out.println("PhysicalCondition : SetEnergy() : " +
	 *                   Math.round(kJoules*100.0)/100.0 + " kJoules"); }
	 */

	/**
	 * Adds to the person's energy intake by eating
	 * 
	 * @param person's energy level in kilojoules
	 */
	public void addEnergy(double foodAmount) {
		// 1 calorie = 4.1858 kJ
		// TODO: vary MAX_KJ according to the individual's physical profile strength,
		// endurance, etc..
		// double FOOD_COMPOSITION_ENERGY_RATIO = 16290; 1kg of food has ~16290 kJ (see
		// notes on people.xml under <food-consumption-rate value="0.62" />)
		// double FACTOR = 0.8D;
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ

		// Note: changing this to a more linear addition of energy.
		// We may want to change it back to exponential. - Scott

		double xdelta = foodAmount * FOOD_COMPOSITION_ENERGY_RATIO / appetite;
		// kJoules += foodAmount * xdelta * Math.log(FOOD_COMPOSITION_ENERGY_RATIO /
		// kJoules) / ENERGY_FACTOR;

		if (kJoules > 10000D) {
			kJoules += xdelta * .75;
		} else if (kJoules > 9000D) {
			kJoules += xdelta * .8;
		} else if (kJoules > 8000D) {
			kJoules += xdelta * .85;
		} else if (kJoules > 7000D) {
			kJoules += xdelta * .9;
		} else if (kJoules > 6000D) {
			kJoules += xdelta * .95;
		} else
			kJoules += xdelta;

		circadian.eatFood(kJoules / 50D);

		if (kJoules > personalMaxEnergy * 2) {
			kJoules = personalMaxEnergy * 2;
		}

	}

	/**
	 * Get the performance factor that effect Person with the complaint.
	 * 
	 * @return The value is between 0 -> 1.
	 */
	public double getPerformanceFactor() {
		return performance;
	}

	/**
	 * Sets the performance factor.
	 * 
	 * @param newPerformance new performance (between 0 and 1).
	 */
	public void setPerformanceFactor(double p) {
		if (p > 1D)
			p = 1D;
		else if (p < 0)
			p = 0;
		if (performance != p)
			performance = p;
		// person.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
	}

	/**
	 * Define the fatigue setting for this person
	 * 
	 * @param newFatigue New fatigue.
	 */
	public void setFatigue(double f) {
		if (f > 3000)
			fatigue = 3000;
		else if (f < 0)
			f = 0;

		fatigue = f;
		// person.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);
	}

	public void setThirst(double t) {
		if (thirst != t)
			thirst = t;
		if (t > THIRST_THRESHOLD && !isThirsty)
			isThirsty = true;
		else if (isThirsty)
			isThirsty = false;
		// person.fireUnitUpdate(UnitEventType.THIRST_EVENT);
	}

	public boolean isThirsty() {
		return isThirsty;
	}

	public void setThirsty(boolean value) {
		isThirsty = value;
	}

	/**
	 * Gets the person's hunger level
	 * 
	 * @return person's hunger
	 */
	public double getHunger() {
		return hunger;
	}

	/**
	 * Checks if a person is starving or no longer starving
	 * 
	 * @param hunger
	 */
	public void checkStarvation(double hunger) {

		if (starvation == null)
			starvation = getMedicalManager().getStarvation();

		if (!isStarving && hunger > starvationStartTime && (kJoules < 120D)) {
			if (!problems.containsKey(starvation)) {
				addMedicalComplaint(starvation);
				isStarving = true;
				// LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
				// person + " is starving. Hunger level : "
				// + Math.round(hunger*10.0)/10.0 + ".", null);
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
			}

			if (taskMgr == null)
				taskMgr = person.getMind().getTaskManager();

			// TODO : how to tell a person to walk back to the settlement ?
			if (person.isInside()) {
				// Stop any on-going tasks
				taskMgr.clearTask();
				// go eat a meal
				taskMgr.addTask(new EatMeal(person));
			}

			// TODO : should check if a person is on a critical mission,

		}

		else if (isStarving && hunger < 500D && kJoules > 800D) {
			if (starved == null)
				starved = problems.get(starvation);
			if (starved != null) {
				starved.startRecovery();
				// isStarving = false;
			}
		}

	}

	/**
	 * Checks if a person is dehydrated
	 * 
	 * @param hunger
	 */
	public void checkDehydration(double thirst) {

		if (dehydration == null)
			dehydration = getMedicalManager().getDehydration();

		if (thirst > dehydrationStartTime) {
			if (!isDehydrated && !problems.containsKey(dehydration)) {
				addMedicalComplaint(dehydration);
				isDehydrated = true;
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
			}

			if (taskMgr == null)
				taskMgr = person.getMind().getTaskManager();

			if (person.isInside()) {
				// Stop any on-going tasks
				taskMgr.clearTask();
				// go drink water by eating a meal
				taskMgr.addTask(new EatMeal(person));
			}

		}

		else if (isDehydrated && thirst < 500D) {
			
			if (dehydrated == null)
				dehydrated = problems.get(dehydration);
			
			if (dehydrated != null) {
				dehydrated.startRecovery();
				// isDehydrated = false;
			}
			// Set to not dehydrated
			isDehydrated = false;
		}
	}

	/**
	 * Sets the person's stress level.
	 * 
	 * @param newStress the new stress level (0.0 to 100.0)
	 */
	public void setStress(double newStress) {
		if (stress != newStress) {
			stress = newStress;
			if (stress > 100D)
				stress = 100D;
			else if (stress < 0D)
				stress = 0D;
			else if (Double.isNaN(stress))
				stress = 0D;
			// person.fireUnitUpdate(UnitEventType.STRESS_EVENT);
		}
	}

	/**
	 * Checks if person has an anxiety attack due to too much stress.
	 * 
	 * @param time the time passing (millisols)
	 */
	private void checkForStressBreakdown(double time) {
		// Expanded Anxiety Attack into either Panic Attack or Depression

		// a person is limited to have only one of them at a time
		if (panicAttack == null)
			panicAttack = getMedicalManager().getComplaintByName(ComplaintType.PANIC_ATTACK);

		if (depression == null)
			depression = medicalManager.getComplaintByName(ComplaintType.DEPRESSION);

		if (!problems.containsKey(panicAttack) && !problems.containsKey(depression)) {

			// Determine stress resilience modifier (0D - 2D).
			// int resilience =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRESS_RESILIENCE);
			// int emotStability =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EMOTIONAL_STABILITY);

			// 0 (strong) to 1 (weak)
			double resilienceModifier = (double) (100.0 - resilience * .6 - emotStability * .4) / 100D;
			double value = stressBreakdownChance / 10D * resilienceModifier;

			if (RandomUtil.lessThanRandPercent(value)) {

				isStressedOut = true;

				double rand = RandomUtil.getRandomDouble(1.0) + inclination_factor;

				if (rand < 0.5) {

					if (panicAttack != null) {
						if (inclination_factor > -.5)
							inclination_factor = inclination_factor - .05;
						addMedicalComplaint(panicAttack);
						person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
						LogConsolidated.log(logger, Level.INFO, 500, sourceName,
								"[" + person.getLocationTag().getQuickLocation() + "] " + name
										+ " suffers from a panic attack.",
								null);

						// the person should be carried to the sickbay at this point
						person.getMind().getTaskManager().clearTask();
						person.getMind().getTaskManager().addTask(new RequestMedicalTreatment(person));

					} else
						logger.log(Level.SEVERE,
								"Could not find 'Panic Attack' medical complaint in 'conf/medical.xml'");

				} else {

					if (depression != null) {
						if (inclination_factor < .5)
							inclination_factor = inclination_factor + .05;
						addMedicalComplaint(depression);
						person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
						LogConsolidated.log(logger, Level.INFO, 500, sourceName,
								"[" + person.getLocationTag().getQuickLocation() + "] " + name
										+ " has an episode of depression.",
								null);

						// the person should be carried to the sickbay at this point
						person.getMind().getTaskManager().clearTask();
						person.getMind().getTaskManager().addTask(new RequestMedicalTreatment(person));

					} else
						logger.log(Level.SEVERE, "Could not find 'Depression' medical complaint in 'conf/medical.xml'");
				}
			}

		}
	}

	/**
	 * Checks if person has very high fatigue.
	 * 
	 * @param time the time passing (millisols)
	 */
	private void checkForHighFatigueCollapse(double time) {

		if (highFatigue == null)
			highFatigue = medicalManager.getComplaintByName(ComplaintType.HIGH_FATIGUE_COLLAPSE);

		if (!problems.containsKey(highFatigue)) {
			// Calculate the modifier (from 10D to 0D) Note that the base
			// high-fatigue-collapse-chance is 5%
			// int endurance =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.ENDURANCE);
			// int strength =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRENGTH);

			// a person with high endurance will be less likely to be collapse
			double modifier = (double) (100 - endurance * .6 - strength * .4) / 100D;

			double value = highFatigueCollapseChance / 5D * modifier;

			if (RandomUtil.lessThanRandPercent(value)) {
				isCollapsed = true;

				if (highFatigue != null) {
					addMedicalComplaint(highFatigue);
					person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
					LogConsolidated.log(logger, Level.INFO, 500, sourceName,
							"[" + person.getLocationTag().getQuickLocation() + "] " + name
									+ " collapses because of high fatigue exhaustion.",
							null);

					// the person should be carried to the sickbay at this point
					person.getMind().getTaskManager().clearTask();
					person.getMind().getTaskManager().addTask(new RequestMedicalTreatment(person));

				} else
					logger.log(Level.SEVERE,
							"Could not find 'High Fatigue Collapse' medical complaint in 'conf/medical.xml'");
			}
		}
	}

	/**
	 * Checks if person has very high fatigue.
	 * 
	 * @param time the time passing (millisols)
	 */
	private void checkRadiationPoisoning(double time) {

		if (radiationPoisoning == null)
			radiationPoisoning = medicalManager.getComplaintByName(ComplaintType.RADIATION_SICKNESS);

		if (!problems.containsKey(radiationPoisoning) && radiation.isSick()) {
			// Calculate the modifier (from 10D to 0D) Note that the base
			// high-fatigue-collapse-chance is 5%
			// int endurance =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.ENDURANCE);
			// int strength =
			// person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRENGTH);

			if (radiationPoisoning != null) {
				addMedicalComplaint(radiationPoisoning);
				isRadiationPoisoned = true;
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
				LogConsolidated.log(logger, Level.INFO, 500, sourceName,
						"[" + person.getLocationTag().getQuickLocation() + "] " + name
								+ " collapses because of radiation poisoning.",
						null);

				// the person should be carried to the sickbay at this point
				person.getMind().getTaskManager().clearTask();
				person.getMind().getTaskManager().addTask(new RequestMedicalTreatment(person));

			} else
				logger.log(Level.SEVERE, "Could not find 'Radiation Sickness' medical complaint in 'conf/medical.xml'");
			/*
			 * // a person with high endurance will be less likely to be collapse double
			 * modifier = (double) (100 - endurance * .6 - strength * .4) / 100D;
			 * 
			 * double value = highFatigueCollapseChance / 5D * modifier;
			 * 
			 * if (RandomUtil.lessThanRandPercent(value)) {
			 * 
			 * }
			 * 
			 * else {
			 * 
			 * }
			 */
		}
	}

	/**
	 * Check for any random ailments that a person comes down with over a period of
	 * time.
	 * 
	 * @param time the time period (millisols).
	 * @return list of ailments occurring. May be empty.
	 */
	private List<Complaint> checkForRandomAilments(double time) {

		List<Complaint> result = new ArrayList<Complaint>(0);

		// Check each possible medical complaint.
		if (allMedicalComplaints == null)
			allMedicalComplaints = medicalManager.getAllMedicalComplaints();

		for (Complaint complaint : allMedicalComplaints) {

			ComplaintType ct = complaint.getType();

			boolean noGo = false;
			// If a person is performing a resting task, then it is impossible to suffer
			// from laceration.
			if (!problems.containsKey(complaint)) {

				if (ct == ComplaintType.LACERATION || ct == ComplaintType.BROKEN_BONE
						|| ct == ComplaintType.PULL_MUSCLE_TENDON || ct == ComplaintType.RUPTURED_APPENDIX) {
					if (person.getTaskDescription().toLowerCase().contains("assist")
							|| person.getTaskDescription().toLowerCase().contains("compil")
							|| person.getTaskDescription().toLowerCase().contains("peer")
							|| person.getTaskDescription().toLowerCase().contains("teach")
//						|| restingTask
					) {
						noGo = true;
					}
				}

				// Check that person does not already have a health problem with this complaint.

				// Note : the following complaints are being initiated in their own methods
				else if (ct == ComplaintType.HIGH_FATIGUE_COLLAPSE || ct == ComplaintType.PANIC_ATTACK
						|| ct == ComplaintType.DEPRESSION
						// Exclude the following 6 environmentally induced complaints
						|| ct == ComplaintType.DEHYDRATION || ct == ComplaintType.STARVATION
						|| ct == ComplaintType.SUFFOCATION || ct == ComplaintType.FREEZING
						|| ct == ComplaintType.HEAT_STROKE || ct == ComplaintType.DECOMPRESSION
						//
						|| ct == ComplaintType.RADIATION_SICKNESS
						// not meaningful to implement suicide until emotional/mood state is in place
						|| ct == ComplaintType.SUICIDE) {
					noGo = true;
				}

				if (!noGo) {
					double probability = complaint.getProbability();
					// Check that medical complaint has a probability > zero
					// since some complaints are secondary complaints and cannot be started
					// by itself
					if (probability > 0D) {
						double taskModifier = 1;
						double tendency = 1;
						
						masterClock = Simulation.instance().getMasterClock();
						marsClock = masterClock.getMarsClock();
						
						int msol = marsClock.getMissionSol();

						if (healthLog.get(ct) != null && msol > 3)
							tendency = 0.5 + healthLog.get(ct) / marsClock.getMissionSol();
						else
							tendency = 1.0;
						double immunity = endurance + strength;

						if (immunity > 100)
							tendency = .75 * tendency - .25 * immunity / 100.0;
						else
							tendency = .75 * tendency + .25 * (100 - immunity) / 100.0;

						if (tendency < 0)
							tendency = 0.0001;

						if (tendency > 2)
							tendency = 2;

						if (ct == ComplaintType.PULL_MUSCLE_TENDON || ct == ComplaintType.BROKEN_BONE) {
							// Note: at the time of workout, pulled muscle can happen
							// TODO: but make a person less prone to pulled muscle while doing other tasks
							// if having consistent workout.
							if (person.getTaskDescription().equalsIgnoreCase("exercising"))
								taskModifier = 1.2;

							else if (person.getTaskDescription().toLowerCase().contains("work")) {
								// Doing outdoor field work increases the risk of having pulled muscle.
								taskModifier = 1.1;

								if (agility > 50)
									taskModifier = .75 * taskModifier - .25 * agility / 100.0;
								else
									taskModifier = .75 * taskModifier + .25 * (50 - agility) / 50.0;
							}

							else if (person.getTaskDescription().toLowerCase().contains("yoga"))
								taskModifier = 1.1;

							else if (person.getTaskDescription().toLowerCase().contains("eva"))
								taskModifier = 1.2;

							else if (person.getTaskDescription().contains("Digging")) {
								taskModifier = 1.3;

								int avoidAccident = strength + agility;
								if (avoidAccident > 50)
									taskModifier = .75 * taskModifier - .25 * avoidAccident / 100.0;
								else
									taskModifier = .75 * taskModifier + .25 * (100 - avoidAccident) / 100.0;
							}

						} else if (ct == ComplaintType.MINOR_BURNS || ct == ComplaintType.MAJOR_BURNS
								|| ct == ComplaintType.BURNS || ct == ComplaintType.LACERATION) {
							if (agility > 50)
								taskModifier = .75 * taskModifier - .25 * agility / 100.0;
							else
								taskModifier = .75 * taskModifier + .25 * (50 - agility) / 50.0;
						}

						if (taskModifier < 0)
							taskModifier = 0.0001;
						if (taskModifier > 2)
							taskModifier = 2;

						// Randomly determine if person suffers from ailment.
						double rand = RandomUtil.getRandomDouble(100D);
						double timeModifier = time / RANDOM_AILMENT_PROBABILITY_TIME;
						// System.out.println("chance : " + chance + " p*t : " + probability *
						// timeModifier);
						if (rand <= probability * taskModifier * tendency * timeModifier) {
							addMedicalComplaint(complaint);
							result.add(complaint);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Adds a new medical complaint to the person.
	 * 
	 * @param complaint the new medical complaint
	 */
	public void addMedicalComplaint(Complaint complaint) {
		if ((complaint != null) && !problems.containsKey(complaint)) {
			// Create a new health problem
			HealthProblem problem = new HealthProblem(complaint, person);
			problems.put(complaint, problem);
			ComplaintType type = complaint.getType();

			// Record this complaint type
			int freq = 0;
			if (healthLog.get(type) != null)
				freq = healthLog.get(type);
			healthLog.put(type, freq + 1);

			// Register this complaint type with a timestamp
			List<String> clocks = null;
			if (healthHistory.get(type) != null) {
				clocks = healthHistory.get(type);
			} else {
				clocks = new ArrayList<>();
			}
			
			masterClock = Simulation.instance().getMasterClock();
			marsClock = masterClock.getMarsClock();
			
			clocks.add(marsClock.getDateTimeStamp());
			healthHistory.put(type, clocks);

			String n = type.getName().toLowerCase();
			String prefix = "[" + person.getLocationTag().getLocale() + "] ";
			String phrase = "";
			String suffix = ".";

			if (person.isInSettlement()) {
				// prefix = "[" + person.getSettlement() + "] ";
				suffix = " in " + person.getBuildingLocation() + ".";
			}

			if (type == ComplaintType.STARVATION)// .equalsIgnoreCase("starvation"))
				phrase = " is starving";
			else if (type == ComplaintType.COLD)
				phrase = " caught a cold";
			else if (type == ComplaintType.FLU)
				phrase = " caught the flu";
			else if (type == ComplaintType.FEVER)
				phrase = " is having a fever";
			else if (type == ComplaintType.DECOMPRESSION)// n.equalsIgnoreCase("decompression"))
				phrase = " is suffering from decompression";
			else if (type == ComplaintType.DEHYDRATION)// n.equalsIgnoreCase("dehydration"))
				phrase = " is suffering from dehydration";
			else if (type == ComplaintType.FREEZING)// n.equalsIgnoreCase("freezing"))
				phrase = " is freezing";
			else if (type == ComplaintType.HEAT_STROKE)// n.equalsIgnoreCase("heat stroke"))
				phrase = " is suffering from a heat stroke";
			else if (type == ComplaintType.SUFFOCATION)// n.equalsIgnoreCase("suffocation"))
				phrase = " is suffocating";
			else if (type == ComplaintType.LACERATION)// n.equalsIgnoreCase("laceration"))
				phrase = " sufferred laceration";
			else if (type == ComplaintType.PULL_MUSCLE_TENDON)// n.equalsIgnoreCase("laceration"))
				phrase = " had a pulled muscle";
			else
				phrase = " is complaining about the " + n;

			LogConsolidated.log(logger, Level.INFO, 0, sourceName, prefix + person + phrase + suffix, null);

			recalculatePerformance();
		}
	}

	/**
	 * Robot consumes given amount of power
	 * 
	 * @param amount    amount of power to consume (in kJ).
	 * @param container unit to get power from
	 * @throws Exception if error consuming power.
	 */
	public void consumePower(double amount, Unit container) {
		if (container == null)
			throw new IllegalArgumentException("container is null");
	}

	/**
	 * Person consumes given amount of packed food
	 * 
	 * @param amount    amount of food to consume (in kg).
	 * @param container unit to get food from
	 * @throws Exception if error consuming food.
	 */
	public void consumePackedFood(double amount, Unit container) {
		Inventory inv = container.getInventory();

//		if (container == null)
//			throw new IllegalArgumentException("container is null");

		// AmountResource foodAR = AmountResource.findAmountResource(foodType);
		double foodEaten = amount;
		double foodAvailable = inv.getAmountResourceStored(ResourceUtil.foodID, false);

		inv.addAmountDemandTotalRequest(ResourceUtil.foodID);

		if (foodAvailable < 0.01D) {
			// throw new IllegalStateException("Warning: less than 0.01 kg dried food
			// remaining!");
			LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName,
					"[" + person.getLocationTag().getQuickLocation() + "]" + " only " + foodAvailable
							+ " kg preserved food remaining.",
					null);
		}

		// if container has less than enough food, finish up all food in the container
		else {

			if (foodEaten > foodAvailable)
				foodEaten = foodAvailable;

			foodEaten = Math.round(foodEaten * 1_000_000.0) / 1_000_000.0;
			// subtract food from container
			inv.retrieveAmountResource(ResourceUtil.foodID, foodEaten);

			inv.addAmountDemand(ResourceUtil.foodID, foodEaten);
		}
	}

	/**
	 * Person consumes given amount of oxygen
	 * 
	 * @param support Life support system providing oxygen.
	 * @param amount  amount of oxygen to consume (in kg)
	 * @return new problem added.
	 * @throws Exception if error consuming oxygen.
	 */
	private boolean consumeOxygen(LifeSupportType support, double amount) {
		double amountRecieved = support.provideOxygen(amount);
		return checkResourceConsumption(amountRecieved, amount / 2D, MIN_VALUE, suffocation);
	}

	/**
	 * This method checks the consume values of a resource. If the actual is less
	 * than the required then a HealthProblem is generated. If the required amount
	 * is satisfied, then any problem is recovered.
	 *
	 * @param actual    The amount of resource provided.
	 * @param require   The amount of resource required.
	 * @param complaint Problem associated to this resource.
	 * @return Has a new problem been added.
	 */
	private boolean checkResourceConsumption(double actual, double required, int bounds, Complaint complaint) {

		boolean newProblem = false;
		if ((bounds == MIN_VALUE) && (actual < required))
			newProblem = true;
		if ((bounds == MAX_VALUE) && (actual > required))
			newProblem = true;

		if (newProblem) {
			addMedicalComplaint(complaint);
			person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
		} else {
			// Is the person suffering from the illness, if so recovery
			// as the amount has been provided
			HealthProblem illness = problems.get(complaint);
			if (illness != null) {
				illness.startRecovery();
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
			}
		}
		return newProblem;
	}

	/**
	 * Person requires minimum air pressure.
	 * 
	 * @param support  Life support system providing air pressure.
	 * @param pressure minimum air pressure person requires (in Pa)
	 * @return new problem added.
	 */
	private boolean requireAirPressure(LifeSupportType support, double pressure) {
		return checkResourceConsumption(support.getAirPressure(), pressure, MIN_VALUE, decompression);
	}

	/**
	 * Person requires minimum temperature.
	 * 
	 * @param support     Life support system providing temperature.
	 * @param temperature minimum temperature person requires (in degrees Celsius)
	 * @return new problem added.
	 */
	private boolean requireTemperature(LifeSupportType support, double minTemperature, double maxTemperature) {

		boolean freeze = checkResourceConsumption(support.getTemperature(), minTemperature, MIN_VALUE, freezing);
		boolean hot = checkResourceConsumption(support.getTemperature(), maxTemperature, MAX_VALUE, heatStroke);
		return freeze || hot;
	}

	/**
	 * Get the details of this Person's death.
	 * 
	 * @return Detail of the death, will be null if person is still alive.
	 */
	public DeathInfo getDeathDetails() {
		return deathDetails;
	}

	/**
	 * Renders this Person dead.
	 * 
	 * @param problem      The health problem that contributes to his death.
	 * @param causedByUser True if it's caused by users
	 */
	public void setDead(HealthProblem problem, Boolean causedByUser) {
		alive = false;

		setFatigue(0D);
		setHunger(0D);
		setPerformanceFactor(0D);
		setStress(0D);

		deathDetails = new DeathInfo(person, problem);

		if (causedByUser) {
			person.setDeclaredDead();

			problem.setState(HealthProblem.DEAD);
			this.serious = problem;
			logger.severe(person + " committed suicide as instructed.");
		}

		person.getMind().setInactive();

		if (person.getVehicle() != null) {
			handleBody();
		}
	}

	/**
	 * Handles the body of a dead person.
	 * 
	 * @param problem
	 */
	public void handleBody() {
		deathDetails.getBodyRetrieved();
		examBody(getDeathDetails().getProblem());
		person.buryBody();
	}

	/**
	 * Retrieves the body.
	 * 
	 * @param problem
	 */
	public void retrieveBody() {
		deathDetails.setBodyRetrieved(true);
	}

	/**
	 * Exams the body and creates the medical event.
	 * 
	 * @param problem
	 */
	public void examBody(HealthProblem problem) {
		logger.log(Level.SEVERE,
				"[" + person.getLocationTag().getQuickLocation() + "] A post-mortem examination was ordered on "
						+ person + ". Cause of death : " + problem.toString().toLowerCase());
		// Create medical event for death.
		MedicalEvent event = new MedicalEvent(person, problem, EventType.MEDICAL_DEATH);
		Simulation.instance().getEventManager().registerNewEvent(event);
	}

	/**
	 * Define the hunger setting for this person
	 * 
	 * @param newHunger New hunger.
	 */
	public void setHunger(double newHunger) {
		if (hunger != newHunger) {
			hunger = newHunger;
		}
	}

	/**
	 * Gets the person's stress level
	 * 
	 * @return stress (0.0 to 100.0)
	 */
	public double getStress() {
		return stress;
	}

	public double getMassPerServing() {
		return foodDryMassPerServing;
	}

	/**
	 * Checks if the person is dead.
	 *
	 * @return true if dead
	 */
	public boolean isDead() {
		return !alive;
	}

	/**
	 * Checks if the person is starving.
	 *
	 * @return true if starving
	 */
	public boolean isStarving() {
		return isStarving;
	}

	public boolean isDeydrated() {
		return isDehydrated;
	}

	/**
	 * Get a string description of the most serious health situation.
	 * 
	 * @return A string containing the current illness if any.
	 */
	public String getHealthSituation() {
		String situation = WELL;
		if (serious != null) {
			if (isDead()) {
				situation = DEAD + serious.getIllness().getType().toString();
			} else {
				situation = ILL + serious.getSituation();
			}
			// else situation = "Not Well";
		}
		return situation;
	}

	/**
	 * Gets the most serious illness.
	 * 
	 * @return most serious illness
	 */
	public Complaint getMostSerious() {
		return serious.getIllness();
	}

	/**
	 * The collection of known Medical Problems.
	 */
	public Collection<HealthProblem> getProblems() {
		return problems.values();
	}

	/**
	 * Calculate how the most serious problem and other metrics would affect a
	 * person's performance.
	 */
	private void recalculatePerformance() {

		double tempPerformance = 1.0D;

		serious = null;

		// Check the existing problems. find most serious problem and how it
		// affects performance
		Iterator<HealthProblem> iter = problems.values().iterator();
		while (iter.hasNext()) {
			HealthProblem problem = iter.next();
			double factor = problem.getPerformanceFactor();
			if (factor < tempPerformance) {
				tempPerformance = factor;
			}

			if ((serious == null) || (serious.getIllness().getSeriousness() < problem.getIllness().getSeriousness())) {
				serious = problem;
			}
		}

		// High hunger reduces performance.
		if (thirst > 400D) {
			tempPerformance -= (thirst - 400D) * THIRST_PERFORMANCE_MODIFIER / 2;
		} else if (thirst > 250D) {
			tempPerformance -= (thirst - 250D) * THIRST_PERFORMANCE_MODIFIER / 4;
		}

		// High hunger reduces performance.
		if (hunger > 1200D) {
			tempPerformance -= (hunger - 1200D) * HUNGER_PERFORMANCE_MODIFIER / 2;
		} else if (hunger > 800D) {
			tempPerformance -= (hunger - 800D) * HUNGER_PERFORMANCE_MODIFIER / 4;
		}

		// High fatigue reduces performance.
		if (fatigue > 1400D) {
			tempPerformance -= (fatigue - 1400D) * FATIGUE_PERFORMANCE_MODIFIER / 2;
		} else if (fatigue > 800D) {
			tempPerformance -= (fatigue - 800D) * FATIGUE_PERFORMANCE_MODIFIER / 4;
			// e.g. f = 1000, p = 1.0 - 500 * .0001/4 = 1.0 - 0.05/4 = 1.0 - .0125 ->
			// reduces by 1.25% on each frame
		}

		// High stress reduces performance.
		if (stress > 90D) {
			tempPerformance -= (stress - 90D) * STRESS_PERFORMANCE_MODIFIER / 2;
		} else if (stress > 70D) {
			tempPerformance -= (stress - 70D) * STRESS_PERFORMANCE_MODIFIER / 4;
			// e.g. p = 100 - 10 * .005 /3 = 1 - .05/4 -> reduces by .0125 or 1.25% on each
			// frame
		}

		// High kJoules improves performance and low kJoules hurts performance.
		if (kJoules > 2000) {
			// double old = tempPerformance;
			tempPerformance += (kJoules - 1000) * ENERGY_PERFORMANCE_MODIFIER / 4;
			// LogConsolidated.log(logger, Level.INFO, 200, sourceName,
			// "kJ > 2000 " + old + " --> " + tempPerformance, null);
		} else if (kJoules < 400) {
			// double old = tempPerformance;
			tempPerformance -= 400_000 / kJoules * ENERGY_PERFORMANCE_MODIFIER / 4;
			// LogConsolidated.log(logger, Level.INFO, 200, sourceName,
			// "kJ < 400 " + old + " --> " + tempPerformance, null);
		}

		setPerformanceFactor(tempPerformance);

	}

	/**
	 * Give the status of a person's hunger level
	 *
	 * @param hunger
	 * @return status
	 */
	public static String getHungerStatus(double hunger, double energy) {
		String status = "N/A";
		if (hunger < 200 || energy > 5000)
			status = Msg.getString("PersonTableModel.column.energy.level1");
		else if (hunger < 500 || energy > 3000)
			status = Msg.getString("PersonTableModel.column.energy.level2");
		else if (hunger < 1000 || energy > 1500)
			status = Msg.getString("PersonTableModel.column.energy.level3");
		else if (hunger < 2000 || energy > 300)
			status = Msg.getString("PersonTableModel.column.energy.level4");
		else
			status = Msg.getString("PersonTableModel.column.energy.level5"); // very hungry
		return status;
	}

	/**
	 * Give the status of a person's water level
	 *
	 * @param water
	 * @return status
	 */
	public static String getThirstyStatus(double thirst) {
		String status = "N/A";
		if (thirst < 150)
			status = Msg.getString("PersonTableModel.column.water.level1");
		else if (thirst < 500)
			status = Msg.getString("PersonTableModel.column.water.level2");
		else if (thirst < 1000)
			status = Msg.getString("PersonTableModel.column.water.level3");
		else if (thirst < 1600) 
			// Note : Use getDehydrationStartTime()
			status = Msg.getString("PersonTableModel.column.water.level4");
		else
			status = Msg.getString("PersonTableModel.column.water.level5");
		return status;
	}
	
	/**
	 * Give the status of a person's fatigue level
	 *
	 * @param fatigue
	 * @return status
	 */
	public static String getFatigueStatus(double value) {
		String status = "N/A";
		if (value < 500)
			status = Msg.getString("PersonTableModel.column.fatigue.level1");
		else if (value < 800)
			status = Msg.getString("PersonTableModel.column.fatigue.level2");
		else if (value < 1200)
			status = Msg.getString("PersonTableModel.column.fatigue.level3");
		else if (value < 1600)
			status = Msg.getString("PersonTableModel.column.fatigue.level4");
		else
			status = Msg.getString("PersonTableModel.column.fatigue.level5");
		return status;
	}

	/**
	 * Give the status of a person's stress level
	 *
	 * @param hunger
	 * @return status
	 */
	public static String getStressStatus(double value) {
		String status = "N/A";
		if (value < 10)
			status = Msg.getString("PersonTableModel.column.stress.level1");
		else if (value < 40)
			status = Msg.getString("PersonTableModel.column.stress.level2");
		else if (value < 75)
			status = Msg.getString("PersonTableModel.column.stress.level3");
		else if (value < 95)
			status = Msg.getString("PersonTableModel.column.stress.level4");
		else
			status = Msg.getString("PersonTableModel.column.stress.level5");
		return status;
	}

	/**
	 * Give the status of a person's hunger level
	 *
	 * @param hunger
	 * @return status
	 */
	public static String getPerformanceStatus(double value) {
		String status = "N/A";
		if (value > 95)
			status = Msg.getString("PersonTableModel.column.performance.level1");
		else if (value > 75)
			status = Msg.getString("PersonTableModel.column.performance.level2");
		else if (value > 50)
			status = Msg.getString("PersonTableModel.column.performance.level3");
		else if (value > 25)
			status = Msg.getString("PersonTableModel.column.performance.level4");
		else 
			status = Msg.getString("PersonTableModel.column.performance.level5");
		// logger.info(" Perf : " + Math.round(value) + " ; status : " +
		// status);
		return status;
	}

	/**
	 * Checks if the person has any serious medical problems.
	 * 
	 * @return true if serious medical problems
	 */
	public boolean hasSeriousMedicalProblems() {
		boolean result = false;
		Iterator<HealthProblem> meds = getProblems().iterator();
		while (meds.hasNext()) {
			if (meds.next().getIllness().getSeriousness() >= 50)
				result = true;
		}
		return result;
	}

	/**
	 * Gets a list of medication affecting the person.
	 * 
	 * @return list of medication.
	 */
	public List<Medication> getMedicationList() {
		return new ArrayList<Medication>(medicationList);
	}

	/**
	 * Checks if the person is affected by the given medication.
	 * 
	 * @param medicationName the name of the medication.
	 * @return true if person is affected by it.
	 */
	public boolean hasMedication(String medicationName) {
		if (medicationName == null)
			throw new IllegalArgumentException("medicationName is null");

		boolean result = false;

		Iterator<Medication> i = medicationList.iterator();
		while (i.hasNext()) {
			if (medicationName.equals(i.next().getName()))
				result = true;
		}

		return result;
	}

	/**
	 * Adds a medication that affects the person.
	 * 
	 * @param medication the medication to add.
	 */
	public void addMedication(Medication medication) {
		if (medication == null)
			throw new IllegalArgumentException("medication is null");
		medicationList.add(medication);
	}

	/**
	 * Gets the oxygen consumption rate per Sol.
	 * 
	 * @return oxygen consumed (kg/Sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getOxygenConsumptionRate() {
		return o2_consumption;
	}

	/**
	 * Gets the water consumption rate per Sol.
	 * 
	 * @return water consumed (kg/Sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getWaterConsumptionRate() {
		return h2o_consumption;
	}

	public double getWaterConsumedPerServing() {
		return waterConsumedPerServing;
	}

	/**
	 * Gets the food consumption rate per Sol.
	 * 
	 * @return food consumed (kg/Sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getFoodConsumptionRate() {
		return food_consumption;
	}

	/**
	 * Gets the dessert consumption rate per Sol.
	 * 
	 * @return dessert consumed (kg/Sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getDessertConsumptionRate() {
		return dessert_consumption;
	}

	/**
	 * Gets the person with this physical condition
	 * 
	 * @return
	 */
	public Person getPerson() {
		return person;
	}

	public RadiationExposure getRadiationExposure() {
		return radiation;
	}

	/**
	 * Gets the medical manager.
	 * 
	 * @return medical manager.
	 */
	private MedicalManager getMedicalManager() {
		if (medicalManager == null)
			medicalManager = Simulation.instance().getMedicalManager();
		return medicalManager;
	}

	public double getBodyMassDeviation() {
		return bodyMassDeviation;
	}

	public boolean isStressedOut() {
		return isStressedOut;
	}

	public boolean isRadiationPoisoned() {
		return isRadiationPoisoned;
	}

	public double[] getMusculoskeletal() {
		return musculoskeletal;
	}

	public void setMuscularSystem(double[] value) {
		musculoskeletal = value;
	}

	public void workOut() {
		musculoskeletal[0] = musculoskeletal[0] + .01; // pain tolerance
		musculoskeletal[2] = musculoskeletal[0] + .1; // muscle soreness
	}

	public double getStarvationStartTime() {
		return starvationStartTime;
	}

	
	public double getDehydrationStartTime() {
		return dehydrationStartTime;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		deathDetails = null;
		// problems.clear();
		problems = null;
		serious = null;
		person = null;

		radiation = null;
		circadian = null;
		taskMgr = null;
		starved = null;
		dehydrated = null;
		medicalManager = null;
		marsClock = null;
		dehydration = null;
		starvation = null;
		freezing = null;
		heatStroke = null;
		decompression = null;
		suffocation = null;
		depression = null;
		panicAttack = null;
		highFatigue = null;
		radiationPoisoning = null;

		// if (medicationList != null) medicationList.clear();
		medicationList = null;
		allMedicalComplaints = null;

	}
}
