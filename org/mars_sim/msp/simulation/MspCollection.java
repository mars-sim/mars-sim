/**
 * Mars Simulation Project
 * MspCollection.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

public abstract class MspCollection {

    // Data members
    private Collection listeners; // Collection of MspCollectionEventListeners.

    /**
     * Constructs a MspCollection object.
     */
    public MspCollection() {

        // Initialize listeners.
	listeners = new ArrayList();
    }

    /**
     * Adds a MspCollectionEventListener to this collection's listeners.
     * @param listener the new listener
     */
    public void addMspCollectionEventListener(MspCollectionEventListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Removes a MspCollectionEventListener from this collection's listeners.
     * @param listener the listener to be removed.
     */
    public void removeMspCollectionEventListener(MspCollectionEventListener listener) {
        if (listeners.contains(listener)) listeners.remove(listener);
    }

    /**
     * Fires a MspCollectionEvent to all the listeners.
     * @param event the event to be fired.
     */
    protected void fireMspCollectionEvent(MspCollectionEvent event) {
        if (!listeners.isEmpty()) {
            Iterator i = listeners.iterator();
	        while (i.hasNext()) {
                ((MspCollectionEventListener) i.next()).collectionModified(event);
	        }
	    }
    }
}
