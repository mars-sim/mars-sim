/*
 * Mars Simulation Project
 * ProcessSpec.java
 * @date 2022-10-23
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the key attributes to a process that converts a set of input Resources
 * into a set of output Resources.
 */
public abstract class ProcessSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double powerRequired;

	private Map<Integer, Double> baseInputRates;
	private Map<Integer, Double> baseOutputRates;

	// Cache some aggregate values
	private Set<Integer> ambientResources;
	private Set<Integer> wasteResources;

	/** How long does it take to complete the process*/
	private int processTime = 100;

	/** The work time required to toggle this process on or off. */
	private int workTime = 10;


	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param powerRequired
	 * @param processTime
	 * @param workTime
	 */
	protected ProcessSpec(String name, double powerRequired, int processTime, int workTime) {
		this.name = name;
		this.baseInputRates = new HashMap<>();
		this.baseOutputRates = new HashMap<>();
		this.wasteResources = new HashSet<>();
		this.ambientResources = new HashSet<>();
		this.powerRequired = powerRequired;
		this.processTime = processTime;
		this.workTime = workTime;
	}

	
	/**
	 * Adds a base input resource rate.
	 *
	 * @param resource the amount resource.
	 * @param rate     base input resource rate (kg/millisol)
	 * @param ambient  is resource from available from surroundings? (air)
	 */
	public void addBaseInputResourceRate(Integer resource, double rate, boolean ambient) {
		if (ambient) {
			ambientResources.add(resource);
		} 

		baseInputRates.put(resource, rate);
	}

	/**
	 * Adds a base output resource rate.
	 *
	 * @param resource the amount resource.
	 * @param rate     base output resource rate (kg/millisol)
	 * @param waste    is resource waste material not to be stored?
	 */
	public void addBaseOutputResourceRate(Integer resource, double rate, boolean waste) {
		if (waste) {
			wasteResources.add(resource);
		}

		baseOutputRates.put(resource, rate);
	}

	public String getName() {
		return name;
	}

	/**
	 * Gets a set of input resources for this process.
	 * 
	 * @return
	 */
	public Set<Integer> getInputResources() {
		return baseInputRates.keySet();
	}

	/**
	 * Gets the base input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseInputRate(Integer resource) {
		return baseInputRates.get(resource);
	}

	/**
	 * Checks if resource is an ambient input.
	 *
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return ambientResources.contains(resource);
	}

	/**
	 * Gets a set of output resources from this process.
	 * 
	 * @return
	 */
	public Set<Integer> getOutputResources() {
		return baseOutputRates.keySet();
	}
	
	/**
	 * Gets the base output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getBaseOutputRate(Integer resource) {
		var value = baseOutputRates.get(resource);
		if (value != null) {
			return value.doubleValue();
		}
		return 0D;
	}

	/**
	 * Checks if resource is a waste output.
	 *
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return wasteResources.contains(resource);
	}

	/**
	 * Gets the power required for this process.
	 * 
	 * @return
	 */
	public double getPowerRequired() {
		return powerRequired;
	}

	/**
	 * Gets the time to complete this process.
	 * 
	 * @return Number of msol
	 */
	public int getProcessTime() {
		return processTime ;
	}

	/**
	 * Gets the work time needed on this process.
	 * 
	 * @return
	 */
	public int getWorkTime() {
		return workTime;
	}
	
	public void destroy() {
		baseInputRates.clear();
		baseOutputRates.clear();

		baseInputRates = null;
		baseOutputRates = null;
		
		ambientResources.clear();
		wasteResources.clear();
		
		ambientResources = null;
		wasteResources = null;
	}
}
