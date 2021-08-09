/**
 * Mars Simulation Project
 * ContainerUtil.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * A utility class for containers.
 */
public final class ContainerUtil {

	/**
	 * Private constructor for utility class.
	 */
	private ContainerUtil() {
	};

	/**
	 * Gets the id of the type of container needed to hold a particular resource.
	 * 
	 * @param resourceID the id of the resource to hold.
	 * @return container id.
	 */
	public static int getContainerClassIDToHoldResource(int resourceID) {
		if (resourceID < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			return getContainerID(ResourceUtil.findAmountResource(resourceID).getPhase());
		}	
		else {
			return getContainerID(PhaseType.SOLID);
		}
		// Note: can expand resource matching  
	}

	/**
	 * Gets the class of the type of container needed to hold a particular resource.
	 * 
	 * @param resourceID the id of the resource to hold.
	 * @return container class or null if none found.
	 */
	public static Class<? extends Equipment> getContainerClassToHoldResource(int resourceID) {
		return getContainerTypeNeeded(ResourceUtil.findAmountResource(resourceID).getPhase());
	}

	/**
	 * Gets the container id needed for an amount resource phase.
	 * 
	 * @param phase the phase type of the amount resource.
	 * @return container id.
	 */
	public static int getContainerID(PhaseType phase) {
		int result = -1;
		switch (phase) {
			case GAS:
				result = EquipmentType.GAS_CANISTER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;// str2int("Gas Canister");;
				break;
			case LIQUID:
				result = EquipmentType.BARREL.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;// .str2int("Barrel");
				break;
			case SOLID:
				result = EquipmentType.BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;// .str2int("Bag");
				break;
			// Note: ask for the mass and usage so that SpecimenBox and LargeBag can be picked.
		}
		return result;
	}

	/**
	 * Gets the container type needed for an amount resource phase.
	 * 
	 * @param phase the phase type of the amount resource.
	 * @return container class.
	 */
	public static Class<? extends Equipment> getContainerTypeNeeded(PhaseType phase) {
		Class<? extends Equipment> result = null;
		switch (phase) {
			case GAS:
				result = GasCanister.class;
				break;
			case LIQUID:
				result = Barrel.class;
				break;
			case SOLID:
				result = Bag.class;	
			//Note: ask for the mass and usage so that SpecimenBox and LargeBag can be picked.
		}
		return result;
	}

	/**
	 * Gets the capacity of the container.
	 * 
	 * @param containerClass the container class.
	 * @return capacity (kg).
	 */
	public static double getContainerCapacity(Class<? extends Equipment> containerClass) {

		if (containerClass == GasCanister.class)
			return GasCanister.CAPACITY;
		else if (containerClass == Barrel.class)
			return Barrel.CAPACITY;
		else if (containerClass == Bag.class)
			return Bag.CAPACITY;
		else if (containerClass == LargeBag.class)
			return LargeBag.CAPACITY;
		else if (containerClass == SpecimenBox.class)
			return SpecimenBox.CAPACITY;
		else
			return 0;
	}

	/**
	 * Gets the capacity of the container.
	 * 
	 * @param id the container id.
	 * @return capacity (kg).
	 */
	public static double getContainerCapacity(int id) {

		if (id == EquipmentType.GAS_CANISTER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return GasCanister.CAPACITY;
		else if (id == EquipmentType.BARREL.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return Barrel.CAPACITY;
		else if (id == EquipmentType.BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return Bag.CAPACITY;
		else if (id == EquipmentType.LARGE_BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return LargeBag.CAPACITY;
		else if (id == EquipmentType.SPECIMEN_BOX.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return SpecimenBox.CAPACITY;
		else
			return 0;
	}

	/**
	 * Gets the phase of amount resource that a container can hold.
	 * 
	 * @param containerClass the container class.
	 * @return amount resource phase.
	 */
	public static PhaseType getContainerPhase(Class<? extends Equipment> containerClass) {
		if (containerClass == GasCanister.class)
			return GasCanister.phaseType;
		else if (containerClass == Barrel.class)
			return Barrel.phaseType;
		else if (containerClass == Bag.class)
			return Bag.phaseType;
		else if (containerClass == LargeBag.class)
			return LargeBag.phaseType;
		else if (containerClass == SpecimenBox.class)
			return SpecimenBox.phaseType;
		else
			return null;
	}
}
