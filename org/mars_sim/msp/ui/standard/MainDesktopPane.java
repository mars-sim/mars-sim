/**
 * Mars Simulation Project
 * MainDesktopPane.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.Coordinates;  
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MainDesktopPane class is the desktop part of the project's UI.
 *  It contains all tool and unit windows, and is itself contained,
 *  along with the tool bars, by the main window.
 */
public class MainDesktopPane extends JDesktopPane implements ComponentListener {

    private Vector unitWindows; // List of open or buttoned unit windows.
    private Vector toolWindows; // List of tool windows.
    private MainWindow mainWindow; // The main window frame.
    private UIProxyManager proxyManager;  // The unit UI proxy manager.

    private ImageIcon backgroundImageIcon; // ImageIcon that contains the tiled background.
    private JLabel backgroundLabel; // Label that contains the tiled background.
    private JLabel logoLabel; // Label that has the centered logo for the project.
    private boolean firstDisplay; // True if this MainDesktopPane hasn't been displayed yet.

    public MainDesktopPane(MainWindow mainWindow, UIProxyManager proxyManager) {

        // Initialize data members
        this.mainWindow = mainWindow;
        this.proxyManager = proxyManager;
        unitWindows = new Vector();
        toolWindows = new Vector();

        // Set background color to black
        setBackground(Color.black);

        // set desktop manager
        setDesktopManager(new MainDesktopManager());

        // Set component listener
        addComponentListener(this);

        // Prepare tool windows
        prepareToolWindows();

        // Create background logo label and make it partially transparent
        logoLabel = new JLabel(new ImageIcon("images/logo2.gif"), JLabel.LEFT);
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

    /** Create background tile when MainDesktopPane is first
      *  displayed. Recenter logoLabel on MainWindow and set
      *  backgroundLabel to the size of MainDesktopPane.
      */
    public void componentResized(ComponentEvent e) {

        // If displayed for the first time, create background image tile.
        // The size of the background tile cannot be determined during construction
        // since it requires the MainDesktopPane be displayed first.
        if (firstDisplay) {
            ImageIcon baseImageIcon = new ImageIcon("images/background.gif");
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

        // Prepare navigator window
        NavigatorWindow navWindow = new NavigatorWindow(this);
        try { navWindow.setClosed(true); } 
        catch (java.beans.PropertyVetoException e) {}

        toolWindows.addElement(navWindow);

        // Prepare search tool window
        SearchWindow searchWindow = new SearchWindow(this);
        try { searchWindow.setClosed(true); } 
        catch (java.beans.PropertyVetoException e) {}

        toolWindows.addElement(searchWindow);
    }

    /** Returns a tool window for a given tool name */
    private ToolWindow getToolWindow(String toolName) {
        ToolWindow resultWindow = null;
        for (int x = 0; x < toolWindows.size(); x++) {
            ToolWindow tempWindow = (ToolWindow) toolWindows.elementAt(x);
            if (tempWindow.getToolName().equals(toolName)) {
                resultWindow = tempWindow;
            }
        }
        return resultWindow;
    }

    /** Centers the map and the globe on given coordinates */
    public void centerMapGlobe(Coordinates targetLocation) {
        ((NavigatorWindow) getToolWindow("Mars Navigator")).
                updateCoords(targetLocation);
    }

    /** Return true if tool window is open */
    public boolean isToolWindowOpen(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if (tempWindow != null) {
            return !tempWindow.isClosed();
        } else {
            return false;
        }
    }

    /** Opens a tool window if necessary */
    public void openToolWindow(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if (tempWindow != null) {
            if (tempWindow.isClosed()) {
                if (tempWindow.hasNotBeenOpened()) {
                    tempWindow.setLocation(getRandomLocation(tempWindow));
                    tempWindow.setOpened();
                }
                add(tempWindow, 0);
                try {
                    tempWindow.setClosed(false);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            tempWindow.show();
        }
    }

    /** Closes a tool window if it is open */
    public void closeToolWindow(String toolName) {
        ToolWindow tempWindow = getToolWindow(toolName);
        if ((tempWindow != null) && !tempWindow.isClosed()) {
            try {
                tempWindow.setClosed(true);
            } catch (java.beans.PropertyVetoException e) {}
        }
    }

    /** Creates and opens a window for a unit if it isn't already in existance and open */
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
        } 
        else {
            tempWindow = unitUIProxy.getUnitDialog(this);
            add(tempWindow, 0);
            try { tempWindow.setSelected(true); } 
            catch (java.beans.PropertyVetoException e) {}

            // Set internal frame listener
            tempWindow.addInternalFrameListener(new UnitWindowListener(this));

            // Put window in random position on desktop
            tempWindow.setLocation(getRandomLocation(tempWindow));

            // Add unit window to unitWindows vector
            unitWindows.addElement(tempWindow);

            // Create new unit button in tool bar if necessary
            mainWindow.createUnitButton(unitUIProxy);
        }

        tempWindow.show();
    }

    /** Disposes a unit window and button */
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

    /** Returns the unit UI proxy manager. */
    public UIProxyManager getProxyManager() { return proxyManager; }
    
    /** Returns a random location on the desktop for a given JInternalFrame */
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

