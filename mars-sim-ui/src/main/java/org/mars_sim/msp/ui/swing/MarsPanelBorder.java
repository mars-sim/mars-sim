/**
 * Mars Simulation Project
 * MarsPanelBorder.java
 * @version 3.1.0 2019-02-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

/**
 * The MarsPanelBorder is a common compound border used for panels.
 */
public class MarsPanelBorder extends CompoundBorder {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public MarsPanelBorder() {

		super(new EtchedBorder(), MainDesktopPane.newEmptyBorder());
	}
}
