/*
 * Mars Simulation Project
 * MonitorWindow.java
 * @date 2021-12-06
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.RowNumberTable;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;


/**
 * The MonitorWindow is a tool window that displays a selection of tables each
 * of which monitor a set of Units.
 */
@SuppressWarnings("serial")
public class MonitorWindow extends ToolWindow implements TableModelListener, ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MonitorWindow.class.getName());

	private static final int STATUS_HEIGHT = 25;
	private static final int WIDTH = 1366;
	private static final int HEIGHT = 480;

	public static final String TITLE = Msg.getString("MonitorWindow.title"); //$NON-NLS-1$

	// Added an custom icon for each tab
	public static final String COLONY_ICON = Msg.getString("icon.colony"); //$NON-NLS-1$
	public static final String MARS_ICON = Msg.getString("icon.mars"); //$NON-NLS-1$
	public static final String BOT_ICON = Msg.getString("icon.bot"); //$NON-NLS-1$
	public static final String MISSION_ICON = Msg.getString("icon.mission"); //$NON-NLS-1$
	public static final String VEHICLE_ICON = Msg.getString("icon.vehicle"); //$NON-NLS-1$
	public static final String CROP_ICON = Msg.getString("icon.crop"); //$NON-NLS-1$
	public static final String EVENT_ICON = Msg.getString("icon.event"); //$NON-NLS-1$
	public static final String FOOD_ICON = Msg.getString("icon.food"); //$NON-NLS-1$
	public static final String PEOPLE_ICON = Msg.getString("icon.people"); //$NON-NLS-1$
	public static final String ANALYTICS_ICON = Msg.getString("icon.analytics"); //$NON-NLS-1$
	public static final String TRADE_ICON = Msg.getString("icon.trade"); //$NON-NLS-1$
	public static final String BUILDING_ICON = Msg.getString("icon.building"); //$NON-NLS-1$;

	public static final String TRASH_ICON = Msg.getString("icon.trash"); //$NON-NLS-1$
	public static final String CENTERMAP_ICON = Msg.getString("icon.centermap"); //$NON-NLS-1$
	public static final String FIND_ICON = Msg.getString("icon.find"); //$NON-NLS-1$
	public static final String COLUMN_ICON = Msg.getString("icon.column"); //$NON-NLS-1$
	public static final String FILTER_ICON = Msg.getString("icon.filter"); //$NON-NLS-1$

	public static final String BAR_ICON = Msg.getString("icon.bar"); //$NON-NLS-1$
	public static final String PIE_ICON = Msg.getString("icon.pie"); //$NON-NLS-1$

	// Data members
	private WebTabbedPane tabsSection;
	// Note: may use JideTabbedPane instead
	private WebLabel rowCount;
	/** Tab showing historical events. */
	private EventTab eventsTab;
	private WebButton buttonPie;
	private WebButton buttonBar;
	private WebButton buttonRemoveTab;
	private WebButton buttonMap;
	private WebButton buttonDetails;
	private WebButton buttonMissions;
	private WebButton buttonFilter;
	private WebButton buttonProps;

	/** Settlement Combo box */
	private WebComboBox settlementListBox;
	private WebPanel statusPanel;

	private JTable table;
	private JTable rowTable;

	private Settlement selectedSettlement;
	private List<Settlement> settlementList;

	private UnitManager unitManager;

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
		WebPanel mainPane = new WebPanel(new BorderLayout(5, 5));
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);
		// Create top pane
		WebPanel topPane = new WebPanel(new GridLayout(1, 5));
		topPane.setPreferredHeight(30);
		mainPane.add(topPane, BorderLayout.NORTH);

		// Create the settlement combo box
        buildSettlementNameComboBox();

		// Create settlement pane
		WebPanel settlementPane = new WebPanel(new BorderLayout(5, 5));
        settlementPane.setSize(getNameLength() * 14, 30);
		settlementPane.add(settlementListBox, BorderLayout.CENTER);
		topPane.add(new JPanel());
		topPane.add(new JPanel());
		topPane.add(settlementPane);
		topPane.add(new JPanel());
		topPane.add(new JPanel());

		// Create tabbed pane for the table
		tabsSection = new WebTabbedPane(StyleId.tabbedpane, SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		// May choose WRAP_TAB_LAYOUT
		tabsSection.setForeground(Color.DARK_GRAY);
		// Add all the tabs
		addAllTabs();
		
//		Note: may use lambda tabsSection.addChangeListener(e -> updateTab())
		
		// Add a listener for the tab changes
		tabsSection.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTab();
			}
		});
					
		mainPane.add(tabsSection, BorderLayout.CENTER);
		
		// Open the Events tab at the start of the sim
