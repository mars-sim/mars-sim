/*
 * Mars Simulation Project
 * TradeGoodsPanel.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.PhaseType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MarsPanelBorder;

@SuppressWarnings("serial")
class TradeGoodsPanel extends WizardPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TradeGoodsPanel.class.getName());
	
	private boolean buyGoods;
	private JLabel errorMessageLabel;
	private JTable goodsTable;
	private JTable tradeTable;
	private JLabel availableGoodsLabel;
	private GoodsTableModel goodsTableModel;
	private TradeTableModel tradeTableModel;
	private JButton leftArrowButton;
	private JButton rightArrowButton;
	private JFormattedTextField amountTextField;
	private NumberFormatter formatter;

	/**
	 * constructor.
	 * 
	 * @param wizard {@link CreateMissionWizard}
	 * @param buyGoods {@link Boolean}
	 */
	public TradeGoodsPanel(CreateMissionWizard wizard, boolean buyGoods) {
		// Use WizardPanel constructor
		super(wizard);

		this.buyGoods = buyGoods;

		// Set the layout.
		setLayout(new BorderLayout());

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create title label.
		String tradeString = "sold";
		if (buyGoods) tradeString = "bought";
		JLabel titleLabel = createTitleLabel("Choose good amounts to be " + tradeString + ".");
		add(titleLabel, BorderLayout.NORTH);

		// Create available goods panel.
		JPanel availableGoodsPane = new JPanel(new BorderLayout());
		availableGoodsPane.setPreferredSize(new Dimension(260, 100));
		add(availableGoodsPane, BorderLayout.WEST);

		// Create available goods label.
		availableGoodsLabel = new JLabel("Available goods at ", SwingConstants.CENTER);
		availableGoodsPane.add(availableGoodsLabel, BorderLayout.NORTH);

		// Create available goods table.
		JScrollPane goodsScrollPane = new JScrollPane();
		availableGoodsPane.add(goodsScrollPane, BorderLayout.CENTER);
		goodsTableModel = new GoodsTableModel();
		goodsTable = new JTable(goodsTableModel);
		goodsTable.setAutoCreateRowSorter(true);
		goodsTable.setRowSelectionAllowed(true);
		goodsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		goodsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting() && (goodsTable.getSelectedRow() > -1)) {
							tradeTable.clearSelection();
							errorMessageLabel.setText(" ");
							leftArrowButton.setEnabled(false);
							amountTextField.setEnabled(true);
							rightArrowButton.setEnabled(true);
						}
					}
				});	
		goodsScrollPane.setViewportView(goodsTable);

		// Create amount outer panel.
		JPanel amountOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(amountOuterPanel, BorderLayout.CENTER);

		// Create amount control panel.
		JPanel amountControlPane = new JPanel(new BorderLayout());
		amountOuterPanel.add(amountControlPane);

		// Create amount label.
		JLabel amountLabel = new JLabel("Amount", SwingConstants.CENTER);
		amountControlPane.add(amountLabel, BorderLayout.NORTH);

		// Create left arrow button.
		leftArrowButton = new JButton("<");
		leftArrowButton.setEnabled(false);
		leftArrowButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							// Remove trade good amount.
							int amount = (Integer) formatter.stringToValue(amountTextField.getText());
							int selectedGoodIndex = tradeTable.getSelectedRow();
							if (selectedGoodIndex > -1) {
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
						catch (ParseException c) {}
					}
				});
		amountControlPane.add(leftArrowButton, BorderLayout.WEST);

		// Create amount text field.
		formatter = new NumberFormatter();
		formatter.setAllowsInvalid(false);
		formatter.setMinimum(0);
		formatter.setMaximum(9999);
		amountTextField = new JFormattedTextField(formatter);
		amountTextField.setValue(0);
		amountTextField.setColumns(5);
		amountTextField.setHorizontalAlignment(JTextField.RIGHT);
		amountTextField.setEnabled(false);
		amountControlPane.add(amountTextField, BorderLayout.CENTER);

		// Create right arrow button.
		rightArrowButton = new JButton(">");
		rightArrowButton.setEnabled(false);
		rightArrowButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Add trade good amount.
					try {
						int amount = (Integer) formatter.stringToValue(amountTextField.getText());
						int selectedGoodIndex = goodsTable.getSelectedRow();
						if (selectedGoodIndex > -1) {
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
							}
							else errorMessageLabel.setText("Amount to add is larger than available amount.");
						}
					}
					catch (ParseException c) {}
				}
			}
		);
		amountControlPane.add(rightArrowButton, BorderLayout.EAST);

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
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting() && (tradeTable.getSelectedRow() > -1)) {
							goodsTable.clearSelection();
							errorMessageLabel.setText(" ");
							leftArrowButton.setEnabled(true);
							amountTextField.setEnabled(true);
							rightArrowButton.setEnabled(false);
						}
					}
				});	
		tradeScrollPane.setViewportView(tradeTable);

		// Create the message label.
		errorMessageLabel = createErrorLabel();
		add(errorMessageLabel, BorderLayout.SOUTH);
	}

	/**
	 * Commits changes from this wizard panel.
	 */
	void clearInfo() {
		goodsTable.clearSelection();
		tradeTable.clearSelection();
		leftArrowButton.setEnabled(false);
		rightArrowButton.setEnabled(false);
		amountTextField.setEnabled(false);
	}

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		boolean result = false;
		try {
			MissionDataBean missionData = getWizard().getMissionData();
			
			// Check if enough containers in trade goods.
			if (hasEnoughContainers(missionData.getStartingSettlement())) {
				// Set buy/sell goods.
				if (buyGoods) missionData.setBuyGoods(tradeTableModel.getTradeGoods());
				else missionData.setSellGoods(tradeTableModel.getTradeGoods());
				result = true;
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Issues with getting the goods from tradeTableModel: " + e.getMessage());
		}
		return result;
	}

	/**
	 * Checks if trade list has enough containers to hold amount resources.
	 * 
	 * @return true if enough containers
	 * @throws Exception if error checking containers.
	 */
	private boolean hasEnoughContainers(Settlement settlement) {
		boolean result = true;

		Map<EquipmentType, Integer> containerMap = new HashMap<>(3);
		containerMap.put(EquipmentType.BAG, getNumberOfTradedContainers(EquipmentType.BAG));
		containerMap.put(EquipmentType.BARREL, getNumberOfTradedContainers(EquipmentType.BARREL));
		containerMap.put(EquipmentType.GAS_CANISTER, getNumberOfTradedContainers(EquipmentType.GAS_CANISTER));

		Map<Good, Integer> tradeGoods = tradeTableModel.getTradeGoods();

		Iterator<Good> i = tradeGoods.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
				AmountResource resource = ResourceUtil.findAmountResource(good.getID());
				PhaseType phase = resource.getPhase();
				EquipmentType containerType = ContainerUtil.getEquipmentTypeNeeded(phase);
				int containerNum = containerMap.get(containerType);
				double capacity = ContainerUtil.getContainerCapacity(containerType);
				double totalCapacity = containerNum * capacity;
				double resourceAmount = tradeGoods.get(good);
				if (resourceAmount > totalCapacity) {
					double neededCapacity = resourceAmount - totalCapacity;
					int neededContainerNum = (int) Math.ceil(neededCapacity / capacity);
					String containerName = containerType.getName().toLowerCase();
					if (neededContainerNum > 1) containerName = containerName + "s";
					errorMessageLabel.setText(neededContainerNum + " " + containerName + " needed to hold " + resource.getName());
					result = false;
					break;
				}
				else {
					int neededContainerNum = (int) Math.ceil(resourceAmount / capacity);
					int remainingContainerNum = containerNum - neededContainerNum;
					containerMap.put(containerType, remainingContainerNum);
				}
			}
			// May consider using container for item in future
		}

		return result;
	}

	/**
	 * Gets the number of containers of a type in the trade list.
	 * 
	 * @param containerType the container class.
	 * @return number of containers.
	 */
	private int getNumberOfTradedContainers(EquipmentType containerType) {
		int result = 0;
		Good containerGood = GoodsUtil.getEquipmentGood(containerType);
		Map<Good, Integer> tradeGoods = tradeTableModel.getTradeGoods();
		if (tradeGoods.containsKey(containerGood)) result = tradeGoods.get(containerGood);
		return result;
	}

	/**
	 * Gets the wizard panel name.
	 * 
	 * @return panel name.
	 */
	String getPanelName() {
		if (buyGoods) return "Buying Goods";
		else return "Selling Goods";
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		// Update available goods label.
		MissionDataBean missionData = getWizard().getMissionData();
		String settlementName = "";
		if (buyGoods) settlementName = missionData.getDestinationSettlement().getName();
		else settlementName = missionData.getStartingSettlement().getName();
		availableGoodsLabel.setText("Available goods at " + settlementName);

		// Update table models.
		goodsTableModel.updateTable();
		tradeTableModel.updateTable();

		// Enable next/final button.
		getWizard().setButtons(true);
	}

	private class GoodsTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Good, Integer> goodsMap;
		private List<Good> goodsList;

		/**
		 * Constructor
		 */
		private GoodsTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Populate goods map.
			goodsList = GoodsUtil.getGoodsList();
			goodsMap = new HashMap<Good, Integer>(goodsList.size());
			Iterator<Good> i = goodsList.iterator();
			while (i.hasNext()) goodsMap.put(i.next(), 0);
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
		void updateTable() {
			// Update good values for settlement.
			MissionDataBean missionData = getWizard().getMissionData();
			Settlement settlement = null;
			if (buyGoods) settlement = missionData.getDestinationSettlement();
			else settlement = missionData.getStartingSettlement();
			Iterator<Good> i = goodsList.iterator();
			while (i.hasNext()) {
				Good good = i.next();
				try {
					int amount = (int) CommerceUtil.getNumInInventory(good, settlement);
					if (checkForVehicle(good)) amount--;
					goodsMap.put(good, amount);
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, "Issues with updating the goods map: " + e.getMessage());
				}
			}
			fireTableDataChanged();
		}

		/**
		 * Checks if good is the same type as the mission vehicle.
		 * @param good the good to check.
		 * @return true if same type of vehicle.
		 */
		private boolean checkForVehicle(Good good) {
			boolean result = false;

			if (!buyGoods && good.getCategory() == GoodCategory.VEHICLE) {
				String missionRoverName = getWizard().getMissionData().getRover().getDescription();
				if (good.getName().equalsIgnoreCase(missionRoverName)) result = true;
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
			tradeList = new ArrayList<Good>();
			tradeMap = new HashMap<Good, Integer>();
		}

		/**
		 * Returns the number of rows in the model.
		 * @return number of rows.
		 */
		public int getRowCount() {
			return tradeList.size();
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

			if (row < tradeList.size()) {
				Good good = tradeList.get(row); 
				if (column == 0) result = good.getName();
				else result = tradeMap.get(good);
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			// Clear map and list.
			tradeList.clear();
			tradeMap.clear();
			fireTableDataChanged();
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
			return new HashMap<Good, Integer>(tradeMap);
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
