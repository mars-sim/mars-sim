/**
 * Mars Simulation Project
 * ConstructionVehicleType.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.simulation.resource.Part;

/**
 * Construction vehicle information.
 */
public class ConstructionVehicleType implements Serializable {

    // Data members
    private String vehicleType;
    private Class vehicleClass;
    private List<Part> attachmentParts;
    
    /**
     * Constructor
     * @param vehicleType the vehicle type.
     * @param vehicleClass the vehicle class.
     * @param attachmentParts list of attachment parts.
     */
    ConstructionVehicleType(String vehicleType, Class vehicleClass, 
            List<Part> attachmentParts) {
        
        this.vehicleType = vehicleType;
        this.vehicleClass = vehicleClass;
        this.attachmentParts = attachmentParts;
    }
    
    /**
     * @return gets the attachment parts.
     */
    public List<Part> getAttachmentParts() {
        return attachmentParts;
    }

    /**
     * @return gets the vehicle class.
     */
    public Class getVehicleClass() {
        return vehicleClass;
    }

    /**
     * @return gets the vehicle type.
     */
    public String getVehicleType() {
        return vehicleType;
    }
    
    @Override
    public String toString() {
        return getVehicleType();
    }
}