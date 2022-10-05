/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2022-10-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	private static UnitManager unitManager;

	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {
	}

	/**
	 * Create a new piece of Equipment. This may be temporary to be shared.
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
			// Store a pressure suit inside the EVA suit
//			((EVASuit)newEqm).storeItemResource(ItemResourceUtil.pressureSuitID, 1);
			break;

		case BAG:
		case BARREL:
		case GAS_CANISTER:
		case LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			break;
			
		case SPECIMEN_BOX:
		case THERMAL_BOTTLE:
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			break;
		default:
			throw new IllegalStateException("Equipment: " + type + " could not be constructed.");
		}

		unitManager.addUnit(newEqm);
		// Add this equipment as being owned by this settlement
		settlement.addEquipment(newEqm);
		// Set the container unit
		newEqm.setContainerUnit(settlement);

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
	 * @param type the equipment type string.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getEquipmentMass(EquipmentType type) {
		switch (type) {
		case BAG:
			return 0.1;
		case BARREL:
			return 1.3;
		case EVA_SUIT:
			return EVASuit.emptyMass;
		case GAS_CANISTER:
			return 4.0;
		case LARGE_BAG:
			return 0.2;
		case SPECIMEN_BOX:
			return 0.65;
		case THERMAL_BOTTLE:
			return 0.5;			
		default:
			throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
		}
	}

	/**
	 * Set up the default Unit Manager to use.
	 * @param mgr
	 */
	public static void initialise(UnitManager mgr) {
		unitManager = mgr;
	}
}
