/**
 * Mars Simulation Project
 * UnitDialog.java
 * @version 2.71 2000-10-23
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

        // Start update thread
        start();
    }

    /** Starts display update thread, and creates a new one if necessary */
    public void start() {
        if ((updateThread == null) || (!updateThread.isAlive())) {
            updateThread = new Thread(this, "unit dialog");
            updateThread.start();
        }
    }

    /** Update thread runner */
    public void run() {

        // Endless refresh loop
        while (true) {

            // Pause for 2 seconds between display refreshs
            try {
                updateThread.sleep(2000);
            } catch (InterruptedException e) {}

            // Update display
            try {
                generalUpdate();
            } catch (NullPointerException e) {
                System.out.println("NullPointerException: " + parentUnit.getName());
            }
        }
    }

    /** Returns unit's name 
     *  @return unit's name
     */
    public String getUnitName() { return parentUnit.getName(); }
    
    /** Returns the unit for this window 
     *  @return unit
     */
    public Unit getUnit() { return parentUnit; }

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
        // nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(Color.black);
        namePanel.add(nameLabel);
	// System.out.println("nameLabel font: " + nameLabel.getFont().getName());
	// System.out.println("nameLabel size: " + nameLabel.getFont().getSize());
    }

    // --- Abstract methods ---

    /** Set up proper window size 
     *  @return the window's size
     */
    protected abstract Dimension setWindowSize();
    
    /** Complete update */
    protected abstract void generalUpdate();
}
