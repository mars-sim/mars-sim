/**
 * Mars Simulation Project
 * SolMetricDataLogger.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * This class logs a number increasing metrics as Doubles for each day. The metrics are keyed on a 
 * particular value.
 */
public class SolMetricDataLogger<K> extends DataLogger<Map<K,Double>> {

	public SolMetricDataLogger(int maxSols) {
		super(maxSols);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Map<K,Double> getNewDataItem() {
		return new HashMap<>();
	}

	/**
	 * Increase the metric on one of the data points. It adds the increment to any existing value.
	 * If no value for this metric is present; it created one.
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
	 * Get a metric from the current day's figures.
	 * @param type
	 * @return
	 */
	public Double getDataPoint(K type) {
		return currentData.get(type);
	}
	
	/**
	 * Calculate the daily average for a specific metric.
	 * For the current day the current msol is taken into account to produce an estimate. 
	 * @param metric Metric requested
	 * @return Daily average
	 */
	public double getDailyAverage(K metric) {
		double sum = 0;
		int numSols = 0;

		for (Map<K, Double> oneDay : dailyData) {
			// Get metric for the day; there may not be any
			double dailyTotal = 0;
			if (oneDay.containsKey(metric)) {
				dailyTotal = oneDay.get(metric);
			}

			// First entry is always today
			if (numSols == 0) {
				sum += ((dailyTotal/currentMsol) * 1_000D);
			}
			else {
				sum += dailyTotal;
			}
			numSols++;
		}

		if (numSols == 0) {
			// No data points
			return 0;
		}
		return sum / numSols;
	}
}
