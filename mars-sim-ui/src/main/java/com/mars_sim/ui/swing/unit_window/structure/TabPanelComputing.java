/*
 * Mars Simulation Project
 * TabPanelComputing.java
 * @date 2024-07-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;

/**
 * This is a tab panel for settlement's computing capability.
 */
@SuppressWarnings("serial")
class TabPanelComputing extends EntityTableTabPanel<Settlement> 
				implements TemporalComponent{

	// default logger.

	private static final String COMPUTING_ICON = "computing";
	private static final String CU = " CUs";
	private static final String SLASH = " / ";
	private static final String KW = " kW";
	
	private JLabel powerloadsLabel;
	private JLabel percentUsageLabel;
	private JLabel cULabel;
	private JLabel entropyLabel;
	
	private BuildingManager manager;

	private TableModel tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelComputing(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelComputing.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(COMPUTING_ICON), null,
			unit, context
		);
		manager = unit.getBuildingManager();
	}
	
	@Override
	protected JPanel createInfoPanel() {
		// Prepare heat info panel.
		AttributePanel springPanel = new AttributePanel(4);

		// Total Power Demand
		double[] powerLoads = manager.getTotalCombinedLoads();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		powerloadsLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.powerload"),
				twoLoads, Msg.getString("BuildingPanelComputation.powerload.tooltip"));

		// Total Usage
		double[] combined = manager.getPeakCurrentPercent();	
		// Get the peak total
		double peak = Math.round(combined[1] * 10.0)/10.0;		
		// Get the total CUs available
		double cUs = Math.round(combined[0] * 10.0)/10.0;
		// Get total usage
		double usage = Math.round((peak - cUs)/peak * 1000.0)/10.0;

		String text = cUs + SLASH + peak + CU;
		
		percentUsageLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.usage"),
					 			StyleManager.DECIMAL1_PERC.format(usage), Msg.getString("BuildingPanelComputation.usage.tooltip"));

		cULabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.computingUnit"),
				text, Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	
		// Total Entropy
		double entropy = manager.getTotalEntropy();
		entropyLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.entropy"),
	 			Math.round(entropy * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelComputation.entropy.tooltip"));	
		
		return springPanel;
	}
	
	/**
	 * Create a table model that shows the comuting details of the Buildings
	 * in the settlement.
	 * 
	 * @return Table model.
	 */
	protected TableModel createModel() {
		tableModel = new TableModel(getEntity());

		return tableModel;
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Total Power Demand
		double[] powerLoads = manager.getTotalCombinedLoads();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		if (!powerloadsLabel.getText().equals(twoLoads))
			powerloadsLabel.setText(twoLoads);
		
		// Total Usage
		double[] combined = manager.getPeakCurrentPercent();	
		// Get the peak total
		double peak = Math.round(combined[1] * 10.0)/10.0;		
		// Get the total CUs available
		double cUs = Math.round(combined[0] * 10.0)/10.0;
		// Get total usage
		double usage = Math.round((peak - cUs)/peak * 1000.0)/10.0;
		
		String text = cUs + SLASH + peak + CU;
		
		percentUsageLabel.setText(StyleManager.DECIMAL1_PERC.format(usage));
		
		if (!cULabel.getText().equalsIgnoreCase(text))
			cULabel.setText(text);
		
		String entropy = Math.round(manager.getTotalEntropy() * 1_000.0)/1_000.0 + "";
		
		if (!entropyLabel.getText().equalsIgnoreCase(entropy))
			entropyLabel.setText(entropy);
		
		// Update  table.
		tableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private static class TableModel extends BaseBuildingModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final int KWE_VAL = 101;
		private static final int KWT_VAL = 102;
		private static final int COOLING_VAL = 103;
		private static final int UTILIZATION_VAL = 104;
		private static final int CUS_VAL = 105;
		private static final int ENTROPY_VAL = 106;

		private static final EntityColumnSpec KWE = new EntityColumnSpec(new ColumnSpec(KWE_VAL, "kWe", Double.class), null);
		private static final EntityColumnSpec KWT = new EntityColumnSpec(new ColumnSpec(KWT_VAL, "kWt", Double.class), null);
		private static final EntityColumnSpec COOLING = new EntityColumnSpec(new ColumnSpec(COOLING_VAL, "Cooling", Double.class), null);
		private static final EntityColumnSpec UTILIZATION = new EntityColumnSpec(new ColumnSpec(UTILIZATION_VAL, "% Util", Double.class), null);
		private static final EntityColumnSpec CUS = new EntityColumnSpec(new ColumnSpec(CUS_VAL, "CUs", String.class, ColumnSpec.STYLE_RIGHT), null);
		private static final EntityColumnSpec ENTROPY = new EntityColumnSpec(new ColumnSpec(ENTROPY_VAL, "Entropy", Double.class), null);

		private List<Building> buildings;

		private TableModel(Settlement settlement) {
			super(NAME, KWE, KWT, COOLING, UTILIZATION, CUS, ENTROPY);
			buildings = settlement.getBuildingManager().getBuildings(FunctionType.COMPUTATION);
			setEntities(buildings);
		}

		@Override
		protected Object getEntityValue(Building b, int valueIndex) {
			return switch (valueIndex) {
				case KWE_VAL -> Math.round(b.getComputation().getFullPowerLoad() * 10.0) / 10.0;
				case KWT_VAL -> Math.round(b.getComputation().getInstantHeatGenerated() * 10.0) / 10.0;
				case COOLING_VAL -> Math.round(b.getComputation().getInstantCoolingLoad() * 10.0) / 10.0;
				case UTILIZATION_VAL -> Math.round(b.getComputation().getUsagePercent() * 10.0) / 10.0;
				case CUS_VAL -> {
					double peak = Math.round(b.getComputation().getPeakCU() * 100.0) / 100.0;
					double computingUnit = Math.round(b.getComputation().getCurrentCU() * 100.0) / 100.0;
					yield computingUnit + SLASH + peak;
				}
				case ENTROPY_VAL -> Math.round(b.getComputation().getEntropy() * 1_000.0) / 1_000.0;
				default -> super.getEntityValue(b, valueIndex);
			};
		}

		public void update() {
			fireTableDataChanged();
		}
	}
}
