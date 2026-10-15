/*
 * Mars Simulation Project
 * WaterIceData.java
 * @date 2026-09-19
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public class WaterIceData extends FieldDataSet {

	private static final long serialVersionUID = 1L;

	public WaterIceData(Settlement settlement, Worker owner, DataType dataType, MarsTime timeCollected, int initialQuality) {
		super(settlement, owner, dataType, timeCollected, initialQuality);
	}
}
