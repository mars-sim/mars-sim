/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing; 

import javax.swing.*;

/** The MainDesktopManager class is a custom desktop manager for the
 *  Mars Simulation Project UI that allows tool and unit detail windows
 *  to disappear when their iconify buttons are pressed.
 */
class MainDesktopManager extends DefaultDesktopManager {

	/** Constructs a MainDesktopManager object */
	public MainDesktopManager() { super(); }
	
	/** Iconifies frame (overridden) 
     *  @param frame the internal frame
     */
	public void iconifyFrame(JInternalFrame frame) { frame.setVisible(false); }
}
