/**
 * Mars Simulation Project
 * ToolFrameListener.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;

/** 
 * ToolFrameListener manages internal frame behaviors for tool windows.
 */
public class ToolFrameListener extends InternalFrameAdapter {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.ToolFrameListener";
	
    	private static Logger logger = Logger.getLogger(CLASS_NAME);

    /** open internal frame (overridden) */
    public void internalFrameOpened(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        try { frame.setClosed(false); } 
        catch (java.beans.PropertyVetoException v) {
            logger.log(Level.SEVERE,frame.getTitle() + " setClosed() is Vetoed!");
        }
    }
}

