/**
 * Mars Simulation Project
 * TradeTableModel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

public class TradeTableModel
extends AbstractTableModel
implements UnitListener, MonitorModel, UnitManagerListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	private List<Good> goodsList;
	private List<Settlement> settlements;

	/**
	 * Constructor.
	 */
	public TradeTableModel() {

		// Initialize goods list.
		goodsList = GoodsUtil.getGoodsList();

		UnitManager unitManager = Simulation.instance().getUnitManager();

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
	public void unitUpdate(UnitEvent event) {
		if (event.getType() == UnitEventType.GOODS_VALUE_EVENT) {
			SwingUtilities.invokeLater(new TradeTableUpdater(event));
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
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
	public String getCountString() {
		return goodsList.size() + " trade goods";
	}

	/**
	 * Get the name of this model. The name will be a description helping
	 * the user understand the contents.
	 *
	 * @return Descriptive name.
	 */
	public String getName() {
		return "Trade Goods";
	}

	/**
	 * Return the object at the specified row indexes.
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return goodsList.get(row);
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
		if (columnIndex == 0) return "Trade Good";
		else if (columnIndex == 1) return "Category";
		else return settlements.get(columnIndex - 2).getName();
	}

	/**
	 * Return the type of the column requested.
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 2) return String.class;
		else return Double.class;
	}

	public int getColumnCount() {
		return settlements.size() + 2;
	}

	public int getRowCount() {
		return goodsList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) return goodsList.get(rowIndex).getName();
		else if (columnIndex == 1) return (getGoodCategory(goodsList.get(rowIndex)));
		else {
			try {
				Settlement settlement = settlements.get(columnIndex - 2);
				Good good = goodsList.get(rowIndex);
				double result = settlement.getGoodsManager().getGoodValuePerItem(good);
				return result;
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	public String getGoodCategory(Good good) {
		String result = good.getCategory();

		if (result.equals("amount resource")) result = "resource";
		else if (result.equals("item resource")) result = "part";
		else if (result.equals("equipment")) {
			if (Container.class.isAssignableFrom(good.getClassType())) result = "container";
		}

		return result;
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
			if (event.getTarget() == null) fireTableDataChanged();
			else {
				int rowIndex = goodsList.indexOf(event.getTarget());
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