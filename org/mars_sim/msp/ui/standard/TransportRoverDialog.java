/**
 * Mars Simulation Project
 * TransportRoverDialog.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.vehicle.TransportRover;
import org.mars_sim.msp.simulation.person.medical.SickBay;

/**
 * The TransportRoverDialog class is the detail window for a transport rover.
 */
public class TransportRoverDialog extends RoverDialog {

    // Data members
    protected TransportRover rover;  // Transport rover related to this detail window
    private SickBayPanel sickBayPane;

    /** Constructs an TransportRoverDialog object
     *  @param parentDesktop desktop pane
     *  @param TransportRoverUIProxy the transport rover's UI proxy
     */
    public TransportRoverDialog(MainDesktopPane parentDesktop, TransportRoverUIProxy transportRoverUIProxy) {
	// Use RoverDialog constructor
	super(parentDesktop, transportRoverUIProxy);
    }

    /** Override setupComponents */
    protected void setupComponents() {

	    // Initialize transport rover
	    rover = (TransportRover) parentUnit;

	    super.setupComponents();

        sickBayPane = new SickBayPanel((SickBay)rover.getMedicalFacility(),
                                        parentDesktop);
        tabPane.addTab("Sick Bay", sickBayPane);
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
	    super.generalUpdate();
        sickBayPane.updateInfo();
    }
}
