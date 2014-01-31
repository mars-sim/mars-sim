/**
 * Mars Simulation Project
 * HealthTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.Medication;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The HealthTabPanel is a tab panel for a person's health.
 */
public class HealthTabPanel extends TabPanel {
    
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private DecimalFormat formatter = new DecimalFormat("0.0");
    private JLabel fatigueLabel;
    private JLabel hungerLabel;
    private JLabel stressLabel;
    private JLabel performanceLabel;
    private MedicationTableModel medicationTableModel;
    private HealthProblemTableModel healthProblemTableModel;
    
    // Data cache
    private double fatigueCache;
    private double hungerCache;
    private double stressCache;
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
        JPanel conditionPanel = new JPanel(new GridLayout(4, 2, 0, 0));
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

		// Prepare streses name label
		JLabel stressNameLabel = new JLabel("Stress", JLabel.LEFT);
		conditionPanel.add(stressNameLabel);
		
		// Prepare stress label
		stressCache = condition.getStress();
		stressLabel = new JLabel(formatter.format(stressCache) + " %", JLabel.RIGHT);
		conditionPanel.add(stressLabel);

        // Prepare performance rating label
        JLabel performanceNameLabel = new JLabel("Performance", JLabel.LEFT);
        conditionPanel.add(performanceNameLabel);

        // Performance rating label
        performanceCache = person.getPerformanceRating() * 100D;
        performanceLabel = new JLabel(formatter.format(performanceCache) + " %", JLabel.RIGHT);
        conditionPanel.add(performanceLabel);
        
        // Prepare table panel.
        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        centerContentPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Prepare medication panel.
        JPanel medicationPanel = new JPanel(new BorderLayout());
        medicationPanel.setBorder(new MarsPanelBorder());
        tablePanel.add(medicationPanel);
        
        // Prepare medication label.
        JLabel medicationLabel = new JLabel("Medication", JLabel.CENTER);
        medicationPanel.add(medicationLabel, BorderLayout.NORTH);
        
        // Prepare medication scroll panel
        JScrollPane medicationScrollPanel = new JScrollPane();
        medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);
        
        // Prepare medication table model.
        medicationTableModel = new MedicationTableModel(person);
        
        // Prepare medication table.
        JTable medicationTable = new JTable(medicationTableModel);
        medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
        medicationTable.setCellSelectionEnabled(false);
        medicationScrollPanel.setViewportView(medicationTable);
        
        // Prepare health problem panel
        JPanel healthProblemPanel = new JPanel(new BorderLayout());
        healthProblemPanel.setBorder(new MarsPanelBorder());
        tablePanel.add(healthProblemPanel);
        
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
        healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 50));
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
        
        // Update stress if necessary.
        if (stressCache != condition.getStress()) {
        	stressCache = condition.getStress();
        	stressLabel.setText(formatter.format(stressCache) + " %");
        }
        
        // Update performance cache if necessary.
        if (performanceCache != (person.getPerformanceRating() * 100D)) {
            performanceCache = person.getPerformanceRating() * 100D;
            performanceLabel.setText(formatter.format(performanceCache) + "%");
        }
        
        // Update medication table model.
        medicationTableModel.update();
        
        // Update health problem table model.
        healthProblemTableModel.update();
    }
    
    /** 
     * Internal class used as model for the health problem table.
     */
    private static class HealthProblemTableModel extends AbstractTableModel {
        
    	/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
        private Collection<?> problemsCache;
        
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
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
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
                Iterator<?> i = problemsCache.iterator();
                int count = 0;
                while (i.hasNext()) {
                    HealthProblem prob = (HealthProblem) i.next();
                    if (count == row) problem = prob;
                    count++;
                }
            }
            
            if (problem != null) {
                if (column == 0) return problem.getIllness().getName();
                else if (column == 1) {
                	String conditionStr = problem.getStateString();
                	if (!condition.isDead()) conditionStr += " (" + problem.getHealthRating() + "%)";
                	return conditionStr;
                }
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
    
    /** 
     * Internal class used as model for the medication table.
     */
    private static class MedicationTableModel extends AbstractTableModel {
        
    	/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
        private List<Medication> medicationCache;
        
        private MedicationTableModel(Person person) {
            condition = person.getPhysicalCondition();
            medicationCache = condition.getMedicationList();
        }
        
        public int getRowCount() {
            return medicationCache.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            else if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Medication";
            else if (columnIndex == 1) return "Duration (millisols)";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            Object result = "unknown";
            if (row < getRowCount()) {
                if (column == 0) result = medicationCache.get(row).getName(); 
                else if (column == 1) result = medicationCache.get(row).getDuration();
            }
            return result;
        }
  
        public void update() {
            
            // Make sure medication cache is current.
            if (!medicationCache.equals(condition.getMedicationList()))
                medicationCache = condition.getMedicationList();
                
            fireTableDataChanged();
        }
    }
}