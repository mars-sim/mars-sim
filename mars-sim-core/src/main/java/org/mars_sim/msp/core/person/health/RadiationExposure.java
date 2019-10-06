/**
 * Mars Simulation Project
 * RadiationExposure.java
 * @version 3.1.0 2017-10-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.hazard.HazardEvent;
import org.mars_sim.msp.core.person.BodyRegionType;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;

public class RadiationExposure implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RadiationExposure.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/*
	 * Curiosity's Radiation Assessment Detector (RAD). Note: Mars rover Curiosity
	 * received an average dose of 300 milli-sieverts (mSv) over the 180-day
	 * journey. 300 mSv is equivalent to 24 CAT scans, or more than 15x the annual
	 * radiation limit for a worker in a nuclear power plant.
	 *
	 * Ref 1. https://www.space.com/24731-mars-radiation-curiosity-rover.html 2.
	 * http://www.boulder.swri.edu/~hassler/rad/ 3.
	 * http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
	 *
	 * 1000 millirem = 1 rem 1 Sievert (Sv) is 100 rem 1000 mSv = 1 Sv 500 mSv = 50
	 * rem 10 mSv = 1 rem
	 *
	 * GRAY UNIT (Gy)
	 *
	 * Exposure from x-rays or gamma rays is measured in units of roentgens. For
	 * example: Total body exposure of 100 roentgens/rad or 1 Gray unit (Gy) causes
	 * radiation sickness. Total body exposure of 400 roentgens/rad (or 4 Gy) causes
	 * radiation sickness and death in half of the individuals who are exposed.
	 * Without medical treatment, nearly everyone who receives more than this amount
	 * of radiation will die within 30 days. 100,000 roentgens/rad (1,000 Gy) causes
	 * almost immediate unconsciousness and death within an hour.
	 *
	 *
	 * REM
	 *
	 * A prompt dose of up to 75 rem result in no apparent health effects. Between
	 * 75 and 200 rem, radiation sickness results (symptoms are vomiting, fatigue,
	 * loss of appetite. Almost everyone recovers within a few weeks. At 300 rem,
	 * some fatalities start to appear, rising to 50% at 450 rem and 80% at 600 rem
	 * Almost no one survives dose of 1,000 rem or more.
	 *
	 * Living at sea level receives an annual dose of 150 millirem (or .15 rem),
	 * versus 300 millirem (or .3 rem) on top of a mountain.
	 *
	 * According to one study, for every 100 rem received, the likelihood of fatal
	 * cancer is 1.8% within 30 years.
	 *
	 * If a Mars Direct mission uses Conjunction trajectory, the estimated round
	 * trip mission radiation dose varies between 41 and 62 rem, depending upon
	 * whether the Sun is at solar min or solar max phase of its 11-year cycle.
	 *
	 * If an astronaut gets a typical dose of 50 rem over the course of a 2.5 years
	 * Mars mission, the chance of getting a fatal cancer due to that exposure is
	 * 50/100 * 1.81% = .905%.
	 */

	/*
	 * Probability of getting hit by GCG/SPE radiation within an interval of 100
	 * milliSol during an EVA [in % per earth hour roughly] RAD surface radiation
	 * data show an average GCR dose equivalent rate of 0.67 millisieverts per day
	 * from August 2012 to June 2013 on the Martian surface. .67 mSv per day * 180
	 * sols = 120.6 mSv
	 *
	 * In comparison, RAD data show an average GCR dose equivalent rate of 1.8
	 * millisieverts per day on the journey to Mars
	 *
	 * References : Ref_A :
	 * http://www.michaeleisen.org/blog/wp-content/uploads/2013/12/Science-2013-
	 * Hassler-science.1244797.pdf Ref_B :
	 * http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-
	 * settlers-be-exposed-to
	 */

	// Compute once for each time interval of time in Settlement.java.

	// If we Assume the following 3 types of radiation below add up to 100%
	// BL = 72.5%, GCR = 25%, SEP = 2.5% : BL + GCR + SEP = 100%
	// TODO: vary the chance according to the solar cycle, day/night and other
	// factors
	// On MSL, SEPs is only 5% of GCRs, not like 10% (=25/2.5) here

	/** The time interval that a person checks for radiation exposure. */
	public static final int RADIATION_CHECK_FREQ = 50; // in millisols
	/** The chance modifier for SEP. Can be twice as much probability of occurrence (an arbitrary value for now). */
	public static final double SEP_CHANCE_SWING = 2D;
	/** The chance modifier for GCR. Can be 3x as much probability of occurrence (an arbitrary value for now). */
	public static final double GCR_CHANCE_SWING = 3D;

	// Baseline radiation is a combination of the solar wind and the secondary
	// radiation as a result of primary
	// radiation interacting with the surface of a planetary body

	// The solar wind is a stream of particles, mainly protons and electrons,
	// flowing from the sun's atmosphere at a
	// speed of about one million miles per hour. The magnetic field carried by the
	// solar wind as it flows past
	// Mars can generate an electric field,
	// https://mars.nasa.gov/news/nasa-mission-reveals-speed-of-solar-wind-stripping-martian-atmosphere

	// Research shows how solar wind and ultraviolet light strip gas from of the top
	// of the planet's atmosphere
	// MAVEN measurements indicate that the solar wind strips away gas at a rate of
	// about 100 grams (~ 1/4 lbs)
	// each second.

	// Without the huge magnetic bubble, called the magnetosphere, which deflects
	// the vast majority of these particles,
	// most of Mars is still subjected to the full force of the solar wind, except
	// in the southern
	// hemisphere at latitude from -5 to -75 and longitude from 150 to 210, where
	// vertical (radial) component of
	// magnetic fields poking out of the Martian crust. Red and blue areas are zones
	// where stronger-than-average
	// magnetic fields protect the planet from solar wind erosion.
	// Source :
	// https://science.nasa.gov/science-news/science-at-nasa/2001/ast31jan_1/
	/** 
	public static final double BASELINE_PERCENT = 72.5; // [in %] calculated

	/** Galactic cosmic rays (GCRs) events. Based on Ref_A's DAT data, ~25% of the GCR for the one day duration of the event. */
	public static final double GCR_PERCENT = 25; // [in %] based on DAT

	/** Percent of Solar energetic particles (SEPs) events [in %] (arbitrary). Note: it Includes Coronal Mass Ejection and Solar Flare. The astronauts should expect one SPE every 2 months on average and a total of3 or 4 during their entire trip, with each one usually lasting not more than a couple of days. Source :  http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-settlers-be-exposed-to. */
	public static final double SEP_PERCENT = 2.5; //
	/** THe Baseline radiation dose per sol [in mSv] arbitrary. */
	public static final double BASELINE_RAD_PER_SOL = .1; //

	/** The average GCR dose equivalent rate [mSv] on the Mars, based on DAT. Note: based on Ref_A's DAT data, the average GCR dose equivalent rate on the Mars surface is 0.64 ± 0.12 mSv/day. The dose equivalent is 50 μSv. */
	public static final double GCR_RAD_PER_SOL = .64;
	/** THe GCR dose modifier[in mSv], based on DAT value. */
	public static final double GCR_RAD_SWING = .12; // 

	/** 
	 * The SEP dose [mSv] per sol.
	 * Note : frequency and intensity of SEP events is sporadic and difficult to predict. 
	 * Its flux varies by several orders of magnitude and are typically dominated by protons. 
	 */
	public static final double SEP_RAD_PER_SOL = .21;
	/** The SEP dose modifier [mSv], assuming 3 orders of magnitude (arbitrary) */
	public static final double SEP_SWING_FACTOR = 1000; 
	// since orders of magnitude are written in powers of 10.
	// e.g. the order of magnitude of 1500 is 3, since 1500 may be written as 1.5 ×
	// 10^3.
	// e.g. the order of magnitude of 1000 is 3, since 1500 may be written as 1.0 ×
	// 10^3.

	// SPE onset times on the order of minutes to hours and durations of hours to
	// days.

	// Additional notes :
	// Ref_A assumes absorbed dose of ~150 mGy/year at the Martian surface.
	// Pavlov et al. assumed an absorbed dose of 50 ±5 mGy/year.
	// The actual absorbed dose measured by the RAD is 76 mGy/yr at the surface.

	// ROWS of the 2-D dose array. */
	private static final int THIRTY_DAY = 0;
	private static final int ANNUAL = 1;
	private static final int CAREER = 2;

	// COLUMNS of the 2-D dose array
	// Organ dose equivalent limits, per NCRP guidelines. */
	private static final int BFO = 0; // BFO = blood-forming organs
	private static final int OCULAR = 1;
	private static final int SKIN = 2;

	// Career whole-body effective dose limits, per NCRP guidelines. */
	private static final int WHOLE_BODY_DOSE = 1000; // TODO: it varies with age and differs in male and female

	private static final String WAS = " was ";
	private static final String EXPOSED_TO = "exposed to ";
	private static final String DOSE = " mSv dose of radiation";
	private static final String EVA_OPERATION = " during an EVA operation near ";

	private int solCache = 1, counter30 = 1, counter360 = 1;

	private int msolsCache;

	private boolean repeated;

	private boolean isSick;

	// <Radiation Shielding>
	// One material in development at NASA has the potential to do both jobs:
	// - Hydrogenated boron nitride nanotubes—known as hydrogenated BNNTs
	// They are tiny, nanotubes made of carbon, boron, and nitrogen, with
	// hydrogen interspersed throughout the empty spaces left in between the tubes.
	// Boron is also an excellent absorber secondary neutrons, making hydrogenated 
	// BNNTs an ideal shielding material.
	// Source :
	// https://www.nasa.gov/feature/goddard/real-martians-how-to-protect-astronauts-from-space-radiation-on-mars

	/** Dose equivalent limits in mSv (milliSieverts). */
	private int[][] DOSE_LIMITS = { { 250, 1000, 1500 }, { 500, 2000, 3000 }, { WHOLE_BODY_DOSE, 4000, 6000 } };

	/** Randomize dose at the start of the sim when a settler arrives on Mars. */
	private double[][] dose;

	// private List<RadiationEvent> eventList = new CopyOnWriteArrayList<>();
	private Map<RadiationEvent, Integer> eventMap = new ConcurrentHashMap<>();

	private Person person;
	
	private static MarsClock marsClock;
	private static MasterClock masterClock;

	static {
//		if (Simulation.instance().getMasterClock() != null) { // for passing maven test
//			masterClock = Simulation.instance().getMasterClock();
//			marsClock = masterClock.getMarsClock();
//		}
	}
	
	public RadiationExposure(PhysicalCondition condition) {
		this.person = condition.getPerson();
		// this.condition = condition;
		dose = new double[3][3];
		
		if (Simulation.instance().getMasterClock() != null) { // for passing maven test
			masterClock = Simulation.instance().getMasterClock();
			marsClock = masterClock.getMarsClock();
		}
	}

	public Map<RadiationEvent, Integer> getRadiationEventMap() {
		return eventMap;
	}

	/**
	 * Adds the dose of radiation exposure. Called by isRadiationDetected.
	 * 
	 * @param bodyRegion
	 * @param amount
	 * @see checkForRadiation() in EVAOperation and WalkOutside
	 */
	public RadiationEvent addDose(int bodyRegion, double amount) {
		// Since amount is cumulative, need to carry over
		dose[bodyRegion][THIRTY_DAY] = dose[bodyRegion][THIRTY_DAY] + amount;
		dose[bodyRegion][ANNUAL] = dose[bodyRegion][ANNUAL] + amount;
		dose[bodyRegion][CAREER] = dose[bodyRegion][CAREER] + amount;

		BodyRegionType region = null;

		if (bodyRegion == BFO)
			region = BodyRegionType.BFO;
		else if (bodyRegion == OCULAR)
			region = BodyRegionType.OCULAR;
		else if (bodyRegion == SKIN)
			region = BodyRegionType.SKIN;

		// if (marsClock == null)
		// marsClock = Simulation.instance().getMasterClock().getMarsClock();

		RadiationEvent event = new RadiationEvent(marsClock, region, Math.round(amount * 10000.0) / 10000.0);
		eventMap.put(event, solCache);

		return event;

	}

	/*
	 * Reduces the dose
	 * 
	 * @bodyRegion
	 * 
	 * @amount
	 */
	public void reduceDose(int bodyRegion, double amount) {

		// amount is cumulative
		dose[bodyRegion][THIRTY_DAY] = dose[bodyRegion][THIRTY_DAY] - amount;
		dose[bodyRegion][ANNUAL] = dose[bodyRegion][ANNUAL] - amount;
		dose[bodyRegion][CAREER] = dose[bodyRegion][CAREER] - amount;

		if (dose[bodyRegion][THIRTY_DAY] < 0)
			dose[bodyRegion][THIRTY_DAY] = 0;
		if (dose[bodyRegion][ANNUAL] < 0)
			dose[bodyRegion][ANNUAL] = 0;
		if (dose[bodyRegion][CAREER] < 0)
			dose[bodyRegion][CAREER] = 0;

	}

	/*
	 * Initialize the dose
	 */
	public void initializeWithRandomDose() {
		for (int y = 0; y < 3; y++) {
			if (y == THIRTY_DAY) {
				dose[BFO][THIRTY_DAY] = rand(10);
				dose[OCULAR][THIRTY_DAY] = dose[BFO][THIRTY_DAY] + rand(15);
				dose[SKIN][THIRTY_DAY] = dose[OCULAR][THIRTY_DAY] + rand(25);
			} else if (y == ANNUAL) {
				dose[BFO][ANNUAL] = rand(30);
				dose[OCULAR][ANNUAL] = dose[BFO][ANNUAL] + rand(60);
				dose[SKIN][ANNUAL] = dose[OCULAR][ANNUAL] + rand(80);
			} else if (y == CAREER) {
				dose[BFO][CAREER] = rand(40);
				dose[OCULAR][CAREER] = dose[BFO][CAREER] + rand(70);
				dose[SKIN][CAREER] = dose[OCULAR][CAREER] + rand(100);
			}
		}

	}

	public int rand(int num) {
		return RandomUtil.getRandomInt(num);
	}

	public void timePassing(double time) {

		// check for the passing of each day
		int solElapsed = marsClock.getMissionSol();
		if (solElapsed != solCache) {
			solCache = solElapsed;
			counter30++;
			counter360++;
			// set the boolean
			// isExposureChecked = false;
		}

		// if (!isExposureChecked && marsClock.getMillisol() > 100 &&
		// marsClock.getMillisol() < 110) {
		// check on the effect of the exposure once a day at between 100 & 110 millisols
		// Note: at fastest simulation speed, it can skip as much as ~5 millisols

		int msol = marsClock.getMillisolInt();// (int)(marsClock.getMillisol() * masterClock.getTimeRatio());
		if (msol % 17 == 0) {
			checkExposureLimit();
			// reset the boolean
			// isExposureChecked = true;
		}

	}

	public boolean isSick() {
		return isSick;
	}

	/*
	 * Checks if the exposure exceeds the limit and reset counters
	 */
	public void checkExposureLimit() {

		// Compare if any element in a person's dose matrix exceeds the limit
		boolean exceeded = false;
		// int interval = -1;
		// int bodyRegion = -1;
		for (int x = 0; x < 3; x++) {
			// Set y < 2 to ignore the annual limit and the career limit for now
			for (int y = 0; y < 1; y++) {
				final double dosage = dose[x][y];
				final int limit = DOSE_LIMITS[x][y];
				if (dosage > limit) {
					// interval = x;
					// bodyRegion = y;
					exceeded = true;
					break;
				}
			}
		}

		if (exceeded)
			isSick = true;
		else
			isSick = false;

		// Note: exposure to a dose of 1 Sv is associated with a five percent increase
		// in fatal cancer risk.
		// TODO if sick is true, call methods in HealthProblem...
		// if (sick) {
		// Complaint radiationScikness = ;
		// condition.addMedicalComplaint(complaint);
		// condition.getPerson().fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
		// }

		if (counter30 == 30) {
			carryOverDosage(THIRTY_DAY);
			counter30 = 0;
		}

		// TODO: convert to martian system. For now, use 360 sol for simplicity and
		// synchronization with the 30-day carryover
		if (counter360 == 360) {
			carryOverDosage(ANNUAL);
			counter360 = 0;
		}

	}

	/*
	 * Recomputes the values in the radiation dosage chart
	 * 
	 * @param type of interval
	 */
	public void carryOverDosage(int interval) {

		double dosage = 0;
		BodyRegionType region = null;

		Iterator<Map.Entry<RadiationEvent, Integer>> entries = eventMap.entrySet().iterator();

		while (entries.hasNext()) {
			Map.Entry<RadiationEvent, Integer> entry = entries.next();
			RadiationEvent key = entry.getKey();
			Integer value = entry.getValue();

			if (solCache - (int) value == interval + 1) {
				dosage = key.getAmount();
				region = key.getBodyRegion();

				int type = 0;

				if (region == BodyRegionType.BFO)
					type = 0;
				else if (region == BodyRegionType.OCULAR)
					type = 1;
				else if (region == BodyRegionType.SKIN)
					type = 2;

				if (interval == 0) {
					// remove the recorded dosage from 31 sols ago
					dose[type][THIRTY_DAY] = dose[type][THIRTY_DAY] - dosage;
					// dose[type][ANNUAL] = dose[type][ANNUAL] + dosage;
				} else if (interval == 1) {
					// remove the recorded dosage from 361 sols ago
					dose[type][ANNUAL] = dose[type][ANNUAL] - dosage;
					// dose[type][CAREER] = dose[type][CAREER] + dosage;
				}
			}
		}
	}

	public double[][] getDose() {
		return dose;
	}

	/**
	 * Check for radiation exposure of the person performing this EVA.
	 * 
	 * @param time the amount of time on EVA (in millisols)
	 * @return true if radiation is detected
	 */
	public boolean isRadiationDetected(double time) {

		int msols = marsClock.getMillisolInt();

		if (msolsCache == msols)
			repeated = true;

		msolsCache = msols;

		// Check every RADIATION_CHECK_FREQ (in millisols)
		if (!repeated && msols % RadiationExposure.RADIATION_CHECK_FREQ == 0) {
			// Use repeated to avoid calculating the exposure over and over when the time
			// ratio is low
			// and millisols iterates very slowly and gives the same value.
			repeated = true;

			double totalExposure = 0;
			double exposure = 0;
			double shield_factor = 0;

			double baseline = 0;
			double sep = 0;
			double gcr = 0;

			// TODO: account for the effect of atmosphere pressure on radiation dosage as
			// shown by RAD data

			// TODO: compute radiation if a person steps outside of a rover on a mission
			// somewhere on Mars
			boolean[] exposed = null;

			if (person.isOutside())
				// if a person is outside
				// TODO: how to make radiation more consistent/less random by coordinates/locale
				// ?
				exposed = person.getAssociatedSettlement().getExposed();

			if (exposed[1])
				shield_factor = RandomUtil.getRandomDouble(1); // arbitrary
			// NOTE: SPE may shield off GCR as shown in Curiosity's RAD data
			// since GCR flux is modulated by solar activity. It DECREASES during solar
			// activity maximum
			// and INCREASES during solar activity minimum
			else
				shield_factor = 1; // arbitrary

			List<RadiationEvent> eventMap = new ArrayList<>();
			// Compute whether a baseline, GCR, or SEP event has occurred
			for (int i = 0; i < 3; i++) {
				if (exposed[i]) {
					// each body region receive a random max dosage
					for (int j = 0; j < 3; j++) {
						double baselevel = 0;
						if (exposed[2]) {
							baselevel = 0.0; // somewhat arbitrary
							sep += (baselevel + RandomUtil.getRandomInt(-1, 1)
									* RandomUtil.getRandomDouble(SEP_RAD_PER_SOL * time / RADIATION_CHECK_FREQ)) // highly
																													// unpredictable,
																													// somewhat
																													// arbitrary
									* RandomUtil.getRandomDouble(SEP_SWING_FACTOR);
						}
						// for now, if SEP happens, ignore GCR and Baseline
						else if (exposed[1]) {
							baselevel = GCR_RAD_PER_SOL * time / 100D;
							gcr += baselevel + RandomUtil.getRandomInt(-1, 1) * RandomUtil
									.getRandomDouble(shield_factor * GCR_RAD_SWING * time / RADIATION_CHECK_FREQ); // according
																													// to
																													// Curiosity
																													// RAD's
																													// data

						}
						// for now, if GCR happens, ignore Baseline
						else if (exposed[0]) {
							baselevel = BASELINE_RAD_PER_SOL * time / RADIATION_CHECK_FREQ;
							baseline += baselevel
									+ RandomUtil.getRandomInt(-1, 1) * RandomUtil.getRandomDouble(baselevel / 3D); // arbitrary
						}

						exposure = sep + gcr + baseline;
						RadiationEvent event = addDose(j, exposure);
						eventMap.add(event);

						totalExposure += exposure;
					}
				}
			}

			if (totalExposure > 0) {
				// String loc = "Outside";
				String coord = person.getCoordinates().getFormattedString();
				String str = person.getName() + WAS + EXPOSED_TO + Math.round(totalExposure * 10000.0) / 10000.0;

				if (person.getVehicle() == null)
					// if a person steps outside of the vehicle
					LogConsolidated.log(logger, Level.INFO, 0, sourceName,
							"[" + coord + "] " + str + DOSE + EVA_OPERATION + coord, null);
				else {
					String activity = "";
					if (person.getMind().getMission() != null)
						activity = person.getMind().getMission().getName();
					else
						activity = person.getTaskDescription();
					LogConsolidated.log(logger, Level.INFO, 0, sourceName,
							"[" + coord + "] " + str + DOSE + " while " + activity, null);
				}

				HistoricalEvent hEvent = new HazardEvent(EventType.HAZARD_RADIATION_EXPOSURE, 
						eventMap,
						"Dose of " + Math.round(totalExposure * 10000.0) / 10000.0 + " mSv",
						person.getTaskDescription(), 
						person.getName(), person.getLocationTag().getImmediateLocation(),
						person.getLocationTag().getLocale(),
						person.getAssociatedSettlement().getName()
						);
				Simulation.instance().getEventManager().registerNewEvent(hEvent);

				person.fireUnitUpdate(UnitEventType.RADIATION_EVENT);

				return true;

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
	public static void initializeInstances(MasterClock c0, MarsClock c1) {
		masterClock = c0;
		marsClock = c1;
	}
	
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
	}


}
