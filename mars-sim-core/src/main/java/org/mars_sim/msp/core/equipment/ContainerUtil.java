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
		return EquipmentType.getResourceID(getContainerClassToHoldResource(resourceID));
	}

	/**
	 * Gets the class of the type of container needed to hold a particular resource.
	 * 
	 * @param resourceID the id of the resource to hold.
	 * @return container class or null if none found.
	 */
	public static EquipmentType getContainerClassToHoldResource(int resourceID) {
		return getContainerTypeNeeded(ResourceUtil.findAmountResource(resourceID).getPhase());
	}

	/**
	 * Gets the container type needed for an amount resource phase.
	 * 
	 * @param phase the phase type of the amount resource.
	 * @return container class.
	 */
	public static EquipmentType getContainerTypeNeeded(PhaseType phase) {
		switch (phase) {
			case GAS:
				return EquipmentType.GAS_CANISTER;
			case LIQUID:
				return EquipmentType.BARREL;
			case SOLID:
				return EquipmentType.BAG;
			default:
				throw new IllegalArgumentException("Unknown phase type " + phase);
		}
	}

	/**
	 * Gets the capacity of the container.
	 * 
	 * @param containerClass the container class.
	 * @return capacity (kg).
	 */
	public static double getContainerCapacity(EquipmentType type) {
		switch(type) {
		case GAS_CANISTER:
			return 50D;
		case BARREL:
			return 50D;
		case BAG:
			return 50D;
		case LARGE_BAG:
			return 100D;
		case SPECIMEN_BOX:
			return 50D;	
		default:
			throw new IllegalArgumentException("Equipment type " + type + " is not a container");
		}
	}

	/**
	 * Gets the phase of amount resource that a container can hold.
	 * 
	 * @param type the container type.
	 * @return amount resource phase.
	 */
	public static PhaseType getContainerPhase(EquipmentType type) {
		switch(type) {
		case GAS_CANISTER:
			return PhaseType.GAS;
		case BARREL:
			return PhaseType.LIQUID;
		case BAG:
			return PhaseType.SOLID;
		case LARGE_BAG:
			return PhaseType.SOLID;
		case SPECIMEN_BOX:
			return PhaseType.SOLID;			
		default:
			throw new IllegalArgumentException("Equipment type " + type + " is not a container");
		}
	}
}
