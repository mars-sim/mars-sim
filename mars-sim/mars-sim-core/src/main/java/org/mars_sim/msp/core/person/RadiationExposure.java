/**
 * Mars Simulation Project
 * RadiationExposure.java
 * @version 3.08 2015-04-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

public class RadiationExposure implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // probability of getting hit by GCG/SPE radiation in an interval of 100 milliSol during an EVA
    public static final double CHANCE_PER_100MSOL_DURING_EVA = .5; // (arbitrary for now)

    public static final double RAD_PER_SOL = .4087; // = 150 mSv / 365 days

	// COLUMNS of the 2-D dose array
	// Organ dose equivalent limits, per NCRP guidelines
	int BFO_dose_equivalent = 0; // BFO = blood-forming organs
	int ocular_lens_dose_equivalent = 1;
	int skin_dose_equivalent = 2;

	// Career whole-body effective dose limits, per NCRP guidelines
	int whole_body_dose_effective = 1000; // TODO: it varies with age and differs in male and female

	// ROWS of the 2-D dose array
	int thirtyDay = 0;
	int annual = 1;
	int career = 2;

	private int solCache = 1;
	private int sol = 1;

	// dose equivalent limits in mSv (milliSieverts)
	int [][] DOSE_LIMITS = {
			{ 250, 1000, 1500 },
			{ 500, 2000, 3000 },
			{ whole_body_dose_effective, 4000, 6000 }	};

	// randomize dose at the start of the sim when a settler arrives on Mars
	double [][] dose;

	@SuppressWarnings("unused")
	private PhysicalCondition condition;
	//private Person person;

	public RadiationExposure(PhysicalCondition condition) {
		//this.person = person;
		this.condition = condition;
		dose = new double[3][3];
	}


	public void addDose(int bodyRegion, int interval, double amount) {
		if (interval == 0) {
			dose[bodyRegion][thirtyDay] = dose[bodyRegion][thirtyDay] + amount;
			dose[bodyRegion][annual] = dose[bodyRegion][annual] + amount;
			dose[bodyRegion][career] = dose[bodyRegion][career] + amount;
		}
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

		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
        // check for the passing of each day
        int solElapsed = MarsClock.getSolOfYear(clock);
        if ( solElapsed != solCache) {
        	// check on the effect of the expose only once a day
        	checkExposure();
        	solCache = solElapsed;
        }

	}

	public void checkExposure() {
		// clear the annual interval dosage exposure
		if (sol%30 == 0) {
			dose[BFO_dose_equivalent][annual] = 0;
			dose[ocular_lens_dose_equivalent][annual] = 0;
			dose[skin_dose_equivalent][annual] = 0;
		}

		if (sol == 365) {
			dose[BFO_dose_equivalent][thirtyDay] = 0;
			dose[ocular_lens_dose_equivalent][thirtyDay] = 0;
			dose[skin_dose_equivalent][thirtyDay] = 0;
			sol = 0;
		}

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

        // if sick is true, call methods in HealthProblem...
        if (sick) {
        	//Complaint nausea = new Complaint();
        	//condition.addMedicalComplaint(complaint);
            //condition.getPerson().fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
        }
		solCache++;

	}

	public double[][] getDose() {
		return dose;
	}

}
