/**
 * Mars Simulation Project
 * ContainerUtil.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Phase;

/**
 * A utility class for containers.
 */
public final class ContainerUtil {

	private static final Coordinates coordinates = new Coordinates(0D, 0D);

	/**
	 * Private constructor for utility class.
	 */
	private ContainerUtil() {};

	/**
	 * Gets the type of container needed to hold a particular resource.
	 * @param resource the amount resource to hold.
	 * @return type of container or null if none found.
	 */
	public static Class<? extends Container> getContainerClassToHoldResource(
		AmountResource resource
	) {
		Phase phase = resource.getPhase();
		return getContainerTypeNeeded(phase);
	}

	/**
	 * Gets the container type needed for an amount resource phase.
	 * @param phase the amount resource phase.
	 * @return container class.
	 */
	public static Class<? extends Container> getContainerTypeNeeded(Phase phase) {
		Class<? extends Container> result = null;
		switch (phase) {
			case GAS : result = GasCanister.class; break;
			case LIQUID : result = Barrel.class; break;
			case SOLID : result = Bag.class;
		}
		return result;
	}

	/**
	 * Gets the capacity of the container.
	 * @param containerClass the container class.
	 * @return capacity (kg).
	 */
	public static double getContainerCapacity(Class<? extends Container> containerClass) {

		double result = 0D;

		Class<? extends Equipment> equipmentClass = (Class<? extends Equipment>) containerClass;

		Container container = (Container) EquipmentFactory.getEquipment(equipmentClass, coordinates, true);
		if (container != null) {
			result = container.getTotalCapacity();
		}

		return result;
	}
	
	/**
	 * Gets the phase of amount resource that a container can hold.
	 * @param containerClass the container class.
	 * @return amount resource phase.
	 */
	public static Phase getContainerPhase(Class<? extends Container> containerClass) {
	    
	    Phase result = null;
	    
	    Class<? extends Equipment> equipmentClass = (Class<? extends Equipment>) containerClass;
	    Container container = (Container) EquipmentFactory.getEquipment(equipmentClass, coordinates, true);
        if (container != null) {
            result = container.getContainingResourcePhase();
        }
	    
        return result;
	}
}