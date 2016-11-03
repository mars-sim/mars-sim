/**
 * Mars Simulation Project
 * TableStyle.java
 * @version 3.1.0 2016-10-27
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.javafx.MainScene;

public class TableStyle extends ZebraJTable{

	private static final long serialVersionUID = 1L;
	
	private static int themeCache;
	//private JTable table;
	private static JTableHeader theHeader;
	private static TableHeaderRenderer theRenderer;
	private static Color border;// = Color.orange;
	private static Color hBack;// = new Color(205, 133, 63); // 205, 133, 63 mud orange
	private static Color hFore;// = new Color(255, 255, 120); // 255, 255, 120 very light yellow

	public TableStyle() {
		super();
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

/*
	public static void setTableStyle(JTable table) {
		//Object[][] rowData = table.getModel().get;
		//Object[] columnNames = null;
		//ZebraJTable t = new ZebraJTable( rowData, columnNames);
		ZebraJTable t = new ZebraJTable( table.getModel());
		t.setBackground( Color.darkGray );
		t.setForeground( Color.white );
		t.setSelectionBackground( Color.yellow );
		t.setSelectionForeground( Color.black );
		//JScrollPane scrollList = new JScrollPane( table );

	}

*/
	/*
	 * Sets theme color for JTable headers and rows and columns
	 */
    public static JTable setTableStyle(JTable table) {
	   	//logger.info("setTableStyle() is on " + Thread.currentThread().getName() );

    	//JTable t = table;
		//SwingUtilities.invokeLater(() -> {	
			// 2016-06-17 Added checking for OS. 
			// Note: NIMROD theme lib doesn't work on linux 
			if (MainScene.OS.equals("linux")) {
			   	//ZebraJTable z = new ZebraJTable(table.getModel());
				//table = (JTable) z;
				;
			}
			else 
				//SwingUtilities.invokeLater(() -> 
				table = editHeader(table);
		//});
		
		return table;
    }
	
    public static JTable editHeader(JTable table) {

	   	ZebraJTable z = new ZebraJTable(table.getModel());
		
    	int theme = MainScene.getTheme();

    	//if (themeCache != theme) {
    	//	themeCache = theme;
    		
	    	Color back = null;
	    	Color fore = null;
	
	    	Color selBack = null;
	    	Color selFore = null;
	
	    	Color grid = null;
	
	    	String themeName = null;
	
			// 255 228 225	MistyRose1
	
	    	if (theme == 1) { // LightTabaco + olive
	    		hBack = new Color(148, 169, 80); // 82, 71, 39 Pantone Coated brownish-green // 33, 66, 0 dark green
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255); // white
	    		fore = new Color(148, 169, 80); // 139 71 38	sienna4 dark brown orange
	    		selBack = new Color(110,139,61); // 110,139,61	pale olive green
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		//border = Color.GRAY;
	    		themeName = "LightTabaco";
	    	}
	    	else if (theme == 2) { // Burdeos
	    		hBack = new Color(119, 46, 47); // 187, 79, 81 Pantone Metallic Red, //117, 0, 0 pale dark red
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(119, 46, 47); // 139 71 38	sienna4
	    		selBack = new Color(167, 81, 84); // pale red // 139,26,26); // 139,26,26 firebrick
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = Color.black;
	    		themeName = "Burdeos";
	    	}
	    	else if (theme == 3) { // DarkTabaco // grey brown
	    		hBack = new Color(153, 78, 37); // 49, 38, 29 Pantone Coated Black // 34, 23, 9 very very dark orange or use 9, 33, 34 very very dark cyan
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(153, 78, 37); //
	    		selBack = new Color(139,87,66); // 139,87,66 lightsalmon 4
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = Color.WHITE;
	    		themeName = "DarkTabaco";
	    	}
	    	else if (theme == 4) { // darkgrey + lime green
	    		hBack = new Color(94, 106, 96); // 94, 106, 96 Very dark grayish lime green.
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(94, 106, 96); // 139 71 38	sienna4
	    		selBack = new Color(139,119,101); // 139,119,101 peachpuff 4
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = Color.GRAY;
	    		themeName = "DarkGrey";
	    	}
	    	else if (theme == 5) { // night + purple
	    		hBack = new Color(87, 87, 211); // 87, 87, 211 light purple
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(87, 87, 211); // 139 71 38	sienna4
	    		selBack = new Color(22,55,139); // 22	55	139	mediumorchid 4
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = Color.DARK_GRAY;
	    		themeName = "Night";
	    	}
	    	else if (theme == 6) { // snow + skyblue
	    		
	    		// see default colors for nimbus 
	    		// https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html#primary
	    		
	    		hBack = new Color(57, 105, 138);// (57, 105, 138) is navy blue; (50, 145,210);//(31, 151, 229); // 100, 149, 237 cornflowerblue , 147, 147, 147 mid grey
	    		hFore = new Color(164, 209, 242); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(42, 79, 105); //(42, 205, 60) is navy blue; (31, 151, 229); // 139 71 38	sienna4
	    		selBack = new Color(144, 208, 229); // (144, 208, 229) is pale cyan; (70, 130, 180) is steelblue/dark sky blue
	    		selFore = new Color(255, 255, 255); // (86, 105, 119) is grey blue; (133, 164, 242) is very pale light blue; 255 255 224 is LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = new Color(57, 105, 138);//Color.LIGHT_GRAY;
	    		
	    		
	    		themeName = "Snow Blue";
	    	}
	
	    	else if (theme == 7) { // standard nimrod
	    		hBack = new Color(101,75,0); // (229, 171, 0) is bright yellow orange // 205, 133, 63 mud orange
	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
	    		back = new Color(255, 255, 255);
	    		fore = new Color(139, 71, 38); // 139 71 38	sienna4
	    		selBack = new Color(189,183,107); // 189,183,107 darkkhaki
	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
	    		border = new Color(101,75,0); //Color.orange;
	    		themeName = "Mud Orange"; //Standard Nimrod";
	    	}
	
		    // Get the TableColumn header to display sorted column
		    theHeader = table.getTableHeader();
		    theRenderer = new TableHeaderRenderer(theHeader.getDefaultRenderer());
		    theHeader.setDefaultRenderer(theRenderer);		
			theHeader.setOpaque(false);	
			theHeader.setFont( new Font( "Dialog", Font.BOLD, 12 ) );	
			
	    	// TODO: why is it NOT working?
			if (hBack != null) theHeader.setBackground(hBack);
			if (hFore != null) theHeader.setForeground(hFore);
		
			theHeader.repaint();	
			
			/*	   	
	    	JTableHeader header = null;
	    	if (table.getTableHeader() != null)
	    		header = table.getTableHeader();

			header.setFont(new Font("Dialog", Font.BOLD, 12));
			header.setBackground(new java.awt.Color(0, 167, 212));
			header.setForeground(java.awt.Color.white);
	*/
			
	/*
				JTableHeader tableHeader = table.getTableHeader();
			    if (tableHeader != null) {
			    	tableHeader.setForeground(TableStyle.getHeaderForegroundColor());
			    	tableHeader.setBackground(TableStyle.getHeaderBackgroundColor());
			    }
	*/
				// 2015-09-24 Align the content to the center of the cell
				//DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
				//renderer.setHorizontalAlignment(SwingConstants.CENTER);
				//renderer.setHorizontalAlignment(JLabel.CENTER);
				//table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		    	//for (int i = 0; i < table.getColumnCount(); ++i) {
		        //    table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		        //}
		
	
			if (fore != null) table.setForeground(fore);
			if (back != null) table.setBackground(back);
		
			if (selFore != null) table.setSelectionForeground(selFore);
			if (selBack != null) table.setSelectionBackground(selBack);

			if (grid != null) table.setGridColor(grid);

			//table.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC
		
			table.setShowGrid(true);
			table.setShowVerticalLines(true);			
			table.setBorder(BorderFactory.createLineBorder(border, 1));			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    	
			return (JTable) z;
	    //}
    	
    	//else
    	//	return (JTable) z;
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

		// userful For JTables in person unit windows
		if (theResult instanceof JLabel) {
			// Must clear the icon if not sorted column. This is a renderer
			// class used to render each column heading in turn.
			JLabel cell = (JLabel) theResult;

			//cell.setHorizontalAlignment(SwingConstants.CENTER); // not useful
			//cell.setHorizontalAlignment(JLabel.CENTER); // not useful

			// 2014-12-17 Added
			cell.setOpaque(true);
			//cell.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC
			//cell.setForeground(Color.WHITE);
			//cell.setBackground(new Color(255, 248, 220)); // 255 248 220 cornsilk1
			MatteBorder border = new MatteBorder(1, 1, 0, 0, TableStyle.getBorderColor());
			cell.setBorder(border);
		}

		//JTableHeader tableHeader = table.getTableHeader();
	    //if (tableHeader != null) {
	    //	tableHeader.setForeground(TableStyle.getHeaderForegroundColor());
	    //	tableHeader.setBackground(TableStyle.getHeaderBackgroundColor());
	    //}

		return theResult;
	}
}
