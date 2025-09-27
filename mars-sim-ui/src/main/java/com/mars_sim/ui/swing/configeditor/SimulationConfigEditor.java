/*
 * Mars Simulation Project
 * SimulationConfigEditor.java
 * @date 2021-08-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.ScenarioConfig;
import com.mars_sim.core.configuration.UserConfigurable;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Crew;
import com.mars_sim.core.person.CrewConfig;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.structure.InitialSettlement;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIConfig;


/**
 * A temporary simulation configuration editor dialog. Will be replaced by
 * SimulationConfigEditor later when it is finished.
 */
public class SimulationConfigEditor {

	/**
	 * Adapter for the UserConfigurableConfig to appear as a ComboModel
	 */
	private final class UserConfigurableComboModel implements ComboBoxModel<String> {

		private static final String BLANK = "";
		private UserConfigurableConfig<? extends UserConfigurable> config;
		private String selectedItem = null;
		private boolean allowNoSelect;

		public UserConfigurableComboModel(UserConfigurableConfig<? extends UserConfigurable> config,
											boolean allowNoSelect) {
			this.config = config;
			this.allowNoSelect = allowNoSelect;
		}

		private List<String> getPossibles() {
			List<String> results = config.getItemNames();

			if (allowNoSelect) {
				List<String> bigResults = new ArrayList<>();
				bigResults.add(BLANK);
				bigResults.addAll(results);
				results = bigResults;
			}
			return results;
		}

		@Override
		public int getSize() {
			return getPossibles().size();
		}

		@Override
		public String getElementAt(int index) {
			return getPossibles().get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
		}

		@Override
		public void setSelectedItem(Object anItem) {
			if (anItem == null || BLANK.equals(anItem)) {
				this.selectedItem = null;
			}
			else {
				this.selectedItem = (String) anItem;
			}
		}

		@Override
		public Object getSelectedItem() {
			return this.selectedItem;
		}
	}


	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(SimulationConfigEditor.class.getName());

	private static final int HORIZONTAL_SIZE = 1024;

	// Data members.
	private boolean hasError;
	private boolean isCrewEditorOpen = true;
	private boolean completed = false;
	private boolean useCrew = true;
	
	private InitialSettlementModel settlementTableModel;
	private JTable settlementTable;

	private ArrivingSettlementModel arrivalTableModel;
	private JTable arrivalTable;

	private JLabel errorLabel;
	private JButton startButton;
	private JFrame f;

	private CrewEditor crewEditor;

	private GameMode mode;
	private SettlementTemplateConfig settlementTemplateConfig;
	private PersonConfig personConfig;
	private AuthorityFactory raFactory;

	private UserConfigurableConfig<Crew> crewConfig;

	private Scenario selectedScenario;
	private ScenarioConfig scenarioConfig;

	private AuthorityEditor authorityEditor;

	private UserConfigurableControl<Scenario> configControl;

	private JTabbedPane tabPanel;

