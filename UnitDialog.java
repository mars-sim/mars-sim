/**
 * Mars Simulation Project
 * UnitDialog.java
 * @version 2.70 2000-09-05
 * @author Scott Davis
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** The UnitDialog class is an abstract UI detail window for a given
 *  unit. It displays information about the unit and its current
 *  status. It is abstract and detail windows for particular types of
 *  units need to be derived from this class. 
 */
public abstract class UnitDialog extends JInternalFrame implements Runnable, ActionListener {

    protected MainDesktopPane parentDesktop;  // Parent Main Window
    protected JButton unitButton;             // Button for unit detail window
    protected Unit parentUnit;                // Parent unit for which detail window is about
    protected JPanel mainPane;                // Content pane
    protected Box mainVerticalBox;            // Main layout box
    protected Thread updateThread;            // Dialog update thread
    protected JButton centerMapButton;        // Center map button


    public UnitDialog(MainDesktopPane parentDesktop, Unit parentUnit) {
	// Use JInternalFrame constructor
	super(parentUnit.getName(), false, true, false, true);
		
	// Initialize data members
	this.parentDesktop = parentDesktop;
	this.parentUnit = parentUnit;
		
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
	
    // Starts display update thread, and creates a new one if necessary
    public void start() {
	if ((updateThread == null) || (!updateThread.isAlive())) {
	    updateThread = new Thread(this, "unit dialog");
	    updateThread.start();
	}
    }
	
    // Update thread runner
    public void run() {
	
	// Endless refresh loop
	while(true) { 
			
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
	
    /** Returns unit's name */
    public String getUnitName() {
	return parentUnit.getName();
    }
	
    /** Returns unit ID number */
    public int getUnitID() {
	return parentUnit.getID();
    }
	
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
	setFont(new Font("Helvetica", Font.BOLD, 12));
		
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
	JLabel nameLabel = new JLabel(parentUnit.getName(), getIcon(), JLabel.CENTER);
	nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
	nameLabel.setHorizontalTextPosition(JLabel.CENTER);
	nameLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
	nameLabel.setForeground(Color.black);
	namePanel.add(nameLabel);
    }

    // --- Abstract methods ---

    /** Set up proper window size */
    protected abstract Dimension setWindowSize();
	
    /** Load image icon */
    public abstract ImageIcon getIcon();
	
    /** Complete update */
    protected abstract void generalUpdate();
}
