/**
 * Mars Simulation Project
 * ArrivingSettlementEditingPanel.java
 * @version 3.1.0 2017-02-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
//import org.mars_sim.network.ClientRegistry;
//import org.mars_sim.network.SettlementRegistry;

/**
 * A panel for creating or editing an arriving settlement.
 */
public class ArrivingSettlementEditingPanel extends TransportItemEditingPanel {

	// Data members
	private String errorString = new String();
	private boolean validation_result = true;

	private JTextField nameTF;
	private JComboBoxMW<String> templateCB;
	private JRadioButton arrivalDateRB;
	private JLabel arrivalDateTitleLabel;
	private JRadioButton timeUntilArrivalRB;
	private JLabel timeUntilArrivalLabel;
	private MartianSolComboBoxModel martianSolCBModel;
	private JLabel solLabel;
	private JComboBoxMW<?> solCB;
	private JLabel monthLabel;
	private JComboBoxMW<?> monthCB;
	private JLabel orbitLabel;
	private JComboBoxMW<?> orbitCB;
	private JTextField solsTF;
	private JLabel solInfoLabel;
	private JTextField latitudeTF;
	private JComboBoxMW<String> latitudeDirectionCB;
	private JTextField longitudeTF;
	private JComboBoxMW<String> longitudeDirectionCB;
	private JLabel errorLabel;
	private JTextField populationTF;
	private JTextField numOfRobotsTF;

	private ModifyTransportItemDialog modifyTransportItemDialog;
	private ResupplyWindow resupplyWindow;
	private NewTransportItemDialog newTransportItemDialog;
	private ArrivingSettlement settlement;
//	private List<SettlementRegistry> settlementList;

	/**
	 * Constructor.
	 * 
	 * @param settlement                the arriving settlement to modify or null if
	 *                                  creating a new one.
	 * @param resupplywindow
	 * @param modifyTransportItemDialog
	 * @param newTransportItemDialog
	 */
	public ArrivingSettlementEditingPanel(ArrivingSettlement settlement, ResupplyWindow resupplyWindow,
			ModifyTransportItemDialog modifyTransportItemDialog, NewTransportItemDialog newTransportItemDialog) {
		// User TransportItemEditingPanel constructor
		super(settlement);
		this.modifyTransportItemDialog = modifyTransportItemDialog;
		this.resupplyWindow = resupplyWindow;
		this.newTransportItemDialog = newTransportItemDialog;
		// Initialize data members.
		this.settlement = settlement;

		setBorder(new MarsPanelBorder());
		setLayout(new BorderLayout(0, 0));

		// Create top edit pane.
		JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
		add(topEditPane, BorderLayout.NORTH);

		JPanel topPane = new JPanel(new BorderLayout(10, 10));// GridLayout(2, 1));
		topEditPane.add(topPane, BorderLayout.NORTH);

		// Create top spring layout.
		JPanel topSpring = new JPanel(new SpringLayout());// GridLayout(3, 1, 0, 10));
		topPane.add(topSpring, BorderLayout.NORTH);

		// Create name pane.
		// JPanel namePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// topInnerEditPane.add(namePane);

		// Create name title label.
		JLabel nameTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.settlementName"), //$NON-NLS-1$
				JLabel.TRAILING);
		// namePane.add(nameTitleLabel);
		topSpring.add(nameTitleLabel);

		// Create name text field.
		nameTF = new JTextField(new String(), 25);
		if (settlement != null) {
			nameTF.setText(settlement.getName());
		}
		// namePane.add(nameTF);
		topSpring.add(nameTF);

		// Create the template pane.
		// JPanel templatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// topInnerEditPane.add(templatePane);

