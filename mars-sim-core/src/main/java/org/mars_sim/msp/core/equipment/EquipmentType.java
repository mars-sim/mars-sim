/**
 * Mars Simulation Project
 * EquipmentType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;


/**
 * The EquipmentType enum class is used for distinguishing between various type of equipments
 */
public enum EquipmentType {
	// Equipment - EVA gear
	EVA_SUIT			(Msg.getString("EquipmentType.EVASuit")), //$NON-NLS-1$ 
	// Container 
	BAG 				(Msg.getString("EquipmentType.bag")), //$NON-NLS-1$
	BARREL 				(Msg.getString("EquipmentType.barrel")), //$NON-NLS-1$
	GAS_CANISTER		(Msg.getString("EquipmentType.gasCanister")), //$NON-NLS-1$
	LARGE_BAG			(Msg.getString("EquipmentType.largeBag")), //$NON-NLS-1$
	SPECIMEN_BOX		(Msg.getString("EquipmentType.specimenBox")); //$NON-NLS-1$

	private String name;	

	private static final int FIRST_EQUIPMENT_RESOURCE_ID = ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
	private static Set<String> nameSet;
	
	private static Set<Integer> idSet;
	
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

	/**
	 * Gets a set of equipment names
	 * 
	 * @return
	 */
	public static Set<String> getNameSet() {
		if (nameSet == null) {
			nameSet = new HashSet<String>();
			for (EquipmentType et : EquipmentType.values()) {
				nameSet.add(et.toString());
			}
		}
		return nameSet;
	}

	/**
	 * Gets a set of equipment ids
	 * 
	 * @return
	 */
	public static Set<Integer> getIDs() {
		if (idSet == null) {
			idSet = new HashSet<Integer>();
			for (EquipmentType e : EquipmentType.values()) {
				idSet.add(e.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID);
			}
		}
		return idSet;
	}
	
	/**
	 * Obtains the type id (not the ordinal id) of the equipment
	 * 
	 * @param name
	 * @return type id
	 */
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (EquipmentType e : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	/**
	 * Obtains the enum type of the equipment with its type id
	 * 
	 * @param typeID
	 * @return {@link EquipmentType}
	 */
	public static EquipmentType convertID2Type(int typeID) {
		return EquipmentType.values()[typeID - FIRST_EQUIPMENT_RESOURCE_ID];
	}
	
	/**
	 * Obtains the type id of the equipment class
	 * 
	 * @param equipmentClass
	 * @return type id
	 */
	public static int convertClass2ID(Class<?> equipmentClass) {
		if (Bag.class.equals(equipmentClass)) return BAG.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
		else if (Barrel.class.equals(equipmentClass)) return BARREL.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
		else if (EVASuit.class.equals(equipmentClass)) return EVA_SUIT.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
		else if (GasCanister.class.equals(equipmentClass)) return GAS_CANISTER.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
        else if (LargeBag.class.equals(equipmentClass)) return LARGE_BAG.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
		else if (SpecimenBox.class.equals(equipmentClass)) return SPECIMEN_BOX.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
		else return -1;
	}
	
	/**
	 * Obtains the enum type of the equipment class
	 * 
	 * @param equipmentClass
	 * @return {@link EquipmentType}
	 */
	public static EquipmentType convertClass2Type(Class<?> equipmentClass) {
		if (Bag.class.equals(equipmentClass)) return BAG;
		else if (Barrel.class.equals(equipmentClass)) return BARREL;
		else if (EVASuit.class.equals(equipmentClass)) return EVA_SUIT;
		else if (GasCanister.class.equals(equipmentClass)) return GAS_CANISTER;
        else if (LargeBag.class.equals(equipmentClass)) return LARGE_BAG;
		else if (SpecimenBox.class.equals(equipmentClass)) return SPECIMEN_BOX;
		else return null;
	}
	
	/**
	 * Obtains the enum type of the equipment with its name
	 * 
	 * @param name
	 * @return {@link EquipmentType}
	 */
	public static EquipmentType convertName2Enum(String name) {
		if (name != null) {
	    	for (EquipmentType et : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(et.name)) {
	    			return et;
	    		}
	    	}
		}
		return null;
	}
	
	/**
	 * COnvert an EquipmentType to the associated resourceID.
	 * This is not good and Equipment should be referenced by the EquipmentType enum everywhere.
	 * Needs revisiting
	 * @return
	 */
	public static int getResourceID(EquipmentType type) {
		return type.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	}
	
	public static int getSpecimentBoxID() {
		return SPECIMEN_BOX.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	}
	
	public static int getBagID() {
		return BAG.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	}
	
	public static int getEVASuitID() {
		return EVA_SUIT.ordinal() + FIRST_EQUIPMENT_RESOURCE_ID;
	}
}
