/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2021-08-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	// Cache maps.
	/** The equipment name set cache. */
	private static Set<String> equipmentNamesCache;

	private static UnitManager unitManager;
	
	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {
	}

	/**
	 * Gets a set of all equipment names.
	 * 
	 * @return set of equipment name strings.
	 */
	public static Set<String> getEquipmentNames() {

		if (equipmentNamesCache == null) {
			equipmentNamesCache = EquipmentType.getNameSet();
		}

		return equipmentNamesCache;

	}

	/**
	 * Create a new piece of Equipment. This may be temporary to be shared.
	 * @param type
	 * @param settlement
	 * @param temp
	 * @return
	 */
	public static synchronized Equipment createEquipment(EquipmentType type, Settlement settlement, boolean temp) {
		// Create the name upfront
		String newName = Equipment.generateName(type.getName());
		if (temp) {
			newName = "Temp " + newName;
		}

		Equipment newEqm = null;
		switch (type) {
		case BAG:
			newEqm =  new Bag(newName, settlement);
			break;
		case BARREL:
			newEqm =  new Barrel(newName, settlement);
			break;
		case EVA_SUIT:
			newEqm =  new EVASuit(newName, settlement);
			break;
		case GAS_CANISTER:
			newEqm =  new GasCanister(newName, settlement);
			break;
		case LARGE_BAG:
			newEqm =  new LargeBag(newName, settlement);
			break;
		case SPECIMEN_BOX:
			newEqm =  new SpecimenBox(newName, settlement);
			break;
		default:
			throw new IllegalStateException("Equipment: " + type + " could not be constructed.");
		}
		unitManager.addUnit(newEqm);
		
		// Stores this equipment into its settlement
		settlement.getInventory().storeUnit(newEqm);
		// Add this equipment as being owned by this settlement
		settlement.addOwnedEquipment(newEqm);

		return newEqm;
	}
	
	/**
	 * Gets an equipment instance from an equipment type string.
	 * 
	 * @param type     the equipment type string.
	 * @param location the location of the equipment.
	 * @param temp     is this equipment only temporary?
	 * @return {@link Equipment}
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment createEquipment(String type, Settlement settlement, boolean temp) {
		
		int id = EquipmentType.convertName2ID(type);
		
		Map<Integer, Equipment> e = settlement.getEquipmentTypeCache();
		
		if (temp && e.containsKey(id)) {
			Equipment eq = e.get(id);
			eq.clean();
			// since it's temporary, it doesn't matter if the location has been defined
			return eq;
		}
	
		// Create a new instance of the equipment		
		Equipment newEqm = createEquipment(EquipmentType.convertName2Enum(type), settlement, temp);
		if (temp) {
			e.put(id, newEqm);
		}

		return newEqm;
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
			return 0.1D;
		case BARREL:
			return 1D;
		case EVA_SUIT:
			return EVASuit.emptyMass;
		case GAS_CANISTER:
			return 2.0D;
		case LARGE_BAG:
			return 0.2;
		case SPECIMEN_BOX:
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
