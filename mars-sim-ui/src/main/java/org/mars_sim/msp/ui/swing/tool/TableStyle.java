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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.javafx.MainScene;

public class TableStyle {

	private static JTableHeader theHeader;
	private static TableHeaderRenderer theRenderer;
	private static Color border = Color.orange;
	private static Color hBack = new Color(205, 133, 63); // 205, 133, 63 mud orange
	private static Color hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow

	public TableStyle() {

	}

	public static Color getBorderColor() {
		return border;
	}

	public static Color getHeaderForegroundColor() {
		return hFore;
	}

	public static Color getHeaderBackgroundColor() {
		return hBack;
	}


    public static void setTableStyle(JTable table) {

    	int theme = MainScene.getTheme();

    	String themeName = null;
    	Color back = null;
    	Color fore = null;

    	Color selBack = null;
    	Color selFore = null;

    	Color grid = null;

		// 255 228 225	MistyRose1

    	if (theme == 1) { // standard nimrod
    		hBack = new Color(205, 133, 63); // 205, 133, 63 mud orange
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = Color.GRAY;
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.orange;
    		themeName = "Standard Nimrod";
    	}
    	else if (theme == 2) { // LightTabaco + olive
    		hBack = new Color(33, 66, 0); // 33, 66, 0 dark green
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255); // white
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4 dark brown orange
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
    		border = Color.GRAY;
    		themeName = "LightTabaco";
    	}
    	else if (theme == 3) { // Burdeos
    		hBack = new Color(117, 0, 0); // 117, 0, 0 pale dark red
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.RED;
    		themeName = "Burdeos";
    	}
    	else if (theme == 4) { // DarkTabaco
    		hBack = new Color(34, 23, 9); // 34, 23, 9 very very dark orange or use 9, 33, 34 very very dark cyan
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.GRAY;
    		themeName = "DarkTabaco";
    	}
    	else if (theme == 5) { // darkgrey + lime green
    		hBack = new Color(94, 106, 96); // 94, 106, 96 Very dark grayish lime green.
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.GREEN;
    		themeName = "DarkGrey";
    	}
    	else if (theme == 6) { // night + purple
    		hBack = new Color(87, 87, 211); // 87, 87, 211 light purple
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.DARK_GRAY;
    		themeName = "Night";
    	}
    	else if (theme == 7) { // snow + skyblue
    		hBack = new Color(147, 147, 147); // 147, 147, 147 mid grey
    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    		back = new Color(255, 255, 255);
    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
    		selBack = new Color( 0, 100 ,0); // 0 100 0	006400	dark green
    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    		grid = new Color(222, 184, 135); // 222 184 135 burlywood
    		border = Color.LIGHT_GRAY;
    		themeName = "Snow";
    	}


    	// Get the TableColumn header to display sorted column
    	theHeader = table.getTableHeader();
    	theRenderer = new TableHeaderRenderer(theHeader.getDefaultRenderer());
    	theHeader.setDefaultRenderer(theRenderer);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		theHeader.setOpaque(false);
    	// TODO: why is it NOT working?
		//theHeader.setBackground(hBack);
		//theHeader.setForeground(hFore);
		//theHeader.repaint();

		theHeader.setFont( new Font( "Dialog", Font.BOLD, 12 ) );

		table.setForeground(fore);
		table.setBackground(back);

		table.setSelectionForeground(selFore);
		table.setSelectionBackground(selBack);

		table.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC

		table.setShowGrid(true);
	    table.setShowVerticalLines(true);
		table.setGridColor(grid);
		table.setBorder(BorderFactory.createLineBorder(border, 1));

		//MainScene.notifyThemeChange(themeName);
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
			MatteBorder border = new MatteBorder(1, 1, 0, 0, TableStyle.getBorderColor());
			cell.setBorder(border);
		}


		JTableHeader tableHeader = table.getTableHeader();
	    if (tableHeader != null) {
	    	// TODO: why is it NOT working?
	    	tableHeader.setForeground(TableStyle.getHeaderForegroundColor());
	    	tableHeader.setBackground(TableStyle.getHeaderBackgroundColor());
	    }

		return theResult;
	}
}
