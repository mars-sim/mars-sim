/**
 * Mars Simulation Project
 * HealthTabPanel.java
 * @version 2.75 2003-06-18
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;


/** 
 * The HealthTabPanel is a tab panel for a person's health.
 */
public class HealthTabPanel extends TabPanel {
    
    private DecimalFormat formatter = new DecimalFormat("0.0");
    private JLabel fatigueLabel;
    private JLabel hungerLabel;
    private JLabel performanceLabel;
    private HealthProblemTableModel healthProblemTableModel;
    
    // Data cache
    
    
    /**
     * Constructor
     *
     * @param proxy the UI proxy for the unit.
     * @param desktop the main desktop.
     */
    public HealthTabPanel(UnitUIProxy proxy, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Health", null, "Health", proxy, desktop);
        
        Person person = (Person) proxy.getUnit();
        PhysicalCondition condition = person.getPhysicalCondition();
        
        // Create health label panel.
        JPanel healthLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(healthLabelPanel);
        
        // Prepare health label
        JLabel healthLabel = new JLabel("Health", JLabel.CENTER);
        healthLabelPanel.add(healthLabel);
        
        // Prepare condition panel
        JPanel conditionPanel = new JPanel(new GridLayout(3, 2, 0, 0));
        conditionPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(conditionPanel, BorderLayout.NORTH);

        // Prepare fatigue name label
        JLabel fatigueNameLabel = new JLabel("Fatigue", JLabel.LEFT);
        conditionPanel.add(fatigueNameLabel);

        // Prepare fatigue label
        fatigueLabel = new JLabel(formatter.format(condition.getFatigue()) + 
            " millisols", JLabel.RIGHT);
        conditionPanel.add(fatigueLabel);

        // Prepare hunger name label
        JLabel hungerNameLabel = new JLabel("Hunger", JLabel.LEFT);
        conditionPanel.add(hungerNameLabel);

        // Prepare hunger label
        hungerLabel = new JLabel(formatter.format(condition.getHunger()) + 
            " millisols", JLabel.RIGHT);
        conditionPanel.add(hungerLabel);

        // Prepare performance rating label
        JLabel performanceNameLabel = new JLabel("Performance", JLabel.LEFT);
        conditionPanel.add(performanceNameLabel);

        // Performance rating label
        performanceLabel = new JLabel(formatter.format(person.getPerformanceRating() * 100D) + 
            " %", JLabel.RIGHT);
        conditionPanel.add(performanceLabel);
        
        // Prepare health problem panel
        JPanel healthProblemPanel = new JPanel(new BorderLayout());
        healthProblemPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(healthProblemPanel, BorderLayout.CENTER);
        
        // Prepare health problem label
        JLabel healthProblemLabel = new JLabel("Health Problems", JLabel.CENTER);
        healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);
        
        // Prepare health problem scroll panel
        JScrollPane healthProblemScrollPanel = new JScrollPane();
        healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);
        
        // Prepare health problem table model
        healthProblemTableModel = new HealthProblemTableModel(proxy);
        
        // Create health problem table
        JTable healthProblemTable = new JTable(healthProblemTableModel);
        healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
        healthProblemTable.setCellSelectionEnabled(false);
        healthProblemScrollPanel.setViewportView(healthProblemTable);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        healthProblemTableModel.update();
    }
    
    /** 
     * Internal class used as model for the skill table.
     */
    private class HealthProblemTableModel extends AbstractTableModel {
        
        PhysicalCondition condition;
        java.util.Collection problemsCache;
        
        private HealthProblemTableModel(UnitUIProxy proxy) {
            Person person = (Person) proxy.getUnit();
            condition = person.getPhysicalCondition();
            problemsCache = condition.getProblems();
        }
        
        public int getRowCount() {
            return problemsCache.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            if (columnIndex == 1) dataType = String.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Problem";
            else if (columnIndex == 1) return "Condition";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            HealthProblem problem = null;
            if (row < problemsCache.size()) {
                Iterator i = problemsCache.iterator();
                int count = 0;
                while (i.hasNext()) {
                    HealthProblem prob = (HealthProblem) i.next();
                    if (count == row) problem = prob;
                    count++;
                }
            }
            
            if (problem != null) {
                if (column == 0) return problem.getIllness().getName();
                else if (column == 1) return problem.getStateString();
                else return "unknown";
            }
            else return "unknown";
        }
  
        public void update() {
            
            // Make sure problems cache is current.
            if (!problemsCache.equals(condition.getProblems()))
                problemsCache = condition.getProblems();
                
            fireTableDataChanged();
        }
    }
}
