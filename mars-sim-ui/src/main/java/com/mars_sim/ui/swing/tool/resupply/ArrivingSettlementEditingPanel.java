/*
 * Mars Simulation Project
 * ArrivingSettlementEditingPanel.java
 * @date 2021-09-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.interplanetary.transport.TransportManager;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementTemplate;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.tool.SpringUtilities;

/**
 * A panel for creating or editing an arriving settlement.
 */
@SuppressWarnings("serial")
public class ArrivingSettlementEditingPanel extends TransportItemEditingPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ArrivingSettlementEditingPanel.class.getName());

	private static final int MAX_FUTURE_ORBITS = 10;
	
	// Data members
	private String errorString = "";
	// the degree sign 
	private String deg = Msg.getString("direction.degreeSign"); //$NON-NLS-1$
	
	private boolean validationResult = true;

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
	private JComboBox<String> latitudeDirectionCB;
	private JTextField longitudeTF;
	private JComboBox<String> longitudeDirectionCB;
	private JLabel errorLabel;
	private JTextField populationTF;
	private JTextField numOfRobotsTF;
	private JComboBox<String> sponsorCB;

	private ModifyTransportItemDialog modifyTransportItemDialog;
	private NewTransportItemDialog newTransportItemDialog;
	private ArrivingSettlement settlement;

	private MasterClock master;

	private TransportManager transportManager;
	private UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param settlement                the arriving settlement to modify or null if
	 *                                  creating a new one.
	 * @param modifyTransportItemDialog
	 * @param newTransportItemDialog
	 */
	public ArrivingSettlementEditingPanel(Simulation sim, ArrivingSettlement settlement,
			ModifyTransportItemDialog modifyTransportItemDialog, NewTransportItemDialog newTransportItemDialog) {
		super(settlement);
		this.modifyTransportItemDialog = modifyTransportItemDialog;
		this.newTransportItemDialog = newTransportItemDialog;
		// Initialize data members.
		this.settlement = settlement;
		this.master = sim.getMasterClock();
		this.transportManager = sim.getTransportManager();
		this.unitManager = sim.getUnitManager();

		setBorder(new MarsPanelBorder());
		setLayout(new BorderLayout(0, 0));

		// Create top edit pane.
		JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
		add(topEditPane, BorderLayout.NORTH);

		JPanel topPane = new JPanel(new BorderLayout(10, 10));
		topEditPane.add(topPane, BorderLayout.NORTH);

		// Create top spring layout.
		JPanel topSpring = new JPanel(new SpringLayout());
		topPane.add(topSpring, BorderLayout.NORTH);

		// Create name title label.
		JLabel nameTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.settlementName"), //$NON-NLS-1$
				SwingConstants.TRAILING);
		topSpring.add(nameTitleLabel);

		// Create name text field.
		nameTF = new JTextField("", 25);
		if (settlement != null) {
			nameTF.setText(settlement.getName());
		}
		topSpring.add(nameTF);

		// Create template title label.
		JLabel templateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.layoutTemplate"), //$NON-NLS-1$
				SwingConstants.TRAILING);
		topSpring.add(templateTitleLabel);

		// Create template combo box.
		Vector<String> templateNames = new Vector<>(settlementTemplateConfig.getItemNames());

		templateCB = new JComboBoxMW<>(templateNames);
		if (settlement != null) {
			templateCB.setSelectedItem(settlement.getTemplate());
		}
		templateCB.addActionListener(e -> updateTemplateDependentFields((String) templateCB.getSelectedItem()));
		
		topSpring.add(templateCB);

		// 2017-02-11 Create sponsor label.
		JLabel sponsorTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.sponsoringAgency"), //$NON-NLS-1$
				SwingConstants.TRAILING);
		topSpring.add(sponsorTitleLabel);

		// Create sponsor CB
		Collection<String> codes = SimulationConfig.instance().getReportingAuthorityFactory().getItemNames();
		sponsorCB = new JComboBox<>(codes.toArray(new String[codes.size()]));
		if (settlement != null) {
			sponsorCB.setSelectedItem(settlement.getSponsorCode());
		}
		topSpring.add(sponsorCB);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(topSpring, 3, 2, // rows, cols
				50, 10, // initX, initY
				10, 10); // xPad, yPad

		JPanel numPane = new JPanel(new GridLayout(1, 2));
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
				SettlementTemplate template = settlementTemplateConfig.getItem(templateName);
				if (template != null) {
					populationNum = template.getDefaultPopulation();
					numOfRobots = template.getDefaultNumOfRobots();
				}
			}
		}
		populationTF = new JTextField(4);
		populationTF.setText(Integer.toString(populationNum));
		populationTF.setHorizontalAlignment(SwingConstants.RIGHT);
		populationPane.add(populationTF);

		numOfRobotsTF = new JTextField(4);
		numOfRobotsTF.setText(Integer.toString(numOfRobots));
		numOfRobotsTF.setHorizontalAlignment(SwingConstants.RIGHT);
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
		arrivalDateRB.addActionListener(e -> {
				JRadioButton rb = (JRadioButton) e.getSource();
				setEnableArrivalDatePane(rb.isSelected());
				setEnableTimeUntilArrivalPane(!rb.isSelected());
		});
		arrivalDateSelectionPane.add(arrivalDateRB);

		// Create arrival date title label.
		arrivalDateTitleLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.arrivalDate")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(arrivalDateTitleLabel);

		// Get default arriving settlement Martian time.
		MarsTime arrivingTime = master.getMarsTime();
		if (settlement != null) {
			arrivingTime = settlement.getArrivalDate();
		}

		// Create sol label.
		solLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.sol")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(solLabel);

		// Create sol combo box.
		martianSolCBModel = new MartianSolComboBoxModel(arrivingTime.getMonth(), arrivingTime.getOrbit());
		solCB = new JComboBoxMW<>(martianSolCBModel);
		solCB.setSelectedItem(arrivingTime.getSolOfMonth());
		arrivalDateSelectionPane.add(solCB);

		// Create month label.
		monthLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.month")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(monthLabel);

		// Create month combo box.
		monthCB = new JComboBoxMW<>(MarsTimeFormat.getMonthNames());
		monthCB.setSelectedItem(arrivingTime.getMonthName());
		monthCB.addActionListener(e -> {
			// Update the solCB based on orbit and month
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
					Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		arrivalDateSelectionPane.add(monthCB);

		// Create orbit label.
		orbitLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.orbit")); //$NON-NLS-1$
		arrivalDateSelectionPane.add(orbitLabel);

		// Create orbit combo box.
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumIntegerDigits(2);
		String[] orbitValues = new String[MAX_FUTURE_ORBITS];
		int startOrbit = arrivingTime.getOrbit();
		for (int x = 0; x < MAX_FUTURE_ORBITS; x++) {
			orbitValues[x] = formatter.format(1L * startOrbit + x);
		}
		orbitCB = new JComboBoxMW<>(orbitValues);
		orbitCB.setSelectedItem(formatter.format(startOrbit));
		orbitCB.addActionListener(e -> {
			// Update the solCB based on orbit and month
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
					Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		arrivalDateSelectionPane.add(orbitCB);

		// Create time until arrival pane.
		JPanel timeUntilArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(timeUntilArrivalPane);

		// Create time until arrival radio button.
		timeUntilArrivalRB = new JRadioButton();
		dateTypeRBGroup.add(timeUntilArrivalRB);
		timeUntilArrivalRB.addActionListener(e -> {
				JRadioButton rb = (JRadioButton) e.getSource();
				setEnableTimeUntilArrivalPane(rb.isSelected());
				setEnableArrivalDatePane(!rb.isSelected());
		});
		timeUntilArrivalPane.add(timeUntilArrivalRB);

		// create time until arrival label.
		timeUntilArrivalLabel = new JLabel(Msg.getString("ArrivingSettlementEditingPanel.solUntilArrival")); //$NON-NLS-1$
		timeUntilArrivalLabel.setEnabled(false);
		timeUntilArrivalPane.add(timeUntilArrivalLabel);

		// Create sols text field.
		MarsTime currentTime = master.getMarsTime();
		int solsDiff = (int) Math.round((arrivingTime.getTimeDiff(currentTime) / 1000D));
		solsTF = new JTextField(4);
		solsTF.setText(Integer.toString(solsDiff));
		solsTF.setHorizontalAlignment(SwingConstants.RIGHT);
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
		JPanel landingLocationPane = new JPanel(new SpringLayout());

		landingLocationPane
				.setBorder(new TitledBorder(Msg.getString("ArrivingSettlementEditingPanel.landingLocation"))); //$NON-NLS-1$

		southPane.add(landingLocationPane);
		southPane.add(emptyLabel);

		// Create latitude title label.
		JLabel latitudeTitleLabel = new JLabel(Msg.getString("direction.latitude"), SwingConstants.TRAILING); //$NON-NLS-1$
		landingLocationPane.add(latitudeTitleLabel);

		// Create latitude text field.
		latitudeTF = new JTextField(4);
		latitudeTitleLabel.setLabelFor(latitudeTF);

		latitudeTF.setHorizontalAlignment(SwingConstants.RIGHT);
		landingLocationPane.add(latitudeTF);

		// Create latitude direction combo box.
		latitudeDirectionCB = new JComboBox<>();
		latitudeDirectionCB.addItem(deg + Msg.getString("direction.northShort")); //$NON-NLS-1$
		latitudeDirectionCB.addItem(deg + Msg.getString("direction.southShort")); //$NON-NLS-1$
		if (settlement != null) {
			String latString = settlement.getLandingLocation().getFormattedLatitudeString();
			// Remove last two characters from formatted latitude string.
			String cleanLatString = latString.substring(0, latString.length() - 3);
			latitudeTF.setText(cleanLatString);

			// Get last character in formatted string. ex: "S".
			String dirString = latString.substring(latString.length() - 1, latString.length());
			latitudeDirectionCB.setSelectedItem(dirString);
		}
		landingLocationPane.add(latitudeDirectionCB);

		// Create longitude title label.
		JLabel longitudeTitleLabel = new JLabel(Msg.getString("direction.longitude"), SwingConstants.TRAILING); //$NON-NLS-1$

		landingLocationPane.add(longitudeTitleLabel);

		// Create longitude text field.
		longitudeTF = new JTextField(4);
		longitudeTitleLabel.setLabelFor(longitudeTF);


		longitudeTF.setHorizontalAlignment(SwingConstants.RIGHT);
		landingLocationPane.add(longitudeTF);

		// Create longitude direction combo box.
		longitudeDirectionCB = new JComboBox<>();
		longitudeDirectionCB.addItem(deg + Msg.getString("direction.westShort")); //$NON-NLS-1$
		longitudeDirectionCB.addItem(deg + Msg.getString("direction.eastShort")); //$NON-NLS-1$
		if (settlement != null) {
			String lonString = settlement.getLandingLocation().getFormattedLongitudeString();
			// Remove last three characters from formatted longitude string.
			String cleanLonString = lonString.substring(0, lonString.length() - 3);
			longitudeTF.setText(cleanLonString);
			// Get last character in formatted string. ex: "W".
			String dirString = lonString.substring(lonString.length() - 1, lonString.length());
			longitudeDirectionCB.setSelectedItem(dirString);
		}

		landingLocationPane.add(longitudeDirectionCB);

		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(landingLocationPane, 2, 3, // rows, cols
				20, 10, // initX, initY
				10, 10); // xPad, yPad

		// Create error pane.
		JPanel errorPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		add(errorPane, BorderLayout.SOUTH);

		// Create error label
		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);
		errorPane.add(errorLabel);

	}

	/**
	 * Sets the components of the arrival date pane to be enabled or disabled.
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
	 * Sets the components of the time until arrival pane to be enabled or disabled.
	 * 
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableTimeUntilArrivalPane(boolean enable) {
		timeUntilArrivalLabel.setEnabled(enable);
		solsTF.setEnabled(enable);
		solInfoLabel.setEnabled(enable);
	}

	/**
	 * Updates the fields dependent upon the Template
	 * 
	 * @param templateName the template name.
	 */
	private void updateTemplateDependentFields(String templateName) {
		if (templateName != null) {
			SettlementTemplate template = settlementTemplateConfig.getItem(templateName);
			if (template != null) {
				numOfRobotsTF.setText(Integer.toString(template.getDefaultNumOfRobots()));
				populationTF.setText(Integer.toString(template.getDefaultPopulation()));
				sponsorCB.setSelectedItem(template.getSponsor());
			}
		}
	}

	/**
	 * Validate the arriving settlement data.
	 *
	 * @return true if data is valid.
	 */
	private boolean validateData() {
		validationResult = true;
		errorString = null;

		// Validate settlement name.
		if (nameTF.getText().trim().isEmpty()) {
			validationResult = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noName"); //$NON-NLS-1$
		}

		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}
		
		// Validate template.
		String templateName = (String) templateCB.getSelectedItem();
		if ((templateName == null) || templateName.trim().isEmpty()) {
			validationResult = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noTemplate"); //$NON-NLS-1$
		}

		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}
		
		// Validate population number.
		String populationNumString = populationTF.getText();
		if (populationNumString.trim().isEmpty()) {
			validationResult = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noPopulation"); //$NON-NLS-1$
		} else {
			try {
				int popNum = Integer.parseInt(populationNumString);
				if (popNum < 0) {
					validationResult = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativePopulation"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validationResult = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidPopulation"); //$NON-NLS-1$
			}
		}
		
		if (!validationResult) {
			errorLabel.setText(errorString);	
			return false;
		}

		// Validate numOfRobots.
		String numOfRobotsString = numOfRobotsTF.getText();
		if (numOfRobotsString.trim().isEmpty()) {
			validationResult = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.nonumOfRobots"); //$NON-NLS-1$
		} else {
			try {
				int numOfRobots = Integer.parseInt(numOfRobotsString);
				if (numOfRobots < 0) {
					validationResult = false;
					errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativenumOfRobots"); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				validationResult = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidnumOfRobots"); //$NON-NLS-1$
			}
		}

		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}

		// Implement addChangeListener() to validate solsTF.
		if (solsTF.isEnabled()) {
			String timeArrivalString = solsTF.getText().trim();
			if (timeArrivalString.isEmpty()) {
				validationResult = false;
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noSols"); //$NON-NLS-1$
				enableButton(false);
			} 
			
			else {
				addChangeListener(solsTF, e -> validateSolsTF(timeArrivalString));
			}
		}

		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}
		
		// Validate latitude value.
		String latitudeString = latitudeTF.getText().trim() + " " 
				+ ((String)latitudeDirectionCB.getSelectedItem()).substring(1, 2);
				
		String error0 = CoordinatesFormat.checkLat(latitudeString);
		if (error0 != null) {
			validationResult = false;
			errorString = error0;
		}
		
		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}


		// Validate longitude value.
		String longitudeString = longitudeTF.getText().trim() + " " 
				+ ((String)longitudeDirectionCB.getSelectedItem()).substring(1, 2);
		
		String error1 = CoordinatesFormat.checkLon(longitudeString);
		if (error1 != null) {
			validationResult = false;
			errorString = error1;
		}
				
		if (!validationResult) {
			errorLabel.setText(errorString);
			return false;
		}
		
		String repeated = checkRepeatingLatLon(latitudeString, longitudeString);
		if (repeated != null) {
			validationResult = false;
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.collision"); //$NON-NLS-1$
		}
		

		if (!validationResult) {
			errorLabel.setText(errorString);	
			return false;
		}
				
		return validationResult;
	}

	/***
	 * Checks for any repeating latitude and longitude
	 */
	private String checkRepeatingLatLon(String latStr, String longStr) {
		// Ensure the latitude/longitude is not being taken already in the table by
		// another settlement
		boolean repeated = false;
	
		Collection<Settlement> list = unitManager.getSettlements();
				
		Set<Coordinates> coordinatesSet = new HashSet<>();
		coordinatesSet.add(new Coordinates(latStr, longStr));
		
		for (Settlement s: list) {
			if (!coordinatesSet.add(s.getCoordinates())) {
				repeated = true;
				break;
			}
		}

		if (repeated) {
			return Msg.getString("Coodinates.error.latitudeLongitudeRepeating"); //$NON-NLS-1$
		}
		
		return null;
	}
	
	/**
	 * Validates the textfield of the 'Sols Until Arrival' 
	 * 
	 * @param timeArrivalString
	 */
	public void validateSolsTF(String timeArrivalString) {
		errorString = null;

		try {

			if (timeArrivalString.equals("-"))
				timeArrivalString = "-1";
			double timeArrival = Double.parseDouble(timeArrivalString);
			if (timeArrival < 0D) {
				validationResult = false;
				enableButton(false);
				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativeSols"); //$NON-NLS-1$
				errorLabel.setText(errorString);
			} else {
					validationResult = true;
					errorString = "";
					errorLabel.setText(errorString);
					enableButton(true);
			}
		} catch (NumberFormatException e) {
			validationResult = false;
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
			private int lastChange = 0;
			private int lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
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

	@Override
	public boolean modifyTransportItem() {
		// Validate the arriving settlement data.
		if (validateData()) {
			populateArrivingSettlement(settlement);
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
			int arrivalSols = 1;
			Coordinates landingLoc = getLandingLocation();
			String sponsor = (String) sponsorCB.getSelectedItem();
			ArrivingSettlement newArrivingSettlement =
					new ArrivingSettlement(name, template, sponsor,
							arrivalSols, landingLoc,
							popNum, numOfRobots);
			populateArrivingSettlement(newArrivingSettlement);
			transportManager.addNewTransportItem(newArrivingSettlement);
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
		settlement.setSponsorCode((String) sponsorCB.getSelectedItem());

		// Populate arrival date.
		MarsTime arrivalDate = getArrivalDate();
		settlement.setArrivalDate(arrivalDate);

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
	 * @return arrival date as MarsTime instance.
	 */
	private MarsTime getArrivalDate() {

		MarsTime result = master.getMarsTime();

		if (arrivalDateRB.isSelected()) {
			// Determine arrival date from arrival date combo boxes.
			try {
				int sol = solCB.getSelectedIndex() + 1;
				int month = monthCB.getSelectedIndex() + 1;
				int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

				// Set millisols to current time if resupply is current date, otherwise 0.
				double millisols = 0D;
				if ((sol == result.getSolOfMonth()) && (month == result.getMonth())
						&& (orbit == result.getOrbit())) {
					millisols = result.getMillisol();
				}

				result = new MarsTime(orbit, month, sol, millisols, -1);
			} catch (NumberFormatException e) {
				logger.severe("Selecting arrivalDateRB but MarsClock is invalid: " + e.getMessage());
			}
		} else if (timeUntilArrivalRB.isSelected()) {
			// Determine arrival date from time until arrival text field.
			try {
				int solsDiff = Integer.parseInt(solsTF.getText());
				if (solsDiff > 0) {
					result = result.addTime(solsDiff * 1000D);
				} else {
					result = result.addTime(master.getMarsTime().getMillisol());
				}
			} catch (NumberFormatException e) {
				logger.severe("Selecting timeUntilArrivalRB but MarsClock is invalid: " + e.getMessage());
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
		String fullLatString = latitudeTF.getText().trim() + " " 
				+ ((String)latitudeDirectionCB.getSelectedItem()).substring(1, 2);
		String fullLonString = longitudeTF.getText().trim() + " " 
				+ ((String)longitudeDirectionCB.getSelectedItem()).substring(1, 2);
		return new Coordinates(fullLatString, fullLonString);
	}
}
