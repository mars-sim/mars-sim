/*
 * Mars Simulation Project
 * ResourceProcessSpec.java
 * @date 2021-08-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.structure.building;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * The ResourceProcessSpec class represents the specification of a process of converting one set of
 * resources to another. This object is shared amongst ResourceProcess of the same type.
 * @see ResourceProcess
 */
public class ResourceProcessSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double powerRequired;
	private boolean defaultOn;
	private Map<Integer, Double> maxInputResourceRates;
	private Map<Integer, Double> maxAmbientInputResourceRates;
	private Map<Integer, Double> maxOutputResourceRates;
	private Map<Integer, Double> maxWasteOutputResourceRates;

	// Cache some aggregate values
	private Set<Integer> inputResources;
	private Set<Integer> outputResources;

	/** The minimum period of time in millisols the process must stay on or off. */
	private int togglePeriodicity = 200;

	/** The work time required to toggle this process on or off. */
	private int toggleDuration = 20;

	private int modules = 1;

	public ResourceProcessSpec(String name, int modules, double powerRequired, boolean defaultOn) {
		this.name = name;
		this.modules = modules;
		this.maxInputResourceRates = new HashMap<>();
		this.maxAmbientInputResourceRates = new HashMap<>();
		this.maxOutputResourceRates = new HashMap<>();
		this.maxWasteOutputResourceRates = new HashMap<>();
		this.defaultOn = defaultOn;
		this.powerRequired = powerRequired;
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
			maxAmbientInputResourceRates.put(resource, modules * rate);
		} else {
			maxInputResourceRates.put(resource, modules * rate);
		}
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
			maxWasteOutputResourceRates.put(resource, modules * rate);
		} else {
			maxOutputResourceRates.put(resource, modules * rate);
		}
	}

	public boolean getDefaultOn() {
		return defaultOn;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get all inputs needed for this process
	 * @return
	 */
	public Set<Integer> getInputResources() {
		if (inputResources == null) {
			inputResources = new HashSet<>();
			inputResources.addAll(maxInputResourceRates.keySet());
			inputResources.addAll(maxAmbientInputResourceRates.keySet());
		}
		return inputResources;
	}

	/**
	 * Get the max input rates of all non-ambient inputs
	 * @return
	 */
	public Map<Integer, Double> getMaxInputResourceRates() {
		return maxInputResourceRates;
	}

	/**
	 * Gets the max input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputResourceRate(Integer resource) {
		double result = 0D;
		if (maxInputResourceRates.containsKey(resource))
			result = maxInputResourceRates.get(resource);
		else if (maxAmbientInputResourceRates.containsKey(resource))
			result = maxAmbientInputResourceRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is an ambient input.
	 *
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return maxAmbientInputResourceRates.containsKey(resource);
	}


	/**
	 * Get all output from this process.
	 * @return
	 */
	public Set<Integer> getOutputResources() {
		if (outputResources == null) {
			outputResources = ConcurrentHashMap.newKeySet();
			outputResources.addAll(maxOutputResourceRates.keySet());
			outputResources.addAll(maxWasteOutputResourceRates.keySet());
		}
		return outputResources;
	}

	/**
	 * Get resource output rates of non-waste products
	 * @return
	 */
	public Map<Integer, Double> getMaxOutputResourceRates() {
		return maxOutputResourceRates;
	}


	/**
	 * Gets the max output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputResourceRate(Integer resource) {
		double result = 0D;
		if (maxOutputResourceRates.containsKey(resource))
			result = maxOutputResourceRates.get(resource);
		else if (maxWasteOutputResourceRates.containsKey(resource))
			result = maxWasteOutputResourceRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is a waste output.
	 *
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return maxWasteOutputResourceRates.containsKey(resource);
	}

	public double getPowerRequired() {
		return powerRequired;
	}

	/**
	 * Minimum period between toggle operations.
	 * @return Number of msol
	 */
	public int getTogglePeriodicity() {
		return togglePeriodicity ;
	}

	/**
	 * Set the minimum period between toggle operations.
	 * @param togglePeriodicity Msols
	 */
	public void setTogglePeriodicity(int togglePeriodicity) {
		this.togglePeriodicity = togglePeriodicity;
	}

	/**
	 * How long does the toggle operation take for this process type
	 * @return
	 */
	public int getToggleDuration() {
		return toggleDuration;
	}

	/**
	 * Set how long a toggle operation takes
	 * @param toggleDuration
	 */
	public void setToggleDuration(int toggleDuration) {
		this.toggleDuration = toggleDuration;
	}
}
