/**
 * Mars Simulation Project
 * TableTab.java
 * @version 3.07 2014-12-06

 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ColumnResizer;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a UnitTableModel in a JTable window. It supports
 * the selection and deletion of rows.
 */
abstract class TableTab
extends MonitorTab {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JTableHeader theHeader;

	private TableHeaderRenderer theRenderer;

	/**
	 * This internal class provides a fixed image icon that is drawn using a Graphics
	 * object. It represents an arrow Icon that can be other ascending or
	 * or descending.
	 */
	static class ColumnSortIcon
	implements Icon {

		static final int midw = 4;
		private Color lightShadow;
		private Color darkShadow;
		private boolean downwards;

		/** constructor. */
		public ColumnSortIcon(boolean downwards, Color baseColor) {
			this.downwards = downwards;
			this.lightShadow = baseColor.brighter();
			this.darkShadow = baseColor.darker().darker();
		}

		public void paintIcon(Component c, Graphics g, int xo, int yo) {
			int w = getIconWidth();
			int xw = xo+w-1;
			int h = getIconHeight();
			int yh = yo+h-1;

			if (downwards)
			{
				g.setColor(lightShadow);
				g.drawLine(xo+midw+1, yo, xw, yh-1);
				g.drawLine(xo, yh, xw, yh);
				g.setColor(darkShadow);
				g.drawLine(xo+midw-1, yo, xo, yh-1);
			}
			else
			{
				g.setColor(lightShadow);
				g.drawLine(xw, yo+1, xo+midw, yh);
				g.setColor(darkShadow);
				g.drawLine(xo+1, yo+1, xo+midw-1, yh);
				g.drawLine(xo, yo, xw, yo);
			}
		}

		public int getIconWidth(){
			return 2 * midw;
		}

		public int getIconHeight() {
			return getIconWidth()-1;
		}
	}

	/**
	 * This renderer use a delegation software design pattern to delegate
	 * this rendering of the table cell header to the real default render,
	 * however this renderer adds in an icon on the cells which are sorted.
	 **/
	class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TableHeaderRenderer(TableCellRenderer theRenderer) {
			defaultRenderer = theRenderer;
		}

		/**
		 * Renderer the specified Table Header cell
		 **/
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column)
		{
			Component theResult = defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus,
					row, column);
			if (theResult instanceof JLabel) {
				// Must clear the icon if not sorted column. This is a renderer
				// class used to render each column heading in turn.
				JLabel cell = (JLabel)theResult;
				Icon icon = null;
				if (column == sortedColumn) {
					if (sortAscending)
						icon = ascendingIcon;
					else
						icon = descendingIcon;
				}
				cell.setIcon(icon);
				// 2014-12-17 Added
				cell.setOpaque(true);
				//cell.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC
				//cell.setForeground(Color.WHITE);
				//cell.setBackground(new Color(255, 248, 220)); // 255 248 220 cornsilk1
				MatteBorder border = new MatteBorder(1, 1, 0, 0, Color.orange);
				cell.setBorder(border);
			}
			return theResult;
		}
	}

	// These icons are used to render the sorting images on the column header
	private static Icon ascendingIcon = null;
	private static Icon descendingIcon = null;
	private final static Icon TABLEICON = ImageLoader.getIcon("Table");

	/** Table component. */
	protected JTable table;
	/** Sortable model proxy. */
	private TableSorter sortedModel;
	/** Constructor will flip this. */
	private boolean sortAscending = true;
	/** Sort column is defined. */
	private int sortedColumn = 0;

    /**
     * Create a Jtable within a tab displaying the specified model.
     * @param model The model of Units to display.
     * @param mandatory Is this table view mandatory.
     * @param singleSelection Does this table only allow single selection?
     */
    public TableTab(final MonitorWindow window, MonitorModel model, boolean mandatory, boolean singleSelection) {
        super(model, mandatory, TABLEICON);

        // Can not create icons until UIManager is up and running
        if (ascendingIcon == null) {
            Color baseColor = UIManager.getColor("Label.background");

            ascendingIcon = new ColumnSortIcon(false, baseColor);
            descendingIcon = new ColumnSortIcon(true, baseColor);
        }

        // If the model is not ordered, allow user the facility
        if (!model.getOrdered()) {
            // Create a sortable model to act as a proxy
            sortedModel = new TableSorter(model);

            // Create scrollable table window
            table = new JTable(sortedModel) {
            	/** default serial id. */
				//private static final long serialVersionUID = 1L;

				/**
            	 * Overriding table change so that selections aren't cleared when rows are deleted.
            	 */
            	public void tableChanged(TableModelEvent e) {

            		if (e.getType() == TableModelEvent.DELETE) {
            			// Store selected row objects.
            			List<Object> selected = getSelection();

            			// Call super implementation to remove row and clear selection.
    					super.tableChanged(e);

    					// Reselect rows if row objects still around.
    					MonitorModel model = (MonitorModel) getModel();
    					Iterator<Object> i = selected.iterator();
    					while (i.hasNext()) {
    						Object selectedObject = i.next();
    						for (int x = 0; x < model.getRowCount(); x++) {
    							if (selectedObject.equals(model.getObject(x))) addRowSelectionInterval(x, x);
    						}
    					}
            		}
            		else super.tableChanged(e);
            	}

                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
                 */
                public String getToolTipText(MouseEvent e) {
                    return getCellText(e);
                };
            };


    		// call it a click to display details button when user double clicks the table
    		table.addMouseListener(
    			new MouseListener() {
    				public void mouseReleased(MouseEvent e) {}
    				public void mousePressed(MouseEvent e) {}
    				public void mouseExited(MouseEvent e) {}
    				public void mouseEntered(MouseEvent e) {}
    				public void mouseClicked(MouseEvent e) {
    					if (e.getClickCount() == 2 && !e.isConsumed()) {
    						window.displayDetails();
    					}
    				}
    			}
    		);
            sortedModel.addTableModelListener(table);

        	// 2014-12-30 Added setTableStyle()
            setTableStyle(table);

         	// Add a mouse listener for the mouse event selecting the sorted column
         	// Not the best way but no double click is provided on Header class
        	theHeader.addMouseListener(new MouseAdapter() {
        		public void mouseClicked(MouseEvent e) {
        			// Find the column at this point
        			int column = table.getTableHeader().columnAtPoint(e.getPoint());
        			setSortColumn(column);
        			table.getTableHeader().repaint();
        		}
        	});
        }
        else {
            // Simple JTable
            table = new JTable(model) {
            	/** default serial id. */
				//private static final long serialVersionUID = 1L;
				/**
            	 * Overriding table change so that selections aren't cleared when rows are deleted.
            	 */
            	public void tableChanged(TableModelEvent e) {

            		if (e.getType() == TableModelEvent.DELETE) {
            			// Store selected row objects.
            			List<Object> selected = getSelection();

            			// Call super implementation to remove row and clear selection.
    					super.tableChanged(e);

    					// Reselect rows if row objects still around.
    					MonitorModel model = (MonitorModel) getModel();
    					Iterator<Object> i = selected.iterator();
    					while (i.hasNext()) {
    						Object selectedObject = i.next();
    						for (int x = 0; x < model.getRowCount(); x++) {
    							if (selectedObject == model.getObject(x)) addRowSelectionInterval(x, x);
    						}
    					}
            		}
            		else super.tableChanged(e);
            	}

                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
                 */
                public String getToolTipText(MouseEvent e) {
                    return getCellText(e);
                };
            };
           	// 2015-01-13 Added setTableStyle()
            setTableStyle(table);
        }

        // Set single selection mode if necessary.
        if (singleSelection) table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a scrolled window and center it with the table
        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(new MarsPanelBorder());
        add(scroller, BorderLayout.CENTER);

        setName(model.getName());
        setSortColumn(0);

        // 2014-12-29 Added ColumnResizer
        final JTable ctable = table;
	    SwingUtilities.invokeLater(() -> ColumnResizer.adjustColumnPreferredWidths(ctable));
    }

	// 2014-12-30 Added setTableStyle()
    public void setTableStyle(JTable table) {

    	// Get the TableColumn header to display sorted column
    	theHeader = table.getTableHeader();
    	theRenderer = new TableHeaderRenderer(theHeader.getDefaultRenderer());
    	theHeader.setDefaultRenderer(theRenderer);

	    // 2014-11-11 Added auto resize
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.getTableHeader().setOpaque(false);
		table.getTableHeader().setBackground(new Color(205, 133, 63));//Color.ORANGE);
		table.getTableHeader().setForeground(new Color(255, 255, 120));
		table.getTableHeader().setFont( new Font( "Dialog", Font.BOLD, 12 ) );
		// Font doesn't get rendered yet
		table.setSelectionForeground(new Color( 0, 100 ,0)); // 0 100 0	006400	dark green
		table.setSelectionBackground(new Color(255, 255, 224)); // 255 255 224	LightYellow1
		// 255 228 225	MistyRose1
		table.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC
		table.setForeground(new Color(139, 71, 38)); // 139 71 38		sienna4
			table.setShowGrid(true);
	    table.setShowVerticalLines(true);
		table.setGridColor(new Color(222, 184, 135)); // 222 184 135burlywood
		table.setBorder(BorderFactory.createLineBorder(Color.orange,1)); // HERE

	}

    /**
     * Display property window anchored to a main desktop.
     *
     * @param desktop Main desktop owing the properties dialog.
     */
    public void displayProps(MainDesktopPane desktop) {
        TableProperties propsWindow = new TableProperties(getName(), table, desktop);
        propsWindow.show();
    }

    /**
     * This return the selected rows in the model that are current
     * selected in this view.
     *
     * @return array of row indexes.
     */
    protected List<Object> getSelection() {
        MonitorModel target = (sortedModel != null ? sortedModel : getModel());

        int indexes[] = {};
        if (table != null) indexes = table.getSelectedRows();
        ArrayList<Object> selectedRows = new ArrayList<Object>();
        for (int indexe : indexes) {
            Object selected = target.getObject(indexe);
            if (selected != null) selectedRows.add(selected);
        }

        return selectedRows;
    }

    /**
     * Get the cell contents under the MouseEvent, this will be displayed
     * as a tooltip.
     * @param e MouseEvent triggering tool tip.
     * @return Tooltip text.
     */
    private String getCellText(MouseEvent e) {
        Point p = e.getPoint();
        int column = table.columnAtPoint(p);
        int row = table.rowAtPoint(p);
        String result = null;
        if ((column >= 0) && (row >= 0)) {
            Object cell = table.getValueAt(row, column);
            if (cell != null) {
                result = cell.toString();
            }
        }
        return result;
    }

    /**
     * Remove this view.
     */
    public void removeTab() {
        super.removeTab();
        table = null;
        if (sortedModel != null) {
        	sortedModel.destroy();
        	sortedModel = null;
        }
    }

    private void setSortColumn(int index) {
        if (sortedModel != null) {
            if (sortedColumn == index) {
                sortAscending = !sortAscending;
            }
            sortedColumn = index;
            sortedModel.sortByColumn(sortedColumn, sortAscending);
        }
    }
}