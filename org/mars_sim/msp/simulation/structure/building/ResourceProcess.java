/**
 * Mars Simulation Project
 * ResourceProcess.java
 * @version 2.75 2003-02-07
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
 
/**
 * The ResourceProcess class represents a process of
 * converting one set of resources to another.
 */
public class ResourceProcess implements Serializable {
    
    private String name;
    private Map maxInputResourceRates;
    private Map maxOutputResourceRates;
    private Inventory inventory;
    private boolean runningProcess;
    private double currentProductionLevel;
    
    /**
     * Constructor
     * @param name the name of the process.
     * @param inventory inventory object to use as resource pool
     */
    public ResourceProcess(String name, Inventory inventory) {
        this.name = name;
        this.inventory = inventory;
        maxInputResourceRates = new HashMap();
        maxOutputResourceRates = new HashMap();
        runningProcess = true;
        currentProductionLevel = 1D;
    }
    
    /**
     * Gets the process name.
     * @return process name as string.
     */
    public String getProcessName() {
        return name;
    }
    
    /**
     * Adds a maximum input resource rate if it doesn't already exist.
     * @param resource name
     * @param rate max input resource rate (kg/sec)
     */
    public void addMaxInputResourceRate(String name, double rate) {
        if (!maxInputResourceRates.containsKey(name)) 
            maxInputResourceRates.put(name, new Double(rate));
    }
    
    /**
     * Adds a maximum output resource rate if it doesn't already exist.
     * @param resource name
     * @param rate max output resource rate (kg/sec)
     */
    public void addMaxOutputResourceRate(String name, double rate) {
        if (!maxOutputResourceRates.containsKey(name)) 
            maxOutputResourceRates.put(name, new Double(rate));
    }
    
    /**
     * Gets the current production level of the process.
     * @return propertion of full production (0D - 1D)
     */
    public double getCurrentProcutionLevel() {
        return currentProductionLevel;
    }
    
    /**
     * Checks if the process is running or not.
     * @return true if process is running.
     */
    public boolean isProcessRunning() {
        return runningProcess;
    }
    
    /**
     * Sets if the process is running or not.
     * @running true if process is running.
     */
    public void setProcessRunning(boolean running) {
        runningProcess = running;
    }
    
    /**
     * Gets the set of input resource names.
     * @return set of resource names.
     */
    public Set getInputResourceNames() {
        if (maxInputResourceRates != null) {
            return maxInputResourceRates.keySet();
        }
        else return new HashSet();
    }
    
    /**
     * Gets the max input resource rate for a given resource name.
     * @return rate in kg/sec.
     */
    public double getMaxInputResourceRate(String resourceName) {
        if (maxInputResourceRates.containsKey(resourceName))
            return ((Double) maxInputResourceRates.get(resourceName)).doubleValue();
        else return 0D;
    }
    
    /**
     * Gets the set of output resource names.
     * @return set of resource names.
     */
    public Set getOutputResourceNames() {
        if (maxOutputResourceRates != null) {
            return maxOutputResourceRates.keySet();
        }
        else return new HashSet();
    }
    
    /**
     * Gets the max output resource rate for a given resource name.
     * @return rate in kg/sec.
     */
    public double getMaxOutputResourceRate(String resourceName) {
        if (maxOutputResourceRates.containsKey(resourceName))
            return ((Double) maxOutputResourceRates.get(resourceName)).doubleValue();
        else return 0D;
    }
    
    /**
     * Processes resources for a given amount of time.
     * @param time (millisols)
     * @param productionLevel proportion of max process rate (0.0D - 1.0D)
     */
    public void processResources(double time, double productionLevel) {
        if ((productionLevel < 0D) || (productionLevel > 1D) || (time < 0D))
            throw new IllegalArgumentException();
        
        // System.out.println(name + " process");
     
        if (runningProcess) {       
            // Convert time from millisols to seconds.
            double timeSec = MarsClock.convertMillisolsToSeconds(time);
            
            // Get resource bottleneck
            double bottleneck = getInputBottleneck(time);
            if (productionLevel > bottleneck) productionLevel = bottleneck;
            
            // System.out.println(name + " production level: " + productionLevel);
            
            // Input resources from inventory.
            Iterator inputI = getInputResourceNames().iterator();
            while (inputI.hasNext()) {
                String resourceName = (String) inputI.next();
                double maxRate = ((Double) maxInputResourceRates.get(resourceName)).doubleValue();
                double resourceRate = maxRate * productionLevel;
                double resourceAmount = resourceRate * timeSec;
                inventory.removeResource(resourceName, resourceAmount);
                // System.out.println(resourceName + " input: " + resourceAmount + "kg.");
            }
            
            // Output resources to inventory.
            Iterator outputI = getOutputResourceNames().iterator();
            while (outputI.hasNext()) {
                String resourceName = (String) outputI.next();
                double maxRate = ((Double) maxOutputResourceRates.get(resourceName)).doubleValue();
                double resourceRate = maxRate * productionLevel;
                double resourceAmount = resourceRate * timeSec;
                inventory.addResource(resourceName, resourceAmount);
                // System.out.println(resourceName + " output: " + resourceAmount + "kg.");
            }
        }
        else productionLevel = 0D;

        // Set the current production level.        
        currentProductionLevel = productionLevel;
    }
    
    /**
     * Finds the bottleneck of input resources from inventory pool.
     * @param time (millisols)
     * @return bottleneck (0.0D - 1.0D)
     */
    private double getInputBottleneck(double time) {
        
        // Check for illegal argument.
        if (time < 0D) throw new IllegalArgumentException("time must be > 0D");
        
        double bottleneck = 1D;
        
        // Convert time from millisols to seconds.
        double timeSec = MarsClock.convertMillisolsToSeconds(time);
        
        Iterator inputI = getInputResourceNames().iterator();
        while (inputI.hasNext()) {
            String resourceName = (String) inputI.next();
            double maxRate = ((Double) maxInputResourceRates.get(resourceName)).doubleValue();
            double desiredResourceAmount = maxRate * timeSec;
            double inventoryResourceAmount = inventory.getResourceMass(resourceName);
            double proportionAvailable = 1D;
            if (desiredResourceAmount > 0D) 
                proportionAvailable = inventoryResourceAmount / desiredResourceAmount;
            if (bottleneck > proportionAvailable) bottleneck = proportionAvailable;
        }
        
        return bottleneck;
    }
}
