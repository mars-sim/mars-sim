/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.mars_sim.msp.simulation.*;

/** 
 * The MainWindow class is the primary UI frame for the project. It
 * contains the tool bars and main desktop pane.
 */
public class MainWindow extends JFrame implements WindowListener {

    private static final String VERSION = "2.76";

    // Data members
    private UnitToolBar unitToolbar; // The unit tool bar
    private MainDesktopPane desktop; // The main desktop

    /** 
     * Constructor
     */
    public MainWindow() {

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

        // Show frame
        setVisible(true);
    }
    
    /**
     * Gets the main desktop panel.
     *
     * @return desktop
     */
    public MainDesktopPane getDesktop() {
        return desktop;
    }

    /**
     * Load a previously saved simulation.
     */
    public void loadSimulation() {
        try {
            JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
            chooser.setDialogTitle("Selected stored simulation");
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
            	Simulation simulation = Simulation.instance();
            	simulation.loadSimulation(chooser.getSelectedFile());
            	desktop.resetDesktop();
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
	    try {
			Simulation.createNewSimulation();
           	desktop.resetDesktop();
	    }
	    catch (Exception e) {
	    	System.err.println("Problem creating new simulation: " + e);
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
            JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
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
        	Simulation.instance().saveSimulation(fileLocn);
        }
        catch(Exception e) {
            e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Problem saving simualtion",
				e.toString(), JOptionPane.ERROR_MESSAGE);
	    }
    }
    
    /** 
     * Create a new unit button in toolbar.
     *
     * @param unit the unit the button is for.
     */
    public void createUnitButton(Unit unit) {
        unitToolbar.createUnitButton(unit);
    }
    
    /** 
     * Disposes a unit button in toolbar.
     *
     * @param unit the unit to dispose.
     */
    public void disposeUnitButton(Unit unit) {
        unitToolbar.disposeUnitButton(unit);
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
        	Simulation sim = Simulation.instance();
        	sim.saveSimulation(null);
        	sim.stop();
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
