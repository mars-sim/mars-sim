/**
 * Mars Simulation Project
 * TransportRoverUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.vehicle.*;

/**
 * User interface proxy for an transport rover.
 */
public class TransportRoverUIProxy extends GroundVehicleUIProxy {

    // Data members
    private TransportRover rover;

    /** Constructs an TransportRoverUIProxy object
     *  @param TransportRover the transport rover
     *  @param proxyManager the unit UI proxy manager
     */
    public TransportRoverUIProxy(TransportRover rover, UIProxyManager proxyManager) {
        super(rover, proxyManager);

        this.rover = rover;
        buttonIcon = ImageLoader.getIcon("TransportRoverIcon");
    }
}
