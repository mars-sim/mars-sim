/**
 * Mars Simulation Project
 * EmergencySupplyPanel.java
 * @version 3.1.0 2017-09-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.Barrel;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.GasCanister;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

/**
 * A wizard panel for getting emergency supplies information.
 */
public class EmergencySupplyPanel extends WizardPanel {

	// Data members.
	private JLabel errorMessageLabel;
	private JTable supplyTable;
	private JTable cargoTable;
	private JLabel availableSupplyLabel;
	private SupplyTableModel supplyTableModel;
	private CargoTableModel cargoTableModel;
	private JButton leftArrowButton;
	private JButton rightArrowButton;
	private JFormattedTextField amountTextField;
	private NumberFormatter formatter;

	EmergencySupplyPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor
		super(wizard);

		// Set the layout.
		setLayout(new BorderLayout());

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create title label.
		JLabel titleLabel = new JLabel("Choose supply amounts to deliver.", JLabel.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		add(titleLabel, BorderLayout.NORTH);

		// Create available supply panel.
		JPanel availableSupplyPane = new JPanel(new BorderLayout());
		availableSupplyPane.setPreferredSize(new Dimension(260, 100));
		add(availableSupplyPane, BorderLayout.WEST);

		// Create available supply label.
		availableSupplyLabel = new JLabel("Available supply at ", JLabel.CENTER);
		availableSupplyPane.add(availableSupplyLabel, BorderLayout.NORTH);

		// Create available supply table.
		JScrollPane supplyScrollPane = new JScrollPane();
		availableSupplyPane.add(supplyScrollPane, BorderLayout.CENTER);
		supplyTableModel = new SupplyTableModel();
		supplyTable = new JTable(supplyTableModel);
		TableStyle.setTableStyle(supplyTable);
		supplyTable.setRowSelectionAllowed(true);
		supplyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		supplyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() && (supplyTable.getSelectedRow() > -1)) {
					cargoTable.clearSelection();
					errorMessageLabel.setText(" ");
					leftArrowButton.setEnabled(false);
					amountTextField.setEnabled(true);
					rightArrowButton.setEnabled(true);
				}
			}
		});
		supplyScrollPane.setViewportView(supplyTable);

		// Create amount outer panel.
		JPanel amountOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(amountOuterPanel, BorderLayout.CENTER);

		// Create amount control panel.
		JPanel amountControlPane = new JPanel(new BorderLayout());
		amountOuterPanel.add(amountControlPane);

		// Create amount label.
		JLabel amountLabel = new JLabel("Amount", JLabel.CENTER);
		amountControlPane.add(amountLabel, BorderLayout.NORTH);

		// Create left arrow button.
		leftArrowButton = new JButton("<");
		leftArrowButton.setEnabled(false);
		leftArrowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Remove cargo amount.
					int amount = (Integer) formatter.stringToValue(amountTextField.getText());
					int selectedGoodIndex = cargoTable.getSelectedRow();
					if (selectedGoodIndex > -1) {
						Good good = cargoTableModel.cargoList.get(selectedGoodIndex);
						int currentAmount = cargoTableModel.cargoMap.get(good);
						if (amount <= currentAmount) {
							cargoTableModel.removeGoodAmount(good, amount);
							supplyTableModel.addGoodAmount(good, amount);
							errorMessageLabel.setText(" ");
						} else
							errorMessageLabel.setText("Amount to remove is larger than cargo amount.");
					}
				} catch (ParseException c) {
				}
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
		rightArrowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Add trade good amount.
				try {
					int amount = (Integer) formatter.stringToValue(amountTextField.getText());
					int selectedGoodIndex = supplyTable.getSelectedRow();
					if (selectedGoodIndex > -1) {
						Good good = supplyTableModel.goodsList.get(selectedGoodIndex);
						int currentAmount = supplyTableModel.goodsMap.get(good);
						if (amount <= currentAmount) {
							if (good.getCategory() == GoodType.VEHICLE
									&& ((amount > 1) || cargoTableModel.hasCargoVehicle())) {
								errorMessageLabel.setText("Only one vehicle can be traded.");
							} else {
								supplyTableModel.removeGoodAmount(good, amount);
								cargoTableModel.addGoodAmount(good, amount);
								errorMessageLabel.setText(" ");
							}
						} else
							errorMessageLabel.setText("Amount to add is larger than available amount.");
					}
				} catch (ParseException c) {
				}
			}
		});
		amountControlPane.add(rightArrowButton, BorderLayout.EAST);

		// Create cargo panel.
		JPanel cargoPane = new JPanel(new BorderLayout());
		cargoPane.setPreferredSize(new Dimension(260, 100));
		add(cargoPane, BorderLayout.EAST);

		// Create cargo label.
		JLabel cargoLabel = new JLabel("Cargo", JLabel.CENTER);
		cargoPane.add(cargoLabel, BorderLayout.NORTH);

		// Create cargo table.
		JScrollPane cargoScrollPane = new JScrollPane();
		cargoPane.add(cargoScrollPane, BorderLayout.CENTER);
		cargoTableModel = new CargoTableModel();
		cargoTable = new JTable(cargoTableModel);
		cargoTable.setRowSelectionAllowed(true);
		cargoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cargoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() && (cargoTable.getSelectedRow() > -1)) {
					cargoTable.clearSelection();
					errorMessageLabel.setText(" ");
					leftArrowButton.setEnabled(true);
					amountTextField.setEnabled(true);
					rightArrowButton.setEnabled(false);
				}
			}
		});
		cargoScrollPane.setViewportView(cargoTable);

		// Create the message label.
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		add(errorMessageLabel, BorderLayout.SOUTH);
	}

	@Override
	String getPanelName() {
		return "Emergency Supplies";
	}

	@Override
	boolean commitChanges() {
		boolean result = false;
		try {
			// Check if enough containers in cargo goods.
			if (hasEnoughContainers()) {

				// Set emergency cargo goods.
				MissionDataBean missionData = getWizard().getMissionData();
				missionData.setEmergencyGoods(cargoTableModel.getCargoGoods());

				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		return result;
	}

	@Override
	void clearInfo() {
		supplyTable.clearSelection();
		cargoTable.clearSelection();
		leftArrowButton.setEnabled(false);
		rightArrowButton.setEnabled(false);
		amountTextField.setEnabled(false);
	}

	@Override
	void updatePanel() {
		// Update available goods label.
		MissionDataBean missionData = getWizard().getMissionData();
		String settlementName = missionData.getStartingSettlement().getName();
		availableSupplyLabel.setText("Available supplies at " + settlementName);

		// Update table models.
		supplyTableModel.updateTable();
		cargoTableModel.updateTable();

		// Enable next/final button.
		getWizard().setButtons(true);
	}

	/**
	 * Checks if trade list has enough containers to hold amount resources.
	 * 
	 * @return true if enough containers
	 * @throws Exception if error checking containers.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean hasEnoughContainers() {
		boolean result = true;

		Map<Class, Integer> containerMap = new HashMap<Class, Integer>(3);
		containerMap.put(Bag.class, getNumberOfCargoContainers(Bag.class));
		containerMap.put(Barrel.class, getNumberOfCargoContainers(Barrel.class));
		containerMap.put(GasCanister.class, getNumberOfCargoContainers(GasCanister.class));

		Map<Good, Integer> cargoGoods = cargoTableModel.getCargoGoods();

		Iterator<Good> i = cargoGoods.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				AmountResource resource = ResourceUtil.findAmountResource(good.getID());
				PhaseType phase = resource.getPhase();
				Class containerType = ContainerUtil.getContainerTypeNeeded(phase);
				int containerNum = containerMap.get(containerType);
				Unit container = EquipmentFactory.createEquipment(containerType, new Coordinates(0, 0), true);
				double capacity = container.getInventory().getAmountResourceCapacity(resource, false);
				double totalCapacity = containerNum * capacity;
				double resourceAmount = cargoGoods.get(good);
				if (resourceAmount > totalCapacity) {
					double neededCapacity = resourceAmount - totalCapacity;
					int neededContainerNum = (int) Math.ceil(neededCapacity / capacity);
					String containerName = container.getName().toLowerCase();
					if (neededContainerNum > 1)
						containerName = containerName + "s";
					errorMessageLabel.setText(
							neededContainerNum + " " + containerName + " needed to hold " + resource.getName());
					result = false;
					break;
				} else {
					int neededContainerNum = (int) Math.ceil(resourceAmount / capacity);
					int remainingContainerNum = containerNum - neededContainerNum;
					containerMap.put(containerType, remainingContainerNum);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the number of containers of a type in the trade list.
	 * 
	 * @param containerType the container class.
	 * @return number of containers.
	 */
	private int getNumberOfCargoContainers(Class containerType) {
		int result = 0;
		Good containerGood = GoodsUtil.getEquipmentGood(containerType);
		Map<Good, Integer> cargoGoods = cargoTableModel.getCargoGoods();
		if (cargoGoods.containsKey(containerGood))
			result = cargoGoods.get(containerGood);
		return result;
	}


	private class SupplyTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Map<Good, Integer> goodsMap;
		private List<Good> goodsList;

		/**
		 * Constructor.
		 */
		private SupplyTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Populate goods map.
			goodsList = GoodsUtil.getGoodsList();
			goodsMap = new HashMap<Good, Integer>(goodsList.size());
			Iterator<Good> i = goodsList.iterator();
			while (i.hasNext())
				goodsMap.put(i.next(), 0);
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return goodsList.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * 
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Good";
			else
				return "Amount";
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < goodsList.size()) {
				Good good = goodsList.get(row);
				if (column == 0)
					result = good.getName();
				else
					result = goodsMap.get(good);
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			// Update good values for settlement.
			MissionDataBean missionData = getWizard().getMissionData();
			Settlement settlement = missionData.getStartingSettlement();
			Iterator<Good> i = goodsList.iterator();
			while (i.hasNext()) {
				Good good = i.next();
				try {
					int amount = (int) TradeUtil.getNumInInventory(good, settlement.getInventory());
					if (checkForVehicle(good))
						amount--;
					goodsMap.put(good, amount);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
			fireTableDataChanged();
		}

		/**
		 * Checks if good is the same type as the mission vehicle.
		 * 
		 * @param good the good to check.
		 * @return true if same type of vehicle.
		 */
		private boolean checkForVehicle(Good good) {
			boolean result = false;

			if (good.getCategory() == GoodType.VEHICLE) {
				String missionRoverName = getWizard().getMissionData().getRover().getDescription();
				if (good.getName().equalsIgnoreCase(missionRoverName))
					result = true;
			}

			return result;
		}

		/**
		 * Adds an amount of a good to the table.
		 * 
		 * @param good   the good to add.
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
		 * 
		 * @param good   the good to remove.
		 * @param amount the amount to remove.
		 */
		void removeGoodAmount(Good good, int amount) {
			if (amount > 0) {
				int finalAmount = goodsMap.get(good) - amount;
				goodsMap.put(good, finalAmount);
				int selectedGoodIndex = supplyTable.getSelectedRow();
				fireTableDataChanged();
				if (selectedGoodIndex > -1) {
					supplyTable.setRowSelectionInterval(selectedGoodIndex, selectedGoodIndex);
				}
			}
		}
	}

	private class CargoTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Map<Good, Integer> cargoMap;
		private List<Good> cargoList;

		/**
		 * Constructor.
		 */
		private CargoTableModel() {
			// Use AbstractTableModel constructor.
			super();

			// Initialize cargo map and list.
			cargoList = new ArrayList<Good>();
			cargoMap = new HashMap<Good, Integer>();
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return cargoList.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * 
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Good";
			else
				return "Amount";
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < cargoList.size()) {
				Good good = cargoList.get(row);
				if (column == 0)
					result = good.getName();
				else
					result = cargoMap.get(good);
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			// Clear map and list.
			cargoList.clear();
			cargoMap.clear();
			fireTableDataChanged();
		}

		/**
		 * Adds an amount of a good to the table.
		 * 
		 * @param good   the good to add.
		 * @param amount the amount to add.
		 */
		void addGoodAmount(Good good, int amount) {
			if (amount > 0) {
				if (cargoList.contains(good))
					amount += cargoMap.get(good);
				else
					cargoList.add(good);
				cargoMap.put(good, amount);
				fireTableDataChanged();
			}
		}

		/**
		 * Removes an amount of a good from the table.
		 * 
		 * @param good   the good to remove.
		 * @param amount the amount to remove.
		 */
		void removeGoodAmount(Good good, int amount) {
			if (amount > 0) {
				int currentAmount = 0;
				if (cargoList.contains(good))
					currentAmount = cargoMap.get(good);
				int finalAmount = currentAmount - amount;

				if (finalAmount > 0) {
					int selectedGoodIndex = cargoTable.getSelectedRow();
					cargoMap.put(good, finalAmount);
					fireTableDataChanged();
					if (selectedGoodIndex > -1)
						cargoTable.setRowSelectionInterval(selectedGoodIndex, selectedGoodIndex);
				} else {
					cargoList.remove(good);
					cargoMap.remove(good);
					fireTableDataChanged();
				}
			}
		}

		/**
		 * Gets the cargo goods.
		 * 
		 * @return map of goods and integers.
		 */
		Map<Good, Integer> getCargoGoods() {
			return new HashMap<Good, Integer>(cargoMap);
		}

		/**
		 * Checks if a vehicle is being towed.
		 * 
		 * @return true if vehicle is towed.
		 */
		private boolean hasCargoVehicle() {
			boolean result = false;
			Iterator<Good> i = cargoList.iterator();
			while (i.hasNext()) {
				if (i.next().getCategory() == GoodType.VEHICLE)
					result = true;
			}
			return result;
		}
	}
}