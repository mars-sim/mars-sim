/**
 * Mars Simulation Project
 * SkillTabPanel.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The SkillTabPanel is a tab panel for the skills of a person.
 */
public class SkillTabPanel extends TabPanel {
    
    private SkillTableModel skillTableModel;
    
    /**
     * Constructor
     *
     * @param person the person.
     * @param desktop the main desktop.
     */
    public SkillTabPanel(Person person, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Skills", null, "Skills", person, desktop);
        
        // Create skill label panel.
        JPanel skillLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(skillLabelPanel);
        
        // Create skill label
        JLabel skillLabel = new JLabel("Skills", JLabel.CENTER);
        skillLabelPanel.add(skillLabel);
        
        // Create skill scroll panel
        JScrollPane skillScrollPanel = new JScrollPane();
        skillScrollPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(skillScrollPanel);
        
        // Create skill table model
        skillTableModel = new SkillTableModel(person);
            
        // Create skill table
        JTable skillTable = new JTable(skillTableModel);
        skillTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
        skillTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        skillTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        skillTable.setCellSelectionEnabled(false);
        skillTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
        skillScrollPanel.setViewportView(skillTable);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        skillTableModel.update();
    }
    
    /** 
     * Internal class used as model for the skill table.
     */
    private class SkillTableModel extends AbstractTableModel {
        
        SkillManager manager;
        Map<String, Integer> skills;
        List<String> skillNames;
        
        private SkillTableModel(Person person) {
            manager = person.getMind().getSkillManager();
            
            String[] keys = manager.getKeys();
            skills = new HashMap<String, Integer>();
            skillNames = new ArrayList<String>();
            for (int x=0; x < keys.length; x++) {
                int level = manager.getSkillLevel(keys[x]);
                if (level > 0) {
                    skillNames.add(keys[x]);
                    skills.put(keys[x], level);
                }
            }
        }
        
        public int getRowCount() {
            return skillNames.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            if (columnIndex == 1) dataType = Integer.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Skill";
            else if (columnIndex == 1) return "Level";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            if (column == 0) return skillNames.get(row);
            else if (column == 1) return skills.get(skillNames.get(row));
            else return "unknown";
        }
  
        public void update() {
            String[] keys = manager.getKeys();
            List<String> newSkillNames = new ArrayList<String>();
            Map<String, Integer> newSkills = new HashMap<String, Integer>();
            for (int x=0; x < keys.length; x++) {
                int level = manager.getSkillLevel(keys[x]);
                if (level > 0) {
                    newSkillNames.add(keys[x]);
                    newSkills.put(keys[x], level);
                }
            }
                
            if (!skills.equals(newSkills)) {
                skillNames = newSkillNames;
                skills = newSkills;
                fireTableDataChanged();
            }
        }
    }
}