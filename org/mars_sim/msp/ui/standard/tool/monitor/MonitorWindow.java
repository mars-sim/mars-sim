/**
 * Mars Simulation Project
 * MonitorWindow.java
 * @version 2.76 2006-08-26
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

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
    private ArrayList tabs = new ArrayList();
    private EventTab eventsTab; // Tab showing historical events.
    private MonitorTab oldTab = null;


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public MonitorWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super(NAME, desktop);
        
        setMaximizable(true);

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
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
        JButton mapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        mapButton.setMargin(new Insets(3, 4, 4, 4));
        mapButton.setToolTipText("Locate selected unit in Mars navigator.");
        mapButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            centerMap();
                        }
                });
        toolbar.add(mapButton);

        JButton detailsButton = new JButton(ImageLoader.getIcon("ShowDetails"));
        detailsButton.setToolTipText("Open unit dialog for selected unit.");
        detailsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayDetails();
                        }
                    });
        toolbar.add(detailsButton);
        
        JButton missionButton = new JButton(ImageLoader.getIcon("Mission"));
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

		JButton filterButton = new JButton(ImageLoader.getIcon("CategoryFilter"));
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
        Dimension dims = new Dimension(100, STATUSHEIGHT);
        rowCount.setPreferredSize(dims);

        // Add the default table tabs
        UnitManager unitManager = Simulation.instance().getUnitManager();
        addTab(new TableTab(new PersonTableModel(unitManager), true, false));
        addTab(new TableTab(new VehicleTableModel(unitManager), true, false));
        addTab(new TableTab(new SettlementTableModel(unitManager), true, false));
        addTab(new MissionTab());
        eventsTab = new EventTab(new EventTableModel(Simulation.instance().getEventManager()));
        addTab(eventsTab);

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
    	else addTab(new TableTab(model, false, false));
    }
    
    /**
     * Checks if a monitor tab contains this model.
     * @param model the model to check for.
     * @return true if a tab contains the model.
     */
    public boolean containsModel(UnitTableModel model) {
    	boolean result = false;
    	Iterator i = tabs.iterator();
    	while (i.hasNext()) {
    		MonitorTab tab = (MonitorTab) i.next();
    		if (tab.getModel().equals(model)) result = true;
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
    	Iterator i = tabs.iterator();
    	while (i.hasNext()) {
    		MonitorTab tab = (MonitorTab) i.next();
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
        if (selectedIdx != -1) {
            selected = (MonitorTab)tabs.get(selectedIdx);
        }
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
    	Iterator i = tabs.iterator();
    	while (i.hasNext()) ((MonitorTab) i.next()).removeTab();
    	tabs.clear();
    }
}