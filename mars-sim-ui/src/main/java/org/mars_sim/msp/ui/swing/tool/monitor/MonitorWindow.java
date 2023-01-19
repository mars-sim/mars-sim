/*
 * Mars Simulation Project
 * MonitorWindow.java
 * @date 2022-07-02
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The MonitorWindow is a tool window that displays a selection of tables each
 * of which monitor a set of Units.
 */
@SuppressWarnings("serial")
public class MonitorWindow extends ToolWindow implements TableModelListener, ActionListener{

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MonitorWindow.class.getName());

	private static final int STATUS_HEIGHT = 25;
	private static final int WIDTH = 1366;
	private static final int HEIGHT = 640;

	public static final String TITLE = Msg.getString("MonitorWindow.title"); //$NON-NLS-1$

	// Added an custom icon for each tab
	public static final String COLONY_ICON = Msg.getString("icon.colony"); //$NON-NLS-1$
	public static final String MARS_ICON = Msg.getString("icon.mars"); //$NON-NLS-1$
	public static final String BOT_ICON = Msg.getString("icon.robot"); //$NON-NLS-1$
	public static final String MISSION_ICON = Msg.getString("icon.mission"); //$NON-NLS-1$
	public static final String VEHICLE_ICON = Msg.getString("icon.vehicle"); //$NON-NLS-1$
	public static final String CROP_ICON = Msg.getString("icon.crop"); //$NON-NLS-1$
	public static final String EVENT_ICON = Msg.getString("icon.event"); //$NON-NLS-1$
	public static final String FOOD_ICON = Msg.getString("icon.food"); //$NON-NLS-1$
	public static final String PEOPLE_ICON = Msg.getString("icon.people"); //$NON-NLS-1$
	public static final String ANALYTICS_ICON = Msg.getString("icon.analytics"); //$NON-NLS-1$
	public static final String TRADE_ICON = Msg.getString("icon.trade"); //$NON-NLS-1$
	public static final String BUILDING_ICON = Msg.getString("icon.building"); //$NON-NLS-1$

	public static final String TRASH_ICON = Msg.getString("icon.trash"); //$NON-NLS-1$
	public static final String LOCATE_ICON = Msg.getString("icon.locate"); //$NON-NLS-1$
	public static final String FIND_ICON = Msg.getString("icon.find"); //$NON-NLS-1$
	public static final String COLUMN_ICON = Msg.getString("icon.column"); //$NON-NLS-1$
	public static final String FILTER_ICON = Msg.getString("icon.filter"); //$NON-NLS-1$

	public static final String BAR_ICON = Msg.getString("icon.bar"); //$NON-NLS-1$
	public static final String PIE_ICON = Msg.getString("icon.pie"); //$NON-NLS-1$

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
	private JButton buttonMissions;
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
		super(TITLE, desktop);

		unitManager = desktop.getSimulation().getUnitManager();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout(5, 5));
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);
		// Create top pane
		JPanel topPane = new JPanel(new GridLayout(1, 5));
		//topPane.setPreferredHeight(30);
		mainPane.add(topPane, BorderLayout.NORTH);

		// Set up settlements
		List<Settlement> initialSettlements = setupSettlements();
		
		// Create the settlement combo box
        buildSettlementNameComboBox(initialSettlements);

		// Create settlement pane
		JPanel settlementPane = new JPanel(new BorderLayout(5, 5));
        settlementPane.setSize(getNameLength() * 14, 30);
		settlementPane.add(settlementComboBox, BorderLayout.CENTER);
		topPane.add(new JPanel());
		topPane.add(new JPanel());
		topPane.add(settlementPane);
		topPane.add(new JPanel());
		topPane.add(new JPanel());

		// Create tabbed pane for the table
		tabsSection = new JTabbedPane(SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		// May choose WRAP_TAB_LAYOUT
		tabsSection.setForeground(Color.DARK_GRAY);
		
		// Add all the tabs
		addAllTabs(initialSettlements);
		
		// Hide settlement box at startup since the all settlement tab is being selected by default
		setSettlementBox(true);
		
		// Use lambda to add a listener for the tab changes
		// Invoked when player clicks on another tab
		tabsSection.addChangeListener(e -> updateTab());
		
		mainPane.add(tabsSection, BorderLayout.CENTER);
		
		// Open the Events tab at the start of the sim
//		May call tabsSection.setSelectedIndex(2)
//		May call table.repaint()

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
		Dimension desktopSize = desktop.getMainWindow().getFrame().getSize();
		Dimension windowSize = getSize();

		int width = (desktopSize.width - windowSize.width) / 2;
		int height = (desktopSize.height - windowSize.height - 100) / 2;
		setLocation(width, height);
		
		// Lastly activate the default tab
		selectNewTab(getSelectedTab());
	}

	/**
	 * Adds all the tabs.
	 */
	private void addAllTabs(List<Settlement> initialSettlements) {
		// Add tabs into the table
		if (!initialSettlements.isEmpty())
			this.selectedSettlement = initialSettlements.get(0);
		
		if (initialSettlements.size() > 1) {
			addTab(new UnitTab(this, new SettlementTableModel(), true, MARS_ICON));
		}
		
		addTab(new UnitTab(this, new SettlementTableModel(selectedSettlement), true, COLONY_ICON));
		addTab(new UnitTab(this, new PersonTableModel(selectedSettlement, true), true, PEOPLE_ICON));
		addTab(new UnitTab(this, new RobotTableModel(selectedSettlement, true), true, BOT_ICON));
		addTab(new UnitTab(this, new BuildingTableModel(selectedSettlement), true, BUILDING_ICON));
		addTab(new UnitTab(this, new CropTableModel(selectedSettlement), true, CROP_ICON));
		
		addTab(new FoodInventoryTab(selectedSettlement, this));
		addTab(new BacklogTab(selectedSettlement, this));

		
		addTab(new TradeTab(selectedSettlement, this));
		
		eventsTab = new EventTab(this, desktop);
		addTab(eventsTab);
		
		addTab(new MissionTab(this));
		addTab(new UnitTab(this, new VehicleTableModel(selectedSettlement), true, VEHICLE_ICON));

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
		buttonPie = new JButton(ImageLoader.getNewIcon(PIE_ICON));
		buttonPie.setToolTipText(Msg.getString("MonitorWindow.tooltip.singleColumnPieChart")); //$NON-NLS-1$
		buttonPie.addActionListener(this);
		statusPanel.add(buttonPie);

		buttonBar = new JButton(ImageLoader.getNewIcon(BAR_ICON));
		buttonBar.setToolTipText(Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart")); //$NON-NLS-1$
		buttonBar.addActionListener(this);
		statusPanel.add(buttonBar);

		buttonRemoveTab = new JButton(ImageLoader.getNewIcon(TRASH_ICON)); // $NON-NLS-1$
		buttonRemoveTab.setToolTipText(Msg.getString("MonitorWindow.tooltip.tabRemove")); //$NON-NLS-1$
		buttonRemoveTab.addActionListener(this);
		statusPanel.add(buttonRemoveTab);

		// Create buttons based on selection
		buttonMap = new JButton(ImageLoader.getNewIcon(LOCATE_ICON)); // $NON-NLS-1$
		buttonMap.setToolTipText(Msg.getString("MonitorWindow.tooltip.centerMap")); //$NON-NLS-1$
		buttonMap.addActionListener(this);
		statusPanel.add(buttonMap);

		buttonDetails = new JButton(ImageLoader.getNewIcon(FIND_ICON)); // $NON-NLS-1$
		buttonDetails.setToolTipText(Msg.getString("MonitorWindow.tooltip.showDetails")); //$NON-NLS-1$
		buttonDetails.addActionListener(this);
		statusPanel.add(buttonDetails);

		buttonMissions = new JButton(ImageLoader.getNewIcon(MISSION_ICON)); // $NON-NLS-1$
		buttonMissions.setToolTipText(Msg.getString("MonitorWindow.tooltip.mission")); //$NON-NLS-1$
		buttonMissions.addActionListener(this);
		statusPanel.add(buttonMissions);

		buttonProps = new JButton(ImageLoader.getNewIcon(COLUMN_ICON)); // $NON-NLS-1$
		buttonProps.setToolTipText(Msg.getString("MonitorWindow.tooltip.preferences")); //$NON-NLS-1$
		buttonProps.addActionListener(this);
		statusPanel.add(buttonProps);

		buttonFilter = new JButton(ImageLoader.getNewIcon(FILTER_ICON));
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
				addTab(new UnitTab(this, model, false, UnitWindow.USER));
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
	@SuppressWarnings("unchecked")
	private void buildSettlementNameComboBox(List<Settlement> startingSettlements) {

		DefaultComboBoxModel<Settlement> model = new DefaultComboBoxModel<>();
		model.addAll(startingSettlements);
		model.setSelectedItem(startingSettlements.get(0));
		settlementComboBox = new JComboBox<>(model);
		settlementComboBox.setSize(getNameLength() * 12, 30);
		settlementComboBox.setOpaque(false);
		settlementComboBox.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		settlementComboBox.setForeground(Color.ORANGE.darker());
		settlementComboBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementComboBox.setRenderer(new PromptComboBoxRenderer());

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
     * Gets the length of the most lengthy settlement name/
     *
     * @return
     */
    private int getNameLength() {
    	Collection<Settlement> list = unitManager.getSettlements();
    	int max = 12;
    	for (Settlement s: list) {
    		int size = s.getName().length();
    		if (max < size)
    			max = size;
    	}
    	return max;
    }

	/**
	 * Gets the index of the monitor tab with the model.
	 *
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	public int getModelIndex(UnitTableModel<?> model) {
		for (Component c: tabsSection.getComponents()) {
			if (c instanceof MonitorTab) {
				MonitorTab tab = (MonitorTab)c;
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
		
		// Disable all buttons
		boolean enableMap = false;
		boolean enableDetails = false;
		boolean enableMission = false;
		boolean enableFilter = false;
		boolean enableSettlement = true;
		
		MonitorModel tabTableModel = selectedTab.getModel();

		if (selectedTab instanceof UnitTab) {
			// Enable these buttons
			enableDetails = true;
			enableMap = true;

			// Is select by Settlement supported ?
			enableSettlement = tabTableModel.setSettlementFilter(selectedSettlement);
			
		}
		else if (selectedTab instanceof MissionTab) {
			// Enable these buttons
			enableDetails = true;
			enableMission = true;

			// Hide the settlement box
			enableSettlement = false;

		}
		else if (selectedTab instanceof EventTab) {
			// Enable these buttons
			enableDetails = true;
			enableFilter = true;

			// Hide the settlement box
			enableSettlement = false;
		}
		else if (selectedTab instanceof BacklogTab) {
			tabTableModel.setSettlementFilter(selectedSettlement);
		}
		else if (selectedTab instanceof FoodInventoryTab) {
			tabTableModel.setSettlementFilter(selectedSettlement);

		} else if (selectedTab instanceof TradeTab) {
			// Enable these buttons
			enableFilter = true;
			
			int rowIndex = ((TradeTab)selectedTab).getTable().getSelectedRow();
			tabTableModel.setSettlementFilter(selectedSettlement);

			scrollToVisible(((TradeTab)selectedTab).getTable(), rowIndex, 0);
		}
		else {
			// Hide the settlement box
			enableSettlement = false;
		}

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
		buttonRemoveTab.setEnabled(!selectedTab.getMandatory());
		buttonBar.setEnabled(enableBar);
		buttonPie.setEnabled(enablePie);
		buttonMap.setEnabled(enableMap);
		buttonDetails.setEnabled(enableDetails);
		buttonMissions.setEnabled(enableMission);
		buttonFilter.setEnabled(enableFilter);
	}

	/**
	 * Scrolls the mouse cursor to a particular row and column.
	 * 
	 * @param table
	 * @param rowIndex
	 * @param vColIndex
	 */
	private static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        
        table.scrollRectToVisible(table.getCellRect(rowIndex, vColIndex, true));
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
			selected.centerMap(desktop);
		}
	}

	public void displayDetails() {
		MonitorTab selected = getSelectedTab();
		if (selected != null) {
			selected.displayDetails(desktop);
		}
	}

	/**
	 * Finds and highlights the mission in Mission Tool.
	 */
	private void displayMission() {
		MonitorTab selected = getSelectedTab();
		if (selected instanceof MissionTab) {
			List<?> rows = selected.getSelection();
			Iterator<?> it = rows.iterator();
			while (it.hasNext()) {
				Object row = it.next();
				if (row instanceof Mission) {
					((MissionTab) selected).displayMission(desktop, (Mission) row);
				}
			}
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
			if (selected != null && !selected.getMandatory()) {
				removeTab(getSelectedTab());
			}
		} else if (source == this.buttonDetails) {
			displayDetails();
		} else if (source == this.buttonMap) {
			centerMap();
		} else if (source == this.buttonMissions) {
			displayMission();
		} else if (source == this.buttonProps) {
			displayProps();
		} else if (source == this.buttonFilter) {
			filterCategories();
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

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;

		public PromptComboBoxRenderer(){
		    setHorizontalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
	            int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(prompt);
				return this;
			}

			if (isSelected) {
	        	  c.setForeground(Color.black);
	        	  c.setBackground(new Color(255,229,204,50)); // pale orange
	          } else {
					c.setForeground(Color.black);
			        c.setBackground(new Color(184,134,11,50)); // mud orange
	          }

	        return c;
	    }
	}
}
