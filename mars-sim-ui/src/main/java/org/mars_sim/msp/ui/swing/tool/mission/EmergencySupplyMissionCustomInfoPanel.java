/**
 * Mars Simulation Project
 * EmergencySupplyMissionCustomInfoPanel.java
 * @version 3.1.0 2017-11-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.structure.goods.Good;

import com.alee.laf.label.WebLabel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

/**
 * A panel for displaying emergency supply mission information.
 */
public class EmergencySupplyMissionCustomInfoPanel
extends MissionCustomInfoPanel {

	// Data members.
	private EmergencySupply mission;
	private EmergencySuppliesTableModel emergencySuppliesTableModel;

	/**
	 * Constructor.
	 */
	public EmergencySupplyMissionCustomInfoPanel() {
		// Use JPanel constructor
		super();

		// Set the layout.
		setLayout(new BorderLayout());

		// Create the emergency supplies label.
		WebLabel emergencySuppliesLabel = new WebLabel(Msg.getString("EmergencySupplyMissionCustomInfoPanel.emergencySupplies"), WebLabel.LEFT); //$NON-NLS-1$
		add(emergencySuppliesLabel, BorderLayout.NORTH);

		// Create a scroll pane for the emergency supplies table.
		WebScrollPane emergencySuppliesScrollPane = new WebScrollPane();
		emergencySuppliesScrollPane.setPreferredSize(new Dimension(-1, -1));
		add(emergencySuppliesScrollPane, BorderLayout.CENTER);

		// Create the emergency supplies table and model.
		emergencySuppliesTableModel = new EmergencySuppliesTableModel();
		WebTable emergencySuppliesTable = new WebTable(emergencySuppliesTableModel);
		emergencySuppliesScrollPane.setViewportView(emergencySuppliesTable);
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		// Do nothing.
	}

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof EmergencySupply) {
			this.mission = (EmergencySupply) mission;
			emergencySuppliesTableModel.updateTable();
		}
	}

	/**
	 * Model for the emergency supplies table.
	 */
	private class EmergencySuppliesTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		protected Map<Good, Integer> goodsMap;
		protected List<Good> goodsList;

		/**
		 * Constructor.
		 */
		private EmergencySuppliesTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Initialize goods map and list.
			goodsList = new ArrayList<Good>();
			goodsMap = new HashMap<Good, Integer>();
		}

		/**
		 * Returns the number of rows in the model.
		 * @return number of rows.
		 */
		public int getRowCount() {
			return goodsList.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("EmergencySupplyMissionCustomInfoPanel.column.good"); //$NON-NLS-1$
			else return Msg.getString("EmergencySupplyMissionCustomInfoPanel.column.amount"); //$NON-NLS-1$
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param row the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = Msg.getString("unknown"); //$NON-NLS-1$

			if (row < goodsList.size()) {
				Good good = goodsList.get(row); 
				if (column == 0) result = good.getName();
				else result = goodsMap.get(good);
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		protected void updateTable() {

			goodsMap = mission.getEmergencySuppliesAsGoods();
			goodsList = new ArrayList<Good>(goodsMap.keySet());
			Collections.sort(goodsList);

			fireTableDataChanged();
		}
	}
}