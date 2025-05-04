/*
 * Mars Simulation Project
 * ManufactureProcessInfo.java
 * @date 2022-07-26
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.List;

import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Information about a type of manufacturing process.
 */
public class ManufactureProcessInfo extends ProcessInfo {

	/** Default serial id. */
	private static final long serialVersionUID = 1L;

	private int effortLevel = 2;
	private String tooling;
	
	
	ManufactureProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, double processTimeRequired, double powerRequired, String powerTool,
			List<ProcessItem> inputList, List<ProcessItem> outputList, int effortLevel) {
		super(name, description, techLevelRequired, skillLevelRequired, workTimeRequired, processTimeRequired,
				powerRequired, inputList, outputList);
		this.effortLevel = effortLevel;
		this.tooling = powerTool;
	}

	ManufactureProcessInfo(String altName, ManufactureProcessInfo source, List<ProcessItem> altInputs) {
		this(altName, source.getDescription(), source.getTechLevelRequired(),
				source.getSkillLevelRequired(), source.getWorkTimeRequired(), source.getProcessTimeRequired(),
				source.getPowerRequired(), source.tooling, altInputs, source.getOutputList(),
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
	 * What tool is used for this process?
	 * @return
	 */
    public String getTooling() {
        return tooling;
    }

	/**
	 * Calculates the total input mass.
	 * 
	 * @return
	 */
	public double calculateTotalInputMass() {
		double mass = 0;
		
		for (var item : getInputList()) {	
			AmountResource ar = ResourceUtil.findAmountResource(item.getId());
			if (ar != null) {
				double amt = item.getAmount();
				mass += amt;
			}		
			else {
				ItemResource ir = ItemResourceUtil.findItemResource(item.getId());
				if (ir != null) {
					double quantity = item.getAmount();
					double massPerItem = ir.getMassPerItem();
					mass += quantity * massPerItem;
				}
			}
		}
		return mass;
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
}
