/**
 * Mars Simulation Project
 * UnitWindowListener.java
 * @version 2.70 2000-02-29
 * @author Scott Davis
 */

import javax.swing.*;
import javax.swing.event.*;

/** The UnitWindowListener class is a custom window listener for unit
 *  detail windows that handles their behavior.
 */
public class UnitWindowListener extends InternalFrameAdapter {
	
    MainDesktopPane desktop;  // Main desktop pane that holds unit windows.
	
    public UnitWindowListener(MainDesktopPane desktop) {
	this.desktop = desktop;
    }
	
    // Overridden parent method
    public void internalFrameOpened(InternalFrameEvent e) { 
	JInternalFrame frame = (JInternalFrame) e.getSource();
	try {
	    frame.setClosed(false);
	} catch(java.beans.PropertyVetoException v) {
	    System.out.println(frame.getTitle() + " setClosed() is Vetoed!");
	}
    } 
	
    // Overriden parent method
    /** Removes unit button from toolbar when unit window is closed. */
    public void internalFrameClosing(InternalFrameEvent e) { 
	int unitID = ((UnitDialog) e.getSource()).getUnitID();
	desktop.disposeUnitWindow(unitID); 
    }
}
