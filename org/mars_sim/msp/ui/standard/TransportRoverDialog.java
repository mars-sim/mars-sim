/**
 * Mars Simulation Project
 * TransportRoverDialog.java
 * @version 2.74 2002-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 
 
import org.mars_sim.msp.simulation.*; 
import org.mars_sim.msp.simulation.vehicle.*;
import java.awt.*;
import javax.swing.*;

/**
 * The TransportRoverDialog class is the detail window for a transport rover.
 */
public class TransportRoverDialog extends GroundVehicleDialog {
    
    // Data members
    protected TransportRover rover;  // Transport rover related to this detail window
	
    /** Constructs an TransportRoverDialog object 
     *  @param parentDesktop desktop pane
     *  @param TransportRoverUIProxy the transport rover's UI proxy
     */
    public TransportRoverDialog(MainDesktopPane parentDesktop, TransportRoverUIProxy transportRoverUIProxy) {
	// Use GroundVehicleDialog constructor	
	super(parentDesktop, transportRoverUIProxy);
    }

    /** Override setupComponents */
    protected void setupComponents() {
		
	// Initialize transport rover 
	rover = (TransportRover) parentUnit;
	
	super.setupComponents();
    }
}
