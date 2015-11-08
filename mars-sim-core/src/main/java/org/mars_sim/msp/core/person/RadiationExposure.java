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

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

public class RadiationExposure implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // probability of getting hit by GCG/SPE radiation in an interval of 100 milliSol during an EVA
    public static final double CHANCE_PER_100MSOL_DURING_EVA = .5; // (arbitrary for now)

    public static final double RAD_PER_SOL = .4087; // = 150 mSv / 365 days

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

	private MarsClock clock;
	private MasterClock masterClock;
	private RadiationEvent event;

	//private List<RadiationEvent> eventList = new CopyOnWriteArrayList<>();
	private Map<RadiationEvent, Integer> eventMap = new ConcurrentHashMap<>();

	@SuppressWarnings("unused")
	private PhysicalCondition condition;
	//private Person person;

	public RadiationExposure(PhysicalCondition condition) {
		//this.person = person;
		this.condition = condition;
		dose = new double[3][3];
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

		if (masterClock == null )
			masterClock = Simulation.instance().getMasterClock();

		clock = masterClock.getMarsClock();

		BodyRegionType region = null;

		if (bodyRegion == 0)
			region = BodyRegionType.BFO;
		else if (bodyRegion == 1)
			region = BodyRegionType.OCULAR;
		else if (bodyRegion == 2)
			region = BodyRegionType.SKIN;

		event = new RadiationEvent(clock, region, amount);
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

		if (masterClock == null )
			masterClock = Simulation.instance().getMasterClock();

		clock = masterClock.getMarsClock();

        // check for the passing of each day
        int solElapsed = MarsClock.getSolOfYear(clock);
        if (solElapsed != solCache) {
        	solCache = solElapsed;
        	counter30++;
        	counter360++;
        	// set the boolean
        	isExposureChecked = false;
        }

		//System.out.println("millisol : " + clock.getMillisol());
        if (!isExposureChecked && clock.getMillisol() > 100 && clock.getMillisol() < 110) {
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

}
