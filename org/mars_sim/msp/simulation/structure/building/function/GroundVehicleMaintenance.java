/**
 * Mars Simulation Project
 * GroundVehicleMaintenance.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The GroundVehicleMaintenance class is a building function for a building
 * capable of maintaining ground vehicles.
 */
public class GroundVehicleMaintenance extends VehicleMaintenance implements Serializable {
    
    public static final String NAME = "Ground Vehicle Maintenance";
    
    /**
     * Constructor
     * @param building the building the function is for.
     * @throws BuildingException if error in construction.
     */
    public GroundVehicleMaintenance(Building building) throws BuildingException {
    	// Call VehicleMaintenance constructor.
    	super(NAME, building);
    	
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			vehicleCapacity = config.getVehicleCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("GroundVehicleMaintenance.constructor: " + e.getMessage());
		}
    }
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) {
        // TODO: Implement later as needed.
        return 0D;
    }
}