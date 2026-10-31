/*
 * Mars Simulation Project
 * DataManager.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.structure.Settlement;


public class DataManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final String FIELDDATA_ADDED = "fieldDataAdded";

	/** The settlement's data library. */
	private List<FieldDataSet> dataArchive;
	private Settlement owner;
	
	/**
	 * Constructor for a {@link DataManager}.
	 */
	public DataManager(Settlement owner) {
		this.owner = owner;
		this.dataArchive = new ArrayList<>();
	}
	
	public void addFieldData(FieldDataSet data) {
		dataArchive.add(data);

		// Notify listeners
		owner.fireUnitUpdate(FIELDDATA_ADDED, data);
	}
	
	/**
	 * Gets the data archive map.
	 * 
	 * @return
	 */
	public List<FieldDataSet> getDataArchive() {
		return dataArchive;
	}

}
