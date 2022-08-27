/**
 * Mars Simulation Project
 * SolSingleMetricDataLogger.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

/**
 * This class records a single value per sol.
 */
public class SolSingleMetricDataLogger extends DataLogger<Double> {

	private static final long serialVersionUID = 1L;
	
	public SolSingleMetricDataLogger(int maxSols) {
		super(maxSols);
	}
	
	/**
	 * The generated item is null to start with.
	 */
	@Override
	protected Double getNewDataItem() {
		return 0D;
	}

	/**
	 * Increases the metric on one of the data points. It adds the increment to any existing value.
	 * If no value for this metric is present; it created one.
	 * 
	 * @param increment Value to add to the existing metric.
	 */
	public void increaseDataPoint(Double increment) {
		updating();
		
		double current = (currentData == null ? 0 : currentData);
		current += increment;
		currentData = current;
		
		// For a single dataitem the item needs to be refreshed
		// because current data is not by reference
		dailyData.set(0, currentData);
	}
	
	/**
	 * Calculates the daily average for the metric.
	 * For the current day the current msol is taken into account to produce an estimate. 
	 * 
	 * @return Daily average
	 */
	public double getDailyAverage() {
		double sum = 0;
		int numSols = 0;

		for (Double dailyTotal : dailyData) {
	
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
