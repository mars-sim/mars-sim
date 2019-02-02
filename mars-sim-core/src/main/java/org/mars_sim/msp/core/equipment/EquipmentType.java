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
//	BUILDING_KIT		("Building Kit"),
	EVA_SUIT			(Msg.getString("EquipmentType.EVASuit")), //$NON-NLS-1$ 
	GAS_CANISTER		(Msg.getString("EquipmentType.gasCanister")), //$NON-NLS-1$
	LARGE_BAG			(Msg.getString("EquipmentType.largeBag")), //$NON-NLS-1$
//	ROBOT				("Robot"),	
	SPECIMEN_CONTAINER	(Msg.getString("EquipmentType.specimenBox")); //$NON-NLS-1$
	// TODO: should 3D printer be an equipment or an itemResource ?
	
	private String type;	

	private static Set<EquipmentType> equipmentSet;

	private static Set<String> equipmentTypeString;
	
	/** hidden constructor. */
	private EquipmentType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.type;
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
	
	public static int convertType2ID(String type) {
		if (type != null) {
	    	for (EquipmentType e : EquipmentType.values()) {
	    		if (type.equalsIgnoreCase(e.type)) {
	    			return e.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
	    		}
	    	}
		}
		return -1;
	}
	
	public static EquipmentType convertID2Type(int id) {
		return EquipmentType.values()[id - ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID];
	}
	
	public static int getEquipmentID(Class<?> equipmentClass) {
		if (Bag.class.equals(equipmentClass)) return BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (Barrel.class.equals(equipmentClass)) return BARREL.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
//		else if (BuildingKit.class.equals(equipmentClass)) return BUILDING_KIT.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (EVASuit.class.equals(equipmentClass)) return EVA_SUIT.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (GasCanister.class.equals(equipmentClass)) return GAS_CANISTER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
        else if (LargeBag.class.equals(equipmentClass)) return LARGE_BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
//        else if (Robot.class.equals(equipmentClass)) return ROBOT.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else if (SpecimenContainer.class.equals(equipmentClass)) return SPECIMEN_CONTAINER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;
		else return -1;
	}
	
	public static EquipmentType getType(String name) {
		if (name != null) {
	    	for (EquipmentType et : EquipmentType.values()) {
	    		if (name.equalsIgnoreCase(et.type)) {
	    			return et;
	    		}
	    	}
		}
		return null;
	}
	
}