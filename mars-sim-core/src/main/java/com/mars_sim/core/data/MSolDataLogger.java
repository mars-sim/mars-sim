/*
 * Mars Simulation Project
 * MSolDataLogger.java
 * @date 2022-07-28
 * @author Barry Evans
 */

package com.mars_sim.core.data;

import java.util.List;

/**
 * This logger records data in a DataLogger for each Sol. Datapoint is timstamped with the msol value as well.
 */
public class MSolDataLogger<T> extends SolListDataLogger<MSolDataItem<T>> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new logger.
	 * 
	 * @param maxSols
	 */
	public MSolDataLogger(int maxSols) {
		super(maxSols);
	}
	/**
	 * Adds a datapoint at the current time & sol.
	 * 
	 * @param data Item to add.
	 */
	public void addDataPoint(T data) {
		MSolDataItem<T> item = new MSolDataItem<T>(currentMsol, data);
		super.addData(item);
	}
	
	/**
	 * Gets the average.
	 * 
	 * @return
	 */
	public double getAverageDouble() {
		int count = 0;
		double sum = 0;

		for (int sol: getHistory().keySet()) {
			List<MSolDataItem<T>> list = getHistory().get(sol);
			for (MSolDataItem<T> s: list) {
				count++;
				T item = s.getData();
				sum += convertGenericToDouble(item);
			}
		}
		
		if (count > 0 && sum > 0)
			return sum / count;
		
		return 0;
	}

}
