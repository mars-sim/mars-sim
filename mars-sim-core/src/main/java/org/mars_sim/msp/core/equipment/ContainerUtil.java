/**
 * Mars Simulation Project
 * ContainerUtil.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * A utility class for containers.
 */
public final class ContainerUtil {

	public static final Coordinates tempCoordinates = new Coordinates(0D, 0D);

	/**
	 * Private constructor for utility class.
	 */
	private ContainerUtil() {
	};

	/**
	 * Gets the type of container needed to hold a particular resource.
	 * 
	 * @param resource the id of the resource to hold.
	 * @return container id.
	 */
	public static int getContainerClassIDToHoldResource(int id) {
		if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			return getContainerID(ResourceUtil.findAmountResource(id).getPhase());
		}	
		else {
			return getContainerID(PhaseType.SOLID);
		}
	}

	/**
	 * Gets the type of container needed to hold a particular resource.
	 * 
	 * @param resource the id of the resource to hold.
	 * @return container class or null if none found.
	 */
	public static Class<? extends Equipment> getContainerClassToHoldResource(int resource) {
		return getContainerTypeNeeded(ResourceUtil.findAmountResource(resource).getPhase());
	}

	/**
	 * Gets the container type needed for an amount resource phase.
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
		else
			return 0;

		// Note : not an inefficient way of finding the phase type of a container
//		double result = 0D;
////		Class<? extends Equipment> equipmentClass = (Class<? extends Equipment>) containerClass;
//		Container container = (Container) EquipmentFactory.createEquipment((Class<? extends Equipment>) containerClass, coordinates, true);
//		if (container != null) {
//			result = container.getTotalCapacity();
//		}
//
//		return result;
	}

	/**
	 * Gets the capacity of the container.
	 * 
	 * @param containerClass the container class.
	 * @return capacity (kg).
	 */
	public static double getContainerCapacity(int id) {

		if (id == EquipmentType.GAS_CANISTER.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return GasCanister.CAPACITY;
		else if (id == EquipmentType.BARREL.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return Barrel.CAPACITY;
		else if (id == EquipmentType.BAG.ordinal() + ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return Bag.CAPACITY;
		else
			return 0;

// Note : inefficient way of finding the total capacity of a container to create a container
//		double result = 0D;		
////		Class<? extends Equipment> u = EquipmentFactory.getEquipmentClass(EquipmentType.int2enum(id).getName());		
//		Container container = (Container) EquipmentFactory.createEquipment(
//				EquipmentFactory.getEquipmentClass(EquipmentType.int2enum(id).getName()),
//				coordinates, true);
//		if (container != null) {
//			result = container.getTotalCapacity();
//		}
//
//		return result;
	}

	/**
	 * Gets the phase of amount resource that a container can hold.
	 * 
	 * @param containerClass the container class.
	 * @return amount resource phase.
	 */
	public static PhaseType getContainerPhase(Class<? extends Equipment> containerClass) {

		PhaseType result = null;

		// Note : not an inefficient way of finding the phase type of a container
		Class<? extends Equipment> equipmentClass = (Class<? extends Equipment>) containerClass;
		Container container = (Container) EquipmentFactory.createEquipment(equipmentClass, tempCoordinates, true);
		if (container != null) {
			result = container.getContainingResourcePhase();
		}

		return result;
	}
}