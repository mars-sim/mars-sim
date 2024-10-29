/*
 * Mars Simulation Project
 * Zone.java
 * @date 2023-10-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public class Zone implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(Zone.class.getName());

	// Area in square meters
	private double area;
	
	private double growthPercent;
	
	private ZoneType type;
	
	private Colony colony;

	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param colony
	 * @param startup Is it at the startup of the simulation ?
	 */
	public Zone(ZoneType type, Colony colony, boolean startup) {
		this.type = type;
		this.colony = colony;

		double factor = RandomUtil.getRandomDouble(.05, .1);

		if (startup) {
			factor = 1;
		}

		if (ZoneType.BUSINESS == type)
			area = factor * RandomUtil.getRandomDouble(25, 50);
		else if (ZoneType.COMMAND_CONTROL == type)
			area = factor * RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.COMMUNICATION == type)
			area = factor * RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.CONSTRUCTION == type)	
			area = factor * RandomUtil.getRandomDouble(50, 200);
		else if (ZoneType.EDUCATION == type)	
			area = factor * RandomUtil.getRandomDouble(10, 30);
		else if (ZoneType.ENGINEERING == type)	
			area = factor * RandomUtil.getRandomDouble(50, 150);
		else if (ZoneType.INDUSTRIAL == type)	
			area = factor * RandomUtil.getRandomDouble(150, 200);
		else if (ZoneType.LIFE_SUPPORT == type)	
			area = factor * RandomUtil.getRandomDouble(100, 150);
		else if (ZoneType.OPERATION == type)	
			area = factor * RandomUtil.getRandomDouble(100, 150);
		else if (ZoneType.RECREATION == type)	
			area = factor * RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.RESEARCH == type)	
			area = factor * RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.RESOURCE == type)	
			area = factor * RandomUtil.getRandomDouble(150, 200);
		else if (ZoneType.TRANSPORTATION == type)	
			area = factor * RandomUtil.getRandomDouble(50, 100);
		
		growthPercent = RandomUtil.getRandomDouble(0.1, 1);
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
//		int missionSol = pulse.getMarsTime().getMissionSol();
		
		if (pulse.isNewHalfSol() || (RandomUtil.getRandomInt(50) == 1)) {
			
			if (ZoneType.RESEARCH == type) {
				int numResearcher = colony.getPopulation().getNumResearchers();
				int numResearchProj = colony.getNumResearchProjects();
				double researchValue = colony.getTotalResearchValue();
				double score = 0;
				if (numResearcher > 0 && numResearchProj > 0)
					score = Math.log10(1 +  researchValue / 5 / numResearchProj / numResearcher);
//				logger.info(colony.getName() + " research: " + score
//						+ "  researchValue: " + researchValue
//						+ "  numResearchProj: " + numResearchProj
//						);
				if (score > .2)
					score = .2;
				else if (score < -.2)
					score = -.2;
				
				growthPercent += RandomUtil.getRandomDouble(-0.011 + score, 0.011 + score);
			}
			
			else if (ZoneType.ENGINEERING == type) {
				int numEngineer = colony.getPopulation().getNumEngineers();
				int numDevelopmentProj = colony.getNumDevelopmentProjects();
				double developmentValue = colony.getTotalDevelopmentValue();
				double score = 0;
				if (numEngineer > 0 && numDevelopmentProj > 0)
					score = Math.log10(1 +  developmentValue / 5 / numDevelopmentProj / numEngineer);
//				logger.info(colony.getName() + " research: " + score
//						+ "  researchValue: " + researchValue
//						+ "  numResearchProj: " + numResearchProj
//						);
				if (score > .2)
					score = .2;
				else if (score < -.2)
					score = -.2;
		
				growthPercent += RandomUtil.getRandomDouble(-0.01 + score, 0.01 + score);
			}
			else {
				
				growthPercent += RandomUtil.getRandomDouble(-0.01, 0.01);
			}
		
			if (growthPercent > 10)
				growthPercent = 10;
			else if (growthPercent < -5)
				growthPercent = -5;
			
			area *= 1 + growthPercent/100;
			
			// Slightly adjust the growth rate after making the contribution to 
			// the increase or decrease of the zone area
			growthPercent = growthPercent *.95;
		}

		return false;
	}

	public ZoneType getZoneType() {
		return type;
	}
	
	public double getArea() {
		return area;
	}
	
	public double getGrowthPercent() {
		return growthPercent;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		type = null;
	}
}
