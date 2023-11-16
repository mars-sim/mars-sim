/*
 * Mars Simulation Project
 * RadiationExposure.java
 * @date 2023-11-05
 * @author Manny Kung
 */

package com.mars_sim.core.person.health;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.hazard.HazardEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.structure.RadiationStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.util.RandomUtil;

/**
 * <p> Curiosity's Radiation Assessment Detector (RAD)
 * Mars rover Curiosity received an average cumulativeDoses of 300 milli-sieverts (mSv) 
 * over the 180-day journey. 300 mSv is equivalent to 24 CAT scans, or more 
 * than 15x the annual radiation limit for a worker in a nuclear power plant.
 *
 * <br> 1. https://www.space.com/24731-mars-radiation-curiosity-rover.html 2.
 * <br> 2. http://www.boulder.swri.edu/~hassler/rad/ 3.
 * <br> 3. http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
 * <br> 
 * <p> Notes on unit conversion: 
 * <br> 1000 millirem = 1 rem 
 * <br> 1 Sievert (Sv) = 1000 mSv = 100 rem 
 * <br> .5 Sv = 500 mSv = 50 rem 
 * <br> .05 Sv = 50 mSv = 5 rem
 * <br> .01 Sv = 10 mSv = 1 rem
 *
 * <br> <p> GRAY UNIT (Gy)
 * <p> Exposure from x-rays or gamma rays is measured in units of roentgens. For
 * example: Total body exposure of 100 roentgens/rad or 1 Gray unit (Gy) causes
 * radiation sickness. Total body exposure of 400 roentgens/rad (or 4 Gy) causes
 * radiation sickness and death in half of the individuals who are exposed.
 * Without medical treatment, nearly everyone who receives more than this amount
 * of radiation will die within 30 days. 100,000 roentgens/rad (1,000 Gy) causes
 * almost immediate unconsciousness and death within an hour.
 *
 * <br> <p> REM
 * <p> A prompt cumulativeDoses of up to 75 rem result in no apparent health effects. Between
 * 75 and 200 rem, radiation sickness results (symptoms are vomiting, fatigue,
 * loss of appetite. Almost everyone recovers within a few weeks. At 300 rem,
 * some fatalities start to appear, rising to 50% at 450 rem and 80% at 600 rem
 * Almost no one survives cumulativeDoses of 1,000 rem or more.
 *
 * <p> Living at sea level receives an annual cumulativeDoses of 150 millirem (or .15 rem),
 * versus 300 millirem (or .3 rem) on top of a mountain.
 *
 * <p> According to one study, for every 100 rem received, the likelihood of fatal
 * cancer is 1.8% within 30 years.
 *
 * <p> If a Mars Direct mission uses Conjunction trajectory, the estimated round
 * trip mission radiation cumulativeDoses varies between 41 and 62 rem, depending upon
 * whether the Sun is at solar min or solar max phase of its 11-year cycle.
 *
 * <p> If an astronaut gets a typical cumulativeDoses of 50 rem over the course of a 2.5 years
 * Mars mission, the chance of getting a fatal cancer due to that exposure is
 * 50/100 * 1.81% = .905%.
 * 
 * <p> Probability of getting hit by GCG/SPE radiation within an interval of 100
 * milliSol during an EVA [in % per earth hour roughly] RAD surface radiation
 * data show an average GCR cumulativeDoses equivalent rate of 0.67 millisieverts per day
 * from August 2012 to June 2013 on the Martian surface. .67 mSv per day * 180
 * sols = 120.6 mSv
 *
 * <p> In comparison, RAD data show an average GCR cumulativeDoses equivalent rate of 1.8
 * millisieverts per day on the journey to Mars
 *
 * <p> References ::
 * <br> 1. http://www.michaeleisen.org/blog/wp-content/uploads/2013/12/Science-2013-
 * Hassler-science.1244797.pdf Ref_B :
 * <br> 2. http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-
 * settlers-be-exposed-to
 * 
 * <p> The RadiationExposure class computes the effect of radiation exposure on a person.
 */