		// Create template title label.
		JLabel templateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.layoutTemplate"), //$NON-NLS-1$
				JLabel.TRAILING);
		// templatePane.add(templateTitleLabel);
		topSpring.add(templateTitleLabel);

		// Create template combo box.
		Vector<String> templateNames = new Vector<String>();
		Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
		while (i.hasNext()) {
			templateNames.add(i.next().getTemplateName());
		}
		templateCB = new JComboBoxMW<String>(templateNames);
		if (settlement != null) {
			templateCB.setSelectedItem(settlement.getTemplate());
		}
		templateCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateTemplatePopulationNum((String) templateCB.getSelectedItem());
				updateTemplateNumOfRobots((String) templateCB.getSelectedItem());
				// 2015-04-23
				updateTemplateLatitude((String) templateCB.getSelectedItem());
				updateTemplateLongitude((String) templateCB.getSelectedItem());
			}
		});
		// templatePane.add(templateCB);
		topSpring.add(templateCB);

		// 2017-02-11 Create sponsor label.
		JLabel sponsorTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.sponsoringAgency"), //$NON-NLS-1$
				JLabel.TRAILING);
		topSpring.add(sponsorTitleLabel);

		// Create template combo box.
		Vector<String> sponsorNames = new Vector<String>();
		Iterator<String> ii = personConfig.createAllCountryList().iterator();
		while (ii.hasNext()) {
			sponsorNames.add(ii.next());
		}
		JComboBoxMW<String> sponsorCB = new JComboBoxMW<String>(sponsorNames);
		if (settlement != null) {
			sponsorCB.setSelectedItem(settlement.getTemplate());
		}
		sponsorCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			}
		});
		topSpring.add(sponsorCB);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(topSpring, 3, 2, // rows, cols
				50, 10, // initX, initY
				10, 10); // xPad, yPad

		JPanel numPane = new JPanel(new GridLayout(1, 2));
		// topInnerEditPane.add(numPane);
		topPane.add(numPane, BorderLayout.CENTER);

		// Create population panel.
		JPanel populationPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		numPane.add(populationPane);

		// Create population label.
		JLabel populationLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.population")); //$NON-NLS-1$
		populationPane.add(populationLabel);

		// Create robot number panel.
		JPanel numOfRobotsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		numPane.add(numOfRobotsPane);

		// Create robot number label.
		JLabel numOfRobotsLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.numOfRobots")); //$NON-NLS-1$
		numOfRobotsPane.add(numOfRobotsLabel);

		// Create population text field.
		int populationNum = 0;
		int numOfRobots = 0;

		if (settlement != null) {
			populationNum = settlement.getPopulationNum();
			numOfRobots = settlement.getNumOfRobots();
		} else {
			// Update the population number based on selected template.
			String templateName = (String) templateCB.getSelectedItem();
			if (templateName != null) {
				SettlementTemplate template = settlementConfig.getSettlementTemplate(templateName);
				if (template != null) {
					populationNum = template.getDefaultPopulation();
					numOfRobots = template.getDefaultNumOfRobots();
				}
			}
		}
		populationTF = new JTextField(4);
		populationTF.setText(Integer.toString(populationNum));
		populationTF.setHorizontalAlignment(JTextField.RIGHT);
		populationPane.add(populationTF);

		numOfRobotsTF = new JTextField(4);
		numOfRobotsTF.setText(Integer.toString(numOfRobots));
		numOfRobotsTF.setHorizontalAlignment(JTextField.RIGHT);
		numOfRobotsPane.add(numOfRobotsTF);

		// Create arrival date pane.
		JPanel arrivalDatePane = new JPanel(new GridLayout(2, 1, 10, 10));
		arrivalDatePane.setBorder(new TitledBorder(Msg.getString("ArrivingSettlementEditingPanel.arrivalDate"))); //$NON-NLS-1$
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
		arrivalDateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.arrivalDate")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(arrivalDateTitleLabel);

		// Get default arriving settlement Martian time.
		MarsClock arrivingTime = Simulation.instance().getMasterClock().getMarsClock();
		if (settlement != null) {
			arrivingTime = settlement.getArrivalDate();
		}

		// Create sol label.
		solLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.sol")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(solLabel);

		// Create sol combo box.
		martianSolCBModel = new MartianSolComboBoxModel(arrivingTime.getMonth(), arrivingTime.getOrbit());
		solCB = new JComboBoxMW<Integer>(martianSolCBModel);
		solCB.setSelectedItem(arrivingTime.getSolOfMonth());
		arrivalDateSelectionPane.add(solCB);

		// Create month label.
		monthLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.month")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(monthLabel);

		// Create month combo box.
		monthCB = new JComboBoxMW<Object>(MarsClock.getMonthNames());
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
		orbitLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.orbit")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(orbitLabel);

		// Create orbit combo box.
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumIntegerDigits(2);
		String[] orbitValues = new String[20];
		int startOrbit = arrivingTime.getOrbit();
		for (int x = 0; x < 20; x++) {
			orbitValues[x] = formatter.format(startOrbit + x);
		}
		orbitCB = new JComboBoxMW<Object>(orbitValues);
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
		timeUntilArrivalLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.solUntilArrival")); //$NON-NLS-1$
		timeUntilArrivalLabel.setEnabled(false);
		timeUntilArrivalPane.add(timeUntilArrivalLabel);

		// Create sols text field.
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
		solsTF = new JTextField(4);
		solsTF.setText(Integer.toString(solsDiff));
		solsTF.setHorizontalAlignment(JTextField.RIGHT);
		solsTF.setEnabled(false);
		timeUntilArrivalPane.add(solsTF);

		// Create sol information label.
		solInfoLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.solsPerOrbit")); //$NON-NLS-1$
		solInfoLabel.setEnabled(false);
		timeUntilArrivalPane.add(solInfoLabel);

		JLabel emptyLabel = new JLabel("          ");

		JPanel southPane = new JPanel(new GridLayout(1, 2, 10, 10));
		topEditPane.add(southPane, BorderLayout.SOUTH);

		// Create landing location panel.
		JPanel landingLocationPane = new JPanel(new SpringLayout());// new GridLayout(2, 5, 0, 10));
		// landingLocationPane.setSize(300, 100);
		// landingLocationPane.setMaximumSize(new Dimension(300, 100));
		// landingLocationPane.setMinimumSize(new Dimension(300, 100));
		landingLocationPane
				.setBorder(new TitledBorder(Msg.getString("ArrivingSettlementEditingPanel.landingLocation"))); //$NON-NLS-1$

		southPane.add(landingLocationPane);
		southPane.add(emptyLabel);

		// Create latitude panel.
		// JPanel latitudePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		// landingLocationPane.add(latitudePane);

		// Create latitude title label.
		JLabel latitudeTitleLabel = new JLabel(Msg.getString("direction.latitude"), JLabel.TRAILING); //$NON-NLS-1$
		// latitudePane.add(latitudeTitleLabel);
		landingLocationPane.add(latitudeTitleLabel);

		// Create latitude text field.
		latitudeTF = new JTextField(4);
		latitudeTitleLabel.setLabelFor(latitudeTF);
		/*
		 * if (settlement != null) { String latString =
		 * settlement.getLandingLocation().getFormattedLatitudeString();
		 * System.out.println("ArrivingSettlementEditingPanel : latString is " +
		 * latString); // Remove last two characters from formatted latitude string.
		 * String cleanLatString = latString.substring(0, latString.length() - 3);
		 * latitudeTF.setText(cleanLatString); }
		 */
		latitudeTF.setHorizontalAlignment(JTextField.RIGHT);
		// latitudePane.add(latitudeTF);
		landingLocationPane.add(latitudeTF);

		// pull the degree sign into the comboboxes so it looks more like mars navigator
		// window
		String deg = Msg.getString("direction.degreeSign"); //$NON-NLS-1$

		// Create latitude direction combo box.
		latitudeDirectionCB = new JComboBoxMW<String>();
		latitudeDirectionCB.addItem(deg + Msg.getString("direction.northShort")); //$NON-NLS-1$
		latitudeDirectionCB.addItem(deg + Msg.getString("direction.southShort")); //$NON-NLS-1$
		if (settlement != null) {
			String latString = settlement.getLandingLocation().getFormattedLatitudeString();
			// System.out.println("ArrivingSettlementEditingPanel : latString is " +
			// latString);
			// Remove last two characters from formatted latitude string.
			String cleanLatString = latString.substring(0, latString.length() - 3);
			latitudeTF.setText(cleanLatString);

			// Get last character in formatted string. ex: "S".
			String dirString = latString.substring(latString.length() - 1, latString.length());
			latitudeDirectionCB.setSelectedItem(dirString);
		}
		// latitudePane.add(latitudeDirectionCB);
		landingLocationPane.add(latitudeDirectionCB);

		// Create longitude panel.
		// JPanel longitudePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		// landingLocationPane.add(longitudePane);

		// Create longitude title label.
		JLabel longitudeTitleLabel = new JLabel(Msg.getString("direction.longitude"), JLabel.TRAILING); //$NON-NLS-1$
		// longitudePane.add(longitudeTitleLabel);
		landingLocationPane.add(longitudeTitleLabel);
		// landingLocationPane.add(emptyLabel);
		// landingLocationPane.add(emptyLabel);

		// Create longitude text field.
		longitudeTF = new JTextField(4);
		longitudeTitleLabel.setLabelFor(longitudeTF);
