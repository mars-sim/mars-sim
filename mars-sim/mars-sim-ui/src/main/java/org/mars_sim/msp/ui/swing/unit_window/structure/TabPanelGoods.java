/**
 * Mars Simulation Project
 * GoodsTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

public class TabPanelGoods
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
			Msg.getString("TabPanelGoods.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelGoods.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		// Prepare goods label panel.
		JPanel goodsLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(goodsLabelPanel);

		// Prepare goods label.
		JLabel goodsLabel = new JLabel(Msg.getString("TabPanelGoods.label"), JLabel.CENTER); //$NON-NLS-1$
		goodsLabelPanel.add(goodsLabel);

		// Create scroll panel for the outer table panel.
		JScrollPane goodsScrollPane = new JScrollPane();
		goodsScrollPane.setPreferredSize(new Dimension(250, 300));
		// increase vertical mousewheel scrolling speed for this one
		goodsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(goodsScrollPane);

		// Prepare outer table panel.
		JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		outerTablePanel.setBorder(new MarsPanelBorder());
		goodsScrollPane.setViewportView(outerTablePanel);   

		// Prepare goods table panel.
		JPanel goodsTablePanel = new JPanel(new BorderLayout(0, 0));
		outerTablePanel.add(goodsTablePanel);

		// Prepare goods table model.
		goodsTableModel = new GoodsTableModel(((Settlement) unit).getGoodsManager());

		// Prepare goods table.
		JTable goodsTable = new JTable(goodsTableModel);
		goodsTable.setCellSelectionEnabled(false);
		goodsTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
		goodsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		goodsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
		goodsTablePanel.add(goodsTable.getTableHeader(), BorderLayout.NORTH);
		goodsTablePanel.add(goodsTable, BorderLayout.CENTER);
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
		private static final long serialVersionUID = 1L;
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
				if (column == 0) return good.getName();
				else if (column == 1) {
					try {
						return manager.getGoodValuePerItem(good);
					}
					catch (Exception e) {
						e.printStackTrace(System.err);
						return null;
					}
				}
				else return null;
			}
			else return null;
		}

		public void update() {
			fireTableDataChanged();
		}
	}
}