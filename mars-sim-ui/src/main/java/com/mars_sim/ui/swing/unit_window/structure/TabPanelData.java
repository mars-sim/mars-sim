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
import javax.swing.table.TableModel;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.collection.DataManager;
import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseDataModel;


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
	private DataTableModel dataTableModel;

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
	 * Show the main power details.
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
		dataTableModel = new DataTableModel();
		setSelection(ALL);
		return dataTableModel;
	}

	/**
	 * Customises the data table's column details such as width and cell renderers.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(20);
//		columnModel.getColumn(0).setCellRenderer(new PowerModeCellRenderer());
	}

	private void setSelection(String selected) {
		List<FieldDataSet> lists = switch(selected) {
		
//			case POWER -> ...);
//			case BUILDINGS_WITH_GENERATION -> ...;
//			case BUILDINGS_NO_GENERATION -> ...;
//			case ALL_BUILDINGS -> ...;
		
			default -> Collections.emptyList();
		};
		
		dataTableModel.setEntities(lists);
	}

	/**
	 * Gets a list of settlement's field data.
	 *
	 * @return list of field data
	 */
	private List<FieldDataSet> getFieldDataSetList() {
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
		dataTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private class DataTableModel extends BaseDataModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final int ID_VAL = 301;
		private static final int TYPE_VAL = 302;
		private static final int TIME_VAL = 303;
		private static final int QUALITY_VAL = 304;
		private static final int OWNER_VAL = 305;
		private static final int WORKTIME_VAL = 306;
		private static final int COORD_VAL = 307;
		
		private static final EntityColumnSpec ID = new EntityColumnSpec(
						new ColumnSpec(ID_VAL, Msg.getString("TabPanelData.column.id"), Integer.class), null);
		private static final EntityColumnSpec TYPE = new EntityColumnSpec(
						new ColumnSpec(TYPE_VAL, Msg.getString("TabPanelData.column.type"), String.class), null);
		private static final EntityColumnSpec TIME = new EntityColumnSpec(
						new ColumnSpec(TIME_VAL, Msg.getString("TabPanelData.column.time"), MarsTime.class), null);
		private static final EntityColumnSpec QUALITY = new EntityColumnSpec(
						new ColumnSpec(QUALITY_VAL, Msg.getString("TabPanelData.column.quality"), Integer.class), null);
		private static final EntityColumnSpec OWNER = new EntityColumnSpec(
						new ColumnSpec(OWNER_VAL, Msg.getString("TabPanelData.column.owner"), String.class), null);
		private static final EntityColumnSpec WORKTIME = new EntityColumnSpec(
						new ColumnSpec(WORKTIME_VAL, Msg.getString("TabPanelData.column.workTime"), Double.class), null);
		private static final EntityColumnSpec COORD = new EntityColumnSpec(
						new ColumnSpec(COORD_VAL, Msg.getString("TabPanelData.column.coordinates"), Coordinates.class), null);
		
		
		private DataTableModel() {
			super(ID, TYPE, TIME, QUALITY, OWNER, WORKTIME, COORD);
		}

		@Override
		protected Object getEntityValue(FieldDataSet entity, int valueIndex) {
			 
			return switch (valueIndex) {
			case ID_VAL -> entity.getIdentifier();
            case TYPE_VAL -> entity.getDataType().toString();
            case QUALITY_VAL -> entity.getQuality();
            case OWNER_VAL -> entity.getOwner().getName();
            case WORKTIME_VAL -> entity.getWorkTime();
			case COORD_VAL -> entity.getCoordinates();

			default -> super.getEntityValue(entity, valueIndex);
			};
		}

//		/**
//		 * Gets tooltip for status column to show the power mode name.
//		 */
//		@Override
//		protected String getEntityDescription(Building building, int valueIndex) {
////			if (valueIndex == STATUS_VAL) {
////				return building.getPowerMonitor().getPowerMode().getName();
////			}
//			
//			return super.getEntityDescription(building, valueIndex);
//		}

		public void update() {
			if (getRowCount() == 0)
				return;
			fireTableRowsUpdated(0, getRowCount() - 1);
		}
	}
}