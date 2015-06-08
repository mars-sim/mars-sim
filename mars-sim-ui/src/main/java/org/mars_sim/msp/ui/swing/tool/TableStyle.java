/**
 * Mars Simulation Project
 * TableStyle.java
 * @version 3.08 2015-06-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class TableStyle {

	private static JTableHeader theHeader;
	private static TableHeaderRenderer theRenderer;

	public TableStyle() {//JTable table) {

	}



    public static void setTableStyle(JTable table) {

    	// Get the TableColumn header to display sorted column
    	theHeader = table.getTableHeader();
    	theRenderer = new TableHeaderRenderer(theHeader.getDefaultRenderer());
    	theHeader.setDefaultRenderer(theRenderer);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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

}


/**
 * This renderer uses a delegation software design pattern to delegate
 * this rendering of the table cell header to the real default render
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
			int column) {

		Component theResult = defaultRenderer.getTableCellRendererComponent(
				table, value, isSelected, hasFocus,
				row, column);
		if (theResult instanceof JLabel) {
			// Must clear the icon if not sorted column. This is a renderer
			// class used to render each column heading in turn.
			JLabel cell = (JLabel) theResult;

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
