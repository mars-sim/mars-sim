/*
 * Mars Simulation Project
 * PhysicalCondition.java
 * @date 2025-08-09
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import com.mars_sim.core.LifeSupportInterface;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.meta.EatDrinkMeta;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.health.Complaint;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.person.health.HealthRiskType;
import com.mars_sim.core.person.health.MedicalManager;
import com.mars_sim.core.person.health.Medication;
import com.mars_sim.core.person.health.CuredProblem;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadioProtectiveAgent;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class represents the Physical Condition of a Person. It models a
 */
public class PhysicalCondition implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PhysicalCondition.class.getName());

	/** The maximum number of sols for storing stats. */
	public static final int MAX_NUM_SOLS = 7;
	/** The maximum number of sols in fatigue [millisols]. */
	public static final int MAX_FATIGUE = 40_000;
	/** The maximum number of sols in hunger [millisols]. */
	public static final int MAX_HUNGER = 40_000;
	/** Reset to hunger [millisols] immediately upon eating. */
	public static final int HUNGER_CEILING_UPON_EATING = 750;
	/** The maximum number of sols in thirst [millisols]. */
	public static final int MAX_THIRST = 7_000;
	/** The maximum number of sols in thirst [millisols]. */
	public static final int THIRST_CEILING_UPON_DRINKING = 500;
	/** The amount of thirst threshold [millisols]. */
	public static final int THIRST_THRESHOLD = 150;
	/** The amount of thirst threshold [millisols]. */
	public static final int HUNGER_THRESHOLD = 250;
	/** The amount of thirst threshold [millisols]. */
	public static final int ENERGY_THRESHOLD = 2525;
	/** The amount of fatigue threshold [millisols]. */
	private static final int FATIGUE_THRESHOLD = 750;
	/** The amount of fatigue threshold [millisols]. */
	public static final int FATIGUE_MIN = 150;
	/** The amount of stress threshold [millisols]. */
	private static final int STRESS_THRESHOLD = 75;
	/** Life support minimum value. */
	private static final int MIN_VALUE = 0;
	/** Life support maximum value. */
	private static final int MAX_VALUE = 1;

	/** Performance modifier for thirst. */
	private static final double THIRST_PERFORMANCE_MODIFIER = .00015D;
	/** Performance modifier for hunger. */
	private static final double HUNGER_PERFORMANCE_MODIFIER = .0001D;
	/** Performance modifier for fatigue. */
	private static final double FATIGUE_PERFORMANCE_MODIFIER = .0005D;
	/** Performance modifier for stress. */
	private static final double STRESS_PERFORMANCE_MODIFIER = .00075D;
	/** Performance modifier for energy. */
	private static final double ENERGY_PERFORMANCE_MODIFIER = .0001D;
	/** The average maximum daily energy intake */
	private static final double STANDARD_DAILY_ENERGY_INTAKE = 10100D;
	/** The average kJ of a 1kg food. Assume each meal has 0.1550 kg and has 2525 kJ. */
	public static final double FOOD_COMPOSITION_ENERGY_RATIO = 16290.323;
	// Note: 1kg of food has ~16290 kJ 
	// See notes on people.xml under <food-consumption-rate value="0.62" />
	public static final double ENERGY_FACTOR = 15D;
	/** The maximum air pressure a person can live without harm in kPa. (somewhat arbitrary). */
	public static final double MAXIMUM_AIR_PRESSURE = 68D; // Assume 68 kPa time dependent
	/** Period of time (millisols) over which random ailments may happen. */
	private static final double RANDOM_AILMENT_PROBABILITY_TIME = 100_000D;
	/** The standard pre-breathing time in the EVA suit. */
	private static final double STANDARD_PREBREATHING_TIME = 40;

	public static final String STANDARD_QUOTE_0 = "Thousands have lived without love, not one without water. – W.H.Auden.";
	public static final String STANDARD_QUOTE_1 = "Remember that no child should go empty stomach in the 21st century.";
	
	/** The default string for degree celsius */
	public static final String DEGREE_CELSIUS = Msg.getString("temperature.sign.degreeCelsius");

	public static final String TBD = "[To Be Determined]";
	private static final String TRIGGERED_DEATH = "Player Triggered Death";
	
	private static double o2Consumption;
	private static double h20Consumption;
	private static double minAirPressure;
	private static double minTemperature;
	private static double maxTemperature;
	private static double foodConsumption;


	/** True if person is starving. */
	private boolean isStarving;
	/** True if person is stressed out. */
	private boolean isStressedOut;
	/** True if person is dehydrated. */
	private boolean isDehydrated;
	/** True if person is alive. */
	private boolean alive;
	/** True if person is radiation Poisoned. */
	private boolean isRadiationPoisoned;
	/** True if person is doing a task that's considered resting. */
	private boolean restingTask;

	private int endurance;
	private int strength;
	private int agility;

	/**
	 * Person's Musculoskeletal system from 0 to 100 (muscle pain tolerance, muscle
	 * health, muscle soreness).
	 */
	private double musclePainTolerance;
	private double muscleHealth;
	private double muscleSoreness;
	
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
	/** Person's energy level [in kJ] */
	private double kJoules;
	/** Person's food appetite (o to 1) */
	private double appetite;
	
	/** The time it takes to prebreathe the air mixture in the EVA suit. */
	private double remainingPrebreathingTime = STANDARD_PREBREATHING_TIME + RandomUtil.getRandomInt(-5, 5);

	private double starvationStartTime;
	
	private double dehydrationStartTime;
	/** Person's max daily energy in kJ */
	private double personalMaxEnergy = STANDARD_DAILY_ENERGY_INTAKE;
	/** Person's Body Mass Deviation */
	private double bodyMassDeviation;
	/** Person's Body Mass Index (BMI) */
	private double bmi;
	
	/**  The amount of water this person would consume each time (assuming drinking water 10 times a day). */
	private double waterConsumedPerServing;
	
	private double waterConsumedPerSol;

	private double attributeCompositeScore;
	
	/** Person owning this physical. */
	private Person person;
	/** Details of persons death. */
	private DeathInfo deathDetails;
	/** Radiation Exposure. */
	private RadiationExposure radiation;

	/** List of medications affecting the person. */
	private List<Medication> medicationList;
	/** Injury/Illness effecting person. */
	private List<HealthProblem> problems;
	/** List of the cured problems */
	private List<CuredProblem> history;

	/** Record of Illness frequency. */
	private Map<ComplaintType, Integer> healthLog;

	/** Health Risk probability. */
	private Map<HealthRiskType, Double> healthRisks;
	
	
	/** 
	 * The amount a person consumes on each sol.
	 * 0: food (kg), 1: meal (kg), 2: dessert (kg), 3: water (kg), 4: oxygen (kg)
	 */
	private SolMetricDataLogger<Integer> consumption;
	
	/** The CircadianClock instance. */
	private transient CircadianClock circadian;
	/** The NaturalAttributeManager instance. */
	private transient NaturalAttributeManager naturalAttributeManager;

	/** The HealthProblem instance. */
	private HealthProblem starved;
	/** The HealthProblem instance. */
	private HealthProblem dehydrated;
	/** Most mostSeriousProblem problem. */
	private HealthProblem mostSeriousProblem;

	private static MasterClock master;

	private static EatDrinkMeta eatMealMeta = new EatDrinkMeta();
	private static MedicalManager medicalManager;

	private static PersonConfig personConfig;

	/**
	 * Constructor 1.
	 *
	 * @param newPerson The person requiring a physical presence.
	 */
	public PhysicalCondition(Person newPerson) {
		person = newPerson;

		circadian = person.getCircadianClock();
		naturalAttributeManager = person.getNaturalAttributeManager();

		alive = true;

		deathDetails = null;

		problems = new CopyOnWriteArrayList<>();
		healthLog = new ConcurrentHashMap<>();
		history = new ArrayList<>();
		healthRisks = new EnumMap<>(HealthRiskType.class);
		medicationList = new CopyOnWriteArrayList<>();

		endurance = naturalAttributeManager.getAttribute(NaturalAttributeType.ENDURANCE);
		strength = naturalAttributeManager.getAttribute(NaturalAttributeType.STRENGTH);
		agility = naturalAttributeManager.getAttribute(NaturalAttributeType.AGILITY);

		// Computes the adjustment from a person's natural attributes
		// value is between 0 and 2
		attributeCompositeScore = (1.5 * endurance + .75 * strength + .75 * agility) / 150;

		// Note: may incorporate real world parameters such as areal density in g cm−2,
		// T-score and Z-score (see https://en.wikipedia.org/wiki/Bone_density)

		musclePainTolerance = (.5 + RandomUtil.getRandomDouble(.5)) * (50 * attributeCompositeScore); 
		muscleSoreness = (.5 + RandomUtil.getRandomDouble(.5)) * (100 - musclePainTolerance); 
		muscleHealth = (.5 + RandomUtil.getRandomDouble(.5)) * (50 + musclePainTolerance + muscleSoreness); 
		
		double height = person.getHeight();
		double heightSquared = height*height/100/100;
		double defaultHeight = personConfig.getDefaultPhysicalChars().getAverageHeight();
		double mass = person.getBaseMass();
		double defaultMass = personConfig.getDefaultPhysicalChars().getAverageWeight();
		
		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// bodyMassDeviation average around 0.7 to 1.3
		bodyMassDeviation = RandomUtil.getGaussianPositive(Math.sqrt(mass/defaultMass*height/defaultHeight), .4);						
		bmi = mass/heightSquared;

		// Assume a person drinks 10 times a day, each time ~375 mL
		waterConsumedPerSol = h20Consumption * bodyMassDeviation ;
		// waterConsumedPerServing is ~ 0.19 kg
		waterConsumedPerServing = waterConsumedPerSol / 10; 

		double sTime = personConfig.getStarvationStartTime();
		starvationStartTime = 1000D * RandomUtil.getGaussianPositive(sTime, bodyMassDeviation / 5);
		
		double dTime = personConfig.getDehydrationStartTime();
		dehydrationStartTime = 1000D * RandomUtil.getGaussianPositive(dTime, bodyMassDeviation / 5);

		isStarving = false;
		isStressedOut = false;
		isDehydrated = false;
		// Initially set performance to 1.0 (=100%) to avoid issues at startup
		performance = 1.0D;

		// Initialize the food consumption logger
		consumption = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		consumption.increaseDataPoint(0, 0.0);
		consumption.increaseDataPoint(1, 0.0);
		consumption.increaseDataPoint(2, 0.0);
		consumption.increaseDataPoint(3, 0.0);
		consumption.increaseDataPoint(4, 0.0);
		
		radiation = new RadiationExposure(newPerson, bodyMassDeviation, attributeCompositeScore);

		initialize();
	}

	private void initializeHealthIndices() {
		// Set up random physical health index
		thirst = RandomUtil.getRandomRegressionInteger(50);
		fatigue = RandomUtil.getRandomRegressionInteger(50);
		stress = RandomUtil.getRandomRegressionInteger(10);
		hunger = RandomUtil.getRandomRegressionInteger(50);
		// kJoules somewhat co-relates with hunger
		kJoules = 10000 + (50 - hunger) * 100;
		performance = 1.0D - (50 - fatigue) * .002 
				- (20 - stress) * .002 
				- (50 - hunger) * .002
				- (50 - thirst) * .002;
	}

	
	/**
	 * Initializes the health risk probability map.
	 * 
	 * @return
	 */
	public void initializeHealthRisks() {
		
		for (HealthRiskType type: HealthRiskType.values()) {
			double probability = RandomUtil.getRandomDouble(5);
			healthRisks.put(type, probability);
		}
	}
	
	
	/**
	 * Initialize values and instances at the beginning of sol 1
	 * (Note : Must skip this when running maven test or else having exceptions)
	 */
	void initialize() {
		// Set up the initial values for each physical health index
		initializeHealthIndices();
		// Derive the personal appetite
		updateAppetite();
		// Derive personal max energy
		updateMaxEnergy();
		// Set up the initial values for health risks
		initializeHealthRisks();
	}

	/**
	 * Updates the personal max energy.
	 */
	void updateMaxEnergy() {
		// Update personal max energy
		personalMaxEnergy = MathUtils.between(STANDARD_DAILY_ENERGY_INTAKE * (1 + appetite/2),
				STANDARD_DAILY_ENERGY_INTAKE / 10, STANDARD_DAILY_ENERGY_INTAKE * 2);;
	}
	
	/**
	 * Updates the personal appetite.
	 */
	void updateAppetite() {
		// Assume that after age 35, metabolism slows down
		int ageFactor = 35 - person.getAge();
		// Get mass factor 
		double averageWeight = personConfig.getDefaultPhysicalChars().getAverageWeight();
		// Get mass factor 
		double massFactor = (person.getBaseMass() - averageWeight) / averageWeight;
		// Get the leptin level
		double leptinLevel = circadian.getLeptin();
		// Get the Ghrelin level
		double GhrelinLevel = circadian.getGhrelin();
		
		double mod = (GhrelinLevel - leptinLevel)/200.0;
		// Get eating pref 
		double eatingPref = person.getPreference().getPreferenceScore(eatMealMeta)/10.0;
		// Derive the appetite
		appetite = (35.0 + ageFactor)/70.0 + massFactor + eatingPref + mod;
		// Limit to between 0 and 1
		appetite = MathUtils.between(appetite, 0, 1);
	}
	
	/**
	 * Gets the personal max energy.
	 */
	public double getPersonalMaxEnergy() {
		return personalMaxEnergy;
	}
	
	/**
	 * Gets the personal appetite.
	 * 
	 * @return
	 */
	public double getAppetite() {
		return appetite;
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
	public void timePassing(ClockPulse pulse, LifeSupportInterface support) {
		
		if (alive) {
			
			double time = pulse.getElapsed();
			
			// Check once a day only
			if (pulse.isNewSol()) {	
//				int solOfMonth = pulse.getMarsTime().getSolOfMonth();
//				if (solOfMonth == 1 || solOfMonth == 8 || solOfMonth == 15 || solOfMonth == 22 || solOfMonth == 29) {
					// Update the personal appetite
					updateAppetite();
					// Update personal max energy
					updateMaxEnergy();
//				}
			}
			
			// Check once per msol (millisol integer)
			if (pulse.isNewIntMillisol()) {
				// Calculate performance and most mostSeriousProblem illness.
				recalculatePerformance();
				// Check radiation 
				radiation.timePassing(pulse);
				
//				// Get stress factor due to settlement overcrowding
//				if (person.isInSettlement() && !person.isRestingTask()) {
//					// Note: this stress factor is different from LifeSupport's timePassing's
//					//       stress modifier for each particular building
//					double stressFactor = person.getSettlement().getStressFactor(time);
//					
//					if (stressFactor > 0) {
//						// Update stress
//				        addStress(stressFactor);
//					}
//				}				
				
				if (stress < STRESS_THRESHOLD) {
					isStressedOut = false;
				}
				
				int msol = pulse.getMarsTime().getMillisolInt();
				if (msol % 7 == 0) {

					// Update starvation
					checkStarvation(hunger);
					// Update dehydration
					checkDehydration(thirst);					
					// Check if person is stressed out
					checkStressOut();
					
					// Check if person is at very high fatigue may collapse.

					// Check radiation poisoning
					if (!isRadiationPoisoned) {
						checkRadiationPoisoning(time);
					}
				}
			}

            double currentO2Consumption;
			if (person.isRestingTask())
				currentO2Consumption = personConfig.getLowO2ConsumptionRate();
			else
				currentO2Consumption = personConfig.getNominalO2ConsumptionRate();

			// Check life support system
			checkLifeSupport(time, currentO2Consumption, support);
			// Update the existing health problems
			checkHealth(pulse);
			
			
			double factor = 1;
			
			if (person.isRestingTask()) {
				// Modify the time factor
				factor = 2;
			}
			
			// Reduce stress
			reduceStress(time / 10 * factor);
			// Update thirst
			increaseThirst(time * bodyMassDeviation * .75 / factor);
			// Update fatigue
			increaseFatigue(time * 1.1 / factor);
			// Update hunger
			increaseHunger(time * bodyMassDeviation * .75 / factor);
			// Update the entropy in muscles
//			muscularAtrophy(time / 4);	
		}
	}

	 /**
	  * Checks and updates existing health problems
	  *
	  * @param time
	  */
	private void checkHealth(ClockPulse pulse) {
		boolean illnessEvent = false;

		if (!problems.isEmpty()) {
			// Throw illness event if any problems already exist.
			illnessEvent = true;
			// A list of complaints (Type of illnesses)
			List<Complaint> newComplaints = new CopyOnWriteArrayList<>();

			Iterator<HealthProblem> hp = problems.iterator();
			while (hp.hasNext()) {
				HealthProblem problem = hp.next();
				// Advance each problem, they may change into a worse problem.
				// If the current is completed or a new problem exists then
				// remove this one.
				Complaint nextComplaintPhase = problem.timePassing(pulse.getElapsed(), this);

				// After sleeping sufficiently, the high fatigue collapse should no longer exist.

				if ((problem.getState() == HealthProblemState.CURED) || (nextComplaintPhase != null)) {

					ComplaintType type = problem.getType();

					logger.log(person, Level.INFO, 20_000,
							"Cured from " + type.getName() + ".");

					if (type == ComplaintType.DEHYDRATION)
						isDehydrated = false;

					else if (type == ComplaintType.STARVATION)
						isStarving = false;

					else if (type == ComplaintType.RADIATION_SICKNESS)
						isRadiationPoisoned = false;
				
					// If nextPhase is not null, remove this problem so that it can
					// properly be transitioned into the next.
					problems.remove(problem);
					
					history.add(problem.toCured(pulse.getMarsTime()));

					Medication expired = null;
					
					for (Medication med : getMedicationList()) {
						// Remove the medication related to this particular problem
						if (problem.getComplaint().getType() == med.getComplaintType()
							// Tag the medication
							|| !med.isMedicated()) {
							expired = med;
							break;
						}
					}
					
					// Take a person off medications that have been "expired"
					getMedicationList().remove(expired);
				}

				// If a new problem, check it doesn't exist already
				if (nextComplaintPhase != null) {
					newComplaints.add(nextComplaintPhase);
				}
			}

			// Add the new problems
			for (Complaint c : newComplaints) {
				addMedicalComplaint(c);
				illnessEvent = true;
			}
		}

		// Generates any random illnesses.
		if (!restingTask) {
			List<Complaint> randomAilments = checkForRandomAilments(pulse);
			illnessEvent = !randomAilments.isEmpty();
		}

		if (illnessEvent) {
			person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
		}
		
		// Add time to all medications affecting the person.
		for (Medication med : getMedicationList()) {
			med.timePassing(pulse);
		}
	}

	/**
	 * Checks on the life support.
	 *
	 * @param time
	 * @param currentO2Consumption
	 * @param support
	 */
	private void checkLifeSupport(double time, double currentO2Consumption, LifeSupportInterface support) {
		if (time > 0) {
			try {
				if (lackOxygen(support, currentO2Consumption * (time / 1000D)))
					logger.severe(person, 60_000, "Reported lack of oxygen.");
				if (badAirPressure(support, minAirPressure))
					logger.severe(person, 60_000, "Reported non-optimal air pressure.");
				if (badTemperature(support, minTemperature, maxTemperature))
					logger.severe(person, 60_000, "Reported non-optimal temperature.");

			} catch (Exception e) {
				logger.severe(person, 60_000, "Reported anomaly in the life support system: ", e);
			}
		}
	}

	/**
	 * Gets the person's fatigue level.
	 *
	 * @return the value from 0 to infinity.
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
		double xdelta = time * STANDARD_DAILY_ENERGY_INTAKE / 1000D;

		// Changing this to a more linear reduction of energy.
		// We may want to change it back to exponential. - Scott

		if (kJoules < 250) {
			// 250 kJ is the lowest possible energy level
			kJoules = 250;
		}
		else if (kJoules < 500) {
			kJoules -= xdelta * .2;
		} else if (kJoules < 1000) {
			kJoules -= xdelta * .25;
		} else if (kJoules < 3000) {
			kJoules -= xdelta * .3;
		} else if (kJoules < 5000) {
			kJoules -= xdelta * .35;
		} else if (kJoules < 7000) {
			kJoules -= xdelta * .4;
		} else if (kJoules < 9000) {
			kJoules -= xdelta * .45;
		} else if (kJoules < 11000) {
			kJoules -= xdelta * .5;
		} else if (kJoules < 13000) {
			kJoules -= xdelta * .55;
		} else if (kJoules < 15000) {
			kJoules -= xdelta * .6;			
		} else if (kJoules < 17000) {
			kJoules -= xdelta * .65;	
		} else
			kJoules -= xdelta * .7;

		person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
	}
	
	/**
	 * Adds to the person's energy intake by eating.
	 *
	 * @param person's energy level in kilojoules
	 */
	public void addEnergy(double foodAmount) {
		// 1 calorie = 4.1858 kJ

		// Each meal (0.155 kg = 0.62 kg daily / a total of 4 meals) has an average of 2525 kJ

		// Note: FOOD_COMPOSITION_ENERGY_RATIO = 16290
		double xdelta = foodAmount * FOOD_COMPOSITION_ENERGY_RATIO 
				* (.75 + .75 * appetite) / ENERGY_FACTOR;

		if (hunger <= 0)
			kJoules = personalMaxEnergy;
		else if (kJoules > 19_000) {
			kJoules += xdelta * .035;
		} else if (kJoules > 17_000) {
			kJoules += xdelta * .06;
		} else if (kJoules > 15_000) {
			kJoules += xdelta * .15;
		} else if (kJoules > 13_000) {
			kJoules += xdelta * .2;
		} else if (kJoules > 11_000) {
			kJoules += xdelta * .25;
		} else if (kJoules > 9_000) {
			kJoules += xdelta * .3;
		} else if (kJoules > 7_000) {
			kJoules += xdelta * .45;
		} else if (kJoules > 5_000) {
			kJoules += xdelta * .55;
		} else if (kJoules > 4_000) {
			kJoules += xdelta * .65;
		} else if (kJoules > 3_000) {
			kJoules += xdelta * .75;			
		} else if (kJoules > ENERGY_THRESHOLD) {
			kJoules += xdelta * .85;	
		} else if (kJoules > ENERGY_THRESHOLD / 2) {
			kJoules += xdelta * .95;	
		} else if (kJoules > ENERGY_THRESHOLD / 4) {
			kJoules += xdelta * 1.1;	
		} else if (kJoules > ENERGY_THRESHOLD / 8) {
			kJoules += xdelta * 1.3;	
		} else
			kJoules = ENERGY_THRESHOLD / 8.0;

		circadian.eatFood(xdelta / 1000D);

		if (kJoules > personalMaxEnergy) {
			kJoules = personalMaxEnergy;
		}
		
		person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
	}

	/**
	 * Gets the performance factor that effect Person with the complaint.
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
		double pp = 0;
		// Muscle soreness impacts the physical performance
		if (p < 0)
			pp = p / getPainSorenessFactor();
		else if (p > 0)
			pp = p * getPainSorenessFactor();

		if (pp > 1D)
			pp = 1D;
		else if (pp < 0)
			pp = 0;
		if (performance != pp) {
			performance = pp;
			person.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
		}
	}

	
	/**
	 * Sets the fatigue value for this person.
	 *
	 * @param newFatigue New fatigue.
	 */
	public void setFatigue(double f) {
		double ff = f;
		if (ff > MAX_FATIGUE)
			ff = MAX_FATIGUE;
		else if (ff < -100)
			ff = -100;

		fatigue = ff;
		person.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);
	}
	
	/**
	 * Increases the fatigue for this person.
	 *
	 * @param delta
	 */
	public void increaseFatigue(double delta) {
		double f = fatigue + delta;
		if (f > MAX_FATIGUE)
			f = MAX_FATIGUE;

		fatigue = f;	
		person.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);
	}
	
	/**
	 * Reduces the fatigue for this person.
	 *
	 * @param delta
	 */
	public void reduceFatigue(double delta) {
		double f = fatigue - delta;
		if (f < -50) 
			f = -50;
		
		fatigue = f;
		person.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);
	}
	
	/**
	 * Sets the thirst value for this person.
	 * 
	 * @param t
	 */
	public void setThirst(double t) {
		double tt = t;
		if (tt > MAX_THIRST)
			tt = MAX_THIRST;
		else if (tt < -50)
			tt = -50;

		thirst = tt;
		person.fireUnitUpdate(UnitEventType.THIRST_EVENT);
	}

	/**
	 * Reduces the thirst setting for this person.
	 *
	 * @param thirstRelieved
	 */
	public void reduceThirst(double delta) {
		double t = thirst - delta;
		if (t < -50)
			t = -50;
		else if (t > THIRST_CEILING_UPON_DRINKING)
			t = THIRST_CEILING_UPON_DRINKING;
		
		thirst = t;
		person.fireUnitUpdate(UnitEventType.THIRST_EVENT);
	}
	
	/**
	 * Increases the hunger setting for this person.
	 *
	 * @param delta
	 */
	public void increaseThirst(double delta) {
		double t = thirst + delta;
		if (t > MAX_THIRST)
			t = MAX_THIRST;
		
		thirst = t;
		person.fireUnitUpdate(UnitEventType.THIRST_EVENT);
	}

	/**
	 * Defines the hunger setting for this person.
	 *
	 * @param newHunger New hunger.
	 */
	public void setHunger(double newHunger) {
		double h = newHunger;
		if (h > MAX_HUNGER)
			h = MAX_HUNGER;
		else if (h < -100)
			h = -100;

		hunger = h;
		person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
	}

	/**
	 * Reduces the hunger setting for this person.
	 *
	 * @param hungerRelieved
	 */
	public void reduceHunger(double hungerRelieved) {
		double h = hunger - hungerRelieved;
		if (h < -100)
			h = -100;
		else if (h > HUNGER_CEILING_UPON_EATING)
			h = HUNGER_CEILING_UPON_EATING;
		
		hunger = h;
		person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
	}
	
	/**
	 * Increases the hunger setting for this person.
	 *
	 * @param hungerAdded
	 */
	public void increaseHunger(double hungerAdded) {
		double h = hunger + hungerAdded * (appetite * .75 + .75);
		if (h > MAX_HUNGER)
			h = MAX_HUNGER;

		hunger = h;
		person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
	}
	
	/**
	 * Gets the person's hunger level.
	 *
	 * @return person's hunger
	 */
	public double getHunger() {
		return hunger;
	}

	/**
	 * Sets the person's stress level.
	 *
	 * @param s the new stress level (0.0 to 100.0)
	 */
	public void setStress(double s) {
		double ss = 0;
		// Muscle pain tolerance may impact the stress level
		if (s < 0)
			ss = s / getPainToleranceFactor();
		else if (s > 0)
			ss = s * getPainToleranceFactor();
		if (ss > 100)
			ss = 100;
		else if (ss < 0
				|| Double.isNaN(stress))
			ss = 0D;
		
		stress = ss;
		person.fireUnitUpdate(UnitEventType.STRESS_EVENT);
	}
	
	/**
	 * Adds to a person's stress level.
	 *
	 * @param d
	 */
	public void addStress(double d) {
		if (stress > 95) {
			logger.warning(person, 30_000, "stress: " + Math.round(stress * 1000.0)/1000.0 + "  d: " + Math.round(d * 1000.0)/1000.0);
		}
		// Note: Some research findings indicate that individuals 
		// with depression often exhibit lower pain tolerance.
		double ss = stress + d / getPainToleranceFactor();
		if (ss > 100)
			ss = 100;
		else if (ss < 0
			|| Double.isNaN(ss))
			ss = 0;
		
		stress = ss;
		person.fireUnitUpdate(UnitEventType.STRESS_EVENT);
	}

	/**
	 * Reduces to a person's stress level.
	 *
	 * @param d
	 */
	public void reduceStress(double d) {
		// Assume high pain tolerance may be associated with low depression/stress. 
		double ss = stress - d * getPainToleranceFactor();
		if (ss > 100)
			ss = 100;
		else if (ss < 0
			|| Double.isNaN(ss))
			ss = 0;
		
		stress = ss;
		person.fireUnitUpdate(UnitEventType.STRESS_EVENT);
	}
	
	/**
	 * Gets the person's stress level.
	 *
	 * @return stress (0.0 to 100.0)
	 */
	public double getStress() {
		return stress;
	}
	
	/**
	 * Checks if a person suffers from stress related health problem.
	 */
	private void checkStressOut() {
		if (stress >= STRESS_THRESHOLD && !isStressedOut()) {
			isStressedOut = true;
		}
		
		if (isStressedOut()) {
			HealthProblem panic = getProblemByType(ComplaintType.PANIC_ATTACK);
	
			if (panic == null || !problems.contains(panic)) {
				if (stress >= 100.0) {
					addMedicalComplaint(medicalManager.getPanicAttack());
					person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
				}
				else if (stress >= STRESS_THRESHOLD) {
					// Anything to do here ?
				}
			}
			
			else if (panic != null && stress < STRESS_THRESHOLD) {
				
				panic.setCured();
				
				isStressedOut = false;

				logger.log(person, Level.INFO, 20_000, "No longer having panic attack (case 2).");
			}
		}
	}
	
	/**
	 * Checks if a person is starving or no longer starving.
	 *
	 * @param hunger
	 */
	private void checkStarvation(double hunger) {

		starved = getProblemByType(ComplaintType.STARVATION);
		
		if (!isStarving && hunger > starvationStartTime) {

			// if problems doesn't have starvation, execute the following
			if (starved == null || !problems.contains(starved)) {
				addMedicalComplaint(medicalManager.getStarvation());
				isStarving = true;
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
				logger.log(person, Level.INFO, 20_000, "Starting starving.");
			}

			// Note : how to tell a person to walk back to the settlement ?
			// Note : should check if a person is on a critical mission,
		}

		else if (starved != null && isStarving) {

			if (hunger < HUNGER_THRESHOLD || kJoules > ENERGY_THRESHOLD) {

				starved.setCured();
				// Set isStarving to false
				isStarving = false;

				logger.log(person, Level.INFO, 20_000, "No longer starving (case 2).");
			}

			// If this person's hunger has reached the buffer zone
			else if (hunger < HUNGER_THRESHOLD * 2 || kJoules > ENERGY_THRESHOLD * 2) {

				starved.startRecovery();
				// Set to not starving
				isStarving = false;

				logger.log(person, Level.INFO, 20_000, "Recovering from hunger. "
						 + "  Hunger: " + (int)hunger
						 + ";  kJ: " + Math.round(kJoules*10.0)/10.0
						 + ";  isStarving: " + isStarving
						 + ";  Status: " + starved.getState());
			}
			
			else if (hunger >= MAX_HUNGER) {
				starved.setState(HealthProblemState.DEAD);
				recordDead(starved, false, STANDARD_QUOTE_1);
			}
		}
	}


	/**
	 * Checks if a person is dehydrated.
	 *
	 * @param hunger
	 */
	private void checkDehydration(double thirst) {

		dehydrated = getProblemByType(ComplaintType.DEHYDRATION);
		
		// If the person's thirst is greater than dehydrationStartTime
		if (!isDehydrated && thirst > dehydrationStartTime) {

			if (dehydrated == null || !problems.contains(dehydrated)) {
				addMedicalComplaint(medicalManager.getDehydration());
				isDehydrated = true;
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
			}
		}

		else if (dehydrated != null && isDehydrated) {

			if (thirst < THIRST_THRESHOLD / 2) {
				dehydrated.setCured();
				// Set dehydrated to false
				isDehydrated = false;

				logger.log(person, Level.INFO, 0, "No longer dehydrated (case 2).");
			}

			// If this person's thirst has reached the buffer zone
			else if (thirst < THIRST_THRESHOLD * 2) {

				dehydrated.startRecovery();
				// Set dehydrated to false
				isDehydrated = false;

				logger.log(person, Level.INFO, 20_000, "Recovering from dehydration. "
						 + "  Thirst: " + (int)thirst
						 + ";  isDehydrated: " + isDehydrated
						 + ";  Status: " + dehydrated.getState());
			}
			else if (thirst >= MAX_THIRST) {
				dehydrated.setState(HealthProblemState.DEAD);
				recordDead(dehydrated, false, STANDARD_QUOTE_0);
			}
		}
	}

	/**
	 * Gets the health problem by a certain complaint type.
	 *
	 * @return Health problem or null if the Person does not have it
	 */
	private HealthProblem getProblemByType(ComplaintType type) {
		for (HealthProblem p: problems) {
			if (p.getType() == type) {
				return p;
			}
		}
		return null;
	}


	/**
	 * Checks if person has radiation poisoning.
	 *
	 * @param time the time passing (millisols)
	 */
	private void checkRadiationPoisoning(double time) {
		
		// Future: need to double check on a person's radiation dosage to determine if he's sick with it.
		
		var radiationPoisoned = getProblemByType(ComplaintType.RADIATION_SICKNESS);

		if (!isRadiationPoisoned && radiation.isSick()) {

			if (radiationPoisoned == null || !problems.contains(radiationPoisoned)) {
				addMedicalComplaint(medicalManager.getComplaintByName(ComplaintType.RADIATION_SICKNESS));
				isRadiationPoisoned = true;
				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
				logger.log(person, Level.INFO, 3000, "Collapsed because of radiation poisoning.");
			}

			else if (radiationPoisoned != null) {

				radiationPoisoned.setCured();
				// Set isStarving to false
				isRadiationPoisoned = false;

				logger.log(person, Level.INFO, 20_000, "No longer having radiation poisoning (case 1).");
			}
		}

		else if (isRadiationPoisoned) {

			if (!radiation.isSick()) {
				if (radiationPoisoned != null) {
					radiationPoisoned.setCured();
					// Set isRadiationPoisoned to false
					isRadiationPoisoned = false;

					logger.log(person, Level.INFO, 20_000, "No longer having radiation poisoning (case 2).");
				}
			}

			// If this person is taking anti-rad meds
			else if (hasMedication(RadioProtectiveAgent.NAME)) {

				if (radiationPoisoned == null)
					radiationPoisoned = getProblemByType(ComplaintType.RADIATION_SICKNESS);

				if (radiationPoisoned != null) {
					radiationPoisoned.startRecovery();
					// Set to not starving
					isRadiationPoisoned = false;
				}

				logger.log(person, Level.INFO, 20_000, "Taking anti-rad meds and recovering from radiation poisoning. "
						 + ";  isRadiationPoisoned: " + radiationPoisoned);
			}
		}

		else if (radiationPoisoned != null) {

			radiationPoisoned.setCured();
			// Set isRadiationPoisoned to false
			isRadiationPoisoned = false;

			logger.log(person, Level.INFO, 20_000, "No longer having radiationPoisoning (case 3).");
		}
	}


	/**
	 * Checks for any random ailments that a person comes down with over a period of
	 * time.
	 *
	 * @param time the time period (millisols).
	 * @return list of ailments occurring. May be empty.
	 */
	private List<Complaint> checkForRandomAilments(ClockPulse pulse) {
		double time  = pulse.getElapsed();

		Task activeTask = person.getTaskManager().getTask();

		PhysicalEffort taskEffort = (activeTask != null ? activeTask.getEffortRequired() : null);
		List<Complaint> result = new ArrayList<>();
		Collection<Complaint> list = medicalManager.getAllMedicalComplaints();
		for (Complaint complaint : list) {
			// Check each possible medical complaint.
			ComplaintType ct = complaint.getType();
			boolean noGo = hasComplaint(complaint);

			if (!noGo) {
				var t = person.getTaskManager().getTask();
				// If the Complaint effort influence is more than the effort of the Task then 
				// this complaint can not occur
				noGo  = (t != null) && ExperienceImpact.isEffortHigher(complaint.getEffortInfluence(),
													t.getEffortRequired());
			}

			// Can this complaint happen?
			if (!noGo) {
				double probability = complaint.getProbability();
				// Check that medical complaint has a probability > zero
				// since some complaints are secondary complaints and cannot be started
				// by itself
				if (probability > 0D) {
					double taskModifier = 1;
					double tendency = 1;

					int msol = pulse.getMarsTime().getMissionSol();

					if (healthLog.get(ct) != null && msol > 3)
						tendency = 0.5 + 1.0 * healthLog.get(ct) / msol;
					else
						tendency = 1.0;
					double immunity = 1.0 * endurance + strength;

					if (immunity > 100)
						tendency = .75 * tendency - .25 * immunity / 100.0;
					else
						tendency = .75 * tendency + .25 * (100 - immunity) / 100.0;

					if ((taskEffort == PhysicalEffort.HIGH) &&
							(PhysicalEffort.NONE != complaint.getEffortInfluence())) {
						// High effort is based on agility.
						taskModifier = 1.2;

						if (agility > 50)
							taskModifier = .75 * taskModifier - .25 * agility / 100.0;
						else
							taskModifier = .75 * taskModifier + .25 * (50 - agility) / 50.0;
					}
					else if ((taskEffort == complaint.getEffortInfluence()) 
									&& (taskEffort == PhysicalEffort.LOW)) {
						if (agility > 50)
							taskModifier = .75 * taskModifier - .25 * agility / 100.0;
						else
							taskModifier = .75 * taskModifier + .25 * (50 - agility) / 50.0;
					}
					else if (person.getTaskManager().getTask() instanceof EVAOperation)
						// match the uppercase EVA
						taskModifier = 1.3;

					tendency = MathUtils.between(tendency, 0.0001, 2D);

					// Randomly determine if person suffers from ailment.
					double rand = RandomUtil.getRandomDouble(100D);
					double timeModifier = time / RANDOM_AILMENT_PROBABILITY_TIME;

					if (rand <= probability * taskModifier * tendency * timeModifier) {
						addMedicalComplaint(complaint);
						result.add(complaint);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Does he have any complaints ?
	 * 
	 * @param c
	 * @return
	 */
	private boolean hasComplaint(Complaint c) {
		for (HealthProblem problem : problems) {
			if (problem.getType() == c.getType()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a new medical complaint to the person.
	 *
	 * @param complaint the new medical complaint
	 * @return 
	 */
	public HealthProblem addMedicalComplaint(Complaint c) {
		for (HealthProblem problem : problems) {
			if (problem.getType() == c.getType()) {
				return problem;
			}
		}

		ComplaintType type = c.getType();
		// Create a new health problem
		HealthProblem newProblem = new HealthProblem(type, person);
		problems.add(newProblem);

		// Record this complaint type
		int freq = 0;
		if (healthLog.get(type) != null)
			freq = healthLog.get(type);
		healthLog.put(type, freq + 1);
		logger.log(person, Level.INFO, 0, "Suffering from " + type.getName() + ".");
		recalculatePerformance();
		return newProblem;
	}


	/**
	 * Does the person consume enough oxygen ?
	 *
	 * @param support Life support system providing oxygen.
	 * @param amount  amount of oxygen to consume (in kg)
	 * @return new problem added.
	 * @throws Exception if error consuming oxygen.
	 */
	private boolean lackOxygen(LifeSupportInterface support, double amount) {
		if (amount > 0) {
			if (support == null) {
				logger.log(person, Level.SEVERE, 1000, "Had no life support.");
				return true;
			}
			else {
				double received = support.provideOxygen(amount);
				// Track the amount consumed
				addGasConsumed(ResourceUtil.OXYGEN_ID, received);
				// Note: how to model how much oxygen we need properly ?
				// Assume one half as the bare minimum
				double required = amount / 2D;

				return checkResourceConsumption(received, required, MIN_VALUE, ComplaintType.SUFFOCATION);
			}
		}

		return false;
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
	private boolean checkResourceConsumption(double actual, double required, int bounds,
					ComplaintType complaint) {

		boolean newProblem = false;
		if (actual - required > 0.000_1 || required - actual > 0.000_1)
			newProblem = false;
		else if ((bounds == MIN_VALUE) && (actual < required))
			newProblem = true;
		else if ((bounds == MAX_VALUE) && (actual > required))
			newProblem = true;

		if (newProblem) {
			String reading = "";
			String unit = "";
			double decimals = 10.0;
			switch (complaint) {
				case SUFFOCATION: 
					reading = "Oxygen";
					unit = " kg";
					decimals = 10_000.0;
					break;
				
				case DECOMPRESSION:
					reading = "Pressure";
					unit = " kPa";
					break;

				case FREEZING:
					reading = "Low Temperature";
					unit = " " + DEGREE_CELSIUS;
					break;

				case HEAT_STROKE:
					reading = "High Temperature";
					unit = " " + DEGREE_CELSIUS;
					break;
					
				default:
			}
			String s = reading + " sensor triggered. "
					+ " Actual: " + Math.round(actual*decimals)/decimals + unit
					+ " Required: " + Math.round(required*decimals)/decimals + unit;
			logger.log(person, Level.SEVERE, 20_000, s);

			addMedicalComplaint(medicalManager.getComplaintByName(complaint));
			person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
		}

//		else {
//			// Is the person suffering from the illness, if so recovery
//			// as the amount has been provided
//			HealthProblem illness = getProblemByType(complaint);
//			if (illness != null) {
//				illness.startRecovery();
//				person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
//			}
//		}
		return newProblem;
	}

	/**
	 * Person requires minimum air pressure.
	 *
	 * @param support  Life support system providing air pressure.
	 * @param pressure minimum air pressure person requires (in Pa)
	 * @return new problem added.
	 */
	private boolean badAirPressure(LifeSupportInterface support, double pressure) {
		return checkResourceConsumption(support.getAirPressure(), pressure, MIN_VALUE, ComplaintType.DECOMPRESSION);
	}

	/**
	 * Person requires minimum temperature.
	 *
	 * @param support     Life support system providing temperature.
	 * @param temperature minimum temperature person requires (in degrees Celsius)
	 * @return new problem added.
	 */
	private boolean badTemperature(LifeSupportInterface support, double minTemperature, double maxTemperature) {
		boolean freeze = checkResourceConsumption(support.getTemperature(), minTemperature, MIN_VALUE, ComplaintType.FREEZING);
		boolean hot = checkResourceConsumption(support.getTemperature(), maxTemperature, MAX_VALUE, ComplaintType.HEAT_STROKE);
		return freeze || hot;
	}

	/**
	 * Gets the details of this Person's death.
	 *
	 * @return Detail of the death, will be null if person is still alive.
	 */
	public DeathInfo getDeathDetails() {
		return deathDetails;
	}

	/**
	 * Revives this person who is dead and bring him back to life.
	 */
	public void reviveToLife() {
		alive = true;
		
		HealthProblem problem = deathDetails.getProblem();
		// Reset the declaredDead
		person.setRevived(problem);
		// Set the mind of the person to active
		person.getMind().setActive();
		// Reset the problem back to degrading
		problem.setState(HealthProblemState.DEGRADING);
		// Starts the recovery
		problem.startRecovery();
		// Transfer this person back to home settlement
		person.getAssociatedSettlement().addACitizen(person);	
		// Set death detail to null
		deathDetails = null;
		
		// Note: will automatically get a new role type	
		
		// Note: check if the vacated role has been filled or not 
		// Should the person retake the same role ?

		logger.log(person, Level.WARNING, 0, "Person was revived as ordered.");
		
		// Send the person to a medical building
		BuildingManager.addPatientToMedicalBed(person, person.getAssociatedSettlement());

		// Let the person go to sleep
		person.getTaskManager().replaceTask(new Sleep(person, 500));
	}

	
	/**
	 * Renders this Person dead, creates DeathInfo, and processes the change.
	 *
	 * @param problem      The health problem that contributes to his death.
	 * @param triggeredByPlayer True if it's caused by users
	 */
	public void recordDead(HealthProblem problem, boolean triggeredByPlayer, String lastWord) {
		alive = false;
		String reason = TBD;
		if (triggeredByPlayer) {
			reason = TRIGGERED_DEATH;
			logger.log(person, Level.WARNING, 0, "Declared dead. Reason: " + reason + ".");
		}

		setPerformanceFactor(0);
		
		// Set the state of the health problem to DEAD
		problem.setState(HealthProblemState.DEAD);
		
		// Set mostSeriousProblem to this problem
		this.mostSeriousProblem = problem;

		// Create the death details
		deathDetails = new DeathInfo(person, problem, reason, lastWord, master.getMarsTime());

		// Backup the role type
		deathDetails.setRoleType(person.getRole().getType());

		// Declare the person dead
		person.setDeclaredDead();
		
	    // Create medical event for performing an post-mortem exam
	    problem.registerHistoricalEvent(EventType.MEDICAL_DEATH);
	    
		// Add the person's death info to the postmortem exam waiting list
		// Note: what if a person died in a settlement outside of home town ?
		medicalManager.addPostmortemExam(person.getAssociatedSettlement(), deathDetails);
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

	/**
	 * Checks if the person is dehydrated.
	 * 
	 * @return
	 */
	public boolean isDehydrated() {
		return isDehydrated;
	}


	/**
	 * Gets the most mostSeriousProblem illness.
	 *
	 * @return most mostSeriousProblem illness
	 */
	public HealthProblem getMostSerious() {
		return mostSeriousProblem;
	}

	/**
	 * Returns a collection of known medical problems.
	 */
	public List<HealthProblem> getProblems() {
		return problems;
	}

	/**
	 * Calculates how the most mostSeriousProblem problem and other metrics would affect a
	 * person's performance.
	 */
	void recalculatePerformance() {

		double maxPerformance = 1.0D;

		mostSeriousProblem = null;

		// Check the existing problems. find most mostSeriousProblem problem and how it
		// affects performance. This is the performance baseline
		for (HealthProblem problem : problems) {
			double factor = problem.getPerformanceFactor();
			if (factor < maxPerformance) {
				maxPerformance = factor;
			}

			if ((mostSeriousProblem == null) || (mostSeriousProblem.getComplaint().getSeriousness() < problem.getComplaint().getSeriousness())) {
				mostSeriousProblem = problem;
			}
		}

		double tempPerformance = maxPerformance;

		// High thirst reduces performance.
		if (thirst > 800D) {
			tempPerformance -= (thirst - 800D) * THIRST_PERFORMANCE_MODIFIER / 2;
		} else if (thirst > 400D) {
			tempPerformance -= (thirst - 400D) * THIRST_PERFORMANCE_MODIFIER / 4;
		}

		// High hunger reduces performance.
		if (hunger > 1600D) {
			tempPerformance -= (hunger - 1600D) * HUNGER_PERFORMANCE_MODIFIER / 2;
		} else if (hunger > 800D) {
			tempPerformance -= (hunger - 800D) * HUNGER_PERFORMANCE_MODIFIER / 4;
		}

		// High fatigue reduces performance.
		if (fatigue > 1500D) {
			tempPerformance -= (fatigue - 1500D) * FATIGUE_PERFORMANCE_MODIFIER / 2;
		} else if (fatigue > 700D) {
			tempPerformance -= (fatigue - 700D) * FATIGUE_PERFORMANCE_MODIFIER / 4;
		}

		// High stress reduces performance.
		if (stress > 75D) {
			tempPerformance -= (stress - 75D) * STRESS_PERFORMANCE_MODIFIER / 2;
		} else if (stress > 50D) {
			tempPerformance -= (stress - 50D) * STRESS_PERFORMANCE_MODIFIER / 4;
		}

		// High kJoules improves performance and low kJoules hurts performance.
		if (kJoules > 7500) {
			tempPerformance += (kJoules - 7500) * ENERGY_PERFORMANCE_MODIFIER / 8;
		} else if (kJoules < 400) {
			tempPerformance -= 400_000 / kJoules * ENERGY_PERFORMANCE_MODIFIER / 4;
		}

		// The adjusted performance can not be more than the baseline max
		if (tempPerformance > maxPerformance) {
			tempPerformance = maxPerformance;
		}
		setPerformanceFactor(tempPerformance);

	}

	/**
	 * Checks if the person has any mostSeriousProblem medical problems.
	 *
	 * @return true if mostSeriousProblem medical problems
	 */
	public boolean hasSeriousMedicalProblems() {
		return problems.stream().anyMatch(m -> m.getComplaint().getSeriousness() >= 50);
	}

	/**
	 * Checks if a person is super unfit.
	 *
	 * @return
	 */
	public boolean isSuperUnfit() {
        return isUnfitByLevel(900, 90, 900, 550);
    }
	
	/**
	 * Checks if a person is nominally unfit.
	 *
	 * @return
	 */
	public boolean isNominallyUnfit() {
        return isUnfitByLevel(700, 70, 700, 450);
    }
	
	/**
	 * Screens if the person is fit for EVA.
	 * 
	 * @return
	 */
	public boolean isEVAFit() {
        return !isUnfitByLevel(500, 50, 500, 350);
	}
	
	/**
	 * Checks fitness against a certain standard.
	 * 
	 * @param fatMax
	 * @param stressMax
	 * @param hunMax
	 * @return
	 */
	public boolean isFitByLevel(int fatMax, int stressMax, int hunMax) {
        return !isUnfitByLevel(fatMax, stressMax, hunMax, hunMax/2);
	}

	/**
	 *  Checks fitness against some maximum levels.
	 *  
	 * @param fatMax
	 * @param stressMax
	 * @param hunMax
	 * @param thirstMax
	 * @return
	 */
	public boolean isUnfitByLevel(int fatMax, int stressMax, int hunMax, int thirstMax) {
        return (fatigue > fatMax || stress > stressMax
        		|| hunger > hunMax || thirst > thirstMax
        		|| hasSeriousMedicalProblems());
	}
	
	/**
	 * Returns the health score of a person. 0 being lowest. 100 being the highest.
	 * 
	 * @return
	 */
	public double computeHealthScore() {
		return (Math.max(100 - fatigue/10, 0) 
				+ Math.max(100 - stress, 0) 
				+ Math.max(100 - hunger/10, 0) 
				+ Math.max(100 - thirst/10, 0) 
				+ Math.max(100 - performance * 100, 0))
				/ 5.0;
	}
	
	/**
	 * Computes the fitness level.
	 *
	 * @return
	 */
	public int computeFitnessLevel() {
		int level = 0;
		if (hasSeriousMedicalProblems()) {
			return 0;
		}

		if (fatigue < 100 && stress < 10 && hunger < 100 && thirst < 50 && kJoules > 12000)
        	level = 5;
		else if (fatigue < 250 && stress < 25 && hunger < 250 && thirst < 125 && kJoules > 10000)
        	level = 4;
        else if (fatigue < 500 && stress < 50 && hunger < 500 && thirst < 250 && kJoules > 8000)
        	level = 3;
        else if (fatigue < 800 && stress < 65 && hunger < 800 && thirst < 400 && kJoules > 6000)
        	level = 2;
        else if (fatigue < 1200 && stress < 80 && hunger < 1200 && thirst < 600 && kJoules > 4000)
        	level = 1;
        else if (fatigue < 1800 && stress < 95 && hunger < 1800 && thirst < 900 && kJoules > 2000)
        	level = 0;

        return level;
	}

	/**
	 * Gets a list of medication affecting the person.
	 *
	 * @return list of medication.
	 */
	public List<Medication> getMedicationList() {
		return medicationList;
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

		return medicationList.stream()
						.anyMatch(m -> m.getName().equals(medicationName));
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
	 * Gets the health risk probability map.
	 * 
	 * @return
	 */
	public Map<HealthRiskType, Double> getHealthRisks() {
		return healthRisks;
	}
	
	
	/**
	 * Gets the oxygen consumption rate per Sol.
	 *
	 * @return oxygen consumed (kg/sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getOxygenConsumptionRate() {
		return o2Consumption;
	}

	/**
	 * Gets the water consumption rate per Sol.
	 *
	 * @return water consumed (kg/sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getWaterConsumptionRate() {
		return h20Consumption;
	}

	public double getWaterConsumedPerServing() {
		return waterConsumedPerServing;
	}

	/**
	 * Gets the food consumption rate per Sol.
	 *
	 * @return food consumed (kg/sol)
	 * @throws Exception if error in configuration.
	 */
	public static double getFoodConsumptionRate() {
		return foodConsumption;
	}


	public RadiationExposure getRadiationExposure() {
		return radiation;
	}


	public double getBodyMassDeviation() {
		return bodyMassDeviation;
	}

	public double getBodyMassIndex() {
		return bmi;
	}
	
	public boolean isStressedOut() {
		return isStressedOut;
	}

	public boolean isRadiationPoisoned() {
		return isRadiationPoisoned;
	}
	
	/**
	 * Tracks the exercise.
	 * 
	 * @param time
	 */
	public void trackExercise(double time) {
		// Regulates hormones
		circadian.exercise(time);
		// Improves musculoskeletal systems
		muscularHypertrophy(time);
		// Record the sleep time [in millisols]
		circadian.recordExercise(time);
	}
	
	/**
	 * Represents an increase in muscle mass resulting in 
	 * an enlargement of skeletal muscle cells.
	 * 
	 * @param time
	 */
	public void muscularHypertrophy(double time) {
		// Tether toward attributeCompositeScore
		double factor = (1 + attributeCompositeScore / 4) * .001 * time;
		musclePainTolerance += factor; // musculoskeletal pain tolerance
		muscleHealth += factor; // musculoskeletal health
		// Decrease in soreness
		muscleSoreness -= factor; // musculoskeletal soreness
		if (musclePainTolerance > 100)
			musclePainTolerance = 100;
		if (muscleHealth > 100)
			muscleHealth = 100;
		if (muscleSoreness < 0)
			muscleSoreness = 0;
		// Increase thirst
		increaseThirst(time/5); 
	}
	
	/**
	 * Represents the deterioration of musculoskeletal systems.
	 * 
	 * @param time
	 */
	public void muscularAtrophy(double time) {
		// Tether toward attributeCompositeScore
		double factor = (1 - attributeCompositeScore / 4) * .001 * time;
		musclePainTolerance -= factor; // muscle pain
		muscleHealth -= factor; // muscle health
		// Increase in soreness
		muscleSoreness += factor; // muscle soreness
		
		if (muscleSoreness > 100)
			muscleSoreness = 100;
		if (muscleHealth < 0)
			muscleHealth = 0;
		if (musclePainTolerance < 0)
			musclePainTolerance = 0;
	}
	
	/**
	 * Gets the pain tolerance factor.
	 * 
	 * @return
	 */
	public double getPainToleranceFactor() {
		return (1 + musclePainTolerance/200);
	}
	
	/**
	 * Gets the pain soreness factor.
	 * 
	 * @return
	 */
	public double getPainSorenessFactor() {
		return (1 + muscleSoreness/200);
	}
	
	/**
	 * Gets the muscle health factor.
	 * 
	 * @return
	 */
	public double getMusleHealthFactor() {
		return (1 + muscleHealth/200);
	}
	
	/**
	 * Reduces the pain soreness.
	 * 
	 * @return
	 */
	public void reduceMuscleSoreness(double time) {
		muscleSoreness -= .001 * time;
	}
	
	/**
	 * Increases the pain soreness.
	 * 
	 * @return
	 */
	public void increaseMuscleSoreness(double time) {
		muscleSoreness += .001 * time;
	}
	
	/**
	 * Increases the pain tolerance.
	 * 
	 * @return
	 */
	public void increasePainTolerance(double time) {
		musclePainTolerance += .001 * time;
	}
	
	/**
	 * Reduces the muscle health.
	 * 
	 * @return
	 */
	public void reduceMuscleHealth(double time) {
		muscleHealth -= .001 * time;
	}
	
	/**
	 * Improves the muscle health.
	 * 
	 * @return
	 */
	public void improveMuscleHealth(double time) {
		muscleHealth += .001 * time;
	}
	
	/**
	 * Checks if it passes the hunger x2 threshold
	 *
	 * @return
	 */
	public boolean isDoubleHungry() {
		return hunger > HUNGER_THRESHOLD * 2 || kJoules < ENERGY_THRESHOLD * 2;
	}
	

	/**
	 * Checks if it passes the hunger threshold
	 *
	 * @return
	 */
	public boolean isHungry() {
		return hunger > HUNGER_THRESHOLD || kJoules < ENERGY_THRESHOLD;
	}

	/**
	 * Checks if it passes the thirst x2 threshold
	 *
	 * @return
	 */
	public boolean isDoubleThirsty() {
		return thirst > THIRST_THRESHOLD * 2;
	}
	
	/**
	 * Checks if it passes the thirst threshold
	 *
	 * @return
	 */
	public boolean isThirsty() {
		return thirst > THIRST_THRESHOLD;
	}

	/**
	 * Checks if it passes the fatigue threshold
	 *
	 * @return
	 */
	public boolean isSleepy() {
		return fatigue > FATIGUE_THRESHOLD;
	}

	/**
	 * Checks if it's above the stress threshold.
	 *
	 * @return
	 */
	public boolean isStressed() {
		// Research findings indicate that individuals 
		// with depression often exhibit lower pain tolerance
		return stress > STRESS_THRESHOLD * getPainToleranceFactor();
	}

	public double getStrengthMod() {
		return (endurance * .6 - strength * .4) / 100D;
	}

	public void reduceRemainingPrebreathingTime(double time) {
		remainingPrebreathingTime -= time;
	}

	public boolean isDonePrebreathing() {
        return remainingPrebreathingTime <= 0;
    }

	public boolean isAtLeast3QuartersDonePrebreathing() {
        return remainingPrebreathingTime <= .25 * STANDARD_PREBREATHING_TIME;
	}
	
	public boolean isAtLeastHalfDonePrebreathing() {
        return remainingPrebreathingTime <= .5 * STANDARD_PREBREATHING_TIME;
    }
	
	public boolean isAtLeastAQuarterDonePrebreathing() {
        return remainingPrebreathingTime <= .75 * STANDARD_PREBREATHING_TIME;
	}
	
	public void resetRemainingPrebreathingTime() {
		remainingPrebreathingTime = STANDARD_PREBREATHING_TIME + RandomUtil.getRandomInt(-5, 5);
	}

	
	/**
	 * Has this person eaten the amount of food that exceed the max daily limits ? 
	 * 
	 * @return
	 */
	public boolean eatTooMuch() {
		double foodEaten = 0;
		Double f = consumption.getDataPoint(0);
		if (f != null)
			foodEaten = f.doubleValue();
		
		double mealEaten = 0;
		Double m = consumption.getDataPoint(1);
		if (m != null)
			mealEaten = m.doubleValue();
		
		double dessertEaten = 0;
		Double d = consumption.getDataPoint(2);
		if (d != null)
			dessertEaten = d.doubleValue();
		return (foodEaten + mealEaten + dessertEaten >= foodConsumption * 1.5
				&& hunger < HUNGER_THRESHOLD);
	}
	
	/**
	 * Has this person drank the amount of water that exceed the max daily limits ? 
	 * 
	 * @return
	 */
	public boolean drinkEnoughWater() {
		Double w = consumption.getDataPoint(3);
		return ((w != null) && (w.doubleValue() >= h20Consumption * 1.5
					&& thirst < THIRST_THRESHOLD));
	}
	
	/**
	 * Records the amount of food/water consumption in kg.
	 * Types :
	 * 0 = preserved food
	 * 1 = meal
	 * 2 = dessert
	 * 3 = water
	 * 
	 * @param amount in kg
	 * @param type
	 */
	public void recordFoodConsumption(double amount, int type) {
		consumption.increaseDataPoint(type, amount);
	}
	
	/**
	 * Returns the consumption history.
	 * 
	 * @return
	 */
	public Map<Integer, Map<Integer, Double>> getConsumptionHistory() {
		return consumption.getHistory();
	}
	
	/**
	 * Gets the daily average water usage of the last x sols Not: most weight on
	 * yesterday's usage. Least weight on usage from x sols ago
	 *
	 * @param type the id of the resource
	 * @return the amount of resource consumed in a day
	 */
	public double getDailyFoodUsage(int type) {
		return consumption.getDailyAverage(type);
	}
	
	/**
	 * Adds the amount of gas consumed.
	 *
	 * @param type
	 * @param amount
	 */
	public void addGasConsumed(int type, double amount) {
		if (type == ResourceUtil.OXYGEN_ID)
			consumption.increaseDataPoint(4, amount);
	}
	

	public double getMuscleSoreness() {
		return muscleSoreness;
	}

    public double getMusclePainTolerance() {
        return musclePainTolerance;
    }

    public double getMuscleHealth() {
    	return muscleHealth;
    }
    
	/**
	 * Gets the history of cured problems.
	 * 
	 * @return
	 */
	public List<CuredProblem> getHealthHistory() {
		return history;
	}

	/**
	 * Initializes that static instances.
	 * 
	 * @param s
	 * @param c0
	 * @param c1
	 * @param m
	 * @param e 
	 */
	public static void initializeInstances(MasterClock c0, MedicalManager m,
											PersonConfig pc, HistoricalEventManager e) {
		medicalManager = m;
		personConfig = pc;
		master = c0;

		h20Consumption = personConfig.getWaterConsumptionRate(); // 3 kg per sol
		o2Consumption = personConfig.getNominalO2ConsumptionRate();

		minAirPressure = personConfig.getMinAirPressure();
		minTemperature = personConfig.getMinTemperature();
		maxTemperature = personConfig.getMaxTemperature();
		foodConsumption = personConfig.getFoodConsumptionRate();

		RadiationExposure.initializeInstances(c0);
		HealthProblem.initializeInstances(m, e);

	}

	public void reinit() {
		circadian = person.getCircadianClock();
		naturalAttributeManager = person.getNaturalAttributeManager();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		deathDetails = null;
		problems = null;
		mostSeriousProblem = null;
		person = null;
		radiation = null;
		circadian = null;
		starved = null;
		dehydrated = null;
		medicationList = null;
	}

}
