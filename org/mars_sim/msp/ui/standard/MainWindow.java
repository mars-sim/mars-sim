/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 2.71 2000-10-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MainWindow class is the primary UI frame for the project. It
 *  contains the tool bars and main desktop pane.
 */
public class MainWindow extends JFrame implements WindowListener {

    // Data members
    private UnitToolBar unitToolbar; // The unit tool bar
    private MainDesktopPane desktop; // The main desktop
    private UIProxyManager proxyManager; // The unit UI proxy manager

    /** Constructs a MainWindow object 
     *  @param mars the virtual Mars
     */
    public MainWindow(VirtualMars mars) {

        // use JFrame constructor
        super("Mars Simulation Project (version 2.71)");

        // Create unit UI proxy manager.
        Unit[] units = mars.getUnitManager().getUnits();
        proxyManager = new UIProxyManager(units);
        
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
        desktop = new MainDesktopPane(this, proxyManager);
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
        System.exit(0);
    }
    public void windowClosed(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}
}

