/**
 * Mars Simulation Project
 * MspCollectionEventListener.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/**
 * The MspCollectionEventListener is an interface for a listener
 * for a MspCollectionEvent.
 */
public interface MspCollectionEventListener extends EventListener {

    /**
     * Responds to a MspCollectionEvent.
     */
    public void collectionModified(MspCollectionEvent event);
}
