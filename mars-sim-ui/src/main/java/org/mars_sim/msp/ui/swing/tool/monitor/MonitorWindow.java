/*
 * Mars Simulation Project
 * MonitorWindow.java
 * @date 2022-07-02
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ConfigurableWindow;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

/**
 * The MonitorWindow is a tool window that displays a selection of tables each
 * of which monitor a set of Units.
 */
@SuppressWarnings("serial")
public class MonitorWindow extends ToolWindow
			implements ConfigurableWindow, TableModelListener, ActionListener{

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MonitorWindow.class.getName());

	private static final int STATUS_HEIGHT = 25;
	private static final int WIDTH = 1366;
	private static final int HEIGHT = 640;

	public static final String NAME = Msg.getString("MonitorWindow.title"); //$NON-NLS-1$
	public static final String ICON = "monitor";

	// Added an custom icon for each tab
	private static final String COLONY_ICON = "settlement";
	private static final String MARS_ICON = "mars";
	private static final String BOT_ICON = "robot";
	private static final String VEHICLE_ICON = "vehicle";
	private static final String CROP_ICON = "crop";
	private static final String PEOPLE_ICON = "people";
	private static final String BUILDING_ICON = "building"; 

	private static final String TRASH_ICON = "action/trash";
	private static final String LOCATE_ICON = "action/locate";
	private static final String DETAILS_ICON = "details";
	private static final String COLUMN_ICON = "action/column";
	private static final String FILTER_ICON = "action/filter";

	private static final String SETTLEMENT_PROP = "SETTLEMENT";
	private static final String TAB_PROP = "TAB";

	// Data members
	private JTabbedPane tabsSection;
	// Note: may use JideTabbedPane instead
	private JLabel rowCount;
	/** The Tab showing historical events. */
	private EventTab eventsTab;

	private JButton buttonPie;
	private JButton buttonBar;
	private JButton buttonRemoveTab;
	private JButton buttonMap;
	private JButton buttonDetails;
	private JButton buttonFilter;
	private JButton buttonProps;

	/** Settlement Combo box */
	private JComboBox<Settlement> settlementComboBox;
	private JPanel statusPanel;

	private Settlement selectedSettlement;

	private UnitManager unitManager;

	private UnitManagerListener umListener;

	private MonitorTab previousTab;

	/**
	 * Constructor.
	 *
	 * @param desktop the desktop pane
	 */
	public MonitorWindow(MainDesktopPane desktop) {
		// Use TableWindow constructor
		super(NAME, desktop);

		unitManager = desktop.getSimulation().getUnitManager();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout(5, 5));
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Get any saved props
		Properties savedProps = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);

		// Set up settlements
		List<Settlement> initialSettlements = setupSettlements();
		if (!initialSettlements.isEmpty()) {
			String defaultSettlement = (savedProps != null ? savedProps.getProperty(SETTLEMENT_PROP) : null);
			if (defaultSettlement != null) {
				for(Settlement s : initialSettlements) {
					if (s.getName().equals(defaultSettlement)) {
						selectedSettlement = s;
					}
				}
			}

			if (selectedSettlement == null) {
				selectedSettlement = initialSettlements.get(0);
			}
		}

		// Create the settlement combo box
        buildSettlementNameComboBox(initialSettlements);

		// Create top pane
		JPanel topPane = new JPanel(new FlowLayout());
		topPane.add(settlementComboBox);

		mainPane.add(topPane, BorderLayout.NORTH);

		// Create tabbed pane for the table
		tabsSection = new JTabbedPane(SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		// Add all the tabs
		addAllTabs(initialSettlements,
									(savedProps != null ? savedProps.getProperty(TAB_PROP) : null));
		
		// Hide settlement box at startup since the all settlement tab is being selected by default
		setSettlementBox(true);
		
		// Use lambda to add a listener for the tab changes
		// Invoked when player clicks on another tab
		tabsSection.addChangeListener(e -> updateTab());
		
		mainPane.add(tabsSection, BorderLayout.CENTER);

		// Create a status panel
		statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mainPane.add(statusPanel, BorderLayout.SOUTH);
	
		// Add the buttons and row count label at the bottom
		addBottomBar();	
	
		// May use NotificationWindow notifyBox = new NotificationWindow(desktop)
		setResizable(true);
		setMaximizable(true);
		setVisible(true);

		setSize(new Dimension(WIDTH, HEIGHT));	
		setMinimumSize(new Dimension(640, 256));
		
		// Lastly activate the default tab
		selectNewTab(getSelectedTab());
	}

	/**
	 * Adds all the tabs.
	 */
	private void addAllTabs(List<Settlement> initialSettlements, String defaultTabName) {
		List<MonitorTab> newTabs = new ArrayList<>();

		// Add tabs into the table	
		if (initialSettlements.size() > 1) {
			newTabs.add(new UnitTab(this, new SettlementTableModel(), true, MARS_ICON));
		}
		
		newTabs.add(new UnitTab(this, new SettlementTableModel(selectedSettlement), true, COLONY_ICON));
		newTabs.add(new UnitTab(this, new PersonTableModel(selectedSettlement, true), true, PEOPLE_ICON));
		newTabs.add(new UnitTab(this, new RobotTableModel(selectedSettlement, true), true, BOT_ICON));
		newTabs.add(new UnitTab(this, new BuildingTableModel(selectedSettlement), true, BUILDING_ICON));
		newTabs.add(new UnitTab(this, new CropTableModel(selectedSettlement), true, CROP_ICON));
		
		newTabs.add(new FoodInventoryTab(selectedSettlement, this));
		newTabs.add(new BacklogTab(selectedSettlement, this));

		
		newTabs.add(new TradeTab(selectedSettlement, this));
		
		eventsTab = new EventTab(this, desktop);
		newTabs.add(eventsTab);
		
		newTabs.add(new MissionTab(this));
		newTabs.add(new UnitTab(this, new VehicleTableModel(selectedSettlement), true, VEHICLE_ICON));

		// Add the enw tabs an search for default
		for(MonitorTab m : newTabs) {
			addTab(m);
			if (m.getName().equals(defaultTabName)) {
				tabsSection.setSelectedComponent(m);
			}
		}
	}

	/**
	 * Adds the bottom bar.
	 */
	private void addBottomBar() {
		// Prepare row count label
		rowCount = new JLabel("  ");
		rowCount.setPreferredSize(new Dimension(120, STATUS_HEIGHT));
		rowCount.setHorizontalAlignment(SwingConstants.LEFT);
		rowCount.setBorder(BorderFactory.createLoweredBevelBorder());
		statusPanel.add(rowCount);

		// Create graph button
		buttonPie = new JButton(ImageLoader.getIconByName(PieChartTab.ICON));
		buttonPie.setToolTipText(Msg.getString("MonitorWindow.tooltip.singleColumnPieChart")); //$NON-NLS-1$
		buttonPie.addActionListener(this);
		statusPanel.add(buttonPie);

		buttonBar = new JButton(ImageLoader.getIconByName(BarChartTab.ICON));
		buttonBar.setToolTipText(Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart")); //$NON-NLS-1$
		buttonBar.addActionListener(this);
		statusPanel.add(buttonBar);

		buttonRemoveTab = new JButton(ImageLoader.getIconByName(TRASH_ICON)); // $NON-NLS-1$
		buttonRemoveTab.setToolTipText(Msg.getString("MonitorWindow.tooltip.tabRemove")); //$NON-NLS-1$
		buttonRemoveTab.addActionListener(this);
		statusPanel.add(buttonRemoveTab);

		// Create buttons based on selection
		buttonMap = new JButton(ImageLoader.getIconByName(LOCATE_ICON)); // $NON-NLS-1$
		buttonMap.setToolTipText(Msg.getString("MonitorWindow.tooltip.centerMap")); //$NON-NLS-1$
		buttonMap.addActionListener(this);
		statusPanel.add(buttonMap);

		buttonDetails = new JButton(ImageLoader.getIconByName(DETAILS_ICON)); // $NON-NLS-1$
		buttonDetails.setToolTipText(Msg.getString("MonitorWindow.tooltip.showDetails")); //$NON-NLS-1$
		buttonDetails.addActionListener(this);
		statusPanel.add(buttonDetails);

		buttonProps = new JButton(ImageLoader.getIconByName(COLUMN_ICON)); // $NON-NLS-1$
		buttonProps.setToolTipText(Msg.getString("MonitorWindow.tooltip.preferences")); //$NON-NLS-1$
		buttonProps.addActionListener(this);
		statusPanel.add(buttonProps);

		buttonFilter = new JButton(ImageLoader.getIconByName(FILTER_ICON));
		buttonFilter.setToolTipText(Msg.getString("MonitorWindow.tooltip.categoryFilter")); //$NON-NLS-1$
		buttonFilter.addActionListener(this);
		statusPanel.add(buttonFilter);

	}

	/**
	 * This method adds the specified Unit table as a new tab in the Monitor. The
	 * model is displayed as a table by default. The name of the tab is that of the
	 * Model.
	 *
	 * @param model The new model to display.
	 */
	public void displayModel(UnitTableModel<?> model) {
		int index = getModelIndex(model);
		if (index != -1)
			tabsSection.setSelectedIndex(index);
		else {
			logger.severe(model + " not found.");
			try {
				addTab(new UnitTab(this, model, false,"usertab"));
			} catch (Exception e) {
				logger.severe(model + " cannot be added.");
			}
		}
	}

	/**
	 * Sets up a list of settlements.
	 *
	 * @return List<Settlement>
	 */
	private List<Settlement> setupSettlements() {
		List<Settlement> settlements = new ArrayList<>();

		if (GameManager.getGameMode() == GameMode.COMMAND) {
			settlements = unitManager.getCommanderSettlements();
		}

		else if (GameManager.getGameMode() == GameMode.SANDBOX) {
			settlements.addAll(unitManager.getSettlements());
		}

		Collections.sort(settlements);
		
		return settlements;
	}

	/**
	 * Builds the settlement combo box/
	 */
	private void buildSettlementNameComboBox(List<Settlement> startingSettlements) {

		DefaultComboBoxModel<Settlement> model = new DefaultComboBoxModel<>();
		model.addAll(startingSettlements);
		model.setSelectedItem(selectedSettlement);
		settlementComboBox = new JComboBox<>(model);
		settlementComboBox.setOpaque(false);
		settlementComboBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$

		// Set the item listener only after the setup is done
		settlementComboBox.addItemListener(event -> {
			Settlement newSettlement = (Settlement) event.getItem();
			// Change to the selected settlement in SettlementMapPanel
			if (newSettlement != selectedSettlement) {
				setSettlement(newSettlement);
				// Need to update the existing tab
				updateTab();
			}
		});

		// Listen for new Settlements
		umListener = event -> {
			if (event.getEventType() == UnitManagerEventType.ADD_UNIT) {
				settlementComboBox.addItem((Settlement) event.getUnit());
			}
		};
		unitManager.addUnitManagerListener(UnitType.SETTLEMENT, umListener);
	}

	/**
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	private void setSettlement(Settlement s) {
		// Set the selected settlement
		selectedSettlement = s;
		// Set the box opaque
		settlementComboBox.setOpaque(false);
	}

	/**
	 * Sets the opaqueness of the settlement box.
	 * 
	 * @param isOpaque
	 */
	private void setSettlementBox(boolean isOpaque) {
		// Set the box opaque
		settlementComboBox.setOpaque(isOpaque);
		settlementComboBox.setEnabled(!isOpaque);
		settlementComboBox.setVisible(!isOpaque);
	}

	/**
	 * Gets the index of the monitor tab with the model.
	 *
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	private int getModelIndex(UnitTableModel<?> model) {
		for (Component c: tabsSection.getComponents()) {
			if (c instanceof MonitorTab tab) {
				if (model.equals(tab.getModel())) {
					return tabsSection.indexOfComponent(c);
				}
			}
		}
		return -1;
	}

	/**
	 * Creates a bar chart and adds it as a new separate tab.
	 */
	private void createBarChart() {
		MonitorModel model = getSelectedTab().getModel();
		int[]columns = ColumnSelector.createBarSelector(desktop, model);

		if (columns != null && columns.length > 0) {
			MonitorTab bar = new BarChartTab(model, columns);
			addTab(bar);
			tabsSection.setSelectedComponent(bar);
		}
	}

	/**
	 * Creates a pie chart and adds it as a new separate tab.
	 */
	private void createPieChart() {
		MonitorModel model = getSelectedTab().getModel();
		if (model != null) {
			int column = ColumnSelector.createPieSelector(desktop, model);
			if (column >= 0) {
				MonitorTab pie = new PieChartTab(model, column);
				addTab(pie);
				tabsSection.setSelectedComponent(pie);
			}
		}
	}

	/**
	 * Returns the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	public MonitorTab getSelectedTab() {
		Component c = tabsSection.getSelectedComponent();
		if (c != null) {
			return (MonitorTab)c;
		}
		else {
			logger.severe("No tab selected.");
			return null;
		}
	}

	/**
	 * Updates the tab content.
	 */
	private void updateTab() {
		
		MonitorTab selectedTab = getSelectedTab();
		if (selectedTab == null)
			return;
		
		// Continue and recreate a new tab
		selectNewTab(selectedTab);
	}
	
	/**
	 * Selects a new tab.
	 * 
	 * @param selectedTab
	 */
	private void selectNewTab(MonitorTab selectedTab) {
		
		MonitorModel tabTableModel = selectedTab.getModel();

		// Disable all buttons
		boolean enableMap = selectedTab.isNavigatable();
		boolean enableDetails = selectedTab.isEntityDriven();
		boolean enableFilter = selectedTab.isFilterable();
		boolean enableSettlement = tabTableModel.setSettlementFilter(selectedSettlement);
		
		boolean enableBar = false;
		boolean enablePie = false;
		if (selectedTab instanceof TableTab) {
			enableBar = true;
			enablePie = true;
		}

		// Configure the listeners
		boolean activiateListeners = true;
		if (previousTab != null) {
			MonitorModel previousModel = previousTab.getModel();

			// If a different tab then activate listeners
			activiateListeners = !(previousTab.equals(selectedTab));
			if (activiateListeners) {
				previousModel.setMonitorEntites(false);
			}

			// Stop listenering for table size changes
			previousModel.removeTableModelListener(this);
		}
		if (activiateListeners) {
			tabTableModel.setMonitorEntites(true);
		}

		// Listener for row changes
		tabTableModel.addTableModelListener(this);
		previousTab = selectedTab;

		// Update the row count label with new numbers
		rowCount.setText(selectedTab.getCountString());
		
		// Set the opaqueness of the settlement box
		setSettlementBox(!enableSettlement);
		buttonRemoveTab.setEnabled(!selectedTab.isMandatory());
		buttonBar.setEnabled(enableBar);
		buttonPie.setEnabled(enablePie);
		buttonMap.setEnabled(enableMap);
		buttonDetails.setEnabled(enableDetails);
		buttonFilter.setEnabled(enableFilter);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if ((e.getType() == TableModelEvent.INSERT) || (e.getType() == TableModelEvent.DELETE)) {
			// Redisplay row count
			MonitorTab selectedTab = getSelectedTab();
			rowCount.setText(selectedTab.getCountString());
		}
	}

	/**
	 * Adds a new tab to Monitor Tool.
	 *
	 * @param newTab
	 */
	private void addTab(MonitorTab newTab) {
		tabsSection.addTab("", newTab.getIcon(), newTab, newTab.getName());
	}

	/**
	 * Retires a tab from Monitor Tool.
	 *
	 * @param tab
	 */
	private void retireTab(MonitorTab tab) {
		tabsSection.remove(tab);
		tab.removeTab();
	}

	/**
	 * Removes a tab from Monitor Tool.
	 *
	 * @param oldTab
	 */
	private void removeTab(MonitorTab oldTab) {
		retireTab(oldTab);
		if (getSelectedTab() == oldTab) {
			tabsSection.setSelectedIndex(0);
			// Update the row count label
			rowCount.setText("");
		}
	}

	private void centerMap() {
		MonitorTab selected = getSelectedTab();
		if (selected != null) {
			Coordinates place = selected.getSelectedCoordinates();
			if (place != null) {
				desktop.centerMapGlobe(place);
			}
		}
	}

	public void displayDetails() {
		MonitorTab selected = getSelectedTab();
		if (selected != null) {
			selected.displayDetails(desktop);
		}
	}

	private void displayProps() {
		MonitorTab selected = getSelectedTab();
		if (selected != null) {
			selected.displayProps(desktop);
		}
	}

	private void filterCategories() {
		EventTab events = eventsTab;
		if (events != null) {
			events.filterCategories(desktop);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == this.buttonPie) {
			createPieChart();
		} else if (source == this.buttonBar) {
			createBarChart();
		} else if (source == this.buttonRemoveTab) {
			MonitorTab selected = getSelectedTab();
			if (selected != null && !selected.isMandatory()) {
				removeTab(getSelectedTab());
			}
		} else if (source == this.buttonDetails) {
			displayDetails();
		} else if (source == this.buttonMap) {
			centerMap();
		} else if (source == this.buttonProps) {
			displayProps();
		} else if (source == this.buttonFilter) {
			filterCategories();
		}
	}

	/** 
	 * Get the details of which tab is selected
	 */
	@Override
	public Properties getUIProps() {
		Properties result = new Properties();
		result.setProperty(SETTLEMENT_PROP, selectedSettlement.getName());
		result.setProperty(TAB_PROP, getSelectedTab().getName());
		return result;
	}

	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		unitManager.removeUnitManagerListener(UnitType.SETTLEMENT, umListener);
	}
}
