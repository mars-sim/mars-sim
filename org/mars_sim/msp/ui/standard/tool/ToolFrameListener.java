/**
 * Mars Simulation Project
 * ToolFrameListener.java
 * @version 2.75 2003-07-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool;

import javax.swing.*;
import javax.swing.event.*;

/** 
 * ToolFrameListener manages internal frame behaviors for tool windows.
 */
public class ToolFrameListener extends InternalFrameAdapter {

    /** open internal frame (overridden) */
    public void internalFrameOpened(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        try { frame.setClosed(false); } 
        catch (java.beans.PropertyVetoException v) {
            System.err.println(frame.getTitle() + " setClosed() is Vetoed!");
        }
    }
}

