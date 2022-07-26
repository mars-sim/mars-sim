/*
 * Mars Simulation Project
 * HouseKeeping.java
 * @date 2022-06-25
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class to represent a fixed set of inspection & cleaning activities.
 */
public class HouseKeeping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Number of inspections
	private static final int NUM_INSPECTIONS = 2;
	// Number of cleaning
	private static final int NUM_CLEANING = 2;
	
	private Map<String, Integer> inspectionMap;
	private Map<String, Integer> cleaningMap;
	
	public HouseKeeping(String[] cleaningList, String[] inspectionList) {

		inspectionMap = new HashMap<>();
		for (String s : inspectionList) {
			inspectionMap.put(s, 0);
		}

		cleaningMap = new HashMap<>();
		for (String s : cleaningList) {
			cleaningMap.put(s, 0);
		}
	}

	/**
	 * Resets all the cleaning tasks to back zero.
	 */
	public void resetCleaning() {
		Map<String, Integer> newMap = new HashMap<>();
		Iterator<Map.Entry<String, Integer>> i = cleaningMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Integer> entry = i.next();
			String s = entry.getKey();
			i.remove();
			newMap.put(s, 0);
		}
		cleaningMap.putAll(newMap);
//		cleaningMap.replaceAll((s, v) -> 0);
	}

	/**
	 * Resets all the cleaning tasks to back zero.
	 */
	public void resetInspected() {
		inspectionMap.replaceAll((s, v) -> 0);
	}
	
	/**
	 * Gets list of items not inspected.
	 * 
	 * @return
	 */
	public List<String> getUninspected() {
		List<String> uninspected = new ArrayList<>();
		for (Entry<String, Integer> s : inspectionMap.entrySet()) {
			if (s.getValue() < NUM_INSPECTIONS)
				uninspected.add(s.getKey());
		}
		return uninspected;
	}

	/**
	 * Gets a list of items not cleaned.
	 * 
	 * @return
	 */
	public List<String> getUncleaned() {
		List<String> uncleaned = new ArrayList<>();
		for (Entry<String, Integer> s : cleaningMap.entrySet()) {
			if (s.getValue() < NUM_CLEANING)
				uncleaned.add(s.getKey());
		}
		return uncleaned;
	}

	/**
	 * A item has been inspected.
	 * 
	 * @param s
	 */
	public void inspected(String s) {
		inspectionMap.put(s, inspectionMap.get(s) + 1);
	}

	/**
	 * An item has been cleaned.
	 * 
	 * @param s
	 */
	public void cleaned(String s) {
		cleaningMap.put(s, cleaningMap.get(s) + 1);
	}

}