public class RadiationExposure implements Serializable, Temporal {

	/**
	 * Class models a cumulativeDoses of radiation over a time range
	 */
	public static class DoseHistory implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private double thirtyDay;
		private double annual;
		private double career;
		
		public DoseHistory(double thirtyDay, double annual, double career) {
			this.thirtyDay = thirtyDay;
			this.annual = annual;
			this.career = career;
		}
		
		public double getThirtyDay() {
			return thirtyDay;
		}

		public double getAnnual() {
			return annual;
		}

		public double getCareer() {
			return career;
		}

		void addToThirtyDay(double delta) {
			thirtyDay += delta;
			if (thirtyDay < 0) {
				thirtyDay = 0;
			}
		}

		void addToAnnual(double delta) {
			annual += delta;
			if (annual < 0) {
				annual = 0;
			}
		}

		void addToCareer(double delta) {
			career += delta;
			if (career < 0) {
				career = 0;
			}
		}

		/**
		 * Is the 30-day cumulative doses history higher than the set limit ?
		 */
		boolean thirtyDayHigherThan(DoseHistory limit) {
			// Only check the 30 day value currently
			return thirtyDay > limit.thirtyDay;
		}
		
		/**
		 * Is the annual cumulative doses history higher than the set limit ?
		 */
		boolean annualHigherThan(DoseHistory limit) {
			// Only check the 30 day value currently
			return annual > limit.annual;
		}
		
