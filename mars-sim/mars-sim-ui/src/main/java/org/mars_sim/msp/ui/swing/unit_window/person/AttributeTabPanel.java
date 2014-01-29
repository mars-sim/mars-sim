/**
 * Mars Simulation Project
 * AttributeTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;


/** 
 * The AttributeTabPanel is a tab panel for the natural attributes of a person.
 */
public class AttributeTabPanel extends TabPanel {
    
    private AttributeTableModel attributeTableModel;
    
    /**
     * Constructor
     *
     * @param person the person.
     * @param desktop the main desktop.
     */
    public AttributeTabPanel(Person person, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Attributes", null, "Natural Attributes", person, desktop);
        
        // Create attribute label panel.
        JPanel attributeLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(attributeLabelPanel);
        
        // Create attribute label
        JLabel attributeLabel = new JLabel("Natural Attributes", JLabel.CENTER);
        attributeLabelPanel.add(attributeLabel);
        
        // Create attribute scroll panel
        JScrollPane attributeScrollPanel = new JScrollPane();
        attributeScrollPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(attributeScrollPanel);
        
        // Create attribute table model
        attributeTableModel = new AttributeTableModel(person);
            
        // Create attribute table
        JTable attributeTable = new JTable(attributeTableModel);
        attributeTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
        attributeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        attributeTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        attributeTable.setCellSelectionEnabled(false);
        // attributeTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
        attributeScrollPanel.setViewportView(attributeTable);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {}
    
    /** 
     * Internal class used as model for the attribute table.
     */
    private static class AttributeTableModel extends AbstractTableModel {
        
        NaturalAttributeManager manager;
        
        private AttributeTableModel(Person person) {
            manager = person.getNaturalAttributeManager();
        }
        
        public int getRowCount() {
            return manager.getAttributeNum();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            if (columnIndex == 1) dataType = String.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Attribute";
            else if (columnIndex == 1) return "Level";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            String[] attributeNames = NaturalAttributeManager.getKeys();
            if (column == 0) return attributeNames[row];
            else if (column == 1) return getLevelString(manager.getAttribute(attributeNames[row]));
            else return "unknown";
        }
  
        public void update() {}
        
        public String getLevelString(int level) {
        	String result = "";
        	
        	if (level < 5) result = "Terrible";
        	else if (level < 20) result = "Very Poor";
        	else if (level < 35) result = "Poor";
        	else if (level < 45) result = "Below Average";
        	else if (level < 55) result = "Average";
        	else if (level < 65) result = "Above Average";
        	else if (level < 80) result = "Good";
        	else if (level < 95) result = "Very Good";
        	else result = "Exceptional";
        	
        	return result;
        }
    }
}
