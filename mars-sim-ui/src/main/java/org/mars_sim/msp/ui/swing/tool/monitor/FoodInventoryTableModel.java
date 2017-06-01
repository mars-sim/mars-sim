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

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.foodProduction.Food;
import org.mars_sim.msp.core.foodProduction.FoodUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.tool.Conversion;

public class FoodInventoryTableModel
extends AbstractTableModel
implements UnitListener, MonitorModel, UnitManagerListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private List<Food> foodList;
	private List<Settlement> settlements;


	/**
	 * Constructor.
	 */
	public FoodInventoryTableModel() {

		// Initialize food list.
		foodList = FoodUtil.getFoodList();

		UnitManager unitManager = Simulation.instance().getUnitManager();

		//FoodInventoryTableModel ft = unitManager.

		// Initialize settlements.
		settlements = new ArrayList<Settlement>(unitManager.getSettlements());

		// Add table as listener to each settlement.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().addUnitListener(this);

		// Add as unit manager listener.
		unitManager.addUnitManagerListener(this);

	}

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if (event.getType() == UnitEventType.GOODS_VALUE_EVENT) {
			SwingUtilities.invokeLater(new FoodTableUpdater(event));
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		// Remove as listener for all settlements.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().removeUnitListener(this);

		// Remove as listener to unit manager.
		Simulation.instance().getUnitManager().removeUnitManagerListener(this);
	}

	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return " " + foodList.size() + " Food Items"; // do need a white space before Food Items
	}

	/**
	 * Get the name of this model. The name will be a description helping
	 * the user understand the contents.
	 *
	 * @return Descriptive name.
	 */
	@Override
	public String getName() {
		return Msg.getString("FoodInventoryTableModel.tabName");
	}

	/**
	 * Return the object at the specified row indexes.
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return foodList.get(row);
	}

	/**
	 * Has this model got a natural order that the model conforms to. If this
	 * value is true, then it implies that the user should not be allowed to
	 * order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Return the name of the column requested.
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) return Msg.getString("FoodInventoryTableModel.firstColumn");
		//else if (columnIndex == 1) return "Category";
		else {
			String columnName = Msg.getString("FoodInventoryTableModel.settlementColumns",
					settlements.get(columnIndex - 1).getName());
			return columnName;
		}
	}

	/**
	 * Return the type of the column requested.
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 1) return String.class;
		else return Double.class;
	}

	public int getColumnCount() {
		return settlements.size() + 1;
	}

	public int getRowCount() {
		return foodList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {


		if (columnIndex == 0) {
			// 2014-11-17 Capitalized Resource Names
			Object result =  foodList.get(rowIndex).getName();
			return Conversion.capitalize(result.toString());
		}
	/*
		else if (columnIndex == 1) {
			// 2014-11-17 Capitalized Category Names
			Object result = getFoodCategoryName(foodList.get(rowIndex));
			return Conversion.capitalize(result.toString());
		}
			*/
		else {
			try {
				//Settlement settlement = settlements.get(columnIndex - 1);
				//Food food = foodList.get(rowIndex);
				//Inventory inv = settlement.getInventory();
			    //String foodName = food.getName();
				//AmountResource ar = AmountResource.findAmountResource(foodName);
				//double foodAvailable = inv.getAmountResourceStored(ar, false);
				return settlements.get(columnIndex - 1).getInventory().getAmountResourceStored(AmountResource.findAmountResource(foodList.get(rowIndex).getName()), false);
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	/** gives back the internationalized name of a food's category.

	public String getFoodCategoryName(Food food) {
		String key = food.getCategory().getMsgKey();
		//if (food.getCategory() == FoodType.EQUIPMENT) {
		//	if (Container.class.isAssignableFrom(food.getClassType())) key = "FoodType.container"; //$NON-NLS-1$
		//}
		return Msg.getString(key);
	}
	 */
	/**
	 * Inner class for updating food table.
	 */
	private class FoodTableUpdater implements Runnable {

		private UnitEvent event;

		private FoodTableUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {
			foodList = FoodUtil.getFoodList();
			if (event.getTarget() == null) fireTableDataChanged();
			else {
				int rowIndex = foodList.indexOf(event.getTarget());
				int columnIndex = settlements.indexOf(event.getSource()) + 2;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {

		if (event.getUnit() instanceof Settlement) {

			Settlement settlement = (Settlement) event.getUnit();

			if (UnitManagerEventType.ADD_UNIT == event.getEventType()) {
				// If settlement is new, add to settlement list.
				if (!settlements.contains(settlement)) {
					settlements.add(settlement);
					settlement.addUnitListener(this);
				}
			}
			else if (UnitManagerEventType.REMOVE_UNIT == event.getEventType()) {
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