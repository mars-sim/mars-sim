/**
 * Mars Simulation Project
 * ResourceProcessManager.java
 * @version 2.75 2004-03-27
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.mars_sim.msp.simulation.Inventory;
 
/**
 * The ResourceProcessManager class is a manager of 
 * resource processes for a building.
 */
public class ResourceProcessManager implements Serializable {
    
    private Building building; // The building this is the manager for.
    private Inventory inventory; // The settlement inventory
    private Collection processes; // The building's resource processes.
    
    // Constructor
    public ResourceProcessManager(Building building, Inventory inventory) {
        this.building = building;
        this.inventory = inventory;
        processes = new ArrayList();
    }
    
    /**
     * Add resource process.
     * @process a new resources process.
     */
    public void addResourceProcess(ResourceProcess process) {
        processes.add(process);
    }
    
    /**
     * Checks if there is a process that has the given name.
     * @param name a process name.
     * @return true if there is a process that has the given name.
     */
    public boolean hasProcess(String name) {
        boolean result = false;
        Iterator i = processes.iterator();
        while (i.hasNext()) {
            ResourceProcess process = (ResourceProcess) i.next();
            if (name.equals(process.getProcessName())) result = true;
        }
        return result;
    }
    
    /**
     * Gets all the resource processes in the manager.
     * @return collection of processes
     */
    public Collection getProcesses() {
        return processes;
    }
    
    /**
     * Gets the number of processes in the manager.
     * @return number of processes
     */
    public int getProcessNumber() {
        return processes.size();
    }
    
    /**
     * Processes resources for a given amount of time.
     * @param time (millisols)
     * @param productionLevel proportion of max process rate (0.0D - 1.0D)
     */
    public void processResources(double time, double productionLevel) {
        
        // Check for bad arguments.
        if ((time < 0D) || (productionLevel < 0D) || (productionLevel > 1D))
            throw new IllegalArgumentException();
   
        // Run each resource process.
        Iterator i = processes.iterator();
        while (i.hasNext()) {
            ResourceProcess process = (ResourceProcess) i.next();
            process.processResources(time, productionLevel, inventory);
        }
    }   
}
