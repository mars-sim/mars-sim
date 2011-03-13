/**
 * Mars Simulation Project
 * TempSimulationConfigEditor.java
 * @version 3.00 2011-03-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;

/**
 * A temporary simulation configuration editor dialog.
 * Will be replaced by SimulationConfigEditor later when it is finished.
 */
public class TempSimulationConfigEditor extends JDialog {

    // Data members.
    private SimulationConfig config;
    private SettlementTableModel settlementTableModel;
    private JTable settlementTable;
    
    /**
     * Constructor
     * @param owner the owner window.
     * @param config the simulation configuration.
     */
    public TempSimulationConfigEditor(Window owner, SimulationConfig config) {
        // Use JDialog constructor.
        super(owner, "Simulation Configuration Editor", ModalityType.APPLICATION_MODAL);
        
        // Initialize data members.
        this.config = config;
        
        // Sets the dialog content panel.
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPanel);
        
        // Create the title label.
        JLabel titleLabel = new JLabel("Choose settlements for the simulation:", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Create settlement scroll panel.
        JScrollPane settlementScrollPane = new JScrollPane();
        settlementScrollPane.setPreferredSize(new Dimension(500, 200));
        add(settlementScrollPane, BorderLayout.CENTER);
        
        // Create settlement table.
        settlementTableModel = new SettlementTableModel();
        settlementTable = new JTable(settlementTableModel);
        settlementTable.setRowSelectionAllowed(true);
        settlementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        settlementTable.getColumnModel().getColumn(0).setPreferredWidth(125);
        settlementTable.getColumnModel().getColumn(1).setPreferredWidth(205);
        settlementTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        settlementTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        settlementScrollPane.setViewportView(settlementTable);
        
        // Create combo box for editing template column in settlement table.
        TableColumn templateColumn = settlementTable.getColumnModel().getColumn(1);
        JComboBox templateCB = new JComboBox();
        SettlementConfig settlementConfig = config.getSettlementConfiguration();
        Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
        while (i.hasNext()) {
            templateCB.addItem(i.next().getTemplateName());
        }
        templateColumn.setCellEditor(new DefaultCellEditor(templateCB));
        
        // Create configuration button outer panel.
        JPanel configurationButtonOuterPanel = new JPanel(new BorderLayout(0, 0));
        add(configurationButtonOuterPanel, BorderLayout.EAST);
        
        // Create configuration button inner panel.
        JPanel configurationButtonInnerPanel = new JPanel(new GridLayout(3, 1));
        configurationButtonOuterPanel.add(configurationButtonInnerPanel, BorderLayout.NORTH);
        
        // Create add settlement button.
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addNewSettlement();
            }
        });
        configurationButtonInnerPanel.add(addButton);
        
        // Create remove settlement button.
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedSettlements();
            } 
        });
        configurationButtonInnerPanel.add(removeButton);
        
        // Create default button.
        JButton defaultButton = new JButton("Default");
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDefaultSettlements();
            }
        });
        configurationButtonInnerPanel.add(defaultButton);
        
        // Create the bottom button panel.
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(bottomButtonPanel, BorderLayout.SOUTH);
        
        // Create the create button.
        JButton createButton = new JButton("Create New Simulation");
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setConfiguration();
                closeWindow();
            }
        });
        bottomButtonPanel.add(createButton);
        
        pack();
        
        // Set the location of the dialog at the center of the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }
    
    /**
     * Adds a new settlement  with default values.
     */
    private void addNewSettlement() {
        // TODO
    }
    
    /**
     * Removes the settlements selected on the table.
     */
    private void removeSelectedSettlements() {
        // TODO
    }
    
    /**
     * Sets the default settlements from the loaded configuration.
     */
    private void setDefaultSettlements() {
        // TODO
    }
    
    /**
     * Set the simulation configuration based on dialog choices.
     */
    private void setConfiguration() {
        // TODO
    }
    
    /**
     * Close and dispose dialog window.
     */
    private void closeWindow() {
        setVisible(false);
        dispose();
    }
    
    private class SettlementInfo {
        String name;
        String template;
        String latitude;
        String longitude;
    }
    
    private class SettlementTableModel extends AbstractTableModel {
        
        private String[] columns;
        private List<SettlementInfo> settlements;
        
        private SettlementTableModel() {
            super();
            
            // Add table columns.
            columns = new String[] { "Name", "Template", "Latitude", "Longitude" };
            
            // Load default settlements.
            settlements = new ArrayList<SettlementInfo>();
            loadDefaultSettlements();
        }
        
        private void loadDefaultSettlements() {
            SettlementConfig settlementConfig = config.getSettlementConfiguration();
            settlements.clear();
            for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
                SettlementInfo info = new SettlementInfo();
                info.name = settlementConfig.getInitialSettlementName(x);
                info.template = settlementConfig.getInitialSettlementTemplate(x);
                info.latitude = settlementConfig.getInitialSettlementLatitude(x);
                info.longitude = settlementConfig.getInitialSettlementLongitude(x);
                settlements.add(info);
            }
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return settlements.size();
        }
        
        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if ((columnIndex > -1) && (columnIndex < columns.length)) {
                return columns[columnIndex];
            }
            else {
                return "invalid column index";
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            Object result = "unknown";
            
            if ((row > -1) && (row < getRowCount())) {
                SettlementInfo info = settlements.get(row);
                if ((column > -1) && (column < getColumnCount())) {
                    switch (column) {
                        case 0: 
                            result = info.name;
                            break;
                        case 1:
                            result = info.template;
                            break;
                        case 2:
                            result = info.latitude;
                            break;
                        case 3:
                            result = info.longitude;
                    }
                }
                else {
                    result = "invalid column index";
                }
            }
            else {
                result = "invalid row index";
            }
            
            return result;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if ((rowIndex > -1) && (rowIndex < getRowCount())) {
                SettlementInfo info = settlements.get(rowIndex);
                if ((columnIndex > -1) && (columnIndex < getColumnCount())) {
                    switch (columnIndex) {
                        case 0: 
                            info.name = (String) aValue;
                            break;
                        case 1:
                            info.template = (String) aValue;
                            break;
                        case 2:
                            info.latitude = (String) aValue;
                            break;
                        case 3:
                            info.longitude = (String) aValue;
                    }
                }
            }
        }
    }
}