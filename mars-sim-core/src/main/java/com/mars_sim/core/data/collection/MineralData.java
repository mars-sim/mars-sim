/*
 * Mars Simulation Project
 * MineralData.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import com.mars_sim.core.time.MarsTime;

public class MineralData extends FieldDataSet {

	public MineralData(DataType dataType, MarsTime timeCollected, int initialQuality) {
		super(dataType, timeCollected, initialQuality);
	}
}