	/**
	 * Constructor.
	 * 
	 * @param config the simulation configuration.
	 */
	public SimulationConfigEditor(SimulationConfig config, ScenarioConfig scenarioConfig, CrewConfig crew) {

		// Initialize data members.
		raFactory = config.getReportingAuthorityFactory();
		settlementTemplateConfig = config.getSettlementTemplateConfiguration();
		personConfig = config.getPersonConfig();
		crewConfig = crew;

		// Preload the config to set up the preferred LAF
		UIConfig configs = new UIConfig();
		configs.parseFile();
		StyleManager.setStyles(configs.getPropSets());

		hasError = false;

		f = new JFrame();
		f.setIconImage(MainWindow.getIconImage());
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});

		f.setSize(HORIZONTAL_SIZE, 350);
		f.setTitle(Msg.getString("SimulationConfigEditor.title")); //$NON-NLS-1$

		// Sets the dialog content panel.
		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		f.setContentPane(contentPanel);

		JPanel topPanel = null;

		if (GameManager.getGameMode() == GameMode.COMMAND) {
			topPanel = new JPanel(new GridLayout(2, 1));
			f.add(topPanel, BorderLayout.NORTH);
		}

		else {
			topPanel = new JPanel(new GridLayout(1, 1));
			f.add(topPanel, BorderLayout.NORTH);
		}

		mode = GameManager.getGameMode();
		
		// Create the title label.
		if (mode == GameMode.COMMAND) {

			String commanderName = personConfig.getCommander().getFullName();
			String sponsor = personConfig.getCommander().getSponsorStr();
			JLabel gameModeLabel = new JLabel(Msg.getString("SimulationConfigEditor.gameMode", "Command Mode"), SwingConstants.CENTER); //$NON-NLS-1$
			StyleManager.applyHeading(gameModeLabel);
			topPanel.add(gameModeLabel);

			JPanel ccPanel = new JPanel(new GridLayout(1, 3));
			topPanel.add(ccPanel);

			JLabel commanderLabel = new JLabel("   " + Msg.getString("SimulationConfigEditor.commanderName",
					commanderName), SwingConstants.LEFT); //$NON-NLS-1$
			ccPanel.add(commanderLabel);

			ccPanel.add(new JLabel());

			JLabel sponsorLabel = new JLabel(Msg.getString("SimulationConfigEditor.sponsorInfo",
					sponsor)  + "                 ", SwingConstants.RIGHT); //$NON-NLS-1$
			ccPanel.add(sponsorLabel);

		}

		else {
			JLabel gameModeLabel = new JLabel(Msg.getString("SimulationConfigEditor.gameMode", "Sandbox Mode"), SwingConstants.CENTER); //$NON-NLS-1$
			StyleManager.applyHeading(gameModeLabel);
			topPanel.add(gameModeLabel);
		}

		tabPanel = new JTabbedPane();
		f.add(tabPanel, BorderLayout.CENTER);

		// Create settlement scroll panel.
		JScrollPane settlementScrollPane = new JScrollPane();
		settlementScrollPane.setPreferredSize(new Dimension(HORIZONTAL_SIZE, 250));
		tabPanel.add("Initial Settlement", settlementScrollPane);
		createSettlementTable(settlementScrollPane);

		// Second tab
		JScrollPane arrivalScrolPane = new JScrollPane();
		arrivalScrolPane.setPreferredSize(new Dimension(HORIZONTAL_SIZE, 250));
		tabPanel.add("Arriving Settlements", arrivalScrolPane);
		createArrivalTable(arrivalScrolPane);

		// Create configuration button outer panel.
		JPanel configurationButtonOuterPanel = new JPanel(new BorderLayout(0, 0));
		f.add(configurationButtonOuterPanel, BorderLayout.EAST);

		// Create configuration button inner top panel.
		JPanel configurationButtonInnerTopPanel = new JPanel(new GridLayout(4, 1));
		configurationButtonOuterPanel.add(configurationButtonInnerTopPanel, BorderLayout.NORTH);

		// Create add settlement button.
		JButton addButton = new JButton(ImageLoader.getIconByName("action/add")); //$NON-NLS-1$
		addButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.add")); //$NON-NLS-1$
		addButton.addActionListener(e -> addNewRows());
		configurationButtonInnerTopPanel.add(addButton);

		// Create remove settlement button.
		JButton removeButton = new JButton(ImageLoader.getIconByName("action/remove")); //$NON-NLS-1$
		removeButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.remove")); //$NON-NLS-1$
		removeButton.addActionListener(e -> removeSelectedSettlements());
		configurationButtonInnerTopPanel.add(removeButton);

		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		f.add(bottomPanel, BorderLayout.SOUTH);

		// Create error label.
		errorLabel = new JLabel("", SwingConstants.CENTER); //$NON-NLS-1$
		errorLabel.setForeground(Color.RED);
		bottomPanel.add(errorLabel, BorderLayout.NORTH);

		// Monitor table models for errors
		settlementTableModel.addTableModelListener(e -> checkModelErrors());
		arrivalTableModel.addTableModelListener(e -> checkModelErrors());

		// Create the config control
		configControl = new UserConfigurableControl<Scenario>(f, "Scenario", scenarioConfig) {

			@Override
			protected void displayItem(Scenario newDisplay) {
				clearError();
				settlementTableModel.loadDefaultSettlements(newDisplay);
				arrivalTableModel.loadDefaultSettlements(newDisplay);
			}

			@Override
			protected Scenario createItem(String newName, String newDescription) {
				return finalizeSettlementConfig(newName, newDescription);
			}
		};

		// Add an Export button
		JButton exportButton = new JButton(ImageLoader.getIconByName("action/export")); //$NON-NLS-1$
		exportButton.setToolTipText("Export");
		exportButton.addActionListener(e -> exportScenario());
		configControl.getPane().add(exportButton);
		JButton importButton = new JButton(ImageLoader.getIconByName("action/import")); //$NON-NLS-1$
		importButton.setToolTipText("Import");
		importButton.addActionListener(e -> importScenario());
		configControl.getPane().add(importButton);
		bottomPanel.add(configControl.getPane(), BorderLayout.WEST);

		// Create the bottom button panel.
		JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomButtonPanel.setBorder(BorderFactory.createTitledBorder("Simulation"));
		bottomPanel.add(bottomButtonPanel, BorderLayout.EAST);

		if (mode == GameMode.COMMAND) {
			// Create the sponsor note label
			JLabel noteLabel = new JLabel("    " + Msg.getString("SimulationConfigEditor.sponsorNote"), SwingConstants.LEFT); //$NON-NLS-1$
			bottomPanel.add(noteLabel, BorderLayout.SOUTH);
		}

		// Create the start button.
		startButton = new JButton(Msg.getString("SimulationConfigEditor.button.newSim")); //$NON-NLS-1$
		startButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.newSim")); //$NON-NLS-1$
		startButton.addActionListener(e -> {
				// Make sure any editing cell is completed, then check if error.
				TableCellEditor editor = settlementTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
				if (!hasError) {

					f.setVisible(false);

					// Recalculate the Scenario in case user has made unsaved changes
					selectedScenario = finalizeSettlementConfig(configControl.getSelectItemName(),
																configControl.getDescription());

					// Close simulation config editor
					closeWindow();
				}
		});

		bottomButtonPanel.add(startButton);

		// Edit Authority button.
		JButton authorityButton = new JButton(ImageLoader.getIconByName("sponsor")); //$NON-NLS-1$
		authorityButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.authorityEditor")); //$NON-NLS-1$
		authorityButton.addActionListener(e -> editAuthorities());

		// Edit Crew button.
		JButton crewButton = new JButton(ImageLoader.getIconByName("people")); //$NON-NLS-1$
		crewButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")); //$NON-NLS-1$
		crewButton.addActionListener(e -> editCrewProfile());

		// Set a check box for enabling/disable the alpha crew button
		JCheckBox cb = new JCheckBox(Msg.getString("SimulationConfigEditor.button.useCrews")); //$NON-NLS-1$
		cb.setSelected(useCrew);
		cb.addItemListener(e -> {
            	 useCrew = (e.getStateChange() == ItemEvent.SELECTED);
        		 crewButton.setEnabled(useCrew);
        });

		bottomButtonPanel.add(cb);

		configurationButtonInnerTopPanel.add(authorityButton);
		configurationButtonInnerTopPanel.add(crewButton);

		// Force a load of the default Scenario
		configControl.setSelectedItem(ScenarioConfig.PREDEFINED_SCENARIOS[0]);

		// Set the location of the dialog at the center of the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((screenSize.width - f.getWidth()) / 2, (screenSize.height - f.getHeight()) / 2);
		f.setVisible(true);

		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				if (isCrewEditorOpen) {
					crewEditor.getJFrame().dispose();
				}
				f.dispose();
			}
		});

		JRootPane rootPane = SwingUtilities.getRootPane(startButton);
		rootPane.setDefaultButton(startButton);
	}

	/**
	 * Exports the selected scenario.
	 */
	private void exportScenario() {
		Scenario sc = configControl.getSeletedItem();

		// Prompt user for saved file
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify location to save export");
		fileChooser.setSelectedFile(new File(sc.getName().replace(' ', '_') + "-export.zip"));
		int userSelection = fileChooser.showSaveDialog(f);

		// Did the user select
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    fileToSave = fileChooser.getSelectedFile();
		}
		else {
			return;
		}

		// Request the export to the specified location
		String errorMessage = null;
		List<String> exported = null;
		try {
			exported = scenarioConfig.createExport(sc, raFactory, crewConfig,
												   new FileOutputStream(fileToSave));
		} catch (IOException e) {
			errorMessage = "Problem : " + e.getMessage();
		}

		showScenarioConfirmation("Export", errorMessage, exported);

	}

	private void showScenarioConfirmation(String direction, String errorMessage, List<String> exported) {
		String outcome = errorMessage;
		if (outcome == null) {
			StringBuilder builder = new  StringBuilder();
			builder.append(direction).append(" ");
			builder.append(exported.stream().collect(Collectors.joining(",\n")));
			outcome = builder.toString();
		}

		// Show dialog
		JOptionPane.showMessageDialog(f,
			    outcome,
			    direction + " outcome",
			    (errorMessage != null ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE));
	}

	private void importScenario() {
		// Prompt user for saved file
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select the scenario export to load");
		int userSelection = fileChooser.showOpenDialog(f);

		// Did the user select
		File fileToOpen = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    fileToOpen = fileChooser.getSelectedFile();
		}
		else {
			return;
		}

		String errorMessage = null;
		List<String> imported = null;
		// Request the export to the specified location
		try {
			imported  = scenarioConfig.importScenario(new FileInputStream(fileToOpen),
										  raFactory, crewConfig);
		} catch (IOException e) {
			errorMessage = "Problem : " + e.getMessage();
		}

		showScenarioConfirmation("Import", errorMessage, imported);
		configControl.reload();
	}

	private void createSettlementTable(JScrollPane settlementScrollPane) {
		// Create settlement table model
		settlementTableModel = new InitialSettlementModel(settlementTemplateConfig, raFactory);

		settlementTable = new JTable(settlementTableModel);
		settlementTable.setRowSelectionAllowed(true);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.SETTLEMENT_COL).setPreferredWidth(80);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.SPONSOR_COL).setPreferredWidth(80);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.PHASE_COL).setPreferredWidth(40);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.SETTLER_COL).setPreferredWidth(30);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.CREW_COL).setPreferredWidth(30);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.BOT_COL).setPreferredWidth(30);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.LAT_COL).setPreferredWidth(35);
		settlementTable.getColumnModel().getColumn(InitialSettlementModel.LON_COL).setPreferredWidth(35);
		settlementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		settlementScrollPane.setViewportView(settlementTable);

		// Create combo box for editing sponsor column in settlement table.
		setSponsorEditor(settlementTable, InitialSettlementModel.SPONSOR_COL);

		// Create combo box for editing crew column in settlement table.
		// Use a custom model to inherit new Crews
		TableColumn crewColumn = settlementTable.getColumnModel().getColumn(InitialSettlementModel.CREW_COL);
		JComboBox<String> crewCB = new JComboBox<>();
		crewCB.setModel(new UserConfigurableComboModel(crewConfig, true));
		crewColumn.setCellEditor(new DefaultCellEditor(crewCB));

		// Create combo box for editing template column in settlement table.
		setTemplateEditor(settlementTable, InitialSettlementModel.PHASE_COL);

		// Align content to center of cell
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
		defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		TableColumn column = null;
		for (int ii = 0; ii < settlementTableModel.getColumnCount(); ii++) {
			column = settlementTable.getColumnModel().getColumn(ii);
			column.setCellRenderer(defaultTableCellRenderer);
		}
	}

	private void setTemplateEditor(JTable table, int column) {
		TableColumn templateColumn = table.getColumnModel().getColumn(column);
		JComboBox<String> templateCB = new JComboBox<>();
		for (String st : settlementTemplateConfig.getItemNames()) {
			templateCB.addItem(st);
		}
		templateColumn.setCellEditor(new DefaultCellEditor(templateCB));
	}

	private void createArrivalTable(JScrollPane parentScrollPane) {
		// Create table
		arrivalTableModel = new ArrivingSettlementModel(settlementTemplateConfig, raFactory);

		arrivalTable = new JTable(arrivalTableModel);
		arrivalTable.setRowSelectionAllowed(true);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.SPONSOR_COL).setPreferredWidth(80);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.TEMPLATE_COL).setPreferredWidth(40);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.SETTLER_COL).setPreferredWidth(30);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.ARRIVAL_COL).setPreferredWidth(20);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.BOT_COL).setPreferredWidth(30);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.LAT_COL).setPreferredWidth(35);
		arrivalTable.getColumnModel().getColumn(ArrivingSettlementModel.LON_COL).setPreferredWidth(35);
		arrivalTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		parentScrollPane.setViewportView(arrivalTable);

		// Create combo box for editing sponsor column in settlement table.
		setSponsorEditor(arrivalTable, ArrivingSettlementModel.SPONSOR_COL);

		// Create combo box for editing template column in settlement table.
		setTemplateEditor(arrivalTable, ArrivingSettlementModel.TEMPLATE_COL);

		// Align content to center of cell
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
		defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		TableColumn column = null;
		for (int ii = 0; ii < arrivalTableModel.getColumnCount(); ii++) {
			column = arrivalTable.getColumnModel().getColumn(ii);
			column.setCellRenderer(defaultTableCellRenderer);
		}
	}

	private void setSponsorEditor(JTable table, int column) {
		TableColumn sponsorColumn = table.getColumnModel().getColumn(column);
		JComboBox<String> sponsorCB = new JComboBox<>();
		sponsorCB.setModel(new UserConfigurableComboModel(raFactory, false));
		sponsorColumn.setCellEditor(new DefaultCellEditor(sponsorCB));
	}

	/**
	 * Checks the table models for errors.
	 */
	private void checkModelErrors() {
		String errorMessage  = settlementTableModel.getErrorMessage();
		if (errorMessage == null) {
			errorMessage = arrivalTableModel.getErrorMessage();
		}

		if (!hasError && (errorMessage != null)) {
			hasError = true;
			errorLabel.setText(errorMessage);
			startButton.setEnabled(false);
			configControl.allowSaving(false);
		}
		else {
			clearError();
		}
	}

	private boolean isSettlementSelected() {
		return tabPanel.getSelectedIndex() == 0;
	}

	/**
	 * Adds a new settlement with default values.
	 */
	private void addNewRows() {
		// Get any known Reporting Authority
		String sponsor = raFactory.getItemNames().iterator().next();
		String template = determineNewSettlementTemplate();
		Coordinates location = Coordinates.getRandomLocation();

		if (isSettlementSelected()) {
			settlementTableModel.addPartialSettlement(sponsor, template, location);
		}
		else {
			arrivalTableModel.addPartialArrival(sponsor, template, location);
		}
	}

	/**
	 * Removes the settlements selected on the table.
	 */
	private void removeSelectedSettlements() {
		if (isSettlementSelected()) {
			int[] rows = settlementTable.getSelectedRows();
			settlementTableModel.removeSettlements(rows);
		}
		else {
			int[] rows = arrivalTable.getSelectedRows();
			arrivalTableModel.removeArrival(rows);
		}
	}

	/**
	 * Edits Authorities.
	 */
	private void editAuthorities() {
		if (authorityEditor == null) {
			authorityEditor = new AuthorityEditor(this, raFactory);
		}
		else {
			authorityEditor.getJFrame().setVisible(true);
		}
	}

	/**
	 * Edits team profile.
	 */
	private void editCrewProfile() {
		if (crewEditor == null || !isCrewEditorOpen) {
			crewEditor = new CrewEditor(this, crewConfig, raFactory);
		}
		else {
			crewEditor.getJFrame().setVisible(true);
		}
	}

	void setCrewEditorOpen(boolean value) {
		isCrewEditorOpen = value;
		crewEditor = null;
	}

	/**
	 * Finalizes the simulation configuration based on dialog choices.
	 */
	private Scenario finalizeSettlementConfig(String name, String description) {
		List<InitialSettlement> is = settlementTableModel.getSettlements();
		List<ArrivingSettlement> arrivals = arrivalTableModel.getArrivals();
		return new Scenario(name, description, is, arrivals, false);
	}

	/**
	 * Closes and disposes dialog window.
	 */
	private void closeWindow() {
		f.dispose();
		wakeUpWaiters();
	}

	/**
	 * Method must be synchronized to register locks.
	 */
	private synchronized void wakeUpWaiters() {
		// Wake up the waiters
		logger.config("Site Editor closed. Waking up.");
		completed = true;
		notifyAll();
	}

	/**
	 * Clears all edit-check errors.
	 */
	private void clearError() {
		if (hasError) {
			hasError = false;
			errorLabel.setText(""); //$NON-NLS-1$
			startButton.setEnabled(true);
			configControl.allowSaving(true);
		}
	}

	/**
	 * Determines a new settlement's template.
	 *
	 * @return template name.
	 */
	private String determineNewSettlementTemplate() {
		String result = null;

		List<String> templates = settlementTemplateConfig.getItemNames();
		if (!templates.isEmpty()) {
			int index = RandomUtil.getRandomInt(templates.size() - 1);
			result = templates.get(index);
		} else
			logger.log(Level.WARNING, Msg.getString("SimulationConfigEditor.log.settlementTemplateNotFound")); //$NON-NLS-1$

		return result;
	}

	public JFrame getFrame() {
		return f;
	}

	/**
	 * Gets the scenario created in the editor.
	 */
	public synchronized Scenario getScenario() {
		while (!completed ) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
            }
        }
        return selectedScenario;
	}
}
