/**
 * Mars Simulation Project
 * TradeTableModel.java
 * @version 3.07 2014-11-17
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

//import org.apache.commons.lang3.text.WordUtils;

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
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.swing.tool.Conversion;

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
		Simulation.instance().getUnitManager().removeUnitManagerListener(this);
	}

	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return " " + goodsList.size() + " Trade Goods";
	}

	/**
	 * Get the name of this model. The name will be a description helping
	 * the user understand the contents.
	 *
	 * @return Descriptive name.
	 */
	@Override
	public String getName() {
		return Msg.getString("TradeTableModel.tabName");
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
		if (columnIndex == 0) return "Trade Goods";
		else if (columnIndex == 1) return "Category";
		else {
			// 2014-11-16 Added "VP at "
			String columnName = "VP at " + settlements.get(columnIndex - 2).getName();
			return columnName;
		}
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
		if (columnIndex == 0) {
			// 2014-11-17 Capitalized Resource Names
			//Object result =  goodsList.get(rowIndex).getName();
			return Conversion.capitalize(goodsList.get(rowIndex).getName().toString());
		}

		else if (columnIndex == 1) {
			// 2014-11-17 Capitalized Category Names
			//Object result = getGoodCategoryName(goodsList.get(rowIndex));
			return Conversion.capitalize(getGoodCategoryName(goodsList.get(rowIndex)).toString());
		}

		else {
			try {
				//Settlement settlement = settlements.get(columnIndex - 2);
				//Good good = goodsList.get(rowIndex);
				//Object result = settlement.getGoodsManager().getGoodValuePerItem(good);
				//return result;
				return settlements.get(columnIndex - 2).getGoodsManager().getGoodValuePerItem(goodsList.get(rowIndex));
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	/** gives back the internationalized name of a good's category. */
	public String getGoodCategoryName(Good good) {
		String key = good.getCategory().getMsgKey();
		if (good.getCategory() == GoodType.EQUIPMENT) {
			if (Container.class.isAssignableFrom(good.getClassType())) key = "GoodType.container"; //$NON-NLS-1$
		}
		return Msg.getString(key);
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
					//fireTableStructureChanged();
				}
			});
		}
	}
}