/**
 * Mars Simulation Project
 * ToolWindow.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import javax.swing.*;

/** The ToolWindow class is an abstract UI window for a tool.
 *  Particular tool windows should be derived from this.
 */
public abstract class ToolWindow extends JInternalFrame {

    // Data members
    protected String toolName; // The name of the tool the window is for.
    protected boolean opened;  // True if window is open.

    /** Constructs a ToolWindow object 
     *  @toolName the name of the tool
     */
    public ToolWindow(String toolName) {

        // use JInternalFrame constructor
        super(toolName, true, // resizable
              true, // closable
              false, // maximizable
              false); // iconifiable

        // Initialize tool name
        this.toolName = new String(toolName);

        opened = false;
    }

    /** Returns tool name 
     *  @return tool name
     */
    public String getToolName() {
        return new String(toolName);
    }

    /** Returns true if tool window has not previously been opened. 
     *  @return true if tool window has not previously been opened
     */
    public boolean hasNotBeenOpened() {
        return !opened;
    }

    /** Sets notOpened to false. */
    public void setOpened() {
        opened = true;
    }
}
