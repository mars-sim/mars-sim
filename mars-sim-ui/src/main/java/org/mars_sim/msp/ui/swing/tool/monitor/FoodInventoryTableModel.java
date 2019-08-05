/**
 * Mars Simulation Project
 * FoodInventoryTableModel.java
 * @version 3.1.0 2017-03-12
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.foodProduction.Food;
import org.mars_sim.msp.core.foodProduction.FoodUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.ui.swing.tool.Conversion;

public class FoodInventoryTableModel extends AbstractTableModel
		implements UnitListener, MonitorModel, UnitManagerListener {

	private static final String FOOD_ITEMS = " Food Items";

	private GameMode mode;
	
	// Data members
	private List<Food> foodList;
	private List<Settlement> settlements = new ArrayList<Settlement>();

	private Settlement commanderSettlement;
	
	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor.
	 */
	public FoodInventoryTableModel() {

		// Initialize food list.
		foodList = FoodUtil.getFoodList();

		// Initialize settlements.
		if (GameManager.mode == GameMode.COMMAND) {
			commanderSettlement = unitManager.getCommanderSettlement();
			settlements.add(commanderSettlement);
		}
		else
			settlements.addAll(unitManager.getSettlements());

		// Add table as listener to each settlement.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext())
			i.next().addUnitListener(this);

		// Add as unit manager listener.
		unitManager.addUnitManagerListener(this);

	}

	/**
	 * Catch unit update event.
	 * 
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		Object source = event.getTarget();
		if (eventType == UnitEventType.GOODS_VALUE_EVENT) {
			if (mode == GameMode.COMMAND) {
				if (source instanceof Good 
						&& unit instanceof Settlement
						&& unit.getName().equalsIgnoreCase(commanderSettlement.getName()))
					SwingUtilities.invokeLater(new FoodTableUpdater(event));
				
			}
			else {
				SwingUtilities.invokeLater(new FoodTableUpdater(event));
			}
		}
	}

//	/**
//	 * Gets the index of the row a given unit is at.
//	 * 
//	 * @param unit the unit to find.
//	 * @return the row index or -1 if not in table model.
//	 */
//	protected int getUnitIndex(Unit unit) {
//		if ((units != null) && units.contains(unit))
//			return getIndex(unit);
//		else
//			return -1;
//	}
	
	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return " " + foodList.size() + FOOD_ITEMS; // do need a white space before Food Items
	}

	/**
	 * Get the name of this model. The name will be a description helping the user
	 * understand the contents.
	 *
	 * @return Descriptive name.
	 */
	@Override
	public String getName() {
		return Msg.getString("FoodInventoryTableModel.tabName");
	}

	/**
	 * Return the object at the specified row indexes.
	 * 
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return foodList.get(row);
	}

	/**
	 * Has this model got a natural order that the model conforms to. If this value
	 * is true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Return the name of the column requested.
	 * 
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return Msg.getString("FoodInventoryTableModel.firstColumn");
		// else if (columnIndex == 1) return "Category";
		else {
			String columnName = Msg.getString("FoodInventoryTableModel.settlementColumns",
					settlements.get(columnIndex - 1).getName());
			return columnName;
		}
	}

	/**
	 * Return the type of the column requested.
	 * 
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 1)
			return String.class;
		else
			return Double.class;
	}

	public int getColumnCount() {
		return settlements.size() + 1;
	}

	public int getRowCount() {
		return foodList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {

		if (columnIndex == 0) {
			// Capitalize Resource Names
			Object result = foodList.get(rowIndex).getName();
			return Conversion.capitalize(result.toString());
		}
//		/*
//		 * else if (columnIndex == 1) { // Capitalize Category Names Object result =
//		 * getFoodCategoryName(foodList.get(rowIndex)); return
//		 * Conversion.capitalize(result.toString()); }
//		 */
		else {
			try {
				// Settlement settlement = settlements.get(columnIndex - 1);
				// Food food = foodList.get(rowIndex);
				// Inventory inv = settlement.getInventory();
				// String foodName = food.getName();
				// AmountResource ar = ResourceUtil.findAmountResource(foodName);
				// double foodAvailable = inv.getAmountResourceStored(ar, false);
				return settlements.get(columnIndex - 1).getInventory().getAmountResourceStored(
						ResourceUtil.findAmountResource(foodList.get(rowIndex).getName()), false);
			} catch (Exception e) {
				return null;
			}
		}
	}

//	/**
//	 * gives back the internationalized name of a food's category.
//	 * 
//	 * public String getFoodCategoryName(Food food) { String key =
//	 * food.getCategory().getMsgKey(); //if (food.getCategory() ==
//	 * FoodType.EQUIPMENT) { // if
//	 * (Container.class.isAssignableFrom(food.getClassType())) key =
//	 * "FoodType.container"; //$NON-NLS-1$ //} return Msg.getString(key); }
//	 */
	
	/**
	 * Inner class for updating food table.
	 */
	private class FoodTableUpdater implements Runnable {

		private UnitEvent event;

		private FoodTableUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {

			if (event.getTarget() == null)
				fireTableDataChanged();
			else {
				foodList = FoodUtil.getFoodList();
				int rowIndex = foodList.indexOf(event.getTarget());
				int columnIndex = settlements.indexOf(event.getSource()) + 2;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {

		if (mode == GameMode.COMMAND
				&& event.getUnit() instanceof Settlement
				&&  settlements.contains((Settlement) event.getUnit())) {
				// Update table structure due to cells changing.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						fireTableStructureChanged();
						fireTableStructureChanged();
					}
				});
		}
		
		else {
			if (event.getUnit() instanceof Settlement) {

				Settlement settlement = (Settlement) event.getUnit();

				if (UnitManagerEventType.ADD_UNIT == event.getEventType()) {
					// If settlement is new, add to settlement list.
					if (!settlements.contains(settlement)) {
						settlements.add(settlement);
						settlement.addUnitListener(this);
					}
				} else if (UnitManagerEventType.REMOVE_UNIT == event.getEventType()) {
					// If settlement is gone, remove from settlement list.
					if (settlements.contains(settlement)) {
						settlements.remove(settlement);
						settlement.removeUnitListener(this);
					}
				}

				// Update table structure due to cells changing.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						fireTableStructureChanged();
						fireTableStructureChanged();
					}
				});
			}		
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		// Remove as listener for all settlements.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext())
			i.next().removeUnitListener(this);

		// Remove as listener to unit manager.
		unitManager.removeUnitManagerListener(this);
		
		foodList = null;
		settlements = null;
		unitManager = null;
	}
}