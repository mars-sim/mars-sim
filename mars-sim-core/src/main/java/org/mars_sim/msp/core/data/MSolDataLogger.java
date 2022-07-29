/*
 * Mars Simulation Project
 * MSolDataLogger.java
 * @date 2022-07-28
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.io.Serializable;

/**
 * This logger records data in a DataLogger for each Sol. Datapoint is timstamped with the msol value as well.
 */
public class MSolDataLogger<T> extends SolListDataLogger<MSolDataItem<T>>
	implements Serializable {

	/**
	 * Creates a new logger.
	 * 
	 * @param maxSols
	 */
	public MSolDataLogger(int maxSols) {
		super(maxSols);
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Adds a datapoint at the current time & sol.
	 * 
	 * @param data Item to add.
	 */
	public void addDataPoint(T data) {
		MSolDataItem<T> item = new MSolDataItem<T>(currentMsol, data);
		super.addData(item);
	}
}
