/*
 * Mars Simulation Project
 * CommanderWindow.java
 * @date 2023-10-11
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.commander;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.moon.Colony;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.util.BasicTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.FlagString;
import org.mars_sim.msp.ui.swing.tool.SmartScroller;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.tools.Msg;


/**
 * Window for the Commanders Dashboard.
 */
@SuppressWarnings("serial")
public class CommanderWindow extends ToolWindow {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CommanderWindow.class.getName());

	public static final String NAME = "Command Dashboard";
	public static final String ICON = "dashboard";

	private static final String DIPLOMATIC_TAB = "Diplomatic";

	private static final String AGRICULTURE_TAB = "Agriculture";
	private static final String COMPUTING_TAB = "Computing";
	private static final String ENGINEERING_TAB = "Engineering";
	private static final String LOGISTIC_TAB = "Logistic";
	private static final String MISSION_TAB = " Mission";
	private static final String RESOURCE_TAB = "Resource";
	private static final String SAFETY_TAB = "Safety";
	private static final String SCIENCE_TAB = "Science";

	private static final String CAN_INITIATE = "Can initiate Trading Mission";
	private static final String CANNOT_INITIATE = "Cannot initiate Trading Mission";
	private static final String ACCEPT = "Accept Trading initiated by other settlements";
	private static final String ACCEPT_NO = "Accept NO Trading initiated by other settlements";
	private static final String SEE_RIGHT = ".    -->";

	private int popCache = 1;
	private int bedCache;
	private int touristCache;
	private int residentCache;
	private int researcherCache;
	
	private double totalAreaCache;
	private double areaPerPersonCache;
	
	private double popRateCache;
	private double bedRateCache;
	private double touristRateCache;
	private double residentRateCache;
	private double researcherRateCache;
		
	
	private JTabbedPane tabPane;
	/** Person Combo box */	
	private JComboBoxMW<Person> personBox;
	/** Settlement Combo box */
	private JComboBox<Settlement> settlementBox;
	/** Settlement Combo box */
	private JComboBox<Building> buildingBox;
	/** Number JSpinner */
	private JSpinner areaSpinner;
	
	private ListModel listModel;
	private JList<TaskJob> list;
	private JTextArea logBookTA;

	private JPanel policyMainPanel;

	/** Check box for overriding EVA. */
	private JCheckBox overrideDigLocalRegolithCB;
	
	/** Check box for overriding EVA. */
	private JCheckBox overrideDigLocalIceCB;
	
	private JScrollPane listScrollPanel;

	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	private JRadioButton r4;
	
	private JLabel popLabel;
	private JLabel bedLabel;
	private JLabel touristLabel;
	private JLabel residentLabel;
	private JLabel researcherLabel;
	private JLabel totalAreaLabel;
	private JLabel areaPerPersonLabel;
	
	private Person cc;

	private Settlement settlement;
	
	private Colony colony;

	/** The MarsClock instance. */
	private MasterClock masterClock;
	private UnitManager unitManager;

	private JPanel tradingPartnersPanel;
	
	private Map<String, Settlement> tradingPartners;
	private List<Colony> colonyList;
	

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		this.masterClock = desktop.getSimulation().getMasterClock();
		unitManager = desktop.getSimulation().getUnitManager();

		List<Settlement> settlementList = new ArrayList<>(unitManager.getSettlements());
		Collections.sort(settlementList);
		settlement = settlementList.get(0);
		cc = settlement.getCommander();
		
		colonyList = new ArrayList<>(Simulation.instance().getLunarColonyManager().getColonySet());
		Collections.sort(colonyList);
		colony = colonyList.get(0);
				
		// Create content panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		JPanel topPane = new JPanel(new FlowLayout());
		mainPane.add(topPane, BorderLayout.NORTH);
		
		buildSettlementComboBox();
		topPane.add(settlementBox);


		JPanel bottomPane = new JPanel(new GridLayout(1, 4));
		bottomPane.setPreferredSize(new Dimension(-1, 50));
		mainPane.add(bottomPane, BorderLayout.SOUTH);

		// Create the info tab panel.
		tabPane = new JTabbedPane();
		mainPane.add(tabPane, BorderLayout.CENTER);
		
		createAgriculturePanel();
		createComputingPanel();
		createEngineeringPanel();
		createDiplomaticPanel();
		createLogisticPanel();
		createMissionPanel();
		createResourcePanel();
		createSafetyPanel();
		createSciencePanel();

		setSize(new Dimension(720, 512));
		setMaximizable(true);
		setResizable(false);

		setVisible(true);
	
		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

	}

	/**
     * Builds the settlement name combo box.
     */
	private void buildSettlementComboBox() {

		SettlementComboBoxModel settlementCBModel = new SettlementComboBoxModel();

		settlementBox = new JComboBox<>(settlementCBModel);
		settlementBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(SwingConstants.CENTER); // center-aligned items
		settlementBox.setRenderer(listRenderer);
		
		settlementBox.addItemListener(event -> {
				Settlement s = (Settlement) event.getItem();
				if (s != null) {
					// Update the selected settlement instance
					changeSettlement(s);
				}
		});
		
		settlementBox.setSelectedIndex(0);
	}
	
	/**
     * Constructs the building combo box.
     */
	private void constructBuildingBox(Settlement settlement, List<Building> bldgs) {

		BuildingComboBoxModel model = new BuildingComboBoxModel(settlement, bldgs);

		buildingBox = new JComboBox<>(model);
		buildingBox.setToolTipText("Select a Building");
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(SwingConstants.CENTER); // center-aligned items
		buildingBox.setRenderer(listRenderer);
	
		buildingBox.setSelectedIndex(0);
	}
	
	/**
	 * Sets up the person combo box.
	 * 
	 * @param s
	 */
	private void setUpPersonComboBox(Settlement s) {
		List<Person> people = new ArrayList<>(s.getAllAssociatedPeople());
		Collections.sort(people);
			
		DefaultComboBoxModel<Person> comboBoxModel = new DefaultComboBoxModel<>();
		
		if (personBox == null) {
			personBox = new JComboBoxMW<>(comboBoxModel);
		}
		else {
			personBox.removeAll();
			personBox.replaceModel(comboBoxModel);
		}
		
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			Person n = i.next();
	    	comboBoxModel.addElement(n);
		}
		
		personBox.setMaximumRowCount(8);
		personBox.setSelectedItem(cc);
	}
	
	
	/**
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	private void changeSettlement(Settlement s) {

		if (settlement != s) {
			setUpPersonComboBox(s);
											
			// Set the selected settlement
			settlement = s;
			// Set the box opaque
			settlementBox.setOpaque(false);

			setupTradingSettlements();
		}
	}

	
	/**
	 * Creates the diplomatic panel.
	 */
	private void createDiplomaticPanel() {
		JPanel panel = new JPanel(new BorderLayout(20, 20));
		tabPane.add(DIPLOMATIC_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		topPanel.setBorder(BorderFactory.createTitledBorder(" Lunar Colonies "));
		panel.add(topPanel, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		topPanel.add(tabbedPane, BorderLayout.NORTH);
		
		if (colonyList != null && !colonyList.isEmpty()) {
			for (Colony c: colonyList) {
				
				JPanel infoPanel = new JPanel(new BorderLayout(20, 20));
				
				AttributePanel labelGrid = new AttributePanel(4, 2);
				labelGrid.setBorder(new EmptyBorder(10, 10, 10, 10));
				infoPanel.add(labelGrid, BorderLayout.NORTH);
				
				String name = c.getName();

				// Name the tab
				tabbedPane.addTab(name, infoPanel);
	
				labelGrid.addRow("Base Name", name);
				
				String sponsorName = c.getReportingAuthority().getName();
				labelGrid.addRow("Corporation/Agency", sponsorName);
				
				labelGrid.addRow("Coordinates", c.getCoordinates().getFormattedString());
				
				List<String> list = c.getReportingAuthority().getCountries();
				
				String countryCode = "";
				String countryName = "Multi-Nationals";
				if (list.size() == 1) {
					countryName = c.getReportingAuthority().getCountries().get(0);
					countryCode = FlagString.getEmoji(countryName);
				}
				
				labelGrid.addLabels("Country", countryName, countryCode);
							
				totalAreaCache = c.getTotalArea();			
				totalAreaLabel = labelGrid.addRow("Total Area (SM)", Math.round(totalAreaCache * 10.0)/10.0 + "");
				
				areaPerPersonCache = totalAreaCache / popCache;
				areaPerPersonLabel = labelGrid.addRow("Area (SM) Per Person", Math.round(areaPerPersonCache * 10.0)/10.0 + "");

				bedCache = c.getPopulation().getNumBed();
				bedRateCache = c.getPopulation().getGrowthNumBed();
				String bedRateCacheString = bedCache + " (" + Math.round(bedRateCache * 10.0)/10.0 + ")";
				bedLabel = labelGrid.addRow("# of Quarters", bedRateCacheString + "");
							
				labelGrid.addRow("Birth Rate", "0.0");
				
				/////////////////////////////////////////////////////////////
				
				JPanel popPanel = new JPanel(new BorderLayout(20, 20));
				infoPanel.add(popPanel, BorderLayout.SOUTH);
				
				AttributePanel popGrid = new AttributePanel(2, 2);
				popPanel.add(popGrid, BorderLayout.NORTH);
				popPanel.setBorder(BorderFactory.createTitledBorder(" Population Types"));
				
				popCache = c.getPopulation().getTotalPopulation();
				popRateCache = c.getPopulation().getGrowthTotalPopulation();
				String popRateCacheString = popCache + " (" + Math.round(popRateCache * 10.0)/10.0 + ")";
				popLabel = popGrid.addRow("Total Population", popRateCacheString + "");
	
				// Update the area per person label right away
				areaPerPersonCache = Math.round(totalAreaCache / popCache * 10.0)/10.0;
				areaPerPersonLabel.setText(areaPerPersonCache + "");
				
				touristCache = c.getPopulation().getNumTourists();
				touristRateCache = c.getPopulation().getGrowthTourists();
				String touristRateCacheString = touristCache + " (" + Math.round(touristRateCache * 10.0)/10.0 + ")";
				touristLabel = popGrid.addRow("# of Tourists", touristRateCacheString + "");
				
				residentCache = c.getPopulation().getNumResidents();
				residentRateCache = c.getPopulation().getGrowthTourists();
				String residentRateCacheString = residentCache + " (" + Math.round(residentRateCache * 10.0)/10.0 + ")";
				residentLabel = popGrid.addRow("# of Residents", residentRateCacheString + "");
				
				researcherCache = c.getPopulation().getNumResearchers();
				researcherRateCache = c.getPopulation().getGrowthTourists();
				String researcherRateCacheString = researcherCache + " (" + Math.round(researcherRateCache * 10.0)/10.0 + ")";
				researcherLabel = popGrid.addRow("# of Researchers", researcherRateCacheString + "");
			}	
		}
		else
			System.out.println("colonyList is null.");
	}
	
	private void updateLunar() {
		
		if (colonyList != null && !colonyList.isEmpty()) {
			
			for (Colony c: colonyList) {

				int newBed = c.getPopulation().getNumBed();
				double newBedRate = c.getPopulation().getGrowthNumBed();
				if (bedCache != newBed
					&& bedRateCache != newBedRate) {
					bedCache = newBed;
					bedRateCache = newBedRate;
					String bedRateCacheString = newBed + " (" + Math.round(newBedRate * 10.0)/10.0 + ")";
					bedLabel.setText(bedRateCacheString);
				}
				
				int newPop = c.getPopulation().getTotalPopulation();
				double newPopRate = c.getPopulation().getGrowthTotalPopulation();
				if (popCache != newPop
					&& popRateCache != newPopRate) {
					popCache = newPop;
					popRateCache = newPopRate;
					String popRateCacheString = newPop + " (" + Math.round(newPopRate * 10.0)/10.0 + ")";
					popLabel.setText(popRateCacheString);
				}
	
				int newTourist = c.getPopulation().getNumTourists();
				double newTouristRate = c.getPopulation().getGrowthTourists();
				if (touristCache != newTourist
					&& touristRateCache != newTouristRate) {
					touristCache = newTourist;
					touristRateCache = newTouristRate;
					String touristRateCacheString = newTourist + " (" + Math.round(newTouristRate * 10.0)/10.0 + ")";
					touristLabel.setText(touristRateCacheString);
				}
				
				int newResident = c.getPopulation().getNumResidents();
				double newResidentRate = c.getPopulation().getGrowthResidents();
				if (residentCache != newResident
					&& residentRateCache != newResidentRate) {
					residentCache = newResident;
					residentRateCache = newResidentRate;
					String residentRateCacheString = newResident + " (" + Math.round(newResidentRate * 10.0)/10.0 + ")";
					residentLabel.setText(residentRateCacheString);
				}
				
				int newResearcher = c.getPopulation().getNumResearchers();
				double newResearcherRate = c.getPopulation().getGrowthResearchers();
				if (researcherCache != newResearcher
					&& researcherRateCache != newResearcherRate) {
					researcherCache = newResearcher;
					researcherRateCache = newResearcherRate;
					String researcherRateCacheString = newResearcher + " (" + Math.round(newResearcherRate * 10.0)/10.0 + ")";
					researcherLabel.setText(researcherRateCacheString);
				}
				
				double newTotalArea = Math.round(c.getTotalArea() * 10.0)/10.0;
				if (totalAreaCache != newTotalArea) {
					totalAreaCache = newTotalArea;
					totalAreaLabel.setText(newTotalArea + "");
				}
				
				double newAreaPerPerson = Math.round(newTotalArea / newPop * 10.0)/10.0;			
				if (areaPerPersonCache != newAreaPerPerson) {
					areaPerPersonCache = newAreaPerPerson;
					areaPerPersonLabel.setText(newAreaPerPerson + "");
				}
			}
		}
	}

	/**
	 * Creates the panel for Agriculture.
	 */
	private void createAgriculturePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(AGRICULTURE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		topPanel.setBorder(BorderFactory.createTitledBorder(" Crop Growing Area "));
		panel.add(topPanel, BorderLayout.NORTH);

		JPanel buildingPanel = new JPanel(new BorderLayout(20, 20));
		buildingPanel.setToolTipText("Choose a farm from the building combobox in this settlement");
		topPanel.add(buildingPanel, BorderLayout.WEST);
		
		JLabel buildingLabel = new JLabel(" Select a Farm : ");		
		buildingPanel.add(buildingLabel, BorderLayout.NORTH);

		List<Building> bldgList = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		
		constructBuildingBox(settlement, bldgList);
		buildingPanel.add(buildingBox, BorderLayout.CENTER);

		// Create spinner panel
		JPanel spinnerPanel = new JPanel(new BorderLayout(20, 20));
		topPanel.add(spinnerPanel, BorderLayout.CENTER);
		
		JLabel areaLabel = new JLabel(" Area Per Crop (in SM) : ");		
		spinnerPanel.add(areaLabel, BorderLayout.NORTH);

		SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
		
		// Go to that selected settlement
		Building bldg = (Building)buildingBox.getSelectedItem();
		Farming farm = null;
		int currentArea = 0;
		if (bldg != null) {
			farm = bldg.getFarming();
			currentArea = farm.getDesignatedCropArea();
		}
		spinnerModel.setValue(currentArea);
		
		logger.info(settlement, bldg, "Current Growing Area per Crop (in SM): " + currentArea + ".");
		
		areaSpinner = new JSpinner(spinnerModel);
		spinnerPanel.add(areaSpinner, BorderLayout.CENTER);
		spinnerPanel.setToolTipText("Change the growing area for each crop in a selected farm");
		
		areaSpinner.addChangeListener(e -> {
			int newArea = (int)spinnerModel.getValue();
			logger.info(settlement, "Setting Growing Area per Crop (in SM) to " + newArea + ".");
			
			if (!bldgList.isEmpty()) {
				for (Building b: bldgList) {
					b.getFarming().setDesignatedCropArea(newArea);
					logger.info(settlement, b, newArea + " SM.");
				}
			}
		});
	}

	  
	public int getGrowingArea() {
		return (int)areaSpinner.getModel().getValue();
	}

	
	
	private void createComputingPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(COMPUTING_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}
	
	
	private void createEngineeringPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(ENGINEERING_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	
	/**
	 * Creates the logistic panel for operation of tasks.
	 */
	private void createLogisticPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		tabPane.add(LOGISTIC_TAB, mainPanel);
		tabPane.setSelectedComponent(mainPanel);

	    JPanel centerPanel = new JPanel(new BorderLayout());
	    mainPanel.add(centerPanel, BorderLayout.CENTER);
	    mainPanel.add(new JLabel("  "), BorderLayout.WEST);
	    mainPanel.add(new JLabel("  "), BorderLayout.EAST);

		JPanel topPanel = new JPanel(new BorderLayout());
		centerPanel.add(topPanel, BorderLayout.NORTH);

		JPanel topBorderPanel = new JPanel(new BorderLayout());
		topPanel.add(topBorderPanel, BorderLayout.NORTH);

		JPanel midPanel = new JPanel(new BorderLayout());
		centerPanel.add(midPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new BorderLayout());
		centerPanel.add(southPanel, BorderLayout.SOUTH);
		
		// Create the person combo box
		createPersonCombobox(topBorderPanel);

		// Create the task combo box
		createTaskCombobox(topBorderPanel);

		// Create the task queue list
		createTaskQueueList(midPanel);

		// Create the log book panel
		createLogBookPanel(midPanel);
	}

	private void createEVAOVerride(JPanel panel) {
		// Create override panel.
		JPanel overridePanel = new JPanel(new GridLayout(1, 2));
		overridePanel.setAlignmentX(CENTER_ALIGNMENT);		
		panel.add(overridePanel, BorderLayout.NORTH);

		// Create DIG_LOCAL_REGOLITH override check box.
		overrideDigLocalRegolithCB = new JCheckBox("Override Digging Regolith");
		overrideDigLocalRegolithCB.setAlignmentX(CENTER_ALIGNMENT);
		overrideDigLocalRegolithCB.setToolTipText("Can only execute this task as a planned EVA"); 
		overrideDigLocalRegolithCB.addActionListener(arg0 -> settlement.setProcessOverride(OverrideType.DIG_LOCAL_REGOLITH, overrideDigLocalRegolithCB.isSelected()));
		overrideDigLocalRegolithCB.setSelected(settlement.getProcessOverride(OverrideType.DIG_LOCAL_REGOLITH));
		overridePanel.add(overrideDigLocalRegolithCB);
		
		// Create DIG_LOCAL_ICE override check box.
		overrideDigLocalIceCB = new JCheckBox("Override Digging Ice");
		overrideDigLocalIceCB.setAlignmentX(CENTER_ALIGNMENT);
		overrideDigLocalIceCB.setToolTipText("Can only execute this task as a planned EVA"); 
		overrideDigLocalIceCB.addActionListener(arg0 -> settlement.setProcessOverride(OverrideType.DIG_LOCAL_ICE, overrideDigLocalIceCB.isSelected()));
		overrideDigLocalIceCB.setSelected(settlement.getProcessOverride(OverrideType.DIG_LOCAL_ICE));
		overridePanel.add(overrideDigLocalIceCB);
	}
	/**
	 * Creates the person combo box.
	 *
	 * @param panel
	 */
	private void createPersonCombobox(JPanel panel) {
      	// Set up combo box model.
		setUpPersonComboBox(settlement);

		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		comboBoxPanel.add(personBox);

		JPanel crewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		crewPanel.add(comboBoxPanel);

		crewPanel.setBorder(BorderFactory.createTitledBorder(" Crew Member "));
		crewPanel.setToolTipText("Choose the crew member to give a task order");
		personBox.setToolTipText("Choose the crew member to give a task order");

	    panel.add(crewPanel, BorderLayout.WEST);
	}

	/**
	 * Creates the task combo box.
	 *
	 * @param panel
	 */
	private void createTaskCombobox(JPanel panel) {
		DefaultComboBoxModel<FactoryMetaTask> taskComboBoxModel = new DefaultComboBoxModel<>();
      	// Set up combo box model.
		for(FactoryMetaTask n : MetaTaskUtil.getPersonMetaTasks()) {
	    	taskComboBoxModel.addElement(n);
		}

		// Create comboBox.
		JComboBoxMW<FactoryMetaTask> taskComboBox = new JComboBoxMW<>(taskComboBoxModel);
		taskComboBox.setMaximumRowCount(10);

		JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		comboBoxPanel.add(taskComboBox);

		JPanel taskPanel = new JPanel(new BorderLayout());
		taskPanel.add(comboBoxPanel, BorderLayout.CENTER);

		taskPanel.setBorder(BorderFactory.createTitledBorder(" Task Order "));
		taskPanel.setToolTipText("Choose a task order to give");
		taskComboBox.setToolTipText("Choose a task order to give");

		// Create a button panel
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    taskPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Create the add button
	    JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(80, 25));
		addButton.addActionListener(e -> {
				Person selected = (Person) personBox.getSelectedItem();
				FactoryMetaTask task = (FactoryMetaTask) taskComboBox.getSelectedItem();
				selected.getMind().getTaskManager().addPendingTask(new BasicTaskJob(task,
													new RatingScore(1D)), true);

				logBookTA.append(masterClock.getMarsTime().getTruncatedDateTimeStamp()
						+ " - Assigning '" + task.getName() + "' to " + selected + "\n");
		        listUpdate();
				repaint();
		});
		buttonPanel.add(addButton);
		
		// Create the delete button
		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(80, 25));
		delButton.addActionListener(evt -> {
			if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
				deleteATask();
				listUpdate();
		    	repaint();
			}
		});
		buttonPanel.add(delButton);

	    panel.add(taskPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates the task queue list.
	 *
	 * @param panel
	 */
	private void createTaskQueueList(JPanel panel) {

	    JLabel label = new JLabel("Task Queue", SwingConstants.CENTER);
		StyleManager.applySubHeading(label);
		label.setBorder(new MarsPanelBorder());

	    JPanel taskQueuePanel = new JPanel(new BorderLayout());
	    taskQueuePanel.add(label, BorderLayout.NORTH);

	    JPanel queueListPanel = new JPanel(new BorderLayout());
		queueListPanel.add(taskQueuePanel, BorderLayout.NORTH);

		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		// Create list model
		listModel = new ListModel();

		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(event -> {
		        if (!event.getValueIsAdjusting() && event != null){
					deleteATask();
		        }
		});

		queueListPanel.add(listScrollPanel, BorderLayout.CENTER);
		
		panel.add(queueListPanel, BorderLayout.EAST); // 2nd add
	}


	/**
	 * Creates the log book panel for recording task orders.
	 *
	 * @param panel
	 */
	private void createLogBookPanel(JPanel panel) {

		JLabel logLabel = new JLabel("Log Book", SwingConstants.CENTER);
		StyleManager.applySubHeading(logLabel);
		logLabel.setBorder(new MarsPanelBorder());

	    JPanel logPanel = new JPanel(new BorderLayout());
	    logPanel.add(logLabel, BorderLayout.NORTH);

		// Create an text area
		JPanel textPanel = new JPanel(new BorderLayout()); 
	    textPanel.add(logPanel, BorderLayout.NORTH);

		logBookTA = new JTextArea(8, 25);
		logBookTA.setOpaque(false);
		logBookTA.setEditable(false);
		JScrollPane scrollTextArea = new JScrollPane (logBookTA,
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Monitor the vertical scroll of jta
		new SmartScroller(scrollTextArea, SmartScroller.VERTICAL, SmartScroller.END);

		textPanel.add(scrollTextArea, BorderLayout.CENTER);
		
	    panel.add(textPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Creates the mission tab panel.
	 */
	private void createMissionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(MISSION_TAB, panel);

		policyMainPanel = new JPanel(new BorderLayout());
		panel.add(policyMainPanel, BorderLayout.NORTH);
		policyMainPanel.setPreferredSize(new Dimension(200, 125));
		policyMainPanel.setMaximumSize(new Dimension(200, 125));

		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(4,1));
		policyMainPanel.add(buttonPanel, BorderLayout.CENTER);

		buttonPanel.setBorder(BorderFactory.createTitledBorder("Trading policy "));
		buttonPanel.setToolTipText("Select your trading policy with other settlements");

		ButtonGroup group0 = new ButtonGroup();
		ButtonGroup group1 = new ButtonGroup();

		r0 = new JRadioButton(CAN_INITIATE, true);
		r1 = new JRadioButton(CANNOT_INITIATE);

		// Set up initial conditions
		if (settlement.isMissionEnable(MissionType.TRADE)) {
			r0.setSelected(true);
			r1.setSelected(false);
		}
		else {
			r0.setSelected(false);
			r1.setSelected(true);
		}

		// Set up initial conditions
		boolean noTrading = false;

		r2 = new JRadioButton(ACCEPT_NO, noTrading);
		r3 = new JRadioButton(ACCEPT, !noTrading);

		JLabel selectLabel = new JLabel(" Choose :");
		selectLabel.setMinimumSize(new Dimension(150, 25));
		selectLabel.setPreferredSize(new Dimension(150, 25));

		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(selectLabel, BorderLayout.NORTH);

		// Set settlement check boxes
		tradingPartnersPanel = new JPanel();
		tradingPartnersPanel.setLayout(new BoxLayout(tradingPartnersPanel, BoxLayout.Y_AXIS));
		setupTradingSettlements();

		innerPanel.add(tradingPartnersPanel, BorderLayout.CENTER);

		JScrollPane innerScroll = new JScrollPane(innerPanel);

		r2.setSelected(false);
		r3.setSelected(true);
		r3.setText(ACCEPT + SEE_RIGHT);
		policyMainPanel.add(innerScroll, BorderLayout.EAST);

		group0.add(r0);
		group0.add(r1);

		group1.add(r2);
		group1.add(r3);

		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);

		PolicyRadioActionListener actionListener = new PolicyRadioActionListener();
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		r2.addActionListener(actionListener);
		r3.addActionListener(actionListener);

	}

	private void setupTradingSettlements() {
		tradingPartnersPanel.removeAll();

		tradingPartners = new HashMap<>();
		for(Settlement s : getOtherSettlements()) {
			JCheckBox cb = new JCheckBox(s.getName(), settlement.isAllowedTradeMission(s));
			cb.addItemListener(e -> {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				Settlement s1 = tradingPartners.get(((JCheckBox) e.getSource()).getText());
				settlement.setAllowTradeMissionFromASettlement(s1, selected);
			});


			tradingPartnersPanel.add(cb);
			tradingPartners.put(s.getName(), s);
		}
	}

	class PolicyRadioActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

	        if (button == r0) {
				logger.config("r0 selected");
	        	settlement.setMissionDisable(MissionType.TRADE, false);
	        } else if (button == r1) {
	        	logger.config("r1 selected");
	        	settlement.setMissionDisable(MissionType.TRADE, true);
	        } else if (button == r2) {
	        	logger.config("r2 selected");
		        disableAllCheckedSettlement();
				r3.setText(ACCEPT);
				policyMainPanel.setEnabled(false);
	        } else if (button == r3) {
	        	logger.config("r3 selected");
				r3.setText(ACCEPT + SEE_RIGHT);
				policyMainPanel.setEnabled(true);
	        }
	    }
	}

	private void createResourcePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(RESOURCE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);

		// Create the checkbox for dig local regolith and ice override
		createEVAOVerride(topPanel);
		
		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(5,1));
		topPanel.add(buttonPanel, BorderLayout.CENTER);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(" Pausing Interval"));
		buttonPanel.setToolTipText("Select the time interval for automatic simulation pausing");

		ButtonGroup group = new ButtonGroup();

		r0 = new JRadioButton("None", true);
		r1 = new JRadioButton("250 millisols");
		r2 = new JRadioButton("333 millisols");
		r3 = new JRadioButton("500 millisols");
		r4 = new JRadioButton("1 sol");

		group.add(r0);
		group.add(r1);
		group.add(r2);
		group.add(r3);
		group.add(r4);

		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);
		buttonPanel.add(r4);

		RadioButtonActionListener actionListener = new RadioButtonActionListener();
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		r2.addActionListener(actionListener);
		r3.addActionListener(actionListener);
		r4.addActionListener(actionListener);

	}

	class RadioButtonActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

	        if (button == r0) {
	        	masterClock.setCommandPause(false, 1000);
	        } else if (button == r1) {
	        	masterClock.setCommandPause(true, 250);
	        } else if (button == r2) {
	        	masterClock.setCommandPause(true, 333.333);
	        } else if (button == r3) {
	        	masterClock.setCommandPause(true, 500);
	        } else if (button == r4) {
	        	masterClock.setCommandPause(true, 999.999);
	        }
	    }
	}

	public void createSafetyPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(SAFETY_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	public void createSciencePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(SCIENCE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

    /**
     * Returns a list of other settlements.
     *
     * @return sample long list data
     */
    protected List<Settlement> getOtherSettlements() {
    	List<Settlement> list0 = new ArrayList<>(unitManager.getSettlements());
    	list0.remove(settlement);
        return list0;

    }

	/**
	 * Picks a task and delete it.
	 */
	public void deleteATask() {
		TaskJob n = list.getSelectedValue();
		if (n != null) {
			((Person) personBox.getSelectedItem()).getMind().getTaskManager().removePendingTask(n);
			logBookTA.append("Delete '" + n + "' from the list of task orders.\n");
		}
		else
			listUpdate();
	}

	public void listUpdate() {
		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
	}

	public boolean isNavPointsMapTabOpen() {
        return tabPane.getSelectedIndex() == 1;
	}

	/**
	 * Updates the window as time has changed.
	 * 
	 * @param pulse The Clock advance
	 */
	@Override
	public void update(ClockPulse pulse) {

		// Update list
		listUpdate();
		
		// Update lunar colonies
		updateLunar();
	}

	private void disableAllCheckedSettlement() {
		for(Component c : tradingPartnersPanel.getComponents()) {
			((JCheckBox) c).setSelected(false);
		}
	}

	/**
	 * Lists model for the tasks in queue.
	 */
	private class ListModel extends AbstractListModel<TaskJob> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<TaskJob> list = new ArrayList<>();

	    private ListModel() {
	    	Person selected = (Person) personBox.getSelectedItem();

	    	if (selected != null) {
	        	List<TaskJob> tasks = selected.getMind().getTaskManager().getPendingTasks();
		        if (tasks != null)
		        	list.addAll(tasks);
	    	}
	    }

        @Override
        public TaskJob getElementAt(int index) {
        	TaskJob result = null;

            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
        	if (list == null)
        		return 0;
        	return list.size();
        }

        /**
         * Updates the list model.
         */
        public void update() {
        	Person selected = (Person) personBox.getSelectedItem();
        	
        	if (selected != null) {
	        	List<TaskJob> newTasks = selected.getMind().getTaskManager().getPendingTasks();
	
	        	if (newTasks != null) {
                // if the list contains duplicate items, it somehow pass this test
		    		    if (list.size() != newTasks.size() || !list.containsAll(newTasks) || !newTasks.containsAll(list)) {	
		                list = new ArrayList<>(newTasks);
		                fireContentsChanged(this, 0, getSize());
		    		    }
	        	}
        	}
        }
	}
	
	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel extends DefaultComboBoxModel<Settlement>
		implements UnitListener {

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();

			// Add addUnitListener
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().addUnitListener(this);
			}

		}

		/**
		 * Updates the list of settlements.
		 */
		private void updateSettlements() {
			// Clear all elements
			removeAllElements();

			List<Settlement> settlements = new ArrayList<>();

			// Add the command dashboard button
			if (GameManager.getGameMode() == GameMode.COMMAND) {
				settlements = unitManager.getCommanderSettlements();
			}

			else { // if (GameManager.getGameMode() == GameMode.SANDBOX) {
				settlements.addAll(unitManager.getSettlements());
			}

			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}
		}

		@Override
		public void unitUpdate(UnitEvent event) {
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building building = (Building) target; // overwrite the dummy building object made by the constructor
				BuildingManager mgr = building.getBuildingManager();
				Settlement s = mgr.getSettlement();
				// Set the selected settlement
				changeSettlement(s);
				// Updated ComboBox
				settlementBox.setSelectedItem(s);
			}

			else if (eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT) {
				// Update the number of citizens
				Settlement s = (Settlement) settlementBox.getSelectedItem();
				// Set the selected settlement
				changeSettlement(s);
				
				setUpPersonComboBox(s);
		
				// Set the box opaque
				settlementBox.setOpaque(false);
			}
		}

		/**
		 * Prepares class for deletion.
		 */
		public void destroy() {
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}
		}
	}
	
	/**
	 * Inner class combo box model for buildings.
	 */
	public class BuildingComboBoxModel extends DefaultComboBoxModel<Building>
		implements UnitListener {

		private Settlement settlement;
		private List<Building> bldgs;
		
		/**
		 * Constructor.
		 */
		public BuildingComboBoxModel(Settlement settlement, List<Building> bldgs) {
			// User DefaultComboBoxModel constructor.
			super();
			this.settlement = settlement;
			this.bldgs = bldgs;

			// Add addUnitListener
			Iterator<Building> i = bldgs.iterator();
			while (i.hasNext()) {
				Building b = i.next();
				b.addUnitListener(this);
				addElement(b);
			}
		}

		/**
		 * Prepares class for deletion.
		 */
		public void destroy() {
			Iterator<Building> i = bldgs.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}
		}

		@Override
		public void unitUpdate(UnitEvent event) {
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building b = (Building) target; // overwrite the dummy building object made by the constructor
				
				if (b.getBuildingManager().getSettlement().equals(this.settlement)) {
					b.addUnitListener(this);
					addElement(b);
				}
			}
			else if (eventType == UnitEventType.REMOVE_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building b = (Building) target; // overwrite the dummy building object made by the constructor

				if (b.getBuildingManager().getSettlement().equals(this.settlement)) {
					b.removeUnitListener(this);
					removeElement(b);
				}
			}
		}
	}
	

	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		tabPane = null;
		personBox = null;
		listModel = null;
		listScrollPanel = null;
		list = null;
		cc = null;
	}
}
