/**
 * Mars Simulation Project
 * MspCollection.java
 * @version 2.81 2007-08-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides an abstract implementation of a Collection for
 * Unit objects. It provides support for a listener to be notified of the
 * addition or removal of Units.
 */
public abstract class MspCollection implements java.io.Serializable {

    // Data members
    // private transient Collection listeners = null; // Collection of MspCollectionEventListeners.

    // We can replace this with another type of collection if we need to.
    private List<Unit> elements;  // Used internally to hold elements.
    
    /**
     * Constructs a MspCollection object.
     */
    public MspCollection() {

        // Array lists are suited to List that have dynamic contents
	//thread safe...
        elements =  Collections.synchronizedList(new ArrayList<Unit>());
    }

    /**
     * Adds a MspCollectionEventListener to this collection's listeners.
     * @param listener the new listener
     */
    /*
    public void addMspCollectionEventListener(MspCollectionEventListener listener) {
        if (listeners == null) listeners = new ArrayList();
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    */

    /** Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured
     *  @return true if this collection changed as a result of the call
     */
    synchronized public boolean add(Unit o) {
        boolean result = elements.add(o);
	    // fireMspCollectionEvent(MspCollectionEvent.ADD, o);
        return result;
    }

    /** Returns true if this collection contains the specific element.
     *  @param o element whose presence in this collection is to be tested
     *  @return true if this collection contains the specified element
     */
    synchronized public boolean contains(Unit o) {
        return elements.contains(o);
    }
    
    /**
     * Gets the first occurence of the given unit in this collection.
     * @param o the unit to search for.
     * @return the index of the unit or -1 if not in collection.
     */
    synchronized public int indexOf(Unit o) {
    	return elements.indexOf(o);
    }
    
    /**
     * Gets a unit in the collection at a given index.
     * @param index the index of the unit.
     * @return the unit or null if invalid index.
     */
    synchronized public Unit get(int index) {
    	Unit result = null;
    	if ((index > -1) && (index < elements.size())) result = elements.get(index);
    	return result;
    }

    /** Returns true if this collection has no elements.
     *  @return true if this collection contains no elements
     */
    synchronized public boolean isEmpty() {
        return elements.isEmpty();
    }

    /** Removes a single instance of the specified element from this
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present
     *  @return true if this collection changed as a result of the call
     */
    synchronized public boolean remove(Unit o) {
        boolean result = elements.remove(o);
	    // fireMspCollectionEvent(MspCollectionEvent.REMOVE, o);
        return result;
    }

    /** Returns the number of elements in this collection.
     *  @return the number of elements in this collection
     */
    synchronized public int size() {
        return elements.size();
    }

    /**
     * Convert the internal collection to an array of Units.
     * @return Array of Units.
     */
    synchronized public Object[] toArray() {
        return elements.toArray();
    }

    /**
     * Return the internal List of Units.
     * @return Unit list.
     */
    synchronized protected List getUnits() {
        return elements;
    }

    /** Removes all of the elements from this collection. */
    synchronized public void clear() {
        elements.clear();
	    // fireMspCollectionEvent(MspCollectionEvent.CLEAR, null);
    }

    /**
     * Removes a MspCollectionEventListener from this collection's listeners.
     * @param listener the listener to be removed.
     */
    /*
    public void removeMspCollectionEventListener(MspCollectionEventListener listener) {
        if (listeners.contains(listener)) listeners.remove(listener);
        if (listeners.size() == 0) {
            listeners = null;
        }
    }
    */

    /**
     * Fires a MspCollectionEvent to all the listeners.
     * @param eventType The event to be fired.
     * @param target Object triggering the event, this may be null.
     */
    /*
    protected void fireMspCollectionEvent(String eventType, Unit target) {
        if (listeners != null) {
            MspCollectionEvent event = new MspCollectionEvent(this, eventType,
                                                              target);
            Iterator i = listeners.iterator();
	        while (i.hasNext()) {
                ((MspCollectionEventListener) i.next()).collectionModified(event);
	        }
	    }
    }
    */
    
 	/**
 	 * Checks if an object is a MSP collection with identical contents.
 	 * @return true if contents are the same.
 	 */
    synchronized public boolean equals(Object object) {
    	boolean result = false;
    	
    	if (object instanceof MspCollection) {
    		MspCollection collection = (MspCollection) object;
    		if (collection.size() == elements.size()) {
    			result = true;
    			Iterator<Unit> i = elements.iterator();
    			while (i.hasNext()) {
    				Unit unit = i.next();
    				if (!collection.contains(unit)) result = false;
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets the generic iterator for this collection.
     * @return iterator
     */
    synchronized public Iterator getIterator() {
    	return elements.iterator();
    }
}