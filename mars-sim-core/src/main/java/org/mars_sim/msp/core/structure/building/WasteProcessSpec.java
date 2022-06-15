/*
 * Mars Simulation Project
 * WasteProcessSpec.java
 * @date 2022-06-15
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The WasteProcessSpec class represents the specification of handling a waste process 
 * of converting one set of resources to another. This object is shared amongst WasteProcess of the same type.
 * 
 * @see WasteProcess
 */
public class WasteProcessSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double powerRequired;
	private boolean defaultOn;
	private Map<Integer, Double> maxInputRates;
	private Map<Integer, Double> maxAmbientInputRates;
	private Map<Integer, Double> maxOutputRates;
	private Map<Integer, Double> maxWasteOutputRates;

	// Cache some aggregate values
	private Set<Integer> inputResources;
	private Set<Integer> outputResources;

	/** The minimum period of time in millisols the process must stay on or off. */
	private int togglePeriodicity = 200;

	/** The work time required to toggle this process on or off. */
	private int toggleDuration = 20;

	private int modules = 1;

	public WasteProcessSpec(String name, int modules, double powerRequired, boolean defaultOn) {
		this.name = name;
		this.modules = modules;
		this.maxInputRates = new HashMap<>();
		this.maxAmbientInputRates = new HashMap<>();
		this.maxOutputRates = new HashMap<>();
		this.maxWasteOutputRates = new HashMap<>();
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
	public void addMaxInputRate(Integer resource, double rate, boolean ambient) {
		if (ambient) {
			maxAmbientInputRates.put(resource, modules * rate);
		} else {
			maxInputRates.put(resource, modules * rate);
		}
	}

	/**
	 * Adds a maximum output resource rate if it doesn't already exist.
	 *
	 * @param resource the amount resource.
	 * @param rate     max output resource rate (kg/millisol)
	 * @param waste    is resource waste material not to be stored?
	 */
	public void addMaxOutputRate(Integer resource, double rate, boolean waste) {
		if (waste) {
			maxWasteOutputRates.put(resource, modules * rate);
		} else {
			maxOutputRates.put(resource, modules * rate);
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
			inputResources.addAll(maxInputRates.keySet());
			inputResources.addAll(maxAmbientInputRates.keySet());
		}
		return inputResources;
	}

	/**
	 * Get the max input rates of all non-ambient inputs
	 * @return
	 */
	public Map<Integer, Double> getMaxInputRates() {
		return maxInputRates;
	}

	/**
	 * Gets the max input resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxInputRate(Integer resource) {
		double result = 0D;
		if (maxInputRates.containsKey(resource))
			result = maxInputRates.get(resource);
		else if (maxAmbientInputRates.containsKey(resource))
			result = maxAmbientInputRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is an ambient input.
	 *
	 * @param resource the resource to check.
	 * @return true if ambient resource.
	 */
	public boolean isAmbientInputResource(Integer resource) {
		return maxAmbientInputRates.containsKey(resource);
	}


	/**
	 * Get all output from this process.
	 * @return
	 */
	public Set<Integer> getOutputResources() {
		if (outputResources == null) {
			outputResources = ConcurrentHashMap.newKeySet();
			outputResources.addAll(maxOutputRates.keySet());
			outputResources.addAll(maxWasteOutputRates.keySet());
		}
		return outputResources;
	}

	/**
	 * Get resource output rates of non-waste products
	 * @return
	 */
	public Map<Integer, Double> getMaxOutputRates() {
		return maxOutputRates;
	}


	/**
	 * Gets the max output resource rate for a given resource.
	 *
	 * @return rate in kg/millisol.
	 */
	public double getMaxOutputRate(Integer resource) {
		double result = 0D;
		if (maxOutputRates.containsKey(resource))
			result = maxOutputRates.get(resource);
		else if (maxWasteOutputRates.containsKey(resource))
			result = maxWasteOutputRates.get(resource);
		return result;
	}

	/**
	 * Checks if resource is a waste output.
	 *
	 * @param resource the resource to check.
	 * @return true if waste output.
	 */
	public boolean isWasteOutputResource(Integer resource) {
		return maxWasteOutputRates.containsKey(resource);
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
