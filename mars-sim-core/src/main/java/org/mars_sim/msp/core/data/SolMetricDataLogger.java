package org.mars_sim.msp.core.data;

import java.util.HashMap;
import java.util.Map;

/**
 * This class logs a number increasing metrics as Doubles for each day. The metrics are keeyed on a 
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
	protected Map<K,Double> getDataItem() {
		return new HashMap<>();
	}

	/**
	 * Increase the metric on one of the data points
	 */
	public void updateDataPoint(K metric, Double newValue) {
		updating();
		
		Double stored = currentData.get(metric);
		
		double current = (stored == null ? 0 : stored);
		current += newValue;
		currentData.put(metric, current);
	}

	/**
	 * Gte a metric from the current day's figures.
	 * @param type
	 * @return
	 */
	public Double getDataPoint(K type) {
		return currentData.get(type);
	}
}
