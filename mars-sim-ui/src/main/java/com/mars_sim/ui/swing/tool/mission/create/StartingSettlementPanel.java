/*
 * Mars Simulation Project
 * StartingSettlementPanel.java
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's starting settlement.
 */
@SuppressWarnings("serial")
class StartingSettlementPanel extends WizardPanel {

	/** The wizard panel name. */
	private final static String NAME = "Starting Settlement";

	// Data members.
	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JLabel errorMessageLabel;

	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	StartingSettlementPanel(final CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);

		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create the select settlement label.
		JLabel selectSettlementLabel = createTitleLabel("Select a starting settlement.");
		add(selectSettlementLabel);

		// Create the settlement panel.
		JPanel settlementPane = new JPanel(new BorderLayout(0, 0));
		settlementPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		settlementPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(settlementPane);

		// Create scroll panel for settlement list.
		JScrollPane settlementScrollPane = new JScrollPane();
		settlementPane.add(settlementScrollPane, BorderLayout.CENTER);

		// Create the settlement table model.
		settlementTableModel = new SettlementTableModel();

		// Create the settlement table.
		settlementTable = new JTable(settlementTableModel);
		// Added sorting
		settlementTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(settlementTableModel));
		settlementTable.setRowSelectionAllowed(true);
		settlementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		settlementTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							int index = settlementTable.getSelectedRow();
							if (index > -1) {
								if (settlementTableModel.isFailureRow(index)) {
									errorMessageLabel.setText("Settlement cannot start the mission. See red cell(s).");
									getWizard().setButtons(false);
								}
								else {
									errorMessageLabel.setText(" ");
									getWizard().setButtons(true);
								}
							}
						}
					}
				}
			);
		// call it a click to next button when user double clicks the table
		settlementTable.addMouseListener(
				new MouseListener() {
					public void mouseReleased(MouseEvent e) {}
					public void mousePressed(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {}
					public void mouseEntered(MouseEvent e) {}
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && !e.isConsumed()) {
							wizard.buttonClickedNext();
						}
					}
				}
				);
		settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
		settlementScrollPane.setViewportView(settlementTable);

		// Create the error message label.
		errorMessageLabel = createErrorLabel();
		add(errorMessageLabel);

		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}

	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		int selectedIndex = settlementTable.getSelectedRow();
		Settlement selectedSettlement = (Settlement) settlementTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setStartingSettlement(selectedSettlement);
		return true;
	}

	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		settlementTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		settlementTableModel.updateTable();
		settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
	}

	/**
	 * A table model for settlements.
	 */
	private class SettlementTableModel
	extends UnitTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 */
		private SettlementTableModel() {
			// Use UnitTableModel constructor.
			super();

			// Add all settlements to table sorted by name.
//			UnitManager manager = Simulation.instance().getUnitManager();
			Collection<Settlement> settlements = CollectionUtils.sortByName(unitManager.getSettlements());
			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) units.add(i.next());

			// Add columns.
			columns.add("Name");
			columns.add("Pop");
			columns.add("Rovers");
			columns.add("Oxygen");
			columns.add("Water");
			columns.add("Food");
			columns.add("Methane");
			columns.add("Methanol");
			columns.add("EVA Suits");
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param row the row whose value is to be queried
		 * @param column the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < units.size()) {
				try {
					Settlement settlement = (Settlement) getUnit(row);

					if (column == 0)
						result = settlement.getName();
					else if (column == 1)
						result = settlement.getIndoorPeopleCount();
					else if (column == 2)
						result = settlement.findNumParkedRovers();
					if (column == 3) {
						result = (int) settlement.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
					}
					else if (column == 4) {
						result = (int) settlement.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
					}
					else if (column == 5) {
						result = (int) settlement.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
					}
					else if (column == 6) {
						result = (int) settlement.getSpecificAmountResourceStored(ResourceUtil.METHANE_ID);
					}
					else if (column == 7) {
						result = (int) settlement.getSpecificAmountResourceStored(ResourceUtil.METHANOL_ID);
					}
					else if (column == 8)
						result = settlement.getNumEVASuit();

					MissionType type = getWizard().getMissionData().getMissionType();
					if (MissionType.EXPLORATION == type) {
						if (column == 9)
							result = settlement.findNumContainersOfType(EquipmentType.SPECIMEN_BOX);
					}
					else if (MissionType.COLLECT_ICE == type ||
							MissionType.COLLECT_REGOLITH == type) {
						if (column == 9)
							result = settlement.findNumContainersOfType(EquipmentType.BAG);
					}
					else if (MissionType.MINING == type) {
						if (column == 9) {
							result = settlement.findNumContainersOfType(EquipmentType.BAG);
						}
						else if (column == 10) {
							result = settlement.findNumVehiclesOfType(VehicleType.LUV);
						}
						else if (column == 11) {
							result = settlement.getItemResourceStored(ItemResourceUtil.PNEUMATIC_DRILL_ID);
						}
						else if (column == 12) {
							result = settlement.getItemResourceStored(ItemResourceUtil.BACKHOE_ID);
						}
					}
				}
				catch (Exception e) {}
			}

			return result;
		}

		
