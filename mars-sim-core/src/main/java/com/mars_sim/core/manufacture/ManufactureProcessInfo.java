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

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private int effortLevel = 2;
	
	
	public ManufactureProcessInfo(String name, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, double processTimeRequired, double powerRequired, List<ProcessItem> inputList,
			List<ProcessItem> outputList, int effortLevel) {
		super(name, description, techLevelRequired, skillLevelRequired, workTimeRequired, processTimeRequired,
				powerRequired, inputList, outputList);
		this.effortLevel = effortLevel;
	}

	/**
	 * Get teh effort level needed for this manu process
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
		double mass = 0;
		
		for (var item : getInputList()) {
			String name = item.getName();
	
			AmountResource ar = ResourceUtil.findAmountResource(name);
			if (ar != null) {
				double amt = item.getAmount();
				mass += amt;
//				System.out.println(name + " " + amt + " kg");
			}		
			else {
				ItemResource ir = ItemResourceUtil.findItemResource(name);
				if (ir != null) {
					double quantity = item.getAmount();
					double massPerItem = ir.getMassPerItem();
					mass += quantity * massPerItem;
//					System.out.println(name + " x" + quantity + " (" + massPerItem + " kg each)");
				}
//				else {
//					BinType type = BinType.convertName2Enum(name);
//					if (type != null) {
//						double quantity = item.getAmount();
//						// Will need to find getMassPerBin
//						mass += quantity * 2;
//					}
//				}
			}
		}
//		System.out.println("Total: " + mass);
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
