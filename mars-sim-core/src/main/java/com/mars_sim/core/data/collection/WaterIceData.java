/*
 * Mars Simulation Project
 * WaterIceData.java
 * @date 2026-09-19
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import com.mars_sim.core.time.MarsTime;

public class WaterIceData extends FieldDataSet {

	public WaterIceData(DataType dataType, MarsTime timeCollected, int initialQuality) {
		super(dataType, timeCollected, initialQuality);
	}

}
