/*
 * Mars Simulation Project
 * ProcessItemFactory.java
 * @date 2024-03-01
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Factory class to create ProcessItem objects
 */
public final class ProcessItemFactory {

    private ProcessItemFactory() {
        // Blocks instantiation
    }

    /**
     * Create a process item defined by it's name and type.
     * The appropriate will be checked according to the type.

     * @param name Name of the item
     * @param type Type of the item
     * @param amount The amount of the item
     * @return
     * @throws IllegalArgumentException if the item name or type is not recognized.
     */
    public static ProcessItem createByName(String name, ItemType type, double amount) {
		switch(type) {
			case AMOUNT_RESOURCE:
				if (ResourceUtil.findAmountResource(name) == null) {
					throw new IllegalArgumentException(name + " is not a known Resource");
				}
				break;
			case PART:
				if (ItemResourceUtil.findItemResource(name) == null) {
					throw new IllegalArgumentException(name + " is not a known Part");	
				}
				break;
			case BIN:
				if (BinType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " is not a known Bin");	
				}
				break;
			case EQUIPMENT:
				if (EquipmentType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " is not a known Equipment");	
				}
				break;
			case VEHICLE:
			default:
		}

        return new ProcessItem(name, type, amount);
	}

}
