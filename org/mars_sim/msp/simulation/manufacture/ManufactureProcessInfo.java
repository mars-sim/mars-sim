/**
 * Mars Simulation Project
 * ManufactureProcessInfo.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

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
	private List<ManufactureProcessItem> inputList;
	private List<ManufactureProcessItem> outputList;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getTechLevelRequired() {
		return techLevelRequired;
	}
	
	public void setTechLevelRequired(int techLevelRequired) {
		this.techLevelRequired = techLevelRequired;
	}
	
	public int getSkillLevelRequired() {
		return skillLevelRequired;
	}
	
	public void setSkillLevelRequired(int skillLevelRequired) {
		this.skillLevelRequired = skillLevelRequired;
	}
	
	public double getWorkTimeRequired() {
		return workTimeRequired;
	}
	
	public void setWorkTimeRequired(double workTimeRequired) {
		this.workTimeRequired = workTimeRequired;
	}
	
	public double getProcessTimeRequired() {
		return processTimeRequired;
	}
	
	public void setProcessTimeRequired(double processTimeRequired) {
		this.processTimeRequired = processTimeRequired;
	}
	
	public List<ManufactureProcessItem> getInputList() {
		return inputList;
	}
	
	public void setInputList(List<ManufactureProcessItem> inputList) {
		this.inputList = inputList;
	}
	
	public List<ManufactureProcessItem> getOutputList() {
		return outputList;
	}
	
	public void setOutputList(List<ManufactureProcessItem> outputList) {
		this.outputList = outputList;
	}
	
	@Override
	public String toString() {
		return name;
	}
}