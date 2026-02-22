/*
 * Mars Simulation Project
 * EmergencySupplyPanel.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.PhaseType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.entitywindow.mission.objectives.GoodsTableModel;

/**
 * A wizard panel for getting emergency supplies information.
 */
@SuppressWarnings("serial")
public class EmergencySupplyPanel extends WizardPanel {
	// Static members.
 	private static Logger logger = Logger.getLogger(EmergencySupplyPanel.class.getName());
 	
	// Data members.
	private JLabel errorMessageLabel;
	private JTable supplyTable;
	private JTable cargoTable;
	private JLabel availableSupplyLabel;
	private GoodsTableModel supplyTableModel;
	private GoodsTableModel cargoTableModel;
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
		JLabel titleLabel = createTitleLabel("Choose supply amounts to deliver.");
		add(titleLabel, BorderLayout.NORTH);

		// Create available supply panel.
		JPanel availableSupplyPane = new JPanel(new BorderLayout());
		availableSupplyPane.setPreferredSize(new Dimension(260, 100));
		add(availableSupplyPane, BorderLayout.WEST);

		// Create available supply label.
		availableSupplyLabel = new JLabel("Available supply at ", SwingConstants.CENTER);
		availableSupplyPane.add(availableSupplyLabel, BorderLayout.NORTH);

		// Create available supply table.
		JScrollPane supplyScrollPane = new JScrollPane();
		availableSupplyPane.add(supplyScrollPane, BorderLayout.CENTER);
		supplyTableModel = new GoodsTableModel();
		supplyTable = new JTable(supplyTableModel);
		supplyTable.setAutoCreateRowSorter(true);
		supplyTable.setRowSelectionAllowed(true);
		supplyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		supplyTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting() && (supplyTable.getSelectedRow() > -1)) {
				cargoTable.clearSelection();
				updateSelector(true);
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
		JLabel amountLabel = new JLabel("Amount", SwingConstants.CENTER);
		amountControlPane.add(amountLabel, BorderLayout.NORTH);

		// Create left arrow button.
		leftArrowButton = new JButton("<");
		leftArrowButton.setEnabled(false);
		leftArrowButton.addActionListener(e -> {
			moveAmount(cargoTable, cargoTableModel, supplyTableModel);
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
		rightArrowButton.addActionListener(e -> {
			moveAmount(supplyTable, supplyTableModel, cargoTableModel);
		});
		amountControlPane.add(rightArrowButton, BorderLayout.EAST);

		// Create cargo panel.
		JPanel cargoPane = new JPanel(new BorderLayout());
		cargoPane.setPreferredSize(new Dimension(260, 100));
		add(cargoPane, BorderLayout.EAST);

		// Create cargo label.
		JLabel cargoLabel = new JLabel("Cargo", SwingConstants.CENTER);
		cargoPane.add(cargoLabel, BorderLayout.NORTH);

		// Create cargo table.
		JScrollPane cargoScrollPane = new JScrollPane();
		cargoPane.add(cargoScrollPane, BorderLayout.CENTER);
		cargoTableModel = new GoodsTableModel();
		cargoTable = new JTable(cargoTableModel);
		cargoTable.setAutoCreateRowSorter(true);
		cargoTable.setRowSelectionAllowed(true);
		cargoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cargoTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting() && (cargoTable.getSelectedRow() > -1)) {
				supplyTable.clearSelection();
				updateSelector(false);
			}
		});
		cargoScrollPane.setViewportView(cargoTable);

		// Create the message label.
		errorMessageLabel = createErrorLabel();
		add(errorMessageLabel, BorderLayout.SOUTH);
	}

	@Override
	String getPanelName() {
		return "Emergency Supplies";
	}
	
	private void updateSelector(boolean doRight) {
		errorMessageLabel.setText(" ");
		leftArrowButton.setEnabled(!doRight);
		amountTextField.setEnabled(true);
		rightArrowButton.setEnabled(doRight);
	}

	/**
	 * Move the specified ammount from one table to the other
	 * @param sourceTable
	 * @param sourceModel
	 * @param targetModel
	 */
	private void moveAmount(JTable sourceTable, GoodsTableModel sourceModel, GoodsTableModel targetModel) {
		try {
			int amount = (Integer) formatter.stringToValue(amountTextField.getText());

			int selectedGoodIndex = sourceTable.getSelectedRow();
			if (selectedGoodIndex > -1) {
				selectedGoodIndex = sourceTable.getRowSorter().convertRowIndexToModel(selectedGoodIndex);
				var selection = sourceModel.getValueAt(selectedGoodIndex);
				if (amount <= selection.amount()) {
					sourceModel.changeGoodAmount(selection.good(), -amount);
					targetModel.changeGoodAmount(selection.good(), amount);
					errorMessageLabel.setText(" ");
				} else
					errorMessageLabel.setText("Amount to add is larger than available amount.");
			}
		} catch (ParseException e) {
			// THis should never happen
		}
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
			
			// Check if enough containers in cargo goods.
			if (hasEnoughContainers(missionData.getStartingSettlement())) {
				// Set emergency cargo goods.
				missionData.setEmergencyGoods(cargoTableModel.getGoods());

				result = true;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Issues with commiting emergency goods from cargo goods: " + e.getMessage());
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
		updateSupplyTable();
		cargoTableModel.updateTable(Collections.emptyMap());

		// Enable next/final button.
		getWizard().setButtons(true);
	}

	/**
	 * Loads the supply table model with the inventrory from the starting Settlement
	 */
	private void updateSupplyTable() {
		// Update good values for settlement.
		MissionDataBean missionData = getWizard().getMissionData();
		Settlement settlement = missionData.getStartingSettlement();

		Map<Good, Integer> goodsMap = new HashMap<>();
		for(Good good :GoodsUtil.getGoodsList()) {
			int amount = (int) CommerceUtil.getNumInInventory(good, settlement);
			if (amount > 0) {
				goodsMap.put(good, amount);
			}
		}

		// Update supply table model.
		supplyTableModel.updateTable(goodsMap);
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
		containerMap.put(EquipmentType.BAG, getNumberOfCargoContainers(EquipmentType.BAG));
		containerMap.put(EquipmentType.BARREL, getNumberOfCargoContainers(EquipmentType.BARREL));
		containerMap.put(EquipmentType.GAS_CANISTER, getNumberOfCargoContainers(EquipmentType.GAS_CANISTER));

		for(var e : cargoTableModel.getGoods().entrySet()) {
			Good good = e.getKey();
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
				AmountResource resource = ResourceUtil.findAmountResource(good.getID());
				PhaseType phase = resource.getPhase();
				EquipmentType containerType = ContainerUtil.getEquipmentTypeNeeded(phase);
				int containerNum = containerMap.get(containerType);
				double capacity = ContainerUtil.getContainerCapacity(containerType);
				double totalCapacity = containerNum * capacity;
				double resourceAmount = e.getValue();
				if (resourceAmount > totalCapacity) {
					double neededCapacity = resourceAmount - totalCapacity;
					int neededContainerNum = (int) Math.ceil(neededCapacity / capacity);
					String containerName = containerType.getName();
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
	private int getNumberOfCargoContainers(EquipmentType containerType) {
		int result = 0;
		Good containerGood = GoodsUtil.getEquipmentGood(containerType);
		Map<Good, Integer> cargoGoods = cargoTableModel.getGoods();
		if (cargoGoods.containsKey(containerGood))
			result = cargoGoods.get(containerGood);
		return result;
	}
}
