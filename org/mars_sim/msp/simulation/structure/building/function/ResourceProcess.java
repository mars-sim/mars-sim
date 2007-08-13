/**
 * Mars Simulation Project
 * ResourceProcess.java
 * @version 2.81 2007-04-23
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.time.*;
 
/**
 * The ResourceProcess class represents a process of
 * converting one set of resources to another.
 */
public class ResourceProcess implements Serializable {
    
	// The work time required to toggle this process on or off. 
	public static final double TOGGLE_RUNNING_WORK_TIME_REQUIRED = 10D;
	
    private String name;
    private Map<AmountResource, Double> maxInputResourceRates;
    private Map<AmountResource, Double> maxAmbientInputResourceRates;
    private Map<AmountResource, Double> maxOutputResourceRates;
    private Map<AmountResource, Double> maxWasteOutputResourceRates;
    private boolean runningProcess;
    private double currentProductionLevel;
    private double toggleRunningWorkTime;
    
    /**
     * Constructor
     * @param name the name of the process.
     * @param defaultOn true of process is on by default, false if off by default. 
     */
    public ResourceProcess(String name, boolean defaultOn) {
        this.name = name;
        maxInputResourceRates = new HashMap<AmountResource, Double>();
        maxAmbientInputResourceRates = new HashMap<AmountResource, Double>();
        maxOutputResourceRates = new HashMap<AmountResource, Double>();
        maxWasteOutputResourceRates = new HashMap<AmountResource, Double>();
        runningProcess = defaultOn;
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
     * @param resource the amount resource.
     * @param rate max input resource rate (kg/sec)
     * @param ambient is resource from available from surroundings? (air)
     */
    public void addMaxInputResourceRate(AmountResource resource, double rate, boolean ambient) {
        if (ambient) {
            if (!maxAmbientInputResourceRates.containsKey(resource)) 
                maxAmbientInputResourceRates.put(resource, new Double(rate));
        }
        else {
            if (!maxInputResourceRates.containsKey(resource)) 
                maxInputResourceRates.put(resource, new Double(rate));
        }
    }
    
    /**
     * Adds a maximum output resource rate if it doesn't already exist.
     * @param resource the amount resource.
     * @param rate max output resource rate (kg/sec)
     * @param waste is resource waste material not to be stored?
     */
    public void addMaxOutputResourceRate(AmountResource resource, double rate, boolean waste) {
        if (waste) {
            if (!maxWasteOutputResourceRates.containsKey(resource))
                maxWasteOutputResourceRates.put(resource, new Double(rate));
        }
        else {
            if (!maxOutputResourceRates.containsKey(resource)) 
                maxOutputResourceRates.put(resource, new Double(rate));
        }
    }
    
    /**
     * Gets the current production level of the process.
     * @return proportion of full production (0D - 1D)
     */
    public double getCurrentProductionLevel() {
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
     * Adds work time to toggling the process on or off.
     * @param time the amount (millisols) of time to add.
     */
    public void addToggleWorkTime(double time) {
    	toggleRunningWorkTime += time;
    	if (toggleRunningWorkTime >= TOGGLE_RUNNING_WORK_TIME_REQUIRED) {
    		toggleRunningWorkTime = 0D;
    		runningProcess = !runningProcess;
    		// if (runningProcess) System.out.println(name + " turned on.");
    		// else System.out.println(name + " turned off.");
    	}
    }
    
    /**
     * Gets the set of input resources.
     * @return set of resources.
     */
    public Set<AmountResource> getInputResources() {   
        Set<AmountResource> results = new HashSet<AmountResource>();
        results.addAll(maxInputResourceRates.keySet());
        results.addAll(maxAmbientInputResourceRates.keySet());
        return results;
    }
    
    /**
     * Gets the max input resource rate for a given resource.
     * @return rate in kg/sec.
     */
    public double getMaxInputResourceRate(AmountResource resource) {
        double result = 0D;
        if (maxInputResourceRates.containsKey(resource))
            result = maxInputResourceRates.get(resource).doubleValue();
        else if (maxAmbientInputResourceRates.containsKey(resource))
            result = maxAmbientInputResourceRates.get(resource).doubleValue();
        return result;
    }
    
    /**
     * Checks if resource is an ambient input.
     * @param resource the resource to check.
     * @return true if ambient resource.
     */
    public boolean isAmbientInputResource(AmountResource resource) {
    	return maxAmbientInputResourceRates.containsKey(resource);
    }
    
    /**
     * Gets the set of output resources.
     * @return set of resources.
     */
    public Set<AmountResource> getOutputResources() {
        Set<AmountResource> results = new HashSet<AmountResource>();
        results.addAll(maxOutputResourceRates.keySet());
        results.addAll(maxWasteOutputResourceRates.keySet());
        return results;
    }
    
    /**
     * Gets the max output resource rate for a given resource.
     * @return rate in kg/sec.
     */
    public double getMaxOutputResourceRate(AmountResource resource) {
        double result = 0D;
        if (maxOutputResourceRates.containsKey(resource))
            result = maxOutputResourceRates.get(resource).doubleValue();
        else if (maxWasteOutputResourceRates.containsKey(resource))
            result = maxWasteOutputResourceRates.get(resource).doubleValue();
        return result;
    }
    
    /**
     * Checks if resource is a waste output.
     * @param resource the resource to check.
     * @return true if waste output.
     */
    public boolean isWasteOutputResource(AmountResource resource) {
    	return maxWasteOutputResourceRates.containsKey(resource);
    }
    
    /**
     * Processes resources for a given amount of time.
     * @param time (millisols)
     * @param productionLevel proportion of max process rate (0.0D - 1.0D)
     * @param inventory the inventory pool to use for processes.
     * @throws Exception if error processing resources.
     */
    public void processResources(double time, double productionLevel, Inventory inventory) throws Exception {
    	
    	if ((productionLevel < 0D) || (productionLevel > 1D) || (time < 0D))
            throw new IllegalArgumentException();
        
        // System.out.println(name + " process");
     
        if (runningProcess) {       
            // Convert time from millisols to seconds.
            double timeSec = MarsClock.convertMillisolsToSeconds(time);
            
            // Get resource bottleneck
            double bottleneck = getInputBottleneck(time, inventory);
            if (productionLevel > bottleneck) productionLevel = bottleneck;
            
            // System.out.println(name + " production level: " + productionLevel);
            
            // Input resources from inventory.
            Iterator inputI = maxInputResourceRates.keySet().iterator();
            while (inputI.hasNext()) {
                AmountResource resource = (AmountResource) inputI.next();
                double maxRate = maxInputResourceRates.get(resource).doubleValue();
                double resourceRate = maxRate * productionLevel;
                double resourceAmount = resourceRate * timeSec;
                double remainingAmount = inventory.getAmountResourceStored(resource);
                if (resourceAmount > remainingAmount) resourceAmount = remainingAmount;
                try {
                	inventory.retrieveAmountResource(resource, resourceAmount);
                }
                catch (Exception e) {}
                // System.out.println(resourceName + " input: " + resourceAmount + "kg.");
            }
            
            // Output resources to inventory.
            Iterator outputI = maxOutputResourceRates.keySet().iterator();
            while (outputI.hasNext()) {
            	AmountResource resource = (AmountResource) outputI.next();
                double maxRate = maxOutputResourceRates.get(resource).doubleValue();
                double resourceRate = maxRate * productionLevel;
                double resourceAmount = resourceRate * timeSec;
                double remainingCapacity = inventory.getAmountResourceRemainingCapacity(resource);
                if (resourceAmount > remainingCapacity) resourceAmount = remainingCapacity;
                try {
                	inventory.storeAmountResource(resource, resourceAmount);
                }
                catch (Exception e) {}
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
     * @param inventory the inventory pool the process uses.
     * @return bottleneck (0.0D - 1.0D)
     * @throws Exception if error getting input bottleneck.
     */
    private double getInputBottleneck(double time, Inventory inventory) throws Exception {
        
        // Check for illegal argument.
        if (time < 0D) throw new IllegalArgumentException("time must be > 0D");
        
        double bottleneck = 1D;
        
        // Convert time from millisols to seconds.
        double timeSec = MarsClock.convertMillisolsToSeconds(time);
        
        Iterator inputI = maxInputResourceRates.keySet().iterator();
        while (inputI.hasNext()) {
        	AmountResource resource = (AmountResource) inputI.next();
            double maxRate = maxInputResourceRates.get(resource).doubleValue();
            double desiredResourceAmount = maxRate * timeSec;
            double inventoryResourceAmount = inventory.getAmountResourceStored(resource);
            double proportionAvailable = 1D;
            if (desiredResourceAmount > 0D) 
                proportionAvailable = inventoryResourceAmount / desiredResourceAmount;
            if (bottleneck > proportionAvailable) bottleneck = proportionAvailable;
        }
        
        return bottleneck;
    }
    
    /**
     * Gets the string value for this object.
     * @return string
     */
    public String toString() {
    	return getProcessName();
    }
}