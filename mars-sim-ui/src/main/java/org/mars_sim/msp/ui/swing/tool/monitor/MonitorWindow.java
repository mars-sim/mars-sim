/**
 * Mars Simulation Project
 * MonitorWindow.java
 * @version 3.1.0 2017-01-19
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.notification.NotificationWindow;
import org.mars_sim.msp.ui.swing.tool.RowNumberTable;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.WebTabbedPane;

import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.jidesoft.swing.Searchable;
import com.jidesoft.swing.SearchableBar;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

/**
 * The MonitorWindow is a tool window that displays a selection of tables each
 * of which monitor a set of Units.
 */
@SuppressWarnings("serial")
public class MonitorWindow extends ToolWindow implements TableModelListener, ActionListener {

	private static final int STATUSHEIGHT = 25;
	private static final int WIDTH = 1300;//1280;
	private static final int HEIGHT = 512;
	
	public static final String NAME = Msg.getString("MonitorWindow.title"); //$NON-NLS-1$

	// Added an custom icon for each tab
	public static final String BASE_ICON = Msg.getString("icon.base"); //$NON-NLS-1$
	public static final String BOT_ICON = Msg.getString("icon.bot"); //$NON-NLS-1$
	public static final String MISSION_ICON = Msg.getString("icon.mission"); //$NON-NLS-1$
	public static final String VEHICLE_ICON = Msg.getString("icon.vehicle"); //$NON-NLS-1$
	public static final String CROP_ICON = Msg.getString("icon.crop"); //$NON-NLS-1$
	public static final String EVENT_ICON = Msg.getString("icon.event"); //$NON-NLS-1$
	public static final String FOOD_ICON = Msg.getString("icon.food"); //$NON-NLS-1$
	public static final String PEOPLE_ICON = Msg.getString("icon.people"); //$NON-NLS-1$
	public static final String TRADE_ICON = Msg.getString("icon.trade"); //$NON-NLS-1$

	public static final String TRASH_ICON = Msg.getString("icon.trash"); //$NON-NLS-1$
	public static final String CENTERMAP_ICON = Msg.getString("icon.centermap"); //$NON-NLS-1$
	public static final String FIND_ICON = Msg.getString("icon.find"); //$NON-NLS-1$
	public static final String COLUMN_ICON = Msg.getString("icon.column"); //$NON-NLS-1$
	public static final String FILTER_ICON = Msg.getString("icon.filter"); //$NON-NLS-1$

	public static final String BAR_ICON = Msg.getString("icon.bar"); //$NON-NLS-1$
	public static final String PIE_ICON = Msg.getString("icon.pie"); //$NON-NLS-1$

	// Data members
	private WebTabbedPane tabsSection;
	// private JideTabbedPane tabsSection;
	
	private WebLabel rowCount;
	
	private ArrayList<MonitorTab> tabs = new ArrayList<MonitorTab>();
	
	/** Tab showing historical events. */
	private EventTab eventsTab;
	
	private MonitorTab oldTab = null;
	
	private WebButton buttonPie;
	private WebButton buttonBar;
	private WebButton buttonRemoveTab;
	private WebButton buttonMap;
	private WebButton buttonDetails;
	private WebButton buttonMissions;
	private WebButton buttonFilter;
	private WebButton buttonProps;

	private MainDesktopPane desktop;

	private MainWindow mainWindow;

	private WebPanel statusPanel;
	
	private JTable table;
	private JTable rowTable;
	
	private Searchable searchable;
	private SearchableBar searchBar;

