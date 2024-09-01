/*
 * Mars Simulation Project
 * CommanderWindow.java
 * @date 2024-07-29
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.commander;

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
import javax.swing.ComboBoxModel;
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

import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colony;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.BasicTaskJob;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.PendingTask;
import com.mars_sim.core.person.ai.task.util.TaskFactory;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.SmartScroller;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * Window for the Commander Dashboard.
 */
@SuppressWarnings("serial")
public class CommanderWindow extends ToolWindow {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CommanderWindow.class.getName());

	public static final String NAME = "dashboard";
	public static final String ICON = "dashboard";
    public static final String TITLE = "Command Dashboard";

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
	private JList<PendingTask> list;
	private JTextArea logBookTA;

	private JPanel policyMainPanel;
	private JPanel tradingPartnersPanel;
	
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

	private JButton prefButton;
	
	private Map<Colony, Integer> popCaches = new HashMap<>();
	private Map<Colony, Integer> bedCaches = new HashMap<>();
	private Map<Colony, Integer> touristCaches = new HashMap<>();
	private Map<Colony, Integer> residentCaches = new HashMap<>();
	private Map<Colony, Integer> engineerCaches = new HashMap<>();
	private Map<Colony, Integer> researcherCaches = new HashMap<>();
	private Map<Colony, Integer> numResearchCaches = new HashMap<>();
	private Map<Colony, Integer> numDevelopmentCaches = new HashMap<>();
	
	private Map<Colony, Double> researchValueCaches = new HashMap<>();
	private Map<Colony, Double> developmentValueCaches = new HashMap<>();
	
	private Map<Colony, Double> researchDemandCaches = new HashMap<>();
	private Map<Colony, Double> developmentDemandCaches = new HashMap<>();
	
	private Map<Colony, Double> activenessResearchCaches = new HashMap<>();
	private Map<Colony, Double> activenessDevelopmentCaches = new HashMap<>();
	
	private Map<Colony, Double> researchAreaCaches = new HashMap<>();
	private Map<Colony, Double> developmentAreaCaches = new HashMap<>();
	
	private Map<Colony, Double> researchAreaGrowthRateCaches = new HashMap<>();
	private Map<Colony, Double> developmentAreaGrowthRateCaches = new HashMap<>();
	
	private Map<Colony, Double> totalAreaCaches = new HashMap<>();
	private Map<Colony, Double> areaPerPersonCaches = new HashMap<>();
	
	private Map<Colony, Double> popRateCaches = new HashMap<>();
	private Map<Colony, Double> bedRateCaches = new HashMap<>();
	
	private Map<Colony, Double> touristRateCaches = new HashMap<>();
	private Map<Colony, Double> residentRateCaches = new HashMap<>();
	private Map<Colony, Double> researcherRateCaches = new HashMap<>();
	private Map<Colony, Double> engineerRateCaches = new HashMap<>();
	
	private Map<Colony, JLabel> popLabels = new HashMap<>();
	private Map<Colony, JLabel> bedLabels = new HashMap<>();
	private Map<Colony, JLabel> touristLabels = new HashMap<>();
	private Map<Colony, JLabel> residentLabels = new HashMap<>();
	private Map<Colony, JLabel> researcherLabels = new HashMap<>();
	private Map<Colony, JLabel> engineerLabels = new HashMap<>();
	
	private Map<Colony, JLabel> totalAreaLabels = new HashMap<>();
	private Map<Colony, JLabel> areaPerPersonLabels = new HashMap<>();
	
	private Map<Colony, JLabel> researchValueLabels = new HashMap<>();
	private Map<Colony, JLabel> developmentValueLabels = new HashMap<>();
	
	private Map<Colony, JLabel> researchDemandLabels = new HashMap<>();
	private Map<Colony, JLabel> developmentDemandLabels = new HashMap<>();
	
	
	private Map<Colony, JLabel> numResearchLabels = new HashMap<>();
	private Map<Colony, JLabel> numDevelopmentLabels = new HashMap<>();
	
	private Map<Colony, JLabel> activenessResearchLabels = new HashMap<>();
	private Map<Colony, JLabel> activenessDevelopmentLabels = new HashMap<>();
	
	private Map<Colony, JLabel> researchAreaLabels = new HashMap<>();
	private Map<Colony, JLabel> developmentAreaLabels = new HashMap<>();

	private Map<String, Settlement> tradingPartners;
	
	private List<Building> greenhouseBldgs;
	
	private List<Colony> colonyList;
	
	private Person cc;

	private Settlement settlement;

	/** The MasterClock instance. */
	private MasterClock masterClock;
	
	private UnitManager unitManager;

	

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, TITLE, desktop);

		this.masterClock = desktop.getSimulation().getMasterClock();
		unitManager = desktop.getSimulation().getUnitManager();

		List<Settlement> settlementList = new ArrayList<>(unitManager.getSettlements());
		Collections.sort(settlementList);
		settlement = settlementList.get(0);
		
		cc = settlement.getCommander();
		
		colonyList = new ArrayList<>(Simulation.instance().getLunarColonyManager().getColonySet());
		Collections.sort(colonyList);
				
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
		createDiplomaticPanel();
		createEngineeringPanel();
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
     * 
	 * @param settlement
	 * @param bldgs
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

			// Modify trading settlements in Mission Tab
			setupTradingSettlements();
			// Modify preference settlement in Mission Tab			
			prefButton.setText("Open " + s.getName() + " Preference tab");
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
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		
		topPanel.add(tabbedPane, BorderLayout.NORTH);
		
		if (colonyList != null && !colonyList.isEmpty()) {
			for (Colony c: colonyList) {
				
				JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
				
				AttributePanel labelGrid = new AttributePanel(3, 2);
				labelGrid.setBorder(new EmptyBorder(5, 5, 5, 5));
				infoPanel.add(labelGrid, BorderLayout.NORTH);
				
				String name = c.getName();

				// Name the tab
				tabbedPane.addTab(name, infoPanel);
	
				// Get the base name
				labelGrid.addRow("Base Name", name);
	
				// Get the coordinates
				labelGrid.addRow("Coordinates", c.getCoordinates().getFormattedString());

				// Get the country name
				List<String> countryList = c.getAuthority().getCountries();				
				String countryName = "Multi-National";
				if (countryList.size() == 1) {
					countryName = c.getAuthority().getCountries().get(0);
//					countryCode = FlagString.getEmoji(countryName);
				}
				labelGrid.addRow("Country", countryName);

				// Get the total area
				double totalAreaCache = c.getTotalArea();	
				totalAreaCaches.put(c, totalAreaCache);
				JLabel totalAreaLabel = labelGrid.addRow("Total Area", Math.round(totalAreaCache * 10.0)/10.0 + " SM");
				totalAreaLabels.put(c, totalAreaLabel);
				
				// Get the sponsor name
				String sponsorName = c.getAuthority().getName();
				labelGrid.addRow("Sponsor", sponsorName);

				// Get the area of person estimate
				int popCache = 1;
				double areaPerPersonCache = totalAreaCache / popCache;
				areaPerPersonCaches.put(c, areaPerPersonCache);
				JLabel areaPerPersonLabel = labelGrid.addRow("Area Per Person", Math.round(areaPerPersonCache * 10.0)/10.0 + " SM");
				areaPerPersonLabels.put(c, areaPerPersonLabel);
				
				// FUTURE: will model and derive birth rate
//				labelGrid.addRow("Birth Rate", "0.0");
				
				/////////////////////////////////////////////////////////////
				
				JPanel popPanel = new JPanel(new BorderLayout(10, 10));
				infoPanel.add(popPanel, BorderLayout.CENTER);
				
				AttributePanel popGrid = new AttributePanel(3, 2);
				popPanel.add(popGrid, BorderLayout.NORTH);
				popGrid.setBorder(BorderFactory.createTitledBorder(" Population Types"));
				
				popCache = c.getPopulation().getTotalPopulation();
				popCaches.put(c, popCache);
				double popRateCache = c.getPopulation().getGrowthTotalPopulation();
				popRateCaches.put(c, popRateCache);
				String popRateCacheString = popCache + " (" + Math.round(popRateCache * 10.0)/10.0 + ")";
				JLabel popLabel = popGrid.addRow("Total Population", popRateCacheString + "");
				popLabels.put(c, popLabel);
				
				/////////////////////////
				
				int bedCache = c.getPopulation().getNumBed();
				bedCaches.put(c, bedCache);
				double bedRateCache = c.getPopulation().getGrowthNumBed();
				bedRateCaches.put(c, bedRateCache);
				String bedRateCacheString = bedCache + " (" + Math.round(bedRateCache * 10.0)/10.0 + ")";
				JLabel bedLabel = popGrid.addRow("# of Beds", bedRateCacheString + "");
				bedLabels.put(c, bedLabel);
				
				
				/////////////////////////////
				
				// Update the area per person label right away
				areaPerPersonCache = Math.round(totalAreaCache / popCache * 10.0)/10.0;
				areaPerPersonCaches.put(c, areaPerPersonCache);
				areaPerPersonLabel.setText(areaPerPersonCache + " SM");
				areaPerPersonLabels.put(c, areaPerPersonLabel);
				
				//////////////////////////////////////////////////////
				
				int touristCache = c.getPopulation().getNumTourists();
				touristCaches.put(c, touristCache);
				double touristRateCache = c.getPopulation().getGrowthTourists();
				touristRateCaches.put(c, touristRateCache);
				String touristRateCacheString = touristCache + " (" + Math.round(touristRateCache * 10.0)/10.0 + ")";
				JLabel touristLabel = popGrid.addRow("# of Tourists", touristRateCacheString + "");
				touristLabels.put(c, touristLabel);
				
				int residentCache = c.getPopulation().getNumResidents();
				residentCaches.put(c, residentCache);
				double residentRateCache = c.getPopulation().getGrowthResidents();
				residentRateCaches.put(c, residentRateCache);
				String residentRateCacheString = residentCache + " (" + Math.round(residentRateCache * 10.0)/10.0 + ")";
				JLabel residentLabel = popGrid.addRow("# of Residents", residentRateCacheString + "");
				residentLabels.put(c, residentLabel);
				
				int researcherCache = c.getPopulation().getNumResearchers();
				researcherCaches.put(c, researcherCache);
				double researcherRateCache = c.getPopulation().getGrowthResearchers();
				researcherRateCaches.put(c, researcherRateCache);
				String researcherRateCacheString = researcherCache + " (" + Math.round(researcherRateCache * 10.0)/10.0 + ")";
				JLabel researcherLabel = popGrid.addRow("# of Researchers", researcherRateCacheString + "");
				researcherLabels.put(c, researcherLabel);
				
				int engineerCache = c.getPopulation().getNumEngineers();
				engineerCaches.put(c, engineerCache);
				double engineerRateCache = c.getPopulation().getGrowthEngineers();
				engineerRateCaches.put(c, engineerRateCache);
				String engineerRateCacheString = engineerCache + " (" + Math.round(engineerRateCache * 10.0)/10.0 + ")";
				JLabel engineerLabel = popGrid.addRow("# of Engineers", engineerRateCacheString + "");
				engineerLabels.put(c, engineerLabel);
				
				////////// Show Statistics on Research and Development //////////
				
				AttributePanel rdGrid = new AttributePanel(5, 2);
				popPanel.add(rdGrid, BorderLayout.CENTER);
				rdGrid.setBorder(BorderFactory.createTitledBorder(" Research and Development"));
	
				
				int numResearchCache = c.getNumResearchProjects();
				numResearchCaches.put(c, numResearchCache);
				String numResearchCacheString = numResearchCache + " (" + Math.round(0 * 10.0)/10.0 + ")";
				JLabel numResearchLabel = rdGrid.addRow("# Research Proj", numResearchCacheString + "");
				numResearchLabels.put(c, numResearchLabel);
				
				int numDevelopmentCache = c.getNumDevelopmentProjects();
				numDevelopmentCaches.put(c, numDevelopmentCache);
				String numDevelopmentCacheString = numDevelopmentCache + " (" + Math.round(0 * 10.0)/10.0 + ")";
				JLabel numDevelopmentLabel = rdGrid.addRow("# Development Proj", numDevelopmentCacheString + "");
				numDevelopmentLabels.put(c, numDevelopmentLabel);
				
				double researchAreaCache = c.getResearchArea();
				double researchAreaGrowthRateCache = c.getResearchAreaGrowthRate();
				researchAreaCaches.put(c, researchAreaCache);
				researchAreaGrowthRateCaches.put(c, researchAreaGrowthRateCache);
				String researchAreaCacheString = Math.round(researchAreaCache * 10.0)/10.0
						+ " (" + Math.round(researchAreaGrowthRateCache * 100.0)/100.0 + ")";
				JLabel researchAreaLabel = rdGrid.addRow("Research Facility Area", researchAreaCacheString + "");
				researchAreaLabels.put(c, researchAreaLabel);
		
				double developmentAreaCache = c.getDevelopmentArea();
				double developmentAreaGrowthRateCache = c.getDevelopmentAreaGrowthRate();
				developmentAreaCaches.put(c, developmentAreaCache);
				developmentAreaGrowthRateCaches.put(c, developmentAreaGrowthRateCache);
				String developmentAreaCacheString = Math.round(developmentAreaCache * 10.0)/10.0
						+ " (" + Math.round(developmentAreaGrowthRateCache * 100.0)/100.0 + ")";
				JLabel developmentAreaLabel = rdGrid.addRow("Development Facility Area", developmentAreaCacheString + "");
				developmentAreaLabels.put(c, developmentAreaLabel);
				
				double activenessResearchCache = c.getAverageResearchActiveness();
				activenessResearchCaches.put(c, activenessResearchCache);
				String activenessResearchCacheString = Math.round(activenessResearchCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel activenessResearchLabel = rdGrid.addRow("Research Activeness", activenessResearchCacheString + "");
				activenessResearchLabels.put(c, activenessResearchLabel);
				
				double activenessDevelopmentCache = c.getAverageDevelopmentActiveness();
				activenessDevelopmentCaches.put(c, activenessDevelopmentCache);
				String activenessDevelopmentCacheString = Math.round(activenessDevelopmentCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel activenessDevelopmentLabel = rdGrid.addRow("Development Activeness", activenessDevelopmentCacheString + "");
				activenessDevelopmentLabels.put(c, activenessDevelopmentLabel);
						
				double researchDemandCache = c.getResearchDemand();
				researchDemandCaches.put(c, researchDemandCache);
				String researchDemandCacheString = Math.round(researchDemandCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel researchDemandLabel = rdGrid.addRow("Research Demand", researchDemandCacheString + "");
				researchDemandLabels.put(c, researchDemandLabel);
				
				double developmentDemandCache = c.getDevelopmentDemand();
				developmentDemandCaches.put(c, developmentDemandCache);
				String developmentDemandCacheString = Math.round(developmentDemandCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel developmentDemandLabel = rdGrid.addRow("Development Demand", developmentDemandCacheString + "");
				developmentDemandLabels.put(c, developmentDemandLabel);
				
				double researchValueCache = c.getTotalResearchValue();
				researchValueCaches.put(c, researchValueCache);
				String researchValueCacheString = Math.round(researchValueCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel researchValueLabel = rdGrid.addRow("Research Values", researchValueCacheString + "");
				researchValueLabels.put(c, researchValueLabel);
				
				double developmentValueCache = c.getTotalDevelopmentValue();
				developmentValueCaches.put(c, developmentValueCache);
				String developmentValueCacheString = Math.round(developmentValueCache * 100.0)/100.0
						+ " (" + Math.round(0 * 100.0)/100.0 + ")";
				JLabel developmentValueLabel = rdGrid.addRow("Development Values", developmentValueCacheString + "");
				developmentValueLabels.put(c, developmentValueLabel);
			}	
		}
	}
	
	/**
	 * Updates the lunar colony panel.
	 */
	private void updateLunarPanel() {
		
		if (colonyList != null && !colonyList.isEmpty()) {
			
			for (Colony c: colonyList) {

				int newBed = c.getPopulation().getNumBed();
				double newBedRate = c.getPopulation().getGrowthNumBed();
				if (bedCaches.get(c) != newBed
					 || bedRateCaches.get(c) != newBedRate) {
					bedCaches.put(c, newBed);
					bedRateCaches.put(c, newBedRate);
					String bedRateCacheString = newBed + " (" + Math.round(newBedRate * 10.0)/10.0 + ")";
					bedLabels.get(c).setText(bedRateCacheString);
				}
				
				int newPop = c.getPopulation().getTotalPopulation();
				double newPopRate = c.getPopulation().getGrowthTotalPopulation();
				if (popCaches.get(c) != newPop
					 || popRateCaches.get(c) != newPopRate) {
					popCaches.put(c, newPop);
					popRateCaches.put(c, newPopRate);
					String popRateCacheString = newPop + " (" + Math.round(newPopRate * 10.0)/10.0 + ")";
					popLabels.get(c).setText(popRateCacheString);
				}
	
				int newTourist = c.getPopulation().getNumTourists();
				double newTouristRate = c.getPopulation().getGrowthTourists();
				if (touristCaches.get(c) != newTourist
					 || touristRateCaches.get(c) != newTouristRate) {
					touristCaches.put(c, newTourist);
					touristRateCaches.put(c, newTouristRate);
					String touristRateCacheString = newTourist + " (" + Math.round(newTouristRate * 10.0)/10.0 + ")";
					touristLabels.get(c).setText(touristRateCacheString);
				}
				
				int newResident = c.getPopulation().getNumResidents();
				double newResidentRate = c.getPopulation().getGrowthResidents();
				if (residentCaches.get(c) != newResident
					 || residentRateCaches.get(c) != newResidentRate) {
					residentCaches.put(c, newResident);
					residentRateCaches.put(c, newResidentRate);
					String residentRateCacheString = newResident + " (" + Math.round(newResidentRate * 10.0)/10.0 + ")";
					residentLabels.get(c).setText(residentRateCacheString);
				}
				
				int newResearcher = c.getPopulation().getNumResearchers();
				double newResearcherRate = c.getPopulation().getGrowthResearchers();
				if (researcherCaches.get(c) != newResearcher
					 || researcherRateCaches.get(c) != newResearcherRate) {
					researcherCaches.put(c, newResearcher);
					researcherRateCaches.put(c, newResearcherRate);
					String researcherRateCacheString = newResearcher + " (" + Math.round(newResearcherRate * 10.0)/10.0 + ")";
					researcherLabels.get(c).setText(researcherRateCacheString);
				}
				
				int newEngineer = c.getPopulation().getNumEngineers();
				double newEngineerRate = c.getPopulation().getGrowthEngineers();
				if (engineerCaches.get(c) != newEngineer
					 || engineerRateCaches.get(c) != newEngineerRate) {
					engineerCaches.put(c, newEngineer);
					engineerRateCaches.put(c, newEngineerRate);
					String engineerRateCacheString = newEngineer + " (" + Math.round(newEngineerRate * 10.0)/10.0 + ")";
					engineerLabels.get(c).setText(engineerRateCacheString);
				}
				
				double newResearchDemand = c.getResearchDemand();
				if (researchDemandCaches.get(c) != newResearchDemand) {
					researchDemandCaches.put(c, newResearchDemand);
					String researchDemandCacheString = Math.round(newResearchDemand * 100.0)/100.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					researchDemandLabels.get(c).setText(researchDemandCacheString);
				}
				
				double newResearchValue = c.getTotalResearchValue();
				if (researchValueCaches.get(c) != newResearchValue) {
					researchValueCaches.put(c, newResearchValue);
					String researchValueCacheString = Math.round(newResearchValue * 100.0)/100.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					researchValueLabels.get(c).setText(researchValueCacheString);
				}
				
				double newDevelopmentDemand = c.getDevelopmentDemand();
				if (developmentDemandCaches.get(c) != newDevelopmentDemand) {
					developmentDemandCaches.put(c, newDevelopmentDemand);
					String developmentDemandCacheString = Math.round(newDevelopmentDemand * 100.0)/100.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					developmentDemandLabels.get(c).setText(developmentDemandCacheString);
				}
				
				double newDevelopmentValue = c.getTotalDevelopmentValue();
				if (developmentValueCaches.get(c) != newDevelopmentValue) {
					developmentValueCaches.put(c, newDevelopmentValue);
					String developmentValueCacheString = Math.round(newDevelopmentValue * 100.0)/100.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					developmentValueLabels.get(c).setText(developmentValueCacheString);
				}
				
				double newActivenessResearch = c.getAverageResearchActiveness();
				if (activenessResearchCaches.get(c) != newActivenessResearch) {
					activenessResearchCaches.put(c, newActivenessResearch);
					String activenessResearchCacheString = Math.round(newActivenessResearch * 10.0)/10.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					activenessResearchLabels.get(c).setText(activenessResearchCacheString);
				}
				
				double newActivenessDevelopment = c.getAverageDevelopmentActiveness();
				if (activenessDevelopmentCaches.get(c) != newActivenessDevelopment) {
					activenessDevelopmentCaches.put(c, newActivenessDevelopment);
					String activenessDevelopmentCacheString = Math.round(newActivenessDevelopment * 10.0)/10.0 
							+ " (" + Math.round(0 * 10.0)/10.0 + ")";
					activenessDevelopmentLabels.get(c).setText(activenessDevelopmentCacheString);
				}
				
				double newResearchArea = c.getResearchArea();
				double newResearchAreaGrowthRateCache = c.getResearchAreaGrowthRate();
				if (researchAreaCaches.get(c) != newResearchArea
						|| researchAreaGrowthRateCaches.get(c) != newResearchAreaGrowthRateCache) {
					researchAreaCaches.put(c, newResearchArea);
					researchAreaGrowthRateCaches.put(c, newResearchAreaGrowthRateCache);
					String researchAreaCacheString = Math.round(newResearchArea * 10.0)/10.0 
							+ " (" + Math.round(newResearchAreaGrowthRateCache * 100.0)/100.0 + ")";
					researchAreaLabels.get(c).setText(researchAreaCacheString);
				}
				
				double newDevelopmentArea = c.getDevelopmentArea();
				double newDevelopmentAreaGrowthRateCache = c.getDevelopmentAreaGrowthRate();
				if (developmentAreaCaches.get(c) != newDevelopmentArea
						|| developmentAreaGrowthRateCaches.get(c) != newDevelopmentAreaGrowthRateCache) {
					developmentAreaCaches.put(c, newDevelopmentArea);
					developmentAreaGrowthRateCaches.put(c, newDevelopmentAreaGrowthRateCache);
					String developmentAreaCacheString = Math.round(newDevelopmentArea * 10.0)/10.0 
							+ " (" + Math.round(newDevelopmentAreaGrowthRateCache * 100.0)/100.0 + ")";
					developmentAreaLabels.get(c).setText(developmentAreaCacheString);
				}
				
				int newNumResearch = c.getNumResearchProjects();
				if (numResearchCaches.get(c) != newNumResearch) {
					numResearchCaches.put(c, newNumResearch);
					String numResearchCacheString = newNumResearch + " (" + Math.round(0 * 10.0)/10.0 + ")";
					numResearchLabels.get(c).setText(numResearchCacheString);
				}
				
				int newNumDevelopment = c.getNumDevelopmentProjects();
				if (numDevelopmentCaches.get(c) != newNumDevelopment) {
					numDevelopmentCaches.put(c, newNumDevelopment);
					String numDevelopmentCacheString = newNumDevelopment + " (" + Math.round(0 * 10.0)/10.0 + ")";
					numDevelopmentLabels.get(c).setText(numDevelopmentCacheString);
				}
				
				double newTotalArea = Math.round(c.getTotalArea() * 10.0)/10.0;
				if (totalAreaCaches.get(c) != newTotalArea) {
					totalAreaCaches.put(c, newTotalArea);
					totalAreaLabels.get(c).setText(newTotalArea + " SM");
				}
				
				double newAreaPerPerson = Math.round(newTotalArea / newPop * 10.0)/10.0;			
				if (areaPerPersonCaches.get(c) != newAreaPerPerson) {
					areaPerPersonCaches.put(c, newAreaPerPerson);
					areaPerPersonLabels.get(c).setText(newAreaPerPerson + "");
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
		buildingPanel.setBorder(BorderFactory.createTitledBorder(" Select a Farm : "));	
		buildingPanel.setToolTipText("Choose a farm from the building combobox in this settlement");
		topPanel.add(buildingPanel, BorderLayout.WEST);
		
		greenhouseBldgs = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		
		constructBuildingBox(settlement, greenhouseBldgs);
		buildingPanel.add(buildingBox, BorderLayout.CENTER);

		// Create spinner panel
		JPanel spinnerPanel = new JPanel(new BorderLayout(20, 20));
		spinnerPanel.setBorder(BorderFactory.createTitledBorder(" Area Per Crop (in SM) : "));
		
		topPanel.add(spinnerPanel, BorderLayout.CENTER);
		
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
				
		areaSpinner = new JSpinner(spinnerModel);
		spinnerPanel.add(areaSpinner, BorderLayout.CENTER);
		spinnerPanel.setToolTipText("Change the growing area for each crop in a selected farm");
		
		areaSpinner.addChangeListener(e -> {
			int newArea = (int)spinnerModel.getValue();
			logger.info(settlement, "Setting Growing Area per Crop (in SM) to " + newArea + ".");
			
			if (!greenhouseBldgs.isEmpty()) {
				for (Building b: greenhouseBldgs) {
					b.getFarming().setDesignatedCropArea(newArea);
					logger.info(b, newArea + " SM.");
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
		DefaultComboBoxModel<TaskFactory> taskComboBoxModel = new DefaultComboBoxModel<>();
      	// Set up combo box model.
	    taskComboBoxModel.addAll(MetaTaskUtil.getPersonTaskFactorys());

		// Create comboBox.
		JComboBoxMW<TaskFactory> taskComboBox = new JComboBoxMW<>(taskComboBoxModel);
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
				TaskFactory task = (TaskFactory) taskComboBox.getSelectedItem();
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

		JPanel prefPanel = new JPanel(new FlowLayout());
		panel.add(prefPanel, BorderLayout.CENTER);
		prefPanel.setBorder(BorderFactory.createTitledBorder("Preferences"));
		prefPanel.setToolTipText("Modify your settlement preference");
		
		prefButton = new JButton();
		prefPanel.add(prefButton);
		prefButton.setText("Go to " + settlement.getName() + " Preference tab");
		prefButton.addActionListener(e -> {
			Settlement selected = (Settlement) settlementBox.getSelectedItem();
			UnitWindow window = getDesktop().openUnitWindow(selected);
			TabPanel tab = window.openTab(Msg.getString("TabPanelPreferences.title")); //$NON-NLS-1$
			if (tab != null) {
				logger.info(selected, "The Preference tab is opened.");
			}
			repaint();
		});

	}

	/**
	 * Sets up trading settlements.
	 */
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

	/**
	 * Action Listener for trading mission policy.
	 */
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

	/**
	 * Creates the resource panel.
	 */
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
		PendingTask n = list.getSelectedValue();
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
		updateLunarPanel();
		
		// Update the greenhouse building list
		updateGreenhouses();
	}

	/**
	 * Updates the greenhouse list.
	 */
	private void updateGreenhouses() {
		List<Building> newList = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		
		if (!greenhouseBldgs.equals(newList)) {
			greenhouseBldgs = newList;
			ComboBoxModel<Building> model = buildingBox.getModel();
			((BuildingComboBoxModel)model).replaceGreenhouses(settlement, newList);
		}		
	}
	
	private void disableAllCheckedSettlement() {
		for(Component c : tradingPartnersPanel.getComponents()) {
			((JCheckBox) c).setSelected(false);
		}
	}

	/**
	 * Lists model for the tasks in queue.
	 */
	private class ListModel extends AbstractListModel<PendingTask> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<PendingTask> list = new ArrayList<>();

	    private ListModel() {
	    	Person selected = (Person) personBox.getSelectedItem();

	    	if (selected != null) {
	        	List<PendingTask> tasks = selected.getMind().getTaskManager().getPendingTasks();
		        if (tasks != null)
		        	list.addAll(tasks);
	    	}
	    }

        @Override
        public PendingTask getElementAt(int index) {
        	PendingTask result = null;

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
	        	List<PendingTask> newTasks = selected.getMind().getTaskManager().getPendingTasks();
	
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

			else { 
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

		public void replaceGreenhouses(Settlement newSettlement, List<Building> newBldgs) {
			// Remove previous UnitListener and elements
			Iterator<Building> i = bldgs.iterator();
			while (i.hasNext()) {
				Building b = i.next();
				b.removeUnitListener(this);
				removeElement(b);
			}
				
			this.settlement = newSettlement;
			this.bldgs = newBldgs;
			
			// Add addUnitListener
			Iterator<Building> ii = newBldgs.iterator();
			while (ii.hasNext()) {
				Building b = ii.next();
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
