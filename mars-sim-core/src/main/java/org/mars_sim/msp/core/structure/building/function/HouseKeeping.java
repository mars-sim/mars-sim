package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HouseKeeping {

	// Number of inspections
	private static final int NUM_INSPECTIONS = 2;
	// Number of cleaning
	private static final int NUM_CLEANING = 2;
	
	private Map<String, Integer> inspectionMap;
	private Map<String, Integer> cleaningMap;
	
	public HouseKeeping(String[] cleaningList, String[] inspectionList) {

		inspectionMap = new HashMap<String, Integer>();
		for (String s : inspectionList) {
			inspectionMap.put(s, 0);
		}

		cleaningMap = new HashMap<String, Integer>();
		for (String s : cleaningList) {
			cleaningMap.put(s, 0);
		}
	}

	/**
	 * Reset all the cleaning tasks to back zero
	 */
	public void resetCleaning() {
		for (String s : cleaningMap.keySet()) {
			cleaningMap.put(s, 0);
		}
	}

	/**
	 * Reset all the cleaning tasks to back zero
	 */
	public void resetInspected() {
		for (String s : inspectionMap.keySet()) {
			inspectionMap.put(s, 0);
		}
	}
	
	/**
	 * Get list of items not inspected
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
	 * Get a list of items not cleaned
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
	 * A item has been inspected
	 * @param s
	 */
	public void inspected(String s) {
		inspectionMap.put(s, inspectionMap.get(s) + 1);
	}

	/**
	 * An item has been cleaned
	 * @param s
	 */
	public void cleaned(String s) {
		cleaningMap.put(s, cleaningMap.get(s) + 1);		
	}

}
