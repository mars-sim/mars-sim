package org.mars_sim.msp.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class logs a list of items for each Sol.
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
	protected List<T> getDataItem() {
		return new ArrayList<T>();
	}

	/**
	 * Add an item to teh current sol list
	 * @param item
	 */
	public void addData(T item) {
		updating();
		currentData.add(item);
	}
}
