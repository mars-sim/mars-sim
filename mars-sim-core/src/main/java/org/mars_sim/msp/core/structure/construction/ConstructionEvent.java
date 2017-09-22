/**
 * Mars Simulation Project
 * ConstructionEvent.java
 * @version 3.1.0 2017-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import java.util.EventObject;

/**
 * A construction related event.
 */
public class ConstructionEvent extends EventObject {

    // Data members
    private String type; // The event type.
    private Object target; // The event target object or null if none.
    
    /**
     * Constructor
     * @param source the object throwing the event.
     * @param type the event type.
     * @param target the event target object (or null if none)
     */
    public ConstructionEvent(Object source, String type, Object target) {
        // Use EventObject constructor.
        super(source);
        
        this.type = type;
        this.target = target;
    }
    
    /**
     * Gets the event type.
     * @return event type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the event target object.
     * @return target object or null if none.
     */
    public Object getTarget() {
        return target;
    }
}