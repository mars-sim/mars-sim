/*
 * Mars Simulation Project
 * TradeGoodsPanel.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard panel for selecting trade goods and amounts from a source settlement.
 */
@SuppressWarnings("serial")
class TradeGoodsPanel extends WizardStep<MissionDataBean> {

	public static final String BUY_ID = "buy_goods";
	public static final String SELL_ID = "sell_goods";
	public static final String SUPPLY_ID = "supply_goods";

	private boolean buyGoods;
	private JLabel errorMessageLabel;
	private JTable goodsTable;
	private JTable tradeTable;
	private GoodsTableModel goodsTableModel;
	private TradeTableModel tradeTableModel;
	private JButton leftArrowButton;
	private JButton rightArrowButton;
	private JFormattedTextField amountTextField;
	private NumberFormatter formatter;

	/**
	 * constructor.
	 * @param id the panel ID.
	 * @param wizard the create mission wizard.
	 * @param state the mission data bean.
	 */
	public TradeGoodsPanel(String id, MissionCreate wizard, MissionDataBean state) {
		super(id, wizard);

		this.buyGoods = BUY_ID.equals(id);

		// Set the layout.
		setLayout(new BorderLayout());

		// Create available goods panel.
		JPanel availableGoodsPane = new JPanel(new BorderLayout());
		availableGoodsPane.setPreferredSize(new Dimension(260, 100));
		add(availableGoodsPane, BorderLayout.WEST);

		// Create available goods label.
		var availableGoodsLabel = new JLabel("Available goods at ", SwingConstants.CENTER);
		availableGoodsPane.add(availableGoodsLabel, BorderLayout.NORTH);

		// Create available goods table.
		Settlement seller = (buyGoods) ? state.getDestinationSettlement() : state.getStartingSettlement();

		JScrollPane goodsScrollPane = new JScrollPane();
		availableGoodsPane.add(goodsScrollPane, BorderLayout.CENTER);
		goodsTableModel = new GoodsTableModel(seller);
		goodsTable = new JTable(goodsTableModel);
		goodsTable.setAutoCreateRowSorter(true);
		goodsTable.setRowSelectionAllowed(true);
		goodsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		goodsTable.getSelectionModel().addListSelectionListener(
				e -> {
					if (e.getValueIsAdjusting() && (goodsTable.getSelectedRow() > -1)) {
						tradeTable.clearSelection();
						errorMessageLabel.setText(" ");
						leftArrowButton.setEnabled(false);
						amountTextField.setEnabled(true);
						rightArrowButton.setEnabled(true);
					}
				});	
		goodsScrollPane.setViewportView(goodsTable);

		// Create amount outer panel.
		JPanel amountOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(amountOuterPanel, BorderLayout.CENTER);

		// Create amount text field.
		formatter = new NumberFormatter();
		formatter.setAllowsInvalid(false);
		formatter.setMinimum(0);
		formatter.setMaximum(9999);
		amountTextField = new JFormattedTextField(formatter);
		amountTextField.setValue(0);
		amountTextField.setColumns(5);
		amountTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		amountTextField.setEnabled(false);
		amountOuterPanel.add(amountTextField);


		// Create left arrow button.
		leftArrowButton = new JButton("<");
		leftArrowButton.setEnabled(false);
		leftArrowButton.addActionListener(
				e -> removeGoods()
				);
		amountOuterPanel.add(leftArrowButton);

		// Create right arrow button.
		rightArrowButton = new JButton(">");
		rightArrowButton.setEnabled(false);
		rightArrowButton.addActionListener(
			e -> addGoods()
		);
		amountOuterPanel.add(rightArrowButton);

		// Create traded goods panel.
		JPanel tradedGoodsPane = new JPanel(new BorderLayout());
		tradedGoodsPane.setPreferredSize(new Dimension(260, 100));
		add(tradedGoodsPane, BorderLayout.EAST);

		// Create traded goods label.
		JLabel tradedGoodsLabel = new JLabel("Selling Goods", SwingConstants.CENTER);
		if (buyGoods) tradedGoodsLabel.setText("Buying Goods");
		tradedGoodsPane.add(tradedGoodsLabel, BorderLayout.NORTH);

		// Create traded goods table.
		JScrollPane tradeScrollPane = new JScrollPane();
		tradedGoodsPane.add(tradeScrollPane, BorderLayout.CENTER);
		tradeTableModel = new TradeTableModel();
		tradeTable = new JTable(tradeTableModel);
		tradeTable.setAutoCreateRowSorter(true);
		tradeTable.setRowSelectionAllowed(true);
		tradeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tradeTable.getSelectionModel().addListSelectionListener(
				e -> {
					if (e.getValueIsAdjusting() && (tradeTable.getSelectedRow() > -1)) {
						goodsTable.clearSelection();
						errorMessageLabel.setText(" ");
						leftArrowButton.setEnabled(true);
						amountTextField.setEnabled(true);
						rightArrowButton.setEnabled(false);
					}
				});	
		tradeScrollPane.setViewportView(tradeTable);

		// Create the message label.
		errorMessageLabel = new JLabel();
		add(errorMessageLabel, BorderLayout.SOUTH);

		availableGoodsLabel.setText("Available goods at " + seller.getName());
	}

