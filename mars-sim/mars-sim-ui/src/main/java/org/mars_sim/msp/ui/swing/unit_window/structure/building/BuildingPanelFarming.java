/**
 * Mars Simulation Project
 * FarmingBuildingPanel.java
 * @version 3.07 2014-11-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.text.WordUtils;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Crop;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The FarmingBuildingPanel class is a building function panel representing 
 * the crop farming status of a settlement building.
 */
public class BuildingPanelFarming
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The farming building. */
	private Farming farm;
	/** The number of farmers label. */
	private JLabel farmersLabel;
	/** The number of crops label. */
	private JLabel cropsLabel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;

	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;

	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	// 2014-11-20 Added tooltip for crops 
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(farm.getBuilding(), desktop);

		// Initialize data members
		this.farm = farm;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Prepare farming label
		JLabel farmingLabel = new JLabel("Farming", JLabel.CENTER);
		labelPanel.add(farmingLabel);

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmersLabel = new JLabel("Number of Farmers: " + farmersCache, JLabel.CENTER);
		labelPanel.add(farmersLabel);

		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsLabel = new JLabel("Number of Crops: " + cropsCache, JLabel.CENTER);
		labelPanel.add(cropsLabel);

		// Create scroll panel for crop table
		JScrollPane cropScrollPanel = new JScrollPane();
		// 2014-10-10 mkung: increased the height from 100 to 130 to make the first 5 rows of crop FULLY visible
		cropScrollPanel.setPreferredSize(new Dimension(200, 130));
		add(cropScrollPanel, BorderLayout.CENTER);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		JTable cropTable = new JTable(cropTableModel){
			private static final long serialVersionUID = 1L;
			// 2014-11-20 Implement Table Cell ToolTip for crops           
            public String getToolTipText(MouseEvent e) {
                String name = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                java.util.List<Crop> crops;
    			StringBuilder result = new StringBuilder("<html>");
    			
                try {
                        crops = farm.getCrops();
                        Crop crop = crops.get(rowIndex);
                        double time;
                        double mass0, mass1;
                        double water;
                        String cropName, cat;
                        cropName = crop.getCropType().getName();
                        cat = crop.getCropType().getCropCategory();
                    	mass0 = crop.getCropType().getEdibleBiomass();
                    	water = 100 * crop.getCropType().getEdibleWaterContent();
                    	mass1 = crop.getCropType().getInedibleBiomass();
                    	time = crop.getCropType().getGrowingTime() /1000;
	                	result.append("&emsp;&nbsp;Crop Name:&emsp;");
	                	result.append(cropName);
	                	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;");
	                	result.append(cat);
	                	result.append("<br>&emsp;Edible Mass:&emsp;");
	                	result.append(mass0).append(" kg");
	                	result.append("<br>&nbsp;Inedible Mass:&emsp;");
	                	result.append(mass1).append(" kg");              
	                	result.append("<br>&nbsp;Growing Days:&emsp;");
	                	result.append(time); 
	                	result.append("<br>&nbsp;Water Content:&emsp;");
	                	result.append(water).append(" %"); 	                	
                    //}
                    //if (tip == "Soybean") {

                    //"use soybeans to make soy products";
                    //if (tip == "Kidney Bean")
                    	//tip = "use soybeans to make soy products";
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }
    			result.append("</html>");
    			return result.toString();
            }
        }; // end of JTable
		
		cropTable.setCellSelectionEnabled(false);
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		// 2014-10-10 mkung: added column 4 showing the crop's category
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		cropScrollPanel.setViewportView(cropTable);
	}

	
	/**
	 * Update this panel
	 */
	public void update() {

		// Update farmers label if necessary.
		if (farmersCache != farm.getFarmerNum()) {
			farmersCache = farm.getFarmerNum();
			farmersLabel.setText("Number of Farmers: " + farmersCache);
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsLabel.setText("Number of Crops: " + cropsCache);
		}

		// Update crop table.
		cropTableModel.update();
	}

	/** 
	 * Internal class used as model for the crop table.
	 */
	private static class CropTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Farming farm;
		private java.util.List<Crop> crops;
		private ImageIcon redDot;
		private ImageIcon yellowDot;
		private ImageIcon greenDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = farm.getCrops();
			redDot = ImageLoader.getIcon("RedDot");
			yellowDot = ImageLoader.getIcon("YellowDot");
			greenDot = ImageLoader.getIcon("GreenDot");
		}

		public int getRowCount() {
			return crops.size();
		}

		// 2014-10-10 mkung: change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) dataType = String.class;			
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			else if (columnIndex == 1) return "Crop";
			else if (columnIndex == 2) return "Phase";
			else if (columnIndex == 3) return "Growth";
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) return "Category";			
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			String phase = crop.getPhase();
			// 2014-10-10 mkung: added the crop's category
			String category = crop.getCategory();

			if (column == 0) {
				double condition = crop.getCondition();
				if (condition > ((double) 2 / (double) 3)) return greenDot;
				else if (condition > ((double) 1 / (double) 3)) return yellowDot;
				else return redDot;
			}
			else if (column == 1) return crop.getCropType().getName();
			else if (column == 2) return phase;
			else if (column == 3) {
				int growth = 0;
				if (phase.equals(Crop.GROWING)) {
					double growingCompleted = crop.getGrowingTimeCompleted() / crop.getCropType().getGrowingTime();
					growth = (int) (growingCompleted * 100D);
				}
				else if (phase.equals(Crop.HARVESTING) || phase.equals(Crop.FINISHED)) growth = 100;
				return String.valueOf(growth) + "%";
			}
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (column == 4) return category;
			else return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}
}
