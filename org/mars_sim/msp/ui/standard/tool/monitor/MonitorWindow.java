/**
 * Mars Simulation Project
 * MonitorWindow.java
 * @version 2.76 2004-06-02
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

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/** The MonitorWindow is a tool window that displays a selection of tables
 *  each of which monitor a set of Units.
 */
public class MonitorWindow extends ToolWindow implements Runnable {

    final private static int STATUSHEIGHT = 17;
    final private static int REFRESH_PERIOD = 3000;


    // Data members
    private JTabbedPane tabsSection;
    private JLabel rowCount;
    private ArrayList tabs = new ArrayList();
    private Thread updateThread;     // Model update thread
    private EventTab eventsTab; // Tab showing historical events.


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public MonitorWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super("Monitor Tool", desktop);
        
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
        pieButton.setToolTipText("Create a Pie chart of a single column");
        pieButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            createPieChart();
                        }
                    });

        toolbar.add(pieButton);

        JButton barButton = new JButton(BarChartTab.BARICON);
        barButton.setToolTipText("Create a Bar chart of multiple columns");
        barButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            createBarChart();
                        }
                    });
        toolbar.add(barButton);

        JButton tabRemove = new JButton(ImageLoader.getIcon("TabRemove"));
        tabRemove.setToolTipText("Remove selected tab");
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
        mapButton.setToolTipText("Center map on selected unit");
        mapButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            centerMap();
                        }
                });
        toolbar.add(mapButton);

        JButton detailsButton = new JButton(ImageLoader.getIcon("ShowDetails"));
        detailsButton.setToolTipText("Show details dialog");
        detailsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayDetails();
                        }
                    });
        toolbar.add(detailsButton);
        toolbar.addSeparator();

        JButton propsButton = new JButton(ImageLoader.getIcon("Preferences"));
        propsButton.setToolTipText("Change the displayed columns");
        propsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            displayProps();
                        }
                    });
        toolbar.add(propsButton);
        toolbar.addSeparator();

		JButton filterButton = new JButton(ImageLoader.getIcon("CategoryFilter"));
		filterButton.setToolTipText("Filter events by category");
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
        addTab(new TableTab(new PersonTableModel(unitManager), true));
        addTab(new TableTab(new VehicleTableModel(unitManager), true));
        addTab(new TableTab(new SettlementTableModel(unitManager), true));
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

        // Start a thread to keep the displayed model updated
        start();
    }

    /**
     * This method add the specified Unit table as a new tab in the Monitor. The
     * model is displayed as a table by default.
     * The name of the tab is that of the Model.
     *
     * @param model The new model to display.
     */
    public void displayModel(UnitTableModel model) {
        addTab(new TableTab(model, false));
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
            String status = selected.update();
            rowCount.setText(status);
        }
    }

    private void addTab(MonitorTab newTab) {
        tabs.add(newTab);
        tabsSection.addTab(newTab.getName(), newTab.getIcon(), newTab);
        tabsSection.setSelectedIndex(tabs.size()-1);
    }

    private void removeTab(MonitorTab oldTab) {
        tabs.remove(oldTab);
        tabsSection.remove(oldTab);

        oldTab.removeTab();
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

    private void displayDetails() {
        MonitorTab selected = getSelected();
        if (selected != null) {
            selected.displayDetails(desktop);
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
    
    /** Starts display update thread, and creates a new one if necessary */
    public void start() {
        if ((updateThread == null) || (!updateThread.isAlive())) {
            updateThread = new Thread(this, "monitor window");
            updateThread.start();
        }
    }

    /** Update thread runner */
    public void run() {

        // Endless refresh loop
        while (true) {

            // Pause for 1 second between display refreshes
            try {
                Thread.sleep(REFRESH_PERIOD);
            }
            catch (InterruptedException e) {
            }

            // Update window
            MonitorTab selected = getSelected();
            if (selected != null) {
                String status = selected.update();
                rowCount.setText(status);
            }
        }
    }
}
