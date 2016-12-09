/**
 * Mars Simulation Project
 * StartingSettlementPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's starting settlement.
 */
class StartingSettlementPanel extends WizardPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
		JLabel selectSettlementLabel = new JLabel("Select a starting settlement.", JLabel.CENTER);
		selectSettlementLabel.setFont(selectSettlementLabel.getFont().deriveFont(Font.BOLD));
		selectSettlementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
									errorMessageLabel.setText("Settlement cannot start the mission (see red cells).");
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
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
		errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
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
			UnitManager manager = Simulation.instance().getUnitManager();
			Collection<Settlement> settlements = CollectionUtils.sortByName(manager.getSettlements());
			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) units.add(i.next());

			// Add columns.
			columns.add("Name");
			columns.add("Pop.");
			columns.add("Rovers");
			columns.add("Oxygen");
			columns.add("Water");
			columns.add("Food");
			columns.add("Dessert");
			columns.add("Methane");
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
					Inventory inv = settlement.getInventory();
					if (column == 0) 
						result = settlement.getName();
					else if (column == 1) 
						result = settlement.getCurrentPopulationNum();
					else if (column == 2) 
						result = inv.findNumUnitsOfClass(Rover.class);
					if (column == 3) {
						//AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
						result = (int) inv.getAmountResourceStored(Rover.oxygenAR, false);
					}
					else if (column == 4) {
						//AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
						result = (int) inv.getAmountResourceStored(Rover.waterAR, false);
					}
					else if (column == 5) {
						//AmountResource food = AmountResource.findAmountResource(LifeSupportType.FOOD);
						result = (int) inv.getAmountResourceStored(Rover.foodAR, false);
					}
					else if (column == 6) {
						result = (int) determineHighestDessertResources(inv);
					}
					else if (column == 7) {
						//AmountResource methane = AmountResource.findAmountResource("methane");
						result = (int) inv.getAmountResourceStored(Rover.methaneAR, false);
					}
					else if (column == 8) 
						result = inv.findNumUnitsOfClass(EVASuit.class);

					String type = getWizard().getMissionData().getType();

					if (type.equals(MissionDataBean.EXPLORATION_MISSION)) {
						if (column == 8)
							result = inv.findNumEmptyUnitsOfClass(SpecimenContainer.class, true);
					}
					else if (type.equals(MissionDataBean.ICE_MISSION) || 
							type.equals(MissionDataBean.REGOLITH_MISSION)) {
						if (column == 8)
							result = inv.findNumEmptyUnitsOfClass(Bag.class, true);
					}
					else if (type.equals(MissionDataBean.MINING_MISSION)) {
						if (column == 8)
							result = inv.findNumEmptyUnitsOfClass(Bag.class, true);
						else if (column == 9)
							result = inv.findNumUnitsOfClass(LightUtilityVehicle.class);
						else if (column == 10) {
							//Part pneumaticDrill = (Part) Part.findItemResource(Mining.PNEUMATIC_DRILL);
							result = inv.getItemResourceNum(ItemResource.pneumaticDrill);
						}
						else if (column == 11) {
							//Part backhoe = (Part) Part.findItemResource(Mining.BACKHOE);
							result = inv.getItemResourceNum(ItemResource.backhoe);
						}
					}
				}
				catch (Exception e) {}
			}

			return result;
		}
		
	
		/**
		 * Determine an unprepared dessert resource to load on the mission.
		 */
		private double determineHighestDessertResources(Inventory inv) {
				
			double highestAmount = 0;
	        AmountResource dessertAR = null;

	        AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();
	        for (AmountResource ar : availableDesserts) {     
	            
	        	double amount = inv.getAmountResourceStored(ar, false);
	        	if (highestAmount <= amount) {
	        		highestAmount = amount;
	        		dessertAR = ar;
	        	}

	        }
	        
	        return highestAmount;
		}
		
		/**
		 * Determine an unprepared dessert resource to load on the mission.
		 */
		private Map<AmountResource, Double> determineDessertResources(Inventory inv) {
		     
			Map<AmountResource, Double> dessert = new HashMap<AmountResource, Double>(1);
			
			double highestAmount = 0;
	        AmountResource dessertAR = null;

	        AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();
	        for (AmountResource ar : availableDesserts) {     
	            
	        	double amount = inv.getAmountResourceStored(ar, false);
	        	if (highestAmount <= amount) {
	        		highestAmount = amount;
	        		dessertAR = ar;
	        	}

	        }
	        
	        dessert.put(dessertAR, highestAmount);
	        
	        return dessert;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			if (columns.size() > 8) {
				for (int x = 0; x < (columns.size() - 8); x++) columns.remove(8);
			}
			String type = getWizard().getMissionData().getType();
			if (type.equals(MissionDataBean.EXPLORATION_MISSION)) columns.add("Specimen Containers");
			else if (type.equals(MissionDataBean.ICE_MISSION) || 
					type.equals(MissionDataBean.REGOLITH_MISSION)) columns.add("Bags");
			else if (type.equals(MissionDataBean.MINING_MISSION)) {
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
			Inventory inv = settlement.getInventory();

			try {
				if (column == 1) {
					if (settlement.getCurrentPopulationNum() == 0) result = true;
				}
				else if (column == 2) {
					if (inv.findNumUnitsOfClass(Rover.class) == 0) result = true;
				}
				else if (column == 3) {
					//AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
					if (inv.getAmountResourceStored(Rover.oxygenAR, false) < 100D) result = true;
				}
				else if (column == 4) {
					//AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
					if (inv.getAmountResourceStored(Rover.waterAR, false) < 100D) result = true;
				}
				else if (column == 5) {
					//AmountResource food = AmountResource.findAmountResource(LifeSupportType.FOOD);
					if (inv.getAmountResourceStored(Rover.foodAR, false) < 100D) result = true;
				}
				else if (column == 6) {
					//AmountResource food = AmountResource.findAmountResource(LifeSupportType.FOOD);
					if (determineHighestDessertResources(inv) < 100D) result = true;
				}
				else if (column == 7) {
					//AmountResource methane = AmountResource.findAmountResource("methane");
					if (inv.getAmountResourceStored(Rover.methaneAR, false) < 100D) result = true;
				}
				else if (column == 8) {
					if (inv.findNumUnitsOfClass(EVASuit.class) == 0) result = true;
				}

				String type = getWizard().getMissionData().getType();
				if (type.equals(MissionDataBean.EXPLORATION_MISSION)) {
					if (column == 9) {
						if (inv.findNumEmptyUnitsOfClass(SpecimenContainer.class, true) < 
								Exploration.REQUIRED_SPECIMEN_CONTAINERS) result = true;
					}
				}
				else if (type.equals(MissionDataBean.ICE_MISSION) || 
						type.equals(MissionDataBean.REGOLITH_MISSION)) {
					if (column == 9) {
						if (inv.findNumEmptyUnitsOfClass(Bag.class, true) < 
								CollectIce.REQUIRED_BAGS) result = true;
					}
				}
				else if (type.equals(MissionDataBean.MINING_MISSION)) {
					if (column == 9) {
						if (inv.findNumEmptyUnitsOfClass(Bag.class, true) < 
								CollectIce.REQUIRED_BAGS) result = true;
					}
					if (column == 10) {
						if (inv.findNumUnitsOfClass(LightUtilityVehicle.class) == 0) result = true;
					}
					else if (column == 11) {
						//Part pneumaticDrill = (Part) Part.findItemResource(Mining.PNEUMATIC_DRILL);
						if (inv.getItemResourceNum(ItemResource.pneumaticDrill) == 0) result = true;
					}
					else if (column == 12) {
						//Part backhoe = (Part) Part.findItemResource(Mining.BACKHOE);
						if (inv.getItemResourceNum(ItemResource.backhoe) == 0) result = true;
					}
				}
			}
			catch (Exception e) {}

			return result;
		}
	}
}