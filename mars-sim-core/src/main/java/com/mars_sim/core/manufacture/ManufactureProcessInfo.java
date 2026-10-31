/*
 * Mars Simulation Project
 * ManufactureProcessInfo.java
 * @date 2025-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.process.ProcessItemFactory;
import com.mars_sim.core.resource.ItemType;

/**
 * Information about a type of manufacturing process.
 */
public class ManufactureProcessInfo extends WorkshopProcessInfo {

	/** Default serial id. */
	private static final long serialVersionUID = 1L;

	private int effortLevel = 2;	
	
	ManufactureProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, double processTimeRequired, double powerRequired, Tooling tool,
			List<ProcessItem> inputList, List<ProcessItem> outputList, int effortLevel) {
		super(name, description, techLevelRequired, skillLevelRequired, workTimeRequired, processTimeRequired,
				powerRequired, tool, inputList, outputList);
		this.effortLevel = effortLevel;
	}

	ManufactureProcessInfo(String altName, ManufactureProcessInfo source, List<ProcessItem> altInputs) {
		this(altName, source.getDescription(), source.getTechLevelRequired(),
				source.getSkillLevelRequired(), source.getWorkTimeRequired(), source.getProcessTimeRequired(),
				source.getPowerRequired(), source.getTooling(), altInputs, source.getOutputList(),
				source.effortLevel);
	}

	/**
	 * Gets the effort level needed for this manu process.
	 * 
	 * @return
	 */
	public int getEffortLevel() {
		return effortLevel;
	}
	

	/**
	 * Calculates the total input mass.
	 * 
	 * @return
	 */
	public double calculateTotalInputMass() {
				return getInputList().stream()
						.mapToDouble(ProcessItemFactory::getMass)
						.sum();
	}
	
	/**
	 * Calculates the quantity of a output product.
	 * 
	 * @return
	 */
	public double calculateOutputQuantity(String productName) {
		for (var item : getOutputList()) {
			String name = item.getName();
			if (productName.equalsIgnoreCase(name)) {
				return item.getAmount();
			}
		}
		
		return 1;
	}

	/**
	 * Get the part IDs of all input items that are parts.
	 * @return
	 */
    public Set<Integer> getInputPartIDs() {
		return getInputList().stream()
						.filter(p -> p.getType() == ItemType.PART)
						.map(ProcessItem::getId)
						.collect(Collectors.toSet());
    }
}
