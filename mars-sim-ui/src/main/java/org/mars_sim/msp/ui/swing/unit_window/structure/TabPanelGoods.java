/*
 * Mars Simulation Project
 * TabPanelGoods.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelGoods extends TabPanel {

	private static final String CART_ICON = Msg.getString("icon.cart"); //$NON-NLS-1$
	
	// Data members
	private JTable goodsTable;
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
			ImageLoader.getNewIcon(CART_ICON),
			Msg.getString("TabPanelGoods.title"), //$NON-NLS-1$
			unit, desktop
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		
 		// Create scroll panel for the outer table panel.
		JScrollPane goodsScrollPane = new JScrollPane();
		goodsScrollPane.setPreferredSize(new Dimension(250, 300));
		// increase vertical mousewheel scrolling speed for this one
		goodsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		content.add(goodsScrollPane);

		// Prepare goods table model.
		goodsTableModel = new GoodsTableModel(((Settlement) getUnit()).getGoodsManager());

		// Prepare goods table.
		goodsTable = new JTable(goodsTableModel);
		goodsScrollPane.setViewportView(goodsTable);
		goodsTable.setRowSelectionAllowed(true);
		
		// Override default cell renderer for formatting double values.
		goodsTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
		
		goodsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		goodsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		
		// Added the two methods below to make all heatTable columns
		// Resizable automatically when its Panel resizes
		goodsTable.setPreferredScrollableViewportSize(new Dimension(225, -1));

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		goodsTable.getColumnModel().getColumn(0).setCellRenderer(renderer);

		// Added sorting
		goodsTable.setAutoCreateRowSorter(true);
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
	private static class GoodsTableModel
	extends AbstractTableModel {

		/** default serial id. */
//		private static final long serialVersionUID = 1L;
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
		
		goodsTable = null;
		goodsTableModel = null;
	}
}
