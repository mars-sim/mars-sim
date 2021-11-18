/*
 * Mars Simulation Project
 * TradeTableModel.java
 * @date 2021-11-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.vehicle.VehicleType;
import org.mars_sim.msp.ui.swing.tool.Conversion;
@SuppressWarnings("serial")
public class TradeTableModel
extends AbstractTableModel
implements UnitListener, MonitorModel, UnitManagerListener {

	private static final String TRADE_GOODS = "Name";
	private static final String QUANTITY = "kg/# - ";
	private static final String VP_AT = "Value - ";
	private static final String PRICE_AT = "Price - ";
	private static final String CATEGORY = "Category";
	private static final String TYPE = "Type";

	private static final String ONE_SPACE = " ";

	protected static final int NUM_INITIAL_COLUMNS = 3;
	private static final int NUM_DATA_COL = 3;

	// Data members
	private List<Good> goodsList;
	private List<Settlement> settlements;

	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor.
	 */
	public TradeTableModel() {

		// Initialize goods list.
		goodsList = GoodsUtil.getGoodsList();

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
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if (event.getType() == UnitEventType.GOODS_VALUE_EVENT) {
			SwingUtilities.invokeLater(new TradeTableUpdater(event));
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
		unitManager.removeUnitManagerListener(this);

		unitManager = null;
	}

	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return new StringBuilder(ONE_SPACE + goodsList.size() + ONE_SPACE + TRADE_GOODS).toString();
	}

	/**
	 * Get the name of this model. The name will be a description helping
	 * the user understand the contents.
	 *
	 * @return Descriptive name.
	 */
	@Override
	public String getName() {
		return Msg.getString("TradeTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Return the object at the specified row indexes.
	 *
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return goodsList.get(row);
	}

	/**
	 * Has this model got a natural order that the model conforms to.
	 *
	 * @return If true, it implies that the user should not be allowed to order.
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
		if (columnIndex == 0) return TRADE_GOODS;
		else if (columnIndex == 1) return CATEGORY;
		else if (columnIndex == 2) return TYPE;
		else {
			int col = columnIndex - NUM_INITIAL_COLUMNS;
			int q = col / NUM_DATA_COL;
			int r = col % NUM_DATA_COL;
			if (r == 0)
				return QUANTITY + settlements.get(q).getName();
			else if (r == 1)
				return VP_AT + settlements.get(q).getName();
			else
				return PRICE_AT + settlements.get(q).getName();
		}
	}

	/**
	 * Return the type of the column requested.
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < NUM_INITIAL_COLUMNS)
			return String.class;
		else {
			return Double.class;
//			int col = columnIndex - NUM_INITIAL_COLUMNS;
//			int r = col % NUM_DATA_COL;
//			if (r == 0)
//				// Note: if using Number.class, this column will NOT be able to sort
//				return Number.class;
//			else if (r == 1)
//				return Double.class;
//			else
//				return Double.class;
		}
	}

	public int getColumnCount() {
		return settlements.size() * NUM_DATA_COL + NUM_INITIAL_COLUMNS;
	}

	public int getRowCount() {
		return goodsList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return Conversion.capitalize(goodsList.get(rowIndex).getName());
		}

		else if (columnIndex == 1) {
			return Conversion.capitalize(getGoodCategoryName(goodsList.get(rowIndex)));
		}

		else if (columnIndex == 2) {
			return Conversion.capitalize(GoodsUtil.getGoodType(goodsList.get(rowIndex)));
		}

		else {
			int col = columnIndex - NUM_INITIAL_COLUMNS;
			int q = col / NUM_DATA_COL;
			int r = col % NUM_DATA_COL;
			if (r == 0)
				return getQuantity(settlements.get(q), goodsList.get(rowIndex).getID());
			else if (r == 1)
				return settlements.get(q).getGoodsManager().getGoodValuePerItem(goodsList.get(rowIndex).getID());
			else
				return Math.round(settlements.get(q).getGoodsManager().getPricePerItem(goodsList.get(rowIndex))*100.0)/100.0;
		}
	}

	/**
	 * Gets the quantity/amount of the resource
	 *
	 * @param settlement
	 * @param id
	 * @return
	 */
    private double getQuantity(Settlement settlement, int id) {

    	if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
    		return Math.round(settlement.getAmountResourceStored(id) * 1.0)/1.0;
    	}
    	else if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
    		return settlement.getItemResourceStored(id);
    	}
    	else if (id < ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID) {
    		return settlement.findNumVehiclesOfType(VehicleType.convertID2Type(id));
    	}
    	else {
    		return settlement.findNumContainersOfType(EquipmentType.convertID2Type(id));
    	}
    }


	/**
	 * Gets the good category name in the internationalized string
	 *
	 * @param good
	 * @return
	 */
	public String getGoodCategoryName(Good good) {
		return good.getCategory().getMsgKey();
	}

	/**
	 * Inner class for updating goods table.
	 */
	private class TradeTableUpdater implements Runnable {

		private UnitEvent event;

		private TradeTableUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {
			if (event.getTarget() == null)
				fireTableDataChanged();
			else {
				int rowIndex = goodsList.indexOf(event.getTarget());
				int columnIndex = settlements.indexOf(event.getSource()) * NUM_DATA_COL + NUM_INITIAL_COLUMNS;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {

		if (event.getUnit().getUnitType() == UnitType.SETTLEMENT) {

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
					//fireTableStructureChanged();
				}
			});
		}
	}
}
