/**
 * Mars Simulation Project
 * MainDesktopPane.java
 * @version 2.78 2005-08-09
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.standard.tool.monitor.UnitTableModel;
import org.mars_sim.msp.ui.standard.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.standard.tool.search.SearchWindow;
import org.mars_sim.msp.ui.standard.tool.time.TimeWindow;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindowFactory;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindowListener;

/** 
 * The MainDesktopPane class is the desktop part of the project's UI.
 * It contains all tool and unit windows, and is itself contained,
 * along with the tool bars, by the main window.
 */
public class MainDesktopPane extends JDesktopPane implements ComponentListener {

    // Data members
    private Collection unitWindows; // List of open or buttoned unit windows.
    private Collection toolWindows; // List of tool windows.
    private MainWindow mainWindow; // The main window frame.
    private ImageIcon backgroundImageIcon; // ImageIcon that contains the tiled background.
    private JLabel backgroundLabel; // Label that contains the tiled background.
    private JLabel logoLabel; // Label that has the centered logo for the project.
    private boolean firstDisplay; // True if this MainDesktopPane hasn't been displayed yet.
    private UpdateThread updateThread; // The desktop update thread.

    /** 
     * Constructor
     *
     * @param mainWindow the main outer window
     */
    public MainDesktopPane(MainWindow mainWindow) {

        // Initialize data members
        this.mainWindow = mainWindow;
        unitWindows = new ArrayList();
        toolWindows = new ArrayList();

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
        
        // Prepare tool windows.
        prepareToolWindows();
        
        // Create update thread.
        updateThread = new UpdateThread(this);
        updateThread.setRun(true);
        updateThread.start();
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
        toolWindows.add(navWindow);

        // Prepare search tool window
        SearchWindow searchWindow = new SearchWindow(this);
        try { searchWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.add(searchWindow);

        // Prepare time tool window
        TimeWindow timeWindow = new TimeWindow(this);
        try { timeWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.add(timeWindow);

        // Prepare monitor tool window
        MonitorWindow monitorWindow = new MonitorWindow(this);
        try { monitorWindow.setClosed(true); }
        catch (java.beans.PropertyVetoException e) { }
        toolWindows.add(monitorWindow);
    }

    /** Returns a tool window for a given tool name
     *  @param toolName the name of the tool window
     *  @return the tool window
     */
    public ToolWindow getToolWindow(String toolName) {
        ToolWindow result = null;
        Iterator i = toolWindows.iterator();
        while (i.hasNext()) {
            ToolWindow window = (ToolWindow) i.next();
            if (toolName.equals(window.getToolName())) result = window;
        }
        
        return result;
    }

    /** Displays a new Unit model in the monitor window
     *  @param model the new model to display
     */
    public void addModel(UnitTableModel model) {
        ((MonitorWindow) getToolWindow("Monitor Tool")).displayModel(model);
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
        ToolWindow window = getToolWindow(toolName);
        if (window != null) return !window.isClosed();
        else return false;
    }

    /** Opens a tool window if necessary
     *  @param toolName the name of the tool window
     */
    public void openToolWindow(String toolName) {
        ToolWindow window = getToolWindow(toolName);
        if (window != null) {
            if (window.isClosed()) {
                if (!window.wasOpened()) {
                    window.setLocation(getRandomLocation(window));
                    window.setWasOpened(true);
                }
                add(window, 0);
                try { 
                    window.setClosed(false); 
                }
                catch (Exception e) { System.out.println(e.toString()); }
            }
            window.show();
            //bring to front if it overlaps with other windows
            try {
            window.setSelected(true);
            } catch (PropertyVetoException e) {
            // ignore if setSelected is vetoed	
            }
        }
    }

    /** Closes a tool window if it is open
     *  @param toolName the name of the tool window
     */
    public void closeToolWindow(String toolName) {
        ToolWindow window = getToolWindow(toolName);
        if ((window != null) && !window.isClosed()) {
            try { window.setClosed(true); }
            catch (java.beans.PropertyVetoException e) {}
        }
    }

    /** 
     * Creates and opens a window for a unit if it isn't 
     * already in existance and open.
     *
     * @param unit the unit the window is for.
     */
    public void openUnitWindow(Unit unit) {

        UnitWindow tempWindow = null;

        Iterator i = unitWindows.iterator();
        while (i.hasNext()) {
            UnitWindow window = (UnitWindow) i.next();
            if (window.getUnit() == unit) tempWindow = window;
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
            // Create new window for unit.
            tempWindow = UnitWindowFactory.getUnitWindow(unit, this);
    
            add(tempWindow, 0);
            tempWindow.pack();

            // Set internal frame listener
            tempWindow.addInternalFrameListener(new UnitWindowListener(this));

            // Put window in random position on desktop
            tempWindow.setLocation(getRandomLocation(tempWindow));

            // Add unit window to unit windows
            unitWindows.add(tempWindow);

            // Create new unit button in tool bar if necessary
            mainWindow.createUnitButton(unit);
        }

        tempWindow.setVisible(true);

        // Correct window becomes selected
        try {
            tempWindow.setSelected(true);
            tempWindow.moveToFront();
        }
        catch (java.beans.PropertyVetoException e) {}
    }

    /** 
     * Disposes a unit window and button.
     *
     * @param unit the unit the window is for.
     */
    public void disposeUnitWindow(Unit unit) {

        // Dispose unit window
        UnitWindow deadWindow = null;
        Iterator i = unitWindows.iterator();
        while (i.hasNext()) {
            UnitWindow window = (UnitWindow) i.next();
            if (unit == window.getUnit()) deadWindow = window;
        }
        
        unitWindows.remove(deadWindow);
        
        if (deadWindow != null) deadWindow.dispose();

        // Have main window dispose of unit button
        mainWindow.disposeUnitButton(unit);
    }
    
    /** 
     * Disposes a unit window and button.
     *
     * @param window the unit window to dispose.
     */
    public void disposeUnitWindow(UnitWindow window) {
        
        if (window != null) {    
            unitWindows.remove(window);
            window.dispose();
        
            // Have main window dispose of unit button
            mainWindow.disposeUnitButton(window.getUnit());
        }
    }

    /**
     * Update the desktop and all of its windows.
     */
    private void update() {
     
        // Update all unit windows.
        Iterator i1 = unitWindows.iterator();
        try {
        	while (i1.hasNext()) {
            	UnitWindow window = (UnitWindow) i1.next();
	            window.update();
    	    }
        }
        catch (ConcurrentModificationException e) {
        	// Concurrent modifications exceptions may occur as 
        	// unit windows are opened.
        }
        
        // Update all tool windows.
        Iterator i2 = toolWindows.iterator();
        try {
        	while (i2.hasNext()) {
            	ToolWindow window = (ToolWindow) i2.next();
            	window.update();
        	}
        }
    	catch (ConcurrentModificationException e) {
    		// Concurrent modifications exceptions may occur as
    		// unit windows are opened.
    	}
    }
    
    /**
     * Resets all windows on the desktop.  Disposes of all unit windows
     * and tool windows, and reconstructs the tool windows.
     */
    void resetDesktop() {
        
        // Stop update thread.
        updateThread.setRun(false);
        
        // Dispose unit windows
        Iterator i1 = unitWindows.iterator();
        while (i1.hasNext()) {
            UnitWindow window = (UnitWindow) i1.next();
            window.dispose();
            mainWindow.disposeUnitButton(window.getUnit());
        }
        
        // Dispose tool windows
        Iterator i2 = toolWindows.iterator();
        while (i2.hasNext()) {
            ToolWindow window = (ToolWindow) i2.next();
            window.dispose();
        }

        // Prepare tool windows
        prepareToolWindows();
        
        // Restart update thread.
        updateThread.setRun(true);
    }

    /** 
     * Gets a random location on the desktop for a given JInternalFrame.
     *
     * @param tempWindow an internal window
     * @return random point on the desktop
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
    
    /** 
     * Internal class thread for update.
     */
    private class UpdateThread extends Thread {
        
        public static final long SLEEP_TIME = 1000; // 1 second.
        MainDesktopPane desktop;
        boolean run = false;
        
        private UpdateThread(MainDesktopPane desktop) {
            super("Desktop update thread");
            
            this.desktop = desktop;
        }
        
        private void setRun(boolean run) {
            this.run = run;
        }
        
        public void run() {
            while (true) {
                if (run) desktop.update();   
                try {
                    Thread.sleep(SLEEP_TIME);
                } 
                catch (InterruptedException e) {}
            }
        }
    }
}