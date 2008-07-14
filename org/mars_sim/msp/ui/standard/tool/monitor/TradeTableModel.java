/**
 * Mars Simulation Project
 * TradeTableModel.java
 * @version 2.85 2008-07-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitEvent;
import org.mars_sim.msp.simulation.UnitListener;
import org.mars_sim.msp.simulation.equipment.Container;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;

public class TradeTableModel extends AbstractTableModel implements
		UnitListener, MonitorModel {
	
	// Data members
	List<Good> goodsList;
	Collection<Settlement> settlements;
	
	/**
	 * Constructor
	 */
	TradeTableModel() {
		
		// Initialize goods list.
		goodsList = GoodsUtil.getGoodsList();
		
		// Initialize settlements.
		settlements = Simulation.instance().getUnitManager().getSettlements();
		
		// Add table as listener to each settlement.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().addUnitListener(this);
	}
	
	public void unitUpdate(UnitEvent event) {
		if (event.getType().equals(GoodsManager.GOODS_VALUE_EVENT)) {
			if (event.getTarget() == null) fireTableDataChanged();
			else {
				Good good = (Good) event.getTarget();
				int rowIndex = goodsList.indexOf(good);
				Settlement settlement = (Settlement) event.getSource();
				Settlement[] settlementArray = settlements.toArray(new Settlement[settlements.size()]);
				int columnIndex = -1;
				for (int x = 0; x < settlementArray.length; x++) {
					if (settlementArray[x] == settlement) columnIndex = x + 2;
				}
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	public void destroy() {
		// Remove as listener for all settlements.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().removeUnitListener(this);
	}

	public String getCountString() {
		return goodsList.size() + " trade goods";
	}

	public String getName() {
		return "Trade Goods";
	}

	public Object getObject(int row) {
		return goodsList.get(row);
	}

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
    	else return ((Settlement)settlements.toArray()[columnIndex - 2]).getName();
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
				Settlement settlement = (Settlement) settlements.toArray()[columnIndex - 2];
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
}