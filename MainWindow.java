/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MainWindow class is the primary UI frame for the project. It
 *  contains the tool bars and main desktop pane.
 */
public class MainWindow extends JFrame implements WindowListener {

    private VirtualMars mars;        // The virtual Mars
    private UnitToolBar unitToolbar; // The unit tool bar
    private MainDesktopPane desktop; // The main desktop

    public MainWindow(VirtualMars mars) {
		
	// use JFrame constructor
	super("Mars Simulation Project (version 2.7)");

	// Initialize data member to parameter.
	this.mars = mars;

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
	mainPane.add(toolToolbar, "West");
		
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
	    frame_size = new Dimension((int) Math.round(screen_size.getWidth() * .8D),
				       (int) Math.round(screen_size.getHeight() * .8D));
	}
	setSize(frame_size);

	// Center frame on screen
	setLocation(((screen_size.width - frame_size.width) / 2),
		    ((screen_size.height - frame_size.height) / 2));	
		
	// Show frame
	setVisible(true);
    }
	
    /** Create a new unit button in toolbar */
    public void createUnitButton(int unitID, String unitName, ImageIcon unitIcon) {
	unitToolbar.createUnitButton(unitID, unitName, unitIcon);
    }
	
    /** Return true if tool window is open */
    public boolean isToolWindowOpen(String toolName) {
	return desktop.isToolWindowOpen(toolName);
    }
	
    /** Opens a tool window if necessary */
    public void openToolWindow(String toolName) {
	desktop.openToolWindow(toolName);
    }
	
    /** Closes a tool window if it is open */
    public void closeToolWindow(String toolName) {
	desktop.closeToolWindow(toolName);
    }
	
    /** Opens a window for a unit if it isn't already open Also makes
     *  a new unit button in toolbar if necessary */
    public void openUnitWindow(int unitID) {
	desktop.openUnitWindow(unitID);
    }
	
    /** Disposes a unit window and button */
    public void disposeUnitWindow(int unitID) {
	desktop.disposeUnitWindow(unitID);
    }
	
    /** Disposes a unit button in toolbar */
    public void disposeUnitButton(int unitID) {
	unitToolbar.disposeUnitButton(unitID);
    }
	
    /** Returns a unit dialog for a given unit ID */
    public UnitDialog getUnitDialog(int unitID) {
	return mars.getDetailWindow(unitID, desktop);
    }
	
    /** Returns an array of unit info for all moving vehicles sorted by name */
    public UnitInfo[] getMovingVehicleInfo() {
	return mars.getMovingVehicleInfo();
    }
	
    /** Returns an array of unit info for all vehicles sorted by name */
    public UnitInfo[] getVehicleInfo() {
	return mars.getVehicleInfo();
    }
	
    /** Returns an array of unit info for all settlements sorted by name */
    public UnitInfo[] getSettlementInfo() {
	return mars.getSettlementInfo();
    }
	
    /** Returns an array of unit info for all people sorted by name */
    public UnitInfo[] getPeopleInfo() {
	return mars.getPeopleInfo();
    }
    
    /** WindowListener methods overridden */
    public void windowClosing(WindowEvent event) { System.exit(0); }
    public void windowClosed(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}
}	
