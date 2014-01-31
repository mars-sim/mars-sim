/**
 * Mars Simulation Project
 * TempSimulationConfigEditor.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.swing.JComboBoxMW;

/**
 * A temporary simulation configuration editor dialog.
 * Will be replaced by SimulationConfigEditor later when it is finished.
 */
public class TempSimulationConfigEditor extends JDialog {

    private static Logger logger = Logger.getLogger(TempSimulationConfigEditor.class.getName());
    
    // Data members.
    private SimulationConfig config;
    private SettlementTableModel settlementTableModel;
    private JTable settlementTable;
    private boolean hasError;
    private JLabel errorLabel;
    private JButton createButton;
    
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
        hasError = false;
        
        // Sets the dialog content panel.
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPanel);
        
        // Create the title label.
        JLabel titleLabel = new JLabel("Choose settlements for the new simulation:", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Create settlement scroll panel.
        JScrollPane settlementScrollPane = new JScrollPane();
        settlementScrollPane.setPreferredSize(new Dimension(585, 200));
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
        settlementTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        settlementScrollPane.setViewportView(settlementTable);
        
        // Create combo box for editing template column in settlement table.
        TableColumn templateColumn = settlementTable.getColumnModel().getColumn(1);
        JComboBox templateCB = new JComboBoxMW();
        SettlementConfig settlementConfig = config.getSettlementConfiguration();
        Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
        while (i.hasNext()) {
            templateCB.addItem(i.next().getTemplateName());
        }
        templateColumn.setCellEditor(new DefaultCellEditor(templateCB));
        
        // Create configuration button outer panel.
        JPanel configurationButtonOuterPanel = new JPanel(new BorderLayout(0, 0));
        add(configurationButtonOuterPanel, BorderLayout.EAST);
        
        // Create configuration button inner top panel.
        JPanel configurationButtonInnerTopPanel = new JPanel(new GridLayout(2, 1));
        configurationButtonOuterPanel.add(configurationButtonInnerTopPanel, BorderLayout.NORTH);
        
        // Create add settlement button.
        JButton addButton = new JButton("Add");
        addButton.setToolTipText("Add a new settlement");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addNewSettlement();
            }
        });
        configurationButtonInnerTopPanel.add(addButton);
        
        // Create remove settlement button.
        JButton removeButton = new JButton("Remove");
        removeButton.setToolTipText("Remove selected settlements");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedSettlements();
            } 
        });
        configurationButtonInnerTopPanel.add(removeButton);
        
        // Create configuration button inner bottom panel.
        JPanel configurationButtonInnerBottomPanel = new JPanel(new GridLayout(1, 1));
        configurationButtonOuterPanel.add(configurationButtonInnerBottomPanel, BorderLayout.SOUTH);
        
        // Create default button.
        JButton defaultButton = new JButton("Default");
        defaultButton.setToolTipText("Reset to default settlements");
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDefaultSettlements();
            }
        });
        configurationButtonInnerBottomPanel.add(defaultButton);
        
        // Create bottom panel.
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Create error label.
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        bottomPanel.add(errorLabel, BorderLayout.NORTH);
        
        // Create the bottom button panel.
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        
        // Create the create button.
        createButton = new JButton("Create New Simulation");
        createButton.setToolTipText("Create a new simulation");
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// Make sure any editing cell is completed, then check if error.
            	TableCellEditor editor = settlementTable.getCellEditor();
            	if (editor != null) {
            		editor.stopCellEditing();
            	}
            	if (!hasError) {
            		setConfiguration();
            		closeWindow();
            	}
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
        SettlementInfo settlement = determineNewSettlementConfiguration();
        settlementTableModel.addSettlement(settlement);
    }
    
    /**
     * Removes the settlements selected on the table.
     */
    private void removeSelectedSettlements() {
        settlementTableModel.removeSettlements(settlementTable.getSelectedRows());
    }
    
    /**
     * Sets the default settlements from the loaded configuration.
     */
    private void setDefaultSettlements() {
        settlementTableModel.loadDefaultSettlements();
    }
    
    /**
     * Set the simulation configuration based on dialog choices.
     */
    private void setConfiguration() {
        SettlementConfig settlementConfig = config.getSettlementConfiguration();
        
        // Clear configuration settlements.
        settlementConfig.clearInitialSettlements();
        
        // Add configuration settlements from table data.
        for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
            String name = (String) settlementTableModel.getValueAt(x, 0);
            String template = (String) settlementTableModel.getValueAt(x, 1);
            String population = (String) settlementTableModel.getValueAt(x, 2);
            int populationNum = Integer.parseInt(population);
            String latitude = (String) settlementTableModel.getValueAt(x, 3);
            String longitude = (String) settlementTableModel.getValueAt(x, 4);
            settlementConfig.addInitialSettlement(name, template, populationNum, latitude, longitude);
        }
    }
    
    /**
     * Close and dispose dialog window.
     */
    private void closeWindow() {
        dispose();
    }
    
    /**
     * Sets an edit-check error.
     * @param errorString the error description.
     */
    private void setError(String errorString) {
        if (!hasError) {
            hasError = true;
            errorLabel.setText(errorString);
            createButton.setEnabled(false);
        }
    }
    
    /**
     * Clears all edit-check errors.
     */
    private void clearError() {
        hasError = false;
        errorLabel.setText("");
        createButton.setEnabled(true);
    }
    
    /**
     * Determines the configuration of a new settlement.
     * @return settlement configuration.
     */
    private SettlementInfo determineNewSettlementConfiguration() {
        SettlementInfo settlement = new SettlementInfo();
        
        settlement.name = determineNewSettlementName();
        settlement.template = determineNewSettlementTemplate();
        settlement.population = determineNewSettlementPopulation(settlement.template);
        settlement.latitude = determineNewSettlementLatitude();
        settlement.longitude = determineNewSettlementLongitude();
        
        return settlement;
    }
    
    /**
     * Determines a new settlement's name.
     * @return name.
     */
    private String determineNewSettlementName() {
        String result = null;
  
        // Try to find unique name in configured settlement name list.
        // Randomly shuffle settlement name list first.
        SettlementConfig settlementConfig = config.getSettlementConfiguration();
        List<String> settlementNames = settlementConfig.getSettlementNameList();
        Collections.shuffle(settlementNames);
        Iterator<String> i = settlementNames.iterator();
        while (i.hasNext()) {
            String name = i.next();
            
            // Make sure settlement name isn't already being used in table.
            boolean nameUsed = false;
            for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
                if (name.equals(settlementTableModel.getValueAt(x, 0))) {
                    nameUsed = true;
                }
            }
            
            // If not being used already, use this settlement name.
            if (!nameUsed) {
                result = name;
                break;
            }
        }
        
        // If no name found, create numbered settlement name: "Settlement 1", "Settlement 2", etc.
        int count = 1;
        while (result == null) {
            String name = "Settlement " + count;
            
            // Make sure settlement name isn't already being used in table.
            boolean nameUsed = false;
            for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
                if (name.equals(settlementTableModel.getValueAt(x, 0))) {
                    nameUsed = true;
                }
            }
            
            // If not being used already, use this settlement name.
            if (!nameUsed) {
                result = name;
            }
            
            count++;
        }
        
        return result;
    }
    
    /**
     * Determines a new settlement's template.
     * @return template name.
     */
    private String determineNewSettlementTemplate() {
        String result = null;
        
        SettlementConfig settlementConfig = config.getSettlementConfiguration();
        List<SettlementTemplate> templates = settlementConfig.getSettlementTemplates();
        if (templates.size() > 0) {
            int index = RandomUtil.getRandomInt(templates.size() - 1);
            result = templates.get(index).getTemplateName();
        }
        else logger.log(Level.WARNING, "No configured settlement templates found");
        
        return result;
    }
    
    /**
     * Determines the new settlement population.
     * @param templateName the settlement template name.
     * @return the new population number.
     */
    private String determineNewSettlementPopulation(String templateName) {
    	
    	String result = "0";
    	
    	if (templateName != null) {
    		SettlementConfig settlementConfig = config.getSettlementConfiguration();
    		Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
    		while (i.hasNext()) {
    			SettlementTemplate template = i.next();
    			if (template.getTemplateName().equals(templateName)) {
    				result = Integer.toString(template.getDefaultPopulation());
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Determines a new settlement's latitude.
     * @return latitude string.
     */
    private String determineNewSettlementLatitude() {
        double phi = Coordinates.getRandomLatitude();
        String formattedLatitude = Coordinates.getFormattedLatitudeString(phi);
        int degreeIndex = formattedLatitude.indexOf('\u00BA');
        return formattedLatitude.substring(0, degreeIndex) + formattedLatitude.substring(degreeIndex + 1, 
                formattedLatitude.length());
    }
    
    /**
     * Determines a new settlement's longitude.
     * @return longitude string.
     */
    private String determineNewSettlementLongitude() {
        double theta = Coordinates.getRandomLongitude();
        String formattedLongitude = Coordinates.getFormattedLongitudeString(theta);
        int degreeIndex = formattedLongitude.indexOf('\u00BA');
        return formattedLongitude.substring(0, degreeIndex) + formattedLongitude.substring(degreeIndex + 1, 
                formattedLongitude.length());
    }
    
    /**
     * Inner class representing a settlement configuration.
     */
    private class SettlementInfo {
        String name;
        String template;
        String population;
        String latitude;
        String longitude;
    }
    
    /**
     * Inner class for the settlement table model.
     */
    private class SettlementTableModel extends AbstractTableModel {
        
        // Data members
        private String[] columns;
        private List<SettlementInfo> settlements;
        
        /**
         * Constructor
         */
        private SettlementTableModel() {
            super();
            
            // Add table columns.
            columns = new String[] { "Name", "Template", "Population", "Latitude", "Longitude" };
            
            // Load default settlements.
            settlements = new ArrayList<SettlementInfo>();
            loadDefaultSettlements();
        }
        
        /**
         * Load the default settlements in the table.
         */
        private void loadDefaultSettlements() {
            SettlementConfig settlementConfig = config.getSettlementConfiguration();
            settlements.clear();
            for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
                SettlementInfo info = new SettlementInfo();
                info.name = settlementConfig.getInitialSettlementName(x);
                info.template = settlementConfig.getInitialSettlementTemplate(x);
                info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
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
                        	result = info.population;
                        	break;
                        case 3:
                            result = info.latitude;
                            break;
                        case 4:
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
//                            info.population = determineNewSettlementPopulation(info.template);
                            break;
                        case 2:
                        	info.population = (String) aValue;
                        	break;
                        case 3:
                            info.latitude = (String) aValue;
                            break;
                        case 4:
                            info.longitude = (String) aValue;
                    }
                }
                
                checkForErrors();
            }
        }
        
        /**
         * Remove a set of settlements from the table.
         * @param rowIndexes an array of row indexes of the settlements to remove.
         */
        private void removeSettlements(int[] rowIndexes) {
            List<SettlementInfo> removedSettlements = new ArrayList<SettlementInfo>(rowIndexes.length);
            
            for (int x = 0; x < rowIndexes.length; x++) {
                if ((rowIndexes[x] > -1) && (rowIndexes[x] < getRowCount())) {
                    removedSettlements.add(settlements.get(rowIndexes[x]));
                }
            }
            
            Iterator<SettlementInfo> i = removedSettlements.iterator();
            while (i.hasNext()) {
                settlements.remove(i.next());
            }
            
            fireTableDataChanged();
        }
        
        /**
         * Adds a new settlement to the table.
         * @param settlement the settlement configuration.
         */
        private void addSettlement(SettlementInfo settlement) {
            settlements.add(settlement);
            fireTableDataChanged();
        }
        
        /**
         * Check for errors in table settlement values.
         */
        private void checkForErrors() {
            clearError();
            
            Iterator<SettlementInfo> i = settlements.iterator();
            while (i.hasNext()) {
                SettlementInfo settlement = i.next();
                
                // Check that settlement name is valid.
                if ((settlement.name == null) || (settlement.name.isEmpty())) {
                    setError("Settlement name cannot be blank");
                }
                
                // Check if population is valid.
                if ((settlement.population == null) || (settlement.population.isEmpty())) {
                	setError("Settlement population cannot be blank");
                }
                else {
                	try {
                		int popInt = Integer.parseInt(settlement.population);
                		if (popInt < 0) {
                			setError("Settlement population must be 0 or larger");
                		}
                	}
                	catch (NumberFormatException e) {
                		setError("Settlement population must be a valid integer number");
                	}
                }
                
                // Check that settlement latitude is valid.
                if ((settlement.latitude == null) || (settlement.latitude.isEmpty())) {
                    setError("Settlement latitude cannot be blank");
                }
                else {
                    String cleanLatitude = settlement.latitude.trim().toUpperCase();
                    if (!cleanLatitude.endsWith("N") && !cleanLatitude.endsWith("S")) {
                        setError("Settlement latitude must end with direction 'N' or 'S'");
                    }
                    else {
                        String numLatitude = cleanLatitude.substring(0, cleanLatitude.length() - 1);
                        try {
                            double doubleLatitude = Double.parseDouble(numLatitude);
                            if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
                                setError("Settlement latitude must begin with a number between 0 and 90");
                            }
                        }
                        catch(NumberFormatException e) {
                            setError("Settlement latitude must begin with a number between 0 and 90");
                        }
                    }
                }
                
                // Check that settlement longitude is valid.
                if ((settlement.longitude == null) || (settlement.longitude.isEmpty())) {
                    setError("Settlement longitude cannot be blank");
                }
                else {
                    String cleanLongitude = settlement.longitude.trim().toUpperCase();
                    if (!cleanLongitude.endsWith("W") && !cleanLongitude.endsWith("E")) {
                        setError("Settlement longitude must end with direction 'W' or 'E'");
                    }
                    else {
                        String numLongitude = cleanLongitude.substring(0, cleanLongitude.length() - 1);
                        try {
                            double doubleLongitude = Double.parseDouble(numLongitude);
                            if ((doubleLongitude < 0) || (doubleLongitude > 180)) {
                                setError("Settlement longitude must begin with a number between 0 and 180");
                            }
                        }
                        catch(NumberFormatException e) {
                            setError("Settlement longitude must begin with a number between 0 and 180");
                        }
                    }
                }
            }
        }
    }
}