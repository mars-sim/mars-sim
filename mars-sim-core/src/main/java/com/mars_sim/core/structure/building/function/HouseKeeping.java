/*
 * Mars Simulation Project
 * HouseKeeping.java
 * @date 2022-06-25
 * @author Barry Evans
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mars_sim.core.tool.RandomUtil;

/**
 * Utility class to represent a fixed set of inspection & cleaning activities.
 */
public class HouseKeeping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// The map of each system that needs inspection and its condition score
	private Map<String, Double> inspectionMap;
	// The map of each system that needs inspection and its cleanliness score
	private Map<String, Double> cleaningMap;

	private double cumulativeWorkTime = 0D;
	
	/**
	 * Constructor.
	 * 
	 * @param cleaningList
	 * @param inspectionList
	 */
	public HouseKeeping(String[] cleaningList, String[] inspectionList) {

		inspectionMap = new HashMap<>();
		for (String s : inspectionList) {
			inspectionMap.put(s, RandomUtil.getRandomDouble(75, 95));
		}

		cleaningMap = new HashMap<>();
		for (String s : cleaningList) {
			cleaningMap.put(s, RandomUtil.getRandomDouble(75, 95));
		}
	}

	public double getAverageInspectionScore() {
		return inspectionMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
	}
	
	public double getAverageCleaningScore() {
		return cleaningMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
	}
	
	/**
	 * Degrades the cleanliness of each system.
	 */
	public void degradeCleaning(double value) {
		cleaningMap.replaceAll((s, v) -> v - value);
	}

	/**
	 * Degrades the condition of each system.
	 */
	public void degradeInspected(double value) {
		inspectionMap.replaceAll((s, v) -> v - value);
	}
	
	public String getLeastInspected() {
		return getLeast(this.inspectionMap);
	}
	
	public String getLeastCleaned() {
		return getLeast(this.cleaningMap);
	}
	
	/**
	 * Gets the least inspected or cleaned item.
	 * 
	 * @return
	 */
	private static String getLeast(Map<String, Double> map) {
		// In future, each person can become the knowledge expert on a particular system
		// and may be more proficient on one system over another
		Entry<String, Double> least = null;
		for (Entry<String, Double> s : map.entrySet()) {
			if (least == null || least.getValue() > s.getValue()) {
				least = s;
		    }
		}
		return (least != null ? least.getKey() : null);
	}

	/**
	 * A item has been inspected.
	 * 
	 * @param s
	 */
	public void inspected(String s, double value) {
		inspectionMap.merge(s, value, Double::sum);
		cumulativeWorkTime += value;
	}

	/**
	 * An item has been cleaned.
	 * 
	 * @param s
	 */
	public void cleaned(String s, double value) {
		cleaningMap.merge(s, value, Double::sum);
		cumulativeWorkTime += value;
	}

	/**
	 * How much time has been spend working of this entity
	 * @return
	 */
    public double getCumulativeWorkTime() {
        return cumulativeWorkTime;
    }
}
