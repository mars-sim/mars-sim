/**
 * Mars Simulation Project
 * FacilityPanel.java
 * @version 2.71 2000-10-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** The FacilityPanel class displays information about a settlement's
 *  facility in the user interface.  It is the abstract parent class
 *  for the displays of each facility.
 */
public abstract class FacilityPanel extends JPanel {

    // Data members
    protected String tabName; // The name for the panel's tab
    protected MainDesktopPane desktop; // The main desktop

    /** Constructs a FacilityPanel object 
     *  @param desktop the desktop pane
     */
    public FacilityPanel(MainDesktopPane desktop) {

        // Initialize data members
        this.desktop = desktop;

        // Set the default font
        setFont(new Font("Helvetica", Font.BOLD, 12));

        // Set a border around the panel
        setBorder(new CompoundBorder(new EtchedBorder(), 
                new EmptyBorder(5, 5, 5, 5)));
    }

    /** Returns the tab name for the facility 
     *  @return the tab name for the facility
     */
    public String getTabName() { return tabName; }

    /** Updates the facility panel's information */
    public abstract void updateInfo();
}
