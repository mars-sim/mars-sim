/**
 * Mars Simulation Project
 * MainDesktopPane.java
 * @version 2.75 2003-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.ui.standard.monitor.MonitorWindow;
import org.mars_sim.msp.ui.standard.monitor.UnitTableModel;
import org.mars_sim.msp.ui.standard.unit_window.*;
import org.mars_sim.msp.ui.standard.unit_window.person.PersonWindow;
import org.mars_sim.msp.ui.standard.unit_window.vehicle.VehicleWindow;
import org.mars_sim.msp.ui.standard.unit_window.structure.SettlementWindow;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MainDesktopPane class is the desktop part of the project's UI.
 *  It contains all tool and unit windows, and is itself contained,
 *  along with the tool bars, by the main window.
 */
public class MainDesktopPane extends JDesktopPane implements ComponentListener {

    // Data members
    private Vector unitWindows; // List of open or buttoned unit windows.
    private Vector toolWindows; // List of tool windows.
    private MainWindow mainWindow; // The main window frame.
    private UIProxyManager proxyManager;  // The unit UI proxy manager.
    private ImageIcon backgroundImageIcon; // ImageIcon that contains the tiled background.
    private JLabel backgroundLabel; // Label that contains the tiled background.
    private JLabel logoLabel; // Label that has the centered logo for the project.
    private boolean firstDisplay; // True if this MainDesktopPane hasn't been displayed yet.

    /** Constructs a MainDesktopPane object
     *  @param mainWindow the main outer window
     */
    public MainDesktopPane(MainWindow mainWindow) {

        // Initialize data members
        this.mainWindow = mainWindow;
        unitWindows = new Vector();
        toolWindows = new Vector();

        // Set background color to black
        setBackground(Color.black);

        // set desktop manager
        setDesktopManager(new MainDesktopManager());

        // Set component listener
        addComponentListener(this);

        // Create background logo label and make it partially transparent
        logoLabel = new JLabel(ImageLoader.getIcon("logo2"), JLabel.LEFT);
        add(logoLabel, Integer.MIN_VALUE);
        logoLabel.setOpaque(false);

        // Create background label and set it to the back layer
        backgroundImageIcon = new ImageIcon();
        backgroundLabel = new JLabel(backgroundImageIcon);
        add(backgroundLabel, Integer.MIN_VALUE);
        backgroundLabel.setLocation(0, 0);
        moveToBack(backgroundLabel);

        // Initialize firstDisplay to true
        firstDisplay = true;
    }

    /** Returns the MainWindow instance
     *  @return MainWindow instance
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    /** Create background tile when MainDesktopPane is first
      *  displayed. Recenter logoLabel on MainWindow and set
      *  backgroundLabel to the size of MainDesktopPane.
      *  @param e the component event
      */
    public void componentResized(ComponentEvent e) {

        // If displayed for the first time, create background image tile.
        // The size of the background tile cannot be determined during construction
        // since it requires the MainDesktopPane be displayed first.
        if (firstDisplay) {
            ImageIcon baseImageIcon = ImageLoader.getIcon("background");
            Dimension screen_size =
                    Toolkit.getDefaultToolkit().getScreenSize();
            Image backgroundImage =
                    createImage((int) screen_size.getWidth(),
                    (int) screen_size.getHeight());
            Graphics backgroundGraphics = backgroundImage.getGraphics();

            for (int x = 0; x < backgroundImage.getWidth(this);
                    x += baseImageIcon.getIconWidth()) {
                for (int y = 0; y < backgroundImage.getHeight(this);
                        y += baseImageIcon.getIconHeight()) {
                    backgroundGraphics.drawImage(
                            baseImageIcon.getImage(), x, y, this);
                }
            }

            backgroundImageIcon.setImage(backgroundImage);

            backgroundLabel.setSize(getSize());
            logoLabel.setSize(logoLabel.getPreferredSize());

            firstDisplay = false;
        }

        // Set the backgroundLabel size to the size of the desktop
        backgroundLabel.setSize(getSize());

        // Recenter the logo on the window
        int Xpos = ((mainWindow.getWidth() - logoLabel.getWidth()) / 2) -
                (int) getLocation().getX();
        int Ypos = ((mainWindow.getHeight() - logoLabel.getHeight()) /
                2) - 45;
        logoLabel.setLocation(Xpos, Ypos);
    }

