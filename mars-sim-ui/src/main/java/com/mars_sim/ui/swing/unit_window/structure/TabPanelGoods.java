/*
 * Mars Simulation Project
 * TabPanelGoods.java
 * @date 2024-08-01
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;

@SuppressWarnings("serial")
public class TabPanelGoods extends TabPanelTable {

	private static final String GOOD_ICON = "trade";
	
	// Data members
	private GoodsTableModel goodsTableModel;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelGoods(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getIconByName(GOOD_ICON),
			Msg.getString("TabPanelGoods.title"), //$NON-NLS-1$
			unit, desktop
		);
	}
	
	/**
	 * Create a table model for the Goods
	 */
	@Override
	protected TableModel createModel() {
		goodsTableModel = new GoodsTableModel(((Settlement) getUnit()).getGoodsManager());
		return goodsTableModel;
	}
	
	/**
	 * Set the width and default rendering
	 * @param columnModel Columns to be configured
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
				
		columnModel.getColumn(0).setPreferredWidth(140);
		columnModel.getColumn(1).setPreferredWidth(80);
		
		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(new NumberCellRenderer(2, true));
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		goodsTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private static class GoodsTableModel extends AbstractTableModel {

		/** default serial id. */
		// Data members
		GoodsManager manager;
		List<?> goods;

		private GoodsTableModel(GoodsManager manager) {
			this.manager = manager;
			goods = GoodsUtil.getGoodsList();
		}

		@Override
		public int getRowCount() {
			return goods.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelGoods.column.good"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelGoods.column.valuePoints"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				Good good = (Good) goods.get(row);
				// Capitalized good's names
				if (column == 0) return good.getName();
				else if (column == 1) {
					try {
						return manager.getGoodValuePoint(good.getID());
					}
					catch (Exception e) {
					}
				}
				else return null;
			}
			
			return null;
		}

		public void update() {
			fireTableDataChanged();
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		goodsTableModel = null;
	}
}