	/**
	 * Constructor.
	 * 
	 * @param desktop the desktop pane
	 */
	public MonitorWindow(MainDesktopPane desktop) {
		// Use TableWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;

		mainWindow = desktop.getMainWindow();

		// Get content pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create a status panel
		statusPanel = new WebPanel();
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mainPane.add(statusPanel, BorderLayout.SOUTH);

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
		// toolbar
		statusPanel.add(buttonBar);

		buttonRemoveTab = new WebButton(ImageLoader.getNewIcon(TRASH_ICON)); // $NON-NLS-1$
		TooltipManager.setTooltip(buttonRemoveTab, Msg.getString("MonitorWindow.tooltip.tabRemove"), //$NON-NLS-1$
				TooltipWay.up); 
		buttonRemoveTab.addActionListener(this);
	
		statusPanel.add(buttonRemoveTab);
	
		// Create buttons based on selection
		buttonMap = new WebButton(ImageLoader.getNewIcon(CENTERMAP_ICON)); // $NON-NLS-1$
		// buttonMap.setMargin(new Insets(3, 4, 4, 4));

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

		buttonFilter = new WebButton(ImageLoader.getNewIcon(FILTER_ICON)); // $NON-NLS-1$
	
		TooltipManager.setTooltip(buttonFilter, Msg.getString("MonitorWindow.tooltip.categoryFilter"), TooltipWay.up);
		buttonFilter.addActionListener(this);

		statusPanel.add(buttonFilter);

		// Create tabbed pane for the table
		tabsSection = new WebTabbedPane(WebTabbedPane.TOP, WebTabbedPane.SCROLL_TAB_LAYOUT); // WRAP_TAB_LAYOUT);//
		
		tabsSection.setForeground(Color.DARK_GRAY);
		mainPane.add(tabsSection, BorderLayout.CENTER);
		
		// Status item for row
		rowCount = new WebLabel("  "); //$NON-NLS-1$
		rowCount.setHorizontalAlignment(SwingConstants.LEFT);
		rowCount.setBorder(BorderFactory.createLoweredBevelBorder());
		statusPanel.add(rowCount);
		Dimension dims = new Dimension(120, STATUSHEIGHT);
		rowCount.setPreferredSize(dims);

		// Add the default table tabs
		// Added notifyBox
		NotificationWindow notifyBox = new NotificationWindow(desktop);

		addTab(new UnitTab(this, new RobotTableModel(desktop), true, BOT_ICON));
		
		addTab(new UnitTab(this, new CropTableModel(), true, CROP_ICON));
		// Added notifyBox
		eventsTab = new EventTab(this, notifyBox, desktop);

		addTab(eventsTab);

		addTab(new FoodInventoryTab(this));

		addTab(new TradeTab(this));

		addTab(new MissionTab(this));

		addTab(new UnitTab(this, new SettlementTableModel(), true, BASE_ICON));

		addTab(new UnitTab(this, new VehicleTableModel(), true, VEHICLE_ICON));

		addTab(new UnitTab(this, new PersonTableModel(desktop), true, PEOPLE_ICON));

		if (GameManager.mode != GameMode.COMMAND) {
			// Add a tab for each settlement
			for (Settlement s : unitManager.getSettlements()) {
	//			addTab(new UnitTab(this, new SettlementTableModel(s), true, BASE_ICON));
				addTab(new UnitTab(this, new PersonTableModel(s, true), true, PEOPLE_ICON));
			}
		}
		
		// Add a listener for the tab changes
		tabsSection.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tabChanged(true);
			}
		});

		// Note: must use setSize() to define a starting size
		setSize(new Dimension(WIDTH, HEIGHT));
