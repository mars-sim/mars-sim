/*
 * Mars Simulation Project
 * MonitorWindow.java
 * @date 2025-07-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.mars_sim.core.Entity;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEventType;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.MapSelector;
import com.mars_sim.ui.swing.utils.SortedComboBoxModel;

/**
 * The MonitorWindow is a tool window that displays a selection of tables each
 * of which monitor a set of Units.
 */
@SuppressWarnings("serial")
public class MonitorWindow extends ContentPanel
			implements ConfigurableWindow, TableModelListener{

	/**
	 * This is a comparator that sorts in the following order:
	 * 1) String
	 * 2) Authority
	 * 3) Settlement
	 */
	private static class SelectionComparator implements Comparator<Object> {

		@Override
		public int compare(Object o1, Object o2) {
			// String are always first
			if (o1 instanceof String) {
				return -1;
			}
			else if (o2 instanceof String) {
				return 1;
			}
			if ((o1 instanceof Settlement) && (o2 instanceof Authority)) {
				return 1;
			}
			else if ((o1 instanceof Authority) && (o2 instanceof Settlement)) {
				return -1;
			}
			else if ((o1 instanceof Entity e1) && (o2 instanceof Entity e2)) {
				return e1.getName().compareTo(e2.getName());
			}

			// Should never get here
			return 0;
		}
	}

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MonitorWindow.class.getName());

	private static final int STATUS_HEIGHT = 25;
	private static final int WIDTH = 1366;
	private static final int HEIGHT = 640;

	private static final String ALL = "All";
	public static final String NAME = "monitor";
	public static final String ICON = "monitor";
    public static final String TITLE = Msg.getString("MonitorWindow.title");

	// Added an custom icon for each tab
	private static final String COLONY_ICON = "settlement";
	private static final String BOT_ICON = "robot";
	private static final String VEHICLE_ICON = "vehicle";
	private static final String CROP_ICON = "crop";
	private static final String FOOD_ICON = "food";
	private static final String TRADE_ICON = "trade";
	private static final String PEOPLE_ICON = "people";
	private static final String BUILDING_ICON = "building"; 
	private static final String TASK_ICON = "task";
	private static final String TRASH_ICON = "action/trash";
	private static final String LOCATE_ICON = "action/locate";
	private static final String DETAILS_ICON = "action/details";
	private static final String COLUMN_ICON = "action/column";
	private static final String FILTER_ICON = "action/filter";

	private static final String FILTER_PROP = "FILTER";
	private static final String TAB_PROP = "TAB";

	// Data members
	private JTabbedPane tabsSection;
	// Note: may use JideTabbedPane instead
	private JLabel rowCount;
	/** The Tab showing historical events. */
	private EventTab eventsTab;

	private JButton buttonProps;
	private JButton buttonPie;
	private JButton buttonBar;
	private JButton buttonRemoveTab;
	private JButton buttonMap;
	private JButton buttonDetails;
	private JButton buttonFilter;
		
	protected JCheckBox aliveB;
	protected JCheckBox deceasedB;
	protected JCheckBox buriedB;
	protected ButtonGroup group = new ButtonGroup();
	
	/** Selection Combo box */
	private JComboBox<Object> selectionCombo;
	
	private JPanel statusPanel;
	
	private JPanel choosePanel;

	private Set<Settlement> currentSelection;

	private UnitManager unitManager;

	private UnitManagerListener umListener;

	private MonitorTab activeTab;

	private Map<Authority, Set<Settlement>> authorities;

	private JLabel selectionDescription;

	private UIContext context;

	/**
	 * Constructor.
	 *
	 * @param desktop the desktop pane
	 */
	public MonitorWindow(UIContext context, Properties uiProps) {
		// Use TableWindow constructor
		super(NAME, TITLE, Placement.BOTTOM);
		
		this.context = context;

		unitManager = context.getSimulation().getUnitManager();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout(5, 5));
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);

		// Set up default selection
		var choices = setupSelectionChoices();
		Object defaultSelection = ALL;
		String previousChoice = (uiProps != null ? uiProps.getProperty(FILTER_PROP) : null);
		if (previousChoice != null) {
			for(Entity s : choices) {
				if (s.getName().equals(previousChoice)) {
					defaultSelection = s;
				}
			}
		}

		// Create the settlement combo box
        buildSelectionCombo(choices, defaultSelection);

		// Create top pane
		JPanel topPane = new JPanel(new FlowLayout());
		topPane.add(new JLabel("Settlement Filter:"));
		topPane.add(selectionCombo);
		selectionDescription = new JLabel("");
		topPane.add(selectionDescription);

		mainPane.add(topPane, BorderLayout.NORTH);
		
		// Update the selection description
		applySelection(defaultSelection);

		// Create tabbed pane for the table
		tabsSection = new JTabbedPane(SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		// Add all the tabs
		addAllTabs(choices,
					(uiProps != null ? uiProps.getProperty(TAB_PROP) : null));
		
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

		var dim = new Dimension(WIDTH, HEIGHT);
		setSize(dim);
		setPreferredSize(dim);	
		setMinimumSize(new Dimension(640, 256));
		
		// Lastly activate the default tab
		selectNewTab(getSelectedTab());
	}

	/**
	 * Adds all the tabs.
	 */
	private void addAllTabs(List<Entity> choices, String defaultTabName) {
		List<MonitorTab> newTabs = new ArrayList<>();

		// Add tabs into the table	
		newTabs.add(new UnitTab(this, new SettlementTableModel(), true, COLONY_ICON));
		newTabs.add(new UnitTab(this, new PersonTableModel(), true, PEOPLE_ICON));
		newTabs.add(new UnitTab(this, new RobotTableModel(), true, BOT_ICON));
		newTabs.add(new UnitTab(this, new BuildingTableModel(), true, BUILDING_ICON));
		newTabs.add(new UnitTab(this, new CropTableModel(context.getSimulation().getConfig()), true, CROP_ICON));
		
		newTabs.add(new TableTab(this, new FoodTableModel(), true, false, FOOD_ICON));

		newTabs.add(new TableTab(this, new BacklogTableModel(), true, false, TASK_ICON));
		
		newTabs.add(new TableTab(this, new TradeTableModel(), true, false, TRADE_ICON));

		// Create eventsTab instance
		eventsTab = new EventTab(this, context);
		newTabs.add(eventsTab);
		
		newTabs.add(new MissionTab(this, context.getSimulation()));
		newTabs.add(new UnitTab(this, new VehicleTableModel(), true, VEHICLE_ICON));

		for (MonitorTab m : newTabs) {
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
		buttonPie.addActionListener(e -> createPieChart());
		statusPanel.add(buttonPie);

		buttonBar = new JButton(ImageLoader.getIconByName(BarChartTab.ICON));
		buttonBar.setToolTipText(Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart")); //$NON-NLS-1$
		buttonBar.addActionListener(e ->createBarChart());
		statusPanel.add(buttonBar);

		buttonRemoveTab = new JButton(ImageLoader.getIconByName(TRASH_ICON)); // $NON-NLS-1$
		buttonRemoveTab.setToolTipText(Msg.getString("MonitorWindow.tooltip.tabRemove")); //$NON-NLS-1$
		buttonRemoveTab.addActionListener(e -> removeSelectedTab());
		statusPanel.add(buttonRemoveTab);

		// Create buttons based on selection
		buttonMap = new JButton(ImageLoader.getIconByName(LOCATE_ICON)); // $NON-NLS-1$
		buttonMap.setToolTipText(Msg.getString("EntityLabel.locate")); //$NON-NLS-1$
		buttonMap.addActionListener(e -> centerMap());
		statusPanel.add(buttonMap);

		buttonDetails = new JButton(ImageLoader.getIconByName(DETAILS_ICON)); // $NON-NLS-1$
		buttonDetails.setToolTipText(Msg.getString("EntityLabel.details")); //$NON-NLS-1$
		buttonDetails.addActionListener(e -> displayDetails());
		statusPanel.add(buttonDetails);

		buttonProps = new JButton(ImageLoader.getIconByName(COLUMN_ICON)); // $NON-NLS-1$
		buttonProps.setToolTipText(Msg.getString("MonitorWindow.tooltip.preferences")); //$NON-NLS-1$
		buttonProps.addActionListener(e -> displayProps());
		statusPanel.add(buttonProps);

		buttonFilter = new JButton(ImageLoader.getIconByName(FILTER_ICON));
		buttonFilter.setToolTipText(Msg.getString("MonitorWindow.tooltip.categoryFilter")); //$NON-NLS-1$
		buttonFilter.addActionListener(e -> filterCategories());
		statusPanel.add(buttonFilter);

		statusPanel.add(new JSeparator(SwingConstants.VERTICAL));

		choosePanel = new JPanel(new GridLayout(1, 3, 0, 0));			
		choosePanel.setBorder(StyleManager.createLabelBorder("Types:"));	
		statusPanel.add(choosePanel, BorderLayout.CENTER);
		
		// Displays the live citizens 
		aliveB = new JCheckBox("Alive", true);
		aliveB.setBorder(BorderFactory.createLoweredBevelBorder());
		aliveB.setToolTipText("Display the live citizens in this settlement"); //$NON-NLS-1$
		aliveB.addActionListener(e -> displayLive());
		choosePanel.add(aliveB);
		
		// Displays the deceased citizens 
		deceasedB = new JCheckBox("Deceased", false);
		deceasedB.setBorder(BorderFactory.createLoweredBevelBorder());
		deceasedB.setToolTipText("Display the deceased citizens in this settlement"); //$NON-NLS-1$
		deceasedB.addActionListener(e -> displayDeceased());
		choosePanel.add(deceasedB);
		
		// Displays the buried citizens 
		buriedB = new JCheckBox("Buried", false);
		buriedB.setBorder(BorderFactory.createLoweredBevelBorder());
		buriedB.setToolTipText("Display the buried citizens in this settlement"); //$NON-NLS-1$
		buriedB.addActionListener(e -> displayBuried());
		choosePanel.add(buriedB);
		
		group.add(aliveB);
		group.add(deceasedB);
		group.add(buriedB);
	}

	/**
	 * This method adds the specified Unit table as a new tab in the Monitor. The
	 * model is displayed as a table by default. The name of the tab is that of the
	 * Model.
	 *
	 * @param model The new model to display.
	 */
	public void displayModel(EntityMonitorModel<?> model) {
		int index = getModelIndex(model);
		if (index != -1)
			tabsSection.setSelectedIndex(index);
		else {
			try {
				UnitTab newTab = new UnitTab(this, model, false,"usertab");
				addTab(newTab);
				tabsSection.setSelectedComponent(newTab);
			} catch (Exception e) {
				logger.severe(model + " cannot be added.");
			}
		}
	}

	/**
	 * Sets up a list of settlements and associated authorities.
	 *
	 * @return Map of authority to settlements
	 */
	private List<Entity> setupSelectionChoices() {

		Collection<Settlement> settlements;
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			settlements = unitManager.getCommanderSettlements();
		}
		else { 
			settlements = unitManager.getSettlements();
		}
		List<Entity> choices = new ArrayList<>(settlements);
		
		// Create the Authority maps
		authorities = new HashMap<>();
		for (var s : settlements) {
			var ra = s.getReportingAuthority();
			authorities.computeIfAbsent(ra, k -> new HashSet<>()).add(s);
		}	

		choices.addAll(authorities.keySet());

		return choices;
	}

	/**
	 * Builds the settlement combo box that uses the settlements and reporting authorities.
	 * 
	 * @param choices
	 * @param selected
	 */
	private void buildSelectionCombo(List<Entity> choices, Object selected) {
		List<Object> converted = new ArrayList<>(choices); // List is a pain

		SortedComboBoxModel<Object> model = new SortedComboBoxModel<>(converted, new SelectionComparator());
		model.addElement(ALL);
		model.setSelectedItem(selected);
		selectionCombo = new JComboBox<>(model);
		selectionCombo.setOpaque(false);
		selectionCombo.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		selectionCombo.setPreferredSize(new Dimension(200, 25));
	
		// Add renderer
		selectionCombo.setRenderer(new SelectionComboRenderer());
		
		// Set the item listener only after the setup is done
		selectionCombo.addItemListener(this::changeSelection);

		// Listen for new Settlements
		umListener = event -> {
			if (event.getEventType() == UnitManagerEventType.ADD_UNIT) {
				addNewSettlement(event.getUnit());
			}
		};
		unitManager.addUnitManagerListener(UnitType.SETTLEMENT, umListener);

	}

	/**
	 * Adds new settlement to the selection and updates the Reporting Authority.
	 * 
	 * @param unit
	 */
	private void addNewSettlement(Unit unit) {
		if (unit instanceof Settlement s) {
			SortedComboBoxModel<Object> ms = (SortedComboBoxModel<Object>) selectionCombo.getModel();
			ms.addElement(s);

			var ra = s.getReportingAuthority();
			if (!authorities.containsKey(ra)) {
				authorities.put(ra, new HashSet<>());
				ms.addElement(ra);
			}
			authorities.get(ra).add(s);

			// Force a refresh in case new Settlement should be displayed
			updateTab();
		}
	}

	/**
	 * Reacts to a change in the Combo selection. 
	 * 
	 * @param event
	 */
	private void changeSelection(ItemEvent event) {
		applySelection(event.getItem());
		updateTab();
	}
	
	/**
	 * Updates the internal selection status based on a new item.
	 * 
	 * @param item
	 */
	private void applySelection(Object item) {
		String newDescription = "";
		Set<Settlement> newSelection = null;

		if (item instanceof Settlement s) {
			newSelection = Set.of(s);
		}
		else if (item instanceof Authority a) {
			newSelection = authorities.get(a);
			if (newSelection != null) {
				newDescription = newSelection.stream()
									.map(Settlement::getName)
									.sorted()
									.collect(Collectors.joining(", ", "(", ")"));
			}
		}
		else if (item instanceof String str) {
			if (!str.equals(ALL)) {
				// Should always be ALL
				return;
			}
			newSelection = new HashSet<>(unitManager.getSettlements());
		}

		// Change to the selection, must be read only as it is shared
		currentSelection = Collections.unmodifiableSet(newSelection);
		selectionDescription.setText(newDescription);
	}

	/**
	 * Sets the opaqueness of the settlement box.
	 * 
	 * @param isOpaque
	 */
	private void setSettlementBox(boolean isOpaque) {
		// Set the box opaque
		selectionCombo.setOpaque(isOpaque);
		selectionCombo.setEnabled(!isOpaque);
		selectionCombo.setVisible(!isOpaque);
		selectionDescription.setVisible(!isOpaque);
	}

	/**
	 * Gets the index of the monitor tab with the model.
	 *
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	private int getModelIndex(EntityMonitorModel<?> model) {
		for (Component c: tabsSection.getComponents()) {
			if ((c instanceof MonitorTab tab) && model.equals(tab.getModel())) {
				return tabsSection.indexOfComponent(c);
			}
		}
		return -1;
	}

	/**
	 * Creates a bar chart and adds it as a new separate tab.
	 */
	private void createBarChart() {
		MonitorModel model = getSelectedTab().getModel();
		int[]columns = ColumnSelector.createBarSelector(context.getTopFrame(), model);

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
			int column = ColumnSelector.createPieSelector(context.getTopFrame(), model);
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

		String title = tabTableModel.getName();
		
		super.setTitle(TITLE + " - " + title);
		
		boolean enableRemove = !selectedTab.isMandatory();
		boolean enableMap = selectedTab.isNavigatable();
		boolean enableDetails = selectedTab.isEntityDriven();
		boolean enableFilter = selectedTab.isFilterable();
		boolean enableSettlement = false;
		boolean enableBar = false;
		boolean enablePie = false;
		
		if (selectedTab instanceof TableTab tt) {
			enableBar = true;
			enablePie = true;
			enableSettlement = tt.setSettlementFilter(currentSelection);
		}

		// Configure the listeners
		boolean activiateListeners = true;
		if (activeTab != null) {
			MonitorModel previousModel = activeTab.getModel();

			// If a different tab then activate listeners
			activiateListeners = !(activeTab.equals(selectedTab));
			if (activiateListeners) {
				previousModel.setMonitorEntites(false);
			}

			// Stop listening for table size changes
			previousModel.removeTableModelListener(this);
		}
		if (activiateListeners) {
			tabTableModel.setMonitorEntites(true);
		}

		// Listener for row changes
		tabTableModel.addTableModelListener(this);
		activeTab = selectedTab;

		// Update the row count label with new numbers
		rowCount.setText(selectedTab.getCountString());
		
		// Set the opaqueness of the settlement box
		setSettlementBox(!enableSettlement);
		buttonRemoveTab.setEnabled(enableRemove);
		buttonBar.setEnabled(enableBar);
		buttonPie.setEnabled(enablePie);
		buttonMap.setEnabled(enableMap);
		buttonDetails.setEnabled(enableDetails);
		buttonFilter.setEnabled(enableFilter);
		buttonProps.setEnabled(!enableRemove);
		
		if (tabTableModel instanceof PersonTableModel) {
			choosePanel.setVisible(true);
			aliveB.setVisible(true);
			deceasedB.setVisible(true);
			buriedB.setVisible(true);
		}
		else {
			choosePanel.setVisible(false);
			aliveB.setVisible(false);
			deceasedB.setVisible(false);
			buriedB.setVisible(false);
		}
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
	 * Removes a tab from Monitor Tool.
	 *
	 * @param oldTab
	 */
	private void removeTab(MonitorTab oldTab) {
		tabsSection.remove(oldTab);
		oldTab.removeTab();

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
				MapSelector.displayCoords(context, place);
			}
		}
	}

	void displayDetails() {
		MonitorTab selected = getSelectedTab();
		if (selected instanceof TableTab tt) {
			for (Object row : tt.getSelection()) {
				if (row instanceof Entity ent) {
					context.showDetails(ent);
				}
			}
		}
	}

	private void displayProps() {
		MonitorTab selected = getSelectedTab();
		if (selected != null) {
			selected.displayProps(context);
		}
	}

	private void filterCategories() {
		EventTab events = eventsTab;
		if (events != null) {
			events.filterCategories(context);
		}
	}

	/**
	 * Removes selected tab.
	 */
	private void removeSelectedTab() {
		MonitorTab selected = getSelectedTab();
		if (selected != null && !selected.isMandatory()) {
			removeTab(getSelectedTab());
		}
	}
	
	/**
	 * Displays the live citizens.
	 */
	private void displayLive() {
		boolean alive = aliveB.isSelected();
		MonitorTab selectedTab = getSelectedTab();
		MonitorModel tabTableModel = selectedTab.getModel();
		if (tabTableModel instanceof PersonTableModel model) {
			if (alive)
				group.clearSelection();
			aliveB.setSelected(alive);
			model.showAlive(alive);
			// refresh the tab
			selectNewTab(selectedTab);
		}
	}
	
	/**
	 * Displays the already-deceased personnel.
	 */
	private void displayDeceased() {
		boolean deceased = deceasedB.isSelected();
		MonitorTab selectedTab = getSelectedTab();
		MonitorModel tabTableModel = selectedTab.getModel();
		if (tabTableModel instanceof PersonTableModel model) {
			if (deceased)
				group.clearSelection();
			deceasedB.setSelected(deceased);
			model.showDeceased(deceased);
			// refresh the tab
			selectNewTab(selectedTab);
		}
	}
		
	/**
	 * Displays the buried citizens.
	 */
	private void displayBuried() {
		boolean buried = buriedB.isSelected();
		MonitorTab selectedTab = getSelectedTab();
		MonitorModel tabTableModel = selectedTab.getModel();
		if (tabTableModel instanceof PersonTableModel model) {
			if (buried)
				group.clearSelection();
			buriedB.setSelected(buried);
			model.showBuried(buried);		
			// refresh the tab
			selectNewTab(selectedTab);
		}
	}
	
	/** 
	 * Gets the details of which tab is selected.
	 */
	@Override
	public Properties getUIProps() {
		Properties result = new Properties();
		Object e = selectionCombo.getSelectedItem();
		if (e instanceof Entity ent) {
			result.setProperty(FILTER_PROP, ent.getName());
		}

		result.setProperty(TAB_PROP, getSelectedTab().getName());
	
		return result;
	}
	
	private class SelectionComboRenderer extends JLabel implements
        ListCellRenderer<Object> {

		public SelectionComboRenderer() {

			setOpaque(true);
			setVerticalAlignment(CENTER);

		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Object> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			// Center horizontally
			setHorizontalAlignment(CENTER); 
			
			this.setFont(list.getFont());

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			
			if (value instanceof Settlement s) {
				this.setText(s.getName());
			}
			else if (value instanceof Authority a) {
				var children = authorities.get(a);
				this.setText(a.getName() + " (" + (children != null ? children.size() : 0) + ")");
			}
			else if (value instanceof String s) {
				this.setText(s);
            }
			
			return this;
		}
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
