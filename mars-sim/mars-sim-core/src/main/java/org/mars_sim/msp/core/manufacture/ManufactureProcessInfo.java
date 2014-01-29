/**
 * Mars Simulation Project
 * ManufactureProcessInfo.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;
import java.util.List;

/**
 * Information about a type of manufacturing process.
 */
public class ManufactureProcessInfo implements Serializable {

	// Data members
	private String name;
	private int techLevelRequired;
	private int skillLevelRequired;
	private double workTimeRequired;
	private double processTimeRequired;
    private double powerRequired;
	private List<ManufactureProcessItem> inputList;
	private List<ManufactureProcessItem> outputList;
	
	/**
	 * Gets the process name.
	 * @return name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the process name.
	 * @param name the name.
	 */
	public void setName(String name) {
		this.name = name;
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
	public List<ManufactureProcessItem> getInputList() {
		return inputList;
	}
	
	/**
	 * Sets the list of the input items required for the process.
	 * @param inputList the input items.
	 */
	public void setInputList(List<ManufactureProcessItem> inputList) {
		this.inputList = inputList;
	}
	
	/**
	 * Gets a list of the output items produced by the process.
	 * @return output items.
	 */
	public List<ManufactureProcessItem> getOutputList() {
		return outputList;
	}
	
	/**
	 * Sets the list of the output items produced by the process.
	 * @param outputList the output items.
	 */
	public void setOutputList(List<ManufactureProcessItem> outputList) {
		this.outputList = outputList;
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