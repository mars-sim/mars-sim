/*
 * Mars Simulation Project
 * Population.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Population implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Population.class.getName());

	private static final int MAX = 60;

	private int numTourists;
	
	private int numResidents;
	
	private int numResearchers;
	
	private int numBeds;
	
	
	private double growthRateTourists;
	
	private double growthRateResidents;
	
	private double growthRateResearchers;
	
	private double growthRateBeds;
	
	
	public Population() {
			
		growthRateTourists = RandomUtil.getRandomDouble(-.4, .4);
		growthRateResidents = RandomUtil.getRandomDouble(-.4, .4);
		growthRateResearchers = RandomUtil.getRandomDouble(-.4, .4);
		growthRateBeds = RandomUtil.getRandomDouble(-.4, .4);
		
		numTourists = RandomUtil.getRandomInt(0, 3);
		numResidents = RandomUtil.getRandomInt(10, 20);
		numResearchers = RandomUtil.getRandomInt(3, 6);
		numBeds = MAX;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		if (pulse.isNewSol()) {
			
			growthRateTourists += RandomUtil.getRandomDouble(-.2, .2);
			growthRateResidents += RandomUtil.getRandomDouble(-.2, .2);
			growthRateResearchers += RandomUtil.getRandomDouble(-.2, .2);
			growthRateBeds += RandomUtil.getRandomDouble(-.2, .2);
			
			// Increase beds
			int rand = RandomUtil.getRandomInt(0, 10);
			if (rand == 0) {
				numBeds++;
			}
			
			if (numTourists + numResidents + numResearchers > numBeds) {
				rand = RandomUtil.getRandomInt(0, 10);
				if (rand == 0) {
					numResidents++;
				}
				else if (rand == 1 || rand == 2) {
					numResearchers++;
				}
				else {
					numTourists++;
				}
			}
			
//			numTourists = numTourists + RandomUtil.getRandomInt(-1, 1);
//			if (numTourists < 0)
//				numTourists = 0;
//			
//			numResidents = numResidents + RandomUtil.getRandomInt(-1, 1);
//			if (numResidents < 0)
//				numResidents = 0;
//			
//			numResearchers = numResearchers + RandomUtil.getRandomInt(-1, 1);
//			if (numResearchers < 0)
//				numResearchers = 0;
			
			while (numTourists + numResidents + numResearchers > numBeds) {
				
				rand = RandomUtil.getRandomInt(0, 10);
				if (rand == 0) {
					numResidents--;
				}
				else if (rand == 1 || rand == 2) {
					numResearchers--;
				}
				else {
					numTourists--;
				}
			}
		}
		
		return false;
	}
	
	public int getNumBed() {
		return numBeds;
	}
	
	public int getNumTourists() {
		return numTourists;
	}
	
	public int getNumResidents() {
		return numResidents;
	}
	
	public int getNumResearchers() {
		return numResearchers;
	}
	
	public int getTotalPopulation() {
		return numTourists + numResidents + numResearchers;
	}
}
