/**
 * Mars Simulation Project
 * EquipmentType.java
 * @version 3.1.0 2017-09-04
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

	BAG 				(Msg.getString("EquipmentType.bag")), //$NON-NLS-1$
	BARREL 				(Msg.getString("EquipmentType.barrel")), //$NON-NLS-1$
	EVA_SUIT			(Msg.getString("EquipmentType.EVASuit")), //$NON-NLS-1$ 
	GAS_CANISTER		(Msg.getString("EquipmentType.gasCanister")), //$NON-NLS-1$
	LARGE_BAG			(Msg.getString("EquipmentType.largeBag")), //$NON-NLS-1$
	SPECIMEN_CONTAINER	(Msg.getString("EquipmentType.specimenBox")); //$NON-NLS-1$

	private String name;	

	private static Set<EquipmentType> enumSet;

	private static Set<String> nameSet;
	
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

	public static Set<String> getNameSet() {
		if (nameSet == null) {
			nameSet = new HashSet<String>();
			for (EquipmentType et : EquipmentType.values()) {
				nameSet.add(et.toString());
			}
		}
		return nameSet;
	}
	
	public static Set<EquipmentType> getEnumSet() {
		if (enumSet == null) {
			for (EquipmentType et : EquipmentType.values()) {
				enumSet.add(et);
			}
		}
		return enumSet;
	}
	
	public static int convertName2ID(String name) {
		if (name != null) {
	    	for (EquipmentType e : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(e.name)) {
	    			return e.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	public static EquipmentType convertOrdinalID2Enum(int ordinalID) {
		return EquipmentType.values()[ordinalID];
	}
	
	public static EquipmentType convertID2Enum(int resourceID) {
		return EquipmentType.values()[resourceID - ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID];
	}
	
	public static int convertClass2ID(Class<?> equipmentClass) {
		if (Bag.class.equals(equipmentClass)) return BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (Barrel.class.equals(equipmentClass)) return BARREL.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (EVASuit.class.equals(equipmentClass)) return EVA_SUIT.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (GasCanister.class.equals(equipmentClass)) return GAS_CANISTER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
        else if (LargeBag.class.equals(equipmentClass)) return LARGE_BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (SpecimenContainer.class.equals(equipmentClass)) return SPECIMEN_CONTAINER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else return -1;
	}
	
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
	
}