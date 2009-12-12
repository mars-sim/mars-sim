/**
 * Mars Simulation Project
 * ConstructionStageInfo.java
 * @version 2.85 2008-10-13
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;

/**
 * Information about a construction stage.
 */
public class ConstructionStageInfo implements Serializable {
    
    // Stage types
    public static final String FOUNDATION = "foundation";
    public static final String FRAME = "frame";
    public static final String BUILDING = "building";
    
    // Data members
    private String name;
    private String type;
    private double workTime;
    private int architectConstructionSkill;
    private String prerequisiteStage;
    private Map<Part, Integer> parts;
    private Map<AmountResource, Double> resources;
    private List<ConstructionVehicleType> vehicles;
    
    /**
     * Constructor
     * @param name the name of the stage.
     * @param type the stage type.
     * @param workTime the work time (millisols) required for construction.
     * @param architectConstructionSkill the construction skill required.
     * @param prerequisiteStage the name of the prerequisite stage.
     * @param parts map of parts required and their number.
     * @param resources map of resources required and their amount.
     * @param vehicles list of construction vehicles required.
     */
    ConstructionStageInfo(String name, String type, double workTime, 
            int architectConstructionSkill, String prerequisiteStage, 
            Map<Part, Integer> parts, Map<AmountResource, Double> resources, 
            List<ConstructionVehicleType> vehicles) {
        
        this.name = name;
        this.type = type;
        this.workTime = workTime;
        this.architectConstructionSkill = architectConstructionSkill;
        this.prerequisiteStage = prerequisiteStage;
        this.parts = parts;
        this.resources = resources;
        this.vehicles = vehicles;
    }

    /**
     * Gets the architect construction skill level.
     * @return skill level.
     */
    public int getArchitectConstructionSkill() {
        return architectConstructionSkill;
    }

    /**
     * Gets the name of the stage.
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parts needed for the stage.
     * @return map of parts and their number.
     */
    public Map<Part, Integer> getParts() {
        return parts;
    }

    /**
     * Gets the prerequisite stage name.
     * @return name.
     */
    public String getPrerequisiteStage() {
        return prerequisiteStage;
    }

    /**
     * Gets the resources needed for the stage.
     * @return map of resources and their amounts (kg).
     */
    public Map<AmountResource, Double> getResources() {
        return resources;
    }

    /**
     * Gets the stage type.
     * @return type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the vehicles needed for the stage.
     * @return vehicle type.
     */
    public List<ConstructionVehicleType> getVehicles() {
        return vehicles;
    }

    /**
     * Gets the construction work time.
     * @return time (millisols).
     */
    public double getWorkTime() {
        return workTime;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}