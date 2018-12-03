/**
 * Mars Simulation Project
 * UnitWindowListener.java
 * @version 3.1.0 2017-03-04
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/** The UnitWindowListener class is a custom window listener for unit
 *  detail windows that handles their behavior.
 */
public class UnitWindowListener extends InternalFrameAdapter {

    // Data members
    private MainDesktopPane desktop; // Main desktop pane that holds unit windows.

    /** Constructs a UnitWindowListener object
     *  @param desktop the desktop pane
     */
    public UnitWindowListener(MainDesktopPane desktop) {
        this.desktop = desktop;
    }

    /**
     * Removes unit button from toolbar when unit window is closed.
     *
     * @param e internal frame event.
     */
    public void internalFrameClosing(InternalFrameEvent e) {
    	desktop.makeUnitWindowInvisible((UnitWindow) e.getSource());
//        desktop.disposeUnitWindow((UnitWindow) e.getSource());
    }
}

