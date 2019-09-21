/**
 * Mars Simulation Project
 * TradeMissionCustomInfoPanel.java
 * @version 3.1.0 2017-11-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.structure.goods.Good;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

/**
 * A panel for displaying trade mission information.
 */
@SuppressWarnings("serial")
public class TradeMissionCustomInfoPanel extends MissionCustomInfoPanel {

	// Data members.
	private Trade mission;
	private SellingGoodsTableModel sellingGoodsTableModel;
	private WebLabel desiredGoodsProfitLabel;
	private DesiredGoodsTableModel desiredGoodsTableModel;
	private WebLabel boughtGoodsProfitLabel;
	private BoughtGoodsTableModel boughtGoodsTableModel;

	/**
	 * Constructor.
	 */
	public TradeMissionCustomInfoPanel() {
		// Use JPanel constructor
		super();

		// Set the layout.
		setLayout(new GridLayout(3, 1));

		// Create the selling goods panel.
		WebPanel sellingGoodsPane = new WebPanel(new BorderLayout());
		add(sellingGoodsPane);

		// Create the selling goods label.
		WebLabel sellingGoodsLabel = new WebLabel("Goods to Sell:", WebLabel.LEFT);
		sellingGoodsPane.add(sellingGoodsLabel, BorderLayout.NORTH);

		// Create a scroll pane for the selling goods table.
		WebScrollPane sellingGoodsScrollPane = new WebScrollPane();
		sellingGoodsScrollPane.setPreferredSize(new Dimension(-1, -1));
		sellingGoodsPane.add(sellingGoodsScrollPane, BorderLayout.CENTER);

		// Create the selling goods table and model.
		sellingGoodsTableModel = new SellingGoodsTableModel();
		WebTable sellingGoodsTable = new WebTable(sellingGoodsTableModel);
		sellingGoodsScrollPane.setViewportView(sellingGoodsTable);

		// Create the desired goods panel.
		WebPanel desiredGoodsPane = new WebPanel(new BorderLayout());
		add(desiredGoodsPane);

		// Create the desired goods label panel.
		WebPanel desiredGoodsLabelPane = new WebPanel(new GridLayout(1, 2, 0, 0));
		desiredGoodsPane.add(desiredGoodsLabelPane, BorderLayout.NORTH);

		// Create the desired goods label.
		WebLabel desiredGoodsLabel = new WebLabel("Desired Goods to Buy:", WebLabel.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsLabel);

		// Create the desired goods profit label.
		desiredGoodsProfitLabel = new WebLabel("Profit:", WebLabel.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsProfitLabel);

		// Create a scroll pane for the desired goods table.
		WebScrollPane desiredGoodsScrollPane = new WebScrollPane();
		desiredGoodsScrollPane.setPreferredSize(new Dimension(-1, -1));
		desiredGoodsPane.add(desiredGoodsScrollPane, BorderLayout.CENTER);

		// Create the desired goods table and model.
		desiredGoodsTableModel = new DesiredGoodsTableModel();
		WebTable desiredGoodsTable = new WebTable(desiredGoodsTableModel);
		desiredGoodsScrollPane.setViewportView(desiredGoodsTable);

		// Create the bought goods panel.
		WebPanel boughtGoodsPane = new WebPanel(new BorderLayout());
		add(boughtGoodsPane);

		// Create the bought goods label panel.
		WebPanel boughtGoodsLabelPane = new WebPanel(new GridLayout(1, 2, 0, 0));
		boughtGoodsPane.add(boughtGoodsLabelPane, BorderLayout.NORTH);

		// Create the bought goods label.
		WebLabel boughtGoodsLabel = new WebLabel("Goods Bought:", WebLabel.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsLabel);

		// Create the bought goods profit label.
		boughtGoodsProfitLabel = new WebLabel("Profit:", WebLabel.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsProfitLabel);

		// Create a scroll pane for the bought goods table.
		WebScrollPane boughtGoodsScrollPane = new WebScrollPane();
		boughtGoodsScrollPane.setPreferredSize(new Dimension(-1, -1));
		boughtGoodsPane.add(boughtGoodsScrollPane, BorderLayout.CENTER);

		// Create the bought goods table and model.
		boughtGoodsTableModel = new BoughtGoodsTableModel();
		WebTable boughtGoodsTable = new WebTable(boughtGoodsTableModel);
		boughtGoodsScrollPane.setViewportView(boughtGoodsTable);
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		if (e.getType() == MissionEventType.BUY_LOAD_EVENT) {
			boughtGoodsTableModel.updateTable();
			updateBoughtGoodsProfit();
		}
	}

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof Trade) {
			this.mission = (Trade) mission;
			sellingGoodsTableModel.updateTable();
			desiredGoodsTableModel.updateTable();
			boughtGoodsTableModel.updateTable();
			updateDesiredGoodsProfit();
			updateBoughtGoodsProfit();
		}
	}

	/**
	 * Updates the desired goods profit label.
	 */
	private void updateDesiredGoodsProfit() {
		int profit = (int) mission.getDesiredProfit();
		desiredGoodsProfitLabel.setText("Profit: " + profit + " VP");
	}

	/**
	 * Updates the bought goods profit label.
	 */
	private void updateBoughtGoodsProfit() {
		int profit = (int) mission.getProfit();
		boughtGoodsProfitLabel.setText("Profit: " + profit + " VP");
	}

	/**
	 * Abstract model for a goods table.
	 */
	private abstract static class GoodsTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		protected Map<Good, Integer> goodsMap;
		protected List<Good> goodsList;

		/**
		 * Constructor.
		 */
		private GoodsTableModel() {
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
			if (columnIndex == 0) return "Good";
			else return "Amount";
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param row the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = "unknown";

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
		protected abstract void updateTable();
	}

	/**
	 * Model for the selling goods table.
	 */
	private class SellingGoodsTableModel
	extends GoodsTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * hidden Constructor.
		 */
		private SellingGoodsTableModel() {
			// Use GoodsTableModel constructor.
			super();
		}

		@Override
		protected void updateTable() {
			if (mission.getSellLoad() != null) {
				goodsMap = mission.getSellLoad();
				goodsList = new ArrayList<Good>(goodsMap.keySet());
				Collections.sort(goodsList);
			}
			else {
				goodsMap.clear();
				goodsList.clear();
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Model for the desired goods table.
	 */
	private class DesiredGoodsTableModel
	extends GoodsTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * hidden Constructor.
		 */
		private DesiredGoodsTableModel() {
			// Use GoodsTableModel constructor.
			super();
		}

		@Override
		protected void updateTable() {
			if (mission.getDesiredBuyLoad() != null) {
				goodsMap = mission.getDesiredBuyLoad();
				goodsList = new ArrayList<Good>(goodsMap.keySet());
				Collections.sort(goodsList);
			}
			else {
				goodsMap.clear();
				goodsList.clear();
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Model for the bought goods table.
	 */
	private class BoughtGoodsTableModel
	extends GoodsTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * hidden Constructor.
		 */
		private BoughtGoodsTableModel() {
			// Use GoodsTableModel constructor.
			super();
		}

		@Override
		protected void updateTable() {
			if (mission.getBuyLoad() != null) {
				goodsMap = mission.getBuyLoad();
				goodsList = new ArrayList<Good>(goodsMap.keySet());
				Collections.sort(goodsList);
			}
			else {
				goodsMap.clear();
				goodsList.clear();
			}
			fireTableDataChanged();
		}
	}
}