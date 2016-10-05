/**
 * Mars Simulation Project
 * RadiationExposure.java
 * @version 3.08 2015-04-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

public class RadiationExposure implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(RadiationExposure.class.getName());

    /* Curiosity's Radiation Assessment Detector (RAD). see http://www.boulder.swri.edu/~hassler/rad/
     * http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
     * Note: Mars rover Curiosity received an average dose of 300 mSv over the 180-day journey.
     * 300 milli-sieverts is equivalent to 24 CAT scans, or more than 15x
     * the annual radiation limit for a worker in a nuclear power plant.
     */

    /* Probability of getting hit by GCG/SPE radiation within an interval of 100 milliSol
     * during an EVA  [in % per earth hour roughly]
     * RAD surface radiation data show an average GCR dose equivalent rate of
     * 0.67 millisieverts per day from August 2012 to June 2013 on the Martian surface.
     * .67 mSv per day * 180 sols = 120.6 mSv
     *
     * In comparison, RAD data show an average GCR dose equivalent rate of
     * 1.8 millisieverts per day on the journey to Mars
     * 
     * References :
     * Ref_A : http://www.michaeleisen.org/blog/wp-content/uploads/2013/12/Science-2013-Hassler-science.1244797.pdf
     * Ref_B : http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-settlers-be-exposed-to
     */

    // Compute once for each time interval of time in Settlement.java.

	// If we Assume the following 3 types of radiation below add up to 100%
	// BL = 72.5%, GCR = 25%, SEP = 2.5% : BL + GCR + SEP = 100%
    // TODO: vary the chance according to the solar cycle, day/night and other factors
    // On MSL, SEPs is only 5% of GCRs, not like 10% (=25/2.5) here

	public static final int RADIATION_CHECK_FREQ = 50; // in millisols
	
	public static final double SEP_CHANCE_SWING = 1.5D; // from 1/2 to twice as much. probability of occurrence modifier (arbitrary)
	public static final double GCR_CHANCE_SWING = 2.7D; // from 1/3 to three times as much. probability of occurrence modifier (arbitrary)
	        
    // Baseline radiation
    public static final double BASELINE_PERCENT = 72.5; //[in %] calculated
    
    // Galactic cosmic rays (GCRs) events
    // Based on Ref_A's DAT data, ~25% of the GCR for the one day duration of the event.
    public static final double GCR_PERCENT = 25; //[in %] based on DAT
  
    // Solar energetic particles (SEPs) events
    public static final double SEP_PERCENT = 2.5; //[in %] arbitrary
    
    
    public static final double BASELINE_RAD_PER_SOL = .1; //  [in mSv] arbitrary
    
    // Based on Ref_A's DAT data, the average GCR dose equivalent rate on the Mars surface
    // is 0.64 ± 0.12 mSv/day. The dose equivalent is 50 μSv,
    public static final double GCR_RAD_PER_SOL = .64; //  [in mSv] based on DAT
    public static final double GCR_RAD_SWING = .12; //  [in mSv] based on DAT
 
    // Note : frequency and intensity of SEP events is sporadic and difficult to predict.
    // Its flux varies by several orders of magnitude and are typically dominated by protons,
    public static final double SEP_RAD_PER_SOL = .21; //  [in mSv] 
    public static final double SEP_SWING_FACTOR = 1000; // assuming 3 orders of magnitude (arbitrary)
    // since orders of magnitude are written in powers of 10. 
    // e.g. the order of magnitude of 1500 is 3, since 1500 may be written as 1.5 × 10^3.
    // e.g. the order of magnitude of 1000 is 3, since 1500 may be written as 1.0 × 10^3.
    
    // SPE onset times on the order of minutes to hours and durations of hours to days.

    // Additional notes : 
    // Ref_A assumes absorbed dose of ~150 mGy/year at the Martian surface. 
    // Pavlov et al. assumed an absorbed dose of 50 ±5 mGy/year. 
    // The actual absorbed dose measured by the RAD is 76 mGy/yr at the surface.


	// ROWS of the 2-D dose array
	private static final int THIRTY_DAY = 0;
	private static final int ANNUAL = 1;
	private static final int CAREER = 2;

	// COLUMNS of the 2-D dose array
	// Organ dose equivalent limits, per NCRP guidelines
	private static final int BFO = 0; // BFO = blood-forming organs
	private static final int OCULAR = 1;
	private static final int SKIN = 2;

	// Career whole-body effective dose limits, per NCRP guidelines
	private static final int WHOLE_BODY_DOSE = 1000; // TODO: it varies with age and differs in male and female

	private int solCache = 1, counter30 = 1, counter360 = 1;

	private boolean isExposureChecked = false;

	// dose equivalent limits in mSv (milliSieverts)
	private int [][] DOSE_LIMITS = {
			{ 250, 1000, 1500 },
			{ 500, 2000, 3000 },
			{ WHOLE_BODY_DOSE, 4000, 6000 }	};

	// randomize dose at the start of the sim when a settler arrives on Mars
	private double [][] dose;

	private MarsClock marsClock;
	private MasterClock masterClock;
	private RadiationEvent event;

	//private List<RadiationEvent> eventList = new CopyOnWriteArrayList<>();
	private Map<RadiationEvent, Integer> eventMap = new ConcurrentHashMap<>();

	@SuppressWarnings("unused")
	private PhysicalCondition condition;
	private Person person;

	public RadiationExposure(PhysicalCondition condition) {
		this.person = condition.getPerson();
		this.condition = condition;
		dose = new double[3][3];
		
		
    	if (Simulation.instance().getMasterClock() != null) // for passing maven test
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();
    	
		//if (masterClock == null)
		//	masterClock = Simulation.instance().getMasterClock();

		//if (marsClock == null)
		//	marsClock = masterClock.getMarsClock(); // cannot pass maven test
	}

	//public List<RadiationEvent> getRadiationEventList() {
	//	return eventList;
	//}

	public Map<RadiationEvent, Integer> getRadiationEventMap() {
		return eventMap;
	}

	// Called by checkForRadiation() in EVAOperation and WalkOutside
	public void addDose(int bodyRegion, double amount) {

		// amount is cumulative
		dose[bodyRegion][THIRTY_DAY] = dose[bodyRegion][THIRTY_DAY] + amount;
		dose[bodyRegion][ANNUAL] = dose[bodyRegion][ANNUAL] + amount;
		dose[bodyRegion][CAREER] = dose[bodyRegion][CAREER] + amount;


		BodyRegionType region = null;

		if (bodyRegion == 0)
			region = BodyRegionType.BFO;
		else if (bodyRegion == 1)
			region = BodyRegionType.OCULAR;
		else if (bodyRegion == 2)
			region = BodyRegionType.SKIN;

		event = new RadiationEvent(marsClock, region, amount);
		eventMap.put(event, solCache);

	}

	public void initializeWithRandomDose() {
	  for (int y = 0; y < 3; y++) {
    	if (y == 0) {
    		dose[0][0] = rand(10);
    		dose[1][0] = dose[0][0] + rand(15);
    		dose[2][0] = dose[1][0] + rand(25);
    	}
    	else if (y == 1) {
    		dose[0][1] = rand(30);
    		dose[1][1] = dose[0][1] + rand(60);
    		dose[2][1] = dose[1][1] + rand(80);
    	}
    	else if (y == 2) {
    		dose[0][2] = rand(40);
    		dose[1][2] = dose[0][2] + rand(70);
    		dose[2][2] = dose[1][2] + rand(100);
    	}
	  }

	}

	public int rand(int num) {
		return RandomUtil.getRandomInt(num);
	}

	public void timePassing(double time) {

        // check for the passing of each day
        int solElapsed = marsClock.getSolElapsedFromStart();
        if (solElapsed != solCache) {
        	solCache = solElapsed;
        	counter30++;
        	counter360++;
        	// set the boolean
        	isExposureChecked = false;
        }

		//System.out.println("millisol : " + clock.getMillisol());
        if (!isExposureChecked && marsClock.getMillisol() > 100 && marsClock.getMillisol() < 110) {
        	// check on the effect of the exposure once a day at between 100 & 110 millisols
            // Note: at fastest simulation speed, it can skip as much as ~5 millisols
        	checkExposure();
        	// reset the boolean
        	isExposureChecked = true;
        }

	}

	/*
	 * Checks if the exposure yesterday exceeds the limit and reset counters
	 */
	public void checkExposure() {

		// Compare if any element in a person's dose matrix exceeds the limit
		boolean sick = false;
		int interval = -1;
		int bodyRegion = -1;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                final double dosage = dose[x][y];
                final int limit = DOSE_LIMITS[x][y];
                if (dosage > limit) {
                	interval = x;
            		bodyRegion = y;
            		sick = true;
                }
            }
        }

        // Note: exposure to a dose of 1 Sv is associated with a five percent increase in fatal cancer risk.
        // TODO if sick is true, call methods in HealthProblem...
        if (sick) {
        	//Complaint nausea = new Complaint();
        	//condition.addMedicalComplaint(complaint);
            //condition.getPerson().fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
        }


		if (counter30 == 30) {
			carryOverDosage(THIRTY_DAY);
			counter30 = 0;
		}

		// TODO: convert to martian system. For now, use 360 sol for simplicity and synchronization with the 30-day carryover
		if (counter360 == 360) {
			carryOverDosage(ANNUAL);
			counter360 = 0;
		}

	}

	/*
	 * Recomputes the values in the radiation dosage chart
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

			if (solCache - (int)value == interval + 1 )  {
				dosage = key.getAmount();
				region = key.getBodyRegion();

				int type = 0;

				if (region == BodyRegionType.BFO )
					type = 0;
				else if (region == BodyRegionType.OCULAR )
					type = 1;
				else if (region == BodyRegionType.SKIN )
					type = 2;

				if (interval == 0) {
					// remove the recorded dosage from 31 sols ago
					dose[type][THIRTY_DAY] = dose[type][THIRTY_DAY] - dosage;
					//dose[type][ANNUAL] = dose[type][ANNUAL] + dosage;
				}
				else if (interval == 1) {
					// remove the recorded dosage from 361 sols ago
					dose[type][ANNUAL] = dose[type][ANNUAL] - dosage;
					//dose[type][CAREER] = dose[type][CAREER] + dosage;
				}
			}
		}
	}

	public double[][] getDose() {
		return dose;
	}


    /**
     * Check for radiation exposure of the person performing this EVA.
     * @param time the amount of time on EVA (in millisols)
     */
    public void checkForRadiation(double time) {

    	if (person != null) {

    		double exposure = 0;
       	    double shield_factor = 0;

      	    // TODO: account for the effect of atmosphere pressure on radiation dosage as shown by RAD data
    		//RadiationExposure re = person.getPhysicalCondition().getRadiationExposure();

    		boolean[] exposed = person.getSettlement().getExposed();

    		if (exposed[1])
	    		shield_factor = RandomUtil.getRandomDouble(1) ; // arbitrary
	    	// NOTE: SPE may shield off GCR as shown in Curiosity's RAD data
	    	// since GCR flux is modulated by solar activity. It DECREASES during solar activity maximum
	    	// and INCREASES during solar activity minimum
	    	else
	    		shield_factor = 1 ; // arbitrary

       	    //System.out.println("chance is " + chance + " rand is "+ rand);
    	    for (int i = 0; i < 3 ; i++) {
    	    	if (exposed[i]) {
    	    	// each body region receive a random max dosage
	        	    for (int j = 0; j < 3 ; j++) {
		    	    	double baselevel = 0;
		    	    	if (exposed[2]) {
		    	    		baselevel = .0; // somewhat arbitrary
			    	    	exposure = (baselevel + RandomUtil.getRandomInt(-1,1) * RandomUtil.getRandomDouble(SEP_RAD_PER_SOL * time/RADIATION_CHECK_FREQ)) // highly unpredictable, somewhat arbitrary
			    	    			* RandomUtil.getRandomDouble(SEP_SWING_FACTOR);
		    	    	}
		    	    	// for now, if SEP happens, ignore GCR and Baseline
		    	    	else if (exposed[1]) {
		    	    		baselevel = GCR_RAD_PER_SOL * time/100D;
			    	    	exposure = baselevel + RandomUtil.getRandomInt(-1,1)
			    	    			* RandomUtil.getRandomDouble(shield_factor * GCR_RAD_SWING * time/RADIATION_CHECK_FREQ); // according to Curiosity RAD's data
		    	    	}
		    	    	// for now, if GCR happens, ignore Baseline
		    	    	else if (exposed[0]) {
		    	    		baselevel = BASELINE_RAD_PER_SOL * time/RADIATION_CHECK_FREQ;
			    	    	exposure = baselevel + RandomUtil.getRandomInt(-1,1)
			    	    		* RandomUtil.getRandomDouble(baselevel/3D); // arbitrary
		    	    	}
		    	    	
		    	    	exposure = Math.round(exposure*10000.0)/10000.0;

		    	    	addDose(j, exposure);
		    	    	//System.out.println("rand is "+ rand);
		    	    	if (i != 0) { // show logger.info for the GCR or SEP event only {
			    	    	logger.info(person.getName() + " was exposed to " + exposure 
			    	    			+ " mSv dose of radiation in body region " 
			    	    			+ i + " during an EVA operation near " + person.getAssociatedSettlement());
			    	    	person.fireUnitUpdate(UnitEventType.RADIATION_EVENT);
		    	    	}
	        	    }
    	    	}
    	    }
    	}
    }

}
