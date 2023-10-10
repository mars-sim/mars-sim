/*
 * Mars Simulation Project
 * Zone.java
 * @date 2023-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Zone implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Zone.class.getName());

	// Area in square meters
	private double area;
	
	private ZoneType type;
	
	private double growthRate;
	
//	private static Map<ZoneType, Double> initialArea = new HashMap<>();
//	
//	static {
//		initialArea.put(ZoneType.BUSINESS, RandomUtil.getRandomDouble(25, 50));
//		initialArea.put(ZoneType.COMMAND_CONTROL, RandomUtil.getRandomDouble(50, 100));
//		initialArea.put(ZoneType.COMMUNICATION, RandomUtil.getRandomDouble(50, 100));
//		initialArea.put(ZoneType.CONSTRUCTION, RandomUtil.getRandomDouble(100, 200));
//		initialArea.put(ZoneType.EDUCATION, RandomUtil.getRandomDouble(10, 50));
//		initialArea.put(ZoneType.ENGINEERING, RandomUtil.getRandomDouble(100, 200));
//		initialArea.put(ZoneType.LIFE_SUPPORT, RandomUtil.getRandomDouble(200, 300));
//		initialArea.put(ZoneType.OPERATION, RandomUtil.getRandomDouble(100, 200));
//		initialArea.put(ZoneType.RECREATION, RandomUtil.getRandomDouble(100, 150));
//		initialArea.put(ZoneType.RESEARCH, RandomUtil.getRandomDouble(100, 200));
//		initialArea.put(ZoneType.RESOURCE_EXTRACTION, RandomUtil.getRandomDouble(300, 400));
//		initialArea.put(ZoneType.TRANSPORTATION, RandomUtil.getRandomDouble(80, 150));
//	}
	
	
//	Zone(ZoneType type, double area) {
//		this.type = type;
//		this.area = area;
//		
//		growthRate = RandomUtil.getRandomDouble(0, 2);
//	}
	
	public Zone(ZoneType type) {
		this.type = type;

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
		
		if (pulse.isNewHalfSol()) {
			growthRate += RandomUtil.getRandomDouble(-0.35, 0.5);
			
			if (growthRate > 10)
				growthRate = 10;
			else if (growthRate < -5)
				growthRate = -5;
		
			area += growthRate;
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
	
}