	private void removeGoods() {
		try {
			// Remove trade good amount.
			int amount = (Integer) formatter.stringToValue(amountTextField.getText());
			int selectedGoodIndex = tradeTable.getSelectedRow();
			if (selectedGoodIndex > -1) {
				selectedGoodIndex = tradeTable.getRowSorter().convertRowIndexToModel(selectedGoodIndex);
				Good good = tradeTableModel.tradeList.get(selectedGoodIndex);
				int currentAmount = tradeTableModel.tradeMap.get(good);
				if (amount <= currentAmount) {
					tradeTableModel.removeGoodAmount(good, amount);
					goodsTableModel.addGoodAmount(good, amount);
					errorMessageLabel.setText(" ");
				}
				else errorMessageLabel.setText("Amount to remove is larger than traded amount.");
			}
		}
		catch (ParseException c) {
			// Should not happen since we disallow invalid input in the text field.
		}
	}
	
	private void addGoods() {
		// Add trade good amount.
		try {
			int amount = (Integer) formatter.stringToValue(amountTextField.getText());
			int selectedGoodIndex = goodsTable.getSelectedRow();
			if (selectedGoodIndex > -1) {
				selectedGoodIndex = goodsTable.getRowSorter().convertRowIndexToModel(selectedGoodIndex);

				Good good = goodsTableModel.goodsList.get(selectedGoodIndex);
				int currentAmount = goodsTableModel.goodsMap.get(good);
				if (amount <= currentAmount) {
					if (good.getCategory() == GoodCategory.VEHICLE && 
							((amount > 1) || tradeTableModel.hasTradedVehicle())) {
						errorMessageLabel.setText("Only one vehicle can be traded.");
					}
					else {
						goodsTableModel.removeGoodAmount(good, amount);
						tradeTableModel.addGoodAmount(good, amount);
						errorMessageLabel.setText(" ");
					}
					setMandatoryDone(true);
				}
				else errorMessageLabel.setText("Amount to add is larger than available amount.");
			}
		}
		catch (ParseException c) {
			// Shoudn't happen since we disallow invalid input in the text field.
		}
	}

	/**
	 * Commits changes from this wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		goodsTable.clearSelection();
		tradeTable.clearSelection();
		leftArrowButton.setEnabled(false);
		rightArrowButton.setEnabled(false);
		amountTextField.setEnabled(false);

		if (buyGoods) 
			 state.setBuyGoods(null);
		else
			state.setSellGoods(null);
		super.clearState(state);
	}

	/**
	 * Commits changes from this wizard panel.
	 * 
	 */
	@Override
	public void updateState(MissionDataBean state) {
		if (buyGoods)
			state.setBuyGoods(tradeTableModel.getTradeGoods());
		else 
			state.setSellGoods(tradeTableModel.getTradeGoods());
	}

	/**
	 * Model of goods available at source Settlement and amounts.
	 */
	private class GoodsTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Good, Integer> goodsMap;

		private List<Good> goodsList;
		
