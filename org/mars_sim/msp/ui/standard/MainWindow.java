/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 2.75 2002-06-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.plaf.metal.*;

/** The MainWindow class is the primary UI frame for the project. It
 *  contains the tool bars and main desktop pane.
 */
public class MainWindow extends JFrame implements WindowListener {

    private static final String VERSION = "2.75";

    // Data members
    private Mars mars;               // The virtual Mars
    private UnitToolBar unitToolbar; // The unit tool bar
    private MainDesktopPane desktop; // The main desktop
    private UIProxyManager proxyManager; // The unit UI proxy manager

    /** Constructs a MainWindow object
     *  @param mars the virtual Mars
     */
    public MainWindow(Mars mars) {

        // use JFrame constructor
        super("Mars Simulation Project (version " + VERSION + ")");

 	// Prepare custom Mars UI theme
	MetalLookAndFeel.setCurrentTheme(new MarsTheme());
    	try {
	    UIManager.setLookAndFeel(new MetalLookAndFeel());
	}
  	catch(UnsupportedLookAndFeelException e) {
	    System.out.println("MainWindow: " + e.toString());
	}
        // Prepare frame
        setVisible(false);
        addWindowListener(this);

        // Prepare menu
        setJMenuBar(new MainWindowMenu(this));

        // Prepare content frame
        JPanel mainPane = new JPanel(new BorderLayout());
        setContentPane(mainPane);

        // Prepare tool toolbar
        ToolToolBar toolToolbar = new ToolToolBar(this);
        mainPane.add(toolToolbar, "North");

        // Prepare unit toolbar
        unitToolbar = new UnitToolBar(this);
        mainPane.add(unitToolbar, "South");

        // Prepare desktop
        desktop = new MainDesktopPane(this);
        mainPane.add(desktop, "Center");

        // Set frame size
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frame_size = new Dimension(screen_size);
        if (screen_size.width > 800) {
            frame_size = new Dimension(
                    (int) Math.round(screen_size.getWidth() * .8D),
                    (int) Math.round(screen_size.getHeight() * .8D));
        }
        setSize(frame_size);

        // Center frame on screen
        setLocation(((screen_size.width - frame_size.width) / 2),
                ((screen_size.height - frame_size.height) / 2));

        setMars(mars);

        // Show frame
        setVisible(true);

    }

    /** Returns the virtual Mars instance
     *  @return the virutal Mars instance
     */
    public Mars getMars() {
        return mars;
    }

    /** Set the virtual Mars instance
     *  @param newMars The new virtual mars instance
     */
    public void setMars(Mars newMars) {

        if (mars != null) {
            mars.stop();
        }

        // Initialize data members
        mars = newMars;

        // Create unit UI proxy manager.
        proxyManager = new UIProxyManager(mars.getUnitManager().getUnits());

        desktop.setProxyManager(proxyManager);
    }

    /** Create a new unit button in toolbar
     *  @param unitUIProxy the unit UI Proxy
     */
    public void createUnitButton(UnitUIProxy unitUIProxy) {
        unitToolbar.createUnitButton(unitUIProxy);
    }

    /** Return true if tool window is open
     *  @param the name of the tool window
     *  @return true if tool window is open
     */
    public boolean isToolWindowOpen(String toolName) {
        return desktop.isToolWindowOpen(toolName);
    }

    /** Finds a tool window
     *  @param toolName the name of the tool window
     */
    public ToolWindow getToolWindow(String toolName) {
        return desktop.getToolWindow(toolName);
    }

    /**
     * Load a previously saved simulation
     */
    public void loadSimulation() {
        try {
            JFileChooser chooser = new JFileChooser(Mars.DEFAULT_DIR);
            chooser.setDialogTitle("Selected stored simulation");
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                Mars newmars = Mars.load(chooser.getSelectedFile());
                if (newmars != null) {
                    setMars(newmars);
                    newmars.start();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Problem loading simulation",
                        e.toString(), JOptionPane.ERROR_MESSAGE);
	    }
    }

    /**
     * Create a new simulation to execute. This displays the new simulation
     * dialog.
     */
    public void newSimulation() {

        SimulationProperties p = mars.getSimulationProperties();
	    NewDialog newDialog = new NewDialog(p, this);
	    if(newDialog.getResult() == JOptionPane.OK_OPTION) {
		    // ##TODO## this should be shifted into a separate thread
		    ProgressMonitor pm = new ProgressMonitor(this,
					"Starting New Simulation...", "",
					0, 100);
		    pm.setMillisToPopup(0);
		    pm.setMillisToDecideToPopup(0);
		    Mars newmars = new Mars(p);
		    pm.setProgress(50);
		    setMars(newmars);
		    newmars.start();
		    pm.close();
        }
    }

    /**
     * Save the current simulation. This display a FileChooser to select the
     * location to save the simulation if the default is not to be used.
     *
     * @param useDefault Shoul dhte user be allowed to override location.
     */
    public void saveSimulation(boolean useDefault) {
        File fileLocn = null;

        if (!useDefault) {
            JFileChooser chooser = new JFileChooser(Mars.DEFAULT_DIR);
            chooser.setDialogTitle("Selected storage location");
            int returnVal = chooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fileLocn = chooser.getSelectedFile();
            }
            else {
                return;
            }
        }

        // Attempt a save
        try {
            mars.store(fileLocn);
        }
        catch(Exception e) {
            e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Problem saving simualtion",
                        e.toString(), JOptionPane.ERROR_MESSAGE);
	    }
    }

    /** Opens a tool window if necessary
     *  @param toolName the name of the tool window
     */
    public void openToolWindow(String toolName) {
        desktop.openToolWindow(toolName);
    }

    /** Closes a tool window if it is open
     *  @param the name of the tool window
     */
    public void closeToolWindow(String toolName) {
        desktop.closeToolWindow(toolName);
    }

    /** Opens a window for a unit if it isn't already open Also makes
      *  a new unit button in toolbar if necessary
      *  @param unitUIProxy the unit UI proxy
      */
    public void openUnitWindow(UnitUIProxy unitUIProxy) {
        desktop.openUnitWindow(unitUIProxy);
    }

    /** Disposes a unit window and button
     *  @param unitUIProxy the unit UI proxy
     */
    public void disposeUnitWindow(UnitUIProxy unitUIProxy) {
        desktop.disposeUnitWindow(unitUIProxy);
    }

    /** Disposes a unit button in toolbar
     *  @param unitUIProxy the unit UI proxy
     */
    public void disposeUnitButton(UnitUIProxy unitUIProxy) {
        unitToolbar.disposeUnitButton(unitUIProxy);
    }

    // WindowListener methods overridden
    public void windowClosing(WindowEvent event) {
        exitSimulation();
    }

    /**
     * Exit the simulation for running and exit.
     */
    public void exitSimulation() {
        try {
            mars.store(null);
        }
        catch(Exception e) {
            System.out.println("Problem saving simulation " + e);
        }
        System.exit(0);
    }

    public void windowClosed(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}
}

