/**
 * Mars Simulation Project
 * ToolWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;

/** 
 * The ToolWindow class is an abstract UI window for a tool.
 * Particular tool windows should be derived from this.
 */
public abstract class ToolWindow extends JInternalFrame {

    // Data members
    protected String name; // The name of the tool the window is for.
    protected MainDesktopPane desktop; // The main desktop.
    protected boolean opened;  // True if window is open.

    /** 
     * Constructor 
     *
     * @param name the name of the tool
     * @param desktop the main desktop.
     */
    public ToolWindow(String name, MainDesktopPane desktop) {

        // use JInternalFrame constructor
        super(name, 
              true, // resizable
              true, // closable
              false, // maximizable
              false); // iconifiable

        // Initialize data members
        this.name = name;
        this.desktop = desktop;
        opened = false;
        
        // Set internal frame listener
        addInternalFrameListener(new ToolFrameListener());
    }

    /** 
     * Gets the tool name.
     *
     * @return tool name
     */
    public String getToolName() {
        return name;
    }

    /** 
     * Checks if the tool window has previously been opened.
     *
     * @return true if tool window has previously been opened.
     */
    public boolean wasOpened() {
        return opened;
    }

    /** 
     * Sets if the window has previously been opened.
     *
     * @param opened true if previously opened.
     */
    public void setWasOpened(boolean opened) {
        this.opened = opened;
    }
    
    /**
     * Update window.
     */
    public void update() {}
    
    /**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {}
}