    // Additional Component Listener methods implemented but not used.
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}

    /** Creates tool windows */
    private void prepareToolWindows() {

        toolWindows.clear();

        // Prepare navigator window
        NavigatorWindow navWindow = new NavigatorWindow(this);
        try { navWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.addElement(navWindow);

        // Prepare search tool window
        SearchWindow searchWindow = new SearchWindow(this);
        try { searchWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.addElement(searchWindow);

        // Prepare time tool window
        TimeWindow timeWindow = new TimeWindow(this);
        try { timeWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.addElement(timeWindow);

        // Prepare monitor tool window
        MonitorWindow monitorWindow = new MonitorWindow(this);
        try { monitorWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.addElement(monitorWindow);
    }

    /** Returns a tool window for a given tool name
     *  @param toolName the name of the tool window
     *  @return the tool window
     */
    ToolWindow getToolWindow(String toolName) {
        ToolWindow resultWindow = null;
        for (int x = 0; x < toolWindows.size(); x++) {
            ToolWindow tempWindow = (ToolWindow) toolWindows.elementAt(x);
            if (tempWindow.getToolName().equals(toolName)) {
                resultWindow = tempWindow;
            }
        }
        return resultWindow;
    }

    /** Displays a new Unit model in the monitor window
     *  @param model the new model to display
     */
    public void addModel(UnitTableModel model) {
        ((MonitorWindow) getToolWindow("Monitor Tool")).
                displayModel(model);
    }

    /** Centers the map and the globe on given coordinates
     *  @param targetLocation the new center location
     */
    public void centerMapGlobe(Coordinates targetLocation) {
        ((NavigatorWindow) getToolWindow("Mars Navigator")).
                updateCoords(targetLocation);
    }

    /** Return true if tool window is open
     *  @param toolName the name of the tool window
     *  @return true true if tool window is open
     */
    public boolean isToolWindowOpen(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if (tempWindow != null) return !tempWindow.isClosed();
        else return false;
    }

    /** Opens a tool window if necessary
     *  @param toolName the name of the tool window
     */
    public void openToolWindow(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if (tempWindow != null) {
            if (tempWindow.isClosed()) {
                if (tempWindow.hasNotBeenOpened()) {
                    tempWindow.setLocation(getRandomLocation(tempWindow));
                    tempWindow.setOpened();
                }
                add(tempWindow, 0);
                try { tempWindow.setClosed(false); }
                catch (Exception e) { System.out.println(e.toString()); }
            }
            tempWindow.show();
        }
    }

    /** Closes a tool window if it is open
     *  @param toolName the name of the tool window
     */
    public void closeToolWindow(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if ((tempWindow != null) && !tempWindow.isClosed()) {
            try { tempWindow.setClosed(true); }
            catch (java.beans.PropertyVetoException e) {}
        }
    }

    /** Creates and opens a window for a unit if it isn't already in existance and open
     *  @param unitUIProxy the unit UI proxy
     */
    public void openUnitWindow(UnitUIProxy unitUIProxy) {

        UnitDialog tempWindow = null;

        for (int x = 0; x < unitWindows.size(); x++) {
            if (((UnitDialog) unitWindows.elementAt(x)).getUnit() ==
                    unitUIProxy.getUnit()) {
                tempWindow = (UnitDialog) unitWindows.elementAt(x);
            }
        }

        if (tempWindow != null) {
            if (tempWindow.isClosed()) add(tempWindow, 0);

            try {
                tempWindow.setIcon(false);
            }
            catch(java.beans.PropertyVetoException e) {
                System.out.println("Problem reopening " + e);
            }
        }
        else {
            tempWindow = unitUIProxy.getUnitDialog(this);
            add(tempWindow, 0);

            // Set internal frame listener
            tempWindow.addInternalFrameListener(new UnitWindowListener(this));

            // Put window in random position on desktop
            tempWindow.setLocation(getRandomLocation(tempWindow));

            // Add unit window to unitWindows vector
            unitWindows.addElement(tempWindow);

            // Create new unit button in tool bar if necessary
            mainWindow.createUnitButton(unitUIProxy);

        }
        tempWindow.setVisible(true);

        // Correct window becomes selected
        try {
            tempWindow.setSelected(true);
            tempWindow.moveToFront();
        }
        catch (java.beans.PropertyVetoException e) {}
        
        // Open a settlement unit window (remove later)
        if (unitUIProxy instanceof SettlementUIProxy) {
            UnitWindow window = new SettlementWindow(this, unitUIProxy);
            add(window, 0);
            window.pack();
            window.setVisible(true);
        }
        
        // Open a person unit window (remove later)
        if (unitUIProxy instanceof PersonUIProxy) {
            UnitWindow window = new PersonWindow(this, unitUIProxy);
            add(window, 0);
            window.pack();
            window.setVisible(true);
        }
        
        // Open a vehicle unit window (remove later)
        if (unitUIProxy instanceof VehicleUIProxy) {
            UnitWindow window = new VehicleWindow(this, unitUIProxy);
            add(window, 0);
            window.pack();
            window.setVisible(true);
        }
    }

    /** Disposes a unit window and button
     *  @param unitUIProxy the unit UI proxy
     */
    public void disposeUnitWindow(UnitUIProxy unitUIProxy) {

        // Dispose unit window
        for (int x = 0; x < unitWindows.size(); x++) {
            UnitDialog tempWindow = (UnitDialog) unitWindows.elementAt(x);
            if (tempWindow.getUnit() == unitUIProxy.getUnit()) {
                unitWindows.removeElement(tempWindow);
                tempWindow.dispose();
            }
        }

        // Have main window dispose of unit button
        mainWindow.disposeUnitButton(unitUIProxy);
    }

    /** Returns the unit UI proxy manager.
     *  @return the unit UI proxy manager
     */
    public UIProxyManager getProxyManager() { return proxyManager; }

    /** set the unit UI proxy manager.
     *  @param manager The unit UI proxy manager
     */
    public void setProxyManager(UIProxyManager manager) {
        proxyManager = manager;

        // Dispose unit windows
        for (int x = 0; x < unitWindows.size(); x++) {
            UnitDialog tempWindow = (UnitDialog) unitWindows.elementAt(x);
            tempWindow.dispose();
            mainWindow.disposeUnitButton(tempWindow.getUnitProxy());
        }
        unitWindows.clear();

        // Dispose tool windows
        for (int x = 0; x < toolWindows.size(); x++) {
            ToolWindow tempWindow = (ToolWindow) toolWindows.elementAt(x);
            tempWindow.dispose();
        }

        // Prepare tool windows
        prepareToolWindows();
    }

    /** Returns a random location on the desktop for a given JInternalFrame
     *  @param tempWindow an internal window
     *  @return random point on the desktop
     */
    private Point getRandomLocation(JInternalFrame tempWindow) {

        Dimension desktop_size = getSize();
        Dimension window_size = tempWindow.getSize();

        int rX = (int) Math.round(Math.random() *
                (desktop_size.width - window_size.width));
        int rY = (int) Math.round(Math.random() *
                (desktop_size.height - window_size.height));

        return new Point(rX, rY);
    }
}

