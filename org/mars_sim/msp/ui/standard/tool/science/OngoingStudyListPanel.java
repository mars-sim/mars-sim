/**
 * Mars Simulation Project
 * OngoingStudyListPanel.java
 * @version 2.87 2009-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.science.ScientificStudy;
import org.mars_sim.msp.simulation.science.ScientificStudyManager;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * A panel showing a selectable list of ongoing scientific studies.
 */
public class OngoingStudyListPanel extends JPanel {

    // Data members
    private ScienceWindow scienceWindow;
    private StudyTableModel studyTableModel;
    
    /**
     * Constructor
     * @param scienceWindow the science window.
     */
    OngoingStudyListPanel(ScienceWindow scienceWindow) {
        // Use JPanel constructor.
        super();
        
        setLayout(new BorderLayout());
        
        // Create title label.
        JLabel titleLabel = new JLabel("Ongoing Scientific Studies", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Create list scroll pane.
        JScrollPane listScrollPane = new JScrollPane();
        listScrollPane.setBorder(new MarsPanelBorder());
        listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(listScrollPane, BorderLayout.CENTER);
        
        // Create study table model.
        studyTableModel = new StudyTableModel();
        
        // Create study table.
        JTable studyTable = new JTable(studyTableModel);
        studyTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        studyTable.setCellSelectionEnabled(false);
        studyTable.setRowSelectionAllowed(true);
        studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScrollPane.setViewportView(studyTable);
    }
    
    /**
     * Updates the panel.
     */
    void update() {
        studyTableModel.update();
    }
    
    /**
     * The study table model inner class.
     */
    private class StudyTableModel extends AbstractTableModel {

        // Data members
        private List<ScientificStudy> studies;
        
        /**
         * Constructor
         */
        private StudyTableModel() {
            ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
            studies = manager.getOngoingStudies();
        }
        
        /**
         * Returns the number of columns in the model. A JTable uses this method to determine 
         * how many columns it should create and display by default. 
         * @return the number of columns in the model
         */
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public String getColumnName(int column) {
            String result = "";
            if (column == 0) result = "Study";
            else if (column == 1) result = "Phase";
            return result;
        }

        /**
         * Returns the number of rows in the model. A JTable uses this method to determine 
         * how many rows it should display. This method should be quick, as it is called 
         * frequently during rendering. 
         * @return the number of rows in the model
         */
        public int getRowCount() {
            return studies.size();
        }

        /**
         * Returns the value for the cell at columnIndex and rowIndex.
         * @param rowIndex the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            String result = "";
            if ((rowIndex >= 0) && (rowIndex < studies.size())) {
                ScientificStudy study = studies.get(rowIndex);
                if (columnIndex == 0) result = study.toString();
                else if (columnIndex == 1) result = study.getPhase();
            }
            return result;
        }
        
        /**
         * Updates the table model.
         */
        private void update() {
            ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
            List<ScientificStudy> newStudies = manager.getOngoingStudies();
            if (!studies.equals(newStudies)) {
                studies = newStudies;
                fireTableDataChanged();
            }
        }
    }
}