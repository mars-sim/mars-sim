/**
 * Mars Simulation Project
 * UnitDialog.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** The UnitDialog class is an abstract UI detail window for a given
 *  unit. It displays information about the unit and its current
 *  status. It is abstract and detail windows for particular types of
 *  units need to be derived from this class.
 */
public abstract class UnitDialog extends JInternalFrame implements Runnable,
        ActionListener {

    // Data members
    protected MainDesktopPane parentDesktop; // Parent Main Window
    protected UnitUIProxy unitUIProxy; // Parent unit's UI proxy
    protected UIProxyManager proxyManager;  // Unit UI proxy manager
    protected JButton unitButton; // Button for unit detail window
    protected Unit parentUnit; // Parent unit for which detail window is about
    protected JPanel mainPane; // Content pane
    protected Box mainVerticalBox; // Main layout box
    protected Thread updateThread; // Dialog update thread
    private boolean keepUpdated;  // Keep this update thread running
    protected JButton centerMapButton; // Center map button

    /** Constructs a UnitDialog class
     *  @param parentDesktop the desktop pane
     *  @param unitUIProxy the unit's UI proxy
     */
    public UnitDialog(MainDesktopPane parentDesktop, UnitUIProxy unitUIProxy) {

        // Use JInternalFrame constructor
        super(unitUIProxy.getUnit().getName(), false, true, false, true);

        // Initialize data members
        this.parentDesktop = parentDesktop;
        this.unitUIProxy = unitUIProxy;
        this.proxyManager = parentDesktop.getProxyManager();
        this.parentUnit = unitUIProxy.getUnit();

        // Initialize cached data members
        initCachedData();

        // Prepare frame
        startGUISetup();
        setupComponents();
        finishGUISetup();

        // Do first update
        generalUpdate();
    }

    /**
     * This window is closing so stop the Thread and nullify so that it can be
     * garbage collected.
     */
    public void dispose() {
        keepUpdated = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
        updateThread = null;
        super.dispose();
    }

    /** Visible method **/
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            start();
        }
    }

    /** Starts display update thread, and creates a new one if necessary */
    private void start() {

        keepUpdated = true;
        if ((updateThread == null) || !updateThread.isAlive()) {
            updateThread = new Thread(this, "unit dialog : " +
                                      unitUIProxy.unit.getName());
            updateThread.start();
        }
        else {
            updateThread.interrupt();
        }

    }


    /** Update thread runner */
    public void run() {

        // Endless refresh loop
        while (keepUpdated) {

            // Pause for 2 seconds between display refresh if visible
            // otherwise just wait a long time
            try {
                long sleeptime = (isVisible() ? 2000 : 60000);
                updateThread.sleep(sleeptime);
            } catch (InterruptedException e) {}

            // Update display
            try {
                generalUpdate();
            } catch (NullPointerException e) {
                System.out.println("NullPointerException: " + parentUnit.getName());
            }
        }
        updateThread = null;
    }

    /** Returns unit's name
     *  @return unit's name
     */
    public String getUnitName() { return parentUnit.getName(); }

    /** Returns the unit for this window
     *  @return unit
     */
    public Unit getUnit() { return parentUnit; }

    /** Returns the unitProxy for this window
     *  @return unitProxy
     */
    public UnitUIProxy getUnitProxy() { return unitUIProxy; }

    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
        Object button = event.getSource();

        // If center map button, center map and globe on unit
        if (button == centerMapButton) {
            parentDesktop.centerMapGlobe(parentUnit.getCoordinates());
        }
    }

    /** Initialize cached data members */
    protected void initCachedData() {}

    /** Start creating window */
    protected void startGUISetup() {
        // Don't show window until finished
        setVisible(false);

        // Set default font
        //setFont(new Font("SansSerif", Font.BOLD, 10));

        // Prepare content pane
        mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
    }

    /** Finish creating window */
    protected void finishGUISetup() {
        // Properly size window
        setSize(setWindowSize());
    }

    /** Prepare and add components to window */
    protected void setupComponents() {

        // Prepare name panel
        JPanel namePanel = new JPanel();
        mainPane.add(namePanel, "North");

        // Prepare name label
        JLabel nameLabel = new JLabel(parentUnit.getName(),
                unitUIProxy.getButtonIcon(), JLabel.CENTER);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        namePanel.add(nameLabel);
    }

    // --- Abstract methods ---

    /** Set up proper window size
     *  @return the window's size
     */
    protected abstract Dimension setWindowSize();

    /** Complete update */
    protected abstract void generalUpdate();

    /** Returns a double value rounded to one decimal point
     *  @param initial the initial double value
     *  @return the rounded value
     */
	public double roundOneDecimal(double initial) {
		return (double) (Math.round(initial * 10D) / 10D);
	}
}
