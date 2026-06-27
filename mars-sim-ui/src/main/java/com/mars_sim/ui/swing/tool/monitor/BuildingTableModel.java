/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2024-07-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;

/**
 * The BuildingTableModel maintains a list of Building objects.
 */
@SuppressWarnings("serial")
class BuildingTableModel extends BaseBuildingModel implements MonitorModel {

	private static final String BUILDINGS = Msg.getString("building.plural");

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel() {
		super(NAME, SETTLEMENT, TYPE, CATEGORY, PWR_MODE, PWR_REQ, PWR_GEN, TEMP);
	}

	/**
	 * Get the index of the settlement column.
	 */
	@Override
	public int getSettlementColumn() {
		return 1;
	}

	@Override
	public String getName() {
		return BUILDINGS;
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {

		var newBuildings = filter.stream()
				.flatMap(s -> s.getBuildingManager().getBuildingSet().stream())
				.toList();
	
		setEntities(newBuildings);

		return true;
	}
}
