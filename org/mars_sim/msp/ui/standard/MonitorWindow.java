/**
 * Mars Simulation Project
 * TableWindow.java
 * @version 2.72 2001-10-24
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/** The MonitorWindow is a tool window that displays a selection of tables
 *  each of which monitor a set of Units.
 */
public class MonitorWindow extends ToolWindow
implements Runnable {

    final private static String ROWSUFFIX = " items";
    final private static int STATUSHEIGHT = 17;

    /**
     * This class represents a single table displayed in the tabs section
     * of the monitor. Each one has a single UnitTableModel associated.
     */
    class TableTab {
        private JTable table;            // Table component
        private UnitTableModel model;    // Mode providing the data

        /**
         * Create a Jtable within a tab displaying the specified model.
         * @param parent Tabbed pane to hold the table.
         * @param model The model of Units to display.
         */
        public TableTab(JTabbedPane parent, UnitTableModel model) {
            this.model = model;

            // Create a panel
            JPanel mainPane = new JPanel(new BorderLayout());
            mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));

            // Create scrollable table window
            table = new JTable(model) {
                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
                 */
                public String getToolTipText(MouseEvent e) {
                    return getCellText(e);
                };
            };


            // Add a scrolled window and center it with the table
            JScrollPane scroller = new JScrollPane(table);
            scroller.setBorder(new EtchedBorder());
            mainPane.add(scroller, "Center");
            parent.add(model.getName(), mainPane);

            model.addAll();
        }

        /**
         * Display details for selected rows
         */
        public void displayDetails() {
            ArrayList units = model.getUnits(table.getSelectedRows());
            Iterator it = units.iterator();
            while(it.hasNext()) {
                desktop.openUnitWindow((UnitUIProxy)it.next());
            }
        }

        /**
         * Display property window
         */
        public void displayProps() {
            MonitorPropsDialog propsWindow =
                      new MonitorPropsDialog(model.getName(), table,
                                             desktop);
            propsWindow.show();
        }

        /**
         * Remove row action has been triggered
         */
        public void removeRows() {
            Collection units = model.getUnits(table.getSelectedRows());
            model.remove(units);
        }

        /**
         * Update the selected table
         */
        public void update() {
            model.update();
        }

        /**
         * Get the cell contents under the MouseEvent, this will be displayed
         * as a tooltip.
         * @param e MouseEvent triggering tool tip.
         * @return Tooltip text.
         */
        private String getCellText(MouseEvent e) {
            Point p = e.getPoint();
            int column = table.columnAtPoint(p);
            int row = table.rowAtPoint(p);
            String result = null;
            if ((column >= 0) && (row >= 0)) {
                Object cell = table.getValueAt(row, column);
                if (cell != null) {
                    result = cell.toString();
                }
            }
            return result;
        }

        /**
         * Get the associated model.
         * @return UnitTableModel associated to the tab.
         */
        public UnitTableModel getModel() {
            return model;
        }
    }

    // Data members
    private MainDesktopPane desktop; // Desktop pane
    private JTabbedPane tabsSection;
    private JLabel rowCount;
    private ArrayList tabs = new ArrayList();
    private Thread updateThread;     // Model update thread


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public MonitorWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super("Monitor Tool");

        // Set internal frame listener
        addInternalFrameListener(new ViewFrameListener());

        // Initialize data members
        this.desktop = desktop;

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        mainPane.add(toolbar, "North");


        // Create remove button
        JButton loadButton = new JButton(new ImageIcon("images/Reload.gif"));
        loadButton.setToolTipText("Load all matching units");
        loadButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            TableTab selected = getSelected();
                            if (selected != null) {
                                selected.getModel().addAll();
                            }
                        }
                    });
        toolbar.add(loadButton);

        JButton detailsButton = new JButton(new ImageIcon("images/ShowDetails.gif"));
        detailsButton.setToolTipText("Show details dialog");
        detailsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            TableTab selected = getSelected();
                            if (selected != null) {
                                selected.displayDetails();
                            }
                        }
                    });
        toolbar.add(detailsButton);

        // Create remove button
        JButton removeButton = new JButton(new ImageIcon("images/RowDelete.gif"));
        removeButton.setToolTipText("Remove selected rows");
        removeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            TableTab selected = getSelected();
                            if (selected != null) {
                                selected.removeRows();
                            }
                        }
                    });
        toolbar.add(removeButton);

        JButton propsButton = new JButton(new ImageIcon("images/Preferences.gif"));
        propsButton.setToolTipText("Change the displayed columns");
        propsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            TableTab selected = getSelected();
                            if (selected != null) {
                                selected.displayProps();
                            }
                        }
                    });
        toolbar.add(propsButton);

        // Create tabbed pane for the table
        tabsSection = new JTabbedPane();
        mainPane.add(tabsSection, "Center");

        // Create a status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        mainPane.add(statusPanel, "South");

        // Status item for row
        rowCount = new JLabel(ROWSUFFIX);
        rowCount.setHorizontalAlignment(SwingConstants.LEFT);
        rowCount.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(rowCount);
        Dimension dims = new Dimension(100, STATUSHEIGHT);
        rowCount.setPreferredSize(dims);

        // Add the default table tabs
        UIProxyManager proxyManager = desktop.getProxyManager();
        addTab(new PersonTableModel(proxyManager));
        addTab(new VehicleTableModel(proxyManager));
        addTab(new SettlementTableModel(proxyManager));
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
     * This method add the specified Unit table as a new tab in the Monitor.
     * The name of the tab is that of the Model.
     * @param model The new model.
     */
    public void addTab(UnitTableModel model) {
        tabs.add(new TableTab(tabsSection, model));
        tabsSection.setSelectedIndex(tabs.size()-1);
    }

    private TableTab getSelected() {
        TableTab selected = null;
        int selectedIdx = tabsSection.getSelectedIndex();
        if (selectedIdx != -1) {
            selected = (TableTab)tabs.get(selectedIdx);
        }
        return selected;
    }

    private void tabChanged() {
        TableTab selected = getSelected();
        if (selected != null) {
            rowCount.setText("  " + selected.getModel().getRowCount() +
                             ROWSUFFIX);
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
                updateThread.sleep(1000);
            }
            catch (InterruptedException e) {
            }

            // Update window
            TableTab selected = getSelected();
            if (selected != null) {
                selected.update();
            }
        }
    }
}
