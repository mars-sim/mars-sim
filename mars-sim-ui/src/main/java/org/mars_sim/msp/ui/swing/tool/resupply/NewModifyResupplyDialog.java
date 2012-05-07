/**
 * Mars Simulation Project
 * NewModifyResupplyDialog.java
 * @version 3.02 2012-05-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;

/**
 * A dialog for modifying or creating new resupply missions.
 */
public class NewModifyResupplyDialog extends JDialog {

    // Data members
    private Resupply resupply;
    private JComboBox destinationCB;
    private JRadioButton arrivalDateRB;
    private JLabel arrivalDateTitleLabel;
    private JRadioButton timeUntilArrivalRB;
    private JLabel timeUntilArrivalLabel;
    private MartianSolComboBoxModel martianSolCBModel;
    private JLabel solLabel;
    private JComboBox solCB;
    private JLabel monthLabel;
    private JComboBox monthCB;
    private JLabel orbitLabel;
    private JComboBox orbitCB;
    private JTextField solsTF;
    private JLabel solInfoLabel;
    private JTextField immigrantsTF;
    
    /**
     * Constructor for creating new resupply mission.
     * @param owner the JFrame owner of the dialog.
     */
    public NewModifyResupplyDialog(JFrame owner) {
        this(owner, "Create New Resupply Mission", null);
    }
    
    /**
     * Constructor for modifying a resupply mission.
     * @param owner the JFrame owner of the dialog.
     * @param resupply the resupply mission to modify.
     */
    public NewModifyResupplyDialog(JFrame owner, Resupply resupply) {
        this(owner, "Modify Resupply Mission", resupply);
    }
    
    /**
     * Constructor
     * @param owner the JFrame owner of the dialog.
     * @param title the dialog title.
     * @param resupply the resupply mission to modify or null if new resupply.
     */
    private NewModifyResupplyDialog(JFrame owner, String title, Resupply resupply) {
        
        // Use JDialog constructor
        super(owner, title, true);
        
        // Initialize data members.
        this.resupply = resupply;
        
        // Set the layout.
        setLayout(new BorderLayout(0, 0));
        
        // Set the border.
        ((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
        
        // Create the edit pane.
        JPanel editPane = new JPanel(new BorderLayout(0, 0));
        editPane.setBorder(new MarsPanelBorder());
        getContentPane().add(editPane, BorderLayout.CENTER);
        
        // Create top edit pane.
        JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
        editPane.add(topEditPane, BorderLayout.NORTH);
        
        // Create destination pane.
        JPanel destinationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topEditPane.add(destinationPane, BorderLayout.NORTH);
        
        // Create destination title label.
        JLabel destinationTitleLabel = new JLabel("Destination: ");
        destinationPane.add(destinationTitleLabel);
        
        // Create destination combo box.
        Vector<Settlement> settlements = new Vector<Settlement>(
                Simulation.instance().getUnitManager().getSettlements());
        Collections.sort(settlements);
        destinationCB = new JComboBox(settlements);
        if (resupply != null) {
            destinationCB.setSelectedItem(resupply.getSettlement());
        }
        destinationPane.add(destinationCB);
        
        // Create arrival date pane.
        JPanel arrivalDatePane = new JPanel(new GridLayout(2, 1, 10, 10));
        arrivalDatePane.setBorder(new TitledBorder("Arrival Date"));
        topEditPane.add(arrivalDatePane, BorderLayout.CENTER);
        
        // Create data type radio button group.
        ButtonGroup dateTypeRBGroup = new ButtonGroup();
        
        // Create arrival date selection pane.
        JPanel arrivalDateSelectionPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        arrivalDatePane.add(arrivalDateSelectionPane);
        
        // Create arrival date radio button.
        arrivalDateRB = new JRadioButton();
        dateTypeRBGroup.add(arrivalDateRB);
        arrivalDateRB.setSelected(true);
        arrivalDateRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JRadioButton rb = (JRadioButton) evt.getSource();
                setEnableArrivalDatePane(rb.isSelected());
                setEnableTimeUntilArrivalPane(!rb.isSelected());
            }
        });
        arrivalDateSelectionPane.add(arrivalDateRB);
        
        // Create arrival date title label.
        arrivalDateTitleLabel = new JLabel("Arrival Date:");
        arrivalDateSelectionPane.add(arrivalDateTitleLabel);
        
