/**
 * Mars Simulation Project
 * EquipmentType.java
 * @version 3.1.0 2017-09-04
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.util.HashSet;
import java.util.Set;

/**
 * The EquipmentType enum class is used for distinguishing between various type of equipments
 */
public enum EquipmentType {

	BAG 				("Bag"),
	BARREL 				("Barrel"),
	BUILDING_KIT		("Building Kit"),
	EVA_SUIT			("EVA Suit"),
	GAS_CANISTER		("Gas Canister"),
	LARGE_BAG			("Large Bag"),
	ROBOT				("Robot"),	
	SPECIMEN_CONTAINER	("Specimen Box");
	// should 3D printer be an equipment or an itemResource ?
	
	
	private String name;	

	private static Set<EquipmentType> equipmentSet;

	private static Set<String> equipmentTypeString;
	
	/** hidden constructor. */
	private EquipmentType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static Set<String> getEquipmentTypeString() {
		if (equipmentTypeString == null) {
			equipmentTypeString = new HashSet<String>();
			for (EquipmentType et : EquipmentType.values()) {
				equipmentTypeString.add(et.toString());
			}
		}
		return equipmentTypeString;
	}
	
	public static Set<EquipmentType> getEquipmentTypeSet() {
		if (equipmentSet == null) {
			for (EquipmentType et : EquipmentType.values()) {
				equipmentSet.add(et);
			}
		}
		return equipmentSet;
	}
}