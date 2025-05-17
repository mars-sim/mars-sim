/*
 * Mars Simulation Project
 * ProcessInfo.java
 * @date 2024-02-25
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import java.io.Serializable;
import java.util.List;

import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;

/**
 * Information about a type of manufacturing process.
 */
public abstract class ProcessInfo implements Serializable , Comparable<ProcessInfo> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private int techLevelRequired;
	private int skillLevelRequired;
	
	private double workTimeRequired;
	private double processTimeRequired;
    private double powerRequired;
    
	private String name;
	private String description;
	
	private List<ProcessItem> inputList;
	private List<ProcessItem> outputList;
	

	protected ProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
						double workTimeRequired,
						double processTimeRequired, double powerRequired, 
						List<ProcessItem> inputList, List<ProcessItem> outputList) {
		this.techLevelRequired = techLevelRequired;
		this.skillLevelRequired = skillLevelRequired;
		this.workTimeRequired = workTimeRequired;
		this.processTimeRequired = processTimeRequired;
		this.powerRequired = powerRequired;
		this.name = name;
		this.description = description;
		this.inputList = inputList;
		this.outputList = outputList;
	}

	/**
	 * Gets the process name.
	 * 
	 * @return name.
	 */
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Gets the tech level required for the process.
	 * 
	 * @return tech level.
	 */
	public int getTechLevelRequired() {
		return techLevelRequired;
	}
	
	/**
	 * Gets the skill level required to work on the process.
	 * 
	 * @return skill level.
	 */
	public int getSkillLevelRequired() {
		return skillLevelRequired;
	}
	
	/**
	 * Gets the work time required to complete the process.
	 * 
	 * @return work time (millisols).
	 */
	public double getWorkTimeRequired() {
		return workTimeRequired;
	}
	
	/**
	 * Gets the process time required to complete the process.
	 * 
	 * @return process time (millisols).
	 */
	public double getProcessTimeRequired() {
		return processTimeRequired;
	}
    
	/**
	 * Gets the power required for the process.
	 * 
	 * @return power (kW hr).
	 */
    public double getPowerRequired() {
        return powerRequired;
    }
	
    /**
     * Gets a list of the input items required for the process.
     * 
     * @return input items.
     */
	public List<ProcessItem> getInputList() {
		return inputList;
	}
	
	/**
	 * Gets a list of the output items produced by the process.
	 * 
	 * @return output items.
	 */
	public List<ProcessItem> getOutputList() {
		return outputList;
	}

	/**
	 * Gets a list of Process having the given output resource name.
	 * 
	 * @param name
	 * @return
	 */
	public List<ProcessItem> getOutputItemsByName(String name) {
		return outputList.stream()
					.filter(i -> i.getName().equalsIgnoreCase(name))
					.toList();
	}

	/**
	 * Is the named resource one of the inputs.
	 * @param name
	 * @return
	 */
	public boolean isInput(String name) {
		for(var o : inputList) {
			if (o.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the named resource one of the outputs.
	 * @param name
	 * @return
	 */
	public boolean isOutput(String name) {
		for(var o : outputList) {
			if (o.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Does a Settlement have the required input resources for this process.
	 * @param source Source of the resources
	 * @return
	 */
	public boolean isResourcesAvailable(Settlement source) {
		for(ProcessItem item : inputList) {
			if (((ItemType.AMOUNT_RESOURCE == item.getType()) 
					&& (source.getAmountResourceStored(item.getId()) < item.getAmount()))
				|| ((ItemType.PART == item.getType())
					&& (source.getItemResourceStored(item.getId()) < (int) item.getAmount()))) {
				return false;
			}
		}

		// Checked everything
		return true;
	}

	/**
	 * Deposit the outputs of this process into the given settlement.
	 * @param settlement
	 * @param updateGoods Update the value of teh goods
	 */
    public void depositOutputs(Settlement settlement, boolean updateGoods) {
		for (var item : outputList) {
			item.deposit(settlement, this, true);
		}
    }

	/**
	 * Retrieve the required inputs from the given settlement.
	 * @param settlement
	 */
	public void retrieveInputs(Settlement settlement) {
		// Consume inputs.
		for (var item : inputList) {
			switch(item.getType()) {
				case AMOUNT_RESOURCE:
					settlement.retrieveAmountResource(item.getId(), item.getAmount());
					break;
				case PART:
					settlement.retrieveItemResource(item.getId(), (int) item.getAmount());
					break;
				default:
					throw new IllegalArgumentException("Process input: " + item.getType() + " not a valid type.");
			}
		}
	}
	
	/**
	 * Returns the used inputs to the given settlement.
	 * @param settlement
	 */
	public void returnInputs(Settlement settlement) {
			// Produce outputs.
		for (var item : inputList) {
			item.deposit(settlement, this, false);
		}
	}

	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than,
	 * equal to, or greater than the specified object.
	 */
	public int compareTo(ProcessInfo p) {
		return name.compareToIgnoreCase(p.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessInfo other = (ProcessInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
