/**
 * Mars Simulation Project
 * ViewFrameListener.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import javax.swing.*;
import javax.swing.event.*;

/** ViewFrameListener manages internal frame behaviors.
 */
public class ViewFrameListener extends InternalFrameAdapter {

    /** open internal frame (overridden) */
    public void internalFrameOpened(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        try { frame.setClosed(false); } 
        catch (java.beans.PropertyVetoException v) {
            System.out.println(frame.getTitle() + " setClosed() is Vetoed!");
        }
    }
}

