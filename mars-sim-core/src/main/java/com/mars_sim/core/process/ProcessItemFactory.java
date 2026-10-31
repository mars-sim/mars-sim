/*
 * Mars Simulation Project
 * ProcessItemFactory.java
 * @date 2024-03-01
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleFactory;
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
     */
    public static ProcessItem createByName(String name, ItemType type, double amount) {
		int id = -1;
		switch(type) {
			case AMOUNT_RESOURCE:
				AmountResource ar = ResourceUtil.findAmountResource(name);
				if (ar  == null) {
					throw new IllegalArgumentException(name + " is not a known Resource");
				}
				id = ar.getID();
				break;
			case PART:
				var p = ItemResourceUtil.findItemResource(name);
				if (p == null) {
					throw new IllegalArgumentException(name + " is not a known Part");	
				}
				id = p.getID();
				break;
			case BIN:
				BinType.valueOf(ConfigHelper.convertToEnumName(name));
				id = BinType.convertName2ID(name);
				break;
			case EQUIPMENT:
				id = EquipmentType.valueOf(ConfigHelper.convertToEnumName(name)).getResourceID();
				break;
			default:
		}

        return new ProcessItem(id, name, type, amount);
	}

	/**
     * Get the total mass for a process item that contributes to the process.

     * @param item The process item to get the mass for.
     * @return
     */
    public static double getMass(ProcessItem item) {
		return item.getAmount() * switch(item.getType()) {
			case AMOUNT_RESOURCE -> 1D;
			case PART -> ItemResourceUtil.findItemResource(item.getId()).getMassPerItem();
			case BIN -> BinFactory.getUsualMass(BinType.convertID2Type(item.getId()));
			case EQUIPMENT -> EquipmentFactory.getUsualMass(EquipmentType.convertID2Type(item.getId()));
			case VEHICLE -> {
				var vc = SimulationConfig.instance().getVehicleConfiguration();
				var spec = vc.getVehicleSpec(item.getName());
				if (spec == null) {
					yield VehicleFactory.getUsualMass(VehicleType.convertID2Type(item.getId()));
				}
				yield spec.getEmptyMass();
			}
			default -> throw new IllegalArgumentException(item.getName() + " is not a known type");
		};
	}
}