        // Get default resupply Martian time.
        MarsClock resupplyTime = Simulation.instance().getMasterClock().getMarsClock();
        if (resupply != null) {
            resupplyTime = resupply.getArrivalDate();
        }
        
        // Create sol label.
        solLabel = new JLabel("Sol");
        arrivalDateSelectionPane.add(solLabel);
        
        // Create sol combo box.
        martianSolCBModel = new MartianSolComboBoxModel(resupplyTime.getMonth(), resupplyTime.getOrbit());
        solCB = new JComboBox(martianSolCBModel);
        solCB.setSelectedItem(resupplyTime.getSolOfMonth());
        arrivalDateSelectionPane.add(solCB);
        
        // Create month label.
        monthLabel = new JLabel("Month");
        arrivalDateSelectionPane.add(monthLabel);
        
        // Create month combo box.
        monthCB = new JComboBox(MarsClock.getMonthNames());
        monthCB.setSelectedItem(resupplyTime.getMonthName());
        monthCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Update sol combo box values.
                martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1, 
                        Integer.parseInt((String) orbitCB.getSelectedItem()));
            }
        });
        arrivalDateSelectionPane.add(monthCB);
        
        // Create orbit label.
        orbitLabel = new JLabel("Orbit");
        arrivalDateSelectionPane.add(orbitLabel);
        
        // Create orbit combo box.
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumIntegerDigits(2);
        String[] orbitValues = new String[20];
        int startOrbit = resupplyTime.getOrbit();
        for (int x = 0; x < 20; x++) {
            orbitValues[x] = formatter.format(startOrbit + x);
        }
        orbitCB = new JComboBox(orbitValues);
        orbitCB.setSelectedItem(formatter.format(startOrbit));
        orbitCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Update sol combo box values.
                martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1, 
                        Integer.parseInt((String) orbitCB.getSelectedItem()));
            }
        });
        arrivalDateSelectionPane.add(orbitCB);
        
        // Create time until arrival pane.
        JPanel timeUntilArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        arrivalDatePane.add(timeUntilArrivalPane);
        
        // Create time until arrival radio button.
        timeUntilArrivalRB = new JRadioButton();
        dateTypeRBGroup.add(timeUntilArrivalRB);
        timeUntilArrivalRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JRadioButton rb = (JRadioButton) evt.getSource();
                setEnableTimeUntilArrivalPane(rb.isSelected());
                setEnableArrivalDatePane(!rb.isSelected());
            }
        });
        timeUntilArrivalPane.add(timeUntilArrivalRB);
        
        // create time until arrival label.
        timeUntilArrivalLabel = new JLabel("Sols Until Arrival:");
        timeUntilArrivalLabel.setEnabled(false);
        timeUntilArrivalPane.add(timeUntilArrivalLabel);
        
        // Create sols text field.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        int solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, currentTime) / 1000D));
        solsTF = new JTextField(6);
        solsTF.setText(Integer.toString(solsDiff));
        solsTF.setHorizontalAlignment(JTextField.RIGHT);
        solsTF.setEnabled(false);
        timeUntilArrivalPane.add(solsTF);
        
        // Create sol information label.
        solInfoLabel = new JLabel("(668 Sols = 1 Martian Orbit)");
        solInfoLabel.setEnabled(false);
        timeUntilArrivalPane.add(solInfoLabel);
        
        // Create immigrants panel.
        JPanel immigrantsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topEditPane.add(immigrantsPane, BorderLayout.SOUTH);
        
        // Create immigrants label.
        JLabel immigrantsLabel = new JLabel("Number of Immigrants: ");
        immigrantsPane.add(immigrantsLabel);
        
        // Create immigrants text field.
        int immigrantsNum = 0;
        if (resupply != null) {
            immigrantsNum = resupply.getNewImmigrantNum();
        }
        immigrantsTF = new JTextField(6);
        immigrantsTF.setText(Integer.toString(immigrantsNum));
        immigrantsTF.setHorizontalAlignment(JTextField.RIGHT);
        immigrantsPane.add(immigrantsTF);
        
        // Create bottom edit pane.
        JPanel bottomEditPane = new JPanel(new BorderLayout(0, 0));
        bottomEditPane.setBorder(new TitledBorder("Supplies"));
        editPane.add(bottomEditPane, BorderLayout.CENTER);
        
        // Create supply table.
        SupplyTableModel supplyTableModel = new SupplyTableModel(resupply);
        JTable supplyTable = new JTable(supplyTableModel);
        supplyTable.getColumnModel().getColumn(2).setCellRenderer(new NumberCellRenderer(0));
        supplyTable.getColumnModel().getColumn(0).setMaxWidth(100);
        supplyTable.getColumnModel().getColumn(1).setMaxWidth(200);
        supplyTable.getColumnModel().getColumn(2).setMaxWidth(150);
        
        // Create supply scroll pane.
        JScrollPane supplyScrollPane = new JScrollPane(supplyTable);
        supplyScrollPane.setPreferredSize(new Dimension(450, 200));
        bottomEditPane.add(supplyScrollPane, BorderLayout.CENTER);
        
        // Create supply button pane.
        JPanel supplyButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomEditPane.add(supplyButtonPane, BorderLayout.SOUTH);
        
        // Create add supply button.
        JButton addSupplyButton = new JButton("Add");
        supplyButtonPane.add(addSupplyButton);
        
        // Create remove supply button.
        JButton removeSupplyButton = new JButton("Remove");
        removeSupplyButton.setEnabled(false);
        supplyButtonPane.add(removeSupplyButton);
        
        // Create the button pane.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        
        if (resupply != null) {
            // Create modify button.
            JButton modifyButton = new JButton("Modify");
            modifyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // Modify resupply mission and close dialog.
                    modifyResupplyMission();
                    dispose();
                }
            });
            buttonPane.add(modifyButton);
        } else {
            // Create create button.
            JButton createButton = new JButton("Create");
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    // Create new resupply mission and close dialog.
                    createResupplyMission();
                    dispose();
                }
            });
            buttonPane.add(createButton);
        }
            
        // Create cancel button.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Close dialog.
                dispose();
            }
            
        });
        buttonPane.add(cancelButton);
        
        // Finish and display dialog.
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }
    
    /**
     * Set the components of the arrival date pane to be enabled or disabled.
     * @param enable true if enable components, false if disable components.
     */
    private void setEnableArrivalDatePane(boolean enable) {
        arrivalDateTitleLabel.setEnabled(enable);
        solLabel.setEnabled(enable);
        solCB.setEnabled(enable);
        monthLabel.setEnabled(enable);
        monthCB.setEnabled(enable);
        orbitLabel.setEnabled(enable);
        orbitCB.setEnabled(enable);
    }
    
    /**
     * Set the components of the time until arrival pane to be enabled or disabled.
     * @param enable true if enable components, false if disable components.
     */
    private void setEnableTimeUntilArrivalPane(boolean enable) {
        timeUntilArrivalLabel.setEnabled(enable);
        solsTF.setEnabled(enable);
        solInfoLabel.setEnabled(enable);
    }
    
    /**
     * Modify the resupply mission.
     */
    private void modifyResupplyMission() {
        // TODO
    }
    
    /**
     * Create the new resupply mission.
     */
    private void createResupplyMission() {
        // TODO
    }
    
    /**
     * Inner class for the Sol combo box model.
     */
    private class MartianSolComboBoxModel extends DefaultComboBoxModel {
        
        // Data members
        private int maxSolNum;
        
        /**
         * Constructor
         * @param month the Martian month number.
         * @param orbit the Martian orbit number.
         */
        private MartianSolComboBoxModel(int month, int orbit) {
            maxSolNum = MarsClock.getSolsInMonth(month, orbit);
            
            for (int x = 1; x <= maxSolNum; x++) {
                addElement(x);
            }
        }
        
        /**
         * Update the items based on the number of sols in the month.
         * @param month the Martian month number.
         * @param orbit the Martian orbit number.
         */
        private void updateSolNumber(int month, int orbit) {
            int newMaxSolNum = MarsClock.getSolsInMonth(month, orbit);
            if (newMaxSolNum != maxSolNum) {
                int oldSelectedSol = (Integer) getSelectedItem();
                
                if (newMaxSolNum < maxSolNum) {
                    removeElementAt(maxSolNum - 1);
                    if (oldSelectedSol == maxSolNum) {
                        setSelectedItem(newMaxSolNum);
                    }
                }
                else {
                    addElement(newMaxSolNum);
                }
            }
        }
    }
}