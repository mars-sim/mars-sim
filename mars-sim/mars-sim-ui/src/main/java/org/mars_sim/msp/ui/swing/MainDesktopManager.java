/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing; 

import javax.swing.*;

/**
 * The MainDesktopManager class is a custom desktop manager for the
 * Mars Simulation Project UI that allows tool and unit detail windows
 * to disappear when their iconify buttons are pressed.
 */
class MainDesktopManager
extends DefaultDesktopManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Constructs a MainDesktopManager object */
	public MainDesktopManager() { super(); }

	/**
	 * Iconifies frame (overridden) 
	 * @param frame the internal frame
	 */
	@Override
	public void iconifyFrame(JInternalFrame frame) { frame.setVisible(false); }
}
