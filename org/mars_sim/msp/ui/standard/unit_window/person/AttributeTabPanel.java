/**
 * Mars Simulation Project
 * AttributeTabPanel.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

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
        attributeTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        attributeTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        attributeTable.setCellSelectionEnabled(false);
        attributeTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
        attributeScrollPanel.setViewportView(attributeTable);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {}
    
    /** 
     * Internal class used as model for the attribute table.
     */
    private class AttributeTableModel extends AbstractTableModel {
        
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
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            if (columnIndex == 1) dataType = Integer.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Attribute";
            else if (columnIndex == 1) return "Level";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            String[] attributeNames = manager.getKeys();
            if (column == 0) return attributeNames[row];
            else if (column == 1) return new Integer(manager.getAttribute(attributeNames[row]));
            else return "unknown";
        }
  
        public void update() {}
    }
}
