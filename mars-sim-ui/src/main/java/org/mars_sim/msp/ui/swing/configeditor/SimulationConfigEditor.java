/**
 * Mars Simulation Project
 * SimulationConfigEditor.java
 * @version 3.1.0 2016-10-27
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.UIManagers;

/**
 * A temporary simulation configuration editor dialog. Will be replaced by
 * SimulationConfigEditor later when it is finished.
 */
public class SimulationConfigEditor {

	/** default logger. */
	private static Logger logger = Logger.getLogger(SimulationConfigEditor.class.getName());

	private static final int HORIZONTAL_SIZE = 1024;

	// Data members.
	private boolean hasError, isCrewEditorOpen = false;

	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JLabel errorLabel;
	private JButton createButton;
	private JFrame f;

	private CrewEditor crewEditor;
	private SimulationConfig config;

	/**
	 * Constructor
	 * 
	 * @param owner
	 *            the owner window.
	 * @param config
	 *            the simulation configuration.
	 */
	@SuppressWarnings("static-access")
	public SimulationConfigEditor(SimulationConfig config, MainWindow mainWindow) {

		// Initialize data members.
		this.config = config;

		hasError = false;

		try {
			// use the weblaf skin
			WebLookAndFeel.install();
			UIManagers.initialize();
			//			NimRODTheme nt = new NimRODTheme(getClass().getClassLoader().getResource("/theme/nimrod.theme"));
//			NimRODLookAndFeel nf = new NimRODLookAndFeel();
//			nf.setCurrentTheme(nt);
//			UIManager.setLookAndFeel(nf);
		} catch (Exception ex) {
			logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), ex); //$NON-NLS-1$
		}

		f = new JFrame();

		f.setSize(HORIZONTAL_SIZE, 500);

		// Sets the dialog content panel.
		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		f.setContentPane(contentPanel);

		// Create the title label.
		JLabel titleLabel = new JLabel(Msg.getString("SimulationConfigEditor.chooseSettlements"), JLabel.CENTER); //$NON-NLS-1$
		f.add(titleLabel, BorderLayout.NORTH);

		// Create settlement scroll panel.
		JScrollPane settlementScrollPane = new JScrollPane();
		settlementScrollPane.setPreferredSize(new Dimension(HORIZONTAL_SIZE, 250));// 585, 200));
		f.add(settlementScrollPane, BorderLayout.CENTER);

		// Create settlement table.
		settlementTableModel = new SettlementTableModel();
		settlementTable = new JTable(settlementTableModel);
		settlementTable.setRowSelectionAllowed(true);
		settlementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		settlementTable.getColumnModel().getColumn(0).setPreferredWidth(125);
		settlementTable.getColumnModel().getColumn(1).setPreferredWidth(185);
		settlementTable.getColumnModel().getColumn(2).setPreferredWidth(65);
		settlementTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		settlementTable.getColumnModel().getColumn(4).setPreferredWidth(65);
		settlementTable.getColumnModel().getColumn(5).setPreferredWidth(65);
		settlementTable.getColumnModel().getColumn(6).setPreferredWidth(50);

		settlementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		settlementTable.setBackground(java.awt.Color.WHITE);

		// 2015-10-06 Added setTableStyle()
		TableStyle.setTableStyle(settlementTable);

		adjustColumn(settlementTable);

		settlementScrollPane.setViewportView(settlementTable);

		// Create combo box for editing template column in settlement table.
		TableColumn templateColumn = settlementTable.getColumnModel().getColumn(1);
		JComboBoxMW<String> templateCB = new JComboBoxMW<String>();
		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
		while (i.hasNext()) {
			templateCB.addItem(i.next().getTemplateName());
		}
		templateColumn.setCellEditor(new DefaultCellEditor(templateCB));

		// 2015-10-03 Align content to center of cell
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
		defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		TableColumn column = null;
		for (int ii = 0; ii < 7; ii++) {
			column = settlementTable.getColumnModel().getColumn(ii);
			// 2015-10-03 Align content to center of cell
			column.setCellRenderer(defaultTableCellRenderer);
		}

		// Create configuration button outer panel.
		JPanel configurationButtonOuterPanel = new JPanel(new BorderLayout(0, 0));
		f.add(configurationButtonOuterPanel, BorderLayout.EAST);

		// Create configuration button inner top panel.
		JPanel configurationButtonInnerTopPanel = new JPanel(new GridLayout(3, 1));
		configurationButtonOuterPanel.add(configurationButtonInnerTopPanel, BorderLayout.NORTH);

		// Create add settlement button.
		JButton addButton = new JButton(Msg.getString("SimulationConfigEditor.button.add")); //$NON-NLS-1$
		addButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.add")); //$NON-NLS-1$
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addNewSettlement();
			}
		});
		configurationButtonInnerTopPanel.add(addButton);

		// Create remove settlement button.
		JButton removeButton = new JButton(Msg.getString("SimulationConfigEditor.button.remove")); //$NON-NLS-1$
		removeButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.remove")); //$NON-NLS-1$
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
		JButton defaultButton = new JButton(" " + Msg.getString("SimulationConfigEditor.button.undo") + " "); //$NON-NLS-1$
		defaultButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.undo")); //$NON-NLS-1$
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaultSettlements();
			}
		});
		configurationButtonInnerBottomPanel.add(defaultButton);

		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		f.add(bottomPanel, BorderLayout.SOUTH);

		// Create error label.
		errorLabel = new JLabel("", JLabel.CENTER); //$NON-NLS-1$
		errorLabel.setForeground(Color.RED);
		bottomPanel.add(errorLabel, BorderLayout.NORTH);

		// Create the bottom button panel.
		JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

		// Create the create button.
		createButton = new JButton("  " + Msg.getString("SimulationConfigEditor.button.newSim") + "  "); //$NON-NLS-1$
		createButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.newSim")); //$NON-NLS-1$
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Make sure any editing cell is completed, then check if error.
				TableCellEditor editor = settlementTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
				if (!hasError) {
					if (mainWindow == null) {
					} else {
						mainWindow.getFrame().dispose();
					}
					f.setVisible(false);
					setConfiguration();
					// Correct order:
					// 1. Simulation.createNewSimulation();
					// 2. MainWindow mw = new MainWindow(true);
					// 3. Simulation.instance().start();
					Simulation.instance().destroyOldSimulation();
					Simulation.createNewSimulation(-1);
					new MainWindow(true);
					Simulation.instance().start(false);

					closeWindow();
				}
			}
		});

		bottomButtonPanel.add(createButton);

		bottomButtonPanel.add(new JLabel("    "));

		// 2014-12-15 Added Edit Alpha Crew button.
		JButton alphaButton = new JButton("  " + Msg.getString("SimulationConfigEditor.button.crewEditor") + "  "); //$NON-NLS-1$
		alphaButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")); //$NON-NLS-1$
		alphaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				editCrewProfile("alpha");
			}
		});

		bottomButtonPanel.add(alphaButton);

		// Set the location of the dialog at the center of the screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((screenSize.width - f.getWidth()) / 2, (screenSize.height - f.getHeight()) / 2);
		f.setVisible(true);

		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				if (isCrewEditorOpen) {
					crewEditor.getJFrame().dispose();
				}
				f.dispose();
			}
		});
	}

	/*
	 * Determines proper width for each column and center aligns each cell content
	 */
	// 2015-10-03 Added adjustColumn()
	private void adjustColumn(JTable t) {
		// If all column heads are wider than the column's cells'
		// contents, then you can just use column.sizeWidthToFit().
		final Object[] longValues = { "Schiaparelli Point", "Mars Direct Base (phase 1)", new Integer(18),
				new Integer(16), new Integer(22), new Integer(22), Boolean.TRUE };

		boolean DEBUG = false;
		// SettlementTableModel model = settlementTableModel;
		// //(SettlementTableModel)table.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		TableCellRenderer headerRenderer = t.getTableHeader().getDefaultRenderer();

		// 2015-10-03 Align content to center of cell
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
		defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		for (int i = 0; i < 7; i++) {

			column = t.getColumnModel().getColumn(i);

			// 2015-10-03 Align content to center of cell
			column.setCellRenderer(defaultTableCellRenderer);

			comp = headerRenderer.getTableCellRendererComponent(t, column.getHeaderValue(), false, false, 0, 0);

			headerWidth = comp.getPreferredSize().width;

			comp = t.getDefaultRenderer(settlementTableModel.getColumnClass(i)).getTableCellRendererComponent(t,
					longValues[i], false, false, 0, i);

			cellWidth = comp.getPreferredSize().width;

			if (DEBUG) {
				System.out.println("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth
						+ "; cellWidth = " + cellWidth);
			}

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	/**
	 * Adds a new settlement with default values.
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
	 * Edits team profile.
	 */
	private void editCrewProfile(String crew) {
		if (crewEditor == null) {
			crewEditor = new CrewEditor(config, this);
			// System.out.println("new CrewEditor()");
		} else if (!isCrewEditorOpen) {
			crewEditor.createGUI();
			// System.out.println("crewEditor.createGUI()");
		}

	}

	public void setCrewEditorOpen(boolean value) {
		isCrewEditorOpen = value;
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
			// System.out.println("populationNum is " + populationNum);
			String numOfRobotsStr = (String) settlementTableModel.getValueAt(x, 3);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			// System.out.println("SimulationConfigEditor : numOfRobots is " + numOfRobots);
			String latitude = (String) settlementTableModel.getValueAt(x, 4);
			String longitude = (String) settlementTableModel.getValueAt(x, 5);

			String sponsor = (String) settlementTableModel.getValueAt(x, 6);
			settlementConfig.addInitialSettlement(name, template, populationNum, numOfRobots, sponsor, latitude,
					longitude);// , maxMSD);
		}
	}

	/**
	 * Close and dispose dialog window.
	 */
	private void closeWindow() {
		// dispose();
		f.dispose();
	}

	/**
	 * Sets an edit-check error.
	 * 
	 * @param errorString
	 *            the error description.
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
		errorLabel.setText(""); //$NON-NLS-1$
		createButton.setEnabled(true);
	}

	/**
	 * Determines the configuration of a new settlement.
	 * 
	 * @return settlement configuration.
	 */
	private SettlementInfo determineNewSettlementConfiguration() {
		SettlementInfo settlement = new SettlementInfo();

		settlement.name = determineNewSettlementName();
		settlement.template = determineNewSettlementTemplate();
		settlement.population = determineNewSettlementPopulation(settlement.template);
		settlement.numOfRobots = determineNewSettlementNumOfRobots(settlement.template);
		settlement.latitude = determineNewSettlementLatitude();
		settlement.longitude = determineNewSettlementLongitude();
		// settlement.maxMSD = "0";
		settlement.sponsor = determineNewSettlementSponsor();

		return settlement;
	}

	/**
	 * Determines the new settlement sponsorship.
	 * 
	 * @return the settlement sponsor name.
	 */
	private String determineNewSettlementSponsor() {

		return Msg.getString("ReportingAuthorityType.MarsSociety"); //$NON-NLS-1$
	}

	/**
	 * Determines a new settlement's name.
	 * 
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

		// If no name found, create numbered settlement name: "Settlement 1",
		// "Settlement 2", etc.
		int count = 1;
		while (result == null) {
			String name = Msg.getString("SimulationConfigEditor.settlement", //$NON-NLS-1$
					Integer.toString(count));

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
	 * 
	 * @return template name.
	 */
	private String determineNewSettlementTemplate() {
		String result = null;

		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<SettlementTemplate> templates = settlementConfig.getSettlementTemplates();
		if (templates.size() > 0) {
			int index = RandomUtil.getRandomInt(templates.size() - 1);
			result = templates.get(index).getTemplateName();
		} else
			logger.log(Level.WARNING, Msg.getString("SimulationConfigEditor.log.settlementTemplateNotFound")); //$NON-NLS-1$

		return result;
	}

	/**
	 * Determines the new settlement population.
	 * 
	 * @param templateName
	 *            the settlement template name.
	 * @return the new population number.
	 */
	private String determineNewSettlementPopulation(String templateName) {

		String result = "0"; //$NON-NLS-1$

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
	 * Determines the new settlement number of robots.
	 * 
	 * @param templateName
	 *            the settlement template name.
	 * @return number of robots.
	 */
	private String determineNewSettlementNumOfRobots(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultNumOfRobots());

				}
			}
		}

		return result;
	}

	/**
	 * Determines a new settlement's latitude.
	 * 
	 * @return latitude string.
	 */
	private String determineNewSettlementLatitude() {
		double phi = Coordinates.getRandomLatitude();
		String formattedLatitude = Coordinates.getFormattedLatitudeString(phi);
		int degreeIndex = formattedLatitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return formattedLatitude.substring(0, degreeIndex) + " "
				+ formattedLatitude.substring(degreeIndex + 1, formattedLatitude.length());
	}

	/**
	 * Determines a new settlement's longitude.
	 * 
	 * @return longitude string.
	 */
	private String determineNewSettlementLongitude() {
		double theta = Coordinates.getRandomLongitude();
		String formattedLongitude = Coordinates.getFormattedLongitudeString(theta);
		int degreeIndex = formattedLongitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return formattedLongitude.substring(0, degreeIndex) + " "
				+ formattedLongitude.substring(degreeIndex + 1, formattedLongitude.length());
	}

	/**
	 * Inner class representing a settlement configuration.
	 */
	private class SettlementInfo {
		String name;
		String template;
		String population;
		String numOfRobots;
		String latitude;
		String longitude;
		String sponsor;
	}

	/**
	 * Inner class for the settlement table model.
	 */
	private class SettlementTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members
		private String[] columns;
		private List<SettlementInfo> settlements;

		/**
		 * Hidden Constructor.
		 */
		private SettlementTableModel() {
			super();

			// Add table columns.
			columns = new String[] { Msg.getString("SimulationConfigEditor.column.name"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.template"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.population"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.numOfRobots"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.latitude"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.longitude"), //$NON-NLS-1$
					Msg.getString("SimulationConfigEditor.column.sponsor") //$NON-NLS-1$
			};

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
				info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
				info.latitude = settlementConfig.getInitialSettlementLatitude(x);
				info.longitude = settlementConfig.getInitialSettlementLongitude(x);
				// info.maxMSD = "0";
				info.sponsor = settlementConfig.getInitialSettlementSponsor(x);
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

		/*
		 * JTable uses this method to determine the default renderer/ editor for each
		 * cell. If we didn't implement this method, then the last column would contain
		 * text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public String getColumnName(int columnIndex) {
			if ((columnIndex > -1) && (columnIndex < columns.length)) {
				return columns[columnIndex];
			} else {
				return Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Object result = Msg.getString("unknown"); //$NON-NLS-1$

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
						result = info.numOfRobots;
						break;
					case 4:
						result = info.latitude;
						break;
					case 5:
						result = info.longitude;
						break;
					case 6:
						result = info.sponsor;
						break;
					case 7:

						break;
					}
				} else {
					result = Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
				}
			} else {
				result = Msg.getString("SimulationConfigEditor.log.invalidRow"); //$NON-NLS-1$
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
						info.population = determineNewSettlementPopulation(info.template);
						info.numOfRobots = determineNewSettlementNumOfRobots(info.template);
						break;
					case 2:
						info.population = (String) aValue;
						break;
					case 3:
						info.numOfRobots = (String) aValue;
						break;

					case 4:
						String latStr = ((String) aValue).trim();
						double doubleLat = 0;
						String dir1 = latStr.substring(latStr.length() - 1, latStr.length());
						if (dir1.toUpperCase().equals("N") | dir1.toUpperCase().equals("S")) {
							if (latStr.length() > 2) {
								doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
								doubleLat = Math.round(doubleLat * 10.0) / 10.0;
								info.latitude = doubleLat + " " + dir1;
							} else
								info.latitude = (String) aValue;
						} else
							info.latitude = (String) aValue;

						break;

					case 5:
						String longStr = ((String) aValue).trim();
						double doubleLong = 0;
						String dir = longStr.substring(longStr.length() - 1, longStr.length());
						if (dir.toUpperCase().equals("E") | dir.toUpperCase().equals("W")) {
							if (longStr.length() > 2) {
								doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
								doubleLong = Math.round(doubleLong * 10.0) / 10.0;
								info.longitude = doubleLong + " " + dir;
							} else
								info.longitude = (String) aValue;
						} else
							info.longitude = (String) aValue;

						break;

					case 6:

						// TODO: correct maxMSD should be loaded
						info.sponsor = (String) aValue;
						break;

					case 7:

						break;
					}
				}

				if (columnIndex != 6)
					checkForErrors();

				fireTableDataChanged();
			}
		}

		/**
		 * Remove a set of settlements from the table.
		 * 
		 * @param rowIndexes
		 *            an array of row indexes of the settlements to remove.
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
		 * 
		 * @param settlement
		 *            the settlement configuration.
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

			// TODO: check if the latitude/longitude pair is not being used in the host
			// server's settlement registry

			try {
				checkRepeatingLatLon();

			} catch (NumberFormatException e) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
			}

			Iterator<SettlementInfo> i = settlements.iterator();
			while (i.hasNext()) {
				SettlementInfo settlement = i.next();

				// Check that settlement name is valid.
				if ((settlement.name == null) || (settlement.name.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.nameMissing")); //$NON-NLS-1$
				}

				// Check if population is valid.
				if ((settlement.population == null) || (settlement.population.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.populationMissing")); //$NON-NLS-1$
				} else {
					try {
						int popInt = Integer.parseInt(settlement.population);
						if (popInt < 0) {
							setError(Msg.getString("SimulationConfigEditor.error.populationTooFew")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.populationInvalid")); //$NON-NLS-1$
					}
				}

				// Check if number of robots is valid.
				if ((settlement.numOfRobots == null) || (settlement.numOfRobots.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsMissing")); //$NON-NLS-1$
				} else {
					try {
						int num = Integer.parseInt(settlement.numOfRobots);
						if (num < 0) {
							setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsTooFew")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsInvalid")); //$NON-NLS-1$
					}
				}

				checkLatLon(settlement);

			}
		}

		/**
		 * Check for the validity of the input latitude and longitude
		 * 
		 * @param settlement
		 */
		private void checkLatLon(SettlementInfo settlement) {

			// Check that settlement latitude is valid.
			if ((settlement.latitude == null) || (settlement.latitude.isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
			} else {
				String cleanLatitude = settlement.latitude.trim().toUpperCase();
				if (!cleanLatitude.endsWith(Msg.getString("direction.northShort"))
						&& !cleanLatitude.endsWith(Msg.getString("direction.southShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
					setError(Msg.getString("SimulationConfigEditor.error.latitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.northShort"), //$NON-NLS-1$
							Msg.getString("direction.southShort") //$NON-NLS-1$
					));
				} else {
					String numLatitude = cleanLatitude.substring(0, cleanLatitude.length() - 1);
					try {
						double doubleLatitude = Double.parseDouble(numLatitude);
						if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
							setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
					}
				}
			}

			// Check that settlement longitude is valid.
			if ((settlement.longitude == null) || (settlement.longitude.isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
			} else {
				String cleanLongitude = settlement.longitude.trim().toUpperCase();
				if (!cleanLongitude.endsWith(Msg.getString("direction.westShort"))
						&& !cleanLongitude.endsWith(Msg.getString("direction.eastShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
					setError(Msg.getString("SimulationConfigEditor.error.longitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.eastShort"), //$NON-NLS-1$
							Msg.getString("direction.westShort") //$NON-NLS-1$
					));
				} else {
					String numLongitude = cleanLongitude.substring(0, cleanLongitude.length() - 1);
					try {
						double doubleLongitude = Double.parseDouble(numLongitude);
						if ((doubleLongitude < 0) || (doubleLongitude > 180)) {
							setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
					}
				}
			}
		}

		/***
		 * Checks for any repeating latitude and longitude
		 */
		private void checkRepeatingLatLon() {
			// Ensure the latitude/longitude is not being taken already in the table by
			// another settlement
			boolean repeated = false;
			int size = settlementTableModel.getRowCount();
			for (int x = 0; x < size; x++) {

				String latStr = ((String) (settlementTableModel.getValueAt(x, 4))).trim().toUpperCase();
				String longStr = ((String) (settlementTableModel.getValueAt(x, 5))).trim().toUpperCase();

				// check if the second from the last character is a digit or a letter, if a
				// letter, setError
				if (Character.isLetter(latStr.charAt(latStr.length() - 2))) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
					return;
				}

				// check if the last character is a digit or a letter, if digit, setError
				if (Character.isDigit(latStr.charAt(latStr.length() - 1))) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
					return;
				}

				if (latStr == null || latStr.length() < 2) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
					return;
				}

				if (longStr == null || longStr.length() < 2) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
					return;
				}

				// System.out.println("settlement.latitude is "+ settlement.latitude);
				if (x + 1 < size) {
					String latNextStr = ((String) (settlementTableModel.getValueAt(x + 1, 4))).trim().toUpperCase();
					String longNextStr = ((String) (settlementTableModel.getValueAt(x + 1, 5))).trim().toUpperCase();

					// System.out.println("latStr is "+ latStr);
					// System.out.println("latNextStr is "+ latNextStr);
					if (latNextStr == null || latNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
						return;
					} else if (latStr.equals(latNextStr)) {
						repeated = true;
						break;
					}

					else {
						double doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
						double doubleLatNext = Double.parseDouble(latNextStr.substring(0, latNextStr.length() - 1));

						if (doubleLatNext == 0 && doubleLat == 0) {
							repeated = true;
							break;
						}
					}

					if (longNextStr == null || longNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
						return;
					} else if (longStr.equals(longNextStr)) {
						repeated = true;
						break;
					}

					else {
						double doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
						double doubleLongNext = Double.parseDouble(longNextStr.substring(0, longNextStr.length() - 1));

						if (doubleLongNext == 0 && doubleLong == 0) {
							repeated = true;
							break;
						}
					}
				}
			}

			if (repeated) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeRepeating")); //$NON-NLS-1$
				return;
			}
		}

		/**
		 * Prepare for deletion.
		 */
		public void destroy() {

			columns = null;
			settlements = null;

		}

	}

	/**
	 * Prepare for deletion.
	 */
	public void destroy() {

		settlementTableModel = null;
		settlementTable = null;
		errorLabel = null;
		createButton = null;
		f = null;
		crewEditor = null;
	}

}