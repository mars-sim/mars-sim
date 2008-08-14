/**
 * Mars Simulation Project
 * ConstructionStageInfo.java
 * @version 2.85 2008-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;

/**
 * Information about a construction stage.
 */
public class ConstructionStageInfo implements Serializable {
    
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
     * @param workTime the work time required for construction.
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
     * @return gets the architect construction skill level.
     */
    public int getArchitectConstructionSkill() {
        return architectConstructionSkill;
    }

    /**
     * @return gets the name of the stage.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Gets the parts needed for the stage.
     */
    public Map<Part, Integer> getParts() {
        return parts;
    }

    /**
     * @return gets the prerequisite stage name.
     */
    public String getPrerequisiteStage() {
        return prerequisiteStage;
    }

    /**
     * @return gets the resources needed for the stage.
     */
    public Map<AmountResource, Double> getResources() {
        return resources;
    }

    /**
     * @return gets the stage type.
     */
    public String getType() {
        return type;
    }

    /**
     * @return gets the vehicles needed for the stage.
     */
    public List<ConstructionVehicleType> getVehicles() {
        return vehicles;
    }

    /**
     * @return gets the construction work time.
     */
    public double getWorkTime() {
        return workTime;
    }
}