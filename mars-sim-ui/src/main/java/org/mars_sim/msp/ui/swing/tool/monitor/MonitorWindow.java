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
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.javafx.MainScene;
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
import com.alee.laf.table.WebTable;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.jidesoft.swing.Searchable;
import com.jidesoft.swing.SearchableBar;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

/**
 * The MonitorWindow is a tool window that displays a selection of tables
 * each of which monitor a set of Units.
 */
public class MonitorWindow
extends ToolWindow
implements TableModelListener, ActionListener {


	final private static int STATUSHEIGHT = 25;

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
	//private JideTabbedPane tabsSection;
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
	private MainScene mainScene;
	private MainWindow mainWindow;

	private WebPanel statusPanel;
	private WebTable table ;
	private Searchable searchable ;
	private SearchableBar _tableSearchableBar;

	/**
	 * Constructor.
	 * @param desktop the desktop pane
	 */
	public MonitorWindow(MainDesktopPane desktop) {
		// Use TableWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;
	
		if (desktop.getMainScene() != null)
			mainScene = desktop.getMainScene();
		else if (desktop.getMainWindow() != null)
			mainWindow = desktop.getMainWindow();

		// Get content pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create a status panel
		statusPanel = new WebPanel();
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mainPane.add(statusPanel, BorderLayout.SOUTH);

		// Create toolbar
		//JToolBar toolbar = new JToolBar();
		//toolbar.setFloatable(false);
		//mainPane.add(toolbar, BorderLayout.NORTH);
		
		// Create graph button
		buttonPie = new WebButton(ImageLoader.getNewIcon(PIE_ICON));
		buttonPie.setToolTipText(Msg.getString("MonitorWindow.tooltip.singleColumnPieChart")); //$NON-NLS-1$
		//TooltipManager.setTooltip (buttonPie, Msg.getString("MonitorWindow.tooltip.singleColumnPieChart"), TooltipWay.up);
		
		buttonPie.addActionListener(this);
		//toolbar
		statusPanel.add(buttonPie);

		buttonBar = new WebButton(ImageLoader.getNewIcon(BAR_ICON));
		buttonBar.setToolTipText(Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonBar, Msg.getString("MonitorWindow.tooltip.multipleColumnBarChart"), TooltipWay.up);
		buttonBar.addActionListener(this);
		//toolbar
		statusPanel.add(buttonBar);

		buttonRemoveTab = new WebButton(ImageLoader.getNewIcon(TRASH_ICON)); //$NON-NLS-1$
		//buttonRemoveTab.setToolTipText(Msg.getString("MonitorWindow.tooltip.tabRemove")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonRemoveTab, Msg.getString("MonitorWindow.tooltip.tabRemove"), TooltipWay.up);
		buttonRemoveTab.addActionListener(this);
		//toolbar
		statusPanel.add(buttonRemoveTab);
		//toolbar.addSeparator();

		// Create buttons based on selection
		buttonMap = new WebButton(ImageLoader.getNewIcon(CENTERMAP_ICON)); //$NON-NLS-1$
		//buttonMap.setMargin(new Insets(3, 4, 4, 4));
		//buttonMap.setToolTipText(Msg.getString("MonitorWindow.tooltip.centerMap")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonMap, Msg.getString("MonitorWindow.tooltip.centerMap"), TooltipWay.up);
		buttonMap.addActionListener(this);
		//toolbar
		statusPanel.add(buttonMap);

		buttonDetails = new WebButton(ImageLoader.getNewIcon(FIND_ICON)); //$NON-NLS-1$
		//buttonDetails.setToolTipText(Msg.getString("MonitorWindow.tooltip.showDetails")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonDetails, Msg.getString("MonitorWindow.tooltip.showDetails"), TooltipWay.up);
		buttonDetails.addActionListener(this);
		//toolbar
		statusPanel.add(buttonDetails);

		buttonMissions = new WebButton(ImageLoader.getNewIcon(MISSION_ICON)); //$NON-NLS-1$
		//buttonMissions.setToolTipText(Msg.getString("MonitorWindow.tooltip.mission")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonMissions, Msg.getString("MonitorWindow.tooltip.mission"), TooltipWay.up);
		buttonMissions.addActionListener(this);
		//toolbar
		statusPanel.add(buttonMissions);
		//toolbar.addSeparator();

		//buttonProps = new WebButton(ImageLoader.getIcon(Msg.getString("img.preferences"))); //$NON-NLS-1$
		buttonProps = new WebButton(ImageLoader.getNewIcon(COLUMN_ICON)); //$NON-NLS-1$
		//buttonProps.setToolTipText(Msg.getString("MonitorWindow.tooltip.preferences")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonProps, Msg.getString("MonitorWindow.tooltip.preferences"), TooltipWay.up);
		buttonProps.addActionListener(this);
		//toolbar
		statusPanel.add(buttonProps);
		//toolbar.addSeparator();

		//buttonFilter = new WebButton(ImageLoader.getIcon(Msg.getString("img.categoryFilter"))); //$NON-NLS-1$
		buttonFilter = new WebButton(ImageLoader.getNewIcon(FILTER_ICON)); //$NON-NLS-1$
		//buttonFilter.setToolTipText(Msg.getString("MonitorWindow.tooltip.categoryFilter")); //$NON-NLS-1$
		TooltipManager.setTooltip (buttonFilter, Msg.getString("MonitorWindow.tooltip.categoryFilter"), TooltipWay.up);
		buttonFilter.addActionListener(this);
		//toolbar
		statusPanel.add(buttonFilter);

//		if (desktop.getMainScene() != null)
//			desktop.getMainScene().setLookAndFeel(ThemeType.Nimrod);
//		
		// Create tabbed pane for the table
		tabsSection = new WebTabbedPane();
//		tabsSection = new JideTabbedPane(JideTabbedPane.TOP);
		//tabsSection.setTabPlacement(JideTabbedPane.BOTTOM);
//		if (MainScene.OS.contains("win")) {
//			if (MainScene.getTheme() == 7)
//				LookAndFeelFactory.installJideExtension(LookAndFeelFactory.EXTENSION_STYLE_OFFICE2007);//.OFFICE2003_STYLE);
//			else 
//				LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
//		}
//		else if (MainScene.OS.contains("mac"))
//			LookAndFeelFactory.installJideExtension();//.OFFICE2003_STYLE);
//		else if (MainScene.OS.contains("linux"))
//			LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);//.OFFICE2003_STYLE);
//
//		tabsSection.setBoldActiveTab(true);
//		tabsSection.setScrollSelectedTabOnWheel(true);
//		tabsSection.setTabResizeMode(JideTabbedPane.RESIZE_MODE_COMPRESSED);
		//tabsSection.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
		//tabsSection.setTabShape(JideTabbedPane.BUTTON_EAST);//.SHAPE_WINDOWS_SELECTED);
		//tabsSection.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003); //COLOR_THEME_VSNET);
		//tabsSection.setBackground(UIDefaultsLookup.getColor("control"));
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
		//UnitManager unitManager = Simulation.instance().getUnitManager();

		// 2014-11-29 Added notifyBox
		NotificationWindow notifyBox = new NotificationWindow(desktop);

		// 2015-01-21 Added RobotTableModel
		addTab(new UnitTab(this, new RobotTableModel(desktop), true, BOT_ICON));
		// 2014-10-14 mkung: added FoodTableModel
		addTab(new UnitTab(this, new CropTableModel(), true, CROP_ICON));
		// 2014-11-29 Added notifyBox 2015-01-15 Added desktop
		eventsTab = new EventTab(this, notifyBox, desktop);

		addTab(eventsTab);
		// 2014-11-25 mkung: added FoodInventoryTab()
		addTab(new FoodInventoryTab(this));

		addTab(new TradeTab(this));

		addTab(new MissionTab(this));

		addTab(new UnitTab(this, new PersonTableModel(desktop), true, PEOPLE_ICON));

		addTab(new UnitTab(this, new SettlementTableModel(), true, BASE_ICON));

		addTab(new UnitTab(this, new VehicleTableModel(), true, VEHICLE_ICON));

		// Add a listener for the tab changes
		tabsSection.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					tabChanged(true);
				}
			}
		);

		// Note: must use setSize() to define a starting size
		setSize(new Dimension(1280, 512));
		setMinimumSize(new Dimension(768, 200));
		// Need to verify why setPreferredSize() prevents Monitor Window from being resizable
		// and create spurious error message in linux in some cases
		//setPreferredSize(new Dimension(1280, 512));
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
	 * model is displayed as a table by default.
	 * The name of the tab is that of the Model.
	 *
	 * @param model The new model to display.
	 */
	public void displayModel(UnitTableModel model) {
		if (containsModel(model)) tabsSection.setSelectedIndex(getModelIndex(model));
		else addTab(new UnitTab(this,model, false, UnitWindow.USER));
	}

	/**
	 * Checks if a monitor tab contains this model.
	 * @param model the model to check for.
	 * @return true if a tab contains the model.
	 */
	public boolean containsModel(UnitTableModel model) {
		boolean result = false;
		Iterator<MonitorTab> i = tabs.iterator();
		while (i.hasNext()) {
			if (i.next().getModel().equals(model)) result = true;
		}
		return result;
	}

	/**
	 * Gets the index of the monitor tab with the model.
	 * @param model the model to check for.
	 * @return tab index or -1 if none.
	 */
	public int getModelIndex(UnitTableModel model) {
		int result = -1;
		Iterator<MonitorTab> i = tabs.iterator();
		while (i.hasNext()) {
			MonitorTab tab = i.next();
			if (tab.getModel().equals(model)) result = tabs.indexOf(tab);
		}
		return result;
	}

	/**
	 * This method creates a new chart window based on the model of the
	 * currently selected window. The chart is added as a separate tab to the
	 * window.
	 */
	private void createBarChart() {
		MonitorModel model = getSelected().getModel();
		int columns[] = ColumnSelector.createBarSelector(desktop, model);

		//if (mainScene != null) {
		//	columns = ColumnSelector.createBarSelector(desktop, model);
		//}

		//else if (mainWindow != null) {
			// Show modal column selector
			//columns = ColumnSelector.createBarSelector(desktop, model);
		//}
		if (columns == null) {
			//System.out.println("createBarChart() : columns = null");
		}
		else if (columns != null) {
			if (columns.length > 0) {
				addTab(new BarChartTab(model, columns));
				//System.out.println("createBarChart() : just done calling new BarChartTab()");
			}
		}
	}

	private void createPieChart() {
		MonitorModel model = getSelected().getModel();
		int column = ColumnSelector.createPieSelector(desktop, model);

		//if (mainScene != null) {
		//	column = ColumnSelector.createPieSelector(desktop, model);
		//}

		//else if (mainWindow != null) {
			// Show modal column selector
			//column = ColumnSelector.createPieSelector(desktop, model);
		//}

		if (column >= 0) {
			//System.out.println("createPieChart() : column >= 0");
			addTab(new PieChartTab(model, column));
			//System.out.println("createPieChart() : just done calling new PieChartTab()");
		} else {
			//System.out.println("createPieChart() : column < 0");
		}
	}
	/**
	 * Return the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	public MonitorTab getSelected() {
		//SwingUtilities.updateComponentTreeUI(this);
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
		//SwingUtilities.updateComponentTreeUI(this);
		//System.out.println("tabChanged()");
		MonitorTab newTab = getSelected();
		WebTable table = null;
		
		if (newTab != oldTab) {
			newTab.getModel().addTableModelListener(this);
			//if (oldTab != null) System.out.println("tabChanged() : oldTab is " + oldTab.getName());
			//System.out.println("tabChanged() : newTab is " + newTab.getName());

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
			}
			else if (newTab instanceof MissionTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonMissions.setEnabled(true);
				table = ((MissionTab) newTab).getTable();
			}
			else if (newTab instanceof EventTab) {
				buttonBar.setEnabled(false);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((EventTab) newTab).getTable();
			}
			else if (newTab instanceof FoodInventoryTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((FoodInventoryTab) newTab).getTable();
			}
			else if (newTab instanceof TradeTab) {
				buttonBar.setEnabled(true);
				buttonMap.setEnabled(true);
				buttonDetails.setEnabled(true);
				buttonFilter.setEnabled(true);
				table = ((TradeTab) newTab).getTable();
			}

			this.table = table;
			
			// 2015-09-25 Update skin theme using TableStyle's setTableStyle()
			if (table != null) { // for pie and bar chart, skip the codes below
				// Note: needed for periodic refreshing in ToolWindow
	            //TableStyle.setTableStyle(new RowNumberTable(table));
	            TableStyle.setTableStyle(table);       
				//statusPanel.remove(_tableSearchableBar);
	            if (reloadSearch)
	            	createSearchableBar(table);
			}

			//String status = newTab.getCountString();
			rowCount.setText(newTab.getCountString());


			if (oldTab != null) {
				MonitorModel model = oldTab.getModel();
				if (model != null) oldTab.getModel().removeTableModelListener(this);
			}

			// Set oldTab to newTab
			oldTab = newTab;
		}

		//SwingUtilities.updateComponentTreeUI(this);
	}

    public void createSearchableBar(WebTable table) {
        //System.out.println("Starting createSearchableBar() for "+ table.getName());
    	//Searchable searchable = null;
    	//SearchableBar _tableSearchableBar = null;

    	if (searchable != null)
    		SearchableUtils.uninstallSearchable(searchable);

    	if (table != null) {
    	    //System.out.println("table is " + table.getName());
    	    //SearchableUtils.uninstallSearchable(searchable);
    	    searchable = SearchableUtils.installSearchable(table);
    	    //searchable.setRepeats(true);
    	    searchable.setPopupTimeout(5000);
    	    searchable.setCaseSensitive(false);
    	    searchable.setHideSearchPopupOnEvent(false);
    	    searchable.setWildcardEnabled(true);
    	    searchable.setHeavyweightComponentEnabled(true);
    	    //searchable.setSearchableProvider(searchableProvider)
    	    searchable.setMismatchForeground(java.awt.Color.PINK);
    	    //WildcardSupport WildcardSupport = new WildcardSupport();
    	    //searchable.setWildcardSupport(new WildcardSupport());

    	    if (_tableSearchableBar != null) {
    	        _tableSearchableBar.setSearchingText("");
    	        statusPanel.remove(_tableSearchableBar);
    	        _tableSearchableBar = null;
    	    }

	        _tableSearchableBar = new SearchableBar(searchable);
	        _tableSearchableBar.setSearchingText("");
    	    _tableSearchableBar.setCompact(true);
    	    //_tableSearchableBar.setSearchingText("*" + _tableSearchableBar.getSearchingText());


    	    //_tableSearchableBar.setVisibleButtons(1);
    	    _tableSearchableBar.setToolTipText("Use wildcards (*, +, ?) for searching. e.g. '*DaVinci' ");

    	    ((TableSearchable) searchable).setMainIndex(-1); // -1 = search for all columns
    	    _tableSearchableBar.setVisibleButtons(SearchableBar.SHOW_NAVIGATION
    	    		| SearchableBar.SHOW_MATCHCASE
    	    		| SearchableBar.SHOW_WHOLE_WORDS
    	    		| SearchableBar.SHOW_STATUS);
    	    _tableSearchableBar.setName(table.getName());
    	    _tableSearchableBar.setShowMatchCount(true);
    	    _tableSearchableBar.setVisible(true);

    	    statusPanel.add(_tableSearchableBar); // , BorderLayout.AFTER_LAST_LINE);

    	    //pack();

    	    //statusPanel.add(_tableSearchableBar); // , BorderLayout.AFTER_LAST_LINE);
    	    statusPanel.invalidate();
    	    statusPanel.revalidate();
    	}
    }


	public void tableChanged(TableModelEvent e) {
		if (e.getType() != TableModelEvent.UPDATE) {
			MonitorTab selected = getSelected();
			if (selected == eventsTab) {
				//String status = selected.getCountString();
				rowCount.setText(eventsTab.getCountString());
			}
		}
		//System.out.println("tableChanged()");
	}

	private void addTab(MonitorTab newTab) {
		tabs.add(newTab);
		tabsSection.addTab(newTab.getName(), newTab.getIcon(), newTab); //"", newTab.getIcon(), newTab);//
		tabsSection.setSelectedIndex(tabs.size()-1);
		//tabChanged(true);
	}

	private void removeTab(MonitorTab oldTab) {
		tabs.remove(oldTab);
		tabsSection.remove(oldTab);

		oldTab.removeTab();
		if (getSelected() == oldTab) {
			tabsSection.setSelectedIndex(0);
		}
		//tabChanged(true);
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
	 * Refreshes the table column/row header
	 */
	public void refreshTable() {
		if (table != null) {
	        TableStyle.setTableStyle(table);
	        TableStyle.setTableStyle(new RowNumberTable(table));
	        MonitorTab selected = getSelected();
			if (selected == eventsTab) {
				//String status = selected.getCountString();
				rowCount.setText(eventsTab.getCountString());
			}
		}
	}

	/**
	 * Prepare tool window for deletion.
	 */
	 public void destroy() {
		 Iterator<MonitorTab> i = tabs.iterator();
		 while (i.hasNext()) i.next().removeTab();
		 tabs.clear();
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

}