//		if (settlement != null) {
//			String lonString = settlement.getLandingLocation().getFormattedLongitudeString();
//			System.out.println("ArrivingSettlementEditingPanel : lonString is " + lonString);
//			// Remove last three characters from formatted longitude string.
//			String cleanLonString = lonString.substring(0, lonString.length() - 3);
//			longitudeTF.setText(cleanLonString);
//		}

		longitudeTF.setHorizontalAlignment(JTextField.RIGHT);
		// longitudePane.add(longitudeTF);
		landingLocationPane.add(longitudeTF);

		// Create longitude direction combo box.
		longitudeDirectionCB = new JComboBoxMW<String>();
		longitudeDirectionCB.addItem(deg + Msg.getString("direction.westShort")); //$NON-NLS-1$
		longitudeDirectionCB.addItem(deg + Msg.getString("direction.eastShort")); //$NON-NLS-1$
		if (settlement != null) {
			String lonString = settlement.getLandingLocation().getFormattedLongitudeString();
			// System.out.println("ArrivingSettlementEditingPanel : lonString is " +
			// lonString);
			// Remove last three characters from formatted longitude string.
			String cleanLonString = lonString.substring(0, lonString.length() - 3);
			longitudeTF.setText(cleanLonString);
			// Get last character in formatted string. ex: "W".
			String dirString = lonString.substring(lonString.length() - 1, lonString.length());
			longitudeDirectionCB.setSelectedItem(dirString);
		}
		// longitudePane.add(longitudeDirectionCB);
		landingLocationPane.add(longitudeDirectionCB);
		// landingLocationPane.add(emptyLabel);
		// landingLocationPane.add(emptyLabel);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(landingLocationPane, 2, 3, // rows, cols
				20, 10, // initX, initY
				10, 10); // xPad, yPad

		// Create error pane.
		JPanel errorPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		add(errorPane, BorderLayout.SOUTH);

		// Create error label
		errorLabel = new JLabel(new String());
		errorLabel.setForeground(Color.RED);
		errorPane.add(errorLabel);

	}

	/**
	 * Set the components of the arrival date pane to be enabled or disabled.
	 * 
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
	 * 
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableTimeUntilArrivalPane(boolean enable) {
		timeUntilArrivalLabel.setEnabled(enable);
		solsTF.setEnabled(enable);
		solInfoLabel.setEnabled(enable);
	}

	/**
	 * Updates the population number text field value based on the selected template
	 * name.
	 * 
	 * @param templateName the template name.
	 */
	private void updateTemplatePopulationNum(String templateName) {
		if (templateName != null) {
			SettlementTemplate template = settlementConfig.getSettlementTemplate(templateName);
			if (template != null) {
				populationTF.setText(Integer.toString(template.getDefaultPopulation()));
			}
		}
	}

	/**
	 * Updates the numOfRobots text field value based on the selected template name.
	 * 
	 * @param templateName the template name.
	 */
	private void updateTemplateNumOfRobots(String templateName) {
		if (templateName != null) {
			SettlementTemplate template = settlementConfig.getSettlementTemplate(templateName);
			if (template != null) {
				numOfRobotsTF.setText(Integer.toString(template.getDefaultNumOfRobots()));
			}
		}
	}

	/**
	 * Updates the Latitude text field value based on the selected template name.
	 * 
	 * @param templateName the template name.
	 */
	private void updateTemplateLatitude(String templateName) {
	}

	/**
	 * Updates the Longitudetext field value based on the selected template name.
	 * 
	 * @param templateName the template name.
	 */
	private void updateTemplateLongitude(String templateName) {
	}

	/**
	 * Validate the arriving settlement data.
	 * 
	 * @param MultiplayerClient
	 * @return true if data is valid.
	 */
	private boolean validateData() {
		validation_result = true;
		errorString = null;

		// Validate settlement name.
		if (nameTF.getText().trim().isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noName"); //$NON-NLS-1$
		}

		// Validate template.
		String templateName = (String) templateCB.getSelectedItem();
		if ((templateName == null) || templateName.trim().isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noTemplate"); //$NON-NLS-1$
		}

		// Validate population number.
		String populationNumString = populationTF.getText();
		if (populationNumString.trim().isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noPopulation"); //$NON-NLS-1$
		} else {
			try {
				int popNum = Integer.parseInt(populationNumString);
				if (popNum < 0) {
					validation_result = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativePopulation"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validation_result = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidPopulation"); //$NON-NLS-1$
			}
		}

		// Validate numOfRobots.
		String numOfRobotsString = numOfRobotsTF.getText();
		if (numOfRobotsString.trim().isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.nonumOfRobots"); //$NON-NLS-1$
		} else {
			try {
				int numOfRobots = Integer.parseInt(numOfRobotsString);
				if (numOfRobots < 0) {
					validation_result = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativenumOfRobots"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validation_result = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidnumOfRobots"); //$NON-NLS-1$
			}
		}

		errorLabel.setText(errorString);

		// 2016-01-15 Implemented addChangeListener() to validate solsTF.
		if (solsTF.isEnabled()) {
			String timeArrivalString = solsTF.getText().trim();
			if (timeArrivalString.isEmpty()) {
				validation_result = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noSols"); //$NON-NLS-1$
				errorLabel.setText(errorString);
				enableButton(false);
				System.out.println("Invalid sol. It cannot be empty.");
			} else {
				// System.out.println("calling addChangeListener()");
				addChangeListener(solsTF, e -> validateSolsTF(timeArrivalString));

				if (errorString == null)
					validation_result = true;
				else
					validation_result = false;
			}
		}

		// Validate latitude value.
		String latitudeString = latitudeTF.getText().trim();
		if (latitudeString.isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noLatitude"); //$NON-NLS-1$
		} else {
			try {
				Double latitudeValue = Double.parseDouble(latitudeString);
				if ((latitudeValue < 0D) || (latitudeValue > 90D)) {
					validation_result = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.rangeLatitude"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validation_result = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidLatitude"); //$NON-NLS-1$
			}
		}

		// Validate longitude value.
		String longitudeString = longitudeTF.getText().trim();
		if (longitudeString.isEmpty()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noLongitude"); //$NON-NLS-1$
		} else {
			try {
				Double longitudeValue = Double.parseDouble(longitudeString);
				if ((longitudeValue < 0D) || (longitudeValue > 180D)) {
					validation_result = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.rangeLongitude"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validation_result = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidLongitude"); //$NON-NLS-1$
			}
		}

		// Check that landing location is not at an existing settlement's location.
		if (validation_result) { // && !validateLandingLocation()) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.collision"); //$NON-NLS-1$
		}

		errorLabel.setText(errorString);

		return validation_result;
	}

	public void validateSolsTF(String timeArrivalString) {
		// System.out.println("running validateSolsTF()");
		errorString = null;

		try {

			List<Integer> sols = new ArrayList<>();
			if (timeArrivalString.equals("-"))
				timeArrivalString = "-1";
			double timeArrival = Double.parseDouble(timeArrivalString);
			// int inputSols = Integer.parseInt(solsTF.getText());
			if (timeArrival < 0D) {
				validation_result = false;
				enableButton(false);
				System.out.println("Invalid entry! Sol must be greater than zero.");
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativeSols"); //$NON-NLS-1$
				errorLabel.setText(errorString);
			} else {
				boolean good = true;
				// Add checking if that sol has already been taken
				JList<?> jList = resupplyWindow.getIncomingListPane().getIncomingList();
				ListModel<?> model = jList.getModel();

				for (int i = 0; i < model.getSize(); i++) {
					Transportable transportItem = (Transportable) model.getElementAt(i);

					if ((transportItem != null)) {
						if (transportItem instanceof Resupply) {
							// Create modify resupply mission dialog.
							Resupply resupply = (Resupply) transportItem;
							MarsClock arrivingTime = resupply.getArrivalDate();
							int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
							sols.add(solsDiff);

						} else if (transportItem instanceof ArrivingSettlement) {
							// Create modify arriving settlement dialog.
							ArrivingSettlement newS = (ArrivingSettlement) transportItem;
							if (!newS.equals(settlement)) {
								MarsClock arrivingTime = newS.getArrivalDate();
								int solsDiff = (int) Math
										.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
								sols.add(solsDiff);
							}
						}
					}
				}

				// System.out.println("sols.size() : " + sols.size() );

				Iterator<Integer> i = sols.iterator();
				while (i.hasNext()) {
					int sol = i.next();
					if (sol == (int) timeArrival) {
						System.out.println("Invalid entry! Sol " + sol + " has already been taken.");
						validation_result = false;
						good = false;
						enableButton(false);
						errorString = Msg.getString("ArrivingSettlementEditingPanel.error.duplicatedSol"); //$NON-NLS-1$
						errorLabel.setText(errorString);
						break;
					}
				}

				if (good) {
					validation_result = true;
					errorString = null;
					errorLabel.setText(errorString);
					enableButton(true);
				}
			}
		} catch (NumberFormatException e) {
			validation_result = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidSols"); //$NON-NLS-1$
			enableButton(false);
			errorLabel.setText(errorString);
		}

	}

	public void enableButton(boolean value) {
		if (modifyTransportItemDialog != null)
			modifyTransportItemDialog.setCommitButton(value);
		else if (newTransportItemDialog != null)
			newTransportItemDialog.setCreateButton(value);
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document}, and a
	 * {@link PropertyChangeListener} on the text component to detect if the
	 * {@code Document} itself is replaced.
	 *
	 * @param text           any text component, such as a {@link JTextField} or
	 *                       {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s when the
	 *                       text is changed; the source object for the events will
	 *                       be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	// see
	// http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				// System.out.println("calling addChangeListener()'s changedUpdate()");
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();
			if (d1 != null)
				d1.removeDocumentListener(dl);
			if (d2 != null)
				d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = text.getDocument();
		if (d != null)
			d.addDocumentListener(dl);
	}

