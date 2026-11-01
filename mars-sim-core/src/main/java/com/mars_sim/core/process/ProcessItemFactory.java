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
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.vehicle.VehicleConfig;
import com.mars_sim.core.vehicle.VehicleSpec;

/**
 * Factory class to create ProcessItem objects
 */
public final class ProcessItemFactory {
    private static final SimLogger logger = SimLogger.getLogger(ProcessItemFactory.class);

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
    public static ProcessItem createByName(VehicleConfig vc, String name, ItemType type, double amount) {
		int id = switch(type) {
			case AMOUNT_RESOURCE:
				AmountResource ar = ResourceUtil.findAmountResource(name);
				if (ar  == null) {
					throw new IllegalArgumentException(name + " is not a known Resource");
				}
				yield ar.getID();
			case PART:
				var p = ItemResourceUtil.findItemResource(name);
				if (p == null) {
					throw new IllegalArgumentException(name + " is not a known Part");	
				}
				yield p.getID();
			case BIN:
				BinType.valueOf(ConfigHelper.convertToEnumName(name));
				yield BinType.convertName2ID(name);
			case EQUIPMENT:
				yield EquipmentType.valueOf(ConfigHelper.convertToEnumName(name)).getResourceID();
			case VEHICLE: {
					var spec = vc.getVehicleSpec(name);
					if (spec == null) {
						throw new IllegalArgumentException(name + " is not a known Vehicle Spec");
					}
					yield spec.getType().getResourceID();
				}
			case ROBOT: {
				// Robot uses the type-model naming convention
				String[] parts = name.split("-");
				if (parts.length != 2) {
					throw new IllegalArgumentException(name + " is not a valid Robot name");
				}
				yield RobotType.valueOf(parts[0].toUpperCase()).getResourceID();
			}
			default:
				yield -1; // Unknown type
		};

        return new ProcessItem(id, name, type, amount);
	}

	/**
	 * Get the associated VehicleSpec for a process item that represents a vehicle.
	 * @param item Item must be of type VEHICLE
	 * @return The associated VehicleSpec for the process item
	 */
	public static VehicleSpec getVehicleSpec(ProcessItem item) {
		if (item.getType() != ItemType.VEHICLE) {
			throw new IllegalArgumentException(item.getName() + " is not a vehicle");
		}

		var vc = SimulationConfig.instance().getVehicleConfiguration();
		var spec = vc.getVehicleSpec(item.getName());
		if (spec == null) {
			throw new IllegalStateException("Vehicle Spec not found for process item " + item.getName());
		}
		return spec;
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
			case VEHICLE -> getVehicleSpec(item).getEmptyMass();
			default -> throw new IllegalArgumentException(item.getName() + " is not a known type");
		};
	}
}
