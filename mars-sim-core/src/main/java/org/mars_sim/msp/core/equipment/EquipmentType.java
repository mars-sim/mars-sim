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
	
//	private static final int STARTING_ID = 2000;
	
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
	
	public static int str2int(String name) {
		if (name != null) {
	    	for (EquipmentType e : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal();
	    		}
	    	}
		}
		
		return -1;
	}
	
	public static EquipmentType int2enum(int ordinal) {
		return EquipmentType.values()[ordinal];
	}
	
	public static int getEquipmentID(Class<?> equipmentClass) {
		if (Bag.class.equals(equipmentClass)) return BAG.ordinal();
		else if (Barrel.class.equals(equipmentClass)) return BARREL.ordinal();
		else if (BuildingKit.class.equals(equipmentClass)) return BUILDING_KIT.ordinal();
		else if (EVASuit.class.equals(equipmentClass)) return EVA_SUIT.ordinal();
		else if (GasCanister.class.equals(equipmentClass)) return GAS_CANISTER.ordinal();
        else if (LargeBag.class.equals(equipmentClass)) return LARGE_BAG.ordinal();
		else if (SpecimenContainer.class.equals(equipmentClass)) return SPECIMEN_CONTAINER.ordinal();
		else return -1;
	}
}