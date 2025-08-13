/*
 * Mars Simulation Project
 * SolMetricDataLogger.java
 * @date 2025-08-13
 * @author Barry Evans
 */

package com.mars_sim.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * This class logs a number increasing metrics as Doubles for each day. The metrics are keyed on a 
 * particular value.
 */
public class SolMetricDataLogger<K> extends DataLogger<Map<K,Double>> {

	private static final long serialVersionUID = 1L;
	
	public SolMetricDataLogger(int maxSols) {
		super(maxSols);
	}


	@Override
	protected Map<K,Double> getNewDataItem() {
		return new HashMap<>();
	}

	/**
	 * Increases the metric on one of the data points. It adds the increment to any existing value.
	 * If no value for this metric is present; it created one.
	 * 
	 * @param increment Value to add to the existing metric.
	 */
	public void increaseDataPoint(K metric, Double increment) {
		updating();
		
		Double stored = currentData.get(metric);
		
		double current = (stored == null ? 0 : stored);
		current += increment;
		currentData.put(metric, current);
	}

	/**
	 * Gets a metric from the current day's figures.
	 * 
	 * @param type
	 * @return
	 */
	public Double getDataPoint(K type) {
		return currentData.get(type);
	}
	
	/**
	 * Calculates the daily average for a specific metric.
	 * For the current day the current msol is taken into account to produce an estimate. 
	 * 
	 * @param metric Metric requested
	 * @return Daily average
	 */
	public double getDailyAverage(K metric) {
		double sum = 0;
		int numSols = 0;

		if (dailyData.size() == 0) {
			// No data points
			return 0;
		}
		
		else if (dailyData.size() == 1) {
			Map<K, Double> oneDay = getTodayData();
			// Get metric for the day; there may not be any
			double dailyTotal = 0.0;
			if (oneDay.containsKey(metric)) {
				dailyTotal = oneDay.get(metric);
			}
			
			// If there's only one sol of data ,
			// then it's a partial sum
			return dailyTotal * currentMillisol / 1_000;
		}
		
		else {
			for (Map<K, Double> oneDay : dailyData) {
				// Get metric for the day; there may not be any
				double dailyTotal = 0.0;
				if (oneDay.containsKey(metric)) {
					dailyTotal = oneDay.get(metric);
				}

				sum += dailyTotal;
				numSols++;
			}
					
			return sum / numSols;
		}
	}
	
	/**
	 * Calculates the cumulative total for a specific metric.
	 * For the current day the current msol is taken into account to produce an estimate. 
	 * 
	 * @param metric Metric requested
	 * @return cumulative total
	 */
	public double getCumulativeTotal(K metric) {
		double sum = 0;

		if (dailyData.size() == 0) {
			// No data points
			return 0;
		}
		
		else if (dailyData.size() == 1) {
			Map<K, Double> oneDay = getTodayData();
			// Get metric for the day; there may not be any
			double dailyTotal = 0.0;
			if (oneDay.containsKey(metric)) {
				dailyTotal = oneDay.get(metric);
			}

			return dailyTotal;
		}
		
		else {
			for (Map<K, Double> oneDay : dailyData) {
				// Get metric for the day; there may not be any
				double dailyTotal = 0.0;
				if (oneDay.containsKey(metric)) {
					dailyTotal = oneDay.get(metric);
				}

				sum += dailyTotal;
			}
			
			return sum;
		}
	}
	
	/**
	 * Returns both the cumulative total and the daily average. for a specific metric.
	 * For the current day the current msol is taken into account to produce an estimate. 
	 * 
	 * @param metric Metric requested
	 * @return double[cumulative total, daily average]
	 */
	public double[] getTotCumulativeDailyAverage(K metric) {
		double sum = 0;
		int numSols = 0;

		if (dailyData.size() == 0) {
			// No data points
			return new double[] {sum, sum};
		}
		
		else if (dailyData.size() == 1) {
			Map<K, Double> oneDay = getTodayData();
			// Get metric for the day; there may not be any
			double dailyTotal = 0.0;
			if (oneDay.containsKey(metric)) {
				dailyTotal = oneDay.get(metric);
			}
			
			return new double[] {dailyTotal, dailyTotal * currentMillisol / 1_000};
		}
		
		else {
			for (Map<K, Double> oneDay : dailyData) {
				// Get metric for the day; there may not be any
				double dailyTotal = 0.0;
				if (oneDay.containsKey(metric)) {
					dailyTotal = oneDay.get(metric);
				}

				sum += dailyTotal;
				numSols++;
			}
			
			return new double[] {sum, sum / numSols};
		}
	}
}
