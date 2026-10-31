/*
 * Mars Simulation Project
 * TabPanelData.java
 * @date 2026-10-13
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseDataModel;


/**
 * This is a tab panel for a settlement's database.
 */
class TabPanelData extends EntityTableTabPanel<Settlement> implements TemporalComponent {

	private static final String DATA_ICON = "data";


	// Data cache
	private JIntegerLabel numLocalLabel;
	private JIntegerLabel numRemoteLabel;
	
	/** Table model for data. */
	private TableModel tableModel;

	private Settlement settlement;

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
		return tableModel;
	}

	/**
	 * Customises the data table's column details such as width and cell renderers.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(5);
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
	private class TableModel extends BaseDataModel {
				
		private TableModel() {
			super(ID, TYPE, TIME, QUALITY, OWNER, WORKTIME, COORD);

			setEntities(settlement.getFieldDataManager().getDataArchive());
		}

		public void update() {
			setEntities(settlement.getFieldDataManager().getDataArchive());
		}
	}
}