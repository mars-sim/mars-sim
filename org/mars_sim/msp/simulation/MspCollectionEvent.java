/**
 * Mars Simulation Project
 * MspCollectionEvent.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/**
 * The MspCollectionEvent is an event that is thrown by an
 * MspCollection when a unit is added or removed.
 */
public class MspCollectionEvent extends EventObject {

    // Data members
    private String type; // Type of event.

    /**
     * Constructs a MspCollectionEvent object.
     * @param source the MspCollection source of the event.
     * @param type the type of the event.
     */
    public MspCollectionEvent(MspCollection source, String type) {

        // User EventObject's constructor.
	super(source);

	// Initialize type.
	this.type = type;
    }

    /**
     * Gets the type of the event.
     * @return event type
     */
    public String getType() {
        return type;
    }
}
