/**
 * Mars Simulation Project
 * ArrivingSettlementEditingPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A panel for creating or editing an arriving settlement.
 */
public class ArrivingSettlementEditingPanel extends TransportItemEditingPanel {

    // Data members
    private ArrivingSettlement settlement;
    private JTextField nameTF;
    private JComboBoxMW templateCB;
    private JRadioButton arrivalDateRB;
    private JLabel arrivalDateTitleLabel;
    private JRadioButton timeUntilArrivalRB;
    private JLabel timeUntilArrivalLabel;
    private MartianSolComboBoxModel martianSolCBModel;
    private JLabel solLabel;
    private JComboBoxMW solCB;
    private JLabel monthLabel;
    private JComboBoxMW monthCB;
    private JLabel orbitLabel;
    private JComboBoxMW orbitCB;
    private JTextField solsTF;
    private JLabel solInfoLabel;
    private JTextField latitudeTF;
    private JComboBoxMW latitudeDirectionCB;
    private JTextField longitudeTF;
    private JComboBoxMW longitudeDirectionCB;
    private JTextField populationTF;
    private JLabel errorLabel;
    
    /**
     * Constructor.
     * @param settlement the arriving settlement to modify
     * or null if creating a new one.
     */
    public ArrivingSettlementEditingPanel(ArrivingSettlement settlement) {
        // User TransportItemEditingPanel constructor
        super(settlement);
        
        // Initialize data members.
        this.settlement = settlement;
        
        setBorder(new MarsPanelBorder());
        setLayout(new BorderLayout(0, 0));
        
        // Create top edit pane.
        JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
        add(topEditPane, BorderLayout.NORTH);
        
        // Create top inner edit pane.
        JPanel topInnerEditPane = new JPanel(new GridLayout(3, 1, 0, 10));
        topEditPane.add(topInnerEditPane, BorderLayout.NORTH);

        // Create name pane.
        JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topInnerEditPane.add(namePane);

        // Create name title label.
        JLabel nameTitleLabel = new JLabel("Settlement Name: ");
        namePane.add(nameTitleLabel);

        // Create name text field.
        nameTF = new JTextField("", 25);
        if (settlement != null) {
            nameTF.setText(settlement.getName());
        }
        namePane.add(nameTF);
        
        // Create the template pane.
        JPanel templatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topInnerEditPane.add(templatePane);
        
        // Create template title label.
        JLabel templateTitleLabel = new JLabel("Layout Template: ");
        templatePane.add(templateTitleLabel);
        
        // Create template combo box.
        Vector<String> templateNames = new Vector<String>();
        Iterator<SettlementTemplate> i = SimulationConfig.instance().
                getSettlementConfiguration().getSettlementTemplates().iterator();
        while (i.hasNext()) {
            templateNames.add(i.next().getTemplateName());
        }
        templateCB = new JComboBoxMW(templateNames);
        if (settlement != null) {
            templateCB.setSelectedItem(settlement.getTemplate());
        }
        templateCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // Update template population num.
                updateTemplatePopulationNum((String) templateCB.getSelectedItem());
            }
        });
        templatePane.add(templateCB);
        
        // Create population panel.
        JPanel populationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topInnerEditPane.add(populationPane);

        // Create population label.
        JLabel populationLabel = new JLabel("Population Number: ");
        populationPane.add(populationLabel);

        // Create population text field.
        int populationNum = 0;
        if (settlement != null) {
            populationNum = settlement.getPopulationNum();
        }
        else {
            // Update the population number based on selected template.
            String templateName = (String) templateCB.getSelectedItem();
            if (templateName != null) {
                SettlementTemplate template = SimulationConfig.instance().
                        getSettlementConfiguration().getSettlementTemplate(templateName);
                if (template != null) {
                    populationNum = template.getDefaultPopulation();
                }
            }
        }
        populationTF = new JTextField(6);
        populationTF.setText(Integer.toString(populationNum));
        populationTF.setHorizontalAlignment(JTextField.RIGHT);
        populationPane.add(populationTF);
        
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

        // Get default arriving settlement Martian time.
        MarsClock arrivingTime = Simulation.instance().getMasterClock().getMarsClock();
        if (settlement != null) {
            arrivingTime = settlement.getArrivalDate();
        }

        // Create sol label.
        solLabel = new JLabel("Sol");
        arrivalDateSelectionPane.add(solLabel);

        // Create sol combo box.
        martianSolCBModel = new MartianSolComboBoxModel(arrivingTime.getMonth(), arrivingTime.getOrbit());
        solCB = new JComboBoxMW(martianSolCBModel);
        solCB.setSelectedItem(arrivingTime.getSolOfMonth());
        arrivalDateSelectionPane.add(solCB);

        // Create month label.
        monthLabel = new JLabel("Month");
        arrivalDateSelectionPane.add(monthLabel);

        // Create month combo box.
        monthCB = new JComboBoxMW(MarsClock.getMonthNames());
        monthCB.setSelectedItem(arrivingTime.getMonthName());
        monthCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Update sol combo box values.
                martianSolCBModel.updateSolNumber((monthCB.getSelectedIndex() + 1), 
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
        int startOrbit = arrivingTime.getOrbit();
        for (int x = 0; x < 20; x++) {
            orbitValues[x] = formatter.format(startOrbit + x);
        }
        orbitCB = new JComboBoxMW(orbitValues);
        orbitCB.setSelectedItem(formatter.format(startOrbit));
        orbitCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Update sol combo box values.
                martianSolCBModel.updateSolNumber((monthCB.getSelectedIndex() + 1), 
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
        int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, currentTime) / 1000D));
        solsTF = new JTextField(6);
        solsTF.setText(Integer.toString(solsDiff));
        solsTF.setHorizontalAlignment(JTextField.RIGHT);
        solsTF.setEnabled(false);
        timeUntilArrivalPane.add(solsTF);

        // Create sol information label.
        solInfoLabel = new JLabel("(668 Sols = 1 Martian Orbit)");
        solInfoLabel.setEnabled(false);
        timeUntilArrivalPane.add(solInfoLabel);

        // Create landing location panel.
        JPanel landingLocationPane = new JPanel(new GridLayout(2, 1, 0, 10));
        landingLocationPane.setBorder(new TitledBorder("Landing Location"));
        topEditPane.add(landingLocationPane, BorderLayout.SOUTH);
        
        // Create latitude panel.
        JPanel latitudePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        landingLocationPane.add(latitudePane);
        
        // Create latitude title label.
        JLabel latitudeTitleLabel = new JLabel("Latitude: ");
        latitudePane.add(latitudeTitleLabel);
        
        // Create latitude text field.
        latitudeTF = new JTextField("0.0", 4);
        if (settlement != null) {
            String latString = settlement.getLandingLocation().getFormattedLatitudeString();
            // Remove last two characters from formatted latitude string.
            String cleanLatString = latString.substring(0, latString.length() - 3);
            latitudeTF.setText(cleanLatString);
        }
        latitudeTF.setHorizontalAlignment(JTextField.RIGHT);
        latitudePane.add(latitudeTF);
        
        // Create latitude degrees label.
        JLabel latDegLabel = new JLabel("\u00BA");
        latitudePane.add(latDegLabel);
        
        // Create latitude direction combo box.
        latitudeDirectionCB = new JComboBoxMW();
        latitudeDirectionCB.addItem("N");
        latitudeDirectionCB.addItem("S");
        if (settlement != null) {
            String latString = settlement.getLandingLocation().getFormattedLatitudeString();
            // Get last character in formatted string. ex: "S".
            String dirString = latString.substring(latString.length() - 1, latString.length());
            latitudeDirectionCB.setSelectedItem(dirString);
        }
        latitudePane.add(latitudeDirectionCB);
        
        // Create longitude panel.
        JPanel longitudePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        landingLocationPane.add(longitudePane);
        
        // Create longitude title label.
        JLabel longitudeTitleLabel = new JLabel("Longitude: ");
        longitudePane.add(longitudeTitleLabel);
        
        // Create longitude text field.
        longitudeTF = new JTextField("0.0", 4);
        if (settlement != null) {
            String lonString = settlement.getLandingLocation().getFormattedLongitudeString();
            // Remove last three characters from formatted longitude string.
            String cleanLonString = lonString.substring(0, lonString.length() - 3);
            longitudeTF.setText(cleanLonString);
        }
        longitudeTF.setHorizontalAlignment(JTextField.RIGHT);
        longitudePane.add(longitudeTF);
        
        // Create longitude degrees label.
        JLabel lonDegLabel = new JLabel("\u00BA");
        longitudePane.add(lonDegLabel);
        
        // Create longitude direction combo box.
        longitudeDirectionCB = new JComboBoxMW();
        longitudeDirectionCB.addItem("W");
        longitudeDirectionCB.addItem("E");
        if (settlement != null) {
            String lonString = settlement.getLandingLocation().getFormattedLongitudeString();
            // Get last character in formatted string. ex: "W".
            String dirString = lonString.substring(lonString.length() - 1, lonString.length());
            longitudeDirectionCB.setSelectedItem(dirString);
        }
        longitudePane.add(longitudeDirectionCB);
        
        // Create error pane.
        JPanel errorPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(errorPane, BorderLayout.SOUTH);
        
        // Create error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorPane.add(errorLabel);
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
     * Updates the population number text field value based on the selected template name.
     * @param templateName the template name.
     */
    private void updateTemplatePopulationNum(String templateName) {
        if (templateName != null) {
            SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
                    getSettlementTemplate(templateName);
            if (template != null) {
                populationTF.setText(Integer.toString(template.getDefaultPopulation()));
            }
        }
    }
    
    /**
     * Validate the arriving settlement data.
     * @return true if data is valid.
     */
    private boolean validateData() {
        boolean result = true;
        String errorString = "";
        
        // Validate settlement name.
        if (nameTF.getText().trim().isEmpty()) {
            result = false;
            errorString = "Settlement name cannot not be blank.";
        }
        
        // Validate template.
        String templateName = (String) templateCB.getSelectedItem();
        if ((templateName == null) || templateName.trim().isEmpty()) {
            result = false;
            errorString = "A settlement template must be selected.";
        }
        
        // Validate population number.
        String populationNumString = populationTF.getText();
        if (populationNumString.trim().isEmpty()) {
            result = false;
            errorString = "Population number cannot be blank. (0 is allowed)";
        }
        else {
            try {
                int popNum = Integer.parseInt(populationNumString);
                if (popNum < 0) {
                    result = false;
                    errorString = "Population number cannot be less than 0.";
                }
            }
            catch (NumberFormatException e) {
                result = false;
                errorString = "Population number must be a valid integer value.";
            }
        }
        
        // Validate sols until arrival number.
        if (solsTF.isEnabled()) {
            String timeArrivalString = solsTF.getText().trim();
            if (timeArrivalString.isEmpty()) {
                result = false;
                errorString = "Sols until arrival cannot be blank.";
            }
            else {
                try {
                    double timeArrival = Double.parseDouble(timeArrivalString);
                    if (timeArrival < 0D) {
                        result = false;
                        errorString = "Sols until arrival cannot be less than 0.";
                    }
                }
                catch (NumberFormatException e) {
                    result = false;
                    errorString = "Sols until arrival must be a valid number.";
                }
            }
        }
        
        // Validate latitude value.
        String latitudeString = latitudeTF.getText().trim();
        if (latitudeString.isEmpty()) {
            result = false;
            errorString = "Latitude value cannot be blank.";
        }
        else {
            try {
                Double latitudeValue = Double.parseDouble(latitudeString);
                if ((latitudeValue < 0D) || (latitudeValue > 90D)) {
                    result = false;
                    errorString = "Latitude value must be in the range of 0.0 to 90.0.";
                }
            }
            catch (NumberFormatException e) {
                result = false;
                errorString = "Latitude value must be a valid number. (0.0 - 90.0)";
            }
        }
        
        // Validate longitude value.
        String longitudeString = longitudeTF.getText().trim();
        if (longitudeString.isEmpty()) {
            result = false;
            errorString = "Longitude value cannot be blank.";
        }
        else {
            try {
                Double longitudeValue = Double.parseDouble(longitudeString);
                if ((longitudeValue < 0D) || (longitudeValue > 180D)) {
                    result = false;
                    errorString = "Longitude value must be in the range of 0.0 to 180.0.";
                }
            }
            catch (NumberFormatException e) {
                result = false;
                errorString = "Longitude value must be a valid number. (0.0 - 180.0)";
            }
        }
        
        // Check that landing location is not at an existing settlement's location.
        if (result && !validateLandingLocation()) {
            result = false;
            errorString = "Landing location cannot be at an existing settlement's location.";
        }
        
        errorLabel.setText(errorString);
        
        return result;
    }
    
    /**
     * Validate that landing location is not equal to an existing settlement's location.
     * @return true if good landing location.
     */
    private boolean validateLandingLocation() {
        boolean result = true;
        
        String fullLatString = latitudeTF.getText().trim() + " " + latitudeDirectionCB.getSelectedItem();
        String fullLonString = longitudeTF.getText().trim() + " " + longitudeDirectionCB.getSelectedItem();
        try {
            Coordinates landingLocation = new Coordinates(fullLatString, fullLonString);
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext()) {
                if (i.next().getCoordinates().equals(landingLocation)) {
                    result = false;
                }
            }
        }
        catch (IllegalStateException e) {
            e.printStackTrace(System.err);
            result = false;
        }
        
        return result;
    }

    @Override
    public boolean modifyTransportItem() {
        // Validate the arriving settlement data.
        if (validateData()) {
            populateArrivingSettlement(settlement);
            settlement.commitModification();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean createTransportItem() {
        // Validate the arriving settlement data.
        if (validateData()) {
            String name = nameTF.getText().trim();
            String template = (String) templateCB.getSelectedItem();
            int popNum = Integer.parseInt(populationTF.getText());
            MarsClock arrivalDate = getArrivalDate();
            Coordinates landingLoc = getLandingLocation();
            ArrivingSettlement newArrivingSettlement = new ArrivingSettlement(name, template, 
                    arrivalDate, landingLoc, popNum);
            populateArrivingSettlement(newArrivingSettlement);
            Simulation.instance().getTransportManager().addNewTransportItem(newArrivingSettlement);
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Populate the arriving settlement with UI data.
     * @param settlement the arriving settlement to populate.
     */
    private void populateArrivingSettlement(ArrivingSettlement settlement) {
        
        // Populate settlement name
        settlement.setName(nameTF.getText().trim());
        
        // Populate template.
        settlement.setTemplate((String) templateCB.getSelectedItem());
        
        // Populate arrival date.
        MarsClock arrivalDate = getArrivalDate();
        settlement.setArrivalDate(arrivalDate);
        
        // Populate launch date.
        MarsClock launchDate = (MarsClock) arrivalDate.clone();
        launchDate.addTime(-1D * ResupplyUtil.AVG_TRANSIT_TIME * 1000D);
        settlement.setLaunchDate(launchDate);
        
        // Set transit state based on launch and arrival time.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        String state = Transportable.PLANNED;
        if (MarsClock.getTimeDiff(currentTime, launchDate) > 0D) {
            state = Transportable.IN_TRANSIT;
            if (MarsClock.getTimeDiff(currentTime, arrivalDate) > 0D) {
                state = Transportable.ARRIVED;
            }
        }
        settlement.setTransitState(state);
        
        // Set population number.
        int popNum = Integer.parseInt(populationTF.getText());
        settlement.setPopulationNum(popNum);
        
        // Set landing location.
        Coordinates landingLocation = getLandingLocation();
        settlement.setLandingLocation(landingLocation);
    }
    
    /**
     * Gets the arrival date from the UI values.
     * @return arrival date as MarsClock instance.
     */
    private MarsClock getArrivalDate() {
        
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        MarsClock result = (MarsClock) currentTime.clone();

        if (arrivalDateRB.isSelected()) {
            // Determine arrival date from arrival date combo boxes.
            try {
                int sol = solCB.getSelectedIndex() + 1;
                int month = monthCB.getSelectedIndex() + 1;
                int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

                // Set millisols to current time if resupply is current date, otherwise 0.
                double millisols = 0D;
                if ((sol == currentTime.getSolOfMonth()) && (month == currentTime.getMonth()) && 
                        (orbit == currentTime.getOrbit())) {
                    millisols = currentTime.getMillisol();
                }

                result = new MarsClock(orbit, month, sol, millisols);
            }
            catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }
        else if (timeUntilArrivalRB.isSelected()) {
            // Determine arrival date from time until arrival text field.
            try {
                int solsDiff = Integer.parseInt(solsTF.getText());
                if (solsDiff > 0) {
                    result.addTime(solsDiff * 1000D);
                }
                else {
                    result.addTime(currentTime.getMillisol());
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }

        return result;
    }
    
    /**
     * Gets the landing location from the UI values.
     * @return landing location coordinates.
     */
    private Coordinates getLandingLocation() {
        
        String fullLatString = latitudeTF.getText().trim() + " " + latitudeDirectionCB.getSelectedItem();
        String fullLonString = longitudeTF.getText().trim() + " " + longitudeDirectionCB.getSelectedItem();
        return new Coordinates(fullLatString, fullLonString);
    }
}