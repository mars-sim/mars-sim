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
import com.mars_sim.tools.util.RandomUtil;

public class Zone implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Zone.class.getName());

	// Area in square meters
	private double area;
	
	private double growthRate;
	
	private ZoneType type;
	
	private Colony colony;

	public Zone(ZoneType type, Colony colony) {
		this.type = type;
		this.colony = colony;

		if (ZoneType.BUSINESS == type)
			area = RandomUtil.getRandomDouble(25, 50);
		else if (ZoneType.COMMAND_CONTROL == type)
			area = RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.COMMUNICATION == type)
			area = RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.CONSTRUCTION == type)	
			area = RandomUtil.getRandomDouble(50, 200);
		else if (ZoneType.EDUCATION == type)	
			area = RandomUtil.getRandomDouble(10, 30);
		else if (ZoneType.ENGINEERING == type)	
			area = RandomUtil.getRandomDouble(50, 150);
		else if (ZoneType.INDUSTRIAL == type)	
			area = RandomUtil.getRandomDouble(150, 200);
		else if (ZoneType.LIFE_SUPPORT == type)	
			area = RandomUtil.getRandomDouble(100, 150);
		else if (ZoneType.OPERATION == type)	
			area = RandomUtil.getRandomDouble(100, 150);
		else if (ZoneType.RECREATION == type)	
			area = RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.RESEARCH == type)	
			area = RandomUtil.getRandomDouble(50, 100);
		else if (ZoneType.RESOURCE == type)	
			area = RandomUtil.getRandomDouble(150, 200);
		else if (ZoneType.TRANSPORTATION == type)	
			area = RandomUtil.getRandomDouble(50, 100);
		
		growthRate = RandomUtil.getRandomDouble(0, 2);
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		int millisolInt = pulse.getMarsTime().getMillisolInt();
		if (pulse.isNewMSol() && millisolInt > 5 && millisolInt % 120 == 1) {
			
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
				score = Math.max(.4, Math.min(-.4, score));
				
				growthRate += RandomUtil.getRandomDouble(-0.01 + score, 0.011 + score);
			}
			else {
				
				growthRate += RandomUtil.getRandomDouble(-0.01, 0.011);
			}

			
			if (growthRate > 10)
				growthRate = 10;
			else if (growthRate < -5)
				growthRate = -5;
			
			area = area * (1 + growthRate/100);
			// Slightly adjust the growth rate after making the contribution to 
			// the increase or decrease of the zone area
			growthRate = growthRate *.9;
		}

		return false;
	}

	public ZoneType getZoneType() {
		return type;
	}
	
	public double getArea() {
		return area;
	}
	
	public double getGrowthRate() {
		return growthRate;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		type = null;
	}
}
