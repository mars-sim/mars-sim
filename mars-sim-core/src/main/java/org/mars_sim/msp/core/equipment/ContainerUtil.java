/*
 * Mars Simulation Project
 * ContainerUtil.java
 * @date 2022-10-04
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
		case THERMAL_BOTTLE:
			return .5;		
		case WHEELBARROW:
			return 100;					
		default:
			throw new IllegalArgumentException("Equipment type " + type + " is not a container");
		}
	}

	/**
	 * Can a container type hold a certain Phase?
	 * 
	 * @param container the container type.
	 * @parma phase Type of material to be stored
	 * @return amount resource phase.
	 */
	public static boolean isPhaseSupported(EquipmentType container, PhaseType phase) {
		switch(container) {
		case GAS_CANISTER:
			return (PhaseType.GAS == phase);
		case BARREL:
			return (PhaseType.LIQUID == phase) || (PhaseType.SOLID == phase);
		case BAG:
			return (PhaseType.SOLID == phase);
		case LARGE_BAG:
			return (PhaseType.SOLID == phase);
		case SPECIMEN_BOX:
			return (PhaseType.SOLID == phase);	
		case THERMAL_BOTTLE:
			return (PhaseType.LIQUID == phase);	
		case WHEELBARROW:
			return (PhaseType.SOLID == phase);
		default:
			throw new IllegalArgumentException("Equipment type " + container + " is not a container");
		}
	}
	

	/**
	 * Gets the least full container..
	 * 
	 * @param owner	Source of containers to search
	 * @param containerType Preferred Type of container to look for
	 * @param resource  the resource for capacity.
	 * @return container.
	 */
	public static Container findLeastFullContainer(EquipmentOwner owner,
												   EquipmentType containerType,
												   int resource) {
		Container result = null;
		double mostCapacity = 0D;

		for(Equipment e : owner.getEquipmentSet()) {
			if (e.getEquipmentType() == containerType) {
				Container container = (Container) e;
				double remainingCapacity = container.getAmountResourceRemainingCapacity(resource);
				if (remainingCapacity >= mostCapacity) {
					result = container;
					mostCapacity = remainingCapacity;
				}
			}
		}

		return result;
	}
}
