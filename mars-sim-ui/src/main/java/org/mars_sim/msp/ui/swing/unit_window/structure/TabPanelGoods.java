/**
 * Mars Simulation Project
 * TabPanelGoods.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

@SuppressWarnings("serial")
public class TabPanelGoods extends TabPanel {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
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
			Msg.getString("TabPanelGoods.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelGoods.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Prepare goods label panel.
		WebPanel goodsLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(goodsLabelPanel);

		// Prepare goods label.
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelGoods.label"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		goodsLabelPanel.add(titleLabel);

		// Create scroll panel for the outer table panel.
		WebScrollPane goodsScrollPane = new WebScrollPane();
		goodsScrollPane.setPreferredSize(new Dimension(250, 300));
		// increase vertical mousewheel scrolling speed for this one
		goodsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(goodsScrollPane);

		// Prepare goods table model.
		goodsTableModel = new GoodsTableModel(((Settlement) unit).getGoodsManager());

		// Prepare goods table.
		goodsTable = new ZebraJTable(goodsTableModel);
		goodsScrollPane.setViewportView(goodsTable);
		goodsTable.setRowSelectionAllowed(true);
		
		// Override default cell renderer for formatting double values.
		goodsTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
		
		goodsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		goodsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		
		// Added the two methods below to make all heatTable columns
		// Resizable automatically when its Panel resizes
		goodsTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		//goodsTable.setAutoResizeMode(WebTable.AUTO_RESIZE_ALL_COLUMNS);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		goodsTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
//		goodsTable.getColumnModel().getColumn(1).setCellRenderer(renderer);

		// Added sorting
		goodsTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(goodsTable);

     	// Added goodsSearchable
     	TableSearchable searchable = SearchableUtils.installSearchable(goodsTable);
        searchable.setPopupTimeout(5000);
     	searchable.setCaseSensitive(false);
        searchable.setMainIndex(0); // -1 = search for all columns

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			this.initializeUI();
		
		TableStyle.setTableStyle(goodsTable);
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

		//private DecimalFormat twoDecimal = new DecimalFormat("#,###,##0.00");

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
				if (column == 0) return Conversion.capitalize(good.getName()) + " ";
				else if (column == 1) {
					try {
						// Note: twoDecimal format is in conflict with Table column number sorting
						//return twoDecimal.format(manager.getGoodValuePerItem(good));
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
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		goodsTable = null;
		goodsTableModel = null;
	}
}