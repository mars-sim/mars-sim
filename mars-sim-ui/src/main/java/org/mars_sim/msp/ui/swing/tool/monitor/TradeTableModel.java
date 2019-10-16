/**
 * Mars Simulation Project
 * TradeTableModel.java
 * @version 3.1.0 2017-09-14
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
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.swing.tool.Conversion;
@SuppressWarnings("serial")
public class TradeTableModel
extends AbstractTableModel
implements UnitListener, MonitorModel, UnitManagerListener {

	private static final String TRADE_GOODS = "Name of Goods";
	private static final String VP_AT = "Value @ ";
	private static final String PRICE_AT = "Price $ @ ";
	private static final String CATEGORY = "Category";
	private static final String ONE_SPACE = " ";
	
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
		if (columnIndex == 0) return TRADE_GOODS;
		else if (columnIndex == 1) return CATEGORY;
		else {
			int col = columnIndex - 2;
			if (col % 2 == 0) // is even
				return VP_AT + settlements.get(col/2).getName();
			else // is odd
				return PRICE_AT + settlements.get(col/2).getName();
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
		return settlements.size() * 2 + 2;
	}

	public int getRowCount() {
		return goodsList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return Conversion.capitalize(goodsList.get(rowIndex).getName().toString());
		}

		else if (columnIndex == 1) {
			return Conversion.capitalize(getGoodCategoryName(goodsList.get(rowIndex)).toString());
		}

		else {
			int col = columnIndex - 2;
			if (col % 2 == 0) // is even
				return settlements.get(col/2).getGoodsManager().getGoodValuePerItem(goodsList.get(rowIndex));
			else // is odd
				return settlements.get(col/2).getGoodsManager().getPricePerItem(goodsList.get(rowIndex));
		}
	}


	/**
	 * Gets the good category name in the internationalized string
	 * @param good
	 * @return
	 */
	public String getGoodCategoryName(Good good) {
		String key = good.getCategory().getMsgKey();
		if (good.getCategory() == GoodType.EQUIPMENT) {
			if (Container.class.isAssignableFrom(good.getClassType())) 
				key = "GoodType.container"; //$NON-NLS-1$
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
				int columnIndex = settlements.indexOf(event.getSource()) * 2 + 2; 
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