/**
 * Mars Simulation Project
 * InfirmaryPanel.java
 * @version 2.75 2003-05-07
 * @author Scott Davis
 * Based on SickBayPanel class
 * @see SickBayPanel
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import java.util.Iterator;
import java.util.List;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.*;

/**
 * The InfirmaryPanel class displays information about an infirmary
 * in the user interface.
 */
public class InfirmaryPanel extends FacilityPanel {

    /**
     * This class provides an internal model on the patient list.
     */
    static class PatientTableModel extends DefaultTableModel {
        static String names[] = {"Name", "Status"};

        private List problems;

        /**
         * Construct a model that monitors the specified List of HealthProblems.
         */
        public PatientTableModel(List problems) {
            this.problems = problems;
        }

        /**
         * Table has 2 column, one for name and another for situation.
         */
        public int getColumnCount() {
            return names.length;
        }

        public String getColumnName(int column) {
            return names[column];
        }

        public int getRowCount() {
            if (problems == null) {
                return 0;
            }
            return problems.size();
        }

        public Object getValueAt(int row, int column) {
            HealthProblem rowObject = (HealthProblem)problems.get(row);
            if (column == 0) {
                return rowObject.getSufferer().getName();
            }
            else if (column == 1) {
                return rowObject.getSituation();
            }
            return "Unknown";
        }

        /**
         * As the number of rows is very small, just fire a full table change
         * each time.
         */
        public void update() {
            fireTableDataChanged();
        }
    }

    private final static String PATIENT_LABEL = "Patients Treated : ";
    private final static int MAX_COLUMNS = 60;

    // Data members
    private MedicalCare infirmary;            // The infirmary
    private JLabel patientNumberLabel;        // A label that displays the current population.
    private PatientTableModel patientModel;   // Model display the current patients.

    // Update data cache
    private int currentTreated;               // Size of sick.

    /** 
     * Constructor
     *
     * @param infirmary the infirmary
     * @param desktop the desktop pane
     */
    public InfirmaryPanel(MedicalCare infirmary, MainDesktopPane desktop) {

        // Use FacilityPanel's constructor
        super(desktop);

	    // Initialize data members
        this.infirmary = infirmary;
	    tabName = "Infirmary";
	    currentTreated = infirmary.getPatientNum();

	    // Set up components
	    setLayout(new BorderLayout());

	    // Prepare info pane
	    JPanel infoPane = new JPanel(new BorderLayout(0, 5));
	    add(infoPane, "Center");

	    // Prepare label pane
	    JPanel labelPane = new JPanel(new GridLayout(2, 1));
	    labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	    infoPane.add(labelPane, "North");

	    // Prepare population capacity label
	    JLabel populationCapacityLabel = new JLabel("Capacity: " + infirmary.getSickBedNum(), JLabel.CENTER);
	    labelPane.add(populationCapacityLabel);

	    // Prepare current treated patients
	    patientNumberLabel = new JLabel(PATIENT_LABEL + currentTreated, JLabel.CENTER);
	    labelPane.add(patientNumberLabel);

	    // Prepare patients table, first column name is smaller
        patientModel = new PatientTableModel(infirmary.getProblemsBeingTreated());
	    JTable patientTable = new JTable(patientModel);
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        JScrollPane scrollList = new JScrollPane(patientTable);

	    // Prepare patients panel
	    JPanel patientsPane = new JPanel(new BorderLayout());
	    patientsPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	    patientsPane.add(new JLabel("Patients", JLabel.CENTER), "North");
        patientsPane.add(scrollList, "Center");
        infoPane.add(patientsPane, "Center");

        // Prepare Treatment list
        JPanel treatmentPane = new JPanel();
	    treatmentPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        StringBuffer message = new StringBuffer();
        message.append("Treatments : ");
        int columnCount = 12;
        int rows = 0;
        Iterator treatments = infirmary.getSupportedTreatments().iterator();
        while(treatments.hasNext()) {
            String item = treatments.next().toString();
            columnCount += item.length();
            if (columnCount > MAX_COLUMNS) {
                treatmentPane.add(new JLabel(message.toString(), JLabel.CENTER));
                message = new StringBuffer();
                columnCount =  item.length() + 6;
                message.append("      ");
                rows++;
            }
            message.append(item);
            if (treatments.hasNext()) {
                message.append(", ");
                columnCount += 2;
            }
        }

        treatmentPane.add(new JLabel(message.toString(), JLabel.CENTER));
        rows++;
        treatmentPane.setLayout(new GridLayout(rows, 1));
        infoPane.add(treatmentPane, "South");
    }

    /** Updates the facility panel's information */
    public void updateInfo() {

	    // Update current population
	    int population = infirmary.getPatientNum();
	    if (currentTreated != population) {
            currentTreated = population;
            patientNumberLabel.setText(PATIENT_LABEL + currentTreated);
	    }

	    // Update patients list
        patientModel.update();
    }
}
