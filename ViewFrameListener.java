/**
 * Mars Simulation Project
 * ViewFrameListener.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

import javax.swing.*;
import javax.swing.event.*;

/** ViewFrameListener manages internal frame behaviors.
 */
public class ViewFrameListener extends InternalFrameAdapter {
	
    // open internal frame (overridden)
    public void internalFrameOpened(InternalFrameEvent e) { 
	JInternalFrame frame = (JInternalFrame) e.getSource();
	try {
	    frame.setClosed(false);
	}
	catch(java.beans.PropertyVetoException v) {
	    System.out.println(frame.getTitle() + " setClosed() is Vetoed!");
	}
    } 
}
