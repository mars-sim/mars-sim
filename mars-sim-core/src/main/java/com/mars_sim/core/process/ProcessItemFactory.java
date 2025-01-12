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
import com.mars_sim.core.vehicle.VehicleType;

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
		int id = -1;
		switch(type) {
			case AMOUNT_RESOURCE:
				var v = ResourceUtil.findAmountResource(name);
				if (v  == null) {
					throw new IllegalArgumentException(name + " is not a known Resource");
				}
				id = v.getID();
				break;
			case PART:
				var p = ItemResourceUtil.findItemResource(name);
				if (p == null) {
					throw new IllegalArgumentException(name + " is not a known Part");	
				}
				id = p.getID();
				break;
			case BIN:
				if (BinType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " is not a known Bin");	
				}
				id = BinType.convertName2ID(name);
				break;
			case EQUIPMENT:
				if (EquipmentType.valueOf(ConfigHelper.convertToEnumName(name)) == null) {
					throw new IllegalArgumentException(name + " is not a known Equipment");	
				}
				id = EquipmentType.convertName2ID(name);
				break;
			case VEHICLE:
				id = VehicleType.convertName2ID(name);
				break;
			default:
		}

        return new ProcessItem(id, name, type, amount);
	}

}