//		/**
//		 * Determine an unprepared dessert resource to load on the mission.
//		 */
//		private double determineHighestDessertResources(Inventory inv) {
//
//			double highestAmount = 0;
//	        AmountResource dessertAR = null;
//
//	        //AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();
//	        for (AmountResource ar : availableDesserts) {
//
//	        	double amount = inv.getAmountResourceStored(ar, false);
//	        	if (highestAmount <= amount) {
//	        		highestAmount = amount;
//	        		dessertAR = ar;
//	        	}
//
//	        }
//
//	        return highestAmount;
//		}

//		/**
//		 * Determine an unprepared dessert resource to load on the mission.
//		 */
//		private Map<AmountResource, Double> determineDessertResources(Inventory inv) {
//
//			Map<AmountResource, Double> dessert = new HashMap<AmountResource, Double>(1);
//
//			double highestAmount = 0;
//	        AmountResource dessertAR = null;
//
//	        //AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();
//	        for (AmountResource ar : availableDesserts) {
//
//	        	double amount = inv.getAmountResourceStored(ar, false);
//	        	if (highestAmount <= amount) {
//	        		highestAmount = amount;
//	        		dessertAR = ar;
//	        	}
//
//	        }
//
//	        dessert.put(dessertAR, highestAmount);
//
//	        return dessert;
//		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			if (columns.size() > 8) {
				for (int x = 0; x < (columns.size() - 8); x++) columns.remove(8);
			}
			
			MissionType type = getWizard().getMissionData().getMissionType();
			if (MissionType.EXPLORATION == type)
				columns.add("Specimen Containers");
			else if (MissionType.COLLECT_ICE == type || MissionType.COLLECT_REGOLITH == type)
				columns.add("Bags");
			else if (MissionType.MINING == type) {
				columns.add("Bags");
				columns.add("Light Utility Vehicles");
				columns.add("Pneumatic Drills");
				columns.add("Backhoes");
			}
			fireTableStructureChanged();
		}

		/**
		 * Checks if a table cell is a failure cell.
		 * @param row the table row.
		 * @param column the table column.
		 * @return true if cell is a failure cell.
		 */
		boolean isFailureCell(int row, int column) {
			boolean result = false;
			Settlement settlement = (Settlement) getUnit(row);

			try {
				if (column == 1) {
					if (settlement.getIndoorPeopleCount() == 0) result = true;
				}
				else if (column == 2) {
					if (settlement.findNumParkedRovers() == 0) result = true;
				}
				else if (column == 3) {
					if (settlement.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID) < 100D) result = true;
				}
				else if (column == 4) {
					if (settlement.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < 100D) result = true;
				}
				else if (column == 5) {
					if (settlement.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID) < 100D) result = true;
				}
				else if (column == 6) {
					if (settlement.getSpecificAmountResourceStored(ResourceUtil.METHANE_ID) < 100D) result = true;
				}
				else if (column == 7) {
					if (settlement.getSpecificAmountResourceStored(ResourceUtil.METHANOL_ID) < 100D) result = true;
				}
				else if (column == 8) {
					if (settlement.getNumEVASuit() == 0) result = true;
				}

				MissionType type = getWizard().getMissionData().getMissionType();
				if (MissionType.EXPLORATION == type) {
					if (column == 9) {
						if (settlement.findNumContainersOfType(EquipmentType.SPECIMEN_BOX) < 
								Exploration.REQUIRED_SPECIMEN_CONTAINERS) result = true;
					}
				}
				else if (MissionType.COLLECT_ICE == type ||
						MissionType.COLLECT_REGOLITH == type) {
					if (column == 9) {
						if (settlement.findNumContainersOfType(EquipmentType.BAG) <
								CollectIce.REQUIRED_BARRELS) result = true;
					}
				}
				else if (MissionType.MINING == type ) {
					if (column == 9) {
						if (settlement.findNumContainersOfType(EquipmentType.BAG) <
								CollectIce.REQUIRED_BARRELS) result = true;
					}
					if (column == 10) {
						if (settlement.findNumVehiclesOfType(VehicleType.LUV) == 0) result = true;
					}
					else if (column == 11) {
						if (settlement.getItemResourceStored(ItemResourceUtil.PNEUMATIC_DRILL_ID) == 0) result = true;
					}
					else if (column == 12) {
						if (settlement.getItemResourceStored(ItemResourceUtil.BACKHOE_ID) == 0) result = true;
					}
				}
			}
			catch (Exception e) {}

			return result;
		}
	}
}
