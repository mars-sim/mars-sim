/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 2.71 2000-10-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import javax.swing.*;

/** The MainDesktopManager class is a custom desktop manager for the
 *  Mars Simulation Project UI that allows tool and unit detail windows
 *  to disapear when their iconify buttons are pressed.
 */
class MainDesktopManager extends DefaultDesktopManager {

	/** Constructs a MainDesktopManager object */
	public MainDesktopManager() { super(); }
	
	/** Iconifies frame (overridden) */
	public void iconifyFrame(JInternalFrame frame) { frame.setVisible(false); }
}
