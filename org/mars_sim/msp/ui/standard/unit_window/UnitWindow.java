/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 2.75 2003-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.*;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends JInternalFrame implements Runnable {
    
    // Data members
    protected MainDesktopPane desktop; // Main window
    protected UnitUIProxy proxy;       // Unit's UI proxy
    private Collection tabPanels;      // The tab panels
    private JTabbedPane centerPanel;   // The center panel
    private Thread updateThread; // temp update thread
    private boolean keepUpdated; // temp
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param proxy the unit UI proxy for this window.
     * @param displayDescription true if unit description is to be displayed.
     */
    public UnitWindow(MainDesktopPane desktop, UnitUIProxy proxy, boolean displayDescription) {
        
        // Use JInternalFrame constructor
        super(proxy.getUnit().getName(), false, true, false, true);

        // Initialize data members
        this.desktop = desktop;
        this.proxy = proxy;
        tabPanels = new ArrayList();
        
        // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        // Create name panel
        JPanel namePanel = new JPanel(new BorderLayout(0, 0));
        mainPane.add(namePanel, BorderLayout.NORTH);

        // Create name label
        JLabel nameLabel = new JLabel(proxy.getUnit().getName(),
            proxy.getButtonIcon(), JLabel.CENTER);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        namePanel.add(nameLabel, BorderLayout.NORTH);
        
        // Create description label if necessary.
        if (displayDescription) {
            JLabel descriptionLabel = new JLabel(proxy.getUnit().getDescription(), JLabel.CENTER);
            namePanel.add(descriptionLabel, BorderLayout.SOUTH);
        }
        
        // Create center panel
        centerPanel = new JTabbedPane();
        mainPane.add(centerPanel, BorderLayout.CENTER);
        
        // Remove later
        start();
    }
    
    /**
     * Adds a tab panel to the center panel.
     *
     * @param panel the tab panel to add.
     */
    protected final void addTabPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            centerPanel.addTab(panel.getTabTitle(), panel.getTabIcon(), 
                panel, panel.getTabToolTip());
        }
    }
     
    /**
     * Gets the proxy for this window.
     *
     * @return unit UI proxy
     */
    public UnitUIProxy getProxy() {
        return proxy;
    }
    
    /**
     * Updates this window.
     */
    protected void update() {
        
        // Update each of the tab panels.
        Iterator i = tabPanels.iterator();
        while (i.hasNext()) {
            TabPanel panel = (TabPanel) i.next();
            panel.update();
        }
    }
    
    // Remove later
    private void start() {

        keepUpdated = true;
        if ((updateThread == null) || !updateThread.isAlive()) {
            updateThread = new Thread(this, "unit window : " + proxy.getUnit().getName());
            updateThread.start();
        }
        else {
            updateThread.interrupt();
        }
    }
    
    public void run() {

        // Endless refresh loop
        while (keepUpdated) {
            
            // Pause for 2 seconds between display refresh if visible
            // otherwise just wait a long time
            try {
                // long sleeptime = (isVisible() ? 2000 : 60000);
                long sleeptime = 2000;
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {}

            // Update display
            update();
        }
        
        updateThread = null;
    }
}
