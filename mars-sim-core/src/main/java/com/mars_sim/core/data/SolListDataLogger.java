/*
 * Mars Simulation Project
 * SolListDataLogger.java
 * @date 2022-07-30
 * @author Barry Evans
 */

package com.mars_sim.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class logs a list of items for each sol.
 */
public class SolListDataLogger<T> extends DataLogger<List<T>> {

	private static final long serialVersionUID = 1L;

	public SolListDataLogger(int maxSols) {
		super(maxSols);
	}

	@Override
	protected List<T> getNewDataItem() {
		return new ArrayList<T>();
	}

	/**
	 * Adds an item to the current sol list.
	 * 
	 * @param item
	 */
	public void addData(T item) {
		updating();
		currentData.add(item);
	}
	
	/**
	 * Gets the average data value.
	 * 
	 * @return
	 */
	public double getAverageDataValue() {
		if (currentData == null) {
			return 0;
		}
		if (currentData.isEmpty()) {
			return 0;
		}
		
//		int count = 0;
//		double sum = 0;
//		
//		for (double sol: getHistory().keySet()) {
//			List<MSolDataItem<Integer>> powers = getHistory().get(sol);
//			for (MSolDataItem<Integer> s: powers) {
//				count++;
//				sum += s.getData();
//			}
//		}
//		
//		if (count > 0 && sum > 0)
//			return sum / count;
		
		double sum = 0;
		int num = 0;
		int size = currentData.size();
		for (int i = 0; i < size; i++) {
			double value = (double) currentData.get(i);
			sum += (double)currentData.get(i);
			num++;
		}
		
		if (num == 0)
			return 0;
		
		return sum/num;
	}
}
