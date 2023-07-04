/*
 * Mars Simulation Project
 * RadiationExposure.java
 * @date 2022-06-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.hazard.HazardEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.RadiationStatus;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * <p> Curiosity's Radiation Assessment Detector (RAD)
 * Mars rover Curiosity received an average dose of 300 milli-sieverts (mSv) 
 * over the 180-day journey. 300 mSv is equivalent to 24 CAT scans, or more 
 * than 15x the annual radiation limit for a worker in a nuclear power plant.
 *
 * <br> 1. https://www.space.com/24731-mars-radiation-curiosity-rover.html 2.
 * <br> 2. http://www.boulder.swri.edu/~hassler/rad/ 3.
 * <br> 3. http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
 * <br> 
 * <p> Notes on unit conversion: 
 * <br> 1000 millirem = 1 rem 
 * <br> 1 Sievert (Sv) is 100 rem 
 * <br> 1000 mSv = 1 Sv 
 * <br> 500 mSv = 50 rem 
 * <br> 10 mSv = 1 rem
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
 * <p> A prompt dose of up to 75 rem result in no apparent health effects. Between
 * 75 and 200 rem, radiation sickness results (symptoms are vomiting, fatigue,
 * loss of appetite. Almost everyone recovers within a few weeks. At 300 rem,
 * some fatalities start to appear, rising to 50% at 450 rem and 80% at 600 rem
 * Almost no one survives dose of 1,000 rem or more.
 *
 * <p> Living at sea level receives an annual dose of 150 millirem (or .15 rem),
 * versus 300 millirem (or .3 rem) on top of a mountain.
 *
 * <p> According to one study, for every 100 rem received, the likelihood of fatal
 * cancer is 1.8% within 30 years.
 *
 * <p> If a Mars Direct mission uses Conjunction trajectory, the estimated round
 * trip mission radiation dose varies between 41 and 62 rem, depending upon
 * whether the Sun is at solar min or solar max phase of its 11-year cycle.
 *
 * <p> If an astronaut gets a typical dose of 50 rem over the course of a 2.5 years
 * Mars mission, the chance of getting a fatal cancer due to that exposure is
 * 50/100 * 1.81% = .905%.
 * 
 * <p> Probability of getting hit by GCG/SPE radiation within an interval of 100
 * milliSol during an EVA [in % per earth hour roughly] RAD surface radiation
 * data show an average GCR dose equivalent rate of 0.67 millisieverts per day
 * from August 2012 to June 2013 on the Martian surface. .67 mSv per day * 180
 * sols = 120.6 mSv
 *
 * <p> In comparison, RAD data show an average GCR dose equivalent rate of 1.8
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
	 * Class models a dose of radiation over a time range
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
		 * Compare this dose hsitory to another and see if any values are higher
		 */
		boolean higherThan(DoseHistory limit) {
			// Only check the 30 day value currently
			return thirtyDay > limit.thirtyDay;
		}
	};

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
	/** THe Baseline radiation dose per sol [in mSv] arbitrary. */
	public static final double BASELINE_RAD_PER_SOL = .1;

	/** The average GCR dose equivalent rate [mSv] on the Mars, based on DAT. Note: based on Ref_A's DAT data, the average GCR dose equivalent rate on the Mars surface is 0.64 ± 0.12 mSv/day. The dose equivalent is 50 μSv. */
	public static final double GCR_RAD_PER_SOL = .64;
	/** THe GCR dose modifier[in mSv], based on DAT value. */
	public static final double GCR_RAD_SWING = .12;

	/**
	 * The SEP dose [mSv] per sol.
	 * <br>Note : frequency and intensity of SEP events is sporadic and difficult to predict.
	 * <br>Its flux varies by several orders of magnitude and are typically dominated by protons.
	 */
	public static final double SEP_RAD_PER_SOL = .21;
	/** 
	 * The SEP dose modifier [mSv], assuming 3 orders of magnitude (arbitrary) 
	 * <br>	The orders of magnitude are written in powers of 10.
	 * <br> e.g. the order of magnitude of 1500 is 3, since 1500 may be written as 1.5 × 10^3.
	 * <br> e.g. the order of magnitude of 1000 is 3, since 1500 may be written as 1.0 × 10^3.
	 */
	public static final double SEP_SWING_FACTOR = 1000;
	
	// Additional notes :
	// Ref_A assumes absorbed dose of ~150 mGy/year at the Martian surface.
	// Pavlov et al. assumed an absorbed dose of 50 ±5 mGy/year.
	// The actual absorbed dose measured by the RAD is 76 mGy/yr at the surface.

	/**
	 * Career whole-body effective dose limits, per NCRP guidelines. 
	 * <br> Note : it should vary with age and differs in male and female
	 */
	private static final int WHOLE_BODY_DOSE = 1000; 

	private static final String EXPOSED_TO = "Exposed to ";
	private static final String DOSE_OF_RAD = " mSv dose of radiation";
	private static final String EVA_OPERATION = " during an EVA operation.";

	private int solCache = 1, counter30 = 1, counter360 = 1;

	private boolean isSick;

	/** Dose equivalent limits in mSv (milliSieverts). */
	private static final DoseHistory[] DOSE_LIMITS = {
										new DoseHistory(250, 1000, 1500), 
										new DoseHistory(500, 2000, 3000), 
										new DoseHistory(WHOLE_BODY_DOSE, 4000, 6000) };

	/** Randomize dose at the start of the sim when a settler arrives on Mars. */
	private DoseHistory[] dose;

	private Map<Integer,Radiation> eventMap = new ConcurrentHashMap<>();

	private Person person;

	private static MasterClock masterClock;


	public RadiationExposure(Person person) {
		this.person = person;
		dose = new DoseHistory[BodyRegionType.values().length];
		DoseHistory bfoDose = new DoseHistory(rand(10), rand(30), rand(40));
		DoseHistory ocularDose = new DoseHistory(bfoDose.getThirtyDay() + rand(15),
												 bfoDose.getAnnual() + rand(45),
												 bfoDose.getCareer() + rand(70));
		DoseHistory skinDose = new DoseHistory(ocularDose.getThirtyDay() + rand(25),
												ocularDose.getAnnual() + rand(60),
												ocularDose.getCareer() + rand(100));
		dose[BodyRegionType.BFO.ordinal()] = bfoDose;
		dose[BodyRegionType.OCULAR.ordinal()] = ocularDose;
		dose[BodyRegionType.SKIN.ordinal()] = skinDose;

	}

	/**
	 * Adds the dose of radiation exposure. Called by isRadiationDetected.
	 *
	 * @param bodyRegion
	 * @param amount
	 * @see checkForRadiation() in EVAOperation and WalkOutside
	 */
	private Radiation addDose(RadiationType radiationType, BodyRegionType bodyRegionType, double amount) {
		DoseHistory active = dose[bodyRegionType.ordinal()];
		
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
	 * Reduces the dose
	 *
	 * @bodyRegion
	 *
	 * @amount
	 */
	public void reduceDose(BodyRegionType bodyRegionType, double amount) {
		DoseHistory active = dose[bodyRegionType.ordinal()];

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
		if (msol % 17 == 0) {
			checkExposureLimit();
		}
		return true;
	}

	public boolean isSick() {
		return isSick;
	}

	/*
	 * Checks if the exposure exceeds the limit and reset counters
	 */
	private void checkExposureLimit() {

		// Compare if any element in a person's dose matrix exceeds the limit
		boolean exceeded = false;
		for(BodyRegionType type : BodyRegionType.values()) {
			DoseHistory active = dose[type.ordinal()];
			DoseHistory limit = DOSE_LIMITS[type.ordinal()];
			if (active.higherThan(limit)) {
				exceeded = true;
			}
		}
	
        isSick = exceeded;

		if (counter30 == 30) {
			carryOverDosage(false);
			counter30 = 0;
		}

		// TODO: convert to martian system. For now, use 360 sol for simplicity and
		// synchronization with the 30-day carryover
		if (counter360 == 360) {
			carryOverDosage(true);
			counter360 = 0;
		}

	}

	/*
	 * Recomputes the values in the radiation dosage chart
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
				dose[region.ordinal()].addToThirtyDay(-dosage);
			}
			else {
				// remove the recorded dosage from 361 sols ago
				dose[region.ordinal()].addToAnnual(-dosage);
			}
		}
	}

	public DoseHistory[] getDose() {
		return dose;
	}

	/**
	 * Check for radiation exposure of the person performing this EVA.
	 *
	 * @param time the amount of time on EVA (in millisols)
	 * @return true if radiation is detected
	 */
	public boolean isRadiationDetected(double time) {
		// Check every RADIATION_CHECK_FREQ (in millisols)
		if (masterClock.getClockPulse().isNewMSol() 
			&& masterClock.getMarsTime().getMillisolInt() % RadiationExposure.RADIATION_CHECK_FREQ == 0) {

			RadiationType radiationType = null;
			BodyRegionType bodyRegionType = null;
			
			double totalExposure = 0;
			double exposure = 0;
			double shield_factor = 0;

			double baseline = 0;
			double sep = 0;
			double gcr = 0;

			// Future: account for the effect of atmosphere pressure on radiation dosage as
			// shown by RAD data

			// Future: compute radiation if a person steps outside of a rover on a mission
			// somewhere on Mars
			
			if (person.isOutside()) {
				// Future: how to make radiation more consistent/less random by coordinates/locale
			
				Radiation rad = null;
				
				RadiationStatus exposed = person.getAssociatedSettlement().getExposed();

				if (exposed.isGCREvent())
					shield_factor = RandomUtil.getRandomDouble(1); // arbitrary
				// NOTE: SEP may shield off GCR as shown in Curiosity's RAD data
				// since GCR flux is modulated by solar activity.
				// It DECREASES during solar activity maximum and 
				// INCREASES during solar activity minimum
				else
					shield_factor = 1; // arbitrary

				int rand = RandomUtil.getRandomInt(10);
				if (rand == 0)
					bodyRegionType = BodyRegionType.OCULAR;
				else if (rand <= 3)
					bodyRegionType = BodyRegionType.BFO;
				else
					bodyRegionType = BodyRegionType.SKIN;
								
				double baselevel = 0;
				if (exposed.isSEPEvent()) {
					radiationType = RadiationType.SEP;

					baselevel = 0.0; 
					// highly unpredictable, somewhat arbitrary
					sep += (baselevel + RandomUtil.getRandomInt(-1, 1)
							* RandomUtil.getRandomDouble(SEP_RAD_PER_SOL * time / RADIATION_CHECK_FREQ)) 
							* RandomUtil.getRandomDouble(SEP_SWING_FACTOR);
					if (sep > 0) {
						rad = addDose(radiationType, bodyRegionType, exposure);
					}	
				}
				// for now, if SEP happens, ignore GCR and Baseline
				else if (exposed.isGCREvent()) {
					radiationType = RadiationType.GCR;
					baselevel = GCR_RAD_PER_SOL * time / 100D;
					// according
					// to
					// Curiosity
					// RAD's
					// data
					gcr += baselevel + RandomUtil.getRandomInt(-1, 1) * RandomUtil
							.getRandomDouble(shield_factor * GCR_RAD_SWING * time / RADIATION_CHECK_FREQ); 
					if (gcr > 0) {
						rad = addDose(radiationType, bodyRegionType, exposure);
					}	
				}
				// for now, if GCR happens, ignore Baseline
				else if (exposed.isBaselineEvent()) {
					radiationType = RadiationType.BASELINE;
					baselevel = BASELINE_RAD_PER_SOL * time / RADIATION_CHECK_FREQ;
					// arbitrary
					baseline += baselevel
							+ RandomUtil.getRandomInt(-1, 1) * RandomUtil.getRandomDouble(baselevel / 3D); 
					if (baseline > 0) {
						rad = addDose(radiationType, bodyRegionType, exposure);
					}	
				}
				else {
					return false;
				}

				exposure = sep + gcr + baseline;
				totalExposure += exposure;
				
				if (totalExposure > 0 && rad != null) {
					String str = EXPOSED_TO + rad.getRadiationType().getName()
							+ " and received " 
							+ Math.round(totalExposure * 10_000.0) / 10_000.0
							+ DOSE_OF_RAD;

					if (person.getVehicle() == null)
						// if a person steps outside of the vehicle
						logger.info(person, str + EVA_OPERATION);
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
		}

		return false;
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
	}


}
