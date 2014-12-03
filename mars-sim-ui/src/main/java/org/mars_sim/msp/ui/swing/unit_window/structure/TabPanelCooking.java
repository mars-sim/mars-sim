/**
 * Mars Simulation Project
 * TabPanelCooking.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking.ReadyMeal;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking.ReadyMealMenu;

/** 
 * This is a tab panel for displaying a settlement's Food Menu.
 */
public class TabPanelCooking
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(TabPanelCooking.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.COOKING;

	// Data Members
    private ReadyMealMenu mealMenu;
    // Cache
	//private int numMealsCache;
	//private int mealQualityCache;
	private CookingTableModel cookingTableModel;
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelCooking(Unit unit, MainDesktopPane desktop) { 

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCooking.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelCooking.tooltip"), //$NON-NLS-1$
			unit, desktop
		);
		
		//Cooking kitchen = null;
		Settlement settlement = (Settlement) unit;
		
		// create a new mealMenu for this settlement
		mealMenu = new ReadyMealMenu();

		mealMenu = updateMealMenu(settlement, mealMenu);

		// Prepare cooking label panel.
		JPanel cookingLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(cookingLabelPanel);

		// Prepare cooking label.
		JLabel cookingLabel = new JLabel(Msg.getString("TabPanelCooking.label"), JLabel.CENTER); //$NON-NLS-1$
		cookingLabelPanel.add(cookingLabel);


		// Create scroll panel for the outer table panel.
		JScrollPane cookingScrollPane = new JScrollPane();
		cookingScrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		cookingScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(cookingScrollPane,BorderLayout.CENTER);

		// Prepare outer table panel.
		JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		outerTablePanel.setBorder(new MarsPanelBorder());
		cookingScrollPane.setViewportView(outerTablePanel);

		// Prepare cooking table panel.
		JPanel cookingTablePanel = new JPanel(new BorderLayout(0, 0));
		outerTablePanel.add(cookingTablePanel);
		// cookingScrollPanel.setViewportView(cookingTablePanel);

		// Prepare cooking table model.
		cookingTableModel = new CookingTableModel(settlement);

		// Prepare cooking table.
		JTable cookingTable = new JTable(cookingTableModel);
		cookingTable.setCellSelectionEnabled(false);
		cookingTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		//cookingTable.getColumnModel().getColumn(0).setPreferredWidth(15);
		cookingTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		cookingTable.getColumnModel().getColumn(1).setPreferredWidth(47);
		cookingTable.getColumnModel().getColumn(2).setPreferredWidth(45);
		cookingTable.getColumnModel().getColumn(3).setPreferredWidth(45);
		cookingTablePanel.add(cookingTable.getTableHeader(), BorderLayout.NORTH);
		cookingTablePanel.add(cookingTable, BorderLayout.CENTER);
	}

	public ReadyMealMenu updateMealMenu(Settlement settlement, ReadyMealMenu mealMenu) {
		
		List<CookedMeal> mealList = new ArrayList<CookedMeal>();
		
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		
        while (i.hasNext()) { 		// for each building's kitchen in the settlement
            Building building = i.next();
            
        	if (building.hasFunction(BuildingFunction.COOKING)) {
        		
				Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
	    		mealList = kitchen.getCookedMealList();
	    		Iterator<CookedMeal> j = mealList.iterator();
	    		
	    			while (j.hasNext()) { 		// for each CookedMeal in a kitchen
	    				CookedMeal nowMeal = j.next();
	    	    		String nowName = nowMeal.getName();
	    		    	int nowServings = kitchen.getMealServings(mealList, nowMeal);
	    	    		//int numKitchens;
	    	    		int nowQuality = nowMeal.getQuality();
	    	    		MarsClock nowExpiration = nowMeal.getExpirationTime();
	    	    		int size = mealMenu.size();
	    	    		//logger.info(" updateMealMenu() : size was " + size);
	    	    		
	    	    		if (size == 0) // size = 0 signify the beginning of each day
	    	    						// when # of mealMenu is reset to zero.
	    	    		  	mealMenu.add(nowName, nowServings, nowQuality, nowExpiration);
	    	    		else {
	    	    			List<ReadyMeal> readyMealList = mealMenu.getReadyMealList();
	    	    			Iterator<ReadyMeal> k = readyMealList.iterator();
	    	    			
	    	    			while (k.hasNext()) {		// for each readyMeal on the list
	    	    				ReadyMeal existingMeal = k.next();
	    	    				String existingMealName = existingMeal.getName();
	    	    				MarsClock existingExpiration = existingMeal.getExpiration();
	    	    				if (nowName == existingMealName) 
	    	    					//if ( nowExpiration != existingExpiration) 
	    	    						// if identical meal, do nothing
	    	    				    		mealMenu.change(nowName, nowServings, nowQuality);
	    	    				else mealMenu.add(nowName, nowServings, nowQuality, nowExpiration);
	    	    			} // end of while (k.hasNext()) {
	    	    		}
	    	    		
	    	    		//int size2 = mealMenu.size();
	    	    		//logger.info(" updateMealMenu() : size is now " + size2);
	    			}	
	        	}  	
    		} // end of while (i.hasNext()) {
        return mealMenu;
	}
	
	
	/**
	 * Updates the info on this panel.
	 */
	public void update() {
	
		List<ReadyMeal> readyMealList = mealMenu.getReadyMealList();
		Iterator<ReadyMeal> i = readyMealList.iterator();
		
		//logger.info(" update() : size was " + readyMealList.size());
		while (i.hasNext()) {		// for each readyMeal on the list
			
			ReadyMeal existingMeal = i.next();
           //logger.info(" Meal : " + meal.getName());
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			int oldDayCache = existingMeal.getExpiration().getSolOfMonth();
			int newDayCache  = currentTime.getSolOfMonth();

			try {
				if (currentTime.getSolOfMonth() != existingMeal.getExpiration().getSolOfMonth())	
					//logger.info(" Today is sol " + newDayCache);
					//logger.info(" The meal was made on sol " + oldDayCache);
					oldDayCache = newDayCache;
					i.remove();
           	} catch (Exception e) {}
		}
		//logger.info(" update() : size is now " + readyMealList.size());
		// Update cooking table.
		cookingTableModel.update();
	}

	/** 
	 * Internal class used as model for the cooking table.
	 */
	private class CookingTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		//private ReadyMealMenu updatedMealMenu;
		//private java.util.List<Building> buildings;
		//private ImageIcon dotRed; // ingredients missing
		//private ImageIcon dotYellow; // meal not available
		//private ImageIcon dotGreen; // meal available

		private CookingTableModel(Settlement settlement) {
		//, ReadyMealMenu updatedMealMenu) {
			this.settlement = settlement;
			//this.updatedMealMenu = updatedMealMenu;
			//buildings = settlement.getBuildingManager().getBuildings();
			//dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			//dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			//dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
		}

		public int getRowCount() {
			return mealMenu.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			//if (columnIndex == 0) dataType = ImageIcon.class;
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			
			String[] columnNames = {
				    "<html>Meal<br>Name</html>",
				    "<html># of<br>Servings</html>",
				    "<html>Best<br>Quality</html>",
				    "<html>Worst<br>Quality</html>"
				};
			
			//if (columnIndex == 0) return Msg.getString("TabPanelCooking.column.s"); //$NON-NLS-1$
			if (columnIndex == 0) return columnNames[0];
					// Msg.getString("TabPanelCooking.column.nameOfMeal"); //$NON-NLS-1$
			else if (columnIndex == 1) return columnNames[1];
					//Msg.getString("TabPanelCooking.column.numberOfServings"); //$NON-NLS-1$
			else if (columnIndex == 2) return columnNames[2];
					//Msg.getString("TabPanelCooking.column.bestQuality"); //$NON-NLS-1$
			else if (columnIndex == 3) return columnNames[3];
					// Msg.getString("TabPanelCooking.column.worstQuality"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			//boolean haveAllIngredients = false;
			/* if (column == 0) {
				if (haveAllIngredients) 
					return dotGreen;
				else return dotRed;
			} else */
			if (column == 0) 
				return mealMenu.getMealName(row);
			
			else if (column == 1) 
				return mealMenu.getNumServings(row);
			
			else if (column == 2) 
				return mealMenu.getBestQuality(row);
			
			else if (column == 3) 
				return mealMenu.getWorstQuality(row);
			
			else return null;
		}

		// TODO: decide in what situation it needs update and at what time ?
		// update every second or after each meal or once a day ?
		public void update() {
			mealMenu = updateMealMenu(settlement, mealMenu);
			fireTableDataChanged();
		}
	}
}