/*
 * Mars Simulation Project
 * SettlementTableModel.java
 * @date 2022-07-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseSettlementModel;

/**
 * The SettlementTableModel that maintains a list of Settlement objects. It maps
 * key attributes of the Settlement into Columns.
 */
@SuppressWarnings("serial")
public class SettlementTableModel extends BaseSettlementModel implements MonitorModel {
	
	private static final String SETTLEMENTS = Msg.getString("settlement.plural");
	
	private static final List<Integer> RESOURCES = List.of(
							ResourceUtil.HYDROGEN_ID,ResourceUtil.METHANE_ID,
							ResourceUtil.METHANOL_ID, ResourceUtil.WATER_ID);

	/**
	 * Constructs a SettlementTableModel model that displays Settlements in the
	 * simulation.
	 */
	public SettlementTableModel() {
		super(NAME, POPULATION, PARKED, MISSION, POWER_GEN, POWER_LOAD, ENERGY_STORED);
		addResourceColumns(RESOURCES);
	}

	@Override
	public String getName() {
		return SETTLEMENTS;
	}

	/**
	 * This model has no switchable Settlement column.
	 */
	@Override
	public int getSettlementColumn() {
		return -1;
	}
	
	/**
	 * Sets the settlement filter for the settlement table.
	 * 
	 * @param filter
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		setEntities(filter);
		return true;
	}
}
