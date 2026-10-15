/*
 * Mars Simulation Project
 * TabPanelData.java
 * @date 2026-10-13
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.collection.DataManager;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.tool.monitor.DataTableModel;


/**
 * This is a tab panel for a settlement's database.
 */
class TabPanelData extends EntityTableTabPanel<Settlement> implements TemporalComponent {

	private static final String DATA_ICON = "data";

	private static final String ALL = "All";

	// Data cache
	private JIntegerLabel numLocalLabel;
	private JIntegerLabel numRemoteLabel;
	
	/** Table model for data. */
	private TableModel tableModel;

	private Settlement settlement;
	private DataManager manager;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelData(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelData.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(DATA_ICON), null,
			settlement, context
		);
		
		this.settlement = settlement;
	}
	
	/**
	 * Shows the main power details.
	 */
	@Override
	protected JPanel createInfoPanel() {

		manager = Simulation.instance().getDataManager();

		JPanel topContentPanel = new JPanel(new BorderLayout());

		// Prepare spring layout power info panel.
		AttributePanel infoPanel = new AttributePanel();
		topContentPanel.add(infoPanel);

		// Create labels
		numLocalLabel = new JIntegerLabel();
		infoPanel.addLabelledItem("# of Local Data Collection Sites", numLocalLabel);
		numRemoteLabel = new JIntegerLabel();
		infoPanel.addLabelledItem("# of Remote Data Collection Sites", numRemoteLabel);
		
		return topContentPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare data table model.
		tableModel = new TableModel();
		setSelection(ALL);
		return tableModel;
	}

	/**
	 * Customises the data table's column details such as width and cell renderers.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(5);
//		columnModel.getColumn(0).setCellRenderer(new PowerModeCellRenderer());
	}

	private void setSelection(String selected) {
		List<FieldDataSet> oldList = getFieldDataList(); 
		
		if (oldList == null) {
			oldList = Collections.emptyList();
		}
		
//		List<FieldDataSet> lists = switch(selected) {
//		
////			case SETTLEMENT -> ...);
//		
//			default -> Collections.emptyList();
//		};
		
		tableModel.setEntities(oldList);
	}

	/**
	 * Gets a list of settlement's field data.
	 *
	 * @return list of field data
	 */
	private List<FieldDataSet> getFieldDataList() {
		return manager.getDataArchive().get(settlement);
	}

	/**
	 * Values are always updated on the Clock pulse as there are so many small attributes.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		int[] array = settlement.getNumDataCollectionSite();
		int numLocal = array[0];
		int numRemote = array[1];
		numLocalLabel.setValue(numLocal);
		numRemoteLabel.setValue(numRemote);
		
		// Update data table.
		tableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	@SuppressWarnings("serial")
	private class TableModel extends DataTableModel {
		
		private TableModel() {
			super(manager);
		}

		@Override
		protected Object getEntityValue(FieldDataSet entity, int valueIndex) {
			 
			return switch (valueIndex) {

			default -> super.getEntityValue(entity, valueIndex);
			};
		}

		/**
		 * Gets tooltip for status column to show the power mode name.
		 */
		@Override
		protected String getEntityDescription(FieldDataSet entity, int valueIndex) {
//			if (valueIndex == QUALITY_VAL) {
//				return "The quality of this dataset";
//			}
			
			return super.getEntityDescription(entity, valueIndex);
		}

		public void update() {
			setSelection(ALL);
			
			if (getRowCount() == 0)
				return;
			
			fireTableRowsUpdated(0, getRowCount() - 1);
		}
	}
	
	/**
	 * Remove the listener for new ScientificStudies when the panel is destroyed.
	 */
	@Override
	public void destroy() {
//		getContext().getSimulation().getDataManager().removeListener(this);

		if (tableModel != null) {
			tableModel.release();
		}
		super.destroy();
	}

}