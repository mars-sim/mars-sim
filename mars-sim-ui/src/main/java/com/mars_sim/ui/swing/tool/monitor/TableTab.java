/*
 * Mars Simulation Project
 * TableTab.java
 * @date 2023-03-29
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.components.MarsTimeTableCellRenderer;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a MonutorTableModel in a Table window. It supports
 * the selection and deletion of rows.
 */
@SuppressWarnings("serial")
public class TableTab extends MonitorTab {

	// Common shared renderers
	private static final NumberCellRenderer DIGIT0_RENDERER = new NumberCellRenderer(0);
	private static final NumberCellRenderer DIGIT1_RENDERER = new NumberCellRenderer(1);
	private static final NumberCellRenderer DIGIT2_RENDERER = new NumberCellRenderer(2);
	private static final NumberCellRenderer DIGIT3_RENDERER = new NumberCellRenderer(3);
	private static final MarsTimeTableCellRenderer TIME_RENDERER = new MarsTimeTableCellRenderer();
	private static final NumberCellRenderer CURRENCY_RENDERER = new NumberCellRenderer(2, "$");

	private TableProperties propsWindow;

	/** Table component. */
	private JTable table;
	private boolean widthAdjusted = false;
	private int settlementColumnId;
	private TableColumn savedSettlementColumn;
	
	/**
	 * Creates a table within a tab displaying the specified model.
	 *
	 * @param model           The model of Units to display.
	 * @param mandatory       Is this table view mandatory.
	 * @param singleSelection Does this table only allow single selection?
	 * @param icon name        Name of the icon; @see {@link ImageLoader#getIconByName(String)}
	 */
	@SuppressWarnings("serial")
	protected TableTab(final MonitorWindow window, final MonitorModel model, boolean mandatory, boolean singleSelection,
			String iconname) {
		super(model, mandatory, true, ImageLoader.getIconByName(iconname));

		settlementColumnId = model.getSettlementColumn();
		
		this.table = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
				return ToolTipTableModel.extractToolTip(e, this);
            }
		};

		// Set default renderers
		this.table.setDefaultRenderer(Integer.class, DIGIT0_RENDERER);
		this.table.setDefaultRenderer(Double.class, DIGIT1_RENDERER);
		this.table.setDefaultRenderer(Number.class, DIGIT2_RENDERER);
		this.table.setDefaultRenderer(MarsTime.class, TIME_RENDERER);

		// Check for special styles
		var colModel = table.getColumnModel();
		for(int colId = 0; colId < model.getColumnCount(); colId++) {
			var col = colModel.getColumn(colId);

			// Use the model index to handle the table hiding or reordering columns
			var style = model.getColumnStyle(col.getModelIndex());

			TableCellRenderer renderer = switch(style) {
				case ColumnSpec.STYLE_CURRENCY -> CURRENCY_RENDERER;
				case ColumnSpec.STYLE_INTEGER -> DIGIT0_RENDERER;
				case ColumnSpec.STYLE_DIGIT1 -> DIGIT1_RENDERER;
				case ColumnSpec.STYLE_DIGIT2 -> DIGIT2_RENDERER;
				case ColumnSpec.STYLE_DIGIT3 -> DIGIT3_RENDERER;
				default -> null;
			};
			if (renderer != null)
				col.setCellRenderer(renderer);
		}

		// call it a click to display details button when user double clicks the table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					window.displayDetails();
				}
			}
		});

		// Allow ordering
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		sorter.setSortsOnUpdates(true);
		table.setRowSorter(sorter);
		
		// Can result in java.lang.ArrayIndexOutOfBoundsException when a process is done and its row is deleted