		/**
		 * Constructor
		 */
		private GoodsTableModel(Settlement source) {
			// Use AbstractTableModel constructor.
			super();

			// Populate goods map.
			goodsList = new ArrayList<>();
			goodsMap = new HashMap<>();

			for(var g : GoodsUtil.getGoodsList()) {
				int amount = (int) CommerceUtil.getNumInInventory(g, source);
				if (amount > 0) {
					goodsMap.put(g, amount);
					goodsList.add(g);
				}
			}
		}

		/**
		 * Returns the number of rows in the model.
		 * @return number of rows.
		 */
		@Override
		public int getRowCount() {
			return goodsList.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * @return number of columns.
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Good";
			else return "Amount";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else return Integer.class;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param row the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		@Override
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
		 * Adds an amount of a good to the table.
		 * @param good the good to add.
		 * @param amount the amount to add.
		 */
		void addGoodAmount(Good good, int amount) {
			if (amount > 0) {
				amount += goodsMap.get(good);
				goodsMap.put(good, amount);
				fireTableDataChanged();
			}
		}

		/**
		 * Removes an amount of a good from the table.
		 * @param good the good to remove.
		 * @param amount the amount to remove.
		 */
		void removeGoodAmount(Good good, int amount) {
			if (amount > 0) {
				int finalAmount = goodsMap.get(good) - amount;
				goodsMap.put(good, finalAmount);
				int selectedGoodIndex = goodsTable.getSelectedRow();
				fireTableDataChanged();
				if (selectedGoodIndex > -1) 
					goodsTable.setRowSelectionInterval(selectedGoodIndex, selectedGoodIndex);
			}
		}
	}

	/**
	 * Model of goods being traded and amounts.
	 */
	private class TradeTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Good, Integer> tradeMap;
		private List<Good> tradeList;

		/**
		 * Constructor
		 */
		private TradeTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Initialize trade map and list.
			tradeList = new ArrayList<>();
			tradeMap = new HashMap<>();
		}

		/**
		 * Returns the number of rows in the model.
		 * @return number of rows.
		 */
		@Override
		public int getRowCount() {
			return tradeList.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * @return number of columns.
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else return Integer.class;
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		@Override
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
		@Override
		public Object getValueAt(int row, int column) {
			Object result = "unknown";

			if (row < tradeList.size()) {
				Good good = tradeList.get(row); 
				if (column == 0) result = good.getName();
				else result = tradeMap.get(good);
			}

			return result;
		}

		/**
		 * Adds an amount of a good to the table.
		 * @param good the good to add.
		 * @param amount the amount to add.
		 */
		void addGoodAmount(Good good, int amount) {
			if (amount > 0) {
				if (tradeList.contains(good)) amount += tradeMap.get(good);
				else tradeList.add(good);
				tradeMap.put(good, amount);
				fireTableDataChanged();
			}
		}

		/**
		 * Removes an amount of a good from the table.
		 * @param good the good to remove.
		 * @param amount the amount to remove.
		 */
		void removeGoodAmount(Good good, int amount) {
			if (amount > 0) {
				int currentAmount = 0;
				if (tradeList.contains(good)) currentAmount = tradeMap.get(good);
				int finalAmount = currentAmount - amount;

				if (finalAmount > 0) {
					int selectedGoodIndex = tradeTable.getSelectedRow();
					tradeMap.put(good, finalAmount);
					fireTableDataChanged();
					if (selectedGoodIndex > -1)
						tradeTable.setRowSelectionInterval(selectedGoodIndex, selectedGoodIndex);
				}
				else {
					tradeList.remove(good);
					tradeMap.remove(good);
					fireTableDataChanged();
				}
			}
		}

		/**
		 * Gets the trade goods.
		 * @return map of goods and integers.
		 */
		Map<Good, Integer> getTradeGoods() {
			return tradeMap;
		}

		/**
		 * Checks if a vehicle is being traded.
		 * @return true if vehicle traded.
		 */
		private boolean hasTradedVehicle() {
			boolean result = false;
			Iterator<Good> i = tradeList.iterator();
			while (i.hasNext()) {
				if (i.next().getCategory() == GoodCategory.VEHICLE) result = true;
			}
			return result;
		}
	}
}