//	/**
//	 * Validate that landing location is not equal to an existing settlement's location.
//	 * @return true if good landing location.
//	 */
//	private boolean validateLandingLocation() {
//		boolean result = true;
//
//		String fullLatString = latitudeTF.getText().trim() + latitudeDirectionCB.getSelectedItem();
//		String fullLonString = longitudeTF.getText().trim() + longitudeDirectionCB.getSelectedItem();
//		try {
//			Coordinates landingLocation = new Coordinates(fullLatString, fullLonString);
//			Iterator<Settlement> i = unitManager.getSettlements().iterator();
//			while (i.hasNext()) {
//				if (i.next().getCoordinates().equals(landingLocation)) {
//					return false;
//				}
//			}
//
//			// 2015-04-23 Added checking of latitudes and longitudes of other clients' existing settlements
//			// Need to load a fresh, updated version of this list every time validateData() is called.
//			settlementList = ClientRegistry.getSettlementList();
//
//			String latitudeString = latitudeTF.getText().trim();
//			String longitudeString = longitudeTF.getText().trim();
//
//			Double latitudeValue = Double.parseDouble(latitudeString);
//			Double longitudeValue = Double.parseDouble(longitudeString);
//
//			latitudeValue = Math.abs(Math.round(latitudeValue*10.0)/10.0);
//			latitudeTF.setText(latitudeValue + "");
//
//			longitudeValue = Math.abs(Math.round(longitudeValue*10.0)/10.0);
//			longitudeTF.setText(longitudeValue + "");
//
//			String latDir = (String) latitudeDirectionCB.getSelectedItem();
//			String lonDir = (String) longitudeDirectionCB.getSelectedItem();
//
//			//System.out.println("latDir.substring(1) is "+ latDir.substring(1));
//
//			// 2015-04-23 Added checking of latitude of existing settlements
//			if (settlementList != null)
//				for(SettlementRegistry s : settlementList) {
//					if (latitudeValue == 0 && s.getLatitude() == 0)
//						return false;
//					else if (longitudeValue == 0 && s.getLongitude() == 0)
//						return false;
//
//					else if (latitudeValue == Math.abs(s.getLatitude())) {
//						if ((s.getLatitude() > 0 && latDir.substring(1).equals("N")))
//							return false;
//						else if ((s.getLatitude() < 0 && latDir.substring(1).equals("S")))
//							return false;
//					}
//					else if (longitudeValue == Math.abs(s.getLongitude())) {
//						if ((s.getLongitude() > 0 && lonDir.substring(1).equals("E")))
//							return false;
//						else if ((s.getLongitude() < 0 && lonDir.substring(1).equals("W")))
//							return false;
//					}
//
//				}
//
//		}
//		catch (IllegalStateException e) {
//			e.printStackTrace(System.err);
//			result = false;
//		}
//
//		return result;
//	}

	@Override
	public boolean modifyTransportItem() {
		// Validate the arriving settlement data.
		if (validateData()) {
			populateArrivingSettlement(settlement);
			settlement.commitModification();
			return true;
		} else {
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
			int numOfRobots = Integer.parseInt(numOfRobotsTF.getText());
			MarsClock arrivalDate = getArrivalDate();
			Coordinates landingLoc = getLandingLocation();
			ArrivingSettlement newArrivingSettlement = new ArrivingSettlement(name, template, arrivalDate, landingLoc,
					popNum, numOfRobots);
			populateArrivingSettlement(newArrivingSettlement);
			Simulation.instance().getTransportManager().addNewTransportItem(newArrivingSettlement);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Populate the arriving settlement with UI data.
	 * 
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
		launchDate.addTime(-1D * ResupplyUtil.getAverageTransitTime() * 1000D);
		settlement.setLaunchDate(launchDate);

		// Set transit state based on launch and arrival time.
		TransitState state = TransitState.PLANNED;
		if (MarsClock.getTimeDiff(marsClock, launchDate) > 0D) {
			state = TransitState.IN_TRANSIT;
			if (MarsClock.getTimeDiff(marsClock, arrivalDate) > 0D) {
				state = TransitState.ARRIVED;
			}
		}
		settlement.setTransitState(state);

		// Set population number.
		int popNum = Integer.parseInt(populationTF.getText());
		settlement.setPopulationNum(popNum);

		// Set number of robots.
		int numOfRobots = Integer.parseInt(numOfRobotsTF.getText());
		settlement.setNumOfRobots(numOfRobots);

		// Set landing location.
		Coordinates landingLocation = getLandingLocation();
		settlement.setLandingLocation(landingLocation);
	}

	/**
	 * Gets the arrival date from the UI values.
	 * 
	 * @return arrival date as MarsClock instance.
	 */
	private MarsClock getArrivalDate() {

		MarsClock result = (MarsClock) marsClock.clone();

		if (arrivalDateRB.isSelected()) {
			// Determine arrival date from arrival date combo boxes.
			try {
				int sol = solCB.getSelectedIndex() + 1;
				int month = monthCB.getSelectedIndex() + 1;
				int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

				// Set millisols to current time if resupply is current date, otherwise 0.
				double millisols = 0D;
				if ((sol == marsClock.getSolOfMonth()) && (month == marsClock.getMonth())
						&& (orbit == marsClock.getOrbit())) {
					millisols = marsClock.getMillisol();
				}

				result = new MarsClock(orbit, month, sol, millisols, -1);
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
			}
		} else if (timeUntilArrivalRB.isSelected()) {
			// Determine arrival date from time until arrival text field.
			try {
				int solsDiff = Integer.parseInt(solsTF.getText());
				if (solsDiff > 0) {
					result.addTime(solsDiff * 1000D);
				} else {
					result.addTime(marsClock.getMillisol());
				}
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
			}
		}

		return result;
	}

	/**
	 * Gets the landing location from the UI values.
	 * 
	 * @return landing location coordinates.
	 */
	private Coordinates getLandingLocation() {
		String fullLatString = latitudeTF.getText().trim() + latitudeDirectionCB.getSelectedItem();
		// System.out.println("fullLatString : " + fullLatString);
		String fullLonString = longitudeTF.getText().trim() + longitudeDirectionCB.getSelectedItem();
		// System.out.println("fullLonString : " + fullLonString);
		return new Coordinates(fullLatString, fullLonString);
	}

	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		modifyTransportItemDialog = null;
		newTransportItemDialog = null;
	}
}