//		May call tabsSection.setSelectedIndex(2)
//		May call table.repaint()

		// Create a status panel
		statusPanel = new WebPanel();
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
	}

	/**
	 * Adds all the tabs
	 */
	public void addAllTabs() {
		// Add tabs into the table
		try {
			if (getSettlements().size() > 1) {
				addTab(new UnitTab(this, new SettlementTableModel(), true, MARS_ICON));
			}

			addTab(new UnitTab(this, new SettlementTableModel(selectedSettlement), true, COLONY_ICON));
			addTab(new UnitTab(this, new PersonTableModel(selectedSettlement, true), true, PEOPLE_ICON));
			addTab(new UnitTab(this, new RobotTableModel(selectedSettlement), true, BOT_ICON));
			
			addTab(new UnitTab(this, new BuildingTableModel(selectedSettlement), true, BUILDING_ICON));
			
			addTab(new UnitTab(this, new CropTableModel(selectedSettlement), true, CROP_ICON));
			
			eventsTab = new EventTab(this, desktop);
			addTab(eventsTab);
			
			addTab(new FoodInventoryTab(this));
			addTab(new TradeTab(this));
			addTab(new MissionTab(this));
			addTab(new UnitTab(this, new VehicleTableModel(selectedSettlement), true, VEHICLE_ICON));

		} catch (Exception e) {
			logger.severe("Problems in adding tabs in MonitorWindow: " + e.getMessage());
		}
	}

	/**
	 * Adds the bottom bar
	 */
	public void addBottomBar() {
		// Prepare row count label
		rowCount = new WebLabel("  ");
		rowCount.setPreferredSize(new Dimension(120, STATUS_HEIGHT));
		rowCount.setHorizontalAlignment(SwingConstants.LEFT);
		rowCount.setBorder(BorderFactory.createLoweredBevelBorder());
		statusPanel.add(rowCount);

		// Create graph button
		buttonPie = new WebButton(ImageLoader.getNewIcon(PIE_ICON));
		TooltipManager.setTooltip(buttonPie, Msg.getString("MonitorWindow.tooltip.singleColumnPieChart"), //$NON-NLS-1$
				TooltipWay.up);
		buttonPie.addActionListener(this);
		statusPanel.add(buttonPie);

		buttonBar = new WebButton(ImageLoader.getNewIcon(BAR_ICON));
		TooltipManager.setTooltip(buttonBar, Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart"), //$NON-NLS-1$
				TooltipWay.up);
		buttonBar.addActionListener(this);
		statusPanel.add(buttonBar);

		buttonRemoveTab = new WebButton(ImageLoader.getNewIcon(TRASH_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonRemoveTab, Msg.getString("MonitorWindow.tooltip.tabRemove"), //$NON-NLS-1$
				TooltipWay.up);
		buttonRemoveTab.addActionListener(this);
		statusPanel.add(buttonRemoveTab);

		// Create buttons based on selection
		buttonMap = new WebButton(ImageLoader.getNewIcon(CENTERMAP_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonMap, Msg.getString("MonitorWindow.tooltip.centerMap"), TooltipWay.up); //$NON-NLS-1$
		buttonMap.addActionListener(this);
		statusPanel.add(buttonMap);

		buttonDetails = new WebButton(ImageLoader.getNewIcon(FIND_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonDetails, Msg.getString("MonitorWindow.tooltip.showDetails"), TooltipWay.up); //$NON-NLS-1$
		buttonDetails.addActionListener(this);
		statusPanel.add(buttonDetails);

		buttonMissions = new WebButton(ImageLoader.getNewIcon(MISSION_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonMissions, Msg.getString("MonitorWindow.tooltip.mission"), TooltipWay.up); //$NON-NLS-1$
		buttonMissions.addActionListener(this);
		statusPanel.add(buttonMissions);

		buttonProps = new WebButton(ImageLoader.getNewIcon(COLUMN_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonProps, Msg.getString("MonitorWindow.tooltip.preferences"), TooltipWay.up); //$NON-NLS-1$
		buttonProps.addActionListener(this);
		statusPanel.add(buttonProps);

		buttonFilter = new WebButton(ImageLoader.getNewIcon(FILTER_ICON));
		TooltipManager.setTooltip(buttonFilter, Msg.getString("MonitorWindow.tooltip.categoryFilter"), TooltipWay.up); //$NON-NLS-1$
		buttonFilter.addActionListener(this);
		statusPanel.add(buttonFilter);

	}

	/**
	 * This method add the specified Unit table as a new tab in the Monitor. The
	 * model is displayed as a table by default. The name of the tab is that of the
	 * Model.
	 *
	 * @param model The new model to display.
	 */
	public void displayModel(UnitTableModel model) {
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
	 * Gets a list of sponsored settlements
	 *
	 * @return List<Settlement>
	 */
	public List<Settlement> getSettlements() {
		if (settlementList == null) {
			List<Settlement> settlements = new ArrayList<>();

			if (GameManager.getGameMode() == GameMode.COMMAND) {
				settlements = unitManager.getCommanderSettlements();
			}

			else if (GameManager.getGameMode() == GameMode.SANDBOX) {
				settlements.addAll(unitManager.getSettlements());
			}

			Collections.sort(settlements);
			settlementList = settlements;
		}

		return settlementList;
	}

	/**
	 * Builds the settlement combo box
	 */
	@SuppressWarnings("unchecked")
	public void buildSettlementNameComboBox() {

		settlementListBox = new WebComboBox(StyleId.comboboxHover, getSettlements());
		settlementListBox.setWidePopup(true);
		settlementListBox.setSize(getNameLength() * 12, 30);
		settlementListBox.setBackground(new Color(205, 133, 63, 128));// (51, 35, 0, 128) is dull gold color
		settlementListBox.setOpaque(false);
		settlementListBox.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		settlementListBox.setForeground(Color.ORANGE.darker());
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementListBox.setRenderer(new PromptComboBoxRenderer());

		int size = settlementListBox.getModel().getSize();
		if (size > 1) {
			// Gets the selected settlement from SettlementMapPanel
			Settlement s = desktop.getSettlementMapPanel().getSettlement();
			// Selects the settlement in the combo box
			settlementListBox.setSelectedItem(s);
			// Change to the selected settlement in SettlementMapPanel
			if (s != null)
				setSettlement(s);
		}

		else if (size == 1) {
			// Selects the first settlement
			settlementListBox.setSelectedIndex(0);
			// Gets the settlement
			Settlement s = (Settlement) settlementListBox.getSelectedItem();
			// Change to the selected settlement in SettlementMapPanel
			setSettlement(s);
		}

		// Set the item listener only after the setup is done
		settlementListBox.addItemListener(new ItemListener() {
			// Note: ensure unitUpdate() update combobox when a new building is added
			@Override
			public void itemStateChanged(ItemEvent event) {
				Settlement newSettlement = (Settlement) event.getItem();
				// Change to the selected settlement in SettlementMapPanel
				if (newSettlement != selectedSettlement) {
					setSettlement(newSettlement);
				}

				// Still need to update the existing tab
				updateTab();
			}
		});
	}

	/**
	 * Change the map display to the selected settlement
	 *
	 * @param s
	 */
	public void setSettlement(Settlement s) {
		// Set the selected settlement
		selectedSettlement = s;
		// Set the box opaque
		settlementListBox.setOpaque(false);
	}

    /**
     * Gets the length of the most lengthy settlement name
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
	 * Checks if a monitor tab contains this model.
	 *
	 * @param model the model to check for.
	 * @return true if a tab contains the model.
	 */
	public boolean containsModel(UnitTableModel model) {
		for (Component c: tabsSection.getComponents()) {
			MonitorTab tab = (MonitorTab)c;
			if (tab.getModel().equals(model)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the index of the monitor tab with the model.
	 *
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	public int getModelIndex(UnitTableModel model) {
		for (Component c: tabsSection.getComponents()) {
			MonitorTab tab = (MonitorTab)c;
			if (tab.getModel().equals(model)) {
				return tabsSection.indexOfComponent(c);
			}
		}
		return -1;
	}

	/**
	 * This method creates a new chart window based on the model of the currently
	 * selected window. The chart is added as a separate tab to the window.
	 */
	private void createBarChart() {
		MonitorModel model = getSelected().getModel();
		int columns[] = ColumnSelector.createBarSelector(desktop, model);

		if (columns != null && columns.length > 0) {
			addTab(new BarChartTab(model, columns));
		}
	}

	private void createPieChart() {
		MonitorModel model = getSelected().getModel();
		if (model != null) {
			int column = ColumnSelector.createPieSelector(desktop, model);
			if (column >= 0) {
				addTab(new PieChartTab(model, column));
			}
		}
	}

	/**
	 * Return the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	public MonitorTab getSelected() {
		Component c = tabsSection.getSelectedComponent();
		if (c != null)
			return (MonitorTab)c;
		else {
			logger.severe("No tab selected.");
			return null;
		}
	}

	public int getSelectedTab() {
		return tabsSection.getSelectedIndex();
	}

	/**
	 * Updates the tab content
	 */
	public void updateTab() {
		// SwingUtilities.updateComponentTreeUI(this);
		MonitorTab selectedTab = getSelected();
		if (selectedTab == null)
			return;
		
		int index = tabsSection.indexOfComponent(selectedTab);
		// if it's the all settlements tab, exit 
		if (index == 0)
			return;
		
		MonitorModel model = selectedTab.getModel();
		TableTab tableTab = null;
		JTable table = null;
		tabsSection.removeChangeListener(tabsSection.getChangeListeners()[0]);
		selectedTab.getModel().removeTableModelListener(this);

		// Disable all buttons
		buttonBar.setEnabled(false);
		buttonMap.setEnabled(false);
		buttonDetails.setEnabled(false);
		buttonMissions.setEnabled(false);
		buttonFilter.setEnabled(false);

		try {
			MonitorTab newTab = null;
			if (selectedTab instanceof UnitTab) {
				// Enable these buttons
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);

				UnitTableModel unitTableModel = (UnitTableModel)model;

				if (model instanceof RobotTableModel) {
					unitTableModel = new RobotTableModel(selectedSettlement);
					newTab = new UnitTab(this, unitTableModel, true, BOT_ICON);
				}
				else if (model instanceof CropTableModel) {
					unitTableModel = new CropTableModel(selectedSettlement);
					newTab = new UnitTab(this, unitTableModel, true, CROP_ICON);
				}
				else if (model instanceof SettlementTableModel) {
					unitTableModel = new SettlementTableModel(selectedSettlement);
					newTab = new UnitTab(this, unitTableModel, true, COLONY_ICON);
				}
				else if (model instanceof VehicleTableModel) {
					unitTableModel = new VehicleTableModel(selectedSettlement);
					newTab = new UnitTab(this, unitTableModel, true, VEHICLE_ICON);
				}
				else if (model instanceof PersonTableModel) {
					unitTableModel = new PersonTableModel(selectedSettlement, true);
					newTab = new UnitTab(this, unitTableModel, true, PEOPLE_ICON);
				}
				else if (model instanceof BuildingTableModel) {
					unitTableModel = new BuildingTableModel(selectedSettlement);
					newTab = new UnitTab(this, unitTableModel, true, BUILDING_ICON);
				}
			} else if (selectedTab instanceof MissionTab) {
				// Enable these buttons
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonMissions.setEnabled(true);

				newTab = new MissionTab(this);

			} else if (selectedTab instanceof EventTab) {
				// Enable these buttons
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);

				eventsTab = new EventTab(this, desktop);
				newTab = eventsTab;

			} else if (selectedTab instanceof FoodInventoryTab) {
				// Enable these buttons
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);

				newTab = new FoodInventoryTab(this);

			} else if (selectedTab instanceof TradeTab) {
				// Enable these buttons
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);

				newTab = new TradeTab(this);
			}

			swapTab(selectedTab, newTab);
			model = newTab.getModel();
			tableTab = (TableTab)newTab;
			table = tableTab.getTable();
			this.table = table;
			model.addTableModelListener(this);
			tabsSection.setSelectedComponent(newTab);
			tabsSection.addChangeListener(e -> updateTab());

			// Update the row count label with new numbers
			rowCount.setText(newTab.getCountString());

		} catch (Exception e) {
			logger.severe("Problems in re-creating tabs in MonitorWindow: " + e.getMessage());
		}

//		if (table != null) {
//			// Note: for pie and bar chart, skip the codes below
//			TableStyle.setTableStyle(table);
//			rowTable = new RowNumberTable(table);
//			// Note: needed for periodic refreshing in ToolWindow
//			TableStyle.setTableStyle(rowTable);
//
//			// May reactivate statusPanel.remove(_tableSearchableBar) later
//			// May reactivate this: if (reloadSearch) createSearchableBar(table) later
//		}
		// SwingUtilities.updateComponentTreeUI(this);
	}


//	public void createSearchableBar(JTable table) {
//		// Searchable searchable = null;
//		// SearchableBar _tableSearchableBar = null;
//
//		if (searchable != null)
//			SearchableUtils.uninstallSearchable(searchable);
//
//		if (table != null) {
//			// SearchableUtils.uninstallSearchable(searchable);
//			searchable = SearchableUtils.installSearchable(table);
//			// searchable.setRepeats(true);
//			searchable.setPopupTimeout(5000);
//			searchable.setCaseSensitive(false);
//			searchable.setHideSearchPopupOnEvent(false);
//			searchable.setWildcardEnabled(true);
//			searchable.setHeavyweightComponentEnabled(true);
//			// searchable.setSearchableProvider(searchableProvider)
//			searchable.setMismatchForeground(Color.PINK);
//			// WildcardSupport WildcardSupport = new WildcardSupport();
//			// searchable.setWildcardSupport(new WildcardSupport());
//
//			if (searchBar != null) {
//				searchBar.setSearchingText("");
//				statusPanel.remove(searchBar);
//				searchBar = null;
//			}
//
//			searchBar = new SearchableBar(searchable);
//			searchBar.setSearchingText("");
//			searchBar.setCompact(true);
//			// _tableSearchableBar.setSearchingText("*" +
//			// _tableSearchableBar.getSearchingText());
//
//			// _tableSearchableBar.setVisibleButtons(1);
//			TooltipManager.setTooltip(searchBar, "Use wildcards (*, +, ?) for searching. e.g. '*DaVinci' ");
//
//			((TableSearchable) searchable).setMainIndex(-1); // -1 = search for all columns
//			searchBar.setVisibleButtons(SearchableBar.SHOW_NAVIGATION | SearchableBar.SHOW_MATCHCASE
//					| SearchableBar.SHOW_WHOLE_WORDS | SearchableBar.SHOW_STATUS);
//			searchBar.setName(table.getName());
//			searchBar.setShowMatchCount(true);
//			searchBar.setVisible(true);
//
//			statusPanel.add(searchBar); // , BorderLayout.AFTER_LAST_LINE);
//
//			// pack();
//
//			// statusPanel.add(_tableSearchableBar); // , BorderLayout.AFTER_LAST_LINE);
//			statusPanel.invalidate();
//			statusPanel.revalidate();
//		}
//	}

//	public void createRadioButton() {
//
//		label1 = new JLabel(new ImageIcon("Grapes1.png"));
//		radio1 = new JRadioButton("");
//		radio1.setName("Grapes");
//
//		label2 = new JLabel(new ImageIcon("Mango.jpg"));
//		radio2 = new JRadioButton("");
//		radio2.setName("Mango");
//
//		label3 = new JLabel(new ImageIcon("Apple.jpg"));
//		radio3 = new JRadioButton("");
//		radio3.setName("Apple");
//
//		label4= new JLabel();
//
//		jf.add(radio1);
//		jf.add(label1);
//		jf.add(radio2);
//		jf.add(label2);
//		jf.add(radio3);
//		jf.add(label3);
//
//		radio1.addActionListener(this);
//		radio2.addActionListener(this);
//		radio3.addActionListener(this);
//
//		jf.setLayout(new FlowLayout());
//		jf.setSize(400,200);
//		jf.setVisible(true);
//	}
//
//
//	public void actionPerformed(ActionEvent ae) {
//		JRadioButton rd = (JRadioButton)ae.getSource();
//
//		if (rd.isSelected()) {
//			label4.setText(rd.getName()+ " is checked");
//			jf.add(label4);
//			jf.setVisible(true);
//		}
//		else {
//			label4.setText(rd.getName()+ " is unchecked");
//			jf.add(label4);
//			jf.setVisible(true);
//		}
//
//	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() != TableModelEvent.UPDATE) {
			refreshTableStyle();
		}
	}

	/**
	 * Adds a new tab to Monitor Tool
	 *
	 * @param newTab
	 */
	private void addTab(MonitorTab newTab) {
		tabsSection.addTab("", newTab.getIcon(), newTab,  newTab.getName());
	}

	/**
	 * Swaps out the old tab with a new tab
	 *
	 * @param oldTab
	 * @param newTab
	 */
	private void swapTab(MonitorTab oldTab, MonitorTab newTab) {
		int index = tabsSection.indexOfComponent(oldTab);
		tabsSection.remove(oldTab);
		oldTab.removeTab();
		tabsSection.insertTab("", newTab.getIcon(), newTab, newTab.getName(), index);
	}


	/**
	 * Retires a tab from Monitor Tool
	 *
	 * @param tab
	 */
	private void retireTab(MonitorTab tab) {
		tabsSection.remove(tab);
		tab.removeTab();
	}

	/**
	 * Removes a tab from Monitor Tool
	 *
	 * @param oldTab
	 */
	private void removeTab(MonitorTab oldTab) {
		retireTab(oldTab);
		if (getSelected() == oldTab) {
			tabsSection.setSelectedIndex(0);
		}
	}

	private void centerMap() {
		MonitorTab selected = getSelected();
		if (selected != null) {
			selected.centerMap(desktop);
		}
	}

	public void displayDetails() {
		MonitorTab selected = getSelected();
		if (selected != null) {
			selected.displayDetails(desktop);
		}
	}

	private void displayMission() {
		MonitorTab selected = getSelected();
		if ((selected instanceof MissionTab) && (selected != null)) {
			((MissionTab) selected).displayMission(desktop);
		}
	}

	private void displayProps() {
		MonitorTab selected = getSelected();
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

	/*
	 * Refreshes the table theme style and row count
	 */
	public void refreshTableStyle() {
		if (table != null) {
			TableStyle.setTableStyle(table);
			rowTable = new RowNumberTable(table);
			TableStyle.setTableStyle(rowTable);
			MonitorTab selected = getSelected();
			if (selected == eventsTab) {
				rowCount.setText(eventsTab.getCountString());
			}
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
			MonitorTab selected = getSelected();
			if (selected != null && !selected.getMandatory()) {
				removeTab(getSelected());
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
	 * Prepare tool window for deletion.
	 */
	@Override
	public void destroy() {
		tabsSection = null;
		rowCount = null;
		eventsTab = null;
		buttonPie = null;
		buttonBar = null;
		buttonRemoveTab = null;
		buttonMap = null;
		buttonDetails = null;
		buttonMissions = null;
		buttonFilter = null;
		buttonProps = null;
		desktop = null;
		statusPanel = null;
		table = null;
		rowTable = null;
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
