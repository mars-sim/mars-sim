/**
 * Mars Simulation Project
 * HealthTabPanel.java
 * @version 2.75 2003-07-16
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
    private double fatigueCache;
    private double hungerCache;
    private double performanceCache;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public HealthTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Health", null, "Health", unit, desktop);
        
        Person person = (Person) unit;
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
        fatigueCache = condition.getFatigue();
        fatigueLabel = new JLabel(formatter.format(fatigueCache) + " millisols", JLabel.RIGHT);
        conditionPanel.add(fatigueLabel);

        // Prepare hunger name label
        JLabel hungerNameLabel = new JLabel("Hunger", JLabel.LEFT);
        conditionPanel.add(hungerNameLabel);

        // Prepare hunger label
        hungerCache = condition.getHunger();
        hungerLabel = new JLabel(formatter.format(hungerCache) + " millisols", JLabel.RIGHT);
        conditionPanel.add(hungerLabel);

        // Prepare performance rating label
        JLabel performanceNameLabel = new JLabel("Performance", JLabel.LEFT);
        conditionPanel.add(performanceNameLabel);

        // Performance rating label
        performanceCache = person.getPerformanceRating() * 100D;
        performanceLabel = new JLabel(formatter.format(performanceCache) + " %", JLabel.RIGHT);
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
        healthProblemTableModel = new HealthProblemTableModel(person);
        
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
        
        Person person = (Person) unit;
        PhysicalCondition condition = person.getPhysicalCondition();
        
        // Update fatigue if necessary.
        if (fatigueCache != condition.getFatigue()) {
            fatigueCache = condition.getFatigue();
            fatigueLabel.setText(formatter.format(fatigueCache) + " millisols");
        }
        
        // Update hunger if necessary.
        if (hungerCache != condition.getHunger()) {
            hungerCache = condition.getHunger();
            hungerLabel.setText(formatter.format(hungerCache) + " millisols");
        }
        
        // Update performance cache if necessary.
        if (performanceCache != (person.getPerformanceRating() * 100D)) {
            performanceCache = person.getPerformanceRating() * 100D;
            performanceLabel.setText(formatter.format(hungerCache) + "%");
        }
        
        // Update health problem table model.
        healthProblemTableModel.update();
    }
    
    /** 
     * Internal class used as model for the skill table.
     */
    private class HealthProblemTableModel extends AbstractTableModel {
        
        PhysicalCondition condition;
        java.util.Collection problemsCache;
        
        private HealthProblemTableModel(Person person) {
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
