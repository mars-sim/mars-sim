/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 2.75 2003-02-20
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.*;
import java.io.Serializable;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * The BuildingManager manages the settlement's buildings.
 */
public class BuildingManager implements Serializable {
    
    private Settlement settlement; // The manager's settlement.
    private Collection buildings; // The settlement's buildings.
    
    /**
     * Constructor
     * @param settlement - the manager's settlement.
     */
    public BuildingManager(Settlement settlement) {
        
        this.settlement = settlement;
        buildings = new ArrayList();
    }
    
    /**
     * Gets the building manager's settlement.
     *
     * @return settlement
     */
    public Settlement getSettlement() {
        return settlement;
    }
    
    /**
     * Adds a new building to the settlement.
     *
     * @param newBuilding the building to add.
     */
    public void addBuilding(Building newBuilding) {
        if (!buildings.contains(newBuilding)) buildings.add(newBuilding);
    }
    
    /**
     * Gets the settlement's collection of buildings.
     *
     * @return collection of buildings
     */
    public Collection getBuildings() {
        return buildings;
    }
    
    /**
     * Gets the buildings in a settlement that implement the given function.
     *
     * @return collection of buildings.
     */
    public Collection getBuildings(Class function) {   
        Collection functionBuildings = new ArrayList();
        
        Iterator i = buildings.iterator();
        while (i.hasNext()) {
            Building building = (Building) i.next();
            if (function.isInstance(building)) functionBuildings.add(building);
        }
         
        return functionBuildings;
    }
    
    /**
     * Gets the number of buildings at the settlement.
     *
     * @return number of buildings
     */
    public int getBuidingNum() {
        return buildings.size();
    }
    
    /**
     * Time passing for all buildings.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        Iterator i = buildings.iterator();
        while (i.hasNext()) ((Building) i.next()).timePassing(time);
    }       
}
