/**
 * Mars Simulation Project
 * MonitorWindow.java
 * @version 3.00 2010-08-10
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

/** The MonitorWindow is a tool window that displays a selection of tables
 *  each of which monitor a set of Units.
 */
public class MonitorWindow extends ToolWindow implements TableModelListener {

	// Tool name
	public static final String NAME = "Monitor Tool";
	
    final private static int STATUSHEIGHT = 17;

    // Data members
    private JTabbedPane tabsSection;
    private JLabel rowCount;
    private ArrayList<MonitorTab> tabs = new ArrayList<MonitorTab>();
    private EventTab eventsTab; // Tab showing historical events.
    private MonitorTab oldTab = null;
    private JButton mapButton;
    private JButton detailsButton;
    private JButton missionButton;
    private JButton filterButton;


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public MonitorWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super(NAME, desktop);
        
        setMaximizable(true);

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new MarsPanelBorder());
        setContentPane(mainPane);

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        mainPane.add(toolbar, "North");


        // Create graph button
        JButton pieButton = new JButton(PieChartTab.PIEICON);
        pieButton.setToolTipText("Create a Pie chart of a single column.");
        pieButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            createPieChart();
                        }
                    });

        toolbar.add(pieButton);

        JButton barButton = new JButton(BarChartTab.BARICON);
        barButton.setToolTipText("Create a Bar chart of multiple columns.");
        barButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            createBarChart();
                        }
                    });
        toolbar.add(barButton);

        JButton tabRemove = new JButton(ImageLoader.getIcon("TabRemove"));
        tabRemove.setToolTipText("Remove selected tab.");
        tabRemove.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            MonitorTab selected = getSelected();
                            if (!selected.getMandatory()) {
                                removeTab(getSelected());
                            }
                        }
                    });
        toolbar.add(tabRemove);
        toolbar.addSeparator();

        // Create buttons based on selection
        mapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        mapButton.setMargin(new Insets(3, 4, 4, 4));
        mapButton.setToolTipText("Locate selected unit in Mars Navigator.");
        mapButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            centerMap();
                        }
                });
        toolbar.add(mapButton);

        detailsButton = new JButton(ImageLoader.getIcon("ShowDetails"));
        detailsButton.setToolTipText("Open unit window for selected unit.");
        detailsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayDetails();
                        }
                    });
        toolbar.add(detailsButton);
        
        missionButton = new JButton(ImageLoader.getIcon("Mission"));
        missionButton.setToolTipText("Open selected mission in mission tool.");
        missionButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayMission();
                        }
                    });
        toolbar.add(missionButton);
        toolbar.addSeparator();

        JButton propsButton = new JButton(ImageLoader.getIcon("Preferences"));
        propsButton.setToolTipText("Change the displayed columns.");
        propsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayProps();
                        }
                    });
        toolbar.add(propsButton);
        toolbar.addSeparator();

		filterButton = new JButton(ImageLoader.getIcon("CategoryFilter"));
		filterButton.setToolTipText("Filter historical events by category.");
		filterButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							filterCategories();
						}
					});
		toolbar.add(filterButton);

        // Create tabbed pane for the table
        tabsSection = new JTabbedPane();
        mainPane.add(tabsSection, "Center");

        // Create a status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        mainPane.add(statusPanel, "South");

        // Status item for row
        rowCount = new JLabel("  ");
        rowCount.setHorizontalAlignment(SwingConstants.LEFT);
        rowCount.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(rowCount);
        Dimension dims = new Dimension(150, STATUSHEIGHT);
        rowCount.setPreferredSize(dims);

        // Add the default table tabs
        UnitManager unitManager = Simulation.instance().getUnitManager();
        addTab(new UnitTab(new PersonTableModel(unitManager), true));
        addTab(new UnitTab(new VehicleTableModel(unitManager), true));
        addTab(new UnitTab(new SettlementTableModel(unitManager), true));
        addTab(new MissionTab());
        eventsTab = new EventTab(new EventTableModel(Simulation.instance().getEventManager()));
        addTab(eventsTab);
        addTab(new TradeTab());

        tabsSection.setSelectedIndex(0);
        tabChanged();

        // Add a listener for the tab changes
        tabsSection.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        tabChanged();
                    }
                }
        );

        // Have to define a starting size
        setSize(new Dimension(600, 300));
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
    	else addTab(new UnitTab(model, false));
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
     * currently selected window. The chart is added as a seperate tab to the
     * window.
     */
    private void createBarChart() {
        MonitorModel model = getSelected().getModel();

        // Show modal column selector
        int columns[] = ColumnSelector.createBarSelector(desktop.getMainWindow(), model);
        if (columns.length > 0) {
            addTab(new BarChartTab(model, columns));
        }
    }

    private void createPieChart() {
        MonitorModel model = getSelected().getModel();

        // Show modal column selector
        int column = ColumnSelector.createPieSelector(desktop.getMainWindow(), model);
        if (column >= 0) {
            addTab(new PieChartTab(model, column));
        }
    }
    /**
     * Return the currently selected tab.
     *
     * @return Monitor tab being displayed.
     */
    private MonitorTab getSelected() {
        MonitorTab selected = null;
        int selectedIdx = tabsSection.getSelectedIndex();
        if ((selectedIdx != -1) && (selectedIdx < tabs.size())) 
            selected = tabs.get(selectedIdx);
        return selected;
    }

    private void tabChanged() {
        MonitorTab selected = getSelected();
        if (selected != null) {
            String status = selected.getCountString();
            rowCount.setText(status);
            if (oldTab != null) {
            	MonitorModel model = oldTab.getModel();
            	if (model != null) oldTab.getModel().removeTableModelListener(this);
            }
            selected.getModel().addTableModelListener(this);
            oldTab = selected;
            
            // Enable/disable buttons based on selected tab.
            mapButton.setEnabled(false);
            detailsButton.setEnabled(false);
            missionButton.setEnabled(false);
            filterButton.setEnabled(false);
            
            if (selected instanceof UnitTab) {
            	mapButton.setEnabled(true);
            	detailsButton.setEnabled(true);
            }
            else if (selected instanceof MissionTab) {
            	mapButton.setEnabled(true);
            	missionButton.setEnabled(true);
            }
            else if (selected instanceof EventTab) {
            	mapButton.setEnabled(true);
            	detailsButton.setEnabled(true);
            	filterButton.setEnabled(true);
            }
        }
    }
    
    public void tableChanged(TableModelEvent e) {
    	if (e.getType() != TableModelEvent.UPDATE) {
    		MonitorTab selected = getSelected();
    		if (selected != null) {
    			String status = selected.getCountString();
    			rowCount.setText(status);
    		}
    	}
    }

    private void addTab(MonitorTab newTab) {
        tabs.add(newTab);
        tabsSection.addTab(newTab.getName(), newTab.getIcon(), newTab);
        tabsSection.setSelectedIndex(tabs.size()-1);
        tabChanged();
    }

    private void removeTab(MonitorTab oldTab) {
        tabs.remove(oldTab);
        tabsSection.remove(oldTab);

        oldTab.removeTab();
        if (getSelected() == oldTab) {
            tabsSection.setSelectedIndex(0);
        }
        tabChanged();
    }

    private void centerMap() {
        MonitorTab selected = getSelected();
        if (selected != null) {
            selected.centerMap(desktop);
        }
    }

    private void displayDetails() {
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
    
    /**
     * Prepare tool window for deletion.
     */
    public void destroy() {
    	Iterator<MonitorTab> i = tabs.iterator();
    	while (i.hasNext()) i.next().removeTab();
    	tabs.clear();
    }
}