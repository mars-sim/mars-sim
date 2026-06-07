/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import java.util.EnumMap;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	// Default mass for uncalculated types
	protected static final double DEFAULT_MASS = 0.0001;
	
	// Base masses (kg) aligned with current manufacturing input-mass/output-quantity calculations.
	private static final EnumMap<EquipmentType, Double> weights = new EnumMap<>(EquipmentType.class);
	static {
		weights.put(EquipmentType.EVA_SUIT, 16.18);
		weights.put(EquipmentType.BAG, 0.1);
		weights.put(EquipmentType.BARREL, 1.3);
		weights.put(EquipmentType.GAS_CANISTER, 4.4);
		weights.put(EquipmentType.LARGE_BAG, 0.2);
		weights.put(EquipmentType.SPECIMEN_BOX, 0.65);
		weights.put(EquipmentType.THERMAL_BOTTLE, 0.5);
		weights.put(EquipmentType.WHEELBARROW, 12.16);
	}
	
	private static UnitManager unitManager;

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

		case BAG, BARREL, GAS_CANISTER, LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			break;
			
		case SPECIMEN_BOX, THERMAL_BOTTLE, WHEELBARROW:			
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			break;
		}

		unitManager.addUnit(newEqm);

		// Add this equipment as being placed in this settlement
		settlement.addEquipment(newEqm);
		settlement.fireUnitUpdate(EntityEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, newEqm);
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
		return weights.getOrDefault(type, DEFAULT_MASS);
    }
    
	/**
	 * Sets up the default Unit Manager to use.
	 * 
	 * @param mgr
	 */
	public static void initialise(UnitManager mgr) {
		unitManager = mgr;
	}
}
