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

import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;

@SuppressWarnings("serial")
class TabPanelGoods extends EntityTableTabPanel<Settlement> implements TemporalComponent{

	private static final String GOOD_ICON = "trade";
	
	// Data members
	private GoodsTableModel goodsTableModel;

	/**
	 * Constructor.
	 * @param unit Settlement to display
	 * @param context UI context 
	 */
	public TabPanelGoods(Settlement unit, UIContext context) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelGoods.title"),
			ImageLoader.getIconByName(GOOD_ICON), null,
			unit, context
		);
	}
	
	/**
	 * Create a table model for the Goods
	 */
	@Override
	protected TableModel createModel() {
		goodsTableModel = new GoodsTableModel(getEntity().getGoodsManager());
		return goodsTableModel;
	}
	
	/**
	 * Sets the width and default rendering.
	 * 
	 * @param columnModel Columns to be configured
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
				
		columnModel.getColumn(0).setPreferredWidth(90);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(50);
		columnModel.getColumn(3).setPreferredWidth(50);
		
		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(new NumberCellRenderer(3));
		columnModel.getColumn(2).setCellRenderer(new NumberCellRenderer(3));
		columnModel.getColumn(3).setCellRenderer(new NumberCellRenderer(2)); // "$ ", "\u20BF "
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
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
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else return Double.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelGoods.column.good");
				case 1 -> Msg.getString("TabPanelGoods.column.demand");
				case 2 -> Msg.getString("TabPanelGoods.column.valuePoints");
				case 3 -> Msg.getString("TabPanelGoods.column.price");
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				Good good = (Good) goods.get(row);
				// Capitalized good's names
				return switch (column) {
					case 0 -> good.getName();
					case 1 -> manager.getDemandScoreWithID(good.getID());
					case 2 -> manager.getGoodValuePoint(good.getID());
					case 3 -> manager.getPrice(good);
					default -> null;
					};
			}
			
			return null;
		}

		public void update() {
			fireTableDataChanged();
		}
	}
}