		/**
		 * Is the career cumulative doses history higher than the set limit ?
		 */
		boolean careerHigherThan(DoseHistory limit) {
			// Only check the 30 day value currently
			return career > limit.career;
		}
		
	};

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RadiationExposure.class.getName());

	/** The time interval that a person checks for radiation exposure. */
	public static final int RADIATION_CHECK_FREQ = 50; // in millisols
	/** The chance modifier for SEP. Can be twice as much probability of occurrence (an arbitrary value for now). */
	public static final double SEP_CHANCE_SWING = 2D;
	/** The chance modifier for GCR. Can be 3x as much probability of occurrence (an arbitrary value for now). */
	public static final double GCR_CHANCE_SWING = 3D;

	/** 
	 * The percentage of probability per millisol of having a Galactic cosmic rays (GCRs) event. 
	 * <br>Note: based on Ref_A's DAT data, there's a ~25% of the GCR for the one day duration of the event. 
	 */
	public static final double GCR_PERCENT = 25.0/1000; // [in %] based on DAT

	/** 
	 * The percentage of probability per millisol of having of Solar energetic particles (SEPs) event. 
	 * <br>Note: it Includes Coronal Mass Ejection and Solar Flare. The astronauts 
	 * <br>should expect one SPE every 2 months on average and a total of 3 or 4 
	 * <br>during their entire trip, with each one usually lasting not more than a
	 * <br>couple of days. 
	 * <br>Source : http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-settlers-be-exposed-to. 
	 */
	public static final double SEP_PERCENT = 2.5/1000; 
	/** THe Baseline radiation cumulativeDoses per sol [in mSv] arbitrary. */
	public static final double BASELINE_RAD_PER_SOL = .1;

	/** The average GCR cumulativeDoses equivalent rate [mSv] on the Mars, based on DAT. Note: based on Ref_A's DAT data, the average GCR cumulativeDoses equivalent rate on the Mars surface is 0.64 ± 0.12 mSv/day. The cumulativeDoses equivalent is 50 μSv. */
	public static final double GCR_RAD_PER_SOL = .64;
	/** THe GCR cumulativeDoses modifier[in mSv], based on DAT value. */
	public static final double GCR_RAD_SWING = .12;

	/**
	 * The SEP cumulativeDoses [mSv] per sol.
	 * <br>Note : frequency and intensity of SEP events is sporadic and difficult to predict.
	 * <br>Its flux varies by several orders of magnitude and are typically dominated by protons.
	 */
	public static final double SEP_RAD_PER_SOL = .21;
	/** 
	 * The SEP cumulativeDoses modifier [mSv], assuming 3 orders of magnitude (arbitrary) 
	 * <br>	The orders of magnitude are written in powers of 10.
	 * <br> e.g. the order of magnitude of 1500 is 3, since 1500 may be written as 1.5 × 10^3.
	 * <br> e.g. the order of magnitude of 1000 is 3, since 1500 may be written as 1.0 × 10^3.
	 */
	public static final double SEP_SWING_FACTOR = 1000;
	
	// Additional notes :
	// Ref_A assumes absorbed cumulativeDoses of ~150 mGy/year at the Martian surface.
	// Pavlov et al. assumed an absorbed cumulativeDoses of 50 ±5 mGy/year.
	// The actual absorbed cumulativeDoses measured by the RAD is 76 mGy/yr at the surface.

	/**
	 * Career whole-body effective cumulativeDoses limits, per NCRP guidelines. 
	 * <br> Note : it should vary with age and differs in male and female
	 */
	private static final int WHOLE_BODY_DOSE = 1000; 

	private static final String EXPOSED_TO = "Exposed to ";
	private static final String DOSE_OF = " mSv cumulativeDoses of ";
	private static final String RAD = "radiation ";
	private static final String EVA_OPERATION = " during an EVA operation.";

	private int solCache = 1, counter30 = 1, counter360 = 1;

	private boolean isSick;

	/** Dose equivalent limits in mSv (milliSieverts). */
	private DoseHistory[] doseLimits = {
										new DoseHistory(250, 1000, 1500), 
										new DoseHistory(500, 2000, 3000), 
										new DoseHistory(WHOLE_BODY_DOSE, 4000, 6000) };

	/** Randomize cumulativeDoses at the start of the sim when a settler arrives on Mars. */
	private DoseHistory[] cumulativeDoses;

	private Map<Integer, Radiation> eventMap = new ConcurrentHashMap<>();

	private Person person;

	private static MasterClock masterClock;
	private static UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param person
	 */
	public RadiationExposure(Person person) {
		this.person = person;
		cumulativeDoses = new DoseHistory[BodyRegionType.values().length];
		
		double bfo0 = rand(10);
		double bfo1 = rand(30) + bfo0;
		double bfo2 = rand(40) + bfo1;
		DoseHistory bfoDose = new DoseHistory(bfo0, bfo1, bfo2);
		
		double ocular0 = rand(5);
		double ocular1 = rand(15) + ocular0;
		double ocular2 = rand(20) + ocular1;			
		DoseHistory ocularDose = new DoseHistory(ocular0, ocular1, ocular2);
		
		double skin0 = rand(20);
		double skin1 = rand(50) + skin0;
		double skin2 = rand(70) + skin1;
		DoseHistory skinDose = new DoseHistory(skin0, skin1, skin2);
		
		cumulativeDoses[BodyRegionType.BFO.ordinal()] = bfoDose;
		cumulativeDoses[BodyRegionType.OCULAR.ordinal()] = ocularDose;
		cumulativeDoses[BodyRegionType.SKIN.ordinal()] = skinDose;
		
		// Vary the dose limit by person'a attributes
		int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		int endurance = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ENDURANCE);
		
		double rand = RandomUtil.getRandomDouble(strength + endurance - 100);
		
		DoseHistory bfoLimit = doseLimits[0];
		bfoLimit.addToThirtyDay(rand/10);
		bfoLimit.addToAnnual(rand/5);
		bfoLimit.addToCareer(rand/2.5);
		
		DoseHistory ocularLimit = doseLimits[1];
		ocularLimit.addToThirtyDay(rand/5);
		ocularLimit.addToAnnual(rand/2.5);
		ocularLimit.addToCareer(rand);

		DoseHistory skinLimit = doseLimits[2];
		skinLimit.addToThirtyDay(rand/2.5);
		skinLimit.addToAnnual(rand);
		skinLimit.addToCareer(rand*2.5);
	}

	/**
	 * Adds the cumulative doses of radiation exposure. Called by isRadiationDetected.
	 *
	 * @see checkForRadiation() in EVAOperation and WalkOutside
	 * @param radiationType
	 * @param bodyRegionType
	 * @param amount
	 * @return
	 */
	private Radiation addDose(RadiationType radiationType, BodyRegionType bodyRegionType, double amount) {
		DoseHistory active = cumulativeDoses[bodyRegionType.ordinal()];
		
		// Since amount is cumulative, need to carry over
		active.addToThirtyDay(amount);
		active.addToAnnual(amount);
		active.addToCareer(amount);

		// Record for later
		Radiation rad = new Radiation(radiationType, bodyRegionType, Math.round(amount * 10000.0) / 10000.0);
		eventMap.put(solCache, rad);

		return rad;
	}

	/*
	 * Reduces the cumulative doses.
	 *
	 * @param bodyRegionType
	 * @param amount
	 */
	public void reduceDose(BodyRegionType bodyRegionType, double amount) {
		DoseHistory active = cumulativeDoses[bodyRegionType.ordinal()];

		// amount is cumulative
		active.addToThirtyDay(-amount);
		active.addToAnnual(-amount);
		active.addToCareer(-amount);
	}


	private int rand(int num) {
		return RandomUtil.getRandomInt(num);
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {

		// check for the passing of each day
		if (pulse.isNewSol()) {
			counter30++;
			counter360++;
		}

		int msol = pulse.getMarsTime().getMillisolInt();
		if (msol % 20 == 0) {
			checkExposureLimit();
		}
		
		// Checks radiation
		// Note: if a person is outside, it's handled by EVAOperation's isRadiationDetected()
		if (!person.isOutside()) {
			isRadiationDetected(pulse.getElapsed());
		}
			
		return true;
	}

	public boolean isSick() {
		return isSick;
	}

	/*
	 * Checks if the exposure exceeds the limit and reset counters.
	 */
	private void checkExposureLimit() {

		// Compare if any element in a person's cumulativeDoses matrix exceeds the limit
		boolean exceeded = false;
		for (BodyRegionType type : BodyRegionType.values()) {
			
			DoseHistory active = cumulativeDoses[type.ordinal()];
			DoseHistory limit = doseLimits[type.ordinal()];
			
			if (active.thirtyDayHigherThan(limit)) {
				exceeded = true;
			}
			
			int rand = RandomUtil.getRandomInt(60);
			if (rand == 60 && active.annualHigherThan(limit)) {
				exceeded = true;
			}
			
			rand = RandomUtil.getRandomInt(100);
			if (rand == 100 && active.careerHigherThan(limit)) {
				exceeded = true;
			}
		}
	
        isSick = exceeded;

		if (counter30 == 30) {
			carryOverDosage(false);
			counter30 = 0;
		}

		// TODO: convert to Martian system. For now, use 360 sol for simplicity and
		// synchronization with the 30-day carryover
		if (counter360 == 360) {
			carryOverDosage(true);
			counter360 = 0;
		}
	}

	/*
	 * Recomputes the values in the radiation dosage chart.
	 *
	 * @param type of interval
	 */
	private void carryOverDosage(boolean annualCheck) {

		int targetSol = solCache - (annualCheck ? 360 : 30);
		Radiation previous = eventMap.get(targetSol);
		if (previous != null) {
			double dosage = previous.getAmount();
			BodyRegionType region = previous.getBodyRegion();

			if (!annualCheck) {
				// remove the recorded dosage from 31 sols ago
				cumulativeDoses[region.ordinal()].addToThirtyDay(-dosage);
			}
			else {
				// remove the recorded dosage from 361 sols ago
				cumulativeDoses[region.ordinal()].addToAnnual(-dosage);
			}
		}
	}

	public DoseHistory[] getDose() {
		return cumulativeDoses;
	}

	/**
	 * Checks for radiation exposure of the person performing this EVA.
	 *
	 * @param time the amount of time on EVA (in millisols)
	 * @return true if radiation is detected
	 */
	public boolean isRadiationDetected(double time) {
		// Check every RADIATION_CHECK_FREQ (in millisols)
		if (masterClock.getClockPulse().isNewMSol() 
			&& masterClock.getMarsTime().getMillisolInt() 
				% RadiationExposure.RADIATION_CHECK_FREQ == RadiationExposure.RADIATION_CHECK_FREQ - 1) {

			RadiationType radiationType = null;
			BodyRegionType bodyRegionType = null;
			
			double totalExposure = 0;
			// The strength of the radiation is reduced by shieldOff param (location dependent)
			double shieldOff = 1;

			double baseline = 0;
			double sep = 0;
			double gcr = 0;

			Radiation rad = null;			
			
			RadiationStatus exposed = null;

			if (person.isInSettlement()) {
				exposed = person.getSettlement().getExposed();
			}
			else if (person.isInVehicle()) {
				exposed = person.getVehicle().getExposed();
			}
			else if (person.isOutside()) {
				
				if (unitManager == null) {
					unitManager = Simulation.instance().getUnitManager();
				}
				
				// Check first if a person is in a settlement vicinity
				Settlement settlement = unitManager.findSettlement(person.getCoordinates());	
				if (settlement != null) {
					exposed = settlement.getExposed();
				}
				else {
					// Check if a person is in a vehicle vicinity
					Vehicle vehicle = CollectionUtils.findVehicle(person.getCoordinates());
					if (vehicle != null) {
						exposed = vehicle.getExposed();
					}
					else
						return false;
				}
			}
			else
				return false;
			
			// Note: for now, target at only one body region. In reality, it can hit all 3 at the same time.
			int rand = RandomUtil.getRandomInt(10);
			if (rand == 0) // 0
				bodyRegionType = BodyRegionType.OCULAR;
			else if (rand <= 3) // 1, 2, 3
				bodyRegionType = BodyRegionType.BFO;
			else // 4-10
				bodyRegionType = BodyRegionType.SKIN;
							
			double base = 0;
			
			// Future: account for the effect of atmosphere pressure on radiation dosage as
			// shown by RAD data

			// Future: compute radiation if a person steps outside of a rover on a mission
			// somewhere on Mars
			if (person.isInVehicle()) {
				shieldOff = .5;
				if (exposed.isSEPEvent()) {
					radiationType = RadiationType.SEP;
					
					shieldOff = RandomUtil.getRandomDouble(shieldOff); 
					base = SEP_RAD_PER_SOL; 
					
					// Note: the onset of SEP should be predictable and detectable,
					// making it easier to avoid
					double mean = RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time); 
					sep = RandomUtil.computeGaussianWithLimit(mean, mean / 10, mean / 100);
//					sep = Math.min(RandomUtil.getRandomRegressionInteger((int)SEP_SWING_FACTOR), 
//							RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time));
					if (sep > 0) {
						rad = addDose(radiationType, bodyRegionType, sep);
					}	
				}
			}
			
			else if (person.isInSettlement()) {
				shieldOff = .25;
				if (exposed.isSEPEvent()) {
					radiationType = RadiationType.SEP;
					
					shieldOff = RandomUtil.getRandomDouble(shieldOff);
					base = SEP_RAD_PER_SOL; 
					
					// Note: the onset of SEP should be predictable and detectable,
					// making it easier to avoid
					double mean = RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time); 
					sep = RandomUtil.computeGaussianWithLimit(mean, mean / 10, mean / 100);
//					sep = Math.min(RandomUtil.getRandomRegressionInteger((int)SEP_SWING_FACTOR), 
//							RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time));
					if (sep > 0) {
						rad = addDose(radiationType, bodyRegionType, sep);
					}	
				}
			}
						
			else if (person.isOutside()) {
				// Future: how to make radiation more consistent/less random by coordinates/locale
			
				if (exposed.isBaselineEvent()) {
					radiationType = RadiationType.BASELINE;
					base = BASELINE_RAD_PER_SOL * time;
					// arbitrary
					baseline 
					= base + RandomUtil.getRandomInt(-1, 1) * RandomUtil.getRandomDouble(base / 3D); 
					if (baseline > 0) {
						rad = addDose(radiationType, bodyRegionType, baseline);
					}	
				}
				
				// Note: for now, if SEP happens, ignore GCR and Baseline
				else if (exposed.isGCREvent()) {
					shieldOff = RandomUtil.getRandomDouble(shieldOff);
					// NOTE: SEP may shield off GCR as shown in Curiosity's RAD data
					// since GCR flux is modulated by solar activity.
					// It DECREASES during solar activity maximum and 
					// INCREASES during solar activity minimum
					
					radiationType = RadiationType.GCR;
					base = GCR_RAD_PER_SOL * time / 100D;
					// according
					// to
					// Curiosity
					// RAD's
					// data
					gcr = base + RandomUtil.getRandomInt(-1, 1) * RandomUtil
							.getRandomDouble(shieldOff * GCR_RAD_SWING * time); 
					if (gcr > 0) {
						rad = addDose(radiationType, bodyRegionType, gcr);
					}
				}
					
				else if (exposed.isSEPEvent()) {
					radiationType = RadiationType.SEP;
					
					shieldOff = RandomUtil.getRandomDouble(shieldOff); // arbitrary
					base = SEP_RAD_PER_SOL; 
					
					// Note: the onset of SEP should be predictable and detectable,
					// making it easier to avoid
					double mean = RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time); 
					sep = RandomUtil.computeGaussianWithLimit(mean, mean / 10, mean / 100);
//					sep = Math.min(RandomUtil.getRandomRegressionInteger((int)SEP_SWING_FACTOR), 
//							RandomUtil.getRandomDouble(shieldOff * SEP_SWING_FACTOR * base * time));
					if (sep > 0) {
						rad = addDose(radiationType, bodyRegionType, sep);
					}	
				}
			}
			
			totalExposure = sep + gcr + baseline;
			
			if (totalExposure > 0 && rad != null) {
				String str = EXPOSED_TO + Math.round(totalExposure * 10_000.0) / 10_000.0
						+ DOSE_OF + rad.getRadiationType().getName() 
						+ " on " + bodyRegionType.getName() + EVA_OPERATION;

				if (person.getVehicle() == null) {
					// if a person steps outside of the vehicle
					logger.info(person, str);
				}
				else {
					String activity = "";
					if (person.getMind().getMission() != null)
						activity = person.getMind().getMission().getName();
					else
						activity = person.getTaskDescription();
					logger.info(person, str + " while " + activity);
				}

				HistoricalEvent hEvent = new HazardEvent(EventType.HAZARD_RADIATION_EXPOSURE,
						rad,
						rad.toString(),
						person.getTaskDescription(),
						person.getName(), 
						person
						);
				Simulation.instance().getEventManager().registerNewEvent(hEvent);

				person.fireUnitUpdate(UnitEventType.RADIATION_EVENT);

				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the dose limits for this person.
	 * 
	 * @return
	 */
	public DoseHistory[] getDoseLimits() {
		return doseLimits;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 *
	 * @param {@link MasterClock}
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(MasterClock c0) {
		masterClock = c0;
	}


	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		cumulativeDoses = null;
		eventMap.clear();
		eventMap = null;
	}
}
