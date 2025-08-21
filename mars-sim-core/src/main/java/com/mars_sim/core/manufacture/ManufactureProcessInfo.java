/*
 * Mars Simulation Project
 * ManufactureProcessInfo.java
 * @date 2025-08-10
 * @author Scott Davis
 */

package com.mars_sim.core.manufacture;

import java.util.List;

import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Information about a type of manufacturing process.
 */
public class ManufactureProcessInfo extends WorkshopProcessInfo {

	/** Default serial id. */
	private static final long serialVersionUID = 1L;

    private static final SimLogger logger = SimLogger.getLogger(ManufactureProcessInfo.class.getName());

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
		double mass = 0;
		
		for (var item : getInputList()) {	
			
			if (ItemType.AMOUNT_RESOURCE == item.getType()) {	
				AmountResource ar = ResourceUtil.findAmountResource(item.getId());
				if (ar != null) {
					double amt = item.getAmount();
					mass += amt;
				}	
			}
			else if (ItemType.PART == item.getType()) {
				ItemResource ir = ItemResourceUtil.findItemResource(item.getId());
				if (ir != null) {
					double quantity = item.getAmount();
					double massPerItem = ir.getMassPerItem();
					mass += quantity * massPerItem;
				}
			}
			else if (ItemType.EQUIPMENT == item.getType()) {
				 String name = item.getName();
				 var equipmentType = EquipmentType.convertName2Enum(name);
				 double massPerItem = EquipmentFactory.getEquipmentMass(equipmentType);
				 int number = (int) item.getAmount();
				 mass += number * massPerItem;
			}
			else if (ItemType.BIN == item.getType()) {
				int number = (int) item.getAmount();
				double massPerItem = BinFactory.getBinMass(BinType.convertName2Enum(item.getName())) * number;
				mass += number * massPerItem;
			}
			
			else
				logger.severe(null, "In calculateTotalInputMass, " +
					item.getType() + " is not a valid type.");
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