//		setMinimumSize(new Dimension(768, 200));
		// Need to verify why setPreferredSize() prevents Monitor Window from being
		// resizable
		// and create spurious error message in linux in some cases
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setResizable(true);
		setMaximizable(true);
		setVisible(true);

		Dimension desktopSize = desktop.getSize();
		Dimension jInternalFrameSize = this.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		setLocation(width, height);

		// Open the people tab at the start of the sim
		tabsSection.setSelectedIndex(2);
		table.repaint();

	}

	/**
	 * This method add the specified Unit table as a new tab in the Monitor. The
	 * model is displayed as a table by default. The name of the tab is that of the
	 * Model.
	 *
	 * @param model The new model to display.
	 */
	public void displayModel(UnitTableModel model) {
		if (containsModel(model))
			tabsSection.setSelectedIndex(getModelIndex(model));
		else
			addTab(new UnitTab(this, model, false, UnitWindow.USER));
	}

	/**
	 * Checks if a monitor tab contains this model.
	 * 
	 * @param model the model to check for.
	 * @return true if a tab contains the model.
	 */
	public boolean containsModel(UnitTableModel model) {
		boolean result = false;
		Iterator<MonitorTab> i = tabs.iterator();
		while (i.hasNext()) {
			if (i.next().getModel().equals(model))
				result = true;
		}
		return result;
	}

	/**
	 * Gets the index of the monitor tab with the model.
	 * 
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	public int getModelIndex(UnitTableModel model) {
		int result = -1;
		Iterator<MonitorTab> i = tabs.iterator();
		while (i.hasNext()) {
			MonitorTab tab = i.next();
			if (tab.getModel().equals(model))
				result = tabs.indexOf(tab);
		}
		return result;
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
		int column = ColumnSelector.createPieSelector(desktop, model);

		if (column >= 0) {
			addTab(new PieChartTab(model, column));
		}
	}

	/**
	 * Return the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	public MonitorTab getSelected() {
		// SwingUtilities.updateComponentTreeUI(this);
		MonitorTab selected = null;
		int selectedIdx = tabsSection.getSelectedIndex();
		if ((selectedIdx != -1) && (selectedIdx < tabs.size()))
			selected = tabs.get(selectedIdx);
		return selected;
	}

	public void setTab() {
		tabsSection.setSelectedIndex(6);
	}

	public void setTableChanged() {
		tabChanged(false);
	}

	public void setSelectedTab() {
		int i = tabsSection.getSelectedIndex();
		tabsSection.setSelectedIndex(i);
	}

	public void tabChanged(boolean reloadSearch) {
		// SwingUtilities.updateComponentTreeUI(this);
		MonitorTab newTab = getSelected();
		JTable table = null;

		if (newTab != oldTab) {
			newTab.getModel().addTableModelListener(this);

			// Disable all buttons
			buttonMap.setEnabled(false);
			buttonDetails.setEnabled(false);
			buttonMissions.setEnabled(false);
			buttonFilter.setEnabled(false);

			if (newTab instanceof UnitTab) {
				buttonBar.setEnabled(false);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				table = ((UnitTab) newTab).getTable();
			} else if (newTab instanceof MissionTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonMissions.setEnabled(true);
				table = ((MissionTab) newTab).getTable();
			} else if (newTab instanceof EventTab) {
				buttonBar.setEnabled(false);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((EventTab) newTab).getTable();
			} else if (newTab instanceof FoodInventoryTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((FoodInventoryTab) newTab).getTable();
			} else if (newTab instanceof TradeTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((TradeTab) newTab).getTable();
			}

			this.table = table;

			// Update skin theme using TableStyle's setTableStyle()
			if (table != null) { // for pie and bar chart, skip the codes below
				// Note: needed for periodic refreshing in ToolWindow
				// TableStyle.setTableStyle(new RowNumberTable(table));
				TableStyle.setTableStyle(table);
				rowTable = new RowNumberTable(table);
				TableStyle.setTableStyle(rowTable);
				// statusPanel.remove(_tableSearchableBar);
				if (reloadSearch)
					createSearchableBar(table);
			}

			// String status = newTab.getCountString();
			rowCount.setText(newTab.getCountString());

			if (oldTab != null) {
				MonitorModel model = oldTab.getModel();
				if (model != null)
					oldTab.getModel().removeTableModelListener(this);
			}

			// Set oldTab to newTab
			oldTab = newTab;
		}

		// SwingUtilities.updateComponentTreeUI(this);
	}

	
	public void createSearchableBar(JTable table) {
		// Searchable searchable = null;
		// SearchableBar _tableSearchableBar = null;

		if (searchable != null)
			SearchableUtils.uninstallSearchable(searchable);

		if (table != null) {
			// SearchableUtils.uninstallSearchable(searchable);
			searchable = SearchableUtils.installSearchable(table);
			// searchable.setRepeats(true);
			searchable.setPopupTimeout(5000);
			searchable.setCaseSensitive(false);
			searchable.setHideSearchPopupOnEvent(false);
			searchable.setWildcardEnabled(true);
			searchable.setHeavyweightComponentEnabled(true);
			// searchable.setSearchableProvider(searchableProvider)
			searchable.setMismatchForeground(Color.PINK);
			// WildcardSupport WildcardSupport = new WildcardSupport();
			// searchable.setWildcardSupport(new WildcardSupport());

			if (searchBar != null) {
				searchBar.setSearchingText("");
				statusPanel.remove(searchBar);
				searchBar = null;
			}

			searchBar = new SearchableBar(searchable);
			searchBar.setSearchingText("");
			searchBar.setCompact(true);
			// _tableSearchableBar.setSearchingText("*" +
			// _tableSearchableBar.getSearchingText());

			// _tableSearchableBar.setVisibleButtons(1);
			TooltipManager.setTooltip(searchBar, "Use wildcards (*, +, ?) for searching. e.g. '*DaVinci' ");
			
			((TableSearchable) searchable).setMainIndex(-1); // -1 = search for all columns
			searchBar.setVisibleButtons(SearchableBar.SHOW_NAVIGATION | SearchableBar.SHOW_MATCHCASE
					| SearchableBar.SHOW_WHOLE_WORDS | SearchableBar.SHOW_STATUS);
			searchBar.setName(table.getName());
			searchBar.setShowMatchCount(true);
			searchBar.setVisible(true);

			statusPanel.add(searchBar); // , BorderLayout.AFTER_LAST_LINE);

			// pack();

			// statusPanel.add(_tableSearchableBar); // , BorderLayout.AFTER_LAST_LINE);
			statusPanel.invalidate();
			statusPanel.revalidate();
		}
	}

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
//			MonitorTab selected = getSelected();
//			if (selected == eventsTab) {
//				rowCount.setText(eventsTab.getCountString());
//			}
		}
	}

	/**
	 * Adds a new tab to Monitor Tool
	 * 
	 * @param newTab
	 */
	private void addTab(MonitorTab newTab) {
		tabs.add(newTab);
		tabsSection.addTab(newTab.getName(), newTab.getIcon(), newTab); // "", newTab.getIcon(), newTab);//
		tabsSection.setSelectedIndex(tabs.size() - 1);
		// tabChanged(true);
	}

	/**
	 * Removes a tab from Monitor Tool
	 * 
	 * @param oldTab
	 */
	private void removeTab(MonitorTab oldTab) {
		tabs.remove(oldTab);
		tabsSection.remove(oldTab);

		oldTab.removeTab();
		if (getSelected() == oldTab) {
			tabsSection.setSelectedIndex(0);
		}
		// tabChanged(true);
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
			if (!selected.getMandatory()) {
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
	public void destroy() {
		Iterator<MonitorTab> i = tabs.iterator();
		while (i.hasNext())
			i.next().removeTab();
		tabs.clear();
		tabs = null;
		tabsSection = null;
		rowCount = null;
		eventsTab = null;
		oldTab = null;
		buttonPie = null;
		buttonBar = null;
		buttonRemoveTab = null;
		buttonMap = null;
		buttonDetails = null;
		buttonMissions = null;
		buttonFilter = null;
		buttonProps = null;

		desktop = null;

		mainWindow = null;

		statusPanel = null;
		
		table = null;
		rowTable = null;
		
		searchable = null;
		searchBar = null;

	}
}