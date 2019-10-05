/**
 * Mars Simulation Project
 * LeadResearcherPanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

/**
 * A wizard panel to select a lead researcher the science mission.
 */
public class LeadResearcherPanel
extends WizardPanel {

	// The wizard panel name.
    private final static String NAME = "Lead Researcher";
    
    // Data members.
    private ResearcherTableModel researcherTableModel;
    private JTable researcherTable;
    private JLabel errorMessageLabel;
    
//	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
    /**
     * Constructor
     * @param wizard the create mission wizard.
     */
    LeadResearcherPanel(CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select lead researcher label.
        JLabel selectResearcherLabel = new JLabel("Select a lead researcher.", JLabel.CENTER);
        selectResearcherLabel.setFont(selectResearcherLabel.getFont().deriveFont(Font.BOLD));
        selectResearcherLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(selectResearcherLabel);
        
        // Create the researcher panel.
        JPanel researcherPane = new JPanel(new BorderLayout(0, 0));
        researcherPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        researcherPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(researcherPane);
        
        // Create scroll panel for researcher list.
        JScrollPane researcherScrollPane = new JScrollPane();
        researcherPane.add(researcherScrollPane, BorderLayout.CENTER);
        
        // Create the researcher table model.
        researcherTableModel = new ResearcherTableModel();
        
        // Create the researcher table.
        researcherTable = new JTable(researcherTableModel);
		TableStyle.setTableStyle(researcherTable);
		researcherTable.setAutoCreateRowSorter(true);  
        researcherTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(researcherTableModel));
        researcherTable.setRowSelectionAllowed(true);
        researcherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        researcherTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        int index = researcherTable.getSelectedRow();
                        if (index > -1) {
                            if (researcherTableModel.isFailureRow(index)) {
                                errorMessageLabel.setText("researcher cannot lead the mission (see red cells).");
                                getWizard().setButtons(false);
                            }
                            else {
                                errorMessageLabel.setText(" ");
                                getWizard().setButtons(true);
                            }
                        }
                    }
                }
            });
        researcherTable.setPreferredScrollableViewportSize(researcherTable.getPreferredSize());
        researcherScrollPane.setViewportView(researcherTable);
        
        // Create the error message label.
        errorMessageLabel = new JLabel(" ", JLabel.CENTER);
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMessageLabel);
        
        // Add a vertical glue.
        add(Box.createVerticalGlue());
    }
    
    @Override
    void clearInfo() {
        researcherTable.clearSelection();
        errorMessageLabel.setText(" ");
    }

    @Override
    boolean commitChanges() {
        int selectedIndex = researcherTable.getSelectedRow();
        Person selectedResearcher = (Person) researcherTableModel.getUnit(selectedIndex);
        getWizard().getMissionData().setLeadResearcher(selectedResearcher);
        getWizard().getMissionData().setStartingSettlement(selectedResearcher.getAssociatedSettlement());
        return true;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        researcherTableModel.updateTable();
        researcherTable.setPreferredScrollableViewportSize(researcherTable.getPreferredSize());
    }
    
    /**
     * Table model for scientific researchers.
     */
    private class ResearcherTableModel
    extends UnitTableModel {
        
        /** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
         * Constructor.
         */
        private ResearcherTableModel() {
            // Use UnitTableModel constructor.
            super();
            
            // Add table columns.
            columns.add("Name");
            columns.add("Study Research Field");
            columns.add("Current Mission");
            columns.add("Performance");
            columns.add("Health");
        }
        
        /**
         * Returns the value for the cell at columnIndex and rowIndex.
         * @param row the row whose value is to be queried.
         * @param column the column whose value is to be queried.
         * @return the value Object at the specified cell.
         */
        public Object getValueAt(int row, int column) {
            Object result = null;
            
            if ((row >= 0) && (row < units.size())) {
                Person person = (Person) getUnit(row);
                
                try {
                    if (column == 0) {
                        result = person.getName();
                    }
                    else if (column == 1) {
                        result = getResearchScience(person).getName();
                    }
                    else if (column == 2) {
                        Mission mission = person.getMind().getMission();
                        if (mission != null) result = mission.getName();
                        else result = "none";
                    }
                    else if (column == 3) {
                        result = (int) (person.getPerformanceRating() * 100D) + "%";
                    }
                    else if (column == 4) {
                        result = person.getPhysicalCondition().getHealthSituation();
                    }
                }
                catch (Exception e) {}
            }
            
            return result;
        }
        
        /**
         * Gets the field of science a person is researching for a scientific study.
         * @param person the person.
         * @return field of science or null if none.
         */
        private ScienceType getResearchScience(Person person) {
        	ScienceType result = null;
            
            ScientificStudy study = getWizard().getMissionData().getStudy();
            if (study.getPrimaryResearcher().equals(person)) {
                result = study.getScience();
            }
            else if (study.getCollaborativeResearchers().keySet().contains(person.getIdentifier())) {
                result = study.getCollaborativeResearchers().get(person.getIdentifier());
            }
            
            return result;
        }
        
        /**
         * Updates the table data.
         */
        void updateTable() {
            units.clear();
            ScientificStudy study = getWizard().getMissionData().getStudy();
            units.add(study.getPrimaryResearcher());
            
            Iterator<Integer> i = study.getCollaborativeResearchers().keySet().iterator();
            while (i.hasNext()) units.add(unitManager.getPersonByID(i.next()));
            
            fireTableDataChanged();
        }
        
        /**
         * Checks if a table cell is a failure cell.
         * @param row the table row.
         * @param column the table column.
         * @return true if cell is a failure cell.
         */
        boolean isFailureCell(int row, int column) {
            boolean result = false;
            
            if (row < units.size()) {
                Person person = (Person) getUnit(row);
                
                if (column == 1) {
                    ScienceType researchScience = getResearchScience(person);
                    ScienceType studyScience = getWizard().getMissionData().getStudy().getScience();
                    if (!studyScience.equals(researchScience)) result = true;
                }
                if (column == 2) {
                    if (person.getMind().getMission() != null) result = true;
                }
            }
            
            return result;
        }

//        /*
//         * Adds researchers to the table.
//         * @param people the collection of researchers to add.
//         *
//        void addResearchers(Collection<Person> researchers) {
//            Iterator<Person> i = researchers.iterator();
//            while (i.hasNext()) {
//                Person researcher = i.next();
//                if (!units.contains(researcher)) units.add(researcher);
//            }
//            units = CollectionUtils.sortByName(units);
//            fireTableDataChanged();
//        }
//        */
//        /*
//         * Removes researchers from the table.
//         * @param researchers the collection of researchers to remove.
//         *
//        void removeResearchers(Collection<Person> researchers) {
//            Iterator<Person> i = researchers.iterator();
//            while (i.hasNext()) {
//                Person researcher = i.next();
//                if (units.contains(researcher)) units.remove(researcher);
//            }
//            fireTableDataChanged();
//        }
    }
}