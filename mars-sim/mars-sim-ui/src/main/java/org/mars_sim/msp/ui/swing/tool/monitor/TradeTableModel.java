/**
 * Mars Simulation Project
 * TradeTableModel.java
 * @version 2.85 2008-07-16
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
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

public class TradeTableModel extends AbstractTableModel implements
		UnitListener, MonitorModel {
	
	// Data members
	private List<Good> goodsList;
	private List<Settlement> settlements;
	
	/**
	 * Constructor
	 */
	TradeTableModel() {
		
		// Initialize goods list.
		goodsList = GoodsUtil.getGoodsList();
		
		// Initialize settlements.
		settlements = new ArrayList<Settlement>(
				Simulation.instance().getUnitManager().getSettlements());
		
		// Add table as listener to each settlement.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().addUnitListener(this);
	}
	
	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		if (event.getType().equals(GoodsManager.GOODS_VALUE_EVENT)) {
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
		if (columnIndex == 0) return ((Good) goodsList.get(rowIndex)).getName();
		else if (columnIndex == 1) return (getGoodCategory((Good) goodsList.get(rowIndex)));
		else {
			try {
				Settlement settlement = settlements.get(columnIndex - 2);
				Good good = (Good) goodsList.get(rowIndex);
				double result = settlement.getGoodsManager().getGoodValuePerItem(good);
				return new Double(result);
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
				int rowIndex = goodsList.indexOf((Good) event.getTarget());
				int columnIndex = settlements.indexOf((Settlement) event.getSource()) + 2;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}
}