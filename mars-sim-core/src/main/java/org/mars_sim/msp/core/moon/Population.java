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

	private static final int INITIAL = 60;

	private double numTourists;
	
	private double numResidents;
	
	private double numResearchers;
	
	private double numBeds;
	
	private double growthRateTourists;
	
	private double growthRateResidents;
	
	private double growthRateResearchers;
	
	private double growthRateBeds;
	
	
	public Population() {
			
		growthRateTourists = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResidents = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResearchers = RandomUtil.getRandomDouble(-.3, .4);
		growthRateBeds = RandomUtil.getRandomDouble(-.3, .4);
		
		numTourists = RandomUtil.getRandomInt(0, 3);
		numResidents = RandomUtil.getRandomInt(10, 20);
		numResearchers = RandomUtil.getRandomInt(3, 6);
		numBeds = RandomUtil.getRandomInt((int)(numTourists + numResidents + numResearchers), INITIAL);
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		if (pulse.isNewHalfSol()) {
			
			// Recalculate tourists
			growthRateTourists += RandomUtil.getRandomDouble(-.15, .2);
			
			if (growthRateTourists > 1)
				growthRateTourists = 1;
			else if (growthRateTourists < -.5)
				growthRateTourists = -.5;
			
			numTourists += growthRateTourists;
			
			// Recalculate residents
			growthRateResidents += RandomUtil.getRandomDouble(-.15, .2);
			
			if (growthRateResidents > 1)
				growthRateResidents = 1;
			else if (growthRateResidents < -.5)
				growthRateResidents = -.5;
			
			numResidents += growthRateResidents;

			// Recalculate researchers
			growthRateResearchers += RandomUtil.getRandomDouble(-.15, .2);
			
			if (growthRateResearchers > 1)
				growthRateResearchers = 1;
			else if (growthRateResearchers < -.5)
				growthRateResearchers = -.5;			
					
			numResearchers += growthRateResearchers;

			// Recalculate beds
			growthRateBeds += RandomUtil.getRandomDouble(-.15, .2);
			
			if (growthRateBeds > 1)
				growthRateBeds = 1;
			else if (growthRateBeds < -0.5)
				growthRateBeds = -0.5;
			
			numBeds += growthRateBeds;
			
			int totPop = getTotalPopulation();
			
			if ((int)numBeds < totPop)
				numBeds = totPop;

			while (numTourists + numResidents + numResearchers < numBeds + 1) {
				
				int rand = RandomUtil.getRandomInt(0, 10);
				if (rand == 0) {
					growthRateResidents -= 0.2;
				}
				else if ((rand == 1 || rand == 2)) {
					growthRateResearchers -= 0.2;
				}
				else {
					growthRateTourists -= 0.2;
				}
			}
		}
		
		return false;
	}
	
	public int getNumBed() {
		return (int)numBeds;
	}
	
	public int getNumTourists() {
		return (int)numTourists;
	}
	
	public int getNumResidents() {
		return (int)numResidents;
	}
	
	public int getNumResearchers() {
		return (int)numResearchers;
	}
	
	public int getTotalPopulation() {
		return (int)numTourists + (int)numResidents + (int)numResearchers;
	}
	
	public double getGrowthNumBed() {
		return growthRateBeds;
	}
	
	public double getGrowthTourists() {
		return growthRateTourists;
	}
	
	public double getGrowthResidents() {
		return growthRateResidents;
	}
	
	public double getGrowthResearchers() {
		return growthRateResearchers;
	}
	
	public double getGrowthTotalPopulation() {
		return growthRateTourists + growthRateResidents + growthRateResearchers;
	}
	
	
	
}
