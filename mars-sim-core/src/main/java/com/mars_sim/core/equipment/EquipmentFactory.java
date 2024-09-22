/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EquipmentFactory.class.getName());

	// Default mass for uncalculated types
	protected static final double DEFAULT_MASS = 0.0001;
	
	private static Map<EquipmentType, Double> weights = new EnumMap<>(EquipmentType.class);
	
	private static UnitManager unitManager;
	private static ManufactureConfig manufactureConfig;

	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {
	}

	/**
	 * Creates a new piece of Equipment. This may be temporary to be shared.
	 * 
	 * @param type
	 * @param settlement
	 * @param temp
	 * @return
	 */
	public static synchronized Equipment createEquipment(EquipmentType type, Settlement settlement) {
		// Create the name upfront
		String newName = Equipment.generateName(type.getName());

		Equipment newEqm = null;
		switch (type) {
		case EVA_SUIT:
			newEqm = new EVASuit(newName, settlement);
			break;

		case BAG:
		case BARREL:
		case GAS_CANISTER:
		case LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			break;
			
		case SPECIMEN_BOX:
		case THERMAL_BOTTLE:
		case WHEELBARROW:			
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			break;
		}

		unitManager.addUnit(newEqm);
		// Add this equipment as being owned by this settlement
		settlement.addEquipment(newEqm);

		return newEqm;
	}

	/**
	 * Gets an equipment instance from an equipment type string.
	 *
	 * @param type     the equipment type string.
	 * @param location the location of the equipment.
	 * @return {@link Equipment}
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment createEquipment(String type, Settlement settlement) {
		// Create a new instance of the equipment
		return createEquipment(EquipmentType.convertName2Enum(type), settlement);
	}

	/**
	 * Gets the empty mass of the equipment.
	 *
	 * @param type the equipment type.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getEquipmentMass(EquipmentType type) {
		return weights.computeIfAbsent(type, t-> calculateMass(t));
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param processName
	 * @return
	 */
    private static double calculateMass(EquipmentType type) {
		
		// Note: it's haphazard to match the string of the manu process since it can change.
		// Will need to implement a better way in matching and looking for the manu process that assemblies the item of interest.
		String processName = switch(type) {
			case BAG -> "Manufacture bag";
			case BARREL -> "Make plastic barrel";
			case EVA_SUIT -> "Assemble EVA suit";
			case GAS_CANISTER -> "Make gas canister";
			case LARGE_BAG -> "Manufacture large bag";
			case SPECIMEN_BOX -> "Make plastic specimen box";
			case THERMAL_BOTTLE -> "Manufacture thermal bottle";
			case WHEELBARROW -> "Make wheelbarrow";
		};

	    Optional<ManufactureProcessInfo> found = manufactureConfig.getManufactureProcessList()
		 	.stream()
			.filter(f -> f.getName().equalsIgnoreCase(processName))
			.findFirst();
	
		if (found.isPresent()) {
			var manufactureProcessInfo = found.get();
			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
			var mass = manufactureProcessInfo.calculateTotalInputMass();
			// Calculate output quantity
			var quantity = manufactureProcessInfo.calculateOutputQuantity(type.getName());					
			// Save the key value pair onto the weights Map
			return mass/quantity;
		}

	    logger.severe("The process '" + processName + "' cannot be found.");
	    		
		return DEFAULT_MASS;
    }
    
	/**
	 * Sets up the default Unit Manager to use.
	 * 
	 * @param mgr
	 */
	public static void initialise(UnitManager mgr, ManufactureConfig mConfig) {
		unitManager = mgr;
		manufactureConfig = mConfig;
	}
}
