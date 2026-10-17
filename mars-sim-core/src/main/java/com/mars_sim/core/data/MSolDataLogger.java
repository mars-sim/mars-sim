/*
 * Mars Simulation Project
 * MSolDataLogger.java
 * @date 2022-07-28
 * @author Barry Evans
 */

package com.mars_sim.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * This logger records data in a DataLogger for each Sol. Datapoint is timstamped with the msol value as well.
 */
public class MSolDataLogger<T> extends DataLogger<List<MSolDataItem<T>>> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new logger.
	 * 
	 * @param maxSols
	 */
	public MSolDataLogger(int maxSols) {
		super(maxSols);
	}
	
	@Override
	protected List<MSolDataItem<T>> getNewDataItem() {
		return new ArrayList<MSolDataItem<T>>();
	}


	/**
	 * Adds a datapoint at the current time & sol.
	 * 
	 * @param data Item to add.
	 */
	public void addDataPoint(T data) {
		MSolDataItem<T> item = new MSolDataItem<T>(currentMillisol, data);

		updating();
		currentData.add(item);
	}

	/**
	 * Visitor pattern for iterating over all data points with their corresponding Sol.
	 * 
	 * @param visitor A BiConsumer that accepts the Sol (Integer) and the data item (T).
	 */
	public void visit(BiConsumer<Integer,T> visitor) {	
		for (int sol: getHistory().keySet()) {
			List<MSolDataItem<T>> list = getHistory().get(sol);
			for (MSolDataItem<T> s: list) {
				visitor.accept(sol, s.getData());
			}
		}
	}
}
