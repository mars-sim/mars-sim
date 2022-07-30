/*
 * Mars Simulation Project
 * SolListDataLogger.java
 * @date 2022-07-30
 * @author Barry Evans
 */

package org.mars_sim.msp.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class logs a list of items for each sol.
 */
public class SolListDataLogger<T> extends DataLogger<List<T>> {

	public SolListDataLogger(int maxSols) {
		super(maxSols);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected List<T> getNewDataItem() {
		return new ArrayList<T>();
	}

	/**
	 * Add an item to the current sol list
	 * @param item
	 */
	public void addData(T item) {
		updating();
		currentData.add(item);
	}
}
