/*
 * Mars Simulation Project
 * Population.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.project.ColonyResearcher;
import com.mars_sim.core.moon.project.ColonySpecialist;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public class Population implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Population.class.getName());

	private static final int INITIAL = 60;

	private double numTourists;
	private double numResidents;
	private double numResearchers;
	private double numEngineers;
	
	private double numLodges;
	
	private double growthRateTourists;
	private double growthRateResidents;
	private double growthRateResearchers;
	private double growthRateEngineers;
	
	private double growthRateLodge;
	
	private Colony colony;
	
	private Set<Colonist> colonists = new HashSet<>();
	
	/**
	 * Constructor.
	 * 
	 * @param colony
	 */
	public Population(Colony colony) {
		this.colony = colony;
		
		growthRateEngineers = RandomUtil.getRandomDouble(-.2, .4);
		growthRateTourists = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResidents = RandomUtil.getRandomDouble(-.3, .4);
		growthRateResearchers = RandomUtil.getRandomDouble(-.3, .4);
		growthRateLodge = RandomUtil.getRandomDouble(-.3, .4);
		
		numEngineers = RandomUtil.getRandomInt(5, 10);
		numTourists = RandomUtil.getRandomInt(0, 3);
		numResidents = RandomUtil.getRandomInt(10, 20);
		numResearchers = RandomUtil.getRandomInt(3, 6);
		numEngineers = RandomUtil.getRandomInt(3, 6);
		
		int totPop = getTotalPopulation();
		numLodges = RandomUtil.getRandomInt(totPop, INITIAL);
	}
	
	public void init() {
		for (int i = 0; i < numResearchers; i++) {
			colonists.add(new ColonyResearcher("Researcher" + i, colony));
		}
		for (int i = 0; i < numEngineers; i++) {
			colonists.add(new ColonySpecialist("Engineer" + i, colony));
		}
	}
	
	public Colony getColony() {
		return colony;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		double researchersCache = numResearchers;
		double engineersCache = numEngineers;
		
		int millisolInt = pulse.getMarsTime().getMillisolInt();
		if (pulse.isNewHalfSol() || pulse.isNewIntMillisol() && millisolInt > 5 && millisolInt % 120 == 1) {

			// Recalculate tourists growth rate
			growthRateTourists += RandomUtil.getRandomDouble(-.125, .2) + growthRateLodge / 5;
			// Recalculate tourists
			numTourists += growthRateTourists;
			
			// Recalculate residents growth rate
			growthRateResidents += RandomUtil.getRandomDouble(-.125, .2) + growthRateLodge / 4;
			// Recalculate residents
			numResidents += growthRateResidents;
			
			// Recalculate researchers growth rate
			growthRateResearchers += RandomUtil.getRandomDouble(-.125, .2) + colony.getResearchDemand()
								+ growthRateLodge / 5;		
			// Recalculate researchers	
			numResearchers += growthRateResearchers;
			
			// Recalculate engineers growth rate
			growthRateEngineers += RandomUtil.getRandomDouble(-.125, .2) + colony.getDevelopmentDemand()
								+ growthRateLodge / 5;		
			// Recalculate engineers	
			numEngineers += growthRateEngineers;

			// Recalculate lodge growth rate
			growthRateLodge += RandomUtil.getRandomDouble(-.05, .1) 
					+ .5 * (growthRateLodge 
					+ .2 * (growthRateResidents * 2 + growthRateResearchers 
					+ growthRateEngineers + growthRateTourists));			
			// Recalculate available lodges
			numLodges += growthRateLodge;

			// Set minimum
			if (numResidents < 0)
				numResidents = 0;
			if (numResearchers < 0)
				numResearchers = 0;
			if (numEngineers < 0)
				numEngineers = 0;
			if (numTourists < 0)
				numTourists = 0;
			if (numLodges < 0)
				numLodges = 0;
			
			// Checks if there is enough lodging units. 
			// If not, slow the growth rate in one type of pop
			if (numTourists + numResidents + numResearchers + numEngineers > numLodges * .95) {
				
				int rand = RandomUtil.getRandomInt(0, 10);
				if (rand == 0) {
					growthRateResidents -= 0.1;
				}
				else if ((rand == 1 || rand == 2)) {
					growthRateResearchers -= 0.1;
				}
				else if ((rand >= 3 || rand <= 5)) {
					growthRateEngineers -= 0.1;
				}
				else {
					growthRateTourists -= 0.1;
				}
				
				growthRateLodge += rand / 6.0;
			}
					
			// Limit the growth rate
			if (growthRateTourists > 1)
				growthRateTourists = 1;
			else if (growthRateTourists < -.5)
				growthRateTourists = -.5;
			
			if (growthRateResidents > 1)
				growthRateResidents = 1;
			else if (growthRateResidents < -.5)
				growthRateResidents = -.5;
			
			if (growthRateEngineers > 1)
				growthRateEngineers = 1;
			else if (growthRateEngineers < -.5)
				growthRateEngineers = -.5;	
			
			if (growthRateResearchers > 1)
				growthRateResearchers = 1;
			else if (growthRateResearchers < -.5)
				growthRateResearchers = -.5;	
			
			if (growthRateLodge > 4)
				growthRateLodge = 4;
			else if (growthRateLodge < -0.15)
				growthRateLodge = -0.15;
			
			if ((int)researchersCache < (int)numResearchers 
					&& !colonists.isEmpty()) {
				removeOneResearcher();
			}
			else if ((int)researchersCache > (int)numResearchers) {
				addOneResearcher();
			}
			// else if they are equal, then no change
			
			if ((int)engineersCache < (int)numEngineers 
					&& !colonists.isEmpty()) {
				removeOneEngineer();
			}
			else if ((int)engineersCache > (int)numEngineers) {
				addOneEngineer();
			}
			// else if they are equal, then no change
			
		}
		
		colonists.forEach(r -> r.timePassing(pulse));
		
		return false;
	}
	
	
	/**
	 * Adds a researcher.
	 */
	private void addOneResearcher() {
		
		colonists.add(new ColonyResearcher("R" 
					+ (int)numResearchers, colony));

		// Pull back the growth rate as a researcher has just been added
		growthRateResearchers = growthRateResearchers * .9;
	}
	
	/**
	 * Adds an engineer.
	 */
	private void addOneEngineer() {		
		colonists.add(new ColonySpecialist("E" 
					+ (int)numEngineers, colony));
		
		// Pull back the growth rate as an engineer has just been added
		growthRateEngineers = growthRateEngineers * .9;
	}
	
	/**
	 * Removes a researcher.
	 */
	private void removeOneResearcher() {
	
		var col = colonists.stream().filter(ColonyResearcher.class::isInstance).findAny();
		if (col.isEmpty()) {
			logger.warning("No researcher to remove!");
			return;
		}
		colonists.remove(col.get());

		// Speed up the growth rate as a researcher has just been removed
		growthRateResearchers = growthRateResearchers * .9;		
	}
	
	/**
	 * Removes an engineer.
	 */
	private void removeOneEngineer() {
	
		var col = colonists.stream().filter(ColonySpecialist.class::isInstance).findAny();
		if (col.isEmpty()) {
			logger.warning("No engineer to remove!");
			return;
		}
		colonists.remove(col.get());
		
		// Speed up the growth rate as an engineer has just been removed
		growthRateEngineers = growthRateEngineers * .9;		
	}
	
	/**
	 * Gets a set of researchers.
	 * 
	 * @return
	 */
	public Set<ColonyResearcher> getResearchers() {
		return colonists.stream()
				.filter(ColonyResearcher.class::isInstance)
				.map(ColonyResearcher.class::cast)
				.collect(Collectors.toSet());
	}
	
	public int getNumLodge() {
		return (int)numLodges;
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
	
	public int getNumEngineers() {
		return (int)numEngineers;
	}
	
	public int getTotalPopulation() {
		return (int)numTourists + (int)numResidents + (int)numResearchers + (int)numEngineers;
	}
	
	public double getGrowthLodge() {
		return growthRateLodge;
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
	
	public double getGrowthEngineers() {
		return growthRateEngineers;
	}
	
	public double getGrowthTotalPopulation() {
		return growthRateTourists + growthRateResidents + growthRateResearchers + growthRateEngineers;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		colony = null;
		colonists.clear();
		colonists = null;
	}
}
