/**
 * Mars Simulation Project
 * MspCollection.java
 * @version 2.75 2002-05-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

public abstract class MspCollection {

    // Data members
    private Collection listeners; // Collection of MspCollectionEventListeners.

    // We can replace this with another type of collection if we need to.
    private List elements;  // Used internally to hold elements.

    /**
     * Constructs a MspCollection object.
     */
    public MspCollection() {

        // Initialize listeners.
	    listeners = new ArrayList();

        // Linked list are suited to List that have dynamic contents
        elements = new LinkedList();
    }

    /**
     * Adds a MspCollectionEventListener to this collection's listeners.
     * @param listener the new listener
     */
    public void addMspCollectionEventListener(MspCollectionEventListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /** Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Unit o) {
	    fireMspCollectionEvent("add", o);
        return elements.add(o);
    }

    /** Returns true if this collection contains the specific element.
     *  @param o element whose presence in this collection is to be tested
     *  @return true if this collection contains the specified element
     */
    public boolean contains(Unit o) {
        return elements.contains(o);
    }

    /** Returns true if this collection has no elements.
     *  @return true if this collection contains no elements
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /** Removes a single instance of the specified element from this
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Unit o) {
	    fireMspCollectionEvent("remove", o);
        return elements.remove(o);
    }

    /** Returns the number of elements in this collection.
     *  @return the number of elements in this collection
     */
    public int size() {
        return elements.size();
    }

    /**
     * Return the internal List of Units.
     * @return Unit list.
     */
    protected List getUnits() {
        return elements;
    }

    /** Removes all of the elements from this collection. */
    public void clear() {
	    fireMspCollectionEvent("clear", null);
        elements.clear();
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
     * @param eventType The event to be fired.
     * @param target Object triggering the event, this may be null.
     */
    protected void fireMspCollectionEvent(String eventType, Unit target) {
        if (!listeners.isEmpty()) {
            MspCollectionEvent event = new MspCollectionEvent(this, eventType,
                                                              target);
            Iterator i = listeners.iterator();
	        while (i.hasNext()) {
                ((MspCollectionEventListener) i.next()).collectionModified(event);
	        }
	    }
    }
}