//		setAutoCreateRowSorter(true);

		
		// Set single selection mode if necessary.
		if (singleSelection)
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		add(scroller, BorderLayout.CENTER);

		setName(model.getName());
		// Use column resizer
		adjustColumnWidth(table);
	}

	public JTable getTable() {
		return table;
	}

	private static void adjustColumnWidth(JTable table) {
		// Gets max width for cells in column as the preferred width
		TableColumnModel columnModel = table.getColumnModel();
		for (int col = 0; col < table.getColumnCount(); col++) {
			TableColumn tableColumn = columnModel.getColumn(col);
		    int preferredWidth = tableColumn.getMinWidth() + 15;
		    TableCellRenderer rend = table.getTableHeader().getDefaultRenderer();
			TableCellRenderer rendCol = tableColumn.getHeaderRenderer();
		    if (rendCol == null) rendCol = rend;
		    Component header = rendCol.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, col);
		    int headerWidth = header.getPreferredSize().width + 15;
		    preferredWidth = Math.max(preferredWidth, headerWidth);

			// Sample the first 20 rows
			for (int row = 0; row < Math.min(20, table.getRowCount()); row++) {
				TableCellRenderer tableCellRenderer = table.getCellRenderer(row, col);
				Component c = table.prepareRenderer(tableCellRenderer, row, col);
				int cellWidth = c.getPreferredSize().width + table.getIntercellSpacing().width + 15;
				preferredWidth = Math.max(cellWidth, preferredWidth);
			}

			tableColumn.setPreferredWidth(preferredWidth);
		}
	}

	/**
	 * Display property window anchored to a main desktop.
	 *
	 * @param desktop Main desktop owing the properties dialog.
	 */
	public void displayProps(MainDesktopPane desktop) {
		if (propsWindow == null) {
			propsWindow = new TableProperties(getName(), table, desktop);
			propsWindow.show();
		} else {
			if (propsWindow.isClosed()) {
				if (!propsWindow.wasOpened()) {
					propsWindow.setWasOpened(true);
				}
				add(propsWindow, 0);
				try {
					propsWindow.setClosed(false);
				} catch (PropertyVetoException e) {
					// Ignore veto problems
				}
			}
			propsWindow.show();
			// bring to front if it overlaps with other propsWindows
			try {
				propsWindow.setSelected(true);
			} catch (PropertyVetoException e) {
				// ignore if setSelected is vetoed
			}
		}
		propsWindow.getContentPane().validate();
		propsWindow.getContentPane().repaint();
		validate();
		repaint();

	}


	/**
	 * This return the selected rows in the model that are current selected in this
	 * view.
	 *
	 * @return array of row indexes.
	 */
	public final List<Object> getSelection() {
		MonitorModel target = getModel();

		int [] indexes = table.getSelectedRows();
		RowSorter<? extends TableModel> sorter = table.getRowSorter();
		List<Object> selectedRows = new ArrayList<>();
		for (int index : indexes) {
            if (sorter != null)
                index = sorter.convertRowIndexToModel(index);

			Object selected = target.getObject(index);
			if (selected != null)
				selectedRows.add(selected);
		}

		return selectedRows;
	}

	protected void setSettlementColumnIndex(int idx) {
		settlementColumnId = idx;
	}

	/**
	 * Filters the settlements.
	 * 
	 * @param currentSelection
	 * @return
	 */
	public boolean setSettlementFilter(Set<Settlement> currentSelection) {
		if (settlementColumnId > 0) {
			boolean showSettlement = (currentSelection.size() > 1);
			if (showSettlement && (savedSettlementColumn != null)) {
				// SHow Settlement but it is hidden
				var tc = table.getColumnModel();
				tc.addColumn(savedSettlementColumn);
				tc.moveColumn(tc.getColumnCount()-1, settlementColumnId);
				savedSettlementColumn = null;
			}
			else if (!showSettlement && (savedSettlementColumn == null)) {
				// No need for Settlement and is no display
				var tc = table.getColumnModel();
				savedSettlementColumn = tc.getColumn(settlementColumnId);
				tc.removeColumn(savedSettlementColumn);
			}
		}
		var accepted = getModel().setSettlementFilter(currentSelection);

		// Automatically adjust the width when a significant data change
		if (!widthAdjusted) {
			widthAdjusted = true;
			adjustColumnWidth(table);
		}

		return accepted;
	}

}
