/*
 * Mars Simulation Project
 * ProcessSpec.java
 * @date 2022-10-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The represents the key attributes fo a process that convers a set of input Resources
 * into a set of output Resources.
 */
public abstract class ProcessSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double powerRequired;

	private Map<Integer, Double> maxInputRates;
	private Map<Integer, Double> maxOutputRates;

	// Cache some aggregate values
	private Set<Integer> ambientResources;
	private Set<Integer> wasteResources;

	/** How long does it take to complet ethe process*/
	private int processTime = 100;

	/** The work time required to toggle this process on or off. */
	private int workTime = 10;


	protected ProcessSpec(String name, double powerRequired, int processTime, int workTime) {
		this.name = name;
		this.maxInputRates = new HashMap<>();
		this.maxOutputRates = new HashMap<>();
		this.wasteResources = new HashSet<>();
		this.ambientResources = new HashSet<>();
		this.powerRequired = powerRequired;
		this.processTime = processTime;
		this.workTime = workTime;
	}

	
	/**
	 * Adds a maximum input resource rate if it doesn't already exist.
	 *
	 * @param resource the amount resource.
	 * @param rate     max input resource rate (kg/millisol)
	 * @param ambient  is resource from available from surroundings? (air)
	 */
	public void addMaxInputResourceRate(Integer resource, double rate, boolean ambient) {
		if (ambient) {
			ambientResources.add(resource);
		} 

		maxInputRates.put(resource, rate);
	}

	/**
	 * Adds a maximum output resource rate if it doesn't already exist.
	 *
	 * @param resource the amount resource.
	 * @param rate     max output resource rate (kg/millisol)
	 * @param waste    is resource waste material not to be stored?
	 */
	public void addMaxOutputResourceRate(Integer resource, double rate, boolean waste) {
		if (waste) {
			wasteResources.add(resource);
		}

		maxOutputRates.put(resource, rate);
	}

	public String getName() {
		return name;
	}

	/**
	 * Get all inputs needed for this process
	 * @return
	 */
	public Set<Integer> getInputResources() {
		return maxInputRates.keySet();
	}

	/**
	 * Gets the max input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputRate(Integer resource) {
		return maxInputRates.get(resource);
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
	 * Get all output from this process.
	 * @return
	 */
	public Set<Integer> getOutputResources() {
		return maxOutputRates.keySet();
	}
	
	/**
	 * Gets the max output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputRate(Integer resource) {
		return maxOutputRates.get(resource);
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

	public double getPowerRequired() {
		return powerRequired;
	}

	/**
	 * TIme to complete one process
	 * @return Number of msol
	 */
	public int getProcessTime() {
		return processTime ;
	}

	/**
	 * Time needed to work on this process
	 * @return
	 */
	public int getWorkTime() {
		return workTime;
	}
}
