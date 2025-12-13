/*
 * Mars Simulation Project
 * Zone.java
 * @date 2023-10-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public class Zone implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

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
	public Zone(ZoneType type, Colony colony) {
		this.type = type;
		this.colony = colony;

		double factor = RandomUtil.getRandomDouble(.05, .1);

		switch (type) {
			case BUSINESS:
				area = factor * RandomUtil.getRandomDouble(25, 50);
				break;
			case COMMAND_CONTROL:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
			case COMMUNICATION:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
			case CONSTRUCTION:
				area = factor * RandomUtil.getRandomDouble(50, 200);
				break;
			case EDUCATION:
				area = factor * RandomUtil.getRandomDouble(10, 30);
				break;
			case ENGINEERING:
				area = factor * RandomUtil.getRandomDouble(50, 150);
				break;
			case INDUSTRIAL:
				area = factor * RandomUtil.getRandomDouble(150, 200);
				break;
			case LIFE_SUPPORT:
				area = factor * RandomUtil.getRandomDouble(100, 150);
				break;
			case OPERATION:
				area = factor * RandomUtil.getRandomDouble(100, 150);
				break;
			case RECREATION:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
			case RESEARCH:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
			case RESOURCE:
				area = factor * RandomUtil.getRandomDouble(150, 200);
				break;
			case TRANSPORTATION:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
			default:
				area = factor * RandomUtil.getRandomDouble(50, 100);
				break;
		}
		
		growthPercent = RandomUtil.getRandomDouble(0.1, 1);
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		if (pulse.isNewHalfSol() || (RandomUtil.getRandomInt(50) == 1)) {
			
			switch (type) {
				case ZoneType.RESEARCH -> {
					int numResearcher = colony.getPopulation().getNumResearchers();
					int numResearchProj = colony.getNumResearchProjects();
					double researchValue = colony.getTotalResearchValue();
					double score = 0;
					if (numResearcher > 0 && numResearchProj > 0)
						score = Math.log10(1 +  researchValue / 5 / numResearchProj / numResearcher);
	
					score = Math.clamp(score, -.2, .2);
					growthPercent += RandomUtil.getRandomDouble(-0.011 + score, 0.011 + score);
				}
				case ZoneType.ENGINEERING -> {
					int numEngineer = colony.getPopulation().getNumEngineers();
					int numDevelopmentProj = colony.getNumDevelopmentProjects();
					double developmentValue = colony.getTotalDevelopmentValue();
					double score = 0;
					if (numEngineer > 0 && numDevelopmentProj > 0)
						score = Math.log10(1 +  developmentValue / 5 / numDevelopmentProj / numEngineer);
					score = Math.clamp(score, -.2, .2);
			
					growthPercent += RandomUtil.getRandomDouble(-0.01 + score, 0.01 + score);
				}
				default -> 	
					growthPercent += RandomUtil.getRandomDouble(-0.01, 0.01);
			}
		
			growthPercent = Math.clamp(growthPercent, -5, 10);
			
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

}
