/*
 * Mars Simulation Project
 * Population.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.authority.Nation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.science.Researcher;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.tools.util.RandomUtil;

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
	
	private Colony colony;
	
	private Set<Colonist> colonists = new HashSet<>();
	
	public Population(Colony colony) {
			
		this.colony = colony;
		
		growthRateTourists = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResidents = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResearchers = RandomUtil.getRandomDouble(-.3, .4);
		growthRateBeds = RandomUtil.getRandomDouble(-.3, .4);
		
		numTourists = RandomUtil.getRandomInt(0, 3);
		numResidents = RandomUtil.getRandomInt(10, 20);
		numResearchers = RandomUtil.getRandomInt(3, 6);
			
		int totPop = getTotalPopulation();
		numBeds = RandomUtil.getRandomInt(totPop, INITIAL);
	
//		System.out.println("Colony: " + colony.getName() 
//				+ "   Tot Pop: " + totPop 
//				+ "   Quarters: " + numBeds);
		
		for (int i = 0; i < numResearchers; i++) {
			colonists.add(new Researcher(colony.getName() + " R" + i, colony.getId()));
		}
	
	}
	
	public Colony getColony() {
		return colony;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		double researchersCache = numResearchers;
		
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
			
//			int totPop = getTotalPopulation();
//			
//			if ((int)numBeds < totPop)
//				numBeds = totPop;

			// Checks if there is enough beds. 
			// If not, slow the growth rate in one type of pop
			if (numTourists + numResidents + numResearchers < numBeds + 1) {
				
				int rand = RandomUtil.getRandomInt(0, 10);
				if (rand == 0) {
					growthRateResidents -= 0.2;
					numResidents += growthRateResidents;
				}
				else if ((rand == 1 || rand == 2)) {
					growthRateResearchers -= 0.2;
					numResearchers += growthRateResearchers;
				}
				else {
					growthRateTourists -= 0.2;
					numTourists += growthRateTourists;
				}
			}
					
			if ((int)researchersCache < (int)numResearchers) {
				removeOneResearcher();
			}
			else if ((int)researchersCache > (int)numResearchers) {
				colonists.add(new Researcher(colony.getName() + " R" 
					+ (int)numResearchers, colony.getId()));
			}
			
		}
		
		return false;
	}
	
	/**
	 * Removes a researcher.
	 */
	public void removeOneResearcher() {
	
		int rand = RandomUtil.getRandomInt(colonists.size() - 1);
	
		List<Colonist> list = new ArrayList<>(colonists);
		
		Colonist c = list.get(rand);
		
		colonists.remove(c);

		Nation nation = colony.getNation();
		
		if (nation == null) {
			String countryName = colony.getAuthority().getOneCountry();
			logger.warning("Colony: " + colony.getName() 
							+ "  Sponsor: " + colony.getAuthority().getName()
							+ "  Country: " + countryName);
		}
		else {
			nation.addColonist(c);
		}
		
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
