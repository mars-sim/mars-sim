/**
 * Mars Simulation Project
 * FarmingBuildingPanel.java
 * @version 2.75 2003-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Farming;
import org.mars_sim.msp.ui.standard.*;

/**
 * The FarmingBuildingPanel class is a building function panel representing 
 * the crop farming status of a settlement building.
 */
public class FarmingBuildingPanel extends BuildingFunctionPanel {
    
    // Data members
    private Farming farm; // The farming building.
    private JLabel farmersLabel; // The number of farmers label.
    private JLabel cropsLabel; // The number of crops label.
    private CropTableModel cropTableModel; // Table model for crop info.
    
    // Data cache
    private int farmersCache;  // The number of farmers cache.
    private int cropsCache; // The number of crops cache.
    
    /**
     * Constructor
     *
     * @param farm the farming building this panel is for.
     * @param desktop The main desktop.
     */
    public FarmingBuildingPanel(Farming farm, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super((Building) farm, desktop);
        
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
        cropScrollPanel.setPreferredSize(new Dimension(160, 100));
        add(cropScrollPanel, BorderLayout.CENTER);
        
        // Prepare crop table model
        cropTableModel = new CropTableModel(farm);
        
        // Prepare crop table
        JTable cropTable = new JTable(cropTableModel);
        cropTable.setCellSelectionEnabled(false);
        cropTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        cropTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        cropTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        cropTable.getColumnModel().getColumn(3).setPreferredWidth(40);
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
    private class CropTableModel extends AbstractTableModel {
        
        Farming farm;
        java.util.List crops;
        ImageIcon redDot;
        ImageIcon yellowDot;
        ImageIcon greenDot;
        
        private CropTableModel(Farming farm) {
            this.farm = farm;
            crops = farm.getCrops();
            redDot = new ImageIcon("images/RedDot.gif");
            yellowDot = new ImageIcon("images/YellowDot.gif");
            greenDot = new ImageIcon("images/GreenDot.gif");
        }
        
        public int getRowCount() {
            return crops.size();
        }
        
        public int getColumnCount() {
            return 4;
        }
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = ImageIcon.class;
            else if (columnIndex == 1) dataType = String.class;
            else if (columnIndex == 2) dataType = String.class;
            else if (columnIndex == 3) dataType = String.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "C";
            else if (columnIndex == 1) return "Crop";
            else if (columnIndex == 2) return "Phase";
            else if (columnIndex == 3) return "Growth";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            
            Crop crop = (Crop) crops.get(row);
            String phase = crop.getPhase();
            
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
            else return "unknown";
        }
        
        public void update() {
            if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
            fireTableDataChanged();
        }
    }
}
