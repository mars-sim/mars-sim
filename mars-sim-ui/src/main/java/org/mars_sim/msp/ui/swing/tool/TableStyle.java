/**
 * Mars Simulation Project
 * TableStyle.java
 * @version 3.1.0 2016-10-27
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
@SuppressWarnings("serial")
public class TableStyle extends ZebraJTable{

	//private static JTableHeader theHeader;
	//private static TableHeaderRenderer theRenderer;
	private static Color border;// = Color.orange;
	private static Color hBack;// = new Color(205, 133, 63); // 205, 133, 63 mud orange
	private static Color hFore;// = new Color(255, 255, 120); // 255, 255, 120 very light yellow

	private static Color back = null;
	private static Color fore = null;

	private static Color selBack = null;
	private static Color selFore = null;

	private static Color grid = null;

	private static String themeName = null;

	private static int themeCache = -1;
	
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
	 * Sets theme color for JTable headers and rows and columns
	 */
    public static void setTableStyle(JTable table) {
	   	//logger.info("setTableStyle() is on " + Thread.currentThread().getName() );

//        if (MainWindow.OS.contains("linux"))
//        	table.setRowHeight(24);
//        else 
        table.setRowHeight(24);
        
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
    	int theme = 0;//MainScene.getTheme();

		// 255 228 225	MistyRose1

	    // Get the TableColumn header to display sorted column
	    //theHeader = table.getTableHeader();
	    //theRenderer = new TableHeaderRenderer(theHeader.getDefaultRenderer());
	    //theHeader.setDefaultRenderer(theRenderer);
    	
	    // 2017-01-19 disable this will allow a gradient color on the header
	    //theHeader.setOpaque(false);
		//theHeader.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
		//theHeader.setBorder(BorderFactory.createLineBorder(border, 1));
		//theHeader.setBorder(new MatteBorder(1, 1, 0, 0, TableStyle.getBorderColor()));
    	
    	// TODO: why is it NOT working?
		//if (hBack != null) theHeader.setBackground(hBack);
		//if (hFore != null) theHeader.setForeground(hFore);

    	SwingUtilities.invokeLater(()-> {
    		
    		if (themeCache != theme) {
    			
    		   	if (theme == 0) { // snow + skyblue
    	    		
    	    		// see default colors for nimbus
    	    		// https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html#primary
    	    		//hFore = new Color(57, 105, 138);// ;//(57, 105, 138) is dark navy blue; (50, 145,210);//(31, 151, 229); // 100, 149, 237 cornflowerblue , 147, 147, 147 mid grey
    	    		//hBack = new Color(198, 217, 217); // Color(198, 217, 217) is super pale light blue 255, 255, 120 very light yellow
    	    		back = new Color(255, 255, 255);
    	    		fore = new Color(42, 79, 105);//(198, 217, 217); ;//(42, 79, 105); //(42, 205, 60) is navy blue; (31, 151, 229); // 139 71 38	sienna4
    	    		selBack = new Color(70, 130, 180); // (144, 208, 229) is pale bright cyan; (70, 130, 180) is steelblue/dark sky blue
    	    		selFore = new Color(255, 255, 255); // (86, 105, 119) is grey blue; (133, 164, 242) is very pale light blue; 255 255 224 is LightYellow1
    	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
    	    		border = Color.WHITE; //Color(165, 247, 233 is bright light blue//new Color(57, 105, 138);//Color.LIGHT_GRAY;
    	    		themeName = "nimbus";

    	    	}

    	    	else if (theme == 1) { // LightTabaco + olive
    	    		hBack = new Color(148, 169, 80); // 82, 71, 39 Pantone Coated brownish-green // 33, 66, 0 dark green
    	    		hFore = new Color(255, 255, 120); // 255, 255, 120 very light yellow
    	    		back = new Color(255, 255, 255); // white
    	    		fore = new Color(148, 169, 80); // 139 71 38	sienna4 dark brown orange
    	    		selBack = new Color(110,139,61); // 110,139,61	pale olive green
    	    		selFore = new Color(255, 255, 224); // 255 255 224	LightYellow1
    	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
    	    		border = Color.WHITE;//Color.GRAY;
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
    	    		hBack = new Color(57, 105, 138);// ;//(57, 105, 138) is navy blue; (50, 145,210);//(31, 151, 229); // 100, 149, 237 cornflowerblue , 147, 147, 147 mid grey
    	    		hFore = new Color(198, 217, 217); // 255, 255, 120 very light yellow
    	    		back = new Color(255, 255, 255);
    	    		fore = new Color(42, 79, 105);//(198, 217, 217); ;//(42, 79, 105); //(42, 205, 60) is navy blue; (31, 151, 229); // 139 71 38	sienna4
    	    		selBack = new Color(144, 208, 229); // (144, 208, 229) is pale cyan; (70, 130, 180) is steelblue/dark sky blue
    	    		selFore = new Color(255, 255, 255); // (86, 105, 119) is grey blue; (133, 164, 242) is very pale light blue; 255 255 224 is LightYellow1
    	    		grid = Color.WHITE; //new Color(222, 184, 135); // 222 184 135 burlywood very soft orange
    	    		border = new Color(165, 247, 233);//Color.WHITE;//new Color(57, 105, 138);//Color.LIGHT_GRAY;
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
    	    		border = Color.WHITE;//new Color(243, 247, 136);//Color.WHITE; //new Color(101,75,0); //Color.orange;
    	    		themeName = "Mud Orange"; //Standard Nimrod";
    	    	}
    		   	
				if (fore != null) table.setForeground(fore);
				if (back != null) table.setBackground(back);
		
				if (selFore != null) table.setSelectionForeground(selFore);
				if (selBack != null) table.setSelectionBackground(selBack);
		
				if (grid != null) table.setGridColor(grid);
		
				//table.setFont(new Font("Helvetica Bold", Font.PLAIN,12)); //new Font("Arial", Font.BOLD, 12)); //Font.ITALIC
				table.setShowGrid(true);
				table.setShowVerticalLines(true);
				//table.setBorder(BorderFactory.createLineBorder(border, 1));
				table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
				table.repaint();
				
				themeCache = theme;
    		}
    	});
    }

}

