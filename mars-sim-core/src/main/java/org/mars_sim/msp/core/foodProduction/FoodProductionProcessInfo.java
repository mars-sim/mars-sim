/**
 * Mars Simulation Project
 * FoodProductionProcessInfo.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.foodProduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;

/**
 * Information about a type of manufacturing process.
 */
public class FoodProductionProcessInfo implements Serializable , Comparable<FoodProductionProcessInfo> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private String description;
	private int techLevelRequired;
	private int skillLevelRequired;
	private double workTimeRequired;
	private double processTimeRequired;
    private double powerRequired;
	private List<FoodProductionProcessItem> inputList;
	private List<FoodProductionProcessItem> outputList;
	
	/**
	 * Gets the process name.
	 * @return name.
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the process name.
	 * @param name the name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the process description.
	 * @param description {@link String}
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the manufacturing tech level required for the process.
	 * @return tech level.
	 */
	public int getTechLevelRequired() {
		return techLevelRequired;
	}
	
	/**
	 * Sets the manufacturing tech level required for the process.
	 * @param techLevelRequired the required tech level.
	 */
	public void setTechLevelRequired(int techLevelRequired) {
		this.techLevelRequired = techLevelRequired;
	}
	
	/**
	 * Gets the material science skill level required to work on the process.
	 * @return skill level.
	 */
	public int getSkillLevelRequired() {
		return skillLevelRequired;
	}
	
	/**
	 * Sets the material science skill level required to work on the process.
	 * @param skillLevelRequired skill level.
	 */
	public void setSkillLevelRequired(int skillLevelRequired) {
		this.skillLevelRequired = skillLevelRequired;
	}
	
	/**
	 * Gets the work time required to complete the process.
	 * @return work time (millisols).
	 */
	public double getWorkTimeRequired() {
		return workTimeRequired;
	}
	
	/**
	 * Sets the work time required to complete the process.
	 * @param workTimeRequired work time (millisols).
	 */
	public void setWorkTimeRequired(double workTimeRequired) {
		this.workTimeRequired = workTimeRequired;
	}
	
	/**
	 * Gets the process time required to complete the process.
	 * @return process time (millisols).
	 */
	public double getProcessTimeRequired() {
		return processTimeRequired;
	}
	
	/**
	 * Sets the process time required to complete the process.
	 * @param processTimeRequired process time (millisols).
	 */
	public void setProcessTimeRequired(double processTimeRequired) {
		this.processTimeRequired = processTimeRequired;
	}
    
	/**
	 * Gets the power required for the process.
	 * @return power (kW hr).
	 */
    public double getPowerRequired() {
        return powerRequired;
    }
    
    /**
     * Sets the power required for the process.
     * @param powerRequired power (kW hr).
     */
    public void setPowerRequired(double powerRequired) {
        this.powerRequired = powerRequired;
    }
	
    /**
     * Gets a list of the input items required for the process.
     * @return input items.
     */
	public List<FoodProductionProcessItem> getInputList() {
		return inputList;
	}
	
	/**
	 * Sets the list of the input items required for the process.
	 * @param inputList the input items.
	 */
	public void setInputList(List<FoodProductionProcessItem> inputList) {
		this.inputList = inputList;
	}
	
	/**
	 * Gets a list of the output items produced by the process.
	 * @return output items.
	 */
	public List<FoodProductionProcessItem> getOutputList() {
		return outputList;
	}

	/**
	 * convenience method that gives back a list of
	 * strings of the output items' names.
	 * @return {@link List}<{@link String}>
	 */
	public List<String> getOutputNames() {
		List<String> list = new ArrayList<String>();
		for (FoodProductionProcessItem item : outputList) {
			list.add(item.getName());
		}
		return list;
	}

	/**
	 * Gets a list of FoodProductionProcessItem having the given output resource name
	 * 
	 * @param name
	 * @return
	 */
	public List<FoodProductionProcessItem> getFoodProductionProcessItem(String name) {
		List<FoodProductionProcessItem> list = new ArrayList<>();
		for (FoodProductionProcessItem item : outputList) {
			if (name.equalsIgnoreCase(item.getName()))
				list.add(item);
		}
		return list;
	}
	
	/**
	 * convenience method that gives back a list of
	 * strings of the input items' names.
	 * @return {@link List}<{@link String}>
	 */
	public List<String> getInputNames() {
		List<String> list = new ArrayList<String>();
		for (FoodProductionProcessItem item : inputList) {
			list.add(item.getName());
		}
		return list;
	}

	/**
	 * Sets the list of the output items produced by the process.
	 * @param outputList the output items.
	 */
	public void setOutputList(List<FoodProductionProcessItem> outputList) {
		this.outputList = outputList;
	}

	/**
	 * Compares this object with the specified object for order.
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than,
	 * equal to, or greater than the specified object.
	 */
	public int compareTo(FoodProductionProcessInfo p) {
		return name.compareToIgnoreCase(p.name);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
	    if (inputList != null) inputList.clear();
	    inputList = null;
	    if (outputList != null) outputList.clear();
	    outputList = null;
	}

	@Override
	public String toString() {
		return name;
	}
}