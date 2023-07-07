/*
 * Mars Simulation Project
 * TableTab.java
 * @date 2023-03-29
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.utils.MarsTimeCellRenderer;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a UnitTableModel in a WebTable window. It supports
 * the selection and deletion of rows.
 */
@SuppressWarnings("serial")
abstract class TableTab extends MonitorTab {

	protected static final NumberCellRenderer DIGIT0_RENDERER = new NumberCellRenderer(0, true);
	protected static final NumberCellRenderer DIGIT2_RENDERER = new NumberCellRenderer(2, true);
	protected static final NumberCellRenderer DIGIT3_RENDERER = new NumberCellRenderer(3, true);
	protected static final MarsTimeCellRenderer TIME_RENDERER = new MarsTimeCellRenderer();

	private TableProperties propsWindow;

	/** Table component. */
	protected JTable table;

	/**
	 * Creates a table within a tab displaying the specified model.
	 *
	 * @param model           The model of Units to display.
	 * @param mandatory       Is this table view mandatory.
	 * @param singleSelection Does this table only allow single selection?
	 * @param iconname        Name of the icon; @see {@link ImageLoader#getIconByName(String)}
	 */
	public TableTab(final MonitorWindow window, final MonitorModel model, boolean mandatory, boolean singleSelection,
			String iconname) {
		super(model, mandatory, true, ImageLoader.getIconByName(iconname));

		// Simple WebTable
		this.table = new JTable(model);

		// Set default renderers
		this.table.setDefaultRenderer(Double.class, DIGIT2_RENDERER);
		this.table.setDefaultRenderer(Number.class, DIGIT2_RENDERER);
		this.table.setDefaultRenderer(Integer.class, DIGIT0_RENDERER);
		this.table.setDefaultRenderer(MarsTime.class, TIME_RENDERER);

		// call it a click to display details button when user double clicks the table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					window.displayDetails();
				}
			}
		});

		// If the model is not fixed ordered then allow user to select order
		if (!model.getOrdered()) {
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
			table.setRowSorter(sorter);
			sorter.setSortsOnUpdates(true);
		}

		// Set single selection mode if necessary.
		if (singleSelection)
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		add(scroller, BorderLayout.CENTER);

		setName(model.getName());

		// Use column resizer
		adjustColumnWidth(table);
	}

	public JTable getTable() {
		return table;
	}

	public static void adjustColumnWidth(JTable table) {
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
				} catch (Exception e) {
					// logger.log(Level.SEVERE,e.toString()); }
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

		int indexes[] = table.getSelectedRows();
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

	/**
	 * Removes this tab.
	 */
	public void removeTab() {
		super.removeTab();
		table = null;
	}

	public void destroy() {
		propsWindow = null;
	}